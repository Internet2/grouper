/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.exception;


/**
 * if in readonly mode from grouper.properties
 */
@SuppressWarnings("serial")
public class GrouperReadonlyException extends RuntimeException {

  /**
   * 
   */
  public GrouperReadonlyException() {
  }

  /**
   * @param message
   */
  public GrouperReadonlyException(String message) {
    super(message);

  }

  /**
   * @param cause
   */
  public GrouperReadonlyException(Throwable cause) {
    super(cause);

  }

  /**
   * @param message
   * @param cause
   */
  public GrouperReadonlyException(String message, Throwable cause) {
    super(message, cause);

  }

}
