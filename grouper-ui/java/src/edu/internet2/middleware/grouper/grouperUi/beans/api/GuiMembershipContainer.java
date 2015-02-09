/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import org.apache.commons.lang.StringUtils;

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

  /**
   * 
   */
  private GuiGroup guiGroupOwner;
  
  /**
   * 
   * @return the gui group container
   */
  public GuiGroup getGuiGroupOwner() {
    if (this.guiGroupOwner == null && !StringUtils.isBlank(this.membershipContainer.getImmediateMembership().getOwnerGroupId())) {
      this.guiGroupOwner = new GuiGroup(this.membershipContainer.getImmediateMembership().getOwnerGroup());
    }
    return this.guiGroupOwner;
  }
  
}
