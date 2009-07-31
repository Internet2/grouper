/*
 * @author mchyzer
 * $Id: LogoutObject.java,v 1.1 2009-07-31 14:27:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.json;


/**
 * bean passed back after logout
 */
public class LogoutObject {

  /** if logout was a success (not sure why it wouldnt) */
  private boolean success;

  /**
   * 
   * @return if success
   */
  public boolean isSuccess() {
    return this.success;
  }

  /**
   * 
   * @param success1
   */
  public void setSuccess(boolean success1) {
    this.success = success1;
  }
  
  
}
