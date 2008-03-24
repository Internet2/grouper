/*
 * @author mchyzer $Id: WsResponseBean.java,v 1.1 2008-03-24 20:19:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.lite;

import edu.internet2.middleware.grouper.ws.soap.WsResponseMeta;
import edu.internet2.middleware.grouper.ws.soap.WsResultMeta;

/**
 * bean (used in Lite) which is a response from web service
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
