package edu.internet2.middleware.grouper.app.adobe;

import java.sql.Types;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperAdobeUser {

  private String id; // userId
  
  private String email;
  
  private String userName;
  
  private String status;
  
  private String type;
  
  private String firstName;
  
  private String lastName;
  
  private String domain;
  
  private String country;
  
  private Set<String> groups;
  

  /**
   * @param targetEntity
   * @param fieldNamesToSet - these are the grouper names in the adobe provisioner wiki.
   * @return
   */
  public static GrouperAdobeUser fromProvisioningEntity(ProvisioningEntity targetEntity, Set<String> fieldNamesToSet) {
    
    GrouperAdobeUser grouperAdobeUser = new GrouperAdobeUser();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      grouperAdobeUser.setId(targetEntity.getId());
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("email")) {      
      grouperAdobeUser.setEmail(targetEntity.getEmail());
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("userName")) {
      grouperAdobeUser.setUserName(targetEntity.retrieveAttributeValueString("userName"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("status")) {
      grouperAdobeUser.setStatus(targetEntity.retrieveAttributeValueString("status"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("type")) {
      grouperAdobeUser.setType(targetEntity.retrieveAttributeValueString("type"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("firstname")) {      
      grouperAdobeUser.setFirstName(targetEntity.retrieveAttributeValueString("firstname"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("lastname")) {      
      grouperAdobeUser.setLastName(targetEntity.retrieveAttributeValueString("lastname"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("domain")) {      
      grouperAdobeUser.setDomain(targetEntity.retrieveAttributeValueString("domain"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("country")) {      
      grouperAdobeUser.setCountry(targetEntity.retrieveAttributeValueString("country"));
    }
    
    return grouperAdobeUser;

  }
  
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity(false);
    
    targetEntity.setId(this.id);
    targetEntity.setEmail(this.email);
    targetEntity.setLoginId(this.userName);
    
    targetEntity.assignAttributeValue("status", this.status);
    targetEntity.assignAttributeValue("type", this.type);
    
    targetEntity.assignAttributeValue("firstName", this.firstName);
    targetEntity.assignAttributeValue("lastName", this.lastName);
    
    targetEntity.assignAttributeValue("domain", this.domain);
    targetEntity.assignAttributeValue("country", this.country);
    
    return targetEntity;
  }


  /**
   * convert from jackson json
   * @param entityNode
   * @param includeLoadedFields
   * @return the user
   */
  public static GrouperAdobeUser fromJson(JsonNode entityNode, boolean includeLoadedFields) {
    GrouperAdobeUser grouperAdobeUser = new GrouperAdobeUser();
    
    /**
     * {
    "id": "abc123",
    "email": "abc@school.edu",
    "status": "active",
    "groups": ["Group name 1", "Group name 2"],
    "username": "ABC@UPENN.EDU",
    "domain": "upenn.edu",
    "firstname": "Dave",
    "lastname": "Smith",
    "type": "federatedID",
    "country": "US"
  }
     */
    
    grouperAdobeUser.id = GrouperUtil.jsonJacksonGetString(entityNode, "id");
    grouperAdobeUser.email = GrouperUtil.jsonJacksonGetString(entityNode, "email");
    
    grouperAdobeUser.firstName = GrouperUtil.jsonJacksonGetString(entityNode, "firstname");
    grouperAdobeUser.lastName = GrouperUtil.jsonJacksonGetString(entityNode, "lastname");
    
    grouperAdobeUser.type = GrouperUtil.jsonJacksonGetString(entityNode, "type");
    grouperAdobeUser.country = GrouperUtil.jsonJacksonGetString(entityNode, "country");
  
    if (includeLoadedFields) {
      
      grouperAdobeUser.userName = GrouperUtil.jsonJacksonGetString(entityNode, "username");
      grouperAdobeUser.domain = GrouperUtil.jsonJacksonGetString(entityNode, "domain");
      grouperAdobeUser.status = GrouperUtil.jsonJacksonGetString(entityNode, "status");
      grouperAdobeUser.groups = GrouperUtil.jsonJacksonGetStringSet(entityNode, "groups");
    }
    
    return grouperAdobeUser;
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableAdobeUser(DdlVersionBean ddlVersionBean, Database database) {
  
    final String tableName = "mock_adobe_user";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
          
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_id", Types.VARCHAR, "100", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "status", Types.VARCHAR, "25", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "type", Types.VARCHAR, "25", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "first_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_name", Types.VARCHAR, "256", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "domain", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "country", Types.VARCHAR, "2", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_adobe_user_user_name", false, "user_name");
    }
    
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

  
  public String getUserName() {
    return userName;
  }

  
  public void setUserName(String userName) {
    this.userName = userName;
  }

  
  public String getStatus() {
    return status;
  }

  
  public void setStatus(String status) {
    this.status = status;
  }

  
  public String getType() {
    return type;
  }

  
  public void setType(String type) {
    this.type = type;
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

  
  public String getDomain() {
    return domain;
  }

  
  public void setDomain(String domain) {
    this.domain = domain;
  }

  
  public String getCountry() {
    return country;
  }

  
  public void setCountry(String country) {
    this.country = country;
  }

  
  public Set<String> getGroups() {
    return groups;
  }

  
  public void setGroups(Set<String> groups) {
    this.groups = groups;
  }
  
  /**
   * convert from jackson json
   * @return the user json node
   */
  public ObjectNode toJson(Set<String> fieldNamesToSet) {
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("email")) {      
      GrouperUtil.jsonJacksonAssignString(result, "email", this.email);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("country")) {      
      GrouperUtil.jsonJacksonAssignString(result, "country", this.country);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("firstName")) {      
      GrouperUtil.jsonJacksonAssignString(result, "firstname", this.firstName);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("lastName")) {      
      GrouperUtil.jsonJacksonAssignString(result, "lastname", this.lastName);
    }
    
    return result;
  }

  
}
