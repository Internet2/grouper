/**
 * 
 */
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * <pre>
 * Class to lookup a membership via web service.  Put in a uuid, or fill in the other fields
 * 
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

}
