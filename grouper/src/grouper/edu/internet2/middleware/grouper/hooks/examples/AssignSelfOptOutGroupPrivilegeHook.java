package edu.internet2.middleware.grouper.hooks.examples;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

import org.apache.commons.logging.Log;

/**
 * AssignSelfOptOutPrivilege adds opt-out privilege for the newly created groups to self (this group's subject)
 */
public class AssignSelfOptOutGroupPrivilegeHook extends GroupHooks {

  /** logger */
  private static final Log logger = GrouperUtil.getLog(Group.class);

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPostCommitInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPostCommitInsert(HooksContext hooksContext,
      HooksGroupBean postCommitInsertBean) {
    //only care about this if not grouper loader
    if (GrouperContextTypeBuiltIn.GROUPER_LOADER.equals(hooksContext
        .getGrouperContextType())) {
      return;
    }

    Group thisGroup = GroupFinder.findByUuid(GrouperSession.startRootSession(),
        postCommitInsertBean.getGroup().getId(), false);
    if (logger.isDebugEnabled()) {
      logger.debug("The Group: " + thisGroup);
      logger.debug("Group's subject " + thisGroup.toSubject());
    }
    //assign opt out priv
    thisGroup.grantPriv(thisGroup.toSubject(), AccessPrivilege.OPTOUT, false);
  }
}
