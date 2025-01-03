package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV29 implements UpgradeTasksInterface {
  
  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    
    if (!GrouperDdlUtils.assertIndexExists("grouper_sync_membership", "grouper_sync_mship_mem_idx")) {
      return true;
    }
        
    return false;
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        if (!GrouperDdlUtils.assertIndexExists("grouper_sync_membership", "grouper_sync_mship_mem_idx")) {
          new GcDbAccess().sql("CREATE INDEX grouper_sync_mship_mem_idx ON grouper_sync_membership (grouper_sync_member_id, last_updated)").executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_sync_mship_mem_idx");
          }
        }
        
        return null;
      }
    });
  }

}
