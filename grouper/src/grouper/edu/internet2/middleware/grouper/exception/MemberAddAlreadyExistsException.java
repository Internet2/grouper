/**
 * 
 */
package edu.internet2.middleware.grouper.exception;

/**
 * when a membership already exists
 * @author mchyzer
 *
 */
public class MemberAddAlreadyExistsException extends MemberAddException {

  /**
   * 
   */
  public MemberAddAlreadyExistsException() {
  }

  /**
   * @param msg
   */
  public MemberAddAlreadyExistsException(String msg) {
    super(msg);
  }

  /**
   * @param msg
   * @param cause
   */
  public MemberAddAlreadyExistsException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * @param cause
   */
  public MemberAddAlreadyExistsException(Throwable cause) {
    super(cause);
  }

}
