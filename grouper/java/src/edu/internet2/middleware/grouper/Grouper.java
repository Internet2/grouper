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
 * {@link Grouper} environment class.
 *
 * @author  blair christensen.
 * @version $Id: Grouper.java,v 1.25 2004-09-08 16:40:21 blair Exp $
 */
public class Grouper {

  private static Properties  conf;
  private static String      confFile; 

  // Grouper executive session
  private static GrouperSession  grprSession;
  // Cached Grouper group fields
  private static GrouperFields   groupFields;
  // Cached Grouper group types
  private static GrouperTypes    groupTypes;
  // Cached Grouper group typeDefs
  private static GrouperTypeDefs groupTypeDefs;


  /**
   * Create {@link Grouper} environment.
   */
  public Grouper() {
    this.conf           = new Properties();
    this.confFile       = "conf/grouper.properties";
    this.grprSession    = null;
    this.groupFields    = new GrouperFields();
    this.groupTypes     = new GrouperTypes();
    this.groupTypeDefs  = new GrouperTypeDefs();
  }

  /**
   * Initialize {@link Grouper} environment.
   * <p>
   * <ul>
   *  <li>Reads run-time configuration</li>
   *  <li>Starts executive {@link GrouperSession} used for 
   *      boostrapping all other sessions.</li>
   *  <li>Reads and caches the following tables:</li>
   *  <ul>
   *   <li><i>grouper_fields</i></li>
   *   <li><i>grouper_typeDefs</i></li>
   *   <li><i>grouper_types</i></li>
   * </ul>
   */
  public void init() {
    // TODO Isn't there a mechanism by which the confFile can be found
    //      via $CLASSPATH and read in more (in)directly?
    try {
      FileInputStream in = new FileInputStream(confFile);
      try {
        conf.load(in);
      } catch (IOException e) {
        System.err.println("Unable to read '" + confFile + "'");
        System.exit(1); 
      }
    } catch (FileNotFoundException e) {
      System.err.println("Failed to find '" + confFile + "'");
      System.exit(1); 
    }

    this.grprSession = new GrouperSession();
    this.grprSession.start(this, this.config("member.system"), true);
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
    if (this.grprSession != null) {
      this.grprSession.end();
    }
  }

  /**
   * Get {@link Grouper} configuration parameter.
   * <p>
   * <ul>
   *  <li>Returns value of requested run-time configuration
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
      Session session = this.grprSession.session();
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
    try {
      Session session = this.grprSession.session();
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
      Session session = this.grprSession.session();
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
   * Valid group fields.
   * <p>
   * Reads and caches the <i>grouper_fields</i> table at
   * {@link Grouper} initialization.
   * 
   * @return  {@link GrouperFields} object.
   */
  public GrouperFields getGroupFields() {
    return this.groupFields;
  }

  /**
   * Valid group type definitions.
   * <p>
   * Reads and caches the <i>grouper_typeDefs</i> table at
   * {@link Grouper} initialization.
   *
   * @return  {@link GrouperTypeDefs} object.
   */
  public GrouperTypeDefs getGroupTypeDefs() {
    return groupTypeDefs;
  }

  /**
   * Valid group types.
   * <p>
   * Reads and caches the <i>grouper_types</i> table at
   * {@link Grouper} initialization.
   * 
   * @return  {@link GrouperTypes} object.
   */
  public GrouperTypes getGroupTypes() {
    return groupTypes;
  }

}

