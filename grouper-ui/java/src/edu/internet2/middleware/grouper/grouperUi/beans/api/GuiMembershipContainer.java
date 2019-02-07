/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
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
  
  /**
   * start label string yyyy/mm/dd
   * @return the start label string yyyy/mm/dd
   */
  public String getImmediateMembershipStartDateLabel() {
        
    if (this.membershipContainer == null || this.membershipContainer.getImmediateMembership() == null || 
        this.membershipContainer.getImmediateMembership().getEnabledTime() == null) {
      return null;
    }
    
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
    
    return simpleDateFormat.format(this.membershipContainer.getImmediateMembership().getEnabledTime());
    
  }

  /**
   * end label string yyyy/mm/dd
   * @return the end label string yyyy/mm/dd
   */
  public String getImmediateMembershipEndDateLabel() {
    
    if (this.membershipContainer == null || this.membershipContainer.getImmediateMembership() == null || 
        this.membershipContainer.getImmediateMembership().getDisabledTime() == null) {
      return null;
    }
    
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
    
    return simpleDateFormat.format(this.membershipContainer.getImmediateMembership().getDisabledTime());
    
  }
  
  /**
   * @return enabled label
   */
  public String getImmediateMembershipEnabledLabel() {
    
    if (this.membershipContainer == null || this.membershipContainer.getImmediateMembership() == null) {
      return null;
    }
    
    boolean enabled = membershipContainer.getImmediateMembership().isEnabled();
    if (enabled) {
      return TextContainer.retrieveFromRequest().getText().get("groupFilterStatusEnabled");
    }
    
    return TextContainer.retrieveFromRequest().getText().get("groupFilterStatusDisabled");
  }
}
