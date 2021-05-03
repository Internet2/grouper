package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

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
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V35.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V35.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  static void addGrouperSyncLogColumns(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_38_addGrouperSyncLogColumns", true)) {
      return;
    }

    Table syncLogTable = GrouperDdlUtils.ddlutilsFindTable(database, "grouper_sync_log", true);
    
    if (GrouperDdlUtils.isPostgres()) {
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncLogTable, "description_clob", Types.VARCHAR, "10000000", false, false, null);
    }
    
    if (GrouperDdlUtils.isMysql()) {
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_sync_log ADD COLUMN description_clob mediumtext;\n");
    } 
    
    if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isHsql()) {
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncLogTable, "description_clob", Types.CLOB, "10000000", false, false, null);
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncLogTable, "description_bytes", Types.BIGINT, "12", false, false, null);
    
  }
  
  /**
   * 
   */
  static void addGrouperSyncLogComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_38_addGrouperSyncLogComments", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync", "last_full_sync_start", "start time of last successful full sync");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync", "last_full_metadata_sync_start", "start time of last successful full metadata sync");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_job", "last_sync_start", "start time of this job");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_log", "description_clob", "description for large data");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_log", "description_bytes", "size of description in bytes");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_log", "sync_timestamp_start", "start of sync operation for log");

//    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_group", "in_grouper", "T if exists in grouper and F is not.  blank if not sure");
//
//    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_group", "in_grouper_insert_or_exists", "T if inserted to grouper on the in_target_start date, or F if it existed then and not sure when inserted");
//
//    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_group", "in_grouper_start", "when this was put in grouper");
//
//    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_group", "in_grouper_end", "when this was taken out of grouper");
//
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_group", "last_group_sync_start", "start of last successful group sync");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_group", "last_group_metadata_sync_start", "start of last successful group metadata sync");

//    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_member", "in_grouper", "T if exists in grouper and F is not.  blank if not sure");
//
//    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_member", "in_grouper_insert_or_exists", "T if inserted to grouper on the in_target_start date, or F if it existed then and not sure when inserted");
//
//    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_member", "in_grouper_start", "when this was put in grouper");
//
//    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_member", "in_grouper_end", "when this was taken out of grouper");
//
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_member", "last_user_sync_start", "start of last successful user sync");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_member", "last_user_metadata_sync_start", "start of last successful user metadata sync");

//    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_membership", "in_grouper", "T if exists in grouper and F is not.  blank if not sure");
//
//    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_membership", "in_grouper_insert_or_exists", "T if inserted to grouper on the in_target_start date, or F if it existed then and not sure when inserted");
//
//    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_membership", "in_grouper_start", "when this was put in grouper");
//
//    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_membership", "in_grouper_end", "when this was taken out of grouper");
  }
  
  
  public static void addGrouperSyncStartColumns(Database database,
      DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    // if building from scratch its already got it
    if(buildingFromScratch(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_5_38_addGrouperSyncStartColumns", true)) {
      return;
    }

    {
      Table grouperSyncTable = GrouperDdlUtils.ddlutilsFindTable(database, "grouper_sync", true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncTable, "last_full_sync_start", 
          Types.TIMESTAMP, null, false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncTable, "last_full_metadata_sync_start", 
          Types.TIMESTAMP, null, false, false);
    }
    
    {
      Table grouperSyncJobTable = GrouperDdlUtils.ddlutilsFindTable(database, "grouper_sync_job", true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncJobTable, "last_sync_start", 
          Types.TIMESTAMP, null, false, false);
    }

    {
      Table grouperSyncGroupTable = GrouperDdlUtils.ddlutilsFindTable(database, "grouper_sync_group", true);
//      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "in_grouper", 
//          Types.VARCHAR, "1", false, false);
//      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "in_grouper_insert_or_exists", 
//          Types.VARCHAR, "1", false, false);
//      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "in_grouper_start", 
//          Types.TIMESTAMP, "1", false, false);
//      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "in_grouper_end", 
//          Types.TIMESTAMP, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "last_group_sync_start", 
          Types.TIMESTAMP, null, false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "last_group_metadata_sync_start", 
          Types.TIMESTAMP, null, false, false);
    }

    {
      Table grouperSyncMemberTable = GrouperDdlUtils.ddlutilsFindTable(database, "grouper_sync_member", true);
//      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncMemberTable, "in_grouper", 
//          Types.VARCHAR, "1", false, false);
//      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncMemberTable, "in_grouper_insert_or_exists", 
//          Types.VARCHAR, "1", false, false);
//      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncMemberTable, "in_grouper_start", 
//          Types.TIMESTAMP, "1", false, false);
//      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncMemberTable, "in_grouper_end", 
//          Types.TIMESTAMP, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncMemberTable, "last_user_sync_start", 
          Types.TIMESTAMP, null, false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncMemberTable, "last_user_metadata_sync_start", 
          Types.TIMESTAMP, null, false, false);
    }

//    {
//      Table grouperSyncMembershipTable = GrouperDdlUtils.ddlutilsFindTable(database, "grouper_sync_membership", true);
//      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncMembershipTable, "in_grouper", 
//          Types.VARCHAR, "1", false, false);
//      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncMembershipTable, "in_grouper_insert_or_exists", 
//          Types.VARCHAR, "1", false, false);
//      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncMembershipTable, "in_grouper_start", 
//          Types.TIMESTAMP, "1", false, false);
//      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncMembershipTable, "in_grouper_end", 
//          Types.TIMESTAMP, "1", false, false);
//    }
    {
      Table grouperSyncLogTable = GrouperDdlUtils.ddlutilsFindTable(database, "grouper_sync_log", true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncLogTable, "sync_timestamp_start", 
          Types.TIMESTAMP, null, false, false);
    }

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
