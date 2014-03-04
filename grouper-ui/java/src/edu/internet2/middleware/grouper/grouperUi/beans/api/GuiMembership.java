/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.text.SimpleDateFormat;

import edu.internet2.middleware.grouper.Membership;


/**
 * gui wrapper around membership
 * @author mchyzer
 *
 */
public class GuiMembership {

  /**
   * start label string yyyy/mm/dd
   * @return the start label string yyyy/mm/dd
   */
  public String getStartDateLabel() {
        
    if (this.membership == null || this.membership.getEnabledTime() == null) {
      return null;
    }
    
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
    
    return simpleDateFormat.format(this.membership.getEnabledTime());
    
  }

  /**
   * end label string yyyy/mm/dd
   * @return the end label string yyyy/mm/dd
   */
  public String getEndDateLabel() {
    
    
    if (this.membership == null || this.membership.getDisabledTime() == null) {
      return null;
    }
    
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
    
    return simpleDateFormat.format(this.membership.getDisabledTime());
    
  }


  /**
   * default constructor
   */
  public GuiMembership() {
    
  }
  
  /**
   * 
   * @param theMembership
   */
  public GuiMembership(Membership theMembership) {
    this.membership = theMembership;
  }
  
  /**
   * membership
   */
  private Membership membership;

  /**
   * membership
   * @return membership
   */
  public Membership getMembership() {
    return this.membership;
  }

  /**
   * membership
   * @param membership1
   */
  public void setMembership(Membership membership1) {
    this.membership = membership1;
  }
  
  
  
}
