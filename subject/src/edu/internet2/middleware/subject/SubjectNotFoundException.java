
package edu.internet2.middleware.subject;

/**
 * Indicates that a Subject is not found within a Source.
 */
public class SubjectNotFoundException extends Exception {

	public SubjectNotFoundException(String msg) {
		super(msg);
	}

	public SubjectNotFoundException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
