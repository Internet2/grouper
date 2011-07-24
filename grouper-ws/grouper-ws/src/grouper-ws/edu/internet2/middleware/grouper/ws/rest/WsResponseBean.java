/*
 * @author mchyzer $Id: WsResponseBean.java,v 1.1 2008-03-25 05:15:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest;

import edu.internet2.middleware.grouper.ws.coresoap.WsResponseMeta;
import edu.internet2.middleware.grouper.ws.coresoap.WsResultMeta;

/**
 * bean (used in Rest) which is a response from web service
 */
public interface WsResponseBean {

  /** get the result metadata 
   * @return the result metadata 
   */
  public WsResultMeta getResultMetadata();

  /** 
   * get the response metadata 
   * @return the response metadata 
   */
  public WsResponseMeta getResponseMetadata();
}
