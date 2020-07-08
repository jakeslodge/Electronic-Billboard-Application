package ControlPanel;

import BillboardViewer.DisplayBillboard;
//import BillboardViewer.Server;
import ControlPanel.CGTemplate;
import TestTools.ScheduleDay;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Main implements ActionListener {

    private JFrame frame;
    private JPanel panel;
    private JButton createBB;
    private JButton listBB;
    private JButton scheduleBB;
    private JButton editUsers;
    private JButton calendar;
    private JButton editSchedule;
    private JButton logOut;

    /**
     * Creates a gui window to access the different features of the control panel
     */
    public Main() {
        frame = new JFrame();
        panel = new JPanel();
        frame.add(panel);

        createBB = new JButton("Create Billboard");
        createBB.addActionListener(this);
        panel.add(createBB);
        listBB = new JButton("List Billboards");
        listBB.addActionListener(this);
        panel.add(listBB);
        scheduleBB = new JButton("Schedule Billboards");
        scheduleBB.addActionListener(this);
        panel.add(scheduleBB);
        //edit
        editSchedule = new JButton("Edit Schedule");
        editSchedule.addActionListener(this);
        panel.add(editSchedule);


        editUsers = new JButton("Edit Users");
        editUsers.addActionListener(this);
        panel.add(editUsers);

        calendar = new JButton("View Week");
        calendar.addActionListener(this);
        panel.add(calendar);

        //creation of logout button
        logOut = new JButton("Logout");
        logOut.addActionListener(this);
        panel.add(logOut);

        /**
         * Sets size, centres and displays the Main menu
         */
        frame.setSize(700, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    /**
     * Check which button clicked, and then open the correct window
     * @param ac actionEvent to check which button clicked
     */
    @Override
    public void actionPerformed(ActionEvent ac) {
        //closes existing GUI and calls new login function
        if (ac.getSource()== logOut) {
            try {
                ServerConnection.Logout();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("logging you out");
            frame.setVisible(false);
            new Login();
        }
        if (ac.getSource() == createBB) {
            try {
                if (ServerConnection.CheckToken()) {
                    System.out.println("Create Billboard");
                    frame.setVisible(false);
                    new CreateBillboard(false);
                } else {
                    new Login();
                }
            } catch (IOException e) {
                System.out.println("Server connection failed");
            }

        } else if (ac.getSource() == listBB) {
            try {
                if (ServerConnection.CheckToken()) {
                    System.out.println("List Billboards");
                    frame.setVisible(false);
                    new ListBillboards();
                    //No logic yet
                } else {
                    System.out.println("Bad token");
                    new Login();
                }
            } catch (IOException e) {
                System.out.println("Server connection failed");
            }
        } else if (ac.getSource() == scheduleBB) {
            try {
                if (ServerConnection.CheckToken()) {
                    System.out.println("Schedule Billboards");
                    frame.setVisible(false);
                    new Schedule();
                } else {
                    System.out.println("Bad token");
                    new Login();
                }
            } catch (IOException e) {
                System.out.println("Server connection failed");
            }
        }
        else if (ac.getSource() == editSchedule) {
            try {
                if (ServerConnection.CheckToken()) {
                    System.out.println("edit schedule");
                    frame.setVisible(false);
                    new EditSchedule();
                } else {
                    System.out.println("Bad token");
                    new Login();
                }
            } catch (IOException e) {
                System.out.println("Server connection failed");
            }
        }
        else if (ac.getSource() == editUsers) {
            try {
                if (ServerConnection.CheckToken()) {
                    System.out.println("Edit Users");
                    frame.setVisible(false);
                    new EditUsers();

                    //No logic yet
                } else {
                    System.out.println("Bad token");
                    new Login();
                    //No logic yet
                }
            } catch (IOException e) {
                System.out.println("Server connection failed");
            }
        } else if (ac.getSource() == calendar)
        {
            try{
                if(ServerConnection.CheckToken())
                {
                    ServerConnection.EstablishConnection();
                    ServerConnection.SendToServer("calender");
                    ScheduleDay[] week = ServerConnection.ReceiveWeek();
                    System.out.println("calender");
                    ControlPanel.CGTemplate cal = new ControlPanel.CGTemplate(week);

                }else {
                    System.out.println("Bad token");
                    new Login();
                    //No logic yet
                }
            }catch (IOException e) {
                System.out.println("Server connection failed");
            }
        }
    }
}
