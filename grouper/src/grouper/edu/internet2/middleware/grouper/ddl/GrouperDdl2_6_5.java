package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.commons.logging.Log;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDdl2_6_5 {

  /**
   * if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V39.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V39.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDdl2_6_5.class);

  static void addGrouperMembersColumns(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_6_5_addGrouperMembersColumn", true)) {
      return;
    }

    Table memberTable = GrouperDdlUtils.ddlutilsFindTable(database, Member.TABLE_GROUPER_MEMBERS, true);
    
    if (buildingFromScratch(ddlVersionBean)) {
      
      //this is required if the member table is new    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(memberTable, Member.COLUMN_SUBJECT_RESOLUTION_ELIGIBLE, Types.VARCHAR, "1", false, true, "T");
    } else {
      if (!GrouperDdlUtils.isPostgres()) {
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(memberTable, Member.COLUMN_SUBJECT_RESOLUTION_ELIGIBLE, Types.VARCHAR, "1", false, false, "T");        
      } else {
        ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_members ADD COLUMN subject_resolution_eligible VARCHAR(1);\n");
      }
    }

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, memberTable.getName(), "member_eligible_idx", false, Member.COLUMN_SUBJECT_RESOLUTION_ELIGIBLE);       

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
        if (!GrouperDdlUtils.isMysql() && !GrouperDdlUtils.isOracle()) {
          ddlVersionBean.getAdditionalScripts().append(
              "update grouper_members set subject_resolution_eligible='T' where subject_resolution_eligible is null;\n" +
              "commit;\n");
        }
      }
    }
    
    if (!buildingFromScratch(ddlVersionBean)) {
      if (GrouperDdlUtils.isPostgres()) {
        ddlVersionBean.getAdditionalScripts().append(          
            "ALTER TABLE " + Member.TABLE_GROUPER_MEMBERS + " ALTER COLUMN " + Member.COLUMN_SUBJECT_RESOLUTION_ELIGIBLE + " SET NOT NULL;\n");
        ddlVersionBean.getAdditionalScripts().append(          
            "ALTER TABLE " + Member.TABLE_GROUPER_MEMBERS + " ALTER COLUMN " + Member.COLUMN_SUBJECT_RESOLUTION_ELIGIBLE + " SET DEFAULT 'T';\n");
      } 
    }    
    
  }

  /**
   * 
   */
  static void addGrouperMembersComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_6_5_addGrouperMembersComments", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_members", Member.COLUMN_SUBJECT_RESOLUTION_ELIGIBLE, "T is this subject is resolvable and has privileges and memberships and should be checked periodically by USDU");

  }
  
  public static final String TABLE_GROUPER_FAILSAFE = "grouper_failsafe";
  
  public static final String COLUMN_GROUPER_FAILSAFE_ID = "id";
  public static final String COLUMN_GROUPER_FAILSAFE_NAME = "name";
  public static final String COLUMN_GROUPER_FAILSAFE_LAST_RUN = "last_run";
  public static final String COLUMN_GROUPER_FAILSAFE_LAST_FAILSAFE_ISSUE_STARTED = "last_failsafe_issue_started";
  public static final String COLUMN_GROUPER_FAILSAFE_LAST_FAILSAFE_ISSUE = "last_failsafe_issue";
  public static final String COLUMN_GROUPER_FAILSAFE_LAST_SUCCESS = "last_success";
  public static final String COLUMN_GROUPER_FAILSAFE_LAST_APPROVAL = "last_approval";
  public static final String COLUMN_GROUPER_FAILSAFE_APPROVAL_MEMBER_ID = "approval_member_id";
  public static final String COLUMN_GROUPER_FAILSAFE_APPROVED_ONCE = "approved_once";
  public static final String COLUMN_GROUPER_FAILSAFE_APPROVED_UNTIL = "approved_until";
  public static final String COLUMN_GROUPER_FAILSAFE_LAST_UPDATED = "last_updated";

  public static final String TABLE_GROUPER_STEM_VIEW_PRIVILEGE = "grouper_stem_view_privilege";
  
  // uuid of member, foreign key cascade delete
  public static final String COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_STEM_UUID = "stem_uuid";
  // uuid of stem, foreign key cascade delete
  public static final String COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_MEMBER_UUID = "member_uuid";
  // G (has group privilege directly in folder), S (has folder privilege on this folder), A (has attribute privilege on an attribute directly in this folder)
  public static final String COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_OBJECT_TYPE = "object_type";

  static void addGrouperStemViewPrivilegeTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_6_5_addGrouperStemViewPrivilegeTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_STEM_VIEW_PRIVILEGE;
  
    Table grouperStemViewPrivilegeTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperStemViewPrivilegeTable, COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_MEMBER_UUID,
        Types.VARCHAR, "40", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperStemViewPrivilegeTable, COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_STEM_UUID,
        Types.VARCHAR, "40", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperStemViewPrivilegeTable, COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_OBJECT_TYPE,
        Types.CHAR, "1", false, true);
    

  }

  /**
   * add grouper password foreign keys
   * @param ddlVersionBean
   * @param database
   */
  static void addGrouperStemViewPrivilegeForeignKeys(Database database, DdlVersionBean ddlVersionBean) {
    

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_addGrouperStemViewPrivilegeForeignKeys", true)) {
      return;
    }

