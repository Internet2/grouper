/*
 * @author mchyzer
 * $Id: HooksMembershipPreInsertBean.java,v 1.1.2.1 2008-06-09 19:26:05 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.internal.dao.MembershipDAO;


/**
 * pre insert bean
 */
public class HooksMembershipPreInsertBean extends HooksBean {

  /** object being inserted */
  private MembershipDAO membershipDao = null;
  
  /**
   * @param theHooksContext
   * @param theMembershipDao 
   */
  public HooksMembershipPreInsertBean(HooksContext theHooksContext, MembershipDAO theMembershipDao) {
    super(theHooksContext);
    this.membershipDao = theMembershipDao;
  }
  
  /**
   * object being inserted
   * @return the MembershipDAO
   */
  public MembershipDAO getMembershipDao() {
    return this.membershipDao;
  }

  
  
}
