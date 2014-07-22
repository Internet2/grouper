/**
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
 */
/*
 * @author mchyzer
 * $Id: GrouperLoaderConfig.java,v 1.8 2008-11-08 08:15:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.ldap.GrouperLoaderLdapServer;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.morphString.Morph;


/**
 *
 */
public class GrouperLoaderConfig extends ConfigPropertiesCascadeBase  {

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
   * @deprecated use GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(property, defaultValue)
   */
  @Deprecated
  public static boolean getPropertyBoolean(String property, boolean defaultValue)
      throws NumberFormatException {
    return retrieveConfig().propertyValueBoolean(property, defaultValue);
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
   * @deprecated GrouperLoaderConfig.retrieveConfig().propertyValueInt(property, defaultValue);
   */
  @Deprecated
  public static int getPropertyInt(String property, int defaultValue)
      throws NumberFormatException {
    return retrieveConfig().propertyValueInt(property, defaultValue);
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
   * @deprecated use GrouperLoaderConfig.retrieveConfig().propertyValueString(property, ""); instead
   */
  @Deprecated
  public static String getPropertyString(String property) {
    return retrieveConfig().propertyValueString(property, "");
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
   * @deprecated use GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(property)
   */
  @Deprecated
  public static String getPropertyString(String property, boolean required) {
    if (required) {
      return retrieveConfig().propertyValueStringRequired(property);
    }
    return retrieveConfig().propertyValueString(property);
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
   * @deprecated use retrieveConfig().propertyValueString(property, defaultValue) instead
   */
  @Deprecated
  public static String getPropertyString(String property, String defaultValue) {
    return retrieveConfig().propertyValueString(property, defaultValue);
  }

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderConfig.class);

  /**
   * no need to construct, use the factory
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

    if (isGrouper && StringUtils.isNotBlank(GrouperLoaderConfig.retrieveConfig().propertyValueString("db." + name + ".user"))) {
      LOG.error("Cant have a database named 'grouper' in " +
          "the grouper-loader.properties.  This is a special name for the " +
          "grouper.hibernate.properties database");
    }
    
    if (isGrouper) {
      
      //the name "hibernate" is a special term, which could be in the grouper-loader.properties, 
      //but defaults to grouper.hibernate.properties
      Properties properties = GrouperHibernateConfig.retrieveConfig().properties();
      
      user = properties.getProperty("hibernate.connection.username");
      pass = properties.getProperty("hibernate.connection.password");
      url = properties.getProperty("hibernate.connection.url");
      driver = properties.getProperty("hibernate.connection.driver_class");
    } else {      
      
      //first look in grouper-loader.properties:
      user = GrouperLoaderConfig.retrieveConfig().propertyValueString("db." + name + ".user");
      if (!StringUtils.isBlank(user)) {
        pass = GrouperLoaderConfig.retrieveConfig().propertyValueString("db." + name + ".pass");
        url = GrouperLoaderConfig.retrieveConfig().propertyValueString("db." + name + ".url");
        driver = GrouperLoaderConfig.retrieveConfig().propertyValueString("db." + name + ".driver");
      
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

    grouperLoaderLdapServer.setConfigFileFromClasspath(getPropertyString("ldap." + name + ".configFileFromClasspath"));

    {
      //#note the URL should start with ldap: or ldaps: if it is SSL.  
      //#It should contain the server and port (optional if not default), and baseDn, 
      //#e.g. ldaps://ldapserver.school.edu:636/dc=school,dc=edu
      //#ldap.personLdap.url = ldaps://ldapserver.school.edu:636/dc=school,dc=edu
      String url = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + name + ".url");
      
      if (StringUtils.isBlank(url) && StringUtils.isBlank(grouperLoaderLdapServer.getConfigFileFromClasspath())) {
        throw new RuntimeException("Cant find the ldap connection named: '" + name + "' in " +
        		"the grouper-loader.properties.  Should have entry: ldap." + name + ".url or ldap." + name + ".configFileFromClasspath");
      }
      
      grouperLoaderLdapServer.setUrl(url);
    }
    
    {
      String user = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + name + ".user");
      //#ldap.personLdap.user = uid=someapp,ou=people,dc=myschool,dc=edu
      if (!StringUtils.isBlank(user)) {
        grouperLoaderLdapServer.setUser(user);
      }
    }
    
    {
      String pass = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + name + ".pass");
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
    grouperLoaderLdapServer.setSaslAuthorizationId(GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + name + ".saslAuthorizationId"));
    grouperLoaderLdapServer.setSaslRealm(GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + name + ".saslRealm"));
    
    //#ldap.personLdap.batchSize = 
    grouperLoaderLdapServer.setBatchSize(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + name + ".batchSize", -1));
        
    //#ldap.personLdap.countLimit = 
    grouperLoaderLdapServer.setCountLimit(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + name + ".countLimit", -1));
    
    grouperLoaderLdapServer.setTimeLimit(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + name + ".timeLimit", -1));

    grouperLoaderLdapServer.setTimeout(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + name + ".timeout", -1));

    grouperLoaderLdapServer.setMinPoolSize(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + name + ".minPoolSize", -1));

    grouperLoaderLdapServer.setMaxPoolSize(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + name + ".maxPoolSize", -1));

    grouperLoaderLdapServer.setValidateOnCheckIn(getPropertyBoolean("ldap." + name + ".validateOnCheckIn", false));
    grouperLoaderLdapServer.setValidateOnCheckOut(getPropertyBoolean("ldap." + name + ".validateOnCheckOut", false));
    grouperLoaderLdapServer.setValidatePeriodically(getPropertyBoolean("ldap." + name + ".validatePeriodically", false));

    grouperLoaderLdapServer.setPagedResultsSize(getPropertyInt("ldap." + name + ".pagedResultsSize", -1));

    grouperLoaderLdapServer.setReferral(getPropertyString("ldap." + name + ".referral"));

    //#validateOnCheckout defaults to true if all other validate methods are false
    if (!grouperLoaderLdapServer.isValidateOnCheckIn() && !grouperLoaderLdapServer.isValidateOnCheckOut() && !grouperLoaderLdapServer.isValidatePeriodically()) {
      grouperLoaderLdapServer.setValidateOnCheckOut(true);
    }
    
    //#ldap.personLdap.validateTimerPeriod = 
    grouperLoaderLdapServer.setValidateTimerPeriod(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + name + ".validateTimerPeriod", -1));
    
    //#ldap.personLdap.pruneTimerPeriod = 
    grouperLoaderLdapServer.setPruneTimerPeriod(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + name + ".pruneTimerPeriod", -1));

    //#if connections expire after a certain amount of time, this is it, in millis, defaults to 300000 (5 minutes)
    //#ldap.personLdap.expirationTime = 
    grouperLoaderLdapServer.setExpirationTime(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + name + ".expirationTime", -1));
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("LDAP config for server id: " + name + ": " + grouperLoaderLdapServer);
    }
    
    return grouperLoaderLdapServer;
  }

  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static GrouperLoaderConfig retrieveConfig() {
    return retrieveConfig(GrouperLoaderConfig.class);
  }

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
    
  }

  /**
   * @see ConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "loader.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "grouper-loader.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "grouper-loader.base.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "loader.config.secondsBetweenUpdateChecks";
  }
  
}
