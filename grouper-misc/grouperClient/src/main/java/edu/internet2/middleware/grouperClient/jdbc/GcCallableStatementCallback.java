package edu.internet2.middleware.grouperClient.jdbc;

import java.sql.CallableStatement;
import java.sql.SQLException;



/**
 * Object that gets a callableStatement back from the connection and the sql - closing it is handled within the framework.
 * @param <T> is the type of object that will be returned.
 * @author harveycg
 */
public abstract class GcCallableStatementCallback<T> {

  /**
   * The query to create the callableStatement against.
   */
  private String query;
  
  
  
  /**
   * @return the query
   */
  public String getQuery() {
    return this.query;
  }

  /**
   * Create a callableStatement from the query given.
   * @param _query
   * 
   */
  public GcCallableStatementCallback(String _query){
    this.query = _query;
  }
  
	/**
	 * <pre>Get access to the database connection. If no exception are thrown, the session will be automatically committed.</pre>
	 * @param callableStatement is the connection access.
	 * @throws SQLException is thrown if things go wrong.
	 * @return the correct type.
	 */
	public abstract T callback(CallableStatement callableStatement)  throws SQLException;
	
	
}
