package edu.internet2.middleware.grouper.app.okta;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;


public class GrouperOktaProvisioner extends GrouperProvisioner {

  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return GrouperOktaTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    return GrouperOktaConfiguration.class;
  }

  @Override
  public void registerProvisioningBehaviors(GrouperProvisioningBehavior grouperProvisioningBehavior) {
    grouperProvisioningBehavior.setGrouperProvisioningBehaviorMembershipType(GrouperProvisioningBehaviorMembershipType.membershipObjects);
  }
  
  @Override
  protected Class<? extends GrouperProvisioningObjectMetadata> grouperProvisioningObjectMetadataClass() {
    return OktaSyncObjectMetadata.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfigurationValidation> grouperProvisioningConfigurationValidationClass() {
    return GrouperProvisioningConfigurationValidation.class;
  }

  
  
  

}
