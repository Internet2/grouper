/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_1;



/**
 * Result of seeing if one subject is a member of a group.  The number of
 * results will equal the number of subjects sent in to the method
 * 
 * @author mchyzer
 */
public class WsHasMemberResult {

  /** sujbect info for hasMember */
  private WsSubject wsSubject;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * @return the wsSubject
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  /**
   * @param wsSubjectResult1 the wsSubject to set
   */
  public void setWsSubject(WsSubject wsSubjectResult1) {
    this.wsSubject = wsSubjectResult1;
  }

  /** empty constructor */
  public WsHasMemberResult() {
    //nothing
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

}
