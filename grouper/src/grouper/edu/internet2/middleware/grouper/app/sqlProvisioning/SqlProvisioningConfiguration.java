package edu.internet2.middleware.grouper.app.sqlProvisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;


public class SqlProvisioningConfiguration extends GrouperProvisioningConfigurationBase {

  private String dbExternalSystemConfigId;
  
  private String membershipTableName;

  private String membershipUserColumn;
  
  private String membershipUserValueFormat;
  
  private String membershipGroupColumn;
  
  private String membershipGroupValueFormat;
  
  private String membershipCreationNumberOfAttributes;
  
  private String membershipCreationColumnTemplate_attr_0;
  
  private String membershipCreationColumnTemplate_val_0;
  
  private String membershipCreationColumnTemplate_attr_1;
  
  private String membershipCreationColumnTemplate_val_1;
  

  @Override
  public void configureSpecificSettings() {
    
    this.dbExternalSystemConfigId = this.retrieveConfigString("dbExternalSystemConfigId", true);
    
    //TODO validate sql config id
    
    this.membershipTableName = this.retrieveConfigString("membershipTableName", true);
    
//    this.membershipUserColumn = this.retrieveConfigString("membershipUserColumn", false);
//    this.membershipUserValueFormat = this.retrieveConfigString("membershipUserValueFormat", true);
//    this.membershipGroupColumn = this.retrieveConfigString("membershipGroupColumn", true);
//    this.membershipGroupValueFormat = this.retrieveConfigString("membershipGroupValueFormat", true);
//    this.membershipCreationNumberOfAttributes = this.retrieveConfigString("membershipCreationNumberOfAttributes", true);
//    this.membershipCreationColumnTemplate_attr_0 = this.retrieveConfigString("membershipCreationColumnTemplate_attr_0", true);
//    this.membershipCreationColumnTemplate_val_0 = this.retrieveConfigString("membershipCreationColumnTemplate_val_0", true);
//    this.membershipCreationColumnTemplate_attr_1 = this.retrieveConfigString("membershipCreationColumnTemplate_attr_1", true);
//    this.membershipCreationColumnTemplate_val_1 = this.retrieveConfigString("membershipCreationColumnTemplate_val_1", true);

    
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


  
  public String getMembershipUserColumn() {
    return membershipUserColumn;
  }


  
  public void setMembershipUserColumn(String membershipUserColumn) {
    this.membershipUserColumn = membershipUserColumn;
  }


  
  public String getMembershipUserValueFormat() {
    return membershipUserValueFormat;
  }


  
  public void setMembershipUserValueFormat(String membershipUserValueFormat) {
    this.membershipUserValueFormat = membershipUserValueFormat;
  }


  
  public String getMembershipGroupColumn() {
    return membershipGroupColumn;
  }


  
  public void setMembershipGroupColumn(String membershipGroupColumn) {
    this.membershipGroupColumn = membershipGroupColumn;
  }


  
  public String getMembershipGroupValueFormat() {
    return membershipGroupValueFormat;
  }


  
  public void setMembershipGroupValueFormat(String membershipGroupValueFormat) {
    this.membershipGroupValueFormat = membershipGroupValueFormat;
  }


  
  public String getMembershipCreationNumberOfAttributes() {
    return membershipCreationNumberOfAttributes;
  }


  
  public void setMembershipCreationNumberOfAttributes(
      String membershipCreationNumberOfAttributes) {
    this.membershipCreationNumberOfAttributes = membershipCreationNumberOfAttributes;
  }


  
  public String getMembershipCreationColumnTemplate_attr_0() {
    return membershipCreationColumnTemplate_attr_0;
  }


  
  public void setMembershipCreationColumnTemplate_attr_0(
      String membershipCreationColumnTemplate_attr_0) {
    this.membershipCreationColumnTemplate_attr_0 = membershipCreationColumnTemplate_attr_0;
  }


  
  public String getMembershipCreationColumnTemplate_val_0() {
    return membershipCreationColumnTemplate_val_0;
  }


  
  public void setMembershipCreationColumnTemplate_val_0(
      String membershipCreationColumnTemplate_val_0) {
    this.membershipCreationColumnTemplate_val_0 = membershipCreationColumnTemplate_val_0;
  }


  
  public String getMembershipCreationColumnTemplate_attr_1() {
    return membershipCreationColumnTemplate_attr_1;
  }


  
  public void setMembershipCreationColumnTemplate_attr_1(
      String membershipCreationColumnTemplate_attr_1) {
    this.membershipCreationColumnTemplate_attr_1 = membershipCreationColumnTemplate_attr_1;
  }


  
  public String getMembershipCreationColumnTemplate_val_1() {
    return membershipCreationColumnTemplate_val_1;
  }


  
  public void setMembershipCreationColumnTemplate_val_1(
      String membershipCreationColumnTemplate_val_1) {
    this.membershipCreationColumnTemplate_val_1 = membershipCreationColumnTemplate_val_1;
  }

}
