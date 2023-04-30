package edu.internet2.middleware.grouper.app.google;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;


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

  @Override
  public List<String> test() throws UnsupportedOperationException {
    
    List<String> errors = new ArrayList<>();
    String testFakeGroupId = "testFakeGroupId";
    // try to retrieve a fake group and if it's 200 or 404, it's all good
    try {
      GrouperGoogleGroup googleGroup = GrouperGoogleApiCommands.retrieveGoogleGroup(this.getConfigId(), testFakeGroupId);
    } catch (Exception e) {
      errors.add("Could not connect with google external system successfully "+GrouperUtil.escapeHtml(e.getMessage(), true));
    }
    
    return errors;
  }
  
  
  
  

}
