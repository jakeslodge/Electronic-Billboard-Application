package BillboardViewer;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.TimerTask;

class RemindTask extends TimerTask {

    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    private Socket client = null;

    /**
     * Thread to run every 15 seconds which receives xml from the server and displays it
     */
    @Override
    public void run() {
        int port = getPort();
        String host = getHost();

        try {
            client = new Socket(host, port);
            try {
                dis = new DataInputStream(client.getInputStream());
                dos = new DataOutputStream(client.getOutputStream());
            } catch (IOException e) {
                System.out.println("Failed to create streams");
            }
            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            String billboard = "";
            try {
                dos.writeUTF("updateBillboard");

                //System.out.println("TEST");
                int res= dis.readInt();
                System.out.println(res);

                int length = dis.readInt();
               byte[] message = new byte[length];
                dis.readFully(message,0,message.length);

                billboard = new String(message);
                System.out.println(billboard);
            } catch (IOException e) {
                System.out.println("Failed to read input stream");
            }

            File test = new File("src/BillboardViewer/billboard.xml");
            try {
                FileWriter writer = new FileWriter("src/BillboardViewer/billboard.xml");
                writer.write(billboard);
                writer.close();
            } catch (IOException e) {
                System.out.println("Couldn't write to file");
            }

            XMLParser parser = null;
            try {
                parser = new XMLParser("src/BillboardViewer/billboard.xml");
            } catch (ParserConfigurationException | IOException | SAXException e) {
                System.out.println("Couldn't create XMLParser object");
            }
            assert parser != null;
            String backgroundCol = parser.getBillboardBG();
            Color col = Color.decode(backgroundCol);

            String text = parser.getMessageText();
            String pic = parser.getPicture();
            String info = parser.getInfoText();

            BillboardViewer bv = new BillboardViewer(col, text, pic, info, parser, false);
            bv.getFrame().dispose();

            try {
                DisplayBillboard display = new DisplayBillboard();
                display.displayBillboard(col, text, pic, info, parser, bv);
            } catch (IOException e) {
                System.out.println("Failed to display billboard");
            }

            try {
                client.close();
            } catch (IOException e) {
                System.out.println("Failed to close client socket");
            }
        } catch (IOException e) {
            BillboardHandler bh = new BillboardHandler();
            bh.errorBillboard();
            System.out.println("Couldn't connect to server");
        }

        System.out.println("closed connection");
    }

    /**
     * Get the port to connect to the server on
     * @return the port
     */
    public static int getPort() {
        Properties p = new Properties();
        FileInputStream propsFile = null;
        try {
            propsFile = new FileInputStream("src/properties");
        } catch (FileNotFoundException e) {
            System.out.println("Can't find properties file");
        }
        try {
            p.load(propsFile);
        } catch (IOException e) {
            System.out.println("Can't load properties file");
        }
        String portStr = p.getProperty("port");
        return Integer.parseInt(portStr);
    }

    /**
     * Get the host to connect to the server on
     * @return the host
     */
    public static String getHost() {
        Properties p = new Properties();
        FileInputStream propsFile = null;
        try {
            propsFile = new FileInputStream("src/properties");
        } catch (FileNotFoundException e) {
            System.out.println("Can't find properties file");
        }
        try {
            p.load(propsFile);
        } catch (IOException e) {
            System.out.println("Can't load properties file");
        }
        String host = p.getProperty("host");
        return host;
    }

}
