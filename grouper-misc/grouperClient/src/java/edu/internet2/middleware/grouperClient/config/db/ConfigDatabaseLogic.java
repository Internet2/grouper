/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.config.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import edu.internet2.middleware.grouperClient.config.GrouperHibernateConfigClient;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.Validate;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.morphString.Morph;


/**
 * logic to cache and retrieve the grouper config from the DB
 * note: do not use any Grouper classes in this class.  Cant use anything that
 * could use anything configurable.
 * This class has some config which will be injected from the outside
 * there should be no imports here
 */
public class ConfigDatabaseLogic {

  /**
   * database cache key
   */
  public static String DATABASE_CACHE_KEY = "edu.internet2.middleware.grouperClient.config.db.ConfigDatabaseLogic.databaseConfigs";
  
  /**
   * cache database configs, key is e.g. grouper.properties
   */
  private static Map<String, Map<String, String>> databaseConfigCache = new HashMap<String, Map<String, String>>();
  /**
   * millis since 1970 that the database configs were last retrieved
   * will cache for grouper.cache.database.configs.seconds in grouper.hibernate.properties
   */
  private static long databaseConfigCacheLastRetrieved = -1;
  
  /**
   * keep this for testing
   */
  public static int databaseConfigRefreshCount = 0;

  /**
   *  
   */
  private static final Log LOG = LogFactory.getLog(ConfigDatabaseLogic.class);
      
  /**
   * 
   */
  public ConfigDatabaseLogic() {
  }

  /**
   * true if table exists
   */
  private static boolean tableExists = false;
  
  /**
   * 
   */
  public static void clearCache() {
    clearCache(true);
  }

