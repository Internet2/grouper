package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

public class GrouperDdl2_6_8 {
  
  /**
   * if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V41.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V41.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  public static final String TABLE_GROUPER_PROV_DUO_USER = "grouper_prov_duo_user";
  
  public static final String COLUMN_GROUPER_PROV_DUO_USER_CONFIG_ID = "config_id";

  public static final String COLUMN_GROUPER_PROV_DUO_USER_ID = "user_id";

  public static final String COLUMN_GROUPER_PROV_DUO_ALIASES = "aliases";
  
  public static final String COLUMN_GROUPER_PROV_DUO_PHONES = "phones";

  public static final String COLUMN_GROUPER_PROV_DUO_IS_PUSH_ENABLED = "is_push_enabled";
  
  public static final String COLUMN_GROUPER_PROV_DUO_USER_EMAIL = "email";
  
  public static final String COLUMN_GROUPER_PROV_DUO_USER_FIRST_NAME = "first_name";
  
  public static final String COLUMN_GROUPER_PROV_DUO_USER_LAST_NAME = "last_name";

  public static final String COLUMN_GROUPER_PROV_DUO_IS_ENROLLED = "is_enrolled";

  public static final String COLUMN_GROUPER_PROV_DUO_LAST_DIRECTORY_SYNC = "last_directory_sync";

  public static final String COLUMN_GROUPER_PROV_DUO_NOTES = "notes";

  public static final String COLUMN_GROUPER_PROV_DUO_REAL_NAME = "real_name";

  public static final String COLUMN_GROUPER_PROV_DUO_STATUS = "status";

  public static final String COLUMN_GROUPER_PROV_DUO_USER_NAME = "user_name";

  public static final String COLUMN_GROUPER_PROV_DUO_CREATED_AT = "created_at";
  
  public static final String COLUMN_GROUPER_PROV_DUO_USER_LAST_LOGIN_TIME = "last_login_time";
  
  static void addGrouperProvDuoUserComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_6_8_addGrouperProvDuoUserComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_PROV_DUO_USER;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "table to load duo users into a sql for reporting and deprovisioning");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_DUO_USER_CONFIG_ID, 
        "duo config id identifies which duo external system is being loaded");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_DUO_USER_ID, 
        "duo internal ID for this user (used in web services)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_DUO_ALIASES, 
        "comma separated list of aliases for the user");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_DUO_PHONES, 
        "comma separated list of phones for the user");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_DUO_IS_PUSH_ENABLED, 
        "is push enabled for one of the registered phones for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_DUO_USER_EMAIL, 
        "email address of the user");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_DUO_USER_FIRST_NAME, 
        "First name of user");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_DUO_USER_LAST_NAME, 
        "Last name of user");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_DUO_IS_ENROLLED, 
        "is user enrolled");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_DUO_LAST_DIRECTORY_SYNC, 
        "last directory sync timestamp");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_DUO_NOTES, 
        "notes for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_DUO_REAL_NAME, 
        "real name of user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_DUO_STATUS, 
        "status of the user. One of active, bypass, disabled, locked out, pending deletion");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_DUO_USER_NAME, 
        "user name of the user");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_DUO_CREATED_AT, 
        "When the user was created in duo");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_DUO_USER_LAST_LOGIN_TIME, 
        "When the user last logged in to duo");
    
  
  }

  static void addGrouperProvDuoUserIndex(DdlVersionBean ddlVersionBean, Database database) {
  
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_6_8_addGrouperProvDuoUserIndex", true)) {
      return;
    }
  
    Table grouperDuoTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        TABLE_GROUPER_PROV_DUO_USER);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperDuoTable.getName(), 
        "grouper_duo_user_config_id_idx", false, 
        COLUMN_GROUPER_PROV_DUO_USER_CONFIG_ID);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperDuoTable.getName(), 
        "grouper_duo_user_user_name_idx", true, 
        COLUMN_GROUPER_PROV_DUO_USER_NAME, COLUMN_GROUPER_PROV_DUO_USER_CONFIG_ID);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperDuoTable.getName(), 
        "grouper_duo_user_id_idx", true, 
        COLUMN_GROUPER_PROV_DUO_USER_ID, COLUMN_GROUPER_PROV_DUO_USER_CONFIG_ID);
    
  }

  static void addGrouperProvDuoUserTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_6_8_addGrouperProvDuoUserTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_PROV_DUO_USER;
  
    Table grouperFileTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_DUO_USER_CONFIG_ID,
        Types.VARCHAR, "50", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_DUO_USER_ID,
        Types.VARCHAR, "40", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_DUO_ALIASES,
        Types.VARCHAR, "4000", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_DUO_PHONES,
        Types.VARCHAR, "4000", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_DUO_IS_PUSH_ENABLED,
        Types.VARCHAR, "1", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_DUO_USER_EMAIL,
        Types.VARCHAR, "200", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_DUO_USER_FIRST_NAME, 
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_DUO_USER_LAST_NAME, 
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_DUO_IS_ENROLLED,
        Types.VARCHAR, "1", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_DUO_LAST_DIRECTORY_SYNC,
        Types.BIGINT, "12", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_DUO_NOTES,
        Types.VARCHAR, "4000", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_DUO_REAL_NAME, 
        Types.VARCHAR, "256", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_DUO_STATUS, 
        Types.VARCHAR, "40", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_DUO_USER_NAME, 
        Types.VARCHAR, "256", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_DUO_CREATED_AT, 
        Types.BIGINT, "12", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_DUO_USER_LAST_LOGIN_TIME, 
        Types.BIGINT, "12", false, false);
    
  }

}
