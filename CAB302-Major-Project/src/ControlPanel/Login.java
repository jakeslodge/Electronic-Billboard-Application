package ControlPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;

import BillboardViewer.*;

import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.getExtendedKeyCodeForChar;

public class Login implements ActionListener {

    private JFrame frame;
    private JPanel panel;
    private JLabel usernameLabel;
    private JTextField usernameField;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JButton login;
    private Password pass;
    private JLabel loginText;

    public static void main(String[] args) {
        Login l = new Login();
    }

    /**
     * Creates login window for users to log into the system
     */
    public Login() {
        frame = new JFrame();
        panel = new JPanel();
        frame.add(panel);
        usernameLabel = new JLabel("Username");
        panel.add(usernameLabel);
        usernameField = new JTextField("", 25);
        panel.add(usernameField);
        passwordLabel = new JLabel("Password");
        panel.add(passwordLabel);
        passwordField = new JPasswordField("", 25);
        panel.add(passwordField);
        login = new JButton("Login");
        panel.add(login);
        login.addActionListener(this);

        loginText = new JLabel("");
        panel.add(loginText);

        //displays and centres login GUI
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(350, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Checks if login button pressed and then hashes the password and sends to network for validation
     * @param ae actionEvent to check which button clicked
     */
    public void actionPerformed(ActionEvent ae) {

            if (ae.getSource() == login)  {
                {
                    String username = usernameField.getText();
                    String password = String.valueOf(passwordField.getPassword());

                    // hash the password for transmission
                    try {
                        // Hash the original pass
                        pass = new Password();
                        byte[] hash = pass.hash(password);
                        String hashedPass = pass.toHexString(hash);
                        //System.out.println("no salt:"+hashedPass);
                        // generate the salt
                        //byte[] saltByte = pass.generateSalt();
                        //String salt = pass.toHexString(saltByte);
                        // concatenate the hashed pass and salt
                        //String pass_salt = hashedPass + salt;
                        // hash this concatenation
                        //byte[] final_hash_byte = pass.hash(pass_salt);
                        //String final_hash = pass.toHexString(final_hash_byte);
                        //System.out.println(final_hash);
                        //Ben testing
                        if (ServerConnection.Login(username, hashedPass)) {
                            //Garbage for bens testing TODO: Remove when not needed at end of production
//                    LocalDateTime ldt = LocalDateTime.now();
//                    Duration dtion = Duration.ofSeconds(86400);
//                    ServerConnection.EstablishConnection();
//                    ServerConnection.SendToServer("TestString",ldt,dtion.toSeconds());
//                    ServerConnection.CloseConnection();
                            loginText.setText("Success! Welcome.");
                            frame.setVisible(false);
                            new Main();
                        }

                        loginText.setText("Bad username/password");
                    } catch (NoSuchAlgorithmException e) {
                        System.out.println("No such algorithm exists!");
                    } catch (IOException e) {
                        System.out.println("Failed to hash password");
                        e.printStackTrace();
                    }
                }
            }
        }

    }