package edu.internet2.middleware.grouper.app.midpointProvisioning;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeDbCache;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeDbCacheSource;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeDbCacheType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeTranslationType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeValueType;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlGrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class MidPointProvisioningConfiguration extends SqlProvisioningConfiguration {

  private String dbExternalSystemConfigId;
  private String midPointTablesPrefix;
  
  private boolean midPointHasTargetAttribute = true;
  
  // comma separated list of targets
  private String midPointListOfTargets;
  
  public String getDbExternalSystemConfigId() {
    return dbExternalSystemConfigId;
  }
  
  public void setDbExternalSystemConfigId(String dbExternalSystemConfigId) {
    this.dbExternalSystemConfigId = dbExternalSystemConfigId;
  }

  public String getMidPointTablesPrefix() {
    return midPointTablesPrefix;
  }

  public void setMidPointTablesPrefix(String midPointTablesPrefix) {
    this.midPointTablesPrefix = midPointTablesPrefix;
  }
  
  
  public boolean isMidPointHasTargetAttribute() {
    return midPointHasTargetAttribute;
  }

  
  public void setMidPointHasTargetAttribute(boolean midPointHasTargetAttribute) {
    this.midPointHasTargetAttribute = midPointHasTargetAttribute;
  }

  
  public String getMidPointListOfTargets() {
    return midPointListOfTargets;
  }

  
  public void setMidPointListOfTargets(String midPointListOfTargets) {
    this.midPointListOfTargets = midPointListOfTargets;
  }
  

  @Override
  public void configureSpecificSettings() {
    
    super.configureSpecificSettings();
    
    this.dbExternalSystemConfigId = this.retrieveConfigString("dbExternalSystemConfigId", true);    
    
    //TODO validate by connecting to midpoint tables with this prefix
    this.midPointTablesPrefix = this.retrieveConfigString("midPointTablesPrefix", false);
    if (StringUtils.isBlank(this.midPointTablesPrefix)) {
      this.midPointTablesPrefix = "gr";
    }
    this.midPointHasTargetAttribute = GrouperUtil.booleanValue(this.retrieveConfigBoolean("midPointHasTargetAttribute", false), true);
    
    if (this.midPointHasTargetAttribute) {
      this.midPointListOfTargets = this.retrieveConfigString("midPointListOfTargets", false);
    }
    
    setOperateOnGrouperMemberships(true);
    setOperateOnGrouperEntities(true);
    setOperateOnGrouperGroups(true);

    setMembershipTableName(this.midPointTablesPrefix + "_mp_memberships");

    setSqlLastModifiedColumnType("long");
    setSqlLastModifiedColumnName("last_modified");
    setSqlDeletedColumnName("deleted");
    
    setMembershipGroupMatchingIdAttribute("group_id_index");
    setMembershipEntityMatchingIdAttribute("subject_id_index");
    
    setEntityTableName(this.midPointTablesPrefix + "_mp_subjects");
    
    setEntityTableIdColumn("subject_id_index");

    setGroupTableName(this.midPointTablesPrefix + "_mp_groups");
    
    setGroupAttributesTableName(this.midPointTablesPrefix + "_mp_group_attributes");
    
    setGroupAttributesGroupForeignKeyColumn("group_id_index");
    
    setGroupAttributesAttributeNameColumn("attribute_name");
    
    setGroupAttributesAttributeValueColumn("attribute_value");
    
    setGroupTableIdColumn("id_index");
    
    setEntityAttributesTableName(this.midPointTablesPrefix + "_mp_subject_attributes");
    
    setEntityAttributesEntityForeignKeyColumn("subject_id_index");
    
    setEntityAttributesAttributeNameColumn("attribute_name");
    
    setEntityAttributesAttributeValueColumn("attribute_value");
    
    setUseSeparateTableForEntityAttributes(true);
    setUseSeparateTableForGroupAttributes(true);
    
    setHasTargetGroupLink(true);
    setHasTargetEntityLink(true);
    
//    setMembershipMatchingIdExpression("${new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.retrieveAttributeValueString('"+getMembershipGroupMatchingIdAttribute()+"'), targetMembership.retrieveAttributeValueString('"+getMembershipEntityMatchingIdAttribute()+"'))}");
    setMembershipMatchingIdExpression("${new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.getAttributes().get('group_id_index').getValue(), targetMembership.getAttributes().get('subject_id_index').getValue())}");
    
    
    for (String attributeName : getTargetGroupAttributeNameToConfig().keySet()) {
      
      // if user has defined an attribute with one of these names then we need to set its storage type to entityTableColumn
      if (StringUtils.equalsAny(attributeName.toLowerCase(), "group_name", "id_index", "display_name", "description", "last_modified", "deleted")) {
        SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) getTargetGroupAttributeNameToConfig().get(attributeName);
        attributeConfig.setStorageType("groupTableColumn");
      } else {
        SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) getTargetGroupAttributeNameToConfig().get(attributeName);
        attributeConfig.setStorageType("separateAttributesTable");
      }
      
    }
    
    for (String attributeName : getTargetEntityAttributeNameToConfig().keySet()) {
      
      // if user has defined an attribute with one of these names then we need to set its storage type to groupTableColumn
      if (StringUtils.equalsAny(attributeName.toLowerCase(), "subject_id_index", "subject_id", "last_modified", "deleted")) {
        SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) getTargetEntityAttributeNameToConfig().get(attributeName);
        attributeConfig.setStorageType("entityTableColumn");
      } else {
        SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) getTargetEntityAttributeNameToConfig().get(attributeName);
        attributeConfig.setStorageType("separateAttributesTable");
      }
      
    }
    
    // group_name - group attribute
    {
      
      if (!getTargetGroupAttributeNameToConfig().containsKey("group_name")) {
        SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) GrouperUtil.newInstance(this.grouperProvisioningConfigurationAttributeClass());
        
        attributeConfig.setGrouperProvisioner(this.getGrouperProvisioner());
//        attributeConfig.setConfigIndex(0);
        attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.group);
        attributeConfig.setName("group_name");
        attributeConfig.setTranslateExpressionType(GrouperProvisioningConfigurationAttributeTranslationType.grouperProvisioningGroupField);
        attributeConfig.setTranslateFromGrouperProvisioningGroupField("name");
        attributeConfig.setStorageType("groupTableColumn");
        
        getTargetGroupAttributeNameToConfig().put(attributeConfig.getName(), attributeConfig);
      }
      
    }
    
    // id_index - group attribute
    {
      
      SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) GrouperUtil.newInstance(this.grouperProvisioningConfigurationAttributeClass());
      
      attributeConfig.setGrouperProvisioner(this.getGrouperProvisioner());
