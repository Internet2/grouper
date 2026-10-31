package edu.internet2.middleware.grouper.app.freshServiceAgent;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemFormElementType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemValueType;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * Provisioning metadata of the Freshservice agent provisioner.  If provisionRoles is on, then each Grouper
 * group (or a folder of groups) says if it provisions to an agent group or to an existing agent role.
 * Like the Azure group type, this can be set on a folder or group, and cannot be changed once provisioned
 * since switching would orphan the target object
 */
public class FreshAgentSyncObjectMetadata extends GrouperProvisioningObjectMetadata {

  /**
   * metadata name of the group type (group or role)
   */
  public static final String METADATA_GROUP_TYPE = "md_grouper_freshserviceGroupType";

  public FreshAgentSyncObjectMetadata() {
  }

  /**
   * init built in metadata after the configuration and behaviors are set
   */
  @Override
  public void initBuiltInMetadata() {
    super.initBuiltInMetadata();

    if (((FreshAgentConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isProvisionRoles()
        && !this.containsMetadataItemByName(METADATA_GROUP_TYPE)) {

      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataFreshserviceGroupTypeDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataFreshserviceGroupTypeLabel");
      grouperProvisioningObjectMetadataItem.setName(METADATA_GROUP_TYPE);
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setShowForFolder(true);
      grouperProvisioningObjectMetadataItem.setCanUpdate(false);

      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.STRING);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.DROPDOWN);

      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      valuesAndLabels.add(new MultiKey(FreshAgentRole.GROUP_TYPE_GROUP, GrouperTextContainer.textOrNull("config.freshserviceGroupTypeGroup")));
      valuesAndLabels.add(new MultiKey(FreshAgentRole.GROUP_TYPE_ROLE, GrouperTextContainer.textOrNull("config.freshserviceGroupTypeRole")));
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);

      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
    }
  }
}
