/*
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.io.*;
import  java.lang.reflect.*;
import  java.util.*;
import  org.apache.log4j.*;


/** 
 * Base {@link Grouper} class.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: Grouper.java,v 1.55 2004-12-03 04:05:02 blair Exp $
 */
public class Grouper {

  /*
   * PUBLIC CONSTANTS
   */
  public static final String DEF_GROUP_TYPE = "base";
  public static final String DEF_LIST_TYPE  = "members";
  public static final String MEM_ALL        = "all";
  public static final String MEM_EFF        = "effective";
  public static final String MEM_IMM        = "immediate";
  public static final String NS_ROOT        = "";


  /*
   * PROTECTED CONSTANTS
   */
  protected static final Logger LOGGER = 
    Logger.getLogger(Grouper.class.getName());


  /*
   * PRIVATE CLASS VARIABLES 
   */

  // Is the environment initialized
  private static boolean        initialized   = false;
  // Are the interfaces initialized
  private static boolean        interfaces    = false;
  // Access priv interface
  private static GrouperAccess  access; 
  // Naming priv interface
  private static GrouperNaming  naming; 
  // A place to hold the run-time environment
  private static Properties     conf          = new Properties();
  // Run-time configuration file
  private static String         confFile      = "grouper.properties"; 
  // Cached Grouper group fields
  private static List           groupFields   = new ArrayList();
  // Cached Grouper group types
  private static List           groupTypes    = new ArrayList();  
  // Cached Grouper group typeDefs
  private static List           groupTypeDefs = new ArrayList();
  // Cached Grouper subject types
  private static List           subjectTypes  = new ArrayList();


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Retrieves a {@link GrouperAccess} access privilege object.
   * <p />
   *
   * @return  {@link GrouperAccess} object.
   */
  public static GrouperAccess access() {
    Grouper._initInterfaces();  
    return access;
  }

  /**
   * Class method to return a run-time configuration setting.
   * 
   * @param   parameter Requested configuration parameter.
   * @return  Value of configuration parameter.
   */
  public static String config(String parameter) {
    Grouper._init();
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
    Grouper._init();
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
    Grouper._init();
    return groupFields;
  }

  /**
   * Class method to confirm validity of a group type.
   * <p>
   *
   * @return  Boolean true if the type is valid, false otherwise.
   */
  public static boolean groupType(String type) {
    Grouper._init();
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
    Grouper._init();
    return groupTypeDefs;
  }

  /**
   * Class method to fetch all valid group types.
   * <p>
   * 
   * @return  List of {@link GrouperType} objects.
   */
  public static List groupTypes() {
    Grouper._init();
    return groupTypes;
  }

  /**
   * Class method to confirm validity of a subject type.
   * <p>
   *
   * @return  Boolean true if the type is valid, false otherwise.
   */
  public static boolean hasSubjectType(String type) {
    Grouper._init();
    Iterator iter = Grouper.subjectTypes().iterator();
    while (iter.hasNext()) {
      SubjectType t = (SubjectType) iter.next();
      if ( t.toString().equals(type) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Retrieves a {@link GrouperNaming} naming privilege object.
   * <p />
   *
   * @return  {@link GrouperNaming} object.
   */
  public static GrouperNaming naming() {
    Grouper._initInterfaces();  
    return naming;
  }

  public static SubjectType subjectType(String type) {
    Grouper._init();
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
    Grouper._init();
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
      Grouper.LOGGER.info("Initializing Grouper");
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

  /*
   * Initialize privilege interfaces.
   */
  private static void _initInterfaces() {
    Grouper._init();
    if (interfaces == false) {
      // Initialize static interfaces
      access = (GrouperAccess) Grouper._interfaceCreate( 
                Grouper.config("interface.access" ) 
               );
      naming = (GrouperNaming) Grouper._interfaceCreate( 
                Grouper.config("interface.naming" ) 
               );
      interfaces = true;
    }
  }

  /*
   * Instantiate an interface reflectively
   */
  private static Object _interfaceCreate(String name) {
    try {
      Class classType     = Class.forName(name);
      Class[] paramsClass = new Class[] { };
      try {
        Constructor con     = classType.getDeclaredConstructor(paramsClass);
        Object[] params     = new Object[] { };
        try {
          return con.newInstance(params);
        } catch (Exception e) {
          System.err.println("Unable to instantiate class: " + name);
          System.exit(1);
        }
      } catch (NoSuchMethodException e) {
        System.err.println("Unable to find constructor for class: " + name);
        System.exit(1);
      }
    } catch (ClassNotFoundException e) {
      System.err.println("Unable to find class: " + name);
      System.exit(1);
    }
    return null;
  }

}

