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
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory M2 (attributeValueIdor), HTTP check against the running local UI: the group
 * attribute value handlers (UiV2GroupAttributeAssignment edit screen, edit submit, delete) only act
 * on a value that belongs to the assignment they authorized.  A user who may update their own
 * assignment sends their assignment id with another assignment's value id; the UI shows
 * simpleAttributeAssign.assignEditNotAllowed, never renders the foreign value, and the foreign value
 * row is unchanged (checked with SQL).  Positive control: the same user edits and deletes their own
 * value through the same handlers.  AttributeAssignValueBindingTest covers only the core binding and
 * delegate; this covers the UI wiring.  The permission limit value handlers (the other two
 * controllers of this fix) are driven by UiV2GroupPermissionLimitHttpTest (advisory M2).
 *
 * <p>Setup (this test's own, removed at the end): folder secPatch04 with groups attackerGroup and
 * protectedGroup, single valued string attributes attackerAttribute=attacker-original and
 * protectedAttribute=protected-original; test.subject.0 (not wheel locally) has update on the
 * attacker side and read only on the protected side.</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which
 * resets the database the running UI uses).  Plain HTTP requests, dev form login, OWASP
 * CSRF token from OwaspJavaScriptServlet.</p>
 */
public class UiV2AttributeValueBindingHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "secPatch04";

  /** the caller */
  private static final String ATTACKER = "test.subject.0";

  /** shown when the value is not bound to the authorized assignment (grouper.textNg.en.us.base.properties) */
  private static final String NOT_ALLOWED = "You are not allowed to edit the attribute assignment";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2AttributeValueBindingHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public UiV2AttributeValueBindingHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2AttributeValueBindingHttpTest(String name) {
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

      SimpleHttpResponse csrfJs = send(SimpleHttpRequest.newBuilder(URI.create(this.baseUrl + "/grouperExternal/public/OwaspJavaScriptServlet")).GET());
      Matcher matcher = Pattern.compile("masterTokenValue = '([^']+)'").matcher(csrfJs.body());
      assertTrue("CSRF token for " + username, matcher.find());
      this.csrfToken = matcher.group(1);
    }

    /**
     * ajax POST with the CSRF header and a form body, like the UI's own requests
     * @param operation e.g. UiV2GroupAttributeAssignment.assignValueEditSubmit
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
   * @param what
   */
  private static void assertRefused(SimpleHttpResponse response, String what) {
    assertTrue(what + " must show the not-allowed error: " + describe(response),
        response.body().contains("\"messageType\":\"error\"") && response.body().contains(NOT_ALLOWED));
    assertFalse(what + " must not show a success message: " + describe(response),
        response.body().contains("\"messageType\":\"success\""));
    assertFalse(what + " must not disclose the protected value: " + describe(response),
        response.body().contains("protected-original"));
  }

  /**
   * @param response
   * @param what
   */
  private static void assertSuccess(SimpleHttpResponse response, String what) {
    assertTrue(what + " shows a success message: " + describe(response),
        response.statusCode() == 200 && response.body().contains("\"messageType\":\"success\""));
  }

  // ---------------------------------------------------------------- db helpers

  /**
   * values of an assignment, read from the database (the UI JVM wrote them; the test JVM caches lie)
   * @param attributeAssignId
   * @return {id, value_string}, may be empty
   */
  private static List<Object[]> valuesInDb(String attributeAssignId) {
    return new GcDbAccess().sql("select id, value_string from grouper_attribute_assign_value where attribute_assign_id = ?")
        .addBindVar(attributeAssignId).selectList(Object[].class);
  }

  /**
   * the single value string of an assignment, or null if it has no value row
   * @param attributeAssignId
   * @return the value
   */
  private static String valueStringInDb(String attributeAssignId) {
    List<Object[]> rows = valuesInDb(attributeAssignId);
    assertTrue("at most one value row for " + attributeAssignId + ": " + rows.size(), rows.size() <= 1);
    return rows.size() == 0 ? null : (String)rows.get(0)[1];
  }

  /**
   * delete the folder and everything in it
   */
  private static void cleanUp04() {
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

  // ---------------------------------------------------------------- test

  /**
   * foreign value id refused on the edit screen, edit submit and delete; own value edited and deleted
   * @throws Exception
   */
  public void testForeignValueIdRefusedInUi() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    cleanUp04();
    try {
      String attackerGroupId;
      String attackerAssignId;
      String attackerValueId;
      String protectedAssignId;
      String protectedValueId;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        Subject attacker = SubjectFinder.findById(ATTACKER, true);

        Group attackerGroup = new GroupSave(rootSession).assignName(STEM_NAME + ":attackerGroup")
            .assignCreateParentStemsIfNotExist(true).save();
        Group protectedGroup = new GroupSave(rootSession).assignName(STEM_NAME + ":protectedGroup")
            .assignCreateParentStemsIfNotExist(true).save();

        AttributeDef attackerDef = new AttributeDefSave(rootSession).assignName(STEM_NAME + ":attackerDef")
            .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true)
            .assignValueType(AttributeDefValueType.string).save();
        AttributeDefName attackerName = new AttributeDefNameSave(rootSession, attackerDef)
            .assignName(STEM_NAME + ":attackerAttribute").save();
        AttributeDef protectedDef = new AttributeDefSave(rootSession).assignName(STEM_NAME + ":protectedDef")
            .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true)
            .assignValueType(AttributeDefValueType.string).save();
        AttributeDefName protectedName = new AttributeDefNameSave(rootSession, protectedDef)
            .assignName(STEM_NAME + ":protectedAttribute").save();

        AttributeAssign attackerAssign = attackerGroup.getAttributeDelegate().assignAttribute(attackerName).getAttributeAssign();
        attackerValueId = attackerAssign.getValueDelegate().assignValue("attacker-original").getAttributeAssignValue().getId();
        AttributeAssign protectedAssign = protectedGroup.getAttributeDelegate().assignAttribute(protectedName).getAttributeAssign();
        protectedValueId = protectedAssign.getValueDelegate().assignValue("protected-original").getAttributeAssignValue().getId();

        // the caller: update on its own side
        attackerGroup.grantPriv(attacker, AccessPrivilege.VIEW, false);
        attackerGroup.grantPriv(attacker, AccessPrivilege.GROUP_ATTR_READ, false);
        attackerGroup.grantPriv(attacker, AccessPrivilege.GROUP_ATTR_UPDATE, false);
        attackerDef.getPrivilegeDelegate().grantPriv(attacker, AttributeDefPrivilege.ATTR_READ, false);
        attackerDef.getPrivilegeDelegate().grantPriv(attacker, AttributeDefPrivilege.ATTR_UPDATE, false);
        // the caller: read only on the protected side (how it would learn the value id)
        protectedGroup.grantPriv(attacker, AccessPrivilege.VIEW, false);
        protectedGroup.grantPriv(attacker, AccessPrivilege.GROUP_ATTR_READ, false);
        protectedDef.getPrivilegeDelegate().grantPriv(attacker, AttributeDefPrivilege.ATTR_READ, false);

        attackerGroupId = attackerGroup.getId();
        attackerAssignId = attackerAssign.getId();
        protectedAssignId = protectedAssign.getId();
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }
      assertEquals("protected-original", valueStringInDb(protectedAssignId));
      assertEquals("attacker-original", valueStringInDb(attackerAssignId));

      UiSession session = new UiSession(baseUrl);
      session.login(ATTACKER, "pass");

      // precondition: the caller cannot open the protected value through its own (protected) assignment
      SimpleHttpResponse precondition = session.postAjax("UiV2GroupAttributeAssignment.assignmentValueMenu",
          "menuItemId=editValue&menuIdOfMenuTarget=" + enc("assignmentValueButton_" + protectedAssignId + "_" + protectedValueId));
      assertRefused(precondition, "precondition, edit screen of the protected assignment");

      // step 2: edit screen with the caller's assignment id and the protected value id (disclosure)
      SimpleHttpResponse editScreen = session.postAjax("UiV2GroupAttributeAssignment.assignmentValueMenu",
          "menuItemId=editValue&menuIdOfMenuTarget=" + enc("assignmentValueButton_" + attackerAssignId + "_" + protectedValueId));
      assertRefused(editScreen, "edit screen, foreign value id");

      // step 1: edit submit with the protected value id in the hidden field (overwrite)
      SimpleHttpResponse editSubmit = session.postAjax("UiV2GroupAttributeAssignment.assignValueEditSubmit",
          "groupId=" + enc(attackerGroupId) + "&attributeAssignId=" + enc(attackerAssignId)
          + "&attributeAssignValueId=" + enc(protectedValueId) + "&valueToEdit=" + enc("protected-modified-by-secPatch04"));
      assertRefused(editSubmit, "edit submit, foreign value id");
      assertEquals("protected value unchanged after the edit attempt", "protected-original", valueStringInDb(protectedAssignId));
      assertEquals("protected value row kept its id", protectedValueId, (String)valuesInDb(protectedAssignId).get(0)[0]);

      // step 3: delete with the protected value id
      SimpleHttpResponse delete = session.postAjax("UiV2GroupAttributeAssignment.assignmentValueMenu",
          "menuItemId=deleteValue&menuIdOfMenuTarget=" + enc("assignmentValueButton_" + attackerAssignId + "_" + protectedValueId));
      assertRefused(delete, "delete, foreign value id");
      assertEquals("protected value still there after the delete attempt", "protected-original", valueStringInDb(protectedAssignId));
      assertEquals("own value untouched by the attempts", "attacker-original", valueStringInDb(attackerAssignId));

      // step 4, positive control: the caller opens, edits and deletes its own value
      SimpleHttpResponse ownEditScreen = session.postAjax("UiV2GroupAttributeAssignment.assignmentValueMenu",
          "menuItemId=editValue&menuIdOfMenuTarget=" + enc("assignmentValueButton_" + attackerAssignId + "_" + attackerValueId));
      assertTrue("own edit screen renders the own value: " + describe(ownEditScreen),
          ownEditScreen.statusCode() == 200 && ownEditScreen.body().contains("attacker-original")
          && !ownEditScreen.body().contains(NOT_ALLOWED));

      SimpleHttpResponse ownEdit = session.postAjax("UiV2GroupAttributeAssignment.assignValueEditSubmit",
          "groupId=" + enc(attackerGroupId) + "&attributeAssignId=" + enc(attackerAssignId)
          + "&attributeAssignValueId=" + enc(attackerValueId) + "&valueToEdit=" + enc("attacker-edited"));
      assertSuccess(ownEdit, "own value edit");
      assertEquals("own value edited", "attacker-edited", valueStringInDb(attackerAssignId));
      assertEquals("protected value still unchanged", "protected-original", valueStringInDb(protectedAssignId));

      // the edit may re-insert the value row, so delete by the current id
      String ownValueIdNow = (String)valuesInDb(attackerAssignId).get(0)[0];
      SimpleHttpResponse ownDelete = session.postAjax("UiV2GroupAttributeAssignment.assignmentValueMenu",
          "menuItemId=deleteValue&menuIdOfMenuTarget=" + enc("assignmentValueButton_" + attackerAssignId + "_" + ownValueIdNow));
      assertSuccess(ownDelete, "own value delete");
      assertNull("own value deleted", valueStringInDb(attackerAssignId));
      assertEquals("protected value survives everything", "protected-original", valueStringInDb(protectedAssignId));
      System.out.println("secPatch04: 3 foreign value id requests refused, own value edited and deleted");

    } finally {
      cleanUp04();
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
