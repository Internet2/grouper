/*
 * @author mchyzer
 * $Id: GrouperReportException.java,v 1.1 2008-12-11 05:49:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.misc;


/**
 * exception thrown from grouper report
 */
@SuppressWarnings("serial")
public class GrouperReportException extends RuntimeException {

  /** current result of grouper report */
  private String result;
  
  /**
   * current result of grouper report
   * @return the result
   */
  public String getResult() {
    return this.result;
  }

  /**
   * current result of grouper report
   * @param result1
   */
  public void setResult(String result1) {
    this.result = result1;
  }

  /**
   * 
   */
  public GrouperReportException() {
  }

  /**
   * @param message
   */
  public GrouperReportException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public GrouperReportException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public GrouperReportException(String message, Throwable cause) {
    super(message, cause);
  }

}
