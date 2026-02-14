package edu.internet2.middleware.grouper.app.freshServiceRequester;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

public class FreshRequesterProvisioner extends GrouperProvisioner {

  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return FreshRequesterTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    return FreshRequesterConfiguration.class;
  }

}
