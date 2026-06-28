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
 * v7 upgrade task that bundles the DDL changes shipping in this release.
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

    // (additional v7 DDL checks for this task can be OR-ed in here)

    return workToDo;
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

}
