/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
package edu.internet2.middleware.authzStandardApiClient.exceptions;

/**
 * grouper client ws exception
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class StandardApiClientWsException extends RuntimeException {

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
  public StandardApiClientWsException(Object theResultObject) {
    this.resultObject = theResultObject;
  }

  /**
   * 
   * @param theResultObject 
   * @param message
   */
  public StandardApiClientWsException(Object theResultObject, String message) {
    super(message);
    this.resultObject = theResultObject;
  }

  /**
   * 
   * @param theResultObject 
   * @param cause
   */
  public StandardApiClientWsException(Object theResultObject, Throwable cause) {
    super(cause);
    this.resultObject = theResultObject;
  }

  /**
   * 
   * @param theResultObject 
   * @param message
   * @param cause
   */
  public StandardApiClientWsException(Object theResultObject, String message, Throwable cause) {
    super(message, cause);
    this.resultObject = theResultObject;
  }

}
