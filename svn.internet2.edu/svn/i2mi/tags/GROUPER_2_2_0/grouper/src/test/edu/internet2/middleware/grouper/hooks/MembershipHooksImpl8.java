/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: MembershipHooksImpl8.java,v 1.4 2009-03-20 19:56:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
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
      mostRecentRemoveMemberSubjectId = subjectId;
      if (StringUtils.equals(SubjectTestHelper.SUBJ1.getId(), subjectId)) {
        throw new HookVeto("hook.veto.subjectId.isNot.subj1", "subject cannot be subj1");
      }
    } catch (MemberNotFoundException mnfe) {
      throw new RuntimeException(mnfe);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPostRemoveMember(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean)
   */
  @Override
  public void membershipPostCommitRemoveMember(HooksContext hooksContext,
      HooksMembershipChangeBean postDeleteCommitMemberBean) {
    try {
      String subjectId = postDeleteCommitMemberBean.getMembership().getMember().getSubjectId();
      mostRecentRemoveCommitMemberSubjectId = subjectId;
    } catch (MemberNotFoundException mnfe) {
      throw new RuntimeException(mnfe);
    }
  }


  /** most recent subject id added to group */
  static String mostRecentRemoveMemberSubjectId;

  /** most recent subject id added to group */
  static String mostRecentRemoveCommitMemberSubjectId;

}
