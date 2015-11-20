package edu.internet2.middleware.grouperClient.ws.beans;

/**
 * holds result of assign action to attribute def request.
 * 
 * @author vsachdeva
 */
public class WsAttributeDefAssignActionResults implements WsResponseBean,
    ResultMetadataHolder {

  /** attribute def to which action(s) are assigned **/
  private WsAttributeDef wsAttributeDef;

  /** actions with operation performed **/
  private WsAttributeDefActionOperationPerformed[] actions;

  /**
   * attribute def to which action(s) are assigned
   * @return wsAttributeDef
   */
  public WsAttributeDef getWsAttributeDef() {
    return this.wsAttributeDef;
  }

  /**
   * attribute def to which action(s) are assigned 
   * @param wsAttributeDef1
   */
  public void setWsAttributeDef(WsAttributeDef wsAttributeDef1) {
    this.wsAttributeDef = wsAttributeDef1;
  }

  /**
   * actions with operation performed
   * @return actions
   */
  public WsAttributeDefActionOperationPerformed[] getActions() {
    return this.actions;
  }

  /**
   * actions with operation performed
   * @param actions1
   */
  public void setActions(WsAttributeDefActionOperationPerformed[] actions1) {
    this.actions = actions1;
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
   * @return the resultMetadata
  */
  @Override
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * @see edu.internet2.middleware.grouperClient.ws.beans.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  @Override
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
