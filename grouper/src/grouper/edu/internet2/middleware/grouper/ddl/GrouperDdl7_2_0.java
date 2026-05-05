/**
 * Copyright 2026 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * DDL for Grouper 7.2.0: grouper_prov_* generic provisioning tables and
 * the grouper_sync.internal_id column used to link them.
 */
public class GrouperDdl7_2_0 {

  private static final String INTERNAL_ID_COMMENT =
      "internal integer id for this table.  Do not refer to this outside of Grouper.  "
          + "This will differ per env (dev/test/prod)";

  private static final String LAST_UPDATED_COMMENT =
      "timestamp in micros since 1970 when this record was last updated";

  /**
   * if building to this version at least
   * @param ddlVersionBean
   * @return true if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    return GrouperDdl.V47.getVersion() <= buildingToVersion;
  }

  // ------- grouper_sync.internal_id -------

  static void addGrouperSyncInternalId(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperSyncInternalId", true)) {
      return;
    }
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_sync");
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
        Types.BIGINT, "20", false, true);
  }

  static void addGrouperSyncInternalIdIndex(DdlVersionBean ddlVersionBean, Database database) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperSyncInternalIdIndex", true)) {
      return;
    }
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_sync",
        "grouper_sync_internal_id_idx", true,
        "internal_id");
  }

  static void addGrouperSyncInternalIdComment(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperSyncInternalIdComment", true)) {
      return;
    }
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        "grouper_sync", "internal_id", INTERNAL_ID_COMMENT);
  }

  // ------- grouper_prov_group -------

  static void addGrouperProvGroupTable(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvGroupTable", true)) {
      return;
    }
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_prov_group");

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
        Types.BIGINT, "20", true, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "grouper_sync_internal_id",
        Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "group_internal_id",
        Types.BIGINT, "20", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "target_group_id",
        Types.VARCHAR, "1000", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "last_updated",
        Types.BIGINT, "20", false, true);
  }

  static void addGrouperProvGroupIndexes(DdlVersionBean ddlVersionBean, Database database) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvGroupIndexes", true)) {
      return;
    }
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_group",
        "grouper_prov_grp_idx0", false, "grouper_sync_internal_id");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_group",
        "grouper_prov_grp_idx1", false, "group_internal_id");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_group",
        "grouper_prov_grp_idx2", false, "target_group_id");
  }

  static void addGrouperProvGroupComments(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvGroupComments", true)) {
      return;
    }
    final String t = "grouper_prov_group";
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, t,
        "Group mappings in a target system for a provisioner");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "internal_id", INTERNAL_ID_COMMENT);
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "grouper_sync_internal_id",
        "foreign key to grouper_sync.internal_id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "group_internal_id",
        "optional foreign key to grouper_groups.internal_id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "target_group_id",
        "target system group id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "last_updated", LAST_UPDATED_COMMENT);
  }

  // ------- grouper_prov_group_attr -------

  static void addGrouperProvGroupAttrTable(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvGroupAttrTable", true)) {
      return;
    }
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_prov_group_attr");

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
        Types.BIGINT, "20", true, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "grouper_sync_internal_id",
        Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "attribute_name",
        Types.VARCHAR, "500", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "attribute_type",
        Types.VARCHAR, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "last_updated",
        Types.BIGINT, "20", false, true);
  }

  static void addGrouperProvGroupAttrIndexes(DdlVersionBean ddlVersionBean, Database database) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvGroupAttrIndexes", true)) {
      return;
    }
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_group_attr",
        "grouper_prov_grpat_idx0", false, "grouper_sync_internal_id");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_group_attr",
        "grouper_prov_grpat_idx1", true, "grouper_sync_internal_id", "attribute_name");
  }

  static void addGrouperProvGroupAttrComments(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvGroupAttrComments", true)) {
      return;
    }
    final String t = "grouper_prov_group_attr";
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, t,
        "Provisioner group attribute name catalog (one row per provisioner per attribute name)");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "internal_id", INTERNAL_ID_COMMENT);
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "grouper_sync_internal_id",
        "foreign key to grouper_sync.internal_id; the catalog of group attribute names is scoped per provisioner");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "attribute_name",
        "group attribute name e.g. group_name or group_description");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "attribute_type",
        "attribute type e.g. string, int, boolean, timestamp");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "last_updated", LAST_UPDATED_COMMENT);
  }

  // ------- grouper_prov_group_attr_value -------

  static void addGrouperProvGroupAttrValueTable(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvGroupAttrValueTable", true)) {
      return;
    }
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        "grouper_prov_group_attr_value");

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
        Types.BIGINT, "20", true, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "prov_group_attr_internal_id",
        Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "prov_group_internal_id",
        Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "value_integer",
        Types.BIGINT, "20", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "value_dictionary_internal_id",
        Types.BIGINT, "20", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "last_updated",
        Types.BIGINT, "20", false, true);
  }

  static void addGrouperProvGroupAttrValueIndexes(DdlVersionBean ddlVersionBean, Database database) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvGroupAttrValueIndexes", true)) {
      return;
    }
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_group_attr_value",
        "grouper_prov_grpatv_idx0", false, "prov_group_attr_internal_id");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_group_attr_value",
        "grouper_prov_grpatv_idx1", false, "prov_group_internal_id");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_group_attr_value",
        "grouper_prov_grpatv_idx2", false, "value_dictionary_internal_id");
  }

  static void addGrouperProvGroupAttrValueComments(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvGroupAttrValueComments", true)) {
      return;
    }
    final String t = "grouper_prov_group_attr_value";
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, t,
        "Provisioner group attribute values");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "internal_id", INTERNAL_ID_COMMENT);
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "prov_group_attr_internal_id",
        "foreign key to grouper_prov_group_attr.internal_id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "prov_group_internal_id",
        "foreign key to grouper_prov_group.internal_id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "value_integer",
        "integer value used for int, boolean, or timestamp");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "value_dictionary_internal_id",
        "foreign key to grouper_dictionary.internal_id for string values");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "last_updated", LAST_UPDATED_COMMENT);
  }

  // ------- grouper_prov_user -------

  static void addGrouperProvUserTable(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvUserTable", true)) {
      return;
    }
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_prov_user");

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
        Types.BIGINT, "20", true, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "grouper_sync_internal_id",
        Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "member_internal_id",
        Types.BIGINT, "20", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "target_user_id",
        Types.VARCHAR, "1000", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "last_updated",
        Types.BIGINT, "20", false, true);
  }

  static void addGrouperProvUserIndexes(DdlVersionBean ddlVersionBean, Database database) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvUserIndexes", true)) {
      return;
    }
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_user",
        "grouper_prov_user_idx0", false, "grouper_sync_internal_id");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_user",
        "grouper_prov_user_idx1", false, "member_internal_id");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_user",
        "grouper_prov_user_idx2", false, "target_user_id");
  }

  static void addGrouperProvUserComments(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvUserComments", true)) {
      return;
    }
    final String t = "grouper_prov_user";
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, t, "Provisioner users");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "internal_id", INTERNAL_ID_COMMENT);
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "grouper_sync_internal_id",
        "foreign key to grouper_sync.internal_id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "member_internal_id",
        "optional foreign key to grouper_members.internal_id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "target_user_id",
        "target system user id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "last_updated", LAST_UPDATED_COMMENT);
  }

  // ------- grouper_prov_user_attr -------

  static void addGrouperProvUserAttrTable(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvUserAttrTable", true)) {
      return;
    }
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_prov_user_attr");

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
        Types.BIGINT, "20", true, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "grouper_sync_internal_id",
        Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "attribute_name",
        Types.VARCHAR, "500", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "attribute_type",
        Types.VARCHAR, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "last_updated",
        Types.BIGINT, "20", false, true);
  }

  static void addGrouperProvUserAttrIndexes(DdlVersionBean ddlVersionBean, Database database) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvUserAttrIndexes", true)) {
      return;
    }
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_user_attr",
        "grouper_prov_userat_idx0", false, "grouper_sync_internal_id");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_user_attr",
        "grouper_prov_userat_idx1", true, "grouper_sync_internal_id", "attribute_name");
  }

  static void addGrouperProvUserAttrComments(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvUserAttrComments", true)) {
      return;
    }
    final String t = "grouper_prov_user_attr";
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, t,
        "Provisioner user attribute name catalog (one row per provisioner per attribute name)");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "internal_id", INTERNAL_ID_COMMENT);
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "grouper_sync_internal_id",
        "foreign key to grouper_sync.internal_id; the catalog of user attribute names is scoped per provisioner");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "attribute_name",
        "user attribute name e.g. user_name or user_email");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "attribute_type",
        "attribute type e.g. string, int, boolean, timestamp");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "last_updated", LAST_UPDATED_COMMENT);
  }

  // ------- grouper_prov_user_attr_value -------

  static void addGrouperProvUserAttrValueTable(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvUserAttrValueTable", true)) {
      return;
    }
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        "grouper_prov_user_attr_value");

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
        Types.BIGINT, "20", true, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "prov_user_attr_internal_id",
        Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "prov_user_internal_id",
        Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "value_integer",
        Types.BIGINT, "20", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "value_dictionary_internal_id",
        Types.BIGINT, "20", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "last_updated",
        Types.BIGINT, "20", false, true);
  }

  static void addGrouperProvUserAttrValueIndexes(DdlVersionBean ddlVersionBean, Database database) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvUserAttrValueIndexes", true)) {
      return;
    }
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_user_attr_value",
        "grouper_prov_useratv_idx0", false, "prov_user_attr_internal_id");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_user_attr_value",
        "grouper_prov_useratv_idx1", false, "prov_user_internal_id");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_user_attr_value",
        "grouper_prov_useratv_idx2", false, "value_dictionary_internal_id");
  }

  static void addGrouperProvUserAttrValueComments(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvUserAttrValueComments", true)) {
      return;
    }
    final String t = "grouper_prov_user_attr_value";
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, t,
        "Provisioner user attribute values");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "internal_id", INTERNAL_ID_COMMENT);
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "prov_user_attr_internal_id",
        "foreign key to grouper_prov_user_attr.internal_id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "prov_user_internal_id",
        "foreign key to grouper_prov_user.internal_id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "value_integer",
        "integer value used for int, boolean, or timestamp");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "value_dictionary_internal_id",
        "foreign key to grouper_dictionary.internal_id for string values");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "last_updated", LAST_UPDATED_COMMENT);
  }

  // ------- grouper_prov_mship_role -------

  static void addGrouperProvMshipRoleTable(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvMshipRoleTable", true)) {
      return;
    }
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_prov_mship_role");

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
        Types.BIGINT, "20", true, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "role_name",
        Types.VARCHAR, "30", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "grouper_sync_internal_id",
        Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "last_updated",
        Types.BIGINT, "20", false, true);
  }

  static void addGrouperProvMshipRoleIndexes(DdlVersionBean ddlVersionBean, Database database) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvMshipRoleIndexes", true)) {
      return;
    }
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_mship_role",
        "grouper_prov_mshipr_idx0", false, "grouper_sync_internal_id");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_mship_role",
        "grouper_prov_mshipr_idx1", false, "grouper_sync_internal_id", "role_name");
  }

  static void addGrouperProvMshipRoleComments(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvMshipRoleComments", true)) {
      return;
    }
    final String t = "grouper_prov_mship_role";
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, t, "Provisioner membership roles");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "internal_id", INTERNAL_ID_COMMENT);
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "role_name",
        "membership role name");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "grouper_sync_internal_id",
        "foreign key to grouper_sync.internal_id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "last_updated", LAST_UPDATED_COMMENT);
  }

  // ------- grouper_prov_mship -------

  static void addGrouperProvMshipTable(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvMshipTable", true)) {
      return;
    }
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_prov_mship");

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
        Types.BIGINT, "20", true, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "grouper_sync_internal_id",
        Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "prov_user_internal_id",
        Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "prov_group_internal_id",
        Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "prov_mship_role_internal_id",
        Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "last_updated",
        Types.BIGINT, "20", false, true);
  }

  static void addGrouperProvMshipIndexes(DdlVersionBean ddlVersionBean, Database database) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvMshipIndexes", true)) {
      return;
    }
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_mship",
        "grouper_prov_mship_idx0", false, "grouper_sync_internal_id");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_mship",
        "grouper_prov_mship_idx1", false, "prov_user_internal_id");
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_prov_mship",
        "grouper_prov_mship_idx2", false, "prov_group_internal_id");
  }

  static void addGrouperProvMshipComments(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addGrouperProvMshipComments", true)) {
      return;
    }
    final String t = "grouper_prov_mship";
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, t, "Provisioner memberships");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "internal_id", INTERNAL_ID_COMMENT);
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "grouper_sync_internal_id",
        "foreign key to grouper_sync.internal_id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "prov_user_internal_id",
        "foreign key to grouper_prov_user.internal_id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "prov_group_internal_id",
        "foreign key to grouper_prov_group.internal_id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "prov_mship_role_internal_id",
        "foreign key to grouper_prov_mship_role.internal_id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "last_updated", LAST_UPDATED_COMMENT);
  }

  // ------- views -------

  static void addProvViews(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_2_0_addProvViews", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_prov_user_attr_v",
        "View of provisioner users joined with their attributes, grouper members, and grouper_sync_member provisioning state. Fans out to one row per user per attribute-value; users with zero attributes appear as one row with null attribute columns",
        GrouperUtil.toSet(
            "provisioner_name", "subject_source_id", "subject_id", "subject_identifier0", "subject_identifier1",
            "attribute_name", "value_string", "value_integer", "attribute_type",
            "member_name", "member_description",
            "provisionable", "in_target", "in_target_insert_or_exists",
            "provisionable_start", "provisionable_end", "in_target_start", "in_target_end",
            "sync_engine", "target_user_id", "grouper_sync_id",
            "user_last_updated", "value_last_updated",
            "prov_user_internal_id", "grouper_sync_internal_id",
            "member_id", "member_id_index", "member_internal_id"),
        GrouperUtil.toSet(
            "provisioner name from grouper_sync",
            "subject source id of the grouper member",
            "subject id of the grouper member",
            "first subject identifier of the grouper member",
            "second subject identifier of the grouper member",
            "user attribute name e.g. user_name or user_email",
            "string attribute value from the grouper dictionary",
            "integer attribute value used for int, boolean, or timestamp types",
            "attribute type e.g. string, int, boolean, timestamp",
            "subject name from grouper_members",
            "subject description from grouper_members",
            "T/F if the grouper member is currently provisionable to this target",
            "T/F if the grouper member currently exists in the target",
            "T/F: T if grouper inserted the user into the target, F if it already existed",
            "timestamp when the grouper member became provisionable to this target",
            "timestamp when the grouper member stopped being provisionable to this target",
            "timestamp when the grouper member first appeared in the target",
            "timestamp when the grouper member was last removed from the target",
            "provisioner engine from grouper_sync",
            "target system user id",
            "grouper_sync uuid",
            "timestamp in micros since 1970 when the grouper_prov_user row was last updated",
            "timestamp in micros since 1970 when the grouper_prov_user_attr_value row was last updated",
            "foreign key to grouper_prov_user.internal_id",
            "foreign key to grouper_sync.internal_id",
            "grouper_members.id uuid",
            "grouper_members.id_index integer id",
            "foreign key to grouper_members.internal_id"),
        "select gs.provisioner_name, gm.subject_source as subject_source_id, gm.subject_id, "
            + "gm.subject_identifier0, gm.subject_identifier1, pua.attribute_name, "
            + "gd.the_text as value_string, puav.value_integer, pua.attribute_type, "
            + "gm.name as member_name, gm.description as member_description, "
            + "gsm.provisionable, gsm.in_target, gsm.in_target_insert_or_exists, "
            + "gsm.provisionable_start, gsm.provisionable_end, gsm.in_target_start, gsm.in_target_end, "
            + "gs.sync_engine, pu.target_user_id, gs.id as grouper_sync_id, "
            + "pu.last_updated as user_last_updated, puav.last_updated as value_last_updated, "
            + "pu.internal_id as prov_user_internal_id, pu.grouper_sync_internal_id, "
            + "gm.id as member_id, gm.id_index as member_id_index, pu.member_internal_id "
            + "from grouper_prov_user pu "
            + "left join grouper_sync gs on gs.internal_id = pu.grouper_sync_internal_id "
            + "left join grouper_members gm on gm.internal_id = pu.member_internal_id "
            + "left join grouper_sync_member gsm on gsm.grouper_sync_id = gs.id and gsm.member_id = gm.id "
            + "left join grouper_prov_user_attr_value puav on puav.prov_user_internal_id = pu.internal_id "
            + "left join grouper_prov_user_attr pua on pua.internal_id = puav.prov_user_attr_internal_id "
            + "left join grouper_dictionary gd on gd.internal_id = puav.value_dictionary_internal_id");

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_prov_group_attr_v",
        "View of provisioner groups joined with their attributes, grouper_groups, and grouper_sync_group provisioning state. Fans out to one row per group per attribute-value; groups with zero attributes appear as one row with null attribute columns",
        GrouperUtil.toSet(
            "provisioner_name", "group_name",
            "attribute_name", "value_string", "value_integer", "attribute_type",
            "provisionable", "in_target", "in_target_insert_or_exists",
            "provisionable_start", "provisionable_end", "in_target_start", "in_target_end",
            "group_description", "group_extension", "group_display_extension",
            "sync_engine", "target_group_id", "grouper_sync_id",
            "group_last_updated", "value_last_updated",
            "prov_group_internal_id", "grouper_sync_internal_id",
            "group_id", "group_id_index", "group_internal_id"),
        GrouperUtil.toSet(
            "provisioner name from grouper_sync",
            "group system name from grouper_groups",
            "group attribute name",
            "string attribute value from the grouper dictionary",
            "integer attribute value used for int, boolean, or timestamp types",
            "attribute type e.g. string, int, boolean, timestamp",
            "T/F if the group is currently provisionable to this target",
            "T/F if the group currently exists in the target",
            "T/F: T if grouper inserted the group into the target, F if it already existed",
            "timestamp when the group became provisionable to this target",
            "timestamp when the group stopped being provisionable to this target",
            "timestamp when the group first appeared in the target",
            "timestamp when the group was last removed from the target",
            "group description from grouper_groups",
            "group extension (last segment of the group name) from grouper_groups",
            "group display extension (last segment of the display name) from grouper_groups",
            "provisioner engine from grouper_sync",
            "target system group id",
            "grouper_sync uuid",
            "timestamp in micros since 1970 when the grouper_prov_group row was last updated",
            "timestamp in micros since 1970 when the grouper_prov_group_attr_value row was last updated",
            "foreign key to grouper_prov_group.internal_id",
            "foreign key to grouper_sync.internal_id",
            "grouper_groups.id uuid",
            "grouper_groups.id_index integer id",
            "foreign key to grouper_groups.internal_id"),
        "select gs.provisioner_name, gg.name as group_name, pga.attribute_name, "
            + "gd.the_text as value_string, pgav.value_integer, pga.attribute_type, "
            + "gsg.provisionable, gsg.in_target, gsg.in_target_insert_or_exists, "
            + "gsg.provisionable_start, gsg.provisionable_end, gsg.in_target_start, gsg.in_target_end, "
            + "gg.description as group_description, gg.extension as group_extension, "
            + "gg.display_extension as group_display_extension, "
            + "gs.sync_engine, pg.target_group_id, gs.id as grouper_sync_id, "
            + "pg.last_updated as group_last_updated, pgav.last_updated as value_last_updated, "
            + "pg.internal_id as prov_group_internal_id, pg.grouper_sync_internal_id, "
            + "gg.id as group_id, gg.id_index as group_id_index, pg.group_internal_id "
            + "from grouper_prov_group pg "
            + "left join grouper_sync gs on gs.internal_id = pg.grouper_sync_internal_id "
            + "left join grouper_groups gg on gg.internal_id = pg.group_internal_id "
            + "left join grouper_sync_group gsg on gsg.grouper_sync_id = gs.id and gsg.group_id = gg.id "
            + "left join grouper_prov_group_attr_value pgav on pgav.prov_group_internal_id = pg.internal_id "
            + "left join grouper_prov_group_attr pga on pga.internal_id = pgav.prov_group_attr_internal_id "
            + "left join grouper_dictionary gd on gd.internal_id = pgav.value_dictionary_internal_id");

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_prov_mship_v",
        "View of provisioner memberships joined with provisioner users/groups, grouper_members, grouper_groups, and grouper_sync_membership provisioning state",
        GrouperUtil.toSet(
            "provisioner_name", "subject_source_id", "subject_id", "subject_identifier0", "subject_identifier1",
            "member_name", "group_name", "role_name",
            "in_target", "in_target_insert_or_exists", "in_target_start", "in_target_end",
            "mship_last_updated", "sync_engine", "target_user_id", "target_group_id",
            "grouper_sync_id", "grouper_sync_mship_id",
            "prov_mship_internal_id", "prov_user_internal_id", "prov_group_internal_id", "grouper_sync_internal_id",
            "member_id", "member_id_index", "member_internal_id",
            "group_id", "group_id_index", "group_internal_id"),
        GrouperUtil.toSet(
            "provisioner name from grouper_sync",
            "subject source id of the grouper member",
            "subject id of the grouper member",
            "first subject identifier of the grouper member",
            "second subject identifier of the grouper member",
            "subject name from grouper_members",
            "group system name from grouper_groups",
            "membership role name from grouper_prov_mship_role",
            "T/F if the membership currently exists in the target",
            "T/F: T if grouper inserted the membership into the target, F if it already existed",
            "timestamp when the membership first appeared in the target",
            "timestamp when the membership was last removed from the target",
            "timestamp in micros since 1970 when the grouper_prov_mship row was last updated",
            "provisioner engine from grouper_sync",
            "target system user id",
            "target system group id",
            "grouper_sync uuid",
            "target system membership id from grouper_sync_membership.membership_id",
            "foreign key to grouper_prov_mship.internal_id",
            "foreign key to grouper_prov_user.internal_id",
            "foreign key to grouper_prov_group.internal_id",
            "foreign key to grouper_sync.internal_id",
            "grouper_members.id uuid",
            "grouper_members.id_index integer id",
            "foreign key to grouper_members.internal_id",
            "grouper_groups.id uuid",
            "grouper_groups.id_index integer id",
            "foreign key to grouper_groups.internal_id"),
        "select gs.provisioner_name, gm.subject_source as subject_source_id, gm.subject_id, "
            + "gm.subject_identifier0, gm.subject_identifier1, gm.name as member_name, "
            + "gg.name as group_name, pmr.role_name, "
            + "gsms.in_target, gsms.in_target_insert_or_exists, gsms.in_target_start, gsms.in_target_end, "
            + "pm.last_updated as mship_last_updated, gs.sync_engine, "
            + "pu.target_user_id, pg.target_group_id, "
            + "gs.id as grouper_sync_id, gsms.membership_id as grouper_sync_mship_id, "
            + "pm.internal_id as prov_mship_internal_id, pm.prov_user_internal_id, "
            + "pm.prov_group_internal_id, pm.grouper_sync_internal_id, "
            + "gm.id as member_id, gm.id_index as member_id_index, pu.member_internal_id, "
            + "gg.id as group_id, gg.id_index as group_id_index, pg.group_internal_id "
            + "from grouper_prov_mship pm "
            + "left join grouper_prov_user pu on pu.internal_id = pm.prov_user_internal_id "
            + "left join grouper_prov_group pg on pg.internal_id = pm.prov_group_internal_id "
            + "left join grouper_prov_mship_role pmr on pmr.internal_id = pm.prov_mship_role_internal_id "
            + "left join grouper_sync gs on gs.internal_id = pm.grouper_sync_internal_id "
            + "left join grouper_members gm on gm.internal_id = pu.member_internal_id "
            + "left join grouper_groups gg on gg.internal_id = pg.group_internal_id "
            + "left join grouper_sync_member gsm_u on gsm_u.grouper_sync_id = gs.id and gsm_u.member_id = gm.id "
            + "left join grouper_sync_group gsg on gsg.grouper_sync_id = gs.id and gsg.group_id = gg.id "
            + "left join grouper_sync_membership gsms on gsms.grouper_sync_id = gs.id "
            + "and gsms.grouper_sync_group_id = gsg.id and gsms.grouper_sync_member_id = gsm_u.id");
  }
}
