package edu.internet2.middleware.grouper.app.boxProvisioner;

import java.sql.Types;

import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperBoxMembership {

  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableBoxMembership(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_box_membership";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {

      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_id", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_id", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_box_mship_gid_idx", false, "group_id");
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_box_mship_uid_idx", false, "user_id");
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_box_mship_uid_idx", true, "group_id", "user_id");
      
      GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, tableName, "mock_box_mship_gid_fkey", "mock_box_group", "group_id", "id");
      GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, tableName, "mock_box_mship_uid_fkey", "mock_box_user", "user_id", "id");
    }
    
  }

  
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }


  private String id;
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  private String groupId;
  
  private String userId;
  
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
}
