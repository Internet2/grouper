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
package edu.internet2.middleware.grouper.authentication;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.morphString.Morph;

/**
 * Store for OAuth pending requests, authorization codes, and registered clients.
 * All data is persisted via GcDbAccess to the OAuth tables (grouper_oauth_client,
 * grouper_oauth_code, grouper_oauth_pend_authz_req).
 *
 * <p>This supports load-balanced deployments where UI and WS run in separate containers.</p>
 * @author mchyzer
 */
public class GrouperOAuthStore {

  private static final Log LOG = GrouperUtil.getLog(GrouperOAuthStore.class);

  // ==================== Pending Requests ====================

  /**
   * store a pending authorization request in the database
   * @param pendingRequest
   */
  public static void storePendingRequest(GrouperOAuthPendingRequest pendingRequest) {

    int expirationSeconds = GrouperConfig.retrieveConfig().propertyValueInt(
        "grouper.oauth.authorizationCode.expirationSeconds", 600);
    long expiresMicros = pendingRequest.getCreatedMicros()
        + (expirationSeconds * 1000000L);
    pendingRequest.setExpiresMicros(expiresMicros);

    new GcDbAccess().storeToDatabase(pendingRequest);
  }

  /**
   * retrieve a pending authorization request by request ID
   * @param requestId
   * @return the pending request or null if expired/not found
   */
  public static GrouperOAuthPendingRequest retrievePendingRequest(String requestId) {
    GrouperOAuthPendingRequest pendingRequest = new GcDbAccess()
        .sql("select * from grouper_oauth_pend_authz_req where request_id = ?")
        .addBindVar(requestId)
        .select(GrouperOAuthPendingRequest.class);

    if (pendingRequest == null) {
      return null;
    }

    // check expiration
    if (pendingRequest.getExpiresMicros() != null
        && (System.currentTimeMillis() * 1000L) > pendingRequest.getExpiresMicros()) {
      new GcDbAccess().deleteFromDatabase(pendingRequest);
      return null;
    }

    return pendingRequest;
  }

  /**
   * remove a pending request from the database
   * @param requestId
   */
  public static void removePendingRequest(String requestId) {
    GrouperOAuthPendingRequest pendingRequest = new GcDbAccess()
        .sql("select * from grouper_oauth_pend_authz_req where request_id = ?")
        .addBindVar(requestId)
        .select(GrouperOAuthPendingRequest.class);
    if (pendingRequest != null) {
      new GcDbAccess().deleteFromDatabase(pendingRequest);
    }
  }

  // ==================== Authorization Codes ====================

  /**
   * store an authorization code in the database
   * @param authCode
   */
  public static void storeAuthorizationCode(GrouperOAuthCode authCode) {

    int expirationSeconds = GrouperConfig.retrieveConfig().propertyValueInt(
        "grouper.oauth.authorizationCode.expirationSeconds", 600);
    long expiresMicros = authCode.getCreatedMicros()
        + (expirationSeconds * 1000000L);
    authCode.setExpiresMicros(expiresMicros);

    new GcDbAccess().storeToDatabase(authCode);
  }

  /**
   * retrieve an authorization code from the database
   * @param code
   * @return the auth code or null if expired/not found
   */
  public static GrouperOAuthCode retrieveAuthorizationCode(String code) {
    GrouperOAuthCode authCode = new GcDbAccess()
        .sql("select * from grouper_oauth_code where code = ?")
        .addBindVar(code)
        .select(GrouperOAuthCode.class);

    if (authCode == null) {
      return null;
    }

    // check expiration
    if (authCode.getExpiresMicros() != null
        && (System.currentTimeMillis() * 1000L) > authCode.getExpiresMicros()) {
      new GcDbAccess().deleteFromDatabase(authCode);
      return null;
    }

    return authCode;
  }

  /**
   * remove an authorization code from the database (after use)
   * @param code
   */
  public static void removeAuthorizationCode(String code) {
    GrouperOAuthCode authCode = new GcDbAccess()
        .sql("select * from grouper_oauth_code where code = ?")
        .addBindVar(code)
        .select(GrouperOAuthCode.class);
    if (authCode != null) {
      new GcDbAccess().deleteFromDatabase(authCode);
    }
  }

  // ==================== Registered Clients ====================

  /**
   * register a client in the database.
   * The client secret will be encrypted via Morph.encrypt() before storing.
   * @param client
   */
  public static void registerClient(GrouperOAuthClient client) {
    // encrypt the client secret before storing
    if (client.getClientSecret() != null) {
      client.setClientSecret(Morph.encrypt(client.getClientSecret()));
    }
    new GcDbAccess().storeToDatabase(client);
  }

