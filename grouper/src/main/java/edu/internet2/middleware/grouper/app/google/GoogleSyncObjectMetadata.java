package edu.internet2.middleware.grouper.app.google;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemFormElementType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemValueType;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouperClient.collections.MultiKey;


public class GoogleSyncObjectMetadata extends GrouperProvisioningObjectMetadata {

  public GoogleSyncObjectMetadata() {}

  /**
   * init built in metadata after the configuration and behaviors are set
   */
  public void initBuiltInMetadata() {
    super.initBuiltInMetadata();

    if (((GrouperGoogleConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isWhoCanAdd() &&
        !this.containsMetadataItemByName("md_grouper_whoCanAdd")) {
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataWhoCanAddDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataWhoCanAddLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_whoCanAdd");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.STRING);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.DROPDOWN);
      
      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      
      valuesAndLabels.add(new MultiKey("ALL_MEMBERS_CAN_ADD", "ALL_MEMBERS_CAN_ADD"));
      valuesAndLabels.add(new MultiKey("ALL_MANAGERS_CAN_ADD", "ALL_MANAGERS_CAN_ADD"));
      valuesAndLabels.add(new MultiKey("ALL_OWNERS_CAN_ADD", "ALL_OWNERS_CAN_ADD"));
      valuesAndLabels.add(new MultiKey("NONE_CAN_ADD", "NONE_CAN_ADD"));
      
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);
      
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
    }
    
    
    if (((GrouperGoogleConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isWhoCanJoin() &&
        !this.containsMetadataItemByName("md_grouper_whoCanJoin")) {
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataWhoCanJoinDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataWhoCanJoinLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_whoCanJoin");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.STRING);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.DROPDOWN);
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
      
      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      
      valuesAndLabels.add(new MultiKey("ANYONE_CAN_JOIN", "ANYONE_CAN_JOIN"));
      valuesAndLabels.add(new MultiKey("ALL_IN_DOMAIN_CAN_JOIN", "ALL_IN_DOMAIN_CAN_JOIN"));
      valuesAndLabels.add(new MultiKey("INVITED_CAN_JOIN", "INVITED_CAN_JOIN"));
      valuesAndLabels.add(new MultiKey("CAN_REQUEST_TO_JOIN", "CAN_REQUEST_TO_JOIN"));
      
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);
    }
    
    
    if (((GrouperGoogleConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isWhoCanViewMembership() &&
        !this.containsMetadataItemByName("md_grouper_whoCanViewMembership")) {
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataWhoCanViewMembershipDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataWhoCanViewMembershipLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_whoCanViewMembership");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.STRING);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.DROPDOWN);
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
      
      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      
      valuesAndLabels.add(new MultiKey("ALL_IN_DOMAIN_CAN_VIEW", "ALL_IN_DOMAIN_CAN_VIEW"));
      valuesAndLabels.add(new MultiKey("ALL_MEMBERS_CAN_VIEW", "ALL_MEMBERS_CAN_VIEW"));
      valuesAndLabels.add(new MultiKey("ALL_MANAGERS_CAN_VIEW", "ALL_MANAGERS_CAN_VIEW"));
      
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);
    }

    if (((GrouperGoogleConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isWhoCanInvite() &&
        !this.containsMetadataItemByName("md_grouper_whoCanInvite")) {
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();
      
      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataWhoCanInviteDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataWhoCanInviteLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_whoCanInvite");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.STRING);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.DROPDOWN);
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
      
      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      
      valuesAndLabels.add(new MultiKey("ALL_MEMBERS_CAN_INVITE", "ALL_MEMBERS_CAN_INVITE"));
      valuesAndLabels.add(new MultiKey("ALL_MANAGERS_CAN_INVITE", "ALL_MANAGERS_CAN_INVITE"));
      valuesAndLabels.add(new MultiKey("ALL_OWNERS_CAN_INVITE", "ALL_OWNERS_CAN_INVITE"));
      valuesAndLabels.add(new MultiKey("NONE_CAN_INVITE", "NONE_CAN_INVITE"));
      
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);
    }
    
    if (((GrouperGoogleConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isWhoCanViewGroup() &&
        !this.containsMetadataItemByName("md_grouper_whoCanViewGroup")) {
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataWhoCanViewGroupDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataWhoCanViewGroupLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_whoCanViewGroup");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.STRING);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.DROPDOWN);
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
      
      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      
      valuesAndLabels.add(new MultiKey("ANYONE_CAN_VIEW", "ANYONE_CAN_VIEW"));
      valuesAndLabels.add(new MultiKey("ALL_IN_DOMAIN_CAN_VIEW", "ALL_IN_DOMAIN_CAN_VIEW"));
      valuesAndLabels.add(new MultiKey("ALL_MEMBERS_CAN_VIEW", "ALL_MEMBERS_CAN_VIEW"));
      valuesAndLabels.add(new MultiKey("ALL_MANAGERS_CAN_VIEW", "ALL_MANAGERS_CAN_VIEW"));
      
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);
    }

    if (((GrouperGoogleConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isWhoCanPostMessage() &&
        !this.containsMetadataItemByName("md_grouper_whoCanPostMessage")) {
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();
      
      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataWhoCanPostMessageDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataWhoCanPostMessageLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_whoCanPostMessage");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.STRING);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.DROPDOWN);
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
      
      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      
      valuesAndLabels.add(new MultiKey("NONE_CAN_POST", "NONE_CAN_POST"));
      valuesAndLabels.add(new MultiKey("ALL_MANAGERS_CAN_POST", "ALL_MANAGERS_CAN_POST"));
      valuesAndLabels.add(new MultiKey("ALL_MEMBERS_CAN_POST", "ALL_MEMBERS_CAN_POST"));
      valuesAndLabels.add(new MultiKey("ALL_OWNERS_CAN_POST", "ALL_OWNERS_CAN_POST"));
      valuesAndLabels.add(new MultiKey("ALL_IN_DOMAIN_CAN_POST", "ALL_IN_DOMAIN_CAN_POST"));
      valuesAndLabels.add(new MultiKey("ANYONE_CAN_POST", "ANYONE_CAN_POST"));
      
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);
    }

    if (((GrouperGoogleConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isAllowExternalMembers() &&
        !this.containsMetadataItemByName("md_grouper_allowExternalMembers")) {
      
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();
      
      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataAllowExternalMembersDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataAllowExternalMembersLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_allowExternalMembers");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setDefaultValue(false);
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.BOOLEAN);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.RADIOBUTTON);
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
      
      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      
      String falseLabel = GrouperTextContainer.textOrNull("config.defaultFalseLabel");
      valuesAndLabels.add(new MultiKey("", GrouperTextContainer.textOrNull("config.defaultValueLabel")+" (" + falseLabel + ")"));
      
      valuesAndLabels.add(new MultiKey("true", GrouperTextContainer.textOrNull("config.defaultTrueLabel")));
      valuesAndLabels.add(new MultiKey("false", GrouperTextContainer.textOrNull("config.defaultFalseLabel")));
      
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);
    }

    if (((GrouperGoogleConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isAllowWebPosting() &&
        !this.containsMetadataItemByName("md_grouper_allowWebPosting")) {
      
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();
      
      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataAllowWebPostingDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataAllowWebPostingLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_allowWebPosting");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setDefaultValue(false);
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.BOOLEAN);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.RADIOBUTTON);
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
      
      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      
      String falseLabel = GrouperTextContainer.textOrNull("config.defaultFalseLabel");
      valuesAndLabels.add(new MultiKey("", GrouperTextContainer.textOrNull("config.defaultValueLabel")+" (" + falseLabel + ")"));
      
      valuesAndLabels.add(new MultiKey("true", GrouperTextContainer.textOrNull("config.defaultTrueLabel")));
      valuesAndLabels.add(new MultiKey("false", GrouperTextContainer.textOrNull("config.defaultFalseLabel")));
      
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);
    }
    

  }
}
