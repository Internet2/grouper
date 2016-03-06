/*
 * @author mchyzer $Id: GrouperRestInvalidRequest.java,v 1.1 2008-03-25 05:15:11 mchyzer Exp $
 */
package edu.internet2.middleware.tierApiAuthzServer.exceptions;

/**
 * exception when there is not a valid request from client
 * this must be called before any response is written
 */
public class AsasRestInvalidRequest extends RuntimeException {

  /**
   * default id
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public AsasRestInvalidRequest() {
    //empty constructor
  }

  /**
   * @param message
   */
  public AsasRestInvalidRequest(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public AsasRestInvalidRequest(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public AsasRestInvalidRequest(String message, Throwable cause) {
    super(message, cause);
  }

}
