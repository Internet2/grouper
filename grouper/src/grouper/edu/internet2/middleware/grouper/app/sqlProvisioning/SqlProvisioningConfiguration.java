package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;


public class SqlProvisioningConfiguration extends GrouperProvisioningConfiguration {
  
  @Override
  protected Class<? extends GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributeClass() {
    return SqlGrouperProvisioningConfigurationAttribute.class;
  }

//  private String groupAttributeTableAttributeNameIsGroupMatchingId;
  
  
  private String dbExternalSystemConfigId;
  
  private String membershipTableName;

//  private String membershipUserColumn;
  
  private String membershipGroupForeignKeyColumn;
  private String membershipEntityForeignKeyColumn;
  
//  private String membershipUserValueFormat;
  
//  private String membershipGroupColumn;
  
//  private String membershipGroupValueFormat;
  
//  private String membershipCreationNumberOfAttributes;
//  
//  private String membershipCreationColumnTemplate_attr_0;
//  
//  private String membershipCreationColumnTemplate_val_0;
//  
//  private String membershipCreationColumnTemplate_attr_1;
//  
//  private String membershipCreationColumnTemplate_val_1;
//  
  /** 
   * table name for group table
   */
  private String groupTableName;
  
  private String groupAttributesTableName;
  
  private String groupAttributesGroupForeignKeyColumn;
  
  private String groupAttributesAttributeNameColumn;
  
  private String groupAttributesAttributeValueColumn;
  
  private String groupAttributesLastModifiedColumn;
  private String groupAttributesLastModifiedColumnType;
  
  private String entityAttributesTableName;
  
  private String entityAttributesEntityForeignKeyColumn;
  
  private String entityAttributesAttributeNameColumn;
  
  private String entityAttributesAttributeValueColumn;
  
  private String entityAttributesLastModifiedColumn;
  private String entityAttributesLastModifiedColumnType;
  
  private boolean useSeparateTableForGroupAttributes;
  
  private boolean useSeparateTableForEntityAttributes;
  
  /**
   * columns in the group table
   */
//  private String groupAttributeNames;
  
  public boolean isUseSeparateTableForEntityAttributes() {
    return useSeparateTableForEntityAttributes;
  }




  public void setUseSeparateTableForEntityAttributes(boolean useSeparateTableForEntityAttributes) {
    this.useSeparateTableForEntityAttributes = useSeparateTableForEntityAttributes;
  }




  /**
   * if there is a group attribute table (like ldap), this is the table name
   */
//  private String groupAttributeTableName;
  
  public boolean isUseSeparateTableForGroupAttributes() {
    return useSeparateTableForGroupAttributes;
  }




  public void setUseSeparateTableForGroupAttributes(boolean useSeparateTableForGroupAttributes) {
    this.useSeparateTableForGroupAttributes = useSeparateTableForGroupAttributes;
  }

  /**
   * if group table has one primary key, this is it
   */
  private String groupTableIdColumn;

  /**
   * single column in group attribute table that links back to group pk
   */
//  private String groupAttributeTableForeignKeyToGroup;
  
  /**
   * primary key column of attribute table
   */
//  private String groupAttributeTableIdColumn;
  
  /**
   * group attribute table has a column which is the attribute name
   */
//  private String groupAttributeTableAttributeNameColumn;
  
  /**
   * group attribute table has attribute value column
   */
//  private String groupAttributeTableAttributeValueColumn;
  
  
  
  /**
   * table name for group table
   * @return
   */
  public String getGroupTableName() {
    return groupTableName;
  }



  
  public String getMembershipGroupForeignKeyColumn() {
    return membershipGroupForeignKeyColumn;
  }




  
  public void setMembershipGroupForeignKeyColumn(String membershipGroupForeignKeyColumn) {
    this.membershipGroupForeignKeyColumn = membershipGroupForeignKeyColumn;
  }




  
  public String getMembershipEntityForeignKeyColumn() {
    return membershipEntityForeignKeyColumn;
  }




  
  public void setMembershipEntityForeignKeyColumn(String membershipEntityForeignKeyColumn) {
    this.membershipEntityForeignKeyColumn = membershipEntityForeignKeyColumn;
  }




