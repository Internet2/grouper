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
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitInterface;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * logic for the built in ip address on networks
 * @author mchyzer
 */
public class PermissionLimitIpOnNetworkRealm extends PermissionLimitBase {

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
      throw new RuntimeException("There is an ip on network realm limit assigned (assignment id: " + limitAssignment.getId() 
          + ") which does not have a value associated with it, it must have a value which is the config name of the ip address realm!"); 
    }
    
    return LimitElUtils.ipOnNetworkRealm(ipAddress, theLimit);
    
  }

  /**
   * @see PermissionLimitInterface#documentationKey()
   */
  public String documentationKey() {
    return "grouperPermissionIpOnNetworks.doc";
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
    
    if (value == null) {
      return "grouperPermissionIpOnNetworkRealm.required";
    }
    
    //test one
    try {
      LimitElUtils.ipOnNetworkRealm("1.2.3.4", value);
      //we are ok
    } catch (Exception e) {
      return "grouperPermissionInvalidIpNetworkRealm";
    }
    
    return null;
  
  }

}
