package ControlPanel;

import jake.server.Server;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class EditUsers implements ActionListener {

    private JFrame frame;
    private JPanel panel;
    private DefaultListModel<String> model = new DefaultListModel<>();
    private JList<String> list = new JList<>(model);
    private JButton add;
    private JButton edit;
    private String[] userList;
    private JButton changePass;
    private JButton delete;
    private JButton homeButton;
    private boolean canAdd = false;

    /**
     * Creates gui window to allow users to edit users
     */
    public EditUsers() {
        frame = new JFrame("Edit Users");
        panel = new JPanel();
        panel.setLayout(null);
        String users = "";
        try {
            ServerConnection.EstablishConnection();
            ServerConnection.SendToServer("getUserList");
            users = ServerConnection.ReceiveData();
            if (!users.equals("NOTHING")) {
                canAdd = true;
            }
            ServerConnection.CloseConnection();
        } catch (IOException e) {
            System.out.println("Server connection failed");
        }
        if (canAdd) {
            userList = users.split("\\|");
            //maximum number of user is 30
            String[] names = new String[30]; // server will send length -> no. of users
            // extract the usernames
            for (int i = 0; i < userList.length; i++) {
                String[] l = userList[i].split("~");
                names[i] = l[0];
            }
            // add names to list
            for (int i = 0; i < names.length; i++) {
                model.addElement(names[i]);
            }
        }

        edit = new JButton("Edit User");
        edit.addActionListener(this);
        edit.setSize(120, 20);
        edit.setLocation(50, 35);
        //edit.addActionListener(this);
        panel.add(edit);

        add = new JButton("Create User");
        add.setSize(130, 20);
        add.setLocation(180, 35);
        add.addActionListener(this);
        panel.add(add);

        changePass = new JButton("Change Password");
        changePass.setSize(150, 20);
        changePass.setLocation(320, 35);
        changePass.addActionListener(this);
        panel.add(changePass);

        delete = new JButton("Delete User");
        delete.setSize(150, 20);
        delete.setLocation(480, 35);
        delete.addActionListener(this);
        panel.add(delete);

        //heading for the list of user
        JLabel listExistingUsers = new JLabel("Existing Users");
        listExistingUsers.setSize(110, 20);
        listExistingUsers.setLocation(300, 75);
        panel.add(listExistingUsers);

        //creates space for list of users
        list.setSize(200, 450);
        list.setLocation(250, 100);
        panel.add(list);
        frame.add(panel);

        //creates main frame to display all buttons
        frame.setSize(700,700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // home button
        homeButton = new JButton("Home");
        homeButton.setSize(100, 20);
        homeButton.setLocation(550, 600);
        homeButton.addActionListener(this);
        panel.add(homeButton);
    }

    /**
     * Check which button on the form was pressed and run code accordingly
     * @param ac actionEvent to check which button clicked
     */
    @Override
    public void actionPerformed(ActionEvent ac) {
        if (ac.getSource() == edit) {
            String userName = list.getSelectedValue(); //(String)
            frame.setVisible(false);
            /*frame set to false so it can be updated to show new perms
            checks to see if user has selected a user to edit
             */
            if (userName == null) {

                new EditUsers();
                JOptionPane.showMessageDialog(null, "Please select a user",
                        "ERROR", JOptionPane.WARNING_MESSAGE);
                System.out.println("name:" + userName);
                frame.setVisible(false);
                return;
            }
            System.out.println("name:" + userName);
            int index = list.getSelectedIndex();
            String usr = userList[index];
            String[] perms = usr.split("~");
            Boolean editUsers = getPermAsBool(Integer.parseInt(perms[1]));
            Boolean schedule = getPermAsBool(Integer.parseInt(perms[2]));
            Boolean createBB = getPermAsBool(Integer.parseInt(perms[3]));
            Boolean editAll = getPermAsBool(Integer.parseInt(perms[perms.length - 1]));

            String pass = "";

            CreateUser cu = new CreateUser(true);
            cu.setValues(userName, pass, createBB, editAll, schedule,
                    editUsers);
        } else if (ac.getSource() == add) {
            CreateUser cu = new CreateUser(false);

        } else if (ac.getSource() == homeButton) {

            new Main();
            frame.setVisible(false);

        }  else if (ac.getSource() == changePass) {
            String userName = list.getSelectedValue(); //(String)
            frame.setVisible(false);
            /*frame set to false so it can be updated to show new perms
            checks to see if user has selected a user to edit
             */
            if (userName == null) {

                new EditUsers();
                JOptionPane.showMessageDialog(null, "Please select a user",
                        "ERROR", JOptionPane.WARNING_MESSAGE);
                System.out.println("name:" + userName);
                frame.setVisible(false);
                //new EditUsers();
                return;
            }
            ChangePassword cp = new ChangePassword(list.    getSelectedValue(),
                    ServerConnection.getUsername());

        } else if (ac.getSource() == delete) {
            String userToDelete = list.getSelectedValue() + "|" + ServerConnection.getUsername();
            try {
                ServerConnection.EstablishConnection();
                ServerConnection.SendToServer("deleteUser", userToDelete);
                //ServerConnection.SendToServer(userToDelete);
                String res = ServerConnection.ReceiveData();
                //warning for user that no user was selected
                String userName2 =  list.getSelectedValue();
                if (userName2 == null)
                {
                    JOptionPane.showMessageDialog(null, "Please select a user to delete",
                            "ERROR", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (res.equals("DENIED")) {
                    JOptionPane.showMessageDialog(null, "You do not have permission to delete" +
                                    " users", "ERROR", JOptionPane.WARNING_MESSAGE);
                            frame.setVisible(false);
                            new EditUsers();
                }
                if (res.equals("OK")) {
                    JOptionPane.showMessageDialog(null, "User Deleted", "Delete User",
                            JOptionPane.INFORMATION_MESSAGE);
                            //refreshes page when user is deleted
                            frame.setVisible(false);
                            new EditUsers();
                }
                if (res.equals("ERROR")) {
                    JOptionPane.showMessageDialog(null, "You are not allowed to delete yourself" +
                                    " silly", "Delete User", JOptionPane.INFORMATION_MESSAGE);
                }

                ServerConnection.CloseConnection();
            } catch (IOException e) {
                //System.out.println("Server connection failed");
            }
        }
    }

    /**
     * Convert the permission from the server to a boolean to be used to fill in the check boxes
     * @param perm value of a permission
     * @return boolean value of permission
     */
    public static Boolean getPermAsBool(int perm) {
        if (perm == 1) {
            return true;
        }
        return false;
    }


}
