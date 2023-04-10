package edu.internet2.middleware.grouper.app.duo.role;

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

public class DuoRoleProvisioningStartWith extends ProvisionerStartWithBase {

  
  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return "duoRoleCommon";
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
    
      
    provisionerSuffixToValue.put("operateOnGrouperGroups", "true");
    provisionerSuffixToValue.put("numberOfGroupAttributes", 1);

    provisionerSuffixToValue.put("customizeGroupCrud", "true");
    provisionerSuffixToValue.put("selectGroups", "false");
    
    provisionerSuffixToValue.put("targetGroupAttribute.0.name", "role");
    
    String roleAttributeValue = startWithSuffixToValue.get("roleAttributeValue");
    if (StringUtils.equals("script", roleAttributeValue)) {
      provisionerSuffixToValue.put("targetGroupAttribute.0.translateExpressionType", "translationScript");
      provisionerSuffixToValue.put("targetGroupAttribute.0.translateExpression", startWithSuffixToValue.get("roleTranslationScript"));
    } else if (StringUtils.equals("other", roleAttributeValue)) {
      //do nothing
    } else { 
      provisionerSuffixToValue.put("targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
      provisionerSuffixToValue.put("targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", roleAttributeValue);
    }
    provisionerSuffixToValue.put("targetGroupAttribute.0.name", "role");
    
    provisionerSuffixToValue.put("groupAttributeValueCacheHas", "true");
    provisionerSuffixToValue.put("groupAttributeValueCache0has", "true");
    provisionerSuffixToValue.put("groupAttributeValueCache0source", "grouper");
    provisionerSuffixToValue.put("groupAttributeValueCache0type", "groupAttribute");
    provisionerSuffixToValue.put("groupAttributeValueCache0groupAttribute", "role");
    
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
    
    provisionerSuffixToValue.put("targetEntityAttribute.1.name", "role");
    provisionerSuffixToValue.put("entityMembershipAttributeName", "role");
    provisionerSuffixToValue.put("entityMembershipAttributeValue", "groupAttributeValueCache0");
    
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
    provisionerSuffixToValue.put("entityAttributeValueCache1entityAttribute", "email");
    
    provisionerSuffixToValue.put("entityMatchingAttributeCount", "2");
    provisionerSuffixToValue.put("entityMatchingAttribute0name", "email");
    provisionerSuffixToValue.put("entityMatchingAttribute1name", "id");
    
    
    provisionerSuffixToValue.put("operateOnGrouperMemberships", "true");
    provisionerSuffixToValue.put("provisioningType", "entityAttributes");
    provisionerSuffixToValue.put("class", "edu.internet2.middleware.grouper.app.duo.role.GrouperDuoRoleProvisioner");
    
  }

  @Override
  public Map<String, String> screenRedraw(Map<String, String> suffixToValue,
      Set<String> suffixesUserJustChanged) {
    
    Map<String, String> result = new HashMap<>();
    
    for (String suffixUserJustChanged: suffixesUserJustChanged) {
      
      if (StringUtils.equals(suffixUserJustChanged, "duoRolePattern")) {
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
    return DuoRoleProvisionerConfiguration.class;
  }
  
}
