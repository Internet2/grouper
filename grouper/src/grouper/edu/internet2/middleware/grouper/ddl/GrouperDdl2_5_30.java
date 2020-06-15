package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.commons.logging.Log;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDdl2_5_30 {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDdl2_5_30.class);
  
  /**
   * if building to this version at least
   */
  private static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V33.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V33.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }
  

  static void addSubjectResolutionColumns(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_30_addSubjectResolutionColumns", true)) {
      return;
    }

    Table memberTable = GrouperDdlUtils.ddlutilsFindTable(database, Member.TABLE_GROUPER_MEMBERS, true);
    
    
    if (buildingFromScratch(ddlVersionBean)) {
      
      //this is required if the member table is new    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(memberTable, Member.COLUMN_SUBJECT_RESOLUTION_DELETED, Types.VARCHAR, "1", false, true, "F");
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(memberTable, Member.COLUMN_SUBJECT_RESOLUTION_RESOLVABLE, Types.VARCHAR, "1", false, true, "T");
    } else {
      if (!GrouperDdlUtils.isPostgres()) {
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(memberTable, Member.COLUMN_SUBJECT_RESOLUTION_DELETED, Types.VARCHAR, "1", false, false, "F");
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(memberTable, Member.COLUMN_SUBJECT_RESOLUTION_RESOLVABLE, Types.VARCHAR, "1", false, false, "T");        
      }
    }

    // just do nothing if there is no upgrade.  i.e. the database already has this
    if (!buildingFromScratch(ddlVersionBean) && GrouperDdlUtils.isPostgres() && ddlVersionBean.getBuildingFromVersion() < GrouperDdl.V33.getVersion()) {
      
      // this will recreate the grouper_groups table in postgres on an existing installation if you dont do this
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_members ADD COLUMN subject_resolution_resolvable VARCHAR(1);\n");
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_members ADD COLUMN subject_resolution_deleted VARCHAR(1);\n");
      ddlVersionBean.getAdditionalScripts().append("CREATE INDEX member_resolvable_idx ON grouper_members (subject_resolution_resolvable);\n");
      ddlVersionBean.getAdditionalScripts().append("CREATE INDEX member_deleted_idx ON grouper_members (subject_resolution_deleted);\n");
      
    } else {
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, memberTable.getName(), "member_resolvable_idx", false, Member.COLUMN_SUBJECT_RESOLUTION_RESOLVABLE);       
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, memberTable.getName(), "member_deleted_idx", false, Member.COLUMN_SUBJECT_RESOLUTION_DELETED);       
    }
    
    if (!buildingFromScratch(ddlVersionBean)) {
      boolean needUpdate = false;
      
      try {
        int count = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_members");
        if (count > 0) {
          needUpdate = true;
        }
      } catch (Exception e) {
        needUpdate = false;
        LOG.info("Exception querying grouper_members", e);
        // group table doesnt exist?
      }
      
      if (needUpdate) {
        ddlVersionBean.getAdditionalScripts().append(
            "update grouper_members set subject_resolution_resolvable='T' where subject_resolution_resolvable is null;\n" +
            "update grouper_members set subject_resolution_deleted='F' where subject_resolution_deleted is null;\n" +
            "commit;\n");
      }
    }
    
    if (!buildingFromScratch(ddlVersionBean) && GrouperDdlUtils.isPostgres()) {
      ddlVersionBean.getAdditionalScripts().append(          
          "ALTER TABLE " + Member.TABLE_GROUPER_MEMBERS + " ALTER COLUMN " + Member.COLUMN_SUBJECT_RESOLUTION_RESOLVABLE + " SET NOT NULL;\n");
      ddlVersionBean.getAdditionalScripts().append(          
          "ALTER TABLE " + Member.TABLE_GROUPER_MEMBERS + " ALTER COLUMN " + Member.COLUMN_SUBJECT_RESOLUTION_RESOLVABLE + " SET DEFAULT 'T';\n");
      
      ddlVersionBean.getAdditionalScripts().append(          
          "ALTER TABLE " + Member.TABLE_GROUPER_MEMBERS + " ALTER COLUMN " + Member.COLUMN_SUBJECT_RESOLUTION_DELETED + " SET NOT NULL;\n");
      ddlVersionBean.getAdditionalScripts().append(          
          "ALTER TABLE " + Member.TABLE_GROUPER_MEMBERS + " ALTER COLUMN " + Member.COLUMN_SUBJECT_RESOLUTION_DELETED + " SET DEFAULT 'F';\n");
    }    
  }

  /**
   * 
   */
  static void addGrouperNowComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_30_addGrouperNowComments", true)) {
      return;
    }
  
    final String tableName = "grouper_time";
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, "Update the row with current time before joining to other tables (e.g. for recent memberships)");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "time_label", "should only need one row with value: now");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "the_utc_timestamp", "timestamp with time zone utc");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "this_tz_timestamp", "timestamp with this time zone (from java)");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "utc_millis_since_1970", "millis since 1970 utc");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "utc_micros_since_1970", "micros since 1970 utc");
  
    
  }

  /**
   * 
   */
  static void addGrouperNowTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_30_addGrouperNowTable", true)) {
      return;
    }
    final String tableName = "grouper_time";
  
    Table grouperNowTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperNowTable, "time_label", 
        Types.VARCHAR, "10", true, true);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperNowTable, "the_utc_timestamp", 
        Types.TIMESTAMP, null, false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperNowTable, "this_tz_timestamp", 
        Types.TIMESTAMP, null, false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperNowTable, "utc_millis_since_1970", 
        Types.BIGINT, "20", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperNowTable, "utc_micros_since_1970", 
        Types.BIGINT, "20", false, true);
  }

  /**
   * 
   */
  static void addGrouperCacheOverallTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_30_addGrouperCacheOverallTable", true)) {
      return;
    }
    final String tableName = "grouper_cache_overall";
  
    Table grouperCacheInstanceTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperCacheInstanceTable, "overall_cache", 
        Types.INTEGER, "1", true, true);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperCacheInstanceTable, "nanos_since_1970", 
        Types.BIGINT, "20", false, true);
  }


  /**
   * 
   */
  static void addGrouperCacheOverallComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_30_addGrouperCacheOverallComments", true)) {
      return;
    }
  
    final String tableName = "grouper_cache_overall";
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, "One row for the most time that any cache needs to be cleared");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "overall_cache", "One row with an integer of 0 only");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "nanos_since_1970", "nanos since 1970 that the most recent cache was cleared");
  
    
  }

  /**
   * 
   */
  static void addGrouperCacheInstanceTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_30_addGrouperCacheInstanceTable", true)) {
      return;
    }
    final String tableName = "grouper_cache_instance";
  
    Table grouperCacheInstanceTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperCacheInstanceTable, "cache_name", 
        Types.VARCHAR, "400", true, true);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperCacheInstanceTable, "nanos_since_1970", 
        Types.BIGINT, "20", false, true);
  }

  /**
   * 
   */
  static void addGrouperCacheInstanceComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_30_addGrouperCacheInstanceComments", true)) {
      return;
    }
  
    final String tableName = "grouper_cache_instance";
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, "Row for each cache instance and the time that it needs to be cleared");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "cache_name", "cache name, if there are two underscores, split and the first part is cache, and second part is instance");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "nanos_since_1970", "time the cache was last changed");  
    
  }

  static void createViewRecentMemLoadV(DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_createViewRecentMemLoadV", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_recent_mships_load_v", 
        "Contains one row for each recent membership in a group for the loader",
        GrouperUtil.toSet("group_name", 
            "subject_source_id", 
            "subject_id"),
        GrouperUtil.toSet("group_name: group name of the loaded group from recent memberships", 
            "subject_source_id: subject source of subject in recent membership", 
            "subject_id: subject id of subject in recent membership"),
        "select grmc.group_name_to as group_name, "
        + "gpmglv.subject_source as subject_source_id, "
        + "gpmglv.subject_id as subject_id "
        + "from grouper_recent_mships_conf grmc,  "
        + "grouper_pit_mship_group_lw_v gpmglv, "
        + "grouper_time gt, "
        + "grouper_members gm " 
        + "where gm.id = gpmglv.member_id " 
        + "and gm.subject_resolution_deleted = 'F' "
        + "and gt.time_label = 'now' "
        + "and (gpmglv.group_id = grmc.group_uuid_from or gpmglv.group_name = grmc.group_name_from) "
        + "and gpmglv.subject_source != 'g:gsa' "
        + "and gpmglv.field_name = 'members' "
        + "and ((grmc.include_eligible = 'T' and gpmglv.the_active = 'T') "
        + "  or (gpmglv.the_end_time >= gt.utc_micros_since_1970 - grmc.recent_micros))");
  }

  static void createViewRecentMembershipsV(DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_createViewRecentMembershipsV", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_recent_mships_conf_v", 
        "Contains one row for each recent membership configured on a group",
        GrouperUtil.toSet("group_name_from", 
            "group_uuid_from", 
            "recent_micros", 
            "group_uuid_to", 
            "group_name_to",
            "include_eligible"),
        GrouperUtil.toSet("group_name_from: group name of the group where the recent memberships are sourced from", 
            "group_uuid_from: group uuid of the group where the recent memberships are sourced from", 
            "recent_micros: number of microseconds of recent memberships", 
            "group_uuid_to: uuid of the group which has the destination for the recent memberships", 
            "group_name_to: name of the group which has the destination for the recent memberships",
            "include_eligible: T or F if eligible subjects are included"),
            "select distinct " + 
            "  gg.name group_name_from," + 
            "  gaaagv_groupUuidFrom.value_string group_uuid_from," + 
            "  gaaagv_recentMembershipsMicros.value_integer recent_micros," + 
            "  gaaagv_groupUuidFrom.group_id group_uuid_to," + 
            "  gaaagv_groupUuidFrom.group_name group_name_to, " + 
            "  gaaagv_includeEligible.value_string include_eligible " +
            "  from " + 
            "  grouper_aval_asn_asn_group_v gaaagv_recentMembershipsMicros," + 
            "  grouper_aval_asn_asn_group_v gaaagv_groupUuidFrom," + 
            "  grouper_aval_asn_asn_group_v gaaagv_includeEligible," + 
            "  grouper_groups gg" + 
            "  where gaaagv_recentMembershipsMicros.attribute_assign_id1 = gaaagv_groupUuidFrom.attribute_assign_id1" + 
            "  and gaaagv_recentMembershipsMicros.attribute_assign_id1 = gaaagv_includeEligible.attribute_assign_id1" + 
            "  and gaaagv_recentMembershipsMicros.attribute_def_name_name2 = 'etc:attribute:recentMemberships:grouperRecentMembershipsMicros'" + 
            "  and gaaagv_groupUuidFrom.attribute_def_name_name2 = 'etc:attribute:recentMemberships:grouperRecentMembershipsGroupUuidFrom'" + 
            "  and gaaagv_includeEligible.attribute_def_name_name2 = 'etc:attribute:recentMemberships:grouperRecentMembershipsIncludeCurrent'" + 
            "  and gaaagv_recentMembershipsMicros.value_integer > 0" + 
            "  and gaaagv_recentMembershipsMicros.value_integer is not null" + 
            "  and gaaagv_groupUuidFrom.value_string is not null" + 
            "  and gaaagv_includeEligible.value_string is not null" + 
            "  and (gaaagv_includeEligible.value_string = 'T' or gaaagv_includeEligible.value_string = 'F')" + 
            "  and gg.id = gaaagv_groupUuidFrom.value_string ");
  }

  /**
   * cache index instances
   * @param ddlVersionBean 
   * @param database
   */
  static void addGrouperCacheInstanceIndexes(Database database, DdlVersionBean ddlVersionBean) {
    if (ddlVersionBean.didWeDoThis("v2_5_addGrouperCacheInstanceIndexes", true)) {
      return;
    }
    
    {
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_cache_instance", 
          "grouper_cache_inst_cache_idx", false, "nanos_since_1970");
    }
  }

  /**
   * 
   */
  static void addGrouperRecentMembershipsComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_5_30_addGrouperRecentMembershipsComments", true)) {
      return;
    }
  
    final String tableName = "grouper_recent_mships_conf";
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, "Contains one row for each recent membership configured on a group, sourced from grouper_recent_mships_conf_v");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "group_uuid_to", "group_uuid_to: uuid of the group which has the destination for the recent memberships");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "group_name_to", "group_name_to: name of the group which has the destination for the recent memberships");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "group_name_from", "group_name_from: group name of the group where the recent memberships are sourced from");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "group_uuid_from", "group_uuid_from: group uuid of the group where the recent memberships are sourced from");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "recent_micros", "recent_micros: number of microseconds of recent memberships");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "include_eligible", "include_eligible: T to include people still in group, F if not");
    
  }

  /**
   * cache index instances
   * @param ddlVersionBean 
   * @param database
   */
  static void addGrouperRecentMembershipsIndexes(Database database, DdlVersionBean ddlVersionBean) {
    if (ddlVersionBean.didWeDoThis("v2_5_addGrouperRecentMembershipsIndexes", true)) {
      return;
    }
    
    {
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_recent_mships_conf", 
          "grouper_recent_mships_idfr_idx", false, "group_uuid_from");
    }
  }

  /**
   * 
   */
  static void addGrouperRecentMembershipsTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_5_30_addGrouperRecentMembershipsTable", true)) {
      return;
    }
    final String tableName = "grouper_recent_mships_conf";
  
    Table grouperCacheInstanceTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperCacheInstanceTable, "group_uuid_to", 
        Types.VARCHAR, "40", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperCacheInstanceTable, "group_name_to", 
        Types.VARCHAR, "1024", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperCacheInstanceTable, "group_uuid_from", 
        Types.VARCHAR, "40", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperCacheInstanceTable, "group_name_from", 
        Types.VARCHAR, "1024", false, true);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperCacheInstanceTable, "recent_micros", 
        Types.BIGINT, "20", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperCacheInstanceTable, "include_eligible", 
        Types.VARCHAR, "1", false, true);

    
  }

  static void addPitMembershipsLwV(DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_5_30_addPitMembershipsLwV", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_pit_memberships_lw_v", 
        "Grouper_pit_memberships_lw_v holds one record for each immediate, composite and effective membership or privilege in the system that currently exists or has existed in the past for members to groups or stems (for privileges).  Note this joins with dates and overlaps so it only contains rows that are applicable and calculates the real start and end time and if active",
        GrouperUtil.toSet("ID", 
            "MEMBERSHIP_ID", 
            "MEMBERSHIP_SOURCE_ID", 
            "GROUP_SET_ID", 
            "MEMBER_ID", 
            "FIELD_ID", 
            "MEMBERSHIP_FIELD_ID", 
            "OWNER_ID", 
            "OWNER_ATTR_DEF_ID", 
            "OWNER_GROUP_ID", 
            "OWNER_STEM_ID", 
            "GROUP_SET_ACTIVE", 
            "GROUP_SET_START_TIME", 
            "GROUP_SET_END_TIME", 
            "MEMBERSHIP_ACTIVE", 
            "MEMBERSHIP_START_TIME", 
            "MEMBERSHIP_END_TIME", 
            "DEPTH", 
            "GROUP_SET_PARENT_ID",
            "THE_START_TIME",
            "THE_END_TIME",
            "THE_ACTIVE"),
        GrouperUtil.toSet("ID: id of this membership", 
            "MEMBERSHIP_ID: id of the immediate (or composite) membership that causes this membership", 
            "MEMBERSHIP_SOURCE_ID: id of the actual (non-pit) immediate (or composite) membership that causes this membership", 
            "GROUP_SET_ID: id of the group set that causes this membership", 
            "MEMBER_ID: member id", 
            "FIELD_ID: field id", 
            "MEMBERSHIP_FIELD_ID: field id of the immediate (or composite) membership that causes this membership", 
            "OWNER_ID: owner id", 
            "OWNER_ATTR_DEF_ID: owner attribute def id if applicable", 
            "OWNER_GROUP_ID: owner group id if applicable", 
            "OWNER_STEM_ID: owner stem id if applicable", 
            "GROUP_SET_ACTIVE: whether the group set is active", 
            "GROUP_SET_START_TIME: start time of the group set", 
            "GROUP_SET_END_TIME: end time of the group set", 
            "MEMBERSHIP_ACTIVE: whether the immediate (or composite) membership is active", 
            "MEMBERSHIP_START_TIME: start time of the immediate (or composite) membership", 
            "MEMBERSHIP_END_TIME: end time of the immediate (or composite) membership", 
            "DEPTH: depth of this membership", 
            "GROUP_SET_PARENT_ID: parent group set",
            "THE_START_TIME: the real start time of this membership",
            "THE_END_TIME: the real end time of this membership",
            "THE_ACTIVE: if this memberships is still active"),
            "select "
                + GrouperDdlUtils.sqlConcatenation("gpmship.id", "gpgs.id", Membership.membershipIdSeparator) + " as membership_id, "
                + "gpmship.id as immediate_membership_id, "
                + "gpmship.source_id as membership_source_id, "
                + "gpgs.id as group_set_id, "
                + "gpmship.member_id, "
                + "gpgs.field_id, "
                + "gpmship.field_id, "
                + "gpgs.owner_id, "
                + "gpgs.owner_attr_def_id, "
                + "gpgs.owner_group_id, "
                + "gpgs.owner_stem_id, " 
                + "gpgs.active, "
                + "gpgs.start_time, "
                + "gpgs.end_time, "
                + "gpmship.active, "
                + "gpmship.start_time, "
                + "gpmship.end_time, "
                + "gpgs.depth, " 
                + "gpgs.parent_id as group_set_parent_id, "
                + " (case when gpgs.start_time >= gpmship.start_time then gpgs.start_time else gpmship.start_time end) as the_start_time," 
                + " (case when gpgs.end_time is null then gpmship.end_time when gpmship.end_time is null then gpgs.end_time when gpgs.end_time <= gpmship.end_time then gpgs.end_time else gpmship.end_time end) as the_end_time," 
                + " (case when gpgs.end_time is null and gpmship.end_time is null then 'T' else 'F'  end) as the_active "
                + "from grouper_pit_memberships gpmship, grouper_pit_group_set gpgs "
                + "where gpmship.owner_id = gpgs.member_id and gpmship.field_id = gpgs.member_field_id and ("
                + "(gpmship.start_time >= gpgs.start_time and (gpgs.end_time >= gpmship.start_time or gpgs.end_time is null))" 
                + " or (gpgs.start_time >= gpmship.start_time and (gpmship.end_time >= gpgs.start_time or gpmship.end_time is null)))" 
                );

  }

  static void addPitMshipsGroupLwV(DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_5_30_addPitMshipsGroupLwV", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_pit_mship_group_lw_v", 
        "grouper_pit_mship_group_lw_v holds one record for each immediate, composite and effective membership or privilege in the system that currently exists or has existed in the past for members to groups or stems (for privileges).  Note this joins with dates and overlaps so it only contains rows that are applicable and calculates the real start and end time and if active.  Holds the group information for memberships or privileges",
        GrouperUtil.toSet(
            "GROUP_NAME", 
            "FIELD_NAME", 
            "SUBJECT_SOURCE",
            "SUBJECT_ID",
            "MEMBER_ID",
            "FIELD_ID",
            "GROUP_ID",
            "THE_START_TIME",
            "THE_END_TIME",
            "THE_ACTIVE",
            "MEMBERSHIP_ID",
            "IMM_MEMBERSHIP_ID"),
        GrouperUtil.toSet(
            "GROUP_NAME: group name is extension and ancestor folder extensions separated by colons", 
            "FIELD_NAME: members, admins, readers, etc", 
            "SUBJECT_SOURCE: subject source id",
            "SUBJECT_ID: subject id in the source",
            "MEMBER_ID: uuid in the grouper_members table (note, could be different than real one if deleted and/or recreated)",
            "FIELD_ID: uuid in the grouper_fields table (note, could be different than real one if deleted and/or recreated)",
            "GROUP_ID: uuid of the grouper group (note, could be different than real one if deleted and/or recreated)",
            "THE_START_TIME: micros since 1970 UTC that the membership started",
            "THE_END_TIME: micros since 1970 UTC that the membership ended or null if still active",
            "THE_ACTIVE: T or F for if this membership is still active",
            "MEMBERSHIP_ID: membership id and colon and group set id which is the effective membership id.  might not exist in grouper if from past",
            "IMM_MEMBERSHIP_ID: membership id for this immediate membership.  might not exist in grouper if from past"),
        "select "
            + "gpg.name as group_name, "
            + "gpf.name as field_name, "
            + "gpm.subject_source, "
            + "gpm.subject_id, "
            + "gpm.source_id as member_id, "
            + "gpf.source_id as field_id, "
            + "gpg.source_id as group_id, "
            + "(case when gpgs.start_time >= gpmship.start_time then gpgs.start_time else gpmship.start_time end) as the_start_time, "
            + "(case when gpgs.end_time is null then gpmship.end_time when gpmship.end_time is null then gpgs.end_time when gpgs.end_time <= gpmship.end_time then gpgs.end_time else gpmship.end_time end) as the_end_time, "
            + "(case when gpgs.end_time is null and gpmship.end_time is null then 'T' else 'F' end) as the_active, "
            + GrouperDdlUtils.sqlConcatenation("gpmship.source_id", "gpgs.source_id", Membership.membershipIdSeparator) + " as membership_id, "
            + "gpmship.source_id as imm_membership_id "
            + "from grouper_pit_memberships gpmship, "
            + "grouper_pit_group_set gpgs, "
            + "grouper_pit_members gpm, "
            + "grouper_pit_groups gpg, "
            + "grouper_pit_fields gpf "
            + "where gpmship.owner_id = gpgs.member_id "
            + "and gpmship.field_id = gpgs.member_field_id "
            + "and gpmship.member_id = gpm.ID "
            + "and gpg.id = gpgs.owner_id "
            + "and gpgs.FIELD_ID = gpf.ID "
            + "and "
            + "( "
            + "   ( "
            + "      gpmship.start_time >= gpgs.start_time "
            + "      and (gpmship.end_time >= gpmship.start_time or gpgs.end_time is null) "
            + "   ) "
            + "   or "
            + "   ( "
            + "      gpgs.start_time >= gpmship.start_time "
            + "      and (gpmship.end_time >= gpgs.start_time or gpmship.end_time is null) "
            + "   ) "
            + ") ");
  }

  static void addPitMshipsStemLwV(DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_5_30_addPitMshipsStemLwV", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_pit_mship_stem_lw_v", 
        "grouper_pit_mship_stem_lw_v holds one record for each immediate, composite and effective stem privilege in the system that currently exists or has existed in the past for members to stems (for privileges).  Note this joins with dates and overlaps so it only contains rows that are applicable and calculates the real start and end time and if active",
        GrouperUtil.toSet(
            "STEM_NAME", 
            "FIELD_NAME", 
            "SUBJECT_SOURCE",
            "SUBJECT_ID",
            "MEMBER_ID",
            "FIELD_ID",
            "STEM_ID",
            "THE_START_TIME",
            "THE_END_TIME",
            "THE_ACTIVE",
            "MEMBERSHIP_ID",
            "IMM_MEMBERSHIP_ID"),
        GrouperUtil.toSet(
            "STEM_NAME: stem name is extension and ancestor folder extensions separated by colons", 
            "FIELD_NAME: admins, creators, etc", 
            "SUBJECT_SOURCE: subject source id",
            "SUBJECT_ID: subject id in the source",
            "MEMBER_ID: uuid in the grouper_members table (note, could be different than real one if deleted and/or recreated)",
            "FIELD_ID: uuid in the grouper_fields table (note, could be different than real one if deleted and/or recreated)",
            "STEM_ID: uuid of the grouper stem (note, could be different than real one if deleted and/or recreated)",
            "THE_START_TIME: micros since 1970 UTC that the membership started",
            "THE_END_TIME: micros since 1970 UTC that the membership ended or null if still active",
            "THE_ACTIVE: T or F for if this membership is still active",
            "MEMBERSHIP_ID: membership id and colon and group set id which is the effective membership id.  might not exist in grouper if from past",
            "IMM_MEMBERSHIP_ID: membership id for this immediate membership.  might not exist in grouper if from past"),
        "select "
        + "gps.name as stem_name, "
        + "gpf.name as field_name, "
        + "gpm.subject_source, "
        + "gpm.subject_id, "
        + "gpm.source_id as member_id, "
        + "gpf.source_id as field_id, "
        + "gps.source_id as stem_id, "
        + "(case when gpgs.start_time >= gpmship.start_time then gpgs.start_time else gpmship.start_time end) as the_start_time, "
        + "(case when gpgs.end_time is null then gpmship.end_time when gpmship.end_time is null then gpgs.end_time when gpgs.end_time <= gpmship.end_time then gpgs.end_time else gpmship.end_time end) as the_end_time, "
        + "(case when gpgs.end_time is null and gpmship.end_time is null then 'T' else 'F' end) as the_active, "
        + GrouperDdlUtils.sqlConcatenation("gpmship.source_id", "gpgs.source_id", Membership.membershipIdSeparator) + " as membership_id, "
        + "gpmship.source_id as imm_membership_id "
        + "from grouper_pit_memberships gpmship, "
        + "grouper_pit_group_set gpgs, "
        + "grouper_pit_members gpm, "
        + "grouper_pit_stems gps, "
        + "grouper_pit_fields gpf "
        + "where gpmship.owner_id = gpgs.member_id "
        + "and gpmship.field_id = gpgs.member_field_id "
        + "and gpmship.member_id = gpm.ID "
        + "and gps.id = gpgs.owner_id "
        + "and gpgs.FIELD_ID = gpf.ID "
        + "and "
        + "( "
        + "   ( "
        + "      gpmship.start_time >= gpgs.start_time "
        + "      and (gpmship.end_time >= gpmship.start_time or gpgs.end_time is null) "
        + "   ) "
        + "  or "
        + "  ( "
        + "     gpgs.start_time >= gpmship.start_time "
        + "      and (gpmship.end_time >= gpgs.start_time or gpmship.end_time is null) "
        + "   ) "
        + ")" );
  }

  

  static void addPitMshipsAttrDefLwV(DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_5_30_addPitMshipsAttrDefLwV", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_pit_mship_attr_lw_v", 
        "grouper_pit_mship_attr_lw_v holds one record for each immediate, composite and effective atribute def privilege in the system that currently exists or has existed in the past for members to attribute def (for privileges).  Note this joins with dates and overlaps so it only contains rows that are applicable and calculates the real start and end time and if active",
        GrouperUtil.toSet(
            "NAME_OF_ATTRIBUTE_DEF", 
            "FIELD_NAME", 
            "SUBJECT_SOURCE",
            "SUBJECT_ID",
            "MEMBER_ID",
            "FIELD_ID",
            "ATTRIBUTE_DEF_ID",
            "THE_START_TIME",
            "THE_END_TIME",
            "THE_ACTIVE",
            "MEMBERSHIP_ID",
            "IMM_MEMBERSHIP_ID"),
        GrouperUtil.toSet(
            "NAME_OF_ATTRIBUTE_DEF: name of attribute def is extension and ancestor folder extensions separated by colons", 
            "FIELD_NAME: admins, creators, etc", 
            "SUBJECT_SOURCE: subject source id",
            "SUBJECT_ID: subject id in the source",
            "MEMBER_ID: uuid in the grouper_members table (note, could be different than real one if deleted and/or recreated)",
            "FIELD_ID: uuid in the grouper_fields table (note, could be different than real one if deleted and/or recreated)",
            "ATTRIBUTE_DEF_ID: uuid of the grouper attribute def (note, could be different than real one if deleted and/or recreated)",
            "THE_START_TIME: micros since 1970 UTC that the membership started",
            "THE_END_TIME: micros since 1970 UTC that the membership ended or null if still active",
            "THE_ACTIVE: T or F for if this membership is still active",
            "MEMBERSHIP_ID: membership id and colon and group set id which is the effective membership id.  might not exist in grouper if from past",
            "IMM_MEMBERSHIP_ID: membership id for this immediate membership.  might not exist in grouper if from past"),
        "select "
        + "gpa.name as name_of_attribute_def, "
        + "gpf.name as field_name, "
        + "gpm.subject_source, "
        + "gpm.subject_id, "
        + "gpm.source_id as member_id, "
        + "gpf.source_id as field_id, "
        + "gpa.source_id as attribute_def_id, "
        + "(case when gpgs.start_time >= gpmship.start_time then gpgs.start_time else gpmship.start_time end) as the_start_time, "
        + "(case when gpgs.end_time is null then gpmship.end_time when gpmship.end_time is null then gpgs.end_time when gpgs.end_time <= gpmship.end_time then gpgs.end_time else gpmship.end_time end) as the_end_time, "
        + "(case when gpgs.end_time is null and gpmship.end_time is null then 'T' else 'F' end) as the_active, "
        + GrouperDdlUtils.sqlConcatenation("gpmship.source_id", "gpgs.source_id", Membership.membershipIdSeparator) + " as membership_id, "
        + "gpmship.source_id as imm_membership_id "
        + "from grouper_pit_memberships gpmship, "
        + "grouper_pit_group_set gpgs, "
        + "grouper_pit_members gpm, "
        + "grouper_pit_attribute_def gpa, "
        + "grouper_pit_fields gpf "
        + "where gpmship.owner_id = gpgs.member_id "
        + "and gpmship.field_id = gpgs.member_field_id "
        + "and gpmship.member_id = gpm.ID "
        + "and gpa.id = gpgs.owner_id "
        + "and gpgs.FIELD_ID = gpf.ID "
        + "and "
        + "( "
        + "   ( "
        + "      gpmship.start_time >= gpgs.start_time "
        + "      and (gpmship.end_time >= gpmship.start_time or gpgs.end_time is null) "
        + "   ) "
        + "   or "
        + "   ( "
        + "      gpgs.start_time >= gpmship.start_time "
        + "     and (gpmship.end_time >= gpgs.start_time or gpmship.end_time is null) "
        + "   ) "
        + ") ");

  }



}