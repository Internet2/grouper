/*
 * @author mchyzer $Id: WsResultCode.java,v 1.1 2008-11-27 14:25:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws.beans;

/**
 * result code enum
 */
public interface WsResultCode {

  /** is this code a success 
   * @return true or false for success or not
   */
  public boolean isSuccess();

  /** get the http status code associated with this status code, e.g. 200 
   * @return the status code e.g. 200 
   */
  public int getHttpStatusCode();

  /** 
   * name of the result code enum
   * @return the name
   */
  public String name();
}
