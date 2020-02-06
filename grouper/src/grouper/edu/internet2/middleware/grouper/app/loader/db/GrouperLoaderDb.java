/**
 * Copyright 2014 Internet2
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
 * $Id: GrouperLoaderDb.java,v 1.1 2008-07-21 18:05:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.mchange.v2.c3p0.C3P0Registry;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DriverManagerDataSource;
import com.mchange.v2.c3p0.PoolBackedDataSource;
import com.mchange.v2.c3p0.WrapperConnectionPoolDataSource;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.morphString.Morph;


/**
 * db profile from grouper.properties (or possibly grouper.hibernate.properties)
 */
public class GrouperLoaderDb {
  
  /** user to login to db */
  private String user;
  
  /** pass to login to db */
  private String pass;
  
  /** url of the db to login to */
  private String url;
  
  /** db driver to use to login */
  private String driver;

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderDb.class);

  /**
   * get a pooled data source by url and user
   * NOTE: this is also in ConfigDatabaseLogic
   * @param url
   * @param user
   * @return the data source or null if not found
   */
  static DataSource retrieveDataSourceFromC3P0(String url, String user) {
    for (Object dataSourceObject : C3P0Registry.getPooledDataSources()) {
      WrapperConnectionPoolDataSource wrapperConnectionPoolDataSource = null;
      if (dataSourceObject instanceof ComboPooledDataSource) {
        ComboPooledDataSource comboPooledDataSource = (ComboPooledDataSource)dataSourceObject;
        wrapperConnectionPoolDataSource = (WrapperConnectionPoolDataSource)comboPooledDataSource.getConnectionPoolDataSource();
      } else {
        PoolBackedDataSource poolBackedDataSource = (PoolBackedDataSource)dataSourceObject;
        wrapperConnectionPoolDataSource = (WrapperConnectionPoolDataSource)poolBackedDataSource.getConnectionPoolDataSource();
      }
      DriverManagerDataSource driverManagerDataSource = (DriverManagerDataSource)wrapperConnectionPoolDataSource.getNestedDataSource();
      String c3p0jdbcUrl = driverManagerDataSource.getJdbcUrl();
      String c3p0user = driverManagerDataSource.getUser();
      if (LOG.isDebugEnabled()) {
        LOG.debug("c3p0 pool user@url: " + c3p0user + "@" + c3p0jdbcUrl);
      }
      if (StringUtils.equals(url, c3p0jdbcUrl) && StringUtils.equals(user, c3p0user)) {
        return (DataSource)dataSourceObject;
      }
    }
    return null;
  }
  
  /**
   * retrieve the config name for a url and user
   * @param url
   * @param user
   * @return the config name from grouper-loader.properties or null if not there
   */
  static String retrieveConfigName(String url, String user) {
    
    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();

    Pattern pattern = Pattern.compile("^db.([^.]+).user$");
    
    for (String propertyName : grouperLoaderConfig.propertyNames()) {
      Matcher matcher = pattern.matcher(propertyName);
      if (!matcher.matches()) {
        continue;
      }
      String configName = matcher.group(1);
      String configUser = grouperLoaderConfig.propertyValueString(propertyName);
      String configUrl = grouperLoaderConfig.propertyValueString("db." + configName + ".url");
      if (StringUtils.equals(url, configUrl) && StringUtils.equals(user, configUser)) {
        return configName;
      }
    }
    return null;
  }
  
  /**
   * retrieve the config name for a url and user
   * @param url
   * @param user
   * @return the config name from grouper-loader.properties or null if not there
   */
  static String retrieveConfigNameClient(String url, String user) {
    
    GrouperClientConfig grouperClientConfig = GrouperClientConfig.retrieveConfig();

    Pattern pattern = Pattern.compile("^grouperClient.jdbc.([^.]+).user$");
    
    for (String propertyName : grouperClientConfig.propertyNames()) {
      Matcher matcher = pattern.matcher(propertyName);
      if (!matcher.matches()) {
        continue;
      }
      String configName = matcher.group(1);
      String configUser = grouperClientConfig.propertyValueString(propertyName);
      String configUrl = grouperClientConfig.propertyValueString("grouperClient.jdbc." + configName + ".url");
      if (StringUtils.equals(url, configUrl) && StringUtils.equals(user, configUser)) {
        return configName;
      }
    }
    return null;
  }
  
  /**
   * get a connection from the db
   * NOTE: this method is used by grouperClient, if you change it here, change it there
   * @return the connection
   */
  public Connection connection() {

    try {

      // this things arent set, then set them and DONT use grouper hibernate as default (unless connection name is grouper)
      if (StringUtils.isBlank(this.url) && !StringUtils.isBlank(this.connectionName)) {
        
        this.url = retrievePoolConfigOrDefaultString(null, this.connectionName, "url", StringUtils.equals(this.connectionName, "grouper"));
        this.driver = retrievePoolConfigOrDefaultString(null, this.connectionName, "driver", StringUtils.equals(this.connectionName, "grouper"));
        this.user = retrievePoolConfigOrDefaultString(null, this.connectionName, "user", StringUtils.equals(this.connectionName, "grouper"));
        this.pass = retrievePoolConfigOrDefaultString(null, this.connectionName, "pass", StringUtils.equals(this.connectionName, "grouper"));
            
        this.pass = Morph.decryptIfFile(this.pass);
        
      }
      
      if (StringUtils.isBlank(this.url)) {
        throw new RuntimeException("Cant find database url in config: " + this);
      }
      
      if (StringUtils.isBlank(this.driver)) {
        this.driver = GrouperClientUtils.convertUrlToDriverClassIfNeeded(this.url, this.driver);
      }
      Class.forName(this.driver);
      
      if (!GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouperLoader.db.connections.pool", true)) {
        
        //not pooling
        Class.forName (this.driver);
    
        // connect
        Connection connection = DriverManager.getConnection(this.url,this.user, this.pass);
        return connection;
      }
      
      //pooling
  
      //see if there is a pool
      DataSource dataSource = retrieveDataSourceFromC3P0(this.url, this.user);
      
      //if so, we are good, this will cover the grouper built in database case with hibernate
      if (dataSource != null) {
        return dataSource.getConnection();
      }
  
      //add a pool entry
      //find name in grouper-loader.properties
      String configName = retrieveConfigName(this.url, this.user);
      
      boolean isClient = false;
      
      if (StringUtils.isBlank(configName)) {
        configName = retrieveConfigNameClient(this.url, this.user);
        if (!StringUtils.isBlank(configName)) {
          isClient = true;
        }
        
      }
      
      ComboPooledDataSource comboPooledDataSource = StringUtils.isBlank(configName) ? new ComboPooledDataSource() : new ComboPooledDataSource(configName);
      
      comboPooledDataSource.setDriverClass(this.driver); //loads the jdbc driver
      comboPooledDataSource.setJdbcUrl(this.url);
      comboPooledDataSource.setUser(this.user);
      comboPooledDataSource.setPassword(this.pass);
      
      {
        Integer minSize = retrievePoolConfigOrDefaultInt(isClient, configName, "min_size");
        if (minSize != null) {
          comboPooledDataSource.setMinPoolSize(minSize);
        }
      }
      
      {
        Integer maxSize = retrievePoolConfigOrDefaultInt(isClient, configName, "max_size");
        if (maxSize != null) {
          comboPooledDataSource.setMaxPoolSize(maxSize);
        }
      }
      
      {
        Integer timeout = retrievePoolConfigOrDefaultInt(isClient, configName, "timeout");
        if (timeout != null) {
          comboPooledDataSource.setMaxIdleTime(timeout);
        }
        
      }

      {
        Integer maxStatements = retrievePoolConfigOrDefaultInt(isClient, configName, "max_statements");
        if (maxStatements != null) {
          comboPooledDataSource.setMaxStatements(maxStatements);
        }

      }

      {
        Integer idleTestPeriod = retrievePoolConfigOrDefaultInt(isClient, configName, "idle_test_period");
        if (idleTestPeriod != null) {
          comboPooledDataSource.setIdleConnectionTestPeriod(idleTestPeriod);
        }
      }

      {
        Integer acquireIncrement = retrievePoolConfigOrDefaultInt(isClient, configName, "acquire_increment");
        if (acquireIncrement != null) {
          comboPooledDataSource.setAcquireIncrement(acquireIncrement);
        }
      }
      
      {
        Boolean validate = retrievePoolConfigOrDefaultBoolean(isClient, configName, "validate");
        if (validate != null) {
          //i assume this is the setting... hmmm
          comboPooledDataSource.setTestConnectionOnCheckout(validate);
        }
      }
      
      {
        Boolean debugUnreturnedConnectionStackTraces = retrievePoolConfigOrDefaultBoolean(isClient, configName, "debugUnreturnedConnectionStackTraces");
        //if set, set them, otherwise defaults
        if (debugUnreturnedConnectionStackTraces != null) {
          comboPooledDataSource.setDebugUnreturnedConnectionStackTraces(debugUnreturnedConnectionStackTraces);
        }
      }
      
      {
        Integer unreturnedConnectionTimeout = retrievePoolConfigOrDefaultInt(isClient, configName, "unreturnedConnectionTimeout");
        if (unreturnedConnectionTimeout != null) {
          comboPooledDataSource.setUnreturnedConnectionTimeout(unreturnedConnectionTimeout);
        }
      }

      //is it there now????
      dataSource = retrieveDataSourceFromC3P0(this.url, this.user);
      
      return dataSource.getConnection();
      
    } catch (Exception e) {
      throw new RuntimeException("Problems with db: " + this, e);
    }
    
  }

  /**
   * look in config file for config entry if exists, if not then get the default
   * @param isClient
   * @param configName
   * @param configItemName
   * return the number or null
   */
  private static Integer retrievePoolConfigOrDefaultInt(boolean isClient, String configName, String configItemName) {
    String configValueString = retrievePoolConfigOrDefaultString(isClient, configName, configItemName);
    return GrouperUtil.intObjectValue(configValueString, true);
  }

  /**
   * look in config file for config entry if exists, if not then get the default
   * @param isClient
   * @param configName
   * @param configItemName
   * return the boolean or null
   */
  private static Boolean retrievePoolConfigOrDefaultBoolean(boolean isClient, String configName, String configItemName) {
    String configValueString = retrievePoolConfigOrDefaultString(isClient, configName, configItemName);
    return GrouperUtil.booleanObjectValue(configValueString);
  }

  /**
   * look in config file for config entry if exists, if not then get the default, this is for c3po stuff
   * @param isClient
   * @param configName
   * @param configItemName
   * @return the config value
   */
  private static String retrievePoolConfigOrDefaultString(boolean isClient, String configName, String configItemName) {
    return retrievePoolConfigOrDefaultString(isClient, configName, configItemName, true);
  }

  
  /**
   * look in config file for config entry if exists, if not then get the default
   * @param isClient
   * @param configName
   * @param configItemName
   * @param useGrouperHibernateAsDefault
   * @return the config value
   */
  private static String retrievePoolConfigOrDefaultString(Boolean isClient, String configName, String configItemName, boolean useGrouperHibernateAsDefault) {
    if (StringUtils.isBlank(configItemName)) {
      throw new RuntimeException("configItemName is null!");
    }
    String configValue = null;
    if (StringUtils.isNotBlank(configName)) {
      if (isClient == null || isClient) {
        configValue = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.jdbc." + configName + ".c3p0." + configItemName);
      } 
      // if we are looking in both and havent found it yet, or if we are specifically not looking in client
      if ((isClient == null && StringUtils.isBlank(configValue)) || (isClient != null && !isClient)) {
      
        //lets see whats in the loader config
        //  ##optional pooling params, these will default to the grouper.hibernate(.base).properties pooling settings
        //  #db.warehouse.c3p0.max_size = 100
        //  #db.warehouse.c3p0.min_size = 0
        //  ##seconds
        //  #db.warehouse.c3p0.timeout = 100
        //  #db.warehouse.c3p0.max_statements = 0
        //  #db.warehouse.c3p0.idle_test_period = 100
        //  #db.warehouse.c3p0.acquire_increment = 1
        //  #db.warehouse.c3p0.validate = false

        configValue = GrouperLoaderConfig.retrieveConfig().propertyValueString("db." + configName + ".c3p0." + configItemName);
      }
    }

    // if we are looking in grouper hibernate as a default if not found and not found
    if (useGrouperHibernateAsDefault && configValue == null) {
      //if null, then default to grouper.hibernate.properties
      //  # Use c3p0 connection pooling (since dbcp not supported in hibernate anymore)
      //  # http://www.hibernate.org/214.html, http://www.hibernate.org/hib_docs/reference/en/html/session-configuration.html
      //  hibernate.c3p0.max_size = 100
      //  hibernate.c3p0.min_size = 0
      //  #seconds
      //  hibernate.c3p0.timeout = 100
      //  hibernate.c3p0.max_statements = 0
      //  hibernate.c3p0.idle_test_period = 100
      //  hibernate.c3p0.acquire_increment = 1
      //  hibernate.c3p0.validate = false

      // translate some
      String grouperConfigItemName = "hibernate.c3p0." + configItemName;
      if (StringUtils.equals(configItemName, "url")) {
        grouperConfigItemName = "hibernate.connection.url";
      } else if (StringUtils.equals(configItemName, "user")) {
        grouperConfigItemName = "hibernate.connection.username";
      } else if (StringUtils.equals(configItemName, "pass")) {
        grouperConfigItemName = "hibernate.connection.password";
      } else if (StringUtils.equals(configItemName, "driver")) {
        grouperConfigItemName = "hibernate.connection.driver_class";
      }
      configValue = GrouperHibernateConfig.retrieveConfig().propertyValueString(grouperConfigItemName);
    }
    
    return configValue;
  }
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() { 
    return "DB: user: " + this.user + ", url: " + this.url + ", driver: " + this.driver + 
        (StringUtils.isNotBlank(this.connectionName) ? (", connectionName: " + this.connectionName) : "");
  }
  
  /**
   * user to login to db
   * @return the user
   */
  public String getUser() {
    return this.user;
  }

  
  /**
   * user to login to db
   * @param user1 the user to set
   */
  public void setUser(String user1) {
    this.user = user1;
  }

  
  /**
   * pass to login to db
   * @return the pass
   */
  public String getPass() {
    return this.pass;
  }

  
  /**
   * pass to login to db
   * @param pass1 the pass to set
   */
  public void setPass(String pass1) {
    this.pass = pass1;
  }

  
  /**
   * url of the db to login to
   * @return the url
   */
  public String getUrl() {
    return this.url;
  }

  
  /**
   * url of the db to login to
   * @param url1 the url to set
   */
  public void setUrl(String url1) {
    this.url = url1;
  }

  
  /**
   * db driver to use to login
   * @return the driver
   */
  public String getDriver() {
    return this.driver;
  }

  
  /**
   * db driver to use to login
   * @param driver1 the driver to set
   */
  public void setDriver(String driver1) {
    this.driver = driver1;
  }

  /**
   * empty constructor
   */
  public GrouperLoaderDb() {
    //empty  
  }

  /**
   * connection name in grouper loader config or grouper client config
   */
  private String connectionName;
  
  /**
   * construct with all fields
   * NOTE: this constructor is used by grouperClient, if you change it here, change it there
   * @param connectionName1 connection name in grouper loader or grouper client config
   */
  public GrouperLoaderDb(String connectionName1) {
    super();
    this.connectionName = connectionName1;
  }

  /**
   * construct with all fields
   * NOTE: this constructor is used by grouperClient, if you change it here, change it there
   * @param user1
   * @param pass1
   * @param url1
   * @param driver1
   */
  public GrouperLoaderDb(String user1, String pass1, String url1, String driver1) {
    super();
    this.user = user1;
    this.pass = pass1;
    this.url = url1;
    this.driver = driver1;
  }

  
}
