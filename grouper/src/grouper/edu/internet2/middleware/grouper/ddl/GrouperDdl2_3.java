package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;
import java.util.Arrays;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.messaging.GrouperMessageHibernate;
import edu.internet2.middleware.grouper.pit.PITField;
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDdl2_3 {

  /**
   * if building to this version at least
   */
  private static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V30.getVersion() <= buildingToVersion;

    return buildingToThisVersionAtLeast;
  }

  /**
   * if building to this version at least
   */
  private static boolean buildingFromScratch(DdlVersionBean ddlVersionBean) {
    int buildingFromVersion = ddlVersionBean.getBuildingFromVersion();
    if (buildingFromVersion <= 0) {
      return true;
    }
    return false;
  }

  /**
   * if building to this version at least
   */
  private static boolean buildingToPreviousVersion(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToPreviousVersion = GrouperDdl.V30.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  static void addMembersTableIdentifier0Column(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("addMembersTableIndifier0Column", true)) {
      return;
    }
    
    Table membersTable = GrouperDdlUtils.ddlutilsFindTable(database, Member.TABLE_GROUPER_MEMBERS, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(membersTable, Member.COLUMN_SUBJECT_IDENTIFIER0, Types.VARCHAR, "255", false, false);
    
  }
  
  static void addMembersTableIdentifier0Comment(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("addMembersTableIndifier0Comment", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        Member.TABLE_GROUPER_MEMBERS,  Member.COLUMN_SUBJECT_IDENTIFIER0, 
          "subject identifier of the subject");

    
  }

  /**
   * Add messaging foreign key
   * @param ddlVersionBean 
   * @param database
   */
  static void addMessagingForeignKey(DdlVersionBean ddlVersionBean, Database database) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("addMessagingForeignKey", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, GrouperMessageHibernate.TABLE_GROUPER_MESSAGE, 
        "fk_message_from_member_id", Member.TABLE_GROUPER_MEMBERS, GrouperMessageHibernate.COLUMN_FROM_MEMBER_ID, "id");
  }
  
  
  static void addPitMembersTableIdentifier0Column(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("addPitMembersTableIndifier0Column", true)) {
      return;
    }
    
    Table pitMembersTable = GrouperDdlUtils.ddlutilsFindTable(database, PITMember.TABLE_GROUPER_PIT_MEMBERS, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(pitMembersTable, PITMember.COLUMN_SUBJECT_IDENTIFIER0, Types.VARCHAR, "255", false, false);
    
  }
  
  
  static void addPitMembersTableIdentifier0Comment(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("addPitMembersTableIdentifier0Comment", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        PITMember.TABLE_GROUPER_PIT_MEMBERS, 
        PITMember.COLUMN_SUBJECT_IDENTIFIER0, 
          "subject identifier of the subject");
    
  }

  
  static void convertStemAdminPrivilege(DdlVersionBean ddlVersionBean, Database database) {

    if (buildingFromScratch(ddlVersionBean)) {
      return;
    }
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("convertStemAdminPrivilege", true)) {
      return;
    }
    
    // stem privilege changing to stemAdmin
    if (GrouperDdlUtils.assertTablesThere(ddlVersionBean, false, false, Field.TABLE_GROUPER_FIELDS, true)) {
      int count = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_fields where name='stemmers'");
      if (count > 0) {
        ddlVersionBean.getAdditionalScripts().append(
          "update grouper_fields set read_privilege='stemAdmin' where read_privilege='stem';\n" +
          "update grouper_fields set write_privilege='stemAdmin' where write_privilege='stem';\n" +
          "update grouper_fields set name='stemAdmins' where name='stemmers';\n" +
          "commit;\n");
      }
    }
    
  }

  static void convertPitStemAdminPrivilege(DdlVersionBean ddlVersionBean, Database database) {

    if (buildingFromScratch(ddlVersionBean)) {
      return;
    }
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("convertPitStemAdminPrivilege", true)) {
      return;
    }
    
    // stem privilege changing to stemAdmin
    if (GrouperDdlUtils.assertTablesThere(ddlVersionBean, false, false, Field.TABLE_GROUPER_FIELDS, true)) {
      if (GrouperDdlUtils.assertTablesThere(ddlVersionBean, false, false, PITField.TABLE_GROUPER_PIT_FIELDS, true)) {
        int count = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_pit_fields where name='stemmers'");
        if (count > 0) {
          ddlVersionBean.getAdditionalScripts().append(
            "update grouper_pit_fields set name='stemAdmins' where name='stemmers';\n" +
            "commit;\n");
        }
      }
    }
    
  }

  /**
   * Add messaging indexes
   * @param ddlVersionBean 
   * @param database
   */
  static void addMessagingIndexes(DdlVersionBean ddlVersionBean, Database database) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("addMessagingIndexes", true)) {
      return;
    }

    {
      Table messageTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
          GrouperMessageHibernate.TABLE_GROUPER_MESSAGE);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, messageTable.getName(), 
          "grpmessage_sent_time_idx", false, GrouperMessageHibernate.COLUMN_SENT_TIME_MICROS);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, messageTable.getName(), 
          "grpmessage_state_idx", false, GrouperMessageHibernate.COLUMN_STATE);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, messageTable.getName(), 
          "grpmessage_queue_name_idx", false, GrouperMessageHibernate.COLUMN_QUEUE_NAME);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, messageTable.getName(), 
          "grpmessage_from_mem_id_idx", false, GrouperMessageHibernate.COLUMN_FROM_MEMBER_ID);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, messageTable.getName(), 
          "grpmessage_attempt_exp_idx", false, GrouperMessageHibernate.COLUMN_ATTEMPT_TIME_EXPIRES_MILLIS);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, messageTable.getName(), 
          "grpmessage_query_idx", true, GrouperMessageHibernate.COLUMN_QUEUE_NAME, 
          GrouperMessageHibernate.COLUMN_STATE, GrouperMessageHibernate.COLUMN_SENT_TIME_MICROS, GrouperMessageHibernate.COLUMN_ID);
      
    }
  }

  /**
   * Add messaging tables
   * @param ddlVersionBean 
   * @param database
   */
  static void addMessagingTables(DdlVersionBean ddlVersionBean, Database database) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("addMessagingTables", true)) {
      return;
    }

    {
      Table messageTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
          GrouperMessageHibernate.TABLE_GROUPER_MESSAGE);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(messageTable, GrouperMessageHibernate.COLUMN_ID, 
          Types.VARCHAR, "40", true, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(messageTable, GrouperMessageHibernate.COLUMN_SENT_TIME_MICROS,
          Types.BIGINT, "20", false, true);
  
      //sent to receiver but not yet confirmed
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(messageTable, GrouperMessageHibernate.COLUMN_GET_ATTEMPT_TIME_MILLIS,
          Types.BIGINT, "20", false, true);
  
      //count of get attempts
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(messageTable, GrouperMessageHibernate.COLUMN_GET_ATTEMPT_COUNT,
          Types.BIGINT, "20", false, true);
      
      //IN_QUEUE, GET_ATTEMPTED, PROCESSED
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(messageTable, GrouperMessageHibernate.COLUMN_STATE,
          Types.VARCHAR, "20", false, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(messageTable, GrouperMessageHibernate.COLUMN_GET_TIME_MILLIS,
          Types.BIGINT, "20", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(messageTable, GrouperMessageHibernate.COLUMN_FROM_MEMBER_ID,
          Types.VARCHAR, "40", false, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(messageTable, GrouperMessageHibernate.COLUMN_QUEUE_NAME,
          Types.VARCHAR, "100", false, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(messageTable, GrouperMessageHibernate.COLUMN_MESSAGE_BODY,
          Types.VARCHAR, "4000", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(messageTable, GrouperMessageHibernate.COLUMN_HIBERNATE_VERSION_NUMBER, 
          Types.BIGINT, null, false, true);
  
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(messageTable, GrouperMessageHibernate.COLUMN_ATTEMPT_TIME_EXPIRES_MILLIS, 
          Types.BIGINT, null, false, false);
    }
  }

  /**
   * Add messaging comments
   * @param ddlVersionBean 
   * @param database
   */
  static void addMessagingComments(DdlVersionBean ddlVersionBean, Database database) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("addMessagingTables", true)) {
      return;
    }
    {
      GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,
          GrouperMessageHibernate.TABLE_GROUPER_MESSAGE,
          "If using the default internal messaging with Grouper, this is the table that holds the messages and state of messages");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperMessageHibernate.TABLE_GROUPER_MESSAGE,
          GrouperMessageHibernate.COLUMN_FROM_MEMBER_ID, "member id of user who sent the message");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperMessageHibernate.TABLE_GROUPER_MESSAGE,
          GrouperMessageHibernate.COLUMN_GET_ATTEMPT_COUNT, "how many times this message has been attempted to be retrieved");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperMessageHibernate.TABLE_GROUPER_MESSAGE,
          GrouperMessageHibernate.COLUMN_GET_ATTEMPT_TIME_MILLIS, "milliseconds since 1970 that the message was attempted to be received");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperMessageHibernate.TABLE_GROUPER_MESSAGE,
          GrouperMessageHibernate.COLUMN_GET_TIME_MILLIS, "millis since 1970 that this message was successfully received");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperMessageHibernate.TABLE_GROUPER_MESSAGE,
          GrouperMessageHibernate.COLUMN_ATTEMPT_TIME_EXPIRES_MILLIS, "millis since 1970 that this message attempt expires if not sent successfully");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperMessageHibernate.TABLE_GROUPER_MESSAGE,
          GrouperMessageHibernate.COLUMN_HIBERNATE_VERSION_NUMBER, "hibernate version, optimistic locking so multiple processes dont update the same record at the same time");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperMessageHibernate.TABLE_GROUPER_MESSAGE,
          GrouperMessageHibernate.COLUMN_ID, "db uuid for this row");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperMessageHibernate.TABLE_GROUPER_MESSAGE,
          GrouperMessageHibernate.COLUMN_MESSAGE_BODY, "message body");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperMessageHibernate.TABLE_GROUPER_MESSAGE,
          GrouperMessageHibernate.COLUMN_QUEUE_NAME, "queue name for the message");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperMessageHibernate.TABLE_GROUPER_MESSAGE,
          GrouperMessageHibernate.COLUMN_SENT_TIME_MICROS, "microseconds since 1970 this message was sent (note this is probably unique, but not necessarily)");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperMessageHibernate.TABLE_GROUPER_MESSAGE,
          GrouperMessageHibernate.COLUMN_STATE, "state of this message: IN_QUEUE, GET_ATTEMPTED, PROCESSED");
    }  
  }

  
  /**
   * Add quartz indexes
   * @param ddlVersionBean
   * @param database
   */
  static void addQuartzIndexes(DdlVersionBean ddlVersionBean, Database database) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("addQuartzIndexes", true)) {
      return;
    }

    {
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_JOB_DETAILS", 
          "idx_qrtz_j_req_recovery", false, "sched_name", "requests_recovery");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_JOB_DETAILS", 
          "idx_qrtz_j_grp", false, "sched_name", "job_group");
    }
    
    {
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_TRIGGERS", 
          "idx_qrtz_t_j", false, "sched_name", "job_name", "job_group");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_TRIGGERS", 
          "idx_qrtz_t_jg", false, "sched_name", "job_group");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_TRIGGERS", 
          "idx_qrtz_t_c", false, "sched_name", "calendar_name");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_TRIGGERS", 
          "idx_qrtz_t_g", false, "sched_name", "trigger_group");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_TRIGGERS", 
          "idx_qrtz_t_state", false, "sched_name", "trigger_state");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_TRIGGERS", 
          "idx_qrtz_t_n_state", false, "sched_name", "trigger_name", "trigger_group", "trigger_state");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_TRIGGERS", 
          "idx_qrtz_t_n_g_state", false, "sched_name", "trigger_group", "trigger_state");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_TRIGGERS", 
          "idx_qrtz_t_next_fire_time", false, "sched_name", "next_fire_time");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_TRIGGERS", 
          "idx_qrtz_t_nft_st", false, "sched_name", "trigger_state", "next_fire_time");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_TRIGGERS", 
          "idx_qrtz_t_nft_misfire", false, "sched_name", "misfire_instr", "next_fire_time");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_TRIGGERS", 
          "idx_qrtz_t_nft_st_misfire", false, "sched_name", "misfire_instr", "next_fire_time", "trigger_state");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_TRIGGERS", 
          "idx_qrtz_t_nft_st_misfire_grp", false, "sched_name", "misfire_instr", "next_fire_time", "trigger_group", "trigger_state");
    }
    
    {
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_FIRED_TRIGGERS", 
          "idx_qrtz_ft_trig_inst_name", false, "sched_name", "instance_name");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_FIRED_TRIGGERS", 
          "idx_qrtz_ft_inst_job_req_rcvry", false, "sched_name", "instance_name", "requests_recovery");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_FIRED_TRIGGERS", 
          "idx_qrtz_ft_j_g", false, "sched_name", "job_name", "job_group");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_FIRED_TRIGGERS", 
          "idx_qrtz_ft_jg", false, "sched_name", "job_group");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_FIRED_TRIGGERS", 
          "idx_qrtz_ft_t_g", false, "sched_name", "trigger_name", "trigger_group");
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_QZ_FIRED_TRIGGERS", 
          "idx_qrtz_ft_tg", false, "sched_name", "trigger_group");
    }
  }
  
  static void dropViewGrouperAvalAsnEfmshipV(DdlVersionBean ddlVersionBean) {

    if (buildingFromScratch(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("dropViewGrouperAvalAsnEfmshipV", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_aval_asn_efmship_v", false);
  }

  /**
   * Add quartz tables
   * @param ddlVersionBean
   * @param database
   */
  static void createViewGrouperAvalAsnEfmshipV(DdlVersionBean ddlVersionBean, Database database, boolean fromAllViews) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    // its dependent on another view...
    if (buildingFromScratch(ddlVersionBean) && !fromAllViews) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("createViewGrouperAvalAsnEfmshipV", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_aval_asn_efmship_v", 
        "grouper_aval_asn_efmship_v: attribute assigned to an effective membership and values (multiple rows if multiple values, no rows if no values)",
        GrouperUtil.toSet("group_name",
          "subject_source_id",
          "subject_id",
          "action",
          "attribute_def_name_name",
          "value_string",
          "value_integer",
          "value_floating",
          "value_member_id",
          "group_display_name",
          "attribute_def_name_disp_name",
          "name_of_attribute_def",
          "attribute_assign_notes",
          "list_name",
          "attribute_assign_delegatable",
          "enabled",
          "enabled_time",
          "disabled_time",
          "group_id",
          "attribute_assign_id",
          "attribute_def_name_id",
          "attribute_def_id",
          "member_id",
          "action_id",
          "attribute_assign_value_id"
        ),
        GrouperUtil.toSet("group_name: name of group assigned the attribute",
            "subject_source_id: source id of the subject being assigned",
            "subject_id: subject id of the subject being assigned",
            "action: the action associated with the attribute assignment (default is assign)",
            "attribute_def_name_name: name of the attribute definition name which is assigned to the group",
            "value_string: if this is a string attributeDef, then this is the string",
            "value_integer: if this is an integer attributeDef, then this is the integer",
            "value_floating: if this is a floating attributeDef, then this is the value",
            "value_member_id: if this is a memberId attributeDef, then this is the value",
            "group_display_name: display name of the group assigned an attribute",
            "attribute_def_name_disp_name: display name of the attribute definition name assigned to the attribute",
            "name_of_attribute_def: name of the attribute definition associated with the attribute definition name assigned to the group",
            "attribute_assign_notes: notes related to the attribute assignment",
            "list_name: name of the membership list for this effective membership",
            "attribute_assign_delegatable: if this assignment is delegatable or grantable: TRUE, FALSE, GRANT",
            "enabled: if this assignment is enabled: T, F",
            "enabled_time: the time (seconds since 1970) that this assignment will be enabled",
            "disabled_time: the time (seconds since 1970) that this assignment will be disabled",
            "group_id: group id of the group assigned the attribute",
            "attribute_assign_id: id of the attribute assignment",
            "attribute_def_name_id: id of the attribute definition name",
            "attribute_def_id: id of the attribute definition",
            "member_id: id of the member assigned the attribute",
            "action_id: attribute assign action id",
            "attribute_assign_value_id: the id of the value"
        ),
        "select distinct gg.name as group_name, "
        + "gm.subject_source as subject_source_id, "
        + "gm.subject_id, "
        + "gaaa.name as action, "
        + "gadn.name as attribute_def_name_name, "
        + " gaav.value_string AS value_string, "          
        + " gaav.value_integer AS value_integer, "
        + " gaav.value_floating AS value_floating, "
        + " gaav.value_member_id AS value_member_id, "
        + "gg.display_name as group_display_name, "
        + "gadn.display_name as attribute_def_name_disp_name, "
        + "gad.name as name_of_attribute_def, "
        + "gaa.notes as attribute_assign_notes, "
        + "gf.name as list_name, "
        + "gaa.attribute_assign_delegatable, "
        + "gaa.enabled, "
        + "gaa.enabled_time, "
        + "gaa.disabled_time, "
        + "gg.id as group_id, "
        + "gaa.id as attribute_assign_id, "
        + "gadn.id as attribute_def_name_id, "
        + "gad.id as attribute_def_id, "
        + "gm.id as member_id, "
        + "gaaa.id as action_id, "
        + " gaav.id AS attribute_assign_value_id "
        + "from grouper_attribute_assign gaa, grouper_memberships_all_v gmav, "
        + "grouper_attribute_def_name gadn, grouper_attribute_def gad, "
        + "grouper_groups gg, grouper_fields gf, grouper_members gm, grouper_attr_assign_action gaaa, grouper_attribute_assign_value gaav  "
        + "where gaav.attribute_assign_id = gaa.id "
        + " and gaa.owner_group_id = gmav.owner_group_id "
        + "and gaa.owner_member_id = gmav.member_id "
        + "and gaa.attribute_def_name_id = gadn.id "
        + "and gadn.attribute_def_id = gad.id "
        + "and gmav.immediate_mship_enabled = 'T' "
        + "and gmav.owner_group_id = gg.id "
        + "and gmav.field_id = gf.id "
        + "and gf.type = 'list' "
        + "and gmav.member_id = gm.id "
        + "and gaa.owner_member_id is not null "
        + "and gaa.owner_group_id is not null "
        + "and gaa.attribute_assign_action_id = gaaa.id ");
  }
  
  /**
   * Add quartz tables
   * @param ddlVersionBean
   * @param database
   */
  static void addQuartzForeignKeys(DdlVersionBean ddlVersionBean, Database database) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("addQuartzForeignKeys", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_QZ_TRIGGERS", "qrtz_trigger_to_jobs_fk", "grouper_QZ_JOB_DETAILS", 
        Arrays.asList(new String[]{"sched_name", "job_name", "job_group"}),
        Arrays.asList(new String[]{"sched_name", "job_name", "job_group"}));
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_QZ_SIMPLE_TRIGGERS", "qrtz_simple_trig_to_trig_fk", "grouper_QZ_TRIGGERS", 
        Arrays.asList(new String[]{"sched_name", "trigger_name", "trigger_group"}),
        Arrays.asList(new String[]{"sched_name", "trigger_name", "trigger_group"}));
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_QZ_CRON_TRIGGERS", "qrtz_cron_trig_to_trig_fk", "grouper_QZ_TRIGGERS", 
        Arrays.asList(new String[]{"sched_name", "trigger_name", "trigger_group"}),
        Arrays.asList(new String[]{"sched_name", "trigger_name", "trigger_group"}));
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_QZ_SIMPROP_TRIGGERS", "qrtz_simprop_trig_to_trig_fk", "grouper_QZ_TRIGGERS", 
        Arrays.asList(new String[]{"sched_name", "trigger_name", "trigger_group"}),
        Arrays.asList(new String[]{"sched_name", "trigger_name", "trigger_group"}));
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_QZ_BLOB_TRIGGERS", "qrtz_blob_trig_to_trig_fk", "grouper_QZ_TRIGGERS", 
        Arrays.asList(new String[]{"sched_name", "trigger_name", "trigger_group"}),
        Arrays.asList(new String[]{"sched_name", "trigger_name", "trigger_group"}));
  }
  
  /**
   * Add quartz tables
   * @param ddlVersionBean
   * @param database
   */
  static void addQuartzTables(DdlVersionBean ddlVersionBean, Database database) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("addQuartzTables", true)) {
      return;
    }

    {
      Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_QZ_JOB_DETAILS");
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "sched_name", Types.VARCHAR, "120", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "job_name", Types.VARCHAR, "200", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "job_group", Types.VARCHAR, "200", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "description", Types.VARCHAR, "250", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "job_class_name", Types.VARCHAR, "250", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "is_durable", Types.BOOLEAN, "1", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "is_nonconcurrent", Types.BOOLEAN, "1", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "is_update_data", Types.BOOLEAN, "1", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "requests_recovery", Types.BOOLEAN, "1", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "job_data", Types.BLOB, null, false, false);
    }
    
    {
      Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_QZ_TRIGGERS");
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "sched_name", Types.VARCHAR, "120", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "trigger_name", Types.VARCHAR, "200", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "trigger_group", Types.VARCHAR, "200", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "job_name", Types.VARCHAR, "200", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "job_group", Types.VARCHAR, "200", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "description", Types.VARCHAR, "250", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "next_fire_time", Types.BIGINT, "13", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "prev_fire_time", Types.BIGINT, "13", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "priority", Types.BIGINT, "13", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "trigger_state", Types.VARCHAR, "16", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "trigger_type", Types.VARCHAR, "8", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "start_time", Types.BIGINT, "13", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "end_time", Types.BIGINT, "13", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "calendar_name", Types.VARCHAR, "200", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "misfire_instr", Types.BIGINT, "2", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "job_data", Types.BLOB, null, false, false);
    }
    
    {
      Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_QZ_SIMPLE_TRIGGERS");
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "sched_name", Types.VARCHAR, "120", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "trigger_name", Types.VARCHAR, "200", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "trigger_group", Types.VARCHAR, "200", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "repeat_count", Types.BIGINT, "7", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "repeat_interval", Types.BIGINT, "12", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "times_triggered", Types.BIGINT, "10", false, true);
    }
    
    {
      Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_QZ_CRON_TRIGGERS");
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "sched_name", Types.VARCHAR, "120", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "trigger_name", Types.VARCHAR, "200", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "trigger_group", Types.VARCHAR, "200", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "cron_expression", Types.VARCHAR, "120", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "time_zone_id", Types.VARCHAR, "80", false, false);
    }
    
    {
      Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_QZ_SIMPROP_TRIGGERS");
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "sched_name", Types.VARCHAR, "120", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "trigger_name", Types.VARCHAR, "200", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "trigger_group", Types.VARCHAR, "200", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "str_prop_1", Types.VARCHAR, "512", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "str_prop_2", Types.VARCHAR, "512", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "str_prop_3", Types.VARCHAR, "512", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "int_prop_1", Types.BIGINT, "10", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "int_prop_2", Types.BIGINT, "10", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "long_prop_1", Types.BIGINT, "13", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "long_prop_2", Types.BIGINT, "13", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "dec_prop_1", Types.DOUBLE, "13", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "dec_prop_2", Types.DOUBLE, "13", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "bool_prop_1", Types.BOOLEAN, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "bool_prop_2", Types.BOOLEAN, "1", false, false);
    }
    
    {
      Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_QZ_BLOB_TRIGGERS");
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "sched_name", Types.VARCHAR, "120", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "trigger_name", Types.VARCHAR, "200", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "trigger_group", Types.VARCHAR, "200", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "blob_data", Types.BLOB, null, false, false);
    }
    
    {
      Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_QZ_CALENDARS");
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "sched_name", Types.VARCHAR, "120", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "calendar_name", Types.VARCHAR, "200", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "calendar", Types.BLOB, null, false, true);
    }
    
    {
      Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_QZ_PAUSED_TRIGGER_GRPS");
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "sched_name", Types.VARCHAR, "120", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "trigger_group", Types.VARCHAR, "200", true, true);
    }
    
    {
      Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_QZ_FIRED_TRIGGERS");
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "sched_name", Types.VARCHAR, "120", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "entry_id", Types.VARCHAR, "95", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "trigger_name", Types.VARCHAR, "200", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "trigger_group", Types.VARCHAR, "200", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "instance_name", Types.VARCHAR, "200", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "fired_time", Types.BIGINT, "13", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "sched_time", Types.BIGINT, "13", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "priority", Types.BIGINT, "13", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "state", Types.VARCHAR, "16", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "job_name", Types.VARCHAR, "200", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "job_group", Types.VARCHAR, "200", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "is_nonconcurrent", Types.BOOLEAN, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "requests_recovery", Types.BOOLEAN, "1", false, false);
    }
    
    {
      Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_QZ_SCHEDULER_STATE");
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "sched_name", Types.VARCHAR, "120", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "instance_name", Types.VARCHAR, "200", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "last_checkin_time", Types.BIGINT, "13", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "checkin_interval", Types.BIGINT, "13", false, true);
    }
    
    {
      Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_QZ_LOCKS");
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "sched_name", Types.VARCHAR, "120", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "lock_name", Types.VARCHAR, "40", true, true);
    }
  }

  
}
