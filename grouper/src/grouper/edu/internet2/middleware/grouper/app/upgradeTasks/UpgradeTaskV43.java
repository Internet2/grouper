package edu.internet2.middleware.grouper.app.upgradeTasks;


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
 * <p>GRP-6653: add unique CONSTRAINTS on oracle for the surrogate internal_id / id_index columns that
 * foreign keys reference (a bare unique index is not a valid FK parent key on oracle - ORA-02270). Each
 * reuses its existing unique index via USING INDEX. Oracle-only: postgres and mysql accept the unique
 * index itself as a FK target. This mirrors how UpgradeTaskV41 already adds grouper_sync_internal_id_unq;
 * these constraints are managed here in the upgrade task (not the ddlutils database-compare model).</p>
 *
 * <p>GRP-7076: widen the group-as-subject identifier and folder-name columns from varchar(255) to
 * varchar(1024) so they line up with grouper_groups.name (already 1024):</p>
 * <ul>
 *   <li>grouper_members.subject_identifier0 / grouper_pit_members.subject_identifier0 - a group
 *       (source g:gsa) stores its fully-qualified name here; a 256-1024 char name used to overflow
 *       this column and fail the group create with an opaque JDBC error.</li>
 *   <li>grouper_stems.name / display_name / alternate_name - so deeply nested folder paths are not
 *       capped at 255.</li>
 * </ul>
 *
 * <p>Per-database behavior (views only block postgres):</p>
 * <ul>
 *   <li><b>oracle</b> - ALTER ... MODIFY (col VARCHAR2(1024)); dependent views auto-invalidate and
 *       recompile, indexes stay full-column.</li>
 *   <li><b>mysql</b> - at 1024 chars these indexes exceed the InnoDB key-length limit, so for each
 *       affected index: DROP INDEX, MODIFY the column (preserving nullability), then recreate the
 *       index as a (255) prefix.</li>
 *   <li><b>postgres</b> - ALTER COLUMN ... TYPE is blocked by the dependent views, and dropping/
 *       recreating that view set during an upgrade is risky.  We deliberately do NOT auto-run the
 *       postgres widening; instead this task reports it as having no automatic DDL work (so the
 *       upgrade is never blocked) and logs a loud reminder.  The widening is a documented manual DBA
 *       task - see the release notes for the drop-view / alter / recreate-view SQL.</li>
 * </ul>
 */
