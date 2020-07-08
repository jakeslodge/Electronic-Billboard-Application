package ControlPanel;

//import BillboardViewer.Server;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ListBillboards implements ActionListener {

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

    /**
     * Creates gui window to show a list of all the billboards
     */
    public ListBillboards() {
        frame = new JFrame("List Billboards");
        panel = new JPanel();
        panel.setLayout(null);

        try {
            if (ServerConnection.CheckToken()) {
                ServerConnection.EstablishConnection();
                ServerConnection.SendToServer("getBillboardList");
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

        edit = new JButton("Edit billboard");
        edit.setSize(120, 20);
        edit.setLocation(50, 35);
        edit.addActionListener(this);
        panel.add(edit);

        delete = new JButton("Delete billboard");
        delete.setSize(130, 20);
        delete.setLocation(180, 35);
        delete.addActionListener(this);
        panel.add(delete);

        //heading for the list of billboards
        JLabel existingBillboardNameLabel = new JLabel("Existing Billboards");
        existingBillboardNameLabel.setSize(110, 20);
        existingBillboardNameLabel.setLocation(438, 35);
        panel.add(existingBillboardNameLabel);

        //setting position of the list of billboards
        list.setSize(300, 500);
        list.setLocation(350, 60);
        panel.add(list);


        homeButton = new JButton("Home");
        homeButton.setSize(100, 20);
        homeButton.setLocation(550, 600);
        homeButton.addActionListener(this);
        panel.add(homeButton);

        //displays list billboards GUI and centres its position
        frame.add(panel);
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
   //   String billboardName = (String) list.getSelectedValue();
        if (ac.getSource() == edit) {
            String billboard = "";

            try {
                ServerConnection.EstablishConnection();
                ServerConnection.SendToServer("getBillboardInfo");
                if(list.getSelectedValue() == null)
                {
                    JOptionPane.showMessageDialog(null, "Please select a billboard to edit", "ERROR",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                frame.setVisible(false);
                ServerConnection.SendToServer(list.getSelectedValue());

                //read the response from bit stream
                billboard = ServerConnection.ReceiveBitstream();
                //billboard = ServerConnection.ReceiveData();
                ServerConnection.CloseConnection();
            } catch (IOException e) {
                System.out.println("Server connection failed");
            }
            String bbName,backgroundColour, messageColour, infoColour, messageData, pictureData,pictureURL,infoData,author;
            String[] split = billboard.split("\\|");

            String[] blocks = new String[8];
            for (int x = 0;x<blocks.length;x++) {
                if (split[x].equals("BLANK")) {
                    blocks[x]="";
                }
                else {
                    blocks[x]=split[x];
                }
            }
            bbName = blocks[0];
            pictureData  = blocks[1];
            pictureURL = blocks[2];//2
            messageData = blocks[3];
            infoData = blocks[4];
            backgroundColour = blocks[5]; //5
            messageColour = blocks[6]; //6
            infoColour= blocks[7];//7

            CreateBillboard cb = new CreateBillboard(true);
            cb.setValues(bbName, backgroundColour, messageColour, messageData, pictureData, pictureURL, infoColour, infoData);
        } else if (ac.getSource() == delete) {
            // remove item from the list
            //ListSelectionModel selmodel = list.getSelectionModel();
            //int index = selmodel.getMinSelectionIndex();
            //model.remove(index);
            if(list.getSelectedValue() == null)
            {
                JOptionPane.showMessageDialog(null, "Please select a billboard to delete", "ERROR",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String deleteBB = ServerConnection.getUsername() + "|" + list.getSelectedValue();
            try {
                if (ServerConnection.CheckToken()) {
                    ServerConnection.EstablishConnection();
                    ServerConnection.SendToServer("deleteBillboard"+deleteBB);
                    //ServerConnection.SendToServer(deleteBB);
                    String res = ServerConnection.ReceiveData();
                    if (res.equals("ERROR")) {
                        JOptionPane.showMessageDialog(null, "Couldn't delete billboard it may be scheduled", "ERROR",
                                JOptionPane.WARNING_MESSAGE);
                    }
                    if (res.equals("OK")) {
                        JOptionPane.showMessageDialog(null, "Billboard deleted", " ",
                                JOptionPane.WARNING_MESSAGE);
                        //refreshes page to see changes
                        frame.setVisible(false);
                        new ListBillboards();
                    }
                    ServerConnection.CloseConnection();
                }
            } catch (IOException e) {
                //System.out.println("Server connection failed");
            }
        }
    }
}
