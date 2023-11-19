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
package edu.internet2.middleware.grouper.exception;

/**
 * Exception thrown when an stem set is not found within the Groups Registry.
 * <p/>
 * @author  shilen.
 * @version $Id$
 */
@SuppressWarnings("serial")
public class StemSetNotFoundException extends RuntimeException {
  
  /**
   * 
   */
  public StemSetNotFoundException() { 
    super(); 
  }
  
  /**
   * 
   * @param msg
   */
  public StemSetNotFoundException(String msg) { 
    super(msg); 
  }
  
  /**
   * 
   * @param msg
   * @param cause
   */
  public StemSetNotFoundException(String msg, Throwable cause) { 
    super(msg, cause); 
  }
  
  /**
   * 
   * @param cause
   */
  public StemSetNotFoundException(Throwable cause) { 
    super(cause); 
  }
}
