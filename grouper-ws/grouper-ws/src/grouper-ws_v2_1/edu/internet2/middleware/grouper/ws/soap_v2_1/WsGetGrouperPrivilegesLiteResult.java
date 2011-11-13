/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_1;



/**
 * Result of retrieving privileges for a user/group combo (and perhaps 
 * filtered by type), will
 * return a list of permissions
 * 
 * @author mchyzer
 */
public class WsGetGrouperPrivilegesLiteResult {

  /**
   * empty
   */
  public WsGetGrouperPrivilegesLiteResult() {
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
   * Privileges for this query
   */
  private WsGrouperPrivilegeResult[] privilegeResults;
  
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
   * field 
   */
  private WsParam[] params;


  /**
   * @return the params
   */
  public WsParam[] getParams() {
    return this.params;
  }

  /**
   * @param params1 the params to set
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

  /**
   * Privileges for this query
   * @return the privileges
   */
  public WsGrouperPrivilegeResult[] getPrivilegeResults() {
    return this.privilegeResults;
  }

  /**
   * Privileges for this query
   * @param privilegeResults1
   */
  public void setPrivilegeResults(WsGrouperPrivilegeResult[] privilegeResults1) {
    this.privilegeResults = privilegeResults1;
  }
}
