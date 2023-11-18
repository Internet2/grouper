package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

public class GrouperDdl2_6_14 {
  
  /**
   * if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V42.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V42.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  static void addGrouperLoaderColumns(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_6_14_addGrouperLoaderColumns", true)) {
      return;
    }

    Table grouperLoaderLogTable = GrouperDdlUtils.ddlutilsFindTable(database, Hib3GrouperLoaderLog.TABLE_GROUPER_LOADER_LOG, true);
    
    if (GrouperDdlUtils.isPostgres()) {
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperLoaderLogTable, Hib3GrouperLoaderLog.COLUMN_JOB_MESSAGE_CLOB, Types.VARCHAR, "10000000", false, false, null);
    }
    
    if (GrouperDdlUtils.isMysql()) {
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_loader_log ADD COLUMN job_message_clob MEDIUMTEXT;\n");
    }
    
    if (GrouperDdlUtils.isOracle()) {
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperLoaderLogTable, Hib3GrouperLoaderLog.COLUMN_JOB_MESSAGE_CLOB, Types.CLOB, "10000000", false, false, null);
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperLoaderLogTable, Hib3GrouperLoaderLog.COLUMN_JOB_MESSAGE_BYTES, Types.BIGINT, "12", false, false, null);
    
  }
  
  /**
   * 
   */
  static void addGrouperLoaderComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_6_14_addGrouperLoaderComments", true)) {
      return;
    }
  
    final String tableName = "grouper_loader_log";
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, Hib3GrouperLoaderLog.COLUMN_JOB_MESSAGE_CLOB, "Could be a status or error message or stack (over 3800 bytes)");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, Hib3GrouperLoaderLog.COLUMN_JOB_MESSAGE_BYTES, "Number of bytes in the job message");
    
  }
  

}
