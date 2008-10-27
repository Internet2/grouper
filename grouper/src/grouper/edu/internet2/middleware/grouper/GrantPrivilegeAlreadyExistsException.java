/**
 * 
 */
package edu.internet2.middleware.grouper;

import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;

/**
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class GrantPrivilegeAlreadyExistsException extends
    GrantPrivilegeException {

  /**
   * 
   */
  public GrantPrivilegeAlreadyExistsException() {
  }

  /**
   * @param msg
   */
  public GrantPrivilegeAlreadyExistsException(String msg) {
    super(msg);
  }

  /**
   * @param msg
   * @param cause
   */
  public GrantPrivilegeAlreadyExistsException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * @param cause
   */
  public GrantPrivilegeAlreadyExistsException(Throwable cause) {
    super(cause);
  }

}
