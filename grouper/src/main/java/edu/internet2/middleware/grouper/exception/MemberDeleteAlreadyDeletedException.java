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
 * 
 */
package edu.internet2.middleware.grouper.exception;

/**
 * when a member is already deleted
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class MemberDeleteAlreadyDeletedException extends MemberDeleteException {

  /**
   * 
   */
  public MemberDeleteAlreadyDeletedException() {
  }

  /**
   * @param msg
   */
  public MemberDeleteAlreadyDeletedException(String msg) {
    super(msg);
  }

  /**
   * @param msg
   * @param cause
   */
  public MemberDeleteAlreadyDeletedException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * @param cause
   */
  public MemberDeleteAlreadyDeletedException(Throwable cause) {
    super(cause);
  }

}
