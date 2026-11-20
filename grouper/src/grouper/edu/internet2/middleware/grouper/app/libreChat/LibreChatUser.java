package edu.internet2.middleware.grouper.app.libreChat;

import java.sql.Types;

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

/**
 * A LibreChat user.  This is the provisioner's TARGET ENTITY.  LibreChat creates users on their first
 * login, so the provisioner only reads them; it never creates, updates or deletes users.  A Grouper
 * subject is matched to a user by username (e.g. the eppn).
 *
 * GRP-7594
 */
public class LibreChatUser {

  /**
   * Create the mock table used by the test mock service to simulate LibreChat users
   * @param ddlVersionBean ddl bean (unused but part of the createTable contract)
   * @param database the ddlutils database to add the table to
   */
  public static void createTableLibreChatUser(DdlVersionBean ddlVersionBean, Database database) {
    final String tableName = "mock_librechat_user";
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "username", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email", Types.VARCHAR, "256", false, false);
    }
  }

  /** LibreChat user _id (24 hex chars) */
  private String id;

  /** username, e.g. the eppn from SSO */
  private String username;

  /** display name */
  private String name;

  /** email */
  private String email;

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return this.email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * Parse a user from /api/admin/users or /api/admin/users/search ("id", with "_id" as a fallback)
   * @param node JSON node
   * @return the user, or null if node is null
   */
  public static LibreChatUser fromJson(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    LibreChatUser user = new LibreChatUser();
    user.id = GrouperUtil.jsonJacksonGetString(node, "id");
    if (GrouperUtil.isBlank(user.id)) {
      user.id = GrouperUtil.jsonJacksonGetString(node, "_id");
    }
    user.username = GrouperUtil.jsonJacksonGetString(node, "username");
    user.name = GrouperUtil.jsonJacksonGetString(node, "name");
    user.email = GrouperUtil.jsonJacksonGetString(node, "email");
    return user;
  }

  /**
   * JSON like the admin API returns it (used by the mock service)
   * @return the JSON node
   */
  public ObjectNode toJson() {
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    GrouperUtil.jsonJacksonAssignString(result, "id", this.id);
    GrouperUtil.jsonJacksonAssignString(result, "username", this.username);
    GrouperUtil.jsonJacksonAssignString(result, "name", this.name);
    GrouperUtil.jsonJacksonAssignString(result, "email", this.email);
    return result;
  }

  /**
   * Convert to a Grouper provisioning entity
   * @return the provisioning entity
   */
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity(false);
    targetEntity.setId(this.id);
    targetEntity.assignAttributeValue("username", this.username);
    if (this.name != null) {
      targetEntity.assignAttributeValue("name", this.name);
    }
    if (this.email != null) {
      targetEntity.assignAttributeValue("email", this.email);
    }
    return targetEntity;
  }

}
