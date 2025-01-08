package edu.internet2.middleware.grouper.app.okta;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
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
  
  private Map<String, String> customAttributes = new HashMap<>();
   

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
  
  


  public Map<String, String> getCustomAttributes() {
    return customAttributes;
  }


  
  public void setCustomAttributes(Map<String, String> customAttributes) {
    this.customAttributes = customAttributes;
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
   
   // populate custom attributes when fieldNamesToSet.contains a field that begins with profile.
   // or when fieldNamesToSet is null
   // look at all the attribute names that user configured;
   
   if (fieldNamesToSet == null) {     
     Map<String, GrouperProvisioningConfigurationAttribute> targetEntityAttributeNameToConfig = targetEntity.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig();
     for (String name: targetEntityAttributeNameToConfig.keySet()) {
       if (StringUtils.startsWith(name, "profile.")) {         
         grouperOktaUser.getCustomAttributes().put(name, targetEntity.retrieveAttributeValueString(name));
       }
     }
   } else {
     for (String name: fieldNamesToSet) {
       if (StringUtils.startsWith(name, "profile.")) {         
         grouperOktaUser.getCustomAttributes().put(name, targetEntity.retrieveAttributeValueString(name));
       }
     }
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
   
   if (this.customAttributes != null && this.customAttributes.size() > 0) {
     
     for (String name: customAttributes.keySet()) {
       targetEntity.assignAttributeValue(name, customAttributes.get(name));
     }
     
   }
   
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
   
   GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
   if (grouperProvisioner != null) {
     GrouperProvisioningConfiguration grouperProvisioningConfiguration = grouperProvisioner.retrieveGrouperProvisioningConfiguration();
     Map<String,GrouperProvisioningConfigurationAttribute> targetEntityAttributeNameToConfig = grouperProvisioningConfiguration.getTargetEntityAttributeNameToConfig();
     
     for (String name: targetEntityAttributeNameToConfig.keySet()) {
       if (StringUtils.startsWith(name, "profile.")) {
         String attributeNameInJson = name.substring(name.indexOf(".")+1);
         String customAttributeValue = GrouperUtil.jsonJacksonGetString(profileNode, attributeNameInJson);
         grouperOktaUser.customAttributes.put(name, customAttributeValue);
       }
     }
   }
   
   // give me all the okta configured provisioning attributes and if there are the matching ones between the json
   // response and configured attribute, load them into the customAttributes map
   
   return grouperOktaUser;
 }
 
 /**
  * convert from jackson json
  * @param groupNode
  * @return the group
  */
 public ObjectNode toJson() {
   ObjectNode result = GrouperUtil.jsonJacksonNode();
   
   ObjectNode profileNode = GrouperUtil.jsonJacksonNode();
 
   GrouperUtil.jsonJacksonAssignString(result, "id", this.id);
   
   GrouperUtil.jsonJacksonAssignString(profileNode, "firstName", this.firstName);

   GrouperUtil.jsonJacksonAssignString(profileNode, "lastName", this.lastName);
   
   GrouperUtil.jsonJacksonAssignString(profileNode, "email", this.email);
   
   GrouperUtil.jsonJacksonAssignString(profileNode, "login", this.login);
   
   if (customAttributes != null && customAttributes.size() > 0) {
     for (String attributeName: customAttributes.keySet()) {
       // remove "profile." from the attribute name 
       String fieldNameToSet = attributeName.substring(attributeName.indexOf(".")+1);
       GrouperUtil.jsonJacksonAssignString(profileNode, fieldNameToSet, customAttributes.get(attributeName));
     }
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
