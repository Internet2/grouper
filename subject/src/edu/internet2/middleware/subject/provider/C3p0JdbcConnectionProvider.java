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
 * $Id: C3p0JdbcConnectionProvider.java,v 1.2 2008-10-13 08:04:29 mchyzer Exp $
 */
package edu.internet2.middleware.subject.provider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.SubjectUtils;


/**
 *
 */
public class C3p0JdbcConnectionProvider implements JdbcConnectionProvider {

  /** if the connection should be readonly */
  private boolean connectionReadOnly;

  /** combo pooled data source */
  private ComboPooledDataSource comboPooledDataSource;
  
  /** logger */
  private static Log log = LogFactory.getLog(C3p0JdbcConnectionProvider.class);

  /**
   * @see edu.internet2.middleware.subject.provider.JdbcConnectionProvider#connectionBean()
   */
  public JdbcConnectionBean connectionBean() throws SQLException {
    Connection connection = this.comboPooledDataSource.getConnection();
    connection.setReadOnly(this.connectionReadOnly);
    return new C3p0JdbcConnectionBean(connection);
  }

  /**
   * bean to hold connection
   */
  public static class C3p0JdbcConnectionBean implements JdbcConnectionBean {
    
    /** reference to connection */
    private Connection connection;
    
    /**
     * construct
     * @param theConnection
     */
    public C3p0JdbcConnectionBean(Connection theConnection) {
      this.connection = theConnection;
    }
    /**
     * @see edu.internet2.middleware.subject.provider.JdbcConnectionBean#connection()
     */
    public Connection connection() throws SQLException {
      return this.connection;
    }
    /**
     * @see edu.internet2.middleware.subject.provider.JdbcConnectionBean#doneWithConnection()
     */
    public void doneWithConnection() {
    }
    /**
     * @see edu.internet2.middleware.subject.provider.JdbcConnectionBean#doneWithConnectionError(java.lang.Throwable)
     */
    public void doneWithConnectionError(Throwable t) {
      throw new RuntimeException(t);
    }

    /**
     * @see edu.internet2.middleware.subject.provider.JdbcConnectionBean#doneWithConnectionFinally()
     */
    public void doneWithConnectionFinally() {
      if (this.connection != null) {
        try {
          //this doesnt actually close it, just returns to pool
          this.connection.close();
        } catch (SQLException ex) {
          log.info("Error while closing JDBC Connection.", ex);
        }
      }
    }
    
  }

  /**
   * 
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    DataSources.destroy( this.comboPooledDataSource );
  }

  /**
   * @see edu.internet2.middleware.subject.provider.JdbcConnectionProvider#init(Properties, java.lang.String, java.lang.String, java.lang.Integer, int, java.lang.Integer, int, java.lang.Integer, int, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, boolean)
   */
  public void init(Properties properties, String sourceId, String driver, Integer maxActive, int defaultMaxActive,
      Integer maxIdle, int defaultMaxIdle, Integer maxWaitSeconds,
      int defaultMaxWaitSeconds, String dbUrl, String dbUser, String dbPassword,
      Boolean readOnly, boolean readOnlyDefault) throws SourceUnavailableException {
    
    this.comboPooledDataSource = new ComboPooledDataSource(); 
    
    JDBCSourceAdapter.loadDriver(sourceId, driver);

    ////loads the jdbc driver 
    //try {
    //  cpds.setDriverClass(  );
    //} catch (PropertyVetoException pve) {
    //  throw new RuntimeException(pve);
    //}
    this.comboPooledDataSource.setJdbcUrl( dbUrl ); 
    this.comboPooledDataSource.setUser(dbUser); 
    this.comboPooledDataSource.setPassword(dbPassword); 
    this.comboPooledDataSource.setMaxPoolSize(SubjectUtils.defaultIfNull(maxActive, defaultMaxActive));
    
    //this isnt used
    if (maxIdle != null) {
      log.warn("maxIdle is not available for c3p0 (in subject API: " + sourceId + ")");
    }
    int checkoutTimeout = 1000*SubjectUtils.defaultIfNull(maxWaitSeconds, defaultMaxWaitSeconds);
    //if checkout timeout is less than 0, then make it 5 minutes
    checkoutTimeout = checkoutTimeout < 0 ? (5 * 60 * 1000) : checkoutTimeout; 
    this.comboPooledDataSource.setCheckoutTimeout(checkoutTimeout);
    this.connectionReadOnly = SubjectUtils.defaultIfNull(readOnly, readOnlyDefault);
    
    {
      //max connection age is if the connection should eventually be thrown out (e.g. for firewalls)
      String maxConnectionAgeString = properties.getProperty("maxConnectionAge");
      
      if (!StringUtils.isBlank(maxConnectionAgeString)) {
        int maxConnectionAgeInteger = SubjectUtils.intValue(maxConnectionAgeString);
        this.comboPooledDataSource.setMaxConnectionAge(maxConnectionAgeInteger);
      }
    }
    
    {
      //if connections should be tested on checkout
      String testConnectionOnCheckoutString = properties.getProperty("testConnectionOnCheckout");
      
      if (!StringUtils.isBlank(testConnectionOnCheckoutString)) {
        boolean testConnectionOnCheckout = SubjectUtils.booleanValue(testConnectionOnCheckoutString);
        this.comboPooledDataSource.setTestConnectionOnCheckout(testConnectionOnCheckout);
      }
            
    }
    
    {
      //test query in database
      String preferredTestQuery = properties.getProperty("preferredTestQuery");
      
      if (!StringUtils.isBlank(preferredTestQuery)) {
        this.comboPooledDataSource.setPreferredTestQuery(preferredTestQuery);
      }
            
    }

    
    {
      //number of seconds to test connections in pool
      String idleConnectionTestPeriodString = properties.getProperty("idleConnectionTestPeriod");
      
      if (!StringUtils.isBlank(idleConnectionTestPeriodString)) {
        int idleConnectionTestPeriodInteger = SubjectUtils.intValue(idleConnectionTestPeriodString);
        this.comboPooledDataSource.setIdleConnectionTestPeriod(idleConnectionTestPeriodInteger);
      }
            
    }
    
    
  }

  /**
   * @see edu.internet2.middleware.subject.provider.JdbcConnectionProvider#requiresJdbcConfigInSourcesXml()
   */
  public boolean requiresJdbcConfigInSourcesXml() {
    return true;
  }

}
