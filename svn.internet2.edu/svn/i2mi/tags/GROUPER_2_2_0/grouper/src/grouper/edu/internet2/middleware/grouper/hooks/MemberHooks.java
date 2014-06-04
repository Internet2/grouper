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
 * $Id: MemberHooks.java,v 1.3 2008-10-17 12:06:37 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMemberBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMemberChangeSubjectBean;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * member related actions
 */
public abstract class MemberHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: memberPostChangeSubject */
  public static final String METHOD_MEMBER_POST_CHANGE_SUBJECT = "memberPostChangeSubject";

  /** constant for method name for: memberPostCommitChangeSubject */
  public static final String METHOD_MEMBER_POST_COMMIT_CHANGE_SUBJECT = "memberPostCommitChangeSubject";

  /** constant for method name for: memberPostCommitDelete */
  public static final String METHOD_MEMBER_POST_COMMIT_DELETE = "memberPostCommitDelete";

  /** constant for method name for: memberPostCommitInsert */
  public static final String METHOD_MEMBER_POST_COMMIT_INSERT = "memberPostCommitInsert";

  /** constant for method name for: memberPostCommitUpdate */
  public static final String METHOD_MEMBER_POST_COMMIT_UPDATE = "memberPostCommitUpdate";

  /** constant for method name for: memberPostDelete */
  public static final String METHOD_MEMBER_POST_DELETE = "memberPostDelete";

  /** constant for method name for: memberPostInsert */
  public static final String METHOD_MEMBER_POST_INSERT = "memberPostInsert";

  /** constant for method name for: memberPostUpdate */
  public static final String METHOD_MEMBER_POST_UPDATE = "memberPostUpdate";

  /** constant for method name for: memberPreChangeSubject */
  public static final String METHOD_MEMBER_PRE_CHANGE_SUBJECT = "memberPreChangeSubject";

  /** constant for method name for: memberPreDelete */
  public static final String METHOD_MEMBER_PRE_DELETE = "memberPreDelete";

  /** constant for method name for: memberPreInsert */
  public static final String METHOD_MEMBER_PRE_INSERT = "memberPreInsert";

  /** constant for method name for: memberPreUpdate */
  public static final String METHOD_MEMBER_PRE_UPDATE = "memberPreUpdate";

  //*****  END GENERATED WITH GenerateMethodConstants.java *****//

  /**
   * in the transaction, but before any work is done, in a change subject
   * @param hooksContext
   * @param hooksMemberChangeSubjectBean
   */
  public void memberPreChangeSubject(HooksContext hooksContext, 
      HooksMemberChangeSubjectBean hooksMemberChangeSubjectBean) {
    
  }
  
  /**
   * in the transaction, but after all work is done, in a change subject
   * @param hooksContext
   * @param hooksMemberChangeSubjectBean
   */
  public void memberPostChangeSubject(HooksContext hooksContext, 
      HooksMemberChangeSubjectBean hooksMemberChangeSubjectBean) {
    
  }

  /**
   * called after a change subject is committed
   * @param hooksContext
   * @param hooksMemberChangeSubjectBean
   */
  public void memberPostCommitChangeSubject(HooksContext hooksContext, 
      HooksMemberChangeSubjectBean hooksMemberChangeSubjectBean) {
    
  }

  /**
   * called right before a member update
   * @param hooksContext
   * @param preUpdateBean
   */
  public void memberPreUpdate(HooksContext hooksContext, HooksMemberBean preUpdateBean) {
    
  }
  
  /**
   * called right after a member update
   * @param hooksContext
   * @param postUpdateBean
   */
  public void memberPostUpdate(HooksContext hooksContext, HooksMemberBean postUpdateBean) {
    
  }
  
  /**
   * called right before a member insert
   * @param hooksContext
   * @param preInsertBean
   */
  public void memberPreInsert(HooksContext hooksContext, HooksMemberBean preInsertBean) {
    
  }
  
  /**
   * called right after a member insert
   * @param hooksContext
   * @param postInsertBean
   */
  public void memberPostInsert(HooksContext hooksContext, HooksMemberBean postInsertBean) {
    
  }
  
  /**
   * called right before a member delete
   * @param hooksContext
   * @param preDeleteBean
   */
  public void memberPreDelete(HooksContext hooksContext, HooksMemberBean preDeleteBean) {
    
  }
  
  /**
   * called right after a member delete
   * @param hooksContext
   * @param postDeleteBean
   */
  public void memberPostDelete(HooksContext hooksContext, HooksMemberBean postDeleteBean) {
    
  }

  /**
   * called right after a member delete commit
   * @param hooksContext
   * @param postCommitDeleteBean
   */
  public void memberPostCommitDelete(HooksContext hooksContext, HooksMemberBean postCommitDeleteBean) {
    
  }

  /**
   * called right after a member insert commit
   * @param hooksContext
   * @param postCommitInsertBean
   */
  public void memberPostCommitInsert(HooksContext hooksContext, HooksMemberBean postCommitInsertBean) {
    
  }

  /**
   * called right after a member update commit
   * @param hooksContext
   * @param postCommitUpdateBean
   */
  public void memberPostCommitUpdate(HooksContext hooksContext, HooksMemberBean postCommitUpdateBean) {
    
  }
  
}
