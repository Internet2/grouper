/**
 * Copyright 2026 Internet2
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
 */
package edu.internet2.middleware.grouper.ws.security;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import edu.internet2.middleware.grouper.authentication.GrouperOidcConfig;
import edu.internet2.middleware.grouper.authentication.GrouperOidcConfig.GrouperOIDCClaimSource;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <pre>
 * Policy checks for WsGrouperOauthAuthentication (OAuth bearer access tokens on Grouper WS).
 *
 * Two checks, both done BEFORE the access token is sent to the identity provider's userinfo
 * endpoint:
 *
 * 1. retrieveConfigAllowedForWs: the OIDC external system the request picked (by the
 *    grouperOidcConfigId request parameter or the ws.security.authn.oauth.clientVersion mapping)
 *    must exist, be enabled, have useForWs=true, and use claimSource=userInfoEndpoint.  Without this
 *    a caller could pick a disabled config, or one set up only for UI login.
 *
 * 2. assertAccessTokenForWs: the token must have been issued for Grouper WS.  The userinfo endpoint
 *    only says the token is valid at the provider, not which resource (aud) or which client
 *    (client_id / azp) it is for, so a token another app obtained would otherwise log in to WS.
 *    The mode is grouper.oidcExternalSystem.&lt;configId&gt;.wsAccessTokenValidation:
 *      jwt            verify the JWT signature with the provider's JWKS (asymmetric algorithms only),
 *                     the issuer, exp / nbf, and the audience and/or client id
 *      introspection  RFC 7662 token introspection with the config's client id and secret, then the
 *                     same audience / client id rules
 *      userInfoOnly   the old behavior (no audience check), explicit opt in, logs a warning
 *      blank          refuse (fail closed), and log an error telling the admin to pick a mode
 *
 * Settings, all in grouper.properties (or the external system config) for that OIDC config id:
 *   grouper.oidcExternalSystem.&lt;configId&gt;.wsAccessTokenValidation       jwt, introspection, userInfoOnly
 *   grouper.oidcExternalSystem.&lt;configId&gt;.wsAccessTokenAudiences        comma separated, token aud must contain one
 *   grouper.oidcExternalSystem.&lt;configId&gt;.wsAccessTokenAllowedClientIds comma separated, client_id or azp must be one
 *   grouper.oidcExternalSystem.&lt;configId&gt;.wsAccessTokenIssuer           required for jwt, checked for introspection if returned
 *   grouper.oidcExternalSystem.&lt;configId&gt;.wsAccessTokenJwksUri          required for jwt
 *   grouper.oidcExternalSystem.&lt;configId&gt;.wsAccessTokenIntrospectionUri required for introspection
 *   grouper.oidcExternalSystem.&lt;configId&gt;.maxClockSkew                  seconds, default 300 (existing key)
 * For jwt and introspection at least one of audiences / allowedClientIds is required.
 *
 * Every refusal throws WsOauthAccessTokenRefusedException.  Its message is for the server log only
 * (it names the config id and the reason, never the token); the caller gets the normal 401.
 *
 * This class only uses APIs that exist from Grouper 5.19.0 on (GrouperOidcConfig getters,
 * GrouperHttpClient, GrouperUtil Jackson helpers, and the core nimbus-jose-jwt classes), so the
 * same file works in every release that has WsGrouperOauthAuthentication.
 * </pre>
 */
public class WsOauthAccessTokenPolicy {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(WsOauthAccessTokenPolicy.class);

  /** validation mode: verify a JWT access token locally */
  public static final String MODE_JWT = "jwt";

  /** validation mode: RFC 7662 token introspection */
  public static final String MODE_INTROSPECTION = "introspection";

  /** validation mode: legacy, userinfo only (no audience check) */
  public static final String MODE_USER_INFO_ONLY = "userInfoOnly";

  /** config prefix of an OIDC external system */
  private static final String CONFIG_PREFIX = "grouper.oidcExternalSystem.";

