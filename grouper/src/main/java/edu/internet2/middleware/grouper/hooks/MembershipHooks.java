/**
 * Copyright 2014 Internet2
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
 * $Id: MembershipHooks.java,v 1.6 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipBean;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * membership related actions
 */
public abstract class MembershipHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: membershipPostAddMember */
  public static final String METHOD_MEMBERSHIP_POST_ADD_MEMBER = "membershipPostAddMember";

  /** constant for method name for: membershipPostCommitAddMember */
  public static final String METHOD_MEMBERSHIP_POST_COMMIT_ADD_MEMBER = "membershipPostCommitAddMember";

  /** constant for method name for: membershipPostCommitDelete */
  public static final String METHOD_MEMBERSHIP_POST_COMMIT_DELETE = "membershipPostCommitDelete";

  /** constant for method name for: membershipPostCommitInsert */
  public static final String METHOD_MEMBERSHIP_POST_COMMIT_INSERT = "membershipPostCommitInsert";

  /** constant for method name for: membershipPostCommitRemoveMember */
  public static final String METHOD_MEMBERSHIP_POST_COMMIT_REMOVE_MEMBER = "membershipPostCommitRemoveMember";

  /** constant for method name for: membershipPostCommitUpdate */
  public static final String METHOD_MEMBERSHIP_POST_COMMIT_UPDATE = "membershipPostCommitUpdate";

  /** constant for method name for: membershipPostDelete */
  public static final String METHOD_MEMBERSHIP_POST_DELETE = "membershipPostDelete";

  /** constant for method name for: membershipPostInsert */
  public static final String METHOD_MEMBERSHIP_POST_INSERT = "membershipPostInsert";

  /** constant for method name for: membershipPostRemoveMember */
  public static final String METHOD_MEMBERSHIP_POST_REMOVE_MEMBER = "membershipPostRemoveMember";

  /** constant for method name for: membershipPostUpdate */
  public static final String METHOD_MEMBERSHIP_POST_UPDATE = "membershipPostUpdate";

  /** constant for method name for: membershipPreAddMember */
  public static final String METHOD_MEMBERSHIP_PRE_ADD_MEMBER = "membershipPreAddMember";

  /** constant for method name for: membershipPreDelete */
  public static final String METHOD_MEMBERSHIP_PRE_DELETE = "membershipPreDelete";

  /** constant for method name for: membershipPreInsert */
  public static final String METHOD_MEMBERSHIP_PRE_INSERT = "membershipPreInsert";

  /** constant for method name for: membershipPreRemoveMember */
  public static final String METHOD_MEMBERSHIP_PRE_REMOVE_MEMBER = "membershipPreRemoveMember";

  /** constant for method name for: membershipPreUpdate */
  public static final String METHOD_MEMBERSHIP_PRE_UPDATE = "membershipPreUpdate";

  //*****  END GENERATED WITH GenerateMethodConstants.java *****//

  /**
   * called right before a membership update
   * @param hooksContext
   * @param preUpdateBean
   */
  public void membershipPreUpdate(HooksContext hooksContext, HooksMembershipBean preUpdateBean) {
    
  }
  
  /**
   * called right after a membership update
   * @param hooksContext
   * @param postUpdateBean
   */
  public void membershipPostUpdate(HooksContext hooksContext, HooksMembershipBean postUpdateBean) {
    
  }
  
  /**
   * called right before a membership update (high level, not the side effects)
   * @param hooksContext
   * @param preAddMemberBean
   */
  public void membershipPreAddMember(HooksContext hooksContext, 
      HooksMembershipChangeBean preAddMemberBean) {
    
  }
  
  /**
   * called right before a membership delete (high level, not the side effects)
   * @param hooksContext
   * @param preDeleteMemberBean
   */
  public void membershipPreRemoveMember(HooksContext hooksContext, 
      HooksMembershipChangeBean preDeleteMemberBean) {
    
  }
  
  /**
   * called right after a membership update (high level, not the side effects)
   * @param hooksContext
   * @param postAddMemberBean
   */
  public void membershipPostAddMember(HooksContext hooksContext, 
      HooksMembershipChangeBean postAddMemberBean) {
    
  }
  
  /**
   * called right after a membership delete (high level, not the side effects)
   * @param hooksContext
   * @param postDeleteMemberBean
   */
  public void membershipPostRemoveMember(HooksContext hooksContext, 
      HooksMembershipChangeBean postDeleteMemberBean) {
    
  }
  
  /**
   * called right before a membership insert
   * @param hooksContext
   * @param preInsertBean
   */
  public void membershipPreInsert(HooksContext hooksContext, HooksMembershipBean preInsertBean) {
    
  }
  
  /**
   * called right after a membership insert
   * @param hooksContext
   * @param postInsertBean
   */
  public void membershipPostInsert(HooksContext hooksContext, HooksMembershipBean postInsertBean) {
    
  }
  
  /**
   * called right before a membership delete
   * @param hooksContext
   * @param preDeleteBean
   */
  public void membershipPreDelete(HooksContext hooksContext, HooksMembershipBean preDeleteBean) {
    
  }
  
  /**
   * called right after a membership delete
   * @param hooksContext
   * @param postDeleteBean
   */
  public void membershipPostDelete(HooksContext hooksContext, HooksMembershipBean postDeleteBean) {
    
  }

  /**
   * called right after a membership update (high level, not the side effects)
   * @param hooksContext
   * @param postAddMemberBean
   */
  public void membershipPostCommitAddMember(HooksContext hooksContext, 
      HooksMembershipChangeBean postAddMemberBean) {
    
  }

  /**
   * called right after a membership delete commit
   * @param hooksContext
   * @param postDeleteBean
   */
  public void membershipPostCommitDelete(HooksContext hooksContext, HooksMembershipBean postDeleteBean) {
    
  }

  /**
   * called right after a membership insert commit
   * @param hooksContext
   * @param postInsertBean
   */
  public void membershipPostCommitInsert(HooksContext hooksContext, HooksMembershipBean postInsertBean) {
    
  }

  /**
   * called right after a membership delete commit (high level, not the side effects)
   * @param hooksContext
   * @param postDeleteMemberBean
   */
  public void membershipPostCommitRemoveMember(HooksContext hooksContext, 
      HooksMembershipChangeBean postDeleteMemberBean) {
    
  }

  /**
   * called right after a membership update commit
   * @param hooksContext
   * @param postUpdateBean
   */
  public void membershipPostCommitUpdate(HooksContext hooksContext, HooksMembershipBean postUpdateBean) {
    
  }
  
}
