package edu.internet2.middleware.grouper.app.upgradeTasks;


import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdl5_0_0;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * v7 upgrade task that bundles the DDL changes shipping in this release (one task per release; add new
 * release DDL here rather than creating another task).
 *
 * <p>GRP-7417: on postgres, change grouper_file.file_contents_clob from VARCHAR(10000000) to TEXT.  The
 * varchar capped stored file contents at about 10MB (and postgres will not allow a varchar much larger
 * than that), TEXT is unbounded.  Postgres only: oracle is already CLOB and mysql is MEDIUMTEXT.  No views
 * reference grouper_file, so the ALTER is not blocked.  Idempotent: skipped once the column is already
 * text.</p>
 *
 * <p>GRP-7439: add grouper_file.created_on_micros and grouper_file.updated_on_micros (bigint, micros since
 * 1970), backfill rows where they are null with the current time, then make both NOT NULL.  Existing rows
 * then look new, so time-based cleanup of files never deletes old rows by surprise.  Idempotent: columns
 * are only added if missing, only null values are backfilled, and only nullable columns are altered.</p>
 *
 * <p>GRP-7445: the zoom list users api returns no id for pending users, so make grouper_prov_zoom_user.id
 * nullable and recreate grouper_zoom_user_id_idx (id, config_id) as non-unique.  Several pending users in
 * one config would otherwise fail the NOT NULL, or collide on the unique index.  Only if the table exists
 * (zoom is optional).  Idempotent: skipped once the column is nullable and the index is non-unique.</p>
 *
 * <p>GRP-7446: add grouper_file.file_contents_blob (nullable; postgres bytea, oracle BLOB, mysql LONGBLOB) for
 * binary files.  No backfill, existing rows are text.  Idempotent: only added if missing.</p>
 *
 * <p>GRP-6303: widen grouper_failsafe.name from varchar(200) to varchar(512) like grouper_loader_log.job_name (the
 * failsafe name is the job name, and subjob names include a group name).  On mysql the unique index
 * grouper_failsafe_name_idx is recreated on the first 255 chars, like stem_name_idx.  Idempotent: only if the column
 * is narrower than 512 or the index is missing (e.g. a mysql run that failed after dropping it).</p>
 *
 * <p>GRP-6677: grouper_data_row_field_asgn_v (and grouper_data_row_assign_v when it was built from the DDL model)
 * selected gdra.internal_id, the data row assign id, as data_row_internal_id.  Replace each view whose stored sql
 * still has that, with gdr.internal_id.  Idempotent: the view sql is read from the database catalog, so a fixed
 * view is left alone.</p>
 */
