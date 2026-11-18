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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory C3 (gshTemplateMutableDownloadDisclosure), HTTP check against the running local
 * UI: a GSH template download link must keep serving the bytes it was issued for, or 404, never a
 * later re-save of the same file by another user.  UiV2DownloadFileVersionTest calls
 * GshTemplateDownloadFile.save and UiV2DownloadFile directly with a proxy session; this test covers
 * what it cannot: the end-to-end flow through the real UI (template run, link in the ajax
 * response, overwrite by a second user, stale link 404, re-run).
 *
 * <p>Setup (this test's own, removed at the end): a compiled V2 GSH template secPatch109 on one
 * group (securityRunType everyone) that saves report.txt for today with the current subject id in
 * it, and that group with VIEW for test.subject.0 and test.subject.1.  Both users save the same
 * grouper_file row (same template, date and name), like a template that writes per-user data
 * under a shared name.</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which
 * resets the database the running UI uses).  Plain HTTP requests, dev form login, OWASP
 * CSRF token from OwaspJavaScriptServlet; one session per user.</p>
 */
public class UiV2DownloadFileVersionHttpTest extends TestCase {

  /** template config id */
  private static final String CONFIG_ID = "secPatch109";

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "secPatch109";

  /** group the template runs on */
  private static final String GROUP_NAME = STEM_NAME + ":shared";

  /** the two users */
  private static final String USER_A = "test.subject.0";

  /** the two users */
  private static final String USER_B = "test.subject.1";

  /** content marker the template writes, followed by the subject id */
  private static final String CONTENT_PREFIX = "secPatch109 private report for ";

