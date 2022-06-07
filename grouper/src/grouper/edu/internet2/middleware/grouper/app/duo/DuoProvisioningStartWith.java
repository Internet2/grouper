package edu.internet2.middleware.grouper.app.duo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
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
    
    if ( StringUtils.isNotBlank(startWithSuffixToValue.get("userAttributesType")) && !StringUtils.equals(startWithSuffixToValue.get("userAttributesType"), "core")) {
      provisionerSuffixToValue.put("entityResolver.entityAttributesNotInSubjectSource", "true");
    }
    
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("manageGroups"), false)) {
      
      provisionerSuffixToValue.put("operateOnGrouperGroups", "true");
      provisionerSuffixToValue.put("numberOfGroupAttributes", 1);
      
      provisionerSuffixToValue.put("targetGroupAttribute.0.name", startWithSuffixToValue.get("groupNameAttributeValue"));
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("useGroupDescription"), true)) {
        provisionerSuffixToValue.put("numberOfGroupAttributes", 2);
        provisionerSuffixToValue.put("targetGroupAttribute.1.name", "description");
      }
      
    }
    
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("manageEntities"), false)) {
      
      provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
      int numberOfEntityAttributes = 1;
      
      provisionerSuffixToValue.put("targetEntityAttribute.0.name", startWithSuffixToValue.get("entityUserName"));
      
      if (StringUtils.isNotBlank(startWithSuffixToValue.get("entityNameSubjectAttribute"))) {
        numberOfEntityAttributes++;
        provisionerSuffixToValue.put("targetEntityAttribute.1.name", startWithSuffixToValue.get("entityNameSubjectAttribute"));
      }
      
      if (StringUtils.isNotBlank(startWithSuffixToValue.get("entityFirstNameSubjectAttribute"))) {
        numberOfEntityAttributes++;
        provisionerSuffixToValue.put("targetEntityAttribute.2.name", startWithSuffixToValue.get("entityFirstNameSubjectAttribute"));
      }
      
      if (StringUtils.isNotBlank(startWithSuffixToValue.get("entityEmailSubjectAttribute"))) {
        numberOfEntityAttributes++;
        provisionerSuffixToValue.put("targetEntityAttribute.3.name", startWithSuffixToValue.get("entityEmailSubjectAttribute"));
      }
      
      provisionerSuffixToValue.put("numberOfEntityAttributes", numberOfEntityAttributes);
      
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
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "manageGroupsReadonlyEntities")) {
          result.put("manageGroups", "true");
          result.put("manageEntities", "false");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "manageEntities")) {
          result.put("manageGroups", "false");
          result.put("manageEntities", "true");
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
    
  }

}
