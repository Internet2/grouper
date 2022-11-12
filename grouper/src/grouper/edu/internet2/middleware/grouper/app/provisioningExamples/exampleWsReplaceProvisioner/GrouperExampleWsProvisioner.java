package edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisioner;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

public class GrouperExampleWsProvisioner extends GrouperProvisioner {

  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return GrouperExampleWsTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    return GrouperExampleWsConfiguration.class;
  }

}
