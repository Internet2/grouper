package edu.internet2.middleware.grouper.ws.soap_v2_0;


/**
 * <pre>
 * results for the get groups call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * SUBJECT_NOT_FOUND: cant find the subject
 * SUBJECT_DUPLICATE: found multiple groups
 * EXCEPTION
 * </pre>
 * @author mchyzer
 */
public class WsGetGroupsResult  {

  /**
   * results for each get groups sent in
   */
  private WsGroup[] wsGroups;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * subject that was added 
   */
  private WsSubject wsSubject;

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsGroup[] getWsGroups() {
    return this.wsGroups;
  }

  /**
   * results for each assignment sent in
   * @param results1 the results to set
   */
  public void setWsGroups(WsGroup[] results1) {
    this.wsGroups = results1;
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * subject that was added
   * @return the subjectId
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  /**
   * subject that was added
   * @param wsSubject1 the wsSubject1 to set
   */
  public void setWsSubject(WsSubject wsSubject1) {
    this.wsSubject = wsSubject1;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

}
