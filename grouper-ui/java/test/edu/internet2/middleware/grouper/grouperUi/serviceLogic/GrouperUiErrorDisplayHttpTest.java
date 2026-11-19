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
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * Security advisory H5 (uiErrorMessageInternalDetails), HTTP check against the running local UI: an
 * unhandled RuntimeException in a UI handler must reach the browser as a generic message with an
 * 8 character error id, not as the exception text (which can carry names, ids and class names).
 * GrouperUiErrorDisplayTest tests the message helper; this test covers what it cannot: the
 * GrouperUiRestServlet catch block wiring, seen in the real ajax response.  The server log line
 * with the same error id goes to the Eclipse console only and is not checked here.
 *
 * <p>Attack: test.subject.0 sends (1) UiV2Group.newGroupSubmit with a bogus group type that names a
 * marker string (the enum lookup error repeats the input), and (2) UiV2CustomUi.customUiGroup for
 * an existing group without a custom UI.  Each answer must be the generic text with an error id and
 * no marker, group name or Java class name; no group may be created (SQL).  Controls: a
 * validation message (group already exists) is still shown with its specific text and no error id,
 * and a valid newGroupSubmit by the same user creates the group (SQL).</p>
 *
 * <p>Setup (this test's own, removed at the end): folder secPatch68 (test.subject.0 has CREATE)
 * with group secPatch68:secretSalaryReview68.</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which resets
 * the database the running UI uses).  Plain HTTP requests, dev form login, OWASP CSRF token
 * from OwaspJavaScriptServlet.</p>
 */
public class GrouperUiErrorDisplayHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "secPatch68";

  /** existing group whose name must not leak */
  private static final String SECRET_GROUP = STEM_NAME + ":secretSalaryReview68";

  /** group the positive control creates */
  private static final String CREATED_GROUP = STEM_NAME + ":created68";

  /** marker sent as the group type; must not come back */
  private static final String MARKER = "secPatch68leakMarker";

  /** user with CREATE on the folder, not wheel */
  private static final String USER = "test.subject.0";

  /** generic text (built-in default; the text key is not in the base properties of the patched release) */
  private static final String GENERIC = "An unexpected error occurred.";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperUiErrorDisplayHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public GrouperUiErrorDisplayHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperUiErrorDisplayHttpTest(String name) {
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

    /** e.g. http://localhost:8080/grouper */
    private final String baseUrl;

    /** OWASP CSRF master token of this session */
    private String csrfToken;

    /**
     * "Basic ..." Authorization header sent with every request of this session, or null when the
     * session is logged in with the dev form login (see login)
     */
    private String basicAuthorization;

    /**
     * @param theBaseUrl
     */
    UiSession(String theBaseUrl) {
      this.baseUrl = theBaseUrl;
    }

    /**
     * form login (dev only), or HTTP Basic where the UI answers 401 (2.6.19),
     * then read the CSRF token
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
        // 2.6.19 has no dev form login (GrouperUiFormLogin exists from 4.26.0): its UI runs with
        // grouper.is.ui.basicAuthn=true (or container BASIC auth) and answers 401 to a request
        // without credentials.  Send HTTP Basic with every request of this session instead; the UI
        // checks it against the same grouper_password UI rows the form login uses.
        this.basicAuthorization = "Basic " + java.util.Base64.getEncoder().encodeToString(
            (username + ":" + password).getBytes(StandardCharsets.UTF_8));
        response = send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + "/grouperUi/app/UiV2Main.index")).GET());
        assertTrue("basic login of " + username + " must be accepted: " + response.statusCode() + " " + response.body(),
            response.statusCode() < 400);
      } else {
        // a good form login is a post/redirect/get; a bad one shows the form again (200)
        assertEquals("form login of " + username + " must redirect: " + response.body(), 302, response.statusCode());
      }

      SimpleHttpResponse csrfJs = send(SimpleHttpRequest.newBuilder(
          URI.create(this.baseUrl + "/grouperExternal/public/OwaspJavaScriptServlet")).GET());
      Matcher matcher = Pattern.compile("masterTokenValue = '([^']+)'").matcher(csrfJs.body());
      assertTrue("CSRF token for " + username, matcher.find());
      this.csrfToken = matcher.group(1);
    }

    /**
     * ajax POST with the CSRF header and a form body, like the UI's own requests
     * @param path after the base url
     * @param formBody urlencoded body
     * @return the response
     * @throws Exception
     */
    SimpleHttpResponse postAjax(String path, String formBody) throws Exception {
      return send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + path))
          .header("X-Requested-With", "XMLHttpRequest")
          .header("OWASPCSRFTOKEN", this.csrfToken)
          .header("Content-Type", "application/x-www-form-urlencoded")
          .POST(SimpleHttpRequest.ofString(formBody)));
    }

    /**
     * @param builder
     * @return the response, 15 second timeout
     * @throws Exception
     */
    private SimpleHttpResponse send(SimpleHttpRequest builder) throws Exception {
      // HTTP Basic sessions (2.6.19) authenticate every request, there is no login cookie
      if (this.basicAuthorization != null) {
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
    // the ajax json starts with the logged in user's settings and ends with screens: show the
    // messages (errors) and the end of the body, not the settings at the start
    java.util.List<String> messages = new java.util.ArrayList<String>();
    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"message\":\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(body);
    while (matcher.find()) {
      messages.add(matcher.group(1));
    }
    return response.statusCode() + " messages: " + messages + " body: "
        + (body.length() <= 1500 ? body : body.substring(body.length() - 1500));
  }

  // ---------------------------------------------------------------- db helpers

  /**
   * @param groupName
   * @return number of groups with that name (SQL, not the API caches)
   */
  private static int groupCount(String groupName) {
    return new GcDbAccess().sql("select count(*) from grouper_groups where name = ?").addBindVar(groupName).select(int.class);
  }

  /**
   * @param likeName
   * @return number of groups whose name is like the pattern
   */
  private static int groupCountLike(String likeName) {
    return new GcDbAccess().sql("select count(*) from grouper_groups where name like ?").addBindVar(likeName).select(int.class);
  }

  /**
   * delete the groups and the folder
   */
  private static void cleanUp68() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Stem stem = StemFinder.findByName(rootSession, STEM_NAME, false);
      if (stem != null) {
        for (Object groupObject : stem.getChildGroups(Stem.Scope.SUB)) {
          ((Group)groupObject).delete();
        }
        stem.delete();
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * the response is the generic error with an 8 character id, and leaks none of the given strings
   * @param response
   * @param what
   * @param mustNotContain
   */
  private static void assertGenericError(SimpleHttpResponse response, String what, String... mustNotContain) {
    String body = response.body() == null ? "" : response.body();
    assertTrue(what + " shows an error message: " + describe(response), body.contains("\"messageType\":\"error\""));
    assertTrue(what + " shows the generic text: " + describe(response), body.contains(GENERIC));
    Matcher matcher = Pattern.compile("error id: ([a-z0-9]+)").matcher(body);
    assertTrue(what + " shows an error id: " + describe(response), matcher.find());
    assertEquals(what + " error id is 8 characters: " + matcher.group(1), 8, matcher.group(1).length());
    for (String secret : mustNotContain) {
      assertFalse(what + " must not show '" + secret + "': " + describe(response), body.contains(secret));
    }
    assertFalse(what + " must not show a Java class name: " + describe(response), body.contains("edu.internet2"));
    assertFalse(what + " must not show an exception name: " + describe(response), body.contains("Exception"));
  }

  // ---------------------------------------------------------------- test

  /**
   * unhandled errors show the generic text and an id; validation text and a valid create still work
   * @throws Exception
   */
  public void testUnexpectedErrorShowsIdNotDetails() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    cleanUp68();
    try {
      String stemId;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        Stem stem = new StemSave(rootSession).assignName(STEM_NAME).assignCreateParentStemsIfNotExist(true).save();
        stem.grantPriv(SubjectFinder.findById(USER, true), NamingPrivilege.CREATE, false);
        new GroupSave(rootSession).assignName(SECRET_GROUP).save();
        stemId = stem.getId();
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }
      assertEquals("secret group exists", 1, groupCount(SECRET_GROUP));

      UiSession user = new UiSession(uiBaseUrl());
      user.login(USER, "pass");
      String newGroupPath = "/grouperUi/app/UiV2Group.newGroupSubmit";

      // attack 1: bogus group type, the enum lookup error would repeat it
      SimpleHttpResponse badType = user.postAjax(newGroupPath, "parentFolderComboName=" + enc(stemId)
          + "&displayExtension=" + enc("bogus68") + "&groupType%5B%5D=" + enc(MARKER));
      assertGenericError(badType, "bogus group type", MARKER);
      assertEquals("no group created by the bogus request", 0, groupCountLike(STEM_NAME + ":bogus68%"));

      // attack 2: custom UI for a group that has none (handler throws a RuntimeException)
      SimpleHttpResponse customUi = user.postAjax("/grouperUi/app/UiV2CustomUi.customUiGroup",
          "groupId=" + enc(SECRET_GROUP));
      assertGenericError(customUi, "custom UI of a group without one", "secretSalaryReview68");

      // control: a handled validation message keeps its specific text and has no error id
      SimpleHttpResponse exists = user.postAjax(newGroupPath, "parentFolderComboName=" + enc(stemId)
          + "&displayExtension=" + enc("secretSalaryReview68") + "&groupType%5B%5D=group");
      assertTrue("already exists text shown: " + describe(exists), exists.body().contains("already exists"));
      assertFalse("validation message has no error id: " + describe(exists), exists.body().contains("error id:"));
      assertEquals("still one secret group", 1, groupCount(SECRET_GROUP));

      // positive control: a valid create by the same user works
      SimpleHttpResponse created = user.postAjax(newGroupPath, "parentFolderComboName=" + enc(stemId)
          + "&displayExtension=" + enc("created68") + "&groupType%5B%5D=group");
      assertFalse("valid create shows no error id: " + describe(created), created.body().contains("error id:"));
      assertEquals("valid create made the group: " + describe(created), 1, groupCount(CREATED_GROUP));
    } finally {
      cleanUp68();
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
