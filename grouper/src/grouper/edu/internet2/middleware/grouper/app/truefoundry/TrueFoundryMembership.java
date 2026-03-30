package edu.internet2.middleware.grouper.app.truefoundry;

import java.sql.Types;

import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class TrueFoundryMembership {

  /**
   * membership role value for a regular team member
   */
  public static final String ROLE_MEMBER = "member";

  /**
   * membership role value for a team manager
   */
  public static final String ROLE_MANAGER = "manager";

  public static void createTableTrueFoundryMembership(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_truefoundry_membership";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_id", Types.VARCHAR, "100", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_email", Types.VARCHAR, "256", false, true);
      // role is "manager" for team managers; null for role assignments
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "role", Types.VARCHAR, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_tfy_mem_group_idx", false, "group_id");
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_tfy_mem_user_idx", false, "user_email");
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_tfy_mem_grp_usr_idx", true, "group_id", "user_email");
    }

  }

  /**
   * unique ID for Hibernate (assigned via GrouperUuid.getUuid())
   */
  private String id;

  /**
   * ID of the group (team ID or role ID)
   */
  private String groupId;

  /**
   * email of the member user — TrueFoundry uses email (not user ID) for membership operations
   */
  private String userEmail;

  /**
   * for team memberships: "member" or "manager" (from manifest.members vs manifest.managers)
   * for role assignments: null
   */
  private String role;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public void setUserEmail(String userEmail) {
    this.userEmail = userEmail;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  /**
   * Returns true if this membership is a team manager role
   */
  public boolean isManager() {
    return ROLE_MANAGER.equals(this.role);
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

}
