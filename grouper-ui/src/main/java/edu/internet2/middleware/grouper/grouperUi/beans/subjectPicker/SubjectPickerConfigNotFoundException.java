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
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.subjectPicker;


/**
 * thrown when a config value is not found
 */
public class SubjectPickerConfigNotFoundException extends RuntimeException {

  /**
   * 
   */
  public SubjectPickerConfigNotFoundException() {
  }

  /**
   * @param message
   */
  public SubjectPickerConfigNotFoundException(String message) {
    super(message);

  }

  /**
   * @param cause
   */
  public SubjectPickerConfigNotFoundException(Throwable cause) {
    super(cause);

  }

  /**
   * @param message
   * @param cause
   */
  public SubjectPickerConfigNotFoundException(String message, Throwable cause) {
    super(message, cause);

  }

}
