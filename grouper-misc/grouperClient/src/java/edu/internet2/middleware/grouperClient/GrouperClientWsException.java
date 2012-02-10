package edu.internet2.middleware.grouperClient;

/**
 * grouper client ws exception
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class GrouperClientWsException extends RuntimeException {

  /**
   * result object
   */
  private Object resultObject;
  
  /**
   * result object
   * @return result object
   */
  public Object getResultObject() {
    return this.resultObject;
  }

  /**
   * result object
   * @param resultObject1
   */
  public void setResultObject(Object resultObject1) {
    this.resultObject = resultObject1;
  }

  /**
   * @param theResultObject 
   * 
   */
  public GrouperClientWsException(Object theResultObject) {
    this.resultObject = theResultObject;
  }

  /**
   * 
   * @param theResultObject 
   * @param message
   */
  public GrouperClientWsException(Object theResultObject, String message) {
    super(message);
    this.resultObject = theResultObject;
  }

  /**
   * 
   * @param theResultObject 
   * @param cause
   */
  public GrouperClientWsException(Object theResultObject, Throwable cause) {
    super(cause);
    this.resultObject = theResultObject;
  }

  /**
   * 
   * @param theResultObject 
   * @param message
   * @param cause
   */
  public GrouperClientWsException(Object theResultObject, String message, Throwable cause) {
    super(message, cause);
    this.resultObject = theResultObject;
  }

}
