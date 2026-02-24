package edu.internet2.middleware.grouper.exception;


/**
 * thrown when a delete is attempted on a configuration that is still referenced by other configurations
 */
@SuppressWarnings("serial")
public class GrouperReferentialIntegrityException extends RuntimeException {

  /**
   *
   */
  public GrouperReferentialIntegrityException() {
  }

  /**
   * @param message
   */
  public GrouperReferentialIntegrityException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public GrouperReferentialIntegrityException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public GrouperReferentialIntegrityException(String message, Throwable cause) {
    super(message, cause);
  }

}
