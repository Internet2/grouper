package edu.internet2.middleware.grouper.app.ldapProvisioning;

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
      if (ldapSyncConfiguration.isOnlyLdapGroupDnOverride()) {
        grouperProvisioningObjectMetadataItem.setRequired(true);
      }
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
    }
    

  }

}
