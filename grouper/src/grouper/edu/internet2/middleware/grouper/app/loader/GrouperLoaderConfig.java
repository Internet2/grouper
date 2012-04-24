/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: GrouperLoaderConfig.java,v 1.8 2008-11-08 08:15:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.ldap.GrouperLoaderLdapServer;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.PropertiesConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;


/**
 *
 */
public class GrouperLoaderConfig {

  /**
   * name of param: loader.retain.db.logs.days
   * number of days to retain db logs in table grouperloader_log.  -1 is forever.  default is 7
   */
  public static final String LOADER_RETAIN_DB_LOGS_DAYS = "loader.retain.db.logs.days";

  /**
   * name of param: loader.thread.pool.size
   * number of threads in the loader threadpool.  Only this number of jobs can run at once
   * jobs which are on deck will block, or will fail if the blocking timeout occurs
   * a job is running if it is loading (not just scheduled).
   * default is 10
   */
  public static final String LOADER_THREAD_POOL_SIZE = "loader.thread.pool.size";

  /**
   * name of param: default.subject.source.id
   * if you want queries which do not specify subject source to come from a certain
   * source, specify here (improves performance so it doesnt search through all sources)
   * default is 10
   */
  public static final String DEFAULT_SUBJECT_SOURCE_ID = "default.subject.source.id";
  
  /**
   * Get a Grouper configuration parameter as boolean (must be true|t|false|f
   * case-insensitive)
   * 
   * @param property to lookup
   * @param defaultValue if the property is not there
   * @return Value of configuration parameter or null if parameter isnt
   *         specified. Exception is thrown if not formatted correcly
   * @throws NumberFormatException
   *             if cannot convert the value to an Integer
   */
  public static boolean getPropertyBoolean(String property, boolean defaultValue)
      throws NumberFormatException {
    String paramString = getPropertyString(property);
    // see if not there
    if (StringUtils.isEmpty(paramString)) {
      return defaultValue;
    }
    // note, cant be blank at this point, so default value doesnt matter
    boolean paramBoolean = GrouperUtil.booleanValue(paramString);
    return paramBoolean;
  }

  /**
   * Get a Grouper configuration parameter an integer
   * 
   * @param property to lookup
   * @param defaultValue of the int if not there
   * @return Value of configuration parameter or null if parameter isnt
   *         specified. Exception is thrown if not formatted correcly
   * @throws NumberFormatException
   *             if cannot convert the value to an Integer
   */
  public static int getPropertyInt(String property, int defaultValue)
      throws NumberFormatException {
    String paramString = getPropertyString(property);
    // see if not there
    if (StringUtils.isEmpty(paramString)) {
      return defaultValue;
    }
    // if there, convert to int
    try {
      int paramInteger = Integer.parseInt(paramString);
      return paramInteger;
    } catch (NumberFormatException nfe) {
      throw new NumberFormatException("Cannot convert the grouper.properties param: "
          + property + " to an Integer.  Config value is '" + paramString + "' " + nfe);
    }
  }

  /**
   * Get a Grouper configuration parameter.
   * 
   * <pre class="eg">
   * String wheel = GrouperLoaderConfig.getProperty(&quot;groups.wheel.group&quot;);
   * </pre>
   * 
   * @param property to lookup
   * @return Value of configuration parameter or an empty string if parameter
   *         is invalid.
   */
  public static String getPropertyString(String property) {
    return getPropertyString(property, "");
  }

  /**
   * Get a Grouper configuration parameter.
   * 
   * <pre class="eg">
   * String wheel = GrouperLoaderConfig.getProperty(&quot;groups.wheel.group&quot;);
   * </pre>
   * 
   * @param property to lookup
   * @param required if property is required.  if so, exception if not found.  if not, null if not found.
   * note if value is not filled in, but name is there, then still exception if required
   * @return Value of configuration parameter or null if parameter
   *         is not there
   */
  public static String getPropertyString(String property, boolean required) {
    String result = getPropertyString(property, null);
    if (result == null && required) {
      throw new RuntimeException("Cant find property: '" + property + "' in config file: grouper-loader.properties");
    }
    return result;
  }

  /**
   * get all properties including test properties
   * @return properties
   */
  public static Properties properties() {
    Properties properties = new Properties();
    properties.putAll(retrievePropertiesConfiguration().getProperties());
    properties.putAll(testConfig);
    return properties;
  }
  
