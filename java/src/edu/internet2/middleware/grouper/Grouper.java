/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
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
import  java.util.*;

/** 
 * {@link Grouper} environment class.
 *
 * @author  blair christensen.
 * @version $Id: Grouper.java,v 1.31 2004-09-19 01:04:05 blair Exp $
 */
public class Grouper {

  private Properties  conf;
  private String      confFile; 

  // Cached Grouper group fields
  // TODO Switch to GrouperFields collection object?
  private List groupFields;
  // Cached Grouper group types
  // TODO Switch to GrouperTypes collection object?
  private List groupTypes;  
  // Cached Grouper group typeDefs
  // TODO Switch to GrouperTypeDefs collection object?
  private List groupTypeDefs;


  /**
   * Create {@link Grouper} environment.
   */
  public Grouper() {
    this.conf           = new Properties();
    this.confFile       = "conf/grouper.properties";
    this.groupFields    = new ArrayList();
    this.groupTypes     = new ArrayList();
    this.groupTypeDefs  = new ArrayList();
  }

  /**
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

    // TODO Perform data validation of some sort for these tables?
    this.groupFields    = GrouperBackend.groupFields();
    this.groupTypeDefs  = GrouperBackend.groupTypeDefs();
    this.groupTypes     = GrouperBackend.groupTypes();
  }

  /**
   * Destroy {@link Grouper} environment.
   */ 
  public void destroy() {
    // Nothing 
  }

  /**
   * {@link Grouper} run-time configuration parameter getter.
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

  /**
   * Valid group fields.
   * <p>
   * Reads and caches the <i>grouper_fields</i> table at
   * {@link Grouper} initialization.
   * 
   * @return  List of {@link GrouperField} objects.
   */
  public List groupFields() {
    return this.groupFields;
  }

  /**
   * Valid group type definitions.
   * <p>
   * Reads and caches the <i>grouper_typeDefs</i> table at
   * {@link Grouper} initialization.
   *
   * @return  List of {@link GrouperTypeDef} objects.
   */
  public List groupTypeDefs() {
    return groupTypeDefs;
  }

  /**
   * Valid group types.
   * <p>
   * Reads and caches the <i>grouper_types</i> table at
   * {@link Grouper} initialization.
   * 
   * @return  List of {@link GrouperType} objects.
   */
  public List groupTypes() {
    return groupTypes;
  }

}

