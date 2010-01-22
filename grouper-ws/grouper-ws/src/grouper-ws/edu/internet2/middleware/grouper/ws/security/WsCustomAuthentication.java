/*
 * @author mchyzer
 * $Id: WsCustomAuthentication.java,v 1.1 2008-04-01 08:38:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.security;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;


/**
 * <pre>
 * implement this interface and provide the class to the classpath and grouper-ws.properties
 * to override the default of httpServletRequest.getUserPrincipal();
 * for non-Rampart authentication
 * 
 * if user is not found, throw a runtime exception.  Could be WsInvalidQueryException
 * which is a type of runtime exception (experiment and see what you want the response to 
 * look like)
 * 
 * </pre>
 */
public interface WsCustomAuthentication {
  
  /**
   * retrieve the current username (subjectId) from the request object.
   * @param httpServletRequest
   * @return the logged in username (subjectId)
   * @throws WsInvalidQueryException if there is a problem
   */
  public String retrieveLoggedInSubjectId(HttpServletRequest httpServletRequest)
    throws WsInvalidQueryException;
  
}