  /**
   * retrieve a registered client from the database.
   * The client secret will be decrypted via Morph.decrypt() after loading.
   * @param clientId
   * @return the client or null if not found
   */
  public static GrouperOAuthClient retrieveClient(String clientId) {
    GrouperOAuthClient client = new GcDbAccess()
        .sql("select * from grouper_oauth_client where client_id = ?")
        .addBindVar(clientId)
        .select(GrouperOAuthClient.class);

    if (client == null) {
      return null;
    }

    // decrypt client secret
    if (client.getClientSecret() != null) {
      try {
        client.setClientSecret(Morph.decrypt(client.getClientSecret()));
      } catch (Exception e) {
        LOG.warn("Could not decrypt client secret for clientId: " + clientId);
      }
    }

    return client;
  }

  /**
   * retrieve a registered client from the database by internal id.
   * The client secret will be decrypted via Morph.decrypt() after loading.
   * @param internalId
   * @return the client or null if not found
   */
  public static GrouperOAuthClient retrieveClientByInternalId(long internalId) {
    GrouperOAuthClient client = new GcDbAccess()
        .sql("select * from grouper_oauth_client where internal_id = ?")
        .addBindVar(internalId)
        .select(GrouperOAuthClient.class);

    if (client == null) {
      return null;
    }

    // decrypt client secret
    if (client.getClientSecret() != null) {
      try {
        client.setClientSecret(Morph.decrypt(client.getClientSecret()));
      } catch (Exception e) {
        LOG.warn("Could not decrypt client secret for internalId: " + internalId);
      }
    }

    return client;
  }

  // ==================== Redirect URI validation ====================

  /**
   * pattern to find all grouper.oauth.redrectUri.{configId}.regex config keys
   */
  private static final Pattern REDIRECT_URI_CONFIG_PATTERN = Pattern.compile(
      "^grouper\\.oauth\\.redrectUri\\.([^.]+)\\.regex$");

  /** whether the warning about the MCP URLs not being configured has been logged */
  private static boolean loggedMcpUrlConfigurationError = false;

  /**
   * whether MCP is enabled on this node.
   *
   * <p>Note this lives in grouper.hibernate.properties rather than grouper.properties, since it
   * is read before the rest of the configuration is available.</p>
   *
   * @return true if grouper.is.mcp is true
   */
  public static boolean mcpEnabled() {
    return GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.mcp", false);
  }

  /**
   * why MCP cannot be served, when {@code grouper.ws.url} or {@code grouper.ui.url} is not
   * configured, or null when both are.
   *
   * <p>Everything MCP publishes about itself is built from these two properties: the token
   * audience and the issuer which bind an access token to this server and this resource, the
   * addresses in the discovery metadata, the address in the {@code WWW-Authenticate} challenge,
   * and the origin the Origin check compares against. There is nothing else to build any of
   * them from. Deriving them from the request instead, which is what this code used to do, put
   * every one of those addresses under the control of whoever set the Host header, and left
   * tokens with neither an audience nor an issuer, so that a token minted by anything else
   * holding the signing key was accepted here.</p>
   *
   * <p>So both are required whenever MCP is enabled. They cannot be required unconditionally,
   * because deployments which do not use MCP have always run without them, and startup is not
   * refused over them either, because that would take down the UI and WS over a property only
   * MCP needs. Instead each MCP and OAuth endpoint checks this and refuses to serve, which
   * fails closed and confines the effect to MCP.</p>
   *
   * @return a message naming what is missing, or null when MCP can be served
   */
  public static String mcpUrlConfigurationError() {

    boolean wsUrlMissing = StringUtils.isBlank(GrouperConfig.getGrouperWsUrl(false));
    boolean uiUrlMissing = StringUtils.isBlank(GrouperConfig.getGrouperUiUrl(false));

    if (!wsUrlMissing && !uiUrlMissing) {
      return null;
    }

    String missing = null;
    if (wsUrlMissing && uiUrlMissing) {
      missing = "grouper.ws.url and grouper.ui.url are";
    } else if (wsUrlMissing) {
      missing = "grouper.ws.url is";
    } else {
      missing = "grouper.ui.url is";
    }

    String error = "MCP is enabled but " + missing + " not configured in grouper.properties, so "
        + "MCP requests are refused.  The addresses MCP publishes to clients, and the audience "
        + "and issuer of its access tokens, are built from these.  "
        + "grouper.ws.url is the URL of Grouper WS "
        + "including its context path, for example https://server.school.edu/grouper-ws/, and "
        + "grouper.ui.url is the URL of Grouper UI, for example "
        + "https://server.school.edu/grouper/.";

    if (!loggedMcpUrlConfigurationError) {
      loggedMcpUrlConfigurationError = true;
      LOG.error(error);
    }

    return error;
  }

