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

  @Override
  public List<ProvisioningGroup> translateGrouperToTargetGroups(List<ProvisioningGroup> grouperProvisioningGroups,
      boolean includeDelete, boolean forCreate) {
    
    GrouperAzureConfiguration grouperAzureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    List<ProvisioningGroup> grouperTargetGroups = super.translateGrouperToTargetGroups(grouperProvisioningGroups, includeDelete, forCreate);
    
    for (ProvisioningGroup grouperProvisioningGroup : GrouperUtil.nonNull(grouperProvisioningGroups)) {
      
      for (String attributeName : new String[] { "assignableToRole", "azureGroupType", "allowOnlyMembersToPost", "hideGroupInOutlook", "subscribeNewGroupMembers", 
          "welcomeEmailDisabled", "resourceProvisioningOptionsTeam"}) {
        String metadataName = "md_grouper_" + attributeName;

        if (!this.getGrouperProvisioner().retrieveGrouperProvisioningObjectMetadata().getGrouperProvisioningObjectMetadataItemsByName().containsKey(metadataName)) {
          continue;
        }
        
        String newValue = grouperProvisioningGroup.retrieveAttributeValueString(metadataName);
        if (StringUtils.isBlank(newValue)) {
          continue;
        }
            
        ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningGroup == null ? null : grouperProvisioningGroup.getProvisioningGroupWrapper();
        ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getGrouperTargetGroup();
        if (grouperTargetGroup == null) {
          continue;
        }
        grouperTargetGroup.assignAttributeValue(attributeName, newValue);
        
      }
      
    }
    return grouperTargetGroups;
    
  }

  
}
