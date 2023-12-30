package edu.internet2.middleware.grouper.app.teamDynamix;

import java.sql.Types;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.azure.GrouperAzureUser;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class TeamDynamixUser {
  
  private String id; // UID
  
  private String firstName;
  
  private String lastName;
  
  private String primaryEmail;
  
  private String company;
  
  private String securityRoleId;

  private int typeId = 1;
  
  private String userName;
  
  private String externalId;
  
  
  public String getExternalId() {
    return externalId;
  }


  
  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }


  
  public String getId() {
    return id;
  }


  public void setId(String id) {
    this.id = id;
  }



  public String getFirstName() {
    return firstName;
  }

  
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  
  public String getLastName() {
    return lastName;
  }

  
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  
  public String getPrimaryEmail() {
    return primaryEmail;
  }

  
  public void setPrimaryEmail(String primaryEmail) {
    this.primaryEmail = primaryEmail;
  }

  
  public String getCompany() {
    return company;
  }

  
  public void setCompany(String company) {
    this.company = company;
  }

  
  public String getSecurityRoleId() {
    return securityRoleId;
  }

  
  public void setSecurityRoleId(String securityRoleId) {
    this.securityRoleId = securityRoleId;
  }

  
  public int getTypeId() {
    return typeId;
  }

  
  public void setTypeId(int typeId) {
    this.typeId = typeId;
  }

  
  public String getUserName() {
    return userName;
  }

  
  public void setUserName(String userName) {
    this.userName = userName;
  }
  
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity();
    targetEntity.assignAttributeValue("FirstName", this.firstName);
    targetEntity.assignAttributeValue("LastName", this.lastName);
    targetEntity.setId(this.id);
    targetEntity.assignAttributeValue("PrimaryEmail", this.primaryEmail);
    targetEntity.assignAttributeValue("Company", this.company);
    targetEntity.assignAttributeValue("SecurityRoleID", this.securityRoleId);
    targetEntity.assignAttributeValue("UserName", this.userName);
    targetEntity.assignAttributeValue("ExternalID", this.externalId);
    return targetEntity;
  }

  
  /**
   * 
   * @param targetEntity
   * @return
   */
  public static TeamDynamixUser fromProvisioningEntity(ProvisioningEntity targetEntity, Set<String> fieldNamesToSet) {
    
    TeamDynamixUser teamDynamixUser = new TeamDynamixUser();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("FirstName")) {    
      teamDynamixUser.setFirstName(targetEntity.retrieveAttributeValueString("FirstName"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("LastName")) {      
      teamDynamixUser.setLastName(targetEntity.retrieveAttributeValueString("LastName"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("PrimaryEmail")) {      
      teamDynamixUser.setPrimaryEmail(targetEntity.retrieveAttributeValueString("PrimaryEmail"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("Company")) {      
      teamDynamixUser.setCompany(targetEntity.retrieveAttributeValueString("Company"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("SecurityRoleID")) {      
      teamDynamixUser.setSecurityRoleId(targetEntity.retrieveAttributeValueString("SecurityRoleID"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      teamDynamixUser.setId(targetEntity.getId());
    }
   
    if (fieldNamesToSet == null || fieldNamesToSet.contains("UserName")) {      
      teamDynamixUser.setUserName(targetEntity.retrieveAttributeValueString("UserName"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("ExternalID")) {      
      teamDynamixUser.setExternalId(targetEntity.retrieveAttributeValueString("ExternalID"));
    }
    
    return teamDynamixUser;
  }
  
  /**
   * convert from jackson json
   * @param entityNode
   * @return the user
   */
  public static TeamDynamixUser fromJson(JsonNode entityNode) {

    if (entityNode == null || !entityNode.has("FirstName")) {
      return null;
    }

    TeamDynamixUser teamDynamixUser = new TeamDynamixUser();
    
    teamDynamixUser.firstName = GrouperUtil.jsonJacksonGetString(entityNode, "FirstName");
    teamDynamixUser.lastName = GrouperUtil.jsonJacksonGetString(entityNode, "LastName");
    teamDynamixUser.primaryEmail = GrouperUtil.jsonJacksonGetString(entityNode, "PrimaryEmail");
    teamDynamixUser.company = GrouperUtil.jsonJacksonGetString(entityNode, "Company");
    teamDynamixUser.securityRoleId = GrouperUtil.jsonJacksonGetString(entityNode, "SecurityRoleID");
    teamDynamixUser.typeId = GrouperUtil.jsonJacksonGetInteger(entityNode, "TypeID");
    teamDynamixUser.userName = GrouperUtil.jsonJacksonGetString(entityNode, "UserName");
    teamDynamixUser.externalId = GrouperUtil.jsonJacksonGetString(entityNode, "ExternalID");
    teamDynamixUser.id = GrouperUtil.jsonJacksonGetString(entityNode, "UID");
    
    return teamDynamixUser;
  }
  
  /**
   * convert from jackson json
   * @param groupNode
   * @return the json user 
   */
  public ObjectNode toJson(Set<String> fieldNamesToSet) {
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("FirstName")) {      
      GrouperUtil.jsonJacksonAssignString(result, "FirstName", this.firstName);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("LastName")) {      
      GrouperUtil.jsonJacksonAssignString(result, "LastName", this.lastName);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("PrimaryEmail")) {      
      GrouperUtil.jsonJacksonAssignString(result, "PrimaryEmail", this.primaryEmail);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("Company")) {      
      GrouperUtil.jsonJacksonAssignString(result, "Company", this.company);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("SecurityRoleID")) {      
      GrouperUtil.jsonJacksonAssignString(result, "SecurityRoleID", this.securityRoleId);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("UserName")) {      
      GrouperUtil.jsonJacksonAssignString(result, "UserName", this.userName);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("ExternalID")) {      
      GrouperUtil.jsonJacksonAssignString(result, "ExternalID", this.externalId);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("TypeID")) {      
      GrouperUtil.jsonJacksonAssignLong(result, "TypeID", Long.valueOf(this.typeId));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("UID")) {
      GrouperUtil.jsonJacksonAssignString(result, "UID", this.id);
    }
    
    return result;
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableTeamDynamixUser(DdlVersionBean ddlVersionBean, Database database) {
  
    final String tableName = "mock_teamdynamix_user";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
          
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
//    "FirstName": "Grouper",
//    "LastName": "API",
//    "PrimaryEmail": "grouper-api@example.com",
//    "Company": "Grouper",
//    "SecurityRoleId": "573ef9e3-e01f-422b-bb1d-a5efbc8553a5",
//    "TypeID": 1,
//    "UserName": "grouper_api_user"
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "first_name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "primary_email", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "company", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "security_role_id", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "external_id", Types.VARCHAR, "256", false, true);
      
    }
    
  }
  
}
