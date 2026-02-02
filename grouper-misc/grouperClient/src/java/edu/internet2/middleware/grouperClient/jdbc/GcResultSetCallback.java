package edu.internet2.middleware.grouperClient.jdbc;

import java.sql.ResultSet;


/**
 * Object that gets a resulset via callback.
 * @param <T> is the type of object that will be returned.
 * @author harveycg
 *
 */
public abstract class GcResultSetCallback<T> {

	/**
	 * Get access to the resultset.
	 * @param resultSet is the resultSet.
	 * @return whatever you want to.
	 * @throws Exception if things go wrong in your code.
	 */
	public abstract T callback(ResultSet resultSet) throws Exception;
	
	
}
