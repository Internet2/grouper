/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemFormElementType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemValueType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslatorBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;


/**
 * sync to ldap
 */
public class LdapSync extends GrouperProvisioner {
  
  /**
   * log object
   */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(LdapSync.class);

  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return LdapProvisioningTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfigurationBase> grouperProvisioningConfigurationClass() {
    return LdapSyncConfiguration.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningTranslatorBase> grouperTranslatorClass() {
    return LdapProvisioningTranslator.class;
  }

  public LdapSyncConfiguration retrieveLdapProvisioningConfiguration() {
    return (LdapSyncConfiguration)this.retrieveGrouperProvisioningConfiguration();
  }

  @Override
  protected Class<? extends GrouperProvisioningConfigurationValidation> grouperProvisioningConfigurationValidationClass() {
    return LdapSyncConfigurationValidation.class;
  } 
  
  private GrouperProvisioningObjectMetadata grouperProvisioningObjectMetadata;
  
  @Override
  public GrouperProvisioningObjectMetadata retrieveGrouperProvisioningObjectMetadata() {
    if (grouperProvisioningObjectMetadata == null) {
      grouperProvisioningObjectMetadata = new GrouperProvisioningObjectMetadata();
      
      //TODO remove it after testing
      List<GrouperProvisioningObjectMetadataItem> items = new ArrayList<GrouperProvisioningObjectMetadataItem>();
      
      GrouperProvisioningObjectMetadataItem item = new GrouperProvisioningObjectMetadataItem();
      item.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.TEXT);
      item.setDescriptionKey("config.LdapGrouperExternalSystem.attribute.url.label");
      item.setLabelKey("config.LdapGrouperExternalSystem.attribute.user.label");
      item.setName("testName");
      item.setRequired(true);
      item.setShowForGroup(true);
      item.setShowForFolder(true);
      item.setShowForMember(true);
      item.setShowForMembership(true);
      item.setValueType(GrouperProvisioningObjectMetadataItemValueType.STRING);
      
      GrouperProvisioningObjectMetadataItem item1 = new GrouperProvisioningObjectMetadataItem();
      item1.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.TEXT);
      item1.setLabelKey("only.for.folder");
      item1.setDescriptionKey("only.for.folder");
      item1.setName("onlyForFolder");
      item1.setRequired(true);
      item1.setShowForFolder(true);
      item1.setShowForMember(true);
      item1.setShowForMembership(true);
      item1.setValueType(GrouperProvisioningObjectMetadataItemValueType.STRING);
      
      GrouperProvisioningObjectMetadataItem item2 = new GrouperProvisioningObjectMetadataItem();
      item2.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.TEXTAREA);
      item2.setLabelKey("textArea label");
      item2.setDescriptionKey("textArea description");
      item2.setName("textAreaName");
      item2.setRequired(true);
      item2.setShowForFolder(true);
      item2.setShowForMember(true);
      item2.setShowForMembership(true);
      item2.setValueType(GrouperProvisioningObjectMetadataItemValueType.STRING);
      
      GrouperProvisioningObjectMetadataItem item3 = new GrouperProvisioningObjectMetadataItem();
      item3.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.DROPDOWN);
      item3.setLabelKey("dropdown label");
      item3.setDescriptionKey("dropdown description");
      item3.setName("dropdownName");
      item3.setRequired(true);
      item3.setShowForFolder(true);
      item3.setShowForMember(true);
      item3.setShowForMembership(true);
      item3.setValueType(GrouperProvisioningObjectMetadataItemValueType.STRING);
      
      List<MultiKey> keysAndLabelsForDropdown = new ArrayList<>();
      keysAndLabelsForDropdown.add(new MultiKey("", ""));
      keysAndLabelsForDropdown.add(new MultiKey("name1", "value1"));
      keysAndLabelsForDropdown.add(new MultiKey("name2", "value2"));
      keysAndLabelsForDropdown.add(new MultiKey("name3", "value3"));
      item3.setKeysAndLabelsForDropdown(keysAndLabelsForDropdown);
      
      items.add(item);
      items.add(item1);
      items.add(item2);
      items.add(item3);
      
      grouperProvisioningObjectMetadata.setGrouperProvisioningObjectMetadataItems(items);
      
    }
    return grouperProvisioningObjectMetadata;
  }

}
