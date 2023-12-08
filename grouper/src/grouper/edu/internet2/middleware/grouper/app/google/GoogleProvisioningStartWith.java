package edu.internet2.middleware.grouper.app.google;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.lang3.StringUtils;

public class GoogleProvisioningStartWith extends ProvisionerStartWithBase {
  
  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return "googleCommon";
  }
  
  @Override
  public void populateProvisionerConfigurationValuesFromStartWith(
      Map<String, String> startWithSuffixToValue,
      Map<String, Object> provisionerSuffixToValue) {
    
    provisionerSuffixToValue.put("googleExternalSystemConfigId", startWithSuffixToValue.get("googleExternalSystemConfigId"));
    
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
    
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("manageGroups"), false)) {
      
      provisionerSuffixToValue.put("operateOnGrouperGroups", "true");
      provisionerSuffixToValue.put("numberOfGroupAttributes", 3);
      
      provisionerSuffixToValue.put("targetGroupAttribute.0.name", "id");
      provisionerSuffixToValue.put("targetGroupAttribute.0.showAdvancedAttribute", "true");
      provisionerSuffixToValue.put("targetGroupAttribute.0.showAttributeCrud", "true");
      provisionerSuffixToValue.put("targetGroupAttribute.0.insert", "false");
      provisionerSuffixToValue.put("targetGroupAttribute.0.update", "false");
      
      String groupNameAttributeValue = startWithSuffixToValue.get("groupNameAttributeValue");
      if (StringUtils.equals("script", groupNameAttributeValue)) {
        provisionerSuffixToValue.put("targetGroupAttribute.1.translateExpressionType", "translationScript");
        provisionerSuffixToValue.put("targetGroupAttribute.1.translateExpression", startWithSuffixToValue.get("groupNameTranslationScript"));
      } else if (StringUtils.equals("other", groupNameAttributeValue)) {
        //do nothing
      } else { 
        provisionerSuffixToValue.put("targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
        provisionerSuffixToValue.put("targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", groupNameAttributeValue);
      }
      provisionerSuffixToValue.put("targetGroupAttribute.1.name", "name");
      
      String groupEmailAttributeValue = startWithSuffixToValue.get("groupEmailAttributeValue");
      if (StringUtils.equals("script", groupEmailAttributeValue)) {
        provisionerSuffixToValue.put("targetGroupAttribute.2.translateExpressionType", "translationScript");
        provisionerSuffixToValue.put("targetGroupAttribute.2.translateExpression", startWithSuffixToValue.get("groupEmailTranslationScript"));
      } else if (StringUtils.equals("other", groupEmailAttributeValue)) {
        //do nothing
      } else { 
        provisionerSuffixToValue.put("targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
        provisionerSuffixToValue.put("targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", groupEmailAttributeValue);
      }
      provisionerSuffixToValue.put("targetGroupAttribute.2.name", "email");
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("useGroupDescription"), true)) {
        
        provisionerSuffixToValue.put("numberOfGroupAttributes", 4);
        provisionerSuffixToValue.put("targetGroupAttribute.3.name", "description");
        provisionerSuffixToValue.put("targetGroupAttribute.3.translateExpressionType", "grouperProvisioningGroupField");
        provisionerSuffixToValue.put("targetGroupAttribute.3.translateFromGrouperProvisioningGroupField", "description");
      }
      
      provisionerSuffixToValue.put("groupAttributeValueCacheHas", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache0has", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache0source", "target");
      provisionerSuffixToValue.put("groupAttributeValueCache0type", "groupAttribute");
      provisionerSuffixToValue.put("groupAttributeValueCache0groupAttribute", "id");
      
      provisionerSuffixToValue.put("groupAttributeValueCache1has", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache1source", "target");
      provisionerSuffixToValue.put("groupAttributeValueCache1type", "groupAttribute");
      provisionerSuffixToValue.put("groupAttributeValueCache1groupAttribute", "name");

      provisionerSuffixToValue.put("hasTargetGroupLink", "true");
      
      provisionerSuffixToValue.put("groupMatchingAttributeCount", "2");
      provisionerSuffixToValue.put("groupMatchingAttribute0name", "name");
      provisionerSuffixToValue.put("groupMatchingAttribute1name", "id");
      
    }
    
    {
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForWhoCanAdd"), true)) {
        provisionerSuffixToValue.put("whoCanAdd", "true");
      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForWhoCanJoin"), true)) {
        provisionerSuffixToValue.put("whoCanJoin", "true");
      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForWhoCanViewMembership"), false)) {
        provisionerSuffixToValue.put("whoCanViewMembership", "true");
      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForWhoCanViewGroup"), false)) {
        provisionerSuffixToValue.put("whoCanViewGroup", "true");
      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForWhoCanInvite"), false)) {
        provisionerSuffixToValue.put("whoCanInvite", "true");
      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForAllowExternalMembers"), false)) {
        provisionerSuffixToValue.put("allowExternalMembers", "true");
      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForWhoCanPostMessage"), false)) {
        provisionerSuffixToValue.put("whoCanPostMessage", "true");
      }

      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasMetadataForAllowWebHosting"), false)) {
        provisionerSuffixToValue.put("allowWebPosting", "true");
      }
      
    }
    
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("manageEntities"), false)) {
      provisionerSuffixToValue.put("makeChangesToEntities", "true");
    }
      
    provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
    
    provisionerSuffixToValue.put("hasTargetEntityLink", "true");
    
    provisionerSuffixToValue.put("targetEntityAttribute.0.name", "id");
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("manageEntities"), false)) {
      provisionerSuffixToValue.put("targetEntityAttribute.0.showAdvancedAttribute", "true");
      provisionerSuffixToValue.put("targetEntityAttribute.0.showAttributeCrud", "true");
      provisionerSuffixToValue.put("targetEntityAttribute.0.insert", "false");
      provisionerSuffixToValue.put("targetEntityAttribute.0.update", "false");
    }
    
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
    provisionerSuffixToValue.put("targetEntityAttribute.1.name", "email");
    
    String entityFamilyNameType = startWithSuffixToValue.get("entityFamilyName");
    if (StringUtils.equals("script", entityFamilyNameType)) {
      provisionerSuffixToValue.put("targetEntityAttribute.2.translateExpressionType", "translationScript");
      provisionerSuffixToValue.put("targetEntityAttribute.2.translateExpression", startWithSuffixToValue.get("entityFamilyNameTranslationScript"));
    } else if (StringUtils.equals("other", entityFamilyNameType)) {
      //do nothing
    } else if (StringUtils.equals("subjectAttribute", entityFamilyNameType)) {

      provisionerSuffixToValue.put("targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
      provisionerSuffixToValue.put("targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "entityAttributeValueCache2");

      provisionerSuffixToValue.put("entityAttributeValueCache2has", "true");
      provisionerSuffixToValue.put("entityAttributeValueCache2source", "grouper");
      provisionerSuffixToValue.put("entityAttributeValueCache2type", "subjectTranslationScript");
      String subjectLastNameAttribute = startWithSuffixToValue.get("subjectLastNameAttribute");
      provisionerSuffixToValue.put("entityAttributeValueCache2translationScript", "${subject.getAttributeValue('"+subjectLastNameAttribute+"')}");

      
    } else { 
      provisionerSuffixToValue.put("targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
      provisionerSuffixToValue.put("targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", entityFamilyNameType);
    }
    provisionerSuffixToValue.put("targetEntityAttribute.2.name", "familyName");
    
    String entityGivenNameType = startWithSuffixToValue.get("entityGivenName");
    if (StringUtils.equals("script", entityGivenNameType)) {
      provisionerSuffixToValue.put("targetEntityAttribute.3.translateExpressionType", "translationScript");
      provisionerSuffixToValue.put("targetEntityAttribute.3.translateExpression", startWithSuffixToValue.get("entityGivenNameTranslationScript"));
    } else if (StringUtils.equals("other", entityGivenNameType)) {
      //do nothing
    } else if (StringUtils.equals("subjectAttribute", entityGivenNameType)) {

      provisionerSuffixToValue.put("targetEntityAttribute.3.translateExpressionType", "grouperProvisioningEntityField");
      provisionerSuffixToValue.put("targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", "entityAttributeValueCache3");

      provisionerSuffixToValue.put("entityAttributeValueCache3has", "true");
      provisionerSuffixToValue.put("entityAttributeValueCache3source", "grouper");
      provisionerSuffixToValue.put("entityAttributeValueCache3type", "subjectTranslationScript");
      String subjectFirstNameAttribute = startWithSuffixToValue.get("subjectFirstNameAttribute");
      provisionerSuffixToValue.put("entityAttributeValueCache3translationScript", "${subject.getAttributeValue('"+subjectFirstNameAttribute+"')}");

    } else { 
      provisionerSuffixToValue.put("targetEntityAttribute.3.translateExpressionType", "grouperProvisioningEntityField");
      provisionerSuffixToValue.put("targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", entityGivenNameType);
    }
    provisionerSuffixToValue.put("targetEntityAttribute.3.name", "givenName");
  
    provisionerSuffixToValue.put("numberOfEntityAttributes", "4");
    
    provisionerSuffixToValue.put("entityAttributeValueCacheHas", "true");

    provisionerSuffixToValue.put("entityAttributeValueCache0has", "true");
    provisionerSuffixToValue.put("entityAttributeValueCache0source", "target");
    provisionerSuffixToValue.put("entityAttributeValueCache0type", "entityAttribute");
    provisionerSuffixToValue.put("entityAttributeValueCache0entityAttribute", "id");
    
    provisionerSuffixToValue.put("entityAttributeValueCache1has", "true");
    provisionerSuffixToValue.put("entityAttributeValueCache1source", "target");
    provisionerSuffixToValue.put("entityAttributeValueCache1type", "entityAttribute");
    provisionerSuffixToValue.put("entityAttributeValueCache1entityAttribute", "email");
    
    provisionerSuffixToValue.put("entityMatchingAttributeCount", "2");
    provisionerSuffixToValue.put("entityMatchingAttribute0name", "email");
    provisionerSuffixToValue.put("entityMatchingAttribute1name", "id");
    
    provisionerSuffixToValue.put("operateOnGrouperMemberships", "true");
    provisionerSuffixToValue.put("provisioningType", "membershipObjects");
    provisionerSuffixToValue.put("class", "edu.internet2.middleware.grouper.app.google.GrouperGoogleProvisioner");
    
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledFullSyncDaemon"), true) || GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledIncrementalSyncDaemon"), true)) {
      provisionerSuffixToValue.put("showAdvanced", "true");
    }

  }

  @Override
  public Map<String, String> screenRedraw(Map<String, String> suffixToValue,
      Set<String> suffixesUserJustChanged) {
    
    Map<String, String> result = new HashMap<>();
    
    for (String suffixUserJustChanged: suffixesUserJustChanged) {
      
      if (StringUtils.equals(suffixUserJustChanged, "googlePattern")) {
        String valueUserEnteredOnScreen = suffixToValue.get(suffixUserJustChanged);
        if (StringUtils.equals(valueUserEnteredOnScreen, "manageGroupsManageEntities")) {
          result.put("manageGroups", "true");
          result.put("manageEntities", "true");
          result.put("selectAllGroups", "true");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "manageGroupsReadonlyEntities")) {
          result.put("manageGroups", "true");
          result.put("manageEntities", "false");
          result.put("selectAllGroups", "true");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "manageEntities")) {
          result.put("manageGroups", "false");
          result.put("manageEntities", "true");
          result.put("selectAllGroups", "false");
        }
        else if (StringUtils.equals(valueUserEnteredOnScreen, "other")) {
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
    
  }

  @Override
  public Class<? extends ProvisioningConfiguration> getProvisioningConfiguration() {
    return GoogleProvisionerConfiguration.class;
  }

}
