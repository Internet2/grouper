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
import  org.apache.commons.lang.*;
import  org.apache.commons.logging.*;


/** 
 * Grouper configuration information.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperConfig.java,v 1.10 2005-12-15 06:31:11 blair Exp $
 *     
*/
public class GrouperConfig {

  // Protected Class Constants
  protected static final String ALL     = "GrouperAll";
  protected static final String BT      = "true";
  protected static final String GWG     = "groups.wheel.group";
  protected static final String GWU     = "groups.wheel.use";
  protected static final String IST     = "application";
  protected static final String MSLGEA  = "memberships.log.group.effective.add";
  protected static final String MSLGED  = "memberships.log.group.effective.del";
  protected static final String MSLSEA  = "memberships.log.stem.effective.add";
  protected static final String MSLSED  = "memberships.log.stem.effective.del";
  protected static final String PAI     = "privileges.access.interface";
  protected static final String PNI     = "privileges.naming.interface";
  protected static final String ROOT    = "GrouperSystem";


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


  // Public Class Methods

  /**
   * Get Grouper configuration instance.
   * @returns {@link GrouperConfig} singleton.
   */
  public static GrouperConfig getInstance() {
    if (cfg == null) {
      cfg = _getConfiguration();
    }
    return cfg;
  } // public static GrouperConfig getInstance()


  // Public Instance Methods

  /**
   * Get a Grouper configuration parameter.
   * @returns Value of configuration parameter or an empty string if
   *   parameter is invalid.
   */
  public String getProperty(String property) {
    String value = new String();
    if ( (property != null) && (this.properties.containsKey(property)) ) {
      value = StringUtils.strip( this.properties.getProperty(property) );
    }
    return value;
  } // public String getProperty(property)


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

