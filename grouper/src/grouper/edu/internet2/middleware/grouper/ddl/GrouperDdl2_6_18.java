package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;

public class GrouperDdl2_6_18 {
  
  /**
   * if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V44.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V44.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }
  
  public static String TABLE_GROUPER_SYNC_DEP_GROUP_USER = "grouper_sync_dep_group_user";
  
  public static String COLUMN_GROUPER_SYNC_DEP_GROUP_USER_ID_INDEX = "id_index";
  public static String COLUMN_GROUPER_SYNC_DEP_GROUP_USER_GROUPER_SYNC_ID = "grouper_sync_id";
  public static String COLUMN_GROUPER_SYNC_DEP_GROUP_USER_GROUP_ID = "group_id";
  public static String COLUMN_GROUPER_SYNC_DEP_GROUP_USER_FIELD_ID = "field_id";
  
  static void addGrouperSyncDepGroupUserIndexes(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_6_5_addGrouperSyncDepGroupUserIndexes", true)) {
      return;
    }
  
    Table grouperSyncDepGroupUserTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        TABLE_GROUPER_SYNC_DEP_GROUP_USER);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperSyncDepGroupUserTable.getName(), 
        "grouper_sync_dep_grp_user_idx0", false, 
        COLUMN_GROUPER_SYNC_DEP_GROUP_USER_GROUPER_SYNC_ID);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperSyncDepGroupUserTable.getName(), 
        "grouper_sync_dep_grp_user_idx1", true, 
        COLUMN_GROUPER_SYNC_DEP_GROUP_USER_GROUPER_SYNC_ID, COLUMN_GROUPER_SYNC_DEP_GROUP_USER_GROUP_ID, COLUMN_GROUPER_SYNC_DEP_GROUP_USER_FIELD_ID);

  }
  
  static void addGrouperSyncDepGroupGroupIndexes(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_6_5_addGrouperSyncDepGroupGroupIndexes", true)) {
      return;
    }
  
    Table grouperSyncDepGroupGroupTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        TABLE_GROUPER_SYNC_DEP_GROUP_GROUP);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperSyncDepGroupGroupTable.getName(), 
        "grouper_sync_dep_grp_grp_idx0", false, 
        COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_GROUPER_SYNC_ID);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperSyncDepGroupGroupTable.getName(), 
        "grouper_sync_dep_grp_grp_idx1", true, 
        COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_GROUPER_SYNC_ID, COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_GROUP_ID, COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_FIELD_ID, COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_PROVISIONABLE_ID);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperSyncDepGroupGroupTable.getName(), 
        "grouper_sync_dep_grp_grp_idx2", false, 
        COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_GROUPER_SYNC_ID, COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_PROVISIONABLE_ID);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperSyncDepGroupGroupTable.getName(), 
        "grouper_sync_dep_grp_grp_idx3", false, 
        COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_GROUPER_SYNC_ID, COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_GROUP_ID, COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_FIELD_ID);

  }
  
  static void addGrouperSyncDepGroupUserComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_6_18_addGrouperSyncDepGroupUserComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_SYNC_DEP_GROUP_USER;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "Groups are listed that are used in user translations.  Users will need to be recalced if there are changes (not membership recalc)");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, COLUMN_GROUPER_SYNC_DEP_GROUP_USER_ID_INDEX, 
        "primary key");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, COLUMN_GROUPER_SYNC_DEP_GROUP_USER_GROUPER_SYNC_ID, 
        "provisioner");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, COLUMN_GROUPER_SYNC_DEP_GROUP_USER_GROUP_ID, 
        "group uuid");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, COLUMN_GROUPER_SYNC_DEP_GROUP_USER_FIELD_ID, 
        "field uuid");
    
  }
  
  /**
   * add grouper sync dep user foreign keys
   * @param ddlVersionBean
   * @param database
   */
  static void addGrouperSyncDepGroupUserForeignKeys(Database database, DdlVersionBean ddlVersionBean) {
    
  
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_6_18_addGrouperSyncDepGroupUserForeignKeys", true)) {
      return;
    }
  
