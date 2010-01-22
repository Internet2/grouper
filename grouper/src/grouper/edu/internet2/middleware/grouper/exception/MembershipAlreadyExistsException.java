/**
 * 
 */
package edu.internet2.middleware.grouper.exception;

/**
 * 
 * Exception if a membership already exists
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class MembershipAlreadyExistsException extends IllegalStateException {

  /**
   * 
   */
  public MembershipAlreadyExistsException() {
  }

  /**
   * @param arg0
   */
  public MembershipAlreadyExistsException(String arg0) {
    super(arg0);
  }

  /**
   * @param arg0
   */
  public MembershipAlreadyExistsException(Throwable arg0) {
    super(arg0);
  }

  /**
   * @param arg0
   * @param arg1
   */
  public MembershipAlreadyExistsException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }
}
