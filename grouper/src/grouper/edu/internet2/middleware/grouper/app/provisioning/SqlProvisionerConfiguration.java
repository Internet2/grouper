package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlMembershipProvisioner;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class SqlProvisionerConfiguration extends ProvisionerConfiguration {

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
    return SqlMembershipProvisioner.class.getName();
  }


  private void assignCacheConfig() {

    {
      String hasTargetUserLink = this.retrieveAttributes().get("hasTargetUserLink").getValue();
      String userPrimaryKey = this.retrieveAttributes().get("userPrimaryKey").getValue();
      
      GrouperConfigurationModuleAttribute moduleAttribute = this.retrieveAttributes().get("syncMemberToId2AttributeValueFormat");
      
      if (hasTargetUserLink.equals("true") && !StringUtils.isBlank(userPrimaryKey) ) {
        // TODO make sure no single quotes.  check here and other places vars are inserted in scripts
        moduleAttribute.setValue("${targetEntity.attributes['" + userPrimaryKey + "']}");
        
      } else {
        moduleAttribute.setValue(null);
      }
    }

    {
      
      String hasTargetUserLink = this.retrieveAttributes().get("hasTargetUserLink").getValue();

      GrouperConfigurationModuleAttribute membershipUserColumn = this.retrieveAttributes().get("membershipUserColumn");
      GrouperConfigurationModuleAttribute membershipUserValueFormat = this.retrieveAttributes().get("membershipUserValueFormat");
      GrouperConfigurationModuleAttribute syncMemberToId3Attribute = this.retrieveAttributes().get("syncMemberToId3AttributeValueFormat");
      
      if (hasTargetUserLink.equals("true") ) {
        if (!StringUtils.isBlank(membershipUserValueFormat.getValue())) {
          syncMemberToId3Attribute.setValue(membershipUserValueFormat.getValue());
        } else {
          syncMemberToId3Attribute.setValue("${targetMembership.attributes['" + membershipUserColumn.getValue() + "']}");
        }
      } else {
        syncMemberToId3Attribute.setValue(null);
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
      String hasTargetGroupLink = this.retrieveAttributes().get("hasTargetGroupLink").getValue();
      
      GrouperConfigurationModuleAttribute groupAttribute = this.retrieveAttributes().get("groupPrimaryKey");
      
      String value = groupAttribute == null ? null: groupAttribute.getValue();
      
      GrouperConfigurationModuleAttribute moduleAttribute = this.retrieveAttributes().get("syncGroupToId2AttributeValueFormat");

      if (hasTargetGroupLink.equals("true") && StringUtils.isBlank(value)) {      
        moduleAttribute.setValue(null);
      } else {
        moduleAttribute.setValue("${targetGroup.attributes['"+value+"']}");
      }
    }
    
    {
      String hasTargetGroupLink = this.retrieveAttributes().get("hasTargetGroupLink").getValue();

      GrouperConfigurationModuleAttribute membershipGroupColumn = this.retrieveAttributes().get("membershipGroupColumn");
      GrouperConfigurationModuleAttribute membershipGroupValueFormat = this.retrieveAttributes().get("membershipGroupValueFormat");
      GrouperConfigurationModuleAttribute syncGroupToId3Attribute = this.retrieveAttributes().get("syncGroupToId3AttributeValueFormat");
      
      if (hasTargetGroupLink.equals("true") ) {
        if (!StringUtils.isBlank(membershipGroupValueFormat.getValue())) {
          syncGroupToId3Attribute.setValue(membershipGroupValueFormat.getValue());
        } else {
          syncGroupToId3Attribute.setValue("${targetMembership.attributes['" + membershipGroupColumn.getValue() + "']}");
        }
      } else {
        syncGroupToId3Attribute.setValue(null);
      }
    }

    {
      String hasTargetGroupLink = this.retrieveAttributes().get("hasTargetGroupLink").getValue();

      String groupSearchAttributeName = this.retrieveAttributes().get("groupSearchAttributeName").getValue();
      String groupSearchAttributeValueFormat = this.retrieveAttributes().get("groupSearchAttributeValueFormat").getValue();

      GrouperConfigurationModuleAttribute moduleAttribute = this.retrieveAttributes().get("syncGroupFromId2AttributeValueFormat");
      
      if (hasTargetGroupLink.equals("true") ) {
        if (!StringUtils.isBlank(groupSearchAttributeValueFormat)) {
          moduleAttribute.setValue(groupSearchAttributeValueFormat);  
        } else {
          moduleAttribute.setValue("${targetGroup.attributes['"+groupSearchAttributeName+"']}");
        }
      } else {
        moduleAttribute.setValue(null);
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
