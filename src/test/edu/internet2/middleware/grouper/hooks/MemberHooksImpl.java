/*
 * @author mchyzer
 * $Id: MemberHooksImpl.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMemberBean;
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

}
