package edu.internet2.middleware.grouper.app.duo;

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

public class DuoProvisioningStartWith extends ProvisionerStartWithBase {
  
  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return "duoCommon";
  }
  
  @Override
  public void populateProvisionerConfigurationValuesFromStartWith(
      Map<String, String> startWithSuffixToValue,
      Map<String, Object> provisionerSuffixToValue) {
    
    provisionerSuffixToValue.put("duoExternalSystemConfigId", startWithSuffixToValue.get("duoExternalSystemConfigId"));
    
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
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("useGroupDescription"), true)) {
        
        provisionerSuffixToValue.put("numberOfGroupAttributes", 3);
        provisionerSuffixToValue.put("targetGroupAttribute.2.name", "description");
        provisionerSuffixToValue.put("targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField");
        provisionerSuffixToValue.put("targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "description");
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
    
    String userNameAttributeType = startWithSuffixToValue.get("entityUserName");
    if (StringUtils.isNotBlank(userNameAttributeType)) {
      if (StringUtils.equals("script", userNameAttributeType)) {
        provisionerSuffixToValue.put("targetEntityAttribute.1.translateExpressionType", "translationScript");
        provisionerSuffixToValue.put("targetEntityAttribute.1.translateExpression", startWithSuffixToValue.get("entityUserNameTranslationScript"));
      } else if (StringUtils.equals("other", userNameAttributeType)) {
        //do nothing
      } else { 
        provisionerSuffixToValue.put("targetEntityAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
        provisionerSuffixToValue.put("targetEntityAttribute.1.translateFromGrouperProvisioningEntityField", userNameAttributeType);
      }
      
      provisionerSuffixToValue.put("targetEntityAttribute.1.name", "loginId");
      
    }
      
    int numberOfEntityAttributes = 2;
    
    String entityNameSubjectAttributeType = startWithSuffixToValue.get("entityNameSubjectAttribute");
    if (StringUtils.isNotBlank(entityNameSubjectAttributeType)) {
      if (StringUtils.equals("script", entityNameSubjectAttributeType)) {
        provisionerSuffixToValue.put("targetEntityAttribute.2.translateExpressionType", "translationScript");
        provisionerSuffixToValue.put("targetEntityAttribute.2.translateExpression", startWithSuffixToValue.get("entityNameTranslationScript"));
      } else if (StringUtils.equals("other", entityNameSubjectAttributeType)) {
        //do nothing
      } else { 
        provisionerSuffixToValue.put("targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField");
        provisionerSuffixToValue.put("targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", entityNameSubjectAttributeType);
      }
      
      provisionerSuffixToValue.put("targetEntityAttribute.2.name", "name");
      numberOfEntityAttributes++;
      
    }

    String entityEmailSubjectAttributeType = startWithSuffixToValue.get("entityEmailSubjectAttribute");
    if (StringUtils.isNotBlank(entityEmailSubjectAttributeType)) {
      if (StringUtils.equals("script", entityEmailSubjectAttributeType)) {
        provisionerSuffixToValue.put("targetEntityAttribute.3.translateExpressionType", "translationScript");
        provisionerSuffixToValue.put("targetEntityAttribute.3.translateExpression", startWithSuffixToValue.get("entityEmailTranslationScript"));
      } else if (StringUtils.equals("other", entityEmailSubjectAttributeType)) {
        //do nothing
      } else { 
        provisionerSuffixToValue.put("targetEntityAttribute.3.translateExpressionType", "grouperProvisioningEntityField");
        provisionerSuffixToValue.put("targetEntityAttribute.3.translateFromGrouperProvisioningEntityField", entityEmailSubjectAttributeType);
      }
      
      provisionerSuffixToValue.put("targetEntityAttribute.3.name", "email");
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
    provisionerSuffixToValue.put("entityAttributeValueCache1entityAttribute", "loginId");
    
    provisionerSuffixToValue.put("entityMatchingAttributeCount", "2");
    provisionerSuffixToValue.put("entityMatchingAttribute0name", "loginId");
    provisionerSuffixToValue.put("entityMatchingAttribute1name", "id");
    
    provisionerSuffixToValue.put("operateOnGrouperMemberships", "true");
    provisionerSuffixToValue.put("provisioningType", "membershipObjects");
    provisionerSuffixToValue.put("class", "edu.internet2.middleware.grouper.app.duo.GrouperDuoProvisioner");
    
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledFullSyncDaemon"), true) || GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledIncrementalSyncDaemon"), true)) {
      provisionerSuffixToValue.put("showAdvanced", "true");
    }

  }

  @Override
  public Map<String, String> screenRedraw(Map<String, String> suffixToValue,
      Set<String> suffixesUserJustChanged) {
    
    Map<String, String> result = new HashMap<>();
    
    for (String suffixUserJustChanged: suffixesUserJustChanged) {
      
      if (StringUtils.equals(suffixUserJustChanged, "duoPattern")) {
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
    return DuoProvisionerConfiguration.class;
  }
}
