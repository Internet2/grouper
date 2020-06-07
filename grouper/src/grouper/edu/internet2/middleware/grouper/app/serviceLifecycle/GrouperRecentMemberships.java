package edu.internet2.middleware.grouper.app.serviceLifecycle;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperRecentMemberships {

  /**
   * 
   * @return the stem name
   */
  public static String recentMembershipsStemName() {
    return GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":attribute:recentMemberships";
  }

  public static final String GROUPER_RECENT_MEMBERSHIPS_MARKER_DEF = "grouperRecentMembershipsMarkerDef";
  
  public static final String GROUPER_RECENT_MEMBERSHIPS_MARKER = "grouperRecentMembershipsMarker";

  public static final String GROUPER_RECENT_MEMBERSHIPS_VALUE_DEF = "grouperRecentMembershipsValueDef";
  
  public static final String GROUPER_RECENT_MEMBERSHIPS_ATTR_DAYS = "grouperRecentMembershipsDays";
  
  public static final String GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_NAME = "grouperRecentMembershipsGroupName";

  public static final String GROUPER_RECENT_MEMBERSHIPS_ATTR_INCLUDE_CURRENT = "grouperRecentMembershipsIncludeCurrent";

  public static final String GROUPER_RECENT_MEMBERSHIPS_LOADER_GROUP_NAME = "grouperRecentMembershipsLoader";
  
  private static String groupQuery = null;
  
  private static String query = null;
  
  public static String query() {
    setupQueries();
    return query;
  }
  
  public static String groupQuery() {
    setupQueries();
    return groupQuery;
  }
  
  private static void setupQueries() {
    if (query == null) {

      // check the point in time against the recent memberships days, check that recent memberships is an integer and check that group name has a colon
      String databasePart = null;
      String regexPart = null;
      String minEndTimePart = null;
      
      // mship:    <---->
      // groupset:    <---->
      // if we are including active people (eligible), then allow both group set and membership with no end time
      databasePart = " and ((gaaagv_includeEligible.value_string = 'true' and gpmship.end_time is null and gpgs.end_time is null) "
          + "or ((gpmship.end_time > $$MIN_END_TIME$$ "
          + "and gpgs.start_time < gpmship.end_time AND (gpgs.end_time is null or gpgs.end_time > gpmship.end_time)) " + 
          " OR (gpgs.end_time > $$MIN_END_TIME$$"
          + " AND gpmship.start_time < gpgs.end_time and (gpmship.end_time is null or gpmship.end_time > gpgs.end_time))))"; 
      
      if (GrouperDdlUtils.isHsql()) {
        regexPart = " and REGEXP_MATCHES (gaaagv_recentMemberships.value_string, '^[0-9]+$') and REGEXP_MATCHES (gaaagv_groupName.value_string, '^.+:.+$') "
            + "and REGEXP_MATCHES (gaaagv_includeEligible.value_string, '^(true|false)$') ";
        minEndTimePart = "(1000*(unix_millis(current_timestamp) - (1000*60*60*24*cast(gaaagv_recentMemberships.value_string as int))))";
      } else if (GrouperDdlUtils.isOracle()) {
        regexPart = " and REGEXP_LIKE (gaaagv_recentMemberships.value_string, '^[0-9]+$') and REGEXP_LIKE (gaaagv_groupName.value_string, '^.+:.+$') "
            + "and REGEXP_LIKE (gaaagv_includeEligible.value_string, '^(true|false)$') ";
        minEndTimePart = "(1000000 * (((cast(current_timestamp at time zone 'UTC' as date) - date '1970-01-01')*24*60*60)-(24*60*60*CAST( gaaagv_recentMemberships.value_string AS number ))))";
      } else if (GrouperDdlUtils.isMysql()) {  
        regexPart = " and gaaagv_recentMemberships.value_string REGEXP '^[0-9]+$' and gaaagv_groupName.value_string REGEXP '^.+:.+$' "
            + "and gaaagv_includeEligible.value_string REGEXP '^(true|false)$' ";
        minEndTimePart = "(1000000 * (UNIX_TIMESTAMP() - (60*60*24*CONVERT(gaaagv_recentMemberships.value_string,UNSIGNED INTEGER))))";
      } else if (GrouperDdlUtils.isPostgres()) {
        regexPart = " and gaaagv_recentMemberships.value_string ~ '^[0-9]+$' and gaaagv_groupName.value_string ~ '^.+:.+$' "
            + "and gaaagv_includeEligible.value_string ~ '^(true|false)$' ";
        minEndTimePart = "cast((1000000 * (extract(EPOCH from clock_timestamp()) - (60*60*24*(cast(gaaagv_recentMemberships.value_string as bigint))))) as bigint)";
      } else {
        LOG.error("Cant find database type");
        return;
      }
      databasePart = StringUtils.replace(databasePart, "$$MIN_END_TIME$$", minEndTimePart) + regexPart;
      query = "select distinct gaaagv_groupName.value_string group_name, gpm.subject_id, gpm.subject_source subject_source_id "
          + "from grouper_pit_memberships gpmship, grouper_pit_group_set gpgs, grouper_pit_members gpm, grouper_pit_groups gpg, grouper_pit_fields gpf, "
          + "grouper_aval_asn_asn_group_v gaaagv_recentMemberships, grouper_aval_asn_asn_group_v gaaagv_groupName, grouper_aval_asn_asn_group_v gaaagv_includeEligible "
          + "where gaaagv_recentMemberships.attribute_assign_id1 = gaaagv_groupName.attribute_assign_id1 and gaaagv_recentMemberships.attribute_assign_id1 = gaaagv_includeEligible.attribute_assign_id1 "
          + "and gaaagv_recentMemberships.attribute_def_name_name2 = '" + recentMembershipsStemName() + ":" + GROUPER_RECENT_MEMBERSHIPS_ATTR_DAYS + "' "
          + "and gaaagv_groupName.attribute_def_name_name2 = '" + recentMembershipsStemName() + ":" + GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_NAME + "' "
          + "and gaaagv_includeEligible.attribute_def_name_name2 = '" + recentMembershipsStemName() + ":" + GROUPER_RECENT_MEMBERSHIPS_ATTR_INCLUDE_CURRENT + "' "
          + "and gpmship.MEMBER_ID = gpm.ID and gpm.subject_source != 'g:gsa' and gpgs.FIELD_ID = gpf.ID "
          + "and gpf.name = 'members' " + databasePart + " "
          + "and gaaagv_groupName.group_id = gpg.source_id "
          + "and gpg.id = gpgs.owner_id "
          + "and gpmship.owner_id = gpgs.member_id "
          + "AND gpmship.field_id = gpgs.member_field_id " 
          + "and (gaaagv_includeEligible.value_string = 'true' or not exists (select 1 from grouper_memberships mship2, grouper_group_set gs2 WHERE mship2.owner_id = gs2.member_id "
          + "AND mship2.field_id = gs2.member_field_id and mship2.member_id = gpm.source_id and gs2.field_id = gpf.source_id "
          + "and gs2.owner_id = gaaagv_recentMemberships.group_id and mship2.enabled = 'T' ) ) ";

      groupQuery = "select gaaagv_recentMemberships.group_id owner_group_id, gaaagv_recentMemberships.group_name owner_group_name, "
          + "gaaagv_recentMemberships.value_string recent_memberships_days, gaaagv_groupName.value_string group_name, gaaagv_includeEligible.value_string include_eligible "
          + "from grouper_aval_asn_asn_group_v gaaagv_recentMemberships, grouper_aval_asn_asn_group_v gaaagv_groupName, grouper_aval_asn_asn_group_v gaaagv_includeEligible "
          + "where gaaagv_recentMemberships.group_id = gaaagv_groupName.group_id and gaaagv_recentMemberships.group_id = gaaagv_includeEligible.group_id "
          + "and gaaagv_recentMemberships.attribute_def_name_name2 = '" + recentMembershipsStemName() + ":" + GROUPER_RECENT_MEMBERSHIPS_ATTR_DAYS + "' "
          + "and gaaagv_groupName.attribute_def_name_name2 = '" + recentMembershipsStemName() + ":" + GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_NAME + "' "
          + "and gaaagv_includeEligible.attribute_def_name_name2 = '" + recentMembershipsStemName() + ":" + GROUPER_RECENT_MEMBERSHIPS_ATTR_INCLUDE_CURRENT + "' "
          + regexPart;


    }
  }
  
  
  
  public static void setupRecentMembershipsLoaderJob(Group group) {
    
    boolean recentMembershipsEnabled = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.recentMemberships.loaderJob.enable", true);

    GroupType grouperLoaderType = GroupTypeFinder.find("grouperLoader", true);
    boolean hasChange = false;
    if (recentMembershipsEnabled) {
      
      // cant find database???
      if (query() == null) {
        return;
      }
      if (!group.hasType(grouperLoaderType)) {
        group.addType(grouperLoaderType);
      }
      if (!StringUtils.equals(group.getAttribute(GrouperLoader.GROUPER_LOADER_TYPE), "SQL_GROUP_LIST")) {
        group.setAttribute(GrouperLoader.GROUPER_LOADER_TYPE, "SQL_GROUP_LIST");
        hasChange = true;
      }
      if (!StringUtils.equals(group.getAttribute(GrouperLoader.GROUPER_LOADER_DB_NAME), "grouper")) {
        group.setAttribute(GrouperLoader.GROUPER_LOADER_DB_NAME, "grouper");
        hasChange = true;
      }
      if (!StringUtils.equals(group.getAttribute(GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE), "CRON")) {
        group.setAttribute(GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE, "CRON");
        hasChange = true;
      }
      if (!StringUtils.equals(group.getAttribute(GrouperLoader.GROUPER_LOADER_QUARTZ_CRON), "0 41 3 * * ?")) {
        group.setAttribute(GrouperLoader.GROUPER_LOADER_QUARTZ_CRON, "0 41 3 * * ?");
        hasChange = true;
      }
      if (!StringUtils.equals(group.getAttribute(GrouperLoader.GROUPER_LOADER_QUERY), query())) {
        group.setAttribute(GrouperLoader.GROUPER_LOADER_QUERY, query());
        hasChange = true;
      }
      if (!StringUtils.equals(group.getAttribute(GrouperLoader.GROUPER_LOADER_GROUP_QUERY), groupQuery())) {
        group.setAttribute(GrouperLoader.GROUPER_LOADER_GROUP_QUERY, groupQuery());
        hasChange = true;
      }
      
    } else {
      
      if (group.hasType(grouperLoaderType)) {
        group.deleteType(grouperLoaderType);
        hasChange = true;
      }
    }
    
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperRecentMemberships.class);
  
}
