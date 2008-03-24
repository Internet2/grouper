/*
 * @author mchyzer $Id: GrouperLiteInvalidRequest.java,v 1.1 2008-03-24 20:19:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.lite;

/**
 * exception when there is not a valid request from client
 * this must be called before any response is written
 */
public class GrouperLiteInvalidRequest extends RuntimeException {

  /**
   * default id
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public GrouperLiteInvalidRequest() {
    //empty constructor
  }

  /**
   * @param message
   */
  public GrouperLiteInvalidRequest(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public GrouperLiteInvalidRequest(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public GrouperLiteInvalidRequest(String message, Throwable cause) {
    super(message, cause);
  }

}
