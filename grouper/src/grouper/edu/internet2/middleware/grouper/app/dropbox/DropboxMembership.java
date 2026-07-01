package edu.internet2.middleware.grouper.app.dropbox;

import java.sql.Types;

import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * Domain object for a Dropbox Business group membership (the association between
 * a team group and a team member).
 *
 * <p>Maps to the members returned by /2/team/groups/get_info and
 * /2/team/groups/members/list.  Memberships are written via
 * /2/team/groups/members/add and /2/team/groups/members/remove, which reference
 * the member by team_member_id.  access_type distinguishes regular members from
 * group owners.</p>
 */
public class DropboxMembership {

  /** access_type value for a regular group member */
  public static final String ACCESS_TYPE_MEMBER = "member";

  /** access_type value for a group owner (can manage the group) */
  public static final String ACCESS_TYPE_OWNER = "owner";

  /**
   * Create the mock database table that simulates Dropbox group memberships for tests.
   * @param ddlVersionBean ddl version bean
   * @param database the database model
   */
  public static void createTableDropboxMembership(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_dropbox_membership";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      // id is a Hibernate-only surrogate key (Dropbox has no membership id)
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_id", Types.VARCHAR, "100", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "team_member_id", Types.VARCHAR, "100", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "access_type", Types.VARCHAR, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_dbx_mem_group_idx", false, "group_id");
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_dbx_mem_member_idx", false, "team_member_id");
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_dbx_mem_grp_mem_idx", true, "group_id", "team_member_id");
    }

  }

  /** unique surrogate id for Hibernate (assigned via GrouperUuid.getUuid()) */
  private String id;

  /** native Dropbox group_id this membership belongs to */
  private String groupId;

  /** native Dropbox team_member_id of the member */
  private String teamMemberId;

  /** access_type: "member" or "owner" */
  private String accessType;

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

  public String getTeamMemberId() {
    return teamMemberId;
  }

  public void setTeamMemberId(String teamMemberId) {
    this.teamMemberId = teamMemberId;
  }

  public String getAccessType() {
    return accessType;
  }

  public void setAccessType(String accessType) {
    this.accessType = accessType;
  }

  /**
   * @return true if this membership grants group owner access
   */
  public boolean isOwner() {
    return ACCESS_TYPE_OWNER.equals(this.accessType);
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

}
