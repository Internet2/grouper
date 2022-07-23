package edu.internet2.middleware.grouper.app.google;

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

public class GrouperGoogleUser {
  
 private String primaryEmail;
 
 private String givenName;
 
 private String familyName;
 
 private String id;
 
 private String password;
 
 
  public String getPrimaryEmail() {
    return primaryEmail;
  }
  
  
  public void setPrimaryEmail(String primaryEmail) {
    this.primaryEmail = primaryEmail;
  }
  
  
  public String getGivenName() {
    return givenName;
  }
  
  
  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }
  
  
  public String getFamilyName() {
    return familyName;
  }
  
  
  public void setFamilyName(String familyName) {
    this.familyName = familyName;
  }
  
  
  public String getPassword() {
    return password;
  }


public void setPassword(String password) {
  this.password = password;
}

/**
  * @param targetEntity
  * @param fieldNamesToSet
  * @return
  */
 public static GrouperGoogleUser fromProvisioningEntity(ProvisioningEntity targetEntity, Set<String> fieldNamesToSet) {
   
   GrouperGoogleUser grouperGoogleUser = new GrouperGoogleUser();
   
   if (fieldNamesToSet == null || fieldNamesToSet.contains("givenName")) {      
     grouperGoogleUser.setGivenName(targetEntity.retrieveAttributeValueString("givenName"));
   }
   if (fieldNamesToSet == null || fieldNamesToSet.contains("familyName")) {      
     grouperGoogleUser.setFamilyName(targetEntity.retrieveAttributeValueString("familyName"));
   }
   if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
     grouperGoogleUser.setId(targetEntity.getId());
   }
   if (fieldNamesToSet == null || fieldNamesToSet.contains("email")) {      
     grouperGoogleUser.setPrimaryEmail(targetEntity.getEmail());
   }
   
   return grouperGoogleUser;

 }
 
 public ProvisioningEntity toProvisioningEntity() {
   
   ProvisioningEntity targetEntity = new ProvisioningEntity();
   
   targetEntity.assignAttributeValue("givenName", this.givenName);
   targetEntity.assignAttributeValue("familyName", this.familyName);
   targetEntity.setId(this.id);
   targetEntity.setEmail(this.primaryEmail);
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
 public static GrouperGoogleUser fromJson(JsonNode entityNode) {
   GrouperGoogleUser grouperGoogleUser = new GrouperGoogleUser();
   
   grouperGoogleUser.primaryEmail = GrouperUtil.jsonJacksonGetString(entityNode, "primaryEmail");
   grouperGoogleUser.id = GrouperUtil.jsonJacksonGetString(entityNode, "id");
   
   JsonNode nameNode = GrouperUtil.jsonJacksonGetNode(entityNode, "name");
   
   grouperGoogleUser.givenName = GrouperUtil.jsonJacksonGetString(nameNode, "givenName");
   grouperGoogleUser.familyName = GrouperUtil.jsonJacksonGetString(nameNode, "familyName");
   
   return grouperGoogleUser;
 }
 
 /**
  * convert from jackson json
  * @param groupNode
  * @return the group
  */
 public ObjectNode toJson(Set<String> fieldNamesToSet) {
   ObjectMapper objectMapper = new ObjectMapper();
   ObjectNode result = objectMapper.createObjectNode();
 
   if (fieldNamesToSet == null || fieldNamesToSet.contains("email")) {      
     GrouperUtil.jsonJacksonAssignString(result, "primaryEmail", this.primaryEmail);
   }
   
   if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {  
     GrouperUtil.jsonJacksonAssignString(result, "id", this.id);
   }
   
   if (fieldNamesToSet == null || fieldNamesToSet.contains("password")) {      
     GrouperUtil.jsonJacksonAssignString(result, "password", this.password);
   }

   ObjectNode nameNode = null;
   
   if (fieldNamesToSet == null || fieldNamesToSet.contains("givenName")) {
     nameNode = GrouperUtil.jsonJacksonNode();
     GrouperUtil.jsonJacksonAssignString(nameNode, "givenName", this.givenName);
   }

   if (fieldNamesToSet == null || fieldNamesToSet.contains("familyName")) {
     if (nameNode == null) {
       nameNode = GrouperUtil.jsonJacksonNode();
     }
     GrouperUtil.jsonJacksonAssignString(nameNode, "familyName", this.familyName);
   }
   
   result.set("name", nameNode);
   
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
 public static void createTableGoogleUser(DdlVersionBean ddlVersionBean, Database database) {
 
   final String tableName = "mock_google_user";
 
   try {
     new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
   } catch (Exception e) {
         
     Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
     GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "primary_email", Types.VARCHAR, "256", false, false);
     GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "given_name", Types.VARCHAR, "256", false, false);
     GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "family_name", Types.VARCHAR, "256", false, false);
     GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
     
     GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_google_user_unique_user_name", true, "primary_email");
     
   }
   
 }

}
