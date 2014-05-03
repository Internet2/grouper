/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui;


/**
 * when no user is authenticated
 */
public class NoUserAuthenticatedException extends RuntimeException {

  /**
   * 
   */
  public NoUserAuthenticatedException() {
  }

  /**
   * @param message
   */
  public NoUserAuthenticatedException(String message) {
    super(message);

  }

  /**
   * @param cause
   */
  public NoUserAuthenticatedException(Throwable cause) {
    super(cause);

  }

  /**
   * @param message
   * @param cause
   */
  public NoUserAuthenticatedException(String message, Throwable cause) {
    super(message, cause);

  }

}
