/*
 * @author mchyzer
 * $Id: WsSampleRestType.java,v 1.1 2008-03-25 05:15:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.samples.types;

import edu.internet2.middleware.grouper.ws.rest.contentType.WsRestRequestContentType;
import edu.internet2.middleware.grouper.ws.rest.contentType.WsRestResponseContentType;


/**
 * type of rest request
 */
public enum WsSampleRestType {

  /** xhtml request */
  xhtml(WsRestRequestContentType.xhtml, WsRestResponseContentType.xhtml),
  
  /** json request */
  json(WsRestRequestContentType.json, WsRestResponseContentType.json),
  
  /** http param request with xhtml response */
  http_xhtml(WsRestRequestContentType.http, WsRestResponseContentType.xhtml),
  
  /** xml request */
  xml(WsRestRequestContentType.xml, WsRestResponseContentType.xml);
  
  /**
   * construct
   * @param wsLiteRequestContentType1
   * @param wsLiteResponseContentType1
   */
  private WsSampleRestType(WsRestRequestContentType wsLiteRequestContentType1,
      WsRestResponseContentType wsLiteResponseContentType1) {
    this.wsLiteRequestContentType = wsLiteRequestContentType1;
    this.wsLiteResponseContentType = wsLiteResponseContentType1;
  }
  
  /** request content type */
  private WsRestRequestContentType wsLiteRequestContentType;
  
  /** response content type */
  private WsRestResponseContentType wsLiteResponseContentType;

  
  /**
   * @return the wsLiteRequestContentType
   */
  public WsRestRequestContentType getWsLiteRequestContentType() {
    return this.wsLiteRequestContentType;
  }

  
  /**
   * @return the wsLiteResponseContentType
   */
  public WsRestResponseContentType getWsLiteResponseContentType() {
    return this.wsLiteResponseContentType;
  }
}
