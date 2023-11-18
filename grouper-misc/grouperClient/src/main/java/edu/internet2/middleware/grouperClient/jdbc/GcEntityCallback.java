package edu.internet2.middleware.grouperClient.jdbc;


/**
 * Object that gets a call for each entity created from a resultset; save heap space.
 * @param <T> is the type of object that will be returned.
 * @author harveycg
 *
 */
public abstract class GcEntityCallback<T> {

	/**
	 * Get a callback for each entity created from the rows of a resultset; save heap space.
	 * @param t is the thing hydrated from the resultset.
	 * @return true to continue, false to exit the resulset scroll.
	 * @throws Exception if things go wrong in your code.
	 */
	public abstract boolean callback(T t) throws Exception;
	
	
}
