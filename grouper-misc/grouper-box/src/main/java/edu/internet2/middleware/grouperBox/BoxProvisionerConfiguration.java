package edu.internet2.middleware.grouperBox;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisionerConfiguration;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class BoxProvisionerConfiguration extends ProvisionerConfiguration {

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
    return GrouperBoxProvisioner.class.getName();
  }

  private void assignCacheConfig() {
    
  }
  
  @Override
  public void insertConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    assignCacheConfig();
    super.insertConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay);
  }

  @Override
  public void editConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    assignCacheConfig();
    super.editConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay);
  }
  
  

}
