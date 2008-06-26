/*
 * @author mchyzer
 * $Id: MembershipHooksImpl4.java,v 1.1 2008-06-26 18:08:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.MemberNotFoundException;
import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPostInsertBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of group hooks for test
 */
public class MembershipHooksImpl4 extends MembershipHooks {

  /** most recent subject id added to group */
  static String mostRecentInsertMemberSubjectId;
  


  /**
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPostInsertBean)
   */
  @Override
  public void membershipPostInsert(HooksContext hooksContext,
      HooksMembershipPostInsertBean postInsertBean) {
    try {
      String subjectId = postInsertBean.getMembership().getMember().getSubjectId();
      mostRecentInsertMemberSubjectId = subjectId;
      if (StringUtils.equals(SubjectTestHelper.SUBJ1.getId(), subjectId)) {
        throw new HookVeto("hook.veto.subjectId.not.subj1", "subject cannot be subj1");
      }
    } catch (MemberNotFoundException mnfe) {
      throw new RuntimeException(mnfe);
    }
  }

}
