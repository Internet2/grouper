package edu.internet2.middleware.grouper.app.google;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GoogleProvisioningTranslator extends GrouperProvisioningTranslator {
  
  @Override
  public List<ProvisioningGroup> translateGrouperToTargetGroups(List<ProvisioningGroup> grouperProvisioningGroups,
      boolean includeDelete, boolean forCreate) {
    
    List<ProvisioningGroup> grouperTargetGroups = super.translateGrouperToTargetGroups(grouperProvisioningGroups, includeDelete, forCreate);
    
    for (ProvisioningGroup grouperProvisioningGroup : GrouperUtil.nonNull(grouperProvisioningGroups)) {
      
      for (String attributeName : new String[] { "whoCanAdd", "whoCanJoin", "whoCanViewMembership", "whoCanViewGroup", "whoCanInvite", 
          "allowExternalMembers", "whoCanPostMessage", "allowWebPosting"}) {
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
        
        GrouperProvisioningConfigurationAttribute configurationAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(attributeName);
        
        if (configurationAttribute != null) {
          if (configurationAttribute.getTranslateExpressionType() != null) {
            continue;
          }
        }
        
        grouperTargetGroup.assignAttributeValue(attributeName, newValue);
        
      }
      
    }
    return grouperTargetGroups;
    
  }

}