  /**
   * table name for group table
   * @param groupTableName
   */
  public void setGroupTableName(String groupTableName) {
    this.groupTableName = groupTableName;
  }



  
  public String getGroupTableIdColumn() {
    return groupTableIdColumn;
  }



  
  public void setGroupTableIdColumn(String groupTableIdColumn) {
    this.groupTableIdColumn = groupTableIdColumn;
  }


  private String entityTableName;
  
  private String entityTableIdColumn;
  
  
  


  
  public String getEntityTableName() {
    return entityTableName;
  }




  
  public void setEntityTableName(String entityTableName) {
    this.entityTableName = entityTableName;
  }







  
  public String getEntityTableIdColumn() {
    return entityTableIdColumn;
  }




  
  public void setEntityTableIdColumn(String entityTableIdColumn) {
    this.entityTableIdColumn = entityTableIdColumn;
  }




  @Override
  public void configureSpecificSettings() {
    
    this.dbExternalSystemConfigId = this.retrieveConfigString("dbExternalSystemConfigId", true);    
    
    //TODO validate sql config id
    
    this.membershipTableName = this.retrieveConfigString("membershipTableName", false);

    this.membershipGroupForeignKeyColumn = this.retrieveConfigString("membershipGroupForeignKeyColumn", false);
    
    this.membershipEntityForeignKeyColumn = this.retrieveConfigString("membershipEntityForeignKeyColumn", false);
    
//    this.membershipUserColumn = this.retrieveConfigString("membershipUserColumn", false);
//    this.membershipUserValueFormat = this.retrieveConfigString("membershipUserValueFormat", true);
//    this.membershipGroupColumn = this.retrieveConfigString("membershipGroupColumn", true);
//    this.membershipGroupValueFormat = this.retrieveConfigString("membershipGroupValueFormat", true);
//    this.membershipCreationNumberOfAttributes = this.retrieveConfigString("membershipCreationNumberOfAttributes", true);
//    this.membershipCreationColumnTemplate_attr_0 = this.retrieveConfigString("membershipCreationColumnTemplate_attr_0", true);
//    this.membershipCreationColumnTemplate_val_0 = this.retrieveConfigString("membershipCreationColumnTemplate_val_0", true);
//    this.membershipCreationColumnTemplate_attr_1 = this.retrieveConfigString("membershipCreationColumnTemplate_attr_1", true);
//    this.membershipCreationColumnTemplate_val_1 = this.retrieveConfigString("membershipCreationColumnTemplate_val_1", true);

    this.entityTableName = this.retrieveConfigString("userTableName", false);
    this.entityTableIdColumn = this.retrieveConfigString("userPrimaryKey", false);
    this.groupTableName = this.retrieveConfigString("groupTableName", false);

    this.groupAttributesTableName = this.retrieveConfigString("groupAttributesTableName", false);
    this.groupAttributesGroupForeignKeyColumn = this.retrieveConfigString("groupAttributesGroupForeignKeyColumn", false);
    this.groupAttributesAttributeNameColumn = this.retrieveConfigString("groupAttributesAttributeNameColumn", false);
    this.groupAttributesAttributeValueColumn = this.retrieveConfigString("groupAttributesAttributeValueColumn", false);
    this.groupAttributesLastModifiedColumn = this.retrieveConfigString("groupAttributesLastModifiedColumn", false);
    this.groupAttributesLastModifiedColumnType = this.retrieveConfigString("groupAttributesLastModifiedColumnType", false);
    this.groupTableIdColumn = this.retrieveConfigString("groupTableIdColumn", false);
    
    this.entityAttributesTableName = this.retrieveConfigString("entityAttributesTableName", false);
    this.entityAttributesEntityForeignKeyColumn = this.retrieveConfigString("entityAttributesEntityForeignKeyColumn", false);
    this.entityAttributesAttributeNameColumn = this.retrieveConfigString("entityAttributesAttributeNameColumn", false);
    this.entityAttributesAttributeValueColumn = this.retrieveConfigString("entityAttributesAttributeValueColumn", false);
    this.entityAttributesLastModifiedColumn = this.retrieveConfigString("entityAttributesLastModifiedColumn", false);
    this.entityAttributesLastModifiedColumnType = this.retrieveConfigString("entityAttributesLastModifiedColumnType", false);
    
    this.useSeparateTableForGroupAttributes = GrouperUtil.booleanValue(this.retrieveConfigBoolean("useSeparateTableForGroupAttributes", false), false);
    
    this.useSeparateTableForEntityAttributes = GrouperUtil.booleanValue(this.retrieveConfigBoolean("useSeparateTableForEntityAttributes", false), false);

    if (!StringUtils.isBlank(this.membershipTableName) && StringUtils.isBlank(this.getMembershipMatchingIdExpression())) {
      //setMembershipMatchingIdExpression("${new edu.internet2.middleware.grouperClient.collections.MultiKey(targetMembership.getProvisioningGroup().retrieveAttributeValueString('"+groupTableIdColumn+"'), targetMembership.getProvisioningEntity().retrieveAttributeValueString('"+entityTableIdColumn+"'))}");
      setMembershipMatchingIdExpression("${new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.retrieveAttributeValueString('"+getMembershipGroupMatchingIdAttribute()+"'), targetMembership.retrieveAttributeValueString('"+getMembershipEntityMatchingIdAttribute()+"'))}");
    }
//    this.groupAttributeTableForeignKeyToGroup = this.retrieveConfigString("groupAttributeTableForeignKeyToGroup", false);
//    this.groupAttributeTableIdColumn = this.retrieveConfigString("groupAttributeTableIdColumn", false);
//    this.groupAttributeTableAttributeNameColumn = this.retrieveConfigString("groupAttributeTableAttributeNameColumn", false);
//    this.groupAttributeTableAttributeValueColumn = this.retrieveConfigString("groupAttributeTableAttributeValueColumn", false);

    
    Map<String, GrouperProvisioningConfigurationAttribute> targetGroupAttributeNameToConfig = this.getTargetGroupAttributeNameToConfig();
    
    for (String name: targetGroupAttributeNameToConfig.keySet()) {
      
      SqlGrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) targetGroupAttributeNameToConfig.get(name);
      
      String storageType = this.retrieveConfigString("targetGroupAttribute" + "."+grouperProvisioningConfigurationAttribute.getConfigIndex()+".storageType", false);
      
      grouperProvisioningConfigurationAttribute.setStorageType(storageType);
      
    }
    
