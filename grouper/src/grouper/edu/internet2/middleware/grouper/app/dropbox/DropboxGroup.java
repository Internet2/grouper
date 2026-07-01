package edu.internet2.middleware.grouper.app.dropbox;

import java.sql.Types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * Domain object for a Dropbox Business team group.
 *
 * <p>Maps to the Dropbox Team API group object as returned by /2/team/groups/list,
 * /2/team/groups/get_info, and /2/team/groups/create.  Grouper manages
 * "company_managed" groups and matches them by the group_external_id field, which
 * Grouper sets to a stable value (e.g. the Grouper group idIndex or name).</p>
 */
public class DropboxGroup {

  /**
   * group_management_type value for a group whose membership is managed by the
   * team (i.e. via the API).  Grouper only provisions company_managed groups.
   */
  public static final String MANAGEMENT_TYPE_COMPANY_MANAGED = "company_managed";

  /**
   * group_management_type value for a group whose membership is managed by the
   * group's own owners in the Dropbox UI.  Grouper does not manage these.
   */
  public static final String MANAGEMENT_TYPE_USER_MANAGED = "user_managed";

  /**
   * Create the mock database table that simulates the Dropbox group store for tests.
   * @param ddlVersionBean ddl version bean
   * @param database the database model
   */
  public static void createTableDropboxGroup(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_dropbox_group";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      // id is the native Dropbox group_id (e.g. "g:abc123def456")
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "100", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "255", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "external_id", Types.VARCHAR, "255", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "management_type", Types.VARCHAR, "30", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "member_count", Types.INTEGER, "11", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_dbx_grp_name_idx", true, "name");
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_dbx_grp_extid_idx", false, "external_id");
    }

  }

  /** native Dropbox group_id (e.g. "g:abc123") */
  private String id;

  /** group_name shown in the Dropbox admin console */
  private String name;

  /** group_external_id — Grouper sets this to a stable match key */
  private String externalId;

  /** group_management_type — "company_managed" or "user_managed" */
  private String managementType;

  /** member_count from list/get_info responses (read-only, not written by Grouper) */
  private Integer memberCount;

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

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public String getManagementType() {
    return managementType;
  }

  public void setManagementType(String managementType) {
    this.managementType = managementType;
  }

  public Integer getMemberCount() {
    return memberCount;
  }

  public void setMemberCount(Integer memberCount) {
    this.memberCount = memberCount;
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * Convert from a Dropbox group JSON node.  Works for both the group summary
   * objects in /2/team/groups/list and the fuller objects in
   * /2/team/groups/get_info and /2/team/groups/create (member lists are read
   * separately as memberships).
   * @param groupNode a Dropbox group JSON object
   * @return the DropboxGroup, or null if groupNode is null
   */
  public static DropboxGroup fromJson(JsonNode groupNode) {
    if (groupNode == null) {
      return null;
    }

    DropboxGroup dropboxGroup = new DropboxGroup();
    dropboxGroup.id = GrouperUtil.jsonJacksonGetString(groupNode, "group_id");
    dropboxGroup.name = GrouperUtil.jsonJacksonGetString(groupNode, "group_name");
    dropboxGroup.externalId = GrouperUtil.jsonJacksonGetString(groupNode, "group_external_id");

    // group_management_type is a Dropbox union: { ".tag": "company_managed" }
    JsonNode managementTypeNode = GrouperUtil.jsonJacksonGetNode(groupNode, "group_management_type");
    if (managementTypeNode != null) {
      dropboxGroup.managementType = GrouperUtil.jsonJacksonGetString(managementTypeNode, ".tag");
    }

    dropboxGroup.memberCount = GrouperUtil.jsonJacksonGetInteger(groupNode, "member_count");

    return dropboxGroup;
  }

  /**
   * Convert to the request body for POST /2/team/groups/create.
   * @return the create request body
   */
  public ObjectNode toCreateJson() {
    ObjectNode bodyNode = GrouperUtil.jsonJacksonNode();
    bodyNode.put("group_name", this.name);
    if (!GrouperUtil.isBlank(this.externalId)) {
      bodyNode.put("group_external_id", this.externalId);
    }
    // Grouper-provisioned groups are always company_managed so membership is API-managed
    ObjectNode managementTypeNode = GrouperUtil.jsonJacksonNode();
    managementTypeNode.put(".tag",
        GrouperUtil.defaultIfBlank(this.managementType, MANAGEMENT_TYPE_COMPANY_MANAGED));
    bodyNode.set("group_management_type", managementTypeNode);
    // do not add the API caller as an owner of the group
    bodyNode.put("add_creator_as_owner", false);
    return bodyNode;
  }

  /**
   * Convert to the request body for POST /2/team/groups/update.  The group is
   * selected by group_id; only the supplied new_* fields are changed.
   * @return the update request body
   */
  public ObjectNode toUpdateJson() {
    ObjectNode bodyNode = GrouperUtil.jsonJacksonNode();

    // GroupSelector union selecting by group_id
    ObjectNode groupSelectorNode = GrouperUtil.jsonJacksonNode();
    groupSelectorNode.put(".tag", "group_id");
    groupSelectorNode.put("group_id", this.id);
    bodyNode.set("group", groupSelectorNode);

    if (this.name != null) {
      bodyNode.put("new_group_name", this.name);
    }
    if (this.externalId != null) {
      bodyNode.put("new_group_external_id", this.externalId);
    }
    bodyNode.put("return_members", false);
    return bodyNode;
  }

  /**
   * Convert to a Grouper provisioning group.  The provisioning group id is the
   * native Dropbox group_id; matching is configured on the externalId attribute.
   * @return the converted target group
   */
  public ProvisioningGroup toProvisioningGroup() {
    ProvisioningGroup targetGroup = new ProvisioningGroup(false);

    if (this.id != null) {
      targetGroup.setId(this.id);
    }
    targetGroup.assignAttributeValue("name", this.name);
    if (this.externalId != null) {
      targetGroup.assignAttributeValue("externalId", this.externalId);
    }
    if (this.managementType != null) {
      targetGroup.assignAttributeValue("managementType", this.managementType);
    }
    if (this.memberCount != null) {
      targetGroup.assignAttributeValue("memberCount", this.memberCount);
    }

    return targetGroup;
  }

  /**
   * Convert from a Grouper provisioning group to a DropboxGroup.
   * @param targetGroup the Grouper provisioning group
   * @param fieldNamesToSet field names to set, or null for all
   * @return the DropboxGroup
   */
  public static DropboxGroup fromProvisioningGroup(ProvisioningGroup targetGroup, java.util.Set<String> fieldNamesToSet) {
    DropboxGroup dropboxGroup = new DropboxGroup();

    // always carry the native group_id (needed for update/delete selectors)
    dropboxGroup.setId(targetGroup.getId());

    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {
      dropboxGroup.setName(targetGroup.retrieveAttributeValueString("name"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("externalId")) {
      dropboxGroup.setExternalId(targetGroup.retrieveAttributeValueString("externalId"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("managementType")) {
      dropboxGroup.setManagementType(targetGroup.retrieveAttributeValueString("managementType"));
    }

    return dropboxGroup;
  }

}
