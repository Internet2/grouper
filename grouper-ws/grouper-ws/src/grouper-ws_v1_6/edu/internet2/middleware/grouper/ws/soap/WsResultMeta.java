/*
 * @author mchyzer $Id: WsResultMeta.java,v 1.7 2008-12-04 07:51:33 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ws.WsResultCode;

/**
 * result metadata (if success, result code, etc) for one result
 * (each ws call can have one or many result metadatas)
 * @see WsResultCode for all implementations of responses
 */
public class WsResultMeta {

  /** params for result */
  private WsParam[] params = null;
  
  /**
   * <pre>
   * code of the result for this subject
   * SUCCESS: means everything ok
   * SUBJECT_NOT_FOUND: cant find the subject
   * SUBJECT_DUPLICATE: found multiple subjects
   *  
   * </pre>
   */
  private String resultCode;

  /**
   * <pre>
   * reserved for future purposes
   *  
   * </pre>
   */
  private String resultCode2;

  /**
   * error message if there is an error
   */
  private StringBuilder resultMessage = null;

  /** T or F as to whether it was a successful assignment */
  private String success;

  /**
   * <pre>
   * code of the result for this subject
   * SUCCESS: means everything ok
   * SUBJECT_NOT_FOUND: cant find the subject
   * SUBJECT_DUPLICATE: found multiple subjects
   *  
   * </pre>
   * 
   * @return the resultCode
   */
  public String getResultCode() {
    return this.resultCode;
  }

  /**
   * <pre>
   * reserved for future purpose
   * </pre>
   * 
   * @return the resultCode
   */
  public String getResultCode2() {
    return this.resultCode2;
  }

  /**
   * error message if there is an error
   * 
   * @return the errorMessage
   */
  public String getResultMessage() {
    return this.resultMessage == null ? null : StringUtils.trimToNull(this.resultMessage.toString());
  }

  /**
   * T or F as to whether it was a successful assignment
   * 
   * @return the success
   */
  public String getSuccess() {
    return this.success;
  }

  
  /**
   * @param resultCode1 the resultCode to set
   */
  public void setResultCode(String resultCode1) {
    this.resultCode = resultCode1;
  }

  /**
   * @param resultCode1 the resultCode2 to set
   */
  public void setResultCode2(String resultCode1) {
    this.resultCode2 = resultCode1;
  }

  /**
   * T or F as to whether it was a successful assignment
   * @param theSuccess T | F
   */
  public void setSuccess(String theSuccess) {
    this.success = theSuccess;
  }

  /**
   * error message if there is an error
   * 
   * @param errorMessage
   *            the errorMessage to set
   */
  public void setResultMessage(String errorMessage) {
    if (StringUtils.isBlank(errorMessage)) {
      this.resultMessage = null;
    } else {
      this.resultMessage = new StringBuilder(errorMessage);
    }
  }

  /**
   * @return the params
   */
  public WsParam[] getParams() {
    return this.params;
  }

  
  /**
   * @param params1 the params to set
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

}
