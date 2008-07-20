/*
 * @author mchyzer
 * $Id: MembershipHooksImpl3.java,v 1.3 2008-07-20 21:18:57 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.MemberNotFoundException;
import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of group hooks for test
 */
public class MembershipHooksImpl3 extends MembershipHooks {

  /** most recent subject id added to group */
  static String mostRecentInsertMemberSubjectId;

  /** value of hooks context */
  static String hooksContextValue;

  /** value of the second hooks context */
  static String hooksContextValue2;

  /**
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipBean)
   */
  @Override
  public void membershipPreInsert(HooksContext hooksContext,
      HooksMembershipBean preInsertBean) {
    try {
      hooksContextValue = (String)hooksContext.getAttribute("testMemberPreInsert");
      hooksContextValue2 = (String)hooksContext.getAttribute("testMemberPreInsert2");
      String subjectId = preInsertBean.getMembership().getMember().getSubjectId();
      mostRecentInsertMemberSubjectId = subjectId;
      if (StringUtils.equals(SubjectTestHelper.SUBJ1.getId(), subjectId)) {
        throw new HookVeto("hook.veto.subjectId.not.subj1", "subject cannot be subj1");
      }
    } catch (MemberNotFoundException mnfe) {
      throw new RuntimeException(mnfe);
    }
  }

}
