package edu.internet2.middleware.grouper.app.truefoundry;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemFormElementType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemValueType;

public class TrueFoundrySyncObjectMetadata extends GrouperProvisioningObjectMetadata {

  public TrueFoundrySyncObjectMetadata() {
  }

  @Override
  public void initBuiltInMetadata() {
    super.initBuiltInMetadata();

    TrueFoundryProvisionerConfiguration config = (TrueFoundryProvisionerConfiguration)
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

    if (config.isTrueFoundryAddTeamManagerMetadata()) {

      String metadataName = config.getTrueFoundryTeamManagerMetadataName();

      if (!this.containsMetadataItemByName(metadataName)) {

        GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

        grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataTrueFoundryTeamManagerLabel");
        grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataTrueFoundryTeamManagerDescription");
        grouperProvisioningObjectMetadataItem.setName(metadataName);
        grouperProvisioningObjectMetadataItem.setShowForMembership(true);
        grouperProvisioningObjectMetadataItem.setCanChange(true);
        grouperProvisioningObjectMetadataItem.setCanUpdate(true);

        grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.BOOLEAN);
        grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.CHECKBOX);

        this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
      }

      String managerGroupMetadataName = config.getTrueFoundryManagerGroupMetadataName();

      if (!this.containsMetadataItemByName(managerGroupMetadataName)) {

        GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

        grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataTrueFoundryManagerGroupNameLabel");
        grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataTrueFoundryManagerGroupNameDescription");
        grouperProvisioningObjectMetadataItem.setName(managerGroupMetadataName);
        grouperProvisioningObjectMetadataItem.setShowForGroup(true);
        grouperProvisioningObjectMetadataItem.setCanChange(true);
        grouperProvisioningObjectMetadataItem.setCanUpdate(true);

        grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.STRING);
        grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.TEXT);

        this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
      }
    }
  }
}
