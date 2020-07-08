package BillboardViewer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;

public class BillboardHandler implements MouseListener, KeyListener {

    /**
     * Displays this if RemindTask cannot connect to the server
     */
    public void errorBillboard() {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();

        // listen for escape key
        frame.addKeyListener(this);
        frame.setFocusable(true);
        frame.setFocusTraversalKeysEnabled(false);

        // listen for mouse click
        frame.addMouseListener(this);

        frame.add(panel);
        // make the jframe fill entire window
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);
        panel.setBackground(Color.WHITE);
        JLabel errorMsg = new JLabel("Error: Cannot connect to server");
        errorMsg.setFont (errorMsg.getFont ().deriveFont(64.0f));
        panel.add(errorMsg);
        frame.setVisible(true);
    }

    /**
     * Creates a message JLabel to add to the billboard
     * @param parser the XMLParser to get the message colour from the xml
     * @param text the message text
     * @return the message on a JLabel
     */
    public JLabel message(XMLParser parser, String text) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        String textCol = parser.getMessageCol();
        Color txtCol = Color.decode(textCol);
        JLabel message = new JLabel(text);
        message.setForeground(txtCol);
        return message;
    }

    /**
     * Creates a information JTextArea to add to the billboard
     * @param parser the XMLParser to get the information colour from the xml
     * @param info the information text
     * @param col billboard background colour
     * @return information on a JTextArea
     */
    public JTextArea info(XMLParser parser, String info, Color col) {
        String infoCol = parser.getInfoCol();
        JTextArea ta = new JTextArea(info);
        ta.setForeground(Color.decode(infoCol));
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBackground(col);
        return ta;
    }

    /**
     * Creates a picture from the picture data
     * @param pic picture data
     * @return the image
     * @throws IOException
     */
    public BufferedImage dataPicture(String pic) throws IOException {
        byte[] decoded = Base64.getDecoder().decode(pic);
        ByteArrayInputStream is = new ByteArrayInputStream(decoded);
        BufferedImage image = ImageIO.read(is);
        return image;
    }

    /**
     * Creates a picture from the url
     * @param pic url
     * @return the image
     * @throws IOException
     */
    public BufferedImage urlPicture(String pic) throws IOException {
        URL url = new URL(pic);
        BufferedImage image =  ImageIO.read(url.openStream());
        return image;
    }

    /**
     * Scales the image to the appropriate size
     * @param image image to be scaled
     * @return scaled image
     */
    public JLabel scaleImage(BufferedImage image, int scaleFactor) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenHeight = (int) screenSize.getHeight() / scaleFactor;
        int screenWidth = (int) screenSize.getWidth() / scaleFactor;

        Dimension imageSz = new Dimension(image.getWidth(), image.getHeight());
        Dimension bound = new Dimension(screenWidth, screenHeight);
        ScaledDimensions sd = new ScaledDimensions(imageSz, bound);
        Dimension scaled = sd.getScaledDimension();

        // scale the image
        Image img = image.getScaledInstance((int)scaled.getWidth(), (int)scaled.getHeight(), Image.SCALE_SMOOTH);
        return new JLabel(new ImageIcon(img));
    }

    /**
     * Sets font size of the message
     * @param label message JLabel
     * @return font to use
     */
    public Font setFontSize(JLabel label) {
        Font labelFont = label.getFont();
        String labelText = label.getText();
        int stringWidth = label.getFontMetrics(labelFont).stringWidth(labelText);
        int componentWidth = label.getWidth();
        double widthRatio = (double) componentWidth / (double) stringWidth;
        int newFontSize = (int) (labelFont.getSize() * widthRatio);
        int componentHeight = label.getHeight();
        int fontSizeToUse = Math.min(newFontSize, componentHeight);
        return new Font(labelFont.getName(), Font.PLAIN, fontSizeToUse);
    }

    /**
     * Sets font size of the information
     * @param ta information JTextArea
     * @return font to use
     */
    public Font setFontSize(JTextArea ta) {
        Font taFont = ta.getFont();
        String taText = ta.getText();
        int stringWidth = ta.getFontMetrics(taFont).stringWidth(taText);
        float c1 = (float) 2;
        float textSize = (float) (c1*Math.sqrt(( (float)ta.getWidth() * ta.getHeight()) / stringWidth));
        return new Font(taFont.getName(), Font.PLAIN, (int)textSize);
    }

    /**
     * Check if the escape key pressed, so it can close the billboard
     * @param e KeyEvent to check which key pressed
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_ESCAPE)) {
            System.exit(0);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    /**
     * Check if mouse clicked on the screen, which closes the billboard
     * @param e MouseEvent
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        System.exit(0);
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
