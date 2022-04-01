package edu.internet2.middleware.grouper.app.google;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;


public class GoogleGrouperExternalSystem extends GrouperExternalSystem {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouper.googleConnector." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouper\\.googleConnector)\\.([^.]+)\\.(.*)$";
  }

  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "myGoogle";
  }

  @Override
  public void validatePreSave(boolean isInsert, boolean fromUi,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, fromUi, errorsToDisplay, validationErrorsToDisplay);
    
    GrouperConfigurationModuleAttribute serviceAccountPKCS12FilePath = this.retrieveAttributes().get("serviceAccountPKCS12FilePath");
    GrouperConfigurationModuleAttribute serviceAccountPrivateKeyPEM = this.retrieveAttributes().get("serviceAccountPrivateKeyPEM");

    if (StringUtils.isBlank(serviceAccountPKCS12FilePath.getValueOrExpressionEvaluation()) && StringUtils.isBlank(serviceAccountPrivateKeyPEM.getValueOrExpressionEvaluation())) {
      validationErrorsToDisplay.put(serviceAccountPKCS12FilePath.getHtmlForElementIdHandle(), GrouperTextContainer.textOrNull("grouperConfigurationValidationGoogleFilePathOrPrivateKeyRequired"));
    }
    
  }
  
  

}
