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
package edu.internet2.middleware.grouper.ws.mcp;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthClient;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthSigningKey;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthStore;
import edu.internet2.middleware.grouper.mcp.GrouperMcpDocSearchIndex;
import edu.internet2.middleware.grouper.mcp.GrouperMcpToolLog;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.j2ee.Authentication;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.security.WsCustomAuthentication;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Subject;

/**
 * MCP (Model Context Protocol) server servlet implementing the Streamable HTTP transport
 * (spec version 2025-03-26).
 *
 * <p>Authentication is supported via two mechanisms:</p>
 * <ol>
 *   <li><strong>OAuth 2.1 JWT</strong> - Bearer token from the Grouper OAuth flow (preferred for MCP clients)</li>
 *   <li><strong>Normal WS authentication</strong> - HTTP Basic auth or container-managed auth
 *   (same as regular Grouper WS)</li>
 * </ol>
 * <p>If a Bearer token is present it is tried first.  If no Bearer token is present,
 * the servlet falls back to normal WS authentication (HTTP Basic if
 * {@code grouper.is.ws.basicAuthn} is enabled, or a custom authentication class).
 * If neither authentication method succeeds, the error message references the
 * OAuth flow.</p>
 *
 * <p>All communication happens on a single endpoint {@code /mcp}:</p>
 * <ul>
 *   <li>POST - Client-to-server JSON-RPC 2.0 messages (initialize, tools/list, tools/call, etc.)</li>
 *   <li>DELETE - Session termination</li>
 *   <li>GET - Returns 405 (server-initiated SSE not needed for this tool server)</li>
 * </ul>
 *
 * <p>The {@code Mcp-Session-Id} header is accepted but not required. A session id is minted on
 * {@code initialize} for clients on spec version 2025-03-26, which send it back on subsequent
 * requests. Sessions were removed from the Streamable HTTP transport in spec version
 * 2026-07-28, so clients on that revision never send the header and do not call
 * {@code initialize} at all. No state is kept against the session id, so both work.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpServlet.class);

  private static final String MCP_PROTOCOL_VERSION = "2025-03-26";

  private static final String SERVER_NAME = "grouper-mcp-server";

  private static final String SERVER_VERSION = "1.0.0";

  private static final String SESSION_ID_HEADER = "Mcp-Session-Id";

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * cache for isSubjectInGroup results.
   * key is MultiKey(subjectId, subjectSourceId, groupPropertyName), value is Boolean.
   * caches for 60 seconds so that group membership changes take effect quickly
   * but we avoid hitting the database on every MCP request.
   */
  private static GrouperCache<MultiKey, Boolean> subjectInGroupCache =
      new GrouperCache<MultiKey, Boolean>(
          GrouperMcpServlet.class.getName() + ".subjectInGroupCache",
          2000, false, 60, 60, false);

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // authenticate - try OAuth JWT first, then fall back to normal WS auth
    GrouperMcpAuthUser authUser = authenticateRequest(request, response);
    if (authUser == null) {
      // response has already been set by authenticateRequest
      return;
    }

    JsonNode jsonRpcRequest;
    try {
      jsonRpcRequest = objectMapper.readTree(request.getInputStream());
    } catch (Exception e) {
      sendJsonRpcError(response, null, -32700, "Parse error");
      return;
    }

    String method = jsonRpcRequest.has("method") ? jsonRpcRequest.get("method").asText() : null;
    JsonNode id = jsonRpcRequest.get("id");
    JsonNode params = jsonRpcRequest.get("params");

    if (method == null) {
      sendJsonRpcError(response, id, -32600, "Invalid Request: missing method");
      return;
    }

    // The Mcp-Session-Id header is optional.  Clients on spec version 2025-03-26 send it back
    // on every request after initialize, and this server still mints one for them.  Sessions
    // were removed from the Streamable HTTP transport in spec version 2026-07-28, so clients on
    // that revision never send it, and requiring it would make every call after the handshake
    // fail.  This server keeps no state against the session id, so there is nothing to look up
    // and nothing to enforce.
    if (LOG.isDebugEnabled()) {
      String sessionId = request.getHeader(SESSION_ID_HEADER);
      LOG.debug("MCP method '" + method + "' with "
          + (StringUtils.isBlank(sessionId) ? "no session id" : "session id: " + sessionId));
    }

    ObjectNode result;

    try {
      switch (method) {
        case "initialize":
          result = handleInitialize(params, response);
          break;
        case "notifications/initialized":
          response.setStatus(HttpServletResponse.SC_ACCEPTED);
          return;
        case "tools/list":
          result = handleToolsList(params, authUser);
          break;
        case "tools/call":
          result = handleToolsCall(params, authUser);
          break;
        case "ping":
          result = objectMapper.createObjectNode();
          break;
        default:
          sendJsonRpcError(response, id, -32601, "Method not found: " + method);
          return;
      }
    } catch (Exception e) {
      LOG.error("Error handling MCP method: " + method, e);
      sendJsonRpcError(response, id, -32603, "Internal error: " + e.getMessage());
      return;
    }

    // build JSON-RPC response
    ObjectNode jsonRpcResponse = objectMapper.createObjectNode();
    jsonRpcResponse.put("jsonrpc", "2.0");
    if (id != null) {
      jsonRpcResponse.set("id", id);
    }
    jsonRpcResponse.set("result", result);

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(jsonRpcResponse));
    response.getWriter().flush();
  }

  /**
   * authenticate the request.  tries OAuth JWT first (Bearer token), then falls back
   * to normal WS authentication (HTTP Basic or container auth).  if neither succeeds,
   * returns null and sets the response to 401 with an OAuth error.
   * @param request the HTTP request
   * @param response the HTTP response
   * @return the authenticated user, or null if authentication failed
   */
  private GrouperMcpAuthUser authenticateRequest(HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    String authHeader = request.getHeader("Authorization");

    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.oauth.logAuthDebug", false)) {
      LOG.warn("MCP authenticateRequest: authHeader "
          + (StringUtils.isBlank(authHeader) ? "is blank"
              : (authHeader.startsWith("Bearer ") ? "is Bearer (len=" + authHeader.length() + ")"
                  : "starts with: " + StringUtils.abbreviate(authHeader, 20)))
          + ", remoteAddr=" + request.getRemoteAddr());
    }

    // try OAuth JWT if Bearer token is present
    if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")
        && GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.mcp.auth.oauth", true)) {
      String bearerToken = authHeader.substring("Bearer ".length()).trim();
      DecodedJWT decodedJwt = GrouperOAuthSigningKey.verifyAndDecodeJwt(bearerToken);
      if (decodedJwt != null) {

        String subjectId = decodedJwt.getSubject();
        String subjectSourceId = decodedJwt.getClaim("subjectSourceId").asString();

        LOG.debug("MCP request authenticated via OAuth: subject=" + subjectId
            + ", client=" + decodedJwt.getClaim("client_id").asString());

        // set REMOTE_USER so WS service logic can identify the logged-in user
        if (StringUtils.isNotBlank(subjectSourceId)) {
          request.setAttribute("REMOTE_USER", subjectSourceId + "::::" + subjectId);
        } else {
          request.setAttribute("REMOTE_USER", subjectId);
        }

        // resolve to a Subject
        GrouperMcpAuthUser authUser = resolveAuthUser(subjectSourceId != null
            ? (subjectSourceId + "::::" + subjectId) : subjectId);
        if (authUser == null) {
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
          return null;
        }

        // extract consent scopes from JWT claims
        authUser.setOAuthAuthenticated(true);
        authUser.setJwtIssuedAt(decodedJwt.getIssuedAt());
        authUser.setConsentScopeReadonly(
            decodedJwt.getClaim("grouper_readonly") != null
            && !decodedJwt.getClaim("grouper_readonly").isNull()
            && decodedJwt.getClaim("grouper_readonly").asBoolean());
        authUser.setConsentScopeReadwrite(
            decodedJwt.getClaim("grouper_readwrite") != null
            && !decodedJwt.getClaim("grouper_readwrite").isNull()
            && decodedJwt.getClaim("grouper_readwrite").asBoolean());
        authUser.setConsentScopeSqlReadonly(
            decodedJwt.getClaim("grouper_sql_readonly") != null
            && !decodedJwt.getClaim("grouper_sql_readonly").isNull()
            && decodedJwt.getClaim("grouper_sql_readonly").asBoolean());
        authUser.setConsentScopeAdminReadonly(
            decodedJwt.getClaim("grouper_admin_readonly") != null
            && !decodedJwt.getClaim("grouper_admin_readonly").isNull()
            && decodedJwt.getClaim("grouper_admin_readonly").asBoolean());
        authUser.setConsentScopeAdminReadwrite(
            decodedJwt.getClaim("grouper_admin_readwrite") != null
            && !decodedJwt.getClaim("grouper_admin_readwrite").isNull()
            && decodedJwt.getClaim("grouper_admin_readwrite").asBoolean());

        // extract readwrite scope restrictions from JWT claims
        if (decodedJwt.getClaim("grouper_readwrite_folders") != null
            && !decodedJwt.getClaim("grouper_readwrite_folders").isNull()) {
          authUser.setConsentReadwriteFolders(
              decodedJwt.getClaim("grouper_readwrite_folders").asList(String.class));
        }
        if (decodedJwt.getClaim("grouper_readwrite_groups") != null
            && !decodedJwt.getClaim("grouper_readwrite_groups").isNull()) {
          authUser.setConsentReadwriteGroups(
              decodedJwt.getClaim("grouper_readwrite_groups").asList(String.class));
        }
        if (decodedJwt.getClaim("grouper_readwrite_subjects") != null
            && !decodedJwt.getClaim("grouper_readwrite_subjects").isNull()) {
          authUser.setConsentReadwriteSubjects(
              decodedJwt.getClaim("grouper_readwrite_subjects").asList(String.class));
        }

        if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.oauth.logAuthDebug", false)) {
          LOG.warn("MCP OAuth scope: readwrite=" + authUser.isConsentScopeReadwrite()
              + ", folders=" + authUser.getConsentReadwriteFolders()
              + ", groups=" + authUser.getConsentReadwriteGroups()
              + ", subjects=" + authUser.getConsentReadwriteSubjects());
        }

        // if readwrite data scope restrictions are enabled and user has readwrite consent,
        // mark that empty restriction lists mean "nothing allowed" (not "wide open")
        if (authUser.isConsentScopeReadwrite()
            && GrouperConfig.retrieveConfig().propertyValueBoolean(
                "grouper.mcp.oauth.requireReadwriteDataScope", true)) {
          authUser.setConsentReadwriteScopeRestricted(true);
        }

        // look up OAuth client to verify it still exists and get internal id for audit logging
        String clientId = decodedJwt.getClaim("client_id").asString();
        if (StringUtils.isNotBlank(clientId)) {
          try {
            GrouperOAuthClient oauthClient = GrouperOAuthStore.retrieveClient(clientId);
            if (oauthClient == null) {
              LOG.warn("MCP auth: OAuth client has been deleted: " + clientId + ", returning 401");
              response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
              response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
              return null;
            }
            authUser.setOauthClientInternalId(oauthClient.getInternalId());
          } catch (Exception e) {
            LOG.warn("Could not look up OAuth client: " + clientId, e);
          }
        }

        return authUser;
      }

      // Bearer token present but not a valid JWT
      LOG.warn("MCP auth: Bearer token present but JWT verification failed, returning 401");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
      return null;
    }

    // try HTTP Basic authentication
    if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Basic ")
        && GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.mcp.auth.httpBasic", false)) {
      boolean runGrouperWsWithBasicAuth = GrouperHibernateConfig.retrieveConfig()
          .propertyValueBoolean("grouper.is.ws.basicAuthn", false);
      if (runGrouperWsWithBasicAuth) {
        boolean isValid = new Authentication().authenticate(authHeader,
            edu.internet2.middleware.grouper.authentication.GrouperPassword.Application.WS,
            request.getRemoteAddr());
        if (isValid) {
          String userName = Authentication.retrieveUsername(authHeader);

          LOG.debug("MCP request authenticated via HTTP Basic: user=" + userName);

          request.setAttribute("REMOTE_USER", userName);

          GrouperMcpAuthUser authUser = resolveAuthUser(userName);
          if (authUser != null) {
            if (!isSubjectInGroup(authUser, "grouper.mcp.users.wsAuthnAllowed")) {
              LOG.warn("MCP access denied for WS-authenticated user (not in wsAuthnAllowed group): " + userName);
              response.setStatus(HttpServletResponse.SC_FORBIDDEN);
              return null;
            }
            return authUser;
          }
        }
      }
    }

    // try container auth
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.mcp.auth.container", false)) {
      String remoteUser = request.getRemoteUser();
      if (StringUtils.isBlank(remoteUser) && request.getUserPrincipal() != null) {
        remoteUser = request.getUserPrincipal().getName();
      }
      if (StringUtils.isNotBlank(remoteUser)) {

        LOG.debug("MCP request authenticated via container: user=" + remoteUser);

        request.setAttribute("REMOTE_USER", remoteUser);
        GrouperMcpAuthUser authUser = resolveAuthUser(remoteUser);
        if (authUser != null) {
          if (!isSubjectInGroup(authUser, "grouper.mcp.users.wsAuthnAllowed")) {
            LOG.warn("MCP access denied for WS-authenticated user (not in wsAuthnAllowed group): " + remoteUser);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
          }
          return authUser;
        }
      }
    }

    // try custom authentication class
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.mcp.auth.customAuthClass", false)) {
      String authenticationClassName = GrouperWsConfig.retrieveConfig().propertyValueString(
          GrouperWsConfig.WS_SECURITY_NON_RAMPART_AUTHENTICATION_CLASS, null);
      if (StringUtils.isNotBlank(authenticationClassName)) {
        try {
          Class<? extends WsCustomAuthentication> theClass = GrouperUtil.forName(authenticationClassName);
          WsCustomAuthentication wsAuthentication = GrouperUtil.newInstance(theClass);
          String userIdLoggedIn = wsAuthentication.retrieveLoggedInSubjectId(request);
          if (StringUtils.isNotBlank(userIdLoggedIn)) {

            LOG.debug("MCP request authenticated via custom auth: user=" + userIdLoggedIn);

            request.setAttribute("REMOTE_USER", userIdLoggedIn);
            GrouperMcpAuthUser authUser = resolveAuthUser(userIdLoggedIn);
            if (authUser != null) {
              if (!isSubjectInGroup(authUser, "grouper.mcp.users.wsAuthnAllowed")) {
                LOG.warn("MCP access denied for WS-authenticated user (not in wsAuthnAllowed group): " + userIdLoggedIn);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return null;
              }
              return authUser;
            }
          }
        } catch (Exception e) {
          LOG.warn("Error in custom WS authentication for MCP: " + e.getMessage(), e);
        }
      }
    }

    // no authentication succeeded
    String resourceMetadataUrl = request.getScheme() + "://" + request.getServerName();
    if (("http".equals(request.getScheme()) && request.getServerPort() != 80)
        || ("https".equals(request.getScheme()) && request.getServerPort() != 443)) {
      resourceMetadataUrl += ":" + request.getServerPort();
    }
    resourceMetadataUrl += request.getContextPath()
        + "/.well-known/oauth-protected-resource";

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setHeader("WWW-Authenticate",
        "Bearer resource_metadata=\"" + resourceMetadataUrl + "\"");
    return null;
  }

  /**
   * resolve a user id (which may contain sourceId::::subjectId) to a GrouperMcpAuthUser
   * holding the resolved Subject.
   * @param userId the user id, possibly in sourceId::::subjectId format
   * @return the auth user, or null if the subject cannot be resolved
   */
  private GrouperMcpAuthUser resolveAuthUser(String userId) {
    String subjectId = userId;
    String subjectSourceId = null;

    // check for sourceId::::subjectId format
    if (StringUtils.contains(userId, "::::")) {
      String[] parts = GrouperUtil.splitTrim(userId, "::::");
      subjectSourceId = parts[0];
      subjectId = parts[1];
    }

    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();

      Subject subject;
      if (StringUtils.isNotBlank(subjectSourceId)) {
        subject = SubjectFinder.findByIdOrIdentifierAndSource(subjectId, subjectSourceId, false);
      } else {
        // check default source config
        String defaultSourceId = StringUtils.trimToNull(
            GrouperWsConfig.retrieveConfig().propertyValueString(
                GrouperWsConfig.WS_LOGGED_IN_SUBJECT_DEFAULT_SOURCE));

        if (StringUtils.isNotBlank(defaultSourceId)) {
          subject = SubjectFinder.findByIdOrIdentifierAndSource(subjectId, defaultSourceId, false);
        } else {
          subject = SubjectFinder.findByIdOrIdentifier(subjectId, false);
        }
      }
      if (subject != null) {
        GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(subject);
        Member member = MemberFinder.findBySubject(grouperSession, subject, true);
        authUser.setMemberInternalId(member.getInternalId());
        return authUser;
      }
    } catch (Exception e) {
      LOG.warn("Error resolving subject for MCP auth: " + userId, e);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    LOG.error("Could not resolve subject for MCP auth: " + userId);
    return null;
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      String pathInfo = request.getPathInfo();
      // MCP clients may request well-known metadata under the MCP endpoint path
      if (pathInfo != null && pathInfo.contains(".well-known")) {
        GrouperMcpWellKnownServlet wellKnownServlet = new GrouperMcpWellKnownServlet();
        wellKnownServlet.doGet(request, response);
        return;
      }
      // server-initiated SSE stream not needed for this tool server
      response.setHeader("Allow", "POST, DELETE");
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    } catch (RuntimeException re) {
      LOG.error("Error in MCP doGet", re);
      throw re;
    }
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      String sessionId = request.getHeader(SESSION_ID_HEADER);
      if (StringUtils.isNotBlank(sessionId)) {
        LOG.info("MCP session terminated: " + sessionId);
      }
      response.setStatus(HttpServletResponse.SC_ACCEPTED);
    } catch (RuntimeException re) {
      LOG.error("Error in MCP doDelete", re);
      throw re;
    }
  }

  /**
   * handle the initialize method, create a session
   */
  private ObjectNode handleInitialize(JsonNode params, HttpServletResponse response) {
    String sessionId = UUID.randomUUID().toString();
    response.setHeader(SESSION_ID_HEADER, sessionId);

    LOG.info("MCP session initialized: " + sessionId);

    ObjectNode result = objectMapper.createObjectNode();
    result.put("protocolVersion", MCP_PROTOCOL_VERSION);

    ObjectNode capabilities = objectMapper.createObjectNode();
    ObjectNode tools = objectMapper.createObjectNode();
    tools.put("listChanged", false);
    capabilities.set("tools", tools);
    result.set("capabilities", capabilities);

    ObjectNode serverInfo = objectMapper.createObjectNode();
    serverInfo.put("name", SERVER_NAME);
    serverInfo.put("version", SERVER_VERSION);
    result.set("serverInfo", serverInfo);

    // include instructions for the AI client (default is in grouper.base.properties)
    String instructions = GrouperConfig.retrieveConfig()
        .propertyValueString("grouper.mcp.instructions");
    if (StringUtils.isNotBlank(instructions)) {
      result.put("instructions", instructions);
    }

    return result;
  }

  /**
   * handle tools/list - only include tools the user is authorized for
   * and that are allowed by the deployer's allow/deny configuration.
   * @param params the JSON-RPC params
   * @param authUser the authenticated user
   */
  private ObjectNode handleToolsList(JsonNode params, GrouperMcpAuthUser authUser) {
    ObjectNode result = objectMapper.createObjectNode();
    ArrayNode toolsArray = objectMapper.createArrayNode();

    // use callbackGrouperSession to put the authenticated MCP user's session
    // on the thread-local, consistent with handleToolsCall
    GrouperSession grouperSession = GrouperSession.start(authUser.getSubject(), false);
    try {
      GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {

          // readonly tools (readwrite implies readonly)
          if (hasReadonlyAccess(authUser)) {
            // only advertise doc_search if at least one source is available for this user
            if (GrouperMcpDocSearchIndex.hasAnySourcesForSubject(authUser.getSubject())) {
              addToolIfAllowed(toolsArray, GrouperMcpDocSearch.toolDefinition());
            }
            addToolIfAllowed(toolsArray, GrouperMcpFindAttributeDefNames.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpFindGroups.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpFindStems.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpGetAttributeAssignmentsLite.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpGetAuditEntries.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpGetGrouperPrivilegesLite.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpGetGroups.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpGetMembersLite.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpGetMemberships.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpGetSubjects.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpHasMember.toolDefinition());
            {
              ObjectNode institutionalToolDef = GrouperMcpInstitutionalTools.toolDefinition(authUser, hasReadwriteAccess(authUser));
              if (institutionalToolDef != null) {
                addToolIfAllowed(toolsArray, institutionalToolDef);
              }
            }
          }

          // readwrite tools
          if (hasReadwriteAccess(authUser)) {
            addToolIfAllowed(toolsArray, GrouperMcpAddMember.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpAssignAttributes.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpAssignGrouperPrivilegesLite.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpDeleteMember.toolDefinition());
            // group/folder-only tools require group or folder scope
            if (authUser.hasGroupOrFolderReadwriteScope()) {
              addToolIfAllowed(toolsArray, GrouperMcpFolderDelete.toolDefinition());
              addToolIfAllowed(toolsArray, GrouperMcpGroupDelete.toolDefinition());
              addToolIfAllowed(toolsArray, GrouperMcpGroupSave.toolDefinition());
            }
          }

          // SQL readonly tools
          if (hasSqlReadonlyAccess(authUser)) {
            addToolIfAllowed(toolsArray, GrouperMcpSqlGetSchema.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpSqlSelect.toolDefinition());
          }

          // admin readonly tools
          if (hasAdminReadonlyAccess(authUser)) {
            addToolIfAllowed(toolsArray, GrouperMcpAdminExternalSystemGet.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpAdminGetDaemonJobMessage.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpAdminGetDaemonJobs.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpAdminSearchConfigs.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpAdminSearchDaemons.toolDefinition());
            addToolIfAllowed(toolsArray, GrouperMcpLdapSearch.toolDefinition());
          }

          // admin readwrite tools
          if (hasAdminReadwriteAccess(authUser)) {
            addToolIfAllowed(toolsArray, GrouperMcpAdminRunDaemonJob.toolDefinition());
          }

          return null;
        }
      });
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    result.set("tools", toolsArray);
    return result;
  }

  /**
   * add a tool definition to the tools array only if the tool is allowed
   * by the deployer's allow/deny configuration.
   * @param toolsArray the array to add to
   * @param toolDef the tool definition from toolDefinition()
   */
  private static void addToolIfAllowed(ArrayNode toolsArray, ObjectNode toolDef) {
    String toolName = toolDef.get("name").asText();
    if (isToolAllowedByConfig(toolName)) {
      toolsArray.add(toolDef);
    }
  }

  /**
   * check if a tool is allowed by the deployer's allow/deny configuration.
   * the allow list (grouper.mcp.tools.allow) specifies which tools to allow;
   * blank means all tools are allowed.
   * the deny list (grouper.mcp.tools.deny) specifies which tools to deny;
   * blank means no tools are denied.
   * effective tools = allow minus deny.
   * @param toolName the tool name
   * @return true if the tool is allowed
   */
  private static boolean isToolAllowedByConfig(String toolName) {
    String allowList = StringUtils.trimToNull(
        GrouperConfig.retrieveConfig().propertyValueString("grouper.mcp.tools.allow"));
    String denyList = StringUtils.trimToNull(
        GrouperConfig.retrieveConfig().propertyValueString("grouper.mcp.tools.deny"));

    // check allow list (null/blank means all allowed)
    if (allowList != null) {
      boolean found = false;
      for (String allowed : GrouperUtil.splitTrim(allowList, ",")) {
        if (StringUtils.equals(allowed, toolName)) {
          found = true;
          break;
        }
      }
      if (!found) {
        return false;
      }
    }

    // check deny list (null/blank means none denied)
    if (denyList != null) {
      for (String denied : GrouperUtil.splitTrim(denyList, ",")) {
        if (StringUtils.equals(denied, toolName)) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * handle tools/call - wraps the tool dispatch with audit logging and throttle checking.
   * Every tool call is logged to the grouper_mcp_tool_log table with request, response,
   * timing, and error information.  Rate limits are checked before execution.
   * @param params the JSON-RPC params
   * @param authUser the authenticated user
   */
  private ObjectNode handleToolsCall(JsonNode params, GrouperMcpAuthUser authUser) {
    if (params == null || !params.has("name")) {
      return buildMcpErrorResult("Error: missing tool name");
    }

    String toolName = params.get("name").asText();
    JsonNode arguments = params.get("arguments");
    String toolCategory = GrouperMcpToolLog.getToolCategory(toolName);

    // --- throttle check ---
    String throttleError = GrouperMcpToolLogUtil.checkThrottle(authUser, toolCategory);
    if (throttleError != null) {
      String requestJson = arguments != null ? arguments.toString() : null;
      GrouperMcpToolLogUtil.logToolCall(authUser, toolName, toolCategory,
          requestJson, throttleError, true,
          System.currentTimeMillis() * 1000L, null);
      return buildMcpErrorResult(throttleError);
    }

    // --- execute and audit ---
    final long startedMicros = System.currentTimeMillis() * 1000L;
    final long startNanos = System.nanoTime();
    final String requestJson = arguments != null ? arguments.toString() : null;

    // Use callbackGrouperSession to put the authenticated MCP user's session
    // on the thread-local. This lets GrouperServiceUtils.retrieveGrouperSession()
    // find the session without going through the WS auth check (etc:wsGroup),
    // while still running as the authenticated user for object-level security.
    GrouperSession grouperSession = GrouperSession.start(authUser.getSubject(), false);
    final ObjectNode[] resultHolder = new ObjectNode[1];
    final boolean[] isErrorHolder = new boolean[] { false };
    final String[] responseTextHolder = new String[] { null };

    try {
      GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {

          try {
            resultHolder[0] = dispatchToolCall(toolName, arguments, authUser);

            // extract response text and error flag from the MCP result
            isErrorHolder[0] = resultHolder[0].has("isError")
                && resultHolder[0].get("isError").asBoolean(false);
            if (resultHolder[0].has("content") && resultHolder[0].get("content").isArray()
                && resultHolder[0].get("content").size() > 0) {
              JsonNode firstContent = resultHolder[0].get("content").get(0);
              if (firstContent.has("text")) {
                responseTextHolder[0] = firstContent.get("text").asText();
              }
            }

          } catch (Exception e) {
            isErrorHolder[0] = true;
            responseTextHolder[0] = "Internal error: " + e.getMessage();
            resultHolder[0] = buildMcpErrorResult(responseTextHolder[0]);
          }
          return null;
        }
      });
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    long durationMicros = (System.nanoTime() - startNanos) / 1000;

    // log to audit table (errors in logging do not affect the response)
    GrouperMcpToolLogUtil.logToolCall(authUser, toolName, toolCategory,
        requestJson, responseTextHolder[0], isErrorHolder[0], startedMicros, durationMicros);

    return resultHolder[0];
  }

  /**
   * dispatch a tool call to the appropriate handler, checking authorization
   * and the deployer's allow/deny configuration.
   * @param toolName the tool name
   * @param arguments the tool arguments
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  private ObjectNode dispatchToolCall(String toolName, JsonNode arguments,
      GrouperMcpAuthUser authUser) {

    // check deployer allow/deny configuration
    if (!isToolAllowedByConfig(toolName)) {
      return buildMcpErrorResult("Access denied: tool '" + toolName
          + "' is not allowed by server configuration.");
    }

    switch (toolName) {
      // readonly tools (alphabetical)
      case "attribute_def_name_find":
        if (!hasReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for attribute_def_name_find. "
              + "Membership in the MCP readonly or readwrite group is required.");
        }
        return GrouperMcpFindAttributeDefNames.execute(arguments, authUser);
      case "doc_search":
        if (!hasReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for doc_search. "
              + "Membership in the MCP readonly or readwrite group is required.");
        }
        return GrouperMcpDocSearch.execute(arguments, authUser);
      case "attribute_assignment_get":
        if (!hasReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for attribute_assignment_get. "
              + "Membership in the MCP readonly or readwrite group is required.");
        }
        return GrouperMcpGetAttributeAssignmentsLite.execute(arguments, authUser);
      case "audit_get":
        if (!hasReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for audit_get. "
              + "Membership in the MCP readonly or readwrite group is required.");
        }
        return GrouperMcpGetAuditEntries.execute(arguments, authUser);
      case "entity_get":
        if (!hasReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for entity_get. "
              + "Membership in the MCP readonly or readwrite group is required.");
        }
        return GrouperMcpGetSubjects.execute(arguments, authUser);
      case "entity_get_groups":
        if (!hasReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for entity_get_groups. "
              + "Membership in the MCP readonly or readwrite group is required.");
        }
        return GrouperMcpGetGroups.execute(arguments, authUser);
      case "folder_find":
        if (!hasReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for folder_find. "
              + "Membership in the MCP readonly or readwrite group is required.");
        }
        return GrouperMcpFindStems.execute(arguments, authUser);
      case "group_find":
        if (!hasReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for group_find. "
              + "Membership in the MCP readonly or readwrite group is required.");
        }
        return GrouperMcpFindGroups.execute(arguments, authUser);
      case "group_get_members":
        if (!hasReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for group_get_members. "
              + "Membership in the MCP readonly or readwrite group is required.");
        }
        return GrouperMcpGetMembersLite.execute(arguments, authUser);
      case "memberships_get":
        if (!hasReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for memberships_get. "
              + "Membership in the MCP readonly or readwrite group is required.");
        }
        return GrouperMcpGetMemberships.execute(arguments, authUser);
      case "group_has_member":
        if (!hasReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for group_has_member. "
              + "Membership in the MCP readonly or readwrite group is required.");
        }
        return GrouperMcpHasMember.execute(arguments, authUser);
      case "privilege_get":
        if (!hasReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for privilege_get. "
              + "Membership in the MCP readonly or readwrite group is required.");
        }
        return GrouperMcpGetGrouperPrivilegesLite.execute(arguments, authUser);
      // readwrite tools (alphabetical)
      case "attribute_assignment_save":
        if (!hasReadwriteAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for attribute_assignment_save. "
              + "Membership in the MCP readwrite group is required.");
        }
        return GrouperMcpAssignAttributes.execute(arguments, authUser);
      case "group_add_member":
        if (!hasReadwriteAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for group_add_member. "
              + "Membership in the MCP readwrite group is required.");
        }
        return GrouperMcpAddMember.execute(arguments, authUser);
      case "folder_delete":
        if (!hasReadwriteAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for folder_delete. "
              + "Membership in the MCP readwrite group is required.");
        }
        if (!authUser.hasGroupOrFolderReadwriteScope()) {
          return buildMcpErrorResult("Access denied: your OAuth scope does not include groups or folders.");
        }
        return GrouperMcpFolderDelete.execute(arguments, authUser);
      case "group_delete":
        if (!hasReadwriteAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for group_delete. "
              + "Membership in the MCP readwrite group is required.");
        }
        if (!authUser.hasGroupOrFolderReadwriteScope()) {
          return buildMcpErrorResult("Access denied: your OAuth scope does not include groups or folders.");
        }
        return GrouperMcpGroupDelete.execute(arguments, authUser);
      case "group_remove_member":
        if (!hasReadwriteAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for group_remove_member. "
              + "Membership in the MCP readwrite group is required.");
        }
        return GrouperMcpDeleteMember.execute(arguments, authUser);
      case "group_save":
        if (!hasReadwriteAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for group_save. "
              + "Membership in the MCP readwrite group is required.");
        }
        if (!authUser.hasGroupOrFolderReadwriteScope()) {
          return buildMcpErrorResult("Access denied: your OAuth scope does not include groups or folders.");
        }
        return GrouperMcpGroupSave.execute(arguments, authUser);
      case "privilege_assign":
        if (!hasReadwriteAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for privilege_assign. "
              + "Membership in the MCP readwrite group is required.");
        }
        return GrouperMcpAssignGrouperPrivilegesLite.execute(arguments, authUser);
      case "institutional_tools":
        if (!hasReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for institutional_tools. "
              + "Membership in the MCP readonly or readwrite group is required.");
        }
        return GrouperMcpInstitutionalTools.execute(arguments, authUser, hasReadwriteAccess(authUser));
      // SQL readonly tools (alphabetical)
      case "sql_get_schema":
        if (!hasSqlReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for sql_get_schema. "
              + "Membership in the MCP SQL readonly group is required.");
        }
        return GrouperMcpSqlGetSchema.execute(arguments, authUser);
      case "sql_select":
        if (!hasSqlReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for sql_select. "
              + "Membership in the MCP SQL readonly group is required.");
        }
        return GrouperMcpSqlSelect.execute(arguments, authUser);
      case "sql_select_count":
        // backward compatibility: route to sql_select with countOnly=true
        if (!hasSqlReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for sql_select_count. "
              + "Membership in the MCP SQL readonly group is required.");
        }
        if (arguments != null && arguments.isObject()) {
          ((ObjectNode) arguments).put("countOnly", true);
        }
        return GrouperMcpSqlSelect.execute(arguments, authUser);
      // admin readonly tools (alphabetical)
      case "admin_config_search":
        if (!hasAdminReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for admin_config_search. "
              + "Membership in the MCP admin readonly group is required.");
        }
        return GrouperMcpAdminSearchConfigs.execute(arguments, authUser);
      case "admin_external_system_get":
        if (!hasAdminReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for admin_external_system_get. "
              + "Membership in the MCP admin readonly group is required.");
        }
        return GrouperMcpAdminExternalSystemGet.execute(arguments, authUser);
      case "admin_daemon_job_message":
        if (!hasAdminReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for admin_daemon_job_message. "
              + "Membership in the MCP admin readonly group is required.");
        }
        return GrouperMcpAdminGetDaemonJobMessage.execute(arguments, authUser);
      case "admin_daemon_logs":
        if (!hasAdminReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for admin_daemon_logs. "
              + "Membership in the MCP admin readonly group is required.");
        }
        return GrouperMcpAdminGetDaemonJobs.execute(arguments, authUser);
      case "admin_daemon_names":
        if (!hasAdminReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for admin_daemon_names. "
              + "Membership in the MCP admin readonly group is required.");
        }
        return GrouperMcpAdminSearchDaemons.execute(arguments, authUser);
      case "ldap":
        if (!hasAdminReadonlyAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for ldap. "
              + "Membership in the MCP admin readonly group is required.");
        }
        return GrouperMcpLdapSearch.execute(arguments, authUser);
      // admin readwrite tools (alphabetical)
      case "admin_daemon_job_run":
        if (!hasAdminReadwriteAccess(authUser)) {
          return buildMcpErrorResult("Access denied: user is not authorized for admin_daemon_job_run. "
              + "Membership in the MCP admin readwrite group is required.");
        }
        return GrouperMcpAdminRunDaemonJob.execute(arguments, authUser);
      default:
        return buildMcpErrorResult("Error: unknown tool: " + toolName);
    }
  }

  /**
   * check if the authenticated user has readonly access.
   * readwrite group membership also grants readonly access.
   * checks actual group membership so removal from the
   * group takes effect immediately even with an unexpired token.
   * wheel group membership does NOT grant access.
   * <p>For OAuth-authenticated users, the user must also have consented
   * to the readonly (or readwrite) scope on the consent page.  This
   * means the JWT must contain the corresponding scope claim.</p>
   * @param authUser the authenticated user
   * @return true if the user is authorized for readonly operations
   */
  private boolean hasReadonlyAccess(GrouperMcpAuthUser authUser) {

    // check group membership (readwrite group also grants readonly access)
    if (!isSubjectInGroup(authUser, "grouper.mcp.users.readonly")
        && !isSubjectInGroup(authUser, "grouper.mcp.users.readwrite")) {
      return false;
    }

    // for OAuth users, require the readonly or readwrite consent scope
    if (authUser.isOAuthAuthenticated()
        && !authUser.isConsentScopeReadonly() && !authUser.isConsentScopeReadwrite()) {
      return false;
    }

    return true;
  }

  /**
   * check if the authenticated user is a member of the MCP readwrite group.
   * checks actual group membership so removal from the
   * group takes effect immediately even with an unexpired token.
   * wheel group membership does NOT grant access.
   * <p>For OAuth-authenticated users, the user must also have consented
   * to the readwrite scope on the consent page.  This means the JWT must
   * contain the grouper_readwrite claim.</p>
   * @param authUser the authenticated user
   * @return true if the user is authorized for readwrite operations
   */
  private boolean hasReadwriteAccess(GrouperMcpAuthUser authUser) {

    // check group membership
    if (!isSubjectInGroup(authUser, "grouper.mcp.users.readwrite")) {
      return false;
    }

    // for OAuth users, also require the readwrite consent scope
    if (authUser.isOAuthAuthenticated() && !authUser.isConsentScopeReadwrite()) {
      return false;
    }

    return true;
  }

  /**
   * check if the authenticated user is a member of the MCP admin readonly group.
   * checks actual group membership so removal from the
   * group takes effect immediately even with an unexpired token.
   * wheel group membership does NOT grant access.
   * <p>Admin readwrite access implies admin readonly access.</p>
   * <p>For OAuth-authenticated users, the user must also have consented
   * to the admin readonly (or admin readwrite) scope on the consent page.
   * This means the JWT must contain the corresponding scope claim.</p>
   * <p>The user must also be a Grouper sysadmin or readonly sysadmin.</p>
   * @param authUser the authenticated user
   * @return true if the user is authorized for admin readonly operations
   */
  private boolean hasAdminReadonlyAccess(GrouperMcpAuthUser authUser) {

    // check group membership (admin readwrite group also grants admin readonly access)
    if (!isSubjectInGroup(authUser, "grouper.mcp.users.adminReadonly")
        && !isSubjectInGroup(authUser, "grouper.mcp.users.adminReadWrite")) {
      return false;
    }

    // for OAuth users, require the admin readonly or admin readwrite consent scope
    if (authUser.isOAuthAuthenticated()
        && !authUser.isConsentScopeAdminReadonly() && !authUser.isConsentScopeAdminReadwrite()) {
      return false;
    }

    return true;
  }

  /**
   * check if the authenticated user is a member of the MCP admin readwrite group.
   * checks actual group membership so removal from the
   * group takes effect immediately even with an unexpired token.
   * wheel group membership does NOT grant access.
   * <p>For OAuth-authenticated users, the user must also have consented
   * to the admin readwrite scope on the consent page.  This means the JWT must
   * contain the grouper_admin_readwrite claim.</p>
   * <p>The user must also be a Grouper sysadmin.</p>
   * @param authUser the authenticated user
   * @return true if the user is authorized for admin readwrite operations
   */
  private boolean hasAdminReadwriteAccess(GrouperMcpAuthUser authUser) {

    // check group membership
    if (!isSubjectInGroup(authUser, "grouper.mcp.users.adminReadWrite")) {
      return false;
    }

    // for OAuth users, also require the admin readwrite consent scope
    if (authUser.isOAuthAuthenticated() && !authUser.isConsentScopeAdminReadwrite()) {
      return false;
    }

    return true;
  }

  /**
   * check if the authenticated user is a member of the MCP SQL readonly group.
   * checks actual group membership so removal from the
   * group takes effect immediately even with an unexpired token.
   * wheel group membership does NOT grant access.
   * <p>For OAuth-authenticated users, the user must also have consented
   * to the SQL readonly scope on the consent page.  This means the JWT must
   * contain the grouper_sql_readonly claim.</p>
   * @param authUser the authenticated user
   * @return true if the user is authorized for SQL readonly operations
   */
  private boolean hasSqlReadonlyAccess(GrouperMcpAuthUser authUser) {

    // check group membership
    if (!isSubjectInGroup(authUser, "grouper.mcp.users.canRunSqlReadonly")) {
      return false;
    }

    // for OAuth users, also require the SQL readonly consent scope
    if (authUser.isOAuthAuthenticated() && !authUser.isConsentScopeSqlReadonly()) {
      return false;
    }

    return true;
  }

  // ----------------------------------------------------------------
  /**
   * check if the authenticated user is a member of a configured group.
   * results are cached for 60 seconds (see subjectInGroupCache).
   * @param authUser the authenticated user
   * @param groupPropertyName the grouper.properties key for the group name
   * @return true if the user is in the group
   */
  private boolean isSubjectInGroup(GrouperMcpAuthUser authUser, String groupPropertyName) {
    String groupName = GrouperConfig.retrieveConfig().propertyValueString(groupPropertyName);
    if (StringUtils.isBlank(groupName)) {
      return false;
    }

    Subject subject = authUser.getSubject();

    // check cache first
    MultiKey cacheKey = new MultiKey(subject.getId(),
        StringUtils.defaultString(subject.getSourceId()), groupPropertyName);
    Boolean cachedResult = subjectInGroupCache.get(cacheKey);
    if (cachedResult != null) {
      return cachedResult;
    }

    // cache miss - check group membership
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      Group group = GroupFinder.findByName(grouperSession, groupName, false);
      if (group == null) {
        subjectInGroupCache.put(cacheKey, false);
        return false;
      }
      boolean isMember = group.hasMember(subject);
      subjectInGroupCache.put(cacheKey, isMember);
      return isMember;
    } catch (Exception e) {
      LOG.error("Error checking group membership for MCP access: " + groupPropertyName, e);
      return false;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * build an MCP tool error result
   * @param errorMessage the error message
   * @return the error result
   */
  private static ObjectNode buildMcpErrorResult(String errorMessage) {
    ObjectNode errorResult = objectMapper.createObjectNode();
    ArrayNode content = objectMapper.createArrayNode();
    ObjectNode textContent = objectMapper.createObjectNode();
    textContent.put("type", "text");
    textContent.put("text", errorMessage);
    content.add(textContent);
    errorResult.set("content", content);
    errorResult.put("isError", true);
    return errorResult;
  }

  /**
   * send a JSON-RPC error response
   */
  private void sendJsonRpcError(HttpServletResponse response, JsonNode id,
      int code, String message) throws IOException {
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.mcp.logClientErrors", false)) {
      LOG.warn("MCP client error (code " + code + "): " + message);
    }
    ObjectNode jsonRpcResponse = objectMapper.createObjectNode();
    jsonRpcResponse.put("jsonrpc", "2.0");
    if (id != null) {
      jsonRpcResponse.set("id", id);
    } else {
      jsonRpcResponse.putNull("id");
    }
    ObjectNode error = objectMapper.createObjectNode();
    error.put("code", code);
    error.put("message", message);
    jsonRpcResponse.set("error", error);

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().write(objectMapper.writeValueAsString(jsonRpcResponse));
    response.getWriter().flush();
  }
}
