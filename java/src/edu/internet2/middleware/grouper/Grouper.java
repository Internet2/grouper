/* 
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.io.*;
import  java.util.*;

/** 
 * {@link Grouper} environment class.
 *
 * @author  blair christensen.
 * @version $Id: Grouper.java,v 1.47 2004-11-23 19:43:26 blair Exp $
 */
public class Grouper {

  /*
   * PUBLIC CONSTANTS
   */
  public static final String DEF_GROUP_TYPE  = "base";
  public static final String DEF_LIST_TYPE   = "members";


  /*
   * PRIVATE CLASS VARIABLES 
   */

  // Is the environment initialized
  private static boolean    initialized   = false;
  // A place to hold the run-time environment
  private static Properties conf          = new Properties();
  // Run-time configuration file
  private static String      confFile     = "grouper.properties"; 
  // Cached Grouper group fields
  private static List       groupFields   = new ArrayList();
  // Cached Grouper group types
  private static List       groupTypes    = new ArrayList();  
  // Cached Grouper group typeDefs
  private static List       groupTypeDefs = new ArrayList();
  // Cached Grouper subject types
  private static List       subjectTypes  = new ArrayList();


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Class method to return a run-time configuration setting.
   * 
   * @param   parameter Requested configuration parameter.
   * @return  Value of configuration parameter.
   */
  public static String config(String parameter) {
    _init();
    return conf.getProperty(parameter);
  }

  /**
   * Class method to confirm whether a given group field is valid 
   * for a given group type.
   * <p>
   *
   * @return  Boolean true if the field is valid for the group type,
   * false otherwise.
   */
  public static boolean groupField(String type, String field) {
    _init();
    // TODO Do I need another version of this method to distinguish
    //      between valid attribute and list data?
    Iterator iter = Grouper.groupTypeDefs().iterator();
    while (iter.hasNext()) {
      GrouperTypeDef td = (GrouperTypeDef) iter.next();
      if ( 
          (td.groupType().equals(type)) && // If the group type matches
          (td.groupField().equals(field))  // .. and the group field matches
         )
      {
        // Then we are considered validated.
        return true;
      }
    }
    return false;
  }

  /**
   * Class method to fetch all valid group fields.
   * <p>
   * 
   * @return  List of {@link GrouperField} objects.
   */
  public static List groupFields() {
    _init();
    return groupFields;
  }

  /**
   * Class method to confirm validity of a group type.
   * <p>
   *
   * @return  Boolean true if the type is valid, false otherwise.
   */
  public static boolean groupType(String type) {
    _init();
    Iterator iter = Grouper.groupTypes().iterator();
    while (iter.hasNext()) {
      GrouperType t = (GrouperType) iter.next();
      if ( t.toString().equals(type) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Class method to fetch all valid group type definitions.
   * <p>
   *
   * @return  List of {@link GrouperTypeDef} objects.
   */
  public static List groupTypeDefs() {
    _init();
    return groupTypeDefs;
  }

  /**
   * Class method to fetch all valid group types.
   * <p>
   * 
   * @return  List of {@link GrouperType} objects.
   */
  public static List groupTypes() {
    _init();
    return groupTypes;
  }

  /**
   * Class method to confirm validity of a subject type.
   * <p>
   *
   * @return  Boolean true if the type is valid, false otherwise.
   */
  public static boolean hasSubjectType(String type) {
    _init();
    Iterator iter = Grouper.subjectTypes().iterator();
    while (iter.hasNext()) {
      SubjectType t = (SubjectType) iter.next();
      if ( t.toString().equals(type) ) {
        return true;
      }
    }
    return false;
  }

  public static SubjectType subjectType(String type) {
    _init();
    SubjectType st    = null;
    Iterator    iter  = Grouper.subjectTypes().iterator();
    while (iter.hasNext()) {
      SubjectType t = (SubjectType) iter.next();
      if ( t.getId().equals(type) ) {
        return t;
      }
    }
    return st;
  }

  /**
   * Class method to fetch all valid subject types.
   *
   * @return List of {@link SubjectType} objects.
   */
  public static List subjectTypes() {
    _init();
    return subjectTypes;
  }


  /*
   * PRIVATE CLASS METHODS
   */

  /*
   * Initialize {@link Grouper} environment.
   */
  private static void _init() {
    if (initialized == false) {
      Grouper     tmp = new Grouper();
      InputStream in  = tmp.getClass().getResourceAsStream(confFile);
      try {
        conf.load(in);
      } catch (IOException e) {
        System.err.println("Unable to read '" + confFile + "'");
        System.exit(1); 
      }

      // TODO Perform data validation of some sort for these tables?
      groupFields   = GrouperBackend.groupFields();
      groupTypeDefs = GrouperBackend.groupTypeDefs();
      groupTypes    = GrouperBackend.groupTypes();
      subjectTypes  = GrouperBackend.subjectTypes();

      initialized = true;
    }
  }

}

