package edu.internet2.middleware.directory.grouper;

import  edu.internet2.middleware.directory.grouper.*;
import  java.io.*;
import  java.sql.*;
import  java.util.List;
import  java.util.ArrayList;
import  java.util.Properties;

/** 
 * Class representing the {@link Grouper} environment.
 *
 * @author  blair christensen.
 * @version $Id: Grouper.java,v 1.18 2004-07-26 17:02:54 blair Exp $
 */
public class Grouper {

  private Properties      conf      = new Properties();
  // XXX That's just wrong.  But it'll do for now.
  private String          confFile  = "conf/grouper.properties";
  private GrouperSession  intSess   = null;
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
   * Initialize {@link Grouper} environment.
   * <p>
   * <ul>
   *  <li>Reads run-time configuration file.</li>
   *  <li>Starts executive {@link GrouperSession} used for 
   *      boostrapping all other sessions.</li>
   *  <li>Reads and caches the following tables:</li>
   *  <ul>
   *   <li><i>grouper_fields</i></li>
   *   <li><i>grouper_groupTypeDefs</i></li>
   *   <li><i>grouper_groupTypes</i></li>
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
    this.intSess = new GrouperSession();
    this.intSess.start(this, this.config("member.system"), true);
  }

  /**
   * Destroy {@link Grouper} environment.
   * <p>
   * <ul>
   *  <li>Stops executive {@link GrouperSession}.</li>
   * </ul>
   */ 
  public void destroy() {
    // TODO Throw an exception if null??
    if (this.intSess != null) {
      this.intSess.end();
    }
  }

  /**
   * Fetch a {@link Grouper} configuration parameter.
   * <p>
   * <ul>
   *  <li>Fetches and returns value of requested run-time configuration
   *      parameter.</li>
   * </ul> 
   * 
   * @param   parameter Requested configuration parameter.
   * @return  Value of configuration parameter.
   */
  public String config(String parameter) {
    return conf.getProperty(parameter);
  }

  /**
   * Provides access to {@link GrouperField} definitions.
   * <p>
   * The <i>grouper_fields</i> table is read and cached
   * at {@link Grouper} initialization.
   * 
   * @return  List of {@link GrouperField} objects.
   */
  public List getField() {
    List fields = new ArrayList();
    return fields;
  }

  /**
   * Provides access to {@link GrouperType} definitions.
   * <p>
   * The <i>grouper_types</i> table is read and cached at 
   * {@link Grouper} initialization.
   * 
   * @return  List of {@link GrouperType} objects.
   */
  public List getType() {
    List types = new ArrayList();
    return types;
  }

  /**
   * Provides access to {@link GrouperTypeDef} definitions.
   * <p>
   * The <i>grouper_typeDefs</i> table is read and cached at 
   * {@link Grouper} initialization.
   */
  public List getTypeDef() {
    List typedefs = new ArrayList();
    return typedefs;
  }

}

