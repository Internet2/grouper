package edu.internet2.middleware.directory.grouper;

import  edu.internet2.middleware.directory.grouper.*;
import  java.io.*;
import  java.sql.*;
import  java.util.List;
import  java.util.Properties;

/** 
 * Provides a Grouper environment.
 *
 * @author blair christensen.
 * @version $Id: Grouper.java,v 1.12 2004-04-30 19:04:39 blair Exp $
 */
public class Grouper {

  private Properties      conf      = new Properties();
  private String          confFile  = "grouper.cf";
  private GrouperSession  s         = null;
  private List            fields    = null;
  private List            types     = null;
  private List            typedefs  = null;

  /**
   * Create {@link Grouper} environment.
   */
  public Grouper() {
    // Nothing -- Yet
  }

  /**
   * Initializes {@link Grouper} environment.
   * <p>
   * <ul>
   *  <li>Reads configuration</li>
   *  <li>Starts executive {@link GrouperSession} session used for
   *      bootstrapping and verifying other sessions.</li>
   *  <li>Reads and cache:</li>
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
    s = new GrouperSession(this);
    s.start("GrouperSystem", true);
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

  /**
   * @return  List of {@link GrouperFIeld} objects representing the
   *   <i>grouper_fields</i> table.
   */
  public List getField() {
    return null;
  }

  /**
   * @return  List of {@link GrouperType} objects representing the
   *   <i>grouper_types</i> table.
   */
  public List getType() {
    return null;
  }

  /**
   * @return  List of {@link GrouperTypeDef} objects representing the
   *   <i>grouper_typeDefs</i> table.
   */
  public List getTypeDef() {
    return null;
  }

}

