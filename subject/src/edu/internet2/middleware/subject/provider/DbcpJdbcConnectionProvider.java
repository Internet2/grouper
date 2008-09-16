/*
 * @author mchyzer $Id: DbcpJdbcConnectionProvider.java,v 1.1 2008-09-16 05:12:09 mchyzer Exp $
 */
package edu.internet2.middleware.subject.provider;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPoolFactory;

import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.SubjectUtils;


/**
 * dbcp pooling for subject api (legacy, should usually only be used if 
 * c3p0 has a problem)
 */
public class DbcpJdbcConnectionProvider implements JdbcConnectionProvider {

  /**
   * bean to hold connection
   */
  public static class DbcpJdbcConnectionBean implements JdbcConnectionBean {
    
    /** reference to connection */
    private Connection connection;
    
    /**
     * construct
     * @param theConnection
     */
    public DbcpJdbcConnectionBean(Connection theConnection) {
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

  /** data source */
  protected DataSource dataSource;


  /** logger */
  private static Log log = LogFactory.getLog(DbcpJdbcConnectionProvider.class);

  /**
   * @see edu.internet2.middleware.subject.provider.JdbcConnectionProvider#connectionBean()
   */
  public JdbcConnectionBean connectionBean() throws SQLException {
    return new DbcpJdbcConnectionBean(this.dataSource.getConnection());
  }

  /**
   * 
   * @see edu.internet2.middleware.subject.provider.JdbcConnectionProvider#init(java.lang.String, java.lang.String, java.lang.Integer, int, java.lang.Integer, int, java.lang.Integer, int, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, boolean)
   */
  public void init(String sourceId, String driver, Integer maxActive, int defaultMaxActive, Integer maxIdle, int defaultMaxIdle,
      Integer maxWaitSeconds, int defaultMaxWaitSeconds, String dbUrl, String dbUser, 
      String dbPassword, Boolean readOnly, boolean readOnlyDefault) throws SourceUnavailableException {
    GenericObjectPool objectPool = new GenericObjectPool(null);
    
    JDBCSourceAdapter.loadDriver(sourceId, driver);

    objectPool.setMaxActive(SubjectUtils.defaultIfNull(maxActive, defaultMaxActive));
    objectPool.setMaxIdle(SubjectUtils.defaultIfNull(maxIdle, defaultMaxIdle));
    long maxWaitMillis = 1000 * SubjectUtils.defaultIfNull(maxWaitSeconds, defaultMaxWaitSeconds);
    objectPool.setMaxWait(maxWaitMillis);
    objectPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
    ConnectionFactory connFactory = null;
    try {
      connFactory = new DriverManagerConnectionFactory(dbUrl, dbUser, dbPassword);
      log.debug("Connection factory initialized.");
    } catch (Exception ex) {
      throw new SourceUnavailableException(
              "Error initializing connection factory: " + dbUrl + ", source: " + sourceId, ex);
    }
    try {
      // StackKeyedObjectPoolFactory supports PreparedStatement pooling.
      new PoolableConnectionFactory(
              connFactory,
              objectPool,
              new StackKeyedObjectPoolFactory(),
              null,
              SubjectUtils.defaultIfNull(readOnly, readOnlyDefault),
              true);
    } catch (Exception ex) {
        throw new SourceUnavailableException(
                "Error initializing poolable connection factory, source: " + sourceId, ex);
    }
    this.dataSource = new PoolingDataSource(objectPool);

  }

}
