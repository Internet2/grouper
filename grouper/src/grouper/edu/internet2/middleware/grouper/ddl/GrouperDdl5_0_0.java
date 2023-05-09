package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDdl5_0_0 {
  
  /**
   * if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V45.getVersion() <= buildingToVersion;

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
    
    boolean buildingToPreviousVersion = GrouperDdl.V45.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  
  static void addGrouperMemberInternalIdComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperMemberInternalIdComments", true)) {
      return;
    }
  
    final String tableName = "grouper_members";
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        "internal_id", 
        "internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)");

  
  }

  static void addGrouperMemberInternalIdColumn(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperMemberInternalIdColumn", true)) {
      return;
    }
  
    Table grouperDuoTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        "grouper_members");

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDuoTable, "internal_id", Types.BIGINT, "12", false, false);
  
  }

  static void addGrouperMemberInternalIdIndex(Database database, DdlVersionBean ddlVersionBean) {
  
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperMembershipRequireIndex", true)) {
      return;
    }
  
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_members", 
        "grouper_mem_internal_id_idx", true, 
        "internal_id");
    
  }
  
  static void addGrouperDictionaryTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDictionaryTable", true)) {
      return;
    }
    
    final String tableName = "grouper_dictionary";
    
    Table grouperDictionaryTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDictionaryTable, "internal_id",
        Types.BIGINT, "20", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDictionaryTable, "created_on",
        Types.TIMESTAMP, null, false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDictionaryTable, "last_referenced",
        Types.TIMESTAMP, null, false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDictionaryTable, "pre_load",
        Types.VARCHAR, "1", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDictionaryTable, "the_text",
        Types.VARCHAR, "4000", false, true);
        
  }
  
  static void addGrouperDictionaryTableIndexes(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDictionaryTableIndexes", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_dictionary", 
        "dictionary_last_referenced_idx", false, 
        "last_referenced");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_dictionary", 
        "dictionary_pre_load_idx", false, 
        "pre_load");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_dictionary", 
        "dictionary_the_text_idx", true, 
        "the_text");
        
  }
  
  static void addGrouperDataProviderTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataProviderTable", true)) {
      return;
    }
    
    final String tableName = "grouper_data_provider";
    
    Table grouperDataProviderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDataProviderTable, "internal_id",
        Types.BIGINT, "20", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDataProviderTable, "config_id",
        Types.VARCHAR, "100", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperDataProviderTable, "created_on",
        Types.TIMESTAMP, null, false, true);

  }
  
  static void addGrouperDataProviderTableIndexes(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataProviderTableIndexes", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_provider", 
        "data_provider_config_id_idx", true, 
        "config_id");
  }
  
  static void addGrouperDataFieldTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataFieldTable", true)) {
      return;
    }
    
    final String tableName = "grouper_data_field";
    
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
        Types.BIGINT, "20", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "config_id",
        Types.VARCHAR, "100", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "created_on",
        Types.TIMESTAMP, null, false, true);

  }
  
  static void addGrouperDataFieldTableIndexes(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataFieldTableIndexes", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_field", 
        "data_field_config_id_idx", true, 
        "config_id");
  }
  
  static void addGrouperDataRowTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataRowTable", true)) {
      return;
    }
    
    final String tableName = "grouper_data_row";
    
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
        Types.BIGINT, "20", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "config_id",
        Types.VARCHAR, "100", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "created_on",
        Types.TIMESTAMP, null, false, true);

  }
  
  static void addGrouperDataRowTableIndexes(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataRowTableIndexes", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_row", 
        "grouper_data_row_config_id_idx", true, 
        "config_id");

  }

  
  static void addGrouperDataFieldAliasTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataFieldAliasTable", true)) {
      return;
    }
    
    final String tableName = "grouper_data_alias";
    
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
        Types.BIGINT, "20", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "data_field_internal_id",
        Types.BIGINT, "20", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "name",
        Types.VARCHAR, "100", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "lower_name",
        Types.VARCHAR, "100", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "created_on",
        Types.TIMESTAMP, null, false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "data_row_internal_id",
        Types.BIGINT, "20", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "alias_type",
        Types.VARCHAR, "1", false, false);

  }
  
  static void addGrouperDataFieldAliasTableIndexes(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataFieldAliasTableIndexes", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_alias", 
        "alias_data_field_intrnl_id_idx", false, 
        "data_field_internal_id");
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_alias", 
        "alias_lower_name_idx", true, 
        "lower_name");
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_alias", 
        "alias_name_idx", true, 
        "name");
    
  }
  
  static void addGrouperDataFieldAliasForeignKey(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataFieldAliasForeignKey", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_data_alias",
        "grouper_data_alias_fk", "grouper_data_field", "data_field_internal_id", "internal_id");

  }
  
  static void addGrouperDataFieldAssignTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataFieldAssignTable", true)) {
      return;
    }
    
    final String tableName = "grouper_data_field_assign";
    
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
        Types.BIGINT, "20", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "member_internal_id",
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "data_field_internal_id",
        Types.BIGINT, "20", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "created_on",
        Types.TIMESTAMP, null, false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "value_integer",
        Types.BIGINT, "20", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "value_dictionary_internal_id",
        Types.BIGINT, "20", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "data_provider_internal_id",
        Types.BIGINT, "20", false, true);
    
  }
  
  static void addGrouperDataFieldAssignTableIndexes(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataFieldAssignTableIndexes", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_field_assign", 
        "fld_assgn_prvdr_intrnl_id_idx", false, 
        "data_provider_internal_id");
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_field_assign", 
        "fld_assgn_field_intrnl_id_idx", false, 
        "data_field_internal_id");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_field_assign", 
        "fld_assgn_mbrs_intrnl_id_idx", false, 
        "member_internal_id");
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_field_assign", 
        "fld_assgn_mbr_intrnl_id_idx", true, 
        "member_internal_id", "data_field_internal_id", "value_integer", "value_dictionary_internal_id", "data_provider_internal_id");
    
  }
  
  static void addGrouperDataFieldAssignTableForeignKey(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataFieldAssignTableForeignKey", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_data_field_assign",
        "grouper_data_field_assign_fk", "grouper_data_field", "data_field_internal_id", "internal_id");

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_data_field_assign",
        "grouper_data_field_assign_fk_1", "grouper_dictionary", "value_dictionary_internal_id", "internal_id");

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_data_field_assign",
        "grouper_data_field_assign_fk_2", "grouper_members", "member_internal_id", "internal_id");

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_data_field_assign",
        "grouper_data_field_assign_fk_3", "grouper_data_provider", "data_provider_internal_id", "internal_id");

  }
  
  static void addGrouperDataRowAssignTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataRowAssignTable", true)) {
      return;
    }
    
    final String tableName = "grouper_data_row_assign";
    
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
        Types.BIGINT, "20", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "member_internal_id",
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "data_row_internal_id",
        Types.BIGINT, "20", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "created_on",
        Types.TIMESTAMP, null, false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "data_provider_internal_id",
        Types.BIGINT, "20", false, true);
    
  }
  
  static void addGrouperDataRowAssignTableIndexes(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataRowAssignTableIndexes", true)) {
      return;
    }
        
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_row_assign", 
        "rw_assg_dt_prvdr_intrnl_id_idx", false, 
        "data_provider_internal_id");
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_row_assign", 
        "rw_assg_dt_rw_intrnl_id_idx", false, 
        "data_row_internal_id");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_row_assign", 
        "rw_assg_mbr_intrnl_id_idx", false, 
        "member_internal_id");
    
  }
  
  static void addGrouperDataRowAssignTableForeignKey(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataRowAssignTableForeignKey", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_data_row_assign",
        "grouper_data_row_assign_fk", "grouper_members", "member_internal_id", "internal_id");
    
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_data_row_assign",
        "grouper_data_row_assign_fk_1", "grouper_data_row", "data_row_internal_id", "internal_id");

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_data_row_assign",
        "grouper_data_row_assign_fk_2", "grouper_data_provider", "data_provider_internal_id", "internal_id");

  }
  
  static void addGrouperDataRowFieldAssignTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataRowFieldAssignTable", true)) {
      return;
    }
    
    final String tableName = "grouper_data_row_field_assign";
    
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
        Types.BIGINT, "20", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "data_row_assign_internal_id",
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "created_on",
        Types.TIMESTAMP, null, false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "value_integer",
        Types.BIGINT, "20", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "value_dictionary_internal_id",
        Types.BIGINT, "20", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "data_field_internal_id",
        Types.BIGINT, "20", false, true);
    
  }
  
  static void addGrouperDataRowFieldAssignTableIndexes(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataRowFieldAssignTableIndexes", true)) {
      return;
    }
        
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_row_field_assign", 
        "dt_rw_fld_asg_fld_intrnl_ididx", false, 
        "data_field_internal_id");
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_row_field_assign", 
        "dtrwfldasg_dtrwsg_intrnl_ididx", false, 
        "data_row_assign_internal_id");

  }
  
  static void addGrouperDataRowFieldAssignTableForeignKey(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataRowFieldAssignTableForeignKey", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_data_row_field_assign",
        "grpr_dt_row_field_assign_fk", "grouper_data_row_assign", "data_row_assign_internal_id", "internal_id");
    
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_data_row_field_assign",
        "grpr_dt_row_field_assign_fk_1", "grouper_dictionary", "value_dictionary_internal_id", "internal_id");

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_data_row_field_assign",
        "grpr_dt_row_field_assign_fk_3", "grouper_data_field", "data_field_internal_id", "internal_id");

  }
  
  static void addGrouperDataGlobalAssignTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataGlobalAssignTable", true)) {
      return;
    }
    
    final String tableName = "grouper_data_global_assign";
    
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
        Types.BIGINT, "20", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "data_field_internal_id",
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "value_integer",
        Types.BIGINT, "20", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "value_dictionary_internal_id",
        Types.BIGINT, "20", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "data_provider_internal_id",
        Types.BIGINT, "20", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "created_on",
        Types.TIMESTAMP, null, false, true);
    
  }
  
  static void addGrouperDataGlobalAssignTableIndexes(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataGlobalAssignTableIndexes", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_global_assign", 
        "grouper_data_global1_idx", false, 
        "data_provider_internal_id");
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_global_assign", 
        "grouper_data_global2_idx", false, 
        "data_field_internal_id");
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_global_assign", 
        "grouper_data_global3_idx", false, 
        "data_field_internal_id", "value_integer");
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_data_global_assign", 
        "grouper_data_global4_idx", false, 
        "data_field_internal_id", "value_dictionary_internal_id");

  }
  
  static void addGrouperDataGlobalAssignTableForeignKey(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_0_0_addGrouperDataGlobalAssignTableForeignKey", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_data_global_assign",
        "grouper_data_global_assign_fk", "grouper_data_field", "data_field_internal_id", "internal_id");
    
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_data_global_assign",
        "grouper_data_global_diction_fk", "grouper_dictionary", "value_dictionary_internal_id", "internal_id");

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, "grouper_data_global_assign",
        "grouper_data_global_prov_fk", "grouper_data_provider", "data_provider_internal_id", "internal_id");

  }
  
  static void createViewGrouperDataFieldAssignV(DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v5_0_0_createViewGrouperDataFieldAssignV", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_data_field_assign_v", 
        "Data field assign view",
        GrouperUtil.toSet("data_field_config_id", 
            "subject_id", 
            "value_text", 
            "value_integer", 
            "data_field_internal_id", "data_field_assign_internal_id", "subject_source_id", "member_id"),
        GrouperUtil.toSet("data_field_config_id: data field config id", 
            "subject_id: subject id of subject",
            "value_text: value text", 
            "value_integer: value integer", 
            "data_field_internal_id: data field internal id", 
            "data_field_assign_internal_id: data field assign internal id", 
            "subject_source_id: subject source id", 
            "member_id: member id"),
        "select gdf.config_id data_field_config_id, gm.subject_id, gd.the_text value_text, gdfa.value_integer, "+
          "gdf.internal_id data_field_internal_id, gdfa.internal_id data_field_assign_internal_id, "+
          "gm.subject_source subject_source_id, gm.id member_id "+
          "from grouper_data_field gdf, grouper_members gm, grouper_data_field_assign gdfa  "+
          "left join grouper_dictionary gd on gdfa.value_dictionary_internal_id = gd.internal_id  "+
          "where gdfa.member_internal_id = gm.internal_id "+
          "and gdfa.data_field_internal_id = gdf.internal_id"
        );
  }
  
  static void createViewGrouperDataRowAssignV(DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v5_0_0_createViewGrouperDataRowAssignV", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_data_row_assign_v", 
        "Data row assign view",
        GrouperUtil.toSet("data_row_config_id", 
            "subject_id", 
            "data_row_internal_id", 
            "data_row_assign_internal_id", 
            "subject_source_id", "member_id"),
        GrouperUtil.toSet("data_row_config_id: data row config id", 
            "subject_id: subject id of subject",
            "data_row_internal_id: data row internal id", 
            "data_row_assign_internal_id: data row assign internal id", 
            "subject_source_id: subject source id", 
            "member_id: member id"),
        "select gdr.config_id data_row_config_id, gm.subject_id, "+
        "gdra.internal_id data_row_internal_id, gdra.internal_id data_row_assign_internal_id, "+
        "gm.subject_source subject_source_id, gm.id member_id "+
        "from grouper_members gm, grouper_data_row_assign gdra, grouper_data_row gdr "+ 
        "where gdra.member_internal_id = gm.internal_id "+
        "and gdr.internal_id = gdra.data_row_internal_id"
        );
  }
  
  static void createViewGrouperDataRowFieldAssignV(DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v5_0_0_createViewGrouperDataRowFieldAssignV", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_data_row_field_asgn_v", 
        "Data row field assign view",
        GrouperUtil.toSet("data_row_config_id", 
            "data_field_config_id", 
            "subject_id", 
            "value_text", 
            "value_integer",
            "subject_source_id",
            "member_id", 
            "data_field_internal_id",
            "data_field_assign_internal_id", 
            "data_row_internal_id", 
            "data_row_assign_internal_id"),
        GrouperUtil.toSet("data_row_config_id: data row config id", 
            "data_field_config_id: data field config id",
            "subject_id: subject id of subject",
            "value_text: value text",
            "value_integer: value integer",
            "subject_source_id: subject source id",
            "member_id: member id", 
            "data_field_internal_id: data field internal id",
            "data_field_assign_internal_id: data field assign internal id", 
            "data_row_internal_id: data row internal id", 
            "data_row_assign_internal_id: data row assign internal id"),
        "select gdr.config_id data_row_config_id, gdf.config_id data_field_config_id, gm.subject_id, gd.the_text value_text, gdrfa.value_integer, "+  
        "gm.subject_source subject_source_id, gm.id member_id, gdf.internal_id data_field_internal_id, gdrfa.internal_id data_field_assign_internal_id, "+
        "gdra.internal_id data_row_internal_id, gdra.internal_id data_row_assign_internal_id "+
        "from grouper_data_field gdf, grouper_members gm, grouper_data_row_assign gdra, grouper_data_row gdr, "+
        "grouper_data_row_field_assign gdrfa "+
        "left join grouper_dictionary gd on gdrfa.value_dictionary_internal_id = gd.internal_id "+ 
        "where gdra.member_internal_id = gm.internal_id "+
        "and gdrfa.data_field_internal_id = gdf.internal_id "+
        "and gdr.internal_id = gdra.data_row_internal_id "+
        "and gdra.internal_id = gdrfa.data_row_assign_internal_id"
        );
  }



}
