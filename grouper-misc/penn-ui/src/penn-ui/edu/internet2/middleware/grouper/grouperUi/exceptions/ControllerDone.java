/**
 * @author mchyzer
 * $Id: ControllerDone.java,v 1.1 2009-08-05 00:57:20 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.exceptions;


/**
 * when the servlet is done with no error
 */
public class ControllerDone extends RuntimeException {

  /**
   * 
   */
  public ControllerDone() {
  }

  /**
   * @param message
   */
  public ControllerDone(String message) {
    super(message);

  }

  /**
   * @param cause
   */
  public ControllerDone(Throwable cause) {
    super(cause);

  }

  /**
   * @param message
   * @param cause
   */
  public ControllerDone(String message, Throwable cause) {
    super(message, cause);

  }

}
