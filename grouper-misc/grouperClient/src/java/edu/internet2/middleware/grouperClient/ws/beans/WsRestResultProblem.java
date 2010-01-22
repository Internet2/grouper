/*
 * @author mchyzer $Id: WsRestResultProblem.java,v 1.1 2008-11-27 14:25:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws.beans;

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
   * @see WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

}
