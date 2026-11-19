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
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalStates;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfig;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigParams;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * Security advisory H1 (WorkflowAssignToAnyGroup), HTTP check against the running local UI: a
 * non-wheel group ADMIN who adds or edits a workflow through the real
 * UiV2GrouperWorkflow.workflowConfigAddSubmit / workflowConfigEditSubmit handlers gets the
 * author-validator error on the form when the complete state's assignToGroup names a group the
 * author has no UPDATE on (the wheel group), and nothing is saved; a group the author can UPDATE
 * saves, and an unchanged re-save works.  GrouperWorkflowConfigAuthorValidatorTest calls the
 * validator directly; this test covers what it cannot: the UI handlers calling it with the
 * logged-in subject, checked against the attribute rows the running UI wrote.
 *
 * <p>Setup (this test's own, removed at the end): folder test:secPatch01 with group a
 * (test.subject.0 ADMIN, owns the workflows) and group c (test.subject.0 UPDATE).  Workflow is off
 * by default (advisory H1), so workflow.enable=true goes in the database config for the test and the
 * prior value is restored at the end.</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which
 * resets the database the running UI uses).  Plain HTTP requests, dev form login, OWASP
 * CSRF token from OwaspJavaScriptServlet.</p>
 */
public class WorkflowConfigAuthorHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch01";

  /** owner group of the workflows, test.subject.0 ADMIN */
  private static final String GROUP_A_NAME = STEM_NAME + ":a";

  /** other group, test.subject.0 UPDATE */
  private static final String GROUP_C_NAME = STEM_NAME + ":c";

  /** non-wheel group admin */
  private static final String AUTHOR = "test.subject.0";

  /** workflow config id of the refused add (never saved) */
  private static final String WHEEL_CONFIG_ID = "secPatch01wheel";

  /** workflow config id of the allowed add */
  private static final String OK_CONFIG_ID = "secPatch01ok";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(WorkflowConfigAuthorHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public WorkflowConfigAuthorHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public WorkflowConfigAuthorHttpTest(String name) {
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
   * delete the groups (their workflow config attributes go with them) and the folder
   */
  private static void deleteData() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String groupName : new String[] {GROUP_A_NAME, GROUP_C_NAME}) {
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
   * every attribute value stored on assignments hung off the group's own attribute assignments,
   * i.e. the workflow config values (id, name, approvals json, ...) of every config on the group
   * @param groupId
   * @return the value strings
   */
  private static List<String> workflowValuesOnGroup(String groupId) {
    return new GcDbAccess().sql("select v.value_string from grouper_attribute_assign marker "
        + "join grouper_attribute_assign sub on sub.owner_attribute_assign_id = marker.id "
        + "join grouper_attribute_assign_value v on v.attribute_assign_id = sub.id "
        + "where marker.owner_group_id = ? and v.value_string is not null")
        .addBindVar(groupId).selectList(String.class);
  }

  /**
   * default approvals with the complete state's assignToGroup set to the given group
   * @param ownerGroupId
   * @param assignToGroup group uuid or name
   * @return the approvals json
   */
  private static String approvalsAssigningTo(String ownerGroupId, String assignToGroup) {
    GrouperWorkflowApprovalStates states = GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(
        GrouperWorkflowApprovalStates.getDefaultApprovalStatesString(ownerGroupId));
    boolean found = false;
    for (GrouperWorkflowApprovalState state : states.getStates()) {
      if (state.getActions() != null && state.getActions().size() > 0
          && "assignToGroup".equals(state.getActions().get(0).getActionName())) {
        state.getActions().get(0).setActionArg0(assignToGroup);
        found = true;
      }
    }
    assertTrue("default approvals have an assignToGroup action", found);
    try {
      return workflowObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(states);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * the add/edit form fields, as the JSP sends them
   * @param groupId
   * @param configId
   * @param configName
   * @param approvals
   * @return urlencoded body
   */
  private static String formBody(String groupId, String configId, String configName, String approvals) {
    return "groupId=" + enc(groupId)
        + "&grouperWorkflowConfigType=grouper"
        + "&grouperWorkflowConfigName=" + enc(configName)
        + "&grouperWorkflowConfigId=" + enc(configId)
        + "&grouperWorkflowConfigDescription=" + enc("security advisory H1 http test")
        + "&grouperWorkflowConfigApprovals=" + enc(approvals)
        + "&grouperWorkflowConfigParams=" + enc(GrouperWorkflowConfigParams.getDefaultConfigParamsString())
        + "&grouperWorkflowConfigForm=" + enc(new GrouperWorkflowConfig().getWorkflowConfigForm())
        + "&grouperWorkflowConfigViewersGroupComboName="
        + "&grouperWorkflowConfigSendEmail=false"
        + "&grouperWorkflowConfigEnabled=true";
  }

  /**
   * @param response
   * @param what
   */
  private static void assertSuccessMessage(SimpleHttpResponse response, String what) {
    assertEquals(what + ": " + describe(response), 200, response.statusCode());
    assertTrue(what + " shows a success message: " + describe(response),
        response.body().contains("\"messageType\":\"success\""));
  }

  /**
   * the form came back with the author-validator error for assignToGroup
   * @param response
   * @param what
   * @param groupRef the reference the error names
   */
  private static void assertAuthorError(SimpleHttpResponse response, String what, String groupRef) {
    assertEquals(what + ": " + describe(response), 200, response.statusCode());
    assertTrue(what + " re-renders the form with errors: " + describe(response),
        response.body().contains("workflowConfigErrors"));
    assertTrue(what + " names the assignToGroup reference: " + describe(response),
        response.body().contains("UPDATE or ADMIN on the group in assignToGroup to use it in a workflow: " + groupRef));
    assertFalse(what + " no success message", response.body().contains("\"messageType\":\"success\""));
  }

  // ---------------------------------------------------------------- test

  /**
   * add with assignToGroup = wheel refused (nothing saved), add with a group the author can UPDATE
   * saves, unchanged edit saves, edit to the wheel group refused (stored approvals unchanged)
   * @throws Exception
   */
  public void testAssignToGroupNeedsAuthorUpdate() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    String wheelGroupName = GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.group");
    deleteData();
    enableWorkflow();
    try {
      String groupAId;
      String groupCId;
      String wheelGroupId;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        Group groupA = new GroupSave(rootSession).assignName(GROUP_A_NAME).assignCreateParentStemsIfNotExist(true).save();
        Group groupC = new GroupSave(rootSession).assignName(GROUP_C_NAME).assignCreateParentStemsIfNotExist(true).save();
        groupA.grantPriv(SubjectFinder.findById(AUTHOR, true), AccessPrivilege.ADMIN, false);
        groupC.grantPriv(SubjectFinder.findById(AUTHOR, true), AccessPrivilege.UPDATE, false);
        groupAId = groupA.getId();
        groupCId = groupC.getId();
        Group wheelGroup = GroupFinder.findByName(rootSession, wheelGroupName, true);
        wheelGroupId = wheelGroup.getId();
        assertFalse("the author must not be wheel", wheelGroup.hasMember(SubjectFinder.findById(AUTHOR, true)));
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }

      UiSession author = new UiSession(baseUrl);
      author.login(AUTHOR, "pass");

      // the running UI sees workflow.enable through the database config cache: wait for the list screen
      SimpleHttpResponse forms = null;
      for (int i = 0; i < 20; i++) {
        forms = author.postAjax("/grouperUi/app/UiV2GrouperWorkflow.viewForms", "groupId=" + enc(groupAId));
        if (forms.body().contains("grouperMainContentDivId")) {
          break;
        }
        Thread.sleep(3000);
      }
      assertTrue("workflow list opens for the group admin (workflow.enable seen): " + describe(forms),
          forms.body().contains("grouperMainContentDivId"));

      String addPath = "/grouperUi/app/UiV2GrouperWorkflow.workflowConfigAddSubmit?groupId=" + enc(groupAId);
      String editPath = "/grouperUi/app/UiV2GrouperWorkflow.workflowConfigEditSubmit?groupId=" + enc(groupAId);

      // step 1 (attack): complete state assigns to the wheel group, by name
      SimpleHttpResponse wheelAdd = author.postAjax(addPath, formBody(groupAId, WHEEL_CONFIG_ID, "secPatch01 wheel",
          approvalsAssigningTo(groupAId, wheelGroupName)));
      assertAuthorError(wheelAdd, "add assigning to the wheel group", wheelGroupName);
      assertFalse("refused config not saved", workflowValuesOnGroup(groupAId).contains(WHEEL_CONFIG_ID));
      assertEquals("nothing saved on the group at all", 0, workflowValuesOnGroup(groupAId).size());

      // step 2 (control): assigns to group c, where the author has UPDATE
      String okApprovals = approvalsAssigningTo(groupAId, groupCId);
      SimpleHttpResponse okAdd = author.postAjax(addPath, formBody(groupAId, OK_CONFIG_ID, "secPatch01 ok", okApprovals));
      assertSuccessMessage(okAdd, "add assigning to group c");
      List<String> afterAdd = workflowValuesOnGroup(groupAId);
      assertTrue("config saved: " + afterAdd, afterAdd.contains(OK_CONFIG_ID));
      boolean approvalsWithC = false;
      for (String value : afterAdd) {
        approvalsWithC = approvalsWithC || value.contains(groupCId);
      }
      assertTrue("saved approvals assign to group c: " + afterAdd, approvalsWithC);

      // step 3 (control): edit, nothing changed
      SimpleHttpResponse sameEdit = author.postAjax(editPath, formBody(groupAId, OK_CONFIG_ID, "secPatch01 ok", okApprovals));
      assertSuccessMessage(sameEdit, "unchanged edit");

      // attack on edit: switch the stored assignToGroup to the wheel group, by uuid
      SimpleHttpResponse wheelEdit = author.postAjax(editPath, formBody(groupAId, OK_CONFIG_ID, "secPatch01 ok",
          approvalsAssigningTo(groupAId, wheelGroupId)));
      assertAuthorError(wheelEdit, "edit assigning to the wheel group", wheelGroupId);
      List<String> afterEdit = workflowValuesOnGroup(groupAId);
      for (String value : afterEdit) {
        assertFalse("stored config does not reference the wheel group: " + value,
            value.contains(wheelGroupId) || value.contains(wheelGroupName));
      }
      assertTrue("config still stored: " + afterEdit, afterEdit.contains(OK_CONFIG_ID));
      approvalsWithC = false;
      for (String value : afterEdit) {
        approvalsWithC = approvalsWithC || value.contains(groupCId);
      }
      assertTrue("stored approvals still assign to group c: " + afterEdit, approvalsWithC);

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

  /**
   * the ObjectMapper the workflow code of this release writes its JSON with: GrouperUtil.objectMapper,
   * or GrouperWorkflowSettings.objectMapper in releases where GrouperUtil has none (e.g. 2.6.19).
   * Looked up by reflection so this one file compiles on every release
   * @return the mapper
   */
  private static com.fasterxml.jackson.databind.ObjectMapper workflowObjectMapper() {
    for (String className : new String[] {"edu.internet2.middleware.grouper.util.GrouperUtil",
        "edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowSettings"}) {
      try {
        return (com.fasterxml.jackson.databind.ObjectMapper)Class.forName(className).getField("objectMapper").get(null);
      } catch (Exception e) {
        // not in this release, try the next
      }
    }
    throw new RuntimeException("no workflow ObjectMapper in this release");
  }

}
