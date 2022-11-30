package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

public class GrouperDdl5_0_0 {
  
  /**
   * if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V45.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V45.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  
  static void addGrouperMemberInternalIdComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperMemberInternalIdComments", true)) {
      return;
    }
  
    final String tableName = "grouper_members";
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        "internal_id", 
        "internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)");

  
  }

  static void addGrouperMemberInternalIdColumn(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperMemberInternalIdColumn", true)) {
      return;
    }
  
    Table grouperDuoTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        "grouper_members");

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDuoTable, "internal_id", Types.BIGINT, "12", false, false);
  
  }

  static void addGrouperMemberInternalIdIndex(Database database, DdlVersionBean ddlVersionBean) {
  
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperMembershipRequireIndex", true)) {
      return;
    }
  
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_members", 
        "grouper_mem_internal_id_idx", true, 
        "internal_id");
    
  }


}
