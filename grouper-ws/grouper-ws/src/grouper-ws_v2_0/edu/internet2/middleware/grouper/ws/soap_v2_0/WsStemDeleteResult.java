/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;

import edu.internet2.middleware.grouper.ws.soap_v2_1.WsResultMeta;
import edu.internet2.middleware.grouper.ws.soap_v2_1.WsStem;


/**
 * Result of one stem being deleted.  The number of
 * these result objects will equal the number of stems sent in to the method
 * to be deleted
 * 
 * @author mchyzer
 */
public class WsStemDeleteResult  {

  /**
   * empty constructor
   */
  public WsStemDeleteResult() {
    //empty I said
  }

  /** stem data */
  private WsStem wsStem;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * @return the wsStem
   */
  public WsStem getWsStem() {
    return this.wsStem;
  }

  /**
   * @param wsStem1 the wsStem to set
   */
  public void setWsStem(WsStem wsStem1) {
    this.wsStem = wsStem1;
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
