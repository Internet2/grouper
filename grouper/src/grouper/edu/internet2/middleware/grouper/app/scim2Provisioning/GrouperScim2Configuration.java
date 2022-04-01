package edu.internet2.middleware.grouper.app.scim2Provisioning;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperScim2Configuration extends ProvisioningConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "provisioner." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(provisioner)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return GrouperScim2Provisioner.class.getName();
  }

  private void assignCacheConfig() {
    
  }
  
  @Override
  public void insertConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay, List<String> actionsPerformed) {
    assignCacheConfig();
    super.insertConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
  }

  @Override
  public void editConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay, List<String> actionsPerformed) {
    assignCacheConfig();
    super.editConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
  }
  
  

}