    for (String name: getTargetEntityAttributeNameToConfig().keySet()) {
      
      SqlGrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) getTargetEntityAttributeNameToConfig().get(name);
      
      String storageType = this.retrieveConfigString("targetEntityAttribute" + "."+grouperProvisioningConfigurationAttribute.getConfigIndex()+".storageType", false);
      
      grouperProvisioningConfigurationAttribute.setStorageType(storageType);
      
    }
    
    
  }

  /**
   * find the matching ID part for membership table that goes to the group
   * @return the column which is the matching ID in memberships for group
   */
  public String getMembershipGroupMatchingIdAttribute() {
    if (StringUtils.isBlank(this.membershipTableName)) {
      return null;
    }
    if (!StringUtils.isBlank(membershipGroupMatchingIdAttribute)) {
      return this.membershipGroupMatchingIdAttribute;
    }

    if (!StringUtils.isBlank(this.membershipGroupForeignKeyColumn)) {
      membershipGroupMatchingIdAttribute = membershipGroupForeignKeyColumn;
      return this.membershipGroupMatchingIdAttribute;
    }

    // "id", "idIndex", "idIndexString", "displayExtension", "displayName", "extension", "groupAttributeValueCache0", "groupAttributeValueCache1", "groupAttributeValueCache2", "groupAttributeValueCache3", "name", "description"
    //  configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.0.name", "group_name");
    //  configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    //  configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    
    for (String groupMapping : new String[] {"id", "idIndex", "idIndexString", "name", "displayName", "extension", "displayExtension", "groupAttributeValueCache0", "groupAttributeValueCache1", "groupAttributeValueCache2", "groupAttributeValueCache3"}) {
      for (String name: getTargetMembershipAttributeNameToConfig().keySet()) {
        
        SqlGrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) this.getTargetMembershipAttributeNameToConfig().get(name);
        
        if (StringUtils.equals(groupMapping, grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupField())) {
          membershipGroupMatchingIdAttribute = name;
          return this.membershipGroupMatchingIdAttribute;
        }
        
      }
      
    }
    throw new RuntimeException("Cant find membership column to use for matching when it involves groups");
  }

  private String membershipEntityMatchingIdAttribute;
  
  private String membershipGroupMatchingIdAttribute;
  
  /**
   * find the matching ID part for membership table that goes to the entity
   * @return the column which is the matching ID in memberships for entity
   */
  public String getMembershipEntityMatchingIdAttribute() {
    if (StringUtils.isBlank(this.membershipTableName)) {
      return null;
    }
    if (!StringUtils.isBlank(membershipEntityMatchingIdAttribute)) {
      return this.membershipEntityMatchingIdAttribute;
    }
    
    if (!StringUtils.isBlank(this.membershipEntityForeignKeyColumn)) {
      membershipEntityMatchingIdAttribute = membershipEntityForeignKeyColumn;
      return this.membershipEntityMatchingIdAttribute;
    }

    // "id", "email", "loginid", "memberId", "entityAttributeValueCache0", "entityAttributeValueCache1", "entityAttributeValueCache2", "entityAttributeValueCache3", "name", "subjectId", "subjectSourceId", "description", "subjectIdentifier0", "subjectIdentifier1", "subjectIdentifier2"
    //  configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.1.name", "subject_id");
    //  configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
    //  configureProvisionerSuffix(provisioningTestConfigInput, "targetMembershipAttribute.1.translateFromGrouperProvisioningEntityField", "subjectId");

    for (String entityMapping : new String[] {"memberId", "subjectId", "subjectIdentifier0", "subjectIdentifier1", "subjectIdentifier2", "email", "loginid", "entityAttributeValueCache0", "entityAttributeValueCache1", "entityAttributeValueCache2", "entityAttributeValueCache3", "id"}) {
      for (String name: getTargetMembershipAttributeNameToConfig().keySet()) {
        
        SqlGrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = (SqlGrouperProvisioningConfigurationAttribute) this.getTargetMembershipAttributeNameToConfig().get(name);
        
        if (StringUtils.equals(entityMapping, grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningEntityField())) {
          membershipEntityMatchingIdAttribute = name;
          return this.membershipEntityMatchingIdAttribute;
        }
        
      }
      
    }
    throw new RuntimeException("Cant find membership column to use for matching when it involves entities");
  }

  
  public String getDbExternalSystemConfigId() {
    return dbExternalSystemConfigId;
  }


  
  public void setDbExternalSystemConfigId(String dbExternalSystemConfigId) {
    this.dbExternalSystemConfigId = dbExternalSystemConfigId;
  }


  
  public String getMembershipTableName() {
    return membershipTableName;
  }


  
  public void setMembershipTableName(String membershipTableName) {
    this.membershipTableName = membershipTableName;
  }

  
  
  public String getGroupAttributesTableName() {
    return groupAttributesTableName;
  }

  
  public void setGroupAttributesTableName(String groupAttributesTableName) {
    this.groupAttributesTableName = groupAttributesTableName;
  }

  
  public String getGroupAttributesGroupForeignKeyColumn() {
    return groupAttributesGroupForeignKeyColumn;
  }

  
  public void setGroupAttributesGroupForeignKeyColumn(
      String groupAttributesGroupForeignKeyColumn) {
    this.groupAttributesGroupForeignKeyColumn = groupAttributesGroupForeignKeyColumn;
  }

  
  public String getGroupAttributesAttributeNameColumn() {
    return groupAttributesAttributeNameColumn;
  }

  
  public void setGroupAttributesAttributeNameColumn(
      String groupAttributesAttributeNameColumn) {
    this.groupAttributesAttributeNameColumn = groupAttributesAttributeNameColumn;
  }

  
  public String getGroupAttributesAttributeValueColumn() {
    return groupAttributesAttributeValueColumn;
  }

  
  public void setGroupAttributesAttributeValueColumn(
      String groupAttributesAttributeValueColumn) {
    this.groupAttributesAttributeValueColumn = groupAttributesAttributeValueColumn;
  }

  
  public String getGroupAttributesLastModifiedColumn() {
    return groupAttributesLastModifiedColumn;
  }

  
  public void setGroupAttributesLastModifiedColumn(
      String groupAttributesLastModifiedColumn) {
    this.groupAttributesLastModifiedColumn = groupAttributesLastModifiedColumn;
  }

  
  public String getEntityAttributesTableName() {
    return entityAttributesTableName;
  }

  
  public void setEntityAttributesTableName(String entityAttributesTableName) {
    this.entityAttributesTableName = entityAttributesTableName;
  }

  
  
  public String getEntityAttributesEntityForeignKeyColumn() {
    return entityAttributesEntityForeignKeyColumn;
  }

  
  public void setEntityAttributesEntityForeignKeyColumn(
      String entityAttributesEntityForeignKeyColumn) {
    this.entityAttributesEntityForeignKeyColumn = entityAttributesEntityForeignKeyColumn;
  }

  public String getEntityAttributesAttributeNameColumn() {
    return entityAttributesAttributeNameColumn;
  }

  
  public void setEntityAttributesAttributeNameColumn(
      String entityAttributesAttributeNameColumn) {
    this.entityAttributesAttributeNameColumn = entityAttributesAttributeNameColumn;
  }

  
  public String getEntityAttributesAttributeValueColumn() {
    return entityAttributesAttributeValueColumn;
  }

  
  public void setEntityAttributesAttributeValueColumn(
      String entityAttributesAttributeValueColumn) {
    this.entityAttributesAttributeValueColumn = entityAttributesAttributeValueColumn;
  }

  
  public String getEntityAttributesLastModifiedColumn() {
    return entityAttributesLastModifiedColumn;
  }

  
  public void setEntityAttributesLastModifiedColumn(
      String entityAttributesLastModifiedColumn) {
    this.entityAttributesLastModifiedColumn = entityAttributesLastModifiedColumn;
  }

  
  public String getGroupAttributesLastModifiedColumnType() {
    return groupAttributesLastModifiedColumnType;
  }

  
  public void setGroupAttributesLastModifiedColumnType(
      String groupAttributesLastModifiedColumnType) {
    this.groupAttributesLastModifiedColumnType = groupAttributesLastModifiedColumnType;
  }

  
  public String getEntityAttributesLastModifiedColumnType() {
    return entityAttributesLastModifiedColumnType;
  }

  
  public void setEntityAttributesLastModifiedColumnType(
      String entityAttributesLastModifiedColumnType) {
    this.entityAttributesLastModifiedColumnType = entityAttributesLastModifiedColumnType;
  }
  
  
  

}
