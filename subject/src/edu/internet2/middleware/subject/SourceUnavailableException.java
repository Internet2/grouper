
package edu.internet2.middleware.subject;

/**
 * Indicates that the Source is not available.
 */
public class SourceUnavailableException extends RuntimeException {

  /**
   * 
   * @param msg
   */
	public SourceUnavailableException(String msg) {
		super(msg);
	}

	/**
	 * 
	 * @param msg
	 * @param cause
	 */
	public SourceUnavailableException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
