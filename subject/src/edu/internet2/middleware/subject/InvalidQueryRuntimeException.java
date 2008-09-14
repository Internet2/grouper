/*
 * @author mchyzer
 * $Id: InvalidQueryRuntimeException.java,v 1.1 2008-09-14 04:54:05 mchyzer Exp $
 */
package edu.internet2.middleware.subject;


/**
 * invalid query runtime
 */
public class InvalidQueryRuntimeException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public InvalidQueryRuntimeException() {
    super();
  }

  /**
   * @param message
   * @param cause
   */
  public InvalidQueryRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   */
  public InvalidQueryRuntimeException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public InvalidQueryRuntimeException(Throwable cause) {
    super(cause);
  }

}
