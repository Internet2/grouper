/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  java.io.*;
import  java.util.*;

/** 
 * {@link Grouper} environment class.
 *
 * @author  blair christensen.
 * @version $Id: Grouper.java,v 1.34 2004-10-11 17:44:37 blair Exp $
 */
public class Grouper {

  /*
   * CLASS VARIABLES 
   */

  // Is the environment initialized
  private static boolean    initialized   = false;
  // A place to hold the run-time environment
  private static Properties conf          = new Properties();
  // Run-time configuration file
  // TODO Make this more dynamic
  private static String      confFile     = "conf/grouper.properties"; 
  // Cached Grouper group fields
  // TODO Switch to GrouperFields collection object?
  private static List       groupFields   = new ArrayList();
  // Cached Grouper group types
  // TODO Switch to GrouperTypes collection object?
  private static List       groupTypes    = new ArrayList();  
  // Cached Grouper group typeDefs
  // TODO Switch to GrouperTypeDefs collection object?
  private static List       groupTypeDefs = new ArrayList();


  /**
   * Create {@link Grouper} object.
   */
  public Grouper() {
    // Nothing -- Yet
    // TODO Throw an exception if called?
  }

  /*
   * CLASS METHODS
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
    // TODO Why the convert?
    List typeDefs = Grouper.groupTypeDefs();
    for (Iterator iter = typeDefs.iterator(); iter.hasNext();) {
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
    // TODO Why the convert?
    List types = Grouper.groupTypes();
    for (Iterator iter = types.iterator(); iter.hasNext();) {
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

  /*
   * PUBLIC METHODS
   */

  /**
   * Destroy {@link Grouper} environment.
   */ 
  public void destroy() {
    // Nothing 
  }

  /*
   * PRIVATE METHODS
   */

  /*
   * Initialize {@link Grouper} environment.
   * <p>
   * <ul>
   *  <li>Reads run-time configuration</li>
   *  <li>Reads and caches the following tables:</li>
   *  <ul>
   *   <li><i>grouper_fields</i></li>
   *   <li><i>grouper_typeDefs</i></li>
   *   <li><i>grouper_types</i></li>
   * </ul>
   */
  private static void _init() {
    // TODO Isn't there a mechanism by which the confFile can be found
    //      via $CLASSPATH and read in more (in)directly?
    if (initialized == false) {
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

      // TODO Perform data validation of some sort for these tables?
      groupFields   = GrouperBackend.groupFields();
      groupTypeDefs = GrouperBackend.groupTypeDefs();
      groupTypes    = GrouperBackend.groupTypes();

      initialized = true;
    }
  }

}

