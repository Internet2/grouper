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


import  java.io.*;
import  java.util.*;


/** 
 * Grouper configuration information.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperConfig.java,v 1.1 2005-11-14 17:35:35 blair Exp $
 *     
*/
class GrouperConfig {

  // Private Class Constants
  private static final String CF = "/grouper.properties";


  // Private Class Variables
  private static GrouperConfig  cfg;
  private static Properties     properties  = new Properties();


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
  protected String getProperty(String property) {
    return properties.getProperty(property);
  } // protected String getProperty(property)


  // Private Class Methods
  private static GrouperConfig _getConfiguration() {
    InputStream in = GrouperConfig.class.getResourceAsStream(CF);
    try {
      properties.load(in);
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

