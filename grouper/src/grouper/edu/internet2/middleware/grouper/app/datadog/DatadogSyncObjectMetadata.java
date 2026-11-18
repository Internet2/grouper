package edu.internet2.middleware.grouper.app.datadog;

import java.util.Collections;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningMetadataGroupReference;
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

  /**
   * name of the group metadata item that holds the name of the group whose members become team admins
   */
  private static final String ADMIN_GROUP_METADATA_NAME = "md_adminGroupName";

  /**
   * The admin group name item names another Grouper group whose members the provisioner copies
   * to Datadog as team admins (as root).  A non-wheel user must be able to READ that group.
   * The UI group save calls the folder validation, ProvisionableGroupSave calls the group one,
   * so both are checked; the folder and membership forms do not show this item, so it is
   * normally absent there.
   * @param nameToValueFromUsersInput metadata being saved
   * @return the name and error message
   */
  @Override
  public Map<String, String> validateMetadataInputForFolder(Map<String, Object> nameToValueFromUsersInput) {
    return validateAdminGroupName(super.validateMetadataInputForFolder(nameToValueFromUsersInput), nameToValueFromUsersInput);
  }

  /**
   * see {@link #validateMetadataInputForFolder(Map)}
   * @param nameToValueFromUsersInput metadata being saved
   * @return the name and error message
   */
  @Override
  public Map<String, String> validateMetadataInputForGroup(Map<String, Object> nameToValueFromUsersInput) {
    return validateAdminGroupName(super.validateMetadataInputForGroup(nameToValueFromUsersInput), nameToValueFromUsersInput);
  }

  /**
   * add an error if the subject in session may not name the admin group
   * @param errorMessages errors so far (may be null)
   * @param nameToValueFromUsersInput metadata being saved
   * @return the errors
   */
  private Map<String, String> validateAdminGroupName(Map<String, String> errorMessages, Map<String, Object> nameToValueFromUsersInput) {
    // the translator only reads the item when this is on
    if (!((DatadogProvisionerConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()).isDatadogAddTeamAdminMetadata()) {
      return errorMessages;
    }
    return GrouperProvisioningMetadataGroupReference.validate(errorMessages, nameToValueFromUsersInput,
        Collections.singleton(ADMIN_GROUP_METADATA_NAME));
  }
}
