/*
 * @author mchyzer
 * $Id: MembershipHooksImpl2.java,v 1.1 2008-06-26 18:08:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPostAddMemberBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of group hooks for test
 */
public class MembershipHooksImpl2 extends MembershipHooks {

  /** most recent subject id added to group */
  static String mostRecentInsertMemberSubjectId;
  

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPostAddMember(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPostAddMemberBean)
   */
  @Override
  public void membershipPostAddMember(HooksContext hooksContext, 
      HooksMembershipPostAddMemberBean postAddMemberBean) {
    String subjectId = postAddMemberBean.getMember().getSubjectId();
    mostRecentInsertMemberSubjectId = subjectId;
    if (StringUtils.equals(SubjectTestHelper.SUBJ1.getId(), subjectId)) {
      throw new HookVeto("hook.veto.subjectId.not.subj1", "subject cannot be subj1");
    }
  }

}