  /**
   * The issuer identifier of this Grouper as an OAuth authorization server.
   *
   * <p>This is the value published as {@code issuer} in the authorization server metadata, and
   * it is also sent back to the client as the {@code iss} parameter on an authorization
   * response. A client which knows about
   * <a href="https://datatracker.ietf.org/doc/html/rfc9207">RFC 9207</a> compares the two and
   * refuses to redeem the authorization code if they differ, which is what protects it from
   * being tricked into sending a code issued by one authorization server to a different one.
   * The two values are therefore read from here rather than worked out separately in each
   * place, since a value which does not match is worse than no value at all.</p>
   *
   * <p>This comes from configuration only. It deliberately does not fall back to deriving a URL
   * from the request: the metadata is served by the web services application and the
   * authorization response is sent by the UI application, so a request derived value could
   * differ between the two, for example behind a reverse proxy or on a different context path.</p>
   *
   * <p>Every caller sits behind {@link #mcpUrlConfigurationError()}, which refuses the request
   * with a message naming what is missing before anything gets here, so the property is asked
   * for with {@code exceptionIfNull}. Returning null instead used to leave tokens with no
   * {@code iss} and no {@code aud} claim and the code which verifies them checking neither, which
   * is a hole that opens quietly; throwing says an entry point is missing that check.</p>
   *
   * @return the issuer identifier, never null
   */
  public static String retrieveIssuerIdentifier() {

    String issuerIdentifier = GrouperConfig.getGrouperWsUrl(true);

    warnIfNotSecurelyReachable(issuerIdentifier, "grouper.ws.url");

    return issuerIdentifier;
  }

  /** property names already warned about for not being https, so each is said once */
  private static final Set<String> loggedNonHttpsProperties =
      Collections.synchronizedSet(new HashSet<String>());

  /**
   * warn once when a URL this server publishes as an OAuth endpoint is not one which can be
   * reached securely.
   *
   * <p>OAuth 2.1 requires every authorization server endpoint to be served over HTTPS. Whether
   * that is true is not something this code can see, since TLS is normally terminated in front
   * of the application and what reaches it is plain HTTP either way. What it can see is the
   * address it publishes to clients, and publishing an {@code http} address is either a server
   * which really is unprotected or a configuration which does not describe the deployment. Both
   * are worth saying out loud, and neither is worth refusing to start over.</p>
   *
   * <p>Loopback addresses are left alone. Running a development instance over plain HTTP on the
   * machine in front of you is normal, and the redirect URI rules carve it out for the same
   * reason.</p>
   *
   * @param url the configured URL, may be null
   * @param propertyName the property it came from, used to name it in the log
   */
  public static void warnIfNotSecurelyReachable(String url, String propertyName) {

    // said once, and once said there is nothing more to work out.  this sits ahead of the
    // checks below because retrieveIssuerIdentifier is called while verifying the token on
    // every request, and there is no reason to parse the same URL again on each of them
    if (loggedNonHttpsProperties.contains(propertyName)) {
      return;
    }

    if (url == null || url.toLowerCase().startsWith("https://") || isLoopbackUrl(url)) {
      return;
    }

    if (loggedNonHttpsProperties.add(propertyName)) {
      LOG.warn(propertyName + " is '" + url + "', which is not https, so the OAuth endpoints "
          + "this server publishes are advertised as reachable without TLS.  Access tokens are "
          + "bearer tokens and anyone who sees one can use it, so configure this as the https "
          + "address clients actually reach.");
    }
  }

