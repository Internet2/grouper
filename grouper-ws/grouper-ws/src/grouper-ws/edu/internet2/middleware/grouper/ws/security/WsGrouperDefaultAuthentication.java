/*
 * @author mchyzer
 * $Id: WsGrouperDefaultAuthentication.java,v 1.1 2008-04-01 08:38:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.security;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;


/**
 * default authentication for grouper if a custom one isnt specified in
 * grouper-ws.properties for non-rampart requests
 */
public class WsGrouperDefaultAuthentication implements WsCustomAuthentication {

  /**
   * 
   * @see edu.internet2.middleware.grouper.ws.security.WsCustomAuthentication#retrieveLoggedInSubjectId(javax.servlet.http.HttpServletRequest)
   */
  public String retrieveLoggedInSubjectId(HttpServletRequest httpServletRequest)
      throws RuntimeException {
    
    // use this to be the user connected, or the user act-as
    String userIdLoggedIn = GrouperServiceJ2ee.retrieveUserPrincipalNameFromRequest();

    return userIdLoggedIn;
  }

}