public class UpgradeTaskV43 implements UpgradeTasksInterface {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTaskV43.class);

  /** target width all of these columns are being widened to */
  private static final int TARGET_SIZE = 1024;

  /**
   * GRP-7076: columns widened 255 -> 1024.  The index/unique/notNull metadata is needed so that on
   * mysql we can drop, widen, and recreate each index as a (255) prefix while preserving the column's
   * NOT NULL / NULL attribute (mysql MODIFY rewrites the whole column definition).
   */
  private static final class WidenCol {
    final String table;
    final String column;
    final String index;
    final boolean unique;
    final boolean notNull;
    WidenCol(String table, String column, String index, boolean unique, boolean notNull) {
      this.table = table;
      this.column = column;
      this.index = index;
      this.unique = unique;
      this.notNull = notNull;
    }
  }

  /** the GRP-7076 columns to widen, with their index and nullability */
  private static final WidenCol[] GRP_7076_COLUMNS = new WidenCol[] {
    new WidenCol("grouper_members",     "subject_identifier0", "member_subjidentifier0_idx",     false, false),
    new WidenCol("grouper_members",     "subject_identifier1", "member_subjidentifier1_idx",     false, false),
    new WidenCol("grouper_members",     "subject_identifier2", "member_subjidentifier2_idx",     false, false),
    new WidenCol("grouper_pit_members", "subject_identifier0", "pit_member_subjidentifier0_idx", false, false),
    new WidenCol("grouper_stems",       "name",                "stem_name_idx",                  true,  true),
    new WidenCol("grouper_stems",       "display_name",        "stem_displayname_idx",           false, true),
    new WidenCol("grouper_stems",       "alternate_name",      "stem_alternate_name_idx",        false, false),
  };

  /**
   * GRP-6653: a surrogate-key column that foreign keys reference needs a unique CONSTRAINT on oracle -
   * a bare unique index is not a valid FK parent key on oracle (ORA-02270).  Each constraint reuses the
   * column's existing unique index via USING INDEX so oracle does not build a second index.  Postgres and
   * mysql accept the unique index itself as a FK target, so this is oracle-only.
   */
  private static final class OracleUniqueConstraint {
    final String table;
    final String constraintName;
    final String usingIndex;
    final String column;
    OracleUniqueConstraint(String table, String constraintName, String usingIndex, String column) {
      this.table = table;
      this.constraintName = constraintName;
      this.usingIndex = usingIndex;
      this.column = column;
    }
  }

  /** the GRP-6653 oracle unique constraints, each pinned to its existing unique index */
  private static final OracleUniqueConstraint[] GRP_6653_ORACLE_CONSTRAINTS = new OracleUniqueConstraint[] {
    new OracleUniqueConstraint("grouper_members", "members_internal_id_unique",   "grouper_mem_internal_id_idx",  "internal_id"),
    new OracleUniqueConstraint("grouper_stems",   "grouper_stems_id_index_unq",   "stem_id_index_idx",            "id_index"),
    new OracleUniqueConstraint("grouper_fields",  "grouper_fie_internal_id_unq",  "grouper_fie_internal_id_idx",  "internal_id"),
    new OracleUniqueConstraint("grouper_groups",  "grouper_grp_internal_id_unq",  "grouper_grp_internal_id_idx",  "internal_id"),
    new OracleUniqueConstraint("grouper_sync",    "grouper_sync_internal_id_unq", "grouper_sync_internal_id_idx", "internal_id"),
  };

  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }

  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("7.3.0");
  }

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    boolean workToDo = false;

    // GRP-7076 widening (postgres is intentionally manual - see grp7076HasAutomaticWork)
    workToDo |= grp7076HasAutomaticWork();

    // GRP-6653 oracle unique constraints
    workToDo |= grp6653HasAutomaticWork();

    // (additional v7 DDL checks for this task can be OR-ed in here)

    return workToDo;
  }

  /**
   * Whether GRP-6653 has automatic work: oracle is missing one or more of the unique constraints.
   * Always false off oracle (postgres/mysql use the unique index as the FK target, nothing to add).
   * @return true if oracle still needs a GRP-6653 constraint added
   */
  private boolean grp6653HasAutomaticWork() {
    if (!GrouperDdlUtils.isOracle()) {
      return false;
    }
    for (OracleUniqueConstraint oracleUniqueConstraint : GRP_6653_ORACLE_CONSTRAINTS) {
      if (!GrouperDdlUtils.assertTableThere(true, oracleUniqueConstraint.table)) {
        continue;
      }
      if (!GrouperDdlUtils.doesConstraintExistOracle(oracleUniqueConstraint.constraintName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Whether the GRP-7076 widening has automatic work to do on this database.
   *
   * <p>On postgres this always returns false (we never auto-run the widening there - the columns are
   * referenced by views that block ALTER COLUMN ... TYPE), but it logs a loud reminder if the columns
   * are still narrow so the manual DBA step is not silently forgotten.  Returning false lets the
   * framework mark this task done and continue rather than blocking the upgrade.</p>
   *
   * @return true if oracle/mysql still has a narrow target column to widen
   */
  private boolean grp7076HasAutomaticWork() {
    String pending = grp7076PendingColumns();

    if (GrouperDdlUtils.isPostgres()) {
      if (pending != null) {
        LOG.error("GRP-7076 MANUAL ACTION REQUIRED on postgres: widen to varchar(1024): " + pending
            + ".  ALTER COLUMN ... TYPE is blocked by dependent views on postgres, so this is a manual"
            + " DBA task - drop the dependent views, alter the columns, then recreate the views (see the"
            + " release notes for the exact SQL).  This upgrade task is being recorded as done WITHOUT"
            + " widening these columns on postgres.");
      }
      return false;
    }

    return pending != null;
  }

  /**
   * @return a comma-separated list of "table.column" target columns that exist and are still narrower
   *         than {@link #TARGET_SIZE}, or null if there is nothing to do
   */
  private String grp7076PendingColumns() {
    StringBuilder pending = new StringBuilder();
    for (WidenCol widenCol : GRP_7076_COLUMNS) {
      if (!GrouperDdlUtils.assertTableThere(true, widenCol.table)) {
        continue;
      }
      if (!GrouperDdlUtils.assertColumnThere(true, widenCol.table, widenCol.column)) {
        continue;
      }
      if (GrouperDdlUtils.getColumnSize(widenCol.table, widenCol.column) >= TARGET_SIZE) {
        continue;
      }
      if (pending.length() > 0) {
        pending.append(", ");
      }
      pending.append(widenCol.table).append(".").append(widenCol.column);
    }
    return pending.length() == 0 ? null : pending.toString();
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        // GRP-7076 column widening
        grp7076WidenColumns(otherJobInput);

        // GRP-6653 oracle unique constraints (FK parent keys)
        grp6653AddOracleUniqueConstraints(otherJobInput);

        // (additional v7 DDL work for this task goes here)

        return null;
      }
    });
  }

  /**
   * GRP-7076: widen the subject_identifier0 and stem name columns to varchar(1024) on oracle/mysql.
   * Postgres is handled as a manual task (see {@link #grp7076HasAutomaticWork()}) so it is skipped here.
   * @param otherJobInput
   */
  private void grp7076WidenColumns(OtherJobInput otherJobInput) {

    // postgres widening is a documented manual DBA task; nothing to auto-run here
    if (GrouperDdlUtils.isPostgres()) {
      return;
    }

    for (WidenCol widenCol : GRP_7076_COLUMNS) {

      if (!GrouperDdlUtils.assertTableThere(true, widenCol.table)) {
        continue;
      }
      if (!GrouperDdlUtils.assertColumnThere(true, widenCol.table, widenCol.column)) {
        continue;
      }
      // idempotent: skip if already at least the target width
      if (GrouperDdlUtils.getColumnSize(widenCol.table, widenCol.column) >= TARGET_SIZE) {
        continue;
      }

      if (GrouperDdlUtils.isOracle()) {
        // oracle keeps the existing NOT NULL/NULL and indexes; just widen the type
        new GcDbAccess().sql("ALTER TABLE " + widenCol.table + " MODIFY (" + widenCol.column
            + " VARCHAR2(" + TARGET_SIZE + "))").executeSql();

      } else if (GrouperDdlUtils.isMysql()) {
        // a full-column index on a varchar(1024) exceeds the InnoDB key-length limit, so drop the
        // index, widen the column (preserving nullability - mysql MODIFY rewrites the whole column
        // definition), then recreate the index as a (255) prefix
        if (GrouperDdlUtils.assertIndexExists(widenCol.table, widenCol.index)) {
          new GcDbAccess().sql("DROP INDEX " + widenCol.index + " ON " + widenCol.table).executeSql();
        }

        new GcDbAccess().sql("ALTER TABLE " + widenCol.table + " MODIFY " + widenCol.column
            + " VARCHAR(" + TARGET_SIZE + ")" + (widenCol.notNull ? " NOT NULL" : " NULL")).executeSql();

        if (!GrouperDdlUtils.assertIndexExists(widenCol.table, widenCol.index)) {
          new GcDbAccess().sql("CREATE " + (widenCol.unique ? "UNIQUE " : "") + "INDEX " + widenCol.index
              + " ON " + widenCol.table + " (" + widenCol.column + "(255))").executeSql();
        }
      }

      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(
            ", GRP-7076 widened " + widenCol.table + "." + widenCol.column + " to varchar(" + TARGET_SIZE + ")");
      }
    }
  }

  /**
   * GRP-6653: add the unique constraints on oracle, each reusing its existing unique index via USING
   * INDEX.  Oracle only - postgres/mysql accept the unique index as a FK target so there is nothing to
   * do there.  Idempotent: skips any constraint that already exists (e.g. created by the install SQL).
   * @param otherJobInput
   */
  private void grp6653AddOracleUniqueConstraints(OtherJobInput otherJobInput) {

    if (!GrouperDdlUtils.isOracle()) {
      return;
    }

    for (OracleUniqueConstraint oracleUniqueConstraint : GRP_6653_ORACLE_CONSTRAINTS) {

      if (!GrouperDdlUtils.assertTableThere(true, oracleUniqueConstraint.table)) {
        continue;
      }
      // idempotent: skip if the constraint is already there
      if (GrouperDdlUtils.doesConstraintExistOracle(oracleUniqueConstraint.constraintName)) {
        continue;
      }

      // pin to the existing unique index (USING INDEX) so oracle reuses it rather than building a second
      // one; if the index is somehow missing, fall back to a plain ADD CONSTRAINT (oracle creates one)
      String sql = "ALTER TABLE " + oracleUniqueConstraint.table + " ADD CONSTRAINT "
          + oracleUniqueConstraint.constraintName + " UNIQUE (" + oracleUniqueConstraint.column + ")";

      if (GrouperDdlUtils.assertIndexExists(oracleUniqueConstraint.table, oracleUniqueConstraint.usingIndex)) {
        sql += " USING INDEX " + oracleUniqueConstraint.usingIndex;
      }

      new GcDbAccess().sql(sql).executeSql();

      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(
            ", GRP-6653 added unique constraint " + oracleUniqueConstraint.constraintName);
      }
    }
  }

}
