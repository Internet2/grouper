package edu.internet2.middleware.grouper.app.libreChat;

import java.sql.Types;

import org.apache.commons.lang3.StringUtils;

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
 * A LibreChat group (the object under /api/admin/groups/{id}).  This is the provisioner's TARGET GROUP.
 *
 * <p>Grouper only manages groups it created: they have source "local" and an idOnTheSource that starts
 * with the configured prefix (default "grouper:").  In the provisioning object the prefix is removed, so
 * the "idOnTheSource" attribute holds just the Grouper value (e.g. the group uuid); the prefix is added
 * back when the group is written to LibreChat.</p>
 *
 * GRP-7594
 */
public class LibreChatGroup {

  /**
   * Create the mock table used by the test mock service to simulate LibreChat groups.  Members are in
   * {@link LibreChatMembership}.
   * @param ddlVersionBean ddl bean (unused but part of the createTable contract)
   * @param database the ddlutils database to add the table to
   */
  public static void createTableLibreChatGroup(DdlVersionBean ddlVersionBean, Database database) {
    final String tableName = "mock_librechat_group";
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "1024", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "source", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id_on_the_source", Types.VARCHAR, "256", false, false);
    }
  }

  /** LibreChat group _id (24 hex chars) */
  private String id;

  /** display name */
  private String name;

  /** description */
  private String description;

  /** local or entra */
  private String source;

  /** external id as stored in LibreChat, including the provisioner's prefix */
  private String idOnTheSource;

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getSource() {
    return this.source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getIdOnTheSource() {
    return this.idOnTheSource;
  }

  public void setIdOnTheSource(String idOnTheSource) {
    this.idOnTheSource = idOnTheSource;
  }

  /**
   * @param prefix the provisioner's idOnTheSource prefix
   * @return true if this group was made by this provisioner (local, and idOnTheSource has the prefix),
   * and has a valid id (anything else is never read or changed)
   */
  public boolean isManaged(String prefix) {
    return LibreChatApiCommands.isObjectId(this.id)
        && StringUtils.equals("local", StringUtils.defaultIfBlank(this.source, "local"))
        && !StringUtils.isBlank(prefix)
        && StringUtils.startsWith(this.idOnTheSource, prefix);
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * Parse a group from the admin API.  Accepts a bare group or the {"group": {...}} wrapper.  The id is
   * "_id" (a Mongo document) with "id" as a fallback.
   * @param node JSON node
   * @return the group, or null if node is null
   */
  public static LibreChatGroup fromJson(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    JsonNode groupNode = node.has("group") ? node.get("group") : node;
    LibreChatGroup group = new LibreChatGroup();
    group.id = GrouperUtil.jsonJacksonGetString(groupNode, "_id");
    if (StringUtils.isBlank(group.id)) {
      group.id = GrouperUtil.jsonJacksonGetString(groupNode, "id");
    }
    group.name = GrouperUtil.jsonJacksonGetString(groupNode, "name");
    group.description = GrouperUtil.jsonJacksonGetString(groupNode, "description");
    group.source = GrouperUtil.jsonJacksonGetString(groupNode, "source");
    group.idOnTheSource = GrouperUtil.jsonJacksonGetString(groupNode, "idOnTheSource");
    return group;
  }

  /**
   * JSON like the admin API returns it (used by the mock service)
   * @return the JSON node
   */
  public ObjectNode toJson() {
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    GrouperUtil.jsonJacksonAssignString(result, "_id", this.id);
    GrouperUtil.jsonJacksonAssignString(result, "name", this.name);
    GrouperUtil.jsonJacksonAssignString(result, "description", this.description);
    GrouperUtil.jsonJacksonAssignString(result, "source", this.source);
    GrouperUtil.jsonJacksonAssignString(result, "idOnTheSource", this.idOnTheSource);
    return result;
  }

  /**
   * Convert to a Grouper provisioning group, removing the prefix from idOnTheSource
   * @param prefix the provisioner's idOnTheSource prefix
   * @return the provisioning group
   */
  public ProvisioningGroup toProvisioningGroup(String prefix) {
    ProvisioningGroup targetGroup = new ProvisioningGroup(false);
    targetGroup.setId(this.id);
    targetGroup.assignAttributeValue("name", this.name);
    if (this.description != null) {
      targetGroup.assignAttributeValue("description", this.description);
    }
    if (this.idOnTheSource != null) {
      targetGroup.assignAttributeValue("idOnTheSource", StringUtils.removeStart(this.idOnTheSource, prefix));
    }
    return targetGroup;
  }

  /**
   * Convert from a Grouper provisioning group, adding the prefix to idOnTheSource
   * @param targetGroup the provisioning group
   * @param prefix the provisioner's idOnTheSource prefix
   * @return the LibreChat group
   */
  public static LibreChatGroup fromProvisioningGroup(ProvisioningGroup targetGroup, String prefix) {
    LibreChatGroup group = new LibreChatGroup();
    group.id = targetGroup.getId();
    group.name = targetGroup.retrieveAttributeValueString("name");
    group.description = targetGroup.retrieveAttributeValueString("description");
    group.source = "local";
    String idOnTheSourceValue = targetGroup.retrieveAttributeValueString("idOnTheSource");
    if (!StringUtils.isBlank(idOnTheSourceValue)) {
      group.idOnTheSource = StringUtils.defaultString(prefix) + idOnTheSourceValue;
    }
    return group;
  }

}
