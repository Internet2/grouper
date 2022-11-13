package edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisioner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class ExampleWsProvisioningStartWith extends ProvisionerStartWithBase {
  
  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return "exampleWs";
  }

  @Override
  public void populateProvisionerConfigurationValuesFromStartWith(
      Map<String, String> startWithSuffixToValue,
      Map<String, Object> provisionerSuffixToValue) {
    
    String groupTranslationValue = startWithSuffixToValue.get("groupTranslation");
    
    if (StringUtils.equalsAny(groupTranslationValue, "extension", "idIndex", "idIndexString", "name", "id")) {
      provisionerSuffixToValue.put("targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
      provisionerSuffixToValue.put("targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", groupTranslationValue);
      
      provisionerSuffixToValue.put("targetMembershipAttribute.1.translateExpressionType", "grouperProvisioningGroupField");
      provisionerSuffixToValue.put("targetMembershipAttribute.1.translateFromGrouperProvisioningGroupField", groupTranslationValue);
      
    } else if (StringUtils.equalsAny(groupTranslationValue, "script")) {
      provisionerSuffixToValue.put("targetGroupAttribute.0.translateExpressionType", "translationScript");
      provisionerSuffixToValue.put("targetMembershipAttribute.1.translateExpressionType", "translationScript");
    }
    
    String entityTranslationValue = startWithSuffixToValue.get("entityTranslation");
    
    if (StringUtils.equalsAny(entityTranslationValue, "subjectId", "subjectIdentifier0", "subjectIdentifier1", "subjectIdentifier2", "idIndex")) {
      provisionerSuffixToValue.put("targetMembershipAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
      provisionerSuffixToValue.put("targetMembershipAttribute.0.translateFromGrouperProvisioningEntityField", entityTranslationValue);
      
    } else if (StringUtils.equalsAny(entityTranslationValue, "script")) {
      provisionerSuffixToValue.put("targetMembershipAttribute.0.translateExpressionType", "translationScript");
    }
    
    provisionerSuffixToValue.put("customizeGroupCrud", "true");
    provisionerSuffixToValue.put("customizeMembershipCrud", "true");
    provisionerSuffixToValue.put("replaceMemberships", "true");
    provisionerSuffixToValue.put("insertMemberships", "false");
    provisionerSuffixToValue.put("deleteMemberships", "false");
    provisionerSuffixToValue.put("selectMemberships", "false");
    provisionerSuffixToValue.put("deleteGroups", "false");
    provisionerSuffixToValue.put("insertGroups", "false");
    provisionerSuffixToValue.put("membership2AdvancedOptions", "true");
    provisionerSuffixToValue.put("membershipMatchingIdExpression", "${new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.retrieveAttributeValueString('role'), targetMembership.retrieveAttributeValueString('netID'))}");
    provisionerSuffixToValue.put("numberOfGroupAttributes", "1");
    provisionerSuffixToValue.put("numberOfMembershipAttributes", "2");
    provisionerSuffixToValue.put("operateOnGrouperGroups", "true");
    provisionerSuffixToValue.put("operateOnGrouperMemberships", "true");
    provisionerSuffixToValue.put("provisioningType", "membershipObjects");
    provisionerSuffixToValue.put("selectGroups", "false");
    provisionerSuffixToValue.put("selectMemberships", "false");
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledFullSyncDaemon"), true) 
        || GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledIncrementalSyncDaemon"), true)) {
      provisionerSuffixToValue.put("showAdvanced", "true");
    }
  
    provisionerSuffixToValue.put("targetGroupAttribute.0.name", "role");
   
    provisionerSuffixToValue.put("targetMembershipAttribute.0.name", "netID");
 
    provisionerSuffixToValue.put("targetMembershipAttribute.1.name", "role");
  
    provisionerSuffixToValue.put("updateGroups", "false");
    
    provisionerSuffixToValue.put("exampleWsSource", startWithSuffixToValue.get("exampleWsSource"));
    provisionerSuffixToValue.put("class", "edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisioner.GrouperExampleWsProvisioner");
    provisionerSuffixToValue.put("exampleWsExternalSystemConfigId", startWithSuffixToValue.get("exampleWsExternalSystemConfigId"));
    
    
  }

  @Override
  public Map<String, String> screenRedraw(Map<String, String> suffixToValue,
      Set<String> suffixesUserJustChanged) {
    return new HashMap<>();
  }

}
