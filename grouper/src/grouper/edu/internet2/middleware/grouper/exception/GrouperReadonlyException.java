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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.exception;


/**
 * if in readonly mode from grouper.properties
 */
@SuppressWarnings("serial")
public class GrouperReadonlyException extends RuntimeException {

  /**
   * 
   */
  public GrouperReadonlyException() {
  }

  /**
   * @param message
   */
  public GrouperReadonlyException(String message) {
    super(message);

  }

  /**
   * @param cause
   */
  public GrouperReadonlyException(Throwable cause) {
    super(cause);

  }

  /**
   * @param message
   * @param cause
   */
  public GrouperReadonlyException(String message, Throwable cause) {
    super(message, cause);

  }

}