  /** only asymmetric JWS algorithms.  Never none, and never HS*: the HMAC key would be the client secret, which Grouper also has */
  private static final Set<JWSAlgorithm> ALLOWED_ALGORITHMS = Collections.unmodifiableSet(new LinkedHashSet<JWSAlgorithm>(java.util.Arrays.asList(
      JWSAlgorithm.RS256, JWSAlgorithm.RS384, JWSAlgorithm.RS512,
      JWSAlgorithm.PS256, JWSAlgorithm.PS384, JWSAlgorithm.PS512,
      JWSAlgorithm.ES256, JWSAlgorithm.ES384, JWSAlgorithm.ES512)));

  /** how long to cache a JWKS document */
  private static final long JWKS_CACHE_MILLIS = 5 * 60 * 1000L;

  /** when a kid is not in the cached JWKS (key rotation), refetch, but not more often than this */
  private static final long JWKS_MIN_REFETCH_MILLIS = 60 * 1000L;

  /** jwks uri to cached JWKS */
  private static final Map<String, CachedJwks> jwksCache = new ConcurrentHashMap<String, CachedJwks>();

  /** config ids already warned about userInfoOnly, so the log is not flooded */
  private static final Set<String> userInfoOnlyWarned = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

  /**
   * a JWKS and when it was fetched
   */
  private static class CachedJwks {
    /** the key set */
    private final JWKSet jwkSet;
    /** System.currentTimeMillis() when fetched */
    private final long fetchedMillis;
    /**
     * @param theJwkSet
     * @param theFetchedMillis
     */
    CachedJwks(JWKSet theJwkSet, long theFetchedMillis) {
      this.jwkSet = theJwkSet;
      this.fetchedMillis = theFetchedMillis;
    }
  }

  /**
   * thrown when the config or the token is refused.  The message is for the server log only
   */
  public static class WsOauthAccessTokenRefusedException extends RuntimeException {

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param message reason, never includes the token
     */
    public WsOauthAccessTokenRefusedException(String message) {
      super(message);
    }

    /**
     * @param message reason, never includes the token
     * @param cause
     */
    public WsOauthAccessTokenRefusedException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * no instances
   */
  private WsOauthAccessTokenPolicy() {
  }

  // ---------------------------------------------------------------------------------------------
  // problem 2: which OIDC config the request may pick
  // ---------------------------------------------------------------------------------------------

  /**
   * The OIDC config the request picked must exist, be enabled, be for WS, and read its claims from
   * the userinfo endpoint.  Call this before anything is sent to the provider.
   * @param configId from the grouperOidcConfigId request parameter or the client version mapping
   * @return the config
   * @throws WsOauthAccessTokenRefusedException if not allowed
   */
  public static GrouperOidcConfig retrieveConfigAllowedForWs(String configId) {

    if (isBlank(configId)) {
      throw new WsOauthAccessTokenRefusedException("OIDC config id is blank");
    }

    GrouperOidcConfig grouperOidcConfig = null;
    try {
      grouperOidcConfig = GrouperOidcConfig.retrieveFromConfigOrCache(configId);
    } catch (RuntimeException re) {
      // an unknown config id fails on its required keys.  Do not pass the config details on to the caller
      throw new WsOauthAccessTokenRefusedException("OIDC config '" + configId + "' not found or not valid", re);
    }
    if (grouperOidcConfig == null) {
      throw new WsOauthAccessTokenRefusedException("OIDC config '" + configId + "' not found");
    }

    // the admin kill switch.  enabled defaults to true in GrouperOidcConfig
    if (!grouperOidcConfig.isEnabled()) {
      throw new WsOauthAccessTokenRefusedException("OIDC config '" + configId + "' is not enabled");
    }

    // the existing WS gate (grouper.oidcExternalSystem.<id>.useForWs, default false), which the oidc_ bearer flow already enforces
    if (!grouperOidcConfig.isWs()) {
      throw new WsOauthAccessTokenRefusedException("OIDC config '" + configId + "' does not have useForWs=true");
    }

    // decodeAccessToken only fills in userinfo claims, so an idToken config cannot work on this path
    if (grouperOidcConfig.getClaimSource() != GrouperOIDCClaimSource.userInfoEndpoint) {
      throw new WsOauthAccessTokenRefusedException("OIDC config '" + configId + "' must have claimSource=userInfoEndpoint for WS access tokens");
    }

    return grouperOidcConfig;
  }

