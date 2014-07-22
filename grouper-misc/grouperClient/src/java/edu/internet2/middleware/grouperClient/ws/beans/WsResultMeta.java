/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author mchyzer $Id: WsResultMeta.java,v 1.2 2008-12-04 07:51:39 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws.beans;

/**
 * result metadata (if success, result code, etc) for one result
 * (each ws call can have one or many result metadatas)
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

  /** T or F as to whether it was a successful assignment */
  private String success;

  /** status code, if 500, then not set */
  private int httpStatusCode = 500;

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
    return this.resultMessage;
  }

  /** result message */
  private String resultMessage;
  
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
   * <pre>
   * code of the result for this subject
   * SUCCESS: means everything ok
   * SUBJECT_NOT_FOUND: cant find the subject
   * SUBJECT_DUPLICATE: found multiple subjects
   *  
   * </pre>
   * 
   * @param resultCode1
   *            the resultCode to set
   */
  public void assignResultCode(String resultCode1) {
    this.resultCode = resultCode1;
  }

  /**
   * error message if there is an error
   * 
   * @param errorMessage
   *            the errorMessage to set
   */
  public void setResultMessage(String errorMessage) {
    this.resultMessage = errorMessage;
  }

  /**
   * T or F as to whether it was a successful assignment
   * 
   * @param success1
   *            the success to set
   */
  public void assignSuccess(String success1) {
    this.success = success1;
  }

  /**
   * status code for http lite / rest .  not a getter so isnt in soap/lite response
   * @return the status code e.g. 200, if 500, then not initted
   */
  public int retrieveHttpStatusCode() {
    return this.httpStatusCode;
  }

  /**
   * status code for http lite / rest .  not a setter so isnt in soap/lite response
   * @param statusCode1 the status code e.g. 200, if 500, then not initted
   */
  public void assignHttpStatusCode(int statusCode1) {
    this.httpStatusCode = statusCode1;
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
