/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_1;



/**
 * Result of one AttributeDefName being saved.  The number of
 * these result objects will equal the number of AttributeDefNames sent in to the method
 * to be saved
 * 
 * @author mchyzer
 */
public class WsAttributeDefNameSaveResult  {

  /** group saved */
  private WsAttributeDefName wsAttributeDefName;
  
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
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  /**
   * empty
   */
  public WsAttributeDefNameSaveResult() {
    //empty
  }
}
