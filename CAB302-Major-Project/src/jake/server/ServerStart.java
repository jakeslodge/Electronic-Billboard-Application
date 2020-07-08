package jake.server;

import ControlPanel.ServerConnection;
import com.sun.tools.jconsole.JConsoleContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.time.*;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;

public class ServerStart {
    public static void main(String[] args) throws IOException {
        Server myServer = new Server();
        myServer.readProps();
        myServer.createTables();
        myServer.setupDBConnections();
        myServer.createNewUser("admin","8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918",1,1,1,1);

        //socket listening
        myServer.startListening();
    }
}
