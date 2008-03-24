/*
 * @author mchyzer $Id: WsInvalidQueryException.java,v 1.1 2008-03-24 20:19:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.exceptions;

/**
 * web service can throw this when invalid input.  will auto-set
 * the response code and message to the message of this exception (wont print stack)
 */
public class WsInvalidQueryException extends RuntimeException {

  /**
   * id
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public WsInvalidQueryException() {
    //empty constructor
  }

  /**
   * @param message
   */
  public WsInvalidQueryException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public WsInvalidQueryException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public WsInvalidQueryException(String message, Throwable cause) {
    super(message, cause);
  }

}
