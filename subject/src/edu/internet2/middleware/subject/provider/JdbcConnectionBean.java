/*
 * @author mchyzer
 * $Id: JdbcConnectionBean.java,v 1.1 2008-09-16 05:12:09 mchyzer Exp $
 */
package edu.internet2.middleware.subject.provider;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * bean that wraps connections
 */
public interface JdbcConnectionBean {

  /**
   * get a connection (dont close this when done, just call "doneWithConnection()"
   * @return the connection
   * @throws SQLException if there is a problem
   */
  public Connection connection() throws SQLException;
  
  /**
   * call this when the connection is done.  This will do any cleanup
   * this is a null-safe method.  
   * @throws SQLException if there is a problem
   */
  public void doneWithConnection() throws SQLException;

  /**
   * call this when the connection is not needed, in the finally block
   * this might return to pool.  In general, this shouldnt throw exceptions
   * since it is done in a finally block, it should only log them
   */
  public void doneWithConnectionFinally();

  /**
   * call this when the connection is done but there was an error, will pass
   * an exception.  This should do whatever and rethrow the exception as runtime
   * @param t 
   */
  public void doneWithConnectionError(Throwable t);
}
