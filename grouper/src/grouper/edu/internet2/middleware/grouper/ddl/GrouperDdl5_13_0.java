package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

public class GrouperDdl5_13_0 {
  
  /**
   * if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    //TODO increment this in v5
    boolean buildingToThisVersionAtLeast = GrouperDdl.V48.getVersion() <= buildingToVersion;

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
    
    //TODO increment this in v5
    boolean buildingToPreviousVersion = GrouperDdl.V48.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }
  
  public static final String TABLE_GROUPER_PROV_ADOBE_USER = "grouper_prov_adobe_user";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_CONFIG_ID = "config_id";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_USER_ID = "user_id";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_EMAIL = "email";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_USER_NAME = "user_name";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_STATUS = "status";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_TYPE= "type";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_FIRST_NAME= "first_name";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_LAST_NAME= "last_name";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_DOMAIN= "domain";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_COUNTRY= "country";

  
  public static final String TABLE_GROUPER_PROV_ADOBE_GROUP = "grouper_prov_adobe_group";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_GROUP_CONFIG_ID = "config_id";

  public static final String COLUMN_GROUPER_PROV_ADOBE_GROUP_GROUP_ID = "group_id";

  public static final String COLUMN_GROUPER_PROV_ADOBE_GROUP_NAME = "name";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_GROUP_TYPE = "type";

  public static final String COLUMN_GROUPER_PROV_ADOBE_GROUP_PRODUCT_NAME = "product_name";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_GROUP_MEMBER_COUNT = "member_count";

  public static final String COLUMN_GROUPER_PROV_ADOBE_GROUP_LICENSE_QUOTA = "license_quota";
  
  
  
  public static final String TABLE_GROUPER_PROV_ADOBE_MEMBERSHIP = "grouper_prov_adobe_membership";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_MEMBERSHIP_CONFIG_ID = "config_id";

  public static final String COLUMN_GROUPER_PROV_ADOBE_MEMBERSHIP_GROUP_ID = "group_id";

  public static final String COLUMN_GROUPER_PROV_ADOBE_MEMBERSHIP_USER_ID = "user_id";
  
  
  static void addGrouperProvAdobeUserComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_5_addGrouperProvAdobeUserComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_PROV_ADOBE_USER;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "table to load adobe users into a sql for reporting, provisioning, and deprovisioning");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_CONFIG_ID, 
        "adobe config id identifies which adobe external system is being loaded");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_CONFIG_ID, 
        "Adobe internal ID for this user (used in web services)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_EMAIL, 
        "email address for the user");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_USER_NAME, 
        "user name for the user");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_STATUS, 
        "user status");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_TYPE, 
        "user type");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_FIRST_NAME, 
        "user first name");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_LAST_NAME,
        "user last name");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_DOMAIN,
        "user domain");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_COUNTRY,
        "user country");
  }
  
  static void addGrouperProvAdobeGroupComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_5_addGrouperProvAdobeGroupComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_PROV_ADOBE_GROUP;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "table to load adobe groups into a sql for reporting, provisioning, and deprovisioning");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_GROUP_CONFIG_ID, 
        "adobe config id identifies which adobe external system is being loaded");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_GROUP_GROUP_ID, 
        "Adobe internal ID for this group (used in web services)");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_GROUP_NAME, 
        "Group name");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_GROUP_TYPE, 
        "Group type");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_GROUP_PRODUCT_NAME, 
        "Group product name");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_GROUP_MEMBER_COUNT, 
        "member count");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_GROUP_LICENSE_QUOTA, 
        "license quota");
    
  }
  
  static void addGrouperProvAdobeMembershipComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_5_addGrouperProvAdobeMembershipComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_PROV_ADOBE_MEMBERSHIP;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "table to load adobe memberships into a sql for reporting, provisioning, and deprovisioning");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_MEMBERSHIP_CONFIG_ID, 
        "adobe config id identifies which adobe external system is being loaded");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_MEMBERSHIP_GROUP_ID, 
        "Membership group id");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_MEMBERSHIP_USER_ID, 
        "Membership user id");
    
  }
  
  static void addGrouperProvAdobeUserIndex(DdlVersionBean ddlVersionBean, Database database) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v5_0_5_addGrouperProvAdobeUserIndex", true)) {
      return;
    }
  
    Table grouperDuoTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        TABLE_GROUPER_PROV_ADOBE_USER);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperDuoTable.getName(), 
        "grouper_prov_adobe_user_idx1", true, 
        COLUMN_GROUPER_PROV_ADOBE_USER_EMAIL, COLUMN_GROUPER_PROV_ADOBE_USER_CONFIG_ID);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperDuoTable.getName(), 
        "grouper_prov_adobe_user_idx2", true, 
        COLUMN_GROUPER_PROV_ADOBE_USER_USER_NAME, COLUMN_GROUPER_PROV_ADOBE_USER_CONFIG_ID);
  }
  
  static void addGrouperProvAdobeGroupIndex(DdlVersionBean ddlVersionBean, Database database) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v5_0_5_addGrouperProvAdobeGroupIndex", true)) {
      return;
    }
  
    Table grouperDuoTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        TABLE_GROUPER_PROV_ADOBE_GROUP);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperDuoTable.getName(), 
        "grouper_prov_adobe_group_idx1", true, 
        COLUMN_GROUPER_PROV_ADOBE_GROUP_GROUP_ID, COLUMN_GROUPER_PROV_ADOBE_GROUP_CONFIG_ID);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperDuoTable.getName(), 
        "grouper_prov_adobe_group_idx2", true, 
        COLUMN_GROUPER_PROV_ADOBE_GROUP_NAME, COLUMN_GROUPER_PROV_ADOBE_GROUP_CONFIG_ID);
  }
  
  static void addGrouperProvAdobeMembershipIndex(DdlVersionBean ddlVersionBean, Database database) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v5_0_5_addGrouperProvAdobeMembershipIndex", true)) {
      return;
    }
  
    Table grouperDuoTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        TABLE_GROUPER_PROV_ADOBE_MEMBERSHIP);
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, grouperDuoTable.getName(), 
        "grouper_prov_adobe_membership_fk1", "grouper_prov_adobe_group",
        "group_id", "group_id");
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, grouperDuoTable.getName(), 
        "grouper_prov_adobe_membership_fk2", "grouper_prov_adobe_user",
        "user_id", "user_id");
    
  }

  static void addGrouperProvAdobeUserTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_5_addGrouperProvAdobeUserTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_PROV_ADOBE_USER;
  
    Table grouperFileTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_CONFIG_ID,
        Types.VARCHAR, "100", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_USER_ID,
        Types.VARCHAR, "100", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_EMAIL,
        Types.VARCHAR, "256", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_USER_NAME,
        Types.VARCHAR, "256", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_STATUS,
        Types.VARCHAR, "30", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_TYPE,
        Types.VARCHAR, "30", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_FIRST_NAME,
        Types.VARCHAR, "100", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_LAST_NAME,
        Types.VARCHAR, "100", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_DOMAIN,
        Types.VARCHAR, "100", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_COUNTRY,
        Types.VARCHAR, "2", false, false);
    
  }
  
  static void addGrouperProvAdobeGroupTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_5_addGrouperProvAdobeGroupTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_PROV_ADOBE_GROUP;
  
    Table grouperFileTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_GROUP_CONFIG_ID,
        Types.VARCHAR, "100", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_GROUP_GROUP_ID,
        Types.BIGINT, "20", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_GROUP_NAME,
        Types.VARCHAR, "2000", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_GROUP_TYPE,
        Types.VARCHAR, "100", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_GROUP_PRODUCT_NAME,
        Types.VARCHAR, "2000", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_GROUP_MEMBER_COUNT,
        Types.BIGINT, "20", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_GROUP_LICENSE_QUOTA,
        Types.BIGINT, "20", false, false);
    
  }
  
  static void addGrouperProvAdobeMembershipTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_5_addGrouperProvAdobeMembershipTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_PROV_ADOBE_MEMBERSHIP;
  
    Table grouperFileTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_MEMBERSHIP_CONFIG_ID,
        Types.VARCHAR, "100", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_MEMBERSHIP_GROUP_ID,
        Types.BIGINT, "20", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_MEMBERSHIP_USER_ID,
        Types.VARCHAR, "100", true, true);
    
  }
  
}
