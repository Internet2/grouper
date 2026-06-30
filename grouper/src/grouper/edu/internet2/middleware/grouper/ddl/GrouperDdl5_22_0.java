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

/**
 * DDL model for Grouper 5.22.0: the user lifecycle event tables
 * grouper_lifecycle_event_config and grouper_lifecycle_event.
 *
 * These tables were originally created on existing databases by UpgradeTaskV37 (raw SQL) and
 * commented by UpgradeTaskV39, but were never represented in the ddlutils model.  Because the
 * deep DDL compare (GrouperDdlCompare / "gsh -registry -deep -check") builds its expected schema
 * from the model, these tables were a blind spot: a manual drop went undetected.  This class
 * back-fills the model so the compare can validate them.  The matching foreign keys are declared
 * in GrouperDdl.addAllForeignKeysViewsEtc() (the compare adds foreign keys in a separate pass).
 */
public class GrouperDdl5_22_0 {

  /**
   * if building to this version at least
   * @param ddlVersionBean
   * @return true if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    return GrouperDdl.V47.getVersion() <= buildingToVersion;
  }

  // ------- grouper_lifecycle_event_config -------

  static void addGrouperLifecycleEventConfigTable(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v5_22_0_addGrouperLifecycleEventConfigTable", true)) {
      return;
    }
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_lifecycle_event_config");

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
        Types.BIGINT, "20", true, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "config_id",
        Types.VARCHAR, "100", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "group_internal_id",
        Types.BIGINT, "20", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "stem_id_index",
        Types.BIGINT, "20", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "data_field_internal_id",
        Types.BIGINT, "20", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "data_row_internal_id",
        Types.BIGINT, "20", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "created_on_micros",
        Types.BIGINT, "20", false, true);
  }

  static void addGrouperLifecycleEventConfigIndexes(DdlVersionBean ddlVersionBean, Database database) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v5_22_0_addGrouperLifecycleEventConfigIndexes", true)) {
      return;
    }
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_lifecycle_event_config",
        "grouper_lcycle_evnt_cnfg_idx", true, "config_id");
  }

  static void addGrouperLifecycleEventConfigComments(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v5_22_0_addGrouperLifecycleEventConfigComments", true)) {
      return;
    }
    final String t = "grouper_lifecycle_event_config";
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, t, "table to store user lifecycle event configs");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "internal_id", "integer id for this table");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "config_id", "unique user friendly id for the config");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "group_internal_id", "group internal id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "stem_id_index", "folder id index");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "data_field_internal_id", "data field internal id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "data_row_internal_id", "data row internal id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "created_on_micros", "when this event config was created");
  }

  // ------- grouper_lifecycle_event -------

  static void addGrouperLifecycleEventTable(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v5_22_0_addGrouperLifecycleEventTable", true)) {
      return;
    }
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, "grouper_lifecycle_event");

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "internal_id",
        Types.BIGINT, "20", true, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "grpr_lcycl_evnt_cnfg_intrnl_id",
        Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "member_internal_id",
        Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "event_micros",
        Types.BIGINT, "20", false, true);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "ntrl_lng_priv_dic_intrnl_id",
        Types.BIGINT, "20", false, false);
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "ntrl_lng_unpriv_dic_intrnl_id",
        Types.BIGINT, "20", false, false);
  }

  static void addGrouperLifecycleEventIndexes(DdlVersionBean ddlVersionBean, Database database) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v5_22_0_addGrouperLifecycleEventIndexes", true)) {
      return;
    }
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_lifecycle_event",
        "grouper_lifecycle_event_uniq_idx", true,
        "grpr_lcycl_evnt_cnfg_intrnl_id", "member_internal_id", "event_micros");
  }

  static void addGrouperLifecycleEventComments(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v5_22_0_addGrouperLifecycleEventComments", true)) {
      return;
    }
    final String t = "grouper_lifecycle_event";
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, t, "table to store user lifecycle events");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "internal_id", "integer id for this table");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "grpr_lcycl_evnt_cnfg_intrnl_id", "internal id of the grouper lifecycle config table");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "member_internal_id", "member internal id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "event_micros", "when the event occurred");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "ntrl_lng_priv_dic_intrnl_id", "dictionary table internal id");
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, t, "ntrl_lng_unpriv_dic_intrnl_id", "dictionary table internal id");
  }

}
