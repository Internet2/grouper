/*******************************************************************************
 * Copyright 2024 Internet2
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
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthStore;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthCode;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthClient;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthPendingRequest;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.OAuthContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Service logic for OAuth 2.1 authorization consent flow.
 * Handles user consent for OAuth client authorization requests.
 *
 * @author mchyzer
 */
public class UiV2OAuth extends UiServiceLogicBase {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UiV2OAuth.class);

  /**
   * OAuth 2.1 authorization endpoint. Called directly by MCP clients with
   * standard OAuth parameters. Validates the request, creates a pending
   * request in the DB, and shows the consent page.
   * <p>
   * The user is already authenticated via the Grouper UI auth mechanism
   * (Shibboleth, CAS, etc.) before this method is reached.
   * </p>
   * @param request
   * @param response
   */
  public void authorize(HttpServletRequest request, HttpServletResponse response) {

    if (!isMcpEnabled()) {
      throw new RuntimeException("MCP is not enabled (grouper.is.mcp is false)");
    }

    GrouperRequestContainer grouperRequestContainer =
        GrouperRequestContainer.retrieveFromRequestOrCreate();

    OAuthContainer oAuthContainer = grouperRequestContainer.getOauthContainer();

    try {

      String clientId = request.getParameter("client_id");
      String redirectUri = request.getParameter("redirect_uri");
      String responseType = request.getParameter("response_type");
      String codeChallenge = request.getParameter("code_challenge");
      String codeChallengeMethod = request.getParameter("code_challenge_method");
      String state = request.getParameter("state");
      String scope = request.getParameter("scope");

      // validate required parameters
      if (StringUtils.isBlank(clientId)) {
        sendAuthorizeError(response, redirectUri, state, "invalid_request",
            "client_id is required", oAuthContainer);
        throw new ControllerDone();
      }

      if (StringUtils.isBlank(redirectUri)) {
        // cannot redirect if no redirect_uri; show error page
        oAuthContainer.setErrorMessage("redirect_uri is required");
        showJsp("/WEB-INF/grouperUi2/oauth/oauthAuthorize.jsp");
        throw new ControllerDone();
      }

      if (!"code".equals(responseType)) {
        sendAuthorizeError(response, redirectUri, state, "unsupported_response_type",
            "Only response_type=code is supported", oAuthContainer);
        throw new ControllerDone();
      }

      if (StringUtils.isBlank(codeChallenge)) {
        sendAuthorizeError(response, redirectUri, state, "invalid_request",
            "code_challenge is required (PKCE)", oAuthContainer);
        throw new ControllerDone();
      }

      if (StringUtils.isBlank(codeChallengeMethod) || !"S256".equals(codeChallengeMethod)) {
        sendAuthorizeError(response, redirectUri, state, "invalid_request",
            "code_challenge_method must be S256", oAuthContainer);
        throw new ControllerDone();
      }

      // validate client registration
      GrouperOAuthClient client = GrouperOAuthStore.retrieveClient(clientId);
      if (client == null) {
        sendAuthorizeError(response, redirectUri, state, "invalid_request",
            "Unknown client_id", oAuthContainer);
        throw new ControllerDone();
      }

      // validate redirect_uri against registered URIs
      if (!client.getRedirectUris().contains(redirectUri)) {
        // do not redirect to an unregistered URI; show error page
        oAuthContainer.setErrorMessage("redirect_uri not registered for this client");
        showJsp("/WEB-INF/grouperUi2/oauth/oauthAuthorize.jsp");
        throw new ControllerDone();
      }

      // validate redirect_uri against configured regex patterns
      {
        String redirectUriRegexError = GrouperOAuthStore.validateRedirectUriAllowed(redirectUri);
        if (redirectUriRegexError != null) {
          // do not redirect to a disallowed URI; show error page
          oAuthContainer.setErrorMessage("redirect_uri not allowed by configured patterns");
          showJsp("/WEB-INF/grouperUi2/oauth/oauthAuthorize.jsp");
          throw new ControllerDone();
        }
      }

      // get logged-in user
      Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

      if (loggedInSubject == null) {
        oAuthContainer.setErrorMessage("You must be logged in to approve this request");
        showJsp("/WEB-INF/grouperUi2/oauth/oauthAuthorize.jsp");
        throw new ControllerDone();
      }

      // create pending request
      GrouperOAuthPendingRequest pendingRequest = new GrouperOAuthPendingRequest();
      pendingRequest.setRequestId(GrouperOAuthStore.generateId());
      pendingRequest.setOauthClientInternalId(client.getInternalId());
      pendingRequest.setRedirectUri(redirectUri);
      pendingRequest.setCodeChallenge(codeChallenge);
      pendingRequest.setCodeChallengeMethod(codeChallengeMethod);
      pendingRequest.setState(state);
      pendingRequest.setScope(scope);
      pendingRequest.setCreatedMicros(System.currentTimeMillis() * 1000L);

      GrouperOAuthStore.storePendingRequest(pendingRequest);

      LOG.info("MCP OAuth authorization request created: requestId=" + pendingRequest.getRequestId()
          + ", clientId=" + clientId);

      // look up client name for display
      String clientName = StringUtils.isNotBlank(client.getClientName())
          ? client.getClientName() : clientId;

      // populate container for JSP
      oAuthContainer.setRequestId(pendingRequest.getRequestId());
      oAuthContainer.setClientId(clientId);
      oAuthContainer.setClientName(clientName);
      oAuthContainer.setScope(scope);
      oAuthContainer.setLoggedInUserName(loggedInSubject.getName());

      // check group memberships for scope checkboxes
      GrouperSession grouperSession = GrouperSession.startRootSession();
      try {
        String readonlyGroupName = GrouperConfig.retrieveConfig()
            .propertyValueString("grouper.mcp.users.readonly");
        String readwriteGroupName = GrouperConfig.retrieveConfig()
            .propertyValueString("grouper.mcp.users.readwrite");
        String sqlReadonlyGroupName = GrouperConfig.retrieveConfig()
            .propertyValueString("grouper.mcp.users.canRunSqlReadonly");
        String adminReadonlyGroupName = GrouperConfig.retrieveConfig()
            .propertyValueString("grouper.mcp.users.adminReadonly");
        String adminReadWriteGroupName = GrouperConfig.retrieveConfig()
            .propertyValueString("grouper.mcp.users.adminReadWrite");

        if (StringUtils.isNotBlank(readonlyGroupName)) {
          Group readonlyGroup = GroupFinder.findByName(grouperSession, readonlyGroupName, false);
          if (readonlyGroup != null && readonlyGroup.hasMember(loggedInSubject)) {
            oAuthContainer.setShowReadonly(true);
          }
        }
        if (StringUtils.isNotBlank(readwriteGroupName)) {
          Group readwriteGroup = GroupFinder.findByName(grouperSession, readwriteGroupName, false);
          if (readwriteGroup != null && readwriteGroup.hasMember(loggedInSubject)) {
            oAuthContainer.setShowReadwrite(true);
          }
        }
        if (StringUtils.isNotBlank(sqlReadonlyGroupName)) {
          Group sqlReadonlyGroup = GroupFinder.findByName(grouperSession, sqlReadonlyGroupName, false);
          if (sqlReadonlyGroup != null && sqlReadonlyGroup.hasMember(loggedInSubject)) {
            oAuthContainer.setShowSqlReadonly(true);
          }
        }
        if (StringUtils.isNotBlank(adminReadonlyGroupName)) {
          Group adminReadonlyGroup = GroupFinder.findByName(grouperSession, adminReadonlyGroupName, false);
          if (adminReadonlyGroup != null && adminReadonlyGroup.hasMember(loggedInSubject)) {
            oAuthContainer.setShowAdminReadonly(true);
          }
        }
        if (StringUtils.isNotBlank(adminReadWriteGroupName)) {
          Group adminReadWriteGroup = GroupFinder.findByName(grouperSession, adminReadWriteGroupName, false);
          if (adminReadWriteGroup != null && adminReadWriteGroup.hasMember(loggedInSubject)) {
            oAuthContainer.setShowAdminReadwrite(true);
          }
        }
      } finally {
        GrouperSession.stopQuietly(grouperSession);
      }

      // if user is not in any scope group, show an error
      if (!oAuthContainer.isShowReadonly() && !oAuthContainer.isShowReadwrite()
          && !oAuthContainer.isShowSqlReadonly()
          && !oAuthContainer.isShowAdminReadonly() && !oAuthContainer.isShowAdminReadwrite()) {
        GrouperOAuthStore.removePendingRequest(pendingRequest.getRequestId());
        sendAuthorizeError(response, redirectUri, state, "access_denied",
            "You are not authorized for any MCP operations", oAuthContainer);
        throw new ControllerDone();
      }

      showJsp("/WEB-INF/grouperUi2/oauth/oauthAuthorize.jsp");

    } catch (ControllerDone cd) {
      throw cd;
    } catch (RuntimeException re) {
      LOG.error("Error processing MCP OAuth authorization request", re);
      oAuthContainer.setErrorMessage("An error occurred processing the authorization request");
      showJsp("/WEB-INF/grouperUi2/oauth/oauthAuthorize.jsp");
    }
    throw new ControllerDone();
  }

  /**
   * Handle the consent form submission (approve or deny).
   * @param request
   * @param response
   */
  public void submitAuthorize(HttpServletRequest request, HttpServletResponse response) {

    if (!isMcpEnabled()) {
      throw new RuntimeException("MCP is not enabled (grouper.is.mcp is false)");
    }

    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {

      String requestId = request.getParameter("oauthRequestId");
      String action = request.getParameter("oauthAction");

      if (StringUtils.isBlank(requestId)) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing request ID");
        throw new ControllerDone();
      }

      GrouperOAuthPendingRequest pendingRequest =
          GrouperOAuthStore.retrievePendingRequest(requestId);

      if (pendingRequest == null) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "Authorization request not found or expired");
        throw new ControllerDone();
      }

      if ("deny".equals(action)) {
        // redirect back with error=access_denied
        GrouperOAuthStore.removePendingRequest(requestId);
        String redirectUrl = pendingRequest.getRedirectUri()
            + (pendingRequest.getRedirectUri().contains("?") ? "&" : "?")
            + "error=access_denied"
            + "&error_description=" + GrouperUtil.escapeUrlEncode("User denied the request");
        if (StringUtils.isNotBlank(pendingRequest.getState())) {
          redirectUrl += "&state=" + GrouperUtil.escapeUrlEncode(pendingRequest.getState());
        }

        LOG.info("MCP OAuth request denied by user: requestId=" + requestId);

        response.sendRedirect(redirectUrl);
        throw new ControllerDone();
      }

      // approve
      if (loggedInSubject == null) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
        throw new ControllerDone();
      }

      grouperSession = GrouperSession.start(loggedInSubject);

      Member member = MemberFinder.findBySubject(grouperSession, loggedInSubject, true);

      // read consent scope checkboxes
      boolean scopeReadonly = "true".equals(request.getParameter("oauthScopeReadonly"));
      boolean scopeReadwrite = "true".equals(request.getParameter("oauthScopeReadwrite"));
      boolean scopeSqlReadonly = "true".equals(request.getParameter("oauthScopeSqlReadonly"));
      boolean scopeAdminReadonly = "true".equals(request.getParameter("oauthScopeAdminReadonly"));
      boolean scopeAdminReadwrite = "true".equals(request.getParameter("oauthScopeAdminReadwrite"));

      // at least one scope must be selected
      if (!scopeReadonly && !scopeReadwrite && !scopeSqlReadonly
          && !scopeAdminReadonly && !scopeAdminReadwrite) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "At least one operation scope must be selected");
        throw new ControllerDone();
      }

      // build consent details JSON
      StringBuilder consentJson = new StringBuilder("{");
      consentJson.append("\"readonly\":").append(scopeReadonly);
      consentJson.append(",\"readwrite\":").append(scopeReadwrite);
      consentJson.append(",\"sqlReadonly\":").append(scopeSqlReadonly);
      consentJson.append(",\"adminReadonly\":").append(scopeAdminReadonly);
      consentJson.append(",\"adminReadwrite\":").append(scopeAdminReadwrite);
      consentJson.append("}");

      // generate authorization code
      String authorizationCode = GrouperOAuthStore.generateId();

      // create the auth code object
      GrouperOAuthCode authCode = new GrouperOAuthCode();
      authCode.setCode(authorizationCode);
      authCode.setOauthClientInternalId(pendingRequest.getOauthClientInternalId());
      authCode.setRedirectUri(pendingRequest.getRedirectUri());
      authCode.setCodeChallenge(pendingRequest.getCodeChallenge());
      authCode.setCodeChallengeMethod(pendingRequest.getCodeChallengeMethod());
      authCode.setMemberInternalId(member.getInternalId());
      authCode.setConsentDetails(consentJson.toString());
      authCode.setUsed(false);
      authCode.setCreatedMicros(System.currentTimeMillis() * 1000L);

      GrouperOAuthStore.storeAuthorizationCode(authCode);

      // update client: increment code_count, set member_internal_id on first code, set last_code_micros
      GrouperOAuthClient oauthClient = GrouperOAuthStore.retrieveClientByInternalId(
          pendingRequest.getOauthClientInternalId());
      if (oauthClient != null) {
        Long currentCount = oauthClient.getCodeCount();
        oauthClient.setCodeCount(currentCount != null ? currentCount + 1 : 1L);
        oauthClient.setLastCodeMicros(System.currentTimeMillis() * 1000L);
        if (oauthClient.getMemberInternalId() == null) {
          oauthClient.setMemberInternalId(member.getInternalId());
        }
        new edu.internet2.middleware.grouperClient.jdbc.GcDbAccess().storeToDatabase(oauthClient);
      }

      // audit
      AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.OAUTH_CONSENT_APPROVE,
          "clientId", oauthClient != null ? oauthClient.getClientId() : String.valueOf(pendingRequest.getOauthClientInternalId()),
          "clientName", oauthClient != null ? oauthClient.getClientName() : null,
          "memberId", member.getId());
      auditEntry.setDescription("OAuth consent approved for client="
          + (oauthClient != null ? oauthClient.getClientId() : pendingRequest.getOauthClientInternalId())
          + ", subject=" + loggedInSubject.getSourceId() + "::::" + loggedInSubject.getId());
      auditEntry.saveOrUpdate(true);

      // clean up the pending request
      GrouperOAuthStore.removePendingRequest(requestId);

      // redirect back to client with authorization code
      String redirectUrl = pendingRequest.getRedirectUri()
          + (pendingRequest.getRedirectUri().contains("?") ? "&" : "?")
          + "code=" + GrouperUtil.escapeUrlEncode(authorizationCode);
      if (StringUtils.isNotBlank(pendingRequest.getState())) {
        redirectUrl += "&state=" + GrouperUtil.escapeUrlEncode(pendingRequest.getState());
      }

      LOG.info("MCP OAuth request approved: requestId=" + requestId
          + ", subject=" + loggedInSubject.getSourceId() + "::::" + loggedInSubject.getId());

      response.sendRedirect(redirectUrl);
      throw new ControllerDone();

    } catch (ControllerDone cd) {
      throw cd;
    } catch (Exception e) {
      LOG.error("Error processing MCP OAuth authorization", e);
      throw new RuntimeException("Error processing MCP OAuth authorization", e);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Check if MCP is enabled via the grouper.is.mcp configuration property.
   * @return true if MCP is enabled
   */
  private static boolean isMcpEnabled() {
    return edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig.retrieveConfig()
        .propertyValueBoolean("grouper.is.mcp", false);
  }

  /**
   * Send an OAuth error response as a redirect to the redirect_uri with error parameters.
   * If redirect_uri is blank, shows an error page instead.
   */
  private void sendAuthorizeError(HttpServletResponse response, String redirectUri,
      String state, String error, String errorDescription,
      OAuthContainer oAuthContainer) {

    if (StringUtils.isBlank(redirectUri)) {
      oAuthContainer.setErrorMessage(errorDescription);
      showJsp("/WEB-INF/grouperUi2/oauth/oauthAuthorize.jsp");
      return;
    }

    try {
      StringBuilder errorUrl = new StringBuilder(redirectUri);
      errorUrl.append(redirectUri.contains("?") ? "&" : "?");
      errorUrl.append("error=").append(GrouperUtil.escapeUrlEncode(error));
      if (StringUtils.isNotBlank(errorDescription)) {
        errorUrl.append("&error_description=").append(
            GrouperUtil.escapeUrlEncode(errorDescription));
      }
      if (StringUtils.isNotBlank(state)) {
        errorUrl.append("&state=").append(GrouperUtil.escapeUrlEncode(state));
      }
      response.sendRedirect(errorUrl.toString());
    } catch (Exception e) {
      throw new RuntimeException("Error sending OAuth error redirect", e);
    }
  }
}
