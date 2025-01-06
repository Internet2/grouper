package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteOldRecords;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV16 implements UpgradeTasksInterface {
  
  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }
  
  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("5.8.1");
  }

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    
    if (GrouperDdlUtils.isColumnNullable("grouper_members", "internal_id", "subject_id", "GrouperSystem")) {
      return true;
    }
    
    return false;
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
      
    if (!GrouperDdlUtils.isColumnNullable("grouper_members", "internal_id", "subject_id", "GrouperSystem")) {
      return;
    }
    
    // ok nulls are allowed so make the change
    GrouperDaemonDeleteOldRecords.verifyTableIdIndexes(null);
    String sql = null;
    
    if (GrouperDdlUtils.isOracle()) {
      sql = "ALTER TABLE grouper_members MODIFY (internal_id NOT NULL)";
    } else if (GrouperDdlUtils.isMysql()) {
      sql = "ALTER TABLE grouper_members MODIFY internal_id BIGINT NOT NULL";
    } else if (GrouperDdlUtils.isPostgres()) {
      sql = "ALTER TABLE grouper_members ALTER COLUMN internal_id SET NOT NULL";
    } else {
      throw new RuntimeException("Which database are we????");
    }
    
    new GcDbAccess().sql(sql).executeSql();
    
  }

}
