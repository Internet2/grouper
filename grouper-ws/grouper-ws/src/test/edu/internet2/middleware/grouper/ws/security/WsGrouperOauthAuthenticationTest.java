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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import junit.textui.TestRunner;

/**
 * <pre>
 * SECURITY tests for WsGrouperOauthAuthentication (ws.security.non-rampart.authentication.class
 * for OAuth bearer access tokens), issue advisory H7.
 *
 * Problem 2: the caller picks the OIDC config (grouperOidcConfigId request parameter), and the
 * class trusted any config, even one with enabled=false or useForWs=false.  Now the config must be
 * enabled with useForWs=true, and nothing is sent to the provider otherwise.
 *
 * Problem 1: the class trusted any token the userinfo endpoint accepted, including tokens issued to
 * other applications.  Now the token must be for Grouper WS, per
 * grouper.oidcExternalSystem.&lt;id&gt;.wsAccessTokenValidation (jwt, introspection, userInfoOnly; blank refuses).
 *
 * A stub identity provider (com.sun.net.httpserver) serves userinfo, JWKS and introspection and
 * counts its hits.  The MCP custom-auth path (grouper.mcp.auth.customAuthClass=true,
 * grouper.mcp.auth.oauth=false) calls the same retrieveLoggedInSubjectId, so these cases cover it.
 *
 * Before the fix this does not compile (WsOauthAccessTokenPolicy is missing); the behavior it guards
 * against is that the disabled and non-WS configs log in as test.subject.0.
 * </pre>
 */
public class WsGrouperOauthAuthenticationTest extends GrouperTest {

  /** issuer of the stub provider */
  private static final String ISSUER = "https://idp.test.example/";

  /** the Grouper WS resource identifier */
  private static final String WS_AUDIENCE = "https://grouper.test.example/grouper-ws";

  /** the Grouper WS OAuth client id */
  private static final String WS_CLIENT_ID = "grouperWsClient";

  /** client secret of the config (also the HS256 key an attacker might try) */
  private static final String CLIENT_SECRET = "thisIsTheClientSecretOfTheConfig0123456789";

  /** what the result is when the fallback class ran */
  private static final String FALLBACK_SUBJECT_ID = "fallback.subject.ran";

  /**
   * fallback class, so the tests can tell a refusal (null) from the fallback running
   */
  public static class TestFallbackAuthentication implements WsCustomAuthentication {
    /**
     * @see WsCustomAuthentication#retrieveLoggedInSubjectId(HttpServletRequest)
     */
    public String retrieveLoggedInSubjectId(HttpServletRequest httpServletRequest) {
      return FALLBACK_SUBJECT_ID;
    }
  }

  /** stub provider */
  private HttpServer httpServer;

  /** stub base url */
  private String baseUrl;

  /** hits on the stub userinfo endpoint */
  private AtomicInteger userInfoHits = new AtomicInteger();

  /** hits on the stub introspection endpoint */
  private AtomicInteger introspectionHits = new AtomicInteger();

  /** hits on the stub jwks endpoint */
  private AtomicInteger jwksHits = new AtomicInteger();

  /** sub the stub userinfo returns */
  private volatile String userInfoSub = SubjectTestHelper.SUBJ0_ID;

  /** body the stub introspection returns */
  private volatile String introspectionBody = "{\"active\":false}";

  /** signing key published in the stub JWKS */
  private RSAKey signingKey;

  /** a key that is not published */
  private RSAKey unpublishedKey;

  /** config keys set by this test, removed in tearDown */
  private Map<String, String> grouperConfigKeys = new HashMap<String, String>();

  /** unique config id suffix: GrouperOidcConfig caches configs by id for a minute */
  private static int configCounter = 0;

  /**
   *
   */
  public WsGrouperOauthAuthenticationTest() {
    super();
  }