  /**
   * 
   */
  public static void clearCache(boolean checkConfigTableExists) {
    LOG.debug("ConfigDatabaseLogic.clearCache()");
    databaseConfigCacheLastRetrieved = -1;
    databaseConfigCache = null;
    if (checkConfigTableExists) {
      tableExists = false;
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

  }

  /**
   * seconds between checking to see if the config files are updated in the database.  If anything edited, then refresh all. 
   * Note that the last edited is stored in a config property for deletes.  -1 means dont check for incrementals.
   * Note if *.config.secondsBetweenUpdateChecks is greater than this number
   * for this config, then it wont update until that amount has passed.
   * grouper.config.secondsBetweenUpdateChecksToDb = 600
   */
  private static int secondsBetweenUpdateChecksToDb = 600;
  
  /**
   * seconds between checking to see if the config files are updated in the database.  If anything edited, then refresh all. 
   * Note that the last edited is stored in a config property for deletes.  -1 means dont check for incrementals.
   * Note if *.config.secondsBetweenUpdateChecks is greater than this number
   * for this config, then it wont update until that amount has passed.
   * grouper.config.secondsBetweenUpdateChecksToDb = 60
   * @param theSeconds
   */
  public static void assignSecondsBetweenUpdateChecksToDb (int theSeconds) {
    secondsBetweenUpdateChecksToDb = theSeconds;
  }
  
  /**
   * readonly database, start as true until we know for sure
   */
  private static boolean readonly = true;
  
  /**
   * set the API as readonly (e.g. during upgrades).  Any updates will throw an exception
   * grouper.api.readonly = false
   *
   * @param theReadonly
   */
  public static void assignReadonly (boolean theReadonly) {
    readonly = theReadonly;
  }
  
  /**
   * seconds between full refreshes of the database config
   * grouper.config.secondsBetweenFullRefresh
   */
  private static int secondsBetweenFullRefresh = 3600;

  /**
   * 
   * @param theSeconds
   */
  public static void assignSecondsBetweenFullRefresh(int theSeconds) {
    secondsBetweenFullRefresh = theSeconds;
  }
  
  /**
   * 
   * @param mainConfigFileName configPropertiesCascadeBase.getMainConfigFileName() e.g. grouper.properties
   * @return the inputStream for this config's properties
   */
  public static InputStream retrieveConfigInputStream(String mainConfigFileName) {

    Map<String, String> configMap = retrieveConfigMap(mainConfigFileName);
    if (configMap == null) {
      configMap = new HashMap<String, String>();
    }
    Properties properties = new Properties();
    
    // this is never null
    for (String key : configMap.keySet()) {
      if (key == null) {
        throw new RuntimeException("Why is key null???? " + mainConfigFileName);
      }
      String value = GrouperClientUtils.defaultString(configMap.get(key));
      properties.put(key, value);
      
    }
    
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try {
      properties.store(byteArrayOutputStream, "");
    } catch (IOException e) {
      throw new RuntimeException("Error in " + mainConfigFileName, e);
    }
    
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    return byteArrayInputStream;

  }
  
  /**
   * 
   * @param mainConfigFileName configPropertiesCascadeBase.getMainConfigFileName() e.g. grouper.properties
   * @return the inputStream for this config's properties
   */
  public static Map<String, String> retrieveConfigMap(String mainConfigFileName) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("operation", "retrieveConfigMap");
    debugMap.put("readonly", readonly);
    debugMap.put("mainConfigFileName", mainConfigFileName);
    long now = System.nanoTime();

    long currentDatabaseConfigCache = databaseConfigCacheLastRetrieved;
    Map<String, Map<String, String>> theDatabaseConfigCache = databaseConfigCache;
    
    try {
      //  Technical design of retrieving the configuration from the database
      //  In the grouper client (since that is where the hierarchical config code is), have the logic to retrieve the configuration from the database.
      //
      //  It should use pooling so that it is efficient.
      //
      //  If should NOT use anything from the grouper API or anything that uses grouper client config.  
      // This is because the database framework in the API uses configuration.  So the configuration cannot 
      // use the API or there is a circular logic problem in looping and bootstrapping.
      //
      // There are some configs for this, but these are set after the first grouper.properties is retrieved.  
      // These are not in the grouper-hibernate.properties so they can be edited at runtime (grouper-hibernate.properties is not stored in the database of course).
      //
      //  Algorithm
      //
      //  A configuration is retrieved from the API (from any of the config files)
      //
      //  e.g.GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc")
      //  If it has not been longer than the *.config.secondsBetweenUpdateChecks for that config 
      // (e.g. grouper.config.secondsBetweenUpdateChecks), then just return the cached (in memory) config
      
      // (done higher up)
      

      //if there is no table there, dont fail
      boolean tableExistsTemp = tableExists;
      
      if (tableExistsTemp || configTableExists()) {
        
        // try to avoid a race condition here
        if (!tableExistsTemp) {
          tableExists = true;
        }
        
        boolean needsRefresh = false;
        
        final boolean databaseConfigCacheIsNull = theDatabaseConfigCache == null;
        debugMap.put("databaseConfigCacheIsNull", databaseConfigCacheIsNull);
        if (databaseConfigCacheIsNull) {
          needsRefresh = true;
        }
  
        if (!needsRefresh) {
          //  If it has been longer, then see if the last full refresh has been longer than grouper.config.secondsBetweenFullRefresh.  
          // If so, then do a full refresh of all configs in DB
          debugMap.put("secondsBetweenFullRefresh", secondsBetweenFullRefresh);
          
          int secondsSinceLastRefresh = (int)(System.currentTimeMillis() - currentDatabaseConfigCache) / 1000;
          debugMap.put("secondsSinceLastRefresh", secondsSinceLastRefresh);
          
          final boolean needsFullRefresh = secondsSinceLastRefresh > secondsBetweenFullRefresh;
          debugMap.put("needsFullRefresh", needsFullRefresh);
          if (needsFullRefresh) {
            
            needsRefresh = true;
          }
          
        }
          
        if (!needsRefresh) {
          //  If it has been longer, then see if has not been longer than grouper.config.secondsBetweenUpdateChecksToDb.  
          // If it has not, then get the DB config for that config file from memory cache
          debugMap.put("databaseConfigCacheLastRetrieved", databaseConfigCacheLastRetrieved);
          debugMap.put("secondsBetweenUpdateChecksToDb", secondsBetweenUpdateChecksToDb);
          
        }
        
        debugMap.put("needsRefresh", needsRefresh);
        if (needsRefresh) {
          synchronized (ConfigDatabaseLogic.class) {
            theDatabaseConfigCache = databaseConfigCache;
            // maybe another thread did this
            if (theDatabaseConfigCache == null || databaseConfigCacheLastRetrieved == currentDatabaseConfigCache) {
              debugMap.put("updatingConfig", true);
              theDatabaseConfigCache = retrieveDatabaseConfigFromDatabase();
              databaseConfigCache = theDatabaseConfigCache;
              databaseConfigRefreshCount++;
              databaseConfigCacheLastRetrieved = System.currentTimeMillis();
            } else {
              debugMap.put("configUpdatedInAnotherThread", true);
            }
          }
        }
        if (theDatabaseConfigCache != null) {
          Map<String, String> configCache = theDatabaseConfigCache.get(mainConfigFileName);
          if (configCache != null) {
            debugMap.put("configCount", configCache.size());
            return configCache;
          }
        }
      }
      debugMap.put("cantFindConfigMap", true);
    } catch (Exception e) {
      debugMap.put("exception", e.getMessage());

      throw new RuntimeException("error", e);
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("ms", (System.nanoTime() - now)/1000000);

        LOG.debug(mapToString(debugMap));
      }
    }
    // not null
    return new HashMap<String, String>();
  }
  
  /**
   * get hash for db creds
   * @param input
   * @return bytes
   */
  public static String sha256(String input) {  
    final String algorithm = "SHA-256";
    try {
      // Static getInstance method is called with hashing SHA  
      MessageDigest md = MessageDigest.getInstance(algorithm);  

      // digest() method called  
      // to calculate message digest of an input  
      // and return array of byte 
      return toHexString(md.digest(input.getBytes(StandardCharsets.UTF_8)));  
    } catch (NoSuchAlgorithmException nsae) {
      throw new RuntimeException("Error in algorith: '" + algorithm + "'", nsae);
    }
  } 
  
  /**
   * 
   * @param hash
   * @return string
   */
  public static String toHexString(byte[] hash) { 
      // Convert byte array into signum representation  
      BigInteger number = new BigInteger(1, hash);  

      // Convert message digest into hex value  
      StringBuilder hexString = new StringBuilder(number.toString(16));  

      // Pad with leading zeros 
      while (hexString.length() < 32) {  
          hexString.insert(0, '0');  
      }  

      return hexString.toString();  
  } 

  /**
   * null safe string compare
   * @param first
   * @param second
   * @return true if equal
   */
  public static boolean equals(String first, String second) {
    if (first == second) {
      return true;
    }
    if (first == null || second == null) {
      return false;
    }
    return first.equals(second);
  }

