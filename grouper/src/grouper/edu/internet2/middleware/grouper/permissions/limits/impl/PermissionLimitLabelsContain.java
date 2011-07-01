package edu.internet2.middleware.grouper.permissions.limits.impl;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.limits.LimitElUtils;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBase;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitInterface;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * logic for the built in to see if user has a label in a set of labels
 * @author mchyzer
 */
public class PermissionLimitLabelsContain extends PermissionLimitBase {

  /**
   * @see PermissionLimitInterface#allowPermission(PermissionEntry, AttributeAssign, Set, Map)
   */
  public boolean allowPermission(PermissionEntry permissionEntry,
      AttributeAssign limitAssignment, Set<AttributeAssignValue> limitAssignmentValues,
      Map<String, Object> limitEnvVars) {
    
    Object userLabels = limitEnvVars.get("labels");
    
    if (!limitEnvVars.containsKey("labels")) {
      throw new RuntimeException("You need to pass in the environment variable 'labels' even if the value is blank");
    }

    //get what the limit is
    String theLimit = null;
    if (GrouperUtil.length(limitAssignmentValues) == 1) {
      theLimit = limitAssignmentValues.iterator().next().getValueString();
    }

    if (StringUtils.isBlank(theLimit)) {
      throw new RuntimeException("There is a labelsContain limit assigned (assignment id: " + limitAssignment.getId() 
          + ") which does not have a value associated with it, it must have a value!"); 
    }
    
    return LimitElUtils.labelsContain((String)userLabels, theLimit);
    
  }

  /**
   * @see PermissionLimitInterface#documentationKey()
   */
  public String documentationKey() {
    return "grouperPermissionLabelsContain.doc";
  }

  /**
   * @see PermissionLimitInterface#validateLimitAssignValue(AttributeAssign, Set)
   */
  public String validateLimitAssignValue(AttributeAssign limitAssign, Set<AttributeAssignValue> limitAssignmentValues) {
    
    //lets see what values we have...
    String value = null;
    
    if (GrouperUtil.length(limitAssignmentValues) == 1) {
      value = limitAssignmentValues.iterator().next().getValueString();
    }
    
    if (StringUtils.isBlank(value)) {
      return "grouperPermissionLabelsContain.required";
    }
    
    return null;
  
  }

}
