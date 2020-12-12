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
            "g_group_name",
            "g_group_id_index",
            "u_source_id",
            "u_subject_id",
            "u_subject_identifier",
            "m_in_target",
            "m_id",
            "m_in_target_insert_or_exists",
            "m_in_target_start",
            "m_in_target_end",
            "m_last_updated",
            "m_membership_id",
            "m_membership_id2",
            "m_metadata_updated",
            "m_error_message",
            "m_error_timestamp",
            "s_id",
            "s_sync_engine",
            "s_provisioner_name",
            "u_id",
            "u_member_id",
            "u_in_target",
            "u_in_target_insert_or_exists",
            "u_in_target_start",
            "u_in_target_end",
            "u_provisionable",
            "u_provisionable_start",
            "u_provisionable_end",
            "u_last_updated",
            "u_last_user_sync_start",
            "u_last_user_sync",
            "u_last_user_meta_sync_start",
            "u_last_user_metadata_sync",
            "u_member_from_id2",
            "u_member_from_id3",
            "u_member_to_id2",
            "u_member_to_id3",
            "u_metadata_updated",
            "u_last_time_work_was_done",
            "u_error_message",
            "u_error_timestamp",
            "g_id",
            "g_group_id",
            "g_provisionable",
            "g_in_target",
            "g_in_target_insert_or_exists",
            "g_in_target_start",
            "g_in_target_end",
            "g_provisionable_start",
            "g_provisionable_end",
            "g_last_updated",
            "g_last_group_sync_start",
            "g_last_group_sync",
            "g_last_group_meta_sync_start",
            "g_last_group_metadata_sync",
            "g_group_from_id2",
            "g_group_from_id3",
            "g_group_to_id2",
            "g_group_to_id3",
            "g_metadata_updated",
            "g_error_message",
            "g_error_timestamp",
            "g_last_time_work_was_done"),
        GrouperUtil.toSet(
            "g_group_name: grouper group system name",
            "g_group_id_index: grouper group id index",
            "u_source_id: subject source id",
            "u_subject_id: subject id",
            "u_subject_identifier: subject identifier0",
            "m_in_target: t/f if provisioned to target",
            "m_id: sync membership id",
            "m_in_target_insert_or_exists: t/f if it was inserted into target or already existed",
            "m_in_target_start: timestamp was inserted or detected to be in target",
            "m_in_target_end: timestamp was removed from target or detected not there",
            "m_last_updated: when sync membership last updated",
            "m_membership_id: link membership id",
            "m_membership_id2: link membership id2",
            "m_metadata_updated: when metadata e.g. links was last updated",
            "m_error_message: error message when last operation occurred unless a success happened afterward",
            "m_error_timestamp: timestamp last error occurred unless a success happened afterward",
            "s_id: sync id overall",
            "s_sync_engine: sync engine",
            "s_provisioner_name: name of provisioner",
            "u_id: sync member id",
            "u_member_id: grouper member uuid for subject",
            "u_in_target: t/f if entity is in target",
            "u_in_target_insert_or_exists: t/f if grouper inserted the entity or if it already existed",
            "u_in_target_start: when this entity started being in target or detected there",
            "u_in_target_end: when this entity stopped being in target or detected not there",
            "u_provisionable: t/f if the entity is provisionable",
            "u_provisionable_start: when this entity started being provisionable",
            "u_provisionable_end: when this entity stopped being provisionable",
            "u_last_updated: when the sync member was last updated",
            "u_last_user_sync_start: when the user was last overall sync started",
            "u_last_user_sync: when the user was last overall synced",
            "u_last_user_meta_sync_start: when the metadata was sync started",
            "u_last_user_metadata_sync: when the metadata was last synced",
            "u_member_from_id2: link data from id2",
            "u_member_from_id3: link data from id3",
            "u_member_to_id2: link data to id2",
            "u_member_to_id3: link data to id3",
            "u_metadata_updated: when metadata was last updated for entity",
            "u_last_time_work_was_done: time last work was done on user object",
            "u_error_message: error message last time work was done on user unless a success happened afterward",
            "u_error_timestamp: timestamp the last error occurred unless a success happened afterwards",
            "g_id: sync group id",
            "g_group_id: grouper group id",
            "g_provisionable: t/f if group is provisionable",
            "g_in_target: t/f if the group is in target",
            "g_in_target_insert_or_exists: t/f if the group was inserted by grouper or already existed in target",
            "g_in_target_start: when the group was detected to be in the target",
            "g_in_target_end: when the group was detected to not be in the target anymore",
            "g_provisionable_start: when this group started being provisionable",
            "g_provisionable_end: when this group stopped being provisionable",
            "g_last_updated: when the sync group was last updated",
            "g_last_group_sync_start: when the group was sync started",
            "g_last_group_sync: when the group was last synced",
            "g_last_group_meta_sync_start: when the metadata sync started",
            "g_last_group_metadata_sync: when the metadata was last synced",
            "g_group_from_id2: link data from id2",
            "g_group_from_id3: link data from id3",
            "g_group_to_id2: link data to id2",
            "g_group_to_id3: link data to id3",
            "g_metadata_updated: when metadata e.g. link data was last updated",
            "g_error_message: if there is an error message last time work was done it is here",
            "g_error_timestamp: timestamp if last time work was done there was an error",
            "g_last_time_work_was_done: timestamp of last time work was done on group"),
            "select g.group_name as g_group_name, g.group_id_index as g_group_id_index, u.source_id as u_source_id, u.subject_id as u_subject_id, u.subject_identifier as u_subject_identifier, "
            + "m.in_target as m_in_target, m.id as m_id, m.in_target_insert_or_exists as m_in_target_insert_or_exists, "
            + "m.in_target_start as m_in_target_start, m.in_target_end as m_in_target_end, m.last_updated as m_last_updated, m.membership_id as m_membership_id, "
            + "m.membership_id2 as m_membership_id2, m.metadata_updated as m_metadata_updated, m.error_message as m_error_message, m.error_timestamp as m_error_timestamp, "
            + "s.id as s_id, s.sync_engine as s_sync_engine, s.provisioner_name as s_provisioner_name, u.id as u_id, "
            + "u.member_id as u_member_id, u.in_target as u_in_target, "
            + "u.in_target_insert_or_exists as u_in_target_insert_or_exists, u.in_target_start as u_in_target_start, u.in_target_end as u_in_target_end, "
            + "u.provisionable as u_provisionable, u.provisionable_start as u_provisionable_start, u.provisionable_end as u_provisionable_end, u.last_updated as u_last_updated, "
            + "u.last_user_sync_start as u_last_user_sync_start, u.last_user_sync as u_last_user_sync, u.last_user_metadata_sync_start as u_last_user_meta_sync_start, u.last_user_metadata_sync as u_last_user_metadata_sync, u.member_from_id2 as u_member_from_id2, u.member_from_id3 as u_member_from_id3, "
            + "u.member_to_id2 as u_member_to_id2, u.member_to_id3 as u_member_to_id3, u.metadata_updated as u_metadata_updated, u.last_time_work_was_done as u_last_time_work_was_done, "
            + "u.error_message as u_error_message, u.error_timestamp as u_error_timestamp, g.id as g_id, g.group_id as g_group_id, "
            + "g.provisionable as g_provisionable, g.in_target as g_in_target, "
            + "g.in_target_insert_or_exists as g_in_target_insert_or_exists, g.in_target_start as g_in_target_start, g.in_target_end as g_in_target_end, "
            + "g.provisionable_start as g_provisionable_start, g.provisionable_end as g_provisionable_end, g.last_updated as g_last_updated, "
            + "g.last_group_sync_start as g_last_group_sync_start, g.last_group_sync as g_last_group_sync, g.last_group_metadata_sync_start as g_last_group_meta_sync_start, g.last_group_metadata_sync as g_last_group_metadata_sync, g.group_from_id2 as g_group_from_id2, "
            + "g.group_from_id3 as g_group_from_id3, g.group_to_id2 as g_group_to_id2, g.group_to_id3 as g_group_to_id3, g.metadata_updated as g_metadata_updated, "
            + "g.error_message as g_error_message, g.error_timestamp as g_error_timestamp, g.last_time_work_was_done as g_last_time_work_was_done  "
            + "from grouper_sync_membership m, grouper_sync_member u, grouper_sync_group g, "
            + "grouper_sync s where m.grouper_sync_id = s.id and u.grouper_sync_id = s.id and g.grouper_sync_id = s.id and m.grouper_sync_group_id = g.id and m.grouper_sync_member_id = u.id"
                );

  }
  
}
