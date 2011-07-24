/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;


/**
 * <pre>
 * Class to lookup a membership via web service.  Put in a uuid, or fill in the other fields
 * 
 * developers make sure each setter calls this.clearMembership();
 * </pre>
 * @author mchyzer
 */
public class WsMembershipLookup {

  /**
   * uuid of the membership to find
   */
  private String uuid;
  
  /**
   * uuid of the membership to find
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid of the membership to find
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * 
   */
  public WsMembershipLookup() {
    //blank
  }

}