  // ---------------------------------------------------------------------------------------------
  // problem 1: was the token issued for Grouper WS
  // ---------------------------------------------------------------------------------------------

  /**
   * Check that the access token was issued for Grouper WS, per the config's wsAccessTokenValidation
   * mode.  Call this after retrieveConfigAllowedForWs and before the userinfo call.
   * @param grouperOidcConfig from retrieveConfigAllowedForWs
   * @param accessToken bearer token from the Authorization header
   * @return the token's sub claim if known (jwt, introspection), else null (userInfoOnly)
   * @throws WsOauthAccessTokenRefusedException if refused
   */
  public static String assertAccessTokenForWs(GrouperOidcConfig grouperOidcConfig, String accessToken) {

    String configId = grouperOidcConfig.getClientConfigId();

    if (isBlank(accessToken)) {
      throw new WsOauthAccessTokenRefusedException("access token is blank, config '" + configId + "'");
    }

    String mode = configString(configId, "wsAccessTokenValidation");

    if (isBlank(mode)) {
      // fail closed: before this setting existed every token the userinfo endpoint accepted was trusted
      LOG.error("WS OAuth access tokens are refused for OIDC config '" + configId + "' until "
          + CONFIG_PREFIX + configId + ".wsAccessTokenValidation is set to jwt, introspection, or userInfoOnly (not recommended)");
      throw new WsOauthAccessTokenRefusedException("wsAccessTokenValidation is not set for OIDC config '" + configId + "'");
    }

    if (MODE_USER_INFO_ONLY.equals(mode)) {
      if (userInfoOnlyWarned.add(configId)) {
        LOG.warn("OIDC config '" + configId + "' has wsAccessTokenValidation=userInfoOnly: WS accepts any access token "
            + "the userinfo endpoint accepts, including tokens issued to other applications.  Use jwt or introspection");
      }
      return null;
    }

    Set<String> audiences = configSet(configId, "wsAccessTokenAudiences");
    Set<String> allowedClientIds = configSet(configId, "wsAccessTokenAllowedClientIds");

    // at least one way to tell that the token is for Grouper WS
    if (audiences.size() == 0 && allowedClientIds.size() == 0) {
      LOG.error("OIDC config '" + configId + "' has wsAccessTokenValidation=" + mode
          + " but neither wsAccessTokenAudiences nor wsAccessTokenAllowedClientIds is set, so all WS access tokens are refused");
      throw new WsOauthAccessTokenRefusedException("no wsAccessTokenAudiences or wsAccessTokenAllowedClientIds for OIDC config '" + configId + "'");
    }

    if (MODE_JWT.equals(mode)) {
      return assertJwt(grouperOidcConfig, accessToken, audiences, allowedClientIds);
    }

    if (MODE_INTROSPECTION.equals(mode)) {
      return assertIntrospection(grouperOidcConfig, accessToken, audiences, allowedClientIds);
    }

    throw new WsOauthAccessTokenRefusedException("invalid wsAccessTokenValidation '" + mode + "' for OIDC config '" + configId
        + "', expecting jwt, introspection, or userInfoOnly");
  }

