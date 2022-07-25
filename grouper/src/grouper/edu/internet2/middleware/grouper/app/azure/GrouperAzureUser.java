package edu.internet2.middleware.grouper.app.azure;

import java.sql.Types;
import java.util.Set;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperAzureUser {

  private boolean accountEnabled;

  private String displayName;

  private String id;

  private String mailNickname;

  private String onPremisesImmutableId;

  private String userPrincipalName;
  
  private String password;
  
  private boolean forceChangePasswordNextSignIn = true;

  public static final String fieldsToSelect="accountEnabled,displayName,id,mailNickname,onPremisesImmutableId,userPrincipalName";
  
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity();
    
    targetEntity.assignAttributeValue("accountEnabled", this.accountEnabled);
    targetEntity.assignAttributeValue("displayName", this.displayName);
    targetEntity.setId(this.id);
    targetEntity.assignAttributeValue("mailNickname", this.mailNickname);
    targetEntity.assignAttributeValue("onPremisesImmutableId", this.onPremisesImmutableId);
    targetEntity.assignAttributeValue("userPrincipalName", this.userPrincipalName);
    return targetEntity;
  }
  
  
  /**
   * 
   * @param targetEntity
   * @return
   */
  public static GrouperAzureUser fromProvisioningEntity(ProvisioningEntity targetEntity, Set<String> fieldNamesToSet) {
    
    GrouperAzureUser grouperAzureUser = new GrouperAzureUser();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("accountEnabled")) {      
      grouperAzureUser.setAccountEnabled(targetEntity.retrieveAttributeValueBoolean("accountEnabled"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("displayName")) {      
      grouperAzureUser.setDisplayName(targetEntity.retrieveAttributeValueString("displayName"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("mailNickname")) {      
      grouperAzureUser.setMailNickname(targetEntity.retrieveAttributeValueString("mailNickname"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("onPremisesImmutableId")) {      
      grouperAzureUser.setOnPremisesImmutableId(targetEntity.retrieveAttributeValueString("onPremisesImmutableId"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("userPrincipalName")) {      
      grouperAzureUser.setUserPrincipalName(targetEntity.retrieveAttributeValueString("userPrincipalName"));
    }
   
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      grouperAzureUser.setId(targetEntity.getId());
    }
    
    return grouperAzureUser;

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean isAccountEnabled() {
    return accountEnabled;
  }

  public void setAccountEnabled(Boolean accountEnabled) {
    this.accountEnabled = (accountEnabled == null ? false : accountEnabled);
  }

  public String getAccountEnabledDb() {
    return accountEnabled ? "T" : "F";
  }

  public void setAccountEnabledDb(String accountEnabledDb) {
    this.accountEnabled = GrouperUtil.booleanValue(accountEnabledDb);
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getOnPremisesImmutableId() {
    return onPremisesImmutableId;
  }

  public void setOnPremisesImmutableId(String onPremisesImmutableId) {
    this.onPremisesImmutableId = onPremisesImmutableId;
  }

  public String getMailNickname() {
    return mailNickname;
  }

  public void setMailNickname(String mailNickname) {
    this.mailNickname = mailNickname;
  }

  public String getUserPrincipalName() {
    return userPrincipalName;
  }

  public void setUserPrincipalName(String userPrincipalName) {
    this.userPrincipalName = userPrincipalName;
  }
  
  
  public void setPassword(String password) {
    this.password = password;
  }
  
  /**
   * convert from jackson json
   * @param entityNode
   * @return the group
   */
  public static GrouperAzureUser fromJson(JsonNode entityNode) {
    GrouperAzureUser grouperAzureUser = new GrouperAzureUser();
    
    /**
     * {
    "@odata.context": "https://graph.microsoft.com/v1.0/$metadata#users/$entity",
    "id": "54396678-d966-46b7-98cb-868a3001587e",
    "businessPhones": [],
    "displayName": "Adele Vance1",
    "givenName": null,
    "jobTitle": null,
    "mail": null,
    "mobilePhone": null,
    "officeLocation": null,
    "preferredLanguage": null,
    "surname": null,
    "userPrincipalName": "Adele1V@erviveksachdevaoutlook.onmicrosoft.com"
}
     */
    grouperAzureUser.accountEnabled = GrouperUtil.jsonJacksonGetBoolean(entityNode, "accountEnabled", false);
    grouperAzureUser.displayName = GrouperUtil.jsonJacksonGetString(entityNode, "displayName");
    grouperAzureUser.id = GrouperUtil.jsonJacksonGetString(entityNode, "id");
    grouperAzureUser.mailNickname = GrouperUtil.jsonJacksonGetString(entityNode, "mailNickname");
    grouperAzureUser.onPremisesImmutableId = GrouperUtil.jsonJacksonGetString(entityNode, "onPremisesImmutableId");
    grouperAzureUser.userPrincipalName = GrouperUtil.jsonJacksonGetString(entityNode, "userPrincipalName");
    
    return grouperAzureUser;
  }

  public static void main(String[] args) {
    
    GrouperAzureUser grouperAzureUser = new GrouperAzureUser();
    
    grouperAzureUser.setAccountEnabled(true);
    grouperAzureUser.setDisplayName("dispName");
    grouperAzureUser.setId("id");
    grouperAzureUser.setMailNickname("mailNick");
    grouperAzureUser.setOnPremisesImmutableId("onPrem");
    grouperAzureUser.setUserPrincipalName("userPri");
  
    String json = GrouperUtil.jsonJacksonToString(grouperAzureUser.toJson(null));
    System.out.println(json);
    
    grouperAzureUser = GrouperAzureUser.fromJson(GrouperUtil.jsonJacksonNode(json));
    
    System.out.println(grouperAzureUser.toString());
    
  }

  /**
   * convert from jackson json
   * @param groupNode
   * @return the group
   */
  public ObjectNode toJson(Set<String> fieldNamesToSet) {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode result = objectMapper.createObjectNode();
  
    if (fieldNamesToSet == null || fieldNamesToSet.contains("accountEnabled")) {      
      GrouperUtil.jsonJacksonAssignBoolean(result, "accountEnabled", this.accountEnabled);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("displayName")) {      
      GrouperUtil.jsonJacksonAssignString(result, "displayName", this.displayName);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      GrouperUtil.jsonJacksonAssignString(result, "id", this.id);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("mailNickname")) {      
      GrouperUtil.jsonJacksonAssignString(result, "mailNickname", this.mailNickname);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("onPremisesImmutableId")) {      
      GrouperUtil.jsonJacksonAssignString(result, "onPremisesImmutableId", this.onPremisesImmutableId);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("userPrincipalName")) {      
      GrouperUtil.jsonJacksonAssignString(result, "userPrincipalName", this.userPrincipalName);
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("password")) {
      
      ObjectNode passwordProfileObjectNode = result.putObject("passwordProfile");
      
      GrouperUtil.jsonJacksonAssignString(passwordProfileObjectNode, "password", this.password);
      GrouperUtil.jsonJacksonAssignBoolean(passwordProfileObjectNode, "forceChangePasswordNextSignIn", this.forceChangePasswordNextSignIn);
    }
    
    return result;
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableAzureUser(DdlVersionBean ddlVersionBean, Database database) {
  
    final String tableName = "mock_azure_user";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
          
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "account_enabled", Types.VARCHAR, "1", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "display_name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "mail_nickname", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "on_premises_immutable_id", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_principal_name", Types.VARCHAR, "256", false, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_azure_user_upn_idx", false, "user_principal_name");
    }
    
  }

}
