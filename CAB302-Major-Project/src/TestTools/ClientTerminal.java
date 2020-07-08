package TestTools;

import ControlPanel.Password;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class ClientTerminal {
    private static DataInputStream dis;
    private static DataOutputStream dos;
    private static Socket s;
    private static String received;
    private static Scanner scn = new Scanner(System.in);

    public static int getPort() {
        try
        {
            Properties props = new Properties();
            FileInputStream in = new FileInputStream("src/properties");
            props.load(in);
            int portNum = Integer.parseInt(props.getProperty("port"));
            return portNum;
        } catch (IOException e) {
            e.printStackTrace();

        }
        return 0;

    }

    public static void main(String[] args) throws IOException
    {
        try
        {
            // getting localhost ip
            InetAddress ip = InetAddress.getByName("127.0.0.1");

            // establish the connection with server port 5056
            s = new Socket(ip, getPort());

            // obtaining input and out streams
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());

            // the following loop performs the exchange of
            // information between client and client handler
            while (true)
            {
                //System.out.println(dis.readUTF());
                String toSend = scn.nextLine();
                //dos.writeUTF(toSend);
                // If client sends exit,close this connection
                // and then break from the while loop
                if(toSend.equals("Q") || toSend.equals("q"))
                {
                    System.out.println("Closing this connection : " + s);
                    s.close();
                    System.out.println("Connection closed");
                    break;
                }
                else if(toSend.equals("login"))
                {
                    System.out.println("username:");
                    String user = scn.nextLine();
                    System.out.println("password:");
                    String pass = scn.nextLine();

                    //lets hash it
                    Password a = new Password();
                    byte[] res = a.hash(pass);
                    String x = a.toHexString(res);

                    pass = x;

                    String packet="CONNECT|"+user+":"+pass;
                    System.out.println(packet);
                    dos.writeUTF(packet);

                    // printing date or time as requested by client
                    received = dis.readUTF();
                    System.out.println(received);

                    String[] token1 = received.split("\\|");
                    String token = token1[1];
                    System.out.println("token is:"+token);

                }
                else if(toSend.equals("create user")){
                    System.out.println("token:");
                    String token = scn.nextLine();
                    String sender = "";
                }



            }
            // closing resources
            scn.close();
            dis.close();
            dos.close();
        }catch(Exception e){
            System.out.println("no server is running");
            //e.printStackTrace();
        }
    }

}
