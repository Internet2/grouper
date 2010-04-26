/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.exception;


/**
 * too many attribute def names
 */
@SuppressWarnings("serial")
public class AttributeDefNameTooManyResults extends RuntimeException {

  /**
   * 
   */
  public AttributeDefNameTooManyResults() {
  }

  /**
   * @param message
   */
  public AttributeDefNameTooManyResults(String message) {
    super(message);

  }

  /**
   * @param cause
   */
  public AttributeDefNameTooManyResults(Throwable cause) {
    super(cause);

  }

  /**
   * @param message
   * @param cause
   */
  public AttributeDefNameTooManyResults(String message, Throwable cause) {
    super(message, cause);

  }

}