    //  alter table grouper_sync_dep_group_user
    //  add CONSTRAINT grouper_sync_dep_grp_user_fk_2 FOREIGN KEY (grouper_sync_id) REFERENCES grouper_sync(id);
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, TABLE_GROUPER_SYNC_DEP_GROUP_USER,
        "grouper_sync_dep_grp_user_fk_2", "grouper_sync" , COLUMN_GROUPER_SYNC_DEP_GROUP_USER_GROUPER_SYNC_ID, "id");

  }

  /**
   * add grouper sync dep group foreign keys
   * @param ddlVersionBean
   * @param database
   */
  static void addGrouperSyncDepGroupGroupForeignKeys(Database database, DdlVersionBean ddlVersionBean) {
    
  
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_6_18_addGrouperSyncDepGroupGroupForeignKeys", true)) {
      return;
    }
  
    
    //  alter table grouper_sync_dep_group_group
    //  add CONSTRAINT grouper_sync_dep_grp_grp_fk_1 FOREIGN KEY (provisionable_group_id) REFERENCES grouper_groups(id);

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, TABLE_GROUPER_SYNC_DEP_GROUP_GROUP,
        "grouper_sync_dep_grp_grp_fk_1", Group.TABLE_GROUPER_GROUPS, COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_PROVISIONABLE_ID, Group.COLUMN_ID);

    //  alter table grouper_sync_dep_group_group
    //  add CONSTRAINT grouper_sync_dep_grp_grp_fk_3 FOREIGN KEY (grouper_sync_id) REFERENCES grouper_sync(id);
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, TABLE_GROUPER_SYNC_DEP_GROUP_GROUP,
        "grouper_sync_dep_grp_grp_fk_3", "grouper_sync", COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_GROUPER_SYNC_ID, "id");

  }

  
  static void addGrouperSyncDepGroupGroupComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_6_18_addGrouperSyncDepGroupGroupComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_SYNC_DEP_GROUP_GROUP;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "Groups are listed that are used in group translations.  Provisionable groups will need to be recalced if there are changes (not membership recalc)");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_ID_INDEX, 
        "primary key");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_GROUPER_SYNC_ID, 
        "provisioner");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_GROUP_ID, 
        "group uuid");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_FIELD_ID, 
        "field uuid");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_FIELD_ID, 
        "group uuid of the provisionable group that uses this other group as a role");
        
  }
  
  static void addGrouperSyncDepGroupUserTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_6_18_addGrouperSyncDepGroupUserTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_SYNC_DEP_GROUP_USER;
  
    Table grouperSyncDepGroupUserTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncDepGroupUserTable, COLUMN_GROUPER_SYNC_DEP_GROUP_USER_ID_INDEX,
        Types.BIGINT, "12", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncDepGroupUserTable, COLUMN_GROUPER_SYNC_DEP_GROUP_USER_GROUPER_SYNC_ID,
        Types.VARCHAR, "40", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncDepGroupUserTable, COLUMN_GROUPER_SYNC_DEP_GROUP_USER_GROUP_ID,
        Types.VARCHAR, "40", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncDepGroupUserTable, COLUMN_GROUPER_SYNC_DEP_GROUP_USER_FIELD_ID,
        Types.VARCHAR, "40", false, true);

    
  }

  public static String TABLE_GROUPER_SYNC_DEP_GROUP_GROUP = "grouper_sync_dep_group_group";
  
  public static String COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_ID_INDEX = "id_index";
  public static String COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_GROUPER_SYNC_ID = "grouper_sync_id";
  public static String COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_GROUP_ID = "group_id";
  public static String COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_FIELD_ID = "field_id";
  public static String COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_PROVISIONABLE_ID = "provisionable_group_id";
  
  static void addGrouperSyncDepGroupGroupTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_6_18_addGrouperSyncDepGroupGroupTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_SYNC_DEP_GROUP_GROUP;
  
    Table grouperSyncDepGroupGroupTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncDepGroupGroupTable, COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_ID_INDEX,
        Types.BIGINT, "12", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncDepGroupGroupTable, COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_GROUPER_SYNC_ID,
        Types.VARCHAR, "40", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncDepGroupGroupTable, COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_GROUP_ID,
        Types.VARCHAR, "40", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncDepGroupGroupTable, COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_FIELD_ID,
        Types.VARCHAR, "40", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncDepGroupGroupTable, COLUMN_GROUPER_SYNC_DEP_GROUP_GROUP_PROVISIONABLE_ID,
        Types.VARCHAR, "40", false, true);

    
  }


  public static void attributeAssignDisallowNotNull(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    // if building from scratch its already got it
    if (buildingFromScratch(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_6_18_attributeAssignDisallowNotNull", true)) {
      return;
    }
    
    int count = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_attribute_assign where disallowed is null");
    if (count > 0) {
      ddlVersionBean.getAdditionalScripts().append(
          "update grouper_attribute_assign set disallowed='F' where disallowed is null;\n" +
          "commit;\n");
    }
    
    count = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_pit_attribute_assign where disallowed is null");
    if (count > 0) {
      ddlVersionBean.getAdditionalScripts().append(
          "update grouper_pit_attribute_assign set disallowed='F' where disallowed is null;\n" +
          "commit;\n");
    }
    
    if (GrouperDdlUtils.isPostgres()) {
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_attribute_assign ALTER COLUMN disallowed SET NOT NULL;\n");
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_attribute_assign ALTER COLUMN disallowed SET DEFAULT 'F';\n");
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_pit_attribute_assign ALTER COLUMN disallowed SET NOT NULL;\n");
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_pit_attribute_assign ALTER COLUMN disallowed SET DEFAULT 'F';\n");
    } else if (GrouperDdlUtils.isMysql()) {
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_attribute_assign MODIFY COLUMN disallowed VARCHAR(1) DEFAULT 'F' NOT NULL;\n");
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_pit_attribute_assign MODIFY COLUMN disallowed VARCHAR(1) DEFAULT 'F' NOT NULL;\n");
    } else if (GrouperDdlUtils.isOracle()) {
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_attribute_assign MODIFY disallowed VARCHAR2(1) DEFAULT 'F' NOT NULL;\n");
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_pit_attribute_assign MODIFY disallowed VARCHAR2(1) DEFAULT 'F' NOT NULL;\n");
    }
  }
}
