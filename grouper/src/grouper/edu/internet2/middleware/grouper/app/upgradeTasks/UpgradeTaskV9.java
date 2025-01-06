package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.entity.EntityUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitUtils;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;

public class UpgradeTaskV9 implements UpgradeTasksInterface {

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
    
        String[] attributeDefNames = new String[] {
            // etc:attribute:entities:entitySubjectIdentifierDef
            EntityUtils.attributeEntityStemName() + ":entitySubjectIdentifierDef",
            // etc:attribute:permissionLimits:limitsDef
            PermissionLimitUtils.attributeLimitStemName() + ":" + PermissionLimitUtils.LIMIT_DEF,
            // etc:attribute:permissionLimits:limitsDefInt
            PermissionLimitUtils.attributeLimitStemName() + ":" + PermissionLimitUtils.LIMIT_DEF_INT,
            // etc:attribute:permissionLimits:limitsDefMarker
            PermissionLimitUtils.attributeLimitStemName() + ":" + PermissionLimitUtils.LIMIT_DEF_MARKER
            
            
        };

        for (String attributeDefName : attributeDefNames) {
          AttributeDef attributeDef = AttributeDefFinder.findByName(attributeDefName, false);
          
          if (attributeDef != null) {
            attributeDef.getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
            attributeDef.getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);
          }
        }          
        
        
        return null;
      }
    });
  }
  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("4.0.0");
  }

}
