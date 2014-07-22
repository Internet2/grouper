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
 * $Id: MemberHooksImpl.java,v 1.4 2009-03-20 19:56:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMemberBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMemberChangeSubjectBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of member hooks for test
 */
public class MemberHooksImpl extends MemberHooks {

  /** most recent extension for testing */
  static String mostRecentPreInsertMemberSubjectId;

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.MemberHooks#memberPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMemberBean)
   */
  @Override
  public void memberPreInsert(HooksContext hooksContext, HooksMemberBean preInsertBean) {
    
    Member member = preInsertBean.getMember();
    String subjectId = (String)member.getSubjectId();
    mostRecentPreInsertMemberSubjectId = subjectId;
    if (StringUtils.equals(SubjectTestHelper.SUBJ1.getId(), subjectId)) {
      throw new HookVeto("hook.veto.member.insert.subjectId.not.subj1", "subjectId cannot be " + SubjectTestHelper.SUBJ1.getId());
    }
    
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.MemberHooks#memberPostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMemberBean)
   */
  @Override
  public void memberPostInsert(HooksContext hooksContext,
      HooksMemberBean postInsertBean) {

    Member member = postInsertBean.getMember();
    String subjectId = (String)member.getSubjectId();
    mostRecentPostInsertMemberSubjectId = subjectId;
    if (StringUtils.equals(SubjectTestHelper.SUBJ3.getId(), subjectId)) {
      throw new HookVeto("hook.veto.member.insert.subjectId.not.subj3", "subjectId cannot be " + SubjectTestHelper.SUBJ3.getId());
    }

  }

  /** most recent extension for testing */
  static String mostRecentPostUpdateMemberSubjectId;

  /**
   * @see edu.internet2.middleware.grouper.hooks.MemberHooks#memberPostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMemberBean)
   */
  @Override
  public void memberPostUpdate(HooksContext hooksContext,
      HooksMemberBean postUpdateBean) {
    
    Member member = postUpdateBean.getMember();
    String subjectId = (String)member.getSubjectId();
    mostRecentPostUpdateMemberSubjectId = subjectId;
    if (StringUtils.equals("whatever4", subjectId)) {
      throw new HookVeto("hook.veto.member.update.subjectId.not.whatever4", "subjectId cannot be whatever4");
    }

  }

  /** most recent extension for testing */
  static String mostRecentPostInsertMemberSubjectId;

  /** most recent extension for testing */
  static String mostRecentPreUpdateMemberSubjectId;

  /**
   * most recent extension for testing 
   */
  static String mostRecentPostCommitInsertMemberSubjectId;

  /**
   * most recent extension for testing 
   */
  static String mostRecentPostCommitUpdateMemberSubjectId;

  /**
   * most recent extension for testing 
   */
  static String mostRecentPostCommitDeleteMemberSubjectId;

  /**
   * most recent extension for testing 
   */
  static String mostRecentPostDeleteMemberSubjectId;

  /**
   * most recent extension for testing 
   */
  static String mostRecentPreDeleteMemberSubjectId;

  /**
   * most recent extension for testing 
   */
  static String mostRecentPostCommitChangeMemberSubjectId;

  /**
   * most recent extension for testing 
   */
  static String mostRecentPostChangeMemberSubjectId;

  /**
   * most recent extension for testing 
   */
  static String mostRecentPreChangeMemberSubjectId;

  /**
   * @see edu.internet2.middleware.grouper.hooks.MemberHooks#memberPreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMemberBean)
   */
  @Override
  public void memberPreUpdate(HooksContext hooksContext,
      HooksMemberBean preUpdateBean) {
    
    Member member = preUpdateBean.getMember();
    String subjectId = (String)member.getSubjectId();
    mostRecentPreUpdateMemberSubjectId = subjectId;
    if (StringUtils.equals("whatever2", subjectId)) {
      throw new HookVeto("hook.veto.member.update.subjectId.not.whatever2", "subjectId cannot be whatever2");
    }

  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.MemberHooks#memberPostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMemberBean)
   */
  @Override
  public void memberPostCommitInsert(HooksContext hooksContext,
      HooksMemberBean postInsertBean) {
  
    Member member = postInsertBean.getMember();
    String subjectId = (String)member.getSubjectId();
    mostRecentPostCommitInsertMemberSubjectId = subjectId;
  
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.MemberHooks#memberPostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMemberBean)
   */
  @Override
  public void memberPostCommitUpdate(HooksContext hooksContext,
      HooksMemberBean postUpdateBean) {
    
    Member member = postUpdateBean.getMember();
    String subjectId = (String)member.getSubjectId();
    mostRecentPostCommitUpdateMemberSubjectId = subjectId;
  
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.MemberHooks#memberPostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMemberBean)
   */
  @Override
  public void memberPostCommitDelete(HooksContext hooksContext,
      HooksMemberBean postUpdateBean) {
    
    Member member = postUpdateBean.getMember();
    String subjectId = (String)member.getSubjectId();
    mostRecentPostCommitDeleteMemberSubjectId = subjectId;
  
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.MemberHooks#memberPostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMemberBean)
   */
  @Override
  public void memberPostDelete(HooksContext hooksContext,
      HooksMemberBean postUpdateBean) {
    
    Member member = postUpdateBean.getMember();
    String subjectId = (String)member.getSubjectId();
    mostRecentPostDeleteMemberSubjectId = subjectId;
    if (StringUtils.equals(SubjectTestHelper.SUBJ6_ID, subjectId)) {
      throw new HookVeto("hook.veto.member.delete.subjectId.not." + SubjectTestHelper.SUBJ6_ID, 
          "subjectId cannot be " + SubjectTestHelper.SUBJ6_ID);
    }
  
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.MemberHooks#memberPreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMemberBean)
   */
  @Override
  public void memberPreDelete(HooksContext hooksContext,
      HooksMemberBean preUpdateBean) {
    
    Member member = preUpdateBean.getMember();
    String subjectId = (String)member.getSubjectId();
    mostRecentPreDeleteMemberSubjectId = subjectId;
    if (StringUtils.equals(SubjectTestHelper.SUBJ5_ID, subjectId)) {
      throw new HookVeto("hook.veto.member.delete.subjectId.not." + SubjectTestHelper.SUBJ5_ID, 
          "subjectId cannot be " + SubjectTestHelper.SUBJ5_ID);
    }
  
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.MemberHooks#memberPostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMemberBean)
   */
  @Override
  public void memberPostCommitChangeSubject(HooksContext hooksContext,
      HooksMemberChangeSubjectBean hooksMemberChangeSubjectBean) {
    
    String subjectId = hooksMemberChangeSubjectBean.getOldSubjectId();
    mostRecentPostCommitChangeMemberSubjectId = subjectId;
  
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.MemberHooks#memberPostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMemberBean)
   */
  @Override
  public void memberPostChangeSubject(HooksContext hooksContext,
      HooksMemberChangeSubjectBean hooksMemberChangeSubjectBean) {
    
    String subjectId = hooksMemberChangeSubjectBean.getOldSubjectId();
    mostRecentPostChangeMemberSubjectId = subjectId;
    if (StringUtils.equals(SubjectTestHelper.SUBJ7_ID, subjectId)) {
      throw new HookVeto("hook.veto.member.update.subjectId.not." + SubjectTestHelper.SUBJ7_ID, 
          "subjectId cannot be " + SubjectTestHelper.SUBJ7_ID);
    }
  
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.MemberHooks#memberPreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMemberBean)
   */
  @Override
  public void memberPreChangeSubject(HooksContext hooksContext,
      HooksMemberChangeSubjectBean hooksMemberChangeSubjectBean) {
    
    String subjectId = hooksMemberChangeSubjectBean.getOldSubjectId();
    mostRecentPreChangeMemberSubjectId = subjectId;
    if (StringUtils.equals(SubjectTestHelper.SUBJ8_ID, subjectId)) {
      throw new HookVeto("hook.veto.member.update.subjectId.not." + SubjectTestHelper.SUBJ8_ID, 
          "subjectId cannot be " + SubjectTestHelper.SUBJ8_ID);
    }
  
  }

}
