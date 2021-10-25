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

    if (((GrouperAzureConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isAllowOnlyMembersToPost() &&
        !this.containsMetadataItemByName("md_grouper_allowOnlyMembersToPost")) {
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataAllowOnlyMembersToPostDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataAllowOnlyMembersToPostLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_allowOnlyMembersToPost");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
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
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.BOOLEAN);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.RADIOBUTTON);
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
      
      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      
      valuesAndLabels.add(new MultiKey("", GrouperTextContainer.textOrNull("config.defaultValueLabel") ));
      valuesAndLabels.add(new MultiKey("true", GrouperTextContainer.textOrNull("config.defaultTrueLabel")));
      valuesAndLabels.add(new MultiKey("false", GrouperTextContainer.textOrNull("config.defaultFalseLabel")));
      
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);
    }

    if (((GrouperAzureConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isResourceProvisioningOptionsTeams() &&
        !this.containsMetadataItemByName("md_grouper_resourceProvisioningOptionsTeams")) {
      
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();
      
      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataResourceProvisioningOptionsTeamsDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataResourceProvisioningOptionsTeamsLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_resourceProvisioningOptionsTeams");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
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
