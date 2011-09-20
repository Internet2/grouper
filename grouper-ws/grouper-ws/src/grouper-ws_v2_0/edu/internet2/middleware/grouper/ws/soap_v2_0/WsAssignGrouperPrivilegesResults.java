/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;

import edu.internet2.middleware.grouper.ws.soap_v2_1.WsAssignGrouperPrivilegesResult;
import edu.internet2.middleware.grouper.ws.soap_v2_1.WsGroup;
import edu.internet2.middleware.grouper.ws.soap_v2_1.WsResponseMeta;
import edu.internet2.middleware.grouper.ws.soap_v2_1.WsResultMeta;
import edu.internet2.middleware.grouper.ws.soap_v2_1.WsStem;


/**
 * Result of assigning or removing a privilege
 * 
 * @author mchyzer
 */
public class WsAssignGrouperPrivilegesResults {

  /**
   * empty
   */
  public WsAssignGrouperPrivilegesResults() {
    //empty
  }

  /**
    * metadata about the result
    */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

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
  private WsResponseMeta responseMetadata = new WsResponseMeta();

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
   * group querying 
   */
  private WsGroup wsGroup;

  /**
   * stem querying 
   */
  private WsStem wsStem;

  /**
   * results for each assignment sent in
   */
  private WsAssignGrouperPrivilegesResult[] results;

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsAssignGrouperPrivilegesResult[] getResults() {
    return this.results;
  }
  
  /**
   * results for each assignment sent in
   * @param results1
   */
  public void setResults(WsAssignGrouperPrivilegesResult[] results1) {
    this.results = results1;
  }

  /**
   * group querying
   * @return the group
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * stem querying
   * @return the stem
   */
  public WsStem getWsStem() {
    return this.wsStem;
  }

  /**
   * group querying
   * @param wsGroup1
   */
  public void setWsGroup(WsGroup wsGroup1) {
    this.wsGroup = wsGroup1;
  }

  /**
   * stem querying
   * @param wsStem1
   */
  public void setWsStem(WsStem wsStem1) {
    this.wsStem = wsStem1;
  }
}
