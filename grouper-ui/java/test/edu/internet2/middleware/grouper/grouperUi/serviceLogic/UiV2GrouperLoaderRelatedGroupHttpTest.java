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
import edu.internet2.middleware.grouper.app.serviceLifecycle.GrouperRecentMemberships;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory L1 (loaderTabDisclosesPrivateRecentMembershipsSourceGroup), HTTP check
 * against the running local UI: the group Loader tab (UiV2GrouperLoader.loader) is shown to
 * ADMINs of the group by default (uiV2.loader.view.by.group.admins).  For a recent-memberships
 * loader it rendered the source ("from") group's link, resolved in a root session, so an ADMIN
 * of the loader group got the uuid, name, path and description of a source group they cannot
 * VIEW.  The edit form (UiV2GrouperLoader.editGrouperLoader) put the source uuid in its
 * combobox.  After the fix the tab shows a "(restricted group)" placeholder and the edit form
 * leaves the combobox empty, unless the user can VIEW (tab) or READ (edit form) the source, or
 * edits loaders.
 *
 * <p>Setup (as root, through the API from this JVM): folder secPatch138 with group
 * recentLoader; folder secPatch138private with group privateSource (every EveryEntity access
 * privilege revoked, a distinctive description).  recentLoader is a recent-memberships loader
 * from privateSource (the attributes editGrouperLoaderSave writes; no loader job is run).
 * test.subject.0 has ADMIN on recentLoader; test.subject.4 has ADMIN on recentLoader and VIEW
 * and READ on privateSource.</p>
 *
 * <p>Expected: test.subject.0 gets the tab with the placeholder and without the uuid, name or
 * description of privateSource, and an edit form without its uuid; test.subject.4 and
 * GrouperSystem get the source link on the tab, and GrouperSystem gets the uuid in the edit
 * form.  The folders are obliterated in finally.  The SQL and LDAP "and groups" lists have the
 * same one-line change and are not covered here (see the manual test steps).</p>
 *
 * <p>Runs only with junit.test.tomcat=true and the default uiV2.loader.view.by.group.admins.
 * Extends TestCase (not GrouperTest, which resets the database the running UI uses).  HTTP
 * helpers copied from advisory L1's test (Java 8, HttpURLConnection).</p>
 */
public class UiV2GrouperLoaderRelatedGroupHttpTest extends TestCase {

  /** folder with the loader group */
  private static final String STEM_NAME = "secPatch138";

  /** private folder with the source group */
  private static final String PRIVATE_STEM_NAME = "secPatch138private";

  /** loader group */
  private static final String LOADER_NAME = STEM_NAME + ":recentLoader";

  /** private source group */
  private static final String SOURCE_NAME = PRIVATE_STEM_NAME + ":privateSource";

  /** description of the private source */
  private static final String SOURCE_DESCRIPTION = "SECPATCH138_PRIVATE_SOURCE_DESCRIPTION";

  /** ADMIN on the loader group, nothing on the source */
  private static final String ATTACKER = "test.subject.0";

  /** ADMIN on the loader group, VIEW and READ on the source */
  private static final String SOURCE_VIEWER = "test.subject.4";

  /** loader tab */
  private static final String LOADER_TAB = "/grouperUi/app/UiV2GrouperLoader.loader";

