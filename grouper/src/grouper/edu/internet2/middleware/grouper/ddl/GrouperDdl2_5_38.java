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

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_group", "last_group_sync_start", "start of last successful group sync");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_group", "last_group_metadata_sync_start", "start of last successful group metadata sync");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_member", "last_user_sync_start", "start of last successful user sync");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_member", "last_user_metadata_sync_start", "start of last successful user metadata sync");

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
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "last_group_sync_start", 
          Types.TIMESTAMP, null, false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "last_group_metadata_sync_start", 
          Types.TIMESTAMP, null, false, false);
    }

    {
      Table grouperSyncMemberTable = GrouperDdlUtils.ddlutilsFindTable(database, "grouper_sync_member", true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncMemberTable, "last_user_sync_start", 
          Types.TIMESTAMP, null, false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncMemberTable, "last_user_metadata_sync_start", 
          Types.TIMESTAMP, null, false, false);
    }

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

  public static void addSyncMembershipView(Database database,
      DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_38_addSyncMembershipView", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_sync_membership_v", 
        "Memberships for provisioning joined with the group, member, and sync tables",
        GrouperUtil.toSet(
            "G_GROUP_NAME",
            "G_GROUP_ID_INDEX",
            "U_SOURCE_ID",
            "U_SUBJECT_ID",
            "U_SUBJECT_IDENTIFIER",
            "M_IN_TARGET",
            "M_ID",
            "M_IN_TARGET_INSERT_OR_EXISTS",
            "M_IN_TARGET_START",
            "M_IN_TARGET_END",
            "M_LAST_UPDATED",
            "M_MEMBERSHIP_ID",
            "M_MEMBERSHIP_ID2",
            "M_METADATA_UPDATED",
            "M_ERROR_MESSAGE",
            "M_ERROR_TIMESTAMP",
            "S_ID",
            "S_SYNC_ENGINE",
            "S_PROVISIONER_NAME",
            "U_ID",
            "U_MEMBER_ID",
            "U_IN_TARGET",
            "U_IN_TARGET_INSERT_OR_EXISTS",
            "U_IN_TARGET_START",
            "U_IN_TARGET_END",
            "U_PROVISIONABLE",
            "U_PROVISIONABLE_START",
            "U_PROVISIONABLE_END",
            "U_LAST_UPDATED",
            "U_LAST_USER_SYNC_START",
            "U_LAST_USER_SYNC",
            "U_LAST_USER_META_SYNC_START",
            "U_LAST_USER_METADATA_SYNC",
            "U_MEMBER_FROM_ID2",
            "U_MEMBER_FROM_ID3",
            "U_MEMBER_TO_ID2",
            "U_MEMBER_TO_ID3",
            "U_METADATA_UPDATED",
            "U_LAST_TIME_WORK_WAS_DONE",
            "U_ERROR_MESSAGE",
            "U_ERROR_TIMESTAMP",
            "G_ID",
            "G_GROUP_ID",
            "G_PROVISIONABLE",
            "G_IN_TARGET",
            "G_IN_TARGET_INSERT_OR_EXISTS",
            "G_IN_TARGET_START",
            "G_IN_TARGET_END",
            "G_PROVISIONABLE_START",
            "G_PROVISIONABLE_END",
            "G_LAST_UPDATED",
            "G_LAST_GROUP_SYNC_START",
            "G_LAST_GROUP_SYNC",
            "G_LAST_GROUP_META_SYNC_START",
            "G_LAST_GROUP_METADATA_SYNC",
            "G_GROUP_FROM_ID2",
            "G_GROUP_FROM_ID3",
            "G_GROUP_TO_ID2",
            "G_GROUP_TO_ID3",
            "G_METADATA_UPDATED",
            "G_ERROR_MESSAGE",
            "G_ERROR_TIMESTAMP",
            "G_LAST_TIME_WORK_WAS_DONE"),
        GrouperUtil.toSet(
            "G_GROUP_NAME: grouper group system name",
            "G_GROUP_ID_INDEX: grouper group id index",
            "U_SOURCE_ID: subject source id",
            "U_SUBJECT_ID: subject id",
            "U_SUBJECT_IDENTIFIER: subject identifier0",
            "M_IN_TARGET: T/F if provisioned to target",
            "M_ID: sync membership id",
            "M_IN_TARGET_INSERT_OR_EXISTS: T/F if it was inserted into target or already existed",
            "M_IN_TARGET_START: timestamp was inserted or detected to be in target",
            "M_IN_TARGET_END: timestamp was removed from target or detected not there",
            "M_LAST_UPDATED: when sync membership last updated",
            "M_MEMBERSHIP_ID: link membership id",
            "M_MEMBERSHIP_ID2: link membership id2",
            "M_METADATA_UPDATED: when metadata e.g. links was last updated",
            "M_ERROR_MESSAGE: error message when last operation occurred unless a success happened afterward",
            "M_ERROR_TIMESTAMP: timestamp last error occurred unless a success happened afterward",
            "S_ID: sync id overall",
            "S_SYNC_ENGINE: sync engine",
            "S_PROVISIONER_NAME: name of provisioner",
            "U_ID: sync member id",
            "U_MEMBER_ID: grouper member uuid for subject",
            "U_IN_TARGET: T/F if entity is in target",
            "U_IN_TARGET_INSERT_OR_EXISTS: T/F if grouper inserted the entity or if it already existed",
            "U_IN_TARGET_START: when this entity started being in target or detected there",
            "U_IN_TARGET_END: when this entity stopped being in target or detected not there",
            "U_PROVISIONABLE: T/F if the entity is provisionable",
            "U_PROVISIONABLE_START: when this entity started being provisionable",
            "U_PROVISIONABLE_END: when this entity stopped being provisionable",
            "U_LAST_UPDATED: when the sync member was last updated",
            "U_LAST_USER_SYNC_START: when the user was last overall sync started",
            "U_LAST_USER_SYNC: when the user was last overall synced",
            "U_LAST_USER_META_SYNC_START: when the metadata was sync started",
            "U_LAST_USER_METADATA_SYNC: when the metadata was last synced",
            "U_MEMBER_FROM_ID2: link data from id2",
            "U_MEMBER_FROM_ID3: link data from id3",
            "U_MEMBER_TO_ID2: link data to id2",
            "U_MEMBER_TO_ID3: link data to id3",
            "U_METADATA_UPDATED: when metadata was last updated for entity",
            "U_LAST_TIME_WORK_WAS_DONE: time last work was done on user object",
            "U_ERROR_MESSAGE: error message last time work was done on user unless a success happened afterward",
            "U_ERROR_TIMESTAMP: timestamp the last error occurred unless a success happened afterwards",
            "G_ID: sync group id",
            "G_GROUP_ID: grouper group id",
            "G_PROVISIONABLE: T/F if group is provisionable",
            "G_IN_TARGET: T/F if the group is in target",
            "G_IN_TARGET_INSERT_OR_EXISTS: T/F if the group was inserted by grouper or already existed in target",
            "G_IN_TARGET_START: when the group was detected to be in the target",
            "G_IN_TARGET_END: when the group was detected to not be in the target anymore",
            "G_PROVISIONABLE_START: when this group started being provisionable",
            "G_PROVISIONABLE_END: when this group stopped being provisionable",
            "G_LAST_UPDATED: when the sync group was last updated",
            "G_LAST_GROUP_SYNC_START: when the group was sync started",
            "G_LAST_GROUP_SYNC: when the group was last synced",
            "G_LAST_GROUP_META_SYNC_START: when the metadata sync started",
            "G_LAST_GROUP_METADATA_SYNC: when the metadata was last synced",
            "G_GROUP_FROM_ID2: link data from id2",
            "G_GROUP_FROM_ID3: link data from id3",
            "G_GROUP_TO_ID2: link data to id2",
            "G_GROUP_TO_ID3: link data to id3",
            "G_METADATA_UPDATED: when metadata e.g. link data was last updated",
            "G_ERROR_MESSAGE: if there is an error message last time work was done it is here",
            "G_ERROR_TIMESTAMP: timestamp if last time work was done there was an error",
            "G_LAST_TIME_WORK_WAS_DONE: timestamp of last time work was done on group"),
            "select G.GROUP_NAME as G_GROUP_NAME, G.GROUP_ID_INDEX as G_GROUP_ID_INDEX, U.SOURCE_ID as U_SOURCE_ID, U.SUBJECT_ID as U_SUBJECT_ID, U.SUBJECT_IDENTIFIER as U_SUBJECT_IDENTIFIER, "
            + "M.IN_TARGET as M_IN_TARGET, M.ID as M_ID, M.IN_TARGET_INSERT_OR_EXISTS as M_IN_TARGET_INSERT_OR_EXISTS, "
            + "M.IN_TARGET_START as M_IN_TARGET_START, M.IN_TARGET_END as M_IN_TARGET_END, M.LAST_UPDATED as M_LAST_UPDATED, M.MEMBERSHIP_ID as M_MEMBERSHIP_ID, "
            + "M.MEMBERSHIP_ID2 as M_MEMBERSHIP_ID2, M.METADATA_UPDATED as M_METADATA_UPDATED, M.ERROR_MESSAGE as M_ERROR_MESSAGE, M.ERROR_TIMESTAMP as M_ERROR_TIMESTAMP, "
            + "S.ID as S_ID, S.SYNC_ENGINE as S_SYNC_ENGINE, S.PROVISIONER_NAME as S_PROVISIONER_NAME, U.ID as U_ID, "
            + "U.MEMBER_ID as U_MEMBER_ID, U.IN_TARGET as U_IN_TARGET, "
            + "U.IN_TARGET_INSERT_OR_EXISTS as U_IN_TARGET_INSERT_OR_EXISTS, U.IN_TARGET_START as U_IN_TARGET_START, U.IN_TARGET_END as U_IN_TARGET_END, "
            + "U.PROVISIONABLE as U_PROVISIONABLE, U.PROVISIONABLE_START as U_PROVISIONABLE_START, U.PROVISIONABLE_END as U_PROVISIONABLE_END, U.LAST_UPDATED as U_LAST_UPDATED, "
            + "U.LAST_USER_SYNC_START as U_LAST_USER_SYNC_START, U.LAST_USER_SYNC as U_LAST_USER_SYNC, U.LAST_USER_METADATA_SYNC_START as U_LAST_USER_META_SYNC_START, U.LAST_USER_METADATA_SYNC as U_LAST_USER_METADATA_SYNC, U.MEMBER_FROM_ID2 as U_MEMBER_FROM_ID2, U.MEMBER_FROM_ID3 as U_MEMBER_FROM_ID3, "
            + "U.MEMBER_TO_ID2 as U_MEMBER_TO_ID2, U.MEMBER_TO_ID3 as U_MEMBER_TO_ID3, U.METADATA_UPDATED as U_METADATA_UPDATED, U.LAST_TIME_WORK_WAS_DONE as U_LAST_TIME_WORK_WAS_DONE, "
            + "U.ERROR_MESSAGE as U_ERROR_MESSAGE, U.ERROR_TIMESTAMP as U_ERROR_TIMESTAMP, G.ID as G_ID, G.GROUP_ID as G_GROUP_ID, "
            + "G.PROVISIONABLE as G_PROVISIONABLE, G.IN_TARGET as G_IN_TARGET, "
            + "G.IN_TARGET_INSERT_OR_EXISTS as G_IN_TARGET_INSERT_OR_EXISTS, G.IN_TARGET_START as G_IN_TARGET_START, G.IN_TARGET_END as G_IN_TARGET_END, "
            + "G.PROVISIONABLE_START as G_PROVISIONABLE_START, G.PROVISIONABLE_END as G_PROVISIONABLE_END, G.LAST_UPDATED as G_LAST_UPDATED, "
            + "G.LAST_GROUP_SYNC_START as G_LAST_GROUP_SYNC_START, G.LAST_GROUP_SYNC as G_LAST_GROUP_SYNC, G.LAST_GROUP_METADATA_SYNC_START as G_LAST_GROUP_META_SYNC_START, G.LAST_GROUP_METADATA_SYNC as G_LAST_GROUP_METADATA_SYNC, G.GROUP_FROM_ID2 as G_GROUP_FROM_ID2, "
            + "G.GROUP_FROM_ID3 as G_GROUP_FROM_ID3, G.GROUP_TO_ID2 as G_GROUP_TO_ID2, G.GROUP_TO_ID3 as G_GROUP_TO_ID3, G.METADATA_UPDATED as G_METADATA_UPDATED, "
            + "G.ERROR_MESSAGE as G_ERROR_MESSAGE, G.ERROR_TIMESTAMP as G_ERROR_TIMESTAMP, G.LAST_TIME_WORK_WAS_DONE as G_LAST_TIME_WORK_WAS_DONE  "
            + "from grouper_sync_membership m, grouper_sync_member u, grouper_sync_group g, "
            + "grouper_sync s where m.grouper_sync_id = s.id and u.grouper_sync_id = s.id and g.grouper_sync_id = s.id and m.grouper_sync_group_id = g.id and m.grouper_sync_member_id = u.id"
                );

  }
  
}
