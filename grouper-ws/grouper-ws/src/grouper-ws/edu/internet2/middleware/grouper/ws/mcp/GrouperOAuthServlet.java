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
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthSigningKey;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthStore;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthCode;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthClient;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * OAuth 2.1 + PKCE servlet for MCP authentication.
 *
 * <p>Handles the following endpoints under {@code /mcp/oauth/*}:</p>
 * <ul>
 *   <li>POST /mcp/oauth/token - Token endpoint (exchanges auth code + PKCE verifier for JWT)</li>
 *   <li>POST /mcp/oauth/register - Dynamic client registration (anonymous, rate-limited)</li>
 * </ul>
 *
 * <p>Registration is anonymous (no authentication required) and protected by:
 * the redirect URI allowlist, IP-based rate limiting, and the fact that
 * registration alone grants zero access — the user must still complete the
 * OAuth consent flow and be in the proper MCP authorization groups.</p>
 *
 * <p>The authorization endpoint is handled by the Grouper UI at
 * {@code grouperUi/app/UiV2OAuth.authorize} so the user gets authenticated
 * and sees the consent page directly without an extra redirect hop.</p>
 *
 * @author mchyzer
 */
public class GrouperOAuthServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private static final Log LOG = GrouperUtil.getLog(GrouperOAuthServlet.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Rate limit cache for registration endpoint.
   * Key: remote IP address, Value: count of registrations in the current window.
   * Cache entries expire after 1 hour (3600 seconds).
   */
  private static GrouperCache<String, Integer> registrationRateLimitCache =
      new GrouperCache<String, Integer>(
          GrouperOAuthServlet.class.getName() + ".registrationRateLimitCache",
          10000, false, 3600, 3600, false);

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      // determine which operation based on pathInfo or servletPath
      // (servletPath is used when mapped directly to /register or /token)
      String pathInfo = request.getPathInfo();
      String servletPath = request.getServletPath();

      if (pathInfo != null && pathInfo.startsWith("/token")
          || "/token".equals(servletPath)) {
        handleToken(request, response);
      } else if (pathInfo != null && pathInfo.startsWith("/register")
          || "/register".equals(servletPath)) {
        handleRegister(request, response);
      } else {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
    } catch (RuntimeException re) {
      LOG.error("Error in MCP OAuth doPost", re);
      throw re;
    }
  }

  /**
   * Handle POST /mcp/oauth/token
   * Exchanges an authorization code + PKCE code_verifier for a JWT access token.
   */
  private void handleToken(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    // token endpoint accepts form-encoded or JSON body
    String grantType = request.getParameter("grant_type");
    String code = request.getParameter("code");
    String clientId = request.getParameter("client_id");
    String codeVerifier = request.getParameter("code_verifier");
    String redirectUri = request.getParameter("redirect_uri");

    // if parameters are not in form-encoded, try JSON body
    if (StringUtils.isBlank(grantType) && "application/json".equals(request.getContentType())) {
      try {
        JsonNode body = objectMapper.readTree(request.getInputStream());
        grantType = body.has("grant_type") ? body.get("grant_type").asText() : null;
        code = body.has("code") ? body.get("code").asText() : null;
        clientId = body.has("client_id") ? body.get("client_id").asText() : null;
        codeVerifier = body.has("code_verifier") ? body.get("code_verifier").asText() : null;
        redirectUri = body.has("redirect_uri") ? body.get("redirect_uri").asText() : null;
      } catch (Exception e) {
        sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
            "invalid_request", "Failed to parse request body");
        return;
      }
    }

    if (!"authorization_code".equals(grantType)) {
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
          "unsupported_grant_type", "Only grant_type=authorization_code is supported");
      return;
    }

    if (StringUtils.isBlank(code)) {
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
          "invalid_request", "code is required");
      return;
    }

    if (StringUtils.isBlank(clientId)) {
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
          "invalid_request", "client_id is required");
      return;
    }

    if (StringUtils.isBlank(codeVerifier)) {
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
          "invalid_request", "code_verifier is required (PKCE)");
      return;
    }

    // look up the authorization code
    GrouperOAuthCode authCode = GrouperOAuthStore.retrieveAuthorizationCode(code);

    if (authCode == null) {
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
          "invalid_grant", "Authorization code not found or expired");
      return;
    }

    // prevent reuse
    if (authCode.isUsed()) {
      GrouperOAuthStore.removeAuthorizationCode(code);
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
          "invalid_grant", "Authorization code already used");
      return;
    }

    // validate client_id matches by looking up the client and comparing internal_id
    GrouperOAuthClient tokenClient = GrouperOAuthStore.retrieveClient(clientId);
    if (tokenClient == null || tokenClient.getInternalId() != authCode.getOauthClientInternalId()) {
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
          "invalid_grant", "client_id does not match");
      return;
    }

    // validate redirect_uri matches if provided
    if (StringUtils.isNotBlank(redirectUri)
        && !redirectUri.equals(authCode.getRedirectUri())) {
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
          "invalid_grant", "redirect_uri does not match");
      return;
    }

    // PKCE verification: S256
    // code_challenge = BASE64URL(SHA256(code_verifier))
    if (!verifyPkce(codeVerifier, authCode.getCodeChallenge(), authCode.getCodeChallengeMethod())) {
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
          "invalid_grant", "PKCE verification failed");
      return;
    }

    // mark code as used
    authCode.setUsed(true);

    // remove the code from the store
    GrouperOAuthStore.removeAuthorizationCode(code);

    // resolve member_internal_id to subject info for the JWT
    Map<Long, Member> memberMap = GrouperDAOFactory.getFactory().getMember()
        .findByInternalIds(Collections.singleton(authCode.getMemberInternalId()), false, null);
    Member member = memberMap.get(authCode.getMemberInternalId());
    if (member == null) {
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
          "invalid_grant", "Member not found for authorization code");
      return;
    }

    // construct the issuer URL
    String issuer = request.getScheme() + "://" + request.getServerName();
    if (("http".equals(request.getScheme()) && request.getServerPort() != 80)
        || ("https".equals(request.getScheme()) && request.getServerPort() != 443)) {
      issuer += ":" + request.getServerPort();
    }
    issuer += request.getContextPath();

    // create JWT (include consent details / granted scopes)
    String jwt = GrouperOAuthSigningKey.createSignedJwt(
        issuer, member.getSubjectId(), member.getSubjectSourceId(), clientId,
        authCode.getConsentDetails());

    int expirationSeconds = GrouperConfig.retrieveConfig().propertyValueInt(
        "grouper.oauth.accessToken.expirationSeconds", 14400);

    // audit (token endpoint is unauthenticated so we need a root session for the audit entry)
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      AuditEntry tokenAuditEntry = new AuditEntry(AuditTypeBuiltin.OAUTH_TOKEN_ISSUE,
          "clientId", clientId,
          "memberId", member.getId());
      tokenAuditEntry.setDescription("OAuth token issued for subject=" + member.getSubjectSourceId()
          + "::::" + member.getSubjectId() + ", clientId=" + clientId);
      tokenAuditEntry.saveOrUpdate(true);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    LOG.info("MCP OAuth token issued for subject=" + member.getSubjectSourceId()
        + "::::" + member.getSubjectId() + ", clientId=" + clientId);

    // return token response
    ObjectNode tokenResponse = objectMapper.createObjectNode();
    tokenResponse.put("access_token", jwt);
    tokenResponse.put("token_type", "Bearer");
    tokenResponse.put("expires_in", expirationSeconds);

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setHeader("Cache-Control", "no-store");
    response.setHeader("Pragma", "no-cache");
    response.getWriter().write(objectMapper.writeValueAsString(tokenResponse));
    response.getWriter().flush();
  }

  /**
   * Handle POST /mcp/oauth/register
   * Dynamic client registration per RFC 7591.
   *
   * <p>Registration is anonymous (no authentication required).  It is protected by:
   * the redirect URI allowlist ({@code grouper.oauth.redrectUri.*.regex}),
   * IP-based rate limiting, and the fact that registration alone grants zero
   * access — the user must still complete the OAuth consent flow.</p>
   *
   * <p>Registration can be disabled entirely by setting
   * {@code grouper.oauth.registration.enabled=false}.</p>
   */
  private void handleRegister(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    // ---- check if registration is enabled ----
    boolean registrationEnabled = GrouperConfig.retrieveConfig().propertyValueBoolean(
        "grouper.oauth.registration.enabled", true);
    if (!registrationEnabled) {
      sendJsonError(response, HttpServletResponse.SC_FORBIDDEN,
          "access_denied", "OAuth client registration is disabled on this server");
      return;
    }

    // ---- IP-based rate limiting ----
    String remoteIp = request.getRemoteAddr();
    int rateLimit = GrouperConfig.retrieveConfig().propertyValueInt(
        "grouper.oauth.registration.rateLimitPerIpPerHour", 20);
    if (rateLimit > 0) {
      Integer currentCount = registrationRateLimitCache.get(remoteIp);
      int count = (currentCount != null) ? currentCount : 0;
      if (count >= rateLimit) {
        LOG.warn("OAuth registration rate limit exceeded for IP: " + remoteIp
            + " (" + count + "/" + rateLimit + " per hour)");
        response.setStatus(429); // Too Many Requests
        sendJsonError(response, 429,
            "rate_limit_exceeded", "Too many registration requests. Try again later.");
        return;
      }
      registrationRateLimitCache.put(remoteIp, count + 1);
    }

    // ---- parse request body ----
    JsonNode body;
    try {
      body = objectMapper.readTree(request.getInputStream());
    } catch (Exception e) {
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
          "invalid_request", "Failed to parse request body");
      return;
    }

    // ---- parse registration fields ----

    // extract redirect_uris (required)
    if (!body.has("redirect_uris") || !body.get("redirect_uris").isArray()
        || body.get("redirect_uris").size() == 0) {
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
          "invalid_request", "redirect_uris is required and must be a non-empty array");
      return;
    }

    Set<String> redirectUris = new LinkedHashSet<String>();
    for (JsonNode uriNode : body.get("redirect_uris")) {
      String uri = uriNode.asText();
      if (StringUtils.isNotBlank(uri)) {
        redirectUris.add(uri);
      }
    }

    if (redirectUris.isEmpty()) {
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
          "invalid_request", "At least one valid redirect_uri is required");
      return;
    }

    // validate redirect URIs against configured regex patterns
    String redirectUriError = GrouperOAuthStore.validateRedirectUrisAllowed(redirectUris);
    if (redirectUriError != null) {
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
          "invalid_redirect_uri", redirectUriError);
      return;
    }

    String clientName = body.has("client_name") ? body.get("client_name").asText() : null;

    // create and register client
    GrouperOAuthClient client = new GrouperOAuthClient();
    client.setClientId(GrouperOAuthStore.generateId());
    client.setRedirectUris(redirectUris);
    client.setClientName(clientName);
    client.setRegisteredMicros(System.currentTimeMillis() * 1000L);

    GrouperOAuthStore.registerClient(client);

    // audit (registration is anonymous so we need a root session for the audit entry)
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.OAUTH_CLIENT_REGISTER,
          "clientId", client.getClientId(),
          "clientName", clientName);
      auditEntry.setDescription("OAuth client registered: clientId=" + client.getClientId()
          + ", clientName=" + clientName + ", ip=" + remoteIp);
      auditEntry.saveOrUpdate(true);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    LOG.info("MCP OAuth client registered: clientId=" + client.getClientId()
        + ", clientName=" + clientName + ", ip=" + remoteIp);

    // return registration response
    ObjectNode registrationResponse = objectMapper.createObjectNode();
    registrationResponse.put("client_id", client.getClientId());
    if (StringUtils.isNotBlank(clientName)) {
      registrationResponse.put("client_name", clientName);
    }
    ArrayNode urisArray = objectMapper.createArrayNode();
    for (String uri : redirectUris) {
      urisArray.add(uri);
    }
    registrationResponse.set("redirect_uris", urisArray);

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(HttpServletResponse.SC_CREATED);
    response.getWriter().write(objectMapper.writeValueAsString(registrationResponse));
    response.getWriter().flush();
  }

  /**
   * Verify PKCE code_verifier against code_challenge using S256 method.
   * @param codeVerifier the verifier from the token request
   * @param codeChallenge the challenge stored from the authorize request
   * @param codeChallengeMethod the method (must be S256)
   * @return true if valid
   */
  private boolean verifyPkce(String codeVerifier, String codeChallenge,
      String codeChallengeMethod) {

    if (!"S256".equals(codeChallengeMethod)) {
      return false;
    }

    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
      String computed = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
      return computed.equals(codeChallenge);
    } catch (Exception e) {
      LOG.error("PKCE verification error", e);
      return false;
    }
  }

  /**
   * Send a JSON error response (for token and register endpoints).
   */
  private void sendJsonError(HttpServletResponse response, int httpStatus,
      String error, String errorDescription) throws IOException {

    ObjectNode errorResponse = objectMapper.createObjectNode();
    errorResponse.put("error", error);
    if (StringUtils.isNotBlank(errorDescription)) {
      errorResponse.put("error_description", errorDescription);
    }

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(httpStatus);
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    response.getWriter().flush();
  }
}
