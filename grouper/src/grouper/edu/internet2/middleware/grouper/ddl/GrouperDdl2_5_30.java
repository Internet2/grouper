package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.commons.logging.Log;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDdl2_5_30 {

  /**
   * if building to this version at least
   */
  private static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V33.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V33.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDdl2_5_30.class);

  
  static void addConfigClobColumns(DdlVersionBean ddlVersionBean, Database database) {
  
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_5_30_addConfigClobColumns", true)) {
      return;
    }
  
    Table groupTable = GrouperDdlUtils.ddlutilsFindTable(database, GrouperConfigHibernate.TABLE_GROUPER_CONFIG, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, GrouperConfigHibernate.COLUMN_CONFIG_VALUE_CLOB_BYTES, Types.BIGINT, "12", false, false);
    
    if (GrouperDdlUtils.isOracle() || GrouperDdlUtils.isHsql()) {
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, GrouperConfigHibernate.COLUMN_CONFIG_VALUE_CLOB, Types.CLOB, null, false, false);

    } else if (GrouperDdlUtils.isMysql()) {

      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_config ADD COLUMN config_value_clob mediumtext;\n");

    } else if (GrouperDdlUtils.isPostgres()) {

      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_config ADD COLUMN config_value_clob varchar(10000000);\n");

    }
  }
  
  static void addConfigClobComments(DdlVersionBean ddlVersionBean, Database database) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_5_addConfigBlobComments", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        GrouperConfigHibernate.TABLE_GROUPER_CONFIG,
        GrouperConfigHibernate.COLUMN_CONFIG_VALUE_CLOB_BYTES, 
          "number of bytes in the CONFIG_VALUE_CLOB column");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        GrouperConfigHibernate.TABLE_GROUPER_CONFIG,
        GrouperConfigHibernate.COLUMN_CONFIG_VALUE_CLOB, 
          "value column if the length if over 3.5k");
  }

}
