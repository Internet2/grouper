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
package edu.internet2.middleware.grouperClient.util;


/**
 * @author mchyzer This class is thrown there is a missing variable in EL
 * @version $Id: NotConcurrentRevisionException.java,v 1.1 2004/05/02 05:14:59
 *          mchyzer Exp $
 */
@SuppressWarnings("serial")
public class GcExpressionLanguageMissingVariableException extends RuntimeException {

  /**
   *  
   */
  public GcExpressionLanguageMissingVariableException() {
    super();
  }

  /**
   * @param s
   */
  public GcExpressionLanguageMissingVariableException(String s) {
    super(s);
  }

  /**
   * @param message
   * @param cause
   */
  public GcExpressionLanguageMissingVariableException(String message, Throwable cause) {
    super(message, cause);
    
  }

  /**
   * @param cause
   */
  public GcExpressionLanguageMissingVariableException(Throwable cause) {
    super(cause);
    
  }
}
