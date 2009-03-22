
package edu.internet2.middleware.subject;

/**
 * Indicates that a Subject is not found within a Source.
 */
public class SubjectNotFoundException extends RuntimeException {

  /**
   * 
   * @param msg
   */
	public SubjectNotFoundException(String msg) {
		super(msg);
	}

	/**
	 * 
	 * @param msg
	 * @param cause
	 */
	public SubjectNotFoundException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
