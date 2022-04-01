package edu.internet2.middleware.grouper.app.duo.role;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemFormElementType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemValueType;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouperClient.collections.MultiKey;


public class DuoRoleSyncObjectMetadata extends GrouperProvisioningObjectMetadata {
  
  public void initBuiltInMetadata() {
    
    super.initBuiltInMetadata();

    if (!this.containsMetadataItemByName("md_grouper_duoRoles")) {
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataDuoRolesDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataDuoRolesLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_duoRoles");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setRequired(true);
      grouperProvisioningObjectMetadataItem.setValidateUniqueValue(true);
      
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.STRING);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.DROPDOWN);
      
      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      
      valuesAndLabels.add(new MultiKey("Owner", GrouperTextContainer.textOrNull("config.duoRoleOwner") ));
      valuesAndLabels.add(new MultiKey("Administrator", GrouperTextContainer.textOrNull("config.duoRoleAdministrator")));
      valuesAndLabels.add(new MultiKey("Application Manager", GrouperTextContainer.textOrNull("config.duoRoleApplicationManager")));
      valuesAndLabels.add(new MultiKey("User Manager", GrouperTextContainer.textOrNull("config.duoRoleUserManager")));
      valuesAndLabels.add(new MultiKey("Help Desk", GrouperTextContainer.textOrNull("config.duoRoleHelpDesk")));
      valuesAndLabels.add(new MultiKey("Billing", GrouperTextContainer.textOrNull("config.duoRoleBilling")));
      valuesAndLabels.add(new MultiKey("Phishing Manager", GrouperTextContainer.textOrNull("config.duoPhishingManager")));
      valuesAndLabels.add(new MultiKey("Read-only", GrouperTextContainer.textOrNull("config.duoRoleReadOnly")));
      
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);
      
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
    }
    
    if (!this.containsMetadataItemByName("md_grouper_duoEmail")) {
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataDuoEmailDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataDuoEmailLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_duoEmail");
      grouperProvisioningObjectMetadataItem.setShowForMember(true);
      grouperProvisioningObjectMetadataItem.setRequired(true);
      grouperProvisioningObjectMetadataItem.setValidateUniqueValue(true);
      
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.STRING);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.TEXT);
      
      this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
    }
    
  }

}
