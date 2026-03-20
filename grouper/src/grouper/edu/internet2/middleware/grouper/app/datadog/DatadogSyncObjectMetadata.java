package edu.internet2.middleware.grouper.app.datadog;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemFormElementType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemValueType;

public class DatadogSyncObjectMetadata extends GrouperProvisioningObjectMetadata {

  public DatadogSyncObjectMetadata() {
  }

  @Override
  public void initBuiltInMetadata() {
    super.initBuiltInMetadata();

    if (((DatadogProvisionerConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isDatadogAddTeamAdminMetadata()
        && !this.containsMetadataItemByName("md_adminGroupName")) {

      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataDatadogAdminGroupNameDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataDatadogAdminGroupNameLabel");
      grouperProvisioningObjectMetadataItem.setName("md_adminGroupName");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setCanChange(true);
      grouperProvisioningObjectMetadataItem.setCanUpdate(true);

      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.STRING);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.TEXT);

      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
    }
  }
}
