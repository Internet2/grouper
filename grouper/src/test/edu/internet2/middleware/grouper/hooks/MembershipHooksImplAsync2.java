/**
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
 */
/*
 * @author mchyzer
 * $Id: MembershipHooksImplAsync2.java,v 1.2 2008-07-20 21:18:57 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.grouper.hooks.logic.HookAsynchronousMarker;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * test implementation of group hooks for test
 */
public class MembershipHooksImplAsync2 extends MembershipHooks implements HookAsynchronousMarker {

  /** most recent subject id added to group */
  static String mostRecentInsertMemberSubjectId;

  /** keep track of hook count seconds (2 for each) */
  static int preAddMemberHookCountAyncSeconds = 0;
  
  /** let the outer know when done */
  static boolean done = false;
  
  /** let the outer know problems */
  static Exception problem;
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPreAddMember(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean)
   */
  @Override
  public void membershipPreAddMember(HooksContext hooksContext, 
      HooksMembershipChangeBean preAddMemberBean) {

        
    done = false;
    problem = null;
    
    String subjectId = preAddMemberBean.getMember().getSubjectId();
    mostRecentInsertMemberSubjectId = subjectId;
    preAddMemberHookCountAyncSeconds++;
    GrouperUtil.sleep(1000);
    preAddMemberHookCountAyncSeconds++;
    
    //get a session
    GrouperSession grouperSession = hooksContext.grouperSession();
    
    if (grouperSession == MembershipHooksTest.grouperSession) {
      problem = new RuntimeException("GrouperSession is the same instance");
    }

    if (!StringUtils.equals(grouperSession.getSubject().getId(), MembershipHooksTest.grouperSession.getSubject().getId())) {
      problem = new RuntimeException("Grouper session doesnt have same subject id: " + grouperSession.getSubject().getId()
          + ", " + MembershipHooksTest.grouperSession.getSubject().getId());
    }
    
    done = true;
    
  }

}
