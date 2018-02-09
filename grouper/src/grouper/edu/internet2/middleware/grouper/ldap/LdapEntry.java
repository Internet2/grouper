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
package edu.internet2.middleware.grouper.ldap;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author shilen
 */
public class LdapEntry {

  private String dn;
  
  private Map<String, LdapAttribute> attributes;

  /**
   * @param dn
   */
  public LdapEntry(String dn) {
    this.dn = dn;
    this.attributes = new LinkedHashMap<String, LdapAttribute>();
  }
  
  /**
   * @return the dn
   */
  public String getDn() {
    return dn;
  }

  
  /**
   * @param dn the dn to set
   */
  public void setDn(String dn) {
    this.dn = dn;
  }

  /**
   * @param attributeName
   * @return attribute
   */
  public LdapAttribute getAttribute(String attributeName) {
    return attributes.get(attributeName.toLowerCase());
  }
  
  /**
   * @param attribute
   */
  public void addAttribute(LdapAttribute attribute) {
    attributes.put(attribute.getName().toLowerCase(), attribute);
  }
  
  public String toString() {

    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("dn", this.dn);

    for (String attribute : this.attributes.keySet()) {
      builder.append(attribute, this.attributes.get(attribute).getStringValues());
    }
    
    return builder.toString();
  }
}
