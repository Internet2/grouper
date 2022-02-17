package edu.internet2.middleware.grouper.app.remedy;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

public class GrouperRemedyProvisioner extends GrouperProvisioner {
  
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return GrouperRemedyTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfigurationBase> grouperProvisioningConfigurationClass() {
    return GrouperRemedyConfiguration.class;
  }

  @Override
  public void registerProvisioningBehaviors(GrouperProvisioningBehavior grouperProvisioningBehavior) {
    grouperProvisioningBehavior.setGrouperProvisioningBehaviorMembershipType(GrouperProvisioningBehaviorMembershipType.membershipObjects);
  }
  
}
