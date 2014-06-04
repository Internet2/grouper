/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import edu.internet2.middleware.grouper.membership.MembershipContainer;


/**
 * gui wrapper around membership container
 * @author mchyzer
 *
 */
public class GuiMembershipContainer {

  /**
   * membership container
   */
  private MembershipContainer membershipContainer;

  /**
   * membership container 
   * @return bean
   */
  public MembershipContainer getMembershipContainer() {
    return this.membershipContainer;
  }

  /**
   * construct
   * @param membershipContainer
   */
  public GuiMembershipContainer(MembershipContainer membershipContainer) {
    super();
    this.membershipContainer = membershipContainer;
  }
  
  
  
}
