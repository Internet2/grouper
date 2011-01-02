/*
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.externalAuthentication;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.confluence.user.ConfluenceAuthenticator;

 /**
  * custom authenticator for jira
  */
@SuppressWarnings("serial")
public class ExternalConfluenceAuthenticator extends ConfluenceAuthenticator implements AtlassianGetUserable {

  /**
   * @see com.atlassian.seraph.auth.DefaultAuthenticator#getUser(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  public Principal getUser(HttpServletRequest request, HttpServletResponse response) {

    return ExternalAuthenticator.getUser(this, request, response, "confluence",
        "seraph_defaultauthenticator_user", "seraph_defaultauthenticator_logged_out_user");
  }
  
  /**
   * @see AtlassianGetUserable#getUser(String)
   */
  public Principal getUser(String username) {
    return super.getUser(username);
  }

}
