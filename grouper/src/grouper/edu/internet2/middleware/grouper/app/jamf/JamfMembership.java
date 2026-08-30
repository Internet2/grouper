package edu.internet2.middleware.grouper.app.jamf;

import java.sql.Types;

import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * A single account-in-role membership: one entry of a Jamf account group's
 * <code>&lt;members&gt;</code> list. In Jamf itself membership is not a standalone object (it is
 * embedded in the role), but the test mock service stores it in its own table so it can simulate
 * the retrieve-modify-write of the member list.
 *
 * <p>{@link #accountName} is the member account's name (EPPN); {@link #groupId} is the native id
 * of the account group (role).</p>
 */
public class JamfMembership {

  /**
   * Create the mock DB table used by the test mock service to simulate role membership.
   * @param ddlVersionBean ddl bean (unused but part of the createTable contract)
   * @param database the ddlutils database to add the table to
   */
  public static void createTableJamfMembership(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_jamf_membership";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_id", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "account_name", Types.VARCHAR, "256", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_jamf_mem_group_idx", false, "group_id");
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_jamf_mem_acct_idx", false, "account_name");
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_jamf_mem_grp_acct_idx", true, "group_id", "account_name");
    }

  }

  /** unique id for Hibernate (assigned via GrouperUuid.getUuid()) */
  private String id;

  /** native id of the account group (role) */
  private String groupId;

  /** member account name (EPPN) */
  private String accountName;

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

  public String getAccountName() {
    return accountName;
  }

  public void setAccountName(String accountName) {
    this.accountName = accountName;
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

}
