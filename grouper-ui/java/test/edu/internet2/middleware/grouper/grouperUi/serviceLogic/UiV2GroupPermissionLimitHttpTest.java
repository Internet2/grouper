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
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitUtils;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory M2 (permissionLimitDeleteAuthorization), HTTP check against the running local UI:
 * UiV2GroupPermission limit delete and limit value delete / edit screen / edit submit call
 * PermissionLimitUiAuthorization before acting.  A read-only user (the security report's reproduction)
 * sends each request for a limit on a role it can only read: every one is refused and the limit and
 * its value are unchanged (checked with SQL; before the fix limitDelete deleted first and refused
 * after).  Also the advisory M2 part of the same handlers: a user who may manage limit A sends limit A
 * with limit B's value id; refused, and B's value is neither shown nor deleted.  Positive control:
 * the same manager edits and deletes limit A's value and deletes a limit, and the permission stays.
 * PermissionLimitUiAuthorizationTest covers only the core class; this covers the UI wiring.
 *
 * <p>Setup (this test's own, removed at the end): folder secPatch15 with role protectedRole, perm
 * def protectedPermissionDef and two allowed role permissions; permission A has the
 * limitWeekday9to5 marker limit and a limitEl limit "amount &lt; 50000", permission B a limitEl
 * limit "amount &lt; 12345".  test.subject.0 has READ, GROUP_ATTR_READ on the role and ATTR_READ on
 * the perm def (no update); test.subject.3 also has ADMIN and GROUP_ATTR_UPDATE on the role and
 * ATTR_UPDATE on the perm def.  The built-in limit defs already grant GrouperAll ATTR_READ and
 * ATTR_UPDATE locally, so this test does not touch them.</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which
 * resets the database the running UI uses).  Plain HTTP requests, dev form login, OWASP
 * CSRF token from OwaspJavaScriptServlet.</p>
 */
public class UiV2GroupPermissionLimitHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "secPatch15";

  /** read-only user */
  private static final String READER = "test.subject.0";

  /** user allowed to manage the limits */
  private static final String MANAGER = "test.subject.3";

  /** value of limit A */
  private static final String VALUE_A = "amount < 50000";

  /** value of limit B */
  private static final String VALUE_B = "amount < 12345";

  /**
   * simplePermissionUpdate.errorCantManageRole: the read-only user lacks GROUP_ATTR_UPDATE on the role
   * derived from the limit (the limit defs already allow GrouperAll ATTR_UPDATE locally)
   */
  private static final String CANT_MANAGE_ROLE = "Error: you are not allowed to edit permissions for this role";

  /** shown when the value is not bound to the limit (grouper.textNg.en.us.base.properties) */
  private static final String LIMIT_NOT_ALLOWED = "You are not allowed to edit the limit";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2GroupPermissionLimitHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public UiV2GroupPermissionLimitHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2GroupPermissionLimitHttpTest(String name) {
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
   * an error message, no success message, and none of the limit values in the response
   * @param response
   * @param what
   * @param expectedText text the error must contain, or null for any error
   */
  private static void assertRefused(SimpleHttpResponse response, String what, String expectedText) {
    String body = response.body();
    assertTrue(what + " must show an error: " + describe(response), body.contains("\"messageType\":\"error\""));
    if (expectedText != null) {
      assertTrue(what + " must show '" + expectedText + "': " + describe(response), body.contains(expectedText));
    }
    assertFalse(what + " must not show a success message: " + describe(response), body.contains("\"messageType\":\"success\""));
    assertFalse(what + " must not disclose a limit value: " + describe(response), body.contains("50000") || body.contains("12345"));
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
   * @param attributeAssignId
   * @return true if the assignment row is in the database
   */
  private static boolean assignmentInDb(String attributeAssignId) {
    return new GcDbAccess().sql("select count(*) from grouper_attribute_assign where id = ?")
        .addBindVar(attributeAssignId).select(int.class) > 0;
  }

  /**
   * @param attributeAssignId
   * @return {id, value_string} of the assignment's values (the UI JVM wrote them; the test JVM caches lie)
   */
  private static List<Object[]> valuesInDb(String attributeAssignId) {
    return new GcDbAccess().sql("select id, value_string from grouper_attribute_assign_value where attribute_assign_id = ?")
        .addBindVar(attributeAssignId).selectList(Object[].class);
  }

  /**
   * @param attributeAssignId
   * @return the single value string, or null if no value row
   */
  private static String valueStringInDb(String attributeAssignId) {
    List<Object[]> rows = valuesInDb(attributeAssignId);
    assertTrue("at most one value row for " + attributeAssignId + ": " + rows.size(), rows.size() <= 1);
    return rows.size() == 0 ? null : (String)rows.get(0)[1];
  }

  /**
   * delete the folder and everything in it
   */
  private static void cleanUp15() {
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
   * read-only user refused on every limit action with no change; foreign value id refused;
   * manager edits and deletes a value and deletes a limit
   * @throws Exception
   */
  public void testLimitHandlersAuthorizeBeforeActing() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    cleanUp15();
    // {AttributeDef, Subject, Privilege} this test granted on the built-in limit defs, revoked in finally
    List<Object[]> limitDefGrants = new java.util.ArrayList<Object[]>();
    try {
      String roleId;
      String permissionAId;
      String weekdayLimitId;
      String elLimitAId;
      String elValueAId;
      String elLimitBId;
      String elValueBId;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        Subject reader = SubjectFinder.findById(READER, true);
        Subject manager = SubjectFinder.findById(MANAGER, true);

        Group role = new GroupSave(rootSession).assignName(STEM_NAME + ":protectedRole")
            .assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
        AttributeDef permissionDef = new AttributeDefSave(rootSession).assignName(STEM_NAME + ":protectedPermissionDef")
            .assignAttributeDefType(AttributeDefType.perm).assignToGroup(true).assignToEffMembership(true).save();
        AttributeDefName actionA = new AttributeDefNameSave(rootSession, permissionDef).assignName(STEM_NAME + ":protectedAction").save();
        AttributeDefName actionB = new AttributeDefNameSave(rootSession, permissionDef).assignName(STEM_NAME + ":otherAction").save();

        AttributeAssign permissionA = role.getPermissionRoleDelegate()
            .assignRolePermission(actionA, PermissionAllowed.ALLOWED).getAttributeAssign();
        AttributeAssign permissionB = role.getPermissionRoleDelegate()
            .assignRolePermission(actionB, PermissionAllowed.ALLOWED).getAttributeAssign();

        AttributeDefName limitWeekday = PermissionLimitUtils.limitWeekday9to5AttributeDefName();
        AttributeDefName limitEl = PermissionLimitUtils.limitElAttributeDefName();
        AttributeAssign weekdayLimit = permissionA.getAttributeDelegate().assignAttribute(limitWeekday).getAttributeAssign();
        permissionA.getAttributeValueDelegate().assignValue(limitEl.getName(), VALUE_A);
        permissionB.getAttributeValueDelegate().assignValue(limitEl.getName(), VALUE_B);
        AttributeAssign elLimitA = permissionA.getAttributeDelegate().retrieveAssignments(limitEl).iterator().next();
        AttributeAssign elLimitB = permissionB.getAttributeDelegate().retrieveAssignments(limitEl).iterator().next();

        // read only: enough to see the limits (the security report's reproduction)
        for (Subject subject : new Subject[] {reader, manager}) {
          role.grantPriv(subject, AccessPrivilege.VIEW, false);
          role.grantPriv(subject, AccessPrivilege.READ, false);
          role.grantPriv(subject, AccessPrivilege.GROUP_ATTR_READ, false);
          permissionDef.getPrivilegeDelegate().grantPriv(subject, AttributeDefPrivilege.ATTR_READ, false);
        }
        // the manager: may edit permissions of this role and assign this perm def
        role.grantPriv(manager, AccessPrivilege.ADMIN, false);
        role.grantPriv(manager, AccessPrivilege.GROUP_ATTR_UPDATE, false);
        permissionDef.getPrivilegeDelegate().grantPriv(manager, AttributeDefPrivilege.ATTR_UPDATE, false);

        // Both users need ATTR_READ/ATTR_UPDATE on the built-in limit defs: PermissionLimitUiAuthorization
        // checks the limit def before the role, so without it the reader gets errorCantEditAttributeDef
        // instead of errorCantManageRole and the manager's positive control fails.  The built-in defs
        // grant GrouperAll only when grouper.permissions.limits.builtin.createAs.public was true when
        // they were created (true by default in 2.6.19, false from 4.x), so it depends on the database.
        // Grant only what is missing and revoke exactly that in finally.
        java.util.Map<String, AttributeDef> limitDefs = new java.util.LinkedHashMap<String, AttributeDef>();
        for (AttributeDefName limitName : new AttributeDefName[] {limitWeekday, limitEl}) {
          limitDefs.put(limitName.getAttributeDefId(), limitName.getAttributeDef());
        }
        for (AttributeDef limitDef : limitDefs.values()) {
          for (Subject subject : new Subject[] {reader, manager}) {
            if (!limitDef.getPrivilegeDelegate().hasAttrRead(subject)
                && limitDef.getPrivilegeDelegate().grantPriv(subject, AttributeDefPrivilege.ATTR_READ, false)) {
              limitDefGrants.add(new Object[] {limitDef, subject, AttributeDefPrivilege.ATTR_READ});
            }
            if (!limitDef.getPrivilegeDelegate().hasAttrUpdate(subject)
                && limitDef.getPrivilegeDelegate().grantPriv(subject, AttributeDefPrivilege.ATTR_UPDATE, false)) {
              limitDefGrants.add(new Object[] {limitDef, subject, AttributeDefPrivilege.ATTR_UPDATE});
            }
          }
        }

        roleId = role.getId();
        permissionAId = permissionA.getId();
        weekdayLimitId = weekdayLimit.getId();
        elLimitAId = elLimitA.getId();
        elLimitBId = elLimitB.getId();
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }
      elValueAId = (String)valuesInDb(elLimitAId).get(0)[0];
      elValueBId = (String)valuesInDb(elLimitBId).get(0)[0];
      assertEquals(VALUE_A, valueStringInDb(elLimitAId));
      assertEquals(VALUE_B, valueStringInDb(elLimitBId));
      String group = "groupId=" + enc(roleId);

      // ---- step 1: the read-only user, every limit action refused, nothing changes
      UiSession readerSession = new UiSession(baseUrl);
      readerSession.login(READER, "pass");

      SimpleHttpResponse readerDeleteLimit = readerSession.postAjax("UiV2GroupPermission.limitMenu",
          group + "&menuItemId=deleteLimit&menuIdOfMenuTarget=" + enc("limitMenuButton_" + weekdayLimitId));
      assertRefused(readerDeleteLimit, "read-only Delete limit", CANT_MANAGE_ROLE);
      assertTrue("limit still there after the read-only Delete limit", assignmentInDb(weekdayLimitId));

      SimpleHttpResponse readerDeleteValue = readerSession.postAjax("UiV2GroupPermission.limitValueMenu",
          group + "&menuItemId=deleteLimitValue&menuIdOfMenuTarget=" + enc("limitAssignValueButton_" + elLimitAId + "_" + elValueAId));
      assertRefused(readerDeleteValue, "read-only delete limit value", CANT_MANAGE_ROLE);

      SimpleHttpResponse readerEditValue = readerSession.postAjax("UiV2GroupPermission.limitValueMenu",
          group + "&menuItemId=editLimitValue&menuIdOfMenuTarget=" + enc("limitAssignValueButton_" + elLimitAId + "_" + elValueAId));
      assertRefused(readerEditValue, "read-only edit limit value screen", CANT_MANAGE_ROLE);

      SimpleHttpResponse readerEditSubmit = readerSession.postAjax("UiV2GroupPermission.limitValueEditSubmit",
          group + "&limitAssignId=" + enc(elLimitAId) + "&limitAssignValueId=" + enc(elValueAId) + "&valueToEdit=" + enc("amount < 1"));
      assertRefused(readerEditSubmit, "read-only limit value edit submit", CANT_MANAGE_ROLE);

      assertTrue("weekday limit unchanged", assignmentInDb(weekdayLimitId));
      assertTrue("el limit A unchanged", assignmentInDb(elLimitAId));
      assertEquals("el limit A value unchanged", VALUE_A, valueStringInDb(elLimitAId));

      // ---- advisory M2 part: the manager may manage limit A, but sends limit B's value id with it
      UiSession managerSession = new UiSession(baseUrl);
      managerSession.login(MANAGER, "pass");

      SimpleHttpResponse foreignEdit = managerSession.postAjax("UiV2GroupPermission.limitValueMenu",
          group + "&menuItemId=editLimitValue&menuIdOfMenuTarget=" + enc("limitAssignValueButton_" + elLimitAId + "_" + elValueBId));
      assertRefused(foreignEdit, "edit screen, limit A with limit B's value id", LIMIT_NOT_ALLOWED);

      SimpleHttpResponse foreignDelete = managerSession.postAjax("UiV2GroupPermission.limitValueMenu",
          group + "&menuItemId=deleteLimitValue&menuIdOfMenuTarget=" + enc("limitAssignValueButton_" + elLimitAId + "_" + elValueBId));
      assertRefused(foreignDelete, "delete, limit A with limit B's value id", LIMIT_NOT_ALLOWED);
      assertEquals("limit B value unchanged", VALUE_B, valueStringInDb(elLimitBId));
      assertEquals("limit A value unchanged", VALUE_A, valueStringInDb(elLimitAId));

      // ---- step 2, positive control: the manager edits and deletes A's value and deletes a limit
      SimpleHttpResponse managerEditScreen = managerSession.postAjax("UiV2GroupPermission.limitValueMenu",
          group + "&menuItemId=editLimitValue&menuIdOfMenuTarget=" + enc("limitAssignValueButton_" + elLimitAId + "_" + elValueAId));
      assertTrue("manager edit screen shows limit A's value: " + describe(managerEditScreen),
          managerEditScreen.statusCode() == 200 && managerEditScreen.body().contains("50000")
          && !managerEditScreen.body().contains("\"messageType\":\"error\""));

      SimpleHttpResponse managerEditSubmit = managerSession.postAjax("UiV2GroupPermission.limitValueEditSubmit",
          group + "&limitAssignId=" + enc(elLimitAId) + "&limitAssignValueId=" + enc(elValueAId) + "&valueToEdit=" + enc("amount < 60000"));
      assertSuccess(managerEditSubmit, "manager limit value edit");
      assertEquals("limit A value edited", "amount < 60000", valueStringInDb(elLimitAId));

      String elValueANow = (String)valuesInDb(elLimitAId).get(0)[0];
      SimpleHttpResponse managerDeleteValue = managerSession.postAjax("UiV2GroupPermission.limitValueMenu",
          group + "&menuItemId=deleteLimitValue&menuIdOfMenuTarget=" + enc("limitAssignValueButton_" + elLimitAId + "_" + elValueANow));
      assertSuccess(managerDeleteValue, "manager limit value delete");
      assertNull("limit A value deleted", valueStringInDb(elLimitAId));

      SimpleHttpResponse managerDeleteLimit = managerSession.postAjax("UiV2GroupPermission.limitMenu",
          group + "&menuItemId=deleteLimit&menuIdOfMenuTarget=" + enc("limitMenuButton_" + weekdayLimitId));
      assertSuccess(managerDeleteLimit, "manager Delete limit");
      assertFalse("weekday limit deleted", assignmentInDb(weekdayLimitId));
      assertTrue("the permission itself stays", assignmentInDb(permissionAId));
      assertEquals("limit B value still unchanged", VALUE_B, valueStringInDb(elLimitBId));

      System.out.println("secPatch15: read-only limit delete / value delete / value edit refused, foreign value id refused, manager edit+delete ok");
    } finally {
      cleanUp15();
      // undo only the limit def grants this test added
      if (limitDefGrants.size() > 0) {
        GrouperSession rootSession = GrouperSession.startRootSession();
        try {
          for (Object[] grant : limitDefGrants) {
            ((AttributeDef)grant[0]).getPrivilegeDelegate().revokePriv((Subject)grant[1],
                (edu.internet2.middleware.grouper.privs.Privilege)grant[2], false);
          }
        } finally {
          GrouperSession.stopQuietly(rootSession);
        }
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
