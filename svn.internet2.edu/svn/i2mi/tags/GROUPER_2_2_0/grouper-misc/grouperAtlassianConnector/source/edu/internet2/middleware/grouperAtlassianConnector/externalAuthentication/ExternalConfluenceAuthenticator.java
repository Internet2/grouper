/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
