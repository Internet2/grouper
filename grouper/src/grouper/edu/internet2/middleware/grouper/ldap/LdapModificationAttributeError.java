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


/**
 * error on attribute modification
 * @author mchyzer
 */
public class LdapModificationAttributeError {

  /**
   * attribute with error if applicable
   */
  private LdapAttribute ldapAttribute;
  
  /**
   * modification type that had issue
   */
  private LdapModificationType ldapModificationType;
  
  /**
   * @return attribute with error if applicable
   */
  public LdapAttribute getLdapAttribute() {
    return ldapAttribute;
  }

  /**
   * @param ldapAttribute attribute with error if applicable
   */
  public void setLdapAttribute(LdapAttribute ldapAttribute) {
    this.ldapAttribute = ldapAttribute;
  }

  /**
   * @return modification type that had issue
   */
  public LdapModificationType getLdapModificationType() {
    return ldapModificationType;
  }

  /**
   * @param ldapModificationType modification type that had issue
   */
  public void setLdapModificationType(LdapModificationType ldapModificationType) {
    this.ldapModificationType = ldapModificationType;
  }

  /**
   * error code if applicable
   * @return error code
   */
  public String getErrorCode() {
    return this.errorCode;
  }

  /**
   * error code if applicable
   * @param errorCode1
   */
  public void setErrorCode(String errorCode1) {
    this.errorCode = errorCode1;
  }

  /**
   * error if applicable
   * @return error
   */
  public Throwable getError() {
    return this.error;
  }

  /**
   * error if applicable
   * @param error1
   */
  public void setError(Throwable error1) {
    this.error = error1;
  }

  /**
   * error code if applicable
   */
  private String errorCode;
  
  /**
   * exception
   */
  private Throwable error;
  
}
