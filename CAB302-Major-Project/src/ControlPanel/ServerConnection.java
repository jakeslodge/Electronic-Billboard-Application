package ControlPanel;

import TestTools.ScheduleDay;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;

public class ServerConnection {
    private static DataInputStream dis;
    private static DataOutputStream dos;
    private static ObjectInputStream ois;
    private static Socket s;
    private static String token;
    private static String clientUsername;

    /***
     * Returns an int that is used for listening/communicating on a port.
     * The port number is set within src/ControlPanel/properties file
     * @return returns an int
     */
    private static int getPort() {
        try {
            Properties props = new Properties();
            FileInputStream in = new FileInputStream("src/properties");
            props.load(in);
            return Integer.parseInt(props.getProperty("port"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;

    }

    /***
     * Returns a string that is used for listening/communicating on a port
     * @return String used for identifying the localhost
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
        return p.getProperty("host");
    }

    /**
     * A function that enables stateless communication to the server over the designated port number
     * @throws IOException
     */
    public static void EstablishConnection() throws IOException {
        // getting localhost ip
        InetAddress ip = InetAddress.getByName(getHost());
        // establish the connection with server port 5056
        s = new Socket(ip, getPort());
        // obtaining input and out streams
        dis = new DataInputStream(s.getInputStream());
        dos = new DataOutputStream(s.getOutputStream());
        ois = new ObjectInputStream(s.getInputStream());

    }

    /**
     * A function that closes the connection to the server
     * @throws IOException
     */
    public static void CloseConnection() throws IOException {
        dos.writeUTF("Q"); //ClientHandler closes when receives Q || q
        s.close();
        dis.close();
        dos.close();
    }

    /**
     * When the client connects to the server this function communicates with the server
     * by sending the users token and verifying that are allowed to access the control panel
     * @return a boolean verifying their token is valid
     * @throws IOException
     */
    public static Boolean CheckToken() throws IOException {
        EstablishConnection();
        SendToServer("VALIDATE|", token);
        if (ReceiveData().contains("true")) {
            CloseConnection();
            return true;
        } else {
            CloseConnection();
            return false;
        }
    }

    /**
     * Function used to enable the user to cease communication with the server and
     * remove their token from being used
     * @throws IOException
     */
    public static void Logout() throws IOException {
        EstablishConnection();
        SendToServer("LOGOUT|", token);
    }

    /**
     * SendToServer is used to transmit data to the server
     * @param data to send
     * @throws IOException
     */
    public static void SendToServer(String data) throws IOException {
        dos.writeUTF(data);
    }

    /**
     * SendToServer(String, byte[], int) is used to transmit data to the server specifically for billboard creation
     * @param req data to send
     * @param data byte array for billboard creation
     * @param dataLength length of the byte array
     * @throws IOException
     */
    public static void SendToServer(String req,byte[] data,int dataLength) throws IOException{
        // THIS IS FOR BILLBOARD CREATION
        //send the request first
        dos.writeUTF(req);
        //send the length of the byte array
        dos.writeInt(dataLength);
        //send the stream
        dos.write(data);

    }

    /**
     * SendToServer(String, data) is used to communicate with the server in an almost http-esque style
     * @param ConnectionType type of communication the following data will be
     * @param data data for the server
     * @throws IOException
     */
    public static void SendToServer(String ConnectionType, String data) throws IOException {
        dos.writeUTF(ConnectionType + data);
    }

    /**
     * SendToServer(string,string,string) is used to communicate with the server in an almost http-esque style
     * @param ConnectionType type of communication the following data will be
     * @param data first bit of data for the server with added ':' at the end
     * @param dataTwo second bit of data for the server
     * @throws IOException
     */
    public static void SendToServer(String ConnectionType, String data, String dataTwo) throws IOException {
        dos.writeUTF(ConnectionType + data + ":" + dataTwo);
    }

    /**
     * Function to distribute the data the server returns through out the program
     * @return String for client data
     * @throws IOException
     */
    public static String ReceiveData() throws IOException
    {
        String temp;
        temp = dis.readUTF();
        return temp;
    }

    /**
     * Function to receive the weekly schedule of billboards
     * @return array of schedules
     * @throws IOException
     */
    public static ScheduleDay[] ReceiveWeek() throws IOException{
        ScheduleDay[] given = new ScheduleDay[7];
        try {
            given=(ScheduleDay[])ois.readObject();
            return given;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return given;
    }

    /**
     * Function to receive a byte[] and convert it to a string for usage elsewhere
     * @return String for conversion to usable data
     * @throws IOException
     */
    public static String ReceiveBitstream() throws IOException
    {
        int len = dis.readInt();
        byte[] message = new byte[len];
        dis.readFully(message,0,message.length);
        String toCreate = new String(message);
        return  toCreate;
    }

    /**
     * Function that retrieves the user who is logged in currently
     * @return string containing the username of currently logged in user
     */
    public static String getUsername()
    {
        return clientUsername;
    }

    /**
     * Function used to send the login information of the user
     * @param username string username
     * @param password string password
     * @return boolean to verify login status
     * @throws IOException
     */
    public static Boolean Login(String username, String password) throws IOException {
        EstablishConnection();
        SendToServer("CONNECT|", username, password);
        clientUsername = username;
        String temp = ReceiveData();
        if (temp.contains("TOKEN|")) {
            token = temp.substring(6);
            System.out.println(token);
            CloseConnection();
            return true;
        } else {
            CloseConnection();
            return false;
        }

    }


}
