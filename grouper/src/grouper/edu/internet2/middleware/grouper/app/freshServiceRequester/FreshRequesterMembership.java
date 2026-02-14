package edu.internet2.middleware.grouper.app.freshServiceRequester;

import java.sql.Types;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class FreshRequesterMembership {
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableFreshMembership(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_freshreq_membership";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {

      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_id", Types.BIGINT, "20", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_id", Types.BIGINT, "20", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.BIGINT, "20", true, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_freshreq_mship_gid_idx", false, "group_id");
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_freshreq_mship_uid_idx", false, "user_id");
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_freshreq_mship_uid_idx", true, "group_id", "user_id");
      
      GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, tableName, "mock_freshreq_mship_gid_fkey", "mock_freshreq_group", "group_id", "id");
      GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, tableName, "mock_freshreq_mship_uid_fkey", "mock_freshreq_user", "user_id", "id");
    }
    
  }
  
  private long userId;

  private long groupId;

  public long getUserId() {
    return userId;
  }


  public void setUserId(long userId) {
    this.userId = userId;
  }


  public long getGroupId() {
    return groupId;
  }

  public void setGroupId(long groupId) {
    this.groupId = groupId;
  }

  private long id;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }
  
  public ProvisioningMembership toProvisioningMembership() {
    ProvisioningMembership targetMembership = new ProvisioningMembership(false);

    targetMembership.setProvisioningGroupId(Long.toString(this.groupId));
    targetMembership.setProvisioningEntityId(Long.toString(this.userId));
    targetMembership.setId(Long.toString(this.id));

    return targetMembership;
  }

}
