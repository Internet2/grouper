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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
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

  /** whether the warning about a missing issuer identifier has been logged */
  private static boolean loggedMissingIssuerIdentifier = false;

  /**
   * The issuer identifier of this Grouper as an OAuth authorization server, or null if it
   * cannot be determined.
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
   * differ between the two, for example behind a reverse proxy or on a different context path.
   * When it is not configured, callers leave the {@code iss} parameter off, which RFC 9207
   * permits, rather than send something which might be wrong.</p>
   *
   * @return the issuer identifier, or null if grouper.ws.url is not configured
   */
  public static String retrieveIssuerIdentifier() {

    String issuerIdentifier = StringUtils.trimToNull(GrouperConfig.getGrouperWsUrl(false));

    if (issuerIdentifier == null && !loggedMissingIssuerIdentifier) {
      loggedMissingIssuerIdentifier = true;
      LOG.warn("grouper.ws.url is not configured, so the iss parameter is left off OAuth "
          + "authorization responses.  Configure it to identify this authorization server to "
          + "clients per RFC 9207.");
    }

    return issuerIdentifier;
  }

  /** whether the warning about a missing MCP resource identifier has been logged */
  private static boolean loggedMissingMcpResourceIdentifier = false;

  /**
   * The canonical URI of the MCP server, which is what an access token issued for it is bound
   * to, or null if it cannot be determined.
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
   * @return the canonical MCP resource URI, or null if grouper.ws.url is not configured
   */
  public static String retrieveMcpResourceIdentifier() {

    String issuerIdentifier = retrieveIssuerIdentifier();

    if (issuerIdentifier == null) {
      if (!loggedMissingMcpResourceIdentifier) {
        loggedMissingMcpResourceIdentifier = true;
        LOG.warn("grouper.ws.url is not configured, so MCP access tokens are neither bound to "
            + "nor checked against the MCP resource.  Configure it so that a token issued for "
            + "another resource cannot be used against MCP.");
      }
      return null;
    }

    return issuerIdentifier + "/mcp";
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
