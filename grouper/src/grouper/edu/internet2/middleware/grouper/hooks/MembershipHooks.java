/*
 * @author mchyzer
 * $Id: MembershipHooks.java,v 1.3 2008-06-26 11:16:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPostDeleteBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPostInsertBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPostUpdateBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPostAddMemberBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPreDeleteBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPreInsertBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPreUpdateBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPreAddMemberBean;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * membership related actions
 */
public abstract class MembershipHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: membershipPostAddMember */
  public static final String METHOD_MEMBERSHIP_POST_ADD_MEMBER = "membershipPostAddMember";

  /** constant for method name for: membershipPostDelete */
  public static final String METHOD_MEMBERSHIP_POST_DELETE = "membershipPostDelete";

  /** constant for method name for: membershipPostInsert */
  public static final String METHOD_MEMBERSHIP_POST_INSERT = "membershipPostInsert";

  /** constant for method name for: membershipPostUpdate */
  public static final String METHOD_MEMBERSHIP_POST_UPDATE = "membershipPostUpdate";

  /** constant for method name for: membershipPreAddMember */
  public static final String METHOD_MEMBERSHIP_PRE_ADD_MEMBER = "membershipPreAddMember";

  /** constant for method name for: membershipPreDelete */
  public static final String METHOD_MEMBERSHIP_PRE_DELETE = "membershipPreDelete";

  /** constant for method name for: membershipPreInsert */
  public static final String METHOD_MEMBERSHIP_PRE_INSERT = "membershipPreInsert";

  /** constant for method name for: membershipPreUpdate */
  public static final String METHOD_MEMBERSHIP_PRE_UPDATE = "membershipPreUpdate";

  //*****  END GENERATED WITH GenerateMethodConstants.java *****//

  /**
   * called right before a membership update
   * @param hooksContext
   * @param preUpdateBean
   */
  public void membershipPreUpdate(HooksContext hooksContext, HooksMembershipPreUpdateBean preUpdateBean) {
    
  }
  
  /**
   * called right after a membership update
   * @param hooksContext
   * @param postUpdateBean
   */
  public void membershipPostUpdate(HooksContext hooksContext, HooksMembershipPostUpdateBean postUpdateBean) {
    
  }
  
  /**
   * called right before a membership update (high level, not the side effects)
   * @param hooksContext
   * @param preAddMemberBean
   */
  public void membershipPreAddMember(HooksContext hooksContext, 
      HooksMembershipPreAddMemberBean preAddMemberBean) {
    
  }
  
  /**
   * called right after a membership update (high level, not the side effects)
   * @param hooksContext
   * @param postAddMemberBean
   */
  public void membershipPostAddMember(HooksContext hooksContext, 
      HooksMembershipPostAddMemberBean postAddMemberBean) {
    
  }
  
  /**
   * called right before a membership insert
   * @param hooksContext
   * @param preInsertBean
   */
  public void membershipPreInsert(HooksContext hooksContext, HooksMembershipPreInsertBean preInsertBean) {
    
  }
  
  /**
   * called right after a membership insert
   * @param hooksContext
   * @param postInsertBean
   */
  public void membershipPostInsert(HooksContext hooksContext, HooksMembershipPostInsertBean postInsertBean) {
    
  }
  
  /**
   * called right before a membership delete
   * @param hooksContext
   * @param preDeleteBean
   */
  public void membershipPreDelete(HooksContext hooksContext, HooksMembershipPreDeleteBean preDeleteBean) {
    
  }
  
  /**
   * called right after a membership insert
   * @param hooksContext
   * @param postDeleteBean
   */
  public void membershipPostDelete(HooksContext hooksContext, HooksMembershipPostDeleteBean postDeleteBean) {
    
  }
  
}
