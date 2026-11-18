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
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory L1 (groupSummaryDisclosesPrivateRelatedGroups), HTTP check against the running
 * local UI: the group summary "more" panel (UiV2Group.viewGroupSummaryMore) must not name related
 * groups the user cannot VIEW.  Before the fix it resolved the related group ids from raw SQL with
 * a GroupFinder that had no privileges assigned, so an ADMIN of the group saw the names and uuids
 * of private groups holding privileges on it or that it holds privileges on, and a READ user saw
 * private member groups.
 *
 * <p>Setup (as root, through the API from this JVM): folder secPatch132 with group team and group
 * visibleMember; folder secPatch132private with groups target, holder and member, with every
 * EveryEntity access privilege revoked.  team has READ on target, holder has UPDATE on team,
 * member and visibleMember are members of team.  test.subject.0 has ADMIN on team and VIEW on
 * visibleMember; test.subject.2 has READ on team and VIEW on visibleMember.</p>
 *
 * <p>Expected: neither subject gets the uuid or name of target, holder or member; both still get
 * visibleMember (the filter does not hide viewable groups); GrouperSystem gets all of them.  The
 * folders are obliterated in finally.</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which resets the
 * database the running UI uses).  Plain HTTP requests, dev form login, OWASP CSRF token from
 * OwaspJavaScriptServlet (helpers copied from advisory M3's test).  Runs on every patched release
 * (5.20.6 and later): 5.20.6 and 5.21.0 have no "more" panel and render the same sections in
 * UiV2Group.viewGroup, so the test posts there when UiV2Group has no viewGroupSummaryMore.  The
 * ABAC section of viewGroupSummary uses the same filter and is not covered here (it needs SQL
 * cache dependencies).</p>
 */
public class UiV2GroupSummaryRelatedGroupsHttpTest extends TestCase {

  /** folder with the viewed group */
  private static final String STEM_NAME = "secPatch132";

  /** folder with the private related groups */
  private static final String PRIVATE_STEM_NAME = "secPatch132private";

  /** ADMIN on team */
  private static final String TEAM_ADMIN = "test.subject.0";

  /** READ on team */
  private static final String TEAM_READER = "test.subject.2";

  /** summary more endpoint */
  private static final String SUMMARY_MORE_PATH = "/grouperUi/app/UiV2Group.viewGroupSummaryMore";

  /** 5.20.6 and 5.21.0: the summary sections are rendered by viewGroup */
  private static final String VIEW_GROUP_PATH = "/grouperUi/app/UiV2Group.viewGroup";

  /**
   * @return the endpoint that renders the related groups in this release
   */
  private static String summaryEndpoint() {
    // by name, not signature: the servlet types are javax or jakarta depending on the release
    for (java.lang.reflect.Method method : UiV2Group.class.getMethods()) {
      if ("viewGroupSummaryMore".equals(method.getName())) {
        return SUMMARY_MORE_PATH;
      }
    }
    return VIEW_GROUP_PATH;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2GroupSummaryRelatedGroupsHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public UiV2GroupSummaryRelatedGroupsHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2GroupSummaryRelatedGroupsHttpTest(String name) {
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

    /** Authorization header value when the UI uses basic auth (2.6.19), else null (form login) */
    private String basicAuthorization;

    /**
     * @param theBaseUrl
     */
    UiSession(String theBaseUrl) {
      this.baseUrl = theBaseUrl;
    }

    /**
     * form login (dev only), then read the CSRF token
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
      if (response.statusCode() == 401
          && response.headers().firstValue("WWW-Authenticate").orElse("").toLowerCase().startsWith("basic")) {
        // 2.6.19 has no form login (GrouperUiFormLogin is 4.x+); its local UI runs with
        // grouper.is.ui.basicAuthn=true, which checks the same UI password on every request
        this.basicAuthorization = "Basic " + java.util.Base64.getEncoder().encodeToString(
            (username + ":" + password).getBytes(StandardCharsets.UTF_8));
        response = send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + "/grouperUi/app/UiV2Main.index")).GET());
        // only a bad password is 401 (a refused section comes later, not as 401)
        assertTrue("basic auth login of " + username + ": " + describe(response), response.statusCode() != 401);
      } else {
        // a good login is a post/redirect/get; a bad one shows the form again (200)
        assertEquals("form login of " + username + " must redirect (on 2.6.19 set grouper.is.ui.basicAuthn=true): "
            + response.body(), 302, response.statusCode());
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
     * ajax GET with the CSRF header, like the UI's combo requests
     * @param path after the base url, with the query string
     * @return the response
     * @throws Exception
     */
    SimpleHttpResponse getAjax(String path) throws Exception {
      return send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + path))
          .header("X-Requested-With", "XMLHttpRequest")
          .header("OWASPCSRFTOKEN", this.csrfToken).GET());
    }

    /**
     * plain GET with the CSRF header and no X-Requested-With, like the browser following the
     * export link
     * @param path after the base url, with the query string
     * @return the response
     * @throws Exception
     */
    SimpleHttpResponse getPlain(String path) throws Exception {
      return send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + path))
          .header("OWASPCSRFTOKEN", this.csrfToken).GET());
    }

    /**
     * @param builder
     * @return the response, 15 second timeout
     * @throws Exception
     */
    private SimpleHttpResponse send(SimpleHttpRequest builder) throws Exception {
      if (this.basicAuthorization != null) {
        // basic auth (2.6.19) is checked on every request, not kept by the session cookie
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

  // ---------------------------------------------------------------- setup

  /**
   * delete both folders and everything in them
   */
  private static void cleanUp132() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String stemName : new String[] {STEM_NAME, PRIVATE_STEM_NAME}) {
        Stem stem = StemFinder.findByName(rootSession, stemName, false);
        if (stem != null) {
          stem.obliterate(false, false);
        }
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * as root: a private group nobody but root can see
   * @param rootSession
   * @param name
   * @return the group
   */
  private static Group privateGroup(GrouperSession rootSession, String name) {
    Group group = new GroupSave(rootSession).assignName(name).save();
    Subject everyEntity = SubjectFinder.findAllSubject();
    for (Privilege privilege : new Privilege[] {AccessPrivilege.VIEW, AccessPrivilege.READ, AccessPrivilege.OPTIN,
        AccessPrivilege.OPTOUT, AccessPrivilege.GROUP_ATTR_READ}) {
      group.revokePriv(everyEntity, privilege, false);
    }
    return group;
  }

  /**
   * as root: the groups, privileges and memberships
   * @return ids: team, target, holder, member, visibleMember
   */
  private static String[] setUp132() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Subject teamAdmin = SubjectFinder.findById(TEAM_ADMIN, true);
      Subject teamReader = SubjectFinder.findById(TEAM_READER, true);
      new StemSave(rootSession).assignName(STEM_NAME).assignCreateParentStemsIfNotExist(true).save();
      new StemSave(rootSession).assignName(PRIVATE_STEM_NAME).assignCreateParentStemsIfNotExist(true).save();
      Group team = new GroupSave(rootSession).assignName(STEM_NAME + ":team").save();
      Group visibleMember = new GroupSave(rootSession).assignName(STEM_NAME + ":visibleMember").save();
      Group target = privateGroup(rootSession, PRIVATE_STEM_NAME + ":target");
      Group holder = privateGroup(rootSession, PRIVATE_STEM_NAME + ":holder");
      Group member = privateGroup(rootSession, PRIVATE_STEM_NAME + ":member");

      // team is used in privileges on target; holder holds a privilege on team
      target.grantPriv(team.toSubject(), AccessPrivilege.READ, false);
      team.grantPriv(holder.toSubject(), AccessPrivilege.UPDATE, false);
      // direct group members of team: one private, one the users can see
      team.addMember(member.toSubject(), false);
      team.addMember(visibleMember.toSubject(), false);

      team.grantPriv(teamAdmin, AccessPrivilege.ADMIN, false);
      team.grantPriv(teamReader, AccessPrivilege.READ, false);
      visibleMember.grantPriv(teamAdmin, AccessPrivilege.VIEW, false);
      visibleMember.grantPriv(teamReader, AccessPrivilege.VIEW, false);
      return new String[] {team.getId(), target.getId(), holder.getId(), member.getId(), visibleMember.getId()};
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  // ---------------------------------------------------------------- test

  /**
   * team admin and team reader get no private related group; GrouperSystem gets them all
   * @throws Exception
   */
  public void testSummaryMoreHidesPrivateRelatedGroups() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    cleanUp132();
    try {
      String[] ids = setUp132();
      String teamId = ids[0];
      String[] privateIds = new String[] {ids[1], ids[2], ids[3]};
      String[] privateNames = new String[] {PRIVATE_STEM_NAME + ":target", PRIVATE_STEM_NAME + ":holder",
          PRIVATE_STEM_NAME + ":member"};
      String body = "groupId=" + enc(teamId);
      String summaryPath = summaryEndpoint();

      // ---- attack: ADMIN (privilege sections and member section) and READ (member section)
      for (String user : new String[] {TEAM_ADMIN, TEAM_READER}) {
        UiSession session = new UiSession(baseUrl);
        session.login(user, "pass");
        SimpleHttpResponse more = session.postAjax(summaryPath, body);
        assertEquals(user + " summary status: " + describe(more), 200, more.statusCode());
        for (int i = 0; i < privateIds.length; i++) {
          assertFalse(user + " must not get the uuid of " + privateNames[i] + ": " + describe(more),
              more.body().contains(privateIds[i]));
          assertFalse(user + " must not get the name of " + privateNames[i] + ": " + describe(more),
              more.body().contains(privateNames[i]));
        }
        // the viewable member group is still listed
        assertTrue(user + " still gets the viewable member group: " + describe(more),
            more.body().contains(ids[4]));
      }

      // ---- positive control: root sees every related group
      UiSession rootUi = new UiSession(baseUrl);
      rootUi.login("GrouperSystem", "pass");
      SimpleHttpResponse rootMore = rootUi.postAjax(summaryPath, body);
      for (int i = 0; i < privateIds.length; i++) {
        assertTrue("GrouperSystem gets " + privateNames[i] + ": " + describe(rootMore),
            rootMore.body().contains(privateIds[i]));
      }

      System.out.println("secPatch132: admin and reader got no private related group, root got all");
    } finally {
      cleanUp132();
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
