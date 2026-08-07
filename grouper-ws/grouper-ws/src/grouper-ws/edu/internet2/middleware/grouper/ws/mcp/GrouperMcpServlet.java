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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

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
 * <p>{@code server/discover} is answered so that a client on spec version 2026-07-28, which has
 * no initialize handshake, can find out which protocol versions this server speaks instead of
 * getting a "method not found" it cannot interpret. This server implements the 2025-03-26
 * semantics and accepts requests declaring any of the handshake based revisions, so those are
 * the versions it advertises.</p>
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

  /**
   * the protocol version which has no initialize handshake, and instead has each request carry
   * its own protocol version and client capabilities and mirror some body fields into HTTP
   * headers.  a request declaring this version is validated and served differently to one from
   * a client on any of the earlier, handshake based revisions.
   */
  private static final String MODERN_PROTOCOL_VERSION = "2026-07-28";

  /**
   * protocol versions this server accepts on a request, newest first.
   *
   * <p>These all establish a session with an initialize handshake and use the request and
   * response shapes this server implements. What the later two revisions added is advertised
   * through capabilities rather than changing the transport, so a request labelled with any of
   * them is served the same way and gets the same tools capability. Listing them means this
   * server will accept and serve such a request, not that it implements every feature those
   * revisions introduced.</p>
   *
   * <p>{@link #MCP_PROTOCOL_VERSION} remains the version answered to initialize.</p>
   */
  private static final String[] SUPPORTED_PROTOCOL_VERSIONS = new String[] {
      MODERN_PROTOCOL_VERSION, "2025-11-25", "2025-06-18", MCP_PROTOCOL_VERSION};

  private static final String SERVER_NAME = "grouper-mcp-server";

  private static final String SERVER_VERSION = "1.0.0";

  private static final String SESSION_ID_HEADER = "Mcp-Session-Id";

  /** HTTP header carrying the protocol version of a request, added in spec version 2025-06-18 */
  private static final String PROTOCOL_VERSION_HEADER = "MCP-Protocol-Version";

  /**
   * key in the request's _meta carrying the protocol version, added in spec version 2026-07-28
   * when the initialize handshake was removed and every request became self describing
   */
  private static final String META_PROTOCOL_VERSION = "io.modelcontextprotocol/protocolVersion";

  /**
   * key in a result's _meta where the server identifies itself, added in spec version
   * 2026-07-28 since there is no longer an initialize handshake to carry it
   */
  private static final String META_SERVER_INFO = "io.modelcontextprotocol/serverInfo";

  /**
   * key in a request's _meta carrying what the client supports.  required on every request in
   * spec version 2026-07-28, since without a handshake there is nowhere else to declare it
   */
  private static final String META_CLIENT_CAPABILITIES =
      "io.modelcontextprotocol/clientCapabilities";

  /** HTTP header mirroring the method of the request body */
  private static final String MCP_METHOD_HEADER = "Mcp-Method";

  /** HTTP header mirroring the name of the tool, resource or prompt the request is for */
  private static final String MCP_NAME_HEADER = "Mcp-Name";

  /** marks the start of a header value which is base64 encoded rather than plain */
  private static final String BASE64_SENTINEL_PREFIX = "=?base64?";

  /** marks the end of a header value which is base64 encoded rather than plain */
  private static final String BASE64_SENTINEL_SUFFIX = "?=";

  /** JSON-RPC error code for headers which do not agree with the request body */
  private static final int ERROR_HEADER_MISMATCH = -32020;

  /** JSON-RPC error code for a protocol version this server does not implement */
  private static final int ERROR_UNSUPPORTED_PROTOCOL_VERSION = -32022;

  /** JSON-RPC error code for params which are not shaped the way the method requires */
  private static final int ERROR_INVALID_PARAMS = -32602;

  /** HTTP header a browser sends identifying the page a request came from */
  private static final String ORIGIN_HEADER = "Origin";

  /**
   * pattern to find all grouper.mcp.allowedOrigin.{configId}.regex config keys
   */
  private static final Pattern ALLOWED_ORIGIN_CONFIG_PATTERN = Pattern.compile(
      "^grouper\\.mcp\\.allowedOrigin\\.([^.]+)\\.regex$");

  /**
   * how long a client may consider a tools/list result fresh, in milliseconds.  the tool list
   * changes when a user's MCP group memberships change, and those memberships are themselves
   * cached on the server for 60 seconds, so a shorter value here would not make a change take
   * effect any sooner.
   */
  private static final int TOOLS_LIST_TTL_MS = 60000;

  /**
   * how long a client may consider a server/discover result fresh, in milliseconds.  what it
   * returns only changes when Grouper is upgraded or reconfigured.
   */
  private static final int DISCOVER_TTL_MS = 3600000;

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

    // check the origin before anything else, so credentials from a page this server does not
    // recognize are not processed at all
    if (rejectIfOriginNotAllowed(request, response)) {
      return;
    }

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

    if (rejectIfProtocolVersionNotSupported(request, response, params, id)) {
      return;
    }

    // a request which declares spec version 2026-07-28 has to carry what that revision
    // requires.  a request from any of the handshake based revisions carries none of it and is
    // served the way this server has always served requests
    boolean modernRequest = isModernProtocolVersion(declaredProtocolVersion(request, params));

    if (modernRequest && rejectIfModernRequestInvalid(request, response, method, params, id)) {
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
        case "server/discover":
          result = handleServerDiscover();
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
          // Spec version 2026-07-28 answers an unknown method with HTTP 404, where the earlier
          // revisions use HTTP 200.  The status on its own is ambiguous, since a server which
          // hosts no MCP endpoint at this path would answer 404 as well.  What tells the two
          // apart is the JSON-RPC error body sent below: a client which is working out what it
          // is talking to reads a 404 carrying one as "this is an MCP server which does not
          // implement that method", and a 404 without one as "this is not an MCP endpoint".
          sendJsonRpcError(response, id, -32601, "Method not found: " + method, null,
              modernRequest ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_OK);
          return;
      }
    } catch (Exception e) {
      LOG.error("Error handling MCP method: " + method, e);
      sendJsonRpcError(response, id, -32603, "Internal error: " + e.getMessage());
      return;
    }

    // Spec version 2026-07-28 requires a result type on every result.  "complete" means the
    // request finished and this is the final content, which is the only kind of result this
    // server produces; the other value, "input_required", is for servers which ask the client
    // for more information part way through a request.  This is set here rather than in each
    // tool so that every method and every tool gets it.  Clients on spec version 2025-03-26
    // ignore fields they do not know about.
    if (!result.has("resultType")) {
      result.put("resultType", "complete");
    }

    // Spec version 2026-07-28 has the server identify itself in every result rather than only
    // in the initialize response, since there is no longer a handshake to carry it.  set here
    // so that every method and every tool gets it, and without disturbing a result which
    // already put something in its _meta.
    addServerInfoToMeta(result);

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
   * add this server's name and version to the _meta of a result, so that a client which has
   * not been through an initialize handshake still knows what it is talking to.  the _meta
   * object is created if the result does not have one, and an entry which is already there is
   * left alone.
   * @param result the result to add to
   */
  private static void addServerInfoToMeta(ObjectNode result) {

    ObjectNode meta = null;
    if (result.has("_meta") && result.get("_meta").isObject()) {
      meta = (ObjectNode)result.get("_meta");
    } else {
      meta = objectMapper.createObjectNode();
      result.set("_meta", meta);
    }

    if (meta.has(META_SERVER_INFO)) {
      return;
    }

    ObjectNode serverInfo = objectMapper.createObjectNode();
    serverInfo.put("name", SERVER_NAME);
    serverInfo.put("version", SERVER_VERSION);
    meta.set(META_SERVER_INFO, serverInfo);
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
   * reject a request whose Origin header this server does not allow.
   *
   * <p>This is protection against DNS rebinding. An attacker's web page cannot read this
   * server's address directly, but it can make the name it was served from resolve to this
   * server and have the victim's browser post here. The browser sends the attacker's page as
   * the Origin, so refusing origins which are not recognized stops that.</p>
   *
   * <p>Only a browser sends an Origin header, so a request without one, such as from a command
   * line client or another server, is not what this protects against and is allowed. A request
   * whose Origin is this server itself is also allowed, since that is not cross origin.</p>
   *
   * <p>Any other origin must match a configured
   * {@code grouper.mcp.allowedOrigin.<configId>.regex} pattern. If no patterns are configured
   * then no cross origin browser request is allowed, which is the safe default and means a
   * deployment has to opt in to browser based clients rather than opt out.</p>
   *
   * @param request the HTTP request
   * @param response the HTTP response
   * @return true if the request was rejected and a response has already been sent
   * @throws IOException if the response cannot be written
   */
  private boolean rejectIfOriginNotAllowed(HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    String origin = StringUtils.trimToNull(request.getHeader(ORIGIN_HEADER));

    // not a browser request
    if (origin == null) {
      return false;
    }

    // the page was served by this server, so this is not cross origin
    if (origin.equals(requestOrigin(request))) {
      return false;
    }

    Map<String, String> regexMap = GrouperConfig.retrieveConfig()
        .propertiesMap(ALLOWED_ORIGIN_CONFIG_PATTERN);

    for (Map.Entry<String, String> regexEntry : GrouperUtil.nonNull(regexMap).entrySet()) {
      String regex = regexEntry.getValue();
      if (StringUtils.isBlank(regex)) {
        continue;
      }
      try {
        if (Pattern.matches(regex, origin)) {
          return false;
        }
      } catch (Exception e) {
        LOG.error("Invalid regex in config key '" + regexEntry.getKey() + "': " + regex, e);
      }
    }

    LOG.warn("MCP request rejected, Origin not allowed: '" + origin + "', remoteAddr="
        + request.getRemoteAddr() + ". Configure grouper.mcp.allowedOrigin.<configId>.regex "
        + "to allow browser based clients from this origin.");

    // no id, this is rejected before the body is read
    sendJsonRpcError(response, null, -32600, "Origin not allowed", null,
        HttpServletResponse.SC_FORBIDDEN);
    return true;
  }

  /**
   * the origin of this server as a browser would compute it for the request, that is the
   * scheme, host and port, with the port left off when it is the default for the scheme
   * @param request the HTTP request
   * @return the origin
   */
  private static String requestOrigin(HttpServletRequest request) {

    StringBuilder origin = new StringBuilder();
    origin.append(request.getScheme()).append("://").append(request.getServerName());

    if (("http".equals(request.getScheme()) && request.getServerPort() != 80)
        || ("https".equals(request.getScheme()) && request.getServerPort() != 443)) {
      origin.append(":").append(request.getServerPort());
    }

    return origin.toString();
  }

  /**
   * validate a request from a client on spec version 2026-07-28, and reject it if it does not
   * carry what that revision requires.
   *
   * <p>Only called for a request which declares that version. Clients on the handshake based
   * revisions send none of this and must not be held to it.</p>
   *
   * <p>Two things are checked. The request must declare what the client supports, as an object,
   * since with no handshake there is nowhere else it could have been said. And the HTTP headers
   * which mirror fields of the request body must all be present and must agree with the body,
   * so that something in the network routing on a header cannot disagree with what this server
   * acts on.</p>
   *
   * @param request the HTTP request
   * @param response the HTTP response
   * @param method the JSON-RPC method
   * @param params the JSON-RPC params, may be null
   * @param id the JSON-RPC id of the request
   * @return true if the request was rejected and a response has already been sent
   * @throws IOException if the response cannot be written
   */
  private boolean rejectIfModernRequestInvalid(HttpServletRequest request,
      HttpServletResponse response, String method, JsonNode params, JsonNode id)
      throws IOException {

    JsonNode metaNode = params == null ? null : params.get("_meta");

    JsonNode clientCapabilitiesNode = metaNode == null ? null
        : metaNode.get(META_CLIENT_CAPABILITIES);

    // a field explicitly set to null says no more than leaving it out does, so both are treated
    // as not declared.  note this cannot be written with has(), which counts a field set to
    // null as present
    if (clientCapabilitiesNode == null || clientCapabilitiesNode.isNull()) {
      sendJsonRpcError(response, id, ERROR_INVALID_PARAMS,
          "Invalid params: " + META_CLIENT_CAPABILITIES + " is required in _meta",
          null, HttpServletResponse.SC_BAD_REQUEST);
      return true;
    }

    // what the client supports is a set of named capabilities, so a string, a number or an
    // array cannot be read as one however well formed it is on its own
    if (!clientCapabilitiesNode.isObject()) {
      sendJsonRpcError(response, id, ERROR_INVALID_PARAMS,
          "Invalid params: " + META_CLIENT_CAPABILITIES + " in _meta must be an object",
          null, HttpServletResponse.SC_BAD_REQUEST);
      return true;
    }

    // Spec version 2026-07-28 requires this header on every request.  A request carrying no
    // version at all is served as spec version 2025-03-26, which had no such header, but this
    // request declared 2026-07-28 in its body and so is held to that revision's rules.  Any
    // disagreement between the header and the body was already caught before this point, so
    // what is left to check here is the header being absent.
    if (protocolVersionFromHeader(request) == null) {
      return rejectHeaderMismatch(response, id,
          "required header " + PROTOCOL_VERSION_HEADER + " is missing");
    }

    String methodHeader = StringUtils.trimToNull(request.getHeader(MCP_METHOD_HEADER));

    if (methodHeader == null) {
      return rejectHeaderMismatch(response, id,
          "required header " + MCP_METHOD_HEADER + " is missing");
    }

    if (!methodHeader.equals(method)) {
      return rejectHeaderMismatch(response, id, MCP_METHOD_HEADER + " header value '"
          + methodHeader + "' does not match body value '" + method + "'");
    }

    // the name header is only required for the methods which name what they act on, and of
    // those this server only implements tools/call
    if ("tools/call".equals(method)) {

      String nameHeader = decodeHeaderValue(
          StringUtils.trimToNull(request.getHeader(MCP_NAME_HEADER)));

      String nameFromBody = params != null && params.has("name")
          ? params.get("name").asText() : null;

      if (nameHeader == null) {
        return rejectHeaderMismatch(response, id,
            "required header " + MCP_NAME_HEADER + " is missing");
      }

      if (!nameHeader.equals(nameFromBody)) {
        return rejectHeaderMismatch(response, id, MCP_NAME_HEADER + " header value '"
            + nameHeader + "' does not match body value '" + nameFromBody + "'");
      }
    }

    return false;
  }

  /**
   * reject a request whose headers do not agree with its body
   * @param response the HTTP response
   * @param id the JSON-RPC id of the request
   * @param detail what did not agree
   * @return true always, so callers can return this directly
   * @throws IOException if the response cannot be written
   */
  private boolean rejectHeaderMismatch(HttpServletResponse response, JsonNode id, String detail)
      throws IOException {
    sendJsonRpcError(response, id, ERROR_HEADER_MISMATCH, "Header mismatch: " + detail,
        null, HttpServletResponse.SC_BAD_REQUEST);
    return true;
  }

  /**
   * decode a header value which the client base64 encoded because it could not be sent as
   * plain ASCII, for example a tool name with an accent in it.  a value which is not marked as
   * encoded is returned as it is.
   * @param headerValue the raw header value, may be null
   * @return the decoded value, or the original if it is not encoded or cannot be decoded
   */
  private static String decodeHeaderValue(String headerValue) {

    if (headerValue == null || !headerValue.startsWith(BASE64_SENTINEL_PREFIX)
        || !headerValue.endsWith(BASE64_SENTINEL_SUFFIX)
        || headerValue.length() < BASE64_SENTINEL_PREFIX.length()
            + BASE64_SENTINEL_SUFFIX.length()) {
      return headerValue;
    }

    String encoded = headerValue.substring(BASE64_SENTINEL_PREFIX.length(),
        headerValue.length() - BASE64_SENTINEL_SUFFIX.length());

    try {
      return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    } catch (Exception e) {
      // leave it as it was, it will not match the body and the request is rejected
      LOG.warn("Could not base64 decode an MCP header value: " + headerValue);
      return headerValue;
    }
  }

  /**
   * the protocol version from the HTTP header, null if the client did not send one
   * @param request the HTTP request
   * @return the version or null
   */
  private static String protocolVersionFromHeader(HttpServletRequest request) {
    return StringUtils.trimToNull(request.getHeader(PROTOCOL_VERSION_HEADER));
  }

  /**
   * the protocol version from the request body's _meta, null if the client did not send one
   * @param params the JSON-RPC params, may be null
   * @return the version or null
   */
  private static String protocolVersionFromMeta(JsonNode params) {
    if (params == null || !params.has("_meta")) {
      return null;
    }
    JsonNode metaNode = params.get("_meta");
    if (metaNode == null || !metaNode.has(META_PROTOCOL_VERSION)) {
      return null;
    }
    return StringUtils.trimToNull(metaNode.get(META_PROTOCOL_VERSION).asText());
  }

  /**
   * the protocol version a request declares, preferring the body since that is where spec
   * version 2026-07-28 puts it
   * @param request the HTTP request
   * @param params the JSON-RPC params, may be null
   * @return the version, or null if the request declares none
   */
  private static String declaredProtocolVersion(HttpServletRequest request, JsonNode params) {
    String versionFromBody = protocolVersionFromMeta(params);
    return versionFromBody != null ? versionFromBody : protocolVersionFromHeader(request);
  }

  /**
   * check if a request is from a client on the revision which carries per-request metadata and
   * mirrors body fields into HTTP headers.  requests from the earlier, handshake based
   * revisions must not be held to those rules, since their clients do not send any of it.
   * @param protocolVersion the version the request declares, may be null
   * @return true if the request is on spec version 2026-07-28
   */
  private static boolean isModernProtocolVersion(String protocolVersion) {
    return MODERN_PROTOCOL_VERSION.equals(protocolVersion);
  }

  /**
   * check if this server accepts requests declaring a protocol version
   * @param protocolVersion the version from the request
   * @return true if supported
   */
  private static boolean isProtocolVersionSupported(String protocolVersion) {
    for (String supportedVersion : SUPPORTED_PROTOCOL_VERSIONS) {
      if (supportedVersion.equals(protocolVersion)) {
        return true;
      }
    }
    return false;
  }

  /**
   * the protocol versions this server accepts, as a JSON array
   * @return the array node
   */
  private static ArrayNode supportedProtocolVersionsArrayNode() {
    ArrayNode supportedVersions = objectMapper.createArrayNode();
    for (String supportedVersion : SUPPORTED_PROTOCOL_VERSIONS) {
      supportedVersions.add(supportedVersion);
    }
    return supportedVersions;
  }

  /**
   * check the protocol version a request declares, and reject it if this server does not
   * implement that version.
   *
   * <p>Spec version 2025-06-18 added the {@code MCP-Protocol-Version} HTTP header, and spec
   * version 2026-07-28 additionally carries the version in the request body's {@code _meta}, so
   * that a request is self describing and no handshake is needed. A request with neither is
   * from a client on spec version 2025-03-26, which had neither, and is served the way this
   * server has always served requests.</p>
   *
   * <p>A client which asks for a version this server does not implement is told which versions
   * it does implement, so the client can retry with one of those rather than guess. Without
   * this the request would be served under this server's own semantics, which the spec calls
   * out as a failure mode because the client cannot tell that happened.</p>
   *
   * @param request the HTTP request
   * @param response the HTTP response
   * @param params the JSON-RPC params, may be null
   * @param id the JSON-RPC id of the request
   * @return true if the request was rejected and a response has already been sent
   * @throws IOException if the response cannot be written
   */
  private boolean rejectIfProtocolVersionNotSupported(HttpServletRequest request,
      HttpServletResponse response, JsonNode params, JsonNode id) throws IOException {

    String versionFromHeader = protocolVersionFromHeader(request);
    String versionFromBody = protocolVersionFromMeta(params);

    // neither present, this is a client on spec version 2025-03-26
    if (versionFromHeader == null && versionFromBody == null) {
      return false;
    }

    // when a client sends both they must agree.  if they disagree, something in the network
    // could route on one value while this server acts on the other
    if (versionFromHeader != null && versionFromBody != null
        && !versionFromHeader.equals(versionFromBody)) {
      sendJsonRpcError(response, id, ERROR_HEADER_MISMATCH,
          "Header mismatch: " + PROTOCOL_VERSION_HEADER + " header value '" + versionFromHeader
          + "' does not match body value '" + versionFromBody + "'",
          null, HttpServletResponse.SC_BAD_REQUEST);
      return true;
    }

    String protocolVersion = versionFromBody != null ? versionFromBody : versionFromHeader;

    if (isProtocolVersionSupported(protocolVersion)) {
      return false;
    }

    ObjectNode data = objectMapper.createObjectNode();
    data.set("supported", supportedProtocolVersionsArrayNode());
    data.put("requested", protocolVersion);

    sendJsonRpcError(response, id, ERROR_UNSUPPORTED_PROTOCOL_VERSION,
        "Unsupported protocol version", data, HttpServletResponse.SC_BAD_REQUEST);
    return true;
  }

  /**
   * handle the server/discover method, which advertises the protocol versions, capabilities and
   * identity of this server.
   *
   * <p>Spec version 2026-07-28 removed the initialize handshake, so a client on that revision
   * has no other way to find out what this server speaks. Servers on that revision MUST
   * implement this method. It is answered here even though this server implements the older
   * revision, so that a client which supports both eras gets a definite answer -- the supported
   * versions listed below -- instead of a "method not found" it has to guess about.</p>
   *
   * <p>The result is the same for every caller, so nothing here is filtered by the
   * authenticated user.</p>
   *
   * @return the discover result
   */
  private ObjectNode handleServerDiscover() {

    ObjectNode result = objectMapper.createObjectNode();

    // results carry a result type in spec version 2026-07-28 and later.  clients on earlier
    // revisions never call this method, so this cannot confuse them
    result.put("resultType", "complete");

    result.set("supportedVersions", supportedProtocolVersionsArrayNode());

    ObjectNode capabilities = objectMapper.createObjectNode();
    ObjectNode tools = objectMapper.createObjectNode();
    tools.put("listChanged", false);
    capabilities.set("tools", tools);
    result.set("capabilities", capabilities);

    // the server identifies itself in the result metadata rather than at the top level, which
    // is where initialize puts it.  every result gets this, so it is added centrally where the
    // response is built rather than here

    String instructions = GrouperConfig.retrieveConfig()
        .propertyValueString("grouper.mcp.instructions");
    if (StringUtils.isNotBlank(instructions)) {
      result.put("instructions", instructions);
    }

    // caching hints, required on this operation in spec version 2026-07-28.  the scope is
    // public because everything above is the same for every caller
    result.put("ttlMs", DISCOVER_TTL_MS);
    result.put("cacheScope", "public");

    return result;
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

    // caching hints, required on this operation in spec version 2026-07-28.  the scope is
    // private because the tool list is filtered by what this user is authorized for, so a
    // shared cache must not hand one user's list to another.  note this is a caching hint
    // only, it is not what enforces authorization: tools/call checks access again on every
    // invocation
    result.put("ttlMs", TOOLS_LIST_TTL_MS);
    result.put("cacheScope", "private");

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
    sendJsonRpcError(response, id, code, message, null, HttpServletResponse.SC_OK);
  }

  /**
   * send a JSON-RPC error response, with error data and an HTTP status code.  the protocol
   * errors added in spec version 2026-07-28 are required to be returned with HTTP 400 rather
   * than the HTTP 200 that other JSON-RPC errors use, and some of them carry data the client
   * needs in order to correct the request.
   * @param response the HTTP response
   * @param id the JSON-RPC id of the request being answered, may be null
   * @param code the JSON-RPC error code
   * @param message the error message
   * @param data additional error data, may be null
   * @param httpStatus the HTTP status code to send
   * @throws IOException if the response cannot be written
   */
  private void sendJsonRpcError(HttpServletResponse response, JsonNode id,
      int code, String message, ObjectNode data, int httpStatus) throws IOException {
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
    if (data != null) {
      error.set("data", data);
    }
    jsonRpcResponse.set("error", error);

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(httpStatus);
    response.getWriter().write(objectMapper.writeValueAsString(jsonRpcResponse));
    response.getWriter().flush();
  }
}
