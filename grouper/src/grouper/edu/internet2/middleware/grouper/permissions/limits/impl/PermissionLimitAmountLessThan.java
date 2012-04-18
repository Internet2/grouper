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
package edu.internet2.middleware.grouper.permissions.limits.impl;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBase;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitDocumentation;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitInterface;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * logic for the built in amount less than some number
 * @author mchyzer
 */
public class PermissionLimitAmountLessThan extends PermissionLimitBase {

  /**
   * @see PermissionLimitInterface#allowPermission(PermissionEntry, AttributeAssign, Set, Map, Set)
   */
  public boolean allowPermission(PermissionEntry permissionEntry,
      AttributeAssign limitAssignment, Set<AttributeAssignValue> limitAssignmentValues,
      Map<String, Object> limitEnvVars, Set<PermissionLimitBean> permissionLimitBeans) {
    
    return amountLimitHelper(limitAssignment, limitAssignmentValues, limitEnvVars, false);
    
  }

  /**
   * @param limitAssignment
   * @param limitAssignmentValues
   * @param limitEnvVars
   * @param allowEquals true for <=, false for <
   * @return true if ok, false if not allowed
   */
  public static boolean amountLimitHelper(AttributeAssign limitAssignment,
      Set<AttributeAssignValue> limitAssignmentValues, Map<String, Object> limitEnvVars, boolean allowEquals) {
    Object amountObject = limitEnvVars.get("amount");
    
    String errorMessage = "You must pass in an env var with name 'amount' which is an integer";

    if (GrouperUtil.isBlank(amountObject)) {
      throw new RuntimeException(errorMessage);
    }
    long amount = -1;
    
    try {
      amount = GrouperUtil.longValue(amountObject);
    } catch(RuntimeException e) {
      throw new RuntimeException(errorMessage + ", received: '" + amountObject + "'");
    }
    
    //get what the limit is
    Long theLimit = null;
    if (GrouperUtil.length(limitAssignmentValues) == 1) {
      theLimit = limitAssignmentValues.iterator().next().getValueInteger();
    }

    if (theLimit == null) {
      throw new RuntimeException("There is an amount limit assigned (assignment id: " + limitAssignment.getId() 
          + ") which does not have a value associated with it, it must have a value!"); 
    }
    
    return allowEquals ? amount <= theLimit : amount < theLimit;
  }

  /**
   * @see PermissionLimitInterface#documentation()
   */
  public PermissionLimitDocumentation documentation() {
    PermissionLimitDocumentation permissionLimitDocumentation = new PermissionLimitDocumentation();
    permissionLimitDocumentation.setDocumentationKey("grouperPermissionAmountLessThan.doc");
    return permissionLimitDocumentation;
  }

  /**
   * @see PermissionLimitInterface#validateLimitAssignValue(AttributeAssign, Set)
   */
  public PermissionLimitDocumentation validateLimitAssignValue(AttributeAssign limitAssign, Set<AttributeAssignValue> limitAssignmentValues) {
    
    //lets see what values we have...
    Long value = null;
    
    if (GrouperUtil.length(limitAssignmentValues) == 1) {
      value = limitAssignmentValues.iterator().next().getValueInteger();
    }
    
    if (value == null) {
      return new PermissionLimitDocumentation("grouperPermissionAmountLessThan.required");
    }
    
    return null;
  
  }

}
