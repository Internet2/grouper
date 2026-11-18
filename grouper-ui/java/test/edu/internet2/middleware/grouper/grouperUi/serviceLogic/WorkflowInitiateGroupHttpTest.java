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
 * Security advisory H1 (workflowInitiateGroupMismatch), HTTP check against the running local UI: the
 * real UiV2GrouperWorkflow.workflowInitiateSubmit handler takes the workflow config
 * (workflowAttributeAssignId) and the group (groupId) as two request params and resolves both as
 * root.  A request that pairs group a's config with group b's uuid must be refused ("not allowed")
 * and write no workflow instance on group b; the same request with group a's uuid creates the
 * instance on group a as before.  GrouperWorkflowInitiateGroupCheckTest only tests the check
 * class; this test covers the handler wiring, checked against the attribute and grouper_file rows
 * the running UI wrote.
 *
 * <p>Setup (this test's own, removed at the end): folder test:secPatch55 with group a, which holds
 * workflow config secPatch55 (default approvals: anyone may initiate), and group b, on which
 * test.subject.0 has no privileges (GrouperAll VIEW/READ revoked).  Workflow is off by default
 * (advisory H1), so workflow.enable=true goes in the database config for the test and the prior
 * value is restored at the end.</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which
 * resets the database the running UI uses).  Plain HTTP requests, dev form login, OWASP
 * CSRF token from OwaspJavaScriptServlet.</p>
 */
public class WorkflowInitiateGroupHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch55";

  /** owner group of the workflow config */
  private static final String GROUP_A_NAME = STEM_NAME + ":a";

  /** group the forged request points at */
  private static final String GROUP_B_NAME = STEM_NAME + ":b";

  /** workflow config id */
  private static final String WORKFLOW_ID = "secPatch55";

  /** requester, not wheel, no privileges on b */
  private static final String REQUESTER = "test.subject.0";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(WorkflowInitiateGroupHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public WorkflowInitiateGroupHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public WorkflowInitiateGroupHttpTest(String name) {
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

    /** "Basic ..." header sent on every request when the release has no form login (2.6.19), else null */
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
      // a good login is a post/redirect/get; a bad one shows the form again (200)
      if (response.statusCode() != 302) {
        // 2.6.19 has no dev form login (GrouperUiFormLogin is 4.x+): its local UI uses HTTP basic
        // auth (grouper.is.ui.basicAuthn=true, UI rows in grouper_password), so the POST above got
        // a 401.  Send the credentials on every request of this session instead.
        this.basicAuthorization = "Basic " + java.util.Base64.getEncoder().encodeToString(
            (username + ":" + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        SimpleHttpResponse check = send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + "/grouperUi/app/UiV2Main.index")).GET());
        assertTrue("login of " + username + " (form login: " + response.statusCode() + " " + response.body()
            + "; basic auth: " + check.statusCode() + ")",
            check.statusCode() == 200 && !check.body().contains("grouperFormLoginUsername"));
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
   * delete the instance snapshots, the groups (their workflow attributes go with them) and the folder
   */
  private static void deleteData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      // the instance's form snapshot is a grouper_file row (workflow.storage.option=database);
      // deleting the group does not remove it
      new GcDbAccess().sql("delete from grouper_file where file_path like ?")
          .addBindVar("/grouperWorkflow/" + WORKFLOW_ID + "_%").executeSql();
      for (String groupName : new String[] {GROUP_A_NAME, GROUP_B_NAME}) {
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
   * @param groupId
   * @return number of attribute assignments directly on the group (workflow config and instance markers)
   */
  private static int assignmentsOnGroup(String groupId) {
    return new GcDbAccess().sql("select count(*) from grouper_attribute_assign where owner_group_id = ?")
        .addBindVar(groupId).select(int.class);
  }

  /**
   * @return number of instance form snapshots of this test's config
   */
  private static int snapshotFiles() {
    return new GcDbAccess().sql("select count(*) from grouper_file where file_path like ?")
        .addBindVar("/grouperWorkflow/" + WORKFLOW_ID + "_%").select(int.class);
  }

  // ---------------------------------------------------------------- test

  /**
   * initiate with a's config and b's uuid refused, nothing on b; with a's uuid, instance on a
   * @throws Exception
   */
  public void testInitiateGroupMustBeConfigOwner() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    deleteData();
    enableWorkflow();
    try {
      String groupAId;
      String groupBId;
      String markerId;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        Group groupA = new GroupSave(rootSession).assignName(GROUP_A_NAME).assignCreateParentStemsIfNotExist(true).save();
        Group groupB = new GroupSave(rootSession).assignName(GROUP_B_NAME).assignCreateParentStemsIfNotExist(true).save();
        // b: the requester has no privileges at all
        groupB.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
        groupB.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
        assertFalse("requester has no VIEW on b", groupB.canHavePrivilege(SubjectFinder.findById(REQUESTER, true), "view", false));
        groupAId = groupA.getId();
        groupBId = groupB.getId();

        // config on a with the default approvals, params and form: anyone may initiate
        String approvals = GrouperWorkflowApprovalStates.getDefaultApprovalStatesString(groupAId);
        String params = GrouperWorkflowConfigParams.getDefaultConfigParamsString();
        GrouperWorkflowConfig config = new GrouperWorkflowConfig();
        config.setWorkflowConfigApprovalsString(approvals);
        config.setWorkflowApprovalStates(GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(approvals));
        config.setWorkflowConfigParamsString(params);
        config.setConfigParams(GrouperWorkflowConfigParams.buildParamsFromJsonString(params));
        config.setOwnerGroup(groupA);
        config.setWorkflowConfigDescription("security advisory H1 http test");
        config.setWorkflowConfigEnabled("true");
        config.setWorkflowConfigId(WORKFLOW_ID);
        config.setWorkflowConfigName("secPatch55");
        config.setWorkflowConfigSendEmail(false);
        config.setWorkflowConfigType("grouper");
        config.setWorkflowConfigViewersGroupId(groupAId);
        GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(config, groupA);
        markerId = GrouperWorkflowConfigService.getWorkflowConfig(groupA, WORKFLOW_ID).getAttributeAssignmentMarkerId();
        assertNotNull("config marker assignment id", markerId);
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }

      UiSession requester = new UiSession(baseUrl);
      requester.login(REQUESTER, "pass");

      // the running UI sees workflow.enable through the database config cache: wait for the join form
      SimpleHttpResponse joinForm = null;
      for (int i = 0; i < 20; i++) {
        joinForm = requester.postAjax("/grouperUi/app/UiV2GrouperWorkflow.showJoinGroupForm", "groupId=" + enc(groupAId));
        if (joinForm.body().contains("groupJoinInitiateWorkflowId")) {
          break;
        }
        Thread.sleep(3000);
      }
      assertTrue("join form opens on a (workflow.enable seen): " + describe(joinForm),
          joinForm.body().contains("groupJoinInitiateWorkflowId"));
      assertTrue("join form posts a's config marker: " + describe(joinForm), joinForm.body().contains(markerId));

      int assignmentsOnA = assignmentsOnGroup(groupAId);
      assertEquals("b starts with no attribute assignments", 0, assignmentsOnGroup(groupBId));
      assertEquals("no snapshot yet", 0, snapshotFiles());

      // step 1 (attack): a's config, groupId swapped to b's uuid (query string and form, like the JSP)
      SimpleHttpResponse forged = requester.postAjax("/grouperUi/app/UiV2GrouperWorkflow.workflowInitiateSubmit?groupId="
          + enc(groupBId) + "&workflowAttributeAssignId=" + enc(markerId), "notes=" + enc("secPatch55 forged"));
      assertEquals("forged initiate: " + describe(forged), 200, forged.statusCode());
      assertTrue("forged initiate shows not allowed: " + describe(forged),
          forged.body().contains("You are not allowed to request membership of this group."));
      assertFalse("forged initiate no success: " + describe(forged), forged.body().contains("\"messageType\":\"success\""));
      assertEquals("no workflow instance on b", 0, assignmentsOnGroup(groupBId));
      assertEquals("nothing new on a either", assignmentsOnA, assignmentsOnGroup(groupAId));
      assertEquals("no snapshot written", 0, snapshotFiles());

      // step 2 (control): the unmodified request, a's config with a's uuid
      SimpleHttpResponse legit = requester.postAjax("/grouperUi/app/UiV2GrouperWorkflow.workflowInitiateSubmit?groupId="
          + enc(groupAId) + "&workflowAttributeAssignId=" + enc(markerId), "notes=" + enc("secPatch55 legit"));
      assertEquals("initiate on a: " + describe(legit), 200, legit.statusCode());
      assertTrue("initiate on a succeeds: " + describe(legit), legit.body().contains("\"messageType\":\"success\""));
      assertEquals("instance created on a", assignmentsOnA + 1, assignmentsOnGroup(groupAId));
      assertEquals("one snapshot written", 1, snapshotFiles());
      assertEquals("still nothing on b", 0, assignmentsOnGroup(groupBId));

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
