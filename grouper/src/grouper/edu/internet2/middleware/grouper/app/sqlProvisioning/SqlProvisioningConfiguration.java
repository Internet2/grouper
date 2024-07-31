package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class SqlProvisioningConfiguration extends GrouperProvisioningConfiguration {
  
  @Override
  protected Class<? extends GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributeClass() {
    return SqlGrouperProvisioningConfigurationAttribute.class;
  }

//  private String groupAttributeTableAttributeNameIsGroupMatchingId;
  
  
  private String dbExternalSystemConfigId;
  
  private String membershipTableName;
  
//  # remove deleted data after hours.  Defaults to 1 week
//  # {valueType: "integer", required: false, order: 76201, subSection: "general2", defaultValue: "168"}
  private int sqlRemoveDeletedDataAfterHours = 168;


//  private String membershipUserColumn;
    
//  private String membershipUserValueFormat;
  
//  private String membershipGroupColumn;
  
//  private String membershipGroupValueFormat;
  

  public int getSqlRemoveDeletedDataAfterHours() {
    return sqlRemoveDeletedDataAfterHours;
  }



  
  public void setSqlRemoveDeletedDataAfterHours(int sqlRemoveDeletedDataAfterHours) {
    this.sqlRemoveDeletedDataAfterHours = sqlRemoveDeletedDataAfterHours;
  }

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
  
  private String entityAttributesTableName;
  
  private String entityAttributesEntityForeignKeyColumn;
  
  private String entityAttributesAttributeNameColumn;
  
  private String entityAttributesAttributeValueColumn;
  
  private boolean useSeparateTableForGroupAttributes;
  
  private boolean useSeparateTableForEntityAttributes;
  
  private String sqlLastModifiedColumnType;
  private String sqlLastModifiedColumnName;
  private String sqlDeletedColumnName;
  
  
  public String getSqlLastModifiedColumnType() {
    return sqlLastModifiedColumnType;
  }


  
  public void setSqlLastModifiedColumnType(String sqlLastModifiedColumnType) {
    this.sqlLastModifiedColumnType = sqlLastModifiedColumnType;
  }




  
  public String getSqlLastModifiedColumnName() {
    return sqlLastModifiedColumnName;
  }




  
  public void setSqlLastModifiedColumnName(String sqlLastModifiedColumnName) {
    this.sqlLastModifiedColumnName = sqlLastModifiedColumnName;
  }




  
  public String getSqlDeletedColumnName() {
    return sqlDeletedColumnName;
  }




  
  public void setSqlDeletedColumnName(String sqlDeletedColumnName) {
    this.sqlDeletedColumnName = sqlDeletedColumnName;
  }




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

    String membershipGroupForeignKeyColumn = this.retrieveConfigString("membershipGroupForeignKeyColumn", false);
    
    String membershipEntityForeignKeyColumn = this.retrieveConfigString("membershipEntityForeignKeyColumn", false);

    membershipGroupMatchingIdAttribute = GrouperUtil.defaultIfBlank(membershipGroupForeignKeyColumn, membershipGroupMatchingIdAttribute);
    membershipEntityMatchingIdAttribute = GrouperUtil.defaultIfBlank(membershipEntityForeignKeyColumn, membershipEntityMatchingIdAttribute);
    
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
   
    
    this.sqlLastModifiedColumnName = this.retrieveConfigString("sqlLastModifiedColumnName", false);
    this.sqlLastModifiedColumnType = this.retrieveConfigString("sqlLastModifiedColumnType", false);
    this.sqlDeletedColumnName = this.retrieveConfigString("sqlDeletedColumnName", false);
    
    this.sqlRemoveDeletedDataAfterHours = GrouperUtil.intValue(this.retrieveConfigInt("sqlRemoveDeletedDataAfterHours", false), this.sqlRemoveDeletedDataAfterHours);
    
    this.groupTableIdColumn = this.retrieveConfigString("groupTableIdColumn", false);
    
    this.entityAttributesTableName = this.retrieveConfigString("entityAttributesTableName", false);
    this.entityAttributesEntityForeignKeyColumn = this.retrieveConfigString("entityAttributesEntityForeignKeyColumn", false);
    this.entityAttributesAttributeNameColumn = this.retrieveConfigString("entityAttributesAttributeNameColumn", false);
    this.entityAttributesAttributeValueColumn = this.retrieveConfigString("entityAttributesAttributeValueColumn", false);
    
    this.useSeparateTableForGroupAttributes = GrouperUtil.booleanValue(this.retrieveConfigBoolean("useSeparateTableForGroupAttributes", false), false);
    
    this.useSeparateTableForEntityAttributes = GrouperUtil.booleanValue(this.retrieveConfigBoolean("useSeparateTableForEntityAttributes", false), false);

   
    if (!StringUtils.isBlank(this.membershipTableName) && StringUtils.isBlank(this.getMembershipMatchingIdExpression())
        && !StringUtils.isBlank(getMembershipGroupMatchingIdAttribute()) && !StringUtils.isBlank(getMembershipEntityMatchingIdAttribute())) {
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

}
