
package edu.internet2.middleware.subject;

/**
 * Indicates that the query specified is not in a valid format.
 */
public class InvalidQueryException extends Exception {

	public InvalidQueryException(String msg) {
		super(msg);
	}

	public InvalidQueryException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
