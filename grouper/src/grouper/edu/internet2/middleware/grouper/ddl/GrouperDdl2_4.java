package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.pit.PITMember;

public class GrouperDdl2_4 {

  /**
   * if building to this version at least
   */
  private static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V31.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V31.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  static void addMembersTableIndifier0Index(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_4_addMembersTableIndifier0Index", true)) {
      return;
    }
    
    Table membersTable = GrouperDdlUtils.ddlutilsFindTable(database, Member.TABLE_GROUPER_MEMBERS, true);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, membersTable.getName(), 
        "member_subjidentifier0_idx", false, "subject_identifier0");
    
  }

  static void addPitMembersTableIndifier0Index(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_4_addPitMembersTableIndifier0Index", true)) {
      return;
    }
    
    Table pitMembersTable = GrouperDdlUtils.ddlutilsFindTable(database, PITMember.TABLE_GROUPER_PIT_MEMBERS, true);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, pitMembersTable.getName(), 
        "pit_member_subjidentifier0_idx", false, "subject_identifier0");
    
  }

  static void addChangeLogEntryTempIndex(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_4_addChangeLogEntryTempIndex", true)) {
      return;
    }
    
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ChangeLogEntry.TABLE_GROUPER_CHANGE_LOG_ENTRY_TEMP,
        "change_log_temp_created_on_idx", false, "created_on");
    

    
  }
  
  static void addConfigurationIndexes(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_4_addConfigurationIndexes", true)) {
      return;
    }

    Table configTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        GrouperConfigHibernate.TABLE_GROUPER_CONFIG);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, configTable.getName(), 
        "grpconfig_config_file_idx", false, 
        GrouperConfigHibernate.COLUMN_CONFIG_FILE_NAME, GrouperConfigHibernate.COLUMN_LAST_UPDATED);
    
    {
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, configTable.getName(), 
          "grpconfig_config_key_idx", false, GrouperConfigHibernate.COLUMN_CONFIG_KEY+"(100)", GrouperConfigHibernate.COLUMN_CONFIG_FILE_NAME+"(50)");
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, configTable.getName(), 
        "grpconfig_last_updated_idx", false, GrouperConfigHibernate.COLUMN_LAST_UPDATED);

    {
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, configTable.getName(), 
          "grpconfig_unique_idx", true, GrouperConfigHibernate.COLUMN_CONFIG_FILE_NAME+"(20)",
          GrouperConfigHibernate.COLUMN_CONFIG_FILE_HIERARCHY+"(20)", GrouperConfigHibernate.COLUMN_CONFIG_KEY+"(100)",
          GrouperConfigHibernate.COLUMN_CONFIG_SEQUENCE);
    }
    
  }
  
  static void addConfigurationTables(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_4_addConfigurationTables", true)) {
      return;
    }
    
    Table configTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        GrouperConfigHibernate.TABLE_GROUPER_CONFIG);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(configTable, GrouperConfigHibernate.COLUMN_ID,
        Types.VARCHAR, "40", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(configTable, GrouperConfigHibernate.COLUMN_CONFIG_FILE_NAME,
        Types.VARCHAR, "100", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(configTable, GrouperConfigHibernate.COLUMN_CONFIG_KEY,
        Types.VARCHAR, "400", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(configTable, GrouperConfigHibernate.COLUMN_CONFIG_VALUE,
        Types.VARCHAR, "4000", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(configTable, GrouperConfigHibernate.COLUMN_CONFIG_COMMENT,
        Types.VARCHAR, "4000", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(configTable, GrouperConfigHibernate.COLUMN_CONFIG_FILE_HIERARCHY,
        Types.VARCHAR, "50", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(configTable, GrouperConfigHibernate.COLUMN_CONFIG_ENCRYPTED,
        Types.VARCHAR, "1", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(configTable, GrouperConfigHibernate.COLUMN_CONFIG_SEQUENCE, 
        Types.BIGINT, null, false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(configTable, GrouperConfigHibernate.COLUMN_CONFIG_VERSION_INDEX, 
        Types.BIGINT, null, false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(configTable, GrouperConfigHibernate.COLUMN_LAST_UPDATED, 
        Types.BIGINT, null, false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(configTable, GrouperConfigHibernate.COLUMN_HIBERNATE_VERSION_NUMBER, 
        Types.BIGINT, null, false, true);

    
  }
  
  
}