//  /**
//   * get a pooled data source by url and user
//   * @param url
//   * @param user
//   * @return the data source or null if not found
//   */
//  private static DataSource retrieveDataSourceFromC3P0(String url, String user) {
//    
//    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
//
//    debugMap.put("operation", "retrieveDataSourceFromC3P0");
//    long now = System.nanoTime();
//
//
//    try {
//
//      // list of data source which match this url and user
//      List<DataSource> dataSourcesMatch = new ArrayList<DataSource>();
//  
//      // loop through all pooled sources
//      final Set pooledDataSources = C3P0Registry.getPooledDataSources();
//      
//      DataSource result = null;
//      
//      debugMap.put("user", user);
//      debugMap.put("url", url);
//      debugMap.put("dataSourcesSize", pooledDataSources.size());
//      
//      for (Object dataSourceObject : pooledDataSources) {
//  
//        WrapperConnectionPoolDataSource wrapperConnectionPoolDataSource = null;
//        
//        if (dataSourceObject instanceof ComboPooledDataSource) {
//          ComboPooledDataSource theComboPooledDataSource = (ComboPooledDataSource)dataSourceObject;
//  
//          wrapperConnectionPoolDataSource = (WrapperConnectionPoolDataSource)theComboPooledDataSource.getConnectionPoolDataSource();
//        } else {
//          PoolBackedDataSource poolBackedDataSource = (PoolBackedDataSource)dataSourceObject;
//          wrapperConnectionPoolDataSource = (WrapperConnectionPoolDataSource)poolBackedDataSource.getConnectionPoolDataSource();
//        }
//
//        DriverManagerDataSource driverManagerDataSource = (DriverManagerDataSource)wrapperConnectionPoolDataSource.getNestedDataSource();
//        String c3p0jdbcUrl = driverManagerDataSource.getJdbcUrl();
//        String c3p0user = driverManagerDataSource.getUser();
//        if (equals(url, c3p0jdbcUrl) && equals(user, c3p0user)) {
//          dataSourcesMatch.add((DataSource)dataSourceObject);
//        }
//      }
//      
//      debugMap.put("dataSourcesMatch", dataSourcesMatch.size());
//
//      if (dataSourcesMatch.size() == 0) {
//        return null;
//      }
//      
//      // this should be the usual situation
//      if (dataSourcesMatch.size() == 1) {
//        // return the same one so the caller knows to keep it cached
//        result = dataSourcesMatch.get(0);
//      }
//
//      if (dataSourcesMatch.size() > 10) {
//        LOG.error("There are " + dataSourcesMatch.size() + " data sources for " + user + "@" + url );
//      }
//
//      if (result == null) {
//        
//        // get the one with most connections?
//        int mostConnections = -1;
//        int index = -1;
//        try {
//          for (int i=0;i<dataSourcesMatch.size();i++) {
//            AbstractPoolBackedDataSource abstractPoolBackedDataSource = (AbstractPoolBackedDataSource)dataSourcesMatch.get(0);
//            if (mostConnections == -1 || abstractPoolBackedDataSource.getNumConnections() > mostConnections) {
//              index = i;
//              mostConnections = abstractPoolBackedDataSource.getNumConnections();
//            }
//            
//          }
//        } catch (SQLException sqle) {
//          throw new RuntimeException("Cant get num of connections");
//        }
//        
//        result = dataSourcesMatch.get(index);
//      }
//      if (LOG.isDebugEnabled() && result instanceof AbstractPoolBackedDataSource) {
//        try {
//          AbstractPoolBackedDataSource abstractPoolBackedDataSource = (AbstractPoolBackedDataSource)result;
//          debugMap.put("conn", abstractPoolBackedDataSource.getNumConnections());
//          debugMap.put("busy", abstractPoolBackedDataSource.getNumBusyConnections());
//          debugMap.put("idle", abstractPoolBackedDataSource.getNumIdleConnections());
//        } catch (SQLException sqle) {
//          throw new RuntimeException("error", sqle);
//        }
//      }
//      return result;
//
//    } catch (RuntimeException e) {
//      debugMap.put("exception", e.getMessage());
//
//      throw e;
//    } finally {
//      if (LOG.isDebugEnabled()) {
//        debugMap.put("ms", (System.nanoTime() - now)/1000000);
//
//        LOG.debug(mapToString(debugMap));
//      }
//    }
//  }

  /**
   * one connection pool of one
   */
  //private static Connection connection;
  
  /**
   * when was connection opened
   */
  //private static long connectionOpenedMillis1970 = -1;
  
  /**
   * @param debugMap
   * @return the sql connectino either from pool or just from itself
   * @throws SQLException 
   * @throws ClassNotFoundException 
   */
  private static synchronized Connection connection(Map<String, Object> debugMap) throws SQLException, ClassNotFoundException {

//    // give it ten minutes
//    if ((System.currentTimeMillis() - connectionOpenedMillis1970) / 1000 > 10 * 60) {
//      debugMap.put("connectionClosedAfterTenMinutes", true);
//      closeQuietly(connection);
//      connection = null;
//    }
//    
//    if (connection != null) {
//      if (connection.isClosed()) {
//        debugMap.put("connectionClosedNeedAnother", true);
//      } else {
//        debugMap.put("reUsingConnection", true);
//        return connection;
//      }
//    }
    
    GrouperHibernateConfigClient grouperHibernateConfig = GrouperHibernateConfigClient.retrieveConfig();

    String dbUrl = grouperHibernateConfig.propertyValueStringRequired("hibernate.connection.url");;
    String dbUser = grouperHibernateConfig.propertyValueString("hibernate.connection.username");;
    String dbPass = grouperHibernateConfig.propertyValueString("hibernate.connection.password");;
    dbPass = Morph.decryptIfFile(dbPass);
    String driver = grouperHibernateConfig.propertyValueString("hibernate.connection.driver_class");
    driver = ConfigDatabaseLogic.convertUrlToDriverClassIfNeeded(dbUrl, driver);

    
    // DataSource theDataSource = retrieveDataSourceFromC3P0(dbUrl, dbUser);
    
    //    if (theDataSource != null) {
    //      
    //      debugMap.put("foundDataSource", true);
    //      
    //      // all good use the pool
    //      return theDataSource.getConnection();
    //    }
    debugMap.put("makingUnpooledConnection", true);
      
    Class.forName(driver);


    Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
    //connectionOpenedMillis1970 = System.currentTimeMillis();
    
    return connection;
    
    //    debugMap.put("makingNewPool", true);
//
//    debugMap.put("dbUrl", dbUrl);
//    debugMap.put("dbUser", dbUser);
//    debugMap.put("dbPass", (dbPass == null || dbPass.length() == 0) ? "empty" : "******");
//    debugMap.put("dbDriver", driver);
//
//    ComboPooledDataSource comboPooledDataSourceTemp = new ComboPooledDataSource();
//      
//    try {
//      Class.forName(driver);
//    } catch (Exception e) {
//      throw new RuntimeException("Cant find class for db driver from grouper-hibernate.properties: " + driver, e);
//    }
//
//    try {
//      comboPooledDataSourceTemp.setDriverClass(driver);
//    } catch (Exception e) {
//      throw new RuntimeException("Error with driver: " + driver, e);
//    }
//    comboPooledDataSourceTemp.setJdbcUrl(dbUrl);
//    comboPooledDataSourceTemp.setUser(dbUser);
//    comboPooledDataSourceTemp.setPassword(dbPass);
//    
//
//    // select from the database
//    {
//      Boolean debugUnreturnedConnectionStackTraces = grouperHibernateConfig.propertyValueBoolean("hibernate.c3p0.debugUnreturnedConnectionStackTraces");
//      if (debugUnreturnedConnectionStackTraces != null) {
//        comboPooledDataSourceTemp.setDebugUnreturnedConnectionStackTraces(debugUnreturnedConnectionStackTraces);
//      }
//    }
//    {
//      Integer unreturnedConnectionTimeout = grouperHibernateConfig.propertyValueInt("hibernate.c3p0.unreturnedConnectionTimeout");
//      if (unreturnedConnectionTimeout != null) {
//        comboPooledDataSourceTemp.setUnreturnedConnectionTimeout(unreturnedConnectionTimeout);
//      }
//    }
//    {
//      Integer acquireRetryDelay = grouperHibernateConfig.propertyValueInt("hibernate.c3p0.acquireRetryDelay");
//      if (acquireRetryDelay != null) {
//        comboPooledDataSourceTemp.setAcquireRetryDelay(acquireRetryDelay);
//      }
//    }
//    {
//      Integer timeout = grouperHibernateConfig.propertyValueInt("hibernate.c3p0.timeout");
//      if (timeout != null) {
//        comboPooledDataSourceTemp.setMaxIdleTime(timeout);
//      }
//    }
//    {
//      Integer idleTestPeriod = grouperHibernateConfig.propertyValueInt("hibernate.c3p0.idle_test_period");
//      if (idleTestPeriod != null) {
//        comboPooledDataSourceTemp.setIdleConnectionTestPeriod(idleTestPeriod);
//      }
//    }
//    {
//      int maxSize = grouperHibernateConfig.propertyValueInt("hibernate.c3p0.max_size", 100);
//      comboPooledDataSourceTemp.setMaxPoolSize(maxSize);
//    }
//    {
//      Integer maxStatements = grouperHibernateConfig.propertyValueInt("hibernate.c3p0.max_statements");
//      if (maxStatements != null) {
//        comboPooledDataSourceTemp.setMaxStatements(maxStatements);
//      }
//    }
//    {
//      // just keep this as zero since this data source is not really going to be used
//      int minSize = 0; //grouperHibernateConfig.propertyValueInt("hibernate.c3p0.min_size", 0);
//      comboPooledDataSourceTemp.setMinPoolSize(minSize);
//    }
//    {
//      Boolean validate = grouperHibernateConfig.propertyValueBoolean("hibernate.c3p0.validate");
//      if (validate != null) {
//        comboPooledDataSourceTemp.setTestConnectionOnCheckout(validate);
//      }
//    }
//    {
//      Integer acquireIncrement = grouperHibernateConfig.propertyValueInt("hibernate.c3p0.acquire_increment");
//      if (acquireIncrement != null) {
//        comboPooledDataSourceTemp.setAcquireIncrement(acquireIncrement);
//      }
//    }
//    
//    dataSourcePreferredNotToUseStatic = comboPooledDataSourceTemp;
//
//    return comboPooledDataSourceTemp;
  }

  
  /**
   * get configs from database
   * @return the list of maps by config name
   */
  private synchronized static Map<String, Map<String, String>> retrieveDatabaseConfigFromDatabase() {
    
//    try {
      return retrieveDatabaseConfigFromDatabaseHelper();
//    } catch (Exception e) {
//      closeQuietly(connection);
//      connection = null;
//    }
//    return retrieveDatabaseConfigFromDatabaseHelper();
  }

  /**
   * get configs from database
   * @return the list of maps by config name
   */
  private synchronized static Map<String, Map<String, String>> retrieveDatabaseConfigFromDatabaseHelper() {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("operation", "retrieveDatabaseConfigFromDatabase");
    long now = System.nanoTime();

    Connection theConnection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;

    Map<String, Map<String, String>> databaseConfigCacheTemp = new HashMap<String, Map<String, String>>();
    try {
      // select from the database
      theConnection = connection(debugMap);
      debugMap.put("gotConnection", true);
    
      preparedStatement = theConnection.prepareStatement("select config_file_name, config_key, config_value, config_encrypted from grouper_config where config_file_hierarchy = ?");
      preparedStatement.setString(1, "INSTITUTION");
  
      resultSet = preparedStatement.executeQuery();
                        
      while (resultSet.next()) {
        String configFileName = resultSet.getString("config_file_name");
        String configKey = resultSet.getString("config_key");
        String configValue = resultSet.getString("config_value");
        String configEncrypted = resultSet.getString("config_encrypted");
        
        Map<String, String> configPropertiesForFile = databaseConfigCacheTemp.get(configFileName);
        
        if (configPropertiesForFile == null) {
          configPropertiesForFile = new HashMap<String, String>();
          databaseConfigCacheTemp.put(configFileName, configPropertiesForFile);
        }
        
        // decrypt if encrypted
        if (booleanValue(configEncrypted, false)) {
          try {
            // TODO dont decrypt this in memory?
            configValue = Morph.decrypt(configValue);
          } catch (RuntimeException re) {
            GrouperClientUtils.injectInException(re, " Problem with configFile: '" + configFileName + "', configKey: '" + configKey + "' ");
            throw re;
          }
        }
        
        configPropertiesForFile.put(configKey, configValue);
      }
      debugMap.put("configFilesFound", databaseConfigCacheTemp.size());
      for (String configFileName : databaseConfigCacheTemp.keySet()) {
        debugMap.put("configFile_" + configFileName + "_propertiesFound", databaseConfigCacheTemp.get(configFileName).size());
      }
      
    } catch (Exception e) {
      debugMap.put("exception", e.getMessage());

      throw new RuntimeException("error", e);
    } finally {
      closeQuietly(resultSet);
      closeQuietly(preparedStatement);
      closeQuietly(theConnection);
      if (LOG.isDebugEnabled()) {
        debugMap.put("ms", (System.nanoTime() - now)/1000000);

        LOG.debug(mapToString(debugMap));
      }
    }
    return databaseConfigCacheTemp;
  }  
    

  /**
   * is an object null or blank
   * 
   * @param object
   * @return true if null or blank
   */
  public static boolean nullOrBlank(Object object) {
    // first handle blanks and nulls
    if (object == null) {
      return true;
    }
    if (object instanceof String && isBlank(((String) object))) {
      return true;
    }
    return false;
  
  }

  /**
   * get the boolean value for an object, cant be null or blank
   * 
   * @param object
   * @return the boolean
   */
  public static boolean booleanValue(Object object) {
    // first handle blanks
    if (nullOrBlank(object)) {
      throw new RuntimeException(
          "Expecting something which can be converted to boolean, but is null or blank: '"
              + object + "'");
    }
    // its not blank, just convert
    if (object instanceof Boolean) {
      return (Boolean) object;
    }
    if (object instanceof String) {
      String string = (String) object;
      if (equalsIgnoreCase(string, "true")
          || equalsIgnoreCase(string, "t")
          || equalsIgnoreCase(string, "yes")
          || equalsIgnoreCase(string, "y")) {
        return true;
      }
      if (equalsIgnoreCase(string, "false")
          || equalsIgnoreCase(string, "f")
          || equalsIgnoreCase(string, "no")
          || equalsIgnoreCase(string, "n")) {
        return false;
      }
      throw new RuntimeException(
          "Invalid string to boolean conversion: '" + string
              + "' expecting true|false or t|f or yes|no or y|n case insensitive");
  
    }
    throw new RuntimeException("Cant convert object to boolean: "
        + object.getClass());
  
  }

    
  /**
   * convert a set to a string (comma separate)
   * @param map
   * @return the String
   */
  public static String mapToString(Map map) {
    if (map == null) {
      return "null";
    }
    if (map.size() == 0) {
      return "empty";
    }
    StringBuilder result = new StringBuilder();
    boolean first = true;
    for (Object object : map.keySet()) {
      if (!first) {
        result.append(", ");
      }
      first = false;
      result.append(object).append(": ").append(map.get(object));
    }
    return result.toString();
  }

  /**
   * close a connection null safe and dont throw exception
   * @param theConnection
   */
  public static void closeQuietly(Connection theConnection) {
    if (theConnection != null) {
      try {
        theConnection.close();
      } catch (Exception e) {
        throw new RuntimeException("Cant close connection!");
      }
    }
  }

  /**
   * Unconditionally close an <code>InputStream</code>.
   * Equivalent to {@link InputStream#close()}, except any exceptions will be ignored.
   * @param input A (possibly null) InputStream
   */
  public static void closeQuietly(InputStream input) {
    if (input == null) {
      return;
    }
  
    try {
      input.close();
    } catch (IOException ioe) {
    }
  }

  /**
   * Unconditionally close an <code>InputStream</code>.
   * Equivalent to {@link InputStream#close()}, except any exceptions will be ignored.
   * @param input A (possibly null) InputStream
   */
  public static void closeQuietly(Statement input) {
    if (input == null) {
      return;
    }
  
    try {
      input.close();
    } catch (Exception ioe) {
    }
  }

  /**
   * close a resultSet null safe and dont throw exception
   * @param resultSet
   */
  public static void closeQuietly(ResultSet resultSet) {
    if (resultSet != null) {
      try {
        resultSet.close();
      } catch (Exception e) {
        //ignore
      }
    }
  }

  /**
   * See if the input is null or if string, if it is empty or blank (whitespace)
   * @param input
   * @return true if blank
   */
  public static boolean isBlank(Object input) {
    if (null == input) {
      return true;
    }
    return (input instanceof String && isBlank((String)input));
  }

  /**
   * <p>Checks if a String is whitespace, empty ("") or null.</p>
   *
   * <pre>
   * isBlank(null)      = true
   * isBlank("")        = true
   * isBlank(" ")       = true
   * isBlank("bob")     = false
   * isBlank("  bob  ") = false
   * </pre>
   *
   * @param str  the String to check, may be null
   * @return <code>true</code> if the String is null, empty or whitespace
   * @since 2.0
   */
  public static boolean isBlank(String str) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
      return true;
    }
    for (int i = 0; i < strLen; i++) {
      if ((Character.isWhitespace(str.charAt(i)) == false)) {
        return false;
      }
    }
    return true;
  }

  /**
   * equalsignorecase
   * @param str1
   * @param str2
   * @return true if the strings are equal ignore case
   */
  public static boolean equalsIgnoreCase(String str1, String str2) {
    return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
  }

  /**
   * get the boolean value for an object
   * 
   * @param object
   * @param defaultBoolean
   *            if object is null or empty
   * @return the boolean
   */
  public static boolean booleanValue(Object object, boolean defaultBoolean) {
    if (nullOrBlank(object)) {
      return defaultBoolean;
    }
    return booleanValue(object);
  }

  /**
   * create last updated record
   */
  private synchronized static void createLastUpdatedRecordInDatabase() {
    
//    try {
      createLastUpdatedRecordInDatabaseHelper();
//    } catch (Exception e) {
//      closeQuietly(connection);
//      connection = null;
//    }
//    createLastUpdatedRecordInDatabaseHelper();
  }

  /**
   * get configs from database
   * mainConfigFileName configPropertiesCascadeBase.getMainConfigFileName() e.g. grouper.properties
   */
  private synchronized static void createLastUpdatedRecordInDatabaseHelper() {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long now = System.nanoTime();
    debugMap.put("operation", "createLastUpdatedRecordInDatabase");
    debugMap.put("readonly", readonly);
  
    Connection theConnection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
  
    try {
      
      if (readonly) {
        // nothing to do
        return;
      }
      
      // select from the database
      theConnection = connection(debugMap);
      theConnection.setAutoCommit(false);
      debugMap.put("gotConnection", true);

      preparedStatement = theConnection.prepareStatement("insert into grouper_config (id, config_file_name, config_key, config_value, "
          + "config_comment, config_file_hierarchy, config_encrypted, config_sequence, config_version_index, last_updated, hibernate_version_number) "
          + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
      
      // id
      preparedStatement.setString(1, uuid());
      
      // config_file_name
      preparedStatement.setString(2, "grouper.properties");
      
      // config_key
      preparedStatement.setString(3, "grouper.config.millisSinceLastDbConfigChanged");
      
      // config_value
      preparedStatement.setString(4, "0");
      
      // config_comment
      preparedStatement.setString(5, "This is internal for Grouper, dont edit this manually!");
      
      // config_file_hierarchy
      preparedStatement.setString(6, "INSTITUTION");

      // config_encrypted
      preparedStatement.setString(7, "F");

      // config_sequence
      preparedStatement.setInt(8, 0);

      // config_version_index
      preparedStatement.setInt(9, 0);
  
      // last_updated
      preparedStatement.setBigDecimal(10, new BigDecimal(System.currentTimeMillis()));
      
      // hibernate_version_number
      preparedStatement.setInt(11, 0);
      
      int rows = preparedStatement.executeUpdate();
      debugMap.put("rows", rows);
      
      theConnection.commit();
                              
    } catch (Exception e) {
      try {
        theConnection.rollback();
      } catch (Exception e2) {
        LOG.debug("Cant rollback", e2);
        // ignore
      }
      debugMap.put("exception", e.getMessage());
  
      throw new RuntimeException("error", e);
    } finally {
      closeQuietly(resultSet);
      closeQuietly(preparedStatement);
      closeQuietly(theConnection);
      if (LOG.isDebugEnabled()) {
        debugMap.put("ms", (System.nanoTime() - now)/1000000);

        LOG.debug(mapToString(debugMap));
      }
    }
  
  }

  /**
   * sleep, if interrupted, throw runtime
   * @param millis
   */
  public static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ie) {
      throw new RuntimeException(ie);
    }
  }

  /**
   * generate a uuid
   * @return uuid
   */
  public static String uuid() {
    String uuid = UUID.randomUUID().toString();
    
    char[] result = new char[32];
    int resultIndex = 0;
    for (int i=0;i<uuid.length();i++) {
      char theChar = uuid.charAt(i);
      if (theChar != '-') {
        if (resultIndex >= result.length) {
          throw new RuntimeException("Why is resultIndex greater than result.length ???? " 
              + resultIndex + " , " + result.length + ", " + uuid);
        }
        result[resultIndex++] = theChar;
      }
    }
    return new String(result);

  }

  /**
   * null safe classname method, gets the unenhanced name
   * 
   * @param object
   * @return the classname
   */
  public static String className(Object object) {
    return object == null ? null : object.getClass().getName();
  }

  /**
   * convert an object to a long
   * @param input
   * @return the number
   */
  public static long longValue(Object input) {
    if (input instanceof String) {
      String string = (String)input;
      return Long.parseLong(string);
    }
    if (input instanceof Number) {
      return ((Number)input).longValue();
    }
    throw new RuntimeException("Cannot convert to long: " + className(input));
  }

  /**
   * see if config table exists
   * @return if the config table exists
   */
  private synchronized static boolean configTableExists() {
    
//    try {
      return configTableExistsHelper();
//    } catch (Exception e) {
//      closeQuietly(connection);
//      connection = null;
//    }
//    return configTableExistsHelper();
  }
  
  
  /**
   * see if config table exists
   * @return if the config table exists
   */
  private synchronized static boolean configTableExistsHelper() {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long now = System.nanoTime();

    debugMap.put("operation", "configTableExists");
  
    Connection theConnection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
  
    try {
      
      // if another thread did this
      if (tableExists) {
        return true;
      }
      
      // select from the database
      theConnection = connection(debugMap);
      debugMap.put("gotConnection", true);
    
      preparedStatement = theConnection.prepareStatement("select count(*) from grouper_config");

      resultSet = preparedStatement.executeQuery();
                        
      if (resultSet.next()) {
        debugMap.put("gotResult", true);
        resultSet.getBigDecimal(1);
        debugMap.put("foundTable", true);
        return true;
      }
    } catch (Exception e) {
      debugMap.put("exception", e.getMessage());
  
    } finally {
      closeQuietly(resultSet);
      closeQuietly(preparedStatement);
      closeQuietly(theConnection);
      if (LOG.isDebugEnabled()) {
        debugMap.put("ms", (System.nanoTime() - now)/1000000);

        LOG.debug(mapToString(debugMap));
      }
    }
    return false;
  }

  /**
   * if there is no driver class specified, then try to derive it from the URL
   * @param connectionUrl
   * @param driverClassName
   * @return the driver class
   */
  public static String convertUrlToDriverClassIfNeeded(String connectionUrl, String driverClassName) {
    //default some of the stuff
    if (isBlank(driverClassName)) {
      
      if (isHsql(connectionUrl)) {
        driverClassName = "org.hsqldb.jdbcDriver";
      } else if (isMysql(connectionUrl)) {
        driverClassName = "com.mysql.jdbc.Driver";
      } else if (isOracle(connectionUrl)) {
        driverClassName = "oracle.jdbc.driver.OracleDriver";
      } else if (isPostgres(connectionUrl)) { 
        driverClassName = "org.postgresql.Driver";
      } else if (isSQLServer(connectionUrl)) {
        driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
      } else {
        
        //if this is blank we will figure it out later
        if (!isBlank(connectionUrl)) {
        
          String error = "Cannot determine the driver class from database URL: " + connectionUrl;
          System.err.println(error);
          LOG.error(error);
          return null;
        }
      }
    }
    return driverClassName;
  
  }

  /**
   * see if the config file seems to be hsql
   * @param connectionUrl url to check against
   * @return see if hsql
   */
  public static boolean isHsql(String connectionUrl) {
    return defaultString(connectionUrl).toLowerCase().contains(":hsqldb:");
  }

  /**
   * see if the config file seems to be mysql
   * @param connectionUrl
   * @return see if mysql
   */
  public static boolean isMysql(String connectionUrl) {
    return defaultString(connectionUrl).toLowerCase().contains(":mysql:");
  }

  /**
   * see if the config file seems to be oracle
   * @param connectionUrl
   * @return see if oracle
   */
  public static boolean isOracle(String connectionUrl) {
    return defaultString(connectionUrl).toLowerCase().contains(":oracle:");
  }

  /**
   * see if the config file seems to be postgres
   * @param connectionUrl
   * @return see if postgres
   */
  public static boolean isPostgres(String connectionUrl) {
    return defaultString(connectionUrl).toLowerCase().contains(":postgresql:");
  }

  /**
   * see if the config file seems to be sql server
   * @param connectionUrl
   * @return see if sql server
   */
  public static boolean isSQLServer(String connectionUrl) {
    return defaultString(connectionUrl).toLowerCase().contains(":sqlserver:");
  }

  /**
   * <p>Returns either the passed in String,
   * or if the String is <code>null</code>, an empty String ("").</p>
   *
   * <pre>
   * StringUtils.defaultString(null)  = ""
   * StringUtils.defaultString("")    = ""
   * StringUtils.defaultString("bat") = "bat"
   * </pre>
   *
   * @see String#valueOf(Object)
   * @param str  the String to check, may be null
   * @return the passed in String, or the empty String if it
   *  was <code>null</code>
   */
  public static String defaultString(String str) {
    return str == null ? "" : str;
  }

}
