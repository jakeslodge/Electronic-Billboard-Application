package BillboardViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;


public class BillboardViewer implements MouseListener, KeyListener {

    private Color col;
    private String text;
    private String pic;
    private String info;
    private XMLParser parser;
    private JFrame frame;
    private JPanel panel;
    private BillboardHandler bh;
    private int screenWidth;
    private int screenHeight;
    private Boolean isPreview;

    /**
     * Create the window for the billboard to be displayed on
     * @param col background colour of the billboard
     * @param text message text of the billboard
     * @param pic picture on the billboard
     * @param info information text of the billboard
     * @param parser XML parser
     * @param isPreview is this being used to preview a billboard
     */
    public BillboardViewer(Color col, String text, String pic, String info, XMLParser parser, Boolean isPreview) {
        this.col = col;
        this.text = text;
        this.pic = pic;
        this.info = info;
        this.parser = parser;
        this.isPreview = isPreview;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = (int) screenSize.getWidth();
        screenHeight = (int) screenSize.getHeight();

        frame = new JFrame();
        panel = new JPanel();

        bh = new BillboardHandler();

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
        panel.setBackground(col);
        //frame.setVisible(true);
    }

    /**
     * Scale the image
     * @param scaleFactor how much to scale the image by, 2 is 1/2, 3 is 1/3
     * @return scaled image
     * @throws IOException
     */
    public JLabel scaledPic(int scaleFactor) throws IOException {
        BufferedImage image = null;
        if (parser.getIsURL()) {
            image = bh.urlPicture(pic);
        } else if (!parser.getIsURL()) {
            image = bh.dataPicture(pic);
        }
        return bh.scaleImage(image, scaleFactor);
    }

    /**
     * Set the frame to be visible
     */
    public void see() {
        frame.setVisible(true);
    }

    /**
     * Add the message
     * @param w width
     * @param h height
     * @return JLabel of the message
     */
    public JLabel addMessage(int w, int h) {
        JLabel label = bh.message(parser, text);
        label.setSize(w, h);
        Font f = bh.setFontSize(label);
        label.setFont(f);
        return label;
    }

    /**
     * Add the information
     * @param w width
     * @param h height
     * @return JTextArea of the information
     */
    public JTextArea addInfo(int w, int h) {
        JTextArea ta = bh.info(parser, info, col);
        Dimension sz = new Dimension(w,h);
        ta.setSize(sz);

        Font f = bh.setFontSize(ta);
        ta.setFont(f);
        return ta;
    }

    /**
     * Set the layout of the page
     */
    public void layout() {
        panel.setLayout(new GridBagLayout());
    }

    /**
     * Set the positioning components in the billboard
     * @param gbc gridbagconstraints
     * @param ta text area
     * @param top distance from the top of the page
     * @param left distance from the left of the page
     * @param bot distance from the bottom of the page
     * @param right distance from the right of the page
     */
    public void setPositioning(GridBagConstraints gbc, JComponent ta, int top, int left, int bot, int right) {
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(top, left, bot, right);
        gbc.weightx = 1;
        panel.add(ta, gbc);
    }

    // layoutMan = 0 -> null layoutmanager
    // layoutMan = 1 -> GridBagLayout

    /**
     * Add the picture
     * @return JLabel of the picture
     * @throws IOException
     */
    public JLabel addPic() throws IOException {
        BufferedImage picImg;
        if (parser.getIsURL()) {
            picImg = bh.urlPicture(pic);
        } else {
            picImg = bh.dataPicture(pic);
        }
        return new JLabel(new ImageIcon(picImg));
    }

    /**
     * manually set the positioning of the components
     * @param comp component
     * @param x x-coordinate
     * @param y y-coordinate
     * @param width width
     * @param height height
     */
    public void setManualPos(JComponent comp, int x, int y, int width, int height) {
        panel.setLayout(null);
        panel.add(comp);
        panel.setVisible(true);
        frame.setVisible(true);
        comp.setSize(width, height);
        //Font f = bh.setFontSize(comp);
        //comp.setFont(f);
        comp.setLocation(x,y);
        //panel.remove(comp);
        panel.add(comp);
    }

    /**
     * Gett the JFrame
     * @return frame
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * Get the JPanel
     * @return panel
     */
    public JPanel getPanel() {
        return panel;
    }

    /**
     * Check if escape key pressed, to then close the billboard. If its a preview, just hide it.
     * @param e KeyEvent to check if escape pressed
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_ESCAPE)) {
            if (isPreview) {
                frame.dispose();
                frame.setVisible(false);
            } else {
                System.exit(0);
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    /**
     * Check if the mouse has been clicked on the screen, to then close the billboard. If its a preview, just hide it.
     * @param e
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (isPreview) {
            frame.dispose();
            frame.setVisible(false);
        } else {
            System.exit(0);
        }
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