  /**
   * After the userinfo call: when the token's own sub is known, userinfo must be about the same user
   * @param grouperOidcConfig
   * @param tokenSubject sub from assertAccessTokenForWs, or null to skip
   * @param userInfoAttributes claims from the userinfo endpoint
   * @throws WsOauthAccessTokenRefusedException if they differ
   */
  public static void assertUserInfoSubjectMatches(GrouperOidcConfig grouperOidcConfig, String tokenSubject,
      Map<String, String> userInfoAttributes) {
    if (tokenSubject == null) {
      return;
    }
    String userInfoSubject = userInfoAttributes == null ? null : userInfoAttributes.get("sub");
    if (!tokenSubject.equals(userInfoSubject)) {
      throw new WsOauthAccessTokenRefusedException("userinfo sub does not match the access token sub, OIDC config '"
          + grouperOidcConfig.getClientConfigId() + "'");
    }
  }

  // ---------------------------------------------------------------------------------------------
  // jwt mode
  // ---------------------------------------------------------------------------------------------

  /**
   * verify a JWT access token locally
   * @param grouperOidcConfig
   * @param accessToken
   * @param audiences
   * @param allowedClientIds
   * @return the sub claim
   */
  private static String assertJwt(GrouperOidcConfig grouperOidcConfig, String accessToken,
      Set<String> audiences, Set<String> allowedClientIds) {

    String configId = grouperOidcConfig.getClientConfigId();

    String issuer = configString(configId, "wsAccessTokenIssuer");
    String jwksUri = configString(configId, "wsAccessTokenJwksUri");
    if (isBlank(issuer) || isBlank(jwksUri)) {
      LOG.error("OIDC config '" + configId + "' has wsAccessTokenValidation=jwt but wsAccessTokenIssuer or wsAccessTokenJwksUri is not set");
      throw new WsOauthAccessTokenRefusedException("wsAccessTokenIssuer and wsAccessTokenJwksUri are required for jwt, OIDC config '" + configId + "'");
    }

    SignedJWT signedJWT = null;
    try {
      // an unsigned (alg none) token fails here: it is not a JWS
      signedJWT = SignedJWT.parse(accessToken);
    } catch (Exception e) {
      throw new WsOauthAccessTokenRefusedException("access token is not a signed JWT, OIDC config '" + configId + "'");
    }

    JWSAlgorithm algorithm = signedJWT.getHeader().getAlgorithm();
    if (algorithm == null || !ALLOWED_ALGORITHMS.contains(algorithm)) {
      throw new WsOauthAccessTokenRefusedException("access token algorithm '" + algorithm + "' is not allowed, OIDC config '" + configId + "'");
    }

    String keyId = signedJWT.getHeader().getKeyID();
    JWK jwk = findKey(grouperOidcConfig, jwksUri, keyId, algorithm, false);
    if (jwk == null) {
      // maybe the provider rotated keys
      jwk = findKey(grouperOidcConfig, jwksUri, keyId, algorithm, true);
    }
    if (jwk == null) {
      throw new WsOauthAccessTokenRefusedException("no signing key for the access token (kid '" + keyId + "') in the JWKS, OIDC config '" + configId + "'");
    }

    boolean verified = false;
    try {
      JWSVerifier verifier = null;
      if (jwk instanceof RSAKey) {
        verifier = new RSASSAVerifier((RSAKey)jwk);
      } else if (jwk instanceof ECKey) {
        verifier = new ECDSAVerifier((ECKey)jwk);
      }
      verified = verifier != null && signedJWT.verify(verifier);
    } catch (Exception e) {
      throw new WsOauthAccessTokenRefusedException("access token signature could not be verified, OIDC config '" + configId + "'", e);
    }
    if (!verified) {
      throw new WsOauthAccessTokenRefusedException("access token signature is not valid, OIDC config '" + configId + "'");
    }

    JWTClaimsSet claims = null;
    String clientId = null;
    try {
      claims = signedJWT.getJWTClaimsSet();
      clientId = claims.getStringClaim("client_id");
      if (isBlank(clientId)) {
        clientId = claims.getStringClaim("azp");
      }
    } catch (Exception e) {
      throw new WsOauthAccessTokenRefusedException("access token claims could not be parsed, OIDC config '" + configId + "'");
    }

    if (!issuer.equals(claims.getIssuer())) {
      throw new WsOauthAccessTokenRefusedException("access token issuer does not match wsAccessTokenIssuer, OIDC config '" + configId + "'");
    }

    long skewMillis = maxClockSkewSeconds(configId) * 1000L;
    long now = System.currentTimeMillis();
    Date expirationTime = claims.getExpirationTime();
    if (expirationTime == null) {
      throw new WsOauthAccessTokenRefusedException("access token has no exp, OIDC config '" + configId + "'");
    }
    if (now > expirationTime.getTime() + skewMillis) {
      throw new WsOauthAccessTokenRefusedException("access token is expired, OIDC config '" + configId + "'");
    }
    Date notBeforeTime = claims.getNotBeforeTime();
    if (notBeforeTime != null && now < notBeforeTime.getTime() - skewMillis) {
      throw new WsOauthAccessTokenRefusedException("access token is not valid yet (nbf), OIDC config '" + configId + "'");
    }

    assertAudienceAndClient(configId, claims.getAudience(), clientId, audiences, allowedClientIds);

    String subject = claims.getSubject();
    if (isBlank(subject)) {
      throw new WsOauthAccessTokenRefusedException("access token has no sub, OIDC config '" + configId + "'");
    }
    return subject;
  }

