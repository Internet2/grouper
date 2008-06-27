/*
 * @author mchyzer
 * $Id: HooksMemberBean.java,v 1.1 2008-06-27 19:04:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Member;


/**
 *
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
