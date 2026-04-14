/**
 * Copyright 2024 Internet2
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

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

/**
 * DDL for Grouper 6.2.0
 * @author mchyzer
 */
public class GrouperDdl6_2_0 {

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
   * add indexes on grouper_data_field_assign for data_field_internal_id with value_dictionary_internal_id and value_integer
   * @param ddlVersionBean
   * @param database
   */
  static void addGrouperDataFieldAssignIndexes(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v6_2_0_addGrouperDataFieldAssignIndexes", true)) {
      return;
    }

    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        "grouper_data_field_assign");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(),
        "fld_assgn_field_dict_idx", false,
        "data_field_internal_id", "value_dictionary_internal_id");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(),
        "fld_assgn_field_int_idx", false,
        "data_field_internal_id", "value_integer");
  }

  /**
   * add index on grouper_group_set for member_id and member_field_id
   * @param ddlVersionBean
   * @param database
   */
  static void addGrouperGroupSetMemberIndex(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v6_2_0_addGrouperGroupSetMemberIndex", true)) {
      return;
    }

    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        "grouper_group_set");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, table.getName(),
        "group_set_member_member_field_idx", false,
        "member_id", "member_field_id");
  }

}
