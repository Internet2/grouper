/*
 * @author mchyzer
 * $Id: HooksMembershipPreInsertBean.java,v 1.1.2.2 2008-06-18 09:22:22 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.internal.dto.MembershipDTO;


/**
 * pre insert bean
 */
public class HooksMembershipPreInsertBean extends HooksBean {

  /** object being inserted */
  private MembershipDTO membershipDto = null;
  
  /**
   * @param theHooksContext
   * @param theMembershipDto 
   */
  public HooksMembershipPreInsertBean(HooksContext theHooksContext, MembershipDTO theMembershipDto) {
    super(theHooksContext);
    this.membershipDto = theMembershipDto;
  }
  
  /**
   * object being inserted
   * @return the MembershipDAO
   */
  public MembershipDTO getMembershipDto() {
    return this.membershipDto;
  }

  
  
}
