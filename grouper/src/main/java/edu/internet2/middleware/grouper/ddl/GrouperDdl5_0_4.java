package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.pit.PITField;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroup;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheMembership;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheMembershipHst;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDdl5_0_4 {
  
  /**
   * if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V46.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V46.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  //TODO add group internal id
  
  static void addGrouperFieldsInternalIdComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperFieldsInternalIdComments", true)) {
      return;
    }
  
    final String tableName = Field.TABLE_GROUPER_FIELDS;
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        "internal_id", 
        "internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)");

  
  }

  static void addGrouperFieldsInternalIdColumn(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperFieldsInternalIdColumn", true)) {
      return;
    }
  
    Table grouperTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,Field.TABLE_GROUPER_FIELDS);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperTable, "internal_id", Types.BIGINT, "12", false, false);
  
  }

  static void addGrouperFieldsInternalIdIndex(Database database, DdlVersionBean ddlVersionBean) {
  
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperFieldsRequireIndex", true)) {
      return;
    }
  
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, Field.TABLE_GROUPER_FIELDS, 
        "grouper_fie_internal_id_idx", true, 
        "internal_id");
    
    
  }
  
  static void addGrouperPITFieldsInternalIdColumn(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperPITFieldsInternalIdColumn", true)) {
      return;
    }
  
    Table grouperTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,PITField.TABLE_GROUPER_PIT_FIELDS);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperTable, "source_internal_id", Types.BIGINT, "12", false, false);
  
  }

  static void addGrouperPITFieldsInternalIdIndex(Database database, DdlVersionBean ddlVersionBean) {
  
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperPITFieldsRequireIndex", true)) {
      return;
    }
  
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, PITField.TABLE_GROUPER_PIT_FIELDS, 
        "pit_field_source_internal_idx", false, 
        "source_internal_id");
    
    
  }
  
  static void addGrouperPITGroupsInternalIdColumn(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperPITGroupsInternalIdColumn", true)) {
      return;
    }
  
    Table grouperTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,PITGroup.TABLE_GROUPER_PIT_GROUPS);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperTable, "source_internal_id", Types.BIGINT, "12", false, false);
  
  }

  static void addGrouperPITGroupsInternalIdIndex(Database database, DdlVersionBean ddlVersionBean) {
  
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperPITGroupsRequireIndex", true)) {
      return;
    }
  
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, PITGroup.TABLE_GROUPER_PIT_GROUPS,
        "pit_group_source_internal_idx", false,
        "source_internal_id");
    
    
  }
  
  static void addGrouperPITMembersInternalIdColumn(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperPITMembersInternalIdColumn", true)) {
      return;
    }
  
    Table grouperTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,PITMember.TABLE_GROUPER_PIT_MEMBERS);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperTable, "source_internal_id", Types.BIGINT, "12", false, false);
  
  }

  static void addGrouperPITMembersInternalIdIndex(Database database, DdlVersionBean ddlVersionBean) {
  
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperPITMembersRequireIndex", true)) {
      return;
    }
  
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, PITMember.TABLE_GROUPER_PIT_MEMBERS,
        "pit_member_source_internal_idx", false,
        "source_internal_id");
    
    
  }
  
  static void addGrouperPITFieldsInternalIdComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperPITFieldsInternalIdComments", true)) {
      return;
    }
  
    final String tableName = PITField.TABLE_GROUPER_PIT_FIELDS;
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        "source_internal_id", 
        "internal integer id from the grouper_fields table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)");
  }
  
  static void addGrouperPITGroupsInternalIdComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperPITGroupsInternalIdComments", true)) {
      return;
    }
  
    final String tableName = PITGroup.TABLE_GROUPER_PIT_GROUPS;
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        "source_internal_id", 
        "internal integer id from the grouper_groups table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)");
  }
  
  static void addGrouperPITMembersInternalIdComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperPITMembersInternalIdComments", true)) {
      return;
    }
  
    final String tableName = PITMember.TABLE_GROUPER_PIT_MEMBERS;
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        "source_internal_id", 
        "internal integer id from the grouper_members table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)");
  }
  
  static void addGrouperSqlCacheGroupTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperSqlCacheTable", true)) {
      return;
    }
    
    final String tableName = SqlCacheGroup.TABLE_GROUPER_SQL_CACHE_GROUP;
    
    Table grouperSqlCacheGroupTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSqlCacheGroupTable, SqlCacheGroup.COLUMN_INTERNAL_ID,
        Types.BIGINT, "20", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSqlCacheGroupTable, SqlCacheGroup.COLUMN_GROUP_INTERNAL_ID,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSqlCacheGroupTable, SqlCacheGroup.COLUMN_FIELD_INTERNAL_ID,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSqlCacheGroupTable, SqlCacheGroup.COLUMN_MEMBERSHIP_SIZE,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSqlCacheGroupTable, SqlCacheGroup.COLUMN_MEMBERSHIP_SIZE_HST,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSqlCacheGroupTable, SqlCacheGroup.COLUMN_CREATED_ON,
        Types.TIMESTAMP, null, false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSqlCacheGroupTable, SqlCacheGroup.COLUMN_ENABLED_ON,
        Types.TIMESTAMP, null, false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSqlCacheGroupTable, SqlCacheGroup.COLUMN_DISABLED_ON,
        Types.TIMESTAMP, null, false, true);


        
  }
  
  static void addGrouperSqlCacheGroupTableIndexes(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperSqlCacheGroupTableIndexes", true)) {
      return;
    }

    // CREATE INDEX grouper_sql_cache_group1_idx ON grouper_sql_cache_group (group_internal_id, field_internal_id);
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, SqlCacheGroup.TABLE_GROUPER_SQL_CACHE_GROUP, 
        "grouper_sql_cache_group1_idx", true, 
        SqlCacheGroup.COLUMN_GROUP_INTERNAL_ID, SqlCacheGroup.COLUMN_FIELD_INTERNAL_ID);

        
  }

  static void addGrouperSqlCacheGroupTableForeignKeys(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperSqlCacheGroupTableForeignKeys", true)) {
      return;
    }

    //ALTER TABLE grouper_sql_cache_group ADD CONSTRAINT grouper_sql_cache_group1_fk FOREIGN KEY (field_internal_id) REFERENCES grouper_fields(internal_id);

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, SqlCacheGroup.TABLE_GROUPER_SQL_CACHE_GROUP,
        "grouper_sql_cache_group1_fk", Field.TABLE_GROUPER_FIELDS, SqlCacheGroup.COLUMN_FIELD_INTERNAL_ID, Field.COLUMN_INTERNAL_ID);

        
  }

  static void addGrouperSqlCacheGroupTableComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperSqlCacheGroupTableComments", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, SqlCacheGroup.TABLE_GROUPER_SQL_CACHE_GROUP, 
        "Holds groups that are cacheable in SQL");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheGroup.TABLE_GROUPER_SQL_CACHE_GROUP, 
        SqlCacheGroup.COLUMN_INTERNAL_ID, 
        "internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheGroup.TABLE_GROUPER_SQL_CACHE_GROUP, 
        SqlCacheGroup.COLUMN_GROUP_INTERNAL_ID, 
        "internal integer id for gruops which are cacheable");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheGroup.TABLE_GROUPER_SQL_CACHE_GROUP, 
        SqlCacheGroup.COLUMN_FIELD_INTERNAL_ID, 
        "internal integer id for the field which is the members or privilege which is cached");


    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheGroup.TABLE_GROUPER_SQL_CACHE_GROUP, 
        SqlCacheGroup.COLUMN_FIELD_INTERNAL_ID, 
        "internal integer id for the field which is the members or privilege which is cached");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheGroup.TABLE_GROUPER_SQL_CACHE_GROUP, 
        SqlCacheGroup.COLUMN_MEMBERSHIP_SIZE, 
        "approximate number of members of this group, used primarily to optimize batching");


    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheGroup.TABLE_GROUPER_SQL_CACHE_GROUP, 
        SqlCacheGroup.COLUMN_MEMBERSHIP_SIZE_HST, 
        "approximate number of rows of HST data for this group, used primarily to optimize batching");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheGroup.TABLE_GROUPER_SQL_CACHE_GROUP, 
        SqlCacheGroup.COLUMN_CREATED_ON, 
        "when this row was created (i.e. when this group started to be cached)");


    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheGroup.TABLE_GROUPER_SQL_CACHE_GROUP, 
        SqlCacheGroup.COLUMN_ENABLED_ON, 
        "when this cache will be ready to use (do not use it while it is being populated)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheGroup.TABLE_GROUPER_SQL_CACHE_GROUP, 
        SqlCacheGroup.COLUMN_DISABLED_ON, 
        "when this cache should stop being used");

  }

  static void addGrouperSqlCacheMshipTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperSqlCacheMshipTable", true)) {
      return;
    }
    
    final String tableName = SqlCacheMembership.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP;
    
    Table grouperSqlCacheMembershipTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSqlCacheMembershipTable, SqlCacheMembership.COLUMN_CREATED_ON,
        Types.TIMESTAMP, null, false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSqlCacheMembershipTable, SqlCacheMembership.COLUMN_FLATTENED_ADD_TIMESTAMP,
        Types.TIMESTAMP, null, false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSqlCacheMembershipTable, SqlCacheMembership.COLUMN_INTERNAL_ID,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSqlCacheMembershipTable, SqlCacheMembership.COLUMN_MEMBER_INTERNAL_ID,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSqlCacheMembershipTable, SqlCacheMembership.COLUMN_SQL_CACHE_GROUP_INTERNAL_ID,
        Types.BIGINT, "20", false, true);
        
  }
  

  static void addGrouperSqlCacheMshipTableIndexes(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperSqlCacheMshipTableIndexes", true)) {
      return;
    }

    //CREATE INDEX grouper_sql_cache_mship1_idx ON grouper_sql_cache_mship (sql_cache_group_internal_id, flattened_add_timestamp);
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, SqlCacheMembership.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP, 
        "grouper_sql_cache_mship1_idx", false, 
        SqlCacheMembership.COLUMN_SQL_CACHE_GROUP_INTERNAL_ID, SqlCacheMembership.COLUMN_FLATTENED_ADD_TIMESTAMP);

    //CREATE INDEX grouper_sql_cache_mship2_idx ON grouper_sql_cache_mship (member_internal_id, sql_cache_group_internal_id);
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, SqlCacheMembership.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP, 
        "grouper_sql_cache_mship2_idx", false, 
        SqlCacheMembership.COLUMN_MEMBER_INTERNAL_ID, SqlCacheMembership.COLUMN_SQL_CACHE_GROUP_INTERNAL_ID);
    
  }

  static void addGrouperSqlCacheMshipTableForeignKeys(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperSqlCacheMshipTableForeignKeys", true)) {
      return;
    }

    //ALTER TABLE grouper_sql_cache_mship ADD CONSTRAINT grouper_sql_cache_mship1_fk FOREIGN KEY (sql_cache_group_internal_id) REFERENCES grouper_sql_cache_group(internal_id);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, SqlCacheMembership.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP,
        "grouper_sql_cache_mship1_fk", SqlCacheGroup.TABLE_GROUPER_SQL_CACHE_GROUP, SqlCacheMembership.COLUMN_SQL_CACHE_GROUP_INTERNAL_ID, SqlCacheGroup.COLUMN_INTERNAL_ID);
        
  }

  static void addGrouperSqlCacheMshipTableComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperSqlCacheMshipTableComments", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, SqlCacheMembership.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP, 
        "Cached memberships based on group and list");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheMembership.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP, 
        SqlCacheMembership.COLUMN_INTERNAL_ID, 
        "internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheMembership.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP, 
        SqlCacheMembership.COLUMN_CREATED_ON, 
        "when this cache row was created");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheMembership.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP, 
        SqlCacheMembership.COLUMN_FLATTENED_ADD_TIMESTAMP, 
        "when this member was last added to this group after not being a member before.  How long this member has been in this group");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheMembership.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP, 
        SqlCacheMembership.COLUMN_MEMBER_INTERNAL_ID, 
        "internal id of the member in this group");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheMembership.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP, 
        SqlCacheMembership.COLUMN_SQL_CACHE_GROUP_INTERNAL_ID, 
        "internal id of the group/list that this member is in");

  }
  
  
  static void addGrouperSqlCacheMshipHstTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperSqlCacheMshipHstTable", true)) {
      return;
    }
    
    final String tableName = SqlCacheMembershipHst.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP_HST;
    
    Table grouperSqlCacheMshipHstTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSqlCacheMshipHstTable, SqlCacheMembershipHst.COLUMN_INTERNAL_ID,
        Types.BIGINT, "20", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSqlCacheMshipHstTable, SqlCacheMembershipHst.COLUMN_END_TIME,
        Types.TIMESTAMP, null, false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSqlCacheMshipHstTable, SqlCacheMembershipHst.COLUMN_START_TIME,
        Types.TIMESTAMP, null, false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSqlCacheMshipHstTable, SqlCacheMembershipHst.COLUMN_SQL_CACHE_GROUP_INTERNAL_ID,
        Types.BIGINT, "20", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSqlCacheMshipHstTable, SqlCacheMembershipHst.COLUMN_MEMBER_INTERNAL_ID,
        Types.BIGINT, "20", true, true);
        
  }
  
  static void addGrouperSqlCacheMshipHstTableIndexes(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperSqlCacheMshipHstTableIndexes", true)) {
      return;
    }

    //  CREATE INDEX grouper_sql_cache_mshhst1_idx ON grouper_sql_cache_mship_hst (sql_cache_group_internal_id, end_time);
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, SqlCacheMembershipHst.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP_HST, 
        "grouper_sql_cache_msh_hst1_idx", true, 
        SqlCacheMembershipHst.COLUMN_SQL_CACHE_GROUP_INTERNAL_ID, SqlCacheMembershipHst.COLUMN_END_TIME);

    //  CREATE INDEX grouper_sql_cache_mshhst2_idx ON grouper_sql_cache_mship_hst (sql_cache_group_internal_id, start_time);
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, SqlCacheMembershipHst.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP_HST, 
        "grouper_sql_cache_msh_hst2_idx", true, 
        SqlCacheMembershipHst.COLUMN_SQL_CACHE_GROUP_INTERNAL_ID, SqlCacheMembershipHst.COLUMN_START_TIME);

    //  CREATE INDEX grouper_sql_cache_mshhst3_idx ON grouper_sql_cache_mship_hst (member_internal_id, sql_cache_group_internal_id, end_time);
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, SqlCacheMembershipHst.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP_HST, 
        "grouper_sql_cache_msh_hst3_idx", true, SqlCacheMembershipHst.COLUMN_INTERNAL_ID,
        SqlCacheMembershipHst.COLUMN_SQL_CACHE_GROUP_INTERNAL_ID, SqlCacheMembershipHst.COLUMN_END_TIME);
        
  }

  static void addGrouperSqlCacheMshipHstTableForeignKeys(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperSqlCacheMshipHstTableForeignKeys", true)) {
      return;
    }

    //ALTER TABLE grouper_sql_cache_mship_hst ADD CONSTRAINT grouper_sql_cache_mshhst1_fk FOREIGN KEY (sql_cache_group_internal_id) REFERENCES grouper_sql_cache_group(internal_id);
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, SqlCacheMembershipHst.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP_HST,
        "grouper_sql_cache_msh_hst1_fk", SqlCacheGroup.TABLE_GROUPER_SQL_CACHE_GROUP, SqlCacheMembershipHst.COLUMN_SQL_CACHE_GROUP_INTERNAL_ID, SqlCacheGroup.COLUMN_INTERNAL_ID);

  }

  static void addGrouperSqlCacheMshipHstTableComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperSqlCacheMshipHstTableComments", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, SqlCacheMembershipHst.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP_HST, 
        "Flattened point in time cache table for memberships or privileges");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheMembershipHst.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP_HST, 
        SqlCacheMembershipHst.COLUMN_INTERNAL_ID, 
        "internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheMembershipHst.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP_HST, 
        SqlCacheMembershipHst.COLUMN_END_TIME, 
        "flattened membership end time");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheMembershipHst.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP_HST, 
        SqlCacheMembershipHst.COLUMN_START_TIME, 
        "flattened membership start time");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheMembershipHst.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP_HST, 
        SqlCacheMembershipHst.COLUMN_MEMBER_INTERNAL_ID, 
        "member internal id of who this membership refers to");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        SqlCacheMembershipHst.TABLE_GROUPER_SQL_CACHE_MEMBERSHIP_HST, 
        SqlCacheMembershipHst.COLUMN_INTERNAL_ID, 
        "internal id of which group/field this membership refers to");


  }
  
  static void createViewGrouperSqlCacheGroupV(DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v5_0_0_createViewGrouperSqlCacheGroupV", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_sql_cache_group_v", 
        "SQL cache group view",
        GrouperUtil.toSet("group_name", 
            "list_name", 
            "membership_size", 
            "group_id", 
            "field_id", 
            "group_internal_id", 
            "field_internal_id"),
        GrouperUtil.toSet("group_name: name of group", 
            "list_name: name of list: members or the privilege like admins", 
            "membership_size: approximate number of memberships in the group", 
            "group_id: uuid of the group", 
            "field_id: uuid of the field", 
            "group_internal_id: group internal id", 
            "field_internal_id: field internal id"),
        "select gg.name group_name, gf.name list_name, membership_size, "
        + " gg.id group_id, gf.id field_id, gg.internal_id group_internal_id, gf.internal_id field_internal_id "
        + " from grouper_sql_cache_group gscg, grouper_fields gf, grouper_groups gg "
        + " where gscg.group_internal_id = gg.internal_id and gscg.field_internal_id = gf.internal_id "
     );
  }
  
  static void createViewGrouperSqlCacheMshipV(DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v5_0_0_createViewGrouperSqlCacheMshipV", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_sql_cache_mship_v", 
        "SQL cache mship view",
        GrouperUtil.toSet("group_name", 
            "list_name", 
            "subject_id", 
            "subject_identifier0", 
            "subject_identifier1",
            "subject_identifier2", 
            "subject_source", 
            "flattened_add_timestamp", 
            "group_id",
            "field_id", 
            "mship_hst_internal_id", 
            "member_internal_id",
            "group_internal_id", 
            "field_internal_id"),
        GrouperUtil.toSet("group_name: name of group", 
            "list_name: name of list e.g. members or admins", 
            "subject_id: subject id", 
            "subject_identifier0: subject identifier0 from subject source and members table", 
            "subject_identifier1: subject identifier1 from subject source and members table",
            "subject_identifier2: subject identifier2 from subject source and members table", 
            "subject_source: subject source id", 
            "flattened_add_timestamp: when this membership started", 
            "group_id: uuid of group",
            "field_id: uuid of field", 
            "mship_hst_internal_id: history internal id", 
            "member_internal_id: member internal id",
            "group_internal_id: group internal id", 
            "field_internal_id: field internal id"),
        " SELECT gg.name AS group_name, gf.name AS list_name, gm.subject_id, gm.subject_identifier0, "
        + " gm.subject_identifier1, gm.subject_identifier2, gm.subject_source, gscm.flattened_add_timestamp, "
        + " gg.id AS group_id, gf.id AS field_id, gscm.internal_id AS mship_internal_id, gm.internal_id AS member_internal_id, "
        + " gg.internal_id AS group_internal_id, gf.internal_id AS field_internal_id "
        + " FROM grouper_sql_cache_group gscg, grouper_sql_cache_mship gscm, grouper_fields gf, "
        + " grouper_groups gg, grouper_members gm "
        + " WHERE gscg.group_internal_id = gg.internal_id AND gscg.field_internal_id = gf.internal_id "
        + " AND gscm.sql_cache_group_internal_id = gscg.internal_id AND gscm.member_internal_id = gm.internal_id ");
  }  
  static void createViewGrouperSqlCacheMshipHstV(DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v5_0_0_createViewGrouperSqlCacheMshipHstV", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_sql_cache_mship_hst_v", 
        "SQL cache mship history view",
        GrouperUtil.toSet("group_name", 
            "list_name", 
            "subject_id", 
            "subject_identifier0", 
            "subject_identifier1",
            "subject_identifier2", 
            "subject_source", 
            "start_time", 
            "end_time", 
            "group_id",
            "field_id", 
            "mship_hst_internal_id", 
            "member_internal_id",
            "group_internal_id", 
            "field_internal_id"),
        GrouperUtil.toSet("group_name: name of group", 
            "list_name: name of list e.g. members or admins", 
            "subject_id: subject id", 
            "subject_identifier0: subject identifier0 from subject source and members table", 
            "subject_identifier1: subject identifier1 from subject source and members table",
            "subject_identifier2: subject identifier2 from subject source and members table", 
            "subject_source: subject source id", 
            "start_time: when this membership started", 
            "end_time: when this membership ended", 
            "group_id: uuid of group",
            "field_id: uuid of field", 
            "mship_hst_internal_id: history internal id", 
            "member_internal_id: member internal id",
            "group_internal_id: group internal id", 
            "field_internal_id: field internal id"),
        " select "
        + " gg.name as group_name, gf.name as list_name, gm.subject_id, gm.subject_identifier0, gm.subject_identifier1, "
        + " gm.subject_identifier2, gm.subject_source, gscmh.start_time, gscmh.end_time, gg.id as group_id, "
        + " gf.id as field_id, gscmh.internal_id as mship_hst_internal_id, gm.internal_id as member_internal_id, "
        + " gg.internal_id as group_internal_id, gf.internal_id as field_internal_id from "
        + " grouper_sql_cache_group gscg, grouper_sql_cache_mship_hst gscmh, grouper_fields gf, "
        + " grouper_groups gg, grouper_members gm where gscg.group_internal_id = gg.internal_id "
        + " and gscg.field_internal_id = gf.internal_id and gscmh.sql_cache_group_internal_id = gscg.internal_id "
        + " and gscmh.member_internal_id = gm.internal_id ");
  }

  static void addGrouperGroupsInternalIdColumn(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperGroupsInternalIdColumn", true)) {
      return;
    }
  
    Table grouperTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,Group.TABLE_GROUPER_GROUPS);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperTable, "internal_id", Types.BIGINT, "12", false, false);
  
  }

  //TODO add group internal id
  
  static void addGrouperGroupsInternalIdComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperGroupsInternalIdComments", true)) {
      return;
    }
  
    final String tableName = Group.TABLE_GROUPER_GROUPS;
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        "internal_id", 
        "internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)");
  
  
  }

  static void addGrouperGroupsInternalIdIndex(Database database, DdlVersionBean ddlVersionBean) {
  
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v5_0_4_addGrouperGroupsRequireIndex", true)) {
      return;
    }
  
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, Group.TABLE_GROUPER_GROUPS, 
        "grouper_grp_internal_id_idx", true, 
        "internal_id");
    
    
  }


}
