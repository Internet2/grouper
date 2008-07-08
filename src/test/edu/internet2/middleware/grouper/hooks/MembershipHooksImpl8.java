/*
 * @author mchyzer
 * $Id: MembershipHooksImpl8.java,v 1.1 2008-07-08 06:51:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.MemberNotFoundException;
import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of group hooks for test
 */
public class MembershipHooksImpl8 extends MembershipHooks {

  /**
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPostRemoveMember(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean)
   */
  @Override
  public void membershipPostRemoveMember(HooksContext hooksContext,
      HooksMembershipChangeBean postDeleteMemberBean) {
    try {
      String subjectId = postDeleteMemberBean.getMembership().getMember().getSubjectId();
      mostRecentDeleteMemberSubjectId = subjectId;
      if (StringUtils.equals(SubjectTestHelper.SUBJ1.getId(), subjectId)) {
        throw new HookVeto("hook.veto.subjectId.isNot.subj1", "subject cannot be subj1");
      }
    } catch (MemberNotFoundException mnfe) {
      throw new RuntimeException(mnfe);
    }
  }


  /** most recent subject id added to group */
  static String mostRecentDeleteMemberSubjectId;

}
