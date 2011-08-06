package edu.internet2.middleware.grouper.pit;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBase;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitDocumentation;

/**
 * test limit
 * @author mchyzer
 *
 */
public class PITPermissionLimit extends PermissionLimitBase {

  /**
   * 
   */
  public PITPermissionLimit() {
    
  }

  /**
   * @see PermissionLimitBase#allowPermission(PermissionEntry, AttributeAssign, Set, Map, Set)
   */
  public boolean allowPermission(PermissionEntry permissionEntry,
      AttributeAssign limitAssignment, Set<AttributeAssignValue> limitAssignmentValues,
      Map<String, Object> limitEnvVars, Set<PermissionLimitBean> permissionLimitBeans) {
    return true;
  }

  /**
   * @see PermissionLimitBase#documentation()
   */
  public PermissionLimitDocumentation documentation() {
    return null;
  }

  /**
   * @see PermissionLimitBase#validateLimitAssignValue(AttributeAssign, Set)
   */
  public PermissionLimitDocumentation validateLimitAssignValue(
      AttributeAssign limitAssign, Set<AttributeAssignValue> limitAssignmentValues) {
    return null;
  }

}
