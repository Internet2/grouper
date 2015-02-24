/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.externalAuthentication;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.atlassian.confluence.event.events.security.LoginEvent;
import com.atlassian.confluence.event.events.security.LoginFailedEvent;
import com.atlassian.confluence.user.ConfluenceAuthenticator;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.auth.LoginReason;

/**
 * an example is here
 * https://github.com/Jasig/java-cas-client/blob/master/cas-client-integration-atlassian/src/main/java/org/jasig/cas/client/integration/atlassian/Confluence35CasAuthenticator.java
 * 
 */
public class ConfluenceExternalAuthenticator extends ConfluenceAuthenticator {

  
  public Principal getUser(final HttpServletRequest request, final HttpServletResponse response) {
    Principal existingUser = getUserFromSession(request);
    if (existingUser != null) {
        LoginReason.OK.stampRequestResponse(request, response);
        return existingUser;
    }

    final HttpSession session = request.getSession();

    Principal principal = loggedInUser(request);
    
    if (principal != null && principal.getName() != null && !"".equals(principal.getName().trim())) {
      
      
      final String username = principal.getName();
      final Principal user = getUser(username);
      final String remoteIP = request.getRemoteAddr();
      final String remoteHost = request.getRemoteHost();
  
      if (user != null) {
          putPrincipalInSessionContext(request, user);
          getElevatedSecurityGuard().onSuccessfulLoginAttempt(request, username);
          // Firing this event is necessary to ensure the user's personal information is initialised correctly.
          getEventPublisher().publish(
                  new LoginEvent(this, username, request.getSession().getId(), remoteHost, remoteIP));
          LoginReason.OK.stampRequestResponse(request, response);
      } else {
          getElevatedSecurityGuard().onFailedLoginAttempt(request, username);
          getEventPublisher().publish(
                  new LoginFailedEvent(this, username, request.getSession().getId(), remoteHost, remoteIP));
      }
      return user;
    }
    
    return super.getUser(request, response);
}


  /**
   * @param request
   * @return principal
   */
  public static Principal loggedInUser(HttpServletRequest request) {
    ExternalAuthenticator externalAuthenticator = new ExternalAuthenticator();

    return ExternalAuthenticator.getUser(externalAuthenticator, request, 
        "confluence", DefaultAuthenticator.LOGGED_IN_KEY, null);
  }

}
