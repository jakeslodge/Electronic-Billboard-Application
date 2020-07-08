package ControlPanel;

public class CreateXML {

    private String backgroundCol;
    private String msgColour;
    private String msgText;
    private String url;
    private String encodedFile;
    private String infoCol;
    private String infoText;
    private String author;

    /**
     * Creates the xml to preview the billboard or send it to the server
     * @param backgroundCol background colour of the billboard
     * @param msgColour message colour of the billboard
     * @param msgText message text of the billboard
     * @param url url of the picture on the billboard
     * @param encodedFile picture data of the picture on the billboard
     * @param infoCol information colour of the billboard
     * @param infoText information text of the billboard
     * @param author user who created the billboard
     */
    public CreateXML(String backgroundCol, String msgColour, String msgText, String url, String encodedFile, String infoCol, String infoText, String author) {
        this.backgroundCol = backgroundCol;
        this.msgColour = msgColour;
        this.msgText = msgText;
        this.url = url;
        this.encodedFile = encodedFile;
        this.infoCol = infoCol;
        this.infoText = infoText;
        this.author = author;
    }

    /**
     * Creates the format of the string to send the created billboard to the server
     * @return the string to send to the server
     */
    public String create() {
        if (backgroundCol.equals("")) {
            backgroundCol = "BLANK";
        }
        if (msgColour.equals("")) {
            msgColour = "BLANK";
        }
        if (msgText.equals("BLANK")) {
            msgText = "BLANK";
        }
        if (url.equals("")) {
            url = "BLANK";
        }
        if (encodedFile.equals("")) {
            encodedFile = "BLANK";
        }
        if (infoCol.equals("")) {
            infoCol = "BLANK";
        }
        if (infoText.equals("")) {
            infoText = "BLANK";
        }
        return backgroundCol + "|" + msgColour + "|" + msgText + "|" + encodedFile + "|" + url + "|" + infoCol + "|" + infoText;
    }

    /**
     * Creates the xml format needed to be able to preview the billboard
     * @return string of the xml
     */
    public String preview() {
        String billboardBG = "";
        String message = "";
        String picURL = "";
        String picData = "";
        String info = "";
        Boolean isURL = false;
        if (!backgroundCol.equals("")) {
            billboardBG = "<billboard background = '" + backgroundCol + "'>";
        } else {
            billboardBG = "<billboard>";
        }
        if (!msgText.equals("")) {
            if (!msgColour.equals("")) {
                message = "<message colour = '" + msgColour + "'>";
            } else {
                message = "<message>";
            }
            message += msgText + "</message>";
        }
        if (!url.equals("")) {
            picURL = "<picture url = '" + url + "'/>";
            isURL = true;
        } else if (!encodedFile.equals("")){
            picData = "<picture data = '" + encodedFile + "'/>";
            isURL = false;
        }
        if (!infoText.equals("")) {
            if (!infoCol.equals("")) {
                info = "<information colour = '" + infoCol + "'>";
            } else {
                info = "<information>";
            }
            info += infoText + "</information>";
        }
        String xml = "";
        if (isURL) {
            xml = billboardBG + "\n" + message + "\n" + picURL + "\n" + info + "\n" + "</billboard>";
        } else {
            xml = billboardBG + "\n" + message + "\n" + picData + "\n" + info + "\n" + "</billboard>";
        }
        System.out.println(xml);
        return xml;
    }
}

