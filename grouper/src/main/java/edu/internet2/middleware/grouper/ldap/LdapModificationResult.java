/**
 * Copyright 2020 Internet2
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
package edu.internet2.middleware.grouper.ldap;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mchyzer
 */
public class LdapModificationResult {


  /**
   * true if full success
   */
  private boolean success;
  
  /**
   * true if full success
   * @return if success
   */
  public boolean isSuccess() {
    return this.success;
  }

  /**
   * true if full success
   * @param success1
   */
  public void setSuccess(boolean success1) {
    this.success = success1;
  }

  /**
   * attribute errors
   */
  private List<LdapModificationAttributeError> attributeErrors;


  /**
   * attribute errors
   * @return list of attribute errors
   */
  public List<LdapModificationAttributeError> getAttributeErrors() {
    return this.attributeErrors;
  }

  /**
   * attribute errors
   * @param attributeErrors1
   */
  public void setAttributeErrors(List<LdapModificationAttributeError> attributeErrors1) {
    this.attributeErrors = attributeErrors1;
  }
  
  /**
   * @param attributeError
   */
  public void addAttributeError(LdapModificationAttributeError attributeError) {
    if (this.attributeErrors == null) {
      this.attributeErrors = new ArrayList<LdapModificationAttributeError>();
    }
    
    this.attributeErrors.add(attributeError);
  }
}