  /**
   * find the verification key in the JWKS
   * @param grouperOidcConfig for the proxy settings
   * @param jwksUri
   * @param keyId kid from the token header, may be null
   * @param algorithm from the token header
   * @param refetch true to refetch (rate limited) instead of using the cache
   * @return the key or null
   */
  private static JWK findKey(GrouperOidcConfig grouperOidcConfig, String jwksUri, String keyId,
      JWSAlgorithm algorithm, boolean refetch) {

    JWKSet jwkSet = retrieveJwks(grouperOidcConfig, jwksUri, refetch);
    if (jwkSet == null) {
      return null;
    }

    boolean ecAlgorithm = algorithm.getName().startsWith("ES");

    JWK found = null;
    for (JWK jwk : jwkSet.getKeys()) {

      // key type must match the algorithm family
      if (ecAlgorithm ? !(jwk instanceof ECKey) : !(jwk instanceof RSAKey)) {
        continue;
      }
      // only signing keys
      if (jwk.getKeyUse() != null && !KeyUse.SIGNATURE.equals(jwk.getKeyUse())) {
        continue;
      }
      // if the key says which algorithm it is for, it must be this one
      if (jwk.getAlgorithm() != null && !algorithm.getName().equals(jwk.getAlgorithm().getName())) {
        continue;
      }
      if (keyId != null) {
        if (keyId.equals(jwk.getKeyID())) {
          return jwk;
        }
        continue;
      }
      // no kid in the token: only accept when exactly one key could match
      if (found != null) {
        return null;
      }
      found = jwk;
    }
    return found;
  }

  /**
   * get the JWKS from the cache or the provider
   * @param grouperOidcConfig for the proxy settings
   * @param jwksUri
   * @param refetch true to refetch if the cached one is older than the minimum refetch time
   * @return the key set or null if it could not be retrieved
   */
  private static JWKSet retrieveJwks(GrouperOidcConfig grouperOidcConfig, String jwksUri, boolean refetch) {

    long now = System.currentTimeMillis();
    CachedJwks cachedJwks = jwksCache.get(jwksUri);

    if (cachedJwks != null) {
      long age = now - cachedJwks.fetchedMillis;
      if (refetch ? age < JWKS_MIN_REFETCH_MILLIS : age < JWKS_CACHE_MILLIS) {
        return refetch ? null : cachedJwks.jwkSet;
      }
    }

    try {
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient()
          .assignUrl(jwksUri)
          .assignGrouperHttpMethod(GrouperHttpMethod.get)
          .assignProxyUrl(grouperOidcConfig.getProxyUrl())
          .assignProxyType(grouperOidcConfig.getProxyType())
          .executeRequest();
      if (grouperHttpClient.getResponseCode() != 200) {
        throw new RuntimeException("response code " + grouperHttpClient.getResponseCode());
      }
      JWKSet jwkSet = JWKSet.parse(grouperHttpClient.getResponseBody());
      jwksCache.put(jwksUri, new CachedJwks(jwkSet, now));
      return jwkSet;
    } catch (Exception e) {
      LOG.error("Error retrieving JWKS for OIDC config '" + grouperOidcConfig.getClientConfigId() + "' from " + jwksUri, e);
      // keep using the last good key set if there is one
      return cachedJwks == null ? null : cachedJwks.jwkSet;
    }
  }

