/**
 * 
 */
package edu.internet2.middleware.grouper.app.azure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;

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
    
    
    /**
     * 
     * ask for group matching/search attribute. dropdown - groupDisplayName or mailNickname
     * ask for entity matching/search attribute. dropdown - userPrincipalName, mailNickname, onPremisesImmutableid, displayname
     */
    
    provisionerSuffixToValue.put("azureExternalSystemConfigId", startWithSuffixToValue.get("azureExternalSystemConfigId"));
    
    if (StringUtils.equals(startWithSuffixToValue.get("userAttributesType"), "entityResolver") || StringUtils.equals(startWithSuffixToValue.get("userAttributesType"), "subjectSourceAndEntityResolver")) {
      provisionerSuffixToValue.put("entityResolver.entityAttributesNotInSubjectSource", "true");
    }
    
    if (StringUtils.equals(startWithSuffixToValue.get("userAttributesType"), "subjectSource") 
        || StringUtils.equals(startWithSuffixToValue.get("userAttributesType"), "subjectSourceAndEntityResolver")) {
      provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
      
      String attributesCommaSeparated = startWithSuffixToValue.get("subjectSourceEntityResolverAttributes");
      if (StringUtils.isNotBlank(attributesCommaSeparated)) {
        provisionerSuffixToValue.put("entityAttributeValueCacheHas", "true");
        String[] attributes = GrouperUtil.splitTrim(attributesCommaSeparated, ",");
        // by this time the validation is already done that there are no more than 2 attributes
        for (int i=0; i<attributes.length; i++) {
          int j = i+2;
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"has", "true");
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"source", "grouper");
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"type", "subjectTranslationScript");
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"translationScript", "${subject.getAttributeValue('"+attributes[i]+"')}");
        }
        
      }
      
    }
    
    
    {
      int numberOfGroupAttributes = 0;
      
      provisionerSuffixToValue.put("operateOnGrouperGroups", "true");
      
      provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".name", "id");
      provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".showAdvancedAttribute", "true");
      provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".showAttributeCrud", "true");
      provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".insert", "false");
      provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".update", "false");
      numberOfGroupAttributes++;
      
      String groupDisplayNameAttributeType = startWithSuffixToValue.get("groupDisplayNameAttributeValue");
      if (StringUtils.equals("script", groupDisplayNameAttributeType)) {
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".translateExpressionType", "translationScript");
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".translateExpression", startWithSuffixToValue.get("groupDisplayNameTranslationScript"));
      } else if (StringUtils.equals("other", groupDisplayNameAttributeType)) {
        //do nothing
      } else { 
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".translateExpressionType", "grouperProvisioningGroupField");
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".translateFromGrouperProvisioningGroupField", groupDisplayNameAttributeType);
      }
      
      provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".name", "displayName");
      numberOfGroupAttributes++;
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("useGroupDescription"), true)) {
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".name", "description");
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".translateExpressionType", "grouperProvisioningGroupField");
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".translateFromGrouperProvisioningGroupField", "description");
        numberOfGroupAttributes++;
      }
      
      String groupMailNicknameAttributeType = startWithSuffixToValue.get("mailNicknameAttributeValue");
      if (StringUtils.equals("script", groupMailNicknameAttributeType)) {
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".translateExpressionType", "translationScript");
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".translateExpression", startWithSuffixToValue.get("mailNicknameTranslationScript"));
      } else if (StringUtils.equals("other", groupMailNicknameAttributeType)) {
        //do nothing
      } else { 
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".translateExpressionType", "grouperProvisioningGroupField");
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".translateFromGrouperProvisioningGroupField", groupMailNicknameAttributeType);
      }
      
      provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".name", "mailNickname");
      numberOfGroupAttributes++;
      
      provisionerSuffixToValue.put("numberOfGroupAttributes", numberOfGroupAttributes);
      
      provisionerSuffixToValue.put("groupAttributeValueCacheHas", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache0has", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache0source", "target");
      provisionerSuffixToValue.put("groupAttributeValueCache0type", "groupAttribute");
      provisionerSuffixToValue.put("groupAttributeValueCache0groupAttribute", "id");

      String groupSearchMatchingAttribute = startWithSuffixToValue.get("groupSearchMatchingAttribute");
      
      provisionerSuffixToValue.put("groupAttributeValueCache1has", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache1source", "target");
      provisionerSuffixToValue.put("groupAttributeValueCache1type", "groupAttribute");
      provisionerSuffixToValue.put("groupAttributeValueCache1groupAttribute", groupSearchMatchingAttribute);

      provisionerSuffixToValue.put("hasTargetGroupLink", "true");
      
      provisionerSuffixToValue.put("groupMatchingAttributeCount", "2");
      provisionerSuffixToValue.put("groupMatchingAttribute0name", groupSearchMatchingAttribute);
      provisionerSuffixToValue.put("groupMatchingAttribute1name", "id");
      
    }
    
    
    {
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForGroupType"), true)) {
        provisionerSuffixToValue.put("azureGroupType", "true");
      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForGroupOwners"), true)) {
        provisionerSuffixToValue.put("groupOwners", "true");
      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForAllowOnlyMembersToPost"), false)) {
        provisionerSuffixToValue.put("allowOnlyMembersToPost", "true");
      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForHideGroupInOutlook"), false)) {
        provisionerSuffixToValue.put("hideGroupInOutlook", "true");
      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForSubscribeNewGroupMembers"), false)) {
        provisionerSuffixToValue.put("subscribeNewGroupMembers", "true");
      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForWelcomeEmailDisabled"), false)) {
        provisionerSuffixToValue.put("welcomeEmailDisabled", "true");
      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForResourceProvisioningOptionsTeam"), false)) {
        provisionerSuffixToValue.put("resourceProvisioningOptionsTeam", "true");
      }
      
    }
    
    {
      int numberOfEntityAttributes = 0;
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("manageEntitiesInAzure"), false)) {
        provisionerSuffixToValue.put("makeChangesToEntities", "true");
      }
      
      provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
      provisionerSuffixToValue.put("hasTargetEntityLink", "true");

      provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".name", "id");
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("manageEntitiesInAzure"), false)) {
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".showAdvancedAttribute", "true");
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".showAttributeCrud", "true");
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".insert", "false");
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".update", "false");
      }
      
      numberOfEntityAttributes++;
      
      String userPrincipalNameAttributeType = startWithSuffixToValue.get("entityUserPrincipalName");
      if (StringUtils.isNotBlank(userPrincipalNameAttributeType)) {
        if (StringUtils.equals("script", userPrincipalNameAttributeType)) {
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "translationScript");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpression", startWithSuffixToValue.get("entityUserPrincipalNameTranslationScript"));
        } else if (StringUtils.equals("other", userPrincipalNameAttributeType)) {
          //do nothing
        } else { 
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateFromGrouperProvisioningEntityField", userPrincipalNameAttributeType);
        }
        
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".name", "userPrincipalName");
        numberOfEntityAttributes++;
      }
      
      String entityMailNicknameType = startWithSuffixToValue.get("entityMailNickname");
      if (StringUtils.isNotBlank(entityMailNicknameType)) {
        if (StringUtils.equals("script", entityMailNicknameType)) {
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "translationScript");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpression", startWithSuffixToValue.get("entityMailNicknameTranslationScript"));
        } else if (StringUtils.equals("other", entityMailNicknameType)) {
          //do nothing
        } else {
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateFromGrouperProvisioningEntityField", entityMailNicknameType);
        }
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".name", "mailNickname");
        numberOfEntityAttributes++;
      }
      
      String entityOnPremisesImmutableIdType = startWithSuffixToValue.get("entityOnPremisesImmutableId");
      if (StringUtils.isNotBlank(entityOnPremisesImmutableIdType)) {
        if (StringUtils.equals("script", entityOnPremisesImmutableIdType)) {
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "translationScript");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpression", startWithSuffixToValue.get("entityOnPremisesImmutableIdTranslationScript"));
        } else if (StringUtils.equals("other", entityOnPremisesImmutableIdType)) {
          //do nothing
        } else {
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateFromGrouperProvisioningEntityField", entityOnPremisesImmutableIdType);
        }
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".name", "onPremisesImmutableId");
        numberOfEntityAttributes++;
      }
      
      
      String entityDisplayNameAttributeType = startWithSuffixToValue.get("entityDisplayName");
      if (StringUtils.isNotBlank(entityDisplayNameAttributeType)) {
        if (StringUtils.equals("script", entityDisplayNameAttributeType)) {
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "translationScript");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpression", startWithSuffixToValue.get("entityDisplayNameTranslationScript"));
        } else if (StringUtils.equals("other", entityDisplayNameAttributeType)) {
          //do nothing
        } else { 
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateFromGrouperProvisioningEntityField", entityDisplayNameAttributeType);
        }
        
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".name", "displayName");
        numberOfEntityAttributes++;
      }
      
      provisionerSuffixToValue.put("numberOfEntityAttributes", numberOfEntityAttributes);
      provisionerSuffixToValue.put("entityAttributeValueCacheHas", "true");

      provisionerSuffixToValue.put("entityAttributeValueCache0has", "true");
      provisionerSuffixToValue.put("entityAttributeValueCache0source", "target");
      provisionerSuffixToValue.put("entityAttributeValueCache0type", "entityAttribute");
      provisionerSuffixToValue.put("entityAttributeValueCache0entityAttribute", "id");
      
      String entitySearchMatchingAttribute = startWithSuffixToValue.get("entitySearchMatchingAttribute");

      provisionerSuffixToValue.put("entityAttributeValueCache1has", "true");
      provisionerSuffixToValue.put("entityAttributeValueCache1source", "target");
      provisionerSuffixToValue.put("entityAttributeValueCache1type", "entityAttribute");
      provisionerSuffixToValue.put("entityAttributeValueCache1entityAttribute", entitySearchMatchingAttribute);
      
      provisionerSuffixToValue.put("entityMatchingAttributeCount", "2");
      provisionerSuffixToValue.put("entityMatchingAttribute0name", entitySearchMatchingAttribute);
      provisionerSuffixToValue.put("entityMatchingAttribute1name", "id");
      
    }
    
    provisionerSuffixToValue.put("operateOnGrouperMemberships", "true");
    provisionerSuffixToValue.put("provisioningType", "membershipObjects");
    provisionerSuffixToValue.put("class", "edu.internet2.middleware.grouper.app.azure.GrouperAzureProvisioner");
    
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledFullSyncDaemon"), true) || GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledIncrementalSyncDaemon"), true)) {
      provisionerSuffixToValue.put("showAdvanced", "true");
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
    
    
    GrouperConfigurationModuleAttribute subjectSourceEntityResoverModuleAttribute = this.retrieveAttributes().get("subjectSourceEntityResolverAttributes");
    if (subjectSourceEntityResoverModuleAttribute != null && StringUtils.isNotBlank(subjectSourceEntityResoverModuleAttribute.getValue())) {
      String commaSeparatedResolverAttributes = subjectSourceEntityResoverModuleAttribute.getValue();
      List<String> list = GrouperUtil.splitTrimToList(commaSeparatedResolverAttributes, ",");
      if (list.size() > 2) {
        String errorMessage = GrouperTextContainer.textOrNull("subjectSourceEntityResolverAttributesTooManyAttributes");
        validationErrorsToDisplay.put(subjectSourceEntityResoverModuleAttribute.getHtmlForElementIdHandle(), errorMessage);
      }
    }
    
    GrouperConfigurationModuleAttribute entitySearchMatchingAttribute = this.retrieveAttributes().get("entitySearchMatchingAttribute");
    String searchMatchingAttributeValue = entitySearchMatchingAttribute.getValueOrExpressionEvaluation();
    if (StringUtils.equals(searchMatchingAttributeValue, "mailNickname") && 
        (mailNickNameAttribute == null || StringUtils.isBlank(mailNickNameAttribute.getValueOrExpressionEvaluation()))) {
      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithAzureConfigurationValidationEntitySearchMatchingAttributeNotValid");
      validationErrorsToDisplay.put(entitySearchMatchingAttribute.getHtmlForElementIdHandle(), errorMessage);
    }
    
    if (StringUtils.equals(searchMatchingAttributeValue, "onPremisesImmutableId") && 
        (onPremAttribute == null || StringUtils.isBlank(onPremAttribute.getValueOrExpressionEvaluation()))) {
      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithAzureConfigurationValidationEntitySearchMatchingAttributeNotValid");
      validationErrorsToDisplay.put(entitySearchMatchingAttribute.getHtmlForElementIdHandle(), errorMessage);
    }
    
    if (StringUtils.equals(searchMatchingAttributeValue, "userPrincipalName") && 
        (principalNameAttribute == null || StringUtils.isBlank(principalNameAttribute.getValueOrExpressionEvaluation()))) {
      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithAzureConfigurationValidationEntitySearchMatchingAttributeNotValid");
      validationErrorsToDisplay.put(entitySearchMatchingAttribute.getHtmlForElementIdHandle(), errorMessage);
    }
    
    
  }
  
  @Override
  public Class<? extends ProvisioningConfiguration> getProvisioningConfiguration() {
    return AzureProvisionerConfiguration.class;
  }
  

}
