package edu.internet2.middleware.grouper.app.duo;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class GrouperDuoUser {

  // email, first_name, groups, last_name, real_name, user_id, user_name
  
  private String email;
  
  private String firstName;
  
  private Set<GrouperDuoGroup> groups;
  
  private String lastName;
  
  private String realName;
  
  private String id; // userId

  private String userName;
  
  private String alias1;
  
  private String alias2;
  
  private String alias3;
  
  private String alias4;
  
  // fields below are ready only. They are used to load data into grouper_prov_duo_user table
  /**
   * alphabetical list of comma separated phone numbers
   */
  private String phones;
  
  /**
   * T when at least one of the phone numbers has push notifications activated
   */
  private Boolean pushEnabled;
  
  /**
   * alphabetical list of comma separated aliases
   */
  private String aliases;
  
  /**
   * Is true if the user has a phone, hardware token, U2F token, or security key available for authentication. Otherwise, false.
   */
  private Boolean enrolled;
  
  /**
   * An integer indicating the last update to the user via directory sync as a Unix timestamp, or null if the user has never synced with an external directory or if the directory that originally created the user has been deleted from Duo.
   */
  private Long lastDirectorySync;
  
  /**
   * Notes about this user. Viewable in the Duo Admin Panel.
   */
  private String notes;
  
  /**
   * User status. One of "active", "bypass", "disabled", "locked out", "pending deletion"
   */
  private String status;
  
  /**
   * The user's creation date timestamp.
   */
  private Long createdAt;
  
  /**
   * An integer indicating the last time this user logged in, as a Unix timestamp, or null if the user has not logged in.
   */
  private Long lastLogin;
  
  
  public String getPushEnabledDb() {
    return pushEnabled == null ? "F" : pushEnabled ? "T" : "F";
  }

  
  public void setPushEnabledDb(String pushEnabled) {
    this.pushEnabled = GrouperUtil.booleanObjectValue(pushEnabled);
  }
  
  
  public Boolean getPushEnabled() {
    return pushEnabled;
  }
  
  public void setPushEnabled(Boolean pushEnabled) {
    this.pushEnabled = pushEnabled;
  }
  
  public Boolean getEnrolled() {
    return enrolled;
  }

  public void setEnrolled(Boolean enrolled) {
    this.enrolled = enrolled;
  }
  
  public String getEnrolledDb() {
    return enrolled == null ? "F" : enrolled ? "T" : "F";
  }
  
  public void setEnrolledDb(String enrolled) {
    this.enrolled = GrouperUtil.booleanObjectValue(enrolled);
  }

  /**
   * @param targetEntity
   * @param fieldNamesToSet - these are the grouper names in the duo provisioner wiki. They are: alias1, alias2, alias3, alias4, id, loginId, firstname, lastname, realname, email
   * @return
   */
  public static GrouperDuoUser fromProvisioningEntity(ProvisioningEntity targetEntity, Set<String> fieldNamesToSet) {
    
    GrouperDuoUser grouperDuoUser = new GrouperDuoUser();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("firstname")) {      
      grouperDuoUser.setFirstName(targetEntity.retrieveAttributeValueString("firstname"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("lastname")) {      
      grouperDuoUser.setLastName(targetEntity.retrieveAttributeValueString("lastname"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      grouperDuoUser.setId(targetEntity.getId());
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("email")) {      
      grouperDuoUser.setEmail(targetEntity.getEmail());
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("loginId")) {
      grouperDuoUser.setUserName(targetEntity.getLoginId());
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {      
      grouperDuoUser.setRealName(targetEntity.getName());
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("alias1")) {      
      grouperDuoUser.setAlias1(targetEntity.retrieveAttributeValueString("alias1"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("alias2")) {      
      grouperDuoUser.setAlias2(targetEntity.retrieveAttributeValueString("alias2"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("alias3")) {      
      grouperDuoUser.setAlias3(targetEntity.retrieveAttributeValueString("alias3"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("alias4")) {      
      grouperDuoUser.setAlias4(targetEntity.retrieveAttributeValueString("alias4"));
    }
    
    return grouperDuoUser;

  }
  
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity();
    
    targetEntity.assignAttributeValue("firstName", this.firstName);
    targetEntity.assignAttributeValue("lastName", this.lastName);
    targetEntity.assignAttributeValue("phones", this.phones);
    targetEntity.assignAttributeValue("isPushEnabled", this.getPushEnabledDb());
    targetEntity.assignAttributeValue("aliase1", this.alias1);
    targetEntity.assignAttributeValue("aliase2", this.alias2);
    targetEntity.assignAttributeValue("aliase3", this.alias3);
    targetEntity.assignAttributeValue("aliase4", this.alias4);
    targetEntity.assignAttributeValue("aliases", this.aliases);
    targetEntity.assignAttributeValue("isEnrolled", this.getEnrolledDb());
    targetEntity.assignAttributeValue("lastDirectorySync", this.lastDirectorySync);
    targetEntity.assignAttributeValue("notes", this.notes);
    targetEntity.assignAttributeValue("status", this.status);
    targetEntity.assignAttributeValue("lastLogin", this.lastLogin);
    targetEntity.assignAttributeValue("createdAt", this.createdAt);
    targetEntity.assignAttributeValue("userName", this.userName);
    
    targetEntity.setId(this.id);
    targetEntity.setEmail(this.email);
    targetEntity.setLoginId(this.userName);
    targetEntity.setName(this.realName);
    return targetEntity;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  
  public String getEmail() {
    return email;
  }

  
  public void setEmail(String email) {
    this.email = email;
  }

  
  public String getFirstName() {
    return firstName;
  }

  
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  
  
  
  public Set<GrouperDuoGroup> getGroups() {
    return groups;
  }

  
  public void setGroups(Set<GrouperDuoGroup> groups) {
    this.groups = groups;
  }

  public String getLastName() {
    return lastName;
  }

  
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  
  public String getRealName() {
    return realName;
  }

  
  public void setRealName(String realName) {
    this.realName = realName;
  }

  
  public String getUserName() {
    return userName;
  }

  
  public void setUserName(String userName) {
    this.userName = userName;
  }
  
  
  public String getPhones() {
    return phones;
  }

  
  public void setPhones(String phones) {
    this.phones = phones;
  }

  
  

  
  public String getAliases() {
    return aliases;
  }

  
  public void setAliases(String aliases) {
    this.aliases = aliases;
  }

  
  public Long getLastDirectorySync() {
    return lastDirectorySync;
  }

  
  public void setLastDirectorySync(Long lastDirectorySync) {
    this.lastDirectorySync = lastDirectorySync;
  }

  
  public String getNotes() {
    return notes;
  }

  
  public void setNotes(String notes) {
    this.notes = notes;
  }

  
  public String getStatus() {
    return status;
  }

  
  public void setStatus(String status) {
    this.status = status;
  }

  
  public Long getCreatedAt() {
    return createdAt;
  }

  
  public void setCreatedAt(Long createdAt) {
    this.createdAt = createdAt;
  }

  
  public Long getLastLogin() {
    return lastLogin;
  }

  
  public void setLastLogin(Long lastLogin) {
    this.lastLogin = lastLogin;
  }
  
  
  public String getAlias1() {
    return alias1;
  }

  public void setAlias1(String alias1) {
    this.alias1 = alias1;
  }

  public String getAlias2() {
    return alias2;
  }
  
  public void setAlias2(String alias2) {
    this.alias2 = alias2;
  }

  public String getAlias3() {
    return alias3;
  }


  
  public void setAlias3(String alias3) {
    this.alias3 = alias3;
  }


  
  public String getAlias4() {
    return alias4;
  }


  
  public void setAlias4(String alias4) {
    this.alias4 = alias4;
  }


  /**
   * convert from jackson json
   * @param entityNode
   * @param includeLoadedFields
   * @return the user
   */
  public static GrouperDuoUser fromJson(JsonNode entityNode, boolean includeLoadedFields) {
    GrouperDuoUser grouperDuoUser = new GrouperDuoUser();
    
    grouperDuoUser.firstName = GrouperUtil.jsonJacksonGetString(entityNode, "firstname");
    grouperDuoUser.lastName = GrouperUtil.jsonJacksonGetString(entityNode, "lastname");
    grouperDuoUser.realName = GrouperUtil.jsonJacksonGetString(entityNode, "realname");
    grouperDuoUser.userName = GrouperUtil.jsonJacksonGetString(entityNode, "username");
    grouperDuoUser.email = GrouperUtil.jsonJacksonGetString(entityNode, "email");
    grouperDuoUser.id = GrouperUtil.jsonJacksonGetString(entityNode, "user_id");
    
    grouperDuoUser.alias1 = GrouperUtil.jsonJacksonGetString(entityNode, "alias1");
    grouperDuoUser.alias2 = GrouperUtil.jsonJacksonGetString(entityNode, "alias2");
    grouperDuoUser.alias3 = GrouperUtil.jsonJacksonGetString(entityNode, "alias3");
    grouperDuoUser.alias4 = GrouperUtil.jsonJacksonGetString(entityNode, "alias4");
    
    ArrayNode groupsNode = (ArrayNode)GrouperUtil.jsonJacksonGetNode(entityNode, "groups");
    Set<GrouperDuoGroup> grouperDuoGroups = new HashSet<GrouperDuoGroup>();
    if (groupsNode != null) {
      Iterator<JsonNode> groupsIterator = groupsNode.iterator();
      while(groupsIterator.hasNext()) {
        JsonNode groupNode = groupsIterator.next();
        GrouperDuoGroup grouperDuoGroup = GrouperDuoGroup.fromJson(groupNode);
        grouperDuoGroups.add(grouperDuoGroup);
      }
    }
    
    grouperDuoUser.groups = grouperDuoGroups;
    
    if (includeLoadedFields) {
      
      List<String> aliases = new ArrayList<>();
      ObjectNode aliasesNode = (ObjectNode) GrouperUtil.jsonJacksonGetNode(entityNode, "aliases");
      if (aliasesNode != null) {
        Iterator<JsonNode> iterator = aliasesNode.elements();
        while (iterator.hasNext()) {
          String alias = iterator.next().asText();
          if (StringUtils.isNotBlank(alias)) {
            alias = alias.replace(",", "&#x2c;");
          }
          aliases.add(alias);
        }
      }
      
      if (aliases.size() > 0) {
        Collections.sort(aliases);
        grouperDuoUser.aliases = String.join(",", aliases);
      }
      
      List<String> phones = new ArrayList<>();
      ArrayNode phonesNode = (ArrayNode)GrouperUtil.jsonJacksonGetNode(entityNode, "phones");
      if (phonesNode != null) {
        Iterator<JsonNode> phonesIterator = phonesNode.iterator();
        while (phonesIterator.hasNext()) {
          JsonNode phoneNode = phonesIterator.next();
          String phoneNumber = GrouperUtil.jsonJacksonGetString(phoneNode, "number");
          phones.add(phoneNumber);
          
          Boolean activated = GrouperUtil.jsonJacksonGetBoolean(phoneNode, "activated");
          if (activated != null && activated) {
            grouperDuoUser.pushEnabled = true;
          }
        }
      }
      
      if (phones.size() > 0) {
        Collections.sort(phones);
        grouperDuoUser.phones = String.join(",", phones);
      }
      
      Boolean isEnrolled = GrouperUtil.jsonJacksonGetBoolean(entityNode, "is_enrolled");
      if (isEnrolled != null && isEnrolled) {
        grouperDuoUser.enrolled = true;
      }
      
      Long lastDirectorySync = GrouperUtil.jsonJacksonGetLong(entityNode, "last_directory_sync");
      if (lastDirectorySync != null) {
        grouperDuoUser.lastDirectorySync = lastDirectorySync;
      }
      
      String notes = GrouperUtil.jsonJacksonGetString(entityNode, "notes");
      grouperDuoUser.notes = notes;

      String status = GrouperUtil.jsonJacksonGetString(entityNode, "status");
      grouperDuoUser.status = status;
      
      Long created = GrouperUtil.jsonJacksonGetLong(entityNode, "created");
      if (created != null) {
        grouperDuoUser.createdAt = created;
      }

      Long lastLogin = GrouperUtil.jsonJacksonGetLong(entityNode, "last_login");
      if (lastLogin != null) {
        grouperDuoUser.lastLogin = lastLogin;
      }
    }
    
    return grouperDuoUser;
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableDuoUser(DdlVersionBean ddlVersionBean, Database database) {
  
    final String tableName = "mock_duo_user";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
          
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      // email, first_name, groups, last_name, real_name, user_id, user_name
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "first_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "real_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "phones", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "push_enabled", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "aliases", Types.VARCHAR, "256", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "alias1", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "alias2", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "alias3", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "alias4", Types.VARCHAR, "256", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "enrolled", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_directory_sync", Types.BIGINT, "15", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "notes", Types.VARCHAR, "512", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "status", Types.VARCHAR, "25", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "created_at", Types.BIGINT, "15", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_login", Types.BIGINT, "15", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_duo_user_unique_user_name", true, "user_name");
      
    }
    
  }

}
