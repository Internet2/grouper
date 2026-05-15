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
 * Upgrade task to drop unused string02..string12 indexes on grouper_change_log_entry_temp.
 * Only string01 (used by PIT "find missing" queries), created_on, and the primary key are
 * referenced by queries; the other string indexes just slow down inserts.
 */
public class UpgradeTaskV42 implements UpgradeTasksInterface {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UpgradeTaskV42.class);

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    if (!GrouperDdlUtils.assertTableThere(true, "grouper_change_log_entry_temp")) {
      return false;
    }
    for (int i = 2; i <= 12; i++) {
      String indexName = String.format("change_log_temp_string%02d_idx", i);
      if (GrouperDdlUtils.assertIndexExists("grouper_change_log_entry_temp", indexName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }

  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("7.0.0");
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        if (!GrouperDdlUtils.assertTableThere(true, "grouper_change_log_entry_temp")) {
          return null;
        }

        for (int i = 2; i <= 12; i++) {
          String indexName = String.format("change_log_temp_string%02d_idx", i);
          if (GrouperDdlUtils.assertIndexExists("grouper_change_log_entry_temp", indexName)) {
            if (GrouperDdlUtils.isMysql()) {
              new GcDbAccess().sql("DROP INDEX " + indexName + " ON grouper_change_log_entry_temp").executeSql();
            } else {
              new GcDbAccess().sql("DROP INDEX " + indexName).executeSql();
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addDeleteCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", dropped index " + indexName);
            }
          }
        }

        return null;
      }
    });
  }

}
