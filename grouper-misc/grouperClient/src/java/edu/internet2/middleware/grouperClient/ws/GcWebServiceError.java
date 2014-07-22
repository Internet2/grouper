/**
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
 */
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
