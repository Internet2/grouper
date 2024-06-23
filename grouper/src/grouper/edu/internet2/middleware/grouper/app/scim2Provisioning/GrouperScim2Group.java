package edu.internet2.middleware.grouper.app.scim2Provisioning;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperScim2Group {

  public static void main(String[] args) {
    
    GrouperScim2Group grouperScimUser = new GrouperScim2Group();
    
    grouperScimUser.setDisplayName("dispName");
    grouperScimUser.setId("i");
  
    String json = GrouperUtil.jsonJacksonToString(grouperScimUser.toJson(null));
    System.out.println(json);
    
    grouperScimUser = GrouperScim2Group.fromJson(GrouperUtil.jsonJacksonNode(json));
    
    System.out.println(grouperScimUser.toString());
    
  }

  /**
   * 
   * @param targetGroup
   * @return
   */
  public static GrouperScim2Group fromProvisioningGroup(ProvisioningGroup targetGroup, Set<String> fieldNamesToSet) {
    
    GrouperScim2Group grouperScim2Group = new GrouperScim2Group();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("displayName")) {      
      grouperScim2Group.setDisplayName(targetGroup.getDisplayName());
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      grouperScim2Group.setId(targetGroup.getId());
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("active")) {      
      grouperScim2Group.setActive(GrouperUtil.booleanValue(targetGroup.retrieveAttributeValueBoolean("active"), true));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("schemas")) {     
      
      Object schemas = targetGroup.retrieveAttributeValue("schemas");
      if (!GrouperUtil.isBlank(schemas)) {
        if (schemas instanceof String) {
          grouperScim2Group.setSchemas((String)schemas);
        } else if (schemas instanceof Collection) {
          Collection schemasColl = (Collection)schemas;
          grouperScim2Group.setSchemas(GrouperUtil.join(schemasColl.iterator(), ","));
        } else {
          throw new RuntimeException("Invalid type: "+schemas + " class: "+schemas.getClass());
        }
      }
      
    }
    
    GrouperScim2ProvisionerConfiguration scimConfig = (GrouperScim2ProvisionerConfiguration) targetGroup.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    for (String attributeName:  scimConfig.getGroupAttributeJsonPointer().keySet()) {
      
      String jsonPointer = scimConfig.getGroupAttributeJsonPointer().get(attributeName);
      ProvisioningAttribute provisioningAttribute = targetGroup.retrieveProvisioningAttribute(attributeName);
      if (provisioningAttribute == null) {
        continue;
      }
      Object valueObject = targetGroup.retrieveAttributeValueString(attributeName);
   
      if (grouperScim2Group.customAttributes == null) {
        grouperScim2Group.customAttributes = new HashMap<>();
      }
      if (grouperScim2Group.customAttributeNameToJsonPointer == null) {
        grouperScim2Group.customAttributeNameToJsonPointer = new HashMap<>();
      }
      
      if (StringUtils.equals(scimConfig.getGroupAttributeJsonValueType().get(attributeName), "boolean")) {
        valueObject = GrouperUtil.booleanValue(valueObject);
      }
      
      grouperScim2Group.customAttributes.put(attributeName, valueObject);
      grouperScim2Group.customAttributeNameToJsonPointer.put(attributeName, jsonPointer);
    }
    
    return grouperScim2Group;

  }

  /**
   * see if this scim path matches the current members 
   * @param path
   * @return userId
   */
  public String validateMembersPath(String path) {
    
    String field = null;
    String membersField = null;
    String value = null;
    
    // objectFieldEqPattern members.value eq "1234567"
    Matcher matcher = GrouperScim2User.objectFieldEqPattern.matcher(path);
    if (!matcher.matches()) {

      // objectIndexFieldEqPattern members[value eq "89bb1940-b905-4575-9e7f-6f887cfb368e"]
      matcher = GrouperScim2User.objectIndexFieldEqPattern.matcher(path);

      if (!matcher.matches()) {
        throw new RuntimeException("Invalid field expression '" + path + "'");
      }
    }
    membersField = matcher.group(2);

    field = matcher.group(1);
    value = matcher.group(3); 

    if (!"members".equalsIgnoreCase(field)) {
      throw new RuntimeException("Expecting emails but received '" + field + "'");
    }

    if ("value".equals(membersField)) {
      return value;
    } else {
      throw new RuntimeException("Expected value but received '" + membersField + "'");
    }
    
  }
  

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  public GrouperScim2Group() {
  }

  public ProvisioningGroup toProvisioningGroup() {
    ProvisioningGroup targetGroup = new ProvisioningGroup(false);
    
    if (this.displayName != null) {
      targetGroup.setDisplayName(this.displayName);
    }
    
    if (this.id != null) {
      targetGroup.setId(this.id);
    }
    
    if (this.schemas != null) {
      targetGroup.assignAttributeValue("schemas", this.schemas);
    }
    
    targetGroup.assignAttributeValue("active", this.active);
    
    if (this.customAttributes != null) {
      GrouperScim2ProvisionerConfiguration scimConfig = (GrouperScim2ProvisionerConfiguration) targetGroup.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      for (String attributeName:  scimConfig.getGroupAttributeJsonPointer().keySet()) {
        Object attributeValue = this.customAttributes.get(attributeName);
        if (GrouperUtil.isBlank(attributeValue)) {
          continue;
        }
        
        if (StringUtils.equals(scimConfig.getGroupAttributeJsonValueType().get(attributeName), "boolean")) {
          attributeValue = GrouperUtil.booleanValue(attributeValue) ? "true": "false";
        }
        targetGroup.assignAttributeValue(attributeName, attributeValue);
      }
    }
    
    return targetGroup;
  }

  /**
   * convert from jackson json
   * @param groupNode
   * @return the group
   */
  public static GrouperScim2Group fromJson(JsonNode groupNode) {
    GrouperScim2Group grouperScimGroup = new GrouperScim2Group();
    
    
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    
    //  {
    //    "id": "9067729b3d-a2cfc8a5-f4ab-4443-9d7d-b32a9013c554",
    //    "meta": {
    //        "resourceType": "Group",
    //        "created": "2020-04-06T16:48:19Z",
    //        "lastModified": "2020-04-06T16:48:19Z"
    //    },
    //    "schemas": [
    //        "urn:ietf:params:scim:schemas:core:2.0:Group"
    //    ],
    //    "displayName": "Group Bar"
    //  }

    grouperScimGroup.setId(GrouperUtil.jsonJacksonGetString(groupNode, "id"));
    grouperScimGroup.setDisplayName(GrouperUtil.jsonJacksonGetString(groupNode, "displayName"));
    
    JsonNode metaNode = groupNode.has("meta") ? groupNode.get("meta") : null;
    
    grouperScimGroup.setCreatedJson(GrouperUtil.jsonJacksonGetString(metaNode, "created"));
    grouperScimGroup.setLastModifiedJson(GrouperUtil.jsonJacksonGetString(metaNode, "lastModified"));
    grouperScimGroup.setActive(GrouperUtil.booleanValue(GrouperUtil.jsonJacksonGetBoolean(groupNode, "active"), true));
    
    if (groupNode.get("schemas") != null) {
      Set<String> schemasStringSet = GrouperUtil.jsonJacksonGetStringSet(groupNode, "schemas");
      grouperScimGroup.schemas = GrouperUtil.join(schemasStringSet.iterator(), ',');
    }
    
    
    GrouperScim2ProvisionerConfiguration scimConfig = (GrouperScim2ProvisionerConfiguration)grouperProvisioner.retrieveGrouperProvisioningConfiguration();
    
    Map<String, String> attributeJsonPointers = scimConfig.getGroupAttributeJsonPointer();
    
    for (String attributeName: attributeJsonPointers.keySet()) {
      String jsonPointer = attributeJsonPointers.get(attributeName);
      JsonNode jsonNode = GrouperUtil.jsonJacksonGetNodeFromJsonPointer(groupNode, jsonPointer);
      if (jsonNode == null) {
        continue;
      }
      
      if (grouperScimGroup.customAttributes == null) {
        grouperScimGroup.customAttributes = new HashMap<>();
      }
      if (grouperScimGroup.customAttributeNameToJsonPointer == null) {
        grouperScimGroup.customAttributeNameToJsonPointer = new HashMap<>();
      }
      grouperScimGroup.customAttributeNameToJsonPointer.put(attributeName, jsonPointer);
      if (jsonNode.isArray()) {
        ArrayNode arrayNode = (ArrayNode) jsonNode;
        if (arrayNode.size() > 0) {
          Set<String> attributeValues = GrouperUtil.jsonJacksonGetStringSetFromJsonPointer(groupNode, jsonPointer);
          attributeValues.remove(null);
          attributeValues.remove("");
          grouperScimGroup.customAttributes.put(attributeName, attributeValues);
        }
      } else if (jsonNode.isValueNode()) { 
        Object attributeValue = GrouperUtil.jsonJacksonGetStringFromJsonPointer(groupNode, jsonPointer);
        if (!GrouperUtil.isBlank(attributeValue)) {
          
          if (StringUtils.equals(scimConfig.getGroupAttributeJsonValueType().get(attributeName), "boolean")) {
            attributeValue = GrouperUtil.booleanValue(attributeValue);
          }
          
          grouperScimGroup.customAttributes.put(attributeName, attributeValue);
        }
      }
    }
    
    return grouperScimGroup;
  }

  /**
   * convert from jackson json
   * @param groupNode
   * @return the group
   */
  public ObjectNode toJson(Set<String> fieldNamesToSet) {
    
    //  {
    //    "id": "9067729b3d-a2cfc8a5-f4ab-4443-9d7d-b32a9013c554",
    //    "meta": {
    //        "resourceType": "Group",
    //        "created": "2020-04-06T16:48:19Z",
    //        "lastModified": "2020-04-06T16:48:19Z"
    //    },
    //    "schemas": [
    //        "urn:ietf:params:scim:schemas:core:2.0:Group"
    //    ],
    //    "displayName": "Group Bar"
    //  }

    ObjectNode result = GrouperUtil.jsonJacksonNode();
  
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      GrouperUtil.jsonJacksonAssignString(result, "id", this.id);
    }
    
    if (fieldNamesToSet == null || (fieldNamesToSet.contains("created") || fieldNamesToSet.contains("lastModified"))) {      
      if (this.created != null || this.lastModified != null) {
        ObjectNode metaNode = GrouperUtil.jsonJacksonNode();
        if (fieldNamesToSet == null || fieldNamesToSet.contains("created")) {
          GrouperUtil.jsonJacksonAssignString(metaNode, "created", this.getCreatedJson());
        }
        if (fieldNamesToSet == null || fieldNamesToSet.contains("lastModified")) {
          GrouperUtil.jsonJacksonAssignString(metaNode, "lastModified", this.getLastModifiedJson());
        }
        result.set("meta", metaNode);
      }
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("displayName")) {      
      GrouperUtil.jsonJacksonAssignString(result, "displayName", this.displayName);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("active")) {      
      GrouperUtil.jsonJacksonAssignBoolean(result, "active", GrouperUtil.booleanValue(this.active, true));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("schemas")) {      
      if (!StringUtils.isBlank(this.schemas)) {
        GrouperUtil.jsonJacksonAssignStringArray(result, "schemas", GrouperUtil.splitTrimToSet(this.schemas, ","));
      }
    }
    
    if (customAttributes != null) {
      for (String attributeName: customAttributes.keySet()) {
        if (fieldNamesToSet == null || fieldNamesToSet.contains(attributeName)) {  
          
          if (customAttributeNameToJsonPointer != null) {
            String jsonPointer = customAttributeNameToJsonPointer.get(attributeName);
            if (StringUtils.isNotBlank(jsonPointer)) {
            //TODO implement jsonJacksonAssignJsonPointer for set, number, boolean
              GrouperUtil.jsonJacksonAssignJsonPointerString(result, jsonPointer, customAttributes.get(attributeName));
            }
          }
        }
      }
    }
    
    return result;
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableScimGroup(DdlVersionBean ddlVersionBean, Database database) {
  
    final String tableName = "mock_scim_group";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
          
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "display_name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "created", Types.TIMESTAMP, null, false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_modified", Types.TIMESTAMP, null, false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "active", Types.VARCHAR, "1", false, true, "T");
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "1024", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_scim_gdisp_name_idx", false, "display_name");
    }
  }

  private Boolean active = true;
  
  
  public Boolean getActive() {
    return active;
  }

  
  public void setActive(Boolean active) {
    this.active = active;
  }

  /**
   * 2020-04-06T16:48:19Z
   */
  private Timestamp created;
  
  /**
   * 2020-04-06T16:48:19Z
   */
  private Timestamp lastModified;
  
  /**
   * 2020-04-06T16:48:19Z
   */
  public Timestamp getCreated() {
    return created;
  }

  /**
   * 2020-04-06T16:48:19Z
   * @param created
   */
  public void setCreated(Timestamp created) {
    this.created = created;
  }

  /**
   * 2020-04-06T16:48:19Z
   */
  public String getCreatedJson() {
    return GrouperUtil.timestampIsoUtcSecondsConvertToString(this.created);
  }

  /**
   * 2020-04-06T16:48:19Z
   * @param created
   */
  public void setCreatedJson(String created) {
    try {
      this.created = GrouperUtil.timestampIsoUtcSecondsConvertFromString(created);
    } catch (RuntimeException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Error with created: '" + created + "'", e);
      }
    }
  }

  /**
   * 2020-04-06T16:48:19Z
   * @return
   */
  public Timestamp getLastModified() {
    return lastModified;
  }

  /**
   * 2020-04-06T16:48:19Z
   * @return
   */
  public String getLastModifiedJson() {
    return GrouperUtil.timestampIsoUtcSecondsConvertToString(this.lastModified);
  }

  /**
   * 2020-04-06T16:48:19Z
   * @param lastModified
   */
  public void setLastModified(Timestamp lastModified) {
    this.lastModified = lastModified;
  }

  /**
   * 2020-04-06T16:48:19Z
   * @param lastModified
   */
  public void setLastModifiedJson(String lastModified) {
    try {
      this.lastModified = GrouperUtil.timestampIsoUtcSecondsConvertFromString(lastModified);
    } catch (RuntimeException re) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Error with created: '" + lastModified + "'", re);
      }
    }      
  }

  private String id;

  private String displayName;

  private String schemas;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperScim2Group.class);
  
  public String getId() {
    return id;
  }

  
  public void setId(String id) {
    this.id = id;
  }

  
  public String getDisplayName() {
    return displayName;
  }

  
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
  
  
  public String getSchemas() {
    return schemas;
  }

  
  public void setSchemas(String schemas) {
    this.schemas = schemas;
  }

  /**
   * If an attribute has a json pointer then the name and string value or set of string values will be here
   */
  private Map<String, Object> customAttributes = null; // name to value

  private Map<String, String> customAttributeNameToJsonPointer = null;
  
  
  public Map<String, Object> getCustomAttributes() {
    return customAttributes;
  }

  
  public void setCustomAttributes(Map<String, Object> customAttributes) {
    this.customAttributes = customAttributes;
  }
  
  
  public Map<String, String> getCustomAttributeNameToJsonPointer() {
    return customAttributeNameToJsonPointer;
  }

  
  public void setCustomAttributeNameToJsonPointer(Map<String, String> customAttributeNameToJsonPointer) {
    this.customAttributeNameToJsonPointer = customAttributeNameToJsonPointer;
  }

  
  
  
}
