/*
 * @author mchyzer
 * $Id: GrouperSessionHooksImpl.java,v 1.1 2008-06-29 17:42:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGrouperSessionBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of grouperSession hooks for test
 */
public class GrouperSessionHooksImpl extends GrouperSessionHooks {

  /** most recent subjectId for testing */
  static String mostRecentPreInsertGrouperSessionSubjectId;

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GrouperSessionHooks#grouperSessionPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGrouperSessionBean)
   */
  @Override
  public void grouperSessionPreInsert(HooksContext hooksContext, HooksGrouperSessionBean preInsertBean) {
    
    GrouperSession grouperSession = preInsertBean.getGrouperSession();
    String subjectId = grouperSession.getSubject().getId();
    mostRecentPreInsertGrouperSessionSubjectId = subjectId;
    if (StringUtils.equals(SubjectTestHelper.SUBJ1.getId(), subjectId)) {
      throw new HookVeto("hook.veto.grouperSession.insert.subjectId.not.test2", "subjectId cannot be " + SubjectTestHelper.SUBJ1.getId());
    }
    
  }

  /** most recent subjectId for testing */
  static String mostRecentPostDeleteGrouperSessionSubjectId;

  /**
   * @see edu.internet2.middleware.grouper.hooks.GrouperSessionHooks#grouperSessionPostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGrouperSessionBean)
   */
  @Override
  public void grouperSessionPostDelete(HooksContext hooksContext,
      HooksGrouperSessionBean postDeleteBean) {
    

    GrouperSession grouperSession = postDeleteBean.getGrouperSession();
    String subjectId = grouperSession.getSubject().getId();
    mostRecentPostDeleteGrouperSessionSubjectId = subjectId;
    if (StringUtils.equals(SubjectTestHelper.SUBJ5.getId(), subjectId)) {
      throw new HookVeto("hook.veto.grouperSession.delete.subjectId.not.test5", 
          "subjectId cannot be " + SubjectTestHelper.SUBJ5.getId());
    }

  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.GrouperSessionHooks#grouperSessionPostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGrouperSessionBean)
   */
  @Override
  public void grouperSessionPostInsert(HooksContext hooksContext,
      HooksGrouperSessionBean postInsertBean) {

    GrouperSession grouperSession = postInsertBean.getGrouperSession();
    String subjectId = grouperSession.getSubject().getId();
    mostRecentPostInsertGrouperSessionSubjectId = subjectId;
    if (StringUtils.equals(SubjectTestHelper.SUBJ3.getId(), subjectId)) {
      throw new HookVeto("hook.veto.grouperSession.insert.subjectId.not.test3", "subjectId cannot be " + SubjectTestHelper.SUBJ3.getId());
    }

  }

  /** most recent subjectId for testing */
  static String mostRecentPostUpdateGrouperSessionSubjectId;

  /** most recent subjectId for testing */
  static String mostRecentPreDeleteGrouperSessionSubjectId;

  /**
   * @see edu.internet2.middleware.grouper.hooks.GrouperSessionHooks#grouperSessionPreDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGrouperSessionBean)
   */
  @Override
  public void grouperSessionPreDelete(HooksContext hooksContext,
      HooksGrouperSessionBean preDeleteBean) {

    GrouperSession grouperSession = preDeleteBean.getGrouperSession();
    String subjectId = grouperSession.getSubject().getId();
    mostRecentPreDeleteGrouperSessionSubjectId = subjectId;
    if (StringUtils.equals(SubjectTestHelper.SUBJ4.getId(), subjectId)) {
      throw new HookVeto("hook.veto.grouperSession.delete.subjectId.not.test4", 
          "subjectId cannot be " + SubjectTestHelper.SUBJ4.getId());
    }
    
  }

  /** most recent subjectId for testing */
  static String mostRecentPostInsertGrouperSessionSubjectId;

  /** most recent subjectId for testing */
  static String mostRecentPreUpdateGrouperSessionSubjectId;

}
