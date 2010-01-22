/*
 * @author shilen
 * $Id: MembershipHooksImpl9.java,v 1.1 2009-08-18 23:11:39 shilen Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipBean;


/**
 * test implementation of group hooks for test
 */
public class MembershipHooksImpl9 extends MembershipHooks {

  /** number of times the preInsert hook was called for effective memberships */
  public static int numPreInsert = 0;
  
  /** number of times the postInsert hook was called for effective memberships */
  public static int numPostInsert = 0;
  
  /** number of times the postCommitInsert hook was called for effective memberships */
  public static int numPostCommitInsert = 0;
  
  /** number of times the preDelete hook was called for effective memberships */
  public static int numPreDelete = 0;
  
  /** number of times the postDelete hook was called for effective memberships */
  public static int numPostDelete = 0;
  
  /** number of times the postCommitDelete hook was called for effective memberships */
  public static int numPostCommitDelete = 0;
  
  /** number of times the preUpdate hook was called for effective memberships */
  public static int numPreUpdate = 0;
  
  /** number of times the postUpdate hook was called for effective memberships */
  public static int numPostUpdate = 0;
  
  /** number of times the postCommitUpdate hook was called for effective memberships */
  public static int numPostCommitUpdate = 0;
  
  /** last membership change */
  public static Membership ms = null;
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipBean)
   */
  @Override
  public void membershipPreInsert(HooksContext hooksContext,
      HooksMembershipBean hooksBean) {

    if (hooksBean.getMembership().getType().equals(Membership.EFFECTIVE)) {
      numPreInsert++;
      ms = hooksBean.getMembership();
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipBean)
   */
  @Override
  public void membershipPostInsert(HooksContext hooksContext,
      HooksMembershipBean hooksBean) {

    if (hooksBean.getMembership().getType().equals(Membership.EFFECTIVE)) {
      numPostInsert++;
      ms = hooksBean.getMembership();
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPostCommitInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipBean)
   */
  @Override
  public void membershipPostCommitInsert(HooksContext hooksContext,
      HooksMembershipBean hooksBean) {

    if (hooksBean.getMembership().getType().equals(Membership.EFFECTIVE)) {
      numPostCommitInsert++;
      ms = hooksBean.getMembership();
    }
  }
  


  /**
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPreDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipBean)
   */
  @Override
  public void membershipPreDelete(HooksContext hooksContext,
      HooksMembershipBean hooksBean) {

    if (hooksBean.getMembership().getType().equals(Membership.EFFECTIVE)) {
      numPreDelete++;
      ms = hooksBean.getMembership();
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipBean)
   */
  @Override
  public void membershipPostDelete(HooksContext hooksContext,
      HooksMembershipBean hooksBean) {

    if (hooksBean.getMembership().getType().equals(Membership.EFFECTIVE)) {
      numPostDelete++;
      ms = hooksBean.getMembership();
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPostCommitDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipBean)
   */
  @Override
  public void membershipPostCommitDelete(HooksContext hooksContext,
      HooksMembershipBean hooksBean) {

    if (hooksBean.getMembership().getType().equals(Membership.EFFECTIVE)) {
      numPostCommitDelete++;
      ms = hooksBean.getMembership();
    }
  }
  
  @Override
  public void membershipPreUpdate(HooksContext hooksContext,
      HooksMembershipBean hooksBean) {

    if (hooksBean.getMembership().getType().equals(Membership.EFFECTIVE)) {
      numPreUpdate++;
      ms = hooksBean.getMembership();
    }
  }
  
  @Override
  public void membershipPostUpdate(HooksContext hooksContext,
      HooksMembershipBean hooksBean) {

    if (hooksBean.getMembership().getType().equals(Membership.EFFECTIVE)) {
      numPostUpdate++;
      ms = hooksBean.getMembership();
    }
  }
  
  @Override
  public void membershipPostCommitUpdate(HooksContext hooksContext,
      HooksMembershipBean hooksBean) {

    if (hooksBean.getMembership().getType().equals(Membership.EFFECTIVE)) {
      numPostCommitUpdate++;
      ms = hooksBean.getMembership();
    }
  }

  /**
   * 
   */
  public static void resetVariables() {

    numPreInsert = 0;
    numPostInsert = 0;
    numPostCommitInsert = 0;
    numPreDelete = 0;
    numPostDelete = 0;    
    numPostCommitDelete = 0;
    numPreUpdate = 0;
    numPostUpdate = 0;    
    numPostCommitUpdate = 0;
    
    ms = null;
  }

}