public class UpgradeTaskV45 implements UpgradeTasksInterface {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTaskV45.class);

  /** GRP-7417: table holding the column to convert */
  private static final String GRP_7417_TABLE = "grouper_file";

  /** GRP-7417: column converted from varchar to text */
  private static final String GRP_7417_COLUMN = "file_contents_clob";

  /** GRP-7439: table getting the created / updated timestamp columns */
  private static final String GRP_7439_TABLE = "grouper_file";

  /** GRP-7439: timestamp columns added to grouper_file */
  private static final String[] GRP_7439_COLUMNS = new String[] {"created_on_micros", "updated_on_micros"};

  /** GRP-7445: zoom user table */
  private static final String GRP_7445_TABLE = "grouper_prov_zoom_user";

  /** GRP-7445: zoom user id column, made nullable */
  private static final String GRP_7445_COLUMN = "id";

  /** GRP-7445: index on (id, config_id), made non-unique */
  private static final String GRP_7445_INDEX = "grouper_zoom_user_id_idx";

  /** GRP-6677: views that selected the wrong column, and their correct select */
  private static final String[][] GRP_6677_VIEWS_AND_SQL = new String[][] {
    {"grouper_data_row_assign_v", GrouperDdl5_0_0.DATA_ROW_ASSIGN_V_SQL},
    {"grouper_data_row_field_asgn_v", GrouperDdl5_0_0.DATA_ROW_FIELD_ASGN_V_SQL}};

  /** GRP-6677: the wrong column expression, as it looks in the normalized view sql */
  private static final String GRP_6677_WRONG_COLUMN = "gdra.internal_id data_row_internal_id";

  /** GRP-6303: failsafe table */
  private static final String GRP_6303_TABLE = "grouper_failsafe";

  /** GRP-6303: failsafe name column, widened */
  private static final String GRP_6303_COLUMN = "name";

  /** GRP-6303: new width of the name column */
  private static final int GRP_6303_WIDTH = 512;

  /** GRP-6303: unique index on the name, a 255 char prefix on mysql */
  private static final String GRP_6303_INDEX = "grouper_failsafe_name_idx";

  /** GRP-7446: table getting the binary contents column */
  private static final String GRP_7446_TABLE = "grouper_file";

  /** GRP-7446: binary contents column */
  private static final String GRP_7446_COLUMN = "file_contents_blob";

  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }

  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("7.7.0");
  }

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    boolean workToDo = false;

    // GRP-7417 postgres grouper_file.file_contents_clob to text
    workToDo |= grp7417HasAutomaticWork();

    // GRP-7439 grouper_file created_on_micros / updated_on_micros
    workToDo |= grp7439HasAutomaticWork();

    // GRP-7445 grouper_prov_zoom_user.id nullable, grouper_zoom_user_id_idx non-unique
    workToDo |= grp7445HasAutomaticWork();

    // GRP-7446 grouper_file.file_contents_blob
    workToDo |= grp7446HasAutomaticWork();

    // GRP-6303 grouper_failsafe.name to varchar(512)
    workToDo |= grp6303HasAutomaticWork();

    // GRP-6677 data row views select the wrong data_row_internal_id
    workToDo |= !grp6677ViewsToReplace().isEmpty();

    // (additional v7 DDL checks for this task can be OR-ed in here)

    return workToDo;
  }

  @Override
  public void updateVersionFromPrevious(final OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        grp7417FileContentsClobToText(otherJobInput);
        grp7439FileTimestamps(otherJobInput);
        grp7445ZoomUserIdNullable(otherJobInput);
        grp7446FileContentsBlob(otherJobInput);
        grp6303FailsafeNameWidth(otherJobInput);

        // keep view changes LAST, add new table / column / index work above this.  Replacing a view is the step
        // most likely to fail (e.g. postgres rejects CREATE OR REPLACE VIEW if the output columns change, or a
        // site view depends on it), and each step commits on its own, so everything above is already applied if
        // it does.  The task is not marked done, and the next run redoes only what is left (every step checks
        // the database first)
        grp6677ReplaceDataRowViews(otherJobInput);
        return null;
      }
    });
  }

  /**
   * Whether GRP-7417 has work: postgres, the grouper_file table exists, and file_contents_clob is not
   * already text.  Always false on oracle/mysql.
   * @return true if the column still needs to be converted
   */
  private boolean grp7417HasAutomaticWork() {
    if (!GrouperDdlUtils.isPostgres()) {
      return false;
    }
    if (!GrouperDdlUtils.assertTableThere(true, GRP_7417_TABLE)) {
      return false;
    }

    // ask information_schema rather than JDBC metadata, the driver reports text as varchar with a
    // driver-dependent size, which is not a reliable signal
    String dataType = new GcDbAccess().sql("select data_type from information_schema.columns "
        + "where table_schema = current_schema() and table_name = ? and column_name = ?")
        .addBindVar(GRP_7417_TABLE).addBindVar(GRP_7417_COLUMN).select(String.class);

    // column missing (should not happen) means nothing we can convert
    if (StringUtils.isBlank(dataType)) {
      return false;
    }
    return !StringUtils.equalsIgnoreCase("text", dataType);
  }

  /**
   * GRP-7417: convert postgres grouper_file.file_contents_clob from VARCHAR(10000000) to TEXT.
   * varchar to text is a binary-compatible change in postgres, so existing data is kept as is.
   * @param otherJobInput
   */
  private void grp7417FileContentsClobToText(OtherJobInput otherJobInput) {
    if (!grp7417HasAutomaticWork()) {
      return;
    }

    new GcDbAccess().sql("ALTER TABLE " + GRP_7417_TABLE + " ALTER COLUMN " + GRP_7417_COLUMN + " TYPE text").executeSql();

    LOG.info("GRP-7417: changed " + GRP_7417_TABLE + "." + GRP_7417_COLUMN + " to text");

    if (otherJobInput != null) {
      otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);
      otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(
          ", changed " + GRP_7417_TABLE + "." + GRP_7417_COLUMN + " to text");
    }
  }

  /**
   * Whether GRP-7439 has work: the grouper_file table exists and a timestamp column is missing or still
   * nullable.  A nullable column means the backfill and NOT NULL have not been done (e.g. a DBA added the
   * columns by hand with auto DDL off), so the task runs (or reports it) instead of being marked done.
   * @return true if a column needs to be added, backfilled, or made NOT NULL
   */
  private boolean grp7439HasAutomaticWork() {
    if (!GrouperDdlUtils.assertTableThere(true, GRP_7439_TABLE)) {
      return false;
    }
    for (String column : GRP_7439_COLUMNS) {
      if (!GrouperDdlUtils.assertColumnThere(true, GRP_7439_TABLE, column)) {
        return true;
      }
      if (grp7439ColumnNullable(column)) {
        return true;
      }
    }
    return false;
  }

  /**
   * whether a grouper_file timestamp column allows nulls.  Read from the catalog, not result set metadata,
   * since the postgres driver caches nullability per connection and would miss the SET NOT NULL below.
   * @param column
   * @return true if nullable
   */
  private boolean grp7439ColumnNullable(String column) {
    return GrouperDdlUtils.isColumnNullableFromCatalog(GRP_7439_TABLE, column);
  }

  /**
   * GRP-7439: set null created_on_micros / updated_on_micros to now.  Each column is backfilled only where
   * it is null, so a value that is already set (e.g. a row saved by the new code) is left alone.
   * @return number of rows updated
   */
  private int grp7439Backfill() {
    // one timestamp for the whole backfill, coalesce so a value that is already set is kept
    long nowMicros = System.currentTimeMillis() * 1000L;
    return new GcDbAccess().sql("update " + GRP_7439_TABLE
        + " set created_on_micros = coalesce(created_on_micros, ?), updated_on_micros = coalesce(updated_on_micros, ?)"
        + " where created_on_micros is null or updated_on_micros is null")
        .addBindVar(nowMicros).addBindVar(nowMicros).executeSql();
  }

  /**
   * GRP-7439: add the grouper_file created_on_micros / updated_on_micros columns if missing (nullable, since
   * existing rows have no value yet), backfill null values with now, then make both NOT NULL.
   * @param otherJobInput
   */
  private void grp7439FileTimestamps(OtherJobInput otherJobInput) {
    if (!grp7439HasAutomaticWork()) {
      return;
    }

    // add each column if missing
    for (String column : GRP_7439_COLUMNS) {
      if (GrouperDdlUtils.assertColumnThere(true, GRP_7439_TABLE, column)) {
        continue;
      }
      if (GrouperDdlUtils.isOracle()) {
        new GcDbAccess().sql("ALTER TABLE " + GRP_7439_TABLE + " ADD " + column + " NUMBER(38)").executeSql();
      } else {
        new GcDbAccess().sql("ALTER TABLE " + GRP_7439_TABLE + " ADD COLUMN " + column + " BIGINT").executeSql();
      }
      LOG.info("GRP-7439: added column " + GRP_7439_TABLE + "." + column);
      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column " + GRP_7439_TABLE + "." + column);
      }
    }

    int rowCount = grp7439Backfill();
    if (rowCount > 0) {
      LOG.info("GRP-7439: backfilled " + rowCount + " " + GRP_7439_TABLE + " rows with created/updated micros");
      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(rowCount);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(
            ", backfilled " + rowCount + " " + GRP_7439_TABLE + " rows with created/updated micros");
      }
    }

    // now that all rows have values, make the columns NOT NULL to match the install / DDL definition
    for (String column : GRP_7439_COLUMNS) {
      if (!grp7439ColumnNullable(column)) {
        continue;
      }

      // backfill again right before the alter, in case an older grouper node (rolling upgrade) inserted a
      // row without the timestamps since the backfill above, otherwise the alter would fail
      grp7439Backfill();

      if (GrouperDdlUtils.isPostgres()) {
        new GcDbAccess().sql("ALTER TABLE " + GRP_7439_TABLE + " ALTER COLUMN " + column + " SET NOT NULL").executeSql();
      } else if (GrouperDdlUtils.isMysql()) {
        new GcDbAccess().sql("ALTER TABLE " + GRP_7439_TABLE + " MODIFY " + column + " BIGINT NOT NULL").executeSql();
      } else {
        new GcDbAccess().sql("ALTER TABLE " + GRP_7439_TABLE + " MODIFY (" + column + " NOT NULL)").executeSql();
      }
      LOG.info("GRP-7439: made " + GRP_7439_TABLE + "." + column + " NOT NULL");
      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", made " + GRP_7439_TABLE + "." + column + " NOT NULL");
      }
    }
  }

  /**
   * Whether GRP-7445 has work: the grouper_prov_zoom_user table exists, and the id column is still NOT NULL
   * or grouper_zoom_user_id_idx is unique (or missing).
   * @return true if the column or index needs to change
   */
  private boolean grp7445HasAutomaticWork() {
    if (!GrouperDdlUtils.assertTableThere(true, GRP_7445_TABLE)) {
      return false;
    }
    if (!GrouperDdlUtils.isColumnNullableFromCatalog(GRP_7445_TABLE, GRP_7445_COLUMN)) {
      return true;
    }
    // read from the catalog, the ddlutils model can be cached during the upgrade and miss the recreate
    Boolean indexUnique = GrouperDdlUtils.isIndexUniqueFromCatalog(GRP_7445_TABLE, GRP_7445_INDEX);
    return indexUnique == null || indexUnique;
  }

  /**
   * GRP-7445: make grouper_prov_zoom_user.id nullable, and drop and recreate grouper_zoom_user_id_idx as
   * non-unique (or create it if missing).
   * @param otherJobInput
   */
  private void grp7445ZoomUserIdNullable(OtherJobInput otherJobInput) {
    if (!grp7445HasAutomaticWork()) {
      return;
    }

    // drop NOT NULL on the id column
    if (!GrouperDdlUtils.isColumnNullableFromCatalog(GRP_7445_TABLE, GRP_7445_COLUMN)) {
      if (GrouperDdlUtils.isPostgres()) {
        new GcDbAccess().sql("ALTER TABLE " + GRP_7445_TABLE + " ALTER COLUMN " + GRP_7445_COLUMN + " DROP NOT NULL").executeSql();
      } else if (GrouperDdlUtils.isMysql()) {
        // MODIFY rewrites the whole column definition, so restate the type
        new GcDbAccess().sql("ALTER TABLE " + GRP_7445_TABLE + " MODIFY " + GRP_7445_COLUMN + " VARCHAR(40) NULL").executeSql();
      } else {
        new GcDbAccess().sql("ALTER TABLE " + GRP_7445_TABLE + " MODIFY (" + GRP_7445_COLUMN + " NULL)").executeSql();
      }
      LOG.info("GRP-7445: made " + GRP_7445_TABLE + "." + GRP_7445_COLUMN + " nullable");
      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", made " + GRP_7445_TABLE + "." + GRP_7445_COLUMN + " nullable");
      }
    }

    // recreate the index as non-unique
    Boolean indexUnique = GrouperDdlUtils.isIndexUniqueFromCatalog(GRP_7445_TABLE, GRP_7445_INDEX);
    if (indexUnique == null || indexUnique) {
      if (indexUnique != null) {
        if (GrouperDdlUtils.isMysql()) {
          new GcDbAccess().sql("DROP INDEX " + GRP_7445_INDEX + " ON " + GRP_7445_TABLE).executeSql();
        } else {
          new GcDbAccess().sql("DROP INDEX " + GRP_7445_INDEX).executeSql();
        }
      }
      new GcDbAccess().sql("CREATE INDEX " + GRP_7445_INDEX + " ON " + GRP_7445_TABLE + " (id, config_id)").executeSql();
      LOG.info("GRP-7445: recreated index " + GRP_7445_INDEX + " as non-unique");
      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", recreated index " + GRP_7445_INDEX + " as non-unique");
      }
    }
  }

  /**
   * Whether GRP-7446 has work: the grouper_file table exists and file_contents_blob is missing
   * @return true if the column needs to be added
   */
  private boolean grp7446HasAutomaticWork() {
    if (!GrouperDdlUtils.assertTableThere(true, GRP_7446_TABLE)) {
      return false;
    }
    return !GrouperDdlUtils.assertColumnThere(true, GRP_7446_TABLE, GRP_7446_COLUMN);
  }

  /**
   * GRP-7446: add grouper_file.file_contents_blob if missing.  Nullable, existing rows are text so there is
   * nothing to backfill
   * @param otherJobInput
   */
  private void grp7446FileContentsBlob(OtherJobInput otherJobInput) {
    if (!grp7446HasAutomaticWork()) {
      return;
    }

    if (GrouperDdlUtils.isPostgres()) {
      new GcDbAccess().sql("ALTER TABLE " + GRP_7446_TABLE + " ADD COLUMN " + GRP_7446_COLUMN + " BYTEA").executeSql();
    } else if (GrouperDdlUtils.isMysql()) {
      new GcDbAccess().sql("ALTER TABLE " + GRP_7446_TABLE + " ADD COLUMN " + GRP_7446_COLUMN + " LONGBLOB NULL").executeSql();
    } else {
      new GcDbAccess().sql("ALTER TABLE " + GRP_7446_TABLE + " ADD " + GRP_7446_COLUMN + " BLOB").executeSql();
    }

    // comments are oracle / postgres only
    if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
      new GcDbAccess().sql("COMMENT ON COLUMN " + GRP_7446_TABLE + "." + GRP_7446_COLUMN + " IS 'binary contents of the file "
          + "(zip, xlsx, etc), null if the contents are text in file_contents_varchar or file_contents_clob'").executeSql();
    }

    LOG.info("GRP-7446: added column " + GRP_7446_TABLE + "." + GRP_7446_COLUMN);
    if (otherJobInput != null) {
      otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
      otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column " + GRP_7446_TABLE + "." + GRP_7446_COLUMN);
    }
  }

  /**
   * GRP-6677: the data row views that exist and still select gdra.internal_id as data_row_internal_id
   * @return view name to its correct select, empty if nothing to do
   */
  private Map<String, String> grp6677ViewsToReplace() {
    Map<String, String> result = new LinkedHashMap<String, String>();
    for (String[] viewAndSql : GRP_6677_VIEWS_AND_SQL) {
      // read from the catalog every time, null if the view is not there
      String definition = GrouperDdlUtils.retrieveViewDefinitionNormalized(viewAndSql[0]);
      if (definition != null && definition.contains(GRP_6677_WRONG_COLUMN)) {
        result.put(viewAndSql[0], viewAndSql[1]);
      }
    }
    return result;
  }

  /**
   * GRP-6677: replace the data row views that select the wrong data_row_internal_id.  Same column names and types,
   * so CREATE OR REPLACE works on all three databases (and keeps grants and comments)
   * @param otherJobInput
   */
  private void grp6677ReplaceDataRowViews(OtherJobInput otherJobInput) {
    for (Map.Entry<String, String> viewAndSql : grp6677ViewsToReplace().entrySet()) {
      new GcDbAccess().sql("CREATE OR REPLACE VIEW " + viewAndSql.getKey() + " AS " + viewAndSql.getValue()).executeSql();
      LOG.info("GRP-6677: replaced view " + viewAndSql.getKey());
      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", replaced view " + viewAndSql.getKey());
      }
    }
  }

  /**
   * Whether GRP-6303 has work: the grouper_failsafe table exists, and the name column is narrower than 512 or the
   * name index is missing
   * @return true if the column or index needs to change
   */
  private boolean grp6303HasAutomaticWork() {
    if (!GrouperDdlUtils.assertTableThere(true, GRP_6303_TABLE)) {
      return false;
    }
    if (GrouperDdlUtils.getColumnSize(GRP_6303_TABLE, GRP_6303_COLUMN) < GRP_6303_WIDTH) {
      return true;
    }
    // read from the catalog, null if the index is not there
    return GrouperDdlUtils.isIndexUniqueFromCatalog(GRP_6303_TABLE, GRP_6303_INDEX) == null;
  }

  /**
   * GRP-6303: widen grouper_failsafe.name to varchar(512).  On mysql drop the unique index first, MODIFY the column
   * (restating NOT NULL since MODIFY rewrites the whole definition), and recreate the index on the first 255 chars.
   * Each step checks the database, so a run that fails partway is finished by the next run
   * @param otherJobInput
   */
  private void grp6303FailsafeNameWidth(OtherJobInput otherJobInput) {
    if (!grp6303HasAutomaticWork()) {
      return;
    }

    if (GrouperDdlUtils.getColumnSize(GRP_6303_TABLE, GRP_6303_COLUMN) < GRP_6303_WIDTH) {
      if (GrouperDdlUtils.isPostgres()) {
        // varchar widening does not rewrite the table, and no views use this table
        new GcDbAccess().sql("ALTER TABLE " + GRP_6303_TABLE + " ALTER COLUMN " + GRP_6303_COLUMN
            + " TYPE VARCHAR(" + GRP_6303_WIDTH + ")").executeSql();
      } else if (GrouperDdlUtils.isMysql()) {
        // the index becomes a prefix index, so drop it first and recreate it below
        if (GrouperDdlUtils.isIndexUniqueFromCatalog(GRP_6303_TABLE, GRP_6303_INDEX) != null) {
          new GcDbAccess().sql("DROP INDEX " + GRP_6303_INDEX + " ON " + GRP_6303_TABLE).executeSql();
        }
        new GcDbAccess().sql("ALTER TABLE " + GRP_6303_TABLE + " MODIFY " + GRP_6303_COLUMN
            + " VARCHAR(" + GRP_6303_WIDTH + ") NOT NULL").executeSql();
      } else {
        // oracle MODIFY keeps NOT NULL
        new GcDbAccess().sql("ALTER TABLE " + GRP_6303_TABLE + " MODIFY (" + GRP_6303_COLUMN
            + " VARCHAR2(" + GRP_6303_WIDTH + "))").executeSql();
      }
      LOG.info("GRP-6303: widened " + GRP_6303_TABLE + "." + GRP_6303_COLUMN + " to " + GRP_6303_WIDTH);
      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", widened " + GRP_6303_TABLE + "." + GRP_6303_COLUMN
            + " to " + GRP_6303_WIDTH);
      }
    }

    // recreate the index if it is missing (mysql above, or a previous run that failed after dropping it)
    if (GrouperDdlUtils.isIndexUniqueFromCatalog(GRP_6303_TABLE, GRP_6303_INDEX) == null) {
      String indexColumn = GrouperDdlUtils.isMysql() ? GRP_6303_COLUMN + "(255)" : GRP_6303_COLUMN;
      new GcDbAccess().sql("CREATE UNIQUE INDEX " + GRP_6303_INDEX + " ON " + GRP_6303_TABLE + " (" + indexColumn + ")").executeSql();
      LOG.info("GRP-6303: created index " + GRP_6303_INDEX);
      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created index " + GRP_6303_INDEX);
      }
    }
  }

}
