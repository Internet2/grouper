/**
 * 
 */
package edu.internet2.middleware.grouper.app.azure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class AzureProvisioningStartWith extends ProvisionerStartWithBase {

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return "azureCommon";
  }
  
  @Override
  public void populateProvisionerConfigurationValuesFromStartWith(
      Map<String, String> startWithSuffixToValue,
      Map<String, Object> provisionerSuffixToValue) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Map<String, String> screenRedraw(Map<String, String> suffixToValue,
      Set<String> suffixesUserJustChanged) {
    
    Map<String, String> result = new HashMap<>();
    
    for (String suffixUserJustChanged: suffixesUserJustChanged) {
      
      if (StringUtils.equals(suffixUserJustChanged, "azurePattern")) {
        String valueUserEnteredOnScreen = suffixToValue.get(suffixUserJustChanged);
        if (StringUtils.equals(valueUserEnteredOnScreen, "manageGroupsManageEntities")) {
          result.put("manageEntitiesInAzure", "true");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "manageGroupsReadonlyEntities")) {
          result.put("manageEntitiesInAzure", "false");
        }
      }
    }
    
    return result;
  }

  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    
    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    } 
    
    GrouperConfigurationModuleAttribute onPremAttribute = this.retrieveAttributes().get("entityOnPremisesImmutableId");
    GrouperConfigurationModuleAttribute principalNameAttribute = this.retrieveAttributes().get("entityUserPrincipalName");
    GrouperConfigurationModuleAttribute mailNickNameAttribute = this.retrieveAttributes().get("entityMailNickname");
    
    if (onPremAttribute == null && principalNameAttribute == null && mailNickNameAttribute == null) {
      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithAzureConfigurationValidationEntityFieldNotSelected");
      errorsToDisplay.add(errorMessage);
      return;
    }
    
    
    boolean allBlank = true;
    
    if (onPremAttribute != null) {
      String value = onPremAttribute.getValueOrExpressionEvaluation();
      if (StringUtils.isNotBlank(value)) {
        allBlank = false;
      }
    }
    
    if (allBlank && principalNameAttribute != null) {
      String value = principalNameAttribute.getValueOrExpressionEvaluation();
      if (StringUtils.isNotBlank(value)) {
        allBlank = false;
      }
    }
    
    if (allBlank && mailNickNameAttribute != null) {
      String value = mailNickNameAttribute.getValueOrExpressionEvaluation();
      if (StringUtils.isNotBlank(value)) {
        allBlank = false;
      }
    }
    
    if (allBlank) {
      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithAzureConfigurationValidationEntityFieldNotSelected");
      errorsToDisplay.add(errorMessage);
    }
    
  }
  

}
