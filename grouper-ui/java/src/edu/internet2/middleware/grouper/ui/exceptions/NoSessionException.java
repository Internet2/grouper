/**
 * @author mchyzer
 * $Id: NoSessionException.java,v 1.1 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.exceptions;


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
