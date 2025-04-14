package edu.internet2.middleware.grouper.app.gshTemplateProvisioner;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;

public abstract class GshTemplateProvisionerBase extends GrouperProvisioner {

  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    return GshTemplateProvisioningConfiguration.class;
  }

}