//      attributeConfig.setConfigIndex(1);
      attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.group);
      attributeConfig.setName("id_index");
      attributeConfig.setValueType(GrouperProvisioningConfigurationAttributeValueType.LONG);
      attributeConfig.setTranslateExpressionType(GrouperProvisioningConfigurationAttributeTranslationType.grouperProvisioningGroupField);
      attributeConfig.setTranslateFromGrouperProvisioningGroupField("idIndex");
      attributeConfig.setStorageType("groupTableColumn");
      
      getTargetGroupAttributeNameToConfig().put(attributeConfig.getName(), attributeConfig);
      
      List<GrouperProvisioningConfigurationAttribute> groupMatchingAttributes = new ArrayList<>();
      List<GrouperProvisioningConfigurationAttribute> groupSearchAttributes = new ArrayList<>();
      
      groupMatchingAttributes.add(attributeConfig);
      groupSearchAttributes.add(attributeConfig);
      this.setGroupSearchAttributes(groupSearchAttributes);
      this.setGroupMatchingAttributes(groupMatchingAttributes);
      this.setGroupMatchingAttributeSameAsSearchAttribute(true);
      
    }
    
    // display_name - group attribute
    {
      if (!getTargetGroupAttributeNameToConfig().containsKey("display_name")) {
        SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) GrouperUtil.newInstance(this.grouperProvisioningConfigurationAttributeClass());
        
        attributeConfig.setGrouperProvisioner(this.getGrouperProvisioner());
//        attributeConfig.setConfigIndex(2);
        attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.group);
        attributeConfig.setName("display_name");
        attributeConfig.setTranslateExpressionType(GrouperProvisioningConfigurationAttributeTranslationType.grouperProvisioningGroupField);
        attributeConfig.setTranslateFromGrouperProvisioningGroupField("displayName");
        attributeConfig.setStorageType("groupTableColumn");
        
        getTargetGroupAttributeNameToConfig().put(attributeConfig.getName(), attributeConfig);
      }
      
    }
    
    // description - group attribute
    {
      
      if (!getTargetGroupAttributeNameToConfig().containsKey("description")) {
        SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) GrouperUtil.newInstance(this.grouperProvisioningConfigurationAttributeClass());
        
        attributeConfig.setGrouperProvisioner(this.getGrouperProvisioner());
//        attributeConfig.setConfigIndex(3);
        attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.group);
        attributeConfig.setName("description");
        attributeConfig.setTranslateExpressionType(GrouperProvisioningConfigurationAttributeTranslationType.grouperProvisioningGroupField);
        attributeConfig.setTranslateFromGrouperProvisioningGroupField("description");
        attributeConfig.setStorageType("groupTableColumn");
        
        getTargetGroupAttributeNameToConfig().put(attributeConfig.getName(), attributeConfig);
      }
      
    }
    
    // last_modified - group attribute
    {
      
      if (StringUtils.isNotBlank(this.getSqlLastModifiedColumnName())) {
        SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) GrouperUtil.newInstance(this.grouperProvisioningConfigurationAttributeClass());
        
        attributeConfig.setGrouperProvisioner(this.getGrouperProvisioner());
//        attributeConfig.setConfigIndex(4);
        attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.group);
        attributeConfig.setName(this.getSqlLastModifiedColumnName());
        attributeConfig.setValueType(GrouperProvisioningConfigurationAttributeValueType.LONG);
        getTargetGroupAttributeNameToConfig().put(attributeConfig.getName(), attributeConfig);
      }
      
    }
    
    // deleted - group attribute 
    {
      
      if (StringUtils.isNotBlank(this.getSqlDeletedColumnName())) {
        SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) GrouperUtil.newInstance(this.grouperProvisioningConfigurationAttributeClass());
        
        attributeConfig.setGrouperProvisioner(this.getGrouperProvisioner());
//        attributeConfig.setConfigIndex(5);
        attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.group);
        attributeConfig.setName(this.getSqlDeletedColumnName());
        getTargetGroupAttributeNameToConfig().put(attributeConfig.getName(), attributeConfig);
      }
      
    }
    
    
    // provisioning target - group attribute - separate table
    {
      SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) GrouperUtil.newInstance(this.grouperProvisioningConfigurationAttributeClass());
      
      attributeConfig.setGrouperProvisioner(this.getGrouperProvisioner());
