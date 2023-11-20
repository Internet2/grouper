package edu.internet2.middleware.grouper.app.boxProvisioner;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperBoxGroup {

  public static void main(String[] args) throws Exception {
    
    GrouperBoxGroup grouperBoxGroup = new GrouperBoxGroup();
    
    String dt = "2022-11-22T20:18:07-06:00";
    
    OffsetDateTime parse = OffsetDateTime.parse(dt);
    System.out.println(Timestamp.from(parse.toInstant()));
    
    
    
//    grouperBoxGroup.setDescription("desc");
//    grouperBoxGroup.setName("name");
//    grouperBoxGroup.setGroupTypeDynamic(true);
//    grouperBoxGroup.setGroupTypeUnified(true);
//    grouperBoxGroup.setId("id");
//    grouperBoxGroup.setMailEnabled(true);
//    grouperBoxGroup.setMailNickname("mailNick");
//    grouperBoxGroup.setSecurityEnabled(true);
//
//    String json = GrouperUtil.jsonJacksonToString(grouperBoxGroup.toJson(null));
//    System.out.println(json);
//    
//    grouperBoxGroup = GrouperBoxGroup.fromJson(GrouperUtil.jsonJacksonNode(json));
//    
//    System.out.println(grouperBoxGroup.toString());
    
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableBoxGroup(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_box_group";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "type", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "1024", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "external_sync_identifier", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_type", Types.VARCHAR, "50", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "invitability_level", Types.VARCHAR, "50", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "member_viewability_level", Types.VARCHAR, "50", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "provenance", Types.VARCHAR, "50", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "can_invite_as_collaborator", Types.VARCHAR, "1", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "created_at", Types.TIMESTAMP, null, false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "modified_at", Types.TIMESTAMP, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_box_group_name_idx", false, "name");
    }
            
  }

  public ProvisioningGroup toProvisioningGroup() {
    
    ProvisioningGroup targetGroup = new ProvisioningGroup();
    
    targetGroup.setId(this.id);
    targetGroup.setName(this.name);
    targetGroup.assignAttributeValue("type", this.type);
    targetGroup.assignAttributeValue("description", this.description);
    targetGroup.assignAttributeValue("externalSyncIdentifier", this.externalSyncIdentifier);
    targetGroup.assignAttributeValue("groupType", this.groupType);
    targetGroup.assignAttributeValue("invitabilityLevel", this.invitabilityLevel);
    targetGroup.assignAttributeValue("memberViewabilityLevel", this.memberViewabilityLevel);
    targetGroup.assignAttributeValue("canInviteAsCollaborator", this.canInviteAsCollaborator);
    targetGroup.assignAttributeValue("provenance", this.provenance);
    
    return targetGroup;
  }
  
  /**
   * 
   * @param targetGroup
   * @return
   */
  public static GrouperBoxGroup fromProvisioningGroup(ProvisioningGroup targetGroup, Set<String> fieldNamesToSet) {
    
    GrouperBoxGroup grouperBoxGroup = new GrouperBoxGroup();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("description")) {      
      grouperBoxGroup.setDescription(targetGroup.retrieveAttributeValueString("description"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {      
      grouperBoxGroup.setName(targetGroup.getName());
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      grouperBoxGroup.setId(targetGroup.getId());
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("type")) {      
      grouperBoxGroup.setType(targetGroup.retrieveAttributeValueString("type"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("externalSyncIdentifier")) {      
      grouperBoxGroup.setExternalSyncIdentifier(targetGroup.retrieveAttributeValueString("externalSyncIdentifier"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("groupType")) {      
      grouperBoxGroup.setGroupType(targetGroup.retrieveAttributeValueString("groupType"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("invitabilityLevel")) {      
      grouperBoxGroup.setInvitabilityLevel(targetGroup.retrieveAttributeValueString("invitabilityLevel"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("memberViewabilityLevel")) {      
      grouperBoxGroup.setMemberViewabilityLevel(targetGroup.retrieveAttributeValueString("memberViewabilityLevel"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("canInviteAsCollaborator")) {      
      
      Boolean canInviteAsCollaborator = targetGroup.retrieveAttributeValueBoolean("canInviteAsCollaborator");
      if (canInviteAsCollaborator == null) {
        canInviteAsCollaborator = false;
      }
      grouperBoxGroup.setCanInviteAsCollaborator(canInviteAsCollaborator);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("provenance")) {      
      grouperBoxGroup.setProvenance(targetGroup.retrieveAttributeValueString("provenance"));
    }
    
    return grouperBoxGroup;

  }
  
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  private String id;
  private String type;
  /**
   * 2020-04-06T16:48:19Z
   */
  private Timestamp createdAt;
  /**
   * 2020-04-06T16:48:19Z
   */
  private Timestamp modifiedAt;
  
  private String description;
  
  private String externalSyncIdentifier;
  
  private String groupType;
  
  private String invitabilityLevel;
  
  private String memberViewabilityLevel;
  
  private String name;
  
  private boolean canInviteAsCollaborator;
  
  private String provenance;
  
  public static final Map<String, String> grouperBoxGroupToBoxSpecificAttributeNames =
      GrouperUtil.toMap("id", "id", "type", "type", "description", "description", "externalSyncIdentifier", "external_sync_identifier", 
          "groupType", "group_type", "invitabilityLevel", "invitability_level", "memberViewabilityLevel", "member_viewability_level", 
          "name", "name", "canInviteAsCollaborator", "permissions.can_invite_as_collaborator", "provenance", "provenance");
  
  public String getType() {
    return type;
  }

  
  public void setType(String type) {
    this.type = type;
  }

  
  public Timestamp getCreatedAt() {
    return createdAt;
  }

  
  public void setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  
  public Timestamp getModifiedAt() {
    return modifiedAt;
  }

  
  public void setModifiedAt(Timestamp modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  
  public String getExternalSyncIdentifier() {
    return externalSyncIdentifier;
  }

  
  public void setExternalSyncIdentifier(String externalSyncIdentifier) {
    this.externalSyncIdentifier = externalSyncIdentifier;
  }

  
  public String getGroupType() {
    return groupType;
  }

  
  public void setGroupType(String groupType) {
    this.groupType = groupType;
  }

  
  public String getInvitabilityLevel() {
    return invitabilityLevel;
  }

  
  public void setInvitabilityLevel(String invitabilityLevel) {
    this.invitabilityLevel = invitabilityLevel;
  }

  
  public String getMemberViewabilityLevel() {
    return memberViewabilityLevel;
  }

  
  public void setMemberViewabilityLevel(String memberViewabilityLevel) {
    this.memberViewabilityLevel = memberViewabilityLevel;
  }

  
  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  
  public boolean isCanInviteAsCollaborator() {
    return canInviteAsCollaborator;
  }

  
  public void setCanInviteAsCollaborator(boolean canInviteAsCollaborator) {
    this.canInviteAsCollaborator = canInviteAsCollaborator;
  }

  
  public String getCanInviteAsCollaboratorDb() {
    return canInviteAsCollaborator ? "T" : "F";
  }

  public void setCanInviteAsCollaboratorDb(String canInviteAsCollaborator) {
    this.canInviteAsCollaborator = GrouperUtil.booleanValue(canInviteAsCollaborator);
  }
  
  public String getProvenance() {
    return provenance;
  }

  
  public void setProvenance(String provenance) {
    this.provenance = provenance;
  }

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  /**
   * 2020-04-06T16:48:19Z
   */
  public String getCreatedJson() {
    return GrouperUtil.timestampIsoUtcSecondsConvertToString(this.createdAt);
  }

  /**
   * 2020-04-06T16:48:19Z
   * @param created
   */
  public void setCreatedJson(String created) {
    OffsetDateTime offsetDateTime = OffsetDateTime.parse(created);
    this.createdAt = Timestamp.from(offsetDateTime.toInstant());
  }
  
  /**
   * 2020-04-06T16:48:19Z
   */
  public String getModifiedJson() {
    return GrouperUtil.timestampIsoUtcSecondsConvertToString(this.modifiedAt);
  }

  /**
   * 2020-04-06T16:48:19Z
   * @param modified
   */
  public void setModifiedJson(String modified) {
    OffsetDateTime offsetDateTime = OffsetDateTime.parse(modified);
    this.modifiedAt = Timestamp.from(offsetDateTime.toInstant());
  }
  
  /**
   * convert from jackson json
   * @param groupNode
   * @return the group
   */
  public static GrouperBoxGroup fromJson(JsonNode groupNode) {
    
    if (groupNode == null) {
      return null;
    }
    
    GrouperBoxGroup grouperBoxGroup = new GrouperBoxGroup();
    grouperBoxGroup.description = GrouperUtil.jsonJacksonGetString(groupNode, "description");
    grouperBoxGroup.type = GrouperUtil.jsonJacksonGetString(groupNode, "type");
    grouperBoxGroup.id = GrouperUtil.jsonJacksonGetString(groupNode, "id");
    grouperBoxGroup.externalSyncIdentifier = GrouperUtil.jsonJacksonGetString(groupNode, "external_sync_identifier");
    grouperBoxGroup.groupType = GrouperUtil.jsonJacksonGetString(groupNode, "group_type");
    grouperBoxGroup.invitabilityLevel = GrouperUtil.jsonJacksonGetString(groupNode, "invitability_level");
    grouperBoxGroup.memberViewabilityLevel = GrouperUtil.jsonJacksonGetString(groupNode, "member_viewability_level");
    grouperBoxGroup.name = GrouperUtil.jsonJacksonGetString(groupNode, "name");
    grouperBoxGroup.provenance = GrouperUtil.jsonJacksonGetString(groupNode, "provenance");
    
//    grouperBoxGroup.setCreatedJson(GrouperUtil.jsonJacksonGetString(groupNode, "created_at"));
//    grouperBoxGroup.setModifiedJson(GrouperUtil.jsonJacksonGetString(groupNode, "modified_at"));
    
    JsonNode permissionsNode = GrouperUtil.jsonJacksonGetNode(groupNode, "permissions");
    if (permissionsNode != null) {
      grouperBoxGroup.canInviteAsCollaborator = GrouperUtil.jsonJacksonGetBoolean(permissionsNode, "can_invite_as_collaborator", false);
    }
    
    return grouperBoxGroup;
  }

  /**
   * convert from jackson json
   * @param fieldNamesToSet
   * @return the group
   */
  public ObjectNode toJson(Set<String> fieldNamesToSet) {
    
    ObjectNode result = GrouperUtil.jsonJacksonNode();

    if (fieldNamesToSet == null || fieldNamesToSet.contains("description")) {      
      result.put("description", this.description);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      result.put("id", this.id);
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {      
      result.put("name", this.name);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("external_sync_identifier")) {      
      result.put("external_sync_identifier", this.externalSyncIdentifier);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("invitability_level")) {      
      result.put("invitability_level", this.invitabilityLevel);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("member_viewability_level")) {      
      result.put("member_viewability_level", this.memberViewabilityLevel);
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("provenance")) {      
      result.put("provenance", this.provenance);
    }
    
    return result;
  }
  
  
  
}
