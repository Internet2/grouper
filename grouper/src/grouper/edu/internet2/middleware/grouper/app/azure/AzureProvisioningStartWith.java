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
import edu.internet2.middleware.grouper.util.GrouperUtil;
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
    
    provisionerSuffixToValue.put("azureExternalSystemConfigId", startWithSuffixToValue.get("azureExternalSystemConfigId"));
    
    if ( StringUtils.isNotBlank(startWithSuffixToValue.get("userAttributesType")) && !StringUtils.equals(startWithSuffixToValue.get("userAttributesType"), "core")) {
      provisionerSuffixToValue.put("entityResolver.entityAttributesNotInSubjectSource", "true");
    }
    
    {
      int numberOfGroupAttributes = 0;
      
      provisionerSuffixToValue.put("operateOnGrouperGroups", "true");
      provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".name", startWithSuffixToValue.get("groupDisplayNameAttributeValue"));
      numberOfGroupAttributes++;
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("useGroupDescription"), true)) {
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".name", "description");
        numberOfGroupAttributes++;
      }
      
      provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".name", startWithSuffixToValue.get("mailNicknameAttributeValue"));
      numberOfGroupAttributes++;
      
      provisionerSuffixToValue.put("numberOfGroupAttributes", numberOfGroupAttributes);
    }
    
    {
      int numberOfMetadataItems = 0;
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForGroupType"), true)) {
        numberOfMetadataItems++;
        provisionerSuffixToValue.put("metadata.0.name", "md_groupType");
        provisionerSuffixToValue.put("metadata.0.showForGroup", "true");
      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForAllowOnlyMembersToPost"), true)) {
        numberOfMetadataItems++;
        provisionerSuffixToValue.put("metadata.1.name", "md_allowOnlyMembersToPost");
        provisionerSuffixToValue.put("metadata.1.showForGroup", "true");
      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForHideGroupInOutlook"), true)) {
        numberOfMetadataItems++;
        provisionerSuffixToValue.put("metadata.2.name", "md_hideGroupInOutlook");
        provisionerSuffixToValue.put("metadata.2.showForGroup", "true");
      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForSubscribeNewGroupMembers"), true)) {
        numberOfMetadataItems++;
        provisionerSuffixToValue.put("metadata.3.name", "md_subscribeNewGroupMembers");
        provisionerSuffixToValue.put("metadata.3.showForGroup", "true");
      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForWelcomeEmailDisabled"), true)) {
        numberOfMetadataItems++;
        provisionerSuffixToValue.put("metadata.4.name", "md_welcomeEmailDisabled");
        provisionerSuffixToValue.put("metadata.4.showForGroup", "true");
      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForResourceProvisioningOptionsTeams"), true)) {
        numberOfMetadataItems++;
        provisionerSuffixToValue.put("metadata.5.name", "md_resourceProvisioningOptionsTeams");
        provisionerSuffixToValue.put("metadata.5.showForGroup", "true");
      }
      
      if (numberOfMetadataItems > 0) {
        provisionerSuffixToValue.put("configureMetadata", "true");
      }
    }
    
    {
      int numberOfEntityAttributes = 0;
      
      provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
      provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".name", startWithSuffixToValue.get("entityUserPrincipalName"));
      numberOfEntityAttributes++;
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("useGroupDescription"), true)) {
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfEntityAttributes+".name", "description");
        numberOfEntityAttributes++;
      }
      
      provisionerSuffixToValue.put("targetGroupAttribute."+numberOfEntityAttributes+".name", startWithSuffixToValue.get("mailNicknameAttributeValue"));
      numberOfEntityAttributes++;
      
      provisionerSuffixToValue.put("numberOfGroupAttributes", numberOfEntityAttributes);
    }
    
    
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
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "other")) {
          result.clear();
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
