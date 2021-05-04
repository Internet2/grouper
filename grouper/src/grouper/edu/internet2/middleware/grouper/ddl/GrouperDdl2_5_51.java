package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

public class GrouperDdl2_5_51 {

  /**
   * if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V37.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V37.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  static void addGrouperSyncGroupMetadataJsonColumn(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_51_addGrouperSyncGroupMetadataJsonColumn", true)) {
      return;
    }

    {
      Table syncGroupTable = GrouperDdlUtils.ddlutilsFindTable(database, "grouper_sync_group", true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncGroupTable, "metadata_json", Types.VARCHAR, "4000", false, false, null);
    }
    
  }

  private final static String comment = "additional metadata for group";
  
  /**
   * 
   */
  static void addGrouperSyncGroupMetadataJsonComment(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_51_addGrouperSyncGroupMetadataJsonComment", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_sync_group", "metadata_json", comment);

  }
}
