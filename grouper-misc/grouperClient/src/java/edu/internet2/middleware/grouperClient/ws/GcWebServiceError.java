/*
 * @author mchyzer
 * $Id: GcWebServiceError.java,v 1.1 2008-12-08 02:55:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws;


/**
 * exception thrown if non success with web service
 */
public class GcWebServiceError extends RuntimeException {

  /** container response object */
  private Object containerResponseObject;
  
  /**
   * @param theContainerResponseObject is the container that had a problem
   */
  public GcWebServiceError(Object theContainerResponseObject) {
    super();
    this.containerResponseObject = theContainerResponseObject;
  }

  /**
   * container response object 
   * @return the container
   */
  public Object getContainerResponseObject() {
    return this.containerResponseObject;
  }

  /**
   * @param theContainerResponseObject is the container that had a problem
   * @param message
   * @param cause
   */
  public GcWebServiceError(Object theContainerResponseObject, String message, Throwable cause) {
    super(message, cause);
    this.containerResponseObject = theContainerResponseObject;
  }

  /**
   * @param theContainerResponseObject is the container that had a problem
   * @param message
   */
  public GcWebServiceError(Object theContainerResponseObject, String message) {
    super(message);
    this.containerResponseObject = theContainerResponseObject;
  }

  /**
   * @param theContainerResponseObject is the container that had a problem
   * @param cause
   */
  public GcWebServiceError(Object theContainerResponseObject, Throwable cause) {
    super(cause);
    this.containerResponseObject = theContainerResponseObject;
  }

}
