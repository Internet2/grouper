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
/*
 * @author mchyzer
 * $Id: GrouperSessionException.java,v 1.1 2008-07-21 04:43:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.exception;


/**
 * Use this to tunnel exceptions through the GrouperSession
 * inverse of control
 */
@SuppressWarnings("serial")
public class GrouperSessionException extends RuntimeException {

  /**
   * @param cause
   */
  public GrouperSessionException(Throwable cause) {
    super(cause);
  }

}
