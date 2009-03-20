/*
 * @author mchyzer
 * $Id: MembershipHooksImpl2.java,v 1.4 2009-03-20 19:56:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of group hooks for test
 */
public class MembershipHooksImpl2 extends MembershipHooks {

  /** most recent subject id added to group */
  static String mostRecentInsertMemberSubjectId;
  

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPostAddMember(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean)
   */
  @Override
  public void membershipPostAddMember(HooksContext hooksContext, 
      HooksMembershipChangeBean postAddMemberBean) {
    String subjectId = postAddMemberBean.getMember().getSubjectId();
    mostRecentInsertMemberSubjectId = subjectId;
    if (StringUtils.equals(SubjectTestHelper.SUBJ1.getId(), subjectId)) {
      throw new HookVeto("hook.veto.subjectId.not.subj1", "subject cannot be subj1");
    }
  }

}
