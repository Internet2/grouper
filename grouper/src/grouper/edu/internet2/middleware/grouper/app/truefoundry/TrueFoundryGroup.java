package edu.internet2.middleware.grouper.app.truefoundry;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class TrueFoundryGroup {

  /**
   * group type value for a TrueFoundry team
   */
  public static final String GROUP_TYPE_TEAM = "team";

  /**
   * group type value for a TrueFoundry role
   */
  public static final String GROUP_TYPE_ROLE = "role";

  public static void createTableTrueFoundryGroup(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_truefoundry_group";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "100", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "36", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "display_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "1024", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_type", Types.VARCHAR, "20", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "resource_type", Types.VARCHAR, "50", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "is_default", Types.VARCHAR, "1", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_tfy_grp_name_type_idx", true, "name", "group_type");
    }

  }

  private String id;

  private String name;

  /**
   * human-readable display name shown in TrueFoundry UI (roles only)
   */
  private String displayName;

  /**
   * description (roles only, required by API)
   */
  private String description;

  /**
   * type of group: "team" or "role"
   */
  private String groupType;

  /**
   * resource type for role assignment: "account" or "tenant" (roles only)
   */
  private String resourceType;

  /**
   * true if this is a built-in role (isDefault=true), false if custom (roles only)
   */
  private Boolean isDefault;

  /**
   * list of member emails (teams only, populated from manifest.members)
   */
  private List<String> members;

  /**
   * list of manager emails (teams only, populated from manifest.managers)
   */
  private List<String> managers;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getGroupType() {
    return groupType;
  }

  public void setGroupType(String groupType) {
    this.groupType = groupType;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public Boolean getIsDefault() {
    return isDefault;
  }

  public void setIsDefault(Boolean isDefault) {
    this.isDefault = isDefault;
  }

  public String getIsDefaultString() {
    return booleanToTf(this.isDefault);
  }

  public void setIsDefaultString(String isDefaultString) {
    this.isDefault = GrouperUtil.booleanObjectValue(isDefaultString);
  }

  public List<String> getMembers() {
    return members;
  }

  public void setMembers(List<String> members) {
    this.members = members;
  }

  public List<String> getManagers() {
    return managers;
  }

  public void setManagers(List<String> managers) {
    this.managers = managers;
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * Convert from TrueFoundry team JSON (from GET /api/svc/v1/teams/user response).
   * Expects a team object from the "data" array.
   * @param teamNode a team object from the teams response
   * @return the TrueFoundryGroup with groupType=team
   */
  @SuppressWarnings("unchecked")
  public static TrueFoundryGroup fromTeamJson(JsonNode teamNode) {
    if (teamNode == null) {
      return null;
    }

    TrueFoundryGroup trueFoundryGroup = new TrueFoundryGroup();
    trueFoundryGroup.groupType = GROUP_TYPE_TEAM;

    trueFoundryGroup.id = GrouperUtil.jsonJacksonGetString(teamNode, "id");
    trueFoundryGroup.name = GrouperUtil.jsonJacksonGetString(teamNode, "teamName");

    JsonNode manifestNode = GrouperUtil.jsonJacksonGetNode(teamNode, "manifest");
    if (manifestNode != null) {
      ArrayNode membersArrayNode = GrouperUtil.jsonJacksonGetArrayNode(manifestNode, "members");
      if (membersArrayNode != null) {
        List<String> membersList = new ArrayList<String>();
        for (int i = 0; i < membersArrayNode.size(); i++) {
          membersList.add(membersArrayNode.get(i).asText());
        }
        trueFoundryGroup.members = membersList;
      }
      ArrayNode managersArrayNode = GrouperUtil.jsonJacksonGetArrayNode(manifestNode, "managers");
      if (managersArrayNode != null) {
        List<String> managersList = new ArrayList<String>();
        for (int i = 0; i < managersArrayNode.size(); i++) {
          managersList.add(managersArrayNode.get(i).asText());
        }
        trueFoundryGroup.managers = managersList;
      }
    }

    return trueFoundryGroup;
  }

  /**
   * Convert from TrueFoundry role JSON (from GET /api/svc/v1/role/list response).
   * Expects a role object from the "data" array.
   * @param roleNode a role object from the role list response
   * @return the TrueFoundryGroup with groupType=role
   */
  public static TrueFoundryGroup fromRoleJson(JsonNode roleNode) {
    if (roleNode == null) {
      return null;
    }

    TrueFoundryGroup trueFoundryGroup = new TrueFoundryGroup();
    trueFoundryGroup.groupType = GROUP_TYPE_ROLE;

    trueFoundryGroup.id = GrouperUtil.jsonJacksonGetString(roleNode, "id");
    trueFoundryGroup.name = GrouperUtil.jsonJacksonGetString(roleNode, "name");
    trueFoundryGroup.resourceType = GrouperUtil.jsonJacksonGetString(roleNode, "resourceType");
    trueFoundryGroup.isDefault = GrouperUtil.jsonJacksonGetBoolean(roleNode, "isDefault");

    JsonNode manifestNode = GrouperUtil.jsonJacksonGetNode(roleNode, "manifest");
    if (manifestNode != null) {
      trueFoundryGroup.displayName = GrouperUtil.jsonJacksonGetString(manifestNode, "displayName");
      trueFoundryGroup.description = GrouperUtil.jsonJacksonGetString(manifestNode, "description");
    }

    return trueFoundryGroup;
  }

  /**
   * Convert to JSON for the team PUT endpoint (PUT /api/svc/v1/teams).
   * Produces the manifest-based body for create or update.
   * Members and managers are specified by email.  The full lists replace
   * the current state on the server (replaceGroupMemberships pattern).
   * @param memberEmails list of member emails (at least one required by the API)
   * @param managerEmails list of manager emails (may be null/empty)
   * @return the team PUT request body
   */
  public ObjectNode toTeamJson(List<String> memberEmails, List<String> managerEmails) {
    ObjectNode bodyNode = GrouperUtil.jsonJacksonNode();
    bodyNode.put("teamName", this.name);
    
    ObjectNode manifestNode = GrouperUtil.jsonJacksonNode();

    manifestNode.put("type", "team");
    manifestNode.put("name", this.name);

    ArrayNode membersArray = GrouperUtil.jsonJacksonArrayNode();
    if (memberEmails != null) {
      for (String email : memberEmails) {
        membersArray.add(email);
      }
    }
    manifestNode.set("members", membersArray);

    if (managerEmails != null && !managerEmails.isEmpty()) {
      ArrayNode managersArray = GrouperUtil.jsonJacksonArrayNode();
      for (String email : managerEmails) {
        managersArray.add(email);
      }
      manifestNode.set("managers", managersArray);
    }

    bodyNode.set("manifest", manifestNode);

    return bodyNode;
  }

  /**
   * Convert to JSON for the role PUT endpoint (PUT /api/svc/v1/role).
   * All manifest fields are required: name, displayName, resourceType, description, permissions, type.
   * Uses minimal placeholder permissions — administrators configure actual permissions in TrueFoundry UI.
   * @param fieldNamesToSet the field names to include, or null for all
   * @return the role PUT request body
   */
  public ObjectNode toRoleJson(Set<String> fieldNamesToSet) {
    ObjectNode bodyNode = GrouperUtil.jsonJacksonNode();
    ObjectNode manifestNode = GrouperUtil.jsonJacksonNode();

    manifestNode.put("type", "role");
    manifestNode.put("name", this.name);
    String theDisplayName = !GrouperUtil.isBlank(this.displayName) ? this.displayName : this.name;
    manifestNode.put("displayName", theDisplayName);
    manifestNode.put("resourceType", !GrouperUtil.isBlank(this.resourceType) ? this.resourceType : "account");
    manifestNode.put("description", !GrouperUtil.isBlank(this.description) ? this.description : theDisplayName);

    // minimal placeholder permissions — admin configures in TrueFoundry UI
    ArrayNode permissionsArray = GrouperUtil.jsonJacksonArrayNode();
    permissionsArray.add("role:ListRoles");
    manifestNode.set("permissions", permissionsArray);

    bodyNode.set("manifest", manifestNode);

    return bodyNode;
  }

  /**
   * Convert to a Grouper provisioning group
   * @return the converted group
   */
  public ProvisioningGroup toProvisioningGroup() {
    ProvisioningGroup targetGroup = new ProvisioningGroup(false);

    if (this.id != null) {
      targetGroup.setId(this.id);
    }
    targetGroup.assignAttributeValue("name", this.name);
    if (this.displayName != null) {
      targetGroup.assignAttributeValue("displayName", this.displayName);
    }
    if (this.description != null) {
      targetGroup.assignAttributeValue("description", this.description);
    }
    if (this.groupType != null) {
      targetGroup.assignAttributeValue("groupType", this.groupType);
    }
    if (this.resourceType != null) {
      targetGroup.assignAttributeValue("resourceType", this.resourceType);
    }
    if (this.isDefault != null) {
      targetGroup.assignAttributeValue("isDefault", this.isDefault);
    }

    return targetGroup;
  }

  /**
   * Convert from a provisioning group to a TrueFoundryGroup
   * @param targetGroup the Grouper provisioning group to convert
   * @param fieldNamesToSet the field names to be set, or null for all
   * @return the TrueFoundryGroup created from the provisioning group
   */
  public static TrueFoundryGroup fromProvisioningGroup(ProvisioningGroup targetGroup, Set<String> fieldNamesToSet) {
    TrueFoundryGroup trueFoundryGroup = new TrueFoundryGroup();

    // always set id since it's needed for update/delete URLs
    trueFoundryGroup.setId(targetGroup.getId());

    // always set name since it's required for team API calls
    trueFoundryGroup.setName(targetGroup.retrieveAttributeValueString("name"));

    if (fieldNamesToSet == null || fieldNamesToSet.contains("displayName")) {
      trueFoundryGroup.setDisplayName(targetGroup.retrieveAttributeValueString("displayName"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("description")) {
      trueFoundryGroup.setDescription(targetGroup.retrieveAttributeValueString("description"));
    }

    // always set groupType since it determines which API (team vs role) to use
    trueFoundryGroup.setGroupType(targetGroup.retrieveAttributeValueString("groupType"));

    // always set resourceType since it's needed for role assignment
    trueFoundryGroup.setResourceType(targetGroup.retrieveAttributeValueString("resourceType"));

    return trueFoundryGroup;
  }

  private static String booleanToTf(Boolean value) {
    if (value == null) {
      return null;
    }
    return value.booleanValue() ? "T" : "F";
  }


}
