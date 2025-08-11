package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheFullSyncDaemon;

public class UpgradeTaskV34 implements UpgradeTasksInterface {
  
  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("5.20.1");
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        if (!GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("registry.checkMembershipCacheIsPopulated", true)) { 
          return null;
        }
        
        SqlCacheFullSyncDaemon.runNowWithoutDaemon(true);
        
        if (otherJobInput != null) {
          otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", ran SqlCacheFullSyncDaemon.runNowWithoutDaemon(true)");
        }
        
        return null;
      }
    });
  }

}
