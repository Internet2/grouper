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
/**
 * @author mchyzer
 * $Id: TooManyResultsWhenFilteringByGroupException.java,v 1.1 2009-12-28 06:08:42 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.subject;

import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;


/**
 * when too many results when searching for subjects and filtering by group
 */
@SuppressWarnings("serial")
public class TooManyResultsWhenFilteringByGroupException extends WsInvalidQueryException {

  /**
   * 
   */
  public TooManyResultsWhenFilteringByGroupException() {
    //nothing
  }

  /**
   * @param message
   */
  public TooManyResultsWhenFilteringByGroupException(String message) {
    super(message);

  }

  /**
   * @param cause
   */
  public TooManyResultsWhenFilteringByGroupException(Throwable cause) {
    super(cause);

  }

  /**
   * @param message
   * @param cause
   */
  public TooManyResultsWhenFilteringByGroupException(String message, Throwable cause) {
    super(message, cause);

  }

}
