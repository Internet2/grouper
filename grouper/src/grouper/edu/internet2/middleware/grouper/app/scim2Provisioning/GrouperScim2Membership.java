package edu.internet2.middleware.grouper.app.scim2Provisioning;

import java.sql.Types;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperScim2Membership {

  public static void main(String[] args) {
        
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  public GrouperScim2Membership() {
  }

  public ProvisioningMembership toProvisioningMembership() {
    ProvisioningMembership targetMembership = new ProvisioningMembership();
    
    if (this.groupId != null) {
      targetMembership.setProvisioningGroupId(this.groupId);
    }
    
    if (this.userId != null) {
      targetMembership.setProvisioningEntityId(this.userId);
    }
    
    if (this.id != null) {
      targetMembership.setId(this.id);
    }
    
    return targetMembership;
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableScimMembership(DdlVersionBean ddlVersionBean, Database database) {
  
    final String tableName = "mock_scim_membership";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
          
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_id", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_id", Types.VARCHAR, "40", false, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_scim_mship_idx", true, "group_id", "user_id");
      
      GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, tableName, "mock_scim_mship_gid_fkey", "mock_scim_group", "group_id", "id");
      GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, tableName, "mock_scim_mship_uid_fkey", "mock_scim_user", "user_id", "id");

    }
  }

  private String userId;
  
  private String groupId;
  
  public String getUserId() {
    return userId;
  }

  
  public void setUserId(String userId) {
    this.userId = userId;
  }

  
  public String getGroupId() {
    return groupId;
  }

  
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  private String id;

  public String getId() {
    return id;
  }

  
  public void setId(String id) {
    this.id = id;
  }
  
  
  
}
