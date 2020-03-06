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
 * @author shilen
 */
public class LdapModificationItem {
  
  private LdapModificationType ldapModificationType;
  private LdapAttribute attribute;

  /**
   * @param ldapModificationType
   * @param attribute
   */
  public LdapModificationItem(LdapModificationType ldapModificationType, LdapAttribute attribute) {
    this.ldapModificationType = ldapModificationType;
    this.attribute = attribute;
  }

  /**
   * @return modification type
   */
  public LdapModificationType getLdapModificationType() {
    return ldapModificationType;
  }

  /**
   * @param ldapModificationType
   */
  public void setLdapModificationType(LdapModificationType ldapModificationType) {
    this.ldapModificationType = ldapModificationType;
  }

  /**
   * @return attribute
   */
  public LdapAttribute getAttribute() {
    return attribute;
  }

  /**
   * @param attribute
   */
  public void setAttribute(LdapAttribute attribute) {
    this.attribute = attribute;
  }
}
