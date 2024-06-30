package edu.internet2.middleware.grouper.app.google;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningValidationIssue;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;

public class GoogleProvisioningConfigurationValidation extends GrouperProvisioningConfigurationValidation {

  @Override
  public void validateFromObjectModel() {
    
    super.validateFromObjectModel();
    
    GrouperProvisioner grouperProvisioner = this.getGrouperProvisioner();
    GrouperGoogleConfiguration googleConfiguration = (GrouperGoogleConfiguration)grouperProvisioner.retrieveGrouperProvisioningConfiguration();
    
    boolean managersConfigured = googleConfiguration.getTargetGroupAttributeNameToConfig().containsKey("managers");
    boolean ownersConfigured = googleConfiguration.getTargetGroupAttributeNameToConfig().containsKey("owners");
    
    if (managersConfigured || ownersConfigured) {
      boolean emailFound = false;
      emailFound = googleConfiguration.getEmailCacheBucket() != null;
      if (!emailFound) {
        this.addErrorMessage(new ProvisioningValidationIssue()
            .assignMessage(GrouperTextContainer.textOrNull("googleConfigurationMustHaveEmailAsCache"))
            .assignJqueryHandle("entityAttributeValueCacheHas"));
      }
      
      boolean idFound = false;
      idFound = googleConfiguration.getEntityIdCacheBucket() != null;
      if (!idFound) {
        this.addErrorMessage(new ProvisioningValidationIssue()
            .assignMessage(GrouperTextContainer.textOrNull("googleConfigurationMustHaveIdAsCache"))
            .assignJqueryHandle("entityAttributeValueCacheHas"));
      }
    }
    
  }
  
  

}
