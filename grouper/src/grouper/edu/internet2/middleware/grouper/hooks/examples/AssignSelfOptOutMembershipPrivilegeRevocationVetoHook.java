package edu.internet2.middleware.grouper.hooks.examples;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 */
public class AssignSelfOptOutMembershipPrivilegeRevocationVetoHook extends MembershipHooks {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log logger = GrouperUtil.getLog(Group.class);

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPreRemoveMember(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean)
   */
  @Override
  public void membershipPreRemoveMember(HooksContext hooksContext,
      HooksMembershipChangeBean preDeleteMemberBean) {
    
    //if we are in group delete, then allow
    if (Group.deleteOccuring()) {
      return;
    }
    
    Field field = preDeleteMemberBean.getMembership().getField();

    if (AccessPrivilege.OPTOUT.getField().getName().equals(field.getName())
        && StringUtils.equals(preDeleteMemberBean.getMember().getSubjectSourceId(), GrouperSourceAdapter.groupSourceId())) {
      Group thisGroup = preDeleteMemberBean.getGroup();
      Group membershipGroup = preDeleteMemberBean.getMember().toGroup();

      if (thisGroup.getUuid().equals(membershipGroup.getUuid())) {
        throw new HookVeto("self.optout.remove.veto",
            "Cannot remove self-assigned OptOut privilege.");
      }
    }
  }
}