  /**
   * whether an authorization code may be sent to this redirect URI over the network it names.
   *
   * <p>https always may. http may only when the address is the machine the client is running
   * on, where nothing leaves that machine. Anything else, including a URI which cannot be
   * parsed or names no host at all, may not.</p>
   *
   * @param redirectUri the redirect URI, may be null
   * @return true if the URI is https or loopback
   */
  private static boolean isRedirectUriSecurelyReachable(String redirectUri) {

    String trimmedRedirectUri = StringUtils.trimToNull(redirectUri);

    if (trimmedRedirectUri == null) {
      return false;
    }

    URI uri;

    try {
      uri = new URI(trimmedRedirectUri);
    } catch (URISyntaxException use) {
      return false;
    }

    String scheme = uri.getScheme();

    // RFC 6749 section 3.1.2 wants an absolute URI, so a scheme and a host, and no fragment.
    // These are checked on the parsed URI rather than by looking at how the text starts, so
    // that something like "https://#x" or a URI with no host at all cannot get through on the
    // strength of its first few characters.
    if (scheme == null || uri.getHost() == null || uri.getFragment() != null) {
      return false;
    }

    if ("https".equalsIgnoreCase(scheme)) {
      return true;
    }

    // http only when it does not leave the machine.  Any other scheme is refused, which
    // includes the private use schemes RFC 8252 allows a native application to register with
    // its operating system, such as com.example.app:/callback.  Those are valid OAuth, but the
    // MCP rule quoted above names only localhost and https, and this follows it.  A deployer
    // cannot re-admit them through the configured patterns, since this is checked first.
    if ("http".equalsIgnoreCase(scheme)) {
      return isLoopbackHost(uri.getHost());
    }

    return false;
  }

  /**
   * whether a URL names the machine it is running on, which is where plain HTTP is acceptable
   * @param url the URL, not null
   * @return true if the host is a loopback address
   */
  private static boolean isLoopbackUrl(String url) {

    try {
      return isLoopbackHost(new URI(url).getHost());
    } catch (URISyntaxException use) {
      return false;
    }
  }

  /**
   * whether a host names the machine it is running on.
   *
   * <p>Decided from the text of the host alone, with no name lookup. A lookup would answer a
   * different question, namely where a name points at the moment it is asked, and the answer
   * could differ between the moment a redirect URI is registered and the moment an
   * authorization code is sent to it.</p>
   *
   * @param host the host from a URI, may be null
   * @return true if it is a loopback address
   */
  private static boolean isLoopbackHost(String host) {

    if (host == null) {
      return false;
    }

    String lowerHost = host.toLowerCase();

    if ("localhost".equals(lowerHost) || "::1".equals(lowerHost) || "[::1]".equals(lowerHost)) {
      return true;
    }

    return isLoopbackIpv4Literal(lowerHost);
  }

  /**
   * whether a host is written as an IPv4 address in 127.0.0.0/8.
   *
   * <p>Each part has to be a run of digits in range, so that a name which merely begins the
   * same way is not taken for an address. {@code 127.attacker.example} is a name somebody else
   * controls, and the only thing it has in common with a loopback address is how it starts.</p>
   *
   * @param host the lower cased host, not null
   * @return true if the host is an IPv4 literal whose first part is 127
   */
  private static boolean isLoopbackIpv4Literal(String host) {

    String[] parts = host.split("\\.", -1);

    if (parts.length != 4) {
      return false;
    }

    for (int i = 0; i < parts.length; i++) {

      String part = parts[i];

      if (part.length() == 0 || part.length() > 3) {
        return false;
      }

      for (int j = 0; j < part.length(); j++) {
        char partChar = part.charAt(j);
        if (partChar < '0' || partChar > '9') {
          return false;
        }
      }

      int partValue = Integer.parseInt(part);

      if (partValue > 255) {
        return false;
      }

      if (i == 0 && partValue != 127) {
        return false;
      }
    }

    return true;
  }

  /**
   * The canonical URI of the MCP server, which is what an access token issued for it is bound to.
   *
   * <p>An access token has to say which resource it was issued for, and the resource server has
   * to refuse a token issued for anything else. Otherwise a token obtained for one resource can
   * be replayed against another which trusts the same signing key, which is the confused deputy
   * problem that the resource parameter of
   * <a href="https://www.rfc-editor.org/rfc/rfc8707.html">RFC 8707</a> exists to prevent.</p>
   *
   * <p>Grouper hosts exactly one MCP endpoint, so this is derived from configuration rather than
   * carried through the authorization flow from what the client asked for. That keeps the
   * audience out of the client's control, and avoids having to store it against the pending
   * request and the authorization code. If Grouper ever hosts more than one MCP resource this
   * has to become a real per-request value instead.</p>
   *
   * <p>Like {@link #retrieveIssuerIdentifier()} this is configuration only, so that the value
   * used when a token is issued cannot differ from the value used when it is verified. A
   * request derived value would differ whenever a server is reachable under more than one
   * hostname.</p>
   *
   * @return the canonical MCP resource URI, never null
   */
  public static String retrieveMcpResourceIdentifier() {

    return retrieveIssuerIdentifier() + "/mcp";
  }

