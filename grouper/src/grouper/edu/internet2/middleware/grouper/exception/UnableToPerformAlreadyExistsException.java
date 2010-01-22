/**
 * 
 */
package edu.internet2.middleware.grouper.exception;

/**
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class UnableToPerformAlreadyExistsException extends
    UnableToPerformException {

  /**
   * 
   */
  public UnableToPerformAlreadyExistsException() {
  }

  /**
   * @param msg
   */
  public UnableToPerformAlreadyExistsException(String msg) {
    super(msg);
  }

  /**
   * @param msg
   * @param cause
   */
  public UnableToPerformAlreadyExistsException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * @param cause
   */
  public UnableToPerformAlreadyExistsException(Throwable cause) {
    super(cause);
  }
}
