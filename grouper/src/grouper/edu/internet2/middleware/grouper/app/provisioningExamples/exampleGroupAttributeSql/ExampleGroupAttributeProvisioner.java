package edu.internet2.middleware.grouper.app.provisioningExamples.exampleGroupAttributeSql;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioningConfiguration;

public class ExampleGroupAttributeProvisioner extends GrouperProvisioner {

  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return ExampleGroupAttributeSqlDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    return SqlProvisioningConfiguration.class;
  }

}
