/* 
 * Copyright (C) 2004 TODO
 * All Rights Reserved. 
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted only as authorized by the Academic
 * Free License version 2.1.
 *
 * A copy of this license is available in the file LICENSE in the
 * top-level directory of the distribution or, alternatively, at
 * <http://www.opensource.org/licenses/afl-2.1.php>
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  java.io.*;
import  java.sql.*;
import  java.util.*;
import  net.sf.hibernate.*;

/** 
 * Class representing the {@link Grouper} environment.
 *
 * @author  blair christensen.
 * @version $Id: Grouper.java,v 1.24 2004-08-24 17:37:57 blair Exp $
 */
public class Grouper {

  private Properties      conf        = new Properties();
  // XXX That's just wrong.  But it'll do for now.
  private String          confFile    = "conf/grouper.properties";

  private GrouperSession  intSess;
  private static GrouperFields   groupFields;
  private static GrouperTypes    groupTypes;
  private static GrouperTypeDefs groupTypeDefs;


  /**
   * Create {@link Grouper} environment.
   */
  public Grouper() {
    this.groupFields   = new GrouperFields();
    this.groupTypes    = new GrouperTypes();
    this.groupTypeDefs = new GrouperTypeDefs();
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
    this._readFields();
    this._readTypes();
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
        "IN CLASS edu.internet2.middleware.grouper.GrouperField"
        );
      for (Iterator iter = q.list().iterator(); iter.hasNext();) {
        GrouperField field = (GrouperField) iter.next();
        this.groupFields.add(field);
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
        "IN CLASS edu.internet2.middleware.grouper.GrouperTypeDef"
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
        "IN CLASS edu.internet2.middleware.grouper.GrouperType"
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
    return this.groupFields;
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

