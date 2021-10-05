package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.authentication.GrouperPassword;

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
}
