/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.cfg;
import  edu.internet2.middleware.grouper.GrouperRuntimeException;

/** 
 * Grouper API configuration.
 * <p/>
 * @author  blair christensen.
 * @version $Id: ApiConfig.java,v 1.3 2007-08-27 15:46:24 blair Exp $
 * @since   @HEAD@
 */
public class ApiConfig implements Configuration {


  /**
   * Property name for <code>AccessAdapter</code> implementation.
   * @since   @HEAD@
   */ 
  public static final String ACCESS_PRIVILEGE_INTERFACE = "privileges.access.interface";
  /**
   * Property name for <code>NamingAdapter</code> implementation.
   * @since   @HEAD@
   */ 
  public static final String NAMING_PRIVILEGE_INTERFACE = "privileges.naming.interface";

  private                   boolean                 useLocal;
  private                   PropertiesConfiguration defaultCfg, localCfg;


  /**
   * Access Grouper API configuration.
   * <p/>
   * @since   @HEAD@
   */
  public ApiConfig() {
    this.initializeConfiguration();
  }


  /**
   * @see     edu.internet2.middleware.grouper.cfg.Configuration#getProperty(String)
   * @since   @HEAD@
   */
  public String getProperty(String property) 
    throws  IllegalArgumentException
  {
    String val = null;
    if (this.useLocal) { 
      val = this.localCfg.getProperty(property);
    }
    if (val == null) {
      val = this.defaultCfg.getProperty(property);
    }
    return val;
  }

  /**
   * @since   @HEAD@
   */
  private void initializeConfiguration() {
    this.useLocal   = true;
    this.defaultCfg = new PropertiesConfiguration("/grouper.properties");
    this.localCfg   = new PropertiesConfiguration("/local.grouper.properties");
    try {
      this.localCfg.getProperty("dao.factory");
    }
    catch (GrouperRuntimeException eInvalidLocalConfig) {
      // TODO 20070802 add "isValid()" (or whatever) check to "PropertiesConfiguration" to avoid this hack
      this.useLocal = false; // invalid local configuration.  don't try again.
    }
  }

  /**
   * @see     edu.internet2.middleware.grouper.cfg.Configuration#setProperty(String, String)
   * @since   @HEAD@
   */
  public String setProperty(String property, String value) {
    if (this.useLocal) {
      return this.localCfg.setProperty(property, value);
    }
    return this.defaultCfg.setProperty(property, value);
  }

}

