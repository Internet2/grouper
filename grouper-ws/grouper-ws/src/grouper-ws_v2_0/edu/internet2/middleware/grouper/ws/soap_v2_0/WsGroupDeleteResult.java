/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;

import edu.internet2.middleware.grouper.ws.soap_v2_1.WsGroup;
import edu.internet2.middleware.grouper.ws.soap_v2_1.WsResultMeta;


/**
 * Result of one group being deleted.  The number of
 * these result objects will equal the number of groups sent in to the method
 * to be deleted
 * 
 * @author mchyzer
 */
public class WsGroupDeleteResult  {

  /**
   * empty constructor
   */
  public WsGroupDeleteResult() {
    //nothing to do
  }

  /**
   * group to be deleted
   */
  private WsGroup wsGroup;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * @return the wsGroup
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * @param wsGroupResult1 the wsGroup to set
   */
  public void setWsGroup(WsGroup wsGroupResult1) {
    this.wsGroup = wsGroupResult1;
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
