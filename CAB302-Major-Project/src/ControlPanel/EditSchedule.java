package ControlPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class EditSchedule implements ActionListener {
    private JFrame frame;
    private JPanel panel;
    private DefaultListModel<String> model = new DefaultListModel<>();
    private JList<String> list = new JList<>(model);
    private JButton edit;
    private JButton delete;
    private String billboards;
    private String[] billboardList;
    private JButton homeButton;
    private boolean canAdd = false;
    private JLabel scheduledBillboards;

    /**
     * Creates gui window to show a list of all the billboards
     */
    public  EditSchedule() {
        frame = new JFrame("Edit Schedule");
        panel = new JPanel();
        panel.setLayout(null);

        try {
            if (ServerConnection.CheckToken()) {
                ServerConnection.EstablishConnection();
                ServerConnection.SendToServer("getScheduleList");
                billboards = ServerConnection.ReceiveData();
                if (!billboards.equals("NOTHING")) {
                    canAdd = true;
                }
                ServerConnection.CloseConnection();
            }
        } catch (IOException e) {
            System.out.println("Server connection failed");
        }
        if (canAdd) {
            billboardList = billboards.split("\\|");
            for (int i = 0; i < billboardList.length; i++) {
                model.addElement(billboardList[i]);
            }
        }

        scheduledBillboards = new JLabel("Scheduled billboards");
        scheduledBillboards.setSize(240, 20);
        scheduledBillboards.setLocation(385, 20);
        panel.add(scheduledBillboards);

        // Creates Delete button and positions it
        delete = new JButton("Delete");
        delete.setSize(100, 20);
        delete.setLocation(100, 60);
        delete.addActionListener(this);
        panel.add(delete);



        // Creates home button and positions it
        homeButton = new JButton("Home");
        homeButton.setSize(100, 20);
        homeButton.setLocation(550, 600);
        homeButton.addActionListener(this);
        panel.add(homeButton);

        //creates space to list billboards
        list.setSize(300, 500);
        list.setLocation(300, 50);
        panel.add(list);
        frame.add(panel);

        // Creates the GUI to house all the above buttons
        frame.setSize(700, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Check if a button was clicked and run code accordingly
     * @param ac actionEvent to check which button clicked
     */
    @Override
    public void actionPerformed(ActionEvent ac) {

        if (ac.getSource() == homeButton) {

            new Main();
            frame.setVisible(false);
        }
        String billboardName = (String) list.getSelectedValue();
         if (ac.getSource() == delete) {
            // remove item from the list
            //ListSelectionModel selmodel = list.getSelectionModel();
            //int index = selmodel.getMinSelectionIndex();
            //model.remove(index);
            String deleteBB = list.getSelectedValue();
            try {
                if (ServerConnection.CheckToken()) {
                    ServerConnection.EstablishConnection();
                    ServerConnection.SendToServer("deleteSchedule"+deleteBB);
                    //ServerConnection.SendToServer(deleteBB);
                    String res = ServerConnection.ReceiveData();
                    if (res.equals("DENIED")) {
                        JOptionPane.showMessageDialog(null, "You do not have permission to " +
                                "delete" + " from the schedule", "ERROR", JOptionPane.WARNING_MESSAGE);
                    }
                    if (res.equals("OK")) {
                        JOptionPane.showMessageDialog(null, "Billboard deleted from schedule",
                                "ok", JOptionPane.WARNING_MESSAGE);
                                //refreshes page to show changes to the schedule
                                frame.setVisible(false);
                                new EditSchedule();
                    }
                    ServerConnection.CloseConnection();
                }
            } catch (IOException e) {
                System.out.println("Server connection failed");
            }
        }
    }
}
