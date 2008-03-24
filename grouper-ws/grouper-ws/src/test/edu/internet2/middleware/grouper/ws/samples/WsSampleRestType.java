/*
 * @author mchyzer
 * $Id: WsSampleRestType.java,v 1.1 2008-03-24 20:19:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.samples;

import edu.internet2.middleware.grouper.ws.lite.contentType.WsLiteRequestContentType;
import edu.internet2.middleware.grouper.ws.lite.contentType.WsLiteResponseContentType;


/**
 * type of rest request
 */
public enum WsSampleRestType {

  /** xhtml request */
  xhtml(WsLiteRequestContentType.xhtml, WsLiteResponseContentType.xhtml),
  
  /** json request */
  json(WsLiteRequestContentType.json, WsLiteResponseContentType.json),
  
  /** http param request with xhtml response */
  http_xhtml(WsLiteRequestContentType.http, WsLiteResponseContentType.xhtml),
  
  /** xml request */
  xml(WsLiteRequestContentType.xml, WsLiteResponseContentType.xml);
  
  /**
   * construct
   * @param wsLiteRequestContentType1
   * @param wsLiteResponseContentType1
   */
  private WsSampleRestType(WsLiteRequestContentType wsLiteRequestContentType1,
      WsLiteResponseContentType wsLiteResponseContentType1) {
    this.wsLiteRequestContentType = wsLiteRequestContentType1;
    this.wsLiteResponseContentType = wsLiteResponseContentType1;
  }
  
  /** request content type */
  private WsLiteRequestContentType wsLiteRequestContentType;
  
  /** response content type */
  private WsLiteResponseContentType wsLiteResponseContentType;

  
  /**
   * @return the wsLiteRequestContentType
   */
  public WsLiteRequestContentType getWsLiteRequestContentType() {
    return this.wsLiteRequestContentType;
  }

  
  /**
   * @return the wsLiteResponseContentType
   */
  public WsLiteResponseContentType getWsLiteResponseContentType() {
    return this.wsLiteResponseContentType;
  }
}
