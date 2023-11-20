package edu.internet2.middleware.grouper.app.boxProvisioner;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperBoxUser {
  
  private String id;
  private String type;
  private String role;
  private Long maxUploadSize;
  private Long spaceAmount;
  
  private boolean isExemptFromDeviceLimits;
  private boolean isExemptFromLoginVerification;
  private boolean isExternalCollabRestricted;
  private boolean isPlatformAccessOnly;
  private boolean isSyncEnabled;
  private boolean canSeeManagedUsers;
  
  private Timestamp createdAt;
  private Timestamp modifiedAt;
  private String login;
  private String name;
  private Long spaceUsed;
  private String status;
  
  public static final Map<String, String> grouperBoxUserToBoxSpecificAttributeNames =
      GrouperUtil.toMap("id", "id", "type", "type", "role", "role", "maxUploadSize", "max_upload_size",
          "spaceAmount", "space_amount", "isExemptFromDeviceLimits", "is_exempt_from_device_limits",
          "isExemptFromLoginVerification", "is_exempt_from_login_verification", 
          "isExternalCollabRestricted", "is_external_collab_restricted", 
          "isPlatformAccessOnly", "is_platform_access_only", "isSyncEnabled", "is_sync_enabled",
          "canSeeManagedUsers", "can_see_managed_users", "login", "login", "name", "name", "spaceUsed", "space_used",
          "status", "status");
  
  
  public String getType() {
    return type;
  }

  
  public void setType(String type) {
    this.type = type;
  }

  
  public String getRole() {
    return role;
  }

  
  public void setRole(String role) {
    this.role = role;
  }

  
  public Long getMaxUploadSize() {
    return maxUploadSize;
  }

  
  public void setMaxUploadSize(Long maxUploadSize) {
    this.maxUploadSize = maxUploadSize;
  }

  
  public Long getSpaceAmount() {
    return spaceAmount;
  }

  
  public void setSpaceAmount(Long spaceAmount) {
    this.spaceAmount = spaceAmount;
  }

  
  public Timestamp getCreatedAt() {
    return createdAt;
  }
  
  public void setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  public Timestamp getModifiedAt() {
    return modifiedAt;
  }
  
  public void setModifiedAt(Timestamp modifiedAt) {
    this.modifiedAt = modifiedAt;
  }
  
  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public Long getSpaceUsed() {
    return spaceUsed;
  }
  
  public void setSpaceUsed(Long spaceUsed) {
    this.spaceUsed = spaceUsed;
  }
  
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
  
  
  public boolean isExemptFromDeviceLimits() {
    return isExemptFromDeviceLimits;
  }


  
  public void setExemptFromDeviceLimits(boolean isExemptFromDeviceLimits) {
    this.isExemptFromDeviceLimits = isExemptFromDeviceLimits;
  }
  
  public String getExemptFromDeviceLimitsDb() {
    return isExemptFromDeviceLimits ? "T" : "F";
  }

  public void setExemptFromDeviceLimitsDb(String exemptFromDeviceLimitsDb) {
    this.isExemptFromDeviceLimits = GrouperUtil.booleanValue(exemptFromDeviceLimitsDb);
  }

  
  public boolean isExemptFromLoginVerification() {
    return isExemptFromLoginVerification;
  }


  
  public void setExemptFromLoginVerification(boolean isExemptFromLoginVerification) {
    this.isExemptFromLoginVerification = isExemptFromLoginVerification;
  }
  
  public String getExemptFromLoginVerificationDb() {
    return isExemptFromLoginVerification ? "T" : "F";
  }

  public void setExemptFromLoginVerificationDb(String exemptFromLoginVerificationDb) {
    this.isExemptFromLoginVerification = GrouperUtil.booleanValue(exemptFromLoginVerificationDb);
  }

  
  public boolean isExternalCollabRestricted() {
    return isExternalCollabRestricted;
  }


  
  public void setExternalCollabRestricted(boolean isExternalCollabRestricted) {
    this.isExternalCollabRestricted = isExternalCollabRestricted;
  }
  
  public String getExternalCollabRestrictedDb() {
    return isExternalCollabRestricted ? "T" : "F";
  }

  public void setExternalCollabRestrictedDb(String externalCollabRestrictedDb) {
    this.isExternalCollabRestricted = GrouperUtil.booleanValue(externalCollabRestrictedDb);
  }
  
  public boolean isPlatformAccessOnly() {
    return isPlatformAccessOnly;
  }

  public void setPlatformAccessOnly(boolean isPlatformAccessOnly) {
    this.isPlatformAccessOnly = isPlatformAccessOnly;
  }
  
  public String getPlatformAccessOnlyDb() {
    return isPlatformAccessOnly ? "T" : "F";
  }

  public void setPlatformAccessOnlyDb(String platformAccessOnly) {
    this.isPlatformAccessOnly = GrouperUtil.booleanValue(platformAccessOnly);
  }
  
  public boolean isSyncEnabled() {
    return isSyncEnabled;
  }
  
  public void setSyncEnabled(boolean isSyncEnabled) {
    this.isSyncEnabled = isSyncEnabled;
  }

  public String getSyncEnabledDb() {
    return isSyncEnabled ? "T" : "F";
  }

  public void setSyncEnabledDb(String syncEnabled) {
    this.isSyncEnabled = GrouperUtil.booleanValue(syncEnabled);
  }
  
  public boolean isCanSeeManagedUsers() {
    return canSeeManagedUsers;
  }
  
  public void setCanSeeManagedUsers(boolean canSeeManagedUsers) {
    this.canSeeManagedUsers = canSeeManagedUsers;
  }
  
  public String getCanSeeManagedUsersDb() {
    return canSeeManagedUsers ? "T" : "F";
  }

  public void setCanSeeManagedUsersDb(String canSeeManagedUsers) {
    this.canSeeManagedUsers = GrouperUtil.booleanValue(canSeeManagedUsers);
  }

  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity();
    
    targetEntity.setId(this.id);
    targetEntity.setName(this.name);
    targetEntity.setEmail(this.login);
    
    targetEntity.assignAttributeValue("login", this.login);
    targetEntity.assignAttributeValue("spaceUsed", this.spaceUsed);
    targetEntity.assignAttributeValue("status", this.status);
    targetEntity.assignAttributeValue("type", this.type);
    targetEntity.assignAttributeValue("role", this.role);
    targetEntity.assignAttributeValue("maxUploadSize", this.maxUploadSize);
    targetEntity.assignAttributeValue("spaceAmount", this.spaceAmount);
    targetEntity.assignAttributeValue("isExemptFromDeviceLimits", this.isExemptFromDeviceLimits);
    targetEntity.assignAttributeValue("isExemptFromLoginVerification", this.isExemptFromLoginVerification);
    targetEntity.assignAttributeValue("isExternalCollabRestricted", this.isExternalCollabRestricted);
    targetEntity.assignAttributeValue("isPlatformAccessOnly", this.isPlatformAccessOnly);
    targetEntity.assignAttributeValue("isSyncEnabled", this.isSyncEnabled);
    targetEntity.assignAttributeValue("canSeeManagedUsers", this.canSeeManagedUsers);
    
    return targetEntity;
  }
  
  
  /**
   * 
   * @param targetEntity
   * @return
   */
  public static GrouperBoxUser fromProvisioningEntity(ProvisioningEntity targetEntity, Set<String> fieldNamesToSet) {
    
    GrouperBoxUser grouperBoxUser = new GrouperBoxUser();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      grouperBoxUser.setId(targetEntity.getId());
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("login")) {      
      grouperBoxUser.setLogin(targetEntity.retrieveAttributeValueString("login"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {      
      grouperBoxUser.setName(targetEntity.retrieveAttributeValueString("name"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("spaceUsed")) {      
      grouperBoxUser.setSpaceUsed(targetEntity.retrieveAttributeValueLong("spaceUsed"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("status")) {      
      grouperBoxUser.setStatus(targetEntity.retrieveAttributeValueString("status"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("type")) {      
      grouperBoxUser.setType(targetEntity.retrieveAttributeValueString("type"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("role")) {      
      grouperBoxUser.setRole(targetEntity.retrieveAttributeValueString("role"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("maxUploadSize")) {      
      grouperBoxUser.setMaxUploadSize(targetEntity.retrieveAttributeValueLong("maxUploadSize"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("spaceAmount")) {      
      grouperBoxUser.setSpaceAmount(targetEntity.retrieveAttributeValueLong("spaceAmount"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("isExemptFromDeviceLimits")) {    
      Boolean valueBoolean = targetEntity.retrieveAttributeValueBoolean("isExemptFromDeviceLimits");
      grouperBoxUser.setExemptFromDeviceLimits(valueBoolean == null ? false: valueBoolean.booleanValue());
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("isExemptFromLoginVerification")) {
      Boolean valueBoolean = targetEntity.retrieveAttributeValueBoolean("isExemptFromLoginVerification");
      grouperBoxUser.setExemptFromLoginVerification(valueBoolean == null ? false: valueBoolean.booleanValue());
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("isExternalCollabRestricted")) {
      Boolean valueBoolean = targetEntity.retrieveAttributeValueBoolean("isExternalCollabRestricted");
      grouperBoxUser.setExternalCollabRestricted(valueBoolean == null ? false: valueBoolean.booleanValue());
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("isPlatformAccessOnly")) {
      Boolean valueBoolean = targetEntity.retrieveAttributeValueBoolean("isPlatformAccessOnly");
      grouperBoxUser.setPlatformAccessOnly(valueBoolean == null ? false: valueBoolean.booleanValue());
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("isSyncEnabled")) {  
      Boolean valueBoolean = targetEntity.retrieveAttributeValueBoolean("isSyncEnabled");
      grouperBoxUser.setSyncEnabled(valueBoolean == null ? false: valueBoolean.booleanValue());
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("canSeeManagedUsers")) {    
      Boolean valueBoolean = targetEntity.retrieveAttributeValueBoolean("canSeeManagedUsers");
      grouperBoxUser.setCanSeeManagedUsers(valueBoolean == null ? false: valueBoolean.booleanValue());
    }
    
    return grouperBoxUser;

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  /**
   * 2020-04-06T16:48:19Z
   */
  public String getCreatedJson() {
    return GrouperUtil.timestampIsoUtcSecondsConvertToString(this.createdAt);
  }

  /**
   * 2020-04-06T16:48:19Z
   * @param created
   */
  public void setCreatedJson(String created) {
    OffsetDateTime offsetDateTime = OffsetDateTime.parse(created);
    this.createdAt = Timestamp.from(offsetDateTime.toInstant());
  }
  
  /**
   * 2020-04-06T16:48:19Z
   */
  public String getModifiedJson() {
    return GrouperUtil.timestampIsoUtcSecondsConvertToString(this.modifiedAt);
  }

  /**
   * 2020-04-06T16:48:19Z
   * @param modified
   */
  public void setModifiedJson(String modified) {
    OffsetDateTime offsetDateTime = OffsetDateTime.parse(modified);
    this.modifiedAt = Timestamp.from(offsetDateTime.toInstant());
  }

  /**
   * convert from jackson json
   * @param entityNode
   * @return the group
   */
  public static GrouperBoxUser fromJson(JsonNode entityNode) {

    if (entityNode == null || !entityNode.has("name")) { 
      return null;
    }

    GrouperBoxUser grouperBoxUser = new GrouperBoxUser();

    grouperBoxUser.id = GrouperUtil.jsonJacksonGetString(entityNode, "id");
    grouperBoxUser.login = GrouperUtil.jsonJacksonGetString(entityNode, "login");
    grouperBoxUser.name = GrouperUtil.jsonJacksonGetString(entityNode, "name");
    grouperBoxUser.status = GrouperUtil.jsonJacksonGetString(entityNode, "status");
    grouperBoxUser.type = GrouperUtil.jsonJacksonGetString(entityNode, "type");
    grouperBoxUser.role = GrouperUtil.jsonJacksonGetString(entityNode, "role");
    grouperBoxUser.spaceUsed = GrouperUtil.jsonJacksonGetLong(entityNode, "space_used");
    grouperBoxUser.spaceAmount = GrouperUtil.jsonJacksonGetLong(entityNode, "space_amount");
    grouperBoxUser.maxUploadSize = GrouperUtil.jsonJacksonGetLong(entityNode, "max_upload_size");
    
    Boolean booleanObject = GrouperUtil.jsonJacksonGetBoolean(entityNode, "is_exempt_from_device_limits");
    grouperBoxUser.isExemptFromDeviceLimits = booleanObject == null? false: booleanObject.booleanValue();
    
    booleanObject = GrouperUtil.jsonJacksonGetBoolean(entityNode, "is_exempt_from_login_verification");
    grouperBoxUser.isExemptFromLoginVerification = booleanObject == null? false: booleanObject.booleanValue();
    
    booleanObject = GrouperUtil.jsonJacksonGetBoolean(entityNode, "is_external_collab_restricted");
    grouperBoxUser.isExternalCollabRestricted = booleanObject == null? false: booleanObject.booleanValue();
    
    booleanObject = GrouperUtil.jsonJacksonGetBoolean(entityNode, "is_platform_access_only");
    grouperBoxUser.isPlatformAccessOnly = booleanObject == null? false: booleanObject.booleanValue();
    
    booleanObject = GrouperUtil.jsonJacksonGetBoolean(entityNode, "is_sync_enabled");
    grouperBoxUser.isSyncEnabled = booleanObject == null? false: booleanObject.booleanValue();
    
    booleanObject = GrouperUtil.jsonJacksonGetBoolean(entityNode, "can_see_managed_users");
    grouperBoxUser.canSeeManagedUsers = booleanObject == null? false: booleanObject.booleanValue();
    
//    grouperBoxUser.setCreatedJson(GrouperUtil.jsonJacksonGetString(entityNode, "created_at"));
//    grouperBoxUser.setModifiedJson(GrouperUtil.jsonJacksonGetString(entityNode, "modified_at"));
    
    return grouperBoxUser;
  }

  public static void main(String[] args) {
    
    GrouperBoxUser grouperBoxUser = new GrouperBoxUser();
    
//    grouperBoxUser.setAccountEnabled(true);
//    grouperBoxUser.setDisplayName("dispName");
//    grouperBoxUser.setId("id");
//    grouperBoxUser.setMailNickname("mailNick");
//    grouperBoxUser.setOnPremisesImmutableId("onPrem");
//    grouperBoxUser.setUserPrincipalName("userPri");
//  
//    String json = GrouperUtil.jsonJacksonToString(grouperBoxUser.toJson(null));
//    System.out.println(json);
//    
//    grouperBoxUser = GrouperBoxUser.fromJson(GrouperUtil.jsonJacksonNode(json));
//    
//    System.out.println(grouperBoxUser.toString());
    
  }

  /**
   * convert from jackson json
   * @param groupNode
   * @return the group
   */
  public ObjectNode toJson(Set<String> fieldNamesToSet) {
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      result.put("id", this.id);
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {      
      result.put("name", this.name);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("login")) {      
      result.put("login", this.login);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("status")) {      
      result.put("status", this.status);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("spaceUsed")) {      
      result.put("space_used", this.spaceUsed);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("type")) {      
      result.put("type", this.type);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("role")) {      
      result.put("role", this.role);
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("maxUploadSize")) {      
      result.put("max_upload_size", this.maxUploadSize);
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("spaceAmount")) {      
      result.put("space_amount", this.spaceAmount);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("isExemptFromDeviceLimits")) {      
      result.put("is_exempt_from_device_limits", this.isExemptFromDeviceLimits);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("isExemptFromLoginVerification")) {      
      result.put("is_exempt_from_login_verification", this.isExemptFromLoginVerification);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("isExternalCollabRestricted")) {      
      result.put("is_external_collab_restricted", this.isExternalCollabRestricted);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("isPlatformAccessOnly")) {      
      result.put("is_platform_access_only", this.isPlatformAccessOnly);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("isSyncEnabled")) {      
      result.put("is_sync_enabled", this.isSyncEnabled);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("canSeeManagedUsers")) {      
      result.put("can_see_managed_users", this.canSeeManagedUsers);
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
  public static void createTableBoxUser(DdlVersionBean ddlVersionBean, Database database) {
  
    final String tableName = "mock_box_user";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
          
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "login", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "type", Types.VARCHAR, "20", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "role", Types.VARCHAR, "20", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "status", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "space_used", Types.BIGINT, null, false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "space_amount", Types.BIGINT, null, false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "max_upload_size", Types.BIGINT, null, false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "is_exempt_from_device_limits", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "is_exempt_from_login_verificat", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "is_external_collab_restricted", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "is_platform_access_only", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "is_sync_enabled", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "can_see_managed_users", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "created_at", Types.TIMESTAMP, null, false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "modified_at", Types.TIMESTAMP, null, false, false);
      
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_box_user_idx", false, "name");
    }
    
  }

}
