package edu.internet2.middleware.directory.grouper;

import java.io.*;
import java.sql.*;
import java.util.Properties;

/** 
 * Provides a Grouper environment.
 *
 * @author blair christensen.
 * @version $Id: Grouper.java,v 1.8 2004-04-13 21:27:39 blair Exp $
 */
public class Grouper {

  /*
    - Read and cache (in a Map?)
      - XXX types
      - XXX typeDefs
      - XXX fields
  */

  private static final Properties conf     = new Properties();
  private static final String     confFile = "grouper.cf";

  /**
   * Create {@link Grouper} environment.
   */
  public Grouper() {

  }

  /**
   * Initializes {@link Grouper} environment.
   * <p>
   * <ul>
   *  <li>Reads configuration</li>
   *  <li>Starts executive {@link GrouperSession} session used for
   *      bootstrapping and verifying other sessions.</li>
   * </ul>
   */
  public void initialize() {
    try {
      FileInputStream in = new FileInputStream(confFile);
      try {
        conf.load(in);
      } catch (IOException e) {
        System.err.println("Unable to read '" + confFile + "'");
      }
    } catch (FileNotFoundException e) {
      System.err.println("Failed to find '" + confFile + "'");
    }
  }

  /**
   * Destroys {@link Grouper} environment.
   * <p>
   * <ul>
   *  <li>Stops executive {@link GrouperSession} session used for
   *      bootstrapping and verifying other sessions.</li>
   * </ul>
   */ 
  public void destroy() {
  }

}