//      attributeConfig.setConfigIndex(4);
      attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.group);
      attributeConfig.setName("target");
      attributeConfig.setTranslateExpressionType(GrouperProvisioningConfigurationAttributeTranslationType.translationScript);
      attributeConfig.setTranslateExpression("${grouperProvisioningGroup.retrieveAttributeValue('md_grouper_midPointTarget')}");
      attributeConfig.setStorageType("separateAttributesTable");
      attributeConfig.setMultiValued(true);
      
      getTargetGroupAttributeNameToConfig().put(attributeConfig.getName(), attributeConfig);
      
    }
    
    
    // group_id_index - membership attribute
    {
      SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) GrouperUtil.newInstance(this.grouperProvisioningConfigurationAttributeClass());
      
      attributeConfig.setGrouperProvisioner(this.getGrouperProvisioner());
//      attributeConfig.setConfigIndex(0);
      attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.membership);
      attributeConfig.setName("group_id_index");
      attributeConfig.setTranslateExpressionType(GrouperProvisioningConfigurationAttributeTranslationType.grouperProvisioningGroupField);
      attributeConfig.setTranslateFromGrouperProvisioningGroupField("idIndex");
      attributeConfig.setValueType(GrouperProvisioningConfigurationAttributeValueType.LONG);
      
      getTargetMembershipAttributeNameToConfig().put(attributeConfig.getName(), attributeConfig);
      
      this.setGroupAttributeValueCacheHas(true);
      GrouperProvisioningConfigurationAttributeDbCache[] groupAttributeDbCaches = this.getGroupAttributeDbCaches();
      if (groupAttributeDbCaches[0] == null) {
        groupAttributeDbCaches[0] = new GrouperProvisioningConfigurationAttributeDbCache(this.getGrouperProvisioner(), 0, "group");
        groupAttributeDbCaches[0].setAttributeName("group_id_index");
        groupAttributeDbCaches[0].setSource(GrouperProvisioningConfigurationAttributeDbCacheSource.target);
        groupAttributeDbCaches[0].setType(GrouperProvisioningConfigurationAttributeDbCacheType.attribute);
      }
      
    }
    
    // subject_id_index - membership attribute
    {
      SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) GrouperUtil.newInstance(this.grouperProvisioningConfigurationAttributeClass());
      
      attributeConfig.setGrouperProvisioner(this.getGrouperProvisioner());
