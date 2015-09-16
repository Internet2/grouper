package edu.internet2.middleware.grouper.hooks.examples;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * AssignReadOnlyAdminPrivilege adds the read privileges for the newly created groups to self (this group's subject)
 * 
 * <pre>
 * assign READ to an admins group based on attribute assignment to a parent folder
 * 
 * configure in grouper.properties:
 * 
 * hooks.group.class=edu.internet2.middleware.grouper.hooks.examples.AssignReadonlyAdminPrivilegeGroupHook
 * hooks.membership.class=edu.internet2.middleware.grouper.hooks.examples.AssignReadonlyAdminPrivilegeVetoMembershipHook
 * 
 * grouper.readonlyAdminEnforced.attributeDefName = a:b:c:reaodnlyAdmin
 * grouper.readonlyAdminEnforced.groupName = c:d:readonlyAdmins
 * 
 * setup objects in GSH:
 * 
 * grouperSession = GrouperSession.startRootSession();
 * String attributeFolderName = "a:b:c";
 * attributeDef = new AttributeDefSave(grouperSession).assignName(attributeFolderName + ":readonlyAdminDef").assignToStem(true).assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker).save();
 * attributeDef.getAttributeDefActionDelegate().configureActionList("assign");
 * attributeDefName = new AttributeDefNameSave(grouperSession, attributeDef).assignName(attributeFolderName + ":readonlyAdmin").assignCreateParentStemsIfNotExist(true).save();
 * groupAdmin = new GroupSave(grouperSession).assignName("c:d:readonlyAdmins").assignCreateParentStemsIfNotExist(true).save();
 * 
 * make a group to test:
 * 
 * stem = new StemSave(grouperSession).assignName("l:m").assignCreateParentStemsIfNotExist(true).save();
 * stem.getAttributeDelegate().assignAttribute(attributeDefName);
 * groupSub = new GroupSave(grouperSession).assignName("l:m:n:o").assignCreateParentStemsIfNotExist(true).save();
 * groupNotSub = new GroupSave(grouperSession).assignName("l:p").assignCreateParentStemsIfNotExist(true).save();
 * 
 * </pre>
 * 
 */
public class AssignReadonlyAdminPrivilegeGroupHook extends GroupHooks {

  /** logger */
  private static final Log logger = GrouperUtil
      .getLog(AssignReadonlyAdminPrivilegeGroupHook.class);

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPostCommitInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPostCommitInsert(final HooksContext hooksContext,
      final HooksGroupBean postCommitInsertBean) {
    //only care about this if not grouper loader
    if (GrouperContextTypeBuiltIn.GROUPER_LOADER.equals(hooksContext
        .getGrouperContextType())) {
      return;
    }

    //since we have security on the type/attribute, we need to do this as root
    GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(),
        new GrouperSessionHandler() {

          @Override
          public Object callback(GrouperSession grouperSession)
              throws GrouperSessionException {
            
            final Group thisGroup = GroupFinder.findByUuid(grouperSession,
                postCommitInsertBean.getGroup().getId(), false);
            
            if (logger.isDebugEnabled()) {
              logger.debug("The Group: " + thisGroup);
            }

            // ReadOnly Admin Enforced Attribute DefName, if found enforce this hook
            final String READONLY_ADMIN_ENFORCED_ATTRIBUTE_DEF_NAME = GrouperConfig
                .retrieveConfig().propertyValueStringRequired(
                    "grouper.readonlyAdminEnforced.attributeDefName");

            if (thisGroup.getAttributeDelegate().hasAttributeOrAncestorHasAttribute(READONLY_ADMIN_ENFORCED_ATTRIBUTE_DEF_NAME, true)) {
              
              // ReadOnly Admin Group
              final String READONLY_ADMIN_GROUP = GrouperConfig.retrieveConfig()
                  .propertyValueStringRequired("grouper.readonlyAdminEnforced.groupName");

              final Group adminGroup = GroupFinder.findByName(grouperSession,
                  READONLY_ADMIN_GROUP, false);

              if (logger.isDebugEnabled()) {
                logger.debug("Admin Group: " + adminGroup);
              }

              //assign read priv which implies view
              thisGroup.grantPriv(adminGroup.toSubject(), AccessPrivilege.READ, false);

            }

            return null;
          }
        });

  }
}