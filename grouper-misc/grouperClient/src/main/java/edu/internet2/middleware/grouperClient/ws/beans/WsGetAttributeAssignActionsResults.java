package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * returned from the attribute assign actions query 
 * @author vsachdeva
 */
public class WsGetAttributeAssignActionsResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * has 0 to many tuples that match the query
   */
  private WsAttributeAssignActionTuple[] wsAttributeAssignActionTuples;
	 
  /**
   * has 0 to many attribute defs related to the names/ids/idIndices that match the query
   */
  private WsAttributeDef[] wsAttributeDefs;

  /**
   * @return assign action tuples
   */
  public WsAttributeAssignActionTuple[] getWsAttributeAssignActionTuples() {
	return this.wsAttributeAssignActionTuples;
  }

  /**
   * has 0 to many tuples that match the query
   * @param wsAttributeAssignActionTuples1
   */
  public void setWsAttributeAssignActionTuples(WsAttributeAssignActionTuple[] wsAttributeAssignActionTuples1) {
	this.wsAttributeAssignActionTuples = wsAttributeAssignActionTuples1;
  }
	
  /**
   * @return 0 to many attribute definitions
   */
  public WsAttributeDef[] getWsAttributeDefs() {
	return this.wsAttributeDefs;
  }

  /**
   * has 0 to many attribute defs related to the names/ids/idIndices that match the query
   * @param wsAttributeDefs1
   */
  public void setWsAttributeDefs(WsAttributeDef[] wsAttributeDefs1) {
	this.wsAttributeDefs = wsAttributeDefs1;
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * @return the resultMetadata
   */
  @Override
  public WsResultMeta getResultMetadata() {
	return this.resultMetadata;
  }

  /**
   * metadata about the result
  */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
  * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
  * @return the response metadata
  */
  @Override
  public WsResponseMeta getResponseMetadata() {
	return this.responseMetadata;
  }

  /**
   * @param resultMetadata1
   *            the resultMetadata to set
  */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
	this.resultMetadata = resultMetadata1;
  }

  /**
   * @param responseMetadata1
   *            the responseMetadata to set
  */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
	this.responseMetadata = responseMetadata1;
  }

}
