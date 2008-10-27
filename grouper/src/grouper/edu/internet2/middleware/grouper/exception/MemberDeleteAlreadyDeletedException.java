/**
 * 
 */
package edu.internet2.middleware.grouper.exception;

/**
 * when a member is already deleted
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class MemberDeleteAlreadyDeletedException extends MemberDeleteException {

  /**
   * 
   */
  public MemberDeleteAlreadyDeletedException() {
  }

  /**
   * @param msg
   */
  public MemberDeleteAlreadyDeletedException(String msg) {
    super(msg);
  }

  /**
   * @param msg
   * @param cause
   */
  public MemberDeleteAlreadyDeletedException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * @param cause
   */
  public MemberDeleteAlreadyDeletedException(Throwable cause) {
    super(cause);
  }

}
