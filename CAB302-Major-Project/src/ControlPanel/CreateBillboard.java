package ControlPanel;

import BillboardViewer.XMLParser;
import jake.server.Server;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Properties;

public class CreateBillboard extends Component implements ActionListener {

    private JFrame frame;
    private JPanel panel;
    private JTextArea msgColour;
    private JTextArea msgText;
    private JTextArea url;
    private JButton fileChooserBtn;
    private File selectedFile;
    private String path;
    private String encodedFile = "";
    private JTextArea infoCol;
    private JTextArea infoText;
    private JButton create;
    private JTextArea backgroundCol;
    private JButton preview;
    private JTextArea bbName;
    private Socket client;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    private JFileChooser fileChooser;
    private JButton clear;
    private boolean isEdit;
    private JButton editBillboard;
    private JButton importBillboard;
    private JFileChooser importChooser;
    private JButton exportBillboard;
    private JFileChooser exportChooser;
    private JButton homeButton;
    private JPanel billboardCreated;

    /**
     * Creates gui window for user to be able to create and edit billboards
     *
     * @param isEdit is the user creating the object in order to edit a billboard
     */
    public CreateBillboard(boolean isEdit) {
        this.isEdit = isEdit;
        frame = new JFrame();
        if (isEdit) {
            frame.setTitle("Edit Billboard");
        } else {
            frame.setTitle("Create Billboard");
        }
        panel = new JPanel();
        panel.setLayout(null);

        JLabel billboardNameLabel = new JLabel("Billboard Name");
        billboardNameLabel.setSize(110, 20);
        billboardNameLabel.setLocation(10, 10);
        panel.add(billboardNameLabel);
        bbName = new JTextArea();
        bbName.setSize(150, 20);
        bbName.setLocation(120, 10);
        if (isEdit) {
            bbName.setEditable(false);
        }
        panel.add(bbName);

        JLabel backgroundColLabel = new JLabel("Billboard Colour");
        backgroundColLabel.setSize(110, 20);
        backgroundColLabel.setLocation(10, 60);
        panel.add(backgroundColLabel);
        backgroundCol = new JTextArea();
        backgroundCol.setSize(100, 20);
        backgroundCol.setLocation(120, 60);
        panel.add(backgroundCol);

        // message colour
        JLabel msgColLabel = new JLabel("Message Colour");
        msgColLabel.setSize(110, 20);
        msgColLabel.setLocation(10, 110);
        panel.add(msgColLabel);
        msgColour = new JTextArea(1, 10);
        msgColour.setSize(100, 20);
        msgColour.setLocation(120, 110);
        panel.add(msgColour);

        // message text
        JLabel msgTextLabel = new JLabel("Message Text");
        msgTextLabel.setSize(110, 20);
        msgTextLabel.setLocation(10, 160);
        panel.add(msgTextLabel);
        msgText = new JTextArea();
        msgText.setLineWrap(true);
        msgText.setSize(500, 60);
        msgText.setLocation(120, 160);
        panel.add(msgText);

        // url
        JLabel urlLabel = new JLabel("URL");
        urlLabel.setSize(130, 20);
        urlLabel.setLocation(10, 250);
        panel.add(urlLabel);
        url = new JTextArea();
        url.setSize(500, 20);
        url.setLocation(120, 250);
        panel.add(url);

        // picture data
        fileChooserBtn = new JButton("Choose picture");
        fileChooserBtn.setSize(120, 20);
        fileChooserBtn.setLocation(120, 300);
        fileChooserBtn.addActionListener(this);
        panel.add(fileChooserBtn);
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        clear = new JButton("Clear Picture");
        clear.setSize(110, 20);
        clear.setLocation(250, 300);
        clear.addActionListener(this);
        panel.add(clear);

        // info col
        JLabel infoColLabel = new JLabel("Info Colour");
        infoColLabel.setSize(110, 20);
        infoColLabel.setLocation(10, 350);
        panel.add(infoColLabel);
        infoCol = new JTextArea();
        infoCol.setSize(100, 20);
        infoCol.setLocation(120, 350);
        panel.add(infoCol);

        // info text
        JLabel infoTextLabel = new JLabel("Info Text");
        infoTextLabel.setSize(110, 20);
        infoTextLabel.setLocation(10, 400);
        panel.add(infoTextLabel);
        infoText = new JTextArea();
        infoText.setLineWrap(true);
        infoText.setSize(500, 60);
        infoText.setLocation(120, 400);
        panel.add(infoText);


        // button to create the billboard
        if (!isEdit) {
            create = new JButton("Create Billboard");
            create.setSize(150, 20);
            create.setLocation(222, 470);
            create.addActionListener(this);
            panel.add(create);
        } else {
            editBillboard = new JButton("Edit Billboard");
            editBillboard.setSize(150, 20);
            editBillboard.setLocation(222, 470);
            editBillboard.addActionListener(this);
            panel.add(editBillboard);
        }

        // button to preview currently entered billboard info
        preview = new JButton("Preview Billboard");
        preview.setSize(150, 20);
        preview.setLocation(72, 470);
        preview.addActionListener(this);
        panel.add(preview);

        importBillboard = new JButton("Import");
        importBillboard.setSize(150, 20);
        importBillboard.setLocation(372, 470);
        importBillboard.addActionListener(this);
        panel.add(importBillboard);
        importChooser = new JFileChooser();
        importChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        exportBillboard = new JButton("Export");
        exportBillboard.setSize(150, 20);
        exportBillboard.setLocation(522, 470);
        exportBillboard.addActionListener(this);
        panel.add(exportBillboard);
        exportChooser = new JFileChooser();
        exportChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        // home button
        homeButton = new JButton("Home");
        homeButton.setSize(100, 20);
        homeButton.setLocation(605, 600);
        homeButton.addActionListener(this);
        panel.add(homeButton);


        //frame size set here and centred
        frame.add(panel);
        frame.setSize(750, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    /**
     * Set the values of the text fields to the parameters supplied
     *
     * @param name            the name of the billboard
     * @param bgCol           the background colour of the billboard
     * @param msgCol          the colour of the message text of the billboard
     * @param messageText     the message text of the billboard
     * @param picData         the picture data of the billboard
     * @param urlPic          the url of the picture in the billboard
     * @param infoColour      the colour of the information text in the billboard
     * @param informationText the information text of the billboard
     */
    public void setValues(String name, String bgCol, String msgCol, String messageText, String picData,
                          String urlPic, String infoColour,
                          String informationText) {
        bbName.setText(name);
        backgroundCol.setText(bgCol);
        msgColour.setText(msgCol);
        msgText.setText(messageText);
        if (!url.equals("")) {
            url.setText(urlPic);
        }
        if (!picData.equals("")) {
            encodedFile = picData;
            File f = new File(picData);
            fileChooser.setCurrentDirectory(f);
        }
        infoCol.setText(infoColour);
        infoText.setText(informationText);
    }

    /**
     * Check which button on the form was pressed and run code accordingly
     *
     * @param ac actionEvent to check which button was clicked
     */
    @Override
    public void actionPerformed(ActionEvent ac) {

        if (ac.getSource() == fileChooserBtn) {
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                path = selectedFile.getAbsolutePath();
                //System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            }
            byte[] bytes = new byte[(int) selectedFile.length()];
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(selectedFile);
            } catch (FileNotFoundException e) {
                System.out.println("Couldn't create file input stream");
            }
            try {
                fis.read(bytes);
            } catch (IOException e) {
                System.out.println("Failed to read bytes");
            }
            encodedFile = Base64.getEncoder().encodeToString(bytes);
            System.out.println(encodedFile);

        } else if (ac.getSource() == homeButton) {

            new Main();
            frame.setVisible(false);

        } else if (ac.getSource() == create) {
            CreateXML c = new CreateXML(backgroundCol.getText(), msgColour.getText(), msgText.getText(),
                    url.getText(), encodedFile, infoCol.getText(), infoText.getText(), ServerConnection.getUsername());
            String xml = c.create();
            String billboardName = bbName.getText();
            xml = billboardName + "|" + xml;

            String received = "";

            try {
                if (ServerConnection.CheckToken()) {
                    ServerConnection.EstablishConnection();
                    // tell server we're creating a billboard & send the billboard data (xml)
                    byte[] b = xml.getBytes(Charset.forName("UTF-8"));
                    ServerConnection.SendToServer("createBillboard", b, b.length);
                    //ServerConnection.SendToServer(b.length);
                    //ServerConnection.SendToServer(b);

                    received = ServerConnection.ReceiveData();
                    System.out.println("server replied:" + received);// OK MEANS ALL GOOD, ERROR means that name is in use

                    if (received.equals("DENIED")) {
                        JOptionPane.showMessageDialog(null, "You do not have permission to create billboards", "DENIED",
                                JOptionPane.WARNING_MESSAGE);
                    }
                    // if the server responds with 'ERROR' -> couldn't create billboard
                    if (received.equals("ERROR")) {
                        JOptionPane.showMessageDialog(null, "Couldn't create billboard", "ERROR",
                                JOptionPane.WARNING_MESSAGE);
                    }
                    if (received.equals("OK")) {
                        JOptionPane.showMessageDialog(null, "Billboard Created", "SUCCESS",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    ServerConnection.CloseConnection();
                } else {
                    System.out.println("You dont have a valid token");
                }
            } catch (IOException e) {
                System.out.println("Server connection failed");
            }
        } else if (ac.getSource() == preview) {
            CreateXML c = new CreateXML(backgroundCol.getText(), msgColour.getText(), msgText.getText(),
                    url.getText(), encodedFile, infoCol.getText(), infoText.getText(), ServerConnection.getUsername());
            String xml = c.preview();
            System.out.println(xml);
            File test = new File("src/ControlPanel/preview.xml");
            try {
                FileWriter writer = new FileWriter("src/ControlPanel/preview.xml");
                writer.write(xml);
                writer.close();
            } catch (IOException e) {
                System.out.println("Couldn't write to file");
            }
            new Preview();
        } else if (ac.getSource() == clear) {
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            encodedFile = "";
        } else if (ac.getSource() == editBillboard) {
            CreateXML c = new CreateXML(backgroundCol.getText(), msgColour.getText(), msgText.getText(),
                    url.getText(), encodedFile, infoCol.getText(), infoText.getText(), ServerConnection.getUsername());
            String xml = c.create();
            String billboardName = bbName.getText();
            xml = billboardName + "|" + xml;

            try {
                if (ServerConnection.CheckToken()) {
                    ServerConnection.EstablishConnection();
                    byte[] b = xml.getBytes(Charset.forName("UTF-8"));
                    ServerConnection.SendToServer("editBillboard", b, b.length);
                    String received = ServerConnection.ReceiveData();
                    if (received.equals("ERROR")) {
                        JOptionPane.showMessageDialog(null, "Cannot edit billboard");
                    }
                    ServerConnection.CloseConnection();
                } else {
                    System.out.println("Invalid Token");
                }
            } catch (IOException e) {
                System.out.println("Server connection failed");
            }
        } else if (ac.getSource() == importBillboard) {
            int result = importChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = importChooser.getSelectedFile();
                path = selectedFile.getAbsolutePath();
                try {
                    XMLParser parser = new XMLParser(path);
                    String bg = parser.getBillboardBG();
                    String msgCol = parser.getMessageCol();
                    String messageText = parser.getMessageText();
                    String pic = parser.getPicture();
                    Boolean isURL = parser.getIsURL();
                    String infoColour = parser.getInfoCol();
                    String informationText = parser.getInfoText();

                    if (!informationText.equals("no info")) {
                        infoText.setText(informationText);
                    } else {
                        infoText.setText("");
                    }
                    if (!messageText.equals("no text")) {
                        msgText.setText(messageText);
                    } else {
                        msgText.setText("");
                    }
                    if (isURL) {
                        backgroundCol.setText(bg);
                        msgColour.setText(msgCol);
                        url.setText(pic);
                        encodedFile = "";
                        infoCol.setText(infoColour);
                    } else {
                        backgroundCol.setText(bg);
                        msgColour.setText(msgCol);
                        url.setText("");
                        encodedFile = pic;
                        infoCol.setText(infoColour);
                    }
                } catch (ParserConfigurationException | IOException | SAXException e) {
                    System.out.println("Failed to create XMLParser object");
                }
            }
        } else if (ac.getSource() == exportBillboard) {
            int result = exportChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = exportChooser.getSelectedFile();
                path = selectedFile.getAbsolutePath();
                try {
                    FileWriter f = new FileWriter(path);
                    CreateXML c = new CreateXML(backgroundCol.getText(), msgColour.getText(), msgText.getText(),
                            url.getText(), encodedFile, infoCol.getText(), infoText.getText(), ServerConnection.getUsername());
                    String xml = c.preview();
                    f.write(xml);
                    f.close();
                } catch (IOException e) {
                    System.out.println("Failed to write xml to file");
                }
            }
        }
    }
}