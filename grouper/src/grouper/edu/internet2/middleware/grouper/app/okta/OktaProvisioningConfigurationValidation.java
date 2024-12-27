package edu.internet2.middleware.grouper.app.okta;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;

public class OktaProvisioningConfigurationValidation extends GrouperProvisioningConfigurationValidation {

  @Override
  public void validateFromObjectModel() {
    
    super.validateFromObjectModel();
    
    GrouperProvisioner grouperProvisioner = this.getGrouperProvisioner();
    GrouperOktaConfiguration oktaConfiguration = (GrouperOktaConfiguration)grouperProvisioner.retrieveGrouperProvisioningConfiguration();
  }
  
  

}
