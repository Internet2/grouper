/*
 * @author mchyzer $Id: WsRequestBean.java,v 1.2 2008-03-26 07:39:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest;

import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

/**
 * Bean (used in Rest) as a request for web service
 */
public interface WsRequestBean {
  
  /** 
   * see which http method this is supposed to be associated with, and
   * override whatever was passed in
   * @return the method
   */
  public GrouperRestHttpMethod retrieveRestHttpMethod();    
}
