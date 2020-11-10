package edu.internet2.middleware.grouper.ddl;

import org.apache.commons.logging.Log;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDdl2_5_38 {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDdl2_5_38.class);
  
  /**
   * if building to this version at least
   */
  private static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V38.getVersion() <= buildingToVersion;

    return buildingToThisVersionAtLeast;
  }

  /**
   * if building to this version at least
   */
  static boolean buildingFromScratch(DdlVersionBean ddlVersionBean) {
    int buildingFromVersion = ddlVersionBean.getBuildingFromVersion();
    if (buildingFromVersion <= 0) {
      return true;
    }
    return false;
  }

  /**
   * if building to this version at least
   */
  @SuppressWarnings("unused")
  private static boolean buildingToPreviousVersion(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToPreviousVersion = GrouperDdl.V38.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  public static void adjustGrouperSyncMembershipIndex(Database database,
      DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    // if building from scratch its right now
    if(buildingFromScratch(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_5_38_adjustGrouperSyncMembershipIndex", true)) {
      return;
    }
    
    String grouperSyncMembership = "grouper_sync_membership";
    String grouperSyncMembershipIndex = "grouper_sync_mship_gr_idx";
    
    Table syncMembershipTable = GrouperDdlUtils.ddlutilsFindTable(database, grouperSyncMembership, true);
    Index index = GrouperDdlUtils.ddlutilsFindIndex(database, grouperSyncMembership, grouperSyncMembershipIndex);
    
    syncMembershipTable.removeIndex(index);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperSyncMembership, 
        "grouper_sync_mship_gr_idx", true, "grouper_sync_id", "grouper_sync_group_id", "grouper_sync_member_id");
    
  }
  
}
