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
package edu.internet2.middleware.grouper.permissions.limits;

import java.io.Serializable;
import java.util.List;

/**
 * key to the nav.properties, and values for args
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class PermissionLimitDocumentation implements Serializable {

  /**
   * 
   */
  public PermissionLimitDocumentation() {    
  }

  /**
   * construct with key
   * @param theDocumentationKey
   */
  public PermissionLimitDocumentation(String theDocumentationKey) {
    this.documentationKey = theDocumentationKey;
  }
  
  /** documentation key in nav.properties */
  private String documentationKey;
  
  /** args for {0}, {1}, etc in the documentation value */
  private List<String> args;

  /**
   * documentation key in nav.properties
   * @return documentation key
   */
  public String getDocumentationKey() {
    return this.documentationKey;
  }

  /**
   * documentation key in nav.properties
   * @param documentationKey1
   */
  public void setDocumentationKey(String documentationKey1) {
    this.documentationKey = documentationKey1;
  }

  /**
   * args for {0}, {1}, etc in the documentation value
   * @return args for {0}, {1}, etc in the documentation value
   */
  public List<String> getArgs() {
    return this.args;
  }

  /**
   * args for {0}, {1}, etc in the documentation value
   * @param args1
   */
  public void setArgs(List<String> args1) {
    this.args = args1;
  }
  
  
  
}
