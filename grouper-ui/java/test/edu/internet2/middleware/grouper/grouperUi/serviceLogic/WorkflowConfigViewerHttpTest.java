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
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalStates;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfig;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigParams;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigService;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * Security advisory H1 (workflowConfigViewerAuthorization), HTTP check against the running local UI:
 * the real UiV2GrouperWorkflow.viewForms handler lists only the workflow configs the user may see
 * (configure on the group, or that config's own viewers group), and viewWorkflowConfigDetails
 * refuses a config the user may not see, with no config id, name, description, approvals JSON or
 * form in the response.  GrouperWorkflowConfigViewCheckTest only tests the check class; this test
 * covers the list filter written in the handler and the details gate.
 *
 * <p>Setup (this test's own, removed at the end): folder test:secPatch112 with group g holding
 * config A (viewers group test:secPatch112:viewersA, member test.subject.1) and config B (no viewers
 * group); test.subject.0 and test.subject.1 VIEW only on g (GrouperAll READ/VIEW revoked),
 * test.subject.3 ADMIN.  None of them is wheel or in the workflow editors group.  Workflow is off
 * by default (advisory H1), so workflow.enable=true goes in the database config for the test and the
 * prior value is restored at the end.  A read path: there is no state to change, so the checks are
 * on what the responses contain.</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which
 * resets the database the running UI uses).  Plain HTTP requests, dev form login, OWASP
 * CSRF token from OwaspJavaScriptServlet.</p>
 */
public class WorkflowConfigViewerHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch112";

  /** owner group of both configs */
  private static final String GROUP_NAME = STEM_NAME + ":g";

  /** viewers group of config A */
  private static final String VIEWERS_A_NAME = STEM_NAME + ":viewersA";

  /** config A id, name and description (distinct strings to look for in responses) */
  private static final String[] CONFIG_A = {"secPatch112a", "secPatch112Alpha", "secPatch112 description alpha"};

  /** config B id, name and description */
  private static final String[] CONFIG_B = {"secPatch112b", "secPatch112Bravo", "secPatch112 description bravo"};

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(WorkflowConfigViewerHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public WorkflowConfigViewerHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public WorkflowConfigViewerHttpTest(String name) {
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
     * @param path
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

  // ---------------------------------------------------------------- workflow.enable

  /** database value of workflow.enable before the test, null if there was no row */
  private String priorWorkflowEnable = null;

  /** true if there was a workflow.enable row before the test */
  private boolean hadWorkflowEnableRow = false;

  /**
   * turn workflow on for the running UI (database config) and for this JVM
   */
  private void enableWorkflow() {
    List<String> values = new GcDbAccess().sql("select config_value from grouper_config where "
        + "config_file_name = 'grouper.properties' and config_key = 'workflow.enable'").selectList(String.class);
    this.hadWorkflowEnableRow = values.size() > 0;
    this.priorWorkflowEnable = this.hadWorkflowEnableRow ? values.get(0) : null;
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("workflow.enable").value("true").store();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("workflow.enable", "true");
  }

  /**
   * put workflow.enable back the way it was
   */
  private void restoreWorkflowEnable() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("workflow.enable");
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      if (this.hadWorkflowEnableRow) {
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("workflow.enable")
            .value(this.priorWorkflowEnable).store();
      } else {
        new GrouperDbConfig().configFileName("grouper.properties").propertyName("workflow.enable").delete();
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  // ---------------------------------------------------------------- data

  /**
   * delete the groups (their workflow attributes go with them) and the folder
   */
  private static void deleteData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String groupName : new String[] {GROUP_NAME, VIEWERS_A_NAME}) {
        Group group = GroupFinder.findByName(rootSession, groupName, false);
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

  /**
   * store a config with the default approvals, params and form
   * @param group
   * @param idNameDescription
   * @param viewersGroupId or null
   */
  private static void saveConfig(Group group, String[] idNameDescription, String viewersGroupId) {
    String approvals = GrouperWorkflowApprovalStates.getDefaultApprovalStatesString(group.getId());
    String params = GrouperWorkflowConfigParams.getDefaultConfigParamsString();
    GrouperWorkflowConfig config = new GrouperWorkflowConfig();
    config.setWorkflowConfigApprovalsString(approvals);
    config.setWorkflowApprovalStates(GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(approvals));
    config.setWorkflowConfigParamsString(params);
    config.setConfigParams(GrouperWorkflowConfigParams.buildParamsFromJsonString(params));
    config.setOwnerGroup(group);
    config.setWorkflowConfigId(idNameDescription[0]);
    config.setWorkflowConfigName(idNameDescription[1]);
    config.setWorkflowConfigDescription(idNameDescription[2]);
    config.setWorkflowConfigEnabled("true");
    config.setWorkflowConfigSendEmail(false);
    config.setWorkflowConfigType("grouper");
    config.setWorkflowConfigViewersGroupId(viewersGroupId);
    GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(config, group);
  }

  /**
   * @param session
   * @param groupId
   * @return the group's workflow list
   * @throws Exception
   */
  private static SimpleHttpResponse list(UiSession session, String groupId) throws Exception {
    return session.postAjax("/grouperUi/app/UiV2GrouperWorkflow.viewForms", "groupId=" + enc(groupId));
  }

  /**
   * @param session
   * @param groupId
   * @param config
   * @return the config details screen
   * @throws Exception
   */
  private static SimpleHttpResponse details(UiSession session, String groupId, String[] config) throws Exception {
    return session.postAjax("/grouperUi/app/UiV2GrouperWorkflow.viewWorkflowConfigDetails",
        "groupId=" + enc(groupId) + "&workflowConfigId=" + enc(config[0]));
  }

  /**
   * @param response
   * @param config
   * @return true if the response shows the config's name or description
   */
  private static boolean shows(SimpleHttpResponse response, String[] config) {
    return response.body().contains(config[1]) || response.body().contains(config[2]);
  }

  /**
   * the details screen rendered the config
   * @param response
   * @param config
   * @param who
   */
  private static void assertDetailsShown(SimpleHttpResponse response, String[] config, String who) {
    assertTrue(who + " sees details of " + config[0] + ": " + describe(response),
        response.statusCode() == 200 && response.body().contains("grouperMainContentDivId")
        && response.body().contains(config[1]) && response.body().contains(config[2]));
  }

  /**
   * the details request was refused: nothing of the config (id, name, description, approvals json,
   * form) in the response
   * @param response
   * @param config
   * @param who
   */
  private static void assertDetailsRefused(SimpleHttpResponse response, String[] config, String who) {
    String body = response.body() == null ? "" : response.body();
    // the handler throws "Operation not permitted", which the UI shows as its generic error
    assertTrue(who + " gets an error for " + config[0] + ": " + describe(response),
        body.contains("\"messageType\":\"error\""));
    assertFalse(who + " must not get the details screen of " + config[0] + ": " + describe(response),
        body.contains("grouperMainContentDivId"));
    assertFalse(who + " must not see the name/description of " + config[0], shows(response, config));
    assertFalse(who + " must not see the approvals json of " + config[0], body.contains("assignToGroup"));
    assertFalse(who + " must not see the form of " + config[0], body.contains("notesForApprovers"));
  }

  // ---------------------------------------------------------------- test

  /**
   * viewer of A lists only A and cannot open B; a VIEW-only user opens neither; a group admin and
   * GrouperSystem list and open both
   * @throws Exception
   */
  public void testWorkflowConfigListAndDetailsPerConfig() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    deleteData();
    enableWorkflow();
    try {
      String groupId;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        Group group = new GroupSave(rootSession).assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
        Group viewersA = new GroupSave(rootSession).assignName(VIEWERS_A_NAME).assignCreateParentStemsIfNotExist(true).save();
        viewersA.addMember(SubjectFinder.findById("test.subject.1", true), false);
        group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
        group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
        group.grantPriv(SubjectFinder.findById("test.subject.0", true), AccessPrivilege.VIEW, false);
        group.grantPriv(SubjectFinder.findById("test.subject.1", true), AccessPrivilege.VIEW, false);
        group.grantPriv(SubjectFinder.findById("test.subject.3", true), AccessPrivilege.ADMIN, false);
        groupId = group.getId();
        saveConfig(group, CONFIG_A, viewersA.getId());
        saveConfig(group, CONFIG_B, null);
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }

      UiSession viewer = new UiSession(baseUrl);
      viewer.login("test.subject.1", "pass");
      UiSession viewOnly = new UiSession(baseUrl);
      viewOnly.login("test.subject.0", "pass");
      UiSession admin = new UiSession(baseUrl);
      admin.login("test.subject.3", "pass");
      UiSession root = new UiSession(baseUrl);
      root.login("GrouperSystem", "pass");

      // the running UI sees workflow.enable through the database config cache: wait for the list
      SimpleHttpResponse rootList = null;
      for (int i = 0; i < 20; i++) {
        rootList = list(root, groupId);
        if (rootList.body().contains("grouperMainContentDivId")) {
          break;
        }
        Thread.sleep(3000);
      }

      // step 4 (control) first, so the screens are known to render: admin and GrouperSystem see both
      for (Object[] whoSession : new Object[][] {{"GrouperSystem", root}, {"test.subject.3 (ADMIN)", admin}}) {
        String who = (String) whoSession[0];
        UiSession session = (UiSession) whoSession[1];
        SimpleHttpResponse both = list(session, groupId);
        assertTrue(who + " lists A: " + describe(both), shows(both, CONFIG_A));
        assertTrue(who + " lists B: " + describe(both), shows(both, CONFIG_B));
        assertDetailsShown(details(session, groupId, CONFIG_A), CONFIG_A, who);
        assertDetailsShown(details(session, groupId, CONFIG_B), CONFIG_B, who);
      }

      // step 1: the viewer of A lists only A
      SimpleHttpResponse viewerList = list(viewer, groupId);
      assertTrue("viewer list renders: " + describe(viewerList), viewerList.body().contains("grouperMainContentDivId"));
      assertTrue("viewer lists A: " + describe(viewerList), shows(viewerList, CONFIG_A));
      assertFalse("viewer must not list B: " + describe(viewerList), shows(viewerList, CONFIG_B));

      // the viewer may open A (control), not B (attack: B's id typed into the request)
      assertDetailsShown(details(viewer, groupId, CONFIG_A), CONFIG_A, "test.subject.1 (viewer of A)");
      assertDetailsRefused(details(viewer, groupId, CONFIG_B), CONFIG_B, "test.subject.1 (viewer of A)");

      // step 3: VIEW only, in no viewers group: neither listed nor opened
      SimpleHttpResponse viewOnlyList = list(viewOnly, groupId);
      assertTrue("view-only list renders: " + describe(viewOnlyList), viewOnlyList.body().contains("grouperMainContentDivId"));
      assertFalse("view-only must not list A", shows(viewOnlyList, CONFIG_A));
      assertFalse("view-only must not list B", shows(viewOnlyList, CONFIG_B));
      assertDetailsRefused(details(viewOnly, groupId, CONFIG_A), CONFIG_A, "test.subject.0 (VIEW only)");
      assertDetailsRefused(details(viewOnly, groupId, CONFIG_B), CONFIG_B, "test.subject.0 (VIEW only)");

    } finally {
      try {
        deleteData();
      } finally {
        restoreWorkflowEnable();
      }
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
