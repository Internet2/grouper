package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteOldRecords;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV38 implements UpgradeTasksInterface {

  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }

  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("7.0.0");
  }

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {

    if (!GrouperDdlUtils.assertColumnThere(true, "grouper_sync", "internal_id")) {
      return true;
    }

    if (!GrouperDdlUtils.assertIndexExists("grouper_sync", "grouper_sync_internal_id_idx")) {
      return true;
    }

    if (GrouperDdlUtils.isColumnNullable("grouper_sync", "internal_id", "id", "doesNotMatter")) {
      return true;
    }

    return false;
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {

    if (!GrouperDdlUtils.assertColumnThere(true, "grouper_sync", "internal_id")) {
      if (GrouperDdlUtils.isPostgres()) {
        new GcDbAccess().sql("ALTER TABLE grouper_sync ADD COLUMN internal_id BIGINT").executeSql();
      } else if (GrouperDdlUtils.isMysql()) {
        new GcDbAccess().sql("ALTER TABLE grouper_sync ADD COLUMN internal_id BIGINT AFTER last_full_metadata_sync_run").executeSql();
      } else {
        new GcDbAccess().sql("ALTER TABLE grouper_sync ADD internal_id NUMBER(38)").executeSql();
      }

      if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) {
        new GcDbAccess().sql("COMMENT ON COLUMN grouper_sync.internal_id IS 'internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)'").executeSql();
      }

      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added column grouper_sync.internal_id");
      }
    }

    if (!GrouperDdlUtils.assertIndexExists("grouper_sync", "grouper_sync_internal_id_idx")) {
      new GcDbAccess().sql("CREATE UNIQUE INDEX grouper_sync_internal_id_idx ON grouper_sync (internal_id)").executeSql();
      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sync_internal_id_idx");
      }
    }

    if (GrouperDdlUtils.isColumnNullable("grouper_sync", "internal_id", "id", "doesNotMatter")) {
      GrouperDaemonDeleteOldRecords.verifyTableIdIndexes(null);

      String sql = null;

      if (GrouperDdlUtils.isOracle()) {
        sql = "ALTER TABLE grouper_sync MODIFY (internal_id NOT NULL)";
      } else if (GrouperDdlUtils.isMysql()) {
        sql = "ALTER TABLE grouper_sync MODIFY internal_id BIGINT NOT NULL";
      } else if (GrouperDdlUtils.isPostgres()) {
        sql = "ALTER TABLE grouper_sync ALTER COLUMN internal_id SET NOT NULL";
      } else {
        throw new RuntimeException("Which database are we????");
      }

      new GcDbAccess().sql(sql).executeSql();
      if (otherJobInput != null) {
        otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
        otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", set column grouper_sync.internal_id to not null");
      }
    }
  }
}

