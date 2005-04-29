
package edu.internet2.middleware.subject;

/**
 * Indicates that the Source is not available.
 */
public class SourceUnavailableException extends Exception {

	public SourceUnavailableException(String msg) {
		super(msg);
	}

	public SourceUnavailableException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
