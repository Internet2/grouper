package edu.internet2.middleware.grouper.app.scim2Provisioning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class ScimProvisioningStartWith extends ProvisionerStartWithBase {
  
  
  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return "scimCommon";
  }
  
  @Override
  public void populateProvisionerConfigurationValuesFromStartWith(
      Map<String, String> startWithSuffixToValue,
      Map<String, Object> provisionerSuffixToValue) {
    
    provisionerSuffixToValue.put("bearerTokenExternalSystemConfigId", startWithSuffixToValue.get("bearerTokenExternalSystemConfigId"));
    
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
    
    
    String scimType = startWithSuffixToValue.get("scimType");
    
    if (StringUtils.equals("Github", scimType)) {
      provisionerSuffixToValue.put("acceptHeader", "application/vnd.github.v3+json");
    }
    
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("manageGroups"), false) && !StringUtils.equals(scimType, "Github")) {
      
      provisionerSuffixToValue.put("operateOnGrouperGroups", "true");
      provisionerSuffixToValue.put("customizeGroupCrud", "true");
      provisionerSuffixToValue.put("updateGroups", "false");
      
      provisionerSuffixToValue.put("numberOfGroupAttributes", "2");
      
      provisionerSuffixToValue.put("targetGroupAttribute.0.name", "id");
      provisionerSuffixToValue.put("targetGroupAttribute.0.showAdvancedAttribute", "true");
      provisionerSuffixToValue.put("targetGroupAttribute.0.showAttributeCrud", "true");
      provisionerSuffixToValue.put("targetGroupAttribute.0.insert", "false");
      provisionerSuffixToValue.put("targetGroupAttribute.0.update", "false");
      
      String groupDisplayNameAttributeValue = startWithSuffixToValue.get("groupDisplayNameAttributeValue");
      if (StringUtils.equals("script", groupDisplayNameAttributeValue)) {
        provisionerSuffixToValue.put("targetGroupAttribute.1.translateExpressionType", "translationScript");
        provisionerSuffixToValue.put("targetGroupAttribute.1.translateExpression", startWithSuffixToValue.get("groupDisplayNameTranslationScript"));
      } else if (StringUtils.equals("other", groupDisplayNameAttributeValue)) {
        //do nothing
      } else { 
        provisionerSuffixToValue.put("targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
        provisionerSuffixToValue.put("targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", groupDisplayNameAttributeValue);
      }
      provisionerSuffixToValue.put("targetGroupAttribute.1.name", "displayName");
      
      provisionerSuffixToValue.put("groupAttributeValueCacheHas", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache0has", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache0source", "target");
      provisionerSuffixToValue.put("groupAttributeValueCache0type", "groupAttribute");
      provisionerSuffixToValue.put("groupAttributeValueCache0groupAttribute", "id");
      
      provisionerSuffixToValue.put("groupAttributeValueCache1has", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache1source", "target");
      provisionerSuffixToValue.put("groupAttributeValueCache1type", "groupAttribute");
      provisionerSuffixToValue.put("groupAttributeValueCache1groupAttribute", "displayName");

      provisionerSuffixToValue.put("hasTargetGroupLink", "true");
      
      provisionerSuffixToValue.put("groupMatchingAttributeCount", "2");
      provisionerSuffixToValue.put("groupMatchingAttribute0name", "displayName");
      provisionerSuffixToValue.put("groupMatchingAttribute1name", "id");
      
    }
    
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("manageEntities"), false)) {
      provisionerSuffixToValue.put("makeChangesToEntities", "true");
    }
      
    provisionerSuffixToValue.put("operateOnGrouperEntities", "true");

    provisionerSuffixToValue.put("hasTargetEntityLink", "true");
    
    provisionerSuffixToValue.put("targetEntityAttribute.0.name", "id");
    provisionerSuffixToValue.put("targetEntityAttribute.0.showAdvancedAttribute", "true");
    provisionerSuffixToValue.put("targetEntityAttribute.0.showAttributeCrud", "true");
    provisionerSuffixToValue.put("targetEntityAttribute.0.insert", "false");
    provisionerSuffixToValue.put("targetEntityAttribute.0.update", "false");
    
    String entityEmailSubjectAttributeType = startWithSuffixToValue.get("entityEmailSubjectAttribute");
    if (StringUtils.equals("script", entityEmailSubjectAttributeType)) {
      provisionerSuffixToValue.put("targetEntityAttribute.1.translateExpressionType", "translationScript");
      provisionerSuffixToValue.put("targetEntityAttribute.1.translateExpression", startWithSuffixToValue.get("entityEmailTranslationScript"));
    } else if (StringUtils.equals("other", entityEmailSubjectAttributeType)) {
      //do nothing
    } else { 
      provisionerSuffixToValue.put("targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
      provisionerSuffixToValue.put("targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", entityEmailSubjectAttributeType);
    }
    provisionerSuffixToValue.put("targetEntityAttribute.1.name", "emailValue");
    
//    String entityFamilyNameType = startWithSuffixToValue.get("entityFamilyName");
//    if (StringUtils.equals("script", entityFamilyNameType)) {
//      provisionerSuffixToValue.put("targetEntityAttribute.2.translateExpressionType", "translationScript");
//      provisionerSuffixToValue.put("targetEntityAttribute.2.translateExpression", startWithSuffixToValue.get("entityFamilyNameTranslationScript"));
//    } else if (StringUtils.equals("other", entityFamilyNameType)) {
//      //do nothing
//    } else { 
//      provisionerSuffixToValue.put("targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
//      provisionerSuffixToValue.put("targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "entityAttributeValueCache2");
//    }
    
    provisionerSuffixToValue.put("targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
    provisionerSuffixToValue.put("targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "entityAttributeValueCache2");
    provisionerSuffixToValue.put("targetEntityAttribute.2.name", "familyName");
//    
//    String entityGivenNameType = startWithSuffixToValue.get("entityGivenName");
//    if (StringUtils.equals("script", entityGivenNameType)) {
//      provisionerSuffixToValue.put("targetEntityAttribute.3.translateExpressionType", "translationScript");
//      provisionerSuffixToValue.put("targetEntityAttribute.3.translateExpression", startWithSuffixToValue.get("entityGivenNameTranslationScript"));
//    } else if (StringUtils.equals("other", entityGivenNameType)) {
//      //do nothing
//    } else { 
//      provisionerSuffixToValue.put("targetEntityAttribute.3.translateExpressionType", "grouperProvisioningEntityField");
//      provisionerSuffixToValue.put("targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", entityGivenNameType);
//    }
    
    provisionerSuffixToValue.put("targetEntityAttribute.3.translateExpressionType", "grouperProvisioningEntityField");
    provisionerSuffixToValue.put("targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", "entityAttributeValueCache3");
    provisionerSuffixToValue.put("targetEntityAttribute.3.name", "givenName");
    
    String entityUsernameType = startWithSuffixToValue.get("entityUsername");
    if (StringUtils.equals("script", entityUsernameType)) {
      provisionerSuffixToValue.put("targetEntityAttribute.4.translateExpressionType", "translationScript");
      provisionerSuffixToValue.put("targetEntityAttribute.4.translateExpression", startWithSuffixToValue.get("entityUsernameTranslationScript"));
    } else if (StringUtils.equals("other", entityUsernameType)) {
      //do nothing
    } else { 
      provisionerSuffixToValue.put("targetEntityAttribute.4.translateExpressionType", "grouperProvisioningEntityField");
      provisionerSuffixToValue.put("targetEntityAttribute.4.translateFromGrouperProvisioningEntityField", entityUsernameType);
    }
    provisionerSuffixToValue.put("targetEntityAttribute.4.name", "userName");
    
    
    String entityDisplayNameType = startWithSuffixToValue.get("entityDisplayName");
    if (StringUtils.equals("script", entityDisplayNameType)) {
      provisionerSuffixToValue.put("targetEntityAttribute.5.translateExpressionType", "translationScript");
      provisionerSuffixToValue.put("targetEntityAttribute.5.translateExpression", startWithSuffixToValue.get("entityDisplayNameTranslationScript"));
    } else if (StringUtils.equals("other", entityDisplayNameType)) {
      //do nothing
    } else { 
      provisionerSuffixToValue.put("targetEntityAttribute.5.translateExpressionType", "grouperProvisioningEntityField");
      provisionerSuffixToValue.put("targetEntityAttribute.5.translateFromGrouperProvisioningEntityField", entityDisplayNameType);
    }
    provisionerSuffixToValue.put("targetEntityAttribute.5.name", "displayName");
    
  
    provisionerSuffixToValue.put("numberOfEntityAttributes", "6");
    
    provisionerSuffixToValue.put("entityAttributeValueCacheHas", "true");

    provisionerSuffixToValue.put("entityAttributeValueCache0has", "true");
    provisionerSuffixToValue.put("entityAttributeValueCache0source", "target");
    provisionerSuffixToValue.put("entityAttributeValueCache0type", "entityAttribute");
    provisionerSuffixToValue.put("entityAttributeValueCache0entityAttribute", "id");
    
    provisionerSuffixToValue.put("entityAttributeValueCache1has", "true");
    provisionerSuffixToValue.put("entityAttributeValueCache1source", "target");
    provisionerSuffixToValue.put("entityAttributeValueCache1type", "entityAttribute");
    provisionerSuffixToValue.put("entityAttributeValueCache1entityAttribute", "userName");
    
    provisionerSuffixToValue.put("entityAttributeValueCache2has", "true");
    provisionerSuffixToValue.put("entityAttributeValueCache2source", "grouper");
    provisionerSuffixToValue.put("entityAttributeValueCache2type", "subjectTranslationScript");
    String subjectLastNameAttribute = startWithSuffixToValue.get("subjectLastNameAttribute");
    provisionerSuffixToValue.put("entityAttributeValueCache2translationScript", "${subject.getAttributeValue('"+subjectLastNameAttribute+"')}");
    
    provisionerSuffixToValue.put("entityAttributeValueCache3has", "true");
    provisionerSuffixToValue.put("entityAttributeValueCache3source", "grouper");
    provisionerSuffixToValue.put("entityAttributeValueCache3type", "subjectTranslationScript");
    String subjectFirstNameAttribute = startWithSuffixToValue.get("subjectFirstNameAttribute");
    provisionerSuffixToValue.put("entityAttributeValueCache3translationScript", "${subject.getAttributeValue('"+subjectFirstNameAttribute+"')}");
    
    provisionerSuffixToValue.put("entityMatchingAttributeCount", "2");
    provisionerSuffixToValue.put("entityMatchingAttribute0name", "userName");
    provisionerSuffixToValue.put("entityMatchingAttribute1name", "id");
    
    provisionerSuffixToValue.put("operateOnGrouperMemberships", "true");
    provisionerSuffixToValue.put("provisioningType", "membershipObjects");
    provisionerSuffixToValue.put("customizeMembershipCrud", "true");
    provisionerSuffixToValue.put("selectMemberships", "false");
    provisionerSuffixToValue.put("replaceMemberships", "true");
    
    provisionerSuffixToValue.put("class", "edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Provisioner");
  }

  @Override
  public Map<String, String> screenRedraw(Map<String, String> suffixToValue,
      Set<String> suffixesUserJustChanged) {
    
    Map<String, String> result = new HashMap<>();
    
    for (String suffixUserJustChanged: suffixesUserJustChanged) {
      
      if (StringUtils.equals(suffixUserJustChanged, "scimPattern")) {
        String valueUserEnteredOnScreen = suffixToValue.get(suffixUserJustChanged);
        if (StringUtils.equals(valueUserEnteredOnScreen, "awsGroupsEntitiesMemberships")) {
          result.put("manageGroups", "true");
          result.put("manageEntities", "true");
          result.put("selectAllGroups", "true");
          result.put("scimType", "AWS");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "githubEntities")) {
          result.put("manageGroups", "false");
          result.put("manageEntities", "true");
          result.put("selectAllGroups", "false");
          result.put("scimType", "Github");
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
    
    GrouperConfigurationModuleAttribute subjectSourceEntityResoverModuleAttribute = this.retrieveAttributes().get("subjectSourceEntityResolverAttributes");
    if (subjectSourceEntityResoverModuleAttribute != null && StringUtils.isNotBlank(subjectSourceEntityResoverModuleAttribute.getValue())) {
      String commaSeparatedResolverAttributes = subjectSourceEntityResoverModuleAttribute.getValue();
      List<String> list = GrouperUtil.splitTrimToList(commaSeparatedResolverAttributes, ",");
      if (list.size() > 2) {
        String errorMessage = GrouperTextContainer.textOrNull("subjectSourceEntityResolverAttributesTooManyAttributes");
        validationErrorsToDisplay.put(subjectSourceEntityResoverModuleAttribute.getHtmlForElementIdHandle(), errorMessage);
      }
    }
    
    GrouperConfigurationModuleAttribute scimTypeAttribute = this.retrieveAttributes().get("scimType");
    if (scimTypeAttribute != null && (GrouperUtil.equals(scimTypeAttribute.getValueOrExpressionEvaluation(), "AWS") || GrouperUtil.equals(scimTypeAttribute.getValueOrExpressionEvaluation(), "Github")) ) {
      GrouperConfigurationModuleAttribute entityFamilyNameAttribute = this.retrieveAttributes().get("subjectLastNameAttribute");
      if (entityFamilyNameAttribute == null || StringUtils.isBlank(entityFamilyNameAttribute.getValueOrExpressionEvaluation())) {
        String errorMessage = GrouperTextContainer.textOrNull("scim2AWSEntityFamilyNameIsRequired");
        validationErrorsToDisplay.put(subjectSourceEntityResoverModuleAttribute.getHtmlForElementIdHandle(), errorMessage);
      }
    }
    if (scimTypeAttribute != null && (GrouperUtil.equals(scimTypeAttribute.getValueOrExpressionEvaluation(), "AWS") || GrouperUtil.equals(scimTypeAttribute.getValueOrExpressionEvaluation(), "Github")) ) {
      GrouperConfigurationModuleAttribute entityGivenNameAttribute = this.retrieveAttributes().get("subjectFirstNameAttribute");
      if (entityGivenNameAttribute == null || StringUtils.isBlank(entityGivenNameAttribute.getValueOrExpressionEvaluation())) {
        String errorMessage = GrouperTextContainer.textOrNull("scim2AWSEntityGivenNameIsRequired");
        validationErrorsToDisplay.put(subjectSourceEntityResoverModuleAttribute.getHtmlForElementIdHandle(), errorMessage);
      }
    }
    if (scimTypeAttribute != null) {
      GrouperConfigurationModuleAttribute entityUserNameAttribute = this.retrieveAttributes().get("entityUsername");
      if (entityUserNameAttribute == null || StringUtils.isBlank(entityUserNameAttribute.getValueOrExpressionEvaluation())) {
        String errorMessage = GrouperTextContainer.textOrNull("scim2AWSEntityUserNameIsRequired");
        validationErrorsToDisplay.put(subjectSourceEntityResoverModuleAttribute.getHtmlForElementIdHandle(), errorMessage);
      }
    }
    if (scimTypeAttribute != null && GrouperUtil.equals(scimTypeAttribute.getValueOrExpressionEvaluation(), "AWS")) {
      GrouperConfigurationModuleAttribute entityDisplayNameAttribute = this.retrieveAttributes().get("entityDisplayName");
      if (entityDisplayNameAttribute == null || StringUtils.isBlank(entityDisplayNameAttribute.getValueOrExpressionEvaluation())) {
        String errorMessage = GrouperTextContainer.textOrNull("scim2AWSEntityDisplayNameIsRequired");
        validationErrorsToDisplay.put(subjectSourceEntityResoverModuleAttribute.getHtmlForElementIdHandle(), errorMessage);
      }
    }
    
  }

  @Override
  public Class<? extends ProvisioningConfiguration> getProvisioningConfiguration() {
    return GrouperScim2Configuration.class;
  }

}
