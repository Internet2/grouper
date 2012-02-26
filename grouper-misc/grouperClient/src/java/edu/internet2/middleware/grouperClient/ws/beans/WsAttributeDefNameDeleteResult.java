/**
 * 
 */
package edu.internet2.middleware.grouperClient.ws.beans;



/**
 * Result of one attribute def name being deleted.  The number of
 * these result objects will equal the number of attribute def names sent in to the method
 * to be deleted
 * 
 * @author mchyzer
 */
public class WsAttributeDefNameDeleteResult implements ResultMetadataHolder {

  /**
   * empty constructor
   */
  public WsAttributeDefNameDeleteResult() {
    //nothing to do
  }

  /**
   * attribute def name to be deleted
   */
  private WsAttributeDefName wsAttributeDefName;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * @return the wsAttributeDefName
   */
  public WsAttributeDefName getWsAttributeDefName() {
    return this.wsAttributeDefName;
  }

  /**
   * @param wsAttributeDefNameResult1 the wsAttributeDefName to set
   */
  public void setWsAttributeDefName(WsAttributeDefName wsAttributeDefNameResult1) {
    this.wsAttributeDefName = wsAttributeDefNameResult1;
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

}
