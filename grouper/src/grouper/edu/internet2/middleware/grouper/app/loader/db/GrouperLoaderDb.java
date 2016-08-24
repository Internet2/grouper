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
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;


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
   * get a connection from the db
   * @return the connection
   */
  public Connection connection() {

    try {

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
      
      ComboPooledDataSource comboPooledDataSource = StringUtils.isBlank(configName) ? new ComboPooledDataSource() : new ComboPooledDataSource(configName);
      
      comboPooledDataSource.setDriverClass(this.driver); //loads the jdbc driver
      comboPooledDataSource.setJdbcUrl(this.url);
      comboPooledDataSource.setUser(this.user);
      comboPooledDataSource.setPassword(this.pass);
      
      Integer minSize = null;
      Integer maxSize = null;
      Integer timeout = null;
      Integer maxStatements = null;
      Integer idleTestPeriod = null;
      Integer acquireIncrement = null;
      Boolean validate = null;

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
      if (!StringUtils.isBlank(configName)) {
        minSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("db." + configName + ".c3p0.min_size");
        maxSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("db." + configName + ".c3p0.max_size");
        timeout = GrouperLoaderConfig.retrieveConfig().propertyValueInt("db." + configName + ".c3p0.timeout");
        maxStatements = GrouperLoaderConfig.retrieveConfig().propertyValueInt("db." + configName + ".c3p0.max_statements");
        idleTestPeriod = GrouperLoaderConfig.retrieveConfig().propertyValueInt("db." + configName + ".c3p0.idle_test_period");
        acquireIncrement = GrouperLoaderConfig.retrieveConfig().propertyValueInt("db." + configName + ".c3p0.acquire_increment");
        validate = GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("db." + configName + ".c3p0.validate");
      }

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
      if (minSize == null) {
        minSize = GrouperHibernateConfig.retrieveConfig().propertyValueInt("hibernate.c3p0.min_size");
      }
      if (maxSize == null) {
        maxSize = GrouperHibernateConfig.retrieveConfig().propertyValueInt("hibernate.c3p0.max_size");
      }
      if (timeout == null) {
        timeout = GrouperHibernateConfig.retrieveConfig().propertyValueInt("hibernate.c3p0.timeout");
      }
      if (maxStatements == null) {
        maxStatements = GrouperHibernateConfig.retrieveConfig().propertyValueInt("hibernate.c3p0.max_statements");
      }
      if (idleTestPeriod == null) {
        idleTestPeriod = GrouperHibernateConfig.retrieveConfig().propertyValueInt("hibernate.c3p0.idle_test_period");
      }
      if (acquireIncrement == null) {
        acquireIncrement = GrouperHibernateConfig.retrieveConfig().propertyValueInt("hibernate.c3p0.acquire_increment");
      }
      if (validate == null) {
        validate = GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("hibernate.c3p0.validate");
      }

      //if set, set them, otherwise defaults
      if (minSize != null) {
        comboPooledDataSource.setMinPoolSize(minSize);
      }
      if (maxSize != null) {
        comboPooledDataSource.setMaxPoolSize(maxSize);
      }
      if (timeout != null) {
        comboPooledDataSource.setMaxIdleTime(timeout);
      }
      if (maxStatements != null) {
        comboPooledDataSource.setMaxStatements(maxStatements);
      }
      if (idleTestPeriod != null) {
        comboPooledDataSource.setIdleConnectionTestPeriod(idleTestPeriod);
      }
      if (acquireIncrement != null) {
        comboPooledDataSource.setAcquireIncrement(acquireIncrement);
      }
      if (validate != null) {
        //i assume this is the setting... hmmm
        comboPooledDataSource.setTestConnectionOnCheckout(validate);
      }

      //is it there now????
      dataSource = retrieveDataSourceFromC3P0(this.url, this.user);
      
      return dataSource.getConnection();
      
    } catch (Exception e) {
      throw new RuntimeException("Problems with db: " + this, e);
    }
    
  }
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() { 
    return "DB: user: " + this.user + ", url: " + this.url + ", driver: " + this.driver;
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
   * construct with all fields
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
    this.driver = GrouperDdlUtils.convertUrlToDriverClassIfNeeded(url1, driver1);
  }

  
}
