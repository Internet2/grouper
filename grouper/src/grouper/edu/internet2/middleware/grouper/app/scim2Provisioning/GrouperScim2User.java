package edu.internet2.middleware.grouper.app.scim2Provisioning;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperScim2User {

  public static void main(String[] args) {
    
    GrouperScim2User grouperScimUser = new GrouperScim2User();
    
    grouperScimUser.setActive(true);
    grouperScimUser.setCostCenter("costCent");
    grouperScimUser.setDisplayName("dispName");
    grouperScimUser.setEmailType("emailTy");
    grouperScimUser.setEmailValue("emailVal");
    grouperScimUser.setEmailType2("emailTy2");
    grouperScimUser.setEmailValue2("emailVal2");
    grouperScimUser.setEmployeeNumber("12345");
    grouperScimUser.setExternalId("extId");
    grouperScimUser.setFamilyName("famName");
    grouperScimUser.setFormattedName("formName");
    grouperScimUser.setGivenName("givName");
    grouperScimUser.setId("i");
    grouperScimUser.setMiddleName("midName");
    grouperScimUser.setUserName("userNam");
    grouperScimUser.setUserType("userTyp");
    grouperScimUser.setOrg("org");
  
    String json = GrouperUtil.jsonJacksonToString(grouperScimUser.toJson(null));
    System.out.println(json);
    
    grouperScimUser = GrouperScim2User.fromJson(GrouperUtil.jsonJacksonNode(json));
    
    System.out.println(grouperScimUser.toString());
    
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  public GrouperScim2User() {
  }

  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity();
    
    if (this.active != null) {
      targetEntity.assignAttributeValue("active", this.active);
    }

    if (this.costCenter != null) {
      targetEntity.assignAttributeValue("costCenter", this.costCenter);
    }
    
    if (this.org != null) {
      targetEntity.assignAttributeValue("org", this.org);
    }
    
    if (this.displayName != null) {
      targetEntity.assignAttributeValue("displayName", this.displayName);
    }
    
    if (this.emailType != null) {
      targetEntity.assignAttributeValue("emailType", this.emailType);
    }
    
    if (this.emailValue != null) {
      targetEntity.assignAttributeValue("emailValue", this.emailValue);
    }
    
    if (this.emailType2 != null) {
      targetEntity.assignAttributeValue("emailType2", this.emailType);
    }
    
    if (this.emailValue2 != null) {
      targetEntity.assignAttributeValue("emailValue2", this.emailValue);
    }
    
    if (this.employeeNumber != null) {
      targetEntity.assignAttributeValue("employeeNumber", this.employeeNumber);
    }
    
    if (this.externalId != null) {
      targetEntity.assignAttributeValue("externalId", this.externalId);
    }
    
    if (this.familyName != null) {
      targetEntity.assignAttributeValue("familyName", this.familyName);
    }
    
    if (this.formattedName != null) {
      targetEntity.assignAttributeValue("formattedName", this.formattedName);
    }
    
    if (this.givenName != null) {
      targetEntity.assignAttributeValue("givenName", this.givenName);
    }

    if (this.schemas != null) {
      targetEntity.assignAttributeValue("schemas", this.schemas);
    }

    if (this.id != null) {
      targetEntity.setId(this.id);
    }
    
    if (this.middleName != null) {
      targetEntity.assignAttributeValue("middleName", this.middleName);
    }
    
    if (this.userName != null) {
      targetEntity.assignAttributeValue("userName", this.userName);
    }
    
    if (this.userType != null) {
      targetEntity.assignAttributeValue("userType", this.userType);
    }
    
    return targetEntity;
  }

  /**
   * convert from jackson json
   * @param entityNode
   * @return the group
   */
  public static GrouperScim2User fromJson(JsonNode entityNode) {
    GrouperScim2User grouperScimUser = new GrouperScim2User();
    
    grouperScimUser.active = GrouperUtil.jsonJacksonGetBoolean(entityNode, "active");
    
    JsonNode enterpriseUserNode = entityNode.get("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User");
    if (enterpriseUserNode != null) {
      
      grouperScimUser.employeeNumber = GrouperUtil.jsonJacksonGetString(enterpriseUserNode, "employeeNumber");
      grouperScimUser.costCenter = GrouperUtil.jsonJacksonGetString(enterpriseUserNode, "costCenter");
      
    }
    
    grouperScimUser.displayName = GrouperUtil.jsonJacksonGetString(entityNode, "displayName");
    grouperScimUser.org = GrouperUtil.jsonJacksonGetString(entityNode, "org");
    
    if (entityNode.has("emails")) {
      ArrayNode emailsNode = (ArrayNode)entityNode.get("emails");
      JsonNode emailNode = null;
      JsonNode emailNode2 = null;
      if (emailsNode.size() == 1) {
        emailNode = emailsNode.get(0);
      } else {
        for (int i=0;i<emailsNode.size();i++) {
          JsonNode currentEmailNode = emailsNode.get(i);
          if (GrouperUtil.jsonJacksonGetBoolean(currentEmailNode, "primary", false)) {
            emailNode = currentEmailNode;
          } else {
            if (emailNode2 == null) {
              emailNode2 = currentEmailNode;
            }
          }
        }
        // uh... multiple emails and no primary... guess there isnt a real email
      }
      if (emailNode != null) {
        grouperScimUser.emailValue = GrouperUtil.jsonJacksonGetString(emailNode, "value");
        grouperScimUser.emailType = GrouperUtil.jsonJacksonGetString(emailNode, "type");
      }
      if (emailNode2 != null) {
        grouperScimUser.emailValue2 = GrouperUtil.jsonJacksonGetString(emailNode2, "value");
        grouperScimUser.emailType2 = GrouperUtil.jsonJacksonGetString(emailNode2, "type");
      }
    }

    grouperScimUser.externalId = GrouperUtil.jsonJacksonGetString(entityNode, "externalId");

    grouperScimUser.id = GrouperUtil.jsonJacksonGetString(entityNode, "id");

    JsonNode nameNode = entityNode.get("name");
    if (nameNode != null) {
      grouperScimUser.formattedName = GrouperUtil.jsonJacksonGetString(nameNode, "formatted");
      grouperScimUser.familyName = GrouperUtil.jsonJacksonGetString(nameNode, "familyName");
      grouperScimUser.givenName = GrouperUtil.jsonJacksonGetString(nameNode, "givenName");
      grouperScimUser.middleName = GrouperUtil.jsonJacksonGetString(nameNode, "middleName");
    }

    if (entityNode.get("schemas") != null) {
      Set<String> schemasStringSet = GrouperUtil.jsonJacksonGetStringSet(entityNode, "schemas");
      grouperScimUser.schemas = GrouperUtil.join(schemasStringSet.iterator(), ',');
    }

    grouperScimUser.userName = GrouperUtil.jsonJacksonGetString(entityNode, "userName");
    grouperScimUser.userType = GrouperUtil.jsonJacksonGetString(entityNode, "userType");
    
    return grouperScimUser;
  }

  /**
   * convert from jackson json
   * @param groupNode
   * @return the group
   */
  public ObjectNode toJson(Set<String> fieldNamesToSet) {
    
    //  {
    //    "active":true,
    //    "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User":{
    //       "employeeNumber":"12345",
    //       "costCenter":"costCent"
    //    },
    //    "id":"i",
    //    "displayName":"dispName",
    //    "emails":[
    //       {
    //          "value":"emailVal",
    //          "primary":true,
    //          "type":"emailTy"
    //       }
    //    ],
    //    "name":{
    //       "formatted":"formName",
    //       "familyName":"famName",
    //       "givenName":"givName",
    //       "middleName":"midName"
    //    },
    //    "externalId":"extId",
    //    "userName":"userNam",
    //    "userType":"userTyp"
    // }

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode result = objectMapper.createObjectNode();
  
    GrouperUtil.jsonJacksonAssignBoolean(result, "active", true);
    
//    if (fieldNamesToSet == null || fieldNamesToSet.contains("active")) {      
//      GrouperUtil.jsonJacksonAssignBoolean(result, "active", this.active);
//    }
    
    if (fieldNamesToSet == null || (fieldNamesToSet.contains("employeeNumber") || fieldNamesToSet.contains("costCenter"))) {      
      if (!StringUtils.isBlank(this.employeeNumber) || !StringUtils.isBlank(this.costCenter)) {
        ObjectNode userExtension = GrouperUtil.jsonJacksonNode();
        if (fieldNamesToSet == null || fieldNamesToSet.contains("employeeNumber")) {
          GrouperUtil.jsonJacksonAssignString(userExtension, "employeeNumber", this.employeeNumber);
        }
        if (fieldNamesToSet == null || fieldNamesToSet.contains("costCenter")) {
          GrouperUtil.jsonJacksonAssignString(userExtension, "costCenter", this.costCenter);
        }
        result.set("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User", userExtension);
      }
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      GrouperUtil.jsonJacksonAssignString(result, "id", this.id);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("displayName")) {      
      GrouperUtil.jsonJacksonAssignString(result, "displayName", this.displayName);
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("org")) {      
      GrouperUtil.jsonJacksonAssignString(result, "org", this.org);
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("emailValue") || fieldNamesToSet.contains("emailValue2")) {     
      if (!StringUtils.isBlank(this.emailValue) || !StringUtils.isBlank(this.emailValue2)) {

        ArrayNode emailsNode = GrouperUtil.jsonJacksonArrayNode();
        boolean hasPrimary = false;
        if (!StringUtils.isBlank(this.emailValue)) {
          ObjectNode emailNode = GrouperUtil.jsonJacksonNode();
          GrouperUtil.jsonJacksonAssignString(emailNode, "value", this.emailValue);
          GrouperUtil.jsonJacksonAssignBoolean(emailNode, "primary", true);
          if (fieldNamesToSet == null || fieldNamesToSet.contains("emailType")) {
            GrouperUtil.jsonJacksonAssignString(emailNode, "type", this.emailType);
          }
          emailsNode.add(emailNode);
          hasPrimary = true;
        }
        if (!StringUtils.isBlank(this.emailValue2)) {
          ObjectNode emailNode = GrouperUtil.jsonJacksonNode();
          GrouperUtil.jsonJacksonAssignString(emailNode, "value", this.emailValue2);
          GrouperUtil.jsonJacksonAssignBoolean(emailNode, "primary", !hasPrimary);
          if (fieldNamesToSet == null || fieldNamesToSet.contains("emailType2")) {
            GrouperUtil.jsonJacksonAssignString(emailNode, "type", this.emailType2);
          }
          emailsNode.add(emailNode);
        }
        
        result.set("emails", emailsNode);
      }
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("formattedName")
        || fieldNamesToSet.contains("familyName") || fieldNamesToSet.contains("givenName")
        || fieldNamesToSet.contains("givenName")) {
      if (!StringUtils.isBlank(this.formattedName) || !StringUtils.isBlank(this.familyName)
          || !StringUtils.isBlank(this.givenName) || !StringUtils.isBlank(this.givenName)) {
        
        ObjectNode nameNode = GrouperUtil.jsonJacksonNode();
        if (fieldNamesToSet == null || fieldNamesToSet.contains("formattedName")) {
          GrouperUtil.jsonJacksonAssignString(nameNode, "formatted", this.formattedName);
        }
        if (fieldNamesToSet == null || fieldNamesToSet.contains("familyName")) {
          GrouperUtil.jsonJacksonAssignString(nameNode, "familyName", this.familyName);
        }
        if (fieldNamesToSet == null || fieldNamesToSet.contains("givenName")) {
          GrouperUtil.jsonJacksonAssignString(nameNode, "givenName", this.givenName);
        }
        if (fieldNamesToSet == null || fieldNamesToSet.contains("middleName")) {
          GrouperUtil.jsonJacksonAssignString(nameNode, "middleName", this.middleName);
        }
        result.set("name", nameNode);
      }
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("externalId")) {      
      GrouperUtil.jsonJacksonAssignString(result, "externalId", this.externalId);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("userName")) {      
      GrouperUtil.jsonJacksonAssignString(result, "userName", this.userName);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("userType")) {      
      GrouperUtil.jsonJacksonAssignString(result, "userType", this.userType);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("schemas")) {      
      if (!StringUtils.isBlank(this.schemas)) {
        GrouperUtil.jsonJacksonAssignStringArray(result, "schemas", GrouperUtil.splitTrimToSet(this.schemas, ","));
      }
    }
    
    return result;
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableScimUser(DdlVersionBean ddlVersionBean, Database database) {
  
    final String tableName = "mock_scim_user";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
          
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "active", Types.VARCHAR, "1", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "cost_center", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "display_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email_type", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email_value", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email_type2", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email_value2", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "employee_number", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "external_id", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "family_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "formatted_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "given_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "middle_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_type", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "org", Types.VARCHAR, "256", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_scim_user_name_idx", false, "user_name");
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_scim_user_name_org_idx", false, "user_name", "org");
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_scim_user_empn_idx", false, "employee_number");
    }
    
  }

  /**
   * emails[0]['value']
   */
  public static Pattern arrayIndexPattern = Pattern.compile("^([^\\[]+)\\[(\\d)+\\]\\['([^\\]]*)'\\]$");

  /**
   * emails.value eq "emailVal"
   */
  public static Pattern objectFieldEqPattern = Pattern.compile("^([^.]+)\\.([^\\s]+)\\s+eq\\s+\"([^\"]+)\"$");

  /**
   * emails[value eq "emailVal"]
   */
  public static Pattern objectIndexFieldEqPattern = Pattern.compile("^([^\\[]+)\\[([^\\s]+) eq \"([^\"]+)\"\\]$");
  
  /**
   * see if this scim path matches the current email 
   * @param path
   */
  public void validateEmail(String path) {
    
    // arrayIndexPattern emails[0]['value']
    Matcher matcher = arrayIndexPattern.matcher(path);
    String field = null;
    String emailField = null;
    Integer index = null;
    String value = null;
    
    if (matcher.matches()) {
      
      index = GrouperUtil.intValue(matcher.group(2));
      
    } else {
      
      // objectFieldEqPattern emails.value eq "emailVal"
      matcher = objectFieldEqPattern.matcher(path);
      if (!matcher.matches()) {
        
        // objectIndexFieldEqPattern emails[value eq "emailVal"]
        matcher = objectIndexFieldEqPattern.matcher(path);
        
        if (!matcher.matches()) {
          throw new RuntimeException("Invalid field expression '" + path + "'");
        }
      }
      emailField = matcher.group(2);
    }
    
    field = matcher.group(1);
    value = matcher.group(3); 

    if (!"emails".equalsIgnoreCase(field)) {
      throw new RuntimeException("Expecting emails but received '" + field + "'");
    }

    if (index != null) {
      if (index != 0) {
        throw new RuntimeException("Expecting index 0 but received " + index);
      }
    } else {
      
      if ("value".equals(emailField)) {
        if (!StringUtils.equals(value, emailValue)) {
          throw new RuntimeException("Expected value '" + this.emailValue + "' but received '" + value + "'");
        }
      } else if ("type".equals(emailField)) {
        
        if (!StringUtils.equals(value, emailType)) {
          throw new RuntimeException("Expected value '" + this.emailType + "' but received '" + value + "'");
        }
      } else {
        throw new RuntimeException("Expected email field 'value' or 'type' but received '" + emailField + "'");
      }
      
    }
    
  }
  
  private String id;

  private String externalId;
  
  private String userName;
  
  private String formattedName;
  
  private String familyName;
  
  private String givenName;

  private String schemas;

  private String middleName;
  
  private String displayName;
  
  private String emailValue;
  
  private String emailType;
  
  private String userType;
  
  private Boolean active = true;
  
  private String employeeNumber;
  
  private String costCenter;
  
  private String org;

  private String emailType2;

  private String emailValue2;

  
  public String getId() {
    return id;
  }

  
  public void setId(String id) {
    this.id = id;
  }

  
  public String getExternalId() {
    return externalId;
  }

  
  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  
  public String getUserName() {
    return userName;
  }

  
  public void setUserName(String userName) {
    this.userName = userName;
  }

  
  public String getFormattedName() {
    return formattedName;
  }

  
  public void setFormattedName(String formattedName) {
    this.formattedName = formattedName;
  }

  
  public String getFamilyName() {
    return familyName;
  }

  
  public void setFamilyName(String familyName) {
    this.familyName = familyName;
  }

  
  
  public String getSchemas() {
    return schemas;
  }

  
  public void setSchemas(String schemas) {
    this.schemas = schemas;
  }

  public String getGivenName() {
    return givenName;
  }

  
  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  
  public String getMiddleName() {
    return middleName;
  }

  
  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }

  
  public String getDisplayName() {
    return displayName;
  }

  
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  
  public String getEmailValue() {
    return emailValue;
  }

  
  public void setEmailValue(String emailValue) {
    this.emailValue = emailValue;
  }

  
  public String getEmailType() {
    return emailType;
  }

  
  public void setEmailType(String emailType) {
    this.emailType = emailType;
  }

  
  
  public String getEmailType2() {
    return emailType2;
  }

  
  public void setEmailType2(String emailType2) {
    this.emailType2 = emailType2;
  }

  
  public String getEmailValue2() {
    return emailValue2;
  }

  
  public void setEmailValue2(String emailValue2) {
    this.emailValue2 = emailValue2;
  }

  public String getUserType() {
    return userType;
  }

  
  public void setUserType(String userType) {
    this.userType = userType;
  }

  
  public Boolean getActive() {
    return active;
  }

  
  public void setActive(Boolean active) {
    this.active = active;
  }

  
  public String getEmployeeNumber() {
    return employeeNumber;
  }

  
  public void setEmployeeNumber(String employeeNumber) {
    this.employeeNumber = employeeNumber;
  }

  
  public String getCostCenter() {
    return costCenter;
  }

  
  public void setCostCenter(String costCenter) {
    this.costCenter = costCenter;
  }

  public String getActiveDb() {
    return active == null ? null : (active ? "T" : "F");
  }
  public void setActiveDb(String theActive) {
    this.active = GrouperUtil.booleanObjectValue(theActive);
  }
  
  
  public String getOrg() {
    return org;
  }
  
  public void setOrg(String org) {
    this.org = org;
  }

  /**
   * 
   * @param targetEntity
   * @return
   */
  public static GrouperScim2User fromProvisioningEntity(ProvisioningEntity targetEntity, Set<String> fieldNamesToSet) {
    
    GrouperScim2User grouperScim2User = new GrouperScim2User();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("active")) {      
      grouperScim2User.setActive(GrouperUtil.booleanObjectValue(targetEntity.retrieveAttributeValueString("active")));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("costCenter")) {      
      grouperScim2User.setCostCenter(targetEntity.retrieveAttributeValueString("costCenter"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("org")) {      
      grouperScim2User.setOrg(targetEntity.retrieveAttributeValueString("org"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("displayName")) {      
      grouperScim2User.setDisplayName(targetEntity.retrieveAttributeValueString("displayName"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("emailType")) {      
      grouperScim2User.setEmailType(targetEntity.retrieveAttributeValueString("emailType"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("emailValue")) {      
      grouperScim2User.setEmailValue(targetEntity.retrieveAttributeValueString("emailValue"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("emailType2")) {      
      grouperScim2User.setEmailType2(targetEntity.retrieveAttributeValueString("emailType2"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("emailValue2")) {      
      grouperScim2User.setEmailValue2(targetEntity.retrieveAttributeValueString("emailValue2"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("employeeNumber")) {      
      grouperScim2User.setEmployeeNumber(targetEntity.retrieveAttributeValueString("employeeNumber"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("externalId")) {      
      grouperScim2User.setExternalId(targetEntity.retrieveAttributeValueString("externalId"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("familyName")) {      
      grouperScim2User.setFamilyName(targetEntity.retrieveAttributeValueString("familyName"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("formattedName")) {      
      grouperScim2User.setFormattedName(targetEntity.retrieveAttributeValueString("formattedName"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("givenName")) {      
      grouperScim2User.setGivenName(targetEntity.retrieveAttributeValueString("givenName"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      grouperScim2User.setId(targetEntity.getId());
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("middleName")) {      
      grouperScim2User.setMiddleName(targetEntity.retrieveAttributeValueString("middleName"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("userName")) {      
      grouperScim2User.setUserName(targetEntity.retrieveAttributeValueString("userName"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("userType")) {      
      grouperScim2User.setUserType(targetEntity.retrieveAttributeValueString("userType"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("schemas")) {      
      grouperScim2User.setSchemas(targetEntity.retrieveAttributeValueString("schemas"));
    }
    
    return grouperScim2User;
  
  }
  
  
  
}
