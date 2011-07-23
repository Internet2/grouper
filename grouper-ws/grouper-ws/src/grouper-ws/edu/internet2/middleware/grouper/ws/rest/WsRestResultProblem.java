/*
 * @author mchyzer $Id: WsRestResultProblem.java,v 1.3 2008-03-30 09:01:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest;

import edu.internet2.middleware.grouper.ws.soap_v2_0.WsResponseMeta;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsResultMeta;

/**
 *
 */
public class WsRestResultProblem implements WsResponseBean {

  /**
   * empty
   */
  public WsRestResultProblem() {
    //empty
  }
  
  /**
   * construct with result
   * @param wsResultMeta1
   */
  public WsRestResultProblem(WsResultMeta wsResultMeta1) {
    this.resultMetadata = wsResultMeta1;
  }
  
  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  
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
   * @return the resultMetadata
   */
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
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

}
