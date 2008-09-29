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
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Grouper API configuration.  If you are accessing a property from grouper.properties,
 * you should probably use GrouperConfig
 * <p/>
 * @author  blair christensen.
 * @version $Id: ApiConfig.java,v 1.14 2008-09-29 03:38:32 mchyzer Exp $
 * @since   1.2.1
 */
public class ApiConfig implements Configuration {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(ApiConfig.class);

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

  /** set some test config overrides */
  public static final Map<String, String> testConfig = new HashMap<String, String>(); 
  
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
    
    val = testConfig.get(property);
    boolean foundValue = testConfig.containsKey(property);
    if (!foundValue && this.useLocal) {
      val = this.localCfg.getProperty(property);
      if (val != null) {
        foundValue = true;
      }
    }
    if (!foundValue) {
      val = this.defaultCfg.getProperty(property);
    }
    return val == null ? null : val.trim();
  }

  /**
   * @since   1.2.1
   */
  private void initializeConfiguration() {
    printConfigOnce();
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

  /** print this once */
  private static boolean printedConfigLocation = false;
  
  /**
   * print where config is read from, to sys out and log warn
   */
  private static void printConfigOnce() {
    if (printedConfigLocation) {
      return;
    }
    printedConfigLocation = true;
    StringBuilder resultString = new StringBuilder();
    File grouperPropertiesFile = GrouperUtil.fileFromResourceName("grouper.properties");
    String propertiesFileLocation = grouperPropertiesFile == null ? "not found" 
        : GrouperUtil.fileCanonicalPath(grouperPropertiesFile); 
    resultString.append("grouper.properties read from: " + propertiesFileLocation + "\n");
    resultString.append("Grouper current directory is: " + new File("").getAbsolutePath() + "\n");
    File hibPropertiesFile = GrouperUtil.fileFromResourceName("grouper.hibernate.properties");
    String hibPropertiesFileLocation = hibPropertiesFile == null ? " [cant find grouper.hibernate.properties]" :
      GrouperUtil.fileCanonicalPath(hibPropertiesFile);
    resultString.append("grouper.hibernate.properties: " + hibPropertiesFileLocation + "\n");
    
    //get log4j file
    File log4jFile = GrouperUtil.fileFromResourceName("log4j.properties");
    String log4jFileLocation = log4jFile == null ? " [cant find log4j.properties]" :
      GrouperUtil.fileCanonicalPath(log4jFile);
    resultString.append("log4j.properties read from:   " + log4jFileLocation + "\n");
    
    resultString.append(GrouperUtil.logDirPrint());    
    Properties grouperHibernateProperties = GrouperUtil.propertiesFromResourceName("grouper.hibernate.properties");
    String url = StringUtils.trim(grouperHibernateProperties.getProperty("hibernate.connection.url"));
    String user = StringUtils.trim(grouperHibernateProperties.getProperty("hibernate.connection.username"));
    resultString.append("grouper.hibernate.properties: " + user + "@" + url + "\n");
    System.out.println(resultString);
    if (!GrouperUtil.isPrintGrouperLogsToConsole()) {
      LOG.warn(resultString);
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

