package edu.internet2.middleware.grouper.app.smartsheet;

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

/**
 * A Smartsheet group (the object under <code>/2.0/groups/{groupId}</code>). This is the
 * provisioner's TARGET GROUP.
 *
 * <p>Group names are unique within the organization. Groups are matched by the native group id
 * (cached once linked) with name as the backup for the first link, so a rename in Grouper is an
 * in-place update. The group owner is the user whose token created it (that user must be a Group
 * Admin or System Admin); Grouper does not manage the owner.</p>
 */
public class SmartsheetGroup {

  /**
   * Create the mock DB table used by the test mock service to simulate Smartsheet groups.
   * Membership is stored separately in {@link SmartsheetMembership}.
   * @param ddlVersionBean ddl bean (unused but part of the createTable contract)
   * @param database the ddlutils database to add the table to
   */
  public static void createTableSmartsheetGroup(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_smartsheet_group";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "1024", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "owner", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "owner_id", Types.VARCHAR, "40", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_smartsheet_group_name_idx", true, "name");
    }

  }

  /** native Smartsheet group id (a long, held as a string); null until created */
  private String id;

  /** group name, unique in the organization */
  private String name;

  /** group description */
  private String description;

  /** email of the group owner (read only) */
  private String owner;

  /** user id of the group owner (read only) */
  private String ownerId;

  /** members, only populated when the group detail (GET /groups/{id}) was fetched */
  private List<SmartsheetUser> members;

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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public List<SmartsheetUser> getMembers() {
    return members;
  }

  public void setMembers(List<SmartsheetUser> members) {
    this.members = members;
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * Parse a Smartsheet group from JSON (an element of GET /groups data[], the GET /groups/{id}
   * detail, or the result of a POST/PUT). When present, the members array is parsed too.
   * @param node the group JSON object
   * @return the SmartsheetGroup, or null if node is null
   */
  public static SmartsheetGroup fromJson(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }

    SmartsheetGroup group = new SmartsheetGroup();
    group.id = GrouperUtil.jsonJacksonGetString(node, "id");
    group.name = GrouperUtil.jsonJacksonGetString(node, "name");
    group.description = GrouperUtil.jsonJacksonGetString(node, "description");
    group.owner = GrouperUtil.jsonJacksonGetString(node, "owner");
    group.ownerId = GrouperUtil.jsonJacksonGetString(node, "ownerId");

    ArrayNode membersNode = GrouperUtil.jsonJacksonGetArrayNode(node, "members");
    if (membersNode != null) {
      List<SmartsheetUser> memberList = new ArrayList<SmartsheetUser>();
      for (int i = 0; i < membersNode.size(); i++) {
        SmartsheetUser member = SmartsheetUser.fromJson(membersNode.get(i));
        if (member != null) {
          memberList.add(member);
        }
      }
      group.members = memberList;
    }

    return group;
  }

  /**
   * Build the JSON body for POST /groups (create) or PUT /groups/{id} (update). On update only the
   * fields in fieldNamesToUpdate are sent. Members are never sent here; Grouper manages them with
   * the group member endpoints.
   * @param fieldNamesToUpdate target attribute names that changed; null sends all managed fields
   * @return the JSON object
   */
  public ObjectNode toJson(Set<String> fieldNamesToUpdate) {
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    if (fieldNamesToUpdate == null || fieldNamesToUpdate.contains("name")) {
      result.put("name", this.name);
    }
    if (fieldNamesToUpdate == null || fieldNamesToUpdate.contains("description")) {
      if (this.description != null || fieldNamesToUpdate != null) {
        result.put("description", GrouperUtil.defaultString(this.description));
      }
    }
    return result;
  }

  /**
   * Convert to a Grouper provisioning group. id is the native group id, name is the backup
   * matching attribute.
   * @return the converted provisioning group
   */
  public ProvisioningGroup toProvisioningGroup() {
    ProvisioningGroup targetGroup = new ProvisioningGroup(false);

    if (this.id != null) {
      targetGroup.setId(this.id);
    }
    targetGroup.assignAttributeValue("name", this.name);
    if (this.description != null) {
      targetGroup.assignAttributeValue("description", this.description);
    }
    if (this.owner != null) {
      targetGroup.assignAttributeValue("owner", this.owner);
    }

    return targetGroup;
  }

  /**
   * Build a SmartsheetGroup from a Grouper provisioning group.
   * @param targetGroup the Grouper provisioning group
   * @return the SmartsheetGroup
   */
  public static SmartsheetGroup fromProvisioningGroup(ProvisioningGroup targetGroup) {
    SmartsheetGroup group = new SmartsheetGroup();
    group.setId(targetGroup.getId());
    group.setName(targetGroup.retrieveAttributeValueString("name"));
    group.setDescription(targetGroup.retrieveAttributeValueString("description"));
    return group;
  }

}
