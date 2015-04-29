/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Membership;
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
   * @param membershipContainer1
   */
  public GuiMembershipContainer(MembershipContainer membershipContainer1) {
    super();
    this.membershipContainer = membershipContainer1;
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

    if (this.guiGroupOwner == null) {

      if (this.membershipContainer != null) {

        Membership immediateMembership = this.membershipContainer.getImmediateMembership();

        if (immediateMembership != null) {
          if (!StringUtils.isBlank(immediateMembership.getOwnerGroupId())) {
            this.guiGroupOwner = new GuiGroup(immediateMembership.getOwnerGroup());
          
          }
        }
      }
    }
    
    return this.guiGroupOwner;
  }
  
}
