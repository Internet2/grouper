package edu.internet2.middleware.grouper.app.scim2Provisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLoader;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

public class GrouperScim2Provisioner extends GrouperProvisioner {
  
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return GrouperScim2TargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    return GrouperScim2ProvisionerConfiguration.class;
  }

  @Override
  public void registerProvisioningBehaviors(GrouperProvisioningBehavior grouperProvisioningBehavior) {
    grouperProvisioningBehavior.setGrouperProvisioningBehaviorMembershipType(GrouperProvisioningBehaviorMembershipType.membershipObjects);
  }
  
  @Override
  protected Class<? extends GrouperProvisioningConfigurationValidation> grouperProvisioningConfigurationValidationClass() {
    return Scim2SyncConfigurationValidation.class;
  }
  

  @Override
  protected Class<? extends GrouperProvisioningLoader> grouperProvisioningLoaderClass() {
    return ScimProvisioningLoader.class;
  }

}
