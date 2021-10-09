package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.authentication.GrouperPasswordRecentlyUsed;
import edu.internet2.middleware.grouper.pit.PITGrouperConfigHibernate;

public class GrouperDdl2_6_2 {

  /**
   * if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V39.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V39.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  static void addGrouperPasswordRecentlyUsedColumns(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_6_2_addGrouperPasswordRecentlyUsedColumns", true)) {
      return;
    }

    {
      Table grouperPasswordRecentlyUsedTable = GrouperDdlUtils.ddlutilsFindTable(database, "grouper_password_recently_used", true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordRecentlyUsedTable, GrouperPasswordRecentlyUsed.COLUMN_ATTEMPT_MILLIS, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordRecentlyUsedTable, GrouperPasswordRecentlyUsed.COLUMN_IP_ADDRESS, Types.VARCHAR, "20", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordRecentlyUsedTable, GrouperPasswordRecentlyUsed.COLUMN_STATUS, Types.CHAR, "1", false, false); 
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordRecentlyUsedTable, GrouperPasswordRecentlyUsed.COLUMN_HIBERNATE_VERSION_NUMBER, Types.BIGINT, null, false, true);
      
    }
    
  }
  
  static void dropGrouperPasswordColumns(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_6_2_dropGrouperPasswordColumns", true)) {
      return;
    }

    {
      Table grouperPasswordTable = GrouperDdlUtils.ddlutilsFindTable(database, "grouper_password", true);
      
      GrouperDdlUtils.ddlutilsDropColumn(grouperPasswordTable, "recent_source_addresses", ddlVersionBean);
      GrouperDdlUtils.ddlutilsDropColumn(grouperPasswordTable, "failed_source_addresses", ddlVersionBean);
      GrouperDdlUtils.ddlutilsDropColumn(grouperPasswordTable, "failed_logins", ddlVersionBean);
      
    }
    
  }

  /**
   * 
   */
  static void addGrouperPasswordRecentlyUsedComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_6_2_addGrouperPasswordRecentlyUsedComments", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_password_recently_used", GrouperPasswordRecentlyUsed.COLUMN_ATTEMPT_MILLIS, "millis since 1970 this password was attempted");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_password_recently_used", GrouperPasswordRecentlyUsed.COLUMN_IP_ADDRESS, "ip address from where the password was attempted");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_password_recently_used", GrouperPasswordRecentlyUsed.COLUMN_STATUS, "status of the attempt. S/F/E etc");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_password_recently_used", GrouperPasswordRecentlyUsed.COLUMN_HIBERNATE_VERSION_NUMBER, "hibernate version number");

  }
}
