/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.text.SimpleDateFormat;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * gui wrapper around membership
 * @author mchyzer
 *
 */
public class GuiMembership {

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GuiMembership)) {
      return false;
    }
    return new EqualsBuilder()
      .append( this.membership, ( (GuiMembership) other ).membership )
      .isEquals();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.membership )
      .toHashCode();
  }


  /**
   * start label string yyyy/MM/dd h:mm a
   * @return the start label string yyyy/MM/dd h:mm a
   */
  public String getStartDateLabel() {
        
    if (this.membership == null || this.membership.getEnabledTime() == null) {
      return null;
    }
    
    return GrouperUtil.timestampHoursMinutesLocalDateTime.format(this.membership.getEnabledTime());
    
  }

  /**
   * end label string yyyy/MM/dd h:mm a
   * @return the end label string yyyy/MM/dd h:mm a
   */
  public String getEndDateLabel() {
    
    
    if (this.membership == null || this.membership.getDisabledTime() == null) {
      return null;
    }
    
    return GrouperUtil.timestampHoursMinutesLocalDateTime.format(this.membership.getDisabledTime());
    
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