  /** loader edit form */
  private static final String LOADER_EDIT = "/grouperUi/app/UiV2GrouperLoader.editGrouperLoader";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2GrouperLoaderRelatedGroupHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public UiV2GrouperLoaderRelatedGroupHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2GrouperLoaderRelatedGroupHttpTest(String name) {
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
  private static void cleanUp138() {
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
   * as root: the private source, the loader group as a recent-memberships loader from it, and
   * the privileges
   * @return ids: loader group, source group
   */
  private static String[] setUp138() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Subject attacker = SubjectFinder.findById(ATTACKER, true);
      Subject sourceViewer = SubjectFinder.findById(SOURCE_VIEWER, true);
      new StemSave(rootSession).assignName(STEM_NAME).assignCreateParentStemsIfNotExist(true).save();
      new StemSave(rootSession).assignName(PRIVATE_STEM_NAME).assignCreateParentStemsIfNotExist(true).save();
      Group source = new GroupSave(rootSession).assignName(SOURCE_NAME).assignDescription(SOURCE_DESCRIPTION).save();
      Subject everyEntity = SubjectFinder.findAllSubject();
      for (Privilege privilege : new Privilege[] {AccessPrivilege.VIEW, AccessPrivilege.READ, AccessPrivilege.OPTIN,
          AccessPrivilege.OPTOUT, AccessPrivilege.GROUP_ATTR_READ}) {
        source.revokePriv(everyEntity, privilege, false);
      }
      Group loader = new GroupSave(rootSession).assignName(LOADER_NAME).save();

      // the same attributes UiV2GrouperLoader.editGrouperLoaderSave writes for RECENT_MEMBERSHIPS
      String prefix = GrouperRecentMemberships.recentMembershipsStemName() + ":";
      AttributeDefName marker = AttributeDefNameFinder.findByName(
          prefix + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER, true);
      AttributeAssign attributeAssign = loader.getAttributeDelegate().assignAttribute(marker).getAttributeAssign();
      attributeAssign.getAttributeValueDelegate().assignValue(
          prefix + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_UUID_FROM, source.getId());
      attributeAssign.getAttributeValueDelegate().assignValue(
          prefix + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_INCLUDE_CURRENT, "F");
      attributeAssign.getAttributeValueDelegate().assignValue(
          prefix + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_MICROS,
          Long.toString(7L * 24 * 60 * 60 * 1000 * 1000));

      loader.grantPriv(attacker, AccessPrivilege.ADMIN, false);
      loader.grantPriv(sourceViewer, AccessPrivilege.ADMIN, false);
      source.grantPriv(sourceViewer, AccessPrivilege.VIEW, false);
      source.grantPriv(sourceViewer, AccessPrivilege.READ, false);
      return new String[] {loader.getId(), source.getId()};
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  // ---------------------------------------------------------------- test

  /**
   * ADMIN of the loader group without VIEW on the source gets the placeholder and no source uuid
   * in the edit form; with VIEW (and root) the source link
   * @throws Exception
   */
  public void testLoaderTabHidesPrivateRecentSource() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    cleanUp138();
    try {
      String[] ids = setUp138();
      String loaderId = ids[0];
      String sourceId = ids[1];
      String body = "groupId=" + enc(loaderId);

      // ---- attack: ADMIN on the loader group, nothing on the source
      UiSession attackerUi = new UiSession(baseUrl);
      attackerUi.login(ATTACKER, "pass");
      SimpleHttpResponse attack = attackerUi.postAjax(LOADER_TAB, body);
      assertEquals("loader tab status: " + describe(attack), 200, attack.statusCode());
      assertTrue("the tab is shown with a placeholder for the source (needs the default"
          + " uiV2.loader.view.by.group.admins=true): " + describe(attack),
          attack.body().contains("grouperRestrictedReference"));
      assertFalse("no uuid of the source: " + describe(attack), attack.body().contains(sourceId));
      assertFalse("no name of the source: " + describe(attack), attack.body().contains("privateSource"));
      assertFalse("no description of the source: " + describe(attack), attack.body().contains(SOURCE_DESCRIPTION));

      SimpleHttpResponse attackEdit = attackerUi.postAjax(LOADER_EDIT, body);
      assertFalse("no uuid of the source in the edit form: " + describe(attackEdit), attackEdit.body().contains(sourceId));
      assertFalse("no name of the source in the edit form: " + describe(attackEdit), attackEdit.body().contains("privateSource"));

      // ---- positive controls: VIEW on the source, and root, get the source link
      for (String allowed : new String[] {SOURCE_VIEWER, "GrouperSystem"}) {
        UiSession session = new UiSession(baseUrl);
        session.login(allowed, "pass");
        SimpleHttpResponse tab = session.postAjax(LOADER_TAB, body);
        assertTrue(allowed + " gets the source link: " + describe(tab), tab.body().contains(sourceId));
        if ("GrouperSystem".equals(allowed)) {
          SimpleHttpResponse edit = session.postAjax(LOADER_EDIT, body);
          assertTrue(allowed + " gets the source uuid in the edit form: " + describe(edit), edit.body().contains(sourceId));
        }
      }

      System.out.println("secPatch138: group admin got the placeholder, source viewer and root got the source");
    } finally {
      cleanUp138();
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
