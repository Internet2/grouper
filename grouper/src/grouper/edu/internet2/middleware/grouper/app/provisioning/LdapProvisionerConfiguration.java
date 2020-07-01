package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class LdapProvisionerConfiguration extends ProvisionerConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "provisioner." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(provisioner)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return LdapSync.class.getName();
  }

  private void assignCacheConfig() {
    
    {
      String provisioningType = this.retrieveAttributes().get("ldapProvisioningType").getValue();
      String hasTargetUserLink = this.retrieveAttributes().get("hasTargetUserLink").getValue();
      
      GrouperConfigurationModuleAttribute moduleAttribute = this.retrieveAttributes().get("syncMemberToId2AttributeValueFormat");
      
      if (provisioningType.equals("userAttributes") || hasTargetUserLink.equals("true") ) {
        moduleAttribute.setValue("${targetEntity.attributes['dn']}");  
      } else {
        moduleAttribute.setValue(null);
      }
    }
    
    {
      String provisioningType = this.retrieveAttributes().get("ldapProvisioningType").getValue();
      String hasTargetUserLink = this.retrieveAttributes().get("hasTargetUserLink").getValue();
      
      GrouperConfigurationModuleAttribute userAttribute = this.retrieveAttributes().get("userAttributeReferredToByGroup");
      GrouperConfigurationModuleAttribute syncMemberToId3Attribute = this.retrieveAttributes().get("syncMemberToId3AttributeValueFormat");
      
      String value = userAttribute == null ? null: userAttribute.getValue();
      value = (StringUtils.isBlank(value) && provisioningType.equals("groupMemberships") && hasTargetUserLink.equals("true")) ?
          userAttribute.getDefaultValue(): value;
      
      if (StringUtils.isBlank(value)) {      
        syncMemberToId3Attribute.setValue(null);
      } else {
        syncMemberToId3Attribute.setValue("${targetEntity.attributes['"+value+"']}");
      }
      
    }
    
    {
      GrouperConfigurationModuleAttribute userAttribute = this.retrieveAttributes().get("userSearchAttributeName");
      GrouperConfigurationModuleAttribute syncMemberFromId2Attribute = this.retrieveAttributes().get("syncMemberFromId2AttributeValueFormat");
      
      String value = userAttribute == null ? null: userAttribute.getValue();
      if (StringUtils.isBlank(value)) {      
        syncMemberFromId2Attribute.setValue(null);
      } else {
        syncMemberFromId2Attribute.setValue("${targetEntity.attributes['"+value+"']}");
      }
    }
    
    {
      GrouperConfigurationModuleAttribute subjectAttribute = this.retrieveAttributes().get("subjectApiAttributeForTargetUser");
      GrouperConfigurationModuleAttribute syncMemberFromId3Attribute = this.retrieveAttributes().get("syncMemberFromId3AttributeValueFormat");
      
      String value = subjectAttribute == null ? null: subjectAttribute.getValue();
      if (StringUtils.isBlank(value)) {      
        syncMemberFromId3Attribute.setValue(null);
      } else {
        syncMemberFromId3Attribute.setValue("${subject.attributes['"+value+"']}");
      }
    }
    
    {
      String provisioningType = this.retrieveAttributes().get("ldapProvisioningType").getValue();
      String hasTargetGroupLink = this.retrieveAttributes().get("hasTargetGroupLink").getValue();
      
      GrouperConfigurationModuleAttribute moduleAttribute = this.retrieveAttributes().get("syncGroupToId2AttributeValueFormat");
      
      if (provisioningType.equals("groupMemberships") || hasTargetGroupLink.equals("true") ) {
        moduleAttribute.setValue("${targetGroup.attributes['dn']}");  
      } else {
        moduleAttribute.setValue(null);
      }
      
    }
    
    {
      
      String provisioningType = this.retrieveAttributes().get("ldapProvisioningType").getValue();
      String hasTargetGroupLink = this.retrieveAttributes().get("hasTargetGroupLink").getValue();
      
      GrouperConfigurationModuleAttribute groupAttribute = this.retrieveAttributes().get("groupAttributeReferredToByUser");
      GrouperConfigurationModuleAttribute syncGroupToId3Attribute = this.retrieveAttributes().get("syncGroupToId3AttributeValueFormat");
      
      String value = groupAttribute == null ? null: groupAttribute.getValue();
      value = (StringUtils.isBlank(value) && provisioningType.equals("userAttributes") && hasTargetGroupLink.equals("true")) ?
          groupAttribute.getDefaultValue(): value;
      
      if (StringUtils.isBlank(value)) {      
        syncGroupToId3Attribute.setValue(null);
      } else {
        syncGroupToId3Attribute.setValue("${targetGroup.attributes['"+value+"']}");
      }
    }
    
    {
      
      GrouperConfigurationModuleAttribute groupAttribute = this.retrieveAttributes().get("groupSearchAttributeName");
      GrouperConfigurationModuleAttribute syncGroupFromId2Attribute = this.retrieveAttributes().get("syncGroupFromId2AttributeValueFormat");
      
      String value = groupAttribute == null ? null: groupAttribute.getValue();
      if (StringUtils.isBlank(value)) {      
        syncGroupFromId2Attribute.setValue(null);
      } else {
        syncGroupFromId2Attribute.setValue("${targetGroup.attributes['"+value+"']}");
      }
    }
    
  }
  
  @Override
  public void insertConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    assignCacheConfig();
    super.insertConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay);
  }

  @Override
  public void editConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    assignCacheConfig();
    super.editConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay);
  }
  
  

}
