/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.simpleMembershipUpdate;


/**
 * thrown when a config value is not found
 */
public class MembershipLiteConfigNotFoundException extends RuntimeException {

  /**
   * 
   */
  public MembershipLiteConfigNotFoundException() {
  }

  /**
   * @param message
   */
  public MembershipLiteConfigNotFoundException(String message) {
    super(message);

  }

  /**
   * @param cause
   */
  public MembershipLiteConfigNotFoundException(Throwable cause) {
    super(cause);

  }

  /**
   * @param message
   * @param cause
   */
  public MembershipLiteConfigNotFoundException(String message, Throwable cause) {
    super(message, cause);

  }

}
