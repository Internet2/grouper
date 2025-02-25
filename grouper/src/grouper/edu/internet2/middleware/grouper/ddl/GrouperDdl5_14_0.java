package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDdl5_14_0 {
  
  public static final String TABLE_GROUPER_PROV_ADOBE_USER = "grouper_prov_adobe_user";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_CONFIG_ID = "config_id";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_USER_ID = "user_id";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_EMAIL = "email";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_USER_NAME = "username";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_STATUS = "status";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_TYPE= "adobe_type";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_FIRST_NAME= "firstname";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_LAST_NAME= "lastname";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_DOMAIN= "domain";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_USER_COUNTRY= "country";

  
  public static final String TABLE_GROUPER_PROV_ADOBE_GROUP = "grouper_prov_adobe_group";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_GROUP_CONFIG_ID = "config_id";

  public static final String COLUMN_GROUPER_PROV_ADOBE_GROUP_GROUP_ID = "group_id";

  public static final String COLUMN_GROUPER_PROV_ADOBE_GROUP_NAME = "name";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_GROUP_TYPE = "adobe_type";

  public static final String COLUMN_GROUPER_PROV_ADOBE_GROUP_PRODUCT_NAME = "product_name";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_GROUP_MEMBER_COUNT = "member_count";

  public static final String COLUMN_GROUPER_PROV_ADOBE_GROUP_LICENSE_QUOTA = "license_quota";
  
  
  
  public static final String TABLE_GROUPER_PROV_ADOBE_MEMBERSHIP = "grouper_prov_adobe_membership";
  
  public static final String COLUMN_GROUPER_PROV_ADOBE_MEMBERSHIP_CONFIG_ID = "config_id";

  public static final String COLUMN_GROUPER_PROV_ADOBE_MEMBERSHIP_GROUP_ID = "group_id";

  public static final String COLUMN_GROUPER_PROV_ADOBE_MEMBERSHIP_USER_ID = "user_id";
  
  
  public static final String TABLE_GROUPER_SQL_CACHE_DEPEND_TYPE = "grouper_sql_cache_depend_type";

  public static final String COLUMN_GROUPER_SQL_CACHE_DEPEND_TYPE_INTERNAL_ID = "internal_id";

  public static final String COLUMN_GROUPER_SQL_CACHE_DEPEND_TYPE_DEPENDENCY_CATEGORY = "dependency_category";

  public static final String COLUMN_GROUPER_SQL_CACHE_DEPEND_TYPE_NAME = "name";
  
  public static final String COLUMN_GROUPER_SQL_CACHE_DEPEND_TYPE_DESCRIPTION = "description";

  
  public static final String TABLE_GROUPER_SQL_CACHE_DEPENDENCY = "grouper_sql_cache_dependency";

  public static final String COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_INTERNAL_ID = "internal_id";

  public static final String COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_DEP_TYPE_INTERNAL_ID = "dep_type_internal_id";

  public static final String COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_OWNER_INTERNAL_ID = "owner_internal_id";

  public static final String COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_DEPENDENT_INTERNAL_ID = "dependent_internal_id";

  public static final String COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_CREATED_ON = "created_on";
  
  
  
  static void addGrouperProvAdobeUserComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!GrouperDdl5_12_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_14_0_addGrouperProvAdobeUserComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_PROV_ADOBE_USER;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "table to load adobe users into a sql for reporting, provisioning, and deprovisioning");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_CONFIG_ID, 
        "adobe config id identifies which adobe external system is being loaded");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_CONFIG_ID, 
        "Adobe internal ID for this user (used in web services)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_EMAIL, 
        "email address for the user");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_USER_NAME, 
        "user name for the user");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_STATUS, 
        "user status");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_TYPE, 
        "user type");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_FIRST_NAME, 
        "user first name");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_LAST_NAME,
        "user last name");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_DOMAIN,
        "user domain");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_USER_COUNTRY,
        "user country");
  }
  
  static void addGrouperProvAdobeGroupComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!GrouperDdl5_12_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_14_0_addGrouperProvAdobeGroupComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_PROV_ADOBE_GROUP;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "table to load adobe groups into a sql for reporting, provisioning, and deprovisioning");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_GROUP_CONFIG_ID, 
        "adobe config id identifies which adobe external system is being loaded");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_GROUP_GROUP_ID, 
        "Adobe internal ID for this group (used in web services)");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_GROUP_NAME, 
        "Group name");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_GROUP_TYPE, 
        "Group type");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_GROUP_PRODUCT_NAME, 
        "Group product name");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_GROUP_MEMBER_COUNT, 
        "member count");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_GROUP_LICENSE_QUOTA, 
        "license quota");
    
  }
  
  static void addGrouperProvAdobeMembershipComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!GrouperDdl5_12_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_14_0_addGrouperProvAdobeMembershipComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_PROV_ADOBE_MEMBERSHIP;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "table to load adobe memberships into a sql for reporting, provisioning, and deprovisioning");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_MEMBERSHIP_CONFIG_ID, 
        "adobe config id identifies which adobe external system is being loaded");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_MEMBERSHIP_GROUP_ID, 
        "Membership group id");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_PROV_ADOBE_MEMBERSHIP_USER_ID, 
        "Membership user id");
    
  }
  
  static void addGrouperProvAdobeUserIndex(DdlVersionBean ddlVersionBean, Database database) {
    if (!GrouperDdl5_12_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v5_14_0_addGrouperProvAdobeUserIndex", true)) {
      return;
    }
  
    Table grouperDuoTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        TABLE_GROUPER_PROV_ADOBE_USER);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperDuoTable.getName(), 
        "grouper_prov_adobe_user_idx1", true, 
        COLUMN_GROUPER_PROV_ADOBE_USER_EMAIL, COLUMN_GROUPER_PROV_ADOBE_USER_CONFIG_ID);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperDuoTable.getName(), 
        "grouper_prov_adobe_user_idx2", true, 
        COLUMN_GROUPER_PROV_ADOBE_USER_USER_NAME, COLUMN_GROUPER_PROV_ADOBE_USER_CONFIG_ID);
  }
  
  static void addGrouperProvAdobeGroupIndex(DdlVersionBean ddlVersionBean, Database database) {
    if (!GrouperDdl5_12_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v5_14_0_addGrouperProvAdobeGroupIndex", true)) {
      return;
    }
  
    Table grouperDuoTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        TABLE_GROUPER_PROV_ADOBE_GROUP);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperDuoTable.getName(), 
        "grouper_prov_adobe_group_idx1", true, 
        COLUMN_GROUPER_PROV_ADOBE_GROUP_GROUP_ID, COLUMN_GROUPER_PROV_ADOBE_GROUP_CONFIG_ID);
    
  }
  
  static void addGrouperProvAdobeMembershipIndex(DdlVersionBean ddlVersionBean, Database database) {
    if (!GrouperDdl5_12_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v5_14_0_addGrouperProvAdobeMembershipIndex", true)) {
      return;
    }
  
    Table grouperDuoTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        TABLE_GROUPER_PROV_ADOBE_MEMBERSHIP);
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, grouperDuoTable.getName(), 
        "grouper_prov_adobe_mship_fk1", "grouper_prov_adobe_group",
        GrouperUtil.toList("config_id", "group_id"), GrouperUtil.toList("config_id", "group_id"));
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, grouperDuoTable.getName(), 
        "grouper_prov_adobe_mship_fk2", "grouper_prov_adobe_user",
        GrouperUtil.toList("config_id", "user_id"), GrouperUtil.toList("config_id", "user_id"));
    
  }

  static void addGrouperProvAdobeUserTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!GrouperDdl5_12_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_14_0_addGrouperProvAdobeUserTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_PROV_ADOBE_USER;
  
    Table grouperFileTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_CONFIG_ID,
        Types.VARCHAR, "50", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_USER_ID,
        Types.VARCHAR, "100", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_EMAIL,
        Types.VARCHAR, "256", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_USER_NAME,
        Types.VARCHAR, "100", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_STATUS,
        Types.VARCHAR, "30", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_TYPE,
        Types.VARCHAR, "30", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_FIRST_NAME,
        Types.VARCHAR, "100", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_LAST_NAME,
        Types.VARCHAR, "100", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_DOMAIN,
        Types.VARCHAR, "100", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_USER_COUNTRY,
        Types.VARCHAR, "2", false, false);
    
  }
  
  static void addGrouperProvAdobeGroupTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!GrouperDdl5_12_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_14_0_addGrouperProvAdobeGroupTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_PROV_ADOBE_GROUP;
  
    Table grouperFileTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_GROUP_CONFIG_ID,
        Types.VARCHAR, "50", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_GROUP_GROUP_ID,
        Types.BIGINT, "20", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_GROUP_NAME,
        Types.VARCHAR, "2000", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_GROUP_TYPE,
        Types.VARCHAR, "100", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_GROUP_PRODUCT_NAME,
        Types.VARCHAR, "2000", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_GROUP_MEMBER_COUNT,
        Types.BIGINT, "20", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_GROUP_LICENSE_QUOTA,
        Types.BIGINT, "20", false, false);
    
  }
  
  static void addGrouperProvAdobeMembershipTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!GrouperDdl5_12_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_14_0_addGrouperProvAdobeMembershipTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_PROV_ADOBE_MEMBERSHIP;
  
    Table grouperFileTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_MEMBERSHIP_CONFIG_ID,
        Types.VARCHAR, "50", true, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_MEMBERSHIP_GROUP_ID,
        Types.BIGINT, "20", true, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperFileTable, COLUMN_GROUPER_PROV_ADOBE_MEMBERSHIP_USER_ID,
        Types.VARCHAR, "100", true, true);
    
  }
  
  static void addGrouperSqlCacheDependTypeTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!GrouperDdl5_12_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_14_0_addGrouperSqlCacheDependTypeTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_SQL_CACHE_DEPEND_TYPE;
  
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, COLUMN_GROUPER_SQL_CACHE_DEPEND_TYPE_INTERNAL_ID, Types.BIGINT, "20", true, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, COLUMN_GROUPER_SQL_CACHE_DEPEND_TYPE_DEPENDENCY_CATEGORY, Types.VARCHAR, "100", false, true);    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, COLUMN_GROUPER_SQL_CACHE_DEPEND_TYPE_NAME, Types.VARCHAR, "100", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, COLUMN_GROUPER_SQL_CACHE_DEPEND_TYPE_DESCRIPTION, Types.VARCHAR, "1024", false, true);
  }
  
  static void addGrouperSqlCacheDependencyTable(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!GrouperDdl5_12_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_14_0_addGrouperSqlCacheDependencyTable", true)) {
      return;
    }
    
    final String tableName = TABLE_GROUPER_SQL_CACHE_DEPENDENCY;
  
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_INTERNAL_ID, Types.BIGINT, "20", true, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_DEP_TYPE_INTERNAL_ID, Types.BIGINT, "20", false, true);    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_OWNER_INTERNAL_ID, Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_DEPENDENT_INTERNAL_ID, Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_CREATED_ON, Types.BIGINT, "20", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, table.getName(), 
        "grouper_sql_cache_dep_fk", TABLE_GROUPER_SQL_CACHE_DEPEND_TYPE,
        GrouperUtil.toList(COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_DEP_TYPE_INTERNAL_ID), GrouperUtil.toList(COLUMN_GROUPER_SQL_CACHE_DEPEND_TYPE_INTERNAL_ID));
  }
  
  static void addGrouperSqlCacheDependTypeIndexes(DdlVersionBean ddlVersionBean, Database database) {
    if (!GrouperDdl5_12_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v5_14_0_addGrouperSqlCacheDependTypeIndexes", true)) {
      return;
    }
  
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, TABLE_GROUPER_SQL_CACHE_DEPEND_TYPE);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(), 
        "grouper_sql_cache_deptype1_idx", true, 
        COLUMN_GROUPER_SQL_CACHE_DEPEND_TYPE_DEPENDENCY_CATEGORY, COLUMN_GROUPER_SQL_CACHE_DEPEND_TYPE_NAME);
  }
  
  static void addGrouperSqlCacheDependencyIndexes(DdlVersionBean ddlVersionBean, Database database) {
    if (!GrouperDdl5_12_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v5_14_0_addGrouperSqlCacheDependencyIndexes", true)) {
      return;
    }
  
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, TABLE_GROUPER_SQL_CACHE_DEPENDENCY);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(), 
        "grouper_sql_cache_dep1_idx", true, 
        COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_DEP_TYPE_INTERNAL_ID, COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_OWNER_INTERNAL_ID, COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_DEPENDENT_INTERNAL_ID);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(), 
        "grouper_sql_cache_dep2_idx", false, 
        COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_OWNER_INTERNAL_ID, COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_DEPENDENT_INTERNAL_ID);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(), 
        "grouper_sql_cache_dep3_idx", false, 
        COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_DEPENDENT_INTERNAL_ID);
  }
  
  static void addGrouperSqlCacheDependTypeComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!GrouperDdl5_12_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_14_0_addGrouperSqlCacheDependTypeComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_SQL_CACHE_DEPEND_TYPE;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "table to store types of dependencies");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_SQL_CACHE_DEPEND_TYPE_INTERNAL_ID, 
        "primary key of the table");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_SQL_CACHE_DEPEND_TYPE_DEPENDENCY_CATEGORY, 
        "category of dependency type");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_SQL_CACHE_DEPEND_TYPE_NAME, 
        "name of dependency type");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_SQL_CACHE_DEPEND_TYPE_DESCRIPTION, 
        "description of dependency type");
  }
  
  static void addGrouperSqlCacheDependencyComments(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!GrouperDdl5_12_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_14_0_addGrouperSqlCacheDependencyComments", true)) {
      return;
    }
  
    final String tableName = TABLE_GROUPER_SQL_CACHE_DEPENDENCY;
  
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, 
        "table to store dependencies");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_INTERNAL_ID, 
        "primary key of the table");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_DEP_TYPE_INTERNAL_ID, 
        "foreign key to grouper_sql_cache_depend_type table");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_OWNER_INTERNAL_ID, 
        "Something that something else is dependent on.  If something in the owner changes, then the dependent object might need to change");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_DEPENDENT_INTERNAL_ID, 
        "This is the internal id of the dependent object.  Check all the dependent objects if something changes in owner");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        tableName, 
        COLUMN_GROUPER_SQL_CACHE_DEPENDENCY_CREATED_ON, 
        "when this row was created");
  }
  
  static void addGrouperDataFieldAssignHstTableAndIndexes(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!GrouperDdl5_12_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_14_0_addGrouperDataFieldAssignHstTableAndIndexes", true)) {
      return;
    }
    
    final String tableName = "grouper_data_field_assign_hst";
  
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id", Types.BIGINT, "20", true, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "member_internal_id", Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "data_field_internal_id", Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "start_time", Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "end_time", Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "value_integer", Types.BIGINT, "20", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "value_dictionary_internal_id", Types.BIGINT, "20", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(), "data_field_assign_hst1_idx", false, "data_field_internal_id", "value_dictionary_internal_id", "end_time");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(), "data_field_assign_hst2_idx", false, "data_field_internal_id", "value_integer", "end_time");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(), "data_field_assign_hst3_idx", false, "member_internal_id", "data_field_internal_id", "value_dictionary_internal_id", "end_time");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(), "data_field_assign_hst4_idx", false, "member_internal_id", "data_field_internal_id", "value_integer", "end_time");
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, table.getName(), "data_field_assign_hst_fk_1", "grouper_members", GrouperUtil.toList("member_internal_id"), GrouperUtil.toList("internal_id"));
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, table.getName(), "data_field_assign_hst_fk_2", "grouper_data_field", GrouperUtil.toList("data_field_internal_id"), GrouperUtil.toList("internal_id"));
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, table.getName(), "data_field_assign_hst_fk_3", "grouper_dictionary", GrouperUtil.toList("value_dictionary_internal_id"), GrouperUtil.toList("internal_id"));
  }
  
  static void addGrouperDataRowAssignHstTableAndIndexes(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!GrouperDdl5_12_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_14_0_addGrouperDataRowAssignHstTableAndIndexes", true)) {
      return;
    }
    
    final String tableName = "grouper_data_row_assign_hst";
  
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id", Types.BIGINT, "20", true, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "member_internal_id", Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "data_row_internal_id", Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "data_row_assign_internal_id", Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "start_time", Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "end_time", Types.BIGINT, "20", false, true);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(), "data_row_assign_hst1_idx", false, "data_row_internal_id", "end_time");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(), "data_row_assign_hst2_idx", false, "data_row_assign_internal_id", "end_time");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(), "data_row_assign_hst3_idx", false, "member_internal_id", "data_row_internal_id", "end_time");
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, table.getName(), "data_row_assign_hst_fk_1", "grouper_members", GrouperUtil.toList("member_internal_id"), GrouperUtil.toList("internal_id"));
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, table.getName(), "data_row_assign_hst_fk_2", "grouper_data_row", GrouperUtil.toList("data_row_internal_id"), GrouperUtil.toList("internal_id"));
  }
  
  static void addGrouperDataRowFieldAsnHstTableAndIndexes(Database database, DdlVersionBean ddlVersionBean) {
    
    if (!GrouperDdl5_12_0.buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v5_14_0_addGrouperDataRowFieldAsnHstTableAndIndexes", true)) {
      return;
    }
    
    final String tableName = "grouper_data_row_field_asn_hst";
  
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id", Types.BIGINT, "20", true, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "data_row_assign_internal_id", Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "data_field_internal_id", Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "value_integer", Types.BIGINT, "20", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "value_dictionary_internal_id", Types.BIGINT, "20", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(), "data_row_field_asn_hst1_idx", false, "data_row_assign_internal_id", "data_field_internal_id", "value_dictionary_internal_id");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(), "data_row_field_asn_hst2_idx", false, "data_row_assign_internal_id", "data_field_internal_id", "value_integer");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(), "data_row_field_asn_hst3_idx", false, "data_field_internal_id", "value_dictionary_internal_id");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(), "data_row_field_asn_hst4_idx", false, "data_field_internal_id", "value_integer");
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, table.getName(), "data_row_field_asn_hst_fk_1", "grouper_data_field", GrouperUtil.toList("data_field_internal_id"), GrouperUtil.toList("internal_id"));
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, table.getName(), "data_row_field_asn_hst_fk_2", "grouper_dictionary", GrouperUtil.toList("value_dictionary_internal_id"), GrouperUtil.toList("internal_id"));
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, table.getName(), "data_row_field_asn_hst_fk_3", "grouper_data_row_assign_hst", GrouperUtil.toList("data_row_assign_internal_id"), GrouperUtil.toList("internal_id"));
  }
}
