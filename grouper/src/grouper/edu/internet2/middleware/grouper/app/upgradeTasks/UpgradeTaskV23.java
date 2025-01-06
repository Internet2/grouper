package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroup;

public class UpgradeTaskV23 implements UpgradeTasksInterface {
  
  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("5.13.1");
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        AttributeDef sqlCacheableGroupDef = AttributeDefFinder.findByName(SqlCacheGroup.attributeDefFolderName() + ":sqlCacheableGroupDef", false);
        if (sqlCacheableGroupDef != null) {
          sqlCacheableGroupDef.delete();
        }
        
        AttributeDef sqlCacheableGroupMarkerDef = AttributeDefFinder.findByName(SqlCacheGroup.attributeDefFolderName() + ":sqlCacheableGroupMarkerDef", false);
        if (sqlCacheableGroupMarkerDef != null) {
          sqlCacheableGroupMarkerDef.delete();
        }
        
        return null;
      }
    });
  }

}
