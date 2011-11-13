package edu.internet2.middleware.grouper.ws.soap_v2_1;



/**
 * <pre>
 * results for assigning permissions call.
 * 
 * result code:
 * code of the result for this attribute assignment overall
 * SUCCESS: means everything ok
 * INSUFFICIENT_PRIVILEGES: problem with some input where privileges are not sufficient
 * INVALID_QUERY: bad inputs
 * EXCEPTION: something bad happened
 * </pre>
 * @author mchyzer
 */
public class WsAssignPermissionsResults {

  /**
   * construct
   */
  public WsAssignPermissionsResults() {
    //empty
  }

  /**
   * attribute def references in the assignments or inputs (and able to be read)
   */
  private WsAttributeDef[] wsAttributeDefs;
  
  /**
   * attribute def references in the assignments or inputs (and able to be read)
   * @return attribute defs
   */
  public WsAttributeDef[] getWsAttributeDefs() {
    return this.wsAttributeDefs;
  }

  /**
   * attribute def references in the assignments or inputs (and able to be read)
   * @param wsAttributeDefs1
   */
  public void setWsAttributeDefs(WsAttributeDef[] wsAttributeDefs1) {
    this.wsAttributeDefs = wsAttributeDefs1;
  }

  /**
   * attribute def names referenced in the assignments or inputs (and able to read)
   */
  private WsAttributeDefName[] wsAttributeDefNames;
  
  /**
   * attribute def names referenced in the assignments or inputs (and able to read)
   * @return attribute def names
   */
  public WsAttributeDefName[] getWsAttributeDefNames() {
    return this.wsAttributeDefNames;
  }

  /**
   * attribute def names referenced in the assignments or inputs (and able to read)
   * @param wsAttributeDefNames1
   */
  public void setWsAttributeDefNames(WsAttributeDefName[] wsAttributeDefNames1) {
    this.wsAttributeDefNames = wsAttributeDefNames1;
  }

  /**
   * the assignment results being queried
   */
  private WsAssignPermissionResult[] wsAssignPermissionResults;
  
  /**
   * the assignment results being queried
   * @return the assignments being queried
   */
  public WsAssignPermissionResult[] getWsAssignPermissionResults() {
    return this.wsAssignPermissionResults;
  }

  /**
   * the assignment results being queried
   * @param wsPermissionAssignResults1
   */
  public void setWsAssignPermissionResults(WsAssignPermissionResult[] wsPermissionAssignResults1) {
    this.wsAssignPermissionResults = wsPermissionAssignResults1;
  }

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * attributes of subjects returned, in same order as the data
   * @return the attributeNames
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @param attributeNamesa the attributeNames to set
   */
  public void setSubjectAttributeNames(String[] attributeNamesa) {
    this.subjectAttributeNames = attributeNamesa;
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * groups that are in the results
   */
  private WsGroup[] wsGroups;

  /**
   * subjects that are in the results
   */
  private WsSubject[] wsSubjects;

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  
  /**
   * @param responseMetadata1 the responseMetadata to set
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }

  /**
   * @return the wsGroups
   */
  public WsGroup[] getWsGroups() {
    return this.wsGroups;
  }

  /**
   * subjects that are in the results
   * @return the subjects
   */
  public WsSubject[] getWsSubjects() {
    return this.wsSubjects;
  }

  /**
   * @param wsGroup1 the wsGroups to set
   */
  public void setWsGroups(WsGroup[] wsGroup1) {
    this.wsGroups = wsGroup1;
  }

  /**
   * subjects that are in the results
   * @param wsSubjects1
   */
  public void setWsSubjects(WsSubject[] wsSubjects1) {
    this.wsSubjects = wsSubjects1;
  }
}
