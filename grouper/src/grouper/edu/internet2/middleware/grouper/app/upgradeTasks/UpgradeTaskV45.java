package edu.internet2.middleware.grouper.app.upgradeTasks;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
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

}
