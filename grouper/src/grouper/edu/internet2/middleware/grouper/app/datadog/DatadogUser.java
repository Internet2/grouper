package edu.internet2.middleware.grouper.app.datadog;

import java.sql.Types;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class DatadogUser {

  public static void createTableDatadogUser(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_datadog_user";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "title", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "disabled", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "service_account", Types.VARCHAR, "1", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_datadog_user_email_idx", true, "email");
    }

  }

  private String id;
  private String email;
  private String name;
  private String title;
  private String handle;
  private Boolean disabled;
  private Boolean serviceAccount;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getHandle() {
    return handle;
  }

  public void setHandle(String handle) {
    this.handle = handle;
  }

  public Boolean getDisabled() {
    return disabled;
  }

  public void setDisabled(Boolean disabled) {
    this.disabled = disabled;
  }

  public String getDisabledString() {
    return booleanToTf(this.disabled);
  }

  public void setDisabledString(String disabledString) {
    this.disabled = tfToBoolean(disabledString);
  }

  public Boolean getServiceAccount() {
    return serviceAccount;
  }

  public void setServiceAccount(Boolean serviceAccount) {
    this.serviceAccount = serviceAccount;
  }

  public String getServiceAccountString() {
    return booleanToTf(this.serviceAccount);
  }

  public void setServiceAccountString(String serviceAccountString) {
    this.serviceAccount = tfToBoolean(serviceAccountString);
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * Convert from Datadog JSON:API format.
   * Expects the "data" object (with type, id, attributes).
   * @param dataNode the "data" object from the JSON:API response
   * @return the DatadogUser
   */
  public static DatadogUser fromJson(JsonNode dataNode) {
    if (dataNode == null) {
      return null;
    }

    DatadogUser datadogUser = new DatadogUser();

    datadogUser.id = GrouperUtil.jsonJacksonGetString(dataNode, "id");

    JsonNode attributesNode = GrouperUtil.jsonJacksonGetNode(dataNode, "attributes");
    if (attributesNode != null) {
      datadogUser.email = GrouperUtil.jsonJacksonGetString(attributesNode, "email");
      datadogUser.name = GrouperUtil.jsonJacksonGetString(attributesNode, "name");
      datadogUser.title = GrouperUtil.jsonJacksonGetString(attributesNode, "title");
      datadogUser.handle = GrouperUtil.jsonJacksonGetString(attributesNode, "handle");
      datadogUser.disabled = GrouperUtil.jsonJacksonGetBoolean(attributesNode, "disabled");
      datadogUser.serviceAccount = GrouperUtil.jsonJacksonGetBoolean(attributesNode, "service_account");
    }

    return datadogUser;
  }

  /**
   * Convert to Datadog JSON:API format for create/update requests.
   * Produces the "data" object with type, id (if set), and attributes.
   * @param fieldNamesToSet the field names to include, or null for all
   * @return the JSON:API "data" object
   */
  public ObjectNode toJson(Set<String> fieldNamesToSet) {
    ObjectNode dataNode = GrouperUtil.jsonJacksonNode();
    dataNode.put("type", "users");

    if (this.id != null) {
      dataNode.put("id", this.id);
    }

    ObjectNode attributesNode = GrouperUtil.jsonJacksonNode();

    if (fieldNamesToSet == null || fieldNamesToSet.contains("email")) {
      if (this.email != null) {
        attributesNode.put("email", this.email);
      }
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {
      if (this.name != null) {
        attributesNode.put("name", this.name);
      }
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("title")) {
      if (this.title != null) {
        attributesNode.put("title", this.title);
      }
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("disabled")) {
      if (this.disabled != null) {
        attributesNode.put("disabled", this.disabled);
      }
    }

    dataNode.set("attributes", attributesNode);

    return dataNode;
  }

  /**
   * Convert to a Grouper provisioning entity
   * @return the converted entity
   */
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity(false);

    if (this.id != null) {
      targetEntity.setId(this.id);
    }
    targetEntity.assignAttributeValue("email", this.email);
    targetEntity.assignAttributeValue("name", this.name);
    targetEntity.assignAttributeValue("title", this.title);

    if (this.disabled != null) {
      targetEntity.assignAttributeValue("disabled", this.disabled);
    }
    if (this.serviceAccount != null) {
      targetEntity.assignAttributeValue("serviceAccount", this.serviceAccount);
    }

    return targetEntity;
  }

  /**
   * Convert from a provisioning entity to a DatadogUser
   * @param targetEntity the Grouper provisioning entity to convert
   * @param fieldNamesToSet the field names to be set
   * @return the DatadogUser created from the provisioning entity
   */
  public static DatadogUser fromProvisioningEntity(ProvisioningEntity targetEntity, Set<String> fieldNamesToSet) {
    DatadogUser datadogUser = new DatadogUser();

    // always set id since it's needed for update/delete URLs
    datadogUser.setId(targetEntity.getId());

    if (fieldNamesToSet == null || fieldNamesToSet.contains("email")) {
      datadogUser.setEmail(targetEntity.retrieveAttributeValueString("email"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {
      datadogUser.setName(targetEntity.retrieveAttributeValueString("name"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("title")) {
      datadogUser.setTitle(targetEntity.retrieveAttributeValueString("title"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("disabled")) {
      datadogUser.setDisabled(targetEntity.retrieveAttributeValueBoolean("disabled"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("serviceAccount")) {
      datadogUser.setServiceAccount(targetEntity.retrieveAttributeValueBoolean("serviceAccount"));
    }

    return datadogUser;
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

}
