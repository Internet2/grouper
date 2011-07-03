package edu.internet2.middleware.grouper.permissions.limits;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;

/**
 * 
 * implement this interface to attach logic to a permission limit
 * 
 * @author mchyzer
 *
 */
public interface PermissionLimitInterface {

  /**
   * if the limit allowed the permission to be allowed
   * @param permissionEntry to check
   * @param limitAssignment the assignment of the limit (e.g. to the permission 
   * assignment a parent assignment, or the role, etc)
   * @param limitAssignmentValues 
   * @param limitEnvVars value could be String, Long, or Double
   * @param permissionLimitBeans all limits for this permission (in case the limit logic needs it... 
   * note, dont use built in caching if this is the case)
   * @return true if allowed, false if not
   */
  public boolean allowPermission(PermissionEntry permissionEntry, AttributeAssign limitAssignment, 
      Set<AttributeAssignValue> limitAssignmentValues, Map<String, Object> limitEnvVars, Set<PermissionLimitBean> permissionLimitBeans);

  /**
   * validate a user entered value(s) on the limit assignment
   * @param limitAssign
   * @param limitAssignmentValues 
   * @return the UI key for the error code (arbitrary, in Grouper should put in nav.properties)
   * or null for ok
   */
  public String validateLimitAssignValue(AttributeAssign limitAssign, Set<AttributeAssignValue> limitAssignmentValues);

  /**
   * return a UI key to documentation about the limit.  for Grouper, put in nav.properties
   * @return a UI key
   */
  public String documentationKey();

  /**
   * if we can cache the result for a some minutes.
   * i.e. for the same attribute assignment and value and input map, is the result the same...
   * e.g. ip address math can be cached, amount limits, etc.  If there are conditions about the 
   * permission names, then dont cache
   * @return the number of minutes to cache
   */
  public int cacheLimitValueResultMinutes();

}
