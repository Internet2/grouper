package edu.internet2.middleware.grouper.ws.soap_v2_1;



/**
 * <pre>
 * results for the AttributeDefNames save call.
 * 
 * result code:
 * code of the result for this AttributeDefName overall
 * SUCCESS: means everything ok
 * ATTRIBUTE_DEF_NAME_NOT_FOUND: cant find the AttributeDefName
 * ATTRIBUTE_DEF_NAME_DUPLICATE: found multiple AttributeDefNames
 * </pre>
 * @author mchyzer
 */
public class WsAttributeDefNameSaveLiteResult {

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * AttributeDefName saved 
   */
  private WsAttributeDefName wsAttributeDefName;

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

  /**
   * @return the wsAttributeDefName
   */
  public WsAttributeDefName getWsAttributeDefName() {
    return this.wsAttributeDefName;
  }

  /**
   * @param wsAttributeDefName1 the wsAttributeDefName to set
   */
  public void setWsAttributeDefName(WsAttributeDefName wsAttributeDefName1) {
    this.wsAttributeDefName = wsAttributeDefName1;
  }

  /**
   * empty
   */
  public WsAttributeDefNameSaveLiteResult() {
    //empty
  }

}
