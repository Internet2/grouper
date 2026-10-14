package edu.internet2.middleware.grouper.app.smartsheet;

import java.sql.Types;

import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * A single user-in-group membership. In Smartsheet itself a membership is not a standalone object
 * (it is the members[] list of a group), but the test mock service stores it in its own table.
 *
 * <p>{@link #groupId} is the native group id and {@link #userId} is the native user id.</p>
 */
public class SmartsheetMembership {

  /**
   * Create the mock DB table used by the test mock service to simulate group membership.
   * @param ddlVersionBean ddl bean (unused but part of the createTable contract)
   * @param database the ddlutils database to add the table to
   */
  public static void createTableSmartsheetMembership(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_smartsheet_membership";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_id", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_id", Types.VARCHAR, "40", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_smartsheet_mem_group_idx", false, "group_id");
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_smartsheet_mem_user_idx", false, "user_id");
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_smartsheet_mem_grp_usr_idx", true, "group_id", "user_id");
    }

  }

  /** unique id for Hibernate (assigned via GrouperUuid.getUuid()) */
  private String id;

  /** native id of the group */
  private String groupId;

  /** native id of the member user */
  private String userId;

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

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

}
