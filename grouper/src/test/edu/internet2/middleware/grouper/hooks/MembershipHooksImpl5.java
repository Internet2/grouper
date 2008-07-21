/*
 * @author mchyzer
 * $Id: MembershipHooksImpl5.java,v 1.3 2008-07-21 04:43:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of group hooks for test
 */
public class MembershipHooksImpl5 extends MembershipHooks {

  /** most recent subject id added to group */
  static String mostRecentDeleteMemberSubjectId;
  

  /**
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPreDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPreDeleteBean)
   */
  @Override
  public void membershipPreDelete(HooksContext hooksContext,
      HooksMembershipBean preDeleteBean) {
    try {
      String subjectId = preDeleteBean.getMembership().getMember().getSubjectId();
      mostRecentDeleteMemberSubjectId = subjectId;
      if (StringUtils.equals(SubjectTestHelper.SUBJ1.getId(), subjectId)) {
        throw new HookVeto("hook.veto.subjectId.not.subj1", "subject cannot be subj1");
      }
    } catch (MemberNotFoundException mnfe) {
      throw new RuntimeException(mnfe);
    }
  }

}