  /**
   * Get a Grouper configuration parameter.
   * 
   * <pre class="eg">
   * String wheel = GrouperLoaderConfig.getProperty(&quot;groups.wheel.group&quot;);
   * </pre>
   * 
   * @param property to lookup
   * @param defaultValue is the value if the property isnt found
   * @return Value of configuration parameter or the default value (will trim the value)
   */
  public static String getPropertyString(String property, String defaultValue) {
    String value = null;
    if (testConfig.containsKey(property)) {
      value = testConfig.get(property);
    } else { 
      value = retrievePropertiesConfiguration().getProperty(property);
    }
    return StringUtils.defaultIfEmpty(StringUtils.trimToEmpty(value), defaultValue);
  }

  /**
   * config cache.  TODO do this smarter... see if the file has changed before reading again
   */
  private static GrouperCache<String, PropertiesConfiguration> configCache = 
    new GrouperCache<String, PropertiesConfiguration>("grouperLoaderConfigCache", 100, false, 120, 120, false);
  
  /** set some test config overrides */
  public static final Map<String, String> testConfig = new HashMap<String, String>(); 

  /**
   * lazy load and cache the properties configuration
   * 
   * @return the properties configuration
   */
  private synchronized static PropertiesConfiguration retrievePropertiesConfiguration() {
    PropertiesConfiguration propertiesConfiguration = configCache.get("config");
    if (propertiesConfiguration == null) {
      propertiesConfiguration = new PropertiesConfiguration("/grouper-loader.properties");
      configCache.put("config", propertiesConfiguration);
    }
    return propertiesConfiguration;
  
  }

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderConfig.class);

  /**
   * no need to construct
   */
  private GrouperLoaderConfig() {
    // no need to construct
  }

  /** hibernate db name */
  private static final String GROUPER_DB_NAME = "grouper";
  
  /**
   * get a profile by name.  if "grouper" then get the hibernate db connection
   * specify the db connection with user, pass, url, and driver class
   * the string after "db." is the name of the connection, and it should not have
   * spaces or other special chars in it
   * db.warehouse.user = mylogin
   * db.warehouse.pass = secret
   * db.warehouse.url = jdbc:mysql://localhost:3306/grouper
   * db.warehouse.driver = com.mysql.jdbc.Driver
   * @param name
   * @return the db
   */
  public static GrouperLoaderDb retrieveDbProfile(String name) {
    
    String pass = null;
    String url = null;
    String driver = null;
    boolean isGrouper = StringUtils.equals(name, GROUPER_DB_NAME);
    String user = null;

    if (StringUtils.isNotBlank(getPropertyString("db." + name + ".user"))) {
      LOG.error("Cant have a database named 'grouper' in " +
          "the grouper-loader.properties.  This is a special name for the " +
          "grouper.hibernate.properties database");
    }
    
    if (isGrouper) {
      
      //the name "hibernate" is a special term, which could be in the grouper-loader.properties, 
      //but defaults to grouper.hibernate.properties
      Properties properties = GrouperUtil.propertiesFromResourceName(
        "grouper.hibernate.properties");
      
      user = properties.getProperty("hibernate.connection.username");
      pass = properties.getProperty("hibernate.connection.password");
      url = properties.getProperty("hibernate.connection.url");
      driver = properties.getProperty("hibernate.connection.driver_class");
    } else {      
      
      //first look in grouper-loader.properties:
      user = getPropertyString("db." + name + ".user");
      if (!StringUtils.isBlank(user)) {
        pass = getPropertyString("db." + name + ".pass");
        url = getPropertyString("db." + name + ".url");
        driver = getPropertyString("db." + name + ".driver");
      
      } else {
        throw new RuntimeException("Cant find the db connection named: '" + name + "' in " +
        		"the grouper-loader.properties.  Should have entries: db." + name + ".user, db." + name 
        		+ ".pass, db." + name + ".url, db." + name + ".driver");
      }
    }
    //might be in external file
    pass = Morph.decryptIfFile(pass);
    GrouperLoaderDb grouperLoaderDb = new GrouperLoaderDb(user, pass, url, driver);
    return grouperLoaderDb;
  }

  /**
   * get a profile by name from grouper-loader.properties
   * specify the ldap connection with user, pass, url, etc
   * the string after "ldap." is the name of the connection, and it should not have
   * spaces or other special chars in it
   * ldap.personLdap.user
   * ldap.personLdap.pass
   * ldap.personLdap.url
   * @param name
   * @return the db
   */
  public static GrouperLoaderLdapServer retrieveLdapProfile(String name) {
    
    GrouperLoaderLdapServer grouperLoaderLdapServer = new GrouperLoaderLdapServer();

    {
      //#note the URL should start with ldap: or ldaps: if it is SSL.  
      //#It should contain the server and port (optional if not default), and baseDn, 
      //#e.g. ldaps://ldapserver.school.edu:636/dc=school,dc=edu
      //#ldap.personLdap.url = ldaps://ldapserver.school.edu:636/dc=school,dc=edu
      String url = getPropertyString("ldap." + name + ".url");
      
      if (StringUtils.isBlank(url)) {
        throw new RuntimeException("Cant find the ldap connection named: '" + name + "' in " +
        		"the grouper-loader.properties.  Should have entry: ldap." + name + ".url");
      }
      grouperLoaderLdapServer.setUrl(url);
    }
    
    {
      String user = getPropertyString("ldap." + name + ".user");
      //#ldap.personLdap.user = uid=someapp,ou=people,dc=myschool,dc=edu
      if (!StringUtils.isBlank(user)) {
        grouperLoaderLdapServer.setUser(user);
      }
    }
    
    {
      String pass = getPropertyString("ldap." + name + ".pass");
      if (!StringUtils.isBlank(pass)) {
        //might be in external file
        pass = Morph.decryptIfFile(pass);
        //#note the password can be stored encrypted in an external file
        //#ldap.personLdap.pass = secret
        grouperLoaderLdapServer.setPass(pass);
      }
    }

    
    //#optional, if you are using tls, set this to TRUE.  Generally you will not be using an SSL URL to use TLS...
    //#ldap.personLdap.tls = true
    grouperLoaderLdapServer.setTls(getPropertyBoolean("ldap." + name + ".tls", false));
    
    //#optional, if using sasl
    //#ldap.personLdap.saslAuthorizationId = 
    //#ldap.personLdap.saslRealm = 
    grouperLoaderLdapServer.setSaslAuthorizationId(getPropertyString("ldap." + name + ".saslAuthorizationId"));
    grouperLoaderLdapServer.setSaslRealm(getPropertyString("ldap." + name + ".saslRealm"));
    
    //#ldap.personLdap.batchSize = 
    grouperLoaderLdapServer.setBatchSize(getPropertyInt("ldap." + name + ".batchSize", -1));
        
    //#ldap.personLdap.countLimit = 
    grouperLoaderLdapServer.setCountLimit(getPropertyInt("ldap." + name + ".countLimit", -1));
    
    grouperLoaderLdapServer.setTimeLimit(getPropertyInt("ldap." + name + ".timeLimit", -1));

    grouperLoaderLdapServer.setTimeout(getPropertyInt("ldap." + name + ".timeout", -1));

    grouperLoaderLdapServer.setMinPoolSize(getPropertyInt("ldap." + name + ".minPoolSize", -1));

    grouperLoaderLdapServer.setMaxPoolSize(getPropertyInt("ldap." + name + ".maxPoolSize", -1));

    grouperLoaderLdapServer.setValidateOnCheckIn(getPropertyBoolean("ldap." + name + ".validateOnCheckIn", false));
    grouperLoaderLdapServer.setValidateOnCheckOut(getPropertyBoolean("ldap." + name + ".validateOnCheckOut", false));
    grouperLoaderLdapServer.setValidatePeriodically(getPropertyBoolean("ldap." + name + ".validatePeriodically", false));

    //#validateOnCheckout defaults to true if all other validate methods are false
    if (!grouperLoaderLdapServer.isValidateOnCheckIn() && !grouperLoaderLdapServer.isValidateOnCheckOut() && !grouperLoaderLdapServer.isValidatePeriodically()) {
      grouperLoaderLdapServer.setValidateOnCheckOut(true);
    }
    
    //#ldap.personLdap.validateTimerPeriod = 
    grouperLoaderLdapServer.setValidateTimerPeriod(getPropertyInt("ldap." + name + ".validateTimerPeriod", -1));
    
    //#ldap.personLdap.pruneTimerPeriod = 
    grouperLoaderLdapServer.setPruneTimerPeriod(getPropertyInt("ldap." + name + ".pruneTimerPeriod", -1));

    //#if connections expire after a certain amount of time, this is it, in millis, defaults to 300000 (5 minutes)
    //#ldap.personLdap.expirationTime = 
    grouperLoaderLdapServer.setExpirationTime(getPropertyInt("ldap." + name + ".expirationTime", -1));
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("LDAP config for server id: " + name + ": " + grouperLoaderLdapServer);
    }
    
    return grouperLoaderLdapServer;
  }
  

  
}
