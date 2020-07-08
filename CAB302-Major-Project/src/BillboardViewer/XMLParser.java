package BillboardViewer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class XMLParser {

    private Document doc;
    private boolean isURL;

    /**
     * Create a parser to parse the received xml
     * @param xmlFile file the xml is stored in
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public XMLParser(String xmlFile) throws ParserConfigurationException, IOException, SAXException {
        File f = new File(xmlFile);
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
        doc = builder.parse(f);
        doc.getDocumentElement().normalize();
    }

    /**
     * Extract the billboard background colour
     * @return background colour
     */
    public String getBillboardBG() {
        NodeList billboardTag = doc.getElementsByTagName("billboard");
        Node billboardN = billboardTag.item(0);
        Element billboardE = (Element) billboardN;
        String bg = "";
        // if a background is supplied use it, otherwise use white
        if (!billboardE.getAttribute("background").equals("")) {
            bg = billboardE.getAttribute("background");
        } else {
            bg = "#FFFFFF"; // white
        }
        return bg;
    }

    /**
     * Extract the message colour
     * @return message colour
     */
    public String getMessageCol() {
        NodeList messageTag = doc.getElementsByTagName("message");
        Node messageN = messageTag.item(0);
        Element messageE = (Element) messageN;
        String messageCol = "";
        // if a message colour is supplied use it, otherwise use black
        if (messageE == null) {
            messageCol = "#000000"; // black
        } else if (!messageE.getAttribute("colour").equals("")) {
            messageCol = messageE.getAttribute("colour");
        } else {
            messageCol = "#000000"; // black
        }
        return messageCol;
    }

    /**
     * Extract the message text
     * @return message text
     */
    public String getMessageText() {
        NodeList messageTag = doc.getElementsByTagName("message");
        Node messageN = messageTag.item(0);
        Element messageE = (Element) messageN;
        String message = "";
        // if there's a message, return it, otherwise signal there's no text
        if (messageE == null) {
            message = "no text";
        } else {
            message = messageTag.item(0).getTextContent();
        }
        /*if (!messageTag.item(0).getTextContent().equals("")) {
            message = messageTag.item(0).getTextContent();
        }*/
        return message;
    }

    /**
     * Extract the picture
     * @return picture
     */
    public String getPicture() {
        NodeList pictureTag = doc.getElementsByTagName("picture");
        Node pictureN = pictureTag.item(0);
        Element pictureE = (Element) pictureN;
        String pic = "";
        if (pictureE == null) {
            pic = "no pic";
        } else if (!pictureE.getAttribute("url").equals("")) {
            isURL = true;
            pic = pictureE.getAttribute("url");
        } else if (!pictureE.getAttribute("data").equals("")) {
            isURL = false;
            pic = pictureE.getAttribute("data");
        } else {
            pic = "no pic";
        }
        return pic;
    }

    /**
     * Extract the information colour
     * @return information colour
     */
    public String getInfoCol() {
        NodeList infoTag = doc.getElementsByTagName("information");
        Node infoN = infoTag.item(0);
        Element infoE = (Element) infoN;
        String infoCol = "";
        // if no colour supplied use black
        if (infoE == null) {
            infoCol = "#000000"; // black
            System.out.println("made it here");
        } else if (!infoE.getAttribute("colour").equals("")) {
            infoCol = infoE.getAttribute("colour");
        } else {
            infoCol = "#000000"; // black
            System.out.println("made it here");
        }
        return infoCol;
    }

    /**
     * Extract the information text
     * @return information text
     */
    public String getInfoText() {
        NodeList infoTag = doc.getElementsByTagName("information");
        Node infoN = infoTag.item(0);
        Element infoE = (Element) infoN;
        String info = "";
        // if no info text, signal this
        if (infoE == null) {
            info = "no info";
        } else {
            info = infoTag.item(0).getTextContent();
        }
        /*if (!infoTag.item(0).getTextContent().equals("")) {
            info = infoTag.item(0).getTextContent();
        }*/
        return info;
    }

    /**
     * Is the picture a URL
     * @return is it a url
     */
    public boolean getIsURL() {
        return isURL;
    }

}