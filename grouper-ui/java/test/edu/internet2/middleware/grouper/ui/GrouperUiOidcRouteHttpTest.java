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
package edu.internet2.middleware.grouper.ui;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory H7 (uiOidcRouteCanonicalizationBypass), HTTP check against the running local UI:
 * GrouperUiFilter must pick a route-specific UI OIDC config (uiPathRegexes) for every spelling of
 * the protected request (extra parameter first, encoded parameter name, id only in the POST body,
 * encoded dot in the operation, a .oidc? in the query).  GrouperUiOidcRouteSelectorTest covers
 * GrouperUiOidcRouteSelector with a proxy request; this test covers what it cannot: that the
 * filter calls it on real requests, with the real servlet container's parsing.
 *
 * <p>No IdP is available locally, and with the dev form login a session without OIDC results is
 * not redirected, so the test does not look for the IdP redirect.  It uses the selector's own
 * fail-closed rule instead: when two route configs match one request, the filter fails the
 * request ("Multiple OIDC external systems cannot be enabled for UI at the same time").  Phase 1:
 * two configs (secPatch98a, secPatch98b) with the same regex for one group; every spelling of
 * that group's viewGroup must fail, another group must still render.  Phase 2: both configs
 * removed; the same spellings render again, which shows the phase 1 failures came from the route
 * match and nothing else (they also render before the configs are stored).  The regex names only this test's group, so other requests and other
 * tests are not affected.</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which
 * resets the database the running UI uses).  Plain HTTP requests, dev form login as
 * GrouperSystem, OWASP CSRF token from OwaspJavaScriptServlet.  A fresh session per request.</p>
 */
public class GrouperUiOidcRouteHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "secPatch98";

  /** the group the route config protects */
  private static final String PROTECTED_GROUP_NAME = STEM_NAME + ":protected";

  /** a group no config protects */
  private static final String OTHER_GROUP_NAME = STEM_NAME + ":other";

  /** the two route configs */
  private static final String[] CONFIG_IDS = {"secPatch98a", "secPatch98b"};

  /** keys stored per config (the regex is added at runtime) */
  private static final String[] CONFIG_SUFFIXES = {"clientId", "useForUi", "uiPathRegexes", "authorizationEndpoint"};

  /** a response that rendered a normal UI screen into the main content div */
  private static final String MAIN_CONTENT_MARKER = "grouperMainContentDivId";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperUiOidcRouteHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public GrouperUiOidcRouteHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperUiOidcRouteHttpTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- http helpers

  /**
   * base url of the running UI: grouper.ui.url if set, else the junit.test.tomcat.* keys
   * @return e.g. http://localhost:8080/grouper
   */
  static String uiBaseUrl() {
    GrouperConfig config = GrouperConfig.retrieveConfig();
    String uiUrl = config.propertyValueString("grouper.ui.url");
    if (uiUrl != null && uiUrl.trim().length() > 0) {
      return uiUrl.trim().replaceAll("/+$", "");
    }
    boolean ssl = config.propertyValueBoolean("junit.test.tomcat.ssl", false);
    String host = config.propertyValueString("junit.test.tomcat.domainName", "localhost");
    int port = config.propertyValueInt("junit.test.tomcat.port", 8080);
    String context = config.propertyValueString("junit.test.tomcat.uiContext", "grouper");
    return (ssl ? "https" : "http") + "://" + host + ":" + port + "/" + context;
  }

  /**
   * one browser-like session: its own cookie jar, no redirect following, CSRF token after login
   */
  static class UiSession {

    /** this session's cookies */
    private final SimpleCookieJar cookieJar = new SimpleCookieJar();

    /**
     * Authorization header value when the UI uses basic auth (releases without the dev form login,
     * e.g. 7.0-7.3, 6.0-6.4, 5.x, 2.6.19), else null (form login)
     */
    private String basicAuthorization;

    /** e.g. http://localhost:8080/grouper */
    private final String baseUrl;

    /** OWASP CSRF master token of this session */
    private String csrfToken;

    /**
     * @param theBaseUrl
     */
    UiSession(String theBaseUrl) {
      this.baseUrl = theBaseUrl;
    }

    /**
     * form login (dev only, grouper.is.ui.formAuthn), then read the CSRF token
     * @param username
     * @param password
     * @throws Exception
     */
    void login(String username, String password) throws Exception {
      String contextPath = URI.create(this.baseUrl).getPath();
      String body = "grouperFormLoginUsername=" + enc(username)
          + "&grouperFormLoginPassword=" + enc(password)
          + "&grouperFormLoginTarget=" + enc(contextPath + "/grouperUi/app/UiV2Main.index");
      SimpleHttpResponse response = send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + "/grouperUi/app/UiV2Main.index"))
          .header("Content-Type", "application/x-www-form-urlencoded")
          .POST(SimpleHttpRequest.ofString(body)));
      if (response.statusCode() == 401) {
        // no dev form login in this release (GrouperUiFormLogin is in 6.5.0, 7.4.0 and later): the
        // local UI runs with grouper.is.ui.basicAuthn=true, which checks the same UI password on
        // every request
        this.basicAuthorization = "Basic " + java.util.Base64.getEncoder().encodeToString(
            (username + ":" + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        response = send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + "/grouperUi/app/UiV2Main.index")).GET());
        // only a bad password is 401 (a refused section comes later, not as 401)
        assertTrue("basic auth login of " + username + ": " + response.statusCode(), response.statusCode() != 401);
      } else {
        // a good login is a post/redirect/get; a bad one shows the form again (200)
        assertEquals("form login of " + username + " must redirect (without form login set "
            + "grouper.is.ui.basicAuthn=true): " + response.body(), 302, response.statusCode());
      }

      SimpleHttpResponse csrfJs = get("/grouperExternal/public/OwaspJavaScriptServlet");
      Matcher matcher = Pattern.compile("masterTokenValue = '([^']+)'").matcher(csrfJs.body());
      assertTrue("CSRF token for " + username, matcher.find());
      this.csrfToken = matcher.group(1);
    }

    /**
     * @param path after the base url
     * @return the response
     * @throws Exception
     */
    SimpleHttpResponse get(String path) throws Exception {
      return send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + path)).GET());
    }

    /**
     * ajax GET with the CSRF header, like the UI's own requests
     * @param path
     * @return the response
     * @throws Exception
     */
    SimpleHttpResponse getAjax(String path) throws Exception {
      return send(ajax(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + path))).GET());
    }

    /**
     * ajax POST with the CSRF header and a form body
     * @param path
     * @param formBody urlencoded body
     * @return the response
     * @throws Exception
     */
    SimpleHttpResponse postAjax(String path, String formBody) throws Exception {
      return send(ajax(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + path)))
          .header("Content-Type", "application/x-www-form-urlencoded")
          .POST(SimpleHttpRequest.ofString(formBody)));
    }

    /**
     * @param builder
     * @return the builder with the ajax and CSRF headers
     */
    private SimpleHttpRequest ajax(SimpleHttpRequest builder) {
      builder.header("X-Requested-With", "XMLHttpRequest");
      if (this.csrfToken != null) {
        builder.header("OWASPCSRFTOKEN", this.csrfToken);
      }
      return builder;
    }

    /**
     * @param builder
     * @return the response, 15 second timeout
     * @throws Exception
     */
    private SimpleHttpResponse send(SimpleHttpRequest builder) throws Exception {
      if (this.basicAuthorization != null) {
        // basic auth is checked on every request, not kept by the session cookie
        builder.header("Authorization", this.basicAuthorization);
      }
      return builder.send(this.cookieJar, 15000);
    }
  }

  /**
   * @param value
   * @return url encoded
   */
  static String enc(String value) {
    try {
      // the String charset name overload: URLEncoder.encode(String, Charset) is Java 10+
      return URLEncoder.encode(value, "UTF-8");
    } catch (java.io.UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);
    }
  }

  /**
   * @param response
   * @return status and the start of the body, for failure messages
   */
  static String describe(SimpleHttpResponse response) {
    String body = response.body() == null ? "" : response.body();
    return response.statusCode() + " " + response.headers().firstValue("Location").orElse("")
        + " " + body.substring(0, Math.min(300, body.length()));
  }

  /**
   * @param response
   * @return true if a normal UI screen was rendered
   */
  private static boolean renderedScreen(SimpleHttpResponse response) {
    return response.statusCode() == 200 && response.body() != null
        && response.body().contains(MAIN_CONTENT_MARKER);
  }

  /**
   * a fresh GrouperSystem session, one warm-up request (resolves the logged in subject, which is
   * cached in the session), then the probe
   * @param baseUrl
   * @param path after the base url, with the query
   * @param body form body (operation and maybe the group id)
   * @return the probe response
   * @throws Exception
   */
  private static SimpleHttpResponse probe(String baseUrl, String path, String body) throws Exception {
    UiSession session = new UiSession(baseUrl);
    session.login("GrouperSystem", "pass");
    SimpleHttpResponse warmUp = session.postAjax("/grouperUi/app/UiV2Main.indexMain", "operation=UiV2Main.indexMain");
    assertTrue("warm-up home screen: " + describe(warmUp), renderedScreen(warmUp));
    return session.postAjax(path, body);
  }

  /**
   * like probe, but a warm-up that does not render returns null instead of failing: used only while
   * waiting for the UI to drop the deleted route configs, when its config caches can briefly disagree
   * @param baseUrl
   * @param path after the base url, with the query
   * @param body form body (operation and maybe the group id)
   * @return the probe response, or null if the warm-up did not render
   * @throws Exception
   */
  private static SimpleHttpResponse probeOrNull(String baseUrl, String path, String body) throws Exception {
    UiSession session = new UiSession(baseUrl);
    session.login("GrouperSystem", "pass");
    SimpleHttpResponse warmUp = session.postAjax("/grouperUi/app/UiV2Main.indexMain", "operation=UiV2Main.indexMain");
    if (!renderedScreen(warmUp)) {
      return null;
    }
    return session.postAjax(path, body);
  }

  /**
   * the spellings of a viewGroup request for the protected group
   * @param groupId
   * @return {label, path with query, body}
   */
  private static String[][] spellings(String groupId) {
    // percent encode the first character of the id value (the handler sees the same id)
    String encodedValue = "%" + Integer.toHexString(groupId.charAt(0)).toUpperCase() + groupId.substring(1);
    String op = "operation=UiV2Group.viewGroup";
    return new String[][] {
        {"canonical query", "/grouperUi/app/UiV2Group.viewGroup?groupId=" + groupId, op},
        {"extra parameter first", "/grouperUi/app/UiV2Group.viewGroup?x=1&groupId=" + groupId, op},
        {"encoded parameter name group%49d", "/grouperUi/app/UiV2Group.viewGroup?group%49d=" + groupId, op},
        {"encoded value", "/grouperUi/app/UiV2Group.viewGroup?groupId=" + encodedValue, op},
        {"id only in the POST body", "/grouperUi/app/UiV2Group.viewGroup", op + "&groupId=" + groupId},
        {"encoded dot UiV2Group%2EviewGroup", "/grouperUi/app/UiV2Group%2EviewGroup?groupId=" + groupId, op},
        {".oidc? in the query", "/grouperUi/app/UiV2Group.viewGroup?groupId=" + groupId + "&x=.oidc?", op},
    };
  }

  // ---------------------------------------------------------------- setup / cleanup

  /**
   * @param configId
   * @param suffix
   * @param value
   */
  private static void storeKey(String configId, String suffix, String value) {
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouper.oidcExternalSystem." + configId + "." + suffix).value(value).store();
  }

  /**
   * store one route config
   * @param configId
   * @param regex
   */
  private static void storeConfig(String configId, String regex) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      storeKey(configId, "clientId", configId + "Client");
      storeKey(configId, "useForUi", "true");
      storeKey(configId, "uiPathRegexes", regex);
      // unreachable on purpose: a redirect here could never complete a login
      storeKey(configId, "authorizationEndpoint", "http://127.0.0.1:9/authorize");
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete one route config's keys
   * @param configId
   */
  private static void deleteConfig(String configId) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String suffix : CONFIG_SUFFIXES) {
        try {
          new GrouperDbConfig().configFileName("grouper.properties")
              .propertyName("grouper.oidcExternalSystem." + configId + "." + suffix).delete();
        } catch (RuntimeException re) {
          // not there
        }
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the groups and folder
   */
  private static void deleteGroups() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String name : new String[] {PROTECTED_GROUP_NAME, OTHER_GROUP_NAME}) {
        Group group = GroupFinder.findByName(rootSession, name, false);
        if (group != null) {
          group.delete();
        }
      }
      Stem stem = StemFinder.findByName(rootSession, STEM_NAME, false);
      if (stem != null) {
        stem.delete();
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  // ---------------------------------------------------------------- test

  /**
   * every spelling of the protected request is routed to the route config
   * @throws Exception
   */
  public void testEverySpellingSelectsRouteConfig() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    for (String configId : CONFIG_IDS) {
      deleteConfig(configId);
    }
    deleteGroups();
    try {
      String protectedGroupId;
      String otherGroupId;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        protectedGroupId = new GroupSave(rootSession).assignName(PROTECTED_GROUP_NAME)
            .assignCreateParentStemsIfNotExist(true).save().getId();
        otherGroupId = new GroupSave(rootSession).assignName(OTHER_GROUP_NAME).save().getId();
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }
      String regex = "^.*UiV2Group\\.viewGroup[?&]groupId=" + protectedGroupId + ".*$";
      String[][] spellings = spellings(protectedGroupId);
      String otherPath = "/grouperUi/app/UiV2Group.viewGroup?groupId=" + otherGroupId;
      String op = "operation=UiV2Group.viewGroup";

      // before any config: every spelling renders (the requests themselves are valid)
      for (String[] spelling : spellings) {
        SimpleHttpResponse response = probe(baseUrl, spelling[1], spelling[2]);
        assertTrue("without config, " + spelling[0] + " must render: " + describe(response), renderedScreen(response));
      }

      // phase 1: two route configs with the same regex; a request that matches fails closed
      storeConfig(CONFIG_IDS[0], regex);
      storeConfig(CONFIG_IDS[1], regex);
      boolean loaded = false;
      for (int i = 0; i < 24 && !loaded; i++) {
        loaded = !renderedScreen(probe(baseUrl, spellings[0][1], spellings[0][2]));
        if (!loaded) {
          Thread.sleep(5000);
        }
      }
      assertTrue("UI never loaded the secPatch98 route configs", loaded);

      for (String[] spelling : spellings) {
        SimpleHttpResponse response = probe(baseUrl, spelling[1], spelling[2]);
        assertFalse("two matching route configs: " + spelling[0] + " must select the route config (request fails): "
            + describe(response), renderedScreen(response));
      }
      SimpleHttpResponse other = probe(baseUrl, otherPath, op);
      assertTrue("another group matches no route config and renders: " + describe(other), renderedScreen(other));

      // phase 2: configs removed; the same requests render again, so the phase 1 failures were
      // the route match and nothing else
      for (String configId : CONFIG_IDS) {
        deleteConfig(configId);
      }
      // wait until every spelling (and its warm-up) renders in one pass: the UI's config caches drop
      // the deleted configs at slightly different times (seen on 6.3.0), so one render is not enough
      boolean dropped = false;
      String lastProblem = null;
      for (int i = 0; i < 24 && !dropped; i++) {
        dropped = true;
        for (String[] spelling : spellings) {
          SimpleHttpResponse response = probeOrNull(baseUrl, spelling[1], spelling[2]);
          if (response == null || !renderedScreen(response)) {
            dropped = false;
            lastProblem = spelling[0] + ": " + (response == null ? "warm-up did not render" : describe(response));
            break;
          }
        }
        if (!dropped) {
          Thread.sleep(5000);
        }
      }
      assertTrue("configs removed: every spelling must render again; last problem: " + lastProblem, dropped);

    } finally {
      for (String configId : CONFIG_IDS) {
        deleteConfig(configId);
      }
      deleteGroups();
    }
  }

  // ---------------------------------------------------------------- http (Java 8)

  /**
   * Minimal HTTP request on HttpURLConnection, shaped like the java.net.http builder calls this
   * test was first written with.  java.net.http is Java 11+, and this one test file has to compile
   * on every release it ships for, including 2.6.19 (Java 8).  Redirects are not followed; cookies
   * are kept by hand in a per-session jar (like a browser, one jar per logged in user).
   */
  static class SimpleHttpRequest {

    /** where to send it */
    private final java.net.URI uri;

    /** request headers, in order: {name, value} */
    private final java.util.List<String[]> headers = new java.util.ArrayList<String[]>();

    /** GET or POST */
    private String method = "GET";

    /** POST body, null for GET */
    private byte[] body;

    /**
     * @param theUri
     */
    private SimpleHttpRequest(java.net.URI theUri) {
      this.uri = theUri;
    }

    /**
     * @param theUri
     * @return a GET request to the uri until POST() is called
     */
    static SimpleHttpRequest newBuilder(java.net.URI theUri) {
      return new SimpleHttpRequest(theUri);
    }

    /**
     * @param text
     * @return the UTF-8 bytes, for POST()
     */
    static byte[] ofString(String text) {
      return text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * @param bytes
     * @return the same bytes, for POST()
     */
    static byte[] ofByteArray(byte[] bytes) {
      return bytes;
    }

    /**
     * @param name
     * @param value
     * @return this
     */
    SimpleHttpRequest header(String name, String value) {
      this.headers.add(new String[] {name, value});
      return this;
    }

    /**
     * @return this, as a GET
     */
    SimpleHttpRequest GET() {
      this.method = "GET";
      this.body = null;
      return this;
    }

    /**
     * @param theBody
     * @return this, as a POST of the body
     */
    SimpleHttpRequest POST(byte[] theBody) {
      this.method = "POST";
      this.body = theBody;
      return this;
    }

    /**
     * send it with the session's cookies, store the cookies the response sets
     * @param cookieJar
     * @param timeoutMillis connect and read timeout
     * @return the response (any status, the body decoded as UTF-8)
     * @throws java.io.IOException
     */
    SimpleHttpResponse send(SimpleCookieJar cookieJar, int timeoutMillis) throws java.io.IOException {
      java.net.HttpURLConnection connection = (java.net.HttpURLConnection)this.uri.toURL().openConnection();
      try {
        connection.setInstanceFollowRedirects(false);
        connection.setUseCaches(false);
        connection.setConnectTimeout(timeoutMillis);
        connection.setReadTimeout(timeoutMillis);
        connection.setRequestMethod(this.method);
        // HttpURLConnection sends a browser-ish Accept by default; java.net.http sent none
        connection.setRequestProperty("Accept", "*/*");
        for (String[] header : this.headers) {
          connection.addRequestProperty(header[0], header[1]);
        }
        String path = this.uri.getRawPath() == null || this.uri.getRawPath().isEmpty() ? "/" : this.uri.getRawPath();
        String cookieHeader = cookieJar.cookieHeader(path);
        if (cookieHeader != null) {
          connection.setRequestProperty("Cookie", cookieHeader);
        }
        if (this.body != null) {
          connection.setDoOutput(true);
          java.io.OutputStream outputStream = connection.getOutputStream();
          try {
            outputStream.write(this.body);
          } finally {
            outputStream.close();
          }
        }
        int status = connection.getResponseCode();
        java.util.Map<String, java.util.List<String>> responseHeaders = new java.util.LinkedHashMap<String, java.util.List<String>>();
        for (java.util.Map.Entry<String, java.util.List<String>> entry : connection.getHeaderFields().entrySet()) {
          // the null key is the status line
          if (entry.getKey() != null) {
            responseHeaders.put(entry.getKey(), entry.getValue());
          }
        }
        SimpleHttpHeaders simpleHeaders = new SimpleHttpHeaders(responseHeaders);
        cookieJar.store(path, simpleHeaders.allValues("Set-Cookie"));
        // 4xx/5xx bodies come from the error stream
        java.io.InputStream inputStream = null;
        try {
          inputStream = connection.getInputStream();
        } catch (java.io.IOException ioe) {
          inputStream = connection.getErrorStream();
        }
        String responseBody = "";
        if (inputStream != null) {
          try {
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
              bytes.write(buffer, 0, read);
            }
            responseBody = new String(bytes.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
          } finally {
            inputStream.close();
          }
        }
        return new SimpleHttpResponse(status, responseBody, simpleHeaders);
      } finally {
        connection.disconnect();
      }
    }
  }

  /**
   * status, body and headers of one response
   */
  static class SimpleHttpResponse {

    /** http status */
    private final int statusCode;

    /** body as UTF-8 text, never null */
    private final String body;

    /** response headers */
    private final SimpleHttpHeaders headers;

    /**
     * @param theStatusCode
     * @param theBody
     * @param theHeaders
     */
    SimpleHttpResponse(int theStatusCode, String theBody, SimpleHttpHeaders theHeaders) {
      this.statusCode = theStatusCode;
      this.body = theBody;
      this.headers = theHeaders;
    }

    /**
     * @return http status
     */
    int statusCode() {
      return this.statusCode;
    }

    /**
     * @return body text
     */
    String body() {
      return this.body;
    }

    /**
     * @return response headers
     */
    SimpleHttpHeaders headers() {
      return this.headers;
    }
  }

  /**
   * response headers, names matched case-insensitively (like java.net.http.HttpHeaders)
   */
  static class SimpleHttpHeaders {

    /** header name to values, as the server sent them */
    private final java.util.Map<String, java.util.List<String>> map;

    /**
     * @param theMap
     */
    SimpleHttpHeaders(java.util.Map<String, java.util.List<String>> theMap) {
      this.map = theMap;
    }

    /**
     * @return all headers
     */
    java.util.Map<String, java.util.List<String>> map() {
      return this.map;
    }

    /**
     * @param name
     * @return every value of the header, empty if none
     */
    java.util.List<String> allValues(String name) {
      java.util.List<String> result = new java.util.ArrayList<String>();
      for (java.util.Map.Entry<String, java.util.List<String>> entry : this.map.entrySet()) {
        if (entry.getKey().equalsIgnoreCase(name) && entry.getValue() != null) {
          result.addAll(entry.getValue());
        }
      }
      return result;
    }

    /**
     * @param name
     * @return the first value of the header
     */
    java.util.Optional<String> firstValue(String name) {
      java.util.List<String> values = allValues(name);
      return values.isEmpty() ? java.util.Optional.<String>empty() : java.util.Optional.ofNullable(values.get(0));
    }
  }

  /**
   * cookies of one session (one host), by path and name, like java.net.CookieManager does for these
   * requests: a cookie goes back on requests under its path; Max-Age=0 or an empty value removes it
   */
  static class SimpleCookieJar {

    /** path + " " + name (a cookie name has no spaces) to {name, value, path} */
    private final java.util.Map<String, String[]> cookies = new java.util.LinkedHashMap<String, String[]>();

    /**
     * @param requestPath
     * @return the Cookie header for a request to the path, or null if no cookie applies
     */
    synchronized String cookieHeader(String requestPath) {
      StringBuilder result = new StringBuilder();
      for (String[] cookie : this.cookies.values()) {
        String cookiePath = cookie[2];
        boolean matches = requestPath.equals(cookiePath) || (requestPath.startsWith(cookiePath)
            && (cookiePath.endsWith("/") || requestPath.charAt(cookiePath.length()) == '/'));
        if (matches) {
          if (result.length() > 0) {
            result.append("; ");
          }
          result.append(cookie[0]).append("=").append(cookie[1]);
        }
      }
      return result.length() == 0 ? null : result.toString();
    }

    /**
     * @param requestPath path of the request the response belongs to (default cookie path)
     * @param setCookieHeaders values of the Set-Cookie headers
     */
    synchronized void store(String requestPath, java.util.List<String> setCookieHeaders) {
      for (String setCookie : setCookieHeaders) {
        String[] parts = setCookie.split(";");
        int equalsIndex = parts[0].indexOf('=');
        if (equalsIndex <= 0) {
          continue;
        }
        String name = parts[0].substring(0, equalsIndex).trim();
        String value = parts[0].substring(equalsIndex + 1).trim();
        // default path: the request path up to its last slash (RFC 6265)
        int lastSlash = requestPath.lastIndexOf('/');
        String path = lastSlash <= 0 ? "/" : requestPath.substring(0, lastSlash);
        boolean expired = value.isEmpty();
        for (int i = 1; i < parts.length; i++) {
          String attribute = parts[i].trim();
          String lower = attribute.toLowerCase();
          if (lower.startsWith("path=")) {
            path = attribute.substring("path=".length()).trim();
          } else if (lower.startsWith("max-age=")) {
            try {
              expired = expired || Long.parseLong(attribute.substring("max-age=".length()).trim()) <= 0;
            } catch (NumberFormatException nfe) {
              // ignore a bad max-age
            }
          } else if (lower.startsWith("expires=") && lower.contains("1970")) {
            expired = true;
          }
        }
        if (path.isEmpty()) {
          path = "/";
        }
        if (expired) {
          this.cookies.remove(path + " " + name);
        } else {
          this.cookies.put(path + " " + name, new String[] {name, value, path});
        }
      }
    }
  }

}
