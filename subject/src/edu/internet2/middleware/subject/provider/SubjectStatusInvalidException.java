/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.subject.provider;

/**
 * exception thrown when the status queried by user is not in the statusesFromUser list
 * 
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class SubjectStatusInvalidException extends RuntimeException {

  /**
   * 
   */
  public SubjectStatusInvalidException() {
    super();
  }

  /**
   * 
   * @param message
   * @param cause
   */
  public SubjectStatusInvalidException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * 
   * @param message
   */
  public SubjectStatusInvalidException(String message) {
    super(message);
  }

  /**
   * 
   * @param cause
   */
  public SubjectStatusInvalidException(Throwable cause) {
    super(cause);
  }

  
  
}
