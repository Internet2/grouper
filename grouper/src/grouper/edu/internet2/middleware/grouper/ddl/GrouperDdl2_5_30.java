package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.commons.logging.Log;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDdl2_5_30 {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDdl2_5_30.class);
  
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
  @SuppressWarnings("unused")
  private static boolean buildingToPreviousVersion(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToPreviousVersion = GrouperDdl.V33.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }
  

  static void addSubjectResolutionColumns(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_30_addSubjectResolutionColumns", true)) {
      return;
    }

    Table memberTable = GrouperDdlUtils.ddlutilsFindTable(database, Member.TABLE_GROUPER_MEMBERS, true);
    
    
    if (buildingFromScratch(ddlVersionBean)) {
      
      //this is required if the member table is new    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(memberTable, Member.COLUMN_SUBJECT_RESOLUTION_DELETED, Types.VARCHAR, "1", false, true, "F");
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(memberTable, Member.COLUMN_SUBJECT_RESOLUTION_RESOLVABLE, Types.VARCHAR, "1", false, true, "T");
    } else {
      if (!GrouperDdlUtils.isPostgres()) {
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(memberTable, Member.COLUMN_SUBJECT_RESOLUTION_DELETED, Types.VARCHAR, "1", false, false, "F");
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(memberTable, Member.COLUMN_SUBJECT_RESOLUTION_RESOLVABLE, Types.VARCHAR, "1", false, false, "T");        
      }
    }

    // just do nothing if there is no upgrade.  i.e. the database already has this
    if (!buildingFromScratch(ddlVersionBean) && GrouperDdlUtils.isPostgres() && ddlVersionBean.getBuildingFromVersion() < GrouperDdl.V32.getVersion()) {
      
      // this will recreate the grouper_groups table in postgres on an existing installation if you dont do this
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_members ADD COLUMN subject_resolution_resolvable VARCHAR(1);\n");
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_members ADD COLUMN subject_resolution_deleted VARCHAR(1);\n");
      ddlVersionBean.getAdditionalScripts().append("CREATE INDEX member_resolvable_idx ON grouper_members (subject_resolution_resolvable);\n");
      ddlVersionBean.getAdditionalScripts().append("CREATE INDEX member_deleted_idx ON grouper_members (subject_resolution_deleted);\n");
      
    } else {
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, memberTable.getName(), "member_resolvable_idx", false, Member.COLUMN_SUBJECT_RESOLUTION_RESOLVABLE);       
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, memberTable.getName(), "member_deleted_idx", false, Member.COLUMN_SUBJECT_RESOLUTION_DELETED);       
    }
    
    if (!buildingFromScratch(ddlVersionBean)) {
      boolean needUpdate = false;
      
      try {
        int count = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_members");
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
            "update grouper_members set subject_resolution_resolvable='T' where subject_resolution_resolvable is null;\n" +
            "update grouper_members set subject_resolution_deleted='F' where subject_resolution_deleted is null;\n" +
            "commit;\n");
      }
    }
    
    if (!buildingFromScratch(ddlVersionBean) && GrouperDdlUtils.isPostgres()) {
      ddlVersionBean.getAdditionalScripts().append(          
          "ALTER TABLE " + Member.TABLE_GROUPER_MEMBERS + " ALTER COLUMN " + Member.COLUMN_SUBJECT_RESOLUTION_RESOLVABLE + " SET NOT NULL;\n");
      ddlVersionBean.getAdditionalScripts().append(          
          "ALTER TABLE " + Member.TABLE_GROUPER_MEMBERS + " ALTER COLUMN " + Member.COLUMN_SUBJECT_RESOLUTION_RESOLVABLE + " SET DEFAULT 'T';\n");
      
      ddlVersionBean.getAdditionalScripts().append(          
          "ALTER TABLE " + Member.TABLE_GROUPER_MEMBERS + " ALTER COLUMN " + Member.COLUMN_SUBJECT_RESOLUTION_DELETED + " SET NOT NULL;\n");
      ddlVersionBean.getAdditionalScripts().append(          
          "ALTER TABLE " + Member.TABLE_GROUPER_MEMBERS + " ALTER COLUMN " + Member.COLUMN_SUBJECT_RESOLUTION_DELETED + " SET DEFAULT 'F';\n");
    }    
  }
}