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
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * Grouper API configuration.  If you are accessing a property from grouper.properties,
 * you should probably use GrouperConfig
 * <p/>
 * @author  blair christensen.
 * @version $Id: ApiConfig.java,v 1.22 2009-09-21 06:14:27 mchyzer Exp $
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
   * Property name for <code>AttributeDefAdapter</code> implementation.
   */
  public static final String ATTRIBUTE_DEF_PRIVILEGE_INTERFACE = "privileges.attributeDef.interface";

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
   * property names
   * @return property names
   */
  public Set<String> getPropertyNames() {
    Set<String> result = new HashSet<String>();
    result.addAll(testConfig.keySet());
    if (this.useLocal) {
      result.addAll(this.localCfg.keySet());
    }
    result.addAll(this.defaultCfg.keySet());
    return result;
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
    } catch (GrouperException eInvalidLocalConfig) {
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

    Properties properties = GrouperUtil.propertiesFromResourceName("grouper.properties");
    
    String displayMessageString = GrouperUtil.propertiesValue(properties, "configuration.display.startup.message");
    
    String grouperStartup = "Grouper starting up: " + versionTimestamp();
    if (!GrouperUtil.booleanValue(displayMessageString, true)) {
      //just log this to make sure we can
      try {
        LOG.warn(grouperStartup);
      } catch (RuntimeException re) {
        //this is bad, print to stderr rightaway (though might dupe)
        System.err.println(GrouperUtil.LOG_ERROR);
        re.printStackTrace();
        throw new RuntimeException(GrouperUtil.LOG_ERROR, re);
      }
      
      return;
    }

    StringBuilder resultString = new StringBuilder();
    resultString.append(grouperStartup + "\n");
    File grouperPropertiesFile = GrouperUtil.fileFromResourceName("grouper.properties");
    String propertiesFileLocation = grouperPropertiesFile == null ? "not found" 
        : GrouperUtil.fileCanonicalPath(grouperPropertiesFile); 
    resultString.append("grouper.properties read from: " + propertiesFileLocation + "\n");

    if (GrouperConfig.getPropertyBoolean("grouper.api.readonly", false)) {
      resultString.append("grouper.api.readonly:         true\n");
    }
    
    resultString.append("Grouper current directory is: " + new File("").getAbsolutePath() + "\n");
    //get log4j file
    File log4jFile = GrouperUtil.fileFromResourceName("log4j.properties");
    String log4jFileLocation = log4jFile == null ? " [cant find log4j.properties]" :
      GrouperUtil.fileCanonicalPath(log4jFile);
    resultString.append("log4j.properties read from:   " + log4jFileLocation + "\n");
    
    resultString.append(GrouperUtil.logDirPrint());    
    File hibPropertiesFile = GrouperUtil.fileFromResourceName("grouper.hibernate.properties");
    String hibPropertiesFileLocation = hibPropertiesFile == null ? " [cant find grouper.hibernate.properties]" :
      GrouperUtil.fileCanonicalPath(hibPropertiesFile);
    resultString.append("grouper.hibernate.properties: " + hibPropertiesFileLocation + "\n");
    
    Properties grouperHibernateProperties = GrouperUtil.propertiesFromResourceName("grouper.hibernate.properties");
    String url = StringUtils.trim(grouperHibernateProperties.getProperty("hibernate.connection.url"));
    String user = StringUtils.trim(grouperHibernateProperties.getProperty("hibernate.connection.username"));
    resultString.append("grouper.hibernate.properties: " + user + "@" + url + "\n");
    String sourcesString = "problem with sources";
    try {
      sourcesString = SourceManager.getInstance().printConfig();
    } catch (Exception e) {
      LOG.error("problem with sources", e);
    }
    resultString.append(sourcesString);
    System.out.println(resultString);
    try {
      if (!GrouperUtil.isPrintGrouperLogsToConsole()) {
        LOG.warn(resultString);
      } else {
        //print something to log to make sure we can
        LOG.warn(grouperStartup);
      }
    } catch (RuntimeException re) {
      //this is bad, print to stderr rightaway (though might dupe)
      System.err.println(GrouperUtil.LOG_ERROR);
      re.printStackTrace();
      throw new RuntimeException(GrouperUtil.LOG_ERROR, re);
    }
  }


  /**
   * @return version timestamp
   */
  public static String versionTimestamp() {
    String buildTimestamp = null;
    try {
      buildTimestamp = GrouperCheckConfig.manifestProperty(ApiConfig.class, new String[]{"Build-Timestamp"});
    } catch (Exception e) {
      //its ok, might not be running in jar
    }
    Properties properties = GrouperUtil.propertiesFromResourceName("grouper.properties");
    
    String env = GrouperUtil.propertiesValue(properties, "grouper.env.name");
    env = StringUtils.defaultIfEmpty(env, "<no label configured>");
    String grouperStartup = "version: " + GrouperVersion.GROUPER_VERSION 
      + ", build date: " + buildTimestamp + ", env: " + env;
    
    return grouperStartup;
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

