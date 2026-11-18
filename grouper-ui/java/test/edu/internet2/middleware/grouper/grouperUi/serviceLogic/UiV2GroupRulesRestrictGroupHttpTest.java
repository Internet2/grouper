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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory H3 (rulesUiRestrictGroupFailsOpen), HTTP check against the running local UI:
 * with rules.restrictRulesUiToMembersOfThisGroupName set to a group the excluded user cannot VIEW,
 * RulesContainer.isCanAddRule and GuiRuleDefinition.isCanEditRule (through RuleUiRestrictGroup)
 * keep that user out of the rules UI.  The excluded group ADMIN sees no add / edit / delete rule
 * controls, and a forged UiV2Group.addRuleOnGroupSubmit and UiV2Group.deleteRuleOnGroup are
 * refused with no rule saved or removed (checked with SQL).  Positive control: a member of the
 * restrict group with the same ADMIN sees the controls, adds a rule (saved) and deletes it.
 * RuleRestrictRulesUiGroupTest covers only RuleUiRestrictGroup; this covers the UI bean wiring.
 *
 * <p>Setup (this test's own, removed at the end): folder secPatch50 with secPatch50:rulesEditors
 * (GrouperAll VIEW/READ revoked, test.subject.7 member) and secPatch50:mine (test.subject.6 and
 * test.subject.7 ADMIN).  Database config rules.restrictRulesUiToMembersOfThisGroupName =
 * secPatch50:rulesEditors while the test runs, restored in finally.  test.subject.6/7 instead of
 * the usual 0/1 because the UI caches the gate answer per subject for 5 minutes
 * (GuiRuleDefinition.rulesEditors), so a recent rules screen by another test would answer from the
 * cache, and this test's denial is not left cached for subjects other tests use.</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which
 * resets the database the running UI uses).  Plain HTTP requests, dev form login, OWASP
 * CSRF token from OwaspJavaScriptServlet.</p>
 */
public class UiV2GroupRulesRestrictGroupHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "secPatch50";

  /** the restrict group */
  private static final String RULES_EDITORS = STEM_NAME + ":rulesEditors";

  /** the group the rules go on */
  private static final String MINE = STEM_NAME + ":mine";

  /** group ADMIN, not in the restrict group, cannot VIEW it */
  private static final String EXCLUDED = "test.subject.6";

  /** group ADMIN and member of the restrict group */
  private static final String EDITOR = "test.subject.7";

  /** the setting under test */
  private static final String CONFIG_KEY = "rules.restrictRulesUiToMembersOfThisGroupName";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2GroupRulesRestrictGroupHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public UiV2GroupRulesRestrictGroupHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2GroupRulesRestrictGroupHttpTest(String name) {
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

      SimpleHttpResponse csrfJs = send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + "/grouperExternal/public/OwaspJavaScriptServlet")).GET());
      Matcher matcher = Pattern.compile("masterTokenValue = '([^']+)'").matcher(csrfJs.body());
      assertTrue("CSRF token for " + username, matcher.find());
      this.csrfToken = matcher.group(1);
    }

    /**
     * ajax POST with the CSRF header and a form body, like the UI's own requests
     * @param operation e.g. UiV2GroupPermission.limitMenu
     * @param formBody urlencoded body
     * @return the response
     * @throws Exception
     */
    SimpleHttpResponse postAjax(String operation, String formBody) throws Exception {
      return send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + "/grouperUi/app/" + operation))
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

  /**
   * @param response
   * @return true if the response shows a success message
   */
  private static boolean isSuccess(SimpleHttpResponse response) {
    return response.body() != null && response.body().contains("\"messageType\":\"success\"");
  }

  // ---------------------------------------------------------------- db helpers

  /**
   * @param groupId
   * @return ids of the rule assignments on the group, read from the database (the UI JVM wrote them)
   */
  private static List<String> ruleIdsInDb(String groupId) {
    return new GcDbAccess().sql("select aa.id from grouper_attribute_assign aa, grouper_attribute_def_name adn "
        + "where adn.id = aa.attribute_def_name_id and aa.owner_group_id = ? and adn.name = 'etc:attribute:rules:rule'")
        .addBindVar(groupId).selectList(String.class);
  }

  /**
   * @param key
   * @return config_value of the grouper.properties database row, or null if there is no row
   */
  private static String[] dbConfigRow(String key) {
    List<String> values = new GcDbAccess().sql("select config_value from grouper_config where "
        + "config_file_name = 'grouper.properties' and config_key = ?").addBindVar(key).selectList(String.class);
    return values.size() == 0 ? null : new String[] {values.get(0)};
  }

  /**
   * delete the folder and everything in it (the rules are attribute assignments on its group)
   */
  private static void cleanUp50() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Stem stem = StemFinder.findByName(rootSession, STEM_NAME, false);
      if (stem != null) {
        stem.obliterate(false, false);
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * a valid predefined pattern (the md's "Example attack" step 3)
   * @param groupId
   * @return form body for UiV2Group.addRuleOnGroupSubmit
   */
  private static String addRuleBody(String groupId) {
    return "groupId=" + enc(groupId) + "&grouperRulePattern=VetoIfTooManyMembers"
        + "&" + enc("config_VetoIfTooManyMembers.limit") + "=1000"
        + "&" + enc("config_VetoIfTooManyMembers.message") + "=" + enc("secPatch50 too many members");
  }

  // ---------------------------------------------------------------- test

  /**
   * excluded group admin: no controls, forged add and delete refused; restrict group member: allowed
   * @throws Exception
   */
  public void testRestrictGroupNotViewableFailsClosed() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    cleanUp50();
    String[] priorConfig = dbConfigRow(CONFIG_KEY);
    boolean configStored = false;
    try {
      String mineId;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        Subject excluded = SubjectFinder.findById(EXCLUDED, true);
        Subject editor = SubjectFinder.findById(EDITOR, true);
        Group rulesEditors = new GroupSave(rootSession).assignName(RULES_EDITORS).assignCreateParentStemsIfNotExist(true).save();
        // the excluded user cannot VIEW the restrict group: the lookup in its own session returned
        // null, which used to skip the gate
        rulesEditors.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
        rulesEditors.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
        rulesEditors.addMember(editor, false);
        Group mine = new GroupSave(rootSession).assignName(MINE).save();
        mine.grantPriv(excluded, AccessPrivilege.ADMIN, false);
        mine.grantPriv(editor, AccessPrivilege.ADMIN, false);
        mineId = mine.getId();

        new GrouperDbConfig().configFileName("grouper.properties").propertyName(CONFIG_KEY).value(RULES_EDITORS).store();
        configStored = true;
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }
      // precondition: GrouperAll has no VIEW/READ on the restrict group
      int allPrivs = new GcDbAccess().sql("select count(*) from grouper_memberships_lw_v where group_name = ? "
          + "and subject_source = 'g:isa' and subject_id = 'GrouperAll' and list_name in ('viewers', 'readers')")
          .addBindVar(RULES_EDITORS).select(int.class);
      assertEquals("GrouperAll has no VIEW/READ on " + RULES_EDITORS, 0, allPrivs);

      // the UI sees database config through the change notification within seconds
      Thread.sleep(10000);

      // ---- step 3 first (so the existing rule of steps 1-2 is made through the UI): the editor
      UiSession editorSession = new UiSession(baseUrl);
      editorSession.login(EDITOR, "pass");
      SimpleHttpResponse editorAdd = editorSession.postAjax("UiV2Group.addRuleOnGroupSubmit", addRuleBody(mineId));
      assertTrue("restrict group member adds a rule: " + describe(editorAdd), isSuccess(editorAdd));
      List<String> ruleIds = ruleIdsInDb(mineId);
      assertEquals("one rule saved by the editor", 1, ruleIds.size());
      String ruleId = ruleIds.get(0);
      SimpleHttpResponse editorRules = editorSession.postAjax("UiV2Group.viewGroupRules", "groupId=" + enc(mineId));
      assertTrue("editor sees add, edit and delete rule controls: " + describe(editorRules),
          editorRules.body().contains("grouperMainContentDivId")
          && editorRules.body().contains("UiV2Group.addRuleOnGroup")
          && editorRules.body().contains("UiV2Group.editRuleOnGroup")
          && editorRules.body().contains("UiV2Group.deleteRuleOnGroup"));

      // ---- step 1: the excluded admin opens the rules screen: the rule is there, no controls.
      // The gate answer is cached 5 minutes per subject in the UI, so if a stale "allowed" answer
      // is cached for this subject, wait for it to expire
      UiSession excludedSession = new UiSession(baseUrl);
      excludedSession.login(EXCLUDED, "pass");
      SimpleHttpResponse excludedRules = null;
      long started = System.currentTimeMillis();
      while (true) {
        excludedRules = excludedSession.postAjax("UiV2Group.viewGroupRules", "groupId=" + enc(mineId));
        assertTrue("excluded admin can open the rules screen: " + describe(excludedRules),
            excludedRules.body().contains("grouperMainContentDivId"));
        if (!excludedRules.body().contains("UiV2Group.editRuleOnGroup") || System.currentTimeMillis() - started > 330000) {
          break;
        }
        System.out.println("secPatch50: excluded user still sees rule controls (cached gate answer?), waiting");
        Thread.sleep(30000);
      }
      assertFalse("excluded admin sees no add rule control: " + describe(excludedRules),
          excludedRules.body().contains("UiV2Group.addRuleOnGroup"));
      assertFalse("excluded admin sees no edit rule control: " + describe(excludedRules),
          excludedRules.body().contains("UiV2Group.editRuleOnGroup"));
      assertFalse("excluded admin sees no delete rule control: " + describe(excludedRules),
          excludedRules.body().contains("UiV2Group.deleteRuleOnGroup"));

      // ---- step 2: forged add submit by the excluded admin: refused, nothing saved
      SimpleHttpResponse excludedAdd = excludedSession.postAjax("UiV2Group.addRuleOnGroupSubmit", addRuleBody(mineId));
      assertFalse("excluded admin's forged add is refused: " + describe(excludedAdd), isSuccess(excludedAdd));
      assertEquals("no rule saved by the excluded admin", 1, ruleIdsInDb(mineId).size());

      // forged delete of the existing rule: refused (deleteRuleOnGroup checks isCanEditRule), rule stays
      SimpleHttpResponse excludedDelete = excludedSession.postAjax("UiV2Group.deleteRuleOnGroup",
          "groupId=" + enc(mineId) + "&ruleId=" + enc(ruleId));
      assertFalse("excluded admin's forged delete is refused: " + describe(excludedDelete), isSuccess(excludedDelete));
      assertEquals("rule still there after the excluded admin's delete", 1, ruleIdsInDb(mineId).size());

      // the refusals are the gate, not a dead session: the same session still renders the screen.
      // (A refused submit throws in the handler, which the UI answers with its generic
      // guiAjaxSessionProblem error JSON; the only difference from the editor's identical, successful
      // request is the restrict group membership.)
      SimpleHttpResponse excludedRulesAgain = excludedSession.postAjax("UiV2Group.viewGroupRules", "groupId=" + enc(mineId));
      assertTrue("excluded admin's session is still alive: " + describe(excludedRulesAgain),
          excludedRulesAgain.body().contains("grouperMainContentDivId"));

      // ---- positive control, continued: the editor deletes the rule
      SimpleHttpResponse editorDelete = editorSession.postAjax("UiV2Group.deleteRuleOnGroup",
          "groupId=" + enc(mineId) + "&ruleId=" + enc(ruleId));
      assertTrue("editor deletes the rule: " + describe(editorDelete), isSuccess(editorDelete));
      assertEquals("rule deleted by the editor", 0, ruleIdsInDb(mineId).size());

      System.out.println("secPatch50: excluded admin saw no controls, forged add/delete refused; editor added and deleted");
    } finally {
      if (configStored) {
        GrouperSession rootSession = GrouperSession.startRootSession();
        try {
          if (priorConfig != null) {
            new GrouperDbConfig().configFileName("grouper.properties").propertyName(CONFIG_KEY).value(priorConfig[0]).store();
          } else {
            new GrouperDbConfig().configFileName("grouper.properties").propertyName(CONFIG_KEY).delete();
          }
        } finally {
          GrouperSession.stopQuietly(rootSession);
        }
      }
      cleanUp50();
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
