package edu.internet2.middleware.grouper.app.freshServiceAgent;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class FreshAgentUser {
  
  public static void createTableFreshUser(DdlVersionBean ddlVersionBean, Database database) {
    
    final String tableName = "mock_freshagent_user";
    
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "first_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.BIGINT, "20", true, true);
      
      // Additional mock fields (nullable)
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "occasional", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "job_title", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "work_phone_number", Types.VARCHAR, "50", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "department_id", Types.BIGINT, "20", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "reporting_manager_id", Types.BIGINT, "20", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "address", Types.VARCHAR, "512", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "external_id", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "custom_fields", Types.VARCHAR, "4000", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "roles_json", Types.VARCHAR, "4000", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "active", Types.VARCHAR, "1", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_freshagent_user_name_idx", true, "email");
    }
    
  }
  
  private Long id;
  private String firstName;
  private String lastName;
  private String email;
  
  /**
   * occasional (day pass) agent if true, full-time (licensed seat) agent if false
   */
  private Boolean occasional;
  private String jobTitle;
  private String workPhoneNumber;
  private Long departmentId;
  private Long reportingManagerId;
  private String address;
  private String externalId;

  /**
   * custom_fields from Freshservice. Keys are arbitrary, values must be String, Long, or Boolean.
   */
  private Map<String, Object> customFields = new HashMap<>();

  /**
   * roles array from Freshservice as raw json, e.g. [{"role_id":123,"assignment_scope":"entire_helpdesk"}].
   * Freshservice requires at least one role when an agent is created.  This is not a provisioning
   * attribute, it is carried so a create can send a default role (see FreshAgentConfiguration)
   */
  private String rolesJson;

  private Boolean active;
  
  /** Prefix for provisioning entity attributes which represent a Freshservice custom field. */
  public static final String CUSTOM_FIELD_ATTRIBUTE_PREFIX = "customField_";

  /**
   * Get the Agent's ID
   * @return the Agent's ID
   */
  public Long getId() {
    return id;
  }

  /**
   * Set the Agent's ID
   * @param id the new ID to set
   */
  public void setId(Long id) {
    this.id = id;
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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Boolean getOccasional() {
    return occasional;
  }

  public void setOccasional(Boolean occasional) {
    this.occasional = occasional;
  }

  /**
   * Hibernate-friendly "T"/"F" mapping to support databases that store booleans as strings.
   */
  public String getOccasionalString() {
    return booleanToTf(this.occasional);
  }

  /**
   * Hibernate-friendly "T"/"F" mapping to support databases that store booleans as strings.
   */
  public void setOccasionalString(String occasionalString) {
    this.occasional = tfToBoolean(occasionalString);
  }

  public String getRolesJson() {
    return rolesJson;
  }

  public void setRolesJson(String rolesJson) {
    this.rolesJson = rolesJson;
  }

  /**
   * see if this agent has a non-empty roles array
   * @return true if roles json is a non-empty array
   */
  public boolean hasRoles() {
    if (GrouperUtil.isBlank(this.rolesJson)) {
      return false;
    }
    JsonNode rolesNode = GrouperUtil.jsonJacksonNode(this.rolesJson);
    return rolesNode != null && rolesNode.isArray() && rolesNode.size() > 0;
  }

  /**
   * assign a single role to this agent, e.g. a default role for a new agent since Freshservice
   * requires a role on create: [{"role_id": roleId, "assignment_scope": assignmentScope}]
   * @param roleId Freshservice role id
   * @param assignmentScope e.g. entire_helpdesk
   */
  public void assignRole(long roleId, String assignmentScope) {
    ObjectNode roleNode = GrouperUtil.jsonJacksonNode();
    roleNode.put("role_id", roleId);
    roleNode.put("assignment_scope", assignmentScope);
    ArrayNode rolesArray = GrouperUtil.jsonJacksonArrayNode();
    rolesArray.add(roleNode);
    this.rolesJson = GrouperUtil.jsonJacksonToString(rolesArray);
  }

  public String getJobTitle() {
    return jobTitle;
  }

  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  public String getWorkPhoneNumber() {
    return workPhoneNumber;
  }

  public void setWorkPhoneNumber(String workPhoneNumber) {
    this.workPhoneNumber = workPhoneNumber;
  }

  public Long getDepartmentId() {
    return departmentId;
  }

  public void setDepartmentId(Long departmentId) {
    this.departmentId = departmentId;
  }

  public Long getReportingManagerId() {
    return reportingManagerId;
  }

  public void setReportingManagerId(Long reportingManagerId) {
    this.reportingManagerId = reportingManagerId;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public Map<String, Object> getCustomFields() {
    return customFields;
  }

  public void setCustomFields(Map<String, Object> customFields) {
    this.customFields = customFields == null ? new HashMap<String, Object>() : normalizeCustomFields(customFields);
  }

  /**
   * Hibernate mapping helper: persist customFields map as JSON in the custom_fields column.
   */
  public String getCustomFieldsJson() {
    if (this.customFields == null || this.customFields.isEmpty()) {
      return null;
    }
    try {
      return GrouperUtil.objectMapper.writeValueAsString(this.customFields);
    } catch (Exception e) {
      throw new RuntimeException("Unable to serialize FreshAgentUser.customFields to JSON", e);
    }
  }

  /**
   * Hibernate mapping helper: load customFields map from JSON stored in the custom_fields column.
   */
  public void setCustomFieldsJson(String customFieldsJson) {
    if (GrouperUtil.isBlank(customFieldsJson)) {
      this.customFields = new HashMap<>();
      return;
    }
    try {
      Map<String, Object> customFieldsMap = GrouperUtil.objectMapper.readValue(customFieldsJson,
          new TypeReference<Map<String, Object>>() {
          });
      this.customFields = customFieldsMap == null ? new HashMap<>() : normalizeCustomFields(customFieldsMap, customFieldsJson);
    } catch (RuntimeException re) {
      // already has helpful context, don't wrap again
      throw re;
    } catch (Exception e) {
      throw new RuntimeException("Unable to parse FreshAgentUser.customFieldsJson. json='" + customFieldsJson + "'", e);
    }
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  /**
   * Hibernate-friendly "T"/"F" mapping to support databases that store booleans as strings.
   */
  public String getActiveString() {
    return booleanToTf(this.active);
  }

  /**
   * Hibernate-friendly "T"/"F" mapping to support databases that store booleans as strings.
   */
  public void setActiveString(String activeString) {
    this.active = tfToBoolean(activeString);
  }

  private static String booleanToTf(Boolean value) {
    if (value == null) {
      return null;
    }
    return value.booleanValue() ? "T" : "F";
  }

  private static Boolean tfToBoolean(String value) {
    if (GrouperUtil.isBlank(value)) {
      return null;
    }
    String trimmed = value.trim();
    if ("T".equalsIgnoreCase(trimmed) || "true".equalsIgnoreCase(trimmed) || "1".equals(trimmed)) {
      return Boolean.TRUE;
    }
    if ("F".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed) || "0".equals(trimmed)) {
      return Boolean.FALSE;
    }
    return null;
  }

  /**
   * Convert to a Grouper provisioning entity
   * @return the converted entity
   */
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity(false);
    
    if (this.id != null) {
      targetEntity.assignAttributeValue("id", this.id);
    }
    targetEntity.assignAttributeValue("firstName", this.firstName);
    targetEntity.assignAttributeValue("lastName", this.lastName);
    targetEntity.assignAttributeValue("email", this.email);

    if (this.occasional != null) {
      targetEntity.assignAttributeValue("occasional", this.occasional);
    }
    targetEntity.assignAttributeValue("jobTitle", this.jobTitle);
    targetEntity.assignAttributeValue("workPhoneNumber", this.workPhoneNumber);
    if (this.departmentId != null) {
      targetEntity.assignAttributeValue("departmentId", this.departmentId);
    }
    if (this.reportingManagerId != null) {
      targetEntity.assignAttributeValue("reportingManagerId", this.reportingManagerId);
    }
    targetEntity.assignAttributeValue("address", this.address);
    targetEntity.assignAttributeValue("externalId", this.externalId);

    // Custom fields are represented as individual provisioning attributes: customField_<fieldName>
    if (this.customFields != null && !this.customFields.isEmpty()) {
      for (Map.Entry<String, Object> entry : this.customFields.entrySet()) {
        String fieldName = entry.getKey();
        Object value = entry.getValue();
        if (GrouperUtil.isBlank(fieldName) || value == null) {
          continue;
        }
        targetEntity.assignAttributeValue(CUSTOM_FIELD_ATTRIBUTE_PREFIX + fieldName, value);
      }
    }

    if (this.active != null) {
      targetEntity.assignAttributeValue("active", this.active);
    }
    
    return targetEntity;
  }
  
  /**
   * Convert from a provisioning entity to a Agent
   * @param targetEntity the Grouper provisioning entity to convert
   * @param fieldNamesToSet the field names to be set
   * @return the Agent created from the provisioning entity
   */
  public static FreshAgentUser fromProvisioningEntity(ProvisioningEntity targetEntity, Set<String> fieldNamesToSet) {
    FreshAgentUser grouperAgentUser = new FreshAgentUser();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {
      if (targetEntity.getId() == null) {
        grouperAgentUser.setId(null);
      } else {
        grouperAgentUser.setId(Long.parseLong(targetEntity.getId()));
      }
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("firstName")) {
      grouperAgentUser.setFirstName(targetEntity.retrieveAttributeValueString("firstName"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("lastName")) {
      grouperAgentUser.setLastName(targetEntity.retrieveAttributeValueString("lastName"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("email")) {
      grouperAgentUser.setEmail(targetEntity.retrieveAttributeValueString("email"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("occasional")) {
      grouperAgentUser.setOccasional(targetEntity.retrieveAttributeValueBoolean("occasional"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("jobTitle")) {
      grouperAgentUser.setJobTitle(targetEntity.retrieveAttributeValueString("jobTitle"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("workPhoneNumber")) {
      grouperAgentUser.setWorkPhoneNumber(targetEntity.retrieveAttributeValueString("workPhoneNumber"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("departmentId")) {
      String departmentIdString = targetEntity.retrieveAttributeValueString("departmentId");
      if (!GrouperUtil.isBlank(departmentIdString)) {
        grouperAgentUser.setDepartmentId(Long.parseLong(departmentIdString));
      }
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("reportingManagerId")) {
      String reportingManagerIdString = targetEntity.retrieveAttributeValueString("reportingManagerId");
      if (!GrouperUtil.isBlank(reportingManagerIdString)) {
        grouperAgentUser.setReportingManagerId(Long.parseLong(reportingManagerIdString));
      }
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("address")) {
      grouperAgentUser.setAddress(targetEntity.retrieveAttributeValueString("address"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("externalId")) {
      grouperAgentUser.setExternalId(targetEntity.retrieveAttributeValueString("externalId"));
    }

    // Custom fields: provisioned as attributes named customField_<fieldName>
    if (fieldNamesToSet == null) {
      // best effort: pull any attributes that start with customField_
      Map<String, Object> customFieldsToSet = null;
      for (String attrName : targetEntity.retrieveAttributes().keySet()) {
        if (GrouperUtil.isBlank(attrName) || !attrName.startsWith(CUSTOM_FIELD_ATTRIBUTE_PREFIX)) {
          continue;
        }
        String customFieldName = attrName.substring(CUSTOM_FIELD_ATTRIBUTE_PREFIX.length());
        if (GrouperUtil.isBlank(customFieldName)) {
          continue;
        }

        Object value = targetEntity.retrieveAttributeValue(attrName);
        if (value == null) {
          continue;
        }

        if (customFieldsToSet == null) {
          customFieldsToSet = new HashMap<>();
        }
        customFieldsToSet.put(customFieldName, value);
      }

      if (customFieldsToSet != null) {
        grouperAgentUser.setCustomFields(customFieldsToSet);
      }
    } else {
      Map<String, Object> customFieldsToSet = null;
      for (String attributeName : fieldNamesToSet) {
        if (GrouperUtil.isBlank(attributeName)) {
          continue;
        }
        if (!attributeName.startsWith(CUSTOM_FIELD_ATTRIBUTE_PREFIX)) {
          // not a custom field attribute name
          continue;
        }

        String fieldName = attributeName.substring(CUSTOM_FIELD_ATTRIBUTE_PREFIX.length());
        if (GrouperUtil.isBlank(fieldName)) {
          continue;
        }

        Object value = targetEntity.retrieveAttributeValue(attributeName);
        if (value == null) {
          continue;
        }

        if (customFieldsToSet == null) {
          customFieldsToSet = new HashMap<>();
        }
        customFieldsToSet.put(fieldName, value);
      }

      if (customFieldsToSet != null) {
        grouperAgentUser.setCustomFields(customFieldsToSet);
      }
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("active")) {
      grouperAgentUser.setActive(targetEntity.retrieveAttributeValueBoolean("active"));
    }
    
    return grouperAgentUser;
  }
  
  /**
   * Get a GrouperAgent object from json Freshservice response
   * @param entityNode the node containing the GrouperAgent
   * @return the GrouperAgent object
   */
  public static FreshAgentUser fromJson(JsonNode entityNode) {
    if (entityNode == null) {
      return null;
    }
    
    FreshAgentUser grouperAgentUser = new FreshAgentUser();
    
    grouperAgentUser.id = GrouperUtil.jsonJacksonGetLong(entityNode, "id");
    
    grouperAgentUser.firstName = GrouperUtil.jsonJacksonGetString(entityNode, "first_name");
    grouperAgentUser.lastName = GrouperUtil.jsonJacksonGetString(entityNode, "last_name");
    grouperAgentUser.email = GrouperUtil.jsonJacksonGetString(entityNode, "email");

    grouperAgentUser.occasional = GrouperUtil.jsonJacksonGetBoolean(entityNode, "occasional");

    JsonNode rolesNode = GrouperUtil.jsonJacksonGetNode(entityNode, "roles");
    if (rolesNode != null && rolesNode.isArray()) {
      grouperAgentUser.rolesJson = GrouperUtil.jsonJacksonToString(rolesNode);
    }
    grouperAgentUser.jobTitle = GrouperUtil.jsonJacksonGetString(entityNode, "job_title");
    grouperAgentUser.workPhoneNumber = GrouperUtil.jsonJacksonGetString(entityNode, "work_phone_number");

    // Freshservice uses department_ids (array). We model a single departmentId; take the first value if present.
    JsonNode departmentIdsNode = GrouperUtil.jsonJacksonGetNode(entityNode, "department_ids");
    if (departmentIdsNode != null && departmentIdsNode.isArray() && departmentIdsNode.size() > 0) {
      JsonNode firstDepartmentIdNode = departmentIdsNode.get(0);
      if (firstDepartmentIdNode != null && firstDepartmentIdNode.isNumber()) {
        grouperAgentUser.departmentId = firstDepartmentIdNode.longValue();
      } else if (firstDepartmentIdNode != null && firstDepartmentIdNode.isTextual()) {
        String departmentIdString = firstDepartmentIdNode.asText();
        if (!GrouperUtil.isBlank(departmentIdString)) {
          grouperAgentUser.departmentId = Long.parseLong(departmentIdString);
        }
      }
    } else {
      // Backwards compatibility with any older payloads
      grouperAgentUser.departmentId = GrouperUtil.jsonJacksonGetLong(entityNode, "department_id");
    }

    grouperAgentUser.reportingManagerId = GrouperUtil.jsonJacksonGetLong(entityNode, "reporting_manager_id");
    grouperAgentUser.address = GrouperUtil.jsonJacksonGetString(entityNode, "address");
    grouperAgentUser.externalId = GrouperUtil.jsonJacksonGetString(entityNode, "external_id");

    JsonNode customFieldsNode = GrouperUtil.jsonJacksonGetNode(entityNode, "custom_fields");
    if (customFieldsNode != null && !customFieldsNode.isNull()) {
      String customFieldsJson = null;
      try {
        customFieldsJson = GrouperUtil.objectMapper.writeValueAsString(customFieldsNode);
      } catch (Exception e) {
        // best effort; keep going
        customFieldsJson = String.valueOf(customFieldsNode);
      }

      if (customFieldsNode.isObject()) {
        try {
          Map<String, Object> customFieldsMap = GrouperUtil.objectMapper.convertValue(customFieldsNode,
              new TypeReference<Map<String, Object>>() {});
          grouperAgentUser.customFields = customFieldsMap == null ? new HashMap<>() : normalizeCustomFields(customFieldsMap, customFieldsJson);
        } catch (RuntimeException re) {
          // preserve detailed normalization exceptions (which include json)
          throw re;
        } catch (Exception e) {
          throw new RuntimeException("Unable to parse FreshAgentUser.custom_fields from Freshservice JSON. json='" + customFieldsJson + "'", e);
        }
      } else {
        throw new RuntimeException(
            "FreshAgentUser.custom_fields must be a JSON object but was: " + customFieldsNode.getNodeType() + ". json='" + customFieldsJson + "'");
      }
    }

    grouperAgentUser.active = GrouperUtil.jsonJacksonGetBoolean(entityNode, "active");

    return grouperAgentUser;
  }
  
  /**
   * Convert a GrouperAgent to json
   * @param fieldNamesToSet the field names we'll be setting
   * @return the json representation of the GrouperAgent
   */
  public ObjectNode toJson(Set<String> fieldNamesToSet) {
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    
//    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {
//      result.put("id", this.id);
//    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("firstName")) {
      result.put("first_name", this.firstName);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("lastName")) {
      result.put("last_name", this.lastName);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("email")) {
      result.put("email", this.email);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("occasional")) {
      if (this.occasional != null) {
        result.put("occasional", this.occasional);
      }
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("roles")) {
      if (!GrouperUtil.isBlank(this.rolesJson)) {
        result.set("roles", GrouperUtil.jsonJacksonNode(this.rolesJson));
      }
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("jobTitle")) {
      if (!GrouperUtil.isBlank(this.jobTitle)) {
        result.put("job_title", this.jobTitle);
      }
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("workPhoneNumber")) {
      if (!GrouperUtil.isBlank(this.workPhoneNumber)) {
        result.put("work_phone_number", this.workPhoneNumber);
      }
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("departmentId")) {
      if (this.departmentId != null) {
        // Freshservice expects department_ids array
        ArrayNode departmentIdsArray = GrouperUtil.jsonJacksonArrayNode();
        departmentIdsArray.add(this.departmentId.longValue());
        result.set("department_ids", departmentIdsArray);
      }
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("reportingManagerId")) {
      if (this.reportingManagerId != null) {
        result.put("reporting_manager_id", this.reportingManagerId);
      }
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("address")) {
      if (!GrouperUtil.isBlank(this.address)) {
        result.put("address", this.address);
      }
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("externalId")) {
      if (!GrouperUtil.isBlank(this.externalId)) {
        result.put("external_id", this.externalId);
      }
    }

    // Custom fields: fieldNamesToSet will contain individual custom field attribute names with prefix customField_
    if (fieldNamesToSet == null) {
      if (this.customFields != null && !this.customFields.isEmpty()) {
        result.set("custom_fields", GrouperUtil.objectMapper.valueToTree(this.customFields));
      }
    } else if (this.customFields != null && !this.customFields.isEmpty()) {
      ObjectNode customFieldsNode = null;

      for (String attributeName : fieldNamesToSet) {
        if (GrouperUtil.isBlank(attributeName)) {
          continue;
        }
        if (!attributeName.startsWith(CUSTOM_FIELD_ATTRIBUTE_PREFIX)) {
          continue;
        }

        String fieldName = attributeName.substring(CUSTOM_FIELD_ATTRIBUTE_PREFIX.length());
        if (GrouperUtil.isBlank(fieldName)) {
          continue;
        }

        Object value = this.customFields.get(fieldName);
        if (value == null) {
          continue;
        }

        if (customFieldsNode == null) {
          customFieldsNode = GrouperUtil.jsonJacksonNode();
        }

        if (value instanceof String) {
          customFieldsNode.put(fieldName, (String)value);
        } else if (value instanceof Boolean) {
          customFieldsNode.put(fieldName, ((Boolean)value).booleanValue());
        } else if (value instanceof Number) {
          customFieldsNode.put(fieldName, ((Number)value).longValue());
        } else {
          throw new RuntimeException("FreshAgentUser.customFields['" + fieldName + "'] had unsupported type "
              + value.getClass().getName());
        }
      }

      if (customFieldsNode != null && customFieldsNode.size() > 0) {
        result.set("custom_fields", customFieldsNode);
      }
    }
    // active is not sent, agents are deactivated with DELETE and reactivated with /reactivate
    
    return result;
  }
  
  private static Map<String, Object> normalizeCustomFields(Map<String, Object> customFieldsMap) {
    return normalizeCustomFields(customFieldsMap, null);
  }

  private static Map<String, Object> normalizeCustomFields(Map<String, Object> customFieldsMap, String customFieldsJsonForError) {
    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<String, Object> entry : customFieldsMap.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();

      if (value == null) {
        result.put(key, null);
        continue;
      }

      if (value instanceof String) {
        result.put(key, value);
        continue;
      }

      if (value instanceof Boolean) {
        result.put(key, value);
        continue;
      }

      if (value instanceof Number) {
        // No decimals allowed; coerce integral numbers to Long
        if (value instanceof Float || value instanceof Double) {
          throw new RuntimeException("FreshAgentUser.customFields['" + key
              + "'] must be String, Long, or Boolean but was decimal number: " + value
              + (customFieldsJsonForError == null ? "" : (". json='" + customFieldsJsonForError + "'")));
        }
        result.put(key, ((Number)value).longValue());
        continue;
      }

      throw new RuntimeException("FreshAgentUser.customFields['" + key
          + "'] must be String, Long, or Boolean but was: " + value.getClass().getName()
          + (customFieldsJsonForError == null ? "" : (". json='" + customFieldsJsonForError + "'")));
    }
    return result;
  }

}