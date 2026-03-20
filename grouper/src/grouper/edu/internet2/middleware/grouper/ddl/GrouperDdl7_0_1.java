package edu.internet2.middleware.grouper.ddl;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Index;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

/**
 * DDL for removing grouper_duo_user_user_name_idx index.
 */
public class GrouperDdl7_0_1 {

  /**
   * if building to this version at least
   * @param ddlVersionBean
   * @return true if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    boolean buildingToThisVersionAtLeast = GrouperDdl.V47.getVersion() <= buildingToVersion;
    return buildingToThisVersionAtLeast;
  }

  /**
   * if building from scratch
   * @param ddlVersionBean
   * @return true if building from scratch
   */
  static boolean buildingFromScratch(DdlVersionBean ddlVersionBean) {
    int buildingFromVersion = ddlVersionBean.getBuildingFromVersion();
    if (buildingFromVersion <= 0) {
      return true;
    }
    return false;
  }

  /**
   * remove grouper_duo_user_user_name_idx if it exists
   * @param database
   * @param ddlVersionBean
   */
  static void removeGrouperDuoUserUserNameIndex(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (buildingFromScratch(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v7_0_1_removeGrouperDuoUserUserNameIndex", true)) {
      return;
    }

    Index index = GrouperDdlUtils.ddlutilsFindIndex(database,
        GrouperDdl2_6_8.TABLE_GROUPER_PROV_DUO_USER, "grouper_duo_user_user_name_idx");

    if (index != null) {
      Table table = GrouperDdlUtils.ddlutilsFindTable(database,
          GrouperDdl2_6_8.TABLE_GROUPER_PROV_DUO_USER, true);
      table.removeIndex(index);
    }
  }
}
