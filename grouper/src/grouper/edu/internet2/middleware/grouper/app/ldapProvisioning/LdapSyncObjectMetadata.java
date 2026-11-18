package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemFormElementType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemValueType;


public class LdapSyncObjectMetadata extends GrouperProvisioningObjectMetadata {

  public LdapSyncObjectMetadata() {
  }

  /**
   * init built in metadata after the configuration and behaviors are set
   */
  public void initBuiltInMetadata() {
    super.initBuiltInMetadata();
    
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    if (ldapSyncConfiguration.isAllowLdapGroupDnOverride() && !this.containsMetadataItemByName("md_grouper_ldapGroupDnOverride")) {
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataLdapGroupDnOverrideDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataLdapGroupDnOverrideLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_ldapGroupDnOverride");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.STRING);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.TEXT);
      // two groups must not claim the same LDAP entry (one would take over the other's members)
      grouperProvisioningObjectMetadataItem.setValidateUniqueValue(true);
      if (ldapSyncConfiguration.isOnlyLdapGroupDnOverride()) {
        grouperProvisioningObjectMetadataItem.setRequired(true);
      }
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
    }
    

  }

  /**
   * The group dn override is set by whoever can assign this provisioner to a group (possibly a
   * delegated, non-wheel group), but it is used with Grouper's LDAP bind.  The save is the gate: a
   * sysadmin (wheel or GrouperSystem) may set any dn, anyone else only a dn under the provisioner's
   * groupSearchBaseDn.  The saving subject is the subject of the current grouper session.  Called by
   * ProvisionableGroupSave (in the caller's session) and any other API caller.
   */
  @Override
  public Map<String, String> validateMetadataInputForGroup(Map<String, Object> nameToValueFromUsersInput) {
    Map<String, String> errorMessages = super.validateMetadataInputForGroup(nameToValueFromUsersInput);
    return this.validateGroupDnOverride(nameToValueFromUsersInput, errorMessages);
  }

  /**
   * The UI group provisioning save (UiV2Provisioning.editProvisioningOnGroupSave) validates group
   * metadata with this folder method, in the logged-in user's session, so the group dn override is
   * checked here too.  Folder, member and membership metadata never carry the override, so nothing
   * changes for those.
   */
  @Override
  public Map<String, String> validateMetadataInputForFolder(Map<String, Object> nameToValueFromUsersInput) {
    Map<String, String> errorMessages = super.validateMetadataInputForFolder(nameToValueFromUsersInput);
    return this.validateGroupDnOverride(nameToValueFromUsersInput, errorMessages);
  }

  /**
   * add an error for md_grouper_ldapGroupDnOverride if the saving subject may not use that dn
   * @param nameToValueFromUsersInput metadata being saved
   * @param errorMessages errors so far, may be null
   * @return the errors (a new map if one was added and there was none)
   */
  private Map<String, String> validateGroupDnOverride(Map<String, Object> nameToValueFromUsersInput, Map<String, String> errorMessages) {
    if (nameToValueFromUsersInput == null) {
      return errorMessages;
    }
    LdapSyncConfiguration ldapSyncConfiguration = (LdapSyncConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    if (!ldapSyncConfiguration.isAllowLdapGroupDnOverride()) {
      return errorMessages;
    }
    Object overrideDn = nameToValueFromUsersInput.get(LdapGroupDnOverrideGuard.MD_GROUP_DN_OVERRIDE);
    String error = LdapGroupDnOverrideGuard.overrideErrorForSaver(overrideDn == null ? null : overrideDn.toString(),
        ldapSyncConfiguration.getGroupSearchBaseDn(), LdapGroupDnOverrideGuard.currentSubjectOrNull());
    if (error != null) {
      if (errorMessages == null) {
        errorMessages = new LinkedHashMap<String, String>();
      }
      errorMessages.put(LdapGroupDnOverrideGuard.MD_GROUP_DN_OVERRIDE, error);
    }
    return errorMessages;
  }

}
