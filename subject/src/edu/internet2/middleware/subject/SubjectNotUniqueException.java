
package edu.internet2.middleware.subject;

/**
 * Indicates that a Subject that a subject assumed to be unique is not.
 */
public class SubjectNotUniqueException extends RuntimeException {

  /**
   * 
   * @param msg
   */
	public SubjectNotUniqueException(String msg) {
		super(msg);
	}

	/**
	 * 
	 * @param msg
	 * @param cause
	 */
	public SubjectNotUniqueException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
