package edu.internet2.middleware.grouper.ws.soap_v2_0;

import edu.internet2.middleware.grouper.ws.soap_v2_1.WsGroup;
import edu.internet2.middleware.grouper.ws.soap_v2_1.WsResponseMeta;
import edu.internet2.middleware.grouper.ws.soap_v2_1.WsResultMeta;
import edu.internet2.middleware.grouper.ws.soap_v2_1.WsSubject;


/**
 * <pre>
 * results for the get members lite call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * EXCEPTION: something bad happened
 * etc
 * </pre>
 * @author mchyzer
 */
public class WsGetMembersLiteResult {

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
   * group that we are checking 
   */
  private WsGroup wsGroup;

  /**
   * results for each assignment sent in
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
   * @return the wsGroup
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsSubject[] getWsSubjects() {
    return this.wsSubjects;
  }

  /**
   * @param wsGroup1 the wsGroup to set
   */
  public void setWsGroup(WsGroup wsGroup1) {
    this.wsGroup = wsGroup1;
  }

  /**
   * results for each assignment sent in
   * @param results1 the results to set
   */
  public void setWsSubjects(WsSubject[] results1) {
    this.wsSubjects = results1;
  }

  /**
   * empty
   */
  public WsGetMembersLiteResult() {
    //empty
  }

}
