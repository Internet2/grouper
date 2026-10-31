package edu.internet2.middleware.grouper.app.freshServiceAgent;

import java.sql.Types;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * A Freshservice agent role.  A Grouper group with groupType "role" provisions to a role instead of
 * an agent group.  Roles are read-only in the Freshservice API (list and view only), so the role must
 * already exist in Freshservice, and the role memberships are the "roles" array on each agent.
 *
 * Agent group ids and role ids are both Freshservice numeric ids and could collide, so the provisioning
 * target id of a role is prefixed: role_123.  That way the DAO can tell a role from an agent group from
 * just the target id (e.g. a membership only has the target group id)
 */
public class FreshAgentRole {

  /**
   * group type of a Grouper group that provisions to a Freshservice agent group
   */
  public static final String GROUP_TYPE_GROUP = "group";

  /**
   * group type of a Grouper group that provisions to a Freshservice agent role
   */
  public static final String GROUP_TYPE_ROLE = "role";

  /**
   * prefix of the provisioning target id of a role so it does not collide with an agent group id
   */
  public static final String ROLE_TARGET_ID_PREFIX = "role_";

  /**
   * see if a provisioning target group id is a role
   * @param targetGroupId
   * @return true if role
   */
  public static boolean isRoleTargetId(String targetGroupId) {
    return targetGroupId != null && targetGroupId.startsWith(ROLE_TARGET_ID_PREFIX);
  }

  /**
   * convert a role provisioning target id (role_123) to the Freshservice role id (123)
   * @param targetGroupId
   * @return the role id
   */
  public static Long roleIdFromTargetId(String targetGroupId) {
    if (!isRoleTargetId(targetGroupId)) {
      throw new RuntimeException("Not a role target id: '" + targetGroupId + "'");
    }
    return GrouperUtil.longValue(targetGroupId.substring(ROLE_TARGET_ID_PREFIX.length()));
  }

  /**
   * convert a Freshservice role id (123) to the role provisioning target id (role_123)
   * @param roleId
   * @return the target id
   */
  public static String targetIdFromRoleId(Long roleId) {
    return roleId == null ? null : (ROLE_TARGET_ID_PREFIX + roleId);
  }

  /**
   * mock table of roles.  The mock has no role endpoints that write since Freshservice doesn't, so tests
   * insert the roles with sql
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableFreshRole(DdlVersionBean ddlVersionBean, Database database) {

    final String roleTableName = "mock_freshagent_role";

    try {
      new GcDbAccess().sql("select count(*) from " + roleTableName).select(int.class);
    } catch (Exception e) {

      Table roleTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, roleTableName);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(roleTable, "id", Types.BIGINT, "20", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(roleTable, "name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(roleTable, "description", Types.VARCHAR, "1024", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, roleTableName, "mock_freshagent_role_name_idx", true, "name");
    }

  }

  /**
   * convert from jackson json
   * @param roleNode
   * @return the role
   */
  public static FreshAgentRole fromJson(JsonNode roleNode) {
    if (roleNode == null) {
      return null;
    }

    FreshAgentRole freshAgentRole = new FreshAgentRole();
    freshAgentRole.id = GrouperUtil.jsonJacksonGetLong(roleNode, "id");
    freshAgentRole.name = GrouperUtil.jsonJacksonGetString(roleNode, "name");
    freshAgentRole.description = GrouperUtil.jsonJacksonGetString(roleNode, "description");

    return freshAgentRole;
  }

  /**
   * Convert to a provisioning group, the target id is prefixed (role_123) and the groupType is role
   * @return a Provisioning Group object
   */
  public ProvisioningGroup toProvisioningGroup() {
    ProvisioningGroup targetGroup = new ProvisioningGroup();
    if (this.id != null) {
      targetGroup.setId(targetIdFromRoleId(this.id));
    }
    targetGroup.assignAttributeValue("description", this.description);
    targetGroup.assignAttributeValue("name", this.name);
    targetGroup.assignAttributeValue("groupType", GROUP_TYPE_ROLE);
    return targetGroup;
  }

  /**
   * see if this role matches a search attribute (id or name) of a provisioning target group
   * @param searchAttribute id or name
   * @param searchAttributeValue role_123 or 123 for id, or the name
   * @return true if matches
   */
  public boolean matches(String searchAttribute, Object searchAttributeValue) {
    String value = GrouperUtil.stringValue(searchAttributeValue);
    if (StringUtils.equals("id", searchAttribute)) {
      if (this.id == null || value == null) {
        return false;
      }
      return StringUtils.equals(targetIdFromRoleId(this.id), value) || StringUtils.equals(Long.toString(this.id), value);
    }
    if (StringUtils.equals("name", searchAttribute)) {
      return StringUtils.equals(this.name, value);
    }
    throw new RuntimeException("id or name is required as a group search attribute");
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  private Long id;
  private String name;
  private String description;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
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

}
