package edu.internet2.middleware.grouper.app.serviceLifecycle;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperGracePeriod {

  /**
   * 
   * @return the stem name
   */
  public static String gracePeriodStemName() {
    return GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":attribute:gracePeriod";
  }

  public static final String GROUPER_GRACE_PERIOD_MARKER_DEF = "grouperGracePeriodMarkerDef";
  
  public static final String GROUPER_GRACE_PERIOD_MARKER = "grouperGracePeriodMarker";

  public static final String GROUPER_GRACE_PERIOD_VALUE_DEF = "grouperGracePeriodValueDef";
  
  public static final String GROUPER_GRACE_PERIOD_ATTR_DAYS = "grouperGracePeriodDays";
  
  public static final String GROUPER_GRACE_PERIOD_ATTR_GROUP_NAME = "grouperGracePeriodGroupName";

  public static final String GROUPER_GRACE_PERIOD_LOADER_GROUP_NAME = "grouperGracePeriodLoader";
  
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

      // check the point in time against the grace period days, check that grace period is an integer and check that group name has a colon
      String databasePart = null;
      String regexPart = null;
      if (GrouperDdlUtils.isHsql()) {
        regexPart = " and REGEXP_MATCHES (gaaagv_gracePeriod.value_string, '^[0-9]+$') and REGEXP_MATCHES (gaaagv_groupName.value_string, '^.+:.+$') ";
        databasePart = " and gpmship.end_time > 1000*(unix_millis(current_timestamp) - (1000*60*60*24*cast(gaaagv_gracePeriod.value_string as int))) " + regexPart;
      } else if (GrouperDdlUtils.isOracle()) {
        regexPart = " and REGEXP_LIKE (gaaagv_gracePeriod.value_string, '^[0-9]+$') and REGEXP_LIKE (gaaagv_groupName.value_string, '^.+:.+$') ";
        databasePart = " and gpmship.end_time > (1000000 * (((sysdate - date '1970-01-01')*24*60*60)-(24*60*60*CAST( gaaagv_gracePeriod.value_string AS number )) ))" + regexPart;
      } else if (GrouperDdlUtils.isMysql()) {  
        regexPart = " and gaaagv_gracePeriod.value_string REGEXP '^[0-9]+$' and gaaagv_groupName.value_string REGEXP '^.+:.+$' ";
        databasePart = " and gpmship.end_time > ((1000000) * (UNIX_TIMESTAMP() - (60*60*24*CONVERT(gaaagv_gracePeriod.value_string,UNSIGNED INTEGER)))) " + regexPart;
      } else if (GrouperDdlUtils.isPostgres()) {
        regexPart = " and gaaagv_gracePeriod.value_string ~ '^[0-9]+$' and gaaagv_groupName.value_string ~ '^.+:.+$' ";
        databasePart = "and gpmship.end_time > cast(((1000000) * (extract(EPOCH from clock_timestamp()) - (60*60*24*(cast(gaaagv_gracePeriod.value_string as bigint))))) as bigint) " + regexPart;
      } else {
        LOG.error("Cant find database type");
        return;
      }
      query = "select distinct gaaagv_groupName.value_string group_name, gpm.subject_id, gpm.subject_source subject_source_id "
          + "from grouper_pit_memberships gpmship, grouper_pit_group_set gpgs, grouper_pit_members gpm, grouper_pit_groups gpg, grouper_pit_fields gpf, "
          + "grouper_aval_asn_asn_group_v gaaagv_gracePeriod, grouper_aval_asn_asn_group_v gaaagv_groupName "
          + "where gaaagv_gracePeriod.group_id = gaaagv_groupName.group_id "
          + "and gaaagv_gracePeriod.attribute_def_name_name2 = '" + gracePeriodStemName() + ":" + GROUPER_GRACE_PERIOD_ATTR_DAYS + "' "
          + "and gaaagv_groupName.attribute_def_name_name2 = '" + gracePeriodStemName() + ":" + GROUPER_GRACE_PERIOD_ATTR_GROUP_NAME + "' "
          + "and gpmship.MEMBER_ID = GPM.ID and GPM.subject_source != 'g:gsa' and gpgs.FIELD_ID = GPF.ID "
          + "and gpf.name = 'members' " + databasePart + " "
          + "and gaaagv_groupName.group_name = gpg.name "
          + "and gpg.id = gpgs.owner_id "
          + "and gpmship.owner_id = gpgs.member_id "
          + "AND gpmship.field_id = gpgs.member_field_id " 
          + "and not exists (select 1 from grouper_memberships mship2, grouper_group_set gs2 WHERE mship2.owner_id = gs2.member_id "
          + "AND mship2.field_id = gs2.member_field_id and mship2.member_id = gpm.source_id and gs2.field_id = gpf.source_id "
          + "and gs2.owner_id = gaaagv_gracePeriod.group_id and mship2.enabled = 'T' ) ";

      groupQuery = "select gaaagv_gracePeriod.group_id owner_group_id, gaaagv_gracePeriod.group_name owner_group_name, "
          + "gaaagv_gracePeriod.value_string grace_period_days, gaaagv_groupName.value_string group_name "
          + "from grouper_aval_asn_asn_group_v gaaagv_gracePeriod, grouper_aval_asn_asn_group_v gaaagv_groupName "
          + "where gaaagv_gracePeriod.group_id = gaaagv_groupName.group_id "
          + "and gaaagv_gracePeriod.attribute_def_name_name2 = '" + gracePeriodStemName() + ":" + GROUPER_GRACE_PERIOD_ATTR_DAYS + "' "
          + "and gaaagv_groupName.attribute_def_name_name2 = '" + gracePeriodStemName() + ":" + GROUPER_GRACE_PERIOD_ATTR_GROUP_NAME + "' " + regexPart;


    }
  }
  
  
  
  public static void setupGracePeriodLoaderJob(Group group) {
    
    String groupName = gracePeriodStemName() + ":" + GrouperGracePeriod.GROUPER_GRACE_PERIOD_LOADER_GROUP_NAME;

    boolean gracePeriodEnabled = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.gracePeriod.loaderJob.enable", true);

    GroupType grouperLoaderType = GroupTypeFinder.find("grouperLoader", true);
    boolean hasChange = false;
    if (gracePeriodEnabled) {
      
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
    
    if (hasChange) {
      GrouperLoaderType.scheduleLoads();
    }
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperGracePeriod.class);
  
}
