package edu.internet2.middleware.grouper.app.azure;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
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
          "welcomeEmailDisabled", "subscribeMembersToCalendarEventsDisabled", "resourceProvisioningOptionsTeam", "groupOwners", "groupOwnersManage", "visibility"}) {
        String metadataName = "md_grouper_" + attributeName;
        if (StringUtils.equals(attributeName, "assignableToRole")) {
          attributeName = "isAssignableToRole";
        }
        
        if (StringUtils.equals(attributeName, "visibility")) {
          
          String visibilityMetadataName = "md_grouper_visibility";
          String hiddenMembershipName = "md_grouper_hiddenMembership";
          
          if (!this.getGrouperProvisioner().retrieveGrouperProvisioningObjectMetadata().getGrouperProvisioningObjectMetadataItemsByName().containsKey(visibilityMetadataName) 
                || !this.getGrouperProvisioner().retrieveGrouperProvisioningObjectMetadata().getGrouperProvisioningObjectMetadataItemsByName().containsKey(hiddenMembershipName)) {
            continue;
          }
          
          String newValueVisibility = grouperProvisioningGroup.retrieveAttributeValueString(visibilityMetadataName);
          String newValueHiddenMembership = grouperProvisioningGroup.retrieveAttributeValueString(hiddenMembershipName);
          if (StringUtils.isBlank(newValueVisibility) && StringUtils.isBlank(newValueHiddenMembership)) {
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
          
          String visibilityValue = null;
          if(GrouperUtil.booleanValue(newValueHiddenMembership, false)){ 
            visibilityValue = "HiddenMembership";
          } else {
            visibilityValue = newValueVisibility;
          }
          
          grouperTargetGroup.assignAttributeValue(attributeName, visibilityValue);
          
        } else {
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
          
          if (StringUtils.equals(attributeName, "groupOwners")) {
            Set<String> owners = GrouperUtil.splitTrimToSet(newValue, ",");
            grouperTargetGroup.assignAttributeValue(attributeName, owners);
          } else {          
            grouperTargetGroup.assignAttributeValue(attributeName, newValue);
          }
        }
        
      }
      
      Set<String> owners = (Set<String>)grouperProvisioningGroup.retrieveAttributeValueSet("groupOwners");
      
      if (GrouperUtil.length(owners) > 0) {
        boolean needsAdjustment = false;
        for (String owner: owners) {
          if (StringUtils.startsWith(owner, "http")) {
            needsAdjustment = true;
            break;
          }
        }
        if (needsAdjustment) {
          Set<String> adjustedOwners = new HashSet<String>();
          for (String owner: owners) {
            if (StringUtils.startsWith(owner, "http")) {
              String uuid = GrouperUtil.suffixAfterChar(owner, '/');
              adjustedOwners.add(uuid);
            } else {
              adjustedOwners.add(owner);
            }
          }
        }
      }
    }
    return grouperTargetGroups;
    
  }

  
}
