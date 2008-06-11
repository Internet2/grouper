/*
 * @author mchyzer
 * $Id: MembershipHooksImpl.java,v 1.1.2.2 2008-06-11 06:19:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPreAddMemberBean;
import edu.internet2.middleware.grouper.hooks.veto.HookVetoMembershipUpdate;


/**
 * test implementation of group hooks for test
 */
public class MembershipHooksImpl extends MembershipHooks {

  /** most recent subject id added to group */
  private static String mostRecentInsertMemberSubjectId;
  
  /**
   * @return the mostRecentInsertMemberSubjectId
   */
  public static String getMostRecentInsertMemberSubjectId() {
    return mostRecentInsertMemberSubjectId;
  }


  /**
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPreAddMember(edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPreAddMemberBean)
   */
  @Override
  public void membershipPreAddMember(
      HooksMembershipPreAddMemberBean preAddMemberBean) {
    String subjectId = preAddMemberBean.getMemberDTO().getSubjectId();
    mostRecentInsertMemberSubjectId = subjectId;
    if (StringUtils.equals(SubjectTestHelper.SUBJ1.getId(), subjectId)) {
      throw new HookVetoMembershipUpdate("hook.veto.subjectId.not.subj1", "subject cannot be subj1");
    }
  }

}
