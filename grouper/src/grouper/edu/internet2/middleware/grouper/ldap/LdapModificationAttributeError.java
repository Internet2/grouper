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

  private LdapModificationItem ldapModificationItem;

  /**
   * @return ldap modification item that had error
   */
  public LdapModificationItem getLdapModificationItem() {
    return ldapModificationItem;
  }

  /**
   * @param ldapModificationItem
   */
  public void setLdapModificationItem(LdapModificationItem ldapModificationItem) {
    this.ldapModificationItem = ldapModificationItem;
  }

  /**
   * error if applicable
   * @return error
   */
  public Exception getError() {
    return this.error;
  }

  /**
   * error if applicable
   * @param error1
   */
  public void setError(Exception error1) {
    this.error = error1;
  }
  
  /**
   * exception
   */
  private Exception error;
  
}
