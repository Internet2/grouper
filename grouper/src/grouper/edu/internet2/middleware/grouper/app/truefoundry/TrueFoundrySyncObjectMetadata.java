package edu.internet2.middleware.grouper.app.truefoundry;

import java.util.Collections;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningMetadataGroupReference;
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

  /**
   * The manager group name item names another Grouper group whose members the provisioner copies
   * to TrueFoundry as team managers (as root).  A non-wheel user must be able to READ that group.
   * The UI group save calls the folder validation, ProvisionableGroupSave calls the group one,
   * so both are checked; the folder and membership forms do not show this item, so it is
   * normally absent there.
   * @param nameToValueFromUsersInput metadata being saved
   * @return the name and error message
   */
  @Override
  public Map<String, String> validateMetadataInputForFolder(Map<String, Object> nameToValueFromUsersInput) {
    return validateManagerGroupName(super.validateMetadataInputForFolder(nameToValueFromUsersInput), nameToValueFromUsersInput);
  }

  /**
   * see {@link #validateMetadataInputForFolder(Map)}
   * @param nameToValueFromUsersInput metadata being saved
   * @return the name and error message
   */
  @Override
  public Map<String, String> validateMetadataInputForGroup(Map<String, Object> nameToValueFromUsersInput) {
    return validateManagerGroupName(super.validateMetadataInputForGroup(nameToValueFromUsersInput), nameToValueFromUsersInput);
  }

  /**
   * add an error if the subject in session may not name the manager group
   * @param errorMessages errors so far (may be null)
   * @param nameToValueFromUsersInput metadata being saved
   * @return the errors
   */
  private Map<String, String> validateManagerGroupName(Map<String, String> errorMessages, Map<String, Object> nameToValueFromUsersInput) {
    TrueFoundryProvisionerConfiguration config = (TrueFoundryProvisionerConfiguration)
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    // the translator only reads the item when this is on
    if (!config.isTrueFoundryAddTeamManagerMetadata()) {
      return errorMessages;
    }
    return GrouperProvisioningMetadataGroupReference.validate(errorMessages, nameToValueFromUsersInput,
        Collections.singleton(config.getTrueFoundryManagerGroupMetadataName()));
  }
}
