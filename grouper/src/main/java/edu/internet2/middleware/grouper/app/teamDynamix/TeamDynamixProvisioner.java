package edu.internet2.middleware.grouper.app.teamDynamix;

import edu.internet2.middleware.grouper.app.azure.AzureProvisioningTranslator;
import edu.internet2.middleware.grouper.app.azure.AzureSyncObjectMetadata;
import edu.internet2.middleware.grouper.app.azure.GrouperAzureConfiguration;
import edu.internet2.middleware.grouper.app.azure.GrouperAzureTargetDao;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

public class TeamDynamixProvisioner extends GrouperProvisioner {
  
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return TeamDynamixTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
//    return TeamDynamixConfiguration.class;
    return null;
  }

  @Override
  public void registerProvisioningBehaviors(GrouperProvisioningBehavior grouperProvisioningBehavior) {
    grouperProvisioningBehavior.setGrouperProvisioningBehaviorMembershipType(GrouperProvisioningBehaviorMembershipType.membershipObjects);
  }
  
  @Override
  protected Class<? extends GrouperProvisioningObjectMetadata> grouperProvisioningObjectMetadataClass() {
    return AzureSyncObjectMetadata.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningTranslator> grouperTranslatorClass() {
    return AzureProvisioningTranslator.class;
  }

}
