package edu.internet2.middleware.grouper.pspng;

/*******************************************************************************
 * Copyright 2015 Internet2
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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * This class represents an LdapUser as a TargetSystemUser. In other words,
 * this class is the adapter that enables Provisioners to keep track of LDAP Users.

 * @author bert
 *
 */
public class LdapUser implements TargetSystemUser {
  final LdapObject ldapObject;
  final String dn;
  
  
  public LdapUser(LdapObject ldapObject) {
    GrouperUtil.assertion(ldapObject != null, "Cannot create LdapUser without an ldap object");
    this.ldapObject = ldapObject;
    this.dn = ldapObject.getDn().toLowerCase();
  }
  
  public LdapObject getLdapObject() {
    return ldapObject;
  }
  
  @Override
  public Object getJexlMap() {
    return ldapObject.getMap();
  }

  @Override
  public String toString() {
    ToStringBuilder result = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    result.append("ldap", ldapObject);

    return result.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dn == null) ? 0 : dn.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LdapUser other = (LdapUser) obj;
    if (dn == null) {
      if (other.dn != null)
        return false;
    }
    else if (!dn.equals(other.dn))
      return false;
    return true;
  }
}
