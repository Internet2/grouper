package edu.internet2.middleware.directory.grouper;

import  edu.internet2.middleware.directory.grouper.*;
import  java.io.*;
import  java.sql.*;
import  java.util.*;
import  net.sf.hibernate.*;

/** 
 * Class representing the {@link Grouper} environment.
 *
 * @author  blair christensen.
 * @version $Id: Grouper.java,v 1.21 2004-08-03 02:17:13 blair Exp $
 */
public class Grouper {

  private Properties      conf        = new Properties();
  // XXX That's just wrong.  But it'll do for now.
  private String          confFile    = "conf/grouper.properties";

  private GrouperSession  intSess;
  private GrouperFields   groupFields;
  private GrouperTypes    groupTypes;
  private GrouperTypeDefs groupTypeDefs;

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
    // TODO Perform data validation of some sort for these tables?
    groupFields   = new GrouperFields();
    this._readFields();
    groupTypes    = new GrouperTypes();
    this._readTypes();
    groupTypeDefs = new GrouperTypeDefs();
    this._readTypeDefs();
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

  /*
   * XXX All of the below is utter madness.  Make sense of it.
   */

  private void _readFields() {
    // XXX Hack.  And I shouldn't need the temporary variable
    //     'session', should I?
    try {
      Session session = this.intSess.session();
      Query q = session.createQuery(
        "SELECT ALL FROM GROUPER_FIELDS " +
        "IN CLASS edu.internet2.middleware.directory.grouper.GrouperField"
        );
      for (Iterator iter = q.list().iterator(); iter.hasNext();) {
        GrouperField field = (GrouperField) iter.next();
        groupFields.add(field);
        // TODO groupFields.add( (GrouperField) iter.next() );
      }
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
  }

  private void _readTypeDefs() {
    // XXX Hack.  And I shouldn't need the temporary variable
    //     'session', should I?
    // XXX Fuck.  How do I Hibernate-map this table?!?!?!
    try {
      Session session = this.intSess.session();
      Query q = session.createQuery(
        "SELECT ALL FROM GROUPER_GROUPTYPEDEFS " +
        "IN CLASS edu.internet2.middleware.directory.grouper.GrouperTypeDef"
        );
      for (Iterator iter = q.list().iterator(); iter.hasNext();) {
        GrouperTypeDef typeDef = (GrouperTypeDef) iter.next();
        groupTypeDefs.add(typeDef);
        // TODO groupTypeDefs.add( (GrouperTypeDefs) iter.next() );
      }
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
  }

  private void _readTypes() {
    // XXX Hack.  And I shouldn't need the temporary variable
    //     'session', should I?
    try {
      Session session = this.intSess.session();
      Query q = session.createQuery(
        "SELECT ALL FROM GROUPER_GROUPTYPES " +
        "IN CLASS edu.internet2.middleware.directory.grouper.GrouperType"
        );
      for (Iterator iter = q.list().iterator(); iter.hasNext();) {
        GrouperType type = (GrouperType) iter.next();
        groupTypes.add(type);
        // TODO groupTypes.add( (GrouperType) iter.next() );
      }
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
  }

  /**
   * Provides access to {@link GrouperField} definitions.
   * <p>
   * The <i>grouper_fields</i> table is read and cached
   * at {@link Grouper} initialization.
   * 
   * @return  TODO
   */
  public GrouperFields getGroupFields() {
    return groupFields;
  }

  /**
   * Provides access to {@link GrouperType} definitions.
   * <p>
   * The <i>grouper_types</i> table is read and cached at 
   * {@link Grouper} initialization.
   * 
   * @return  TODO
   */
  public GrouperTypes getGroupTypes() {
    return groupTypes;
  }

  /**
   * Provides access to {@link GrouperTypeDef} definitions.
   * <p>
   * The <i>grouper_typeDefs</i> table is read and cached at 
   * {@link Grouper} initialization.
   *
   * @return  TODO
   */
  public GrouperTypeDefs getGroupTypeDefs() {
    return groupTypeDefs;
  }

}

