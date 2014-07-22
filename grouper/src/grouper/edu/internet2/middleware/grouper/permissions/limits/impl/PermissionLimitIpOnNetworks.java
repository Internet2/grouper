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
package edu.internet2.middleware.grouper.permissions.limits.impl;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.limits.LimitElUtils;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBase;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitDocumentation;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitInterface;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * logic for the built in ip address on networks
 * @author mchyzer
 */
public class PermissionLimitIpOnNetworks extends PermissionLimitBase {

  /**
   * @see PermissionLimitInterface#allowPermission(PermissionEntry, AttributeAssign, Set, Map, Set)
   */
  public boolean allowPermission(PermissionEntry permissionEntry,
      AttributeAssign limitAssignment, Set<AttributeAssignValue> limitAssignmentValues,
      Map<String, Object> limitEnvVars, Set<PermissionLimitBean> permissionLimitBeans) {
    
    String ipAddress = (String)limitEnvVars.get("ipAddress");
    String errorMessage = "You must pass in an env var with name 'ipAddress' which is a string which looks like: 1.2.3.4";

    if (GrouperUtil.isBlank(ipAddress)) {
      throw new RuntimeException(errorMessage);
    }
    
    //get what the limit is
    String theLimit = null;
    if (GrouperUtil.length(limitAssignmentValues) == 1) {
      theLimit = limitAssignmentValues.iterator().next().getValueString();
    }

    if (StringUtils.isBlank(theLimit)) {
      throw new RuntimeException("There is an ip on networks limit assigned (assignment id: " + limitAssignment.getId() 
          + ") which does not have a value associated with it, it must have a value which are the networks where the ip address is allowed!"); 
    }
    
    return LimitElUtils.ipOnNetworks(ipAddress, theLimit);
    
  }

  /**
   * @see PermissionLimitInterface#documentation()
   */
  public PermissionLimitDocumentation documentation() {
    PermissionLimitDocumentation permissionLimitDocumentation = new PermissionLimitDocumentation();
    permissionLimitDocumentation.setDocumentationKey("grouperPermissionIpOnNetworks.doc");
    return permissionLimitDocumentation;
  }

  /**
   * @see PermissionLimitInterface#validateLimitAssignValue(AttributeAssign, Set)
   */
  public PermissionLimitDocumentation validateLimitAssignValue(AttributeAssign limitAssign, Set<AttributeAssignValue> limitAssignmentValues) {
    
    //lets see what values we have...
    String value = null;
    
    if (GrouperUtil.length(limitAssignmentValues) == 1) {
      value = limitAssignmentValues.iterator().next().getValueString();
    }
    
    if (value == null) {
      return new PermissionLimitDocumentation("grouperPermissionIpOnNetworks.required");
    }
    
    //test one
    try {
      LimitElUtils.ipOnNetworks("1.2.3.4", value);
      //we are ok
    } catch (Exception e) {
      return new PermissionLimitDocumentation("grouperPermissionInvalidIpNetworks");
    }
    
    return null;
  
  }

}
