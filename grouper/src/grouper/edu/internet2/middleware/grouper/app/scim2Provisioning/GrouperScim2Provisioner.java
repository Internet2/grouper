package edu.internet2.middleware.grouper.app.scim2Provisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

public class GrouperScim2Provisioner extends GrouperProvisioner {
  
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return GrouperScim2TargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfigurationBase> grouperProvisioningConfigurationClass() {
    return GrouperScim2ProvisionerConfiguration.class;
  }

  @Override
  public void registerProvisioningBehaviors(GrouperProvisioningBehavior grouperProvisioningBehavior) {
    grouperProvisioningBehavior.setGrouperProvisioningBehaviorMembershipType(GrouperProvisioningBehaviorMembershipType.membershipObjects);
  }

}
