package ControlPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class ChangePassword implements ActionListener {

    private JFrame frame;
    private JPanel panel;
    private JLabel usernameLabel;
    private JTextArea usernameTA;
    //private JLabel currentPassLabel;
    //private JTextArea currentPassTA;
    private JLabel newPassLabel;
    private JTextArea newPassTA;
    private JButton submit;
    private Socket client;
    private DataOutputStream dos;
    private DataInputStream dis;
    private String targetUsername;
    private String requestUsername;

    /**
     * Creates gui window to enable user to change their account's password
     * @param targetUsername the username of user whose password is to be changed
     * @param reqUsername username of user who is trying to change the password
     */
    public ChangePassword(String targetUsername, String reqUsername) {
        this.targetUsername = targetUsername;
        this.requestUsername = reqUsername;
        frame = new JFrame("Change Password");
        panel = new JPanel();
        panel.setLayout(null);

        usernameLabel = new JLabel("Username");
        usernameLabel.setSize(110, 20);
        usernameLabel.setLocation(10,10);
        panel.add(usernameLabel);
        usernameTA = new JTextArea(targetUsername);
        usernameTA.setSize(500, 20);
        usernameTA.setLocation(120, 10);
        usernameTA.setEditable(false);
        panel.add(usernameTA);

        /*currentPassLabel = new JLabel("Current Password");
        currentPassLabel.setSize(110, 20);
        currentPassLabel.setLocation(10, 60);
        panel.add(currentPassLabel);
        currentPassTA = new JTextArea(currentPass);
        currentPassTA.setSize(500, 20);
        currentPassTA.setLocation(120, 60);
        panel.add(currentPassTA); */

        newPassLabel = new JLabel("New Password");
        newPassLabel.setSize(110, 20);
        newPassLabel.setLocation(10, 60);
        panel.add(newPassLabel);
        newPassTA = new JTextArea();
        newPassTA.setSize(500, 20);
        newPassTA.setLocation(120, 60);
        panel.add(newPassTA);

        submit = new JButton("Submit");
        submit.addActionListener(this);
        submit.setSize(150, 30);
        submit.setLocation(275, 150);
        panel.add(submit);

        frame.add(panel);
        frame.setSize(700,700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Hashes the new password and sends it, along with the target and requester usernames, to the server
     * @param ac actionEvent to check which button clicked
     */
    @Override
    public void actionPerformed(ActionEvent ac) {
        if (ac.getSource() == submit) {
            Password pass = new Password();
            byte[] hash = new byte[0];
            try {
                hash = pass.hash(newPassTA.getText());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            String hashedPass = pass.toHexString(hash);
            String userPass = requestUsername + "|" + targetUsername + "|" + hashedPass;
            try {
                ServerConnection.EstablishConnection();
                ServerConnection.SendToServer("updatePassword", userPass);
                ServerConnection.SendToServer(userPass);
                String received = ServerConnection.ReceiveData();
                ServerConnection.CloseConnection();

                if (received.equals("DENIED")) {
                    JOptionPane.showMessageDialog(null, "Couldn't change password", "ERROR",
                            JOptionPane.WARNING_MESSAGE);
                }
                if (received.equals("OK")) {
                    JOptionPane.showMessageDialog(null, "Password Updated", "Edit Users",
                            JOptionPane.INFORMATION_MESSAGE);
                    frame.setVisible(false);
                    new EditUsers();
                }
                //if all else fails close the window
                else {
                    frame.setVisible(false);
                }

                //ServerConnection.CloseConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
