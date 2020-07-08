package BillboardViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

public class DisplayBillboard {

    /**
     * Display all the components correctly on the billboard
     * @param col background colour of the billboard
     * @param text message text of the billboard
     * @param pic picture to put on the billboard
     * @param info information text of the billboard
     * @param parser XMLParser
     * @param bv BillboardViewer
     * @throws IOException
     */
    public void displayBillboard(Color col, String text, String pic, String info, XMLParser parser, BillboardViewer bv) throws IOException {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();

        // error
        if (text.equals("no text") && pic.equals("no pic") && info.equals("no info")) {
            System.out.println("Not supplied enough child nodes");
        }

        if (text.equals("no text") && !pic.equals("no pic") && info.equals("no info")) {
            JLabel image = bv.scaledPic(2);
            bv.getPanel().setLayout(new BorderLayout());
            assert image != null;
            bv.getPanel().add(image, BorderLayout.CENTER);
        } else if (!text.equals("no text") && pic.equals("no pic") && info.equals("no info")) {
            JLabel msgLabel = bv.addMessage(screenWidth, (int) (screenHeight));
            bv.getPanel().add(msgLabel);
            bv.getPanel().setVisible(true);
            bv.getFrame().setVisible(true);
            msgLabel.setSize(msgLabel.getWidth(), msgLabel.getHeight());
            Font msgFont = msgLabel.getFont();
            String msgText = msgLabel.getText();
            int msgStrWidth = msgLabel.getFontMetrics(msgFont).stringWidth(msgText);
            bv.getPanel().remove(msgLabel);
            bv.setManualPos(msgLabel, (screenWidth / 2) - (msgStrWidth/2), (int)(screenHeight/2)-(msgLabel.getHeight()/2), msgLabel.getWidth(), msgLabel.getHeight());
        } else if (text.equals("no text") && pic.equals("no pic") && !info.equals("no info")) {
            JTextArea ta = bv.addInfo((int) (screenWidth * 0.7), (int) (screenHeight * 0.5));
            bv.layout();
            GridBagConstraints gbc1 = new GridBagConstraints();
            bv.setPositioning(gbc1,ta, (int)(screenHeight*0.25), (int)(screenWidth*0.15), (int)(screenHeight*0.25), (int)(screenWidth*0.15));
        } else if (!text.equals("no text") && !pic.equals("no pic") && info.equals("no info")) {
            JLabel picLabel = bv.scaledPic(2);
            bv.getPanel().add(picLabel);
            bv.getPanel().setVisible(true);
            bv.getFrame().setVisible(true);
            picLabel.setSize(picLabel.getWidth(), picLabel.getHeight());
            bv.getPanel().remove(picLabel);
            bv.setManualPos(picLabel,(screenWidth/2) - (picLabel.getWidth()/2), (int)(screenHeight*0.66) - (picLabel.getHeight()/2), picLabel.getWidth(), picLabel.getHeight());
            JLabel msgLabel = bv.addMessage(screenWidth, screenHeight/3);
            Font msgFont = msgLabel.getFont();
            String msgText = msgLabel.getText();
            int msgStrWidth = msgLabel.getFontMetrics(msgFont).stringWidth(msgText);
            bv.setManualPos(msgLabel, (screenWidth/2)- msgStrWidth/2, (int)(screenHeight/3) -  msgLabel.getHeight(),
                    msgLabel.getWidth(), msgLabel.getHeight());

            /*JLabel msgLabel = bv.addMessage(screenWidth, screenHeight/3);
            bv.getPanel().add(msgLabel);
            bv.getPanel().setVisible(true);
            bv.getFrame().setVisible(true);
            msgLabel.setSize(msgLabel.getWidth(), msgLabel.getHeight());
            Font msgFont = msgLabel.getFont();
            String msgText = msgLabel.getText();
            int msgStrWidth = msgLabel.getFontMetrics(msgFont).stringWidth(msgText);
            bv.getPanel().remove(msgLabel);
            bv.setManualPos(msgLabel, (screenWidth/2)-(msgLabel.getWidth()/2), (int)(screenHeight*0.33)-msgFont.getSize(),
                    msgLabel.getWidth(), msgLabel.getHeight());

            JLabel picLabel = bv.scaledPic(2);
            bv.getPanel().add(picLabel);
            bv.getPanel().setVisible(true);
            bv.getFrame().setVisible(true);
            picLabel.setSize(picLabel.getWidth(), picLabel.getHeight());
            bv.getPanel().remove(picLabel);
            bv.setManualPos(picLabel, (screenWidth/2) - (picLabel.getWidth()/2), (int)(screenHeight*0.66) - (picLabel.getHeight()/2),
                    picLabel.getWidth(), picLabel.getHeight());*/
        } else if (!text.equals("no text") && pic.equals("no pic") && !info.equals("no info")) {
            JLabel msgLabel = bv.addMessage(screenWidth, screenHeight/2);
            bv.getPanel().add(msgLabel);
            bv.getPanel().setVisible(true);
            bv.getFrame().setVisible(true);
            msgLabel.setSize(msgLabel.getWidth(), msgLabel.getHeight());
            Font msgFont = msgLabel.getFont();
            String msgText = msgLabel.getText();
            int msgStrWidth = msgLabel.getFontMetrics(msgFont).stringWidth(msgText);
            bv.getPanel().remove(msgLabel);
            bv.setManualPos(msgLabel, (screenWidth / 2) - (msgStrWidth/2), (int)(screenHeight*0.25)-(msgLabel.getHeight()/2), msgLabel.getWidth(), msgLabel.getHeight());

            JTextArea ta = bv.addInfo((int) (screenWidth*0.7), (int) (screenHeight * 0.5));
            bv.getPanel().setVisible(true);
            bv.getFrame().setVisible(true);
            ta.setSize(ta.getWidth(), ta.getHeight());
            Font infoFont = ta.getFont();
            String infoText = ta.getText();
            int infoStrWidth = ta.getFontMetrics(infoFont).stringWidth(infoText)/countLines(ta);
            bv.getPanel().remove(ta);
            bv.setManualPos(ta, (int)((screenWidth/2) - (infoStrWidth*0.5)), (int)(screenHeight * 0.75) - (ta.getHeight()/2), ta.getWidth(), ta.getHeight());

        } else if (text.equals("no text") && !pic.equals("no pic") && !info.equals("no info")) {
            JLabel picLabel = bv.scaledPic(3);
            bv.layout();
            GridBagConstraints gbc1 = new GridBagConstraints();
            bv.setPositioning(gbc1, picLabel, (int)(screenHeight*0.33),0,(int)(screenHeight*0.66),0);
            JTextArea ta = bv.addInfo((int) (screenWidth * 0.7), (int) (screenHeight * 0.66));
            GridBagConstraints gbc2 = new GridBagConstraints();
            bv.setPositioning(gbc2, ta, (int)(screenHeight*0.66),(int)(screenWidth*0.15),(int)(screenHeight*0.33),(int)(screenWidth*0.15));
        } else {
            JLabel picLabel = bv.scaledPic(3);
            bv.layout();
            GridBagConstraints gbc1 = new GridBagConstraints();
            bv.setPositioning(gbc1, picLabel, 0,0,0,0);
            JLabel msgLabel = bv.addMessage(screenWidth, (int)(screenHeight * 0.25));
            Font msgFont = msgLabel.getFont();
            String msgText = msgLabel.getText();
            int stringWidth = msgLabel.getFontMetrics(msgFont).stringWidth(msgText);
            GridBagConstraints gbc2 = new GridBagConstraints();
            bv.setPositioning(gbc2, msgLabel, (int)(screenHeight*0.25)-msgFont.getSize()/2,(screenWidth/2)-stringWidth/2,(int)(screenHeight*0.75),0);
            JTextArea ta = bv.addInfo((int) (screenWidth * 0.7), (int) (screenHeight * 0.5) - (screenHeight / 3));
            GridBagConstraints gbc3 = new GridBagConstraints();
            bv.setPositioning(gbc3, ta, (int)(screenHeight*0.75),(int)(screenWidth*0.15),(int)(screenHeight*0.25),(int)(screenWidth*0.15));
        }
        bv.see();
    }

    /**
     * Count the number of lines in a JTextArea to size the font appropriately
     * @param ta JTextArea
     * @return number of lines
     */
    private static int countLines(JTextArea ta) {
        AttributedString text = new AttributedString(ta.getText());
        text.addAttribute(TextAttribute.FONT, ta.getFont());
        FontRenderContext frc = ta.getFontMetrics(ta.getFont()).getFontRenderContext();
        AttributedCharacterIterator act = text.getIterator();
        LineBreakMeasurer lb_measurer = new LineBreakMeasurer(act, frc);
        Insets textAreaInsets = ta.getInsets();
        float formatWidth = ta.getWidth() - textAreaInsets.left - textAreaInsets.right;
        lb_measurer.setPosition(act.getBeginIndex());

        int numLines = 0;
        while (lb_measurer.getPosition() < act.getEndIndex()) {
            lb_measurer.nextLayout(formatWidth);
            numLines++;
        }

        return numLines;
    }

}
