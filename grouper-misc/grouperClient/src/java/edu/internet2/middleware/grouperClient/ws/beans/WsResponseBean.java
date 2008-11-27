/*
 * @author mchyzer $Id: WsResponseBean.java,v 1.1 2008-11-27 14:25:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws.beans;

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
