/**
 * @author mchyzer
 * $Id: NoSessionException.java,v 1.1 2009-09-09 15:10:03 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.exceptions;


/**
 * when the invalid session is detected (container not there on internal call)
 */
public class NoSessionException extends RuntimeException {

  /**
   * 
   */
  public NoSessionException() {
  }

  /**
   * @param message
   */
  public NoSessionException(String message) {
    super(message);
    
  }
  
}