//    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, TABLE_GROUPER_STEM_VIEW_PRIVILEGE,
//      "fk_grouper_st_v_pr_mem", Member.TABLE_GROUPER_MEMBERS, COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_MEMBER_UUID, Member.COLUMN_ID);
//    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, TABLE_GROUPER_STEM_VIEW_PRIVILEGE,
//      "fk_grouper_st_v_pr_st", Stem.TABLE_GROUPER_STEMS, COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_STEM_UUID, Stem.COLUMN_ID);
  }

  static void addGrouperStemViewPrivilegeIndex(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_6_5_addGrouperStemViewPrivilegeIndex", true)) {
      return;
    }
  
    Table grouperStemViewPrivilegeTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        TABLE_GROUPER_STEM_VIEW_PRIVILEGE);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperStemViewPrivilegeTable.getName(), 
        "grouper_stem_v_priv_mem_idx", false, 
        COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_MEMBER_UUID, COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_OBJECT_TYPE);
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperStemViewPrivilegeTable.getName(), 
        "grouper_stem_v_priv_stem_idx", false, 
        COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_STEM_UUID, COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_OBJECT_TYPE);

  }

  static void addGrouperStemViewPrivilegeComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_6_5_addGrouperStemViewPrivilegeComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_STEM_VIEW_PRIVILEGE;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "caches which stems (not inherited) that a user can view since they have a privilege on an object in the folder");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_MEMBER_UUID, 
        "member uuid of the subject, foreign key cascade delete");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_STEM_UUID, 
        "stem uuid of the stem with a view privilege, foreign key cascade delete");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_OBJECT_TYPE, 
        "G (has group privilege directly in folder), S (has folder privilege on this folder),"
        + " A (has attribute privilege on an attribute directly in this folder)");
  }
  public static final String TABLE_GROUPER_LAST_LOGIN = "grouper_last_login";
  
  // member uuid of subject, foreign key cascade delete
  public static final String COLUMN_GROUPER_LAST_LOGIN_MEMBER_UUID = "member_uuid";
  // When last logged in millis since 1970
  public static final String COLUMN_GROUPER_LAST_LOGIN_MILLIS = "last_login";
  // When last needed stem view
  public static final String COLUMN_GROUPER_LAST_STEM_VIEW_NEED = "last_stem_view_need";
  // When stem view privs last computed
  public static final String COLUMN_GROUPER_LAST_STEM_VIEW_COMPUTE = "last_stem_view_compute";

  static void addGrouperLastLoginTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_6_5_addGrouperLastLoginTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_LAST_LOGIN;
  
    Table grouperLastLoginTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperLastLoginTable, COLUMN_GROUPER_LAST_LOGIN_MEMBER_UUID,
        Types.VARCHAR, "40", true, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperLastLoginTable, COLUMN_GROUPER_LAST_LOGIN_MILLIS,
        Types.BIGINT, "12", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperLastLoginTable, COLUMN_GROUPER_LAST_STEM_VIEW_NEED,
        Types.BIGINT, "12", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperLastLoginTable, COLUMN_GROUPER_LAST_STEM_VIEW_COMPUTE,
        Types.BIGINT, "12", false, false);
    

  }

  /**
   * add grouper password foreign keys
   * @param ddlVersionBean
   * @param database
   */
  static void addGrouperLastLoginForeignKey(Database database, DdlVersionBean ddlVersionBean) {
    
  
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_5_addGrouperLastLoginForeignKey", true)) {
      return;
    }
  
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, TABLE_GROUPER_LAST_LOGIN,
      "fk_grouper_last_login_mem", Member.TABLE_GROUPER_MEMBERS, COLUMN_GROUPER_LAST_LOGIN_MEMBER_UUID, Member.COLUMN_ID);
  }

  static void addGrouperLastLoginIndex(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_6_5_addGrouperLastLoginIndex", true)) {
      return;
    }
  
    Table grouperLastLoginTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        TABLE_GROUPER_LAST_LOGIN);
    
    // I think oracle already has this
    if (!GrouperDdlUtils.isOracle()) {

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperLastLoginTable.getName(), 
          "grouper_last_login_mem_idx", true, 
          COLUMN_GROUPER_STEM_VIEW_PRIVILEGE_MEMBER_UUID);
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperLastLoginTable.getName(), 
        "grouper_last_login_login_idx", false, 
        COLUMN_GROUPER_LAST_LOGIN_MILLIS);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperLastLoginTable.getName(), 
        "grouper_last_login_st_view_idx", false, 
        COLUMN_GROUPER_LAST_STEM_VIEW_NEED);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperLastLoginTable.getName(), 
        "grouper_last_login_st_comp_idx", false, 
        COLUMN_GROUPER_LAST_STEM_VIEW_COMPUTE);

  }

  static void addGrouperLastLoginComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_6_5_addGrouperLastLoginComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_LAST_LOGIN;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "caches when someone has logged in to grouper in some regard last");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, COLUMN_GROUPER_LAST_LOGIN_MEMBER_UUID, 
        "member uuid of the subject, foreign key cascade delete");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, COLUMN_GROUPER_LAST_LOGIN_MILLIS, 
        "when last logged in millis since 1970");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, COLUMN_GROUPER_LAST_STEM_VIEW_NEED, 
        "when last needed stem view");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, COLUMN_GROUPER_LAST_STEM_VIEW_COMPUTE, 
        "when stem view privs last computed");

  }

  static void addGrouperFailsafeComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_6_5_addGrouperFailsafeComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_FAILSAFE;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "holds failsafe state and approvals");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_FAILSAFE_ID, 
        "uuid of this row");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_FAILSAFE_NAME,
        "name of this failsafe job, e.g. the job name in loader log");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_FAILSAFE_LAST_RUN,
        "millis since 1970 of last run of this job (fail or not)");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_FAILSAFE_LAST_FAILSAFE_ISSUE_STARTED,
        "millis since 1970 of when the last failsafe issue started");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_FAILSAFE_LAST_FAILSAFE_ISSUE,
        "millis since 1970 of when the last failsafe issue occurred");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_FAILSAFE_LAST_SUCCESS,
        "millis since 1970 of when last success of job occurred");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_FAILSAFE_LAST_APPROVAL,
        "millis since 1970 of last approval of failsafe");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_FAILSAFE_APPROVAL_MEMBER_ID,
        "member uuid of user who last approved the failsafe");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_FAILSAFE_APPROVED_ONCE,
        "T if next run is approved (e.g. click button) and F if next run is not approved (steady state)");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_FAILSAFE_APPROVED_UNTIL,
        "millis since 1970 that failsafes are approved for the job");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_FAILSAFE_LAST_UPDATED,
        "millis since 1970 that this row was last updated");

  }

  static void addGrouperFailsafeIndex(Database database, DdlVersionBean ddlVersionBean) {
  
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_6_5_addGrouperFailsafeIndex", true)) {
      return;
    }
  
    Table grouperFailsafeTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        TABLE_GROUPER_FAILSAFE);
    
    // oracle already has this I think
    if (!GrouperDdlUtils.isOracle()) {
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperFailsafeTable.getName(), 
          "grouper_failsafe_id_idx", true, 
          COLUMN_GROUPER_FAILSAFE_ID);
    }
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperFailsafeTable.getName(), 
        "grouper_failsafe_name_idx", true, 
        COLUMN_GROUPER_FAILSAFE_NAME);

  }

  static void addGrouperFailsafeTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_6_5_addGrouperFailsafeTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_FAILSAFE;
  
    Table grouperFailsafeTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFailsafeTable, COLUMN_GROUPER_FAILSAFE_ID,
        Types.VARCHAR, "40", true, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFailsafeTable, COLUMN_GROUPER_FAILSAFE_NAME,
        Types.VARCHAR, "200", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFailsafeTable, COLUMN_GROUPER_FAILSAFE_LAST_RUN,
        Types.BIGINT, "12", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFailsafeTable, COLUMN_GROUPER_FAILSAFE_LAST_FAILSAFE_ISSUE_STARTED,
        Types.BIGINT, "12", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFailsafeTable, COLUMN_GROUPER_FAILSAFE_LAST_FAILSAFE_ISSUE,
        Types.BIGINT, "12", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFailsafeTable, COLUMN_GROUPER_FAILSAFE_LAST_SUCCESS,
        Types.BIGINT, "12", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFailsafeTable, COLUMN_GROUPER_FAILSAFE_LAST_APPROVAL,
        Types.BIGINT, "12", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFailsafeTable, COLUMN_GROUPER_FAILSAFE_APPROVAL_MEMBER_ID,
        Types.VARCHAR, "40", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFailsafeTable, COLUMN_GROUPER_FAILSAFE_APPROVED_ONCE,
        Types.VARCHAR, "1", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFailsafeTable, COLUMN_GROUPER_FAILSAFE_APPROVED_UNTIL,
        Types.BIGINT, "12", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFailsafeTable, COLUMN_GROUPER_FAILSAFE_LAST_UPDATED,
        Types.BIGINT, "12", false, false);
    

  }

}
