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
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.authentication.GrouperPassword;
import edu.internet2.middleware.grouper.authentication.GrouperPasswordSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.framework.TestCase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * Security advisory H7 (alternateUiRouteAdmissionAuthorization), HTTP check against the running
 * local UI: with require.group.for.logins set, a subject outside the login group must be denied
 * on the /grouperExternal/app/ servlet alias and with a forged operation=Misc.* parameter, the
 * same as on the normal route.  GrouperUiRouteSectionTest covers the GrouperUiRouteSection
 * classification with a proxy request; this test covers what it cannot: GrouperUiFilter admission
 * through the real servlet mappings, plus that an allowed subject and the external self register
 * screen still work.
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which
 * resets the database the running UI uses).  Plain HTTP requests with the dev form login
 * (grouper.is.ui.formAuthn) and the OWASP CSRF token from OwaspJavaScriptServlet.</p>
 *
 * <p>require.group.for.logins is global, and other tests may use the same Tomcat at the same
 * time, so the login group holds GrouperSystem and test.subject.0-9 (everyone who can log in
 * locally), and the excluded subject is this test's own secPatch21user (jdbc subject row and a UI
 * password row, both removed at the end).  Cleanup removes the config first, waits until the UI
 * admits the excluded subject again, and only then deletes the group, so the UI never points at a
 * missing login group.</p>
 */
public class GrouperUiRouteAdmissionHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "secPatch21";

  /** login group */
  private static final String GROUP_NAME = STEM_NAME + ":allowed";

  /** subject outside the login group (jdbc subject row this test inserts) */
  private static final String EXCLUDED_USER = "secPatch21user";

  /** UI password of the excluded subject (local test value, row deleted at the end) */
  private static final String EXCLUDED_PASS = "secPatch21pass";

  /** subject inside the login group */
  private static final String ALLOWED_USER = "test.subject.1";

  /** the grouper-ui config key under test */
  private static final String REQUIRE_GROUP_KEY = "require.group.for.logins";

  /** a response that rendered a normal UI screen into the main content div */
  private static final String MAIN_CONTENT_MARKER = "grouperMainContentDivId";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperUiRouteAdmissionHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public GrouperUiRouteAdmissionHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperUiRouteAdmissionHttpTest(String name) {
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
      if (response.statusCode() == 401
          && response.headers().firstValue("WWW-Authenticate").orElse("").toLowerCase().startsWith("basic")) {
        // 2.6.19 has no form login (GrouperUiFormLogin is 4.x+); its local UI runs with
        // grouper.is.ui.basicAuthn=true, which checks the same UI password on every request
        this.basicAuthorization = "Basic " + java.util.Base64.getEncoder().encodeToString(
            (username + ":" + password).getBytes(StandardCharsets.UTF_8));
        response = get("/grouperUi/app/UiV2Main.index");
        // only a bad password is 401; a subject outside the login group still gets past authn
        // and is refused later by the section check, so do not require 200 here
        assertTrue("basic auth login of " + username + ": " + describe(response), response.statusCode() != 401);
      } else {
        // a good login is a post/redirect/get; a bad one shows the form again (200)
        assertEquals("form login of " + username + " must redirect (on 2.6.19 set grouper.is.ui.basicAuthn=true): "
            + response.body(), 302, response.statusCode());
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
   * a fresh session of the excluded subject asks for the home screen the normal way
   * @param baseUrl
   * @return true if the UI rendered it (login group not enforced)
   * @throws Exception
   */
  private static boolean excludedSubjectAdmitted(String baseUrl) throws Exception {
    UiSession session = new UiSession(baseUrl);
    session.login(EXCLUDED_USER, EXCLUDED_PASS);
    if (renderedScreen(session.postAjax("/grouperUi/app/UiV2Main.indexMain", "operation=UiV2Main.indexMain"))) {
      return true;
    }
    // also the same probe as the control below (fresh session): on some releases (7.2.1 with basic
    // auth) the ajax probe was refused while the page still rendered, so the wait ended too early
    UiSession pageSession = new UiSession(baseUrl);
    pageSession.login(EXCLUDED_USER, EXCLUDED_PASS);
    SimpleHttpResponse page = pageSession.getAjax("/grouperUi/app/UiV2Main.index");
    return renderedScreen(page) || (page.statusCode() == 200 && page.body().contains("<title>Grouper"));
  }

  // ---------------------------------------------------------------- setup / cleanup

  /**
   * insert the excluded subject, give it a UI password, make the login group, set the config
   */
  private static void setUp21() {
    cleanUp21Data();
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      // the excluded subject: a jdbc subject row like test.subject.N
      HibernateSession.bySqlStatic().executeSql("insert into subject (subjectid, subjecttypeid, name) values (?, 'person', ?)",
          listOf(EXCLUDED_USER, "my name is " + EXCLUDED_USER));
      String[][] attributes = {{"name", "name." + EXCLUDED_USER}, {"loginid", "id." + EXCLUDED_USER},
          {"description", "description." + EXCLUDED_USER}, {"email", EXCLUDED_USER + "@example.edu"}};
      for (String[] attribute : attributes) {
        HibernateSession.bySqlStatic().executeSql("insert into subjectattribute (subjectid, name, value, searchvalue) values (?, ?, ?, ?)",
            listOf(EXCLUDED_USER, attribute[0], attribute[1], attribute[1].toLowerCase()));
      }
      new GrouperPasswordSave().assignApplication(GrouperPassword.Application.UI)
          .assignUsername(EXCLUDED_USER).assignPassword(EXCLUDED_PASS).save();

      // login group: everyone who can log in to the local UI, except the excluded subject
      Group group = new GroupSave(rootSession).assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
      group.addMember(SubjectFinder.findRootSubject(), false);
      for (int i = 0; i < 10; i++) {
        group.addMember(SubjectFinder.findById("test.subject." + i, true), false);
      }

      new GrouperDbConfig().configFileName("grouper-ui.properties").propertyName(REQUIRE_GROUP_KEY)
          .value(GROUP_NAME).store();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * remove the config (this test is the only one that sets it)
   */
  private static void deleteConfig21() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      new GrouperDbConfig().configFileName("grouper-ui.properties").propertyName(REQUIRE_GROUP_KEY).delete();
    } catch (RuntimeException re) {
      // not there
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the group, folder, password and subject this test created
   */
  private static void cleanUp21Data() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, GROUP_NAME, false);
      if (group != null) {
        group.delete();
      }
      Stem stem = StemFinder.findByName(rootSession, STEM_NAME, false);
      if (stem != null) {
        stem.delete();
      }
      HibernateSession.bySqlStatic().executeSql("delete from grouper_password_recently_used where grouper_password_id in "
          + "(select id from grouper_password where username = ? and application = 'UI')", listOf(EXCLUDED_USER));
      HibernateSession.bySqlStatic().executeSql("delete from grouper_password where username = ? and application = 'UI'", listOf(EXCLUDED_USER));
      HibernateSession.bySqlStatic().executeSql("delete from subjectattribute where subjectid = ?", listOf(EXCLUDED_USER));
      HibernateSession.bySqlStatic().executeSql("delete from subject where subjectid = ?", listOf(EXCLUDED_USER));
      // the member row the UI made when the excluded subject logged in, and its membership in the
      // UI user data group (every UI login is added there)
      List<String> memberIds = new GcDbAccess().sql("select id from grouper_members where subject_id = ? and subject_source = 'jdbc'")
          .addBindVar(EXCLUDED_USER).selectList(String.class);
      Group userDataGroup = GroupFinder.findByName(rootSession, GrouperConfig.retrieveConfig().propertyValueString(
          "grouper.rootStemForBuiltinObjects", "etc") + ":grouperUi:grouperUiUserData", false);
      for (String memberId : memberIds) {
        Member member = MemberFinder.findByUuid(rootSession, memberId, false);
        if (member != null && userDataGroup != null) {
          userDataGroup.deleteMember(member, false);
        }
        try {
          HibernateSession.bySqlStatic().executeSql("delete from grouper_members where id = ?", listOf(memberId));
        } catch (RuntimeException re) {
          System.out.println("secPatch21: member row " + memberId + " still referenced, left in place: " + re.getMessage());
        }
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
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

  // ---------------------------------------------------------------- test

  /**
   * the excluded subject is denied on the normal route, the external alias and the forged
   * operation=Misc.* parameter; the allowed subject and the self register screen still work
   * @throws Exception
   */
  public void testAlternateRoutesDenied() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    setUp21();
    try {
      // the UI reads database config through a cache; wait until it enforces the login group
      boolean enforced = false;
      for (int i = 0; i < 20 && !enforced; i++) {
        enforced = !excludedSubjectAdmitted(baseUrl);
        if (!enforced) {
          Thread.sleep(5000);
        }
      }
      assertTrue("UI never enforced " + REQUIRE_GROUP_KEY + "=" + GROUP_NAME, enforced);

      // control: the normal route denies the excluded subject (fresh session for each request,
      // since an allowed section is cached in the http session)
      UiSession control = new UiSession(baseUrl);
      control.login(EXCLUDED_USER, EXCLUDED_PASS);
      SimpleHttpResponse controlResponse = control.getAjax("/grouperUi/app/UiV2Main.index");
      assertFalse("control must be denied: " + describe(controlResponse), renderedScreen(controlResponse)
          || (controlResponse.statusCode() == 200 && controlResponse.body().contains("<title>Grouper")));

      // bypass 1: the external servlet alias, page and ajax
      UiSession external = new UiSession(baseUrl);
      external.login(EXCLUDED_USER, EXCLUDED_PASS);
      SimpleHttpResponse externalIndex = external.getAjax("/grouperExternal/app/UiV2Main.index");
      assertFalse("external alias UiV2Main.index must not render the UI: " + describe(externalIndex),
          externalIndex.statusCode() == 200 && externalIndex.body().contains("<title>Grouper"));
      SimpleHttpResponse externalIndexMain = external.postAjax("/grouperExternal/app/UiV2Main.indexMain", "operation=UiV2Main.indexMain");
      assertFalse("external alias UiV2Main.indexMain must not render the home screen: " + describe(externalIndexMain),
          renderedScreen(externalIndexMain));
      assertFalse("no recent activity through the external alias", externalIndexMain.body().contains("Recent activity"));

      // bypass 2: forged operation=Misc.* on the normal mapping
      UiSession misc = new UiSession(baseUrl);
      misc.login(EXCLUDED_USER, EXCLUDED_PASS);
      SimpleHttpResponse miscResponse = misc.postAjax("/grouperUi/app/UiV2Subject.viewSubject?operation=Misc.probe",
          "subjectId=" + enc(EXCLUDED_USER) + "&sourceId=jdbc");
      assertFalse("operation=Misc.probe must not return the subject view: " + describe(miscResponse),
          renderedScreen(miscResponse));
      assertFalse("no subject view JSON", miscResponse.body().contains("\"subjectView"));

      // the same excluded session on the external alias for a lookalike class is refused too
      UiSession lookalike = new UiSession(baseUrl);
      lookalike.login(EXCLUDED_USER, EXCLUDED_PASS);
      SimpleHttpResponse lookalikeResponse = lookalike.postAjax("/grouperExternal/app/UiV2Subject.viewSubject",
          "subjectId=" + enc(EXCLUDED_USER) + "&sourceId=jdbc");
      assertFalse("external alias UiV2Subject.viewSubject must be refused: " + describe(lookalikeResponse),
          renderedScreen(lookalikeResponse));

      // allowed subject: normal UI home and a subject screen still work
      UiSession allowed = new UiSession(baseUrl);
      allowed.login(ALLOWED_USER, "pass");
      SimpleHttpResponse allowedHome = allowed.postAjax("/grouperUi/app/UiV2Main.indexMain", "operation=UiV2Main.indexMain");
      assertTrue("allowed subject home screen: " + describe(allowedHome), renderedScreen(allowedHome));
      SimpleHttpResponse allowedSubject = allowed.postAjax("/grouperUi/app/UiV2Subject.viewSubject",
          "subjectId=" + enc(ALLOWED_USER) + "&sourceId=jdbc");
      assertTrue("allowed subject view: " + describe(allowedSubject), renderedScreen(allowedSubject));
      // the external alias now runs only the self register screens, for allowed subjects too
      SimpleHttpResponse allowedExternal = allowed.postAjax("/grouperExternal/app/UiV2Main.indexMain", "operation=UiV2Main.indexMain");
      assertFalse("external alias UiV2Main.indexMain is refused for an allowed subject too: " + describe(allowedExternal),
          renderedScreen(allowedExternal));

      // the external self register screen and its ../app/UiV2ExternalSubjectSelfRegister.* call still work
      UiSession selfRegister = new UiSession(baseUrl);
      selfRegister.login(ALLOWED_USER, "pass");
      SimpleHttpResponse publicPage = selfRegister.get(
          "/grouperExternal/public/UiV2Public.index?operation=UiV2ExternalSubjectSelfRegister.externalSubjectSelfRegister");
      assertEquals("self register page: " + describe(publicPage), 200, publicPage.statusCode());
      SimpleHttpResponse selfRegisterAjax = selfRegister.postAjax(
          "/grouperExternal/app/UiV2ExternalSubjectSelfRegister.externalSubjectSelfRegister",
          "operation=UiV2ExternalSubjectSelfRegister.externalSubjectSelfRegister");
      assertEquals("self register ajax: " + describe(selfRegisterAjax), 200, selfRegisterAjax.statusCode());
      assertTrue("self register ajax returns gui actions: " + describe(selfRegisterAjax),
          selfRegisterAjax.body().startsWith("{") && selfRegisterAjax.body().contains("\"actions\""));

    } finally {
      deleteConfig21();
      // do not delete the login group while the UI may still require it
      boolean released = false;
      for (int i = 0; i < 20 && !released; i++) {
        try {
          released = excludedSubjectAdmitted(baseUrl);
        } catch (Throwable t) {
          released = false;
        }
        if (!released) {
          Thread.sleep(5000);
        }
      }
      if (!released) {
        System.out.println("secPatch21: UI still enforced the login group after 100 s; deleting it anyway");
      }
      cleanUp21Data();
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
