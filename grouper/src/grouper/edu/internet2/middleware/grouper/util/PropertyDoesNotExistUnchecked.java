/*
 * @author mchyzer
 * $Id: PropertyDoesNotExistUnchecked.java,v 1.1.2.1 2008-03-19 18:46:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;


/**
 * unchecked property does not exist exception
 */
public class PropertyDoesNotExistUnchecked extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public PropertyDoesNotExistUnchecked() {
    super();
  }

  /**
   * @param message
   * @param cause
   */
  public PropertyDoesNotExistUnchecked(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   */
  public PropertyDoesNotExistUnchecked(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public PropertyDoesNotExistUnchecked(Throwable cause) {
    super(cause);
  }

}
