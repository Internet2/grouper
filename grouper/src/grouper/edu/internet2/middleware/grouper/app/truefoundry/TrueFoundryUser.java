package edu.internet2.middleware.grouper.app.truefoundry;

import java.sql.Types;
import java.util.Set;

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
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class TrueFoundryUser {

  public static void createTableTrueFoundryUser(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_truefoundry_user";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "100", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "display_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "active", Types.VARCHAR, "1", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_tfy_user_email_idx", true, "email");
    }

  }

  private String id;

  private String email;

  /**
   * display name, set/updated via SCIM PATCH on the native user ID
   */
  private String displayName;

  /**
   * whether the user is active; deactivate sets to false, activate sets to true
   */
  private Boolean active;

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

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public String getActiveString() {
    return booleanToTf(this.active);
  }

  public void setActiveString(String activeString) {
    this.active = GrouperUtil.booleanObjectValue(activeString);
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * Convert from TrueFoundry subjects API response.
   * Expects a user object from the "users" array.
   * @param userNode a user object from the subjects response
   * @return the TrueFoundryUser
   */
  public static TrueFoundryUser fromJson(JsonNode userNode) {
    if (userNode == null) {
      return null;
    }

    TrueFoundryUser trueFoundryUser = new TrueFoundryUser();

    trueFoundryUser.id = GrouperUtil.jsonJacksonGetString(userNode, "id");
    trueFoundryUser.email = GrouperUtil.jsonJacksonGetString(userNode, "email");
    trueFoundryUser.active = GrouperUtil.jsonJacksonGetBoolean(userNode, "active");

    JsonNode metadataNode = GrouperUtil.jsonJacksonGetNode(userNode, "metadata");
    if (metadataNode != null) {
      trueFoundryUser.displayName = GrouperUtil.jsonJacksonGetString(metadataNode, "displayName");
    }

    return trueFoundryUser;
  }

  /**
   * Convert to JSON for the register endpoint (POST /api/svc/v1/users/register).
   * Only email is sent; no ID is returned in the response.
   * @return the register request body
   */
  public ObjectNode toRegisterJson() {
    ObjectNode registerNode = GrouperUtil.jsonJacksonNode();
    registerNode.put("email", this.email);
    registerNode.put("sendInviteEmail", false);
    return registerNode;
  }

  /**
   * Convert to SCIM PATCH JSON for updating displayName.
   * Uses the native user ID directly (no separate SCIM ID lookup needed).
   * Note: do not create users via SCIM — only PATCH existing natively-registered users.
   * @return the SCIM PatchOp request body
   */
  public ObjectNode toScimPatchDisplayNameJson() {
    ObjectNode patchNode = GrouperUtil.jsonJacksonNode();
    ArrayNode schemasArray = GrouperUtil.jsonJacksonArrayNode();
    schemasArray.add("urn:ietf:params:scim:schemas:core:2.0:PatchOp");
    patchNode.set("schemas", schemasArray);

    ArrayNode operationsArray = GrouperUtil.jsonJacksonArrayNode();
    ObjectNode operation = GrouperUtil.jsonJacksonNode();
    operation.put("op", "replace");
    operation.put("path", "displayName");
    operation.put("value", this.displayName);
    operationsArray.add(operation);
    patchNode.set("Operations", operationsArray);

    return patchNode;
  }

  /**
   * Convert to a Grouper provisioning entity.
   * TrueFoundry is email-based, so the provisioning entity ID is the user's email address.
   * The email is also used as the SCIM user identifier for display name updates.
   * @return the converted entity
   */
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity(false);

    // id is the native TrueFoundry user ID (e.g. "pt3vuwlxupmefpk8i9cj11du").
    targetEntity.assignAttributeValue("id", this.id);

    targetEntity.assignAttributeValue("email", this.email);
    if (this.displayName != null) {
      targetEntity.assignAttributeValue("displayName", this.displayName);
    }
    if (this.active != null) {
      targetEntity.assignAttributeValue("active", this.active);
    }

    return targetEntity;
  }

  /**
   * Convert from a provisioning entity to a TrueFoundryUser.
   * The provisioning entity ID is the user's email (TrueFoundry is email-based).
   * @param targetEntity the Grouper provisioning entity to convert
   * @param fieldNamesToSet the field names to be set, or null for all
   * @return the TrueFoundryUser created from the provisioning entity
   */
  public static TrueFoundryUser fromProvisioningEntity(ProvisioningEntity targetEntity, Set<String> fieldNamesToSet) {
    TrueFoundryUser trueFoundryUser = new TrueFoundryUser();

    // entity ID is the native TrueFoundry user ID; email is a separate attribute
    trueFoundryUser.setId(targetEntity.getId());
    String email = targetEntity.retrieveAttributeValueString("email");
    trueFoundryUser.setEmail(email);

    if (fieldNamesToSet == null || fieldNamesToSet.contains("displayName")) {
      trueFoundryUser.setDisplayName(targetEntity.retrieveAttributeValueString("displayName"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("active")) {
      trueFoundryUser.setActive(targetEntity.retrieveAttributeValueBoolean("active"));
    }

    return trueFoundryUser;
  }

  private static String booleanToTf(Boolean value) {
    if (value == null) {
      return null;
    }
    return value.booleanValue() ? "T" : "F";
  }


}
