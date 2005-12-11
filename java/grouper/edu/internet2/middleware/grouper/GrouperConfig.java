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
import  org.apache.commons.logging.*;


/** 
 * Grouper configuration information.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperConfig.java,v 1.5 2005-12-11 06:28:39 blair Exp $
 *     
*/
class GrouperConfig {

  // Protected Class Constants
  protected static final String BT  = "true";
  protected static final String GWG = "groups.wheel.group";
  protected static final String GWU = "groups.wheel.use";
  protected static final String PAI = "privileges.access.interface";
  protected static final String PNI = "privileges.naming.interface";


  // Private Class Constants
  private static final String CF      = "/grouper.properties";
  private static final String ERR_GC  = "unable to read grouper configuration file: ";
  private static final Log    LOG     = LogFactory.getLog(GrouperConfig.class);


  // Private Class Variables
  private static GrouperConfig cfg;


  // Private Instance Variables
  private Properties properties = new Properties();


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
  } // protected static GrouperConfig getInstance()


  // Protected Instance Methods
  protected String getProperty(String property) {
    return this.properties.getProperty(property);
  } // protected String getProperty(property)


  // Private Class Methods
  private static GrouperConfig _getConfiguration() {
    InputStream in = GrouperConfig.class.getResourceAsStream(CF);
    try {
      cfg = new GrouperConfig();
      cfg.properties.load(in);
    }
    catch (IOException eIOE) {
      String err = ERR_GC + eIOE.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
    return cfg;     
  } // private static GrouperConfig _getConfiguration()

}

