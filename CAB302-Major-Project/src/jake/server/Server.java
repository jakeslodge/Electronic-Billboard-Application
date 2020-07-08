package jake.server;

import ControlPanel.Password;
import ControlPanel.Schedule;
import TestTools.ScheduleDay;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.desktop.SystemEventListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Server {
    //global variables
    private String MYSQL_DRIVER = "org.mariadb.jdbc.Driver";
    private String MYSQL_SCHEMA = "";
    private String MYSQL_URL = "";
    private String MYSQL_USER = "";
    private String MYSQL_PASS = "";

    //global connection objects
    private Connection con;
    private ResultSet rs;

    /**
     * Attempts to read the props file in the directory
     * If successfull will write those values
     * <p>
     * Prints(.props information)
     */
    public void readProps()  {
        try {
            //read the .props file
            Properties props = new Properties();
            FileInputStream in = new FileInputStream("src/jake/server/db.props");
            props.load(in);
            MYSQL_URL= props.getProperty("jdbc.url");
            MYSQL_SCHEMA = props.getProperty("jdbc.schema");
            MYSQL_USER = props.getProperty("jdbc.username");
            MYSQL_PASS = props.getProperty("jdbc.password");
            System.out.println("Setting up database, information given:");
            System.out.println("URL: "+MYSQL_URL);
            System.out.println("Schema: "+MYSQL_SCHEMA);
            System.out.println("username: "+MYSQL_USER);
            System.out.println("password: "+MYSQL_PASS);
        }
        catch(IOException e){
            System.err.println("The props file given can not be found");
        }

    }

    public void setupDBConnections(){
        try {
            System.out.println("Attempting to set up connection objects");
            Class.forName("org.mariadb.jdbc.Driver");
            con = DriverManager.getConnection(MYSQL_URL + "/" + MYSQL_SCHEMA, MYSQL_USER, MYSQL_PASS);
        }
        catch(SQLException sqle){
            System.err.println("Could not connect to the database, check your credentials");
            //sqle.printStackTrace();
        }
        catch(ClassNotFoundException ex) {
            System.err.println("ClassNotFoundException:\n"+ex.toString());
            ex.printStackTrace();

        }
    }

    /**
     * Drops a row from the schedule give a row ID
     * @param rowID
     */
    public void dropRows(int rowID) {
        //Connection con;
        //ResultSet rs;
        try {
            //con = DriverManager.getConnection(MYSQL_URL + "/" + MYSQL_SCHEMA, MYSQL_USER, MYSQL_PASS);

            PreparedStatement pst = con.prepareStatement("DELETE FROM schedule WHERE billboardID= ?");
            pst.setInt(1,rowID);
            pst.executeUpdate();
        }
        catch(SQLException sqle){
            System.err.println("sql con error");
            sqle.printStackTrace();
        }

    }

    /**
     *
     * @param n - the length of how many characters you would like the string
     * @return a string of random letters of length n
     */
    public String getAlphaNumericString(int n)
    {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    /**
     * Takes a result set and calulcates how many rows there are
     * @param rs a Result set that is scrollable
     * @return gives back the number of rows
     */
    public int queryResultsNumber(ResultSet rs){
        int size = 0;
        try
        {
            rs.last();
            size = rs.getRow();
            rs.beforeFirst();
        }
        catch (Exception ex){
            System.err.println("no results given");
            return 0;
        }
        System.out.println(size+" <-- results found");
        return size;
    }

    /**
     *
     * @param idToChange the billboard Id of the what to change
     * @param startTime the billboard start time
     * @param timeToSuppliment the endTime of the billboard id To change will be replaced with (End time)
     */
    public void adjustEndTime(int idToChange,LocalDateTime startTime ,LocalDateTime timeToSuppliment)
    {
        //Connection con;
        //ResultSet rs;
        try {
            //con = DriverManager.getConnection(MYSQL_URL+"/"+MYSQL_SCHEMA, MYSQL_USER, MYSQL_PASS);
            PreparedStatement pst = con.prepareStatement("UPDATE schedule SET endTime = ? , duration = ? WHERE billboardID = ?");

            //calculate the new duration aswell
            LocalTime newdurationTime = LocalTime.of(0,0);
            newdurationTime = newdurationTime.plus(Duration.between(startTime,timeToSuppliment));
            Time durationToInsert = Time.valueOf(newdurationTime);
            Timestamp toInsert = Timestamp.valueOf(timeToSuppliment);


            pst.clearParameters();
            pst.setInt(3,idToChange);
            pst.setTime(2,durationToInsert);
            pst.setTimestamp(1,toInsert);
            pst.executeUpdate();
        }
        catch (SQLException sqle){
            System.err.println("sql con error");
            sqle.printStackTrace();
        }

    }

    /**
     *
     * @param idToChange the ID of the billboard to change
     * @param endTime the billboards original end
     * @param newEnd the incoming billboards  ending time
     */
    public void adjustStartTime(int idToChange,LocalDateTime endTime ,LocalDateTime newEnd)
    {
        //Connection con;
        //ResultSet rs;
        try {
            //con = DriverManager.getConnection(MYSQL_URL+"/"+MYSQL_SCHEMA, MYSQL_USER, MYSQL_PASS);
            PreparedStatement pst = con.prepareStatement("UPDATE schedule SET startTime = ? , duration = ? WHERE billboardID = ?");

            //calculate the new duration aswell
            LocalTime newdurationTime = LocalTime.of(0,0);
            newdurationTime = newdurationTime.plus(Duration.between(newEnd,endTime));
            Time durationToInsert = Time.valueOf(newdurationTime);
            Timestamp toInsert = Timestamp.valueOf(newEnd);


            pst.clearParameters();
            pst.setInt(3,idToChange);
            pst.setTime(2,durationToInsert);
            pst.setTimestamp(1,toInsert);
            pst.executeUpdate();
        }
        catch (SQLException sqle){
            System.err.println("sql con error");
            sqle.printStackTrace();
        }

    }

    /**
     * Will return if a time falls within a current scheduling
     *
     * @param timeToTest a Local date time to test
     * @return returns true if there is a clash, false if there is nothing scheduled in that time
     */
    public Boolean willTimeClash(LocalDateTime timeToTest){
        Timestamp sqlTimeToTest = Timestamp.valueOf(timeToTest);

        //Connection con;
        //ResultSet rs;
        try {
            //con = DriverManager.getConnection(MYSQL_URL + "/" + MYSQL_SCHEMA, MYSQL_USER, MYSQL_PASS);

            PreparedStatement pst = con.prepareStatement("SELECT * FROM schedule WHERE startTime <= ? AND endTime >= ?");
            pst.setTimestamp(1,sqlTimeToTest);
            pst.setTimestamp(2,sqlTimeToTest);

            rs=pst.executeQuery();

            if (queryResultsNumber(rs)==0)
            {
                System.out.println("no clash");
                return false;
            }
            else{
                System.out.println("clash detected");
                return true;
            }

        }
        catch (SQLException sqle){
            System.err.println("sql con error");
            sqle.printStackTrace();
        }
        return false;
    }

    /**
     *
     * @param newBillboardName a billboard to be scheduled name
     * @param newStartingTime a billboards starting time
     * @param newBillboardDuration a billboards duration
     * @param bookedBy the username of the person booking it
     * @return OK, if the user has permission, denied otherwise
     */
    public String resolveScheduleClash(String newBillboardName,LocalDateTime newStartingTime, Duration newBillboardDuration, String bookedBy){

        Boolean newListingAdded = false;

        LocalDateTime newEndTime = newStartingTime.plus(newBillboardDuration);
        //lets convert those times to an sql format
        Timestamp sqlNewStartingTime = Timestamp.valueOf(newStartingTime);
        Timestamp sqlNewEndTime = Timestamp.valueOf(newEndTime);
        System.out.println("Java = Start:" + newStartingTime + " end:" +newEndTime);
        System.out.println("SQL = Start:" + sqlNewStartingTime + " end:"+sqlNewEndTime);


        //lets start database querying
        //Connection con;
        //ResultSet rs;


        //drop tables where start and finish covers it
        try{
            //con = DriverManager.getConnection(MYSQL_URL+"/"+MYSQL_SCHEMA, MYSQL_USER, MYSQL_PASS);
            PreparedStatement pst = con.prepareStatement("SELECT * FROM schedule WHERE startTime >= ? AND endTime <= ?",ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            pst.setTimestamp(1,sqlNewStartingTime);
            pst.setTimestamp(2,sqlNewEndTime);

            rs = pst.executeQuery();

            if (queryResultsNumber(rs)!=0)
            {
                while(rs.next()){
                    //lets go through the rows and drop them
                    int dropID = rs.getInt("billboardID");
                    dropRows(dropID);
                    System.out.println("Dropping a row where the new start and finish overlaps");
                }
            }
            else{
                System.out.println("No rows to drop");
            }

        }
        catch(SQLException sqle){
            System.err.println("sql con error");
            sqle.printStackTrace();
        }

        //This value is good to be added straight away
        if (!willTimeClash(newStartingTime) && !willTimeClash(newEndTime)){ // TODO THIS WILL CAUSE THE progrma to ignore billboard sppaning all the time add a check to see if anything is in those times

            System.out.println("Row added that had no clashes");

            addToSchedule(newBillboardName,newStartingTime,newBillboardDuration,bookedBy);
            return ("OK");
        }

        try {
            //con = DriverManager.getConnection(MYSQL_URL+"/"+MYSQL_SCHEMA, MYSQL_USER, MYSQL_PASS);
            PreparedStatement pst = con.prepareStatement("SELECT * FROM schedule WHERE startTime <= ? AND endTime >= ?",ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            pst.setTimestamp(1,sqlNewStartingTime);
            pst.setTimestamp(2,sqlNewEndTime);

            rs = pst.executeQuery();

            if (queryResultsNumber(rs)!=0)
            {
                //there has been a result lets chop it up
                rs.next(); //highlight the row and extract the results

                int oldID = rs.getInt("billboardID");
                String oldBillboardName = rs.getString("billboardName");
                LocalDateTime oldStartTime = rs.getTimestamp("startTime").toLocalDateTime();
                LocalDateTime oldEndTime = rs.getTimestamp("endTime").toLocalDateTime();
                Duration oldDuration = Duration.between(oldStartTime,oldEndTime);

                System.out.println(oldID+" ~ "+oldBillboardName+" ~ "+oldStartTime+" ~ "+oldDuration+" ~ "+oldEndTime);

                //first step is to trim the oldEnd Time to be the new Start
                adjustEndTime(oldID,oldStartTime,newStartingTime);

                //create a new schedule from the end of New to end of old
                Duration tempDuration = Duration.between(newEndTime,oldEndTime);
                addToSchedule(oldBillboardName,newEndTime,tempDuration,bookedBy);

                //insert new one
                addToSchedule(newBillboardName,newStartingTime,newBillboardDuration,bookedBy);

                System.out.println("Row added where the start and finish were in another");

                //con.close();
                newListingAdded = true;
                return ("OK");

            }


        } // see if the given start and finish is in between any other times
        catch (SQLException sqle){
            System.out.println("sql con error");
            sqle.printStackTrace();
        }


        if(willTimeClash(newStartingTime)) {

            System.out.println("The given start time clashes with another, attempting to bring the other forward");
            try {
                //con = DriverManager.getConnection(MYSQL_URL + "/" + MYSQL_SCHEMA, MYSQL_USER, MYSQL_PASS);

                PreparedStatement pst = con.prepareStatement("SELECT * FROM schedule WHERE startTime <= ? AND endTime >= ?");
                pst.setTimestamp(1,sqlNewStartingTime);
                pst.setTimestamp(2,sqlNewStartingTime);

                rs=pst.executeQuery();
                rs.next();

                int clashID = rs.getInt("billboardID");
                String clashName = rs.getString("billboardName");
                LocalDateTime clashStart = rs.getTimestamp("startTime").toLocalDateTime();

                adjustEndTime(clashID,clashStart,newStartingTime);
            }
            catch (SQLException sqle) {
                System.err.println("sql error in fixing start");
                sqle.printStackTrace();
            }
        }//will the new given start time affect current schedule

        if(willTimeClash(newEndTime)){ //will the new end type affect the schedule
            try {
                //con = DriverManager.getConnection(MYSQL_URL + "/" + MYSQL_SCHEMA, MYSQL_USER, MYSQL_PASS);

                PreparedStatement pst = con.prepareStatement("SELECT * FROM schedule WHERE startTime <= ? AND endTime >= ?");
                pst.setTimestamp(1,sqlNewEndTime);
                pst.setTimestamp(2,sqlNewEndTime);

                rs=pst.executeQuery();
                rs.next();

                int clashID = rs.getInt("billboardID");
                LocalDateTime clashEnd = rs.getTimestamp("endTime").toLocalDateTime();

                adjustStartTime(clashID,clashEnd,newEndTime);
            }
            catch (SQLException sqle) {
                System.err.println("sql error in fixing start");
                sqle.printStackTrace();
            }
        }

        //add the value now  that everything is adjusted
        System.out.println("fixed clashes adding value");
        addToSchedule(newBillboardName,newStartingTime,newBillboardDuration,bookedBy);
        return ("OK");
    }

    /**
     * Creates the tables to be used in the database
     */
    public void createTables() {
        Connection con;
        Statement st;
        ResultSet rs;
        try{
            System.out.println("Creating tables");
            Class.forName("org.mariadb.jdbc.Driver");
            //con = DriverManager.getConnection(MYSQL_URL,MYSQL_USER,MYSQL_PASS);
            con = DriverManager.getConnection(MYSQL_URL+"/"+MYSQL_SCHEMA, MYSQL_USER, MYSQL_PASS);

            System.out.println(MYSQL_URL);
            st = con.createStatement();



            String sql = "CREATE TABLE IF NOT EXISTS REGISTRATION "
                    + "(id INTEGER not NULL, "
                    + " first VARCHAR(255), "
                    + " last VARCHAR(255), "
                    + " age INTEGER, "
                    + " PRIMARY KEY ( id ))";

            String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "ID INT NOT NULL AUTO_INCREMENT," +
                    "userName VARCHAR(50) NULL DEFAULT 0," +
                    "password VARCHAR(120) NULL DEFAULT 0," +
                    "salt VARCHAR(120) NULL DEFAULT 0," +
                    "sessionExp DATETIME NULL DEFAULT NOW()," +
                    "sessionToken VARCHAR(256) NULL DEFAULT NULL," +
                    "editUsersPerm BIT NULL DEFAULT NULL," +
                    "scheduleBillboardsPerm BIT NULL DEFAULT NULL," +
                    "createBillboardsPerm BIT NULL DEFAULT NULL," +
                    "editAllBillboardsPerm BIT NULL DEFAULT NULL," +
                    "PRIMARY KEY (ID))";

            String sqlBillboards = "CREATE TABLE IF NOT EXISTS billboards (" +
                    "billboardID INT NOT NULL AUTO_INCREMENT," +
                    "billboardName VARCHAR(50) NOT NULL," +
                    "pictureData MEDIUMBLOB," +
                    "pictureURL VARCHAR(200)," +
                    "messageData TEXT," +
                    "infoData TEXT," +
                    "backgroundColour TINYTEXT NOT NULL DEFAULT '#FFFFFF'," +
                    "messageColour TINYTEXT NOT NULL DEFAULT '#FFFFFF'," +
                    "infoColour TINYTEXT NOT NULL DEFAULT '#FFFFFF'," +
                    "author VARCHAR(50) NULL DEFAULT 0," +
                    "PRIMARY KEY (billboardID, billboardName))";

            String sqlSchedule = "CREATE TABLE IF NOT EXISTS schedule (" +
                    "billboardID INT NOT NULL AUTO_INCREMENT," +
                    "billboardName VARCHAR(50) NOT NULL," +
                    "startTime DATETIME NOT NULL," +
                    "duration TIME NOT NULL," +
                    "endTime DATETIME NOT NULL," +
                    "bookedBy VARCHAR(50) NOT NULL," +
                    "PRIMARY KEY(billboardID, billboardName))";


            st.executeUpdate(sqlUsers);
            st.executeUpdate(sqlBillboards);
            st.executeUpdate(sqlSchedule);
            System.out.println("Tables have been created");
            //con.close();
        }
        catch(ClassNotFoundException ex) {
            System.err.println("ClassNotFoundException:\n"+ex.toString());
            ex.printStackTrace();

        }
        catch(SQLException ex) {
            //System.err.println("SQLException:\n"+ex.toString());
            ex.printStackTrace();
            System.err.println("Could not connect to the database with provided information, please ensure your props file is correct");
        }
    }

    /**
     *
     * @param userName in plaintext
     * @param password a hashed password
     * @param editUsers 0 or 1
     * @param schedule 0 or 1
     * @param createBillboards 0 or 1
     * @param editAllBillboards 0 or 1
     * @returns true or false, false meaning that the username is already in use and cannot be used, true meaning  the user was created!
     */
    public Boolean createNewUser(String userName, String password,int editUsers,int schedule,int createBillboards,int editAllBillboards)
    {
        //first step is to see if that user is already in the database
        //Connection con;
        //Statement st;
        //ResultSet rs;
        try{
            //Class.forName(MYSQL_DRIVER);
            //con = DriverManager.getConnection(MYSQL_URL+"/"+MYSQL_SCHEMA, MYSQL_USER, MYSQL_PASS);
            PreparedStatement pst = con.prepareStatement("SELECT * FROM users WHERE userName=?");

            pst.setString(1,userName);

            PreparedStatement pst1 = con.prepareStatement("INSERT INTO users(userName, password," +
                    "salt, editUsersPerm, scheduleBillboardsPerm, createBillboardsPerm," +
                    "editAllBillboardsPerm) VALUES(?,?,?,?,?,?,?)");

            rs=pst.executeQuery();

            if (queryResultsNumber(rs)==0)
            {
                System.out.println("username is not taken lets add it");
                //first step is to generate the salt
                jake.server.Password pass = new jake.server.Password();
                // generate the salt
                byte[] saltByte = pass.generateSalt();
                String salt = pass.toHexString(saltByte);
                System.out.println("New user pass:"+password);
                System.out.println("New user salt:"+salt);
                // concatenate the hashed pass and salt
                String pass_salt = password + salt;
                System.out.println("New user password+salt:"+pass_salt);
                // hash this concatenation
                byte[] final_hash_byte = pass.hash(pass_salt);
                String final_hash = pass.toHexString(final_hash_byte);
                System.out.println("password and salt hashed="+final_hash);

                pst1.clearParameters();
                pst1.setString(1,userName);
                pst1.setString(2,final_hash);
                pst1.setString(3,salt);
                pst1.setInt(4,editUsers);
                pst1.setInt(5,schedule);
                pst1.setInt(6,createBillboards);
                pst1.setInt(7,editAllBillboards);

                pst1.execute();
                return true;
            }
            else{
                System.out.println("username already taken");
                return false;
            }

        }
        catch(SQLException ex) {

            System.err.println("SQLException:\n"+ex.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }catch (NullPointerException e){
            System.err.println("Could not add new user, the database must be incorrectly setup");
        }

        return false;
    }

    /**
     * Takes a username and password, if this correct authentication is given
     * then it returns a token and update the expiry to 24hrs from the request
     *
     * @param userName in plaintext
     * @param password takes a password that is already hashed
     * @return a new generated session token
     */
    public String userLogin(String userName, String password)
    {
        //lets get the info for the given user
        //first step is to see if that user is already in the database
        //Connection con;
        //Statement st;
        //ResultSet rs;
        try {
            //Class.forName(MYSQL_DRIVER);
            //con = DriverManager.getConnection(MYSQL_URL + "/" + MYSQL_SCHEMA, MYSQL_USER, MYSQL_PASS);
            PreparedStatement pst = con.prepareStatement("SELECT * FROM users WHERE userName=?");
            pst.setString(1,userName);

            rs=pst.executeQuery();

            // user exists
            if (queryResultsNumber(rs) > 0)
            {
                rs.next(); //make sure you are actually reading it
                jake.server.Password pass = new jake.server.Password();
                // get the salt from the database instead of earlier when we generated it
                // use .getBytes() instead of .getString because we can't cast from String to Byte normally
                String salt = rs.getString("salt");
                String pass_salt = password + salt;
                System.out.println("salt:"+salt);
                System.out.println("Attempting:"+password+salt);
                byte[] final_hash_byte = pass.hash(pass_salt);
                String final_hash = pass.toHexString(final_hash_byte);
                System.out.println("attempted result:"+final_hash);

                // check if matches the password in the database

                String dbPass = rs.getString("password");
                System.out.println(final_hash);
                System.out.println(dbPass);
                if (final_hash.equals(dbPass)) {
                    // update expiry to 24 hours from now
                    LocalDateTime newExpiry = LocalDateTime.now().plus(24, ChronoUnit.HOURS);
                    //DateTimeFormatter formatExpiry = DateTimeFormatter.ofPattern("yyyy-mm-dd hh:mm:ss");
                    //String formattedExpiry = newExpiry.format(formatExpiry);
                    java.sql.Timestamp sqlexp = Timestamp.valueOf(newExpiry);

                    // generate token and update token and expiry in database
                    String token = getAlphaNumericString(255); // 256 is the max size of a token in the database
                    PreparedStatement pst2 = con.prepareStatement("UPDATE users SET sessionToken = ?, sessionExp = ? WHERE password = ?");
                    pst2.setString(1, token);
                    pst2.setTimestamp(2, sqlexp);
                    pst2.setString(3, final_hash);
                    rs = pst2.executeQuery();

                    // return token
                    return token;
                } else {
                    System.err.println("Passwords do not match");
                    return("ERROR");
                }

            } else {
                System.err.println("No user exists with that username");
                return("ERROR");
            }

        }
        catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
        }

        catch (NoSuchAlgorithmException ex) {
            System.err.println("NoSuchAlgorithmException:\n"+ex.toString());
        }

        return("ERROR:Generic");
    }

    /**
     * Takes the user's username and action requested and returns a boolean indicating
     * whether the user has the correct permission required to take the action or not
     * @param userName Plaintext
     * @param actionRequested Plaintext. An action type ("editUsers", "scheduleBillboards", "createBillboards" or "editAllBillboards")
     * @return Boolean
     */
    public Boolean validatePerms(String userName, String actionRequested)
    {
        Connection con;
        Statement st;
        ResultSet rs;
        try {
            Class.forName(MYSQL_DRIVER);
            con = DriverManager.getConnection(MYSQL_URL+"/"+MYSQL_SCHEMA, MYSQL_USER, MYSQL_PASS);
            PreparedStatement pst = con.prepareStatement("SELECT * FROM users WHERE userName = ?");

            pst.setString(1, userName); // userName being this function's parameter
            rs = pst.executeQuery();

            if (queryResultsNumber(rs) > 0) {
                rs.next();
                System.out.println("retrieving user permissions");
                byte editUsers = rs.getByte("editUsersPerm");
                byte scheduleBB = rs.getByte("scheduleBillboardsPerm");
                byte createBB = rs.getByte("createBillboardsPerm");
                byte editAllBB = rs.getByte("editAllBillboardsPerm");

                if (actionRequested == "editUsers") {
                    System.out.println(editUsers);
                    if (editUsers == 1) {
                        System.out.println(userName + " does have permission to edit users.");
                        return true;
                    } else {
                        System.err.println(userName + " does not have permission to edit users.");
                        return false;
                    }
                }

                if (actionRequested == "scheduleBillboards") {
                    if (scheduleBB == 1) {
                        System.out.println(userName + " does have permission to schedule billboards.");
                        return true;
                    } else {
                        System.err.println(userName + " does not have permission to schedule billboards.");
                        return false;
                    }
                }

                if (actionRequested == "createBillboards") {
                    if (createBB == 1) {
                        System.out.println(userName + " does have permission to create billboards.");
                        return true;
                    } else {
                        System.err.println(userName + " does not have permission to create billboards.");
                        return false;
                    }
                }

                if (actionRequested == "editAllBillboards") {
                    if (editAllBB == 1) {
                        System.out.println(userName + " does have permission to edit all billboards.");
                        return true;
                    } else {
                        System.err.println(userName + " does not have permission to edit all billboards.");
                        return false;
                    }
                }

            } else {
                System.err.println("Username does not exist. Is it correct?");
                return false;
            }
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * When called will clean up the billboard schedule making sure any old entries or ones out of date are cleaned up
      */
    public void scheduleCleanup(){
        //Connection con;
        //Statement st;
        //ResultSet rs;
        try {
            //Class.forName(MYSQL_DRIVER);
            //con = DriverManager.getConnection(MYSQL_URL + "/" + MYSQL_SCHEMA, MYSQL_USER, MYSQL_PASS);
            PreparedStatement pst = con.prepareStatement("DELETE FROM schedule WHERE duration=? OR endTime < ?");
            LocalDateTime now = LocalDateTime.now();
            Timestamp sqlNow = Timestamp.valueOf(now);
            Time sqlZero = Time.valueOf("00:00:00");
            pst.setTime(1,sqlZero);
            pst.setTimestamp(2,sqlNow);
            pst.executeUpdate();
        }
        catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
        }
    }

    //refactored version
    public String addBillboard(String billboardName, String backgroundColour, String messageColour, String infoColour,
                                  String messageData, String pictureData, String pictureURL, String infoData, String author)
    {
        //first step is to check if that billboard is in use
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM billboards WHERE billboardName = ?");
            pst.setString(1,billboardName);
            rs = pst.executeQuery();
            if(queryResultsNumber(rs) > 0){
                return("ERROR");
            }
            else
            {
                //lets see if any values are missing and need to be default
                if(backgroundColour.equals(""))
                {
                    backgroundColour="#FFFFFF";
                }
                if (messageColour.equals(""))
                {
                    messageColour="#000000";
                }
                if(infoColour.equals(""))
                {
                    infoColour="#000000";
                }
                //Lets add it
                PreparedStatement pst1 = con.prepareStatement("INSERT INTO billboards(billboardName, backgroundColour," +
                        "messageColour, infoColour, messageData, pictureData," +
                        "pictureURL, infoData, author) VALUES(?,?,?,?,?,?,?,?,?)");
                Blob pictureDataBlob = con.createBlob();
                //convert the picture data to a blob
                try {
                    byte[] byteData = pictureData.getBytes("UTF-8");
                    pictureDataBlob.setBytes(1,byteData);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                pst1.setString(1, billboardName);
                pst1.setString(2, backgroundColour);
                pst1.setString(3, messageColour);
                pst1.setString(4, infoColour);
                pst1.setString(5, messageData);
                pst1.setBlob(6, pictureDataBlob);
                pst1.setString(7, pictureURL);
                pst1.setString(8, infoData);
                pst1.setString(9, author);


                pst1.execute();

                System.out.println("Billboard has been added");
                return("OK");
            }

        }
        catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
        }
        return("Hello");
    }

    /**
     * Will add a billboard to the schedule when parsed the objects
     *
     * @param billboardName as a String
     * @param startTime in a java.time.localDatetime
     * @param duration in a java.time.Duration
     */
    public void addToSchedule(String billboardName, LocalDateTime startTime, Duration duration, String bookedBy) {
        Connection con;
        Statement st;
        ResultSet rs;
        try{
            Class.forName(MYSQL_DRIVER);
            con = DriverManager.getConnection(MYSQL_URL+"/"+MYSQL_SCHEMA, MYSQL_USER, MYSQL_PASS);
            PreparedStatement pst = con.prepareStatement("INSERT INTO schedule(billboardName,startTime,duration,endTime,bookedBy)VALUES(?,?,?,?,?)");

            //Convert all our java date time to SQL format
            LocalDateTime endTime = startTime.plus(duration);
            LocalTime durationTime = LocalTime.of(0,0);
            durationTime = durationTime.plus(duration);

            Timestamp sqlStartTime = Timestamp.valueOf(startTime);
            Timestamp sqlEndTime = Timestamp.valueOf(endTime);
            Time sqlDuration = Time.valueOf(durationTime);

            pst.clearParameters();
            pst.setString(1,billboardName);
            pst.setTimestamp(2,sqlStartTime);
            pst.setTime(3,sqlDuration);
            pst.setTimestamp(4,sqlEndTime);
            pst.setString(5,bookedBy);

            pst.execute();

            System.out.println("Schedule has been added");
          //  con.close();
        }
        catch(ClassNotFoundException ex) {
            System.err.println("ClassNotFoundException:\n"+ex.toString());
            ex.printStackTrace();

        }
        catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
        }
    }

    /**
     * Check the start time of a pre-existing billboard, used exclusively for a server unit test
     * assert that would have taken much longer to setup all the database connection stuff again
     */
    public String getScheduleStartTime(String billboardName){

        try{
            PreparedStatement pst = con.prepareStatement("SELECT startTime FROM schedule WHERE billboardName = ?");
            pst.setString(1, billboardName);
            rs = pst.executeQuery();

            if (queryResultsNumber(rs) > 0)
            {
                return("OK");
            }
            else{
                return("NOTHING");
            }

        }
        catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
        }


        return("ayy");
    }

    /**
     * Will find out what to play at the current time, checks the schedule and gets that billboard info
     */
    public String whatToPlay() {
        //first step is to see what is playing
        //Connection con;
        //Statement st;
        //ResultSet rs;

        String XML = "";
        String defaultBGColour = "#7AA1A1";
        String defaultColour = "#FFFFFF";
        //billboard info
        String billboardName = "";
        String pictureData = "";
        String pictureURL = "";
        String messageData = "";
        String infoData = "";
        String backgroundColour = "";
        String messageColour = "";
        String infoColour = "";
        String author = "";

        try {
            //Class.forName(MYSQL_DRIVER);
            //con = DriverManager.getConnection(MYSQL_URL + "/" + MYSQL_SCHEMA, MYSQL_USER, MYSQL_PASS);
            PreparedStatement pst = con.prepareStatement("SELECT * FROM schedule WHERE startTime <= ? AND endTime >= ?");
            LocalDateTime now = LocalDateTime.now();
            Timestamp sqlNow = Timestamp.valueOf(now);
            pst.setTimestamp(1,sqlNow);
            pst.setTimestamp(2,sqlNow);
            System.out.println(now);
            rs = pst.executeQuery();

            // default billboard
            if (queryResultsNumber(rs)==0){

//                DocumentBuilderFactory docbf = DocumentBuilderFactory.newInstance();
//                DocumentBuilder docb = docbf.newDocumentBuilder();
//                Document doc = docb.newDocument();
//
//                // billboard tag
//                Element bbTag = doc.createElement("billboard");
//                doc.appendChild(bbTag);
//
//                // message tag
//                Element msgTag = doc.createElement("message");
//                msgTag.appendChild(doc.createTextNode(defaultMSG));
//                bbTag.appendChild(msgTag);
//
//                TransformerFactory tf = TransformerFactory.newInstance();
//                Transformer t = tf.newTransformer();
//                StringWriter sWriter = new StringWriter();
//                StreamResult sResult = new StreamResult(sWriter);
//                DOMSource dSource = new DOMSource(doc);
//                t.transform(dSource, sResult);

//                XML = sWriter.toString();

                String defaultMSG = "Advertise here and gain more traction.";

                XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<billboard background=\"" + defaultBGColour +"\"><message colour=\"" + defaultColour + "\">"
                        + defaultMSG + "</message></billboard>";
                return XML;

            } else {//take that billboards name and get its info
                rs.next();
                String billboardToDisplay = rs.getString("billboardName");
                System.out.println(billboardToDisplay);

                //lets query that to get the rest of what we need
                PreparedStatement pst2 = con.prepareStatement("SELECT * FROM billboards WHERE billboardName = ?");
                pst2.setString(1,billboardToDisplay);
                rs = pst2.executeQuery();
                rs.next();
                billboardName = rs.getString("billboardName");
                Blob ablob = rs.getBlob("pictureData");
                pictureData = new String(ablob.getBytes(1l,(int) ablob.length()));
                Blob bblob = rs.getBlob("pictureURL");
                pictureURL = new String(bblob.getBytes(1l,(int) bblob.length()));
                messageData = rs.getString("messageData");
                infoData = rs.getString("infoData");
                backgroundColour = rs.getString("backgroundColour");
                messageColour = rs.getString("messageColour");
                infoColour = rs.getString("infoColour");
                author = rs.getString("author");

//                System.out.println("~~~~~~~~~ info to turn into an xml ~~~~~~");
//                System.out.println(pictureData);
//                System.out.println(pictureURL);
//                System.out.println(messageData);
//                System.out.println(infoData);
//                System.out.println(backgroundColour);
//                System.out.println(messageColour);
//                System.out.println(infoColour);
//                System.out.println(author);
//                System.out.println("~~~~~~~~~~~~~~ end of xml ~~~~~~~~~~~~~~~~~");


                XML = "<?xml version = \"1.0\" encoding=\"UTF-8\"?>";

                if (!backgroundColour.equals("")) {
                    XML += "<billboard background=\"" + backgroundColour + "\">";
                }

                if (backgroundColour.equals("")) {
                    XML += "<billboard background=\"" + defaultBGColour + "\">";
                }

                if (messageColour.equals("") && !messageData.equals("")) {
                    // default colour
                    String msgNoColour = "<message colour=\"" + defaultColour + "\">" + messageData + "</message>";
                    XML += msgNoColour;
                }

                if (!messageColour.equals("") && !messageData.equals("")) {
                    String msgAndColour = "<message colour=\"" + messageColour + "\">" + messageData + "</message>";
                    XML += msgAndColour;
                }

                if (!pictureData.equals("")) {
                    String picDataTag = "<picture data=\"" + pictureData + "\"/>";
                    XML += picDataTag;
                }

                if (!pictureURL.equals("")) {
                    String picURLTag = "<picture url=\"" + pictureURL + "\"/>";
                    XML += picURLTag;
                }

                if (!infoData.equals("") && infoColour.equals("")) {
                    // default colour
                    String infoNoColour = "<information colour=\"" + defaultColour + "\">" + infoData + "</information>";
                    XML += infoNoColour;
                }

                if (!infoData.equals("") && !infoColour.equals(""))  {
                    // both are present
                    String infoAndColour = "<information colour=\"" + infoColour + "\">" + infoData + "</information>";
                    XML += infoAndColour;
                }

                // close tag
                XML += "</billboard>";

                System.out.println(XML);
                return XML;
            }
        }

        catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
        }
        return XML;
    }

    /**
     * Gets the list of billboards created by that user
     * @param username the username do be queried
     * @return
     */
    public String getBillboardListForUser(String username){

        try{
            PreparedStatement pst = con.prepareStatement("SELECT * FROM billboards WHERE author = ?");
            pst.setString(1,username);
            rs = pst.executeQuery();
            int noBillboards = queryResultsNumber(rs);
            String billboardList = "";
            if (noBillboards > 0)
            {
                for(int x = 0;x<noBillboards;x++)
                {
                    rs.next();
                    String curr = rs.getString("billboardName");
                    billboardList = billboardList + curr + "|";
                }
                billboardList = billboardList.substring(0,billboardList.length() -1);
                return billboardList;
            }
            else{
                return("NOTHING");
            }

        }
        catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
        }


        return("ayy");
    }

    /**
     * Called by the middleware to get a list of all the billboards stored
     *
     * returns: BillboardA|BillboardB|BillboardC etc....
     */

    public String getBillboardListAll(){

        try{
            PreparedStatement pst = con.prepareStatement("SELECT * FROM billboards");
            rs = pst.executeQuery();
            int noBillboards = queryResultsNumber(rs);
            String billboardList = "";
            if (noBillboards > 0)
            {
                for(int x = 0;x<noBillboards;x++)
                {
                    rs.next();
                    String curr = rs.getString("billboardName");
                    billboardList = billboardList + curr + "|";
                }
                billboardList = billboardList.substring(0,billboardList.length() -1);
                return billboardList;
            }
            else{
                return("NOTHING");
            }

        }
        catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
        }


        return("ayy");
    }

    /**
     * Gets the details of the scheduled billboards
     * @return returns the scheduled billboards
     */
    public String getScheduleList(){
        try{
            PreparedStatement pst = con.prepareStatement("SELECT * FROM schedule");
            rs = pst.executeQuery();
            int noSchedule = queryResultsNumber(rs);
            String billboardList = "";
            if (noSchedule > 0)
            {
                for(int x = 0;x<noSchedule;x++)
                {
                    rs.next();
                    String curr = rs.getString("billboardName");
                    LocalDateTime t = rs.getTimestamp("startTime").toLocalDateTime();
                    String booker = rs.getString("bookedBy");
                    String time = t.toString();
                    time = time.replace("T"," ");
                    System.out.println(time);
                    billboardList = billboardList + curr +" ~ "+time+"~"+booker+"|";
                }
                billboardList = billboardList.substring(0,billboardList.length() -1);
                return billboardList;
            }
            else{
                return("NOTHING");
            }

        }
        catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
        }
        return "hi";
    }

    /**
     * Called by the middleware to get a list of all the users stored
     *
     * returns: user1|user2|user3 etc....
     * As well as there perms
     *
     */
    public String getUsersList(){
        try{
            PreparedStatement pst = con.prepareStatement("SELECT * FROM users");
            rs = pst.executeQuery();
            int noUsers = queryResultsNumber(rs);
            String userList = "";
            if (noUsers > 0)
            {
                for(int x = 0;x<noUsers;x++)
                {
                    rs.next();
                    String curr = rs.getString("userName");
                    int p1 = rs.getInt("editUsersPerm");
                    int p2 = rs.getInt("scheduleBillboardsPerm");
                    int p3 = rs.getInt("createBillboardsPerm");
                    int p4 = rs.getInt("editAllBillboardsPerm");
                    userList = userList + curr + "~"+p1+"~"+p2+"~"+p3+"~"+p4+ "|";
                }
                userList = userList.substring(0,userList.length() -1);
                return userList;
            }
            else{
                return("NOTHING");
            }

        }
        catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
        }
        return("user list");
    }
    /**
     * updates a users permissions
     * @param rUser requesting user
     * @param tUser target user
     * @param p1 edit users perms
     * @param p2 schedule billboard perm
     * @param p3 create billboard
     * @param p4 editAllBillboards
     * @return
     */
    public String updateUserPerms(String rUser,String tUser, int p1,int p2, int p3,int p4){

        //lets check if they are trying to edit themeselves
        try{
            if(rUser == tUser)
            {
                //they are not allowed to touch there own edit users
                PreparedStatement pst = con.prepareStatement("UPDATE users SET scheduleBillboardsPerm = ?, createBillboardsPerm = ?, editAllBillboardsPerm = ? WHERE userName = ?");
                pst.setInt(1, p2);
                pst.setInt(2, p3);
                pst.setInt(3, p4);
                pst.setString(4, tUser);
                pst.executeUpdate();
            }
            else {
                // we are free to edit perms
                PreparedStatement pst = con.prepareStatement("UPDATE users SET editUsersPerm = ?, scheduleBillboardsPerm = ?, createBillboardsPerm = ?, editAllBillboardsPerm = ? WHERE userName = ?");
                pst.setInt(1, p1);
                pst.setInt(2, p2);
                pst.setInt(3, p3);
                pst.setInt(4, p4);
                pst.setString(5, tUser);
                pst.executeUpdate();
            }


            System.out.println("Perms for "+tUser+" edited:"+p1+","+p2+","+p3+","+p4);
        }
        catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
        }
        return("OK");
    }

    /**
     *
     * @param rUser the requesting user
     * @param tUser the target user
     * @param newPassword a password hash of the new password
     * @return DENIED if the user does not have permissions, OK if they password is updated
     */
    public String updatePassword(String rUser,String tUser,String newPassword){
        //check if someone is editing another user

        System.out.println("req:"+rUser);
        System.out.println("t:"+tUser);

        if(!rUser.equals(tUser))
        {
            //see if they are allowed
            if(!validatePerms(rUser,"editUsers"))
            {
                return("DENIED");
            }
        }
        //otherwise is it ok to update the password
        jake.server.Password pass = new jake.server.Password();
        //new salt
        byte[] saltByte = pass.generateSalt();
        String salt = pass.toHexString(saltByte);
        String pass_salt = newPassword+salt;
        try {
            byte[]finalHash = pass.hash(pass_salt);
            String final_pass = pass.toHexString(finalHash);

            //new salt and password generated lets update the database
            PreparedStatement pst = con.prepareStatement("UPDATE users SET password = ?, salt = ? WHERE userName = ?");
            pst.setString(1,final_pass);
            pst.setString(2,salt);
            pst.setString(3,tUser);

            pst.executeUpdate();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
        }


        return("OK");
    }

    /**
     * Returns all the details about a billboard given the name
     * @param name of the billboard to get information of
     * @return a string "billboard colour | billboard message | etc.. "
     */
    public String getBillboardInfo(String name){
        try{
            PreparedStatement pst = con.prepareStatement("SELECT * FROM billboards WHERE billboardName = ?");
            pst.setString(1,name);
            rs = pst.executeQuery();
            rs.next();

            String billboardName = "";
            String pictureData = "";
            String pictureURL = "";
            String messageData = "";
            String infoData = "";
            String backgroundColour = "";
            String messageColour = "";
            String infoColour = "";
            String author = "";
            billboardName = rs.getString("billboardName");
            Blob ablob = rs.getBlob("pictureData");
            pictureData = new String(ablob.getBytes(1l,(int) ablob.length()));
            Blob bblob = rs.getBlob("pictureURL");
            pictureURL = new String(bblob.getBytes(1l,(int) bblob.length()));
            messageData = rs.getString("messageData");
            infoData = rs.getString("infoData");
            backgroundColour = rs.getString("backgroundColour");
            messageColour = rs.getString("messageColour");
            infoColour = rs.getString("infoColour");
            author = rs.getString("author");

            String billboardResult="";
            billboardResult=billboardResult+billboardName+"|";
            if(!pictureData.equals("")){
                billboardResult=billboardResult+pictureData+"|";
            }
            else{
                billboardResult=billboardResult+"BLANK"+"|";
            }
            if(!pictureURL.equals("")){
                billboardResult=billboardResult+pictureURL+"|";
            }
            else{
                billboardResult=billboardResult+"BLANK"+"|";
            }
            if(!messageData.equals("")){
                billboardResult=billboardResult+messageData+"|";
            }
            else{
                billboardResult=billboardResult+"BLANK"+"|";
            }
            if(!infoData.equals("")){
                billboardResult=billboardResult+infoData+"|";
            }
            else{
                billboardResult=billboardResult+"BLANK"+"|";
            }
            if(!backgroundColour.equals("")){
                billboardResult=billboardResult+backgroundColour+"|";
            }
            else{
                billboardResult=billboardResult+"BLANK"+"|";
            }
            if(!messageColour.equals("")){
                billboardResult=billboardResult+messageColour+"|";
            }
            else{
                billboardResult=billboardResult+"BLANK"+"|";
            }
            if(!infoColour.equals("")){
                billboardResult=billboardResult+infoColour+"|";
            }
            else{
                billboardResult=billboardResult+"BLANK"+"|";
            }
            billboardResult = billboardResult+author;
            return(billboardResult);
        }
        catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
        }

        return("hi");
    }

    /**
     * Updates a billboard in the database with the new infomation
     * @param newUpdate a string of information about the new billboard "colour|picture|etc..."
     * @return "OK" or "DENIED" if the user has permission
     */
    public String editBillboard(String newUpdate)
    {
        String[] blocks = newUpdate.split("\\|");

        String billboardName,backgroundColour, messageColour, infoColour, messageData, pictureData,pictureURL,infoData,author;

        String[] place = new String[9];

        for(int x = 0; x < blocks.length;x++)
        {

            if (blocks[x].equals("BLANK")) //5 , 6 ,7 colours
            {
                place[x]="";
            }
            else
            {
                place[x]=blocks[x];
            }

        }

        billboardName = place[0];
        pictureData  = place[4];
        pictureURL = place[5];
        messageData = place[3];
        infoData = place[7];
        backgroundColour = place[1];
        messageColour = place[2];
        infoColour= place[6];
        //author= place[8];
        if(backgroundColour.equals(""))
        {
            backgroundColour="#FFFFFF";
        }
        if (messageColour.equals(""))
        {
            messageColour="#000000";
        }
        if(infoColour.equals(""))
        {
            infoColour="#000000";
        }

        //outtest
        System.out.println("Updating:"+billboardName+" with: ");
        for (int x = 0;x<place.length;x++)
        {
            System.out.println(place[x]);
        }

        //time to update it
        try {

            Blob pictureDataBlob = con.createBlob();
            //convert the picture data to a blob
            try {
                byte[] byteData = pictureData.getBytes("UTF-8");
                pictureDataBlob.setBytes(1,byteData);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            PreparedStatement pst = con.prepareStatement("UPDATE billboards SET pictureData = ?, pictureURL = ?,messageData=?,infoData=?,backgroundColour=?,messageColour=?,infoColour=? WHERE billboardName = ?");
            //pst.setString(1,pictureData);
            pst.setString(2,pictureURL);
            pst.setBlob(1,pictureDataBlob);
            pst.setString(3,messageData);
            pst.setString(4,infoData);
            pst.setString(5,backgroundColour);
            pst.setString(6,messageColour);
            pst.setString(7,infoColour);
            pst.setString(8,billboardName);
            pst.executeUpdate();
        }
        catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
        }

        return("OK");
    }

    /**
     * Delete a user from the users database
     * @param rUser request user
     * @param tUser target user
     * @return Denied if the user does not have permission to do so, OK if the user is deleted
     */
    public String deleteUser(String rUser, String tUser)
    {
        try{
            if (rUser.equals(tUser))
            {
                return ("ERROR"); //cant delete yourself
            }
            else {
                PreparedStatement pst = con.prepareStatement("DELETE FROM users WHERE userName = ?");
                pst.setString(1,tUser);
                pst.executeUpdate();

                PreparedStatement pst1 = con.prepareStatement("UPDATE billboards SET author = 'DELETED_USER' WHERE author =?");
                pst1.setString(1,tUser);
                pst1.executeUpdate();

                return("OK");
            }

        }
        catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
        }
        return "hi";
    }

    /**
     * Method to delete a billboard from the billboards table
     * @param billboard, name of the billboard to delete
     * @return Error if the billboard is scheduled, OK if all good
     */
    public String deleteBillboard(String billboard)
    {
        //check if its scheduled
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM SCHEDULE WHERE billboardName = ?");
            pst.setString(1, billboard);
            if(queryResultsNumber(pst.executeQuery())>0)
            {
                //its scheduled so we are not allowed to delete it
                return("ERROR");
            }
            else {
                //the billboard isnt scheduled so we cant delete it
                PreparedStatement pst1 = con.prepareStatement("DELETE FROM billboards WHERE billboardName = ?");
                pst1.setString(1,billboard);
                pst1.executeUpdate();
                return("OK");
            }
        }catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
        }
        return "OK";
    }

    /**
     * Delete a billboard from the table, and remove all scheduling of it
     * @param billboard
     * @return
     */
    public String deleteBillboardAndSchedule(String billboard){
        try {
            PreparedStatement pst = con.prepareStatement("DELETE FROM schedule WHERE billboardName = ?");
            PreparedStatement pst1 = con.prepareStatement("DELETE FROM billboards WHERE billboardName = ?");
            pst1.setString(1,billboard);
            pst.setString(1,billboard);
            pst1.executeUpdate();
            pst.executeUpdate();

        }catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
        }
        return "OK";

    }

    /**
     * Delete a schedule
     * @param start starting Date time of the schedule to be deleted
     * @param name name of the billboard to be deleted
     * @return OK confirmation that the billboard has been deleted
     */
    public String deleteSchedule(LocalDateTime start,String name)
    {
        try {
            PreparedStatement pst = con.prepareStatement("DELETE FROM schedule WHERE billboardName = ? AND startTime = ?");
            pst.setString(1,name);

            Timestamp sqlStartTime = Timestamp.valueOf(start);

            pst.setTimestamp(2,sqlStartTime);
            pst.executeUpdate();
        }
        catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
        }


        return "OK";
    }

    /**
     *
     * @return returns an array of the Schedule Day class containing information about
     * the information for the next week.
     */
    public ScheduleDay[] calendar()
    {
        //lets declare the week
        ScheduleDay[] week = new ScheduleDay[7];

        String sqlStart = "8:00";
        String sqlEnd = "17:50";

        for(int i=0;i<week.length;i++)
        {
            LocalTime start = LocalTime.of(8,0);
            LocalTime end = LocalTime.of(17,0);
            LocalDate today = LocalDate.now();
            LocalDateTime dayS=today.atTime(start);
            LocalDateTime dayE=today.atTime(end);
            dayS = dayS.plusDays(i);
            dayE = dayE.plusDays(i);

            LocalDate forStamp = today.plusDays(i);

            try {

                Timestamp sqlTodays = Timestamp.valueOf(dayS);
                Timestamp sqlTodaye = Timestamp.valueOf(dayE);
                System.out.println(sqlTodays);
                System.out.println(sqlTodaye);

            //    PreparedStatement pst = con.prepareStatement("SELECT * FROM schedule WHERE (startTime >= ? AND endTime <=?) OR (endTime>=? AND endTime<=?) OR (startTime<= ?)");


                Statement stmt = con.createStatement();
                //String query = "SELECT * FROM schedule WHERE (startTime >= '"+sqlTodays+"' AND endTime<= '"+sqlTodaye+"' )";
                String query = "SELECT * FROM schedule WHERE (startTime >= '"+sqlTodays+"' AND endTime<= '"+sqlTodaye+"' ) OR (endTime>= '"+sqlTodays+"' AND endTime <= '"+sqlTodaye+"' ) OR (startTime<= '"+sqlTodaye+"' AND startTime >= '"+sqlTodays+"')";
                rs = stmt.executeQuery(query);



                int length = queryResultsNumber(rs);
                String[] names = new String[length];
                LocalTime[] s = new LocalTime[length];
                LocalTime[]e = new LocalTime[length];
                String[] bookingList = new String[length];

                int z = 0;
                while(rs.next())
                {
                    names[z]=rs.getString("billboardName");
                    s[z]=rs.getTimestamp("startTime").toLocalDateTime().toLocalTime();
                    e[z] = rs.getTimestamp("endTime").toLocalDateTime().toLocalTime();



                    //bookingList[z] = rs.getString("bookedBy");

                    //change this for the author
                    PreparedStatement ps2 = con.prepareStatement("SELECT * FROM billboards WHERE billboardName = ?");
                    ps2.setString(1,names[z]);
                    ResultSet r2;
                    r2 = ps2.executeQuery();
                    r2.next();
                    String author = r2.getString("author");
                    bookingList[z] = author;



                    z++;
                }

                //System.out.println(week[i]);


                ScheduleDay temp = new ScheduleDay();
                temp.setNames(names);
                temp.setStartTimes(s);
                temp.setEndTimes(e);
                temp.setDate(forStamp);
                temp.setupPixels();
                temp.setBookedBy(bookingList);
                temp.printDay();
                week[i]=temp;




            }catch(SQLException ex) {
            System.err.println("SQLException:\n"+ex.toString());
            }

            //lets draw it


        }
        //to call it
        //CGTemplate cal = new CGTemplate(week);
        return week;

    }

    private static int portNum;

    /***
     * Returns an int that is used for listening/communicating on a port.
     * The port number is set within src/jake/server/properties file
     * @return returns an int
     */
    public static int getPort() {
        try
        {
            Properties props = new Properties();
            FileInputStream in = new FileInputStream("src/properties");
            props.load(in);
            portNum = Integer.parseInt(props.getProperty("port"));
            return portNum;
        } catch (IOException e) {
            e.printStackTrace();

        }
        return 0;

    }

    public void startListening() throws IOException {
        // server is listening on port number set in properties file
        ServerSocket ss = new ServerSocket(getPort());
        System.out.println("Listening on Port: " + getPort());
        // running infinite loop for getting
        // client request
        while (true)
        {
            Socket s = null;

            try
            {
                // socket object to receive incoming client requests
                s = ss.accept();
                System.out.println("new connection : " + s);
                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                System.out.println("make a new thread");
                // create a new thread object
                Thread t = new ClientHandler(s, dis, dos,oos);
                // Invoking the start() method
                t.start();
            }
            catch (Exception e){
                s.close();
                e.printStackTrace();
            }

        }
    }

}