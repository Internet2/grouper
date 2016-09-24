/*******************************************************************************
 * Copyright 2016 Internet2
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
 *******************************************************************************/
/*
 * @author mchyzer $Id: GrouperRestInvalidRequest.java,v 1.1 2008-03-25 05:15:11 mchyzer Exp $
 */
package edu.internet2.middleware.tierInstrumentationCollector.exceptions;

/**
 * exception when there is not a valid request from client
 * this must be called before any response is written
 */
public class TicRestInvalidRequest extends RuntimeException {

  /**
   * default id
   */
  private static final long serialVersionUID = 1L;

  /**
   * http response code for this invalid request
   */
  private String httpResponseCode;
  
  /**
   * tier response code for this invalid request
   */
  private String tierResultCode;
  
  
  
  /**
   * @param httpResponseCode the httpResponseCode to set
   */
  public void setHttpResponseCode(String httpResponseCode) {
    this.httpResponseCode = httpResponseCode;
  }


  
  /**
   * @param tierResultCode the tierResultCode to set
   */
  public void setTierResultCode(String tierResultCode) {
    this.tierResultCode = tierResultCode;
  }


  /**
   * http response code for this invalid request
   * @return the httpResponseCode
   */
  public String getHttpResponseCode() {
    return this.httpResponseCode;
  }

  
  /**
   * tier response code for this invalid request
   * @return the tierResponseCode
   */
  public String getTierResultCode() {
    return this.tierResultCode;
  }

  /**
   * @param theHttpResponseCode
   * @param theTierResponseCode
   */
  public TicRestInvalidRequest(String theHttpResponseCode, String theTierResponseCode) {
    //empty constructor
    this.httpResponseCode = theHttpResponseCode;
    this.tierResultCode = theTierResponseCode;
  }

  /**
   * @param theHttpResponseCode
   * @param theTierResponseCode
   * @param message
   */
  public TicRestInvalidRequest(String message, String theHttpResponseCode, String theTierResponseCode) {
    super(message);
    this.httpResponseCode = theHttpResponseCode;
    this.tierResultCode = theTierResponseCode;
  }

  /**
   * @param theHttpResponseCode
   * @param theTierResponseCode
   * @param cause
   */
  public TicRestInvalidRequest(Throwable cause, String theHttpResponseCode, String theTierResponseCode) {
    super(cause);
    this.httpResponseCode = theHttpResponseCode;
    this.tierResultCode = theTierResponseCode;
  }

  /**
   * @param theHttpResponseCode
   * @param theTierResponseCode
   * @param message
   * @param cause
   */
  public TicRestInvalidRequest(String message, Throwable cause, String theHttpResponseCode, String theTierResponseCode) {
    super(message, cause);
    this.httpResponseCode = theHttpResponseCode;
    this.tierResultCode = theTierResponseCode;
  }

}
