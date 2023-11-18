package edu.internet2.middleware.grouperClient.jdbc;



/**
 * Object that gets a dbAccess object with a session in it.
 * @param <T> is the type of object that will be returned.
 * @author harveycg
 *
 */
public abstract class GcTransactionCallback<T> {

	/**
	 * <pre>Get access to the dbAccess with a session. If no excpetions are thrown, the session will be automatically committed.
	 * You can also commit or rollback manually by calling DbAccess.commitSession() or dbAccess.rollbackSession().</pre>
	 * @param dbAccess is the access instance.
	 * @return the correct type.
	 */
	public abstract T callback(GcDbAccess dbAccess);
	
	
}
