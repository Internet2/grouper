package edu.internet2.middleware.grouper.ddl;

import org.apache.commons.logging.Log;
import org.apache.ddlutils.model.Database;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDdl2_6_6 {

  /**
   * if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V40.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V40.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDdl2_6_6.class);

  static void fixGrouperMembersColumnPostgres(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_6_6_fixGrouperMembersColumnPostgres", true)) {
      return;
    }

    if (GrouperDdlUtils.isPostgres() && !buildingFromScratch(ddlVersionBean)) {

      boolean needUpdate = false;

      try {
        int count = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_members where subject_resolution_eligible is null");
        if (count > 0) {
          needUpdate = true;
        }
      } catch (Exception e) {
        needUpdate = false;
        LOG.info("Exception querying grouper_members", e);
        // group table doesnt exist?
      }

      if (needUpdate) {
        ddlVersionBean.getAdditionalScripts().append(
            "update grouper_members set subject_resolution_eligible='T' where subject_resolution_eligible is null;\n" +
            "commit;\n");
      }

      ddlVersionBean.getAdditionalScripts().append(          
          "ALTER TABLE " + Member.TABLE_GROUPER_MEMBERS + " ALTER COLUMN " + Member.COLUMN_SUBJECT_RESOLUTION_ELIGIBLE + " SET NOT NULL;\n");
      ddlVersionBean.getAdditionalScripts().append(          
          "ALTER TABLE " + Member.TABLE_GROUPER_MEMBERS + " ALTER COLUMN " + Member.COLUMN_SUBJECT_RESOLUTION_ELIGIBLE + " SET DEFAULT 'T';\n");
    }
  }
}
