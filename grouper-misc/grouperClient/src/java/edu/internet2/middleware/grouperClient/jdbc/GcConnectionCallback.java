package edu.internet2.middleware.grouperClient.jdbc;

import java.sql.Connection;



/**
 * Object that gets a database connection object.
 * @param <T> is the type of object that will be returned.
 *
 */
public abstract class GcConnectionCallback<T> {

	/**
	 * <pre>Get access to the database connection. If no exception are thrown, the session will be automatically committed.</pre>
	 * @param connection is the connection access.
	 * @return the correct type.
	 */
	public abstract T callback(Connection connection);
	
	
}
