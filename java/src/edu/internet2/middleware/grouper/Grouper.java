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


/** 
 * Class providing access to the {@link Grouper} environment.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: Grouper.java,v 1.67 2005-01-30 20:48:36 blair Exp $
 */
public class Grouper {

  /*
   * PUBLIC CONSTANTS
   */
  /**
   * Default group type.
   */
  public static final String DEF_GROUP_TYPE = "base";
  /**
   * Default list type.
   */
  public static final String DEF_LIST_TYPE  = "members";
  /**
   * Default subject type.
   */
  public static final String DEF_SUBJ_TYPE  = "person";
  /**
   * Effective and immediate memberships.
   */ 
  public static final String MEM_ALL        = "all";
  /**
   * Effective memberships only.
   */
  public static final String MEM_EFF        = "effective";
  /**
   * Immediate memberships only.
   */
  public static final String MEM_IMM        = "immediate";
  /**
   * Root namespace.
   */
  public static final String NS_ROOT        = "";
  /**
    * {@link GrouperGroup} type for namespaces.
    */
  public static final String NS_TYPE        = "naming";
  /**
   * {@link GrouperAccess} <i>ADMIN</i> privilege.
   */
  public static final String PRIV_ADMIN     = "ADMIN";
  /**
   * {@link GrouperAccess} <i>OPTIN</i> privilege.
   */
  public static final String PRIV_OPTIN     = "OPTIN";
  /**
   * {@link GrouperAccess} <i>OPTOUT</i> privilege.
   */
  public static final String PRIV_OPTOUT    = "OPTOUT";
  /**
   * {@link GrouperAccess} <i>READ</i> privilege.
   */
  public static final String PRIV_READ      = "READ";
  /**
   * {@link GrouperAccess} <i>UPDATE</i> privilege.
   */
  public static final String PRIV_UPDATE    = "UPDATE";
  /**
   * {@link GrouperAccess} <i>VIEW</i> privilege.
   */
  public static final String PRIV_VIEW      = "VIEW";
  /**
   * {@link GrouperNaming} <i>CREATE</i> privilege.
   */
  public static final String PRIV_CREATE    = "CREATE";
  /**
   * {@link GrouperNaming} <i>STEM</i> privilege.
   */
  public static final String PRIV_STEM      = "STEM";


  /*
   * PROTECTED CONSTANTS
   */
  // TODO Replace with configurable 'hierarchy.delimiter'
  protected static final String HIER_DELIM  = ":";
  protected static final String KLASS_GG    =
    "edu.internet2.middleware.grouper.GrouperGroup";
  protected static final String KLASS_GM    =
    "edu.internet2.middleware.grouper.GrouperMember";
  protected static final String KLASS_GS    =
    "edu.internet2.middleware.grouper.GrouperSession";


  /*
   * PRIVATE CLASS VARIABLES 
   */

  // Is the environment initialized?
  private static boolean        initialized   = false;
  // Are the interfaces initialized?
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
  // For logging
  private static GrouperLog     log           = new GrouperLog();


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Retrieve a {@link GrouperAccess} access privilege resolver.
   * <p />
   *
   * @return  {@link GrouperAccess} object.
   */
  public static GrouperAccess access() {
    Grouper._initInterfaces();  
    return access;
  }

  /**
   * Retrieve a {@link Grouper} configuration parameter.
   * <p />
   * 
   * @param   parameter Requested configuration parameter.
   * @return  Value of configuration parameter.
   */
  public static String config(String parameter) {
    Grouper._init();
    return conf.getProperty(parameter);
  }

  /**
   * Check whether a group field is valid for a given group type.
   * <p />
   *
   * @param   type  {@link GrouperGroup} type
   * @param   field {@link GrouperGroup} field
   * @return  True if the field is valid for the group type.
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
   * Retrieve all valid group fields.
   * <p />
   * 
   * @return  List of {@link GrouperField} objects.
   */
  public static List groupFields() {
    Grouper._init();
    return groupFields;
  }

  /**
   * Check whether a group type is valid.
   * <p />
   *
   * @param   type  {@link GrouperGroup} type
   * @return  True if the type is valid.
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
   * Retrieve all group type definitions.
   * <p />
   *
   * @return  List of {@link GrouperTypeDef} objects.
   */
  public static List groupTypeDefs() {
    Grouper._init();
    return groupTypeDefs;
  }

  /**
   * Retrieve all group types.
   * <p />
   * 
   * @return  List of {@link GrouperType} objects.
   */
  public static List groupTypes() {
    Grouper._init();
    return groupTypes;
  }

  /**
   * Check whether an I2MI {@link Subject} type is valid.
   * <p />
   *
   * @param   type  {@link Subject} type 
   * @return  True if the type is valid.
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
   * Retrieve a {@link GrouperNaming} naming privilege resolver.
   * <p />
   *
   * @return  {@link GrouperNaming} object.
   */
  public static GrouperNaming naming() {
    Grouper._initInterfaces();  
    return naming;
  }

  /**
   * Retrieve a I2MI {@link SubjectType}.
   * <p />
   *
   * @param   type  {@link SubjectType} to return.
   * @return  A {@link SubjectType} object.
   */
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
   * Retrieve all I2MI {@link Subject} types.
   * <p />
   *
   * @return List of {@link SubjectType} objects.
   */
  public static List subjectTypes() {
    Grouper._init();
    return subjectTypes;
  }


  /*
   * PROTECTED CLASS METHODS
   */

  /**
   * Retrieves the {@link GrouperLog} logging object.
   * <p />
   * TODO I could envision making this public...
   *
   * @return  {@link GrouperLog} object.
   */
  protected static GrouperLog log() {
    return log;
  }

  /*
   * PRIVATE CLASS METHODS
   */

  /*
   * Initialize {@link Grouper} environment.
   */
  private static void _init() {
    if (initialized == false) {
      log.event("Initializing Grouper");
      // TODO Hateful
      GrouperSession tmp = new GrouperSession();
      InputStream    in  = tmp.getClass()
                              .getResourceAsStream("/" + confFile);
      try {
        conf.load(in);
      } catch (IOException e) {
        throw new RuntimeException("Unable to read '"+ confFile + "'");
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
          throw new RuntimeException("Unable to instantiate class: " + name);
        }
      } catch (NoSuchMethodException e) {
        throw new RuntimeException("Unable to find constructor for class: " + name);
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Unable to find class: " + name);
    }
  }

}

