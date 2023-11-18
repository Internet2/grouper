package edu.internet2.middleware.grouper.ddl;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;

public class GrouperDdl2_6_18 {
  
  /**
   * if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V44.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V44.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }
  

  public static void attributeAssignDisallowNotNull(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    // if building from scratch its already got it
    if (buildingFromScratch(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_6_18_attributeAssignDisallowNotNull", true)) {
      return;
    }
    
    int count = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_attribute_assign where disallowed is null");
    if (count > 0) {
      ddlVersionBean.getAdditionalScripts().append(
          "update grouper_attribute_assign set disallowed='F' where disallowed is null;\n" +
          "commit;\n");
    }
    
    count = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_pit_attribute_assign where disallowed is null");
    if (count > 0) {
      ddlVersionBean.getAdditionalScripts().append(
          "update grouper_pit_attribute_assign set disallowed='F' where disallowed is null;\n" +
          "commit;\n");
    }
    
    if (GrouperDdlUtils.isPostgres()) {
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_attribute_assign ALTER COLUMN disallowed SET NOT NULL;\n");
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_attribute_assign ALTER COLUMN disallowed SET DEFAULT 'F';\n");
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_pit_attribute_assign ALTER COLUMN disallowed SET NOT NULL;\n");
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_pit_attribute_assign ALTER COLUMN disallowed SET DEFAULT 'F';\n");
    } else if (GrouperDdlUtils.isMysql()) {
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_attribute_assign MODIFY COLUMN disallowed VARCHAR(1) DEFAULT 'F' NOT NULL;\n");
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_pit_attribute_assign MODIFY COLUMN disallowed VARCHAR(1) DEFAULT 'F' NOT NULL;\n");
    } else if (GrouperDdlUtils.isOracle()) {
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_attribute_assign MODIFY disallowed VARCHAR2(1) DEFAULT 'F' NOT NULL;\n");
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_pit_attribute_assign MODIFY disallowed VARCHAR2(1) DEFAULT 'F' NOT NULL;\n");
    }
  }
}
