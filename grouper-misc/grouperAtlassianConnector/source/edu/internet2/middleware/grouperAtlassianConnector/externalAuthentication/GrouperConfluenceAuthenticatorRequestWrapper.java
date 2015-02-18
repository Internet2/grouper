/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.externalAuthentication;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.atlassian.seraph.RequestParameterConstants;


/**
 * wrapper to put user/pass (not real pass!) in request params since confluence expects it
 */
public class GrouperConfluenceAuthenticatorRequestWrapper extends HttpServletRequestWrapper {
  
  
  
  /**
   * @param request
   */
  public GrouperConfluenceAuthenticatorRequestWrapper(HttpServletRequest request) {
    super(request);
  }

  /**
   * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
   */
  @Override
  public String getParameter(String name) {
    
    //for some reason the authenticator is asking for user/pass, so we need to pretend like it is there
    if (RequestParameterConstants.OS_COOKIE.equals(name)
        || RequestParameterConstants.OS_USERNAME.equals(name)
        || RequestParameterConstants.OS_PASSWORD.equals(name)
        ) {

      Principal loggedInUser =  ConfluenceExternalAuthenticator.loggedInUser((HttpServletRequest)this.getRequest());
      
      if (loggedInUser != null && loggedInUser.getName() != null && !"".equals(loggedInUser.getName().trim())) {
        if (RequestParameterConstants.OS_COOKIE.equals(name)) {
          return "true";
        }
        return loggedInUser.getName();
      }
    }
            
    //delegate to wrapped request
    return this.getRequest().getParameter(name);
  }
  

}
