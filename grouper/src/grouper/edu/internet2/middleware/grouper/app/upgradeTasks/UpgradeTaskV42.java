package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV42 implements UpgradeTasksInterface {

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {

    if (GrouperDdlUtils.isOracle()) {
      if (!GrouperDdlUtils.doesConstraintExistOracle("grouper_sync_internal_id_unq")) {
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

        if (GrouperDdlUtils.isOracle()) {
          if (!GrouperDdlUtils.doesConstraintExistOracle("grouper_sync_internal_id_unq")) {
            try {
              new GcDbAccess().sql("ALTER TABLE grouper_sync ADD CONSTRAINT grouper_sync_internal_id_unq UNIQUE (internal_id)").executeSql();
            } catch (Exception e) {
              if (!GrouperUtil.getFullStackTrace(e).contains("ORA-02261")) {
                // throw if the exception is anything other than the constraint already exists
                throw e;
              }
            }
            if (otherJobInput != null) {
              otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
              otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added constraint grouper_sync_internal_id_unq");
            }
          }
        }

        return null;
      }
    });
  }

}
