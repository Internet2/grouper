/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_1;



/**
 * Result of one member changing its subject
 * 
 * @author mchyzer
 */
public class WsMemberChangeSubjectResult  {

  /** subject that was switched to */
  private WsSubject wsSubjectNew;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * subject that was switched from
   */
  private WsSubject wsSubjectOld;

  /**
   * subject that was added
   * @return the subjectId
   */
  public WsSubject getWsSubjectNew() {
    return this.wsSubjectNew;
  }

  /**
   * subject that was added
   * @param wsSubject1 the wsSubject1 to set
   */
  public void setWsSubjectNew(WsSubject wsSubject1) {
    this.wsSubjectNew = wsSubject1;
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

  /**
   * subject that was switched from
   * @return the subjectId
   */
  public WsSubject getWsSubjectOld() {
    return this.wsSubjectOld;
  }

  /**
   * subject that was switched from
   * @param wsSubject1 the wsSubject1 to set
   */
  public void setWsSubjectOld(WsSubject wsSubject1) {
    this.wsSubjectOld = wsSubject1;
  }

}
