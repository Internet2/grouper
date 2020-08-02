package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.commons.logging.Log;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.pit.PITField;
import edu.internet2.middleware.grouper.pit.PITGrouperConfigHibernate;
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDdl2_5_34 {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDdl2_5_34.class);
  
  /**
   * if building to this version at least
   */
  private static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V34.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V34.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }
  

  static void addGrouperConfigColumns(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_34_addGrouperConfigColumns", true)) {
      return;
    }

    Table configTable = GrouperDdlUtils.ddlutilsFindTable(database, GrouperConfigHibernate.TABLE_GROUPER_CONFIG, true);
    
    if (GrouperDdlUtils.isPostgres()) {
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(configTable, GrouperConfigHibernate.COLUMN_CONFIG_VALUE_CLOB, Types.VARCHAR, "10000000", false, false, null);
    }
    
    if (GrouperDdlUtils.isMysql()) {
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_config ADD COLUMN config_value_clob mediumtext;\n");
    } 
    
    if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isHsql()) {
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(configTable, GrouperConfigHibernate.COLUMN_CONFIG_VALUE_CLOB, Types.CLOB, "10000000", false, false, null);
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(configTable, GrouperConfigHibernate.COLUMN_CONFIG_VALUE_BYTES, Types.BIGINT, "12", false, false, null);
    
  }
  
  /**
   * 
   */
  static void addGrouperConfigComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_34_addGrouperConfigComments", true)) {
      return;
    }
  
    final String tableName = "grouper_config";
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, GrouperConfigHibernate.COLUMN_CONFIG_VALUE_CLOB, "config value for large data");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, GrouperConfigHibernate.COLUMN_CONFIG_VALUE_BYTES, "size of config value in bytes");
    
  }
  
  static void addGrouperPitConfigTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_34_addGrouperPitConfigTable", true)) {
      return;
    }
    final String tableName = PITGrouperConfigHibernate.TABLE_GROUPER_PIT_CONFIG;
  
    Table grouperPitConfigTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_ID,
        Types.VARCHAR, "40", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_CONFIG_FILE_NAME,
        Types.VARCHAR, "100", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_CONFIG_KEY,
        Types.VARCHAR, "400", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_CONFIG_VALUE,
        Types.VARCHAR, "4000", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_CONFIG_COMMENT,
        Types.VARCHAR, "4000", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_CONFIG_FILE_HIERARCHY,
        Types.VARCHAR, "50", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_CONFIG_ENCRYPTED,
        Types.VARCHAR, "1", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_CONFIG_SEQUENCE, 
        Types.BIGINT, null, false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_CONFIG_VERSION_INDEX, 
        Types.BIGINT, null, false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_LAST_UPDATED, 
        Types.BIGINT, null, false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_HIBERNATE_VERSION_NUMBER, 
        Types.BIGINT, null, false, true);
    
    if (GrouperDdlUtils.isPostgres()) {
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_CONFIG_VALUE_CLOB, Types.VARCHAR, "10000000", false, false, null);
    }
    
    if (GrouperDdlUtils.isMysql()) {
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_pit_config ADD COLUMN config_value_clob mediumtext;\n");
    } 
    
    if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isHsql()) {
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_CONFIG_VALUE_CLOB, Types.CLOB, "10000000", false, false, null);
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_CONFIG_VALUE_BYTES, Types.BIGINT, "12", false, false, null);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_SOURCE_ID, Types.VARCHAR, "40", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_CONTEXT_ID, Types.VARCHAR, "40", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_ACTIVE,
        Types.VARCHAR, "1", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_START_TIME,
        Types.BIGINT, "20", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPitConfigTable, PITGrouperConfigHibernate.COLUMN_END_TIME,
        Types.BIGINT, "20", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperPitConfigTable.getName(), 
        "pit_config_context_idx", false, PITGrouperConfigHibernate.COLUMN_CONTEXT_ID);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperPitConfigTable.getName(), 
        "pit_config_source_id_idx", false, PITGrouperConfigHibernate.COLUMN_SOURCE_ID);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperPitConfigTable.getName(),
        "pit_config_start_idx", true, PITGrouperConfigHibernate.COLUMN_START_TIME, PITMembership.COLUMN_SOURCE_ID);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperPitConfigTable.getName(), 
        "pit_config_end_idx", false, PITGrouperConfigHibernate.COLUMN_END_TIME);
  }
  
  static void addGrouperPitConfigComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_34_addGrouperPitConfigComments", true)) {
      return;
    }
  
    final String tableName = PITGrouperConfigHibernate.TABLE_GROUPER_PIT_CONFIG;

    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "keeps track of grouper config.  Records are never deleted from this table");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        PITGrouperConfigHibernate.COLUMN_ID, 
        "uuid of record is unique for all records in table and primary key");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        PITGrouperConfigHibernate.COLUMN_SOURCE_ID, 
        "source_id: id of the grouper_config table");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        PITGrouperConfigHibernate.COLUMN_CONFIG_VALUE_BYTES, 
        "size of config value in bytes");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        PITGrouperConfigHibernate.COLUMN_CONFIG_VALUE_CLOB, 
        "config value for large data");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        PITGrouperConfigHibernate.COLUMN_CONFIG_FILE_NAME, 
          "Config file name of the config this record relates to, e.g. grouper.config.properties");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        PITGrouperConfigHibernate.COLUMN_CONFIG_KEY, 
          "key of the config, not including elConfig");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        PITGrouperConfigHibernate.COLUMN_CONFIG_VALUE, 
          "Value of the config");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        PITGrouperConfigHibernate.COLUMN_CONFIG_COMMENT, 
          "documentation of the config value");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        PITGrouperConfigHibernate.COLUMN_CONFIG_FILE_HIERARCHY, 
          "config file hierarchy, e.g. base, institution, or env");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        PITGrouperConfigHibernate.COLUMN_CONFIG_ENCRYPTED, 
          "if the value is encrypted");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        PITGrouperConfigHibernate.COLUMN_CONFIG_SEQUENCE, 
          "if there is more data than fits in the column this is the 0 indexed order");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        PITGrouperConfigHibernate.COLUMN_CONFIG_VERSION_INDEX, 
          "for built in configs, this is the index that will identify if the database configs should be replaced from the java code");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        PITGrouperConfigHibernate.COLUMN_LAST_UPDATED, 
          "when this record was inserted or last updated");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,
        PITGrouperConfigHibernate.COLUMN_HIBERNATE_VERSION_NUMBER, 
          "hibernate uses this to version rows");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,  
        PITGrouperConfigHibernate.COLUMN_ACTIVE, 
          "T or F if this is an active record based on start and end dates");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,  
        PITGrouperConfigHibernate.COLUMN_START_TIME, 
          "millis from 1970 when this record was inserted");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,  
        PITGrouperConfigHibernate.COLUMN_END_TIME, 
          "millis from 1970 when this record was deleted");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName,  
        PITGrouperConfigHibernate.COLUMN_CONTEXT_ID, 
        "Context id links together audit entry with the row");
    
    
  }
  
}
