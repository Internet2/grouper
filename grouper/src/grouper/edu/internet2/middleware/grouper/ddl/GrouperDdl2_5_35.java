package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.commons.logging.Log;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDdl2_5_35 {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDdl2_5_35.class);
  
  /**
   * if building to this version at least
   */
  private static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V35.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V35.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }
  

  static void addGrouperSyncLogColumns(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_35_addGrouperSyncLogColumns", true)) {
      return;
    }

    Table syncLogTable = GrouperDdlUtils.ddlutilsFindTable(database, "grouper_sync_log", true);
    
    if (GrouperDdlUtils.isPostgres()) {
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncLogTable, "description_clob", Types.VARCHAR, "10000000", false, false, null);
    }
    
    if (GrouperDdlUtils.isMysql()) {
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_sync_log ADD COLUMN description_clob mediumtext;\n");
    } 
    
    if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isHsql()) {
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncLogTable, "description_clob", Types.CLOB, "10000000", false, false, null);
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncLogTable, "description_bytes", Types.BIGINT, "12", false, false, null);
    
  }
  
  /**
   * 
   */
  static void addGrouperSyncLogComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_35_addGrouperSyncLogComments", true)) {
      return;
    }
  
    final String tableName = "grouper_sync_log";
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "description_clob", "description for large data");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "description_bytes", "size of description in bytes");
    
  }
  
}
