package edu.internet2.middleware.grouperClient.jdbc;


import java.sql.PreparedStatement;
import java.sql.SQLException;



/**
 * Object that gets a preparedStatement back from the connection and the sql - closing it is handled within the framework.
 * @param <T> is the type of object that will be returned.
 *
 */
public abstract class GcPreparedStatementCallback<T> {

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
  public GcPreparedStatementCallback(String _query){
    this.query = _query;
  }
  
	/**
	 * <pre>Get access to the database connection. If no exceptions are thrown, the session will be automatically committed.</pre>
	 * @param preparedStatement is the connection access.
	 * @throws SQLException is thrown if things go wrong.
	 * @return the correct type.
	 */
	public abstract T callback(PreparedStatement preparedStatement)  throws SQLException;
	
	
}
