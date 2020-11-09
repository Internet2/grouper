package edu.internet2.middleware.grouperBox;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

public class GrouperBoxProvisioner extends GrouperProvisioner {

  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return GrouperBoxTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfigurationBase> grouperProvisioningConfigurationClass() {
    return GrouperBoxConfiguration.class;
  }

  @Override
  public void registerProvisioningBehaviors(GrouperProvisioningBehavior grouperProvisioningBehavior) {
    grouperProvisioningBehavior.setGrouperProvisioningBehaviorMembershipType(GrouperProvisioningBehaviorMembershipType.membershipObjects);
  }

}
