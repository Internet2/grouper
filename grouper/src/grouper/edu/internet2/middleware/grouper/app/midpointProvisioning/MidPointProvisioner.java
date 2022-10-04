package edu.internet2.middleware.grouper.app.midpointProvisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioningDao;

public class MidPointProvisioner extends GrouperProvisioner {
  
  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    if (!this.retrieveGrouperProvisioningConfiguration().isConfigured()) {
      throw new RuntimeException("Why is provisioner not configured???");
    }
    return SqlProvisioningDao.class;
  }

  public MidPointProvisioningConfiguration retrieveMidPointProvisioningConfiguration() {
    return (MidPointProvisioningConfiguration)this.retrieveGrouperProvisioningConfiguration();
  }
  
  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    return MidPointProvisioningConfiguration.class;
  }

  @Override
  public void registerProvisioningBehaviors(
      GrouperProvisioningBehavior grouperProvisioningBehavior) {

  // TODO
  //  this.retrieveSqlProvisioningConfiguration().getSqlProvisioningType().registerProvisioningBehaviors(
  //      grouperProvisioningBehavior);
    
  }
  
  @Override
  protected Class<? extends GrouperProvisioningObjectMetadata> grouperProvisioningObjectMetadataClass() {
    return MidpointSyncObjectMetadata.class;
  }

}
