package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

public class GrouperDdl5_12_0 {
  
  /**
   * if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    //TODO increment this in v5
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
    
    //TODO increment this in v5
    boolean buildingToPreviousVersion = GrouperDdl.V47.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }
  
  public static final String TABLE_GROUPER_PROV_AZURE_USER = "grouper_prov_azure_user";
  
  public static final String COLUMN_GROUPER_PROV_AZURE_USER_CONFIG_ID = "config_id";

  public static final String COLUMN_GROUPER_PROV_AZURE_USER_ACCOUNT_ENABLED = "account_enabled";

  public static final String COLUMN_GROUPER_PROV_AZURE_USER_DISPLAY_NAME = "display_name";
  
  public static final String COLUMN_GROUPER_PROV_AZURE_USER_ID = "id";
  
  public static final String COLUMN_GROUPER_PROV_AZURE_USER_MAIL_NICKNAME = "mail_nickname";

  public static final String COLUMN_GROUPER_PROV_AZURE_USER_ON_PREMISES_IMMUTABLE_ID = "on_premises_immutable_id";

  public static final String COLUMN_GROUPER_PROV_AZURE_USER_PRINCIPAL_NAME = "user_principal_name";
  
  static void addGrouperProvAzureUserComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_5_addGrouperProvAzureUserComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_PROV_AZURE_USER;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "table to load azuer users into a sql for reporting, provisioning, and deprovisioning");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_AZURE_USER_CONFIG_ID, 
        "azure config id identifies which azure external system is being loaded");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_AZURE_USER_ID, 
        "Azure internal ID for this user (used in web services)");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_AZURE_USER_ACCOUNT_ENABLED, 
        "Is account enabled");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_AZURE_USER_DISPLAY_NAME, 
        "display name for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
            tableName, 
            COLUMN_GROUPER_PROV_AZURE_USER_MAIL_NICKNAME, 
            "mail nickname for the user");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
            tableName, 
            COLUMN_GROUPER_PROV_AZURE_USER_ON_PREMISES_IMMUTABLE_ID, 
            "on premises immutable id");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
            tableName, 
            COLUMN_GROUPER_PROV_AZURE_USER_PRINCIPAL_NAME, 
            "user principal name");
  }
  
  static void addGrouperProvAzureUserIndex(DdlVersionBean ddlVersionBean, Database database) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v5_0_5_addGrouperProvAzureUserIndex", true)) {
      return;
    }
  
    Table grouperDuoTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        TABLE_GROUPER_PROV_AZURE_USER);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperDuoTable.getName(), 
        "grouper_prov_azure_user_idx1", true, 
        COLUMN_GROUPER_PROV_AZURE_USER_PRINCIPAL_NAME, COLUMN_GROUPER_PROV_AZURE_USER_CONFIG_ID);

  }

  static void addGrouperProvAzureUserTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_5_addGrouperProvAzureUserTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_PROV_AZURE_USER;
  
    Table grouperFileTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_AZURE_USER_CONFIG_ID,
        Types.VARCHAR, "50", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_AZURE_USER_ID,
        Types.VARCHAR, "256", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_AZURE_USER_ACCOUNT_ENABLED,
        Types.VARCHAR, "1", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_AZURE_USER_DISPLAY_NAME,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_AZURE_USER_MAIL_NICKNAME,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_AZURE_USER_ON_PREMISES_IMMUTABLE_ID,
        Types.VARCHAR, "256", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_AZURE_USER_PRINCIPAL_NAME,
        Types.VARCHAR, "256", false, false);
    
  }
  
}