  /**
   * Validate that a single redirect URI matches at least one configured
   * {@code grouper.oauth.redrectUri.<configId>.regex} pattern.
   * If no regex patterns are configured, all redirect URIs are rejected
   * (secure-by-default).
   *
   * @param redirectUri the redirect URI to validate
   * @return null if the URI is allowed, or a descriptive error message if not
   */
  public static String validateRedirectUriAllowed(String redirectUri) {

    // The MCP specification says every redirect URI must be either localhost or use https.
    // This is checked before the configured patterns rather than left to them, because it is a
    // floor the authorization server owes whatever a deployer writes: a pattern which let an
    // http address through would have this server send an authorization code across the network
    // in the clear.  Note this is narrower than OAuth on its own, which also lets a native
    // application use a private use scheme it has registered with its operating system.
    if (!isRedirectUriSecurelyReachable(redirectUri)) {
      LOG.warn("OAuth redirect URI rejected, '" + redirectUri + "' is not an https URL and is "
          + "not http with a loopback host.  MCP allows only those two, since an authorization "
          + "code is sent to this address.  Private use schemes such as com.example.app:/cb are "
          + "deliberately not accepted, and cannot be allowed through "
          + "grouper.oauth.redrectUri.<configId>.regex.");
      return "redirect_uri '" + redirectUri + "' must be an https URL, or http with a loopback "
          + "host such as 127.0.0.1";
    }

    Map<String, String> regexMap = GrouperConfig.retrieveConfig()
        .propertiesMap(REDIRECT_URI_CONFIG_PATTERN);

    if (regexMap.isEmpty()) {
      LOG.warn("OAuth redirect URI rejected because no "
          + "grouper.oauth.redrectUri.*.regex patterns are configured. "
          + "Attempted URI: '" + redirectUri + "'");
      return "No redirect URI patterns are configured; all redirect URIs "
          + "are rejected. Configure grouper.oauth.redrectUri.<configId>.regex "
          + "in grouper.properties.";
    }

    // test the URI against each configured regex
    for (Map.Entry<String, String> entry : regexMap.entrySet()) {
      String regex = entry.getValue();
      if (regex != null) {
        try {
          if (Pattern.matches(regex, redirectUri)) {
            return null; // allowed
          }
        } catch (Exception e) {
          LOG.error("Invalid regex in config key '" + entry.getKey()
              + "': " + regex, e);
        }
      }
    }

    // none matched - build descriptive log message with up to 10 patterns
    List<String> patternValues = new ArrayList<String>(regexMap.values());
    int showCount = Math.min(patternValues.size(), 10);
    List<String> patternsToShow = patternValues.subList(0, showCount);

    String logMessage = "OAuth redirect URI not allowed: '" + redirectUri
        + "'. Configured redirect URI patterns (showing up to 10 of "
        + patternValues.size() + "): " + patternsToShow;

    LOG.warn(logMessage);

    return "redirect_uri '" + redirectUri
        + "' does not match any configured redirect URI pattern";
  }

  /**
   * Validate that all redirect URIs in a set match at least one configured
   * {@code grouper.oauth.redrectUri.<configId>.regex} pattern.
   *
   * @param redirectUris the redirect URIs to validate
   * @return null if all URIs are allowed, or a descriptive error message
   *   for the first disallowed URI
   */
  public static String validateRedirectUrisAllowed(Set<String> redirectUris) {
    for (String uri : redirectUris) {
      String error = validateRedirectUriAllowed(uri);
      if (error != null) {
        return error;
      }
    }
    return null;
  }

  // ==================== Utilities ====================

  /**
   * generate a unique ID
   * @return uuid string
   */
  public static String generateId() {
    return UUID.randomUUID().toString();
  }

  /**
   * clean up expired OAuth entries from the database
   * @return number of rows deleted
   */
  public static int cleanupExpiredEntries() {
    long nowMicros = System.currentTimeMillis() * 1000L;

    int deletedCodes = new GcDbAccess()
        .sql("delete from grouper_oauth_code where expires_micros is not null and expires_micros < ?")
        .addBindVar(nowMicros)
        .executeSql();

    int deletedPending = new GcDbAccess()
        .sql("delete from grouper_oauth_pend_authz_req where expires_micros is not null and expires_micros < ?")
        .addBindVar(nowMicros)
        .executeSql();

    int total = deletedCodes + deletedPending;
    if (total > 0) {
      LOG.info("Cleaned up " + total + " expired OAuth entries ("
          + deletedCodes + " codes, " + deletedPending + " pending requests)");
    }
    return total;
  }
}