  // ---------------------------------------------------------------------------------------------
  // introspection mode
  // ---------------------------------------------------------------------------------------------

  /**
   * RFC 7662 token introspection, authenticated with the config's client id and secret
   * @param grouperOidcConfig
   * @param accessToken
   * @param audiences
   * @param allowedClientIds
   * @return the sub claim, or null if the introspection response has none
   */
  private static String assertIntrospection(GrouperOidcConfig grouperOidcConfig, String accessToken,
      Set<String> audiences, Set<String> allowedClientIds) {

    String configId = grouperOidcConfig.getClientConfigId();

    String introspectionUri = configString(configId, "wsAccessTokenIntrospectionUri");
    if (isBlank(introspectionUri)) {
      LOG.error("OIDC config '" + configId + "' has wsAccessTokenValidation=introspection but wsAccessTokenIntrospectionUri is not set");
      throw new WsOauthAccessTokenRefusedException("wsAccessTokenIntrospectionUri is required for introspection, OIDC config '" + configId + "'");
    }

    JsonNode response = null;
    try {
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient()
          .assignUrl(introspectionUri)
          .assignGrouperHttpMethod(GrouperHttpMethod.post)
          .assignProxyUrl(grouperOidcConfig.getProxyUrl())
          .assignProxyType(grouperOidcConfig.getProxyType())
          .assignUser(grouperOidcConfig.getClientId())
          .assignPassword(grouperOidcConfig.getClientSecret())
          // never log the token
          .assignDoNotLogParameters("token")
          .assignDoNotLogResponseBody(true)
          .addBodyParameter("token", accessToken)
          .addBodyParameter("token_type_hint", "access_token")
          .addHeader("Accept", "application/json")
          .executeRequest();
      if (grouperHttpClient.getResponseCode() != 200) {
        throw new RuntimeException("response code " + grouperHttpClient.getResponseCode());
      }
      response = GrouperUtil.jsonJacksonNode(grouperHttpClient.getResponseBody());
    } catch (Exception e) {
      LOG.error("Error calling the introspection endpoint for OIDC config '" + configId + "': " + introspectionUri, e);
      throw new WsOauthAccessTokenRefusedException("introspection failed, OIDC config '" + configId + "'", e);
    }

    // active is the only field RFC 7662 requires
    JsonNode activeNode = response == null ? null : response.get("active");
    if (activeNode == null || !activeNode.isBoolean() || !activeNode.booleanValue()) {
      throw new WsOauthAccessTokenRefusedException("access token is not active, OIDC config '" + configId + "'");
    }

    // issuer is optional in the response, but if both are known they must match
    String issuer = configString(configId, "wsAccessTokenIssuer");
    String responseIssuer = textOrNull(response.get("iss"));
    if (!isBlank(issuer) && responseIssuer != null && !issuer.equals(responseIssuer)) {
      throw new WsOauthAccessTokenRefusedException("introspection iss does not match wsAccessTokenIssuer, OIDC config '" + configId + "'");
    }

    JsonNode expNode = response.get("exp");
    if (expNode != null && expNode.isNumber()) {
      long skewMillis = maxClockSkewSeconds(configId) * 1000L;
      if (System.currentTimeMillis() > expNode.longValue() * 1000L + skewMillis) {
        throw new WsOauthAccessTokenRefusedException("access token is expired, OIDC config '" + configId + "'");
      }
    }

    // aud may be a string or an array
    Set<String> tokenAudiences = new LinkedHashSet<String>();
    JsonNode audNode = response.get("aud");
    if (audNode != null) {
      if (audNode.isArray()) {
        Iterator<JsonNode> iterator = audNode.elements();
        while (iterator.hasNext()) {
          String aud = textOrNull(iterator.next());
          if (aud != null) {
            tokenAudiences.add(aud);
          }
        }
      } else {
        String aud = textOrNull(audNode);
        if (aud != null) {
          tokenAudiences.add(aud);
        }
      }
    }

    String clientId = textOrNull(response.get("client_id"));
    if (isBlank(clientId)) {
      clientId = textOrNull(response.get("azp"));
    }

    assertAudienceAndClient(configId, new java.util.ArrayList<String>(tokenAudiences), clientId, audiences, allowedClientIds);

    return textOrNull(response.get("sub"));
  }

