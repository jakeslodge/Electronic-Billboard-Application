package ControlPanel;

import jake.server.Server;

import javax.swing.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Schedule implements ActionListener {

    private JFrame frame;
    private JPanel panel;
    private JLabel billboardNameLabel;
    private JTextArea billboardNameTA;
    private JLabel startTimeLabel;
    private JTextArea startTimeTA;
    private JLabel durationLabel;
    private JTextArea durationTA;
    private JButton schedule;
    private Socket client;
    private DataInputStream dis;
    private DataOutputStream dos;
    private DefaultListModel<String> model = new DefaultListModel<>();
    private JList<String> list = new JList<>(model);
    private String[] billboardList;
    private String billboards;
    private JButton homeButton;
    private boolean canAdd = false;
    private JLabel frequencyLabel;
    private JRadioButton weekly;
    private JRadioButton monthly;
    private JRadioButton none;
    private JTextField minuteFrequency;
    private JRadioButton minutes;
    private JLabel availableBillboardLabel;
    private JLabel scheduleRulesLabel;
    private ButtonGroup g;
    private JLabel scheduleRulesLabel2;

    public static void main(String[] args) {
        Schedule s = new Schedule();
    }

    /**
     * Creates gui window for users to be able to schedule a billboard
     */
    public Schedule() {
        frame = new JFrame("Schedule Billboard");
        panel = new JPanel();
        panel.setLayout(null);
        list.setSize(300, 500);
        list.setLocation(380, 40);
        panel.add(list);

        // heading for list of available billboards
        availableBillboardLabel = new JLabel("Available Billboards");
        availableBillboardLabel.setSize(140, 20);
        availableBillboardLabel.setLocation(470, 10);
        panel.add(availableBillboardLabel);

        try {
            if (ServerConnection.CheckToken()) {
                ServerConnection.EstablishConnection();
                ServerConnection.SendToServer("schedulelist");
                billboards = ServerConnection.ReceiveData();
                if (!billboards.equals("NOTHING")) {
                    canAdd = true;
                }
                ServerConnection.CloseConnection();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (canAdd) {
            billboardList = billboards.split("\\|");
            for (int i = 0; i < billboardList.length; i++) {
                model.addElement(billboardList[i]);
            }
        }

        billboardNameLabel = new JLabel("Billboard Name");
        billboardNameLabel.setSize(110, 20);
        billboardNameLabel.setLocation(10, 10);
        panel.add(billboardNameLabel);
        billboardNameTA = new JTextArea();
        billboardNameTA.setSize(150, 20);
        billboardNameTA.setLocation(200, 10);
        billboardNameTA.setEditable(false);
        panel.add(billboardNameTA);

        startTimeLabel = new JLabel("Start Time dd-MM-yyyy HH:mm");
        startTimeLabel.setSize(250, 20);
        startTimeLabel.setLocation(10, 60);
        panel.add(startTimeLabel);
        startTimeTA = new JTextArea();
        startTimeTA.setSize(150, 20);
        startTimeTA.setLocation(200, 60);
        panel.add(startTimeTA);

        durationLabel = new JLabel("Duration in Minutes");
        durationLabel.setSize(110, 20);
        durationLabel.setLocation(10, 110);
        panel.add(durationLabel);
        durationTA = new JTextArea();
        durationTA.setSize(150, 20);
        durationTA.setLocation(200, 110);
        panel.add(durationTA);

        frequencyLabel = new JLabel("Frequency");
        frequencyLabel.setSize(110, 20);
        frequencyLabel.setLocation(10, 170);
        panel.add(frequencyLabel);

        g = new ButtonGroup();
//        minutes = new JRadioButton("Every 'x' minutes");
//        minutes.setSize(150, 20);
//        minutes.setLocation(40, 220);
//        g.add(minutes);
//        panel.add(minutes);
        weekly = new JRadioButton("Every day for a week");
        weekly.setSize(200, 20);
        weekly.setLocation(40, 270);
        weekly.setActionCommand("WEEK");
        g.add(weekly);
        panel.add(weekly);
//        monthly = new JRadioButton("Every hour");
//        monthly.setSize(110, 20);
//        monthly.setLocation(40, 320);
//        g.add(monthly);
//        panel.add(monthly);
        none = new JRadioButton("None");
        none.setSelected(true);
        none.setSize(110, 20);
        none.setLocation(40, 370);
        none.setActionCommand("NONE");
        g.add(none);
        panel.add(none);

//        minuteFrequency = new JTextField();
//        minuteFrequency.setSize(50, 20);
//        minuteFrequency.setLocation(200, 220);
//        panel.add(minuteFrequency);

        schedule = new JButton("Schedule");
        schedule.setSize(150, 20);
        schedule.setLocation(120, 400);
        schedule.addActionListener(this);
        panel.add(schedule);

        //scheduling rules user information
        scheduleRulesLabel = new JLabel("Please Note***  Calendar will only display billboards scheduled ");
        scheduleRulesLabel.setSize(640, 20);
        scheduleRulesLabel.setLocation(10, 550);
        panel.add(scheduleRulesLabel);
        scheduleRulesLabel2 = new JLabel("between 8am and 5pm and must be in 24hr time");
        scheduleRulesLabel2.setSize(640, 20);
        scheduleRulesLabel2.setLocation(50, 570);
        panel.add(scheduleRulesLabel2);

        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList theList = (JList) mouseEvent.getSource();
                if (mouseEvent.getClickCount() == 1) {
                    billboardNameTA.setText(list.getSelectedValue());
                }
            }
        };
        list.addMouseListener(mouseListener);

        // home button
        homeButton = new JButton("Home");
        homeButton.setSize(100, 20);
        homeButton.setLocation(550, 600);
        homeButton.addActionListener(this);
        panel.add(homeButton);

        //displays frame and centres it
        frame.add(panel);
        frame.setSize(700, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Check if schedule button clicked, so it can send the billboard schedule to the server
     * @param ac actionEvent to check if button clicked
     */
    @Override
    public void actionPerformed(ActionEvent ac) {
    //checks if home button is selected
        if (ac.getSource() == homeButton) {
            new Main();
            frame.setVisible(false);
        }
        if (ac.getSource() == schedule) {
            String billboardSchedule = billboardNameTA.getText() + "|" + startTimeTA.getText() + "|" + durationTA.getText() +"|"+g.getSelection().getActionCommand() ;
            try {
                if (ServerConnection.CheckToken()) {
                    ServerConnection.EstablishConnection();
                    ServerConnection.SendToServer("scheduleBillboard", billboardSchedule);
                    String response = ServerConnection.ReceiveData();
                    if (response.equals("DENIED")) {
                        JOptionPane.showMessageDialog(null, "You do not have permission to schedule billboards", "ERROR",
                                JOptionPane.WARNING_MESSAGE);
                    }
                    if (response.equals("NOSELECT")) {
                        JOptionPane.showMessageDialog(null, "You must select a billboard", "ERROR",
                                JOptionPane.WARNING_MESSAGE);
                    }
                    if (response.equals("OK")) {
                        JOptionPane.showMessageDialog(null, "billboard scheduled successfully", "Added",
                                JOptionPane.WARNING_MESSAGE);
                    }
                    if (response.equals("ERROR")) {
                        JOptionPane.showMessageDialog(null, "Start Time or Duration have been given incorrectly", "ERROR",
                                JOptionPane.WARNING_MESSAGE);
                    }
                    ServerConnection.CloseConnection();
                }
            } catch (IOException e) {
                System.out.println("Server connection failed");
            }
        }
    }
}
