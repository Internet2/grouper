package edu.internet2.middleware.grouper.permissions.limits.impl;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBase;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;
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
   * @see PermissionLimitInterface#documentationKey()
   */
  public String documentationKey() {
    return "grouperPermissionAmountLessThan.doc";
  }

  /**
   * @see PermissionLimitInterface#validateLimitAssignValue(AttributeAssign, Set)
   */
  public String validateLimitAssignValue(AttributeAssign limitAssign, Set<AttributeAssignValue> limitAssignmentValues) {
    
    //lets see what values we have...
    Long value = null;
    
    if (GrouperUtil.length(limitAssignmentValues) == 1) {
      value = limitAssignmentValues.iterator().next().getValueInteger();
    }
    
    if (value == null) {
      return "grouperPermissionAmountLessThan.required";
    }
    
    return null;
  
  }

}
