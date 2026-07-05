package edu.internet2.middleware.grouper.app.google;

import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
   
   private String orgUnitPath;
 
  
  public String getOrgUnitPath() {
    return orgUnitPath;
  }
  
  
  
  public void setOrgUnitPath(String orgUnitPath) {
    this.orgUnitPath = orgUnitPath;
  }


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
   if (fieldNamesToSet == null || fieldNamesToSet.contains("orgUnitPath")) {      
     grouperGoogleUser.setOrgUnitPath(targetEntity.retrieveAttributeValueString("orgUnitPath"));
   }
   
   return grouperGoogleUser;

 }
 
 /**
  * Google native field name -> Grouper target-attribute name, EXCEPTIONS ONLY. Any name not in
  * this map is identical in both namespaces (givenName, familyName, orgUnitPath, id). This is the
  * single source of truth for the one name that differs, so the live read ({@link #toProvisioningEntity()}),
  * the live write ({@link #toJson(Set)}), and the sync-back capture
  * ({@code GrouperGoogleProvisioningTargetNativeSync}) can never disagree about it. Capturing the
  * matching attribute under the wrong name silently breaks fullSyncUsersFromSyncBack: a
  * cache-reconstructed user would fail to match and every member would be re-read from the target.
  */
 private static final Map<String, String> NATIVE_NAME_TO_GROUPER_NAME;
 private static final Map<String, String> GROUPER_NAME_TO_NATIVE_NAME;
 static {
   Map<String, String> nativeToGrouper = new HashMap<String, String>();
   // Google returns the user's email in "primaryEmail"; Grouper matches/manages it as "email".
   nativeToGrouper.put("primaryEmail", "email");
   Map<String, String> grouperToNative = new HashMap<String, String>();
   for (Map.Entry<String, String> entry : nativeToGrouper.entrySet()) {
     grouperToNative.put(entry.getValue(), entry.getKey());
   }
   NATIVE_NAME_TO_GROUPER_NAME = Collections.unmodifiableMap(nativeToGrouper);
   GROUPER_NAME_TO_NATIVE_NAME = Collections.unmodifiableMap(grouperToNative);
 }

 /**
  * Translate a Google native field name to the Grouper target-attribute name -- identity when the
  * name is not one of the exceptions in {@link #NATIVE_NAME_TO_GROUPER_NAME}.
  * @param nativeName the Google native (JSON/bean) field name
  * @return the Grouper target-attribute name
  */
 public static String nativeNameToGrouperName(String nativeName) {
   String grouperName = NATIVE_NAME_TO_GROUPER_NAME.get(nativeName);
   return grouperName != null ? grouperName : nativeName;
 }

 /**
  * Translate a Grouper target-attribute name to the Google native field name -- identity when the
  * name is not one of the exceptions in {@link #GROUPER_NAME_TO_NATIVE_NAME}.
  * @param grouperName the Grouper target-attribute name
  * @return the Google native (JSON/bean) field name
  */
 public static String grouperNameToNativeName(String grouperName) {
   String nativeName = GROUPER_NAME_TO_NATIVE_NAME.get(grouperName);
   return nativeName != null ? nativeName : grouperName;
 }

 /**
  * Grouper target-attribute name -> Google native (JSON/bean) field name, EXCEPTIONS ONLY (currently
  * just email -> primaryEmail). The sync-back capture layer uses this to normalize + auto-inject
  * renamed attributes so the shadow speaks grouper names. Unmodifiable, empty-safe.
  * @return the grouper-name -> native-name exceptions map
  */
 public static Map<String, String> grouperNameToNativeNameExceptions() {
   return GROUPER_NAME_TO_NATIVE_NAME;
 }

 public ProvisioningEntity toProvisioningEntity() {

   ProvisioningEntity targetEntity = new ProvisioningEntity(false);

   // name each attribute by its Grouper target-attribute name via the shared map (identity for all
   // but primaryEmail -> email), so a live read and a sync-back reconstruction produce the same shape.
   // NB: setEmail(x) is just assignAttributeValue("email", x), so the primaryEmail line is equivalent.
   targetEntity.assignAttributeValue(nativeNameToGrouperName("givenName"), this.givenName);
   targetEntity.assignAttributeValue(nativeNameToGrouperName("familyName"), this.familyName);
   targetEntity.assignAttributeValue(nativeNameToGrouperName("orgUnitPath"), this.orgUnitPath);
   targetEntity.setId(this.id);
   targetEntity.assignAttributeValue(nativeNameToGrouperName("primaryEmail"), this.primaryEmail);
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
   grouperGoogleUser.orgUnitPath = GrouperUtil.jsonJacksonGetString(entityNode, "orgUnitPath");
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
   ObjectNode result = GrouperUtil.jsonJacksonNode();
 
   if (fieldNamesToSet == null || fieldNamesToSet.contains("email")) {
     // grouper "email" -> native "primaryEmail" via the shared map (same source of truth as the read)
     GrouperUtil.jsonJacksonAssignString(result, grouperNameToNativeName("email"), this.primaryEmail);
   }
   
   if (fieldNamesToSet == null || fieldNamesToSet.contains("orgUnitPath")) {      
     GrouperUtil.jsonJacksonAssignString(result, "orgUnitPath", this.orgUnitPath);
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
     GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "org_unit_path", Types.VARCHAR, "40", false, false);
     
     GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_google_user_unique_user_name", true, "primary_email");
     
   }
   
 }

}