  // ---------------------------------------------------------------------------------------------
  // shared
  // ---------------------------------------------------------------------------------------------

  /**
   * the audience and client id rules: each one that is configured must pass.  A missing field refuses.
   * @param configId
   * @param tokenAudiences aud of the token
   * @param tokenClientId client_id or azp of the token
   * @param audiences configured, may be empty
   * @param allowedClientIds configured, may be empty
   */
  private static void assertAudienceAndClient(String configId, List<String> tokenAudiences, String tokenClientId,
      Set<String> audiences, Set<String> allowedClientIds) {

    if (audiences.size() > 0) {
      boolean match = false;
      for (String aud : GrouperUtil.nonNull(tokenAudiences)) {
        if (audiences.contains(aud)) {
          match = true;
          break;
        }
      }
      if (!match) {
        throw new WsOauthAccessTokenRefusedException("access token aud is not in wsAccessTokenAudiences, OIDC config '" + configId + "'");
      }
    }

    if (allowedClientIds.size() > 0) {
      if (isBlank(tokenClientId) || !allowedClientIds.contains(tokenClientId)) {
        throw new WsOauthAccessTokenRefusedException("access token client id '" + tokenClientId
            + "' is not in wsAccessTokenAllowedClientIds, OIDC config '" + configId + "'");
      }
    }
  }

  /**
   * @param configId
   * @return the existing maxClockSkew setting for the config, default 300 seconds
   */
  private static int maxClockSkewSeconds(String configId) {
    return GrouperConfig.retrieveConfig().propertyValueInt(CONFIG_PREFIX + configId + ".maxClockSkew", 300);
  }

  /**
   * @param configId
   * @param suffix
   * @return the trimmed config value or null
   */
  private static String configString(String configId, String suffix) {
    String value = GrouperConfig.retrieveConfig().propertyValueString(CONFIG_PREFIX + configId + "." + suffix);
    return isBlank(value) ? null : value.trim();
  }

  /**
   * @param configId
   * @param suffix
   * @return the comma separated config value as a set, never null
   */
  private static Set<String> configSet(String configId, String suffix) {
    Set<String> result = new LinkedHashSet<String>();
    String value = configString(configId, suffix);
    if (value != null) {
      for (String item : value.split(",")) {
        if (!isBlank(item)) {
          result.add(item.trim());
        }
      }
    }
    return result;
  }

  /**
   * @param jsonNode
   * @return the text of a text node, else null
   */
  private static String textOrNull(JsonNode jsonNode) {
    return jsonNode != null && jsonNode.isTextual() ? jsonNode.textValue() : null;
  }

  /**
   * no commons-lang here: 5.x and 6.x+ use different versions
   * @param string
   * @return true if null or whitespace
   */
  private static boolean isBlank(String string) {
    return string == null || string.trim().length() == 0;
  }

}
