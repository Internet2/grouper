/*
 * Copyright (C) 2004-2005 The University Of Chicago
 * All Rights Reserved. 
 *
 * You may use and distribute under the same terms as Grouper itself
 */


import  java.io.*;
import  java.sql.*;
import  java.util.*;
import  org.apache.commons.cli.*;


/**
 * Sample loader for populating the <i>Subject</i> and
 * <i>SubjectAttribute</i> tables from a CSV input file.
 * <p />
 * See <i>README</i> for more information.
 * 
 * @author  blair christensen.
 * @version $Id: csv2subject.java,v 1.19 2005-09-08 01:30:55 blair Exp $ 
 */
class csv2subject {

  /*
   * PRIVATE CONSTANTS
   */
  private static final String CF    = "hibernate.properties";
  private static final String NAME  = "csv2subject";
  private static final String TABLE = "Subject";


  /*
   * PRIVATE CLASS VARIABLES
   */
  private static CommandLine  cmd;
  private static Properties   conf          = new Properties();
  private static Connection   conn;
  private static boolean      debug         = false;
  private static String       jdbcDriver    = new String();
  private static String       jdbcURL       = new String();
  private static String       jdbcUsername  = new String();
  private static String       jdbcPassword  = new String();
  private static Options      options;
  private static String       path;
  private static Map          subjects      = new HashMap();
  private static boolean      verbose       = false;
  private static PreparedStatement st_desc, st_login, st_subj;


  /*
   * PUBLIC CLASS METHODS
   */
  public static void main(String[] args) {
    _opts(args);        // Parse and handle command line options
    _cfRead();          // Read configuration
    _jdbcConnect();     // Open JDBC connection
    _csvRead();         // Read and parse input file
    _csvAdd();          // Add subjects
    _jdbcDisconnect();  // Close JDBC connection
  }


  /*
   * PRIVATE CLASS METHODS
   */

  /* 
   * Read configuration file.
   */
  private static void _cfRead() {
    InputStream in = csv2subject.class
                                .getResourceAsStream("/" + CF);
    try { 
      conf.load(in);
    } catch (IOException ie) { 
      System.err.println("Unable to read '" + CF + "'");
      System.exit(1);
    }
    jdbcDriver    = (String) conf.get("hibernate.connection.driver_class");
    jdbcURL       = (String) conf.get("hibernate.connection.url");
    jdbcUsername  = (String) conf.get("hibernate.connection.username");
    jdbcPassword  = (String) conf.get("hibernate.connection.password");
    _verbose("driver    " + jdbcDriver);
    _verbose("url       " + jdbcURL);
    _verbose("username  " + jdbcUsername);
    _verbose("password  " + jdbcPassword);
  }

  /* 
   * Add CSV contents to registry.
   */
  private static void _csvAdd() {
    if (subjects != null) {
      Set keys = subjects.keySet();
      _verbose("There are " + keys.size() + " subjects to add.");
      Iterator iter = keys.iterator();
      while (iter.hasNext()) {
        String  key   = (String)  iter.next();
        Map     attrs = (Map)     subjects.get(key);
        String  msg   = 
          " " + attrs.get("type") + ":" + (String) attrs.get("id");
        if ( _addSubject(key, attrs) ) {
          _verbose("Added"+msg);
        } else {
          _verbose("Unable to add"+msg);
        } // if (_addSubject(keys, attrs))
      } // while (iter.hasNext())
    } // if (subjects != null)
  } // private static void _csvAdd()

  /* 
   * Read in CSV file
   */
  private static void _csvRead() {
    if (path != null) {
      try { 
        BufferedReader  br    = new BufferedReader(new FileReader(path));
        String          line  = null; 
        while ((line=br.readLine()) != null){ 
          _parseCSV(line);
        } // while ((line=br.readLine()) != null
        br.close(); 
      } catch (IOException e) { 
        System.err.println("Error processing '" + path + "': " + e);
      } // try 
    } else {
      _usage();
    } // if (path != null)
  } // private static void _csvRead

  /* 
   * Conditionally print SQL messages depending upon verbosity level.
   */
  private static void _debug(String msg) {
    if (debug == true) {
      System.err.println(msg);
    }
  }

  /*
   * Initialize JDBC connection.
   */
  private static void _jdbcConnect() {
    try {
      Class.forName(jdbcDriver).newInstance();
      try {
        conn = DriverManager.getConnection(
          jdbcURL, jdbcUsername, jdbcPassword
        );
        _verbose("Connected to " + jdbcURL);
        // Prepare statements for later insertion 
        st_subj   = conn.prepareStatement(
          "INSERT INTO SUBJECT"
          + " (subjectID, subjectTypeID, name, description)"
          + " VALUES (?, ?, ?, ?)"
        );
        st_desc   = conn.prepareStatement(
          "INSERT INTO SUBJECTATTRIBUTE"
          + " (subjectID, name, value, searchValue)" 
          + " VALUES (?, 'description', ?, ?)"
        );
        st_login  = conn.prepareStatement(
          "INSERT INTO SUBJECTATTRIBUTE"
          + " (subjectID, name, value, searchValue)" 
          + " VALUES (?, 'loginid', ?, ?)"
        );
      } catch (SQLException se) {
        System.err.println("Unable to connect: " + se);
        System.exit(1);
      }
    } catch(ClassNotFoundException ce) {
      System.err.println("Unable to find class '" + jdbcDriver + "'");
      System.exit(1);
    } catch(InstantiationException ie) {
      System.err.println(
        "Unable to instantiate class '" + jdbcDriver + "'"
      );
      System.exit(1);
    } catch(IllegalAccessException iae) {
      System.err.println("Unable to access class '" + jdbcDriver + "'");
      System.exit(1);
    }
  }

