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
import java.net.URLDecoder;
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
      // the token this endpoint issues is bound to this server and to its MCP resource by an
      // issuer and an audience which come from grouper.ws.url, and the authorization endpoint a
      // client was sent to comes from grouper.ui.url.  without them there is no token worth
      // issuing, so nothing is issued
      String mcpUrlConfigurationError = GrouperOAuthStore.mcpUrlConfigurationError();
      if (mcpUrlConfigurationError != null) {
        sendJsonError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "server_error",
            mcpUrlConfigurationError);
        return;
      }

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
    String clientSecret = request.getParameter("client_secret");

    // if parameters are not in form-encoded, try JSON body
    if (StringUtils.isBlank(grantType) && isJsonContentType(request)) {
      try {
        JsonNode body = objectMapper.readTree(request.getInputStream());
        grantType = body.has("grant_type") ? body.get("grant_type").asText() : null;
        code = body.has("code") ? body.get("code").asText() : null;
        clientId = body.has("client_id") ? body.get("client_id").asText() : null;
        codeVerifier = body.has("code_verifier") ? body.get("code_verifier").asText() : null;
        redirectUri = body.has("redirect_uri") ? body.get("redirect_uri").asText() : null;
        clientSecret = body.has("client_secret") ? body.get("client_secret").asText() : null;
      } catch (Exception e) {
        sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
            "invalid_request", "Failed to parse request body");
        return;
      }
    }

    // RFC 6749 section 2.3.1 lets a client send its credentials either in an HTTP Basic
    // Authorization header or as request parameters, and requires the authorization server to
    // support the header form.  Whether the header was used decides whether the failure response
    // below carries a WWW-Authenticate challenge, per RFC 6749 section 5.2.
    boolean attemptedBasic = attemptedBasicAuthorization(request);

    if (attemptedBasic) {

      String[] basicCredentials = parseBasicAuthorization(request);

      if (basicCredentials == null) {
        LOG.warn("MCP OAuth token request has an unparseable Basic Authorization header");
        sendInvalidClient(response, true);
        return;
      }

      // "The client MUST NOT use more than one authentication method in each request", RFC 6749
      // section 2.3.  Two sets of credentials leave it ambiguous which one is being asserted.
      if (StringUtils.isNotBlank(clientSecret)) {
        LOG.warn("MCP OAuth token request sent a client_secret parameter and a Basic "
            + "Authorization header, which RFC 6749 section 2.3 does not allow");
        sendInvalidClient(response, true);
        return;
      }

      // when client_id is sent both ways it has to agree, otherwise which client is being
      // authenticated depends on which one the server happens to read
      if (StringUtils.isNotBlank(clientId) && !clientId.equals(basicCredentials[0])) {
        LOG.warn("MCP OAuth token request has a client_id parameter which does not match the "
            + "client_id in its Basic Authorization header");
        sendInvalidClient(response, true);
        return;
      }

      clientId = basicCredentials[0];
      clientSecret = basicCredentials[1];
    }

    if (StringUtils.isBlank(clientId)) {
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
          "invalid_request", "client_id is required");
      return;
    }

    // Authenticate the client before the authorization code is looked up, per RFC 6749 section
    // 4.1.3.  Doing it afterwards would leave the check unreachable for a request carrying a bad
    // code, and would let an unauthenticated caller learn whether a given code exists from which
    // of the two errors came back.
    GrouperOAuthClient tokenClient = GrouperOAuthStore.retrieveClient(clientId);

    if (!authenticateClient(response, tokenClient, clientId, clientSecret, attemptedBasic)) {
      return;
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
      GrouperOAuthStore.claimAuthorizationCode(code);
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
          "invalid_grant", "Authorization code already used");
      return;
    }

    // the code has to have been issued to the client which just authenticated
    if (tokenClient.getInternalId() != authCode.getOauthClientInternalId()) {
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

    // Claim the code, and stop here if it was already claimed.  The claim is what makes the
    // redemption single use, so it has to come after every check the code is judged by and
    // before the token is created: a request which loses the race gets no token, rather than a
    // token minted from a code another request had already redeemed.  Checking a "used" flag
    // read earlier cannot do this, since two requests can read the same unused row.
    //
    // The error is the one an unknown code gets, so that losing the race cannot be told apart
    // from presenting a code which never existed.
    if (!GrouperOAuthStore.claimAuthorizationCode(code)) {
      LOG.warn("MCP OAuth token request could not claim authorization code for clientId: "
          + clientId + ", so it was redeemed by another request or has already been cleaned up.");
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
          "invalid_grant", "Authorization code not found or expired");
      return;
    }

    // resolve member_internal_id to subject info for the JWT
    Map<Long, Member> memberMap = GrouperDAOFactory.getFactory().getMember()
        .findByInternalIds(Collections.singleton(authCode.getMemberInternalId()), false, null);
    Member member = memberMap.get(authCode.getMemberInternalId());
    if (member == null) {
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
          "invalid_grant", "Member not found for authorization code");
      return;
    }

    // create JWT (include consent details / granted scopes).  the issuer is read from
    // configuration inside createSignedJwt, since that is the value the token is checked
    // against when it comes back
    String jwt = GrouperOAuthSigningKey.createSignedJwt(
        member.getSubjectId(), member.getSubjectSourceId(), clientId,
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

    // check if client_secret is requested.  a method this server does not implement is refused
    // rather than quietly treated as "none", since a client which asked to authenticate and was
    // registered as public would have no way of noticing
    String tokenEndpointAuthMethod = body.has("token_endpoint_auth_method")
        ? body.get("token_endpoint_auth_method").asText() : "none";
    boolean needsSecret = "client_secret_post".equals(tokenEndpointAuthMethod)
        || "client_secret_basic".equals(tokenEndpointAuthMethod);

    if (!needsSecret && !"none".equals(tokenEndpointAuthMethod)) {
      sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "invalid_client_metadata",
          "token_endpoint_auth_method must be one of none, client_secret_post, "
              + "client_secret_basic");
      return;
    }

    // create and register client
    GrouperOAuthClient client = new GrouperOAuthClient();
    client.setClientId(GrouperOAuthStore.generateId());
    client.setRedirectUris(redirectUris);
    client.setClientName(clientName);
    client.setRegisteredMicros(System.currentTimeMillis() * 1000L);

    // generate client_secret if requested
    String plainTextSecret = null;
    if (needsSecret) {
      plainTextSecret = GrouperOAuthStore.generateId();
      client.setClientSecret(plainTextSecret);
    }

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
    if (StringUtils.isNotBlank(plainTextSecret)) {
      registrationResponse.put("client_secret", plainTextSecret);
    }
    // RFC 7591 section 3.2.1 returns the registered metadata, and this field in particular says
    // how the client is expected to authenticate at the token endpoint.  Without it a client has
    // to infer that from whether a secret came back, and a client which infers wrongly is now
    // refused rather than silently let through
    registrationResponse.put("token_endpoint_auth_method", tokenEndpointAuthMethod);
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
   * Authenticate the client making a token request, sending the failure response itself when it
   * does not authenticate.
   *
   * <p>A client registered with a secret is a confidential client and has to present that secret.
   * A client registered without one is a public client, is bound to its authorization request by
   * PKCE alone, and must not present a secret: OAuth 2.1 says an authorization server must not
   * accept credentials for a client it never issued any to, and accepting them would mean a
   * caller could claim to be authenticated as a client which cannot authenticate.</p>
   *
   * <p>An unknown client and a wrong secret get the same status, the same error code and the same
   * description, so that the response says nothing about whether a client_id a caller guessed is
   * registered.  Which of the two it was is written to the log, where only an administrator sees
   * it.  Note the two are not indistinguishable in every respect: an unknown client returns
   * before anything is decrypted, so how long the request took still separates them.  Closing
   * that would mean decrypting something for a client which does not exist, and it buys little
   * here, since client_ids are UUIDs and are not worth guessing at.</p>
   *
   * @param response the response, written to only when authentication fails
   * @param client the registered client, null when the client_id is not registered
   * @param clientId the client_id from the request, used for logging
   * @param presentedSecret the secret the client presented, may be null
   * @param attemptedBasic whether the client used an HTTP Basic Authorization header
   * @return true if the client authenticated and the request should carry on
   */
  private boolean authenticateClient(HttpServletResponse response, GrouperOAuthClient client,
      String clientId, String presentedSecret, boolean attemptedBasic) throws IOException {

    if (client == null) {
      LOG.warn("MCP OAuth token request for a client_id which is not registered: " + clientId);
      sendInvalidClient(response, attemptedBasic);
      return false;
    }

    if (GrouperOAuthStore.clientHasSecret(client)) {

      if (StringUtils.isBlank(presentedSecret)) {
        LOG.warn("MCP OAuth token request did not present a client_secret for clientId: "
            + clientId + ", which is registered as a confidential client.  Send the secret as a "
            + "client_secret parameter or as the password of an HTTP Basic Authorization header.");
        sendInvalidClient(response, attemptedBasic);
        return false;
      }

      if (!GrouperOAuthStore.clientSecretMatches(client, presentedSecret)) {
        LOG.warn("MCP OAuth token request presented the wrong client_secret for clientId: "
            + clientId);
        sendInvalidClient(response, attemptedBasic);
        return false;
      }

      return true;
    }

    if (StringUtils.isNotBlank(presentedSecret)) {
      LOG.warn("MCP OAuth token request presented a client_secret for clientId: " + clientId
          + ", which is registered as a public client and was never issued one.  Either remove "
          + "the secret from the client configuration, or re-register it as a confidential "
          + "client with token_endpoint_auth_method of client_secret_post or client_secret_basic.");
      sendInvalidClient(response, attemptedBasic);
      return false;
    }

    return true;
  }

  /**
   * whether the request body is JSON.
   *
   * <p>Only the media type is compared. A Content-Type may carry parameters after a semicolon,
   * and {@code application/json; charset=utf-8} is as much JSON as {@code application/json} is,
   * so comparing the header as a whole used to leave such a request with nothing parsed at all:
   * a servlet container turns only a form encoded body into request parameters, so every value
   * came back null and the request was refused over whichever field happened to be checked
   * first, naming a field the client had in fact sent. The comparison ignores case because
   * <a href="https://www.rfc-editor.org/rfc/rfc9110#section-8.3">RFC 9110 section 8.3</a> says a
   * media type is case insensitive.</p>
   *
   * @param request the token request
   * @return true if the body is JSON
   */
  private boolean isJsonContentType(HttpServletRequest request) {

    String contentType = request.getContentType();

    if (contentType == null) {
      return false;
    }

    int semicolonIndex = contentType.indexOf(';');
    String mediaType = semicolonIndex < 0 ? contentType : contentType.substring(0, semicolonIndex);

    return "application/json".equalsIgnoreCase(mediaType.trim());
  }

  /**
   * whether the request carries an HTTP Basic Authorization header, whether or not it can be
   * parsed.
   *
   * <p>This decides whether a failure response carries a WWW-Authenticate challenge, which RFC
   * 6749 section 5.2 says to include only when the client attempted to authenticate that way.
   * Sending one to a client which never used the header invites it to retry with credentials it
   * does not have.</p>
   *
   * @param request the token request
   * @return true if there is a Basic Authorization header
   */
  private boolean attemptedBasicAuthorization(HttpServletRequest request) {

    String authorization = request.getHeader("Authorization");

    return authorization != null && authorization.regionMatches(true, 0, "Basic ", 0, 6);
  }

  /**
   * Parse the client_id and client secret out of an HTTP Basic Authorization header.
   *
   * <p>Both halves are form-urlencoded before being base64 encoded, per RFC 6749 section 2.3.1,
   * so both are decoded on the way back out.</p>
   *
   * @param request the token request, which has a Basic Authorization header
   * @return an array of client_id and secret, or null if the header cannot be parsed
   */
  private String[] parseBasicAuthorization(HttpServletRequest request) {

    String encoded = StringUtils.trimToNull(request.getHeader("Authorization").substring(6));

    if (encoded == null) {
      return null;
    }

    String decoded = null;

    try {
      decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException iae) {
      return null;
    }

    // the client_id cannot itself contain a colon, since it is form-urlencoded, so the first one
    // separates the two halves and any later one belongs to the secret
    int colonIndex = decoded.indexOf(':');

    if (colonIndex < 0) {
      return null;
    }

    String basicClientId = null;
    String basicClientSecret = null;

    try {
      basicClientId = URLDecoder.decode(decoded.substring(0, colonIndex), StandardCharsets.UTF_8);
      basicClientSecret = URLDecoder.decode(decoded.substring(colonIndex + 1),
          StandardCharsets.UTF_8);
    } catch (IllegalArgumentException iae) {
      return null;
    }

    if (StringUtils.isBlank(basicClientId)) {
      return null;
    }

    return new String[] { basicClientId, basicClientSecret };
  }

  /**
   * Send the response for a client which did not authenticate.
   *
   * <p>RFC 6749 section 5.2 gives this error the 401 status, and says to include a
   * WWW-Authenticate header matching the scheme the client used, which means sending one only
   * when the client actually tried HTTP Basic.</p>
   *
   * @param response the response
   * @param attemptedBasic whether the client used an HTTP Basic Authorization header
   */
  private void sendInvalidClient(HttpServletResponse response, boolean attemptedBasic)
      throws IOException {

    if (attemptedBasic) {
      response.setHeader("WWW-Authenticate", "Basic realm=\"Grouper MCP token endpoint\", "
          + "charset=\"UTF-8\"");
    }

    // deliberately says nothing about which client_id was sent, whether it is registered, or
    // whether a secret was expected
    sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
        "invalid_client", "Client authentication failed");
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
