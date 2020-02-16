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
  
  public static enum DatabaseConfigType {
    grouper {

      @Override
      public String configValue(String databaseKey, String configItemName) {
        if (!StringUtils.isBlank(databaseKey) && !StringUtils.equals("grouper", databaseKey)) {
          throw new RuntimeException("Getting grouper database config but database key is '" + databaseKey + "'");
        }
        if (coreConfigOption(configItemName)) {
          
          if (StringUtils.equals("user", configItemName)) {
            return GrouperHibernateConfig.retrieveConfig().propertyValueStringRequired("hibernate.connection.username");
          }

          if (StringUtils.equals("pass", configItemName)) {
            return GrouperHibernateConfig.retrieveConfig().propertyValueString("hibernate.connection.password");
          }

          if (StringUtils.equals("url", configItemName)) {
            return GrouperHibernateConfig.retrieveConfig().propertyValueStringRequired("hibernate.connection.url");
          }

          if (StringUtils.equals("driver", configItemName)) {
            return GrouperHibernateConfig.retrieveConfig().propertyValueString("hibernate.connection.driver_class");
          }
          throw new RuntimeException("Not expecting config item: '" + configItemName + "'");
        }
        return GrouperHibernateConfig.retrieveConfig().propertyValueString("hibernate.c3p0." + configItemName);
      }
    },
    
    loader {

      @Override
      public String configValue(String databaseKey, String configItemName) {
        if (coreConfigOption(configItemName)) {
          if (StringUtils.equals("user", configItemName)) {
            return GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("db." + databaseKey + ".user");
          }
  
          if (StringUtils.equals("pass", configItemName)) {
            return GrouperLoaderConfig.retrieveConfig().propertyValueString("db." + databaseKey + ".pass");
          }
  
          if (StringUtils.equals("url", configItemName)) {
            return GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("db." + databaseKey + ".url");
          }
  
          if (StringUtils.equals("driver", configItemName)) {
            return GrouperLoaderConfig.retrieveConfig().propertyValueString("db." + databaseKey + ".driver");
          }
          throw new RuntimeException("Not expecting config item: '" + configItemName + "'");
        }
        return GrouperLoaderConfig.retrieveConfig().propertyValueString("db." + databaseKey + ".c3p0." + configItemName);
      }
    },
    
    client {

      @Override
      public String configValue(String databaseKey, String configItemName) {
        
        if (coreConfigOption(configItemName)) {
          if (StringUtils.equals("user", configItemName)) {
            return GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.jdbc." + databaseKey + ".user");
          }
  
          if (StringUtils.equals("pass", configItemName)) {
            return GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.jdbc." + databaseKey + ".pass");
          }
  
          if (StringUtils.equals("url", configItemName)) {
            return GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.jdbc." + databaseKey + ".url");
          }
  
          if (StringUtils.equals("driver", configItemName)) {
            return GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.jdbc." + databaseKey + ".driver");
          }
          throw new RuntimeException("Not expecting config item: '" + configItemName + "'");
        }
        return GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.jdbc." + databaseKey + ".c3p0." + configItemName);
      }
    };
    
    /**
     * get a config
     * @param configItemName
     * @return the string value
     */
    public abstract String configValue(String connectionName, String configItemName);    
  }
  
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
   * retrieve the config name for a url and user or null if not found
   * @param url
   * @param user
   * @return the config name from grouper-loader.properties or null if not there
   */
  static String retrieveConfigName(String url, String user) {

    if (StringUtils.equals(url, DatabaseConfigType.grouper.configValue(null, "url"))
        && StringUtils.equals(url, DatabaseConfigType.grouper.configValue(null, "user"))) {
      return "grouper";
    }
    
    {
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
    }
    
    {
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
    }
    
    return null;
  }
  
  /**
   * type of config
   */
  private DatabaseConfigType databaseConfigType = null;
  
  /**
   * get a connection from the db
   * NOTE: this method is used by grouperClient, if you change it here, change it there
   * @return the connection
   */
  public Connection connection() {

    try {
      
      if (!StringUtils.isBlank(this.connectionName)) {
        
        if (databaseConfigType == null) {
          databaseConfigType = configTypeWithDatabaseConnection(this.connectionName);
        }
        
        String theUrl = databaseConfigType.configValue(this.connectionName, "url");
        String theDriver = databaseConfigType.configValue(this.connectionName, "driver");
        String theUser = databaseConfigType.configValue(this.connectionName, "user");
        String thePass = databaseConfigType.configValue(this.connectionName, "pass");
        
        if (!StringUtils.isBlank(this.url) && !StringUtils.equals(this.url, theUrl)) {
          throw new RuntimeException("In database connectionName '" + this.connectionName + "' the url doesnt match: '" + this.url + "', '" + theUrl + "'");
        }
        
        if (!StringUtils.isBlank(this.url) && !StringUtils.equals(this.user, theUser)) {
          throw new RuntimeException("In database connectionName '" + this.connectionName + "' the user doesnt match: '" + this.user + "', '" + theUser + "'");
        }
        
        this.url = theUrl;
        this.driver = theDriver;
        this.user = theUser;
        this.pass = thePass;

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
      if (StringUtils.isBlank(this.connectionName)) {
        this.connectionName = retrieveConfigName(this.url, this.user);
        if (this.databaseConfigType == null &&!StringUtils.isBlank(this.connectionName)) {
          databaseConfigType = configTypeWithDatabaseConnection(this.connectionName);
        }
      }
            
      ComboPooledDataSource comboPooledDataSource = StringUtils.isBlank(this.connectionName) 
          ? new ComboPooledDataSource() : new ComboPooledDataSource(this.connectionName);
      
      comboPooledDataSource.setDriverClass(this.driver); //loads the jdbc driver
      comboPooledDataSource.setJdbcUrl(this.url);
      comboPooledDataSource.setUser(this.user);
      comboPooledDataSource.setPassword(this.pass);
      
      {
        Integer minSize = retrievePoolConfigOrDefaultInt("min_size");
        if (minSize != null) {
          comboPooledDataSource.setMinPoolSize(minSize);
        }
      }
      
      {
        Integer maxSize = retrievePoolConfigOrDefaultInt("max_size");
        if (maxSize != null) {
          comboPooledDataSource.setMaxPoolSize(maxSize);
        }
      }
      
      {
        Integer timeout = retrievePoolConfigOrDefaultInt("timeout");
        if (timeout != null) {
          comboPooledDataSource.setMaxIdleTime(timeout);
        }
        
      }

      {
        Integer maxStatements = retrievePoolConfigOrDefaultInt("max_statements");
        if (maxStatements != null) {
          comboPooledDataSource.setMaxStatements(maxStatements);
        }

      }

      {
        Integer idleTestPeriod = retrievePoolConfigOrDefaultInt("idle_test_period");
        if (idleTestPeriod != null) {
          comboPooledDataSource.setIdleConnectionTestPeriod(idleTestPeriod);
        }
      }

      {
        Integer acquireIncrement = retrievePoolConfigOrDefaultInt("acquire_increment");
        if (acquireIncrement != null) {
          comboPooledDataSource.setAcquireIncrement(acquireIncrement);
        }
      }
      
      {
        Boolean validate = retrievePoolConfigOrDefaultBoolean("validate");
        if (validate != null) {
          //i assume this is the setting... hmmm
          comboPooledDataSource.setTestConnectionOnCheckout(validate);
        }
      }
      
      {
        Boolean debugUnreturnedConnectionStackTraces = retrievePoolConfigOrDefaultBoolean("debugUnreturnedConnectionStackTraces");
        //if set, set them, otherwise defaults
        if (debugUnreturnedConnectionStackTraces != null) {
          comboPooledDataSource.setDebugUnreturnedConnectionStackTraces(debugUnreturnedConnectionStackTraces);
        }
      }
      
      {
        Integer unreturnedConnectionTimeout = retrievePoolConfigOrDefaultInt("unreturnedConnectionTimeout");
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
  private Integer retrievePoolConfigOrDefaultInt(String configItemName) {
    String configValueString = retrievePoolConfigOrDefaultString(configItemName);
    return GrouperUtil.intObjectValue(configValueString, true);
  }

  /**
   * look in config file for config entry if exists, if not then get the default
   * @param isClient
   * @param configName
   * @param configItemName
   * return the boolean or null
   */
  private Boolean retrievePoolConfigOrDefaultBoolean(String configItemName) {
    String configValueString = retrievePoolConfigOrDefaultString(configItemName);
    return GrouperUtil.booleanObjectValue(configValueString);
  }

  /**
   * 
   * @param configItemName
   * @return true if something that shouldnt inherit from hibernate properties
   */
  public static boolean coreConfigOption(String configItemName) {
    return StringUtils.equals(configItemName, "user") 
        || StringUtils.equals(configItemName, "pass")
        || StringUtils.equals(configItemName, "url")
        || StringUtils.equals(configItemName, "driver");
  }
  
  /**
   * see which config file we are dealing with (as a base)
   * @param connectionName
   * @param configItemName
   * @return grouper, client, or loader
   */
  public static DatabaseConfigType configTypeWithDatabaseConnection(String connectionName) {
    boolean grouperConfig = StringUtils.equals("grouper", connectionName);
    boolean clientConfig = StringUtils.isNotBlank(GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.jdbc." + connectionName + ".url"));
    boolean loaderConfig = StringUtils.isNotBlank(GrouperLoaderConfig.retrieveConfig().propertyValueString("db." + connectionName + ".url"));
    int configCount = 0;
    if (grouperConfig) {
      configCount++;
    }
    if (clientConfig) {
      configCount++;
    }
    if (loaderConfig) {
      configCount++;
    }
    if (configCount > 1 && grouperConfig) {
      LOG.error("Database 'grouper' should not be defined in " + (clientConfig ? "client " : "") 
          + (clientConfig && loaderConfig ? "or " : "") + ": '" 
          + (loaderConfig ? "loader " : "") + "'" + connectionName + "'");
      return DatabaseConfigType.grouper;
    }
    if (loaderConfig && clientConfig) {
      LOG.error("Database is configured in loader and client, delete the client one: '" + connectionName + "'.  Note: will use loader....");
      return DatabaseConfigType.loader;
    }
    if (grouperConfig) {
      return DatabaseConfigType.grouper;
    }
    if (loaderConfig) {
      return DatabaseConfigType.loader;
    }
    if (clientConfig) {
      return DatabaseConfigType.client;
    }
    throw new RuntimeException("Shouldnt get here: '" + connectionName + "'");
  }

 
  /**
   * look in config file for config entry if exists, if not then get the default
   * @param isClient
   * @param configName
   * @param configItemName
   * @param useGrouperHibernateAsDefault
   * @return the config value
   */
  public String retrievePoolConfigOrDefaultString(String configItemName) {
    if (StringUtils.isBlank(configItemName)) {
      throw new RuntimeException("configItemName is null!");
    }
    
    if (coreConfigOption(configItemName)) {
      throw new RuntimeException("Cant get core configs this way! '" + configItemName + "'");
    }
    
    String configValue = null;
    
    if (this.databaseConfigType != null && StringUtils.isNotBlank(this.connectionName)) {
      
      configValue = this.databaseConfigType.configValue(this.connectionName, configItemName);
      
    }

    // if not found get from grouper
    if (StringUtils.isBlank(configValue)) {

      configValue = DatabaseConfigType.grouper.configValue(null, configItemName);
      
    }
    
    return configValue;
  }
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() { 
    return "DB: user: " + this.user + ", url: " + this.url 
        + (this.databaseConfigType != null ? (", databaseConfigType: " + this.databaseConfigType) : "")
        + (StringUtils.isNotBlank(this.driver) ? (", driver: " + this.driver) : "")
        + (StringUtils.isNotBlank(this.connectionName) ? (", connectionName: " + this.connectionName) : "");
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
