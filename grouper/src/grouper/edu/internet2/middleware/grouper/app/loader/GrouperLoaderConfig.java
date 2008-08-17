/*
 * @author mchyzer
 * $Id: GrouperLoaderConfig.java,v 1.3 2008-08-17 15:33:02 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.cfg.PropertiesConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.util.rijndael.Morph;


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
   * cache the properties configuration
   */
  private static PropertiesConfiguration propertiesConfiguration = null;

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
    // if there, convert to boolean
    try {
      // note, cant be blank at this point, so default value doesnt matter
      boolean paramBoolean = GrouperUtil.booleanValue(property);
      return paramBoolean;
    } catch (NumberFormatException nfe) {
      throw new NumberFormatException("Cannot convert the grouper.properties param: "
          + property + " to an Integer.  Config value is '" + paramString + "' " + nfe);
    }
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
   * String wheel = GrouperConfig.getProperty(&quot;groups.wheel.group&quot;);
   * </pre>
   * 
   * @param property to lookup
   * @return Value of configuration parameter or an empty string if parameter
   *         is invalid.
   * @since 1.1.0
   */
  public static String getPropertyString(String property) {
    return retrievePropertiesConfiguration().getProperty(property);
  }

  /**
   * Get a Grouper configuration parameter.
   * 
   * <pre class="eg">
   * String wheel = GrouperConfig.getProperty(&quot;groups.wheel.group&quot;);
   * </pre>
   * 
   * @param property to lookup
   * @param defaultValue is the value if the property isnt found
   * @return Value of configuration parameter or the default value (will trim the value)
   * @since 1.1.0
   */
  public static String getPropertyString(String property, String defaultValue) {
    String value = retrievePropertiesConfiguration().getProperty(property);
    return StringUtils.defaultIfEmpty(StringUtils.trimToEmpty(value), defaultValue);
  }

  /**
   * lazy load and cache the properties configuration
   * 
   * @return the properties configuration
   */
  private static PropertiesConfiguration retrievePropertiesConfiguration() {
    if (propertiesConfiguration == null) {
      propertiesConfiguration = new PropertiesConfiguration("/grouper-loader.properties");
    }
    return propertiesConfiguration;
  
  }

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
        if (isGrouper) {
          throw new RuntimeException("Cant have a database named 'grouper' in " +
          		"the grouper-loader.properties.  This is a special name for the " +
          		"grouper.hibernate.properties database");
        }
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
    pass = Morph.decrypt(pass);
    GrouperLoaderDb grouperLoaderDb = new GrouperLoaderDb(user, pass, url, driver);
    return grouperLoaderDb;
  }
  

  
}
