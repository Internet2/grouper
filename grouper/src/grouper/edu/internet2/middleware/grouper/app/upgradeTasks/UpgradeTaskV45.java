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
 */
public class UpgradeTaskV45 implements UpgradeTasksInterface {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTaskV45.class);

  /** GRP-7417: table holding the column to convert */
  private static final String GRP_7417_TABLE = "grouper_file";

  /** GRP-7417: column converted from varchar to text */
  private static final String GRP_7417_COLUMN = "file_contents_clob";

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

    // (additional v7 DDL checks for this task can be OR-ed in here)

    return workToDo;
  }

  @Override
  public void updateVersionFromPrevious(final OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        grp7417FileContentsClobToText(otherJobInput);
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

}
