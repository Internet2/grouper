package edu.internet2.middleware.directory.grouper;

import java.io.*;
import java.sql.*;
import java.util.Properties;

/** 
 * Provides a Grouper environment.
 *
 * @author blair christensen.
 * @version $Id: Grouper.java,v 1.6 2004-04-02 21:04:11 blair Exp $
 */
public class Grouper {

  /*
    - Read configuration
    - Establish database connection
      - XXX Or should this be per session?
    - Read and cache (in a Map?)
      - XXX types
      - XXX typeDefs
      - XXX fields
  */


  /* these remnants have got to go */
  private Connection  con;
  private int         sessionID;
  private String      cred;

  private Properties  conf     = new Properties();
  private String      confFile = "grouper.cf";

  private String jdbcDriver;
  private String jdbcPassword;
  private String jdbcUrl;
  private String jdbcUsername;

  public Grouper() {
    con       = null;
    sessionID = -1;
    cred      = null;

    try {
      FileInputStream in = new FileInputStream(confFile);
      try {
        conf.load(in);
        // XXX Try, try, try...
        jdbcDriver   = conf.getProperty("jdbc.driver");
        jdbcPassword = conf.getProperty("jdbc.password");
        jdbcUrl      = conf.getProperty("jdbc.url");
        jdbcUsername = conf.getProperty("jdbc.username");
      } catch (IOException e) {
        System.err.println("Unable to read '" + confFile + "'");
      }
    } catch (FileNotFoundException e) {
      System.err.println("Failed to find '" + confFile + "'");
    }
  }

  /*
    - XXX Do we need another method to reload the configuration, reconnect
          to the database, and reread/recache types, etc.?
      - How would this work?  Perhaps leave this alone for now.
  */

}

