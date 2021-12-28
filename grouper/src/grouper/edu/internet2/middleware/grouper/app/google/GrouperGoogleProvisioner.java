package edu.internet2.middleware.grouper.app.google;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;


public class GrouperGoogleProvisioner extends GrouperProvisioner {

  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return GrouperGoogleTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfigurationBase> grouperProvisioningConfigurationClass() {
    return GrouperGoogleConfiguration.class;
  }

  @Override
  public void registerProvisioningBehaviors(GrouperProvisioningBehavior grouperProvisioningBehavior) {
    grouperProvisioningBehavior.setGrouperProvisioningBehaviorMembershipType(GrouperProvisioningBehaviorMembershipType.membershipObjects);
  }
  
  @Override
  protected Class<? extends GrouperProvisioningObjectMetadata> grouperProvisioningObjectMetadataClass() {
    return GoogleSyncObjectMetadata.class;
  }

}