//      attributeConfig.setConfigIndex(1);
      attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.membership);
      attributeConfig.setName("subject_id_index");
      attributeConfig.setTranslateExpressionType(GrouperProvisioningConfigurationAttributeTranslationType.grouperProvisioningEntityField);
      attributeConfig.setTranslateFromGrouperProvisioningEntityField("idIndex");
      attributeConfig.setValueType(GrouperProvisioningConfigurationAttributeValueType.LONG);
      
      getTargetMembershipAttributeNameToConfig().put(attributeConfig.getName(), attributeConfig);
    }
    
    // last_modified - membership attribute
    {
      
      if (StringUtils.isNotBlank(this.getSqlLastModifiedColumnName())) {
        SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) GrouperUtil.newInstance(this.grouperProvisioningConfigurationAttributeClass());
        
        attributeConfig.setGrouperProvisioner(this.getGrouperProvisioner());
//        attributeConfig.setConfigIndex(2);
        attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.membership);
        attributeConfig.setName(this.getSqlLastModifiedColumnName());
        attributeConfig.setValueType(GrouperProvisioningConfigurationAttributeValueType.LONG);
        getTargetMembershipAttributeNameToConfig().put(attributeConfig.getName(), attributeConfig);
      }
      
    }
    
    // deleted - membership attribute 
    {
      
      if (StringUtils.isNotBlank(this.getSqlDeletedColumnName())) {
        SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) GrouperUtil.newInstance(this.grouperProvisioningConfigurationAttributeClass());
        
        attributeConfig.setGrouperProvisioner(this.getGrouperProvisioner());
//        attributeConfig.setConfigIndex(3);
        attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.membership);
        attributeConfig.setName(this.getSqlDeletedColumnName());
        getTargetMembershipAttributeNameToConfig().put(attributeConfig.getName(), attributeConfig);
      }
      
    }
    
    
    
    // subject_id_index - subject attribute
    {
      SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) GrouperUtil.newInstance(this.grouperProvisioningConfigurationAttributeClass());
      
      attributeConfig.setGrouperProvisioner(this.getGrouperProvisioner());
