package edu.internet2.middleware.grouper.app.scim2Provisioning;

import org.apache.commons.codec.binary.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningCompare;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningUpdatable;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperScim2Compare extends GrouperProvisioningCompare {
  
  @Override
  public boolean attributeValueEquals(String attributeName, Object grouperValue,
      Object targetValue, ProvisioningUpdatable grouperTargetUpdatable) {
    
    boolean originalCheck = super.attributeValueEquals(attributeName, grouperValue, targetValue,
        grouperTargetUpdatable);
    
    //we're only affecting the comparison for active attribute
    if (!StringUtils.equals(attributeName, "active")) {
      return originalCheck;
    }
    
    // if we're deleting entities then just use the original values
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isDeleteEntities()) {
      return originalCheck;
    }
    
    // if we're not deleting entities and there's an active attribute then we're only enabling entities
    // disabling is managed when we're deleting entities and selecting the option of disabling instead of hard delete
    if (GrouperUtil.booleanValue(grouperValue, true)) {
      return originalCheck;
    }
    
    // since we're not disabling entities, don't compare
    return true;
  }

}
