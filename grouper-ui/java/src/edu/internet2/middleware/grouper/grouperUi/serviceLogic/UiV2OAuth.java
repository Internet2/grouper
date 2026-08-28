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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthStore;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthCode;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthClient;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthPendingRequest;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.OAuthContainer;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
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

      // an authorization code issued here is redeemed for a token whose issuer and audience
      // come from grouper.mcp.baseUrl, falling back to grouper.ws.url, so without one of them
      // there is nothing worth authorizing.  the page
      // says only that the server is not configured, since whoever is looking at it is a user
      // being asked to approve something and not the person who can fix it; the detail is
      // logged once by mcpUrlConfigurationError
      if (GrouperOAuthStore.mcpUrlConfigurationError() != null) {
        oAuthContainer.setErrorMessage("MCP is not fully configured on this server.  "
            + "Contact your Grouper administrator.");
        showJsp("/WEB-INF/grouperUi2/oauth/oauthAuthorize.jsp");
        throw new ControllerDone();
      }

      String clientId = request.getParameter("client_id");
      String redirectUri = request.getParameter("redirect_uri");
      String responseType = request.getParameter("response_type");
      String codeChallenge = request.getParameter("code_challenge");
      String codeChallengeMethod = request.getParameter("code_challenge_method");
      String state = request.getParameter("state");
      String scope = request.getParameter("scope");

      // Everything which decides whether this redirect_uri can be redirected to is checked
      // first, and any of it failing shows an error page rather than redirecting.  Only after
      // that is the redirect_uri known to belong to a registered client, and only then may a
      // problem with the rest of the request be reported by sending the user back to it.
      // Reporting an error by redirecting to a redirect_uri which has not been checked would
      // let anyone use this endpoint to bounce a browser to any address they like, which
      // RFC 6749 section 4.1.2.1 requires an authorization server not to do.

      if (StringUtils.isBlank(redirectUri)) {
        // nowhere to redirect to
        oAuthContainer.setErrorMessage("redirect_uri is required");
        showJsp("/WEB-INF/grouperUi2/oauth/oauthAuthorize.jsp");
        throw new ControllerDone();
      }

      if (StringUtils.isBlank(clientId)) {
        // without a client there is nothing to check the redirect_uri against
        oAuthContainer.setErrorMessage("client_id is required");
        showJsp("/WEB-INF/grouperUi2/oauth/oauthAuthorize.jsp");
        throw new ControllerDone();
      }

      // validate client registration
      GrouperOAuthClient client = GrouperOAuthStore.retrieveClient(clientId);
      if (client == null) {
        oAuthContainer.setErrorMessage("Unknown client_id");
        showJsp("/WEB-INF/grouperUi2/oauth/oauthAuthorize.jsp");
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

      // the redirect_uri is now known to be one this client registered, so the rest of the
      // request can be reported to the client by redirecting back to it

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

      // The client says which resource it wants the token for.  This Grouper issues tokens for
      // one resource, its MCP endpoint, so a request for anything else is refused rather than
      // being quietly given a token which would not work there.  The token is bound to the MCP
      // resource when it is issued, so there is nothing to carry forward from here.
      {
        String resource = StringUtils.trimToNull(request.getParameter("resource"));
        String mcpResourceIdentifier = GrouperOAuthStore.retrieveMcpResourceIdentifier();

        if (resource != null && mcpResourceIdentifier != null
            && !resource.equals(mcpResourceIdentifier)
            && !resource.equals(StringUtils.removeEnd(mcpResourceIdentifier, "/"))) {
          LOG.warn("OAuth authorization refused, resource '" + resource
              + "' is not this server's MCP resource '" + mcpResourceIdentifier + "'");
          sendAuthorizeError(response, redirectUri, state, "invalid_target",
              "resource is not a resource of this authorization server", oAuthContainer);
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
      oAuthContainer.setRequireReadwriteDataScope(
          GrouperConfig.retrieveConfig().propertyValueBoolean(
              "grouper.mcp.oauth.requireReadwriteDataScope", true));

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
            oAuthContainer.setShowReadonly(true);
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
            oAuthContainer.setShowAdminReadonly(true);
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

      GrouperTextContainer.assignThreadLocalVariable("maxFolders",
          String.valueOf(GrouperConfig.retrieveConfig().propertyValueInt("grouper.mcp.oauth.maxReadwriteFolders", 10)));
      GrouperTextContainer.assignThreadLocalVariable("maxGroups",
          String.valueOf(GrouperConfig.retrieveConfig().propertyValueInt("grouper.mcp.oauth.maxReadwriteGroups", 10)));
      GrouperTextContainer.assignThreadLocalVariable("maxSubjects",
          String.valueOf(GrouperConfig.retrieveConfig().propertyValueInt("grouper.mcp.oauth.maxReadwriteSubjects", 50)));

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

      // a pending request can outlive the configuration it was created under, so this is
      // checked again here rather than relied on from the authorize step
      if (GrouperOAuthStore.mcpUrlConfigurationError() != null) {
        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
            "MCP is not fully configured on this server.  Contact your Grouper administrator.");
        throw new ControllerDone();
      }

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
        redirectUrl += issuerParam();

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

      // readwrite implies readonly, admin readwrite implies admin readonly
      if (scopeReadwrite) {
        scopeReadonly = true;
      }
      if (scopeAdminReadwrite) {
        scopeAdminReadonly = true;
      }

      // at least one scope must be selected
      if (!scopeReadonly && !scopeReadwrite && !scopeSqlReadonly
          && !scopeAdminReadonly && !scopeAdminReadwrite) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST,
            GrouperTextContainer.textOrNull("oauthConsentAtLeastOneScopeRequired"));
        throw new ControllerDone();
      }

      // read readwrite scope restrictions (if readwrite is selected)
      List<String> readwriteFolders = new ArrayList<String>();
      List<String> readwriteGroups = new ArrayList<String>();
      List<String> readwriteSubjects = new ArrayList<String>();

      if (scopeReadwrite) {
        // one hidden field per pick, carrying the id the combobox gave the page.  the scope is
        // stored by name, so the ids are translated here, once, rather than on every pick
        List<String> stemIds = readScopeHiddenFields(request, "extraStemId_");
        List<String> groupIds = readScopeHiddenFields(request, "extraGroupId_");

        readwriteFolders = resolveStemNames(stemIds, loggedInSubject);
        readwriteGroups = resolveGroupNames(groupIds, loggedInSubject);
        readwriteSubjects = resolveSubjectIds(readScopeHiddenFields(request, "extraSubjectId_"));

        // Anything which did not resolve is refused rather than dropped.  Carrying on with a
        // short list would hand out a wider consent than the one on the screen, since a folder
        // and group list which ends up empty leaves that dimension unscoped rather than closed.
        //
        // Subjects are deliberately not checked this way.  Nothing is looked up for them, so the
        // only reason that list shrinks is resolveSubjectIds collapsing two picks of the same
        // subject from different sources, which is right: the scope matches a subject id without
        // a source.  That cannot turn a non empty pick into an empty list, which is the case
        // this check exists to catch.
        if (readwriteFolders.size() != stemIds.size() || readwriteGroups.size() != groupIds.size()) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST,
              GrouperTextContainer.textOrNull("oauthConsentReadwriteValidationError"));
          throw new ControllerDone();
        }

        // validate at least one restriction is present (when config requires it)
        boolean requireReadwriteDataScope = GrouperConfig.retrieveConfig()
            .propertyValueBoolean("grouper.mcp.oauth.requireReadwriteDataScope", true);
        if (requireReadwriteDataScope && readwriteFolders.isEmpty()
            && readwriteGroups.isEmpty() && readwriteSubjects.isEmpty()) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST,
              GrouperTextContainer.textOrNull("oauthConsentReadwriteAtLeastOneRequired"));
          throw new ControllerDone();
        }

        // The maximums again.  The page keeps to them while somebody is picking and
        // ajaxValidateReadwriteScope checks them before it lets the form go, but this is the
        // endpoint which actually issues the code, and it is reachable without either of those.
        if (readwriteFolders.size() > GrouperConfig.retrieveConfig()
                .propertyValueInt("grouper.mcp.oauth.maxReadwriteFolders", 10)
            || readwriteGroups.size() > GrouperConfig.retrieveConfig()
                .propertyValueInt("grouper.mcp.oauth.maxReadwriteGroups", 10)
            || readwriteSubjects.size() > GrouperConfig.retrieveConfig()
                .propertyValueInt("grouper.mcp.oauth.maxReadwriteSubjects", 50)) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST,
              GrouperTextContainer.textOrNull("oauthConsentReadwriteValidationError"));
          throw new ControllerDone();
        }
      }

      // build consent details JSON
      ObjectMapper consentMapper = new ObjectMapper();
      ObjectNode consentNode = consentMapper.createObjectNode();
      consentNode.put("readonly", scopeReadonly);
      consentNode.put("readwrite", scopeReadwrite);
      consentNode.put("sqlReadonly", scopeSqlReadonly);
      consentNode.put("adminReadonly", scopeAdminReadonly);
      consentNode.put("adminReadwrite", scopeAdminReadwrite);

      // add readwrite scope restrictions if present
      if (!readwriteFolders.isEmpty()) {
        ArrayNode foldersArray = consentNode.putArray("readwriteFolders");
        for (String folder : readwriteFolders) {
          foldersArray.add(folder);
        }
      }
      if (!readwriteGroups.isEmpty()) {
        ArrayNode groupsArray = consentNode.putArray("readwriteGroups");
        for (String group : readwriteGroups) {
          groupsArray.add(group);
        }
      }
      if (!readwriteSubjects.isEmpty()) {
        ArrayNode subjectsArray = consentNode.putArray("readwriteSubjects");
        for (String subject : readwriteSubjects) {
          subjectsArray.add(subject);
        }
      }

      String consentJson = consentNode.toString();

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
      authCode.setConsentDetails(consentJson);
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
        // the retrieve above no longer decrypts the client secret onto this object, so this
        // write leaves the stored secret exactly as it was.  it used to decrypt it, and this
        // write then put the secret back in the clear, the first time a client was ever used
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
      redirectUrl += issuerParam();

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
   * AJAX endpoint to validate the readwrite scope restrictions before form submission.
   * Uses the standard GuiResponseJs framework. On success, executes oauthDoSubmit().
   * On error, displays a validation message via the standard #messaging div.
   * @param request
   * @param response
   */
  public void ajaxValidateReadwriteScope(HttpServletRequest request,
      HttpServletResponse response) {

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      // one hidden field per pick, carrying the id the combobox gave the page.  the picking is
      // done in the browser, so this is both the translation to names and the backstop against a
      // post which did not come from the screen
      List<String> stemIds = readScopeHiddenFields(request, "extraStemId_");
      List<String> groupIds = readScopeHiddenFields(request, "extraGroupId_");

      List<String> folderPaths = resolveStemNames(stemIds, loggedInSubject);
      List<String> groupPaths = resolveGroupNames(groupIds, loggedInSubject);
      List<String> subjectIds = resolveSubjectIds(readScopeHiddenFields(request, "extraSubjectId_"));

      // see the note on the same check in submitAuthorize: a pick which no longer resolves has to
      // stop the consent, because dropping it would widen the scope rather than narrow it
      if (folderPaths.size() != stemIds.size()) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#oauthReadwriteFolderComboErrorId",
            GrouperTextContainer.textOrNull("oauthConsentReadwriteFolderNotFound")));
        return;
      }

      if (groupPaths.size() != groupIds.size()) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#oauthReadwriteGroupComboErrorId",
            GrouperTextContainer.textOrNull("oauthConsentReadwriteGroupNotFound")));
        return;
      }

      // check if data scope restrictions are required by config
      boolean requireReadwriteDataScope = GrouperConfig.retrieveConfig()
          .propertyValueBoolean("grouper.mcp.oauth.requireReadwriteDataScope", true);

      // at least one must be non-empty (when config requires it)
      if (folderPaths.isEmpty() && groupPaths.isEmpty() && subjectIds.isEmpty()) {
        if (requireReadwriteDataScope) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
              "#readwriteScopeSection",
              GrouperTextContainer.textOrNull("oauthConsentReadwriteAtLeastOneRequired")));
          return;
        }
        // if not required and all empty, nothing to validate — submit the form
        guiResponseJs.addAction(GuiScreenAction.newScript("oauthDoSubmit()"));
        return;
      }

      // count checks (limits are configurable)
      int maxFolders = GrouperConfig.retrieveConfig().propertyValueInt("grouper.mcp.oauth.maxReadwriteFolders", 10);
      int maxGroups = GrouperConfig.retrieveConfig().propertyValueInt("grouper.mcp.oauth.maxReadwriteGroups", 10);
      int maxSubjects = GrouperConfig.retrieveConfig().propertyValueInt("grouper.mcp.oauth.maxReadwriteSubjects", 50);
      int maxGroupsInFolders = GrouperConfig.retrieveConfig().propertyValueInt("grouper.mcp.oauth.maxGroupsInReadwriteFolders", 500);

      if (folderPaths.size() > maxFolders) {
        GrouperTextContainer.assignThreadLocalVariable("itemCount",
            String.valueOf(folderPaths.size()));
        GrouperTextContainer.assignThreadLocalVariable("maxCount",
            String.valueOf(maxFolders));
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#oauthReadwriteFolderComboErrorId",
            GrouperTextContainer.textOrNull("oauthConsentReadwriteTooManyFolders")));
        return;
      }
      if (groupPaths.size() > maxGroups) {
        GrouperTextContainer.assignThreadLocalVariable("itemCount",
            String.valueOf(groupPaths.size()));
        GrouperTextContainer.assignThreadLocalVariable("maxCount",
            String.valueOf(maxGroups));
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#oauthReadwriteGroupComboErrorId",
            GrouperTextContainer.textOrNull("oauthConsentReadwriteTooManyGroups")));
        return;
      }
      if (subjectIds.size() > maxSubjects) {
        GrouperTextContainer.assignThreadLocalVariable("itemCount",
            String.valueOf(subjectIds.size()));
        GrouperTextContainer.assignThreadLocalVariable("maxCount",
            String.valueOf(maxSubjects));
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#oauthReadwriteSubjectComboErrorId",
            GrouperTextContainer.textOrNull("oauthConsentReadwriteTooManySubjects")));
        return;
      }

      // How many groups the folders cover.  The folders and groups themselves are not looked up
      // again: they are here because resolveStemNames and resolveGroupNames just resolved them,
      // and anything which did not resolve was already dropped rather than carried this far.
      if (!folderPaths.isEmpty()) {

        long totalGroupCount = 0;

        for (String folderPath : folderPaths) {
          // count groups under this folder (recursive)
          long groupCount = new GcDbAccess()
              .sql("SELECT count(*) FROM grouper_groups WHERE name LIKE ?")
              .addBindVar(folderPath + ":%")
              .select(Long.class);
          totalGroupCount += groupCount;
        }

        if (totalGroupCount >= maxGroupsInFolders) {
          GrouperTextContainer.assignThreadLocalVariable("groupCount",
              String.valueOf(totalGroupCount));
          GrouperTextContainer.assignThreadLocalVariable("maxCount",
              String.valueOf(maxGroupsInFolders));
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
              "#oauthReadwriteFolderComboErrorId",
              GrouperTextContainer.textOrNull("oauthConsentReadwriteTooManyGroupsInFolders")));
          return;
        }
      }

      // all valid — submit the form
      guiResponseJs.addAction(GuiScreenAction.newScript("oauthDoSubmit()"));

    } catch (Exception e) {
      LOG.error("Error in AJAX readwrite scope validation", e);
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
          GrouperTextContainer.textOrNull("oauthConsentReadwriteServerError")));
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
      errorUrl.append(issuerParam());
      response.sendRedirect(errorUrl.toString());
    } catch (Exception e) {
      throw new RuntimeException("Error sending OAuth error redirect", e);
    }
  }

  /**
   * The {@code iss} parameter to append to an authorization response, identifying this Grouper
   * as the authorization server which issued it, per
   * <a href="https://datatracker.ietf.org/doc/html/rfc9207">RFC 9207</a>. A client which knows
   * about it compares this against the issuer it recorded from the authorization server
   * metadata and refuses the response if they differ, which stops it being tricked into sending
   * an authorization code to the wrong authorization server.
   *
   * <p>Sent on error responses as well as successful ones, since a client which only checked
   * the successful case would still be open to the mix-up this prevents.</p>
   *
   * @return the parameter to append, starting with an ampersand
   */
  private static String issuerParam() {

    return "&iss=" + GrouperUtil.escapeUrlEncode(GrouperOAuthStore.retrieveIssuerIdentifier());
  }
  
  /**
   * Translate the folder ids a consent form is carrying into folder names.
   *
   * <p>The picker hands back an id, because that is what a combobox has, and the scope is stored
   * by name, because a folder scope means the groups whose names start with it.  The translation
   * happens once here rather than on every pick.  One query, not one per folder.</p>
   *
   * <p>Ids which do not resolve are left out, and the caller must treat a short list as a failure
   * rather than carrying on.  Dropping one quietly would widen the consent, not narrow it: an
   * empty folder and group list does not mean "no groups", it means "this dimension is not
   * scoped", so losing the only folder somebody picked turns their consent into every group they
   * can already write to.  The root folder is dropped here for the same reason it is kept out of
   * the picker: its name is the empty string, and a folder scope matches the groups whose names
   * start with the folder and a colon, so it would restrict nothing while still counting as a
   * restriction for grouper.mcp.oauth.requireReadwriteDataScope.</p>
   *
   * <p>Resolved as the user doing the consenting, not as root, and with no privileges assigned,
   * which is the same thing UiV2Stem.stemViewFilter offers: the folders they can see.  An id for
   * anything else simply does not resolve and is dropped.  Resolving as root would translate any
   * id somebody cared to post into a name, and that name is kept on the consent, which would
   * turn this screen into a way to read the folder tree by guessing ids.</p>
   *
   * <p>Runs in the caller's session, which is already the logged in user's.</p>
   *
   * @param stemIds the ids from the form
   * @param loggedInSubject who is consenting
   * @return the names, in the order the ids were given
   */
  private static List<String> resolveStemNames(List<String> stemIds, Subject loggedInSubject) {

    List<String> stemNames = new ArrayList<String>();

    if (stemIds.isEmpty()) {
      return stemNames;
    }

    Map<String, String> idToName = new HashMap<String, String>();

    for (Stem stem : GrouperUtil.nonNull(new StemFinder().assignStemIds(stemIds)
        .assignSubject(loggedInSubject).findStems())) {
      idToName.put(stem.getId(), stem.getName());
    }

    for (String stemId : stemIds) {
      String stemName = idToName.get(stemId);
      if (StringUtils.isNotBlank(stemName)) {
        stemNames.add(stemName);
      }
    }

    return stemNames;
  }

  /**
   * Translate the group ids a consent form is carrying into group names.  See
   * {@link #resolveStemNames(List, Subject)}, which this mirrors.
   *
   * <p>Filtered on UPDATE, which is what UiV2Group.groupUpdateFilter offers in the picker, so a
   * group the user could not have picked does not resolve here either.</p>
   *
   * @param groupIds the ids from the form
   * @param loggedInSubject who is consenting
   * @return the names, in the order the ids were given
   */
  private static List<String> resolveGroupNames(List<String> groupIds, Subject loggedInSubject) {

    List<String> groupNames = new ArrayList<String>();

    if (groupIds.isEmpty()) {
      return groupNames;
    }

    Map<String, String> idToName = new HashMap<String, String>();

    for (Group group : GrouperUtil.nonNull(new GroupFinder().assignGroupIds(groupIds)
        .assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
        .assignSubject(loggedInSubject).findGroups())) {
      idToName.put(group.getId(), group.getName());
    }

    for (String groupId : groupIds) {
      String groupName = idToName.get(groupId);
      if (StringUtils.isNotBlank(groupName)) {
        groupNames.add(groupName);
      }
    }

    return groupNames;
  }

  /**
   * Pull the subject ids out of what the subject picker handed back.
   *
   * <p>That picker is the shared one, whose ids are sourceId||subjectId.  The scope is stored as
   * a bare subject id, and matches an id or one of a subject's identifiers, so the source half is
   * dropped.  Nothing is looked up: a subject id which matches nothing authorizes nothing.</p>
   *
   * @param pickedValues what the form carried
   * @return the subject ids, in the order they were picked
   */
  private static List<String> resolveSubjectIds(List<String> pickedValues) {

    List<String> subjectIds = new ArrayList<String>();

    for (String pickedValue : pickedValues) {

      // the first separator, not the last: a source id has no || in it, but nothing stops a
      // subject id from having one, and that whole id is what the scope is stored as
      String subjectId = StringUtils.trimToNull(StringUtils.substringAfter(pickedValue, "||"));

      if (subjectId == null) {
        // typed in rather than picked, so there is no source half to drop
        subjectId = StringUtils.trimToNull(pickedValue);
      }

      if (subjectId != null && !subjectIds.contains(subjectId)) {
        subjectIds.add(subjectId);
      }
    }

    return subjectIds;
  }

  /**
   * Read the values a consent form is carrying for one dimension of the readwrite data scope.
   *
   * <p>Each pick is a hidden field in the form rather than anything held on the server, so nothing
   * is kept for a consent page somebody abandons.</p>
   *
   * <p>Unlike the group import screen this was modelled on, a gap in the numbering is skipped
   * rather than treated as the end of the list.  Stopping there would silently shorten the scope,
   * and a folder or group list which ends up empty leaves that dimension unscoped rather than
   * closed, so a dropped field would widen the consent instead of narrowing it.</p>
   *
   * @param request the request
   * @param fieldNamePrefix e.g. extraStemId_
   * @return the values in the order they were picked, never null
   */
  private static List<String> readScopeHiddenFields(HttpServletRequest request, String fieldNamePrefix) {

    List<String> values = new ArrayList<String>();

    for (int i = 0; i < 1000; i++) {

      String value = StringUtils.trimToNull(request.getParameter(fieldNamePrefix + i));

      if (value != null && !values.contains(value)) {
        values.add(value);
      }
    }

    return values;
  }
}
