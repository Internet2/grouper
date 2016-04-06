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


/**
 * This class represents an LdapGroup as a TargetSystemGroup. In other words,
 * this class is the adapter that enables Provisioners to keep track of LDAP Groups.
 * 
 * @author bert
 *
 */
public class LdapGroup implements TargetSystemGroup {
  final LdapObject ldapObject;
  
  
  public LdapGroup(LdapObject ldapObject) {
    this.ldapObject = ldapObject;
  }
  
  public LdapObject getLdapObject() {
    return ldapObject;
  }
  
  public boolean wasFullMembershipFetched(LdapGroupProvisioner provisioner) {
    return ldapObject.attributesRequested.contains(provisioner.config.getMemberAttributeName());
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

}
