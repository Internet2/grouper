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
 * $Id: NonPooledConnectionProvider.java,v 1.2 2008-10-13 08:04:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import edu.internet2.middleware.grouperClient.jdbc.GcJdbcConnectionBean;
import edu.internet2.middleware.grouperClient.jdbc.GcJdbcConnectionProvider;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;



/**
 *
 */
public class NonPooledConnectionProvider implements GcJdbcConnectionProvider {

  /** if the connection should be readonly */
  private boolean connectionReadOnly;

  /**
   * db connect url 
   */
  private String dbUrl;

  /**
   * db username
   */
  private String dbUser;
  
  /**
   * db password
   */
  private String dbPassword;
  
  /**
   * db readonly
   */
  private Boolean readOnly;
  
  /**
   * readonly default if not specified
   */
  private boolean readOnlyDefault;
  
  /** logger */
  private static Log log = LogFactory.getLog(NonPooledConnectionProvider.class);

  /**
   * @see edu.internet2.middleware.subject.provider.JdbcConnectionProvider#connectionBean()
   */
  public GcJdbcConnectionBean connectionBean() {
    try {
      Connection connection = DriverManager.getConnection(this.dbUrl, this.dbUser, this.dbPassword);
  
      connection.setAutoCommit(false);
      connection.setReadOnly(this.connectionReadOnly);
      return new NonPooledConnectionBean(connection);
    } catch (SQLException sqlException) {
      throw new RuntimeException(sqlException);
    }
  }

  /**
   * bean to hold connection
   */
  public static class NonPooledConnectionBean implements GcJdbcConnectionBean {
    
    /** reference to connection */
    private Connection connection;
    
    /**
     * construct
     * @param theConnection
     */
    public NonPooledConnectionBean(Connection theConnection) {
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
   * @see edu.internet2.middleware.subject.provider.JdbcConnectionProvider#init(Properties, java.lang.String, java.lang.String, java.lang.Integer, int, java.lang.Integer, int, java.lang.Integer, int, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, boolean)
   */
  public void init(Properties properties, String sourceId, String driver, Integer maxActive, int defaultMaxActive,
      Integer maxIdle, int defaultMaxIdle, Integer maxWaitSeconds,
      int defaultMaxWaitSeconds, String theDbUrl, String theDbUser, String theDbPassword,
      Boolean readOnly, boolean readOnlyDefault) {
    
    try {
      Class.forName(driver).newInstance();
      log.debug("Loading JDBC driver: " + driver);
    } catch (Exception ex) {
      throw new RuntimeException("Error loading JDBC driver: "
          + driver, ex);
    }
    log.debug("JDBC driver loaded. " + driver);

    ////loads the jdbc driver 
    //try {
    //  cpds.setDriverClass(  );
    //} catch (PropertyVetoException pve) {
    //  throw new RuntimeException(pve);
    //}
    this.dbUrl = theDbUrl; 
    this.dbUser = theDbUser; 
    this.dbPassword = theDbPassword; 
    
    this.connectionReadOnly = GrouperClientUtils.defaultIfNull(readOnly, readOnlyDefault);
    
  }

  /**
   * @see edu.internet2.middleware.subject.provider.JdbcConnectionProvider#requiresJdbcConfigInSourcesXml()
   */
  public boolean requiresJdbcConfigInSourcesXml() {
    return true;
  }

}
