package edu.internet2.middleware.grouperClient.ws.beans;



/**
 * returned from the attribute def name find query
 * 
 * @author mchyzer
 * 
 */
public class WsFindAttributeDefNamesResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * has 0 to many attribute def names that match the query
   */
  private WsAttributeDefName[] attributeDefNameResults;

  /**
   * has 0 to many attribute defs related to the names that match the query
   */
  private WsAttributeDef[] attributeDefs;

  /**
   * has 0 to many attribute defs related to the names that match the query
   * @return attribute defs
   */
  public WsAttributeDef[] getAttributeDefs() {
    return this.attributeDefs;
  }

  /**
   * has 0 to many attribute defs related to the names that match the query
   * @param attributeDefs1
   */
  public void setAttributeDefs(WsAttributeDef[] attributeDefs1) {
    this.attributeDefs = attributeDefs1;
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * has 0 to many attribute def names that match the query by example
   * 
   * @return the attribute def name Results
   */
  public WsAttributeDefName[] getAttributeDefNameResults() {
    return this.attributeDefNameResults;
  }

  /**
   * basic results to the query
   * @param attributeDefNameResults1 the groupResults to set
   */
  public void setAttributeDefNameResults(WsAttributeDefName[] attributeDefNameResults1) {
    this.attributeDefNameResults = attributeDefNameResults1;
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
