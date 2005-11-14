/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;


import  java.lang.reflect.*;
import  java.io.*;
import  java.util.*;


/** 
 * Grouper configuration information.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperConfig.java,v 1.2 2005-11-14 18:35:39 blair Exp $
 *     
*/
class GrouperConfig {

  // Private Class Constants
  private static final String CF = "/grouper.properties";


  // Private Class Variables
  private static AccessPrivilege  access;
  private static GrouperConfig    cfg;
  private static NamingPrivilege  naming;
  private static Properties       properties  = new Properties();


  // Constructors
  private GrouperConfig() {
    // nothing
  } // private GrouperConfig()


  // Protected Class Methods
  protected static GrouperConfig getInstance() {
    if (cfg == null) {
      cfg = _getConfiguration();
    }
    return cfg;
  } // protected static GruoperConfig getInstance()


  // Protected Instance Methods

  protected AccessPrivilege getAccess() {
    return access;
  } // protected AccessPrivilege getAccess()

  protected String getProperty(String property) {
    return properties.getProperty(property);
  } // protected String getProperty(property)

  protected NamingPrivilege getNaming() {
    return naming;
  } // protected NamingPrivilege getNaming()


  // Private Class Methods

  private static Object _createInterface(String name) {
    try {
      Class   classType     = Class.forName(name);
      Class[] paramsClass   = new Class[] { };
      try {
        Constructor con = 
          classType.getDeclaredConstructor(paramsClass);
        Object[] params = new Object[] { };
        try {
          return con.newInstance(params);
        } 
        catch (Exception e) {
          throw new RuntimeException(
            "Unable to instantiate class: " + name 
          );
        }
      } 
      catch (NoSuchMethodException eNSM) {
        throw new RuntimeException(
          "Unable to find constructor for class: " + name);
      }
    } 
    catch (ClassNotFoundException eCNF) {
      throw new RuntimeException("Unable to find class: " + name);
    }
  } // private static Object _createInterface(name)

  private static GrouperConfig _getConfiguration() {
    InputStream in = GrouperConfig.class.getResourceAsStream(CF);
    try {
      properties.load(in);
      access = (AccessPrivilege) _createInterface(
        properties.getProperty("interface.access")
      );
      naming = (NamingPrivilege) _createInterface(
        properties.getProperty("interface.naming")
      );
    }
    catch (IOException eIOE) {
      throw new RuntimeException(
        "unable to read grouper configuration: " + eIOE.getMessage()
      );
    }
    cfg = new GrouperConfig();
    return cfg;     
  } // private static GrouperConfig _getConfiguration()

}

