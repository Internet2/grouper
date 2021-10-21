package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.authentication.GrouperPassword;
import edu.internet2.middleware.grouper.authentication.GrouperPasswordRecentlyUsed;
import edu.internet2.middleware.grouper.file.GrouperFile;

public class GrouperDdl2_6_1 {

  /**
   * if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V38.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V38.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  static void addGrouperPasswordColumns(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_6_1_addGrouperPasswordColumns", true)) {
      return;
    }

    {
      Table grouperPasswordTable = GrouperDdlUtils.ddlutilsFindTable(database, "grouper_password", true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_EXPIRES_MILLIS, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_CREATED_MILLIS, Types.BIGINT, "20", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_MEMBER_ID_WHO_SET_PASSWORD, Types.VARCHAR, "40", false, false); 
       
    }
    
  }

  /**
   * 
   */
  static void addGrouperPasswordComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_6_1_addGrouperPasswordComments", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_password", GrouperPassword.COLUMN_EXPIRES_MILLIS, "millis since 1970 this password is going to expire");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_password", GrouperPassword.COLUMN_CREATED_MILLIS, "millis since 1970 this password was created");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_password", GrouperPassword.COLUMN_MEMBER_ID_WHO_SET_PASSWORD, "member id who set this password");

  }
  
  static void addGrouperPasswordRecentlyUsedColumns(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_6_1_addGrouperPasswordRecentlyUsedColumns", true)) {
      return;
    }

    {
      Table grouperPasswordRecentlyUsedTable = GrouperDdlUtils.ddlutilsFindTable(database, "grouper_password_recently_used", true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordRecentlyUsedTable, GrouperPasswordRecentlyUsed.COLUMN_ATTEMPT_MILLIS, Types.BIGINT, "20", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordRecentlyUsedTable, GrouperPasswordRecentlyUsed.COLUMN_IP_ADDRESS, Types.VARCHAR, "20", false, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordRecentlyUsedTable, GrouperPasswordRecentlyUsed.COLUMN_STATUS, Types.CHAR, "1", false, true); 
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordRecentlyUsedTable, GrouperPasswordRecentlyUsed.COLUMN_HIBERNATE_VERSION_NUMBER, Types.BIGINT, null, false, true);
      
    }
    
  }
  
  static void dropGrouperPasswordColumns(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_6_1_dropGrouperPasswordColumns", true)) {
      return;
    }

    {
      Table grouperPasswordTable = GrouperDdlUtils.ddlutilsFindTable(database, "grouper_password", true);
      
      GrouperDdlUtils.ddlutilsDropColumn(grouperPasswordTable, "recent_source_addresses", ddlVersionBean);
      GrouperDdlUtils.ddlutilsDropColumn(grouperPasswordTable, "failed_source_addresses", ddlVersionBean);
      GrouperDdlUtils.ddlutilsDropColumn(grouperPasswordTable, "failed_logins", ddlVersionBean);
      
    }
    
  }

  /**
   * 
   */
  static void addGrouperPasswordRecentlyUsedComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_6_1_addGrouperPasswordRecentlyUsedComments", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_password_recently_used", GrouperPasswordRecentlyUsed.COLUMN_ATTEMPT_MILLIS, "millis since 1970 this password was attempted");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_password_recently_used", GrouperPasswordRecentlyUsed.COLUMN_IP_ADDRESS, "ip address from where the password was attempted");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_password_recently_used", GrouperPasswordRecentlyUsed.COLUMN_STATUS, "status of the attempt. S/F/E etc");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_password_recently_used", GrouperPasswordRecentlyUsed.COLUMN_HIBERNATE_VERSION_NUMBER, "hibernate version number");

  }

  public static final String TABLE_GROUPER_PROV_ZOOM_USER = "grouper_prov_zoom_user";
  
  public static final String COLUMN_GROUPER_PROV_ZOOM_USER_CONFIG_ID = "config_id";

  public static final String COLUMN_GROUPER_PROV_ZOOM_USER_MEMBER_ID = "member_id";
  
  public static final String COLUMN_GROUPER_PROV_ZOOM_USER_ID = "id";
  
  public static final String COLUMN_GROUPER_PROV_ZOOM_USER_EMAIL = "email";
  
  public static final String COLUMN_GROUPER_PROV_ZOOM_USER_FIRST_NAME = "first_name";
  
  public static final String COLUMN_GROUPER_PROV_ZOOM_USER_LAST_NAME = "last_name";
  
  public static final String COLUMN_GROUPER_PROV_ZOOM_USER_TYPE = "type";
  
  public static final String COLUMN_GROUPER_PROV_ZOOM_USER_PMI = "pmi";
  
  public static final String COLUMN_GROUPER_PROV_ZOOM_USER_TIMEZONE = "timezone";
  
  public static final String COLUMN_GROUPER_PROV_ZOOM_USER_VERIFIED = "verified";
  
  public static final String COLUMN_GROUPER_PROV_ZOOM_USER_CREATED_AT = "created_at";
  
  public static final String COLUMN_GROUPER_PROV_ZOOM_USER_LAST_LOGIN_TIME = "last_login_time";
  
  public static final String COLUMN_GROUPER_PROV_ZOOM_USER_LANGUAGE = "language";
  
  public static final String COLUMN_GROUPER_PROV_ZOOM_USER_STATUS = "status";
  
  public static final String COLUMN_GROUPER_PROV_ZOOM_USER_ROLE_ID = "role_id";
  
  static void addGrouperProvZoomUserComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_6_1_addGrouperProvZoomUserComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_PROV_ZOOM_USER;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "table to load zoom users into a sql for reporting and deprovisioning");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ZOOM_USER_CONFIG_ID, 
        "zoom config id identifies which zoom external system is being loaded");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ZOOM_USER_MEMBER_ID,
        "If the zoom user is mapped to a Grouper subject, this is the member uuid of the subject");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        COLUMN_GROUPER_PROV_ZOOM_USER_ID, "Zoom internal ID for this user (used in web services)");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        COLUMN_GROUPER_PROV_ZOOM_USER_EMAIL, "Zoom friendly unique id for the user, also their email address");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        COLUMN_GROUPER_PROV_ZOOM_USER_FIRST_NAME, "First name of user");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        COLUMN_GROUPER_PROV_ZOOM_USER_LAST_NAME, "Last name of user");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        COLUMN_GROUPER_PROV_ZOOM_USER_TYPE, "User type is 1 for basic, 2 for licensed, and 3 for on prem, 99 for none, see Zoom docs");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        COLUMN_GROUPER_PROV_ZOOM_USER_PMI, "Zoom pmi, see zoom docs");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        COLUMN_GROUPER_PROV_ZOOM_USER_TIMEZONE, "Timezone of users in zoom");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        COLUMN_GROUPER_PROV_ZOOM_USER_VERIFIED, "If the user has been verified by zoom");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        COLUMN_GROUPER_PROV_ZOOM_USER_CREATED_AT, "When the user was created in zoom");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        COLUMN_GROUPER_PROV_ZOOM_USER_LAST_LOGIN_TIME, "When the user last logged in to zoom");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        COLUMN_GROUPER_PROV_ZOOM_USER_LANGUAGE, "Language the user uses in zoom");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        COLUMN_GROUPER_PROV_ZOOM_USER_STATUS, "Status in zoom see docs");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        COLUMN_GROUPER_PROV_ZOOM_USER_ROLE_ID, "Role ID in zoom see docs");
  
  }

  static void addGrouperProvZoomUserIndex(DdlVersionBean ddlVersionBean, Database database) {
  
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_6_1_addGrouperProvZoomUserIndex", true)) {
      return;
    }
  
    Table grouperZoomTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        TABLE_GROUPER_PROV_ZOOM_USER);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperZoomTable.getName(), 
        "grouper_zoom_user_config_id_idx", false, 
        COLUMN_GROUPER_PROV_ZOOM_USER_CONFIG_ID);

    {
      String scriptOverrideName = ddlVersionBean.isSmallIndexes() ? 
          "\nCREATE UNIQUE INDEX grouper_zoom_user_email_idx ON grouper_prod_zoom_user (email(100), config_id);\n" : null;
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ddlVersionBean, grouperZoomTable.getName(), 
          "grouper_zoom_user_email_idx", scriptOverrideName, true, COLUMN_GROUPER_PROV_ZOOM_USER_EMAIL, COLUMN_GROUPER_PROV_ZOOM_USER_CONFIG_ID);
    }
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperZoomTable.getName(), 
        "grouper_zoom_user_id_idx", true, 
        COLUMN_GROUPER_PROV_ZOOM_USER_ID, COLUMN_GROUPER_PROV_ZOOM_USER_CONFIG_ID);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperZoomTable.getName(), 
        "grouper_zoom_user_member_id_idx", false, 
        COLUMN_GROUPER_PROV_ZOOM_USER_MEMBER_ID, COLUMN_GROUPER_PROV_ZOOM_USER_CONFIG_ID);
  }

  static void addGrouperProvZoomUserTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_6_1_addGrouperProvZoomUserTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_PROV_ZOOM_USER;
  
    Table grouperFileTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ZOOM_USER_CONFIG_ID,
        Types.VARCHAR, "50", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ZOOM_USER_MEMBER_ID,
        Types.VARCHAR, "40", false, false);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ZOOM_USER_ID,
        Types.VARCHAR, "40", false, true);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ZOOM_USER_EMAIL,
        Types.VARCHAR, "200", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ZOOM_USER_FIRST_NAME, 
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ZOOM_USER_LAST_NAME, 
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ZOOM_USER_TYPE,
        Types.BIGINT, "12", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ZOOM_USER_PMI, 
        Types.BIGINT, "12", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ZOOM_USER_TIMEZONE,
        Types.VARCHAR, "100", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ZOOM_USER_VERIFIED,
        Types.BIGINT, "12", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ZOOM_USER_CREATED_AT,
        Types.BIGINT, "12", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ZOOM_USER_LAST_LOGIN_TIME,
        Types.BIGINT, "12", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ZOOM_USER_LANGUAGE,
        Types.VARCHAR, "100", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ZOOM_USER_STATUS,
        Types.BIGINT, "40", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ZOOM_USER_ROLE_ID,
        Types.BIGINT, "12", false, false);

  }
}
