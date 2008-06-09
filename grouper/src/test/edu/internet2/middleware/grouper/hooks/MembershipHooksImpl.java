/*
 * @author mchyzer
 * $Id: MembershipHooksImpl.java,v 1.1.2.1 2008-06-09 19:26:05 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.DefaultMemberOf;
import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPreUpdateHighLevelBean;
import edu.internet2.middleware.grouper.hooks.veto.HookVetoGroupInsert;
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
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPreUpdateHighLevel(edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPreUpdateHighLevelBean)
   */
  @Override
  public void membershipPreUpdateHighLevel(
      HooksMembershipPreUpdateHighLevelBean preUpdateHighLevelBean) {
    DefaultMemberOf defaultMemberOf = preUpdateHighLevelBean.getDefaultMemberOf();
    String subjectId = defaultMemberOf.getMemberDTO().getSubjectId();
    mostRecentInsertMemberSubjectId = subjectId;
    if (StringUtils.equals(SubjectTestHelper.SUBJ1.getId(), subjectId)) {
      throw new HookVetoMembershipUpdate("hook.veto.subjectId.not.subj1", "subject cannot be subj1");
    }
  }

}
