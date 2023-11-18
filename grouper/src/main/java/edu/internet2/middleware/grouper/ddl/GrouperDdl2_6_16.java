package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

public class GrouperDdl2_6_16 {
  
  /**
   * if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V43.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V43.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  public static final String TABLE_GROUPER_MSHIP_REQ_CHANGE = "grouper_mship_req_change";
  
  public static final String COLUMN_GROUPER_MSHIP_REQ_CHANGE_ID = "id";

  public static final String COLUMN_GROUPER_MSHIP_REQ_CHANGE_MEMBER_ID = "member_id";

  public static final String COLUMN_GROUPER_MSHIP_REQ_CHANGE_GROUP_ID = "group_id";

  public static final String COLUMN_GROUPER_MSHIP_REQ_CHANGE_THE_TIMESTAMP = "the_timestamp";

  public static final String COLUMN_GROUPER_MSHIP_REQ_CHANGE_ENGINE = "engine";

  
  static void addGrouperMembershipRequireComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_6_16_addGrouperMembershipRequireComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_MSHIP_REQ_CHANGE;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "table to log membership requirements when memberships fall out");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_MSHIP_REQ_CHANGE_ID, 
        "integer id for this table");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_MSHIP_REQ_CHANGE_MEMBER_ID, 
        "grouper_members uuid reference");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_MSHIP_REQ_CHANGE_GROUP_ID, 
        "grouper_groups id reference");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_MSHIP_REQ_CHANGE_THE_TIMESTAMP, 
        "when the event took place");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_MSHIP_REQ_CHANGE_ENGINE, 
        "H = hook, C = change log consumer, F = full sync");
    
  
  }

  static void addGrouperMembershipRequireIndex(DdlVersionBean ddlVersionBean, Database database) {
  
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_6_16_addGrouperMembershipRequireIndex", true)) {
      return;
    }
  
    Table grouperDuoTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        TABLE_GROUPER_MSHIP_REQ_CHANGE);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperDuoTable.getName(), 
        "grouper_mship_req_mem_gr_idx", false, 
        COLUMN_GROUPER_MSHIP_REQ_CHANGE_GROUP_ID, COLUMN_GROUPER_MSHIP_REQ_CHANGE_MEMBER_ID);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperDuoTable.getName(), 
        "grouper_mship_req_mem_idx", true, 
        COLUMN_GROUPER_MSHIP_REQ_CHANGE_MEMBER_ID);

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperDuoTable.getName(), 
        "grouper_mship_req_time_idx", false, 
        COLUMN_GROUPER_MSHIP_REQ_CHANGE_THE_TIMESTAMP);
    
  }

  static void addGrouperMembershipRequireTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_6_16_addGrouperMembershipRequireTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_MSHIP_REQ_CHANGE;
  
    Table grouperMembershipRequireTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperMembershipRequireTable, COLUMN_GROUPER_MSHIP_REQ_CHANGE_ID,
        Types.BIGINT, "12", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperMembershipRequireTable, COLUMN_GROUPER_MSHIP_REQ_CHANGE_MEMBER_ID,
        Types.VARCHAR, "40", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperMembershipRequireTable, COLUMN_GROUPER_MSHIP_REQ_CHANGE_GROUP_ID,
        Types.VARCHAR, "40", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperMembershipRequireTable, COLUMN_GROUPER_MSHIP_REQ_CHANGE_THE_TIMESTAMP,
        Types.TIMESTAMP, null, false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperMembershipRequireTable, COLUMN_GROUPER_MSHIP_REQ_CHANGE_ENGINE,
        Types.VARCHAR, "1", false, true);
    
  }

  public static void addMemberIdIndexColumn(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_6_16_addMemberIdIndexColumn", true)) {
      return;
    }

    Table memberTable = GrouperDdlUtils.ddlutilsFindTable(database, Member.TABLE_GROUPER_MEMBERS, true);

    //this is required if the group table is new
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(memberTable, Member.COLUMN_ID_INDEX, 
        Types.BIGINT, "20", false, false); 

  }

  public static void addMemberIdIndexIndex(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_6_16_addMemberIdIndexIndex", true)) {
      return;
    }

    //unique index on the new col... note, is this allowed in all ds if there is null data there?  hmm...
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, Member.TABLE_GROUPER_MEMBERS, 
        "member_id_index_idx", true, Member.COLUMN_ID_INDEX);

  }

  public static void addMemberIdIndexComment(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_6_16_addMemberIdIndexComment", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        Member.TABLE_GROUPER_MEMBERS, Member.COLUMN_ID_INDEX, 
        "Sequential id index integer that can we used outside of Grouper");

  }

}
