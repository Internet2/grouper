package edu.internet2.middleware.grouper.app.sqlProvisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;


public class SqlProvisioningConfiguration extends GrouperProvisioningConfigurationBase {

//  private String groupAttributeTableAttributeNameIsGroupMatchingId;
  
  
  private SqlProvisioningType sqlProvisioningType;

  
  
  public SqlProvisioningType getSqlProvisioningType() {
    return sqlProvisioningType;
  }

  public void setSqlProvisioningType(SqlProvisioningType sqlProvisioningType) {
    this.sqlProvisioningType = sqlProvisioningType;
  }


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
  
  /**
   * columns in the group table
   */
//  private String groupAttributeNames;
  
  /**
   * if there is a group attribute table (like ldap), this is the table name
   */
//  private String groupAttributeTableName;
  
  /**
   * if group table has one primary key, this is it
   */
  private String groupTableIdColumn;

  private String membershipTableIdColumn;
  
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


//  private String entityAttributeTableForeignKeyToEntity;
  
  private String entityTableName;
  
//  private String entityAttributeTableAttributeNameColumn;
  
//  private String entityAttributeNames;
//  
//  private String entityAttributeTableName;
  
  private String entityTableIdColumn;
  
//  private String entityAttributeTableAttributeValueColumn;
  
  
  


  
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
    
    String sqlProvisioningTypeString = this.retrieveConfigString("provisioningType", true);
    // TODO
    //this.sqlProvisioningType = SqlProvisioningType.valueOfIgnoreCase(sqlProvisioningTypeString, true);
    
    
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

    //this.entityAttributeTableForeignKeyToEntity = this.retrieveConfigString("entityAttributeTableForeignKeyToEntity", false);
    this.entityTableName = this.retrieveConfigString("userTableName", false);
    //this.entityAttributeTableAttributeNameColumn = this.retrieveConfigString("entityAttributeTableAttributeNameColumn", false);
//    this.entityAttributeNames = this.retrieveConfigString("entityAttributeNames", false);
//    this.entityAttributeTableName = this.retrieveConfigString("entityAttributeTableName", false);
    this.entityTableIdColumn = this.retrieveConfigString("userPrimaryKey", false);
//    this.entityAttributeTableAttributeValueColumn = this.retrieveConfigString("entityAttributeTableAttributeValueColumn", false);
    
//    this.groupAttributeTableAttributeNameIsGroupMatchingId = this.retrieveConfigString("groupAttributeTableAttributeNameIsGroupMatchingId", false);
    this.groupTableName = this.retrieveConfigString("groupTableName", false);
//    this.groupAttributeNames = this.retrieveConfigString("groupAttributeNames", false);
//    this.groupAttributeTableName = this.retrieveConfigString("groupAttributeTableName", false);
    this.groupTableIdColumn = this.retrieveConfigString("groupPrimaryKey", false);
    this.membershipTableIdColumn = this.retrieveConfigString("membershipPrimaryKey", false);
    
    //setMembershipMatchingIdExpression("${new edu.internet2.middleware.grouperClient.collections.MultiKey(targetMembership.getProvisioningGroup().retrieveAttributeValueString('"+groupTableIdColumn+"'), targetMembership.getProvisioningEntity().retrieveAttributeValueString('"+entityTableIdColumn+"'))}");
    setMembershipMatchingIdExpression("${new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.retrieveAttributeValueString('"+membershipGroupForeignKeyColumn+"'), targetMembership.retrieveAttributeValueString('"+membershipEntityForeignKeyColumn+"'))}");
//    this.groupAttributeTableForeignKeyToGroup = this.retrieveConfigString("groupAttributeTableForeignKeyToGroup", false);
//    this.groupAttributeTableIdColumn = this.retrieveConfigString("groupAttributeTableIdColumn", false);
//    this.groupAttributeTableAttributeNameColumn = this.retrieveConfigString("groupAttributeTableAttributeNameColumn", false);
//    this.groupAttributeTableAttributeValueColumn = this.retrieveConfigString("groupAttributeTableAttributeValueColumn", false);

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

  
  public String getMembershipTableIdColumn() {
    return membershipTableIdColumn;
  }

  
  public void setMembershipTableIdColumn(String membershipTableIdColumn) {
    this.membershipTableIdColumn = membershipTableIdColumn;
  }
  
  

}
