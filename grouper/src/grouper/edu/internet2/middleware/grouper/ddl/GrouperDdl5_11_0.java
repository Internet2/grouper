package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

public class GrouperDdl5_11_0 {
  
  /**
   * if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
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
    
    boolean buildingToPreviousVersion = GrouperDdl.V48.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  public static final String TABLE_GROUPER_PROV_SCIM_USER = "grouper_prov_scim_user";
  
  public static final String COLUMN_GROUPER_PROV_SCIM_USER_CONFIG_ID = "config_id";

  public static final String COLUMN_GROUPER_PROV_SCIM_USER_ACTIVE = "active";

  public static final String COLUMN_GROUPER_PROV_SCIM_USER_COST_CENTER = "cost_center";
  
  public static final String COLUMN_GROUPER_PROV_SCIM_USER_DEPARTMENT = "department";
  
  public static final String COLUMN_GROUPER_PROV_SCIM_USER_DISPLAY_NAME = "display_name";

  public static final String COLUMN_GROUPER_PROV_SCIM_USER_DIVISION = "division";

  public static final String COLUMN_GROUPER_PROV_SCIM_USER_EMAIL_TYPE = "email_type";
  
  public static final String COLUMN_GROUPER_PROV_SCIM_USER_EMAIL_VALUE = "email_value";
  
  public static final String COLUMN_GROUPER_PROV_SCIM_USER_EMAIL_TYPE2 = "email_type2";

  public static final String COLUMN_GROUPER_PROV_SCIM_USER_EMAIL_VALUE2 = "email_value2";
  
  public static final String COLUMN_GROUPER_PROV_SCIM_USER_EMPLOYEE_NUMBER = "employee_number";

  public static final String COLUMN_GROUPER_PROV_SCIM_USER_EXTERNAL_ID = "external_id";

  public static final String COLUMN_GROUPER_PROV_SCIM_USER_FAMILY_NAME = "family_name";

  public static final String COLUMN_GROUPER_PROV_SCIM_USER_FORMATTED_NAME = "formatted_name";

  public static final String COLUMN_GROUPER_PROV_SCIM_USER_GIVEN_NAME = "given_name";
  
  public static final String COLUMN_GROUPER_PROV_SCIM_USER_ID = "id";

  public static final String COLUMN_GROUPER_PROV_SCIM_USER_MIDDLE_NAME = "middle_name";

  public static final String COLUMN_GROUPER_PROV_SCIM_USER_PHONE_NUMBER = "phone_number";
  
  public static final String COLUMN_GROUPER_PROV_SCIM_USER_PHONE_NUMBER_TYPE = "phone_number_type";
  
  public static final String COLUMN_GROUPER_PROV_SCIM_USER_PHONE_NUMBER2 = "phone_number2";
  
  public static final String COLUMN_GROUPER_PROV_SCIM_USER_PHONE_NUMBER_TYPE2 = "phone_number_type2";
  
  public static final String COLUMN_GROUPER_PROV_SCIM_USER_SCHEMAS = "schemas";

  public static final String COLUMN_GROUPER_PROV_SCIM_USER_TITLE = "title";

  public static final String COLUMN_GROUPER_PROV_SCIM_USER_USER_NAME = "user_name";

  public static final String COLUMN_GROUPER_PROV_SCIM_USER_USER_TYPE = "user_type";
  
  
  public static final String TABLE_GROUPER_PROV_SCIM_USER_ATTR = "grouper_prov_scim_user_attr";
  
  public static final String COLUMN_GROUPER_PROV_SCIM_USER_ATTR_CONFIG_ID = "config_id";
  public static final String COLUMN_GROUPER_PROV_SCIM_USER_ATTR_ID = "id";
  public static final String COLUMN_GROUPER_PROV_SCIM_USER_ATTR_ATTR_NAME = "attribute_name";
  public static final String COLUMN_GROUPER_PROV_SCIM_USER_ATTR_ATTR_VALUE = "attribute_value";
  
  static void addGrouperProvScimUserComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_5_addGrouperProvScimUserComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_PROV_SCIM_USER;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "table to load scim users into a sql for reporting, provisioning, and deprovisioning");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_CONFIG_ID, 
        "scim config id identifies which scim external system is being loaded");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_ID, 
        "scim internal ID for this user (used in web services)");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_ACTIVE, 
        "Is user active");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_COST_CENTER, 
        "cost center for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_DEPARTMENT, 
        "department for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_DISPLAY_NAME, 
        "display name for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_EMAIL_TYPE, 
        "email type for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_EMAIL_VALUE, 
        "email value for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_EMAIL_TYPE2, 
        "email type2 for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_EMAIL_VALUE2, 
        "email value2 for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_EMPLOYEE_NUMBER, 
        "employee number for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_EXTERNAL_ID, 
        "external id for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_FAMILY_NAME, 
        "family name for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_FORMATTED_NAME, 
        "formatted name for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_GIVEN_NAME, 
        "given name for the user");
    
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_MIDDLE_NAME, 
        "middle name for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_PHONE_NUMBER, 
        "phone number for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_PHONE_NUMBER_TYPE, 
        "phone number type for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_PHONE_NUMBER2, 
        "phone number2 for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_PHONE_NUMBER_TYPE2, 
        "phone number type2 for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_SCHEMAS, 
        "schemas for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_TITLE, 
        "title for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_USER_NAME, 
        "user name for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_USER_TYPE, 
        "user type for the user");
    
  }
  
  static void addGrouperProvScimUserAttributeComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_5_addGrouperProvScimUserAttributeComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_PROV_SCIM_USER_ATTR;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "table to load scim user attributes into a sql for reporting, provisioning, and deprovisioning");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_ATTR_CONFIG_ID, 
        "scim config id identifies which scim external system is being loaded");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_ATTR_ID, 
        "scim internal ID for this user (used in web services)");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_ATTR_ATTR_NAME, 
        "scim user attribute name");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_SCIM_USER_ATTR_ATTR_VALUE, 
        "scim user attribute value");
    
  }

  static void addGrouperProvScimUserIndex(DdlVersionBean ddlVersionBean, Database database) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v5_0_5_addGrouperProvScimUserIndex", true)) {
      return;
    }
  
    Table grouperDuoTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        TABLE_GROUPER_PROV_SCIM_USER);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperDuoTable.getName(), 
        "grouper_prov_scim_user_idx1", false, COLUMN_GROUPER_PROV_SCIM_USER_EMAIL_VALUE,
        COLUMN_GROUPER_PROV_SCIM_USER_CONFIG_ID);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperDuoTable.getName(), 
        "grouper_prov_scim_user_idx2", true, 
        COLUMN_GROUPER_PROV_SCIM_USER_USER_NAME, COLUMN_GROUPER_PROV_SCIM_USER_CONFIG_ID);

  }
  
  static void addGrouperProvScimUserAttributeIndex(DdlVersionBean ddlVersionBean, Database database) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v5_0_5_addGrouperProvScimUserAttributeIndex", true)) {
      return;
    }
  
    Table grouperScimUserAttrTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        TABLE_GROUPER_PROV_SCIM_USER_ATTR);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperScimUserAttrTable.getName(), 
        "grouper_prov_scim_usat_idx1", false, 
        COLUMN_GROUPER_PROV_SCIM_USER_ATTR_CONFIG_ID, COLUMN_GROUPER_PROV_SCIM_USER_ATTR_ATTR_NAME);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperScimUserAttrTable.getName(), 
        "grouper_prov_scim_usat_idx2", true, 
        COLUMN_GROUPER_PROV_SCIM_USER_ATTR_ID, COLUMN_GROUPER_PROV_SCIM_USER_ATTR_CONFIG_ID, COLUMN_GROUPER_PROV_SCIM_USER_ATTR_ATTR_VALUE);

  }

  static void addGrouperProvScimUserTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_5_addGrouperProvScimUserTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_PROV_SCIM_USER;
  
    Table grouperFileTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_CONFIG_ID,
        Types.VARCHAR, "50", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_ID,
        Types.VARCHAR, "256", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_ACTIVE,
        Types.VARCHAR, "1", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_COST_CENTER,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_DEPARTMENT,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_DISPLAY_NAME,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_DIVISION,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_EMAIL_TYPE,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_EMAIL_VALUE,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_EMAIL_TYPE2,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_EMAIL_VALUE2,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_EMPLOYEE_NUMBER,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_EXTERNAL_ID,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_FAMILY_NAME,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_FORMATTED_NAME,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_GIVEN_NAME,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_MIDDLE_NAME,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_PHONE_NUMBER,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_PHONE_NUMBER_TYPE,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_PHONE_NUMBER2,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_PHONE_NUMBER_TYPE2,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_SCHEMAS,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_TITLE,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_USER_NAME,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_USER_TYPE,
        Types.VARCHAR, "256", false, false);
    
  }
  
  static void addGrouperProvScimUserAttributeTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_5_addGrouperProvScimUserAttributeTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_PROV_SCIM_USER_ATTR;
  
    Table grouperFileTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_ATTR_CONFIG_ID,
        Types.VARCHAR, "50", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_ATTR_ID,
        Types.VARCHAR, "256", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_ATTR_ATTR_NAME,
        Types.VARCHAR, "256", true, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_SCIM_USER_ATTR_ATTR_VALUE,
        Types.VARCHAR, "4000", true, false);
    
  }

}
