package edu.internet2.middleware.grouper.app.freshServiceAgent;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeSync;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

public class FreshAgentProvisioner extends GrouperProvisioner {

  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return FreshAgentTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    return FreshAgentConfiguration.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningTargetNativeSync> grouperProvisioningTargetNativeSyncClass() {
    return FreshAgentProvisioningTargetNativeSync.class;
  }

  /**
   * group type metadata (agent group or role) if provisionRoles is on
   */
  @Override
  protected Class<? extends GrouperProvisioningObjectMetadata> grouperProvisioningObjectMetadataClass() {
    return FreshAgentSyncObjectMetadata.class;
  }

}
