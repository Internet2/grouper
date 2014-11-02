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

package edu.internet2.middleware.grouperVoot.messages;

/**
 * Class representing an error in the VOOT call.
 * 
 * @author mchyzer
 * @author Andrea Biancini <andrea.biancini@gmail.com>
 */
public class VootErrorResponse {
  
  /** Error property */
  private String error;
  /** Error description property */
  private String error_description;
  
  /**
   * Constructor with only short error name.
   * @param error the error name.
   */
  public VootErrorResponse(String error) {
    this.error = error;
  }
  
  /**
   * Constructor with short error name and its description.
   * @param error the error name.
   * @param error_description the description of the error occurred.
   */
  public VootErrorResponse(String error, String error_description) {
    this.error = error;
    this.error_description = error_description;
  }
  
  /**
   * Return the error message.
   * @return the error message.
   */
  public String getError() {
    return error;
  }
  
  /**
   * Set the error message.
   * @param error the error message.
   */
  public void setError(String error) {
    this.error = error;
  }
  
  /**
   * Return the error description.
   * @return the error description.
   */
  public String getError_description() {
    return error_description;
  }
  
  /**
   * Set the error description.
   * @param error_description the error description.
   */
  public void setError_description(String error_description) {
    this.error_description = error_description;
  }
}
