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
 * $Id: InvalidQueryRuntimeException.java,v 1.1 2008-08-18 06:15:58 mchyzer Exp $
 */
package edu.internet2.middleware.subject.provider;


/**
 * invalid query runtime
 */
public class InvalidQueryRuntimeException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public InvalidQueryRuntimeException() {
    super();
  }

  /**
   * @param message
   * @param cause
   */
  public InvalidQueryRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   */
  public InvalidQueryRuntimeException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public InvalidQueryRuntimeException(Throwable cause) {
    super(cause);
  }

}
