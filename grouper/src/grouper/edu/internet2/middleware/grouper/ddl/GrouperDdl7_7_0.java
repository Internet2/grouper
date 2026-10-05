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
import edu.internet2.middleware.grouper.file.GrouperFile;

/**
 * DDL for Grouper 7.7.0 (model for fresh installs and the database compare; existing databases are
 * changed by UpgradeTaskV45).
 *
 * <p>GRP-7439: grouper_file.created_on_micros and grouper_file.updated_on_micros.</p>
 * <p>GRP-7446: grouper_file.file_contents_blob.</p>
 */
public class GrouperDdl7_7_0 {

  /**
   * if building to this version at least
   * @param ddlVersionBean
   * @return true if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    return GrouperDdl.V47.getVersion() <= buildingToVersion;
  }

  // ------- grouper_file created / updated timestamps (GRP-7439) -------

  /**
   * GRP-7439: add grouper_file.created_on_micros and updated_on_micros, NOT NULL (the DAO sets both
   * on insert, and UpgradeTaskV45 backfills existing rows before making them NOT NULL)
   * @param database
   * @param ddlVersionBean
   */
  static void addGrouperFileTimestampColumns(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_7_0_addGrouperFileTimestampColumns", true)) {
      return;
    }
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, GrouperFile.TABLE_GROUPER_FILE);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperFile.COLUMN_CREATED_ON_MICROS,
        Types.BIGINT, "20", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperFile.COLUMN_UPDATED_ON_MICROS,
        Types.BIGINT, "20", false, true);
  }

  /**
   * GRP-7439: comments for grouper_file.created_on_micros and updated_on_micros
   * @param database
   * @param ddlVersionBean
   */
  static void addGrouperFileTimestampComments(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_7_0_addGrouperFileTimestampComments", true)) {
      return;
    }
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GrouperFile.TABLE_GROUPER_FILE,
        GrouperFile.COLUMN_CREATED_ON_MICROS,
        "timestamp in micros since 1970 when this file row was created");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GrouperFile.TABLE_GROUPER_FILE,
        GrouperFile.COLUMN_UPDATED_ON_MICROS,
        "timestamp in micros since 1970 when this file row was last saved");
  }

  // ------- grouper_file binary contents (GRP-7446) -------

  /**
   * GRP-7446: add grouper_file.file_contents_blob, nullable (only binary files use it).  Types.BLOB with no size
   * like the quartz job_data columns: postgres BYTEA, oracle BLOB, mysql LONGBLOB
   * @param database
   * @param ddlVersionBean
   */
  static void addGrouperFileBlobColumn(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_7_0_addGrouperFileBlobColumn", true)) {
      return;
    }
    Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, GrouperFile.TABLE_GROUPER_FILE);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, GrouperFile.COLUMN_FILE_CONTENTS_BLOB,
        Types.BLOB, null, false, false);
  }

  /**
   * GRP-7446: comment for grouper_file.file_contents_blob
   * @param database
   * @param ddlVersionBean
   */
  static void addGrouperFileBlobComments(Database database, DdlVersionBean ddlVersionBean) {
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    if (ddlVersionBean.didWeDoThis("v7_7_0_addGrouperFileBlobComments", true)) {
      return;
    }
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean,
        GrouperFile.TABLE_GROUPER_FILE,
        GrouperFile.COLUMN_FILE_CONTENTS_BLOB,
        "binary contents of the file (zip, xlsx, etc), null if the contents are text in file_contents_varchar or file_contents_clob");
  }

}
