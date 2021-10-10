package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.authentication.GrouperPassword;
import edu.internet2.middleware.grouper.authentication.GrouperPasswordRecentlyUsed;

public class GrouperDdl2_6_1 {

  /**
   * if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V38.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V38.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  static void addGrouperPasswordColumns(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_6_1_addGrouperPasswordColumns", true)) {
      return;
    }

    {
      Table grouperPasswordTable = GrouperDdlUtils.ddlutilsFindTable(database, "grouper_password", true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_EXPIRES_MILLIS, Types.BIGINT, "20", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_CREATED_MILLIS, Types.BIGINT, "20", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_MEMBER_ID_WHO_SET_PASSWORD, Types.VARCHAR, "40", false, false); 
       
    }
    
  }

  /**
   * 
   */
  static void addGrouperPasswordComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_6_1_addGrouperPasswordComments", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_password", GrouperPassword.COLUMN_EXPIRES_MILLIS, "millis since 1970 this password is going to expire");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_password", GrouperPassword.COLUMN_CREATED_MILLIS, "millis since 1970 this password was created");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_password", GrouperPassword.COLUMN_MEMBER_ID_WHO_SET_PASSWORD, "member id who set this password");

  }
  
  static void addGrouperPasswordRecentlyUsedColumns(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_6_1_addGrouperPasswordRecentlyUsedColumns", true)) {
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

    if (ddlVersionBean.didWeDoThis("v2_6_1_dropGrouperPasswordColumns", true)) {
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

    if (ddlVersionBean.didWeDoThis("v2_6_1_addGrouperPasswordRecentlyUsedComments", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_password_recently_used", GrouperPasswordRecentlyUsed.COLUMN_ATTEMPT_MILLIS, "millis since 1970 this password was attempted");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_password_recently_used", GrouperPasswordRecentlyUsed.COLUMN_IP_ADDRESS, "ip address from where the password was attempted");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_password_recently_used", GrouperPasswordRecentlyUsed.COLUMN_STATUS, "status of the attempt. S/F/E etc");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, "grouper_password_recently_used", GrouperPasswordRecentlyUsed.COLUMN_HIBERNATE_VERSION_NUMBER, "hibernate version number");

  }
}
