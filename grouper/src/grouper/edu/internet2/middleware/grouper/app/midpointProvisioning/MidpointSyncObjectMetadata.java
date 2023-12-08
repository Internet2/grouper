package edu.internet2.middleware.grouper.app.midpointProvisioning;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSyncConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemFormElementType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemValueType;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import org.apache.commons.lang3.StringUtils;

public class MidpointSyncObjectMetadata extends GrouperProvisioningObjectMetadata {
  
  /**
   * init built in metadata after the configuration and behaviors are set
   */
  public void initBuiltInMetadata() {
    super.initBuiltInMetadata();
    
    MidPointProvisioningConfiguration ldapSyncConfiguration = (MidPointProvisioningConfiguration)this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    if (StringUtils.isNotBlank(ldapSyncConfiguration.getMidPointListOfTargets()) && !this.containsMetadataItemByName("md_grouper_midPointTarget")) {
      
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();

      grouperProvisioningObjectMetadataItem.setDescriptionKey("grouperProvisioningMetadataMidpointTargetDescription");
      grouperProvisioningObjectMetadataItem.setLabelKey("grouperProvisioningMetadataMidpointTargetLabel");
      grouperProvisioningObjectMetadataItem.setName("md_grouper_midPointTarget");
      grouperProvisioningObjectMetadataItem.setShowForGroup(true);
      grouperProvisioningObjectMetadataItem.setShowForFolder(true);
      grouperProvisioningObjectMetadataItem.setValueType(GrouperProvisioningObjectMetadataItemValueType.SET);
      grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.CHECKBOX);
      grouperProvisioningObjectMetadataItem.setRequired(true);
      
      List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
      
      String midPointListOfTargets = ldapSyncConfiguration.getMidPointListOfTargets();
      String[] targetNames = GrouperUtil.split(midPointListOfTargets, ",");
      
      for (String targetName: targetNames) {
        valuesAndLabels.add(new MultiKey(targetName, targetName));
      }
      
      grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(valuesAndLabels);
      
      if (valuesAndLabels.size() > 0) {
        this.getGrouperProvisioningObjectMetadataItems().add(grouperProvisioningObjectMetadataItem);
      }
    }
    

  }

}
