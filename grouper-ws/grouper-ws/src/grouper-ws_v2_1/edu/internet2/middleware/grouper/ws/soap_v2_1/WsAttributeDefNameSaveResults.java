package edu.internet2.middleware.grouper.ws.soap_v2_1;



/**
 * <pre>
 * results for the attribute def names save call.
 * 
 * result code:
 * code of the result for this attribute def name overall
 * SUCCESS: means everything ok
 * ATTRIBUTE_DEF_NAME_NOT_FOUND: cant find the attribute def name
 * ATTRIBUTE_DEF_NAME_DUPLICATE: found multiple attribute def names
 * </pre>
 * @author mchyzer
 */
public class WsAttributeDefNameSaveResults {

  /**
   * results for each attribute def name sent in
   */
  private WsAttributeDefNameSaveResult[] results;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * results for each attribute def name sent in
   * @return the results
   */
  public WsAttributeDefNameSaveResult[] getResults() {
    return this.results;
  }

  /**
   * results for each attribute def name sent in
   * @param results1 the results to set
   */
  public void setResults(WsAttributeDefNameSaveResult[] results1) {
    this.results = results1;
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  /**
   * @param responseMetadata1 the responseMetadata to set
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

}
