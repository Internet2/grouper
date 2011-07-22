/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.soap;


/**
 * container to add in attribute assign disallowed
 */
public class WsAttributeAssignDisallowed {

  /** T of F for if this is disallowed.  Defaults to false, only available in 2.0+ */
  private String disallowed;

  
  /**
   * @return the disallowed
   */
  public String getDisallowed() {
    return this.disallowed;
  }

  
  /**
   * @param disallowed1 the disallowed to set
   */
  public void setDisallowed(String disallowed1) {
    this.disallowed = disallowed1;
  }
  
}
