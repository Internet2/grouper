
package edu.internet2.middleware.subject;

/**
 * Indicates that a Subject that a subject assumed to be unique is not.
 */
public class SubjectNotUniqueException extends Exception {

	public SubjectNotUniqueException(String msg) {
		super(msg);
	}

	public SubjectNotUniqueException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
