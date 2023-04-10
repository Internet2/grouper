package edu.internet2.middleware.grouper.app.remedyV2;

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

public class RemedyProvisioningStartWith extends ProvisionerStartWithBase {
  
  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return "remedyCommon";
  }
  
  @Override
  public void populateProvisionerConfigurationValuesFromStartWith(
      Map<String, String> startWithSuffixToValue,
      Map<String, Object> provisionerSuffixToValue) {
    
    provisionerSuffixToValue.put("remedyExternalSystemConfigId", startWithSuffixToValue.get("remedyExternalSystemConfigId"));
    
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
      provisionerSuffixToValue.put("numberOfGroupAttributes", 2);
      
      provisionerSuffixToValue.put("targetGroupAttribute.0.name", "permissionGroupId");
      provisionerSuffixToValue.put("targetGroupAttribute.0.showAdvancedAttribute", "true");
      provisionerSuffixToValue.put("targetGroupAttribute.0.showAttributeCrud", "true");
      provisionerSuffixToValue.put("targetGroupAttribute.0.insert", "false");
      provisionerSuffixToValue.put("targetGroupAttribute.0.update", "false");
      
      String permissionGroupAttributeValue = startWithSuffixToValue.get("permissionGroupAttributeValue");
      if (StringUtils.equals("script", permissionGroupAttributeValue)) {
        provisionerSuffixToValue.put("targetGroupAttribute.1.translateExpressionType", "translationScript");
        provisionerSuffixToValue.put("targetGroupAttribute.1.translateExpression", startWithSuffixToValue.get("permissionGroupTranslationScript"));
        provisionerSuffixToValue.put("targetMembershipAttribute.0.translateExpressionType", "translationScript");
        provisionerSuffixToValue.put("targetMembershipAttribute.0.translateExpression", startWithSuffixToValue.get("permissionGroupTranslationScript"));
      } else if (StringUtils.equals("other", permissionGroupAttributeValue)) {
        //do nothing
      } else { 
        provisionerSuffixToValue.put("targetMembershipAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
        provisionerSuffixToValue.put("targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField", permissionGroupAttributeValue);
        provisionerSuffixToValue.put("targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
        provisionerSuffixToValue.put("targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", permissionGroupAttributeValue);
      }
      provisionerSuffixToValue.put("targetGroupAttribute.1.name", "permissionGroup");
      provisionerSuffixToValue.put("targetMembershipAttribute.0.name", "permissionGroup");
      
      provisionerSuffixToValue.put("targetMembershipAttribute.1.name", "permissionGroupId");
      provisionerSuffixToValue.put("targetMembershipAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
      provisionerSuffixToValue.put("targetMembershipAttribute.1.translateFromGrouperProvisioningGroupField", "groupAttributeValueCache0");

      provisionerSuffixToValue.put("targetMembershipAttribute.2.name", "personId");
      provisionerSuffixToValue.put("targetMembershipAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
      provisionerSuffixToValue.put("targetMembershipAttribute.2.translateFromGrouperProvisioningEntityField", "entityAttributeValueCache0");
      
      provisionerSuffixToValue.put("groupAttributeValueCacheHas", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache0has", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache0source", "target");
      provisionerSuffixToValue.put("groupAttributeValueCache0type", "groupAttribute");
      provisionerSuffixToValue.put("groupAttributeValueCache0groupAttribute", "permissionGroupId");
      
      provisionerSuffixToValue.put("groupAttributeValueCache1has", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache1source", "target");
      provisionerSuffixToValue.put("groupAttributeValueCache1type", "groupAttribute");
      provisionerSuffixToValue.put("groupAttributeValueCache1groupAttribute", "permissionGroup");

      provisionerSuffixToValue.put("hasTargetGroupLink", "true");
      
      provisionerSuffixToValue.put("groupMatchingAttributeCount", "2");
      provisionerSuffixToValue.put("groupMatchingAttribute0name", "permissionGroup");
      provisionerSuffixToValue.put("groupMatchingAttribute1name", "permissionGroupId");
      
    }
    
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("manageEntities"), false)) {
      provisionerSuffixToValue.put("makeChangesToEntities", "true");
    }
      
    provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
    
    provisionerSuffixToValue.put("hasTargetEntityLink", "true");
    
    provisionerSuffixToValue.put("targetEntityAttribute.0.name", "personId");
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("manageEntities"), false)) {
      provisionerSuffixToValue.put("targetEntityAttribute.0.showAdvancedAttribute", "true");
      provisionerSuffixToValue.put("targetEntityAttribute.0.showAttributeCrud", "true");
      provisionerSuffixToValue.put("targetEntityAttribute.0.insert", "false");
      provisionerSuffixToValue.put("targetEntityAttribute.0.update", "false");
    }
    
    String loginId = startWithSuffixToValue.get("loginId");
    if (StringUtils.equals("script", loginId)) {
      provisionerSuffixToValue.put("targetEntityAttribute.1.translateExpressionType", "translationScript");
      provisionerSuffixToValue.put("targetEntityAttribute.1.translateExpression", startWithSuffixToValue.get("loginIdTranslationScript"));
      
      provisionerSuffixToValue.put("targetMembershipAttribute.3.translateExpressionType", "translationScript");
      provisionerSuffixToValue.put("targetMembershipAttribute.3.translateExpression", startWithSuffixToValue.get("loginIdTranslationScript"));
      
    } else if (StringUtils.equals("other", loginId)) {
      //do nothing
    } else { 
      provisionerSuffixToValue.put("targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
      provisionerSuffixToValue.put("targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", loginId);
      
      provisionerSuffixToValue.put("targetMembershipAttribute.3.translateExpressionType", "grouperProvisioningEntityField");
      provisionerSuffixToValue.put("targetMembershipAttribute.3.translateFromGrouperProvisioningEntityField", loginId);
    }
    provisionerSuffixToValue.put("targetEntityAttribute.1.name", "remedyLoginId");
    provisionerSuffixToValue.put("targetMembershipAttribute.3.name", "remedyLoginId");
  
    provisionerSuffixToValue.put("numberOfEntityAttributes", "2");
    provisionerSuffixToValue.put("numberOfMembershipAttributes", "4");
    
    provisionerSuffixToValue.put("entityAttributeValueCacheHas", "true");

    provisionerSuffixToValue.put("entityAttributeValueCache0has", "true");
    provisionerSuffixToValue.put("entityAttributeValueCache0source", "target");
    provisionerSuffixToValue.put("entityAttributeValueCache0type", "entityAttribute");
    provisionerSuffixToValue.put("entityAttributeValueCache0entityAttribute", "personId");
    
    provisionerSuffixToValue.put("entityAttributeValueCache1has", "true");
    provisionerSuffixToValue.put("entityAttributeValueCache1source", "target");
    provisionerSuffixToValue.put("entityAttributeValueCache1type", "entityAttribute");
    provisionerSuffixToValue.put("entityAttributeValueCache1entityAttribute", "remedyLoginId");
    
    provisionerSuffixToValue.put("entityMatchingAttributeCount", "2");
    provisionerSuffixToValue.put("entityMatchingAttribute0name", "remedyLoginId");
    provisionerSuffixToValue.put("entityMatchingAttribute1name", "personId");
    
    provisionerSuffixToValue.put("operateOnGrouperMemberships", "true");
    provisionerSuffixToValue.put("provisioningType", "membershipObjects");
    provisionerSuffixToValue.put("class", "edu.internet2.middleware.grouper.app.remedyV2.GrouperRemedyProvisioner");

    provisionerSuffixToValue.put("membership2AdvancedOptions", "true");
    provisionerSuffixToValue.put("membershipMatchingIdExpression", "${new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.retrieveAttributeValueString('permissionGroupId'), targetMembership.retrieveAttributeValueString('remedyLoginId'))}");
    
  }

  @Override
  public Map<String, String> screenRedraw(Map<String, String> suffixToValue,
      Set<String> suffixesUserJustChanged) {
    
    Map<String, String> result = new HashMap<>();
    
    for (String suffixUserJustChanged: suffixesUserJustChanged) {
      
      if (StringUtils.equals(suffixUserJustChanged, "remedyPattern")) {
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
    return RemedyProvisionerConfiguration.class;
  }

}
