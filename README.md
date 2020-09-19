# Electronic Billboard Application
Software Development Project

A set of three Java applications the make an Electronic Billboard
Display and Management System.

• Application #1 is the Billboard Viewer, described in more detail below. It will connect over a
network (LAN or internet) to the Billboard Server and display whatever the server tells it to,
without requiring further user involvement. This will be a GUI application using Java Swing.

• Application #2 is the Billboard Server, also described in more detail below. The Billboard
Viewer and Billboard Administrator will connect to it over LAN or internet. It will connect to
a MariaDB database via JDBC. The database will store information about the contents of
billboards, their scheduling, and user information used by the billboard administrator. This
will be a command-line application that will run on a server and will not be interacted with
directly by users.

• Application #3 is the Billboard Control Panel, also described in more detail below. This will
be another client-facing GUI application using Java Swing, but it will be used to configure the
billboard system— adding new billboard designs and configuring when they are displayed
