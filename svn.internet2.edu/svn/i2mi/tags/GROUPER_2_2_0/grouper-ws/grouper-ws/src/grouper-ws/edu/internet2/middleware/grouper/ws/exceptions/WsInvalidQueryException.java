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
/*
 * @author mchyzer $Id: WsInvalidQueryException.java,v 1.1 2008-03-24 20:19:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.exceptions;

/**
 * web service can throw this when invalid input.  will auto-set
 * the response code and message to the message of this exception (wont print stack)
 */
public class WsInvalidQueryException extends RuntimeException {

  /**
   * id
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public WsInvalidQueryException() {
    //empty constructor
  }

  /**
   * @param message
   */
  public WsInvalidQueryException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public WsInvalidQueryException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public WsInvalidQueryException(String message, Throwable cause) {
    super(message, cause);
  }

}