  /**
   * @param name
   */
  public WsGrouperOauthAuthenticationTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new WsGrouperOauthAuthenticationTest("testJwtValidTokenLogsIn"));
  }

  /**
   * start the stub provider
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(2048);
      KeyPair keyPair = keyPairGenerator.generateKeyPair();
      this.signingKey = new RSAKey.Builder((RSAPublicKey)keyPair.getPublic()).privateKey((RSAPrivateKey)keyPair.getPrivate())
          .keyID("k1").keyUse(KeyUse.SIGNATURE).build();
      KeyPair otherKeyPair = keyPairGenerator.generateKeyPair();
      this.unpublishedKey = new RSAKey.Builder((RSAPublicKey)otherKeyPair.getPublic()).privateKey((RSAPrivateKey)otherKeyPair.getPrivate())
          .keyID("k2").keyUse(KeyUse.SIGNATURE).build();

      this.httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      this.httpServer.createContext("/userinfo", new HttpHandler() {
        public void handle(HttpExchange exchange) throws IOException {
          WsGrouperOauthAuthenticationTest.this.userInfoHits.incrementAndGet();
          respond(exchange, "{\"sub\":\"" + WsGrouperOauthAuthenticationTest.this.userInfoSub + "\"}");
        }
      });
      this.httpServer.createContext("/jwks", new HttpHandler() {
        public void handle(HttpExchange exchange) throws IOException {
          WsGrouperOauthAuthenticationTest.this.jwksHits.incrementAndGet();
          // public part only
          respond(exchange, new JWKSet(WsGrouperOauthAuthenticationTest.this.signingKey.toPublicJWK()).toString());
        }
      });
      this.httpServer.createContext("/introspect", new HttpHandler() {
        public void handle(HttpExchange exchange) throws IOException {
          WsGrouperOauthAuthenticationTest.this.introspectionHits.incrementAndGet();
          respond(exchange, WsGrouperOauthAuthenticationTest.this.introspectionBody);
        }
      });
      this.httpServer.start();
      this.baseUrl = "http://127.0.0.1:" + this.httpServer.getAddress().getPort();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // the fallback class, so a refusal (null) can be told apart from the fallback running
    GrouperWsConfig.retrieveConfig().propertiesOverrideMap().put("ws.security.authn.oauth.fallback.class",
        TestFallbackAuthentication.class.getName());
  }

  /**
   * stop the stub, remove config
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    if (this.httpServer != null) {
      this.httpServer.stop(0);
    }
    for (String key : this.grouperConfigKeys.keySet()) {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(key);
    }
    GrouperWsConfig.retrieveConfig().propertiesOverrideMap().remove("ws.security.authn.oauth.fallback.class");
    super.tearDown();
  }

  /**
   * write a JSON response
   * @param exchange
   * @param body
   * @throws IOException
   */
  private static void respond(HttpExchange exchange, String body) throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().add("Content-Type", "application/json");
    exchange.sendResponseHeaders(200, bytes.length);
    OutputStream outputStream = exchange.getResponseBody();
    outputStream.write(bytes);
    outputStream.close();
  }

  /**
   * set a grouper.properties override and remember it for tearDown
   * @param key
   * @param value
   */
  private void grouperConfig(String key, String value) {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(key, value);
    this.grouperConfigKeys.put(key, value);
  }

  /**
   * configure a new OIDC external system pointing at the stub
   * @param enabled
   * @param useForWs
   * @param mode wsAccessTokenValidation, or null for blank
   * @return the config id
   */
  private String configure(boolean enabled, boolean useForWs, String mode) {
    String configId = "wsOauthTest" + (++configCounter) + "_" + System.currentTimeMillis();
    String prefix = "grouper.oidcExternalSystem." + configId + ".";
    grouperConfig(prefix + "enabled", Boolean.toString(enabled));
    grouperConfig(prefix + "useForWs", Boolean.toString(useForWs));
    grouperConfig(prefix + "useConfigurationMetadata", "false");
    grouperConfig(prefix + "claimSource", "userInfoEndpoint");
    grouperConfig(prefix + "userInfoUri", this.baseUrl + "/userinfo");
    grouperConfig(prefix + "tokenEndpointUri", this.baseUrl + "/token");
    grouperConfig(prefix + "authorizeUri", this.baseUrl + "/authorize");
    grouperConfig(prefix + "clientId", WS_CLIENT_ID);
    grouperConfig(prefix + "clientSecret", CLIENT_SECRET);
    grouperConfig(prefix + "scope", "openid");
    grouperConfig(prefix + "oidcResponseType", "code");
    grouperConfig(prefix + "subjectSourceId", "jdbc");
    grouperConfig(prefix + "subjectIdType", "subjectId");
    grouperConfig(prefix + "subjectIdClaimName", "sub");
    if (mode != null) {
      grouperConfig(prefix + "wsAccessTokenValidation", mode);
    }
    grouperConfig(prefix + "wsAccessTokenIssuer", ISSUER);
    grouperConfig(prefix + "wsAccessTokenJwksUri", this.baseUrl + "/jwks");
    grouperConfig(prefix + "wsAccessTokenIntrospectionUri", this.baseUrl + "/introspect");
    grouperConfig(prefix + "wsAccessTokenAudiences", WS_AUDIENCE);
    return configId;
  }

  /**
   * a WS request with a bearer token and the config id as a request parameter
   * @param configId may be null
   * @param accessToken may be null
   * @return the request
   */
  private static HttpServletRequest request(final String configId, final String accessToken) {
    InvocationHandler handler = new InvocationHandler() {
      public Object invoke(Object proxy, Method method, Object[] args) {
        if ("getParameter".equals(method.getName()) && "grouperOidcConfigId".equals(args[0])) {
          return configId;
        }
        if ("getHeader".equals(method.getName()) && "Authorization".equals(args[0])) {
          return accessToken == null ? null : "Bearer " + accessToken;
        }
        if ("getRequestURI".equals(method.getName())) {
          return "/grouper-ws/servicesRest/v4_0_000/groups";
        }
        return null;
      }
    };
    return (HttpServletRequest) Proxy.newProxyInstance(WsGrouperOauthAuthenticationTest.class.getClassLoader(),
        new Class<?>[] { HttpServletRequest.class }, handler);
  }

  /**
   * claims of a good token for Grouper WS
   * @return the builder
   */
  private static JWTClaimsSet.Builder goodClaims() {
    return new JWTClaimsSet.Builder().issuer(ISSUER).audience(WS_AUDIENCE).subject(SubjectTestHelper.SUBJ0_ID)
        .claim("client_id", WS_CLIENT_ID)
        .issueTime(new Date()).expirationTime(new Date(System.currentTimeMillis() + 3600 * 1000L));
  }

  /**
   * sign a token with an RSA key
   * @param rsaKey
   * @param claims
   * @return the serialized token
   */
  private static String sign(RSAKey rsaKey, JWTClaimsSet claims) {
    try {
      SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(), claims);
      signedJWT.sign(new RSASSASigner(rsaKey));
      return signedJWT.serialize();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param configId
   * @param accessToken
   * @return the logged in subject id, or null
   */
  private static String login(String configId, String accessToken) {
    return new WsGrouperOauthAuthentication().retrieveLoggedInSubjectId(request(configId, accessToken));
  }

  /**
   * control: an enabled WS config and a good token for Grouper WS logs in
   */
  /**
   * what the authenticator returns for SUBJ0: sourceId::::subjectId, so the logged in subject is the
   * one the token resolved to, not whatever has that id in the default source (2026-11-10)
   * @return the login string
   */
  private static String subj0Login() {
    return SubjectTestHelper.SUBJ0.getSourceId() + GrouperWsConfig.WS_SEPARATOR + SubjectTestHelper.SUBJ0_ID;
  }

  /**
   * a WS "Bearer oidc_<configId>_<code>" header for a config an admin disabled is refused before the
   * code is exchanged (2026-11-10, the GrouperOidc change ships in advisory H7's file sets)
   */
  public void testDisabledOidcConfigBearerHeaderRefused() {
    // the bearer header pattern does not allow underscores in the config id, so build this config by hand;
    // grouperConfig() records each key and tearDown removes them
    String configId = "secPatch06disabled" + System.currentTimeMillis();
    String prefix = "grouper.oidcExternalSystem." + configId + ".";
    grouperConfig(prefix + "enabled", "false");
    grouperConfig(prefix + "useForWs", "true");
    grouperConfig(prefix + "useConfigurationMetadata", "false");
    grouperConfig(prefix + "claimSource", "userInfoEndpoint");
    grouperConfig(prefix + "userInfoUri", this.baseUrl + "/userinfo");
    grouperConfig(prefix + "tokenEndpointUri", this.baseUrl + "/token");
    grouperConfig(prefix + "authorizeUri", this.baseUrl + "/authorize");
    grouperConfig(prefix + "clientId", WS_CLIENT_ID);
    grouperConfig(prefix + "clientSecret", CLIENT_SECRET);
    grouperConfig(prefix + "scope", "openid");
    grouperConfig(prefix + "oidcResponseType", "code");
    grouperConfig(prefix + "subjectSourceId", "jdbc");
    grouperConfig(prefix + "subjectIdType", "subjectId");
    grouperConfig(prefix + "subjectIdClaimName", "sub");
    try {
      new edu.internet2.middleware.grouper.authentication.GrouperOidc()
          .assignBearerTokenHeader("Bearer oidc_" + configId + "_someCode").assignWs(true).decode();
      fail("a disabled OIDC config must not authenticate");
    } catch (RuntimeException re) {
      assertTrue(re.toString(), GrouperUtilHelper.causeChainContains(re, "is not enabled in the external system"));
    }
  }

  /**
   * small helper: does any exception in the cause chain mention the text
   */
  private static class GrouperUtilHelper {
    static boolean causeChainContains(Throwable t, String text) {
      for (Throwable c = t; c != null; c = c.getCause()) {
        if (c.getMessage() != null && c.getMessage().contains(text)) {
          return true;
        }
      }
      return false;
    }
  }

  public void testJwtValidTokenLogsIn() {
    String configId = configure(true, true, WsOauthAccessTokenPolicy.MODE_JWT);
    assertEquals(subj0Login(), login(configId, sign(this.signingKey, goodClaims().build())));
    assertEquals(1, this.userInfoHits.get());
  }

  /**
   * problem 2: enabled=false, useForWs=false, an unknown config id: refused before anything is sent
   * to the provider, and the fallback does not run
   */
  public void testConfigNotAllowedForWsRefusedBeforeUserInfo() {
    String token = sign(this.signingKey, goodClaims().build());

    String disabled = configure(false, true, WsOauthAccessTokenPolicy.MODE_JWT);
    assertNull(login(disabled, token));

    // the reproduced case: a config set up for UI login only
    String notForWs = configure(true, false, WsOauthAccessTokenPolicy.MODE_JWT);
    assertNull(login(notForWs, token));

    assertNull(login("wsOauthTestDoesNotExist" + System.currentTimeMillis(), token));

    assertEquals(0, this.userInfoHits.get());
    assertEquals(0, this.jwksHits.get());
    assertEquals(0, this.introspectionHits.get());
  }

  /**
   * control: no bearer token still goes to the fallback class
   */
  public void testNoBearerTokenRunsFallback() {
    String configId = configure(true, true, WsOauthAccessTokenPolicy.MODE_JWT);
    assertEquals(FALLBACK_SUBJECT_ID, login(configId, null));
  }

  /**
   * problem 1, jwt mode: wrong aud, wrong iss, expired, alg none, HS256 with the client secret,
   * unknown key, and a userinfo sub that is not the token sub are all refused
   */
  public void testJwtTokenNotForGrouperWsRefused() throws Exception {
    String configId = configure(true, true, WsOauthAccessTokenPolicy.MODE_JWT);

    // a token another app obtained: right provider, wrong audience
    assertNull(login(configId, sign(this.signingKey, goodClaims().audience("https://survey.test.example").build())));

    assertNull(login(configId, sign(this.signingKey, goodClaims().issuer("https://other-idp.test.example/").build())));

    assertNull(login(configId, sign(this.signingKey, goodClaims()
        .expirationTime(new Date(System.currentTimeMillis() - 3600 * 1000L)).build())));

    assertNull(login(configId, new PlainJWT(goodClaims().build()).serialize()));

    SignedJWT hmac = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), goodClaims().build());
    hmac.sign(new MACSigner(CLIENT_SECRET.getBytes(StandardCharsets.UTF_8)));
    assertNull(login(configId, hmac.serialize()));

    assertNull(login(configId, sign(this.unpublishedKey, goodClaims().build())));

    // none of those reached userinfo
    assertEquals(0, this.userInfoHits.get());

    // a good token, but userinfo is about someone else
    this.userInfoSub = SubjectTestHelper.SUBJ1_ID;
    assertNull(login(configId, sign(this.signingKey, goodClaims().build())));
  }

  /**
   * jwt mode with only an allowed client id list (providers without a resource-specific aud)
   */
  public void testJwtAllowedClientIds() {
    String configId = configure(true, true, WsOauthAccessTokenPolicy.MODE_JWT);
    String prefix = "grouper.oidcExternalSystem." + configId + ".";
    grouperConfig(prefix + "wsAccessTokenAudiences", "");
    grouperConfig(prefix + "wsAccessTokenAllowedClientIds", WS_CLIENT_ID);

    assertEquals(subj0Login(), login(configId, sign(this.signingKey,
        goodClaims().audience("anything").build())));
    assertNull(login(configId, sign(this.signingKey, goodClaims().claim("client_id", "surveyApp").build())));
  }

  /**
   * introspection mode: inactive, wrong aud, no aud or client id are refused; a good response logs in
   */
  public void testIntrospection() {
    String configId = configure(true, true, WsOauthAccessTokenPolicy.MODE_INTROSPECTION);
    String opaqueToken = "opaque-access-token-0123456789";

    this.introspectionBody = "{\"active\":false}";
    assertNull(login(configId, opaqueToken));

    this.introspectionBody = "{\"active\":true,\"aud\":\"https://survey.test.example\",\"sub\":\"" + SubjectTestHelper.SUBJ0_ID + "\"}";
    assertNull(login(configId, opaqueToken));

    this.introspectionBody = "{\"active\":true,\"sub\":\"" + SubjectTestHelper.SUBJ0_ID + "\"}";
    assertNull(login(configId, opaqueToken));

    assertEquals(0, this.userInfoHits.get());

    this.introspectionBody = "{\"active\":true,\"aud\":[\"x\",\"" + WS_AUDIENCE + "\"],\"client_id\":\"" + WS_CLIENT_ID
        + "\",\"iss\":\"" + ISSUER + "\",\"sub\":\"" + SubjectTestHelper.SUBJ0_ID + "\"}";
    assertEquals(subj0Login(), login(configId, opaqueToken));
    assertEquals(4, this.introspectionHits.get());
  }

  /**
   * blank wsAccessTokenValidation refuses (fail closed); userInfoOnly is the explicit legacy opt in
   */
  public void testValidationModeBlankRefusedUserInfoOnlyAllowed() {
    String opaqueToken = "opaque-access-token-0123456789";

    String blankMode = configure(true, true, null);
    assertNull(login(blankMode, opaqueToken));
    assertEquals(0, this.userInfoHits.get());

    String legacy = configure(true, true, WsOauthAccessTokenPolicy.MODE_USER_INFO_ONLY);
    assertEquals(subj0Login(), login(legacy, opaqueToken));
  }

}
