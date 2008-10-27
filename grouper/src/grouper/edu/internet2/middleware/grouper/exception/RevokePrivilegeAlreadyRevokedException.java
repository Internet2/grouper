/**
 * 
 */
package edu.internet2.middleware.grouper.exception;

/**
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class RevokePrivilegeAlreadyRevokedException extends
    RevokePrivilegeException {

  /**
   * 
   */
  public RevokePrivilegeAlreadyRevokedException() {
  }

  /**
   * @param msg
   */
  public RevokePrivilegeAlreadyRevokedException(String msg) {
    super(msg);
  }

  /**
   * @param msg
   * @param cause
   */
  public RevokePrivilegeAlreadyRevokedException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * @param cause
   */
  public RevokePrivilegeAlreadyRevokedException(Throwable cause) {
    super(cause);
  }

}
