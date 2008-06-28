/*
 * @author mchyzer
 * $Id: HooksMemberBean.java,v 1.2 2008-06-28 06:55:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Member;


/**
 * bean to hold objects for member low level hooks
 */
public class HooksMemberBean extends HooksBean {
  
  /** object being affected */
  private Member member = null;
  
  /**
   * @param theMember
   */
  public HooksMemberBean(Member theMember) {
    this.member = theMember;
  }
  
  /**
   * object being inserted
   * @return the Member
   */
  public Member getMember() {
    return this.member;
  }

}
