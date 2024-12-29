package edu.internet2.middleware.grouper.app.okta;

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

public class GrouperOktaUser {
  
  private String firstName;
 
  private String lastName;
 
  private String id;
 
  private String email;
 
  private String login;
   

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

  
  public String getEmail() {
    return email;
  }

  
  public void setEmail(String email) {
    this.email = email;
  }

  
  public String getLogin() {
    return login;
  }

  
  public void setLogin(String login) {
    this.login = login;
  }

/**
  * @param targetEntity
  * @param fieldNamesToSet
  * @return
  */
 public static GrouperOktaUser fromProvisioningEntity(ProvisioningEntity targetEntity, Set<String> fieldNamesToSet) {
   
   GrouperOktaUser grouperOktaUser = new GrouperOktaUser();
   
   if (fieldNamesToSet == null || fieldNamesToSet.contains("firstName")) {      
     grouperOktaUser.setFirstName(targetEntity.retrieveAttributeValueString("firstName"));
   }
   if (fieldNamesToSet == null || fieldNamesToSet.contains("lastName")) {      
     grouperOktaUser.setLastName(targetEntity.retrieveAttributeValueString("lastName"));
   }
   if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
     grouperOktaUser.setId(targetEntity.getId());
   }
   if (fieldNamesToSet == null || fieldNamesToSet.contains("email")) {      
     grouperOktaUser.setEmail(targetEntity.getEmail());
   }
   if (fieldNamesToSet == null || fieldNamesToSet.contains("login")) {      
     grouperOktaUser.setLogin(targetEntity.retrieveAttributeValueString("login"));
   }
   
   return grouperOktaUser;

 }
 
 public ProvisioningEntity toProvisioningEntity() {
   
   ProvisioningEntity targetEntity = new ProvisioningEntity(false);
   
   targetEntity.assignAttributeValue("firstName", this.firstName);
   targetEntity.assignAttributeValue("lastName", this.lastName);
   targetEntity.assignAttributeValue("email", this.email);
   targetEntity.assignAttributeValue("login", this.login);
   targetEntity.setId(this.id);
   targetEntity.setEmail(this.email);
   return targetEntity;
 }

 public String getId() {
   return id;
 }

 public void setId(String id) {
   this.id = id;
 }

 /**
  * convert from jackson json
  * @param entityNode
  * @return the user
  */
 public static GrouperOktaUser fromJson(JsonNode entityNode) {
   GrouperOktaUser grouperOktaUser = new GrouperOktaUser();
   
   grouperOktaUser.id = GrouperUtil.jsonJacksonGetString(entityNode, "id");
   
   JsonNode profileNode = GrouperUtil.jsonJacksonGetNode(entityNode, "profile");
   
   grouperOktaUser.firstName = GrouperUtil.jsonJacksonGetString(profileNode, "firstName");
   grouperOktaUser.lastName = GrouperUtil.jsonJacksonGetString(profileNode, "lastName");
   grouperOktaUser.login = GrouperUtil.jsonJacksonGetString(profileNode, "login");
   grouperOktaUser.email = GrouperUtil.jsonJacksonGetString(profileNode, "email");
   
   return grouperOktaUser;
 }
 
 /**
  * convert from jackson json
  * @param groupNode
  * @return the group
  */
 public ObjectNode toJson(Set<String> fieldNamesToSet) {
   ObjectNode result = GrouperUtil.jsonJacksonNode();
   
   ObjectNode profileNode = GrouperUtil.jsonJacksonNode();
 
   if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {  
     GrouperUtil.jsonJacksonAssignString(result, "id", this.id);
   }
   
   if (fieldNamesToSet == null || fieldNamesToSet.contains("firstName")) {
     GrouperUtil.jsonJacksonAssignString(profileNode, "firstName", this.firstName);
   }

   if (fieldNamesToSet == null || fieldNamesToSet.contains("lastName")) {
     GrouperUtil.jsonJacksonAssignString(profileNode, "lastName", this.lastName);
   }
   
   if (fieldNamesToSet == null || fieldNamesToSet.contains("email")) {      
     GrouperUtil.jsonJacksonAssignString(profileNode, "email", this.email);
   }
   
   if (fieldNamesToSet == null || fieldNamesToSet.contains("login")) {      
     GrouperUtil.jsonJacksonAssignString(profileNode, "login", this.login);
   }
   
   result.set("profile", profileNode);
   
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
 public static void createTableOktaUser(DdlVersionBean ddlVersionBean, Database database) {
 
   final String tableName = "mock_okta_user";
 
   try {
     new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
   } catch (Exception e) {
         
     Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
     GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email", Types.VARCHAR, "256", false, false);
     GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "login", Types.VARCHAR, "256", false, false);
     GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "first_name", Types.VARCHAR, "256", false, false);
     GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_name", Types.VARCHAR, "256", false, false);
     GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
     
     GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_okta_user_unique_user_name", true, "login");
     
   }
   
 }

}
