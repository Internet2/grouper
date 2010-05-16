/*
 * @author mchyzer $Id: WsResultCode.java,v 1.2 2008-12-04 07:51:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws;

import edu.internet2.middleware.grouper.misc.GrouperVersion;

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
  
  /** get the name label for a certain version of client 
   * @param clientVersion 
   * @return */
  public String nameForVersion(GrouperVersion clientVersion);
  
}
