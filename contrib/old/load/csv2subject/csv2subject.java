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
 * Sample laoder for populating the <i>grouper_subject</i> table from a
 * CVS input file.
 * <p />
 * See <i>README</i> for more information.
 * 
 * @author  blair christensen.
 * @version $Id: csv2subject.java,v 1.16 2005-07-15 15:04:20 blair Exp $ 
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
  private static Map          newSubs       = new HashMap();
  private static Options      options;
  private static String       path;
  private static boolean      verbose       = false;



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

  /* (!javadoc)
   * Read configuration file.
   * <p />
   * @return Boolean true if succesful.
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
    } catch (FileNotFoundException fe) {
      System.err.println("Could not find '" + CF + "'");
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

  /* (!javadoc)
   * Add CSV contents to registry.
   */
  private static void _csvAdd() {
    if (newSubs != null) {
      Set keys = newSubs.keySet();
      _verbose("There are " + keys.size() + " subjects to add.");
      Iterator iter = keys.iterator();
      while (iter.hasNext()) {
        String subjID     = (String) iter.next();
        String subjTypeID = (String) newSubs.get(subjID);
        if (_sqlAdd(subjID, subjTypeID)) {
          _verbose(
            "Added sid=`" + subjID + "', subjTypeID=`" + 
            subjTypeID + "'"
          );
        } else {
          _verbose(
            "Unable to add sid=`" + subjID + "', subjTypeID=`" + 
            subjTypeID + "'"
          );
        }
      }
    } else {
      _verbose("Subject list is not valid.");
    }
  }

  /* (!javadoc)
   * Read in CSV file
   * <p />
   * @param path Path to input CSV file
   */
  private static void _csvRead() {
    if (path != null) {
      try { 
        BufferedReader  br    = new BufferedReader(new FileReader(path));
        String          line  = null; 
        while ((line=br.readLine()) != null){ 
          StringTokenizer st = new StringTokenizer(line, ",");
          // FIXME Blindly assume that if we have two tokens, they are two
          //       *good* tokens
          if (st.countTokens() == 2) {
            String subjID     = st.nextToken();
            String subjTypeID = st.nextToken();
            _verbose(
              "Found sid=`" + subjID + "', subjTypeID=`" + subjTypeID + "'"
            );
            newSubs.put(subjID, subjTypeID);
          } else {
            System.err.println("Skipping.  Invalid format: '" + line + "'");
          }
        } 
        br.close(); 
      } catch (IOException e) { 
        System.err.println("Error processing '" + path + "': " + e);
        // Kill whatever might have been added to the hashmap and then
        // carry on so that our connection is closed
        newSubs = new HashMap();
      }
    } else {
      _usage();
    }
  }

  /* (!javadoc)
   *
   * Conditionally print SQL messages depending upon verbosity level.
   * <p />
   * @param   msg Message to print if running verbosely.
   */
  private static void _debug(String msg) {
    if (debug == true) {
      System.err.println(msg);
    }
  }

  /* (!javadoc)
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

  /* (!javadoc)
   * Close JDBC connection.
   */
  private static void _jdbcDisconnect() {
    if (conn != null) { 
      try {
        conn.commit();
        _verbose("JDBC commit performed");
        try {
          conn.close();
          _verbose("JDBC connection closed");
        } catch (SQLException ce) {
          System.err.println("Unable to close JDBC connection: " + ce);
          System.exit(1);
        }
      } catch (SQLException come) {
        System.err.println("Unable to perform JDBC commit: " + come);
        System.exit(1);
      }
    }
  }

  /* (!javadoc)
   * Handle command line options.
   * <p />
   * @param   String Array.
   */
  private static void _opts(String[] args) {
    _optsParse(args); // Parse CLI options
    _optsProcess();   // Handle CLI options
  }

  /* (!javadoc)
   * Parse command line options.
   * <p />
   * @param   String array.
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

  /* (!javadoc)
   *
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

  /* (!javadoc)
   * Add subject 
   */
  private static boolean _sqlAdd(String subjID, String subjTypeID) {
    boolean rv = false;
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      try {
        String insert = "INSERT INTO " + TABLE        +
                        "(subjectID, subjectTypeID) " +
                        "VALUES ("                    +
                        "'" + subjID      + "', "     +
                        "'" + subjTypeID  + "'"       +
                        ")";
        int cnt = stmt.executeUpdate(insert);
        if (cnt == 1) {
          _debug("Added 1 row to '" + TABLE + "'");
          rv = true;
        }
      } catch (SQLException eue) {
        _debug("Error executing statement: " + eue);
      }
    } catch (SQLException cse) {
      System.err.println("Error creating insert statement: " + cse);
    }
    return rv;
  }

  /* (!javadoc)
   *
   * Print usage information.
   */
  private static void _usage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(NAME, options);
  }

  /* (!javadoc)
   *
   * Conditionally print messages depending upon verbosity level.
   * <p />
   * @param   msg Message to print if running verbosely.
   */
  private static void _verbose(String msg) {
    if (verbose == true) {
      System.err.println(msg);
    }
  }

}

