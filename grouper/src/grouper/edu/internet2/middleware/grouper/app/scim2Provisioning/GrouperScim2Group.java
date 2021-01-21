package edu.internet2.middleware.grouper.app.scim2Provisioning;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
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
    ProvisioningGroup targetGroup = new ProvisioningGroup();
    
    if (this.displayName != null) {
      targetGroup.assignAttributeValue("displayName", this.displayName);
    }
    
    if (this.id != null) {
      targetGroup.setId(this.id);
    }
    
    return targetGroup;
  }

  /**
   * convert from jackson json
   * @param entityNode
   * @return the group
   */
  public static GrouperScim2Group fromJson(JsonNode groupNode) {
    GrouperScim2Group grouperScimGroup = new GrouperScim2Group();
    
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

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode result = objectMapper.createObjectNode();
  
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
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_scim_gdisp_name_idx", false, "display_name");
    }
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
    this.created = GrouperUtil.timestampIsoUtcSecondsConvertFromString(created);
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
    this.lastModified = GrouperUtil.timestampIsoUtcSecondsConvertFromString(lastModified);
  }

  private String id;

  private String displayName;
  
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
  
  
  
}
