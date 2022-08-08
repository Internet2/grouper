package edu.internet2.middleware.grouper.app.azure;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 */
public class AzureProvisioningTranslator extends GrouperProvisioningTranslator {

  /**
   * auto translate metadata
   */
  @Override
  public Object attributeTranslation(Object currentValue, Map<String, Object> elVariableMap,
      boolean forCreate,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute,
      ProvisioningGroupWrapper provisioningGroupWrapper,
      ProvisioningEntityWrapper provisioningEntityWrapper) {

    Object attributeValue = super.attributeTranslation(currentValue, elVariableMap, forCreate,
        grouperProvisioningConfigurationAttribute, provisioningGroupWrapper,
        provisioningEntityWrapper);
    
    if (grouperProvisioningConfigurationAttribute == null) {
      return attributeValue;
    }

    if (forCreate && grouperProvisioningConfigurationAttribute.getTranslateExpressionTypeCreateOnly() != null) {
      return attributeValue;
    }
    
    if (grouperProvisioningConfigurationAttribute.getTranslateExpressionType() != null) {
      return attributeValue;
    }
    String expressionToUse = grouperProvisioningConfigurationAttribute == null ? null : getTargetExpressionToUse(forCreate, grouperProvisioningConfigurationAttribute);
    String translateFromGrouperProvisioningGroupField = grouperProvisioningConfigurationAttribute == null ? null : getTranslateFromGrouperProvisioningGroupField(forCreate, grouperProvisioningConfigurationAttribute);

    if (grouperProvisioningConfigurationAttribute != null 
        && !StringUtils.isBlank(translateFromGrouperProvisioningGroupField)
        && grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() 
          == GrouperProvisioningConfigurationAttributeType.group
        && StringUtils.isBlank(expressionToUse)) {
      
      if (StringUtils.equalsAny(grouperProvisioningConfigurationAttribute.getName(), 
          "assignableToRole", "azureGroupType", "allowOnlyMembersToPost", "hideGroupInOutlook", "subscribeNewGroupMembers", 
          "welcomeEmailDisabled", "resourceProvisioningOptionsTeam")) {
      
      
        GrouperAzureConfiguration grouperAzureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
        String metadataName = "md_grouper_" + grouperProvisioningConfigurationAttribute.getName();
        if (grouperAzureConfiguration.getMetadataNameToMetadataItem().containsKey(metadataName)) {
          String newValue = provisioningGroupWrapper.getGrouperProvisioningGroup().retrieveAttributeValueString(metadataName);
          if (!StringUtils.isBlank(newValue)) {
            attributeValue = newValue;
          }
        }
      }
    }
    
    return attributeValue;
  }
  
}