//      attributeConfig.setConfigIndex(0);
      attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.entity);
      attributeConfig.setName("subject_id_index");
      attributeConfig.setTranslateExpressionType(GrouperProvisioningConfigurationAttributeTranslationType.grouperProvisioningEntityField);
      attributeConfig.setTranslateFromGrouperProvisioningEntityField("idIndex");
      attributeConfig.setStorageType("entityTableColumn");
      attributeConfig.setValueType(GrouperProvisioningConfigurationAttributeValueType.LONG);
      
      getTargetEntityAttributeNameToConfig().put(attributeConfig.getName(), attributeConfig);
      
      List<GrouperProvisioningConfigurationAttribute> entityMatchingAttributes = new ArrayList<>();
      List<GrouperProvisioningConfigurationAttribute> entitySearchAttributes = new ArrayList<>();
      
      entityMatchingAttributes.add(attributeConfig);
      entitySearchAttributes.add(attributeConfig);
      this.setEntitySearchAttributes(entitySearchAttributes);
      this.setEntityMatchingAttributes(entityMatchingAttributes);
      this.setEntityMatchingAttributeSameAsSearchAttribute(true);

      this.setEntityAttributeValueCacheHas(true);
      GrouperProvisioningConfigurationAttributeDbCache[] entityAttributeDbCaches = this.getEntityAttributeDbCaches();
      if (entityAttributeDbCaches[0] == null) {
        entityAttributeDbCaches[0] = new GrouperProvisioningConfigurationAttributeDbCache(this.getGrouperProvisioner(), 0, "entity");
        entityAttributeDbCaches[0].setAttributeName("subject_id_index");
        entityAttributeDbCaches[0].setSource(GrouperProvisioningConfigurationAttributeDbCacheSource.target);
        entityAttributeDbCaches[0].setType(GrouperProvisioningConfigurationAttributeDbCacheType.attribute);
      }
            
    }
    
    // subject_id - subject attribute
    {
      if (!getTargetEntityAttributeNameToConfig().containsKey("subject_id")) {
        SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) GrouperUtil.newInstance(this.grouperProvisioningConfigurationAttributeClass());
        
        attributeConfig.setGrouperProvisioner(this.getGrouperProvisioner());
//        attributeConfig.setConfigIndex(1);
        attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.entity);
        attributeConfig.setName("subject_id");
        attributeConfig.setTranslateExpressionType(GrouperProvisioningConfigurationAttributeTranslationType.grouperProvisioningEntityField);
        attributeConfig.setTranslateFromGrouperProvisioningEntityField("subjectId");
        attributeConfig.setStorageType("entityTableColumn");
        
        getTargetEntityAttributeNameToConfig().put(attributeConfig.getName(), attributeConfig);
      }
      
    }
    
    // last_modified - subject attribute
    {
      
      if (StringUtils.isNotBlank(this.getSqlLastModifiedColumnName())) {
        SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) GrouperUtil.newInstance(this.grouperProvisioningConfigurationAttributeClass());
        
        attributeConfig.setGrouperProvisioner(this.getGrouperProvisioner());
//        attributeConfig.setConfigIndex(2);
        attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.entity);
        attributeConfig.setName(this.getSqlLastModifiedColumnName());
        attributeConfig.setValueType(GrouperProvisioningConfigurationAttributeValueType.LONG);
        getTargetEntityAttributeNameToConfig().put(attributeConfig.getName(), attributeConfig);
      }
      
    }
    
    // deleted - subject attribute 
    {
      
      if (StringUtils.isNotBlank(this.getSqlDeletedColumnName())) {
        SqlGrouperProvisioningConfigurationAttribute attributeConfig = (SqlGrouperProvisioningConfigurationAttribute) GrouperUtil.newInstance(this.grouperProvisioningConfigurationAttributeClass());
        
        attributeConfig.setGrouperProvisioner(this.getGrouperProvisioner());
//        attributeConfig.setConfigIndex(3);
        attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.entity);
        attributeConfig.setName(this.getSqlDeletedColumnName());
        getTargetEntityAttributeNameToConfig().put(attributeConfig.getName(), attributeConfig);
      }
      
    }
    
  }

}
