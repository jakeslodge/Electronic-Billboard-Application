package jake.server;

import TestTools.ScheduleDay;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ClientHandler extends Thread {

    private static String userName;
    final DataInputStream dis;
    final DataOutputStream dos;
    final ObjectOutputStream oos;
    final Socket s;

    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, ObjectOutputStream oos) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.oos = oos;
    }


    /**
     * ClientLogin() enables the user to access the server by checking
     *     the string that server has received from the login form.
     *     It splits the username & password and incorporates token checking
     * @param received - param that the client sends containing connection info, username and pass concatenated
     * @return boolean
     */
    private boolean ClientLogin(String received) {
        try {
            String usernameString = SplitUserName(received);//Finds the username
            String passwordString = SplitPassword(received);//Finds the password
            userName = usernameString;
            Server tempServer = new Server();
            tempServer.readProps();
            tempServer.setupDBConnections();
            //Generates a token for the user
            String token = tempServer.userLogin(usernameString, passwordString);
            System.out.println("generated token:" + token);
            //Creates a token object for storage
            TokenStorage NewConnection = new TokenStorage(usernameString, token, LocalDateTime.now());
            //Stores the token object
            TokenStorage.StoreToken(NewConnection);

            //Checking if the token is valid and not an error
            //If valid, sends token to the client for their future use
            if (!token.contains("ERROR")) {
                token = "TOKEN|" + token;
                dos.writeUTF(token);

                return true;

            } else {
                dos.writeUTF("ERROR");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Receives CONNECT|username:password sent from the client and splits it up using regex
     * @param readUTF the string containing all log in information sent by the client
     * @return users username
     */
    private String SplitUserName(String readUTF) {
        String[] tempString = readUTF.split("[CONNECT|:]");
        return tempString[tempString.length - 2];
    }

    /**
     * Receives CONNECT|username:password sent from the client and splits it up using regex
     * @param readUTF the string containing all log in information sent by the client
     * @return users password
     */
    private String SplitPassword(String readUTF) {
        String[] tempString = readUTF.split("[CONNECT|:]");
        return tempString[tempString.length - 1];
    }

    /**
     * Main communication point between the Client and server
     * Information sent from the client gets sorted here and dealt with
     * @param clientRequest received from the client when they make requests to the server
     * @throws IOException
     */
    private void RequestIntercept(String clientRequest) throws IOException {
        boolean loggedIn;
        if (clientRequest.contains("CONNECT")) {
            loggedIn = ClientLogin(clientRequest);
            System.out.println(loggedIn);
        }
        else if (clientRequest.contains("POST"))
        {
            System.out.println("billboard update requested");
            //Create
            Server tempServer = new Server();
            tempServer.readProps();
            String holder = tempServer.whatToPlay();
            System.out.println(holder);
            byte[] b = holder.getBytes(Charset.forName("UTF-8"));

            dos.writeInt(b.length);
            dos.write(b);
            this.s.close();
        } else if (clientRequest.contains("VALIDATE")) {
            //Seperate VALIDATE| from token value
            String temp = clientRequest.substring(9);

            //Checks for valid token, returns boolean to client
            Boolean returnValue = TokenStorage.ValidateToken(temp);
            dos.writeUTF(returnValue.toString());
        } else if (clientRequest.contains("LOGOUT")) {
            //Seperate LOGOUT| from client request string
            String temp = clientRequest.substring(7);

            //Find token in storage and remove to logout the client
            TokenStorage.ExpireToken(temp);
        } else if (clientRequest.contains("updateBillboard")) {
            Server tempServer = new Server();
            tempServer.readProps();
            tempServer.setupDBConnections();
            String holder = tempServer.whatToPlay();
            System.out.println(holder);
            byte[] b = holder.getBytes(Charset.forName("UTF-8"));
            int length = b.length;

            System.out.println("new array has length of:" + length);


            dos.writeInt(length);
            dos.write(b);

        } else if (clientRequest.contains("createUser")) {
            Server tempServer = new Server();
            tempServer.readProps();
            tempServer.setupDBConnections();

            //Checking if the user has the "editUsers" permissions to create a new user
            if (tempServer.validatePerms(userName, "editUsers")) {

                //Removing the createUser| from start of connection string
                String req = clientRequest.substring(10);
                System.out.println(req);

                String reqb[] = req.split("\\|");
                String user = reqb[0];
                String pass = reqb[1];
                int p1 = Integer.parseInt(reqb[2]);
                int p2 = Integer.parseInt(reqb[3]);
                int p3 = Integer.parseInt(reqb[4]);
                int p4 = Integer.parseInt(reqb[5]);

                if (user.length() == 0) {
                    dos.writeUTF("NOUSERNAME");
                    return;
                }


                Boolean res = tempServer.createNewUser(user, pass, p1, p2, p3, p4);
                if (res) {
                    System.out.println("res:OK");
                    dos.writeUTF("OK");
                } else {
                    System.out.println("res:ERROR");
                    dos.writeUTF("ERROR");
                }
            } else {
                System.out.println("res:DENIED");
                dos.writeUTF("DENIED");
            }

        } else if (clientRequest.contains("createBillboard")) {
            Server tempServer = new Server();
            tempServer.readProps();
            tempServer.setupDBConnections();

            //Checking if the user has permission to create billboards
            if (!tempServer.validatePerms(userName, "createBillboards")) {
                dos.writeUTF("DENIED");
                return;
            }

            System.out.println(clientRequest);
            int len = dis.readInt();
            byte[] message = new byte[len];
            dis.readFully(message, 0, message.length);
            String toCreate = new String(message);
            System.out.println(toCreate);

            //lets break it down so we can start creating it
            String billboardName, backgroundColour, messageColour, infoColour, messageData, pictureData, pictureURL, infoData, author;
            author = userName;

            String[] split = toCreate.split("\\|");

            String[] blocks = new String[8];
            for (int x = 0; x < blocks.length; x++) {

                if (split[x].equals("BLANK")) {
                    blocks[x] = "";
                } else {
                    blocks[x] = split[x];
                }

            }

            billboardName = blocks[0];
            pictureData = blocks[4];
            pictureURL = blocks[5];
            messageData = blocks[3];
            infoData = blocks[7];
            backgroundColour = blocks[1];
            messageColour = blocks[2];
            infoColour = blocks[6];

            String res = tempServer.addBillboard(billboardName, backgroundColour, messageColour, infoColour, messageData, pictureData, pictureURL, infoData, author);
            System.out.println("Response to send:" + res);
            dos.writeUTF(res);


        } else if (clientRequest.contains("updatePassword")) {
            //lets update their password
            String req = clientRequest.substring(14);
            System.out.println("substring= " + req);
            String[] reqb = req.split("\\|");

            String ru = userName;
            String tu = reqb[1];
            String p = reqb[2];

            System.out.println(ru);
            System.out.println(tu);
            System.out.println(p);

            //interact with server
            Server tempServer = new Server();
            tempServer.readProps();
            tempServer.setupDBConnections();

            //System.out.println(tempServer.updatePassword(ru,tu,p));
            dos.writeUTF(tempServer.updatePassword(ru, tu, p));


        } else if (clientRequest.contains("scheduleBillboard")) {
            Server tempServer = new Server();
            tempServer.readProps();
            tempServer.setupDBConnections();

            //Check to see if user has permissions to schedule billboards
            if (tempServer.validatePerms(userName, "scheduleBillboards")) {
                String req = clientRequest.substring(17);
                String[] split = req.split("\\|");
                String billboardName = split[0];
                String start = split[1];
                String duration = split[2];
                String freq = split[3];

                System.out.println("name:" + billboardName);
                if (billboardName.equals("")) {
                    dos.writeUTF("NOSELECT");
                    return;
                }

                try {

                    //Formatting the dateTime into a more suiteable dateTime format
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                    LocalDateTime dateTime = LocalDateTime.parse(start, formatter);

                    Duration d = Duration.ofMinutes(Integer.parseInt(duration));

                    if (freq.equals("NONE")) {
                        tempServer.resolveScheduleClash(billboardName, dateTime, d, userName);
                    } else if (freq.equals("WEEK")) {
                        for (int z = 0; z < 7; z++) {
                            tempServer.resolveScheduleClash(billboardName, dateTime, d, userName);
                            dateTime = dateTime.plusDays(1);
                        }
                    }

                    tempServer.scheduleCleanup();
                    dos.writeUTF("OK");
                } catch (DateTimeParseException e) {
                    dos.writeUTF("ERROR");
                } catch (NumberFormatException e) {
                    dos.writeUTF("ERROR");
                }


                //res


            } else {
                dos.writeUTF("DENIED");
                System.out.println("DENIED");
            }


        } else if (clientRequest.contains("getBillboardInfo")) {

            //get billboard info for that one
            String x = dis.readUTF();
            //interact with server
            Server tempServer = new Server();
            tempServer.readProps();
            tempServer.setupDBConnections();
            String res = tempServer.getBillboardInfo(x);
            System.out.println("res:" + res);
            //pack it into a byte stream
            byte[] b = res.getBytes(Charset.forName("UTF-8"));
            dos.writeInt(b.length);
            dos.write(b);

        } else if (clientRequest.contains("schedulelist")) {
            Server tempServer = new Server();
            tempServer.readProps();
            tempServer.setupDBConnections();
            String res = tempServer.getBillboardListAll();
            dos.writeUTF(res);
        } else if (clientRequest.contains("getBillboardList")) {
            //they want the billboard list, should we give them all or just there own
            Server tempServer = new Server();
            tempServer.readProps();
            tempServer.setupDBConnections();
            if (tempServer.validatePerms(userName, "editAllBillboards")) {
                //they are allowed to have all
                String res = tempServer.getBillboardListAll();
                System.out.println("RES:" + res);
                dos.writeUTF(res);
            } else {
                //they are only allowed there own
                String res = tempServer.getBillboardListForUser(userName);
                System.out.println("RES:" + res);
                dos.writeUTF(res);
            }

        } else if (clientRequest.contains("deleteUser")) {

            Server tempServer = new Server();
            tempServer.readProps();
            tempServer.setupDBConnections();

            String req = clientRequest.substring(10);
            System.out.println(req);

            String[] a = req.split("\\|");
            String t = a[0];
            String r = a[1];

            System.out.println(t + r);

            if (tempServer.validatePerms(r, "editUsers")) {
                String res = tempServer.deleteUser(r, t);
                System.out.println(res); // responce to send
                dos.writeUTF(res);
            } else {
                String res = "DENIED";
                System.out.println(res); //responce to send
                dos.writeUTF(res);
            }


        } else if (clientRequest.contains("getUserList")) {
            System.out.println("userlist request check perms");
            Server tempServer = new Server();
            tempServer.readProps();
            tempServer.setupDBConnections();
            if (!tempServer.validatePerms(userName, "editUsers")) {
                int p1 = 0;
                int p2 = tempServer.validatePerms(userName, "scheduleBillboards") ? 1 : 0;
                int p3 = tempServer.validatePerms(userName, "createBillboards") ? 1 : 0;
                int p4 = tempServer.validatePerms(userName, "editAllBillboards") ? 1 : 0;
                String toSendBack = userName + "~" + p1 + "~" + p2 + "~" + p3 + "~" + p4;
                System.out.println("res:" + toSendBack);
                dos.writeUTF(toSendBack);
            } else {
                String toSendBack = tempServer.getUsersList();
                System.out.println(toSendBack);
                dos.writeUTF(toSendBack);
            }
        } else if (clientRequest.contains("updateUserPerms")) {

            Server tempServer = new Server();
            tempServer.readProps();
            tempServer.setupDBConnections();

            String req = clientRequest.substring(14);
            System.out.println(req);

            String[] x = req.split("\\|");
            String r = x[0];
            String t = x[1];
            int p1 = Integer.parseInt(x[2]);
            int p2 = Integer.parseInt(x[3]);
            int p3 = Integer.parseInt(x[4]);
            int p4 = Integer.parseInt(x[5]);

            //lets check they are allowed to edit perms
            if (tempServer.validatePerms(userName, "editUsers")) {
                String res = tempServer.updateUserPerms(r, t, p1, p2, p3, p4);
                System.out.println(res);
                dos.writeUTF(res);
            } else {
                String res = "DENIED";
                System.out.println(res);
                dos.writeUTF(res);
            }

        } else if (clientRequest.contains("deleteBillboard")) {

            String bill = clientRequest.substring(15);

            String[] x = bill.split("\\|");
            String bill2 = x[1];

            System.out.println(bill2);
            Server tempServer = new Server();
            tempServer.readProps();
            tempServer.setupDBConnections();

            //edit all, can delete any and will remove those from the schedule
            if (tempServer.validatePerms(userName, "editAllBillboards")) {
                String res = tempServer.deleteBillboardAndSchedule(bill2);
                System.out.println("res:" + res);
                dos.writeUTF(res);
            }
            //create billboard perm, have to delete own and make sure its not scheduled
            else if (tempServer.validatePerms(userName, "createBillboards")) {
                String res = tempServer.deleteBillboard(bill2);
                System.out.println("res:" + res);
                dos.writeUTF(res);
            } else {
                //denied
                System.out.println("res:DENIED");
                dos.writeUTF("DENIED");
            }


        } else if (clientRequest.contains("calender")) {
            System.out.println("triggered!");
            //anyone can request the calender so this does not matter
            Server tempServer = new Server();
            tempServer.readProps();
            tempServer.setupDBConnections();

            ScheduleDay[] weekToSend = tempServer.calendar();
            System.out.println("sending a responce!");
            oos.writeObject(weekToSend);

        } else if (clientRequest.contains("editBillboard")) {
            int len = dis.readInt();
            byte[] message = new byte[len];
            dis.readFully(message, 0, message.length);
            String toCreate = new String(message);
            System.out.println(toCreate);
            Server tempServer = new Server();
            tempServer.readProps();
            tempServer.setupDBConnections();
            tempServer.editBillboard(toCreate);
            dos.writeUTF("OK");
        } else if (clientRequest.contains("getScheduleList")) {
            Server tempServer = new Server();
            tempServer.readProps();
            tempServer.setupDBConnections();
            String s = tempServer.getScheduleList();
            dos.writeUTF(s);
        } else if (clientRequest.contains("deleteSchedule")) {
            //lets remove the request part
            System.out.println(clientRequest);
            String req = clientRequest.substring(14);
            System.out.println(req);

            Server tempServer = new Server();
            tempServer.readProps();
            tempServer.setupDBConnections();

            //see if they are allowed
            if (tempServer.validatePerms(userName, "scheduleBillboards")) {
                //lets try parse it
                String[] data = req.split("~");
                if (data.length == 1) {
                    dos.writeUTF("ok");
                } else {
                    String name = data[0];
                    String date = data[1];
                    date = date.substring(1);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime dateTime = LocalDateTime.parse(date, formatter);

                    String res = tempServer.deleteSchedule(dateTime, name);
                    dos.writeUTF(res);
                }
            } else {
                //they are not allowed
                dos.writeUTF("DENIED");
            }


        }

    }

    /**
     * When the client connects to the associated port it opens connection and makes a thread of the server.
     * This method is the start point for each thread, where it listens for the client to make requests and sends it
     * off to be dealt with in the RequestIntercept() function
     *
     */
    @Override
    public void run() {
        String received;
        System.out.println("Server has received this connection and is starting");
        while (true) {
            try {
                received = dis.readUTF();
                if (received.equals("Q") || received.equals("q")) {
                    System.out.println("Client" + this.s + " is closing");
                    this.s.close();
                    break;
                } else {
                    System.out.println("Server Received: " + received);
                    //dos.writeUTF("You sent: " + received);
                    RequestIntercept(received);
                }
            } catch (IOException e) {
                e.printStackTrace();
                //must have closed on the client
                break;
            }
        }
    }
}
