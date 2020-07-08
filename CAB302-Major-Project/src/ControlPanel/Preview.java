package ControlPanel;


import BillboardViewer.BillboardViewer;
import BillboardViewer.DisplayBillboard;
import BillboardViewer.XMLParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;

public class Preview {

    /**
     * Display a preview of the billboard being created
     */
    public Preview() {
        XMLParser parser = null;
        try {
            parser = new XMLParser("src/ControlPanel/preview.xml");
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        assert parser != null;
        String backgroundCol = parser.getBillboardBG();
        Color col = Color.decode(backgroundCol);

        String text = parser.getMessageText();
        String pic = parser.getPicture();
        String info = parser.getInfoText();

        BillboardViewer bv = new BillboardViewer(col, text, pic, info, parser, true);
        //bv.getFrame().setUndecorated(false);

        DisplayBillboard display = new DisplayBillboard();
        try {
            display.displayBillboard(col, text, pic, info, parser, bv);
        } catch (IOException e) {
            System.out.println("Error display preview of billboard");
        }
    }

}
