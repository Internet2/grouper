package edu.internet2.middleware.directory.grouper;

import java.io.*;
import java.sql.*;
import java.util.Properties;

/** 
 * Provides a Grouper environment.
 *
 * @author blair christensen.
 * @version $Id: Grouper.java,v 1.10 2004-04-28 19:00:22 blair Exp $
 */
public class Grouper {

  private static final Properties conf     = new Properties();
  private static final String     confFile = "grouper.cf";

  /**
   * Create {@link Grouper} environment.
   */
  public Grouper() {
    // Nothing
  }

  /**
   * Initializes {@link Grouper} environment.
   * <p>
   * <ul>
   *  <li>Reads configuration</li>
   *  <li>Starts executive {@link GrouperSession} session used for
   *      bootstrapping and verifying other sessions.</li>
   *  <li>Reads and caches:</li>
   *  <ul>
   *   <li><i>grouper_fields</i></li>
   *   <li><i>grouper_typeDefs</i></li>
   *   <li><i>grouper_types</i></li>
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
    // Nothing -- Yet
  }

  /**
   * Return a {@link Grouper} configuration parameter.
   * <p>
   * <ul>
   *  <li>Returns value of requested parameter from the internal
   *      Map (or whatever) of runtime configuration information.</li>
   *  <li>XXX Add to docs/examples/.</li>
   * </ul> 
   * 
   * @param   parameter Requested configuration parameter.
   * @return  Value of configuration parameter.
   */
  public String config(String parameter) {
    return conf.getProperty(parameter);
  }
}

