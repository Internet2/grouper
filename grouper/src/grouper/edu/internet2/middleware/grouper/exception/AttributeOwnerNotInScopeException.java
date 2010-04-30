/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.exception;


/**
 * when there are scopes on an attribute def, then attribute def name cannot
 * be assigned to an owner which is not in scope
 */
@SuppressWarnings("serial")
public class AttributeOwnerNotInScopeException extends RuntimeException {

  /**
   * 
   */
  public AttributeOwnerNotInScopeException() {
  }

  /**
   * @param message
   */
  public AttributeOwnerNotInScopeException(String message) {
    super(message);

  }

  /**
   * @param cause
   */
  public AttributeOwnerNotInScopeException(Throwable cause) {
    super(cause);

  }

  /**
   * @param message
   * @param cause
   */
  public AttributeOwnerNotInScopeException(String message, Throwable cause) {
    super(message, cause);

  }

}
