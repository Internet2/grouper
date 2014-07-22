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
 * $Id: GrouperStaleObjectStateException.java,v 1.1 2009-03-24 17:12:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.exception;


/**
 * grouper specific way to say that someone else has edited this object,
 * user should refresh object state and make changes again
 */
@SuppressWarnings("serial")
public class GrouperStaleObjectStateException extends RuntimeException {

  /**
   * 
   */
  public GrouperStaleObjectStateException() {
  }

  /**
   * @param message
   */
  public GrouperStaleObjectStateException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public GrouperStaleObjectStateException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public GrouperStaleObjectStateException(String message, Throwable cause) {
    super(message, cause);
  }

}