  /*
   * Close JDBC connection.
   */
  private static void _jdbcDisconnect() {
    if (conn != null) { 
      try {
        conn.close();
        _verbose("JDBC connection closed");
      } catch (SQLException ce) {
        System.err.println("Unable to close JDBC connection: " + ce);
        System.exit(1);
      } // try
    } // if (conn != null)
  } // private static void _jdbcDisconnect()

  /*
   * Handle command line options.
   */
  private static void _opts(String[] args) {
    _optsParse(args); // Parse CLI options
    _optsProcess();   // Handle CLI options
  }

  /* 
   * Parse command line options.
   */
  private static void _optsParse(String[] args) {
    options = new Options();
    options.addOption("d", false, "Be more verbose about SQL");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("file");
    OptionBuilder.withDescription("Specify input file [REQUIRED]");
    options.addOption( OptionBuilder.create("f") );
    options.addOption("h", false, "Print usage information");
    options.addOption("v", false, "Be more verbose");
    CommandLineParser parser = new PosixParser();
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.err.println("Unable to parse command line options: " + e.getMessage());
      System.exit(1);
    }
  }

  /* 
   * Process command line options.
   */
  private static void _optsProcess() {
    if (cmd == null) {
      System.err.println("Error parsing command line options!");
      System.exit(1);
    }
    // Handle help first
    if (cmd.hasOption("h")) {
      _usage();
      System.exit(0);
    }
    // And then verbose because it may affect output later in this
    // method
    if (cmd.hasOption("v")) {
      verbose = true;
      _verbose("Enabling verbose mode");
    }
    // And now everything else
    if (cmd.hasOption("d")) {
      debug = true;
      _verbose("Enabling SQL debug mode");
    }
    if (cmd.hasOption("f")) {
      path = cmd.getOptionValue("f");
      _verbose("Using input file '" + path + "'");
    }
  }

  /*
   * parse a line of .csv input
   */
  private static void _parseCSV(String line) {
    StringTokenizer st = new StringTokenizer(line, ",");
    // TODO Yuck
    if (st.countTokens() >= 2) {
      Map     attrs = new HashMap();
      String desc, id, key, login, name, type;
      id    = st.nextToken();
      type  = st.nextToken();
      key   = type + ":" + id;
      attrs.put("id", id);
      attrs.put("type", type);
      if (st.hasMoreTokens()) {
        name = st.nextToken();
        if (st.hasMoreTokens()) {
          login = st.nextToken();
        } else {
          login = id;
        } // if (st.hasMoreTokens())
        desc = name;
      } else {
        desc = login = name = id;
      } // if (st.hasMoreTokens())
      attrs.put("desc" , desc);
      attrs.put("login", login);
      attrs.put("name" , name);
      if (st.hasMoreTokens()) {
        System.err.println(
          "WARNING: Too many input elements in '" + line + "'"
        );
      } // if (st.hasMoreTokens())
      String[] desc_ary = desc.split("\\s+", 2);
      if (desc_ary.length > 1) {
        attrs.put( "desc_v" , desc_ary[1] + ", " + desc_ary[0] );
        attrs.put( 
          "desc_sv", 
          desc_ary[1].toLowerCase() + " "  + desc_ary[0].toLowerCase() 
        );
      } else {
        attrs.put( "desc_v" , desc );
        attrs.put( "desc_sv", desc.toLowerCase() );
      } // if (desc_ary.length > 1)
    subjects.put(key, attrs);
    } // if (st.countTokens() >= 2)
  } // private static void _parseCSV

  /* 
   * Add subject 
   */
  private static boolean _addSubject(String key, Map attrs) {
    boolean rv = false;
    try {
      conn.setAutoCommit(false);
      // Subject
      st_subj.setString(  1, (String) attrs.get("id")      );
      st_subj.setString(  2, (String) attrs.get("type")    );
      st_subj.setString(  3, (String) attrs.get("name")    );
      st_subj.setString(  4, (String) attrs.get("desc")    );
      st_subj.executeUpdate();
      // Attribute: description
      st_desc.setString(  1, (String) attrs.get("id")      );
      st_desc.setString(  2, (String) attrs.get("desc_v")  );
      st_desc.setString(  3, (String) attrs.get("desc_sv") );
      st_desc.executeUpdate();
      // Attribute: loginid
      st_login.setString( 1, (String) attrs.get("id")      );
      st_login.setString( 2, (String) attrs.get("login")   );
      st_login.setString( 3, (String) attrs.get("login")   );
      st_login.executeUpdate();
      conn.commit();
      conn.setAutoCommit(true);
      rv = true;
    } catch (SQLException e) {
      System.err.println(
        "Error adding subject: " + key + " " + e.getMessage()
      );
    }
    return rv;
  }

  /* 
   *
   * Print usage information.
   */
  private static void _usage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(NAME, options);
  }

  /*
   * Conditionally print messages depending upon verbosity level.
   */
  private static void _verbose(String msg) {
    if (verbose == true) {
      System.err.println(msg);
    }
  }

}

