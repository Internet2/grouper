package edu.internet2.middleware.grouper.app.remedyV2.digitalMarketplace;

import java.sql.Types;
import java.util.LinkedHashSet;
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

public class GrouperDigitalMarketplaceUser {
  
  /**
  * json for this user
  */
 private JsonNode jsonObject;
 
 /**
  * remedy id for a person
  */
 private String userId;

 /**
  * netId of user
  */
 private String loginName;
 
 
  public String getUserId() {
    return userId;
  }
  
  
  public void setUserId(String userId) {
    this.userId = userId;
  }
  
  
  public String getLoginName() {
    return loginName;
  }
  
  
  public void setLoginName(String loginName) {
    this.loginName = loginName;
  }
  
  /**
   * extensions of groups the user is in
   */
  private Set<String> groups = new LinkedHashSet<String>();
  
  /**
   * extensions of groups the user is in
   * @return the groups
   */
  public Set<String> getGroups() {
    return this.groups;
  }

  /**
    * json for this user
    * @return the json
    */
   public JsonNode getJsonObject() {
     return this.jsonObject;
   }
   
   /**
    * json for this user
    * @param jsonObject1 the json to set
    */
   public void setJsonObject(JsonNode jsonObject1) {
     this.jsonObject = jsonObject1;
   }

   
   public ProvisioningEntity toProvisioningEntity() {
     ProvisioningEntity targetEntity = new ProvisioningEntity(false);
     
     targetEntity.assignAttributeValue("loginName", this.loginName);
     targetEntity.assignAttributeValue("userId", this.userId);
     
     
     return targetEntity;
   }
   
   
   /**
    * 
    * @param targetEntity
    * @return
    */
   public static GrouperDigitalMarketplaceUser fromProvisioningEntity(ProvisioningEntity targetEntity, Set<String> fieldNamesToSet) {
     
     GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser = new GrouperDigitalMarketplaceUser();
     
     if (fieldNamesToSet == null || fieldNamesToSet.contains("loginName")) {      
       grouperDigitalMarketplaceUser.setLoginName(targetEntity.retrieveAttributeValueString("loginName"));
     }
     
     if (fieldNamesToSet == null || fieldNamesToSet.contains("userId")) {      
       grouperDigitalMarketplaceUser.setUserId(targetEntity.retrieveAttributeValueString("userId"));
     }
     
     return grouperDigitalMarketplaceUser;

   }

   /**
    * convert from jackson json
    * @param entityNode
    * @return the group
    */
   public static GrouperDigitalMarketplaceUser fromJson(JsonNode entityNode) {

     if (entityNode == null || !entityNode.has("loginName")) {
       return null;
     }

     GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser = new GrouperDigitalMarketplaceUser();
     
     grouperDigitalMarketplaceUser.loginName = GrouperUtil.jsonJacksonGetString(entityNode, "loginName");
     grouperDigitalMarketplaceUser.userId = GrouperUtil.jsonJacksonGetString(entityNode, "userId");

     return grouperDigitalMarketplaceUser;
   }

   /**
    * convert from jackson json
    * @param fieldNamesToSet
    */
   public ObjectNode toJson(Set<String> fieldNamesToSet) {
     ObjectNode result = GrouperUtil.jsonJacksonNode();
   
     if (fieldNamesToSet == null || fieldNamesToSet.contains("loginName")) {      
       GrouperUtil.jsonJacksonAssignString(result, "loginName", this.loginName);
     }
     
     if (fieldNamesToSet == null || fieldNamesToSet.contains("userId")) {      
       GrouperUtil.jsonJacksonAssignString(result, "userId", this.userId);
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
   public static void createTableDigitalMarketplaceUser(DdlVersionBean ddlVersionBean, Database database) {
   
     final String tableName = "mock_digital_marketplace_user";
   
     try {
       new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
     } catch (Exception e) {
       Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
       
       GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "login_name", Types.VARCHAR, "40", false, true);
       GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_id", Types.VARCHAR, "40", true, true);
     }
     
   }
}