  /** compiled template: saves report.txt for today with the current subject in it */
  private static final String TEMPLATE_JAVA_SOURCE = ""
      + "package edu.internet2.middleware.grouper.gshTest;\n"
      + "\n"
      + "import java.time.LocalDate;\n"
      + "\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
      + "\n"
      + "/** security advisory C3 HTTP test: per user data under a shared file name */\n"
      + "public class SecPatch109Download extends GshTemplateV2 {\n"
      + "\n"
      + "  @Override\n"
      + "  public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) {\n"
      + "    GshTemplateOutput gshTemplateOutput = out.getGsh_builtin_gshTemplateOutput();\n"
      + "    String subjectId = in.getGsh_builtin_subject().getId();\n"
      + "    gshTemplateOutput.assignDownloadFile(LocalDate.now().toString(), \"report.txt\",\n"
      + "        \"" + CONTENT_PREFIX + "\" + subjectId + \" at \" + System.nanoTime() + \"\\n\");\n"
      + "    gshTemplateOutput.addOutputLine(\"secPatch109 saved report.txt for \" + subjectId);\n"
      + "  }\n"
      + "}\n";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2DownloadFileVersionHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public UiV2DownloadFileVersionHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2DownloadFileVersionHttpTest(String name) {
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
   * @param values
   * @return list of the values
   */
  static List<Object> listOf(Object... values) {
    List<Object> list = new ArrayList<Object>();
    for (Object value : values) {
      list.add(value);
    }
    return list;
  }

  /** the download link in the ajax json (gson may write = as \u003d) */
  private static final Pattern TOKEN_PATTERN = Pattern.compile("UiV2DownloadFile\\.download\\?token(?:=|\\\\u003d)([A-Za-z0-9_\\-]+)");

  /** the progress poll in the ajax json */
  private static final Pattern UNIQUE_ID_PATTERN = Pattern.compile("customTemplateExecuteStatus\\?uniqueId(?:=|\\\\u003d)([A-Za-z0-9]+)");

  /**
   * run the template on the group like the Run button does, follow the progress polls, and
   * return the download token from the response
   * @param session
   * @param groupId
   * @return the token, or null if the response had none (e.g. the UI has not loaded the config yet)
   * @throws Exception
   */
  private static String runTemplate(UiSession session, String groupId) throws Exception {
    SimpleHttpResponse response = session.postAjax("/grouperUi/app/UiV2Template.customTemplateExecute",
        "groupId=" + enc(groupId) + "&templateType=" + enc(CONFIG_ID));
    for (int i = 0; i < 30; i++) {
      Matcher tokenMatcher = TOKEN_PATTERN.matcher(response.body());
      if (tokenMatcher.find()) {
        return tokenMatcher.group(1);
      }
      Matcher uniqueIdMatcher = UNIQUE_ID_PATTERN.matcher(response.body());
      if (!uniqueIdMatcher.find()) {
        System.out.println("secPatch109: no download link in run response: " + describe(response));
        return null;
      }
      Thread.sleep(1000);
      response = session.postAjax("/grouperUi/app/UiV2Template.customTemplateExecuteStatus?uniqueId="
          + uniqueIdMatcher.group(1), "");
    }
    return null;
  }

  /**
   * @param session
   * @param token
   * @return the download response (plain GET, like location.href)
   * @throws Exception
   */
  private static SimpleHttpResponse download(UiSession session, String token) throws Exception {
    return session.get("/grouperUi/app/UiV2DownloadFile.download?token=" + enc(token));
  }

  // ---------------------------------------------------------------- setup / cleanup

  /** template config keys */
  private static String[][] templateConfig(String groupId) {
    return new String[][] {
        {"templateType", "gsh"},
        {"templateMode", "compiled"},
        {"templateVersion", "V2"},
        {"enabled", "true"},
        {"templateName", "secPatch109 download"},
        {"templateDescription", "security advisory C3 HTTP test"},
        {"showOnFolders", "false"},
        {"showOnGroups", "true"},
        {"groupShowType", "certainGroups"},
        {"groupUuidsToShow", groupId},
        {"securityRunType", "everyone"},
        {"runAsType", "GrouperSystem"},
        {"displayErrorOutput", "true"},
        {"numberOfInputs", "0"},
        {"gshTemplate", TEMPLATE_JAVA_SOURCE},
    };
  }

  /**
   * delete the template config, its download files, the group and folder
   */
  private static void cleanUp109() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String[] keyValue : templateConfig("x")) {
        try {
          new GrouperDbConfig().configFileName("grouper.properties")
              .propertyName("grouperGshTemplate." + CONFIG_ID + "." + keyValue[0]).delete();
        } catch (RuntimeException re) {
          // not there
        }
      }
      HibernateSession.bySqlStatic().executeSql("delete from grouper_file where file_path like ?",
          listOf("/gshTemplateDownload/" + CONFIG_ID + "/%"));
      Group group = GroupFinder.findByName(rootSession, GROUP_NAME, false);
      if (group != null) {
        group.delete();
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
   * user A's link stops working (404) once user B re-saves the same file; it never serves B's
   * content; A's new run serves A's content again
   * @throws Exception
   */
  public void testOverwrittenDownloadIs404() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    cleanUp109();
    try {
      String groupId;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        Group group = new GroupSave(rootSession).assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
        group.grantPriv(SubjectFinder.findById(USER_A, true), AccessPrivilege.VIEW, false);
        group.grantPriv(SubjectFinder.findById(USER_B, true), AccessPrivilege.VIEW, false);
        groupId = group.getId();
        for (String[] keyValue : templateConfig(groupId)) {
          new GrouperDbConfig().configFileName("grouper.properties")
              .propertyName("grouperGshTemplate." + CONFIG_ID + "." + keyValue[0]).value(keyValue[1]).store();
        }
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }

      UiSession sessionA = new UiSession(baseUrl);
      sessionA.login(USER_A, "pass");
      UiSession sessionB = new UiSession(baseUrl);
      sessionB.login(USER_B, "pass");

      // step 1: A runs the template (retry while the UI picks up the new config) and checks the link once
      String tokenA1 = null;
      for (int i = 0; i < 12 && tokenA1 == null; i++) {
        tokenA1 = runTemplate(sessionA, groupId);
        if (tokenA1 == null) {
          Thread.sleep(5000);
        }
      }
      assertNotNull("A never got a download link", tokenA1);
      SimpleHttpResponse a1 = download(sessionA, tokenA1);
      assertEquals("A's first download: " + describe(a1), 200, a1.statusCode());
      assertTrue("A's first download is A's report: " + describe(a1), a1.body().contains(CONTENT_PREFIX + USER_A));

      // step 2: B runs the same template on the same group the same day: same grouper_file row
      String tokenB = runTemplate(sessionB, groupId);
      assertNotNull("B never got a download link", tokenB);
      SimpleHttpResponse b1 = download(sessionB, tokenB);
      assertEquals("B's download: " + describe(b1), 200, b1.statusCode());
      assertTrue("B's download is B's report: " + describe(b1), b1.body().contains(CONTENT_PREFIX + USER_B));

      // step 3: A's old link must not serve B's report: 404
      SimpleHttpResponse a1again = download(sessionA, tokenA1);
      assertFalse("A's stale link must never serve B's report: " + describe(a1again),
          a1again.body() != null && a1again.body().contains(CONTENT_PREFIX + USER_B));
      assertEquals("A's stale link is a 404: " + describe(a1again), 404, a1again.statusCode());
      // and it stays dead (the token was removed from the session)
      assertEquals("A's stale link stays a 404", 404, download(sessionA, tokenA1).statusCode());

      // step 4: A runs again and downloads A's own report
      String tokenA2 = runTemplate(sessionA, groupId);
      assertNotNull("A's second run gave no link", tokenA2);
      SimpleHttpResponse a2 = download(sessionA, tokenA2);
      assertEquals("A's second download: " + describe(a2), 200, a2.statusCode());
      assertTrue("A's second download is A's report: " + describe(a2), a2.body().contains(CONTENT_PREFIX + USER_A));

      // and now B's link is the stale one
      SimpleHttpResponse b1again = download(sessionB, tokenB);
      assertFalse("B's stale link must never serve A's report", b1again.body() != null
          && b1again.body().contains(CONTENT_PREFIX + USER_A));
      assertEquals("B's stale link is a 404: " + describe(b1again), 404, b1again.statusCode());

      // a token is bound to its session: B cannot use A's live link
      SimpleHttpResponse crossSession = download(sessionB, tokenA2);
      assertEquals("A's token in B's session is a 404: " + describe(crossSession), 404, crossSession.statusCode());

    } finally {
      cleanUp109();
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
