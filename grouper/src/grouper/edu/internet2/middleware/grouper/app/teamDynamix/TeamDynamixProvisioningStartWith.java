/**
 * 
 */
package edu.internet2.middleware.grouper.app.teamDynamix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 */
public class TeamDynamixProvisioningStartWith extends ProvisionerStartWithBase {

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return "teamDynamixCommon";
  }
  
  @Override
  public void populateProvisionerConfigurationValuesFromStartWith(
      Map<String, String> startWithSuffixToValue,
      Map<String, Object> provisionerSuffixToValue) {
    
    
    /**
     * 
     * ask for group matching/search attribute. dropdown - name, id
     * ask for entity matching/search attribute. dropdown - externalId, id
     */
    
    provisionerSuffixToValue.put("teamDynamixExternalSystemConfigId", startWithSuffixToValue.get("teamDynamixExternalSystemConfigId"));
    
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
      
      String groupNameAttributeType = startWithSuffixToValue.get("groupNameAttributeValue");
      if (StringUtils.equals("script", groupNameAttributeType)) {
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".translateExpressionType", "translationScript");
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".translateExpression", startWithSuffixToValue.get("groupNameTranslationScript"));
      } else if (StringUtils.equals("other", groupNameAttributeType)) {
        //do nothing
      } else { 
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".translateExpressionType", "grouperProvisioningGroupField");
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".translateFromGrouperProvisioningGroupField", groupNameAttributeType);
      }
      
      provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".name", "Name");
      numberOfGroupAttributes++;
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("useGroupDescription"), true)) {
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".name", "Description");
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".translateExpressionType", "grouperProvisioningGroupField");
        provisionerSuffixToValue.put("targetGroupAttribute."+numberOfGroupAttributes+".translateFromGrouperProvisioningGroupField", "description");
        numberOfGroupAttributes++;
      }
      
      provisionerSuffixToValue.put("numberOfGroupAttributes", numberOfGroupAttributes);
      
      provisionerSuffixToValue.put("groupAttributeValueCacheHas", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache0has", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache0source", "target");
      provisionerSuffixToValue.put("groupAttributeValueCache0type", "groupAttribute");
      provisionerSuffixToValue.put("groupAttributeValueCache0groupAttribute", "id");

      provisionerSuffixToValue.put("groupAttributeValueCache1has", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache1source", "target");
      provisionerSuffixToValue.put("groupAttributeValueCache1type", "groupAttribute");
      provisionerSuffixToValue.put("groupAttributeValueCache1groupAttribute", "Name");

      provisionerSuffixToValue.put("hasTargetGroupLink", "true");
      
      provisionerSuffixToValue.put("groupMatchingAttributeCount", "2");
      provisionerSuffixToValue.put("groupMatchingAttribute0name", "Name");
      provisionerSuffixToValue.put("groupMatchingAttribute1name", "id");
      
    }
    
    
    {
      int numberOfEntityAttributes = 0;
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("manageEntities"), false)) {
        provisionerSuffixToValue.put("makeChangesToEntities", "true");
      }
      
      provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
      provisionerSuffixToValue.put("hasTargetEntityLink", "true");

      provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".name", "id");
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("manageEntities"), false)) {
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".showAdvancedAttribute", "true");
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".showAttributeCrud", "true");
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".insert", "false");
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".update", "false");
      }
      
      numberOfEntityAttributes++;
      
      String userFirstNameAttributeType = startWithSuffixToValue.get("entityUserFirstName");
      if (StringUtils.isNotBlank(userFirstNameAttributeType)) {
        if (StringUtils.equals("script", userFirstNameAttributeType)) {
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "translationScript");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpression", startWithSuffixToValue.get("entityUserFirstNameTranslationScript"));
        } else if (StringUtils.equals("other", userFirstNameAttributeType)) {
          //do nothing
        } else if (StringUtils.equals("subjectAttribute", userFirstNameAttributeType)) {

          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateFromGrouperProvisioningEntityField", "entityAttributeValueCache2");

          provisionerSuffixToValue.put("entityAttributeValueCache2has", "true");
          provisionerSuffixToValue.put("entityAttributeValueCache2source", "grouper");
          provisionerSuffixToValue.put("entityAttributeValueCache2type", "subjectTranslationScript");
          String subjectFirstNameAttribute = startWithSuffixToValue.get("subjectUserFirstNameAttribute");
          provisionerSuffixToValue.put("entityAttributeValueCache2translationScript", "${subject.getAttributeValue('"+subjectFirstNameAttribute+"')}");
        } else { 
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateFromGrouperProvisioningEntityField", userFirstNameAttributeType);
        }
        
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".name", "FirstName");
        numberOfEntityAttributes++;
      }
      
      String entityUserLastNameType = startWithSuffixToValue.get("entityUserLastName");
      if (StringUtils.isNotBlank(entityUserLastNameType)) {
        if (StringUtils.equals("script", entityUserLastNameType)) {
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "translationScript");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpression", startWithSuffixToValue.get("entityUserLastNameTranslationScript"));
        } else if (StringUtils.equals("other", entityUserLastNameType)) {
          //do nothing
        } else if (StringUtils.equals("subjectAttribute", entityUserLastNameType)) {

          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateFromGrouperProvisioningEntityField", "entityAttributeValueCache3");

          provisionerSuffixToValue.put("entityAttributeValueCache3has", "true");
          provisionerSuffixToValue.put("entityAttributeValueCache3source", "grouper");
          provisionerSuffixToValue.put("entityAttributeValueCache3type", "subjectTranslationScript");
          String subjectLasttNameAttribute = startWithSuffixToValue.get("subjectUserLastNameAttribute");
          provisionerSuffixToValue.put("entityAttributeValueCache3translationScript", "${subject.getAttributeValue('"+subjectLasttNameAttribute+"')}");
        } else {
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateFromGrouperProvisioningEntityField", entityUserLastNameType);
        }
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".name", "LastName");
        numberOfEntityAttributes++;
      }
      
      String entityPrimaryEmailType = startWithSuffixToValue.get("entityPrimaryEmail");
      if (StringUtils.isNotBlank(entityPrimaryEmailType)) {
        if (StringUtils.equals("script", entityPrimaryEmailType)) {
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "translationScript");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpression", startWithSuffixToValue.get("entityPrimaryEmailTranslationScript"));
        } else if (StringUtils.equals("other", entityPrimaryEmailType)) {
          //do nothing
        } else {
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateFromGrouperProvisioningEntityField", entityPrimaryEmailType);
        }
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".name", "PrimaryEmail");
        numberOfEntityAttributes++;
      }
      
      
      String entityUsernameAttributeType = startWithSuffixToValue.get("entityUsername");
      if (StringUtils.isNotBlank(entityUsernameAttributeType)) {
        if (StringUtils.equals("script", entityUsernameAttributeType)) {
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "translationScript");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpression", startWithSuffixToValue.get("entityUsernameTranslationScript"));
        } else if (StringUtils.equals("other", entityUsernameAttributeType)) {
          //do nothing
        } else { 
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateFromGrouperProvisioningEntityField", entityUsernameAttributeType);
        }
        
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".name", "UserName");
        numberOfEntityAttributes++;
      }
      
      String entityExternalIdType = startWithSuffixToValue.get("entityExternalId");
      if (StringUtils.isNotBlank(entityExternalIdType)) {
        if (StringUtils.equals("script", entityExternalIdType)) {
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "translationScript");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpression", startWithSuffixToValue.get("entityExternalIdTranslationScript"));
        } else if (StringUtils.equals("other", entityExternalIdType)) {
          //do nothing
        } else { 
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateFromGrouperProvisioningEntityField", entityExternalIdType);
        }
        
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".name", "ExternalID");
        numberOfEntityAttributes++;
      }
      
      String entitySecurityRoleIdType = startWithSuffixToValue.get("entitySecurityRoleId");
      if (StringUtils.isNotBlank(entitySecurityRoleIdType)) {
        if (StringUtils.equals("script", entitySecurityRoleIdType)) {
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "translationScript");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpression", startWithSuffixToValue.get("entitySecurityRoleIdTranslationScript"));
        } else if (StringUtils.equals("other", entitySecurityRoleIdType)) {
          //do nothing
        } else { 
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateFromGrouperProvisioningEntityField", entitySecurityRoleIdType);
        }
        
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".name", "SecurityRoleID");
        numberOfEntityAttributes++;
      }
      
      String entityCompanyType = startWithSuffixToValue.get("entityCompany");
      if (StringUtils.isNotBlank(entityCompanyType)) {
        if (StringUtils.equals("script", entityCompanyType)) {
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "translationScript");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpression", startWithSuffixToValue.get("entityCompanyTranslationScript"));
        } else if (StringUtils.equals("other", entityCompanyType)) {
          //do nothing
        } else { 
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".translateFromGrouperProvisioningEntityField", entityCompanyType);
        }
        
        provisionerSuffixToValue.put("targetEntityAttribute."+numberOfEntityAttributes+".name", "Company");
        numberOfEntityAttributes++;
      }
      
      provisionerSuffixToValue.put("numberOfEntityAttributes", numberOfEntityAttributes);
      provisionerSuffixToValue.put("entityAttributeValueCacheHas", "true");

      provisionerSuffixToValue.put("entityAttributeValueCache0has", "true");
      provisionerSuffixToValue.put("entityAttributeValueCache0source", "target");
      provisionerSuffixToValue.put("entityAttributeValueCache0type", "entityAttribute");
      provisionerSuffixToValue.put("entityAttributeValueCache0entityAttribute", "id");
      
      provisionerSuffixToValue.put("entityAttributeValueCache1has", "true");
      provisionerSuffixToValue.put("entityAttributeValueCache1source", "target");
      provisionerSuffixToValue.put("entityAttributeValueCache1type", "entityAttribute");
      provisionerSuffixToValue.put("entityAttributeValueCache1entityAttribute", "ExternalID");
      
      provisionerSuffixToValue.put("entityMatchingAttributeCount", "2");
      provisionerSuffixToValue.put("entityMatchingAttribute0name", "ExternalID");
      provisionerSuffixToValue.put("entityMatchingAttribute1name", "id");
      
    }
    
    provisionerSuffixToValue.put("operateOnGrouperMemberships", "true");
    provisionerSuffixToValue.put("provisioningType", "membershipObjects");
    provisionerSuffixToValue.put("class", "edu.internet2.middleware.grouper.app.teamDynamix.TeamDynamixProvisioner");
    
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledFullSyncDaemon"), true) || GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledIncrementalSyncDaemon"), true)) {
      provisionerSuffixToValue.put("showAdvanced", "true");
    }
    
  }

  @Override
  public Map<String, String> screenRedraw(Map<String, String> suffixToValue,
      Set<String> suffixesUserJustChanged) {
    
    Map<String, String> result = new HashMap<>();
    
    for (String suffixUserJustChanged: suffixesUserJustChanged) {
      
      if (StringUtils.equals(suffixUserJustChanged, "teamDynamixPattern")) {
        String valueUserEnteredOnScreen = suffixToValue.get(suffixUserJustChanged);
        if (StringUtils.equals(valueUserEnteredOnScreen, "manageGroupsManageEntities")) {
          result.put("manageGroups", "true");
          result.put("manageEntities", "true");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "manageGroupsReadonlyEntities")) {
          result.put("manageGroups", "true");
          result.put("manageEntities", "false");
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
    
//    GrouperConfigurationModuleAttribute onPremAttribute = this.retrieveAttributes().get("entityOnPremisesImmutableId");
//    GrouperConfigurationModuleAttribute principalNameAttribute = this.retrieveAttributes().get("entityUserPrincipalName");
//    GrouperConfigurationModuleAttribute mailNickNameAttribute = this.retrieveAttributes().get("entityMailNickname");
//    
//    if (onPremAttribute == null && principalNameAttribute == null && mailNickNameAttribute == null) {
//      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithAzureConfigurationValidationEntityFieldNotSelected");
//      errorsToDisplay.add(errorMessage);
//      return;
//    }
//    
//    
//    boolean allBlank = true;
//    
//    if (onPremAttribute != null) {
//      String value = onPremAttribute.getValueOrExpressionEvaluation();
//      if (StringUtils.isNotBlank(value)) {
//        allBlank = false;
//      }
//    }
//    
//    if (allBlank && principalNameAttribute != null) {
//      String value = principalNameAttribute.getValueOrExpressionEvaluation();
//      if (StringUtils.isNotBlank(value)) {
//        allBlank = false;
//      }
//    }
//    
//    if (allBlank && mailNickNameAttribute != null) {
//      String value = mailNickNameAttribute.getValueOrExpressionEvaluation();
//      if (StringUtils.isNotBlank(value)) {
//        allBlank = false;
//      }
//    }
//    
//    if (allBlank) {
//      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithAzureConfigurationValidationEntityFieldNotSelected");
//      errorsToDisplay.add(errorMessage);
//    }
//    
//    
//    GrouperConfigurationModuleAttribute subjectSourceEntityResoverModuleAttribute = this.retrieveAttributes().get("subjectSourceEntityResolverAttributes");
//    if (subjectSourceEntityResoverModuleAttribute != null && StringUtils.isNotBlank(subjectSourceEntityResoverModuleAttribute.getValue())) {
//      String commaSeparatedResolverAttributes = subjectSourceEntityResoverModuleAttribute.getValue();
//      List<String> list = GrouperUtil.splitTrimToList(commaSeparatedResolverAttributes, ",");
//      if (list.size() > 2) {
//        String errorMessage = GrouperTextContainer.textOrNull("subjectSourceEntityResolverAttributesTooManyAttributes");
//        validationErrorsToDisplay.put(subjectSourceEntityResoverModuleAttribute.getHtmlForElementIdHandle(), errorMessage);
//      }
//    }
//    
//    GrouperConfigurationModuleAttribute entitySearchMatchingAttribute = this.retrieveAttributes().get("entitySearchMatchingAttribute");
//    String searchMatchingAttributeValue = entitySearchMatchingAttribute.getValueOrExpressionEvaluation();
//    if (StringUtils.equals(searchMatchingAttributeValue, "mailNickname") && 
//        (mailNickNameAttribute == null || StringUtils.isBlank(mailNickNameAttribute.getValueOrExpressionEvaluation()))) {
//      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithAzureConfigurationValidationEntitySearchMatchingAttributeNotValid");
//      validationErrorsToDisplay.put(entitySearchMatchingAttribute.getHtmlForElementIdHandle(), errorMessage);
//    }
//    
//    if (StringUtils.equals(searchMatchingAttributeValue, "onPremisesImmutableId") && 
//        (onPremAttribute == null || StringUtils.isBlank(onPremAttribute.getValueOrExpressionEvaluation()))) {
//      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithAzureConfigurationValidationEntitySearchMatchingAttributeNotValid");
//      validationErrorsToDisplay.put(entitySearchMatchingAttribute.getHtmlForElementIdHandle(), errorMessage);
//    }
//    
//    if (StringUtils.equals(searchMatchingAttributeValue, "userPrincipalName") && 
//        (principalNameAttribute == null || StringUtils.isBlank(principalNameAttribute.getValueOrExpressionEvaluation()))) {
//      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithAzureConfigurationValidationEntitySearchMatchingAttributeNotValid");
//      validationErrorsToDisplay.put(entitySearchMatchingAttribute.getHtmlForElementIdHandle(), errorMessage);
//    }
    
    
  }
  
  @Override
  public Class<? extends ProvisioningConfiguration> getProvisioningConfiguration() {
    return TeamDynamixProvisionerConfiguration.class;
  }
  

}
