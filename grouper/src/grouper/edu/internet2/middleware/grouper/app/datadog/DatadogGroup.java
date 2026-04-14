package edu.internet2.middleware.grouper.app.datadog;

import java.sql.Types;
import java.util.LinkedHashSet;
import java.util.Set;

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

public class DatadogGroup {

  public static void createTableDatadogGroup(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_datadog_group";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "handle", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "1024", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_type", Types.VARCHAR, "20", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_dd_grp_name_type_idx", true, "name", "group_type");
    }

  }

  private String id;
  private String name;

  /**
   * team handle (URL-friendly slug), used for teams only
   */
  private String handle;

  /**
   * team description, used for teams only
   */
  private String description;

  /**
   * type of group: "role" or "team"
   */
  private String groupType;

  /**
   * set of user IDs that are team admins (only populated when datadogAddTeamAdminMetadata is true)
   */
  private Set<String> admins;

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

  public String getHandle() {
    return handle;
  }

  public void setHandle(String handle) {
    this.handle = handle;
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

  public Set<String> getAdmins() {
    return admins;
  }

  public void setAdmins(Set<String> admins) {
    this.admins = admins;
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * Convert from Datadog JSON:API format.
   * Expects the "data" object (with type, id, attributes).
   * @param dataNode the "data" object from the JSON:API response
   * @return the DatadogGroup
   */
  public static DatadogGroup fromJson(JsonNode dataNode) {
    if (dataNode == null) {
      return null;
    }

    DatadogGroup datadogGroup = new DatadogGroup();

    datadogGroup.id = GrouperUtil.jsonJacksonGetString(dataNode, "id");

    JsonNode attributesNode = GrouperUtil.jsonJacksonGetNode(dataNode, "attributes");
    if (attributesNode != null) {
      datadogGroup.name = GrouperUtil.jsonJacksonGetString(attributesNode, "name");
      datadogGroup.handle = GrouperUtil.jsonJacksonGetString(attributesNode, "handle");
      datadogGroup.description = GrouperUtil.jsonJacksonGetString(attributesNode, "description");
    }

    return datadogGroup;
  }

  /**
   * Convert to Datadog JSON:API format for create/update requests.
   * Produces the "data" object with type and attributes.
   * The JSON:API "type" depends on groupType (roles vs teams).
   * @param fieldNamesToSet the field names to include, or null for all
   * @return the JSON:API "data" object
   */
  public ObjectNode toJson(Set<String> fieldNamesToSet) {
    ObjectNode dataNode = GrouperUtil.jsonJacksonNode();
    dataNode.put("type", jsonApiType());

    if (this.id != null) {
      dataNode.put("id", this.id);
    }

    ObjectNode attributesNode = GrouperUtil.jsonJacksonNode();

    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {
      if (this.name != null) {
        attributesNode.put("name", this.name);
      }
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("handle")) {
      if (this.handle != null) {
        attributesNode.put("handle", this.handle);
      }
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("description")) {
      if (this.description != null) {
        attributesNode.put("description", this.description);
      }
    }

    dataNode.set("attributes", attributesNode);

    return dataNode;
  }

  /**
   * Get the JSON:API type string for this group type.
   * @return "roles" or "teams"
   */
  public String jsonApiType() {
    if ("team".equals(this.groupType)) {
      return "team";
    }
    return "roles";
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
    if (this.handle != null) {
      targetGroup.assignAttributeValue("handle", this.handle);
    }
    if (this.description != null) {
      targetGroup.assignAttributeValue("description", this.description);
    }
    if (this.groupType != null) {
      targetGroup.assignAttributeValue("groupType", this.groupType);
    }
    if (this.admins != null) {
      targetGroup.assignAttributeValue("admins", this.admins);
    }

    return targetGroup;
  }

  /**
   * Convert from a provisioning group to a DatadogGroup
   * @param targetGroup the Grouper provisioning group to convert
   * @param fieldNamesToSet the field names to be set
   * @return the DatadogGroup created from the provisioning group
   */
  public static DatadogGroup fromProvisioningGroup(ProvisioningGroup targetGroup, Set<String> fieldNamesToSet) {
    DatadogGroup datadogGroup = new DatadogGroup();

    // always set id since it's needed for update/delete URLs
    datadogGroup.setId(targetGroup.getId());

    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {
      datadogGroup.setName(targetGroup.retrieveAttributeValueString("name"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("handle")) {
      datadogGroup.setHandle(targetGroup.retrieveAttributeValueString("handle"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("description")) {
      datadogGroup.setDescription(targetGroup.retrieveAttributeValueString("description"));
    }

    // always set groupType since it determines which API (team vs role) to use
    datadogGroup.setGroupType(targetGroup.retrieveAttributeValueString("groupType"));

    // always set admins
    @SuppressWarnings("unchecked")
    Set<String> admins = (Set<String>) targetGroup.retrieveAttributeValueSet("admins");
    datadogGroup.setAdmins(admins);

    return datadogGroup;
  }

}
