
package edu.internet2.middleware.subject;

/**
 * Indicates that too many results where found in findAll search in Source.
 */
public class SubjectTooManyResults extends RuntimeException {

  /**
   * 
   * @param msg
   */
	public SubjectTooManyResults(String msg) {
		super(msg);
	}

	/**
	 * 
	 * @param msg
	 * @param cause
	 */
	public SubjectTooManyResults(String msg, Throwable cause) {
		super(msg, cause);
	}

}
