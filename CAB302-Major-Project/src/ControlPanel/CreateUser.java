package ControlPanel;

import jake.server.Server;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class CreateUser implements ActionListener {

    private JFrame frame;
    private JPanel panel;
    private JLabel usernameLabel;
    private JTextArea usernameTA;
    private JLabel passwordLabel;
    private JTextArea passwordTA;
    private JLabel permissionsLabel;
    private JCheckBox createBillboards;
    private JCheckBox editAllBillboards;
    private JCheckBox scheduleBillboards;
    private JCheckBox editUsers;
    private JButton create;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket client;
    private Boolean isEdit;
    private JButton homeButton;

    /**
     * Creates gui window for user to create another user, or to edit user permissions
     * @param isEdit is the user creating the object to edit user permissions or to create a user
     */
    public CreateUser(Boolean isEdit) {
        this.isEdit = isEdit;
        frame = new JFrame();
        if (isEdit) {
            frame.setTitle("Edit User");
        } else {
            frame.setTitle("Create User");
        }
        panel = new JPanel();
        panel.setLayout(null);

        usernameLabel = new JLabel("Username");
        usernameLabel.setSize(110, 20);
        usernameLabel.setLocation(10, 10);
        panel.add(usernameLabel);
        usernameTA = new JTextArea("");
        usernameTA.setSize(500, 20);
        usernameTA.setLocation(120, 10);
        panel.add(usernameTA);

        passwordLabel = new JLabel("Password");
        passwordLabel.setSize(110, 20);
        passwordLabel.setLocation(10, 60);
        panel.add(passwordLabel);
        passwordTA = new JTextArea("");
        passwordTA.setSize(500, 20);
        passwordTA.setLocation(120, 60);
        if (isEdit) {
            passwordTA.setEditable(false);
            usernameTA.setEditable(false);
        }
        panel.add(passwordTA);

        permissionsLabel = new JLabel("Permissions:");
        permissionsLabel.setSize(200, 20);
        permissionsLabel.setLocation(10, 110);
        panel.add(permissionsLabel);

        createBillboards = new JCheckBox("Create Billboards");
        createBillboards.setSize(200, 20);
        createBillboards.setLocation(30, 160);
        panel.add(createBillboards);
        editAllBillboards = new JCheckBox("Edit All Billboards");
        editAllBillboards.setSize(200, 20);
        editAllBillboards.setLocation(30, 210);
        panel.add(editAllBillboards);
        scheduleBillboards = new JCheckBox("Schedule Billboards");
        scheduleBillboards.setSize(200, 20);
        scheduleBillboards.setLocation(30, 260);
        panel.add(scheduleBillboards);
        editUsers = new JCheckBox("Edit Users");
        editUsers.setSize(200, 20);
        editUsers.setLocation(30, 310);
        panel.add(editUsers);
        //creates the submit button
        create = new JButton("Submit");
        create.setSize(150, 30);
        create.setLocation(275, 400);
        create.addActionListener(this);
        panel.add(create);
        //creates and centers frame then displays buttons created above
        frame.add(panel);
        frame.setSize(700,700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Set the values of the text fields
     * @param username username of the user
     * @param pass password of the user
     * @param createBB createBillboard permission value
     * @param editBB editBillboard permission value
     * @param schedule scheduleBillboard permission value
     * @param editUsrs editUsers permission value
     */
    public void setValues(String username, String pass, Boolean createBB, Boolean editBB, Boolean schedule, Boolean editUsrs) {
        usernameTA.setText(username);
        passwordTA.setText(pass);
        createBillboards.setSelected(createBB);
        editAllBillboards.setSelected(editBB);
        scheduleBillboards.setSelected(schedule);
        editUsers.setSelected(editUsrs);
    }

    /**
     * Check which button on the form was pressed and run code accordingly
     * @param ac actionEvent to check which button pressed
     */
    @Override
    public void actionPerformed(ActionEvent ac) {
        if (ac.getSource() == create && !isEdit) {

            //hash the password
            String hashedpass = "";
            try {
                Password pass = new Password();
                byte[] hash = pass.hash(passwordTA.getText());
                hashedpass = pass.toHexString(hash);
            }
            catch (NoSuchAlgorithmException e) {
                System.out.println("No such algorithm exists!");
            }

            // create string to send
            String user = usernameTA.getText() + "|" + hashedpass + "|" + getPerm(editUsers.isSelected()) + "|" +
                    getPerm(scheduleBillboards.isSelected()) + "|" + getPerm(createBillboards.isSelected()) + "|" +
                    getPerm(editAllBillboards.isSelected());
            // connection stuff
            try {
                if (ServerConnection.CheckToken()) {
                    ServerConnection.EstablishConnection();
                    System.out.println(user);
                    ServerConnection.SendToServer("createUser", user);
                    String res = ServerConnection.ReceiveData();
                    if (res.equals("ERROR")) {
                        JOptionPane.showMessageDialog(null, "That username is already taken"
                                , "ERROR", JOptionPane.WARNING_MESSAGE);
                    }
                    if (res.equals("NOUSERNAME")) {
                        JOptionPane.showMessageDialog(null, "No username given", "ERROR",
                                JOptionPane.WARNING_MESSAGE);
                                frame.setVisible(false);
                                //new EditUsers();
                    }
                    if (res.equals("DENIED")) {
                        JOptionPane.showMessageDialog(null, "You do not have permission to " +
                                        "create users", "DENIED", JOptionPane.WARNING_MESSAGE);
                        frame.setVisible(false);
                    }
                    if (res.equals("OK")) {
                        JOptionPane.showMessageDialog(null, "User Created", "User Manager",
                                JOptionPane.INFORMATION_MESSAGE);
                        //refreshes page when new user is created
                        frame.setVisible(false);
                        new EditUsers();

                    }
                    ServerConnection.CloseConnection();
                }
            } catch (IOException e) {
                System.out.println("Server connection failed");
            }
        } else if (ac.getSource() == create && isEdit) {
            String userPerms = ServerConnection.getUsername() + "|" + usernameTA.getText() + "|" +
                    getPerm(editUsers.isSelected()) + "|" + getPerm(scheduleBillboards.isSelected()) + "|" +
                    getPerm(createBillboards.isSelected()) + "|" + getPerm(editAllBillboards.isSelected());
            try {
                if (ServerConnection.CheckToken()) {
                    ServerConnection.EstablishConnection();
                    ServerConnection.SendToServer("updateUserPerms", userPerms);
                    ServerConnection.SendToServer(userPerms);
                    String received = ServerConnection.ReceiveData();

                    if (received.equals("DENIED")) {
                        JOptionPane.showMessageDialog(null, "You are not allowed to edit " +
                                        "permissions", "DENIED",
                                JOptionPane.WARNING_MESSAGE);
                        frame.setVisible(false);
                    }
                    if (received.equals("OK")) {
                        JOptionPane.showMessageDialog(null, "Permissions successfully updated"
                                , "Edit Users",
                                JOptionPane.INFORMATION_MESSAGE);
                                //closes frame after changes are made
                                frame.setVisible(false);
                                new EditUsers();
                    }

                    ServerConnection.CloseConnection();
                }
            } catch (IOException e) {
                //System.out.println("Server connection failed");
            }

        }
    }

    /**
     * Convert boolean value of permission to integer value used by the server and database
     * @param perm value of the permission
     * @return returns the integer value of permission
     */
    public static int getPerm(Boolean perm) {
        if (perm) {
            return 1;
        }
        return 0;
    }
}
