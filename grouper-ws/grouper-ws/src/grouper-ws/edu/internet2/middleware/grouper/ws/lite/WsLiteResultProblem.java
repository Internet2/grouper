/*
 * @author mchyzer $Id: WsLiteResultProblem.java,v 1.1 2008-03-24 20:19:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.lite;

import edu.internet2.middleware.grouper.ws.soap.WsResponseMeta;
import edu.internet2.middleware.grouper.ws.soap.WsResultMeta;

/**
 *
 */
public class WsLiteResultProblem implements WsResponseBean {

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
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * @see edu.internet2.middleware.grouper.ws.lite.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

}
