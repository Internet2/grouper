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
import org.apache.commons.lang.StringUtils;

import  edu.internet2.middleware.grouper.GrouperRuntimeException;

/** 
 * Grouper API configuration.  If you are accessing a property from grouper.properties,
 * you should probably use G
 * <p/>
 * @author  blair christensen.
 * @version $Id: ApiConfig.java,v 1.6 2008-06-29 17:42:41 mchyzer Exp $
 * @since   1.2.1
 */
public class ApiConfig implements Configuration {


  /**
   * Property name for <code>AccessAdapter</code> implementation.
   * @since   1.2.1
   */ 
  public static final String ACCESS_PRIVILEGE_INTERFACE = "privileges.access.interface";
  /**
   * Property name for <code>NamingAdapter</code> implementation.
   * @since   1.2.1
   */ 
  public static final String NAMING_PRIVILEGE_INTERFACE = "privileges.naming.interface";

  /** if use local.grouper.properties */
  private boolean useLocal;

  /** default config */
  private PropertiesConfiguration defaultCfg;

  /** local config */
  private PropertiesConfiguration localCfg;

  /**
   * Access Grouper API configuration.
   * <p/>
   * @since   1.2.1
   */
  public ApiConfig() {
    this.initializeConfiguration();
  }


  /**
   * @see     edu.internet2.middleware.grouper.cfg.Configuration#getProperty(String)
   * @since   1.2.1
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
    return val == null ? null : val.trim();
  }

  /**
   * @since   1.2.1
   */
  private void initializeConfiguration() {
    this.useLocal = true;
    this.defaultCfg = new PropertiesConfiguration("/grouper.properties");
    this.localCfg = new PropertiesConfiguration("/local.grouper.properties");
    try {
      this.localCfg.getProperty("dao.factory");
    } catch (GrouperRuntimeException eInvalidLocalConfig) {
      // TODO 20070802 add "isValid()" (or whatever) check to "PropertiesConfiguration" to avoid this hack
      this.useLocal = false; // invalid local configuration.  don't try again.
    }
  }

  /**
   * @see     edu.internet2.middleware.grouper.cfg.Configuration#setProperty(String, String)
   * @since   1.2.1
   */
  public String setProperty(String property, String value) {
    if (this.useLocal) {
      return this.localCfg.setProperty(property, value);
    }
    return this.defaultCfg.setProperty(property, value);
  }
}

