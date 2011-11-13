/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_1;



/**
 * <pre>
 * Class to lookup a membership via web service.  Put in a uuid, or fill in the other fields
 * 
 * developers make sure each setter calls this.clearMembership();
 * </pre>
 * @author mchyzer
 */
public class WsMembershipAnyLookup {

  /** group lookup for group */
  private WsGroupLookup wsGroupLookup;
  
  /** subject lookup for subject */
  private WsSubjectLookup wsSubjectLookup;
  
  /**
   * group lookup for group
   * @return group lookup
   */
  public WsGroupLookup getWsGroupLookup() {
    return this.wsGroupLookup;
  }

  /**
   * group lookup for group
   * @param wsGroupLookup1
   */
  public void setWsGroupLookup(WsGroupLookup wsGroupLookup1) {
    this.wsGroupLookup = wsGroupLookup1;
  }

  /**
   * subject lookup for subject
   * @return subject lookup
   */
  public WsSubjectLookup getWsSubjectLookup() {
    return this.wsSubjectLookup;
  }

  /**
   * subject lookup for subject
   * @param wsSubjectLookup1
   */
  public void setWsSubjectLookup(WsSubjectLookup wsSubjectLookup1) {
    this.wsSubjectLookup = wsSubjectLookup1;
  }

  /**
   * 
   */
  public WsMembershipAnyLookup() {
    //blank
  }

}
