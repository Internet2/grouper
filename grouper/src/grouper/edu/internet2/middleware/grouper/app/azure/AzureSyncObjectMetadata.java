package edu.internet2.middleware.grouper.app.azure;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemFormElementType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemValueType;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouperClient.collections.MultiKey;


public class AzureSyncObjectMetadata extends GrouperProvisioningObjectMetadata {
  
  public AzureSyncObjectMetadata() {
  }

  /**
   * init built in metadata after the configuration and behaviors are set
   */
  public void initBuiltInMetadata() {
    super.initBuiltInMetadata();
    
    if (((GrouperAzureConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isAzureGroupType() &&
        !this.containsMetadataItemByName("md_grouper_azureGroupType")) {
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataAzureGroupTypeDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataAzureGroupTypeLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_azureGroupType");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setShowForFolder(true);
      grouperProvisioningObjectMetadataItem.setCanUpdate(false);
      //grouperProvisioningObjectMetadataItem.setValidateUniqueValue(true);
      
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.STRING);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.DROPDOWN);
      
      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      
      //valuesAndLabels.add(new MultiKey("distributionGroup", GrouperTextContainer.textOrNull("config.azureDistributionGroup") ));
      valuesAndLabels.add(new MultiKey("security", GrouperTextContainer.textOrNull("config.azureSecurity")));
      //valuesAndLabels.add(new MultiKey("securityMailEnabled", GrouperTextContainer.textOrNull("config.azureSecurityMailEnabled")));
      valuesAndLabels.add(new MultiKey("unified", GrouperTextContainer.textOrNull("config.azureUnified")));
      valuesAndLabels.add(new MultiKey("unifiedSecurityEnabled", GrouperTextContainer.textOrNull("config.azureUnifiedSecurityEnabled")));
      
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);
      
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
    }

    if (((GrouperAzureConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isAllowOnlyMembersToPost() &&
        !this.containsMetadataItemByName("md_grouper_allowOnlyMembersToPost")) {
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataAllowOnlyMembersToPostDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataAllowOnlyMembersToPostLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_allowOnlyMembersToPost");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setShowForFolder(true);
      grouperProvisioningObjectMetadataItem.setCanUpdate(false);
      //grouperProvisioningObjectMetadataItem.setValidateUniqueValue(true);
      
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.BOOLEAN);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.RADIOBUTTON);
      
      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      
      valuesAndLabels.add(new MultiKey("", GrouperTextContainer.textOrNull("config.defaultValueLabel") ));
      valuesAndLabels.add(new MultiKey("true", GrouperTextContainer.textOrNull("config.defaultTrueLabel")));
      valuesAndLabels.add(new MultiKey("false", GrouperTextContainer.textOrNull("config.defaultFalseLabel")));
      
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);
      
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
    }
    
    if (((GrouperAzureConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isAssignableToRole() &&
        !this.containsMetadataItemByName("md_grouper_assignableToRole")) {
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataAssignableToRoleDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataAssignableToRoleLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_assignableToRole");
      grouperProvisioningObjectMetadataItem.setShowEl("${md_grouper_azureGroupType == 'security' || md_grouper_azureGroupType == 'securityMailEnabled' || md_grouper_azureGroupType == 'unifiedSecurityEnabled'}");
      
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setShowForFolder(true);
      
      grouperProvisioningObjectMetadataItem.setCanUpdate(false);
      
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.BOOLEAN);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.RADIOBUTTON);
      
      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      
      valuesAndLabels.add(new MultiKey("", GrouperTextContainer.textOrNull("config.defaultValueLabel") ));
      valuesAndLabels.add(new MultiKey("true", GrouperTextContainer.textOrNull("config.defaultTrueLabel")));
      valuesAndLabels.add(new MultiKey("false", GrouperTextContainer.textOrNull("config.defaultFalseLabel")));
      
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);
      
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
    }
    
    
    if (((GrouperAzureConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isHideGroupInOutlook() &&
        !this.containsMetadataItemByName("md_grouper_hideGroupInOutlook")) {
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataHideGroupInOutlookDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataHideGroupInOutlookLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_hideGroupInOutlook");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setShowForFolder(true);
      grouperProvisioningObjectMetadataItem.setCanUpdate(false);
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.BOOLEAN);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.RADIOBUTTON);
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
      
      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      
      valuesAndLabels.add(new MultiKey("", GrouperTextContainer.textOrNull("config.defaultValueLabel") ));
      valuesAndLabels.add(new MultiKey("true", GrouperTextContainer.textOrNull("config.defaultTrueLabel")));
      valuesAndLabels.add(new MultiKey("false", GrouperTextContainer.textOrNull("config.defaultFalseLabel")));
      
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);
    }
    
    
    if (((GrouperAzureConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isSubscribeNewGroupMembers() &&
        !this.containsMetadataItemByName("md_grouper_subscribeNewGroupMembers")) {
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataSubscribeNewGroupMembersDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataSubscribeNewGroupMembersLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_subscribeNewGroupMembers");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setShowForFolder(true);
      grouperProvisioningObjectMetadataItem.setCanUpdate(false);
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.BOOLEAN);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.RADIOBUTTON);
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
      
      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      
      valuesAndLabels.add(new MultiKey("", GrouperTextContainer.textOrNull("config.defaultValueLabel") ));
      valuesAndLabels.add(new MultiKey("true", GrouperTextContainer.textOrNull("config.defaultTrueLabel")));
      valuesAndLabels.add(new MultiKey("false", GrouperTextContainer.textOrNull("config.defaultFalseLabel")));
      
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);
    }
    
    if (((GrouperAzureConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isWelcomeEmailDisabled() &&
        !this.containsMetadataItemByName("md_grouper_welcomeEmailDisabled")) {
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataWelcomeEmailDisabledDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataWelcomeEmailDisabledLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_welcomeEmailEnabled");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setShowForFolder(true);
      grouperProvisioningObjectMetadataItem.setCanUpdate(false);
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.BOOLEAN);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.RADIOBUTTON);
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
      
      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      
      valuesAndLabels.add(new MultiKey("", GrouperTextContainer.textOrNull("config.defaultValueLabel") ));
      valuesAndLabels.add(new MultiKey("true", GrouperTextContainer.textOrNull("config.defaultTrueLabel")));
      valuesAndLabels.add(new MultiKey("false", GrouperTextContainer.textOrNull("config.defaultFalseLabel")));
      
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);
    }

    if (((GrouperAzureConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isResourceProvisioningOptionsTeam() &&
        !this.containsMetadataItemByName("md_grouper_resourceProvisioningOptionsTeam")) {
      
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();
      
      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataResourceProvisioningOptionsTeamDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataResourceProvisioningOptionsTeamLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_resourceProvisioningOptionsTeam");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setShowForFolder(true);
      grouperProvisioningObjectMetadataItem.setCanChange(false);
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.BOOLEAN);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.RADIOBUTTON);
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
      
      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      
      valuesAndLabels.add(new MultiKey("", GrouperTextContainer.textOrNull("config.defaultValueLabel") ));
      valuesAndLabels.add(new MultiKey("true", GrouperTextContainer.textOrNull("config.defaultTrueLabel")));
      valuesAndLabels.add(new MultiKey("false", GrouperTextContainer.textOrNull("config.defaultFalseLabel")));
      
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);
    }
    

  }

}
