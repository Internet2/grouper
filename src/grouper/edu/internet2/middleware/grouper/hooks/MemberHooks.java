/*
 * @author mchyzer
 * $Id: MemberHooks.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMemberBean;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * member related actions
 */
public abstract class MemberHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

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

  /** constant for method name for: memberPreDelete */
  public static final String METHOD_MEMBER_PRE_DELETE = "memberPreDelete";

  /** constant for method name for: memberPreInsert */
  public static final String METHOD_MEMBER_PRE_INSERT = "memberPreInsert";

  /** constant for method name for: memberPreUpdate */
  public static final String METHOD_MEMBER_PRE_UPDATE = "memberPreUpdate";

  //*****  END GENERATED WITH GenerateMethodConstants.java *****//

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
