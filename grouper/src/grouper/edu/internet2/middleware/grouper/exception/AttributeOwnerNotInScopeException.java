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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.exception;


/**
 * when there are scopes on an attribute def, then attribute def name cannot
 * be assigned to an owner which is not in scope
 */
@SuppressWarnings("serial")
public class AttributeOwnerNotInScopeException extends RuntimeException {

  /**
   * 
   */
  public AttributeOwnerNotInScopeException() {
  }

  /**
   * @param message
   */
  public AttributeOwnerNotInScopeException(String message) {
    super(message);

  }

  /**
   * @param cause
   */
  public AttributeOwnerNotInScopeException(Throwable cause) {
    super(cause);

  }

  /**
   * @param message
   * @param cause
   */
  public AttributeOwnerNotInScopeException(String message, Throwable cause) {
    super(message, cause);

  }

}
