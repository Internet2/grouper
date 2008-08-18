/*
 * @author mchyzer
 * $Id: InvalidQueryRuntimeException.java,v 1.1 2008-08-18 06:15:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.subj;


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
