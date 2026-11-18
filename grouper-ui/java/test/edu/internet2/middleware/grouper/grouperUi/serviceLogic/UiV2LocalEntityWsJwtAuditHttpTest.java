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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * Security advisory H7 (wsJwtKeyCreateDeleteNotAudited), HTTP check against the running local UI: a
 * non-wheel ADMIN of a local entity generates, edits (allowed networks and a shorter expiration),
 * rotates and deletes the entity's WS JWT key through the real UiV2LocalEntity handlers, and each
 * step leaves an audit row naming that admin (not root) with no key material.
 * GrouperPasswordSaveWsJwtAuditTest calls GrouperPasswordSave directly; this test covers what it
 * cannot: the UI handlers (delete now goes through GrouperPasswordSave, the partial edit path),
 * checked against the database rows (grouper_password, grouper_audit_entry) the running UI wrote.
 *
 * <p>Setup (this test's own, removed at the end): folder secPatch59 with local entity
 * secPatch59:svc, test.subject.0 ADMIN on it (test.subject.0 is not wheel locally).</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which
 * resets the database the running UI uses).  Plain HTTP requests, dev form login, OWASP
 * CSRF token from OwaspJavaScriptServlet.</p>
 */
public class UiV2LocalEntityWsJwtAuditHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "secPatch59";

  /** local entity */
  private static final String ENTITY_NAME = STEM_NAME + ":svc";

  /** non-wheel entity admin */
  private static final String ADMIN_USER = "test.subject.0";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2LocalEntityWsJwtAuditHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public UiV2LocalEntityWsJwtAuditHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2LocalEntityWsJwtAuditHttpTest(String name) {
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
     * form login (dev only, grouper.is.ui.formAuthn), or HTTP Basic where the UI answers 401 (2.6.19),
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
    return response.statusCode() + " " + response.headers().firstValue("Location").orElse("")
        + " " + body.substring(0, Math.min(300, body.length()));
  }

  // ---------------------------------------------------------------- db helpers

  /**
   * @param entityMemberId
   * @return {id, allowed_from_cidrs, expires_millis, the_password} of the entity's WS key, or null
   */
  private static Object[] keyRow(String entityMemberId) {
    List<Object[]> rows = new GcDbAccess().sql("select id, allowed_from_cidrs, expires_millis, the_password from grouper_password "
        + "where username = ? and application = 'WS'").addBindVar(entityMemberId).selectList(Object[].class);
    return rows.size() == 0 ? null : rows.get(0);
  }

  /**
   * @param passwordId
   * @return {description, logged_in_member_id, act_as_member_id} of the WS JWT key audit rows, oldest first
   */
  private static List<Object[]> auditRows(String passwordId) {
    return new GcDbAccess().sql("select description, logged_in_member_id, act_as_member_id from grouper_audit_entry "
        + "where description like ? order by created_on, last_updated")
        .addBindVar("% WS JWT key for local entity: %passwordId: " + passwordId + "%").selectList(Object[].class);
  }

  /**
   * @param value
   * @return string or null
   */
  private static String str(Object value) {
    return value == null ? null : value.toString();
  }

  /**
   * delete the key rows, their audit rows, the entity and the folder
   */
  private static void cleanUp59() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group entity = GroupFinder.findByName(rootSession, ENTITY_NAME, false);
      if (entity != null) {
        String entityMemberId = MemberFinder.findBySubject(rootSession, entity.toSubject(), true).getId();
        List<String> passwordIds = new GcDbAccess().sql("select id from grouper_password where username = ? and application = 'WS'")
            .addBindVar(entityMemberId).selectList(String.class);
        for (String passwordId : passwordIds) {
          new GcDbAccess().sql("delete from grouper_password_recently_used where grouper_password_id = ?").addBindVar(passwordId).executeSql();
          new GcDbAccess().sql("delete from grouper_password where id = ?").addBindVar(passwordId).executeSql();
        }
        entity.delete();
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
   * @param response
   * @param what
   */
  private static void assertSuccessMessage(SimpleHttpResponse response, String what) {
    assertEquals(what + ": " + describe(response), 200, response.statusCode());
    assertTrue(what + " shows a success message: " + describe(response),
        response.body().contains("\"messageType\":\"success\""));
  }

  // ---------------------------------------------------------------- test

  /**
   * create, edit, rotate, delete as the non-wheel entity admin; four audit rows by that admin
   * @throws Exception
   */
  public void testWsJwtKeyLifecycleAudited() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    cleanUp59();
    String passwordId = null;
    try {
      String entityId;
      String entitySourceId;
      String entityMemberId;
      String adminMemberId;
      String rootMemberId;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        Group entity = new GroupSave(rootSession).assignName(ENTITY_NAME).assignTypeOfGroup(TypeOfGroup.entity)
            .assignCreateParentStemsIfNotExist(true).save();
        entity.grantPriv(SubjectFinder.findById(ADMIN_USER, true), AccessPrivilege.ADMIN, false);
        entityId = entity.getId();
        // local entities are their own subject source (grouperEntities), not g:gsa
        entitySourceId = entity.toSubject().getSourceId();
        entityMemberId = MemberFinder.findBySubject(rootSession, entity.toSubject(), true).getId();
        adminMemberId = MemberFinder.findBySubject(rootSession, SubjectFinder.findById(ADMIN_USER, true), true).getId();
        rootMemberId = MemberFinder.findBySubject(rootSession, SubjectFinder.findRootSubject(), true).getId();
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }
      String subjectParams = "subjectId=" + enc(entityId) + "&sourceId=" + enc(entitySourceId);

      UiSession admin = new UiSession(baseUrl);
      admin.login(ADMIN_USER, "pass");

      // the WS JWT key screen opens for the entity admin
      SimpleHttpResponse view = admin.postAjax("/grouperUi/app/UiV2LocalEntity.viewLocalEntityWSJwtKeys", subjectParams);
      assertTrue("key screen for the entity admin: " + describe(view), view.statusCode() == 200
          && view.body().contains("viewWsJwtKey"));

      // step 1: generate a key
      String createExpires = LocalDate.now().plusDays(60).toString();
      SimpleHttpResponse create = admin.postAjax("/grouperUi/app/UiV2LocalEntity.createWsJwtKeySubmit", subjectParams
          + "&localEntityAllowedFromCidrs=" + enc("10.0.0.0/8") + "&localEntityExpiresAtDate=" + enc(createExpires));
      assertSuccessMessage(create, "generate key");
      Object[] created = keyRow(entityMemberId);
      assertNotNull("key row after generate", created);
      passwordId = str(created[0]);
      long createdExpiresMillis = Long.parseLong(str(created[2]));
      String createdPublicKey = str(created[3]);

      // step 2: edit, other networks and a shorter expiration
      String editExpires = LocalDate.now().plusDays(10).toString();
      SimpleHttpResponse edit = admin.postAjax("/grouperUi/app/UiV2LocalEntity.editWsJwtKeySubmit", subjectParams
          + "&localEntityAllowedFromCidrs=" + enc("10.1.0.0/16") + "&localEntityExpiresAtDate=" + enc(editExpires));
      assertSuccessMessage(edit, "edit key");
      Object[] edited = keyRow(entityMemberId);
      assertEquals("same key row after edit", passwordId, str(edited[0]));
      assertEquals("allowed networks saved", "10.1.0.0/16", str(edited[1]));
      long editedExpiresMillis = Long.parseLong(str(edited[2]));
      assertTrue("shorter expiration saved: " + createdExpiresMillis + " -> " + editedExpiresMillis,
          editedExpiresMillis < createdExpiresMillis);

      // the edit screen reopens with the saved values
      SimpleHttpResponse reopen = admin.postAjax("/grouperUi/app/UiV2LocalEntity.editWsJwtKey", subjectParams);
      assertTrue("edit screen reopens with the new networks: " + describe(reopen), reopen.body().contains("10.1.0.0/16"));

      // step 3: rotate (new key pair, same row)
      SimpleHttpResponse rotate = admin.postAjax("/grouperUi/app/UiV2LocalEntity.deleteWsJwtKeyCreateNew", subjectParams);
      assertSuccessMessage(rotate, "rotate key");
      Object[] rotated = keyRow(entityMemberId);
      assertEquals("same key row after rotate", passwordId, str(rotated[0]));
      assertFalse("new public key after rotate", createdPublicKey.equals(str(rotated[3])));
      String rotatedPublicKey = str(rotated[3]);

      // step 4: delete
      SimpleHttpResponse delete = admin.postAjax("/grouperUi/app/UiV2LocalEntity.deleteWsJwtKey", subjectParams);
      assertSuccessMessage(delete, "delete key");
      assertNull("key row gone after delete", keyRow(entityMemberId));
      SimpleHttpResponse afterDelete = admin.postAjax("/grouperUi/app/UiV2LocalEntity.viewLocalEntityWSJwtKeys", subjectParams);
      assertFalse("deleted key not listed: " + describe(afterDelete), afterDelete.body().contains(passwordId));

      // audit: Created, Updated, Updated (new public key), Deleted, each by the admin, not root
      List<Object[]> audits = auditRows(passwordId);
      List<String> descriptions = new ArrayList<String>();
      for (Object[] audit : audits) {
        descriptions.add(str(audit[0]));
      }
      assertEquals("four WS JWT key audit rows: " + descriptions, 4, audits.size());
      String[] actions = {"Created ", "Updated ", "Updated (new public key) ", "Deleted "};
      for (int i = 0; i < 4; i++) {
        String description = str(audits.get(i)[0]);
        String loggedIn = str(audits.get(i)[1]);
        String actAs = str(audits.get(i)[2]);
        assertTrue("audit " + i + " is " + actions[i] + ": " + descriptions, description.startsWith(actions[i] + "WS JWT key"));
        assertTrue("audit " + i + " names the entity: " + description, description.contains(ENTITY_NAME));
        assertTrue("audit " + i + " actor is the admin: logged in " + loggedIn + ", act as " + actAs,
            adminMemberId.equals(loggedIn) || adminMemberId.equals(actAs));
        assertFalse("audit " + i + " actor is not root", rootMemberId.equals(loggedIn) || rootMemberId.equals(actAs));
        // no key material: no part of either public key
        for (String publicKey : new String[] {createdPublicKey, rotatedPublicKey}) {
          String keyPart = publicKey.replaceAll("-----[^-]+-----", "").replaceAll("\\s", "");
          keyPart = keyPart.substring(Math.max(0, keyPart.length() / 2 - 20), keyPart.length() / 2 + 20);
          assertFalse("audit " + i + " has no key material", description.contains(keyPart));
        }
      }
      assertTrue("edit audit has the new networks: " + descriptions.get(1), descriptions.get(1).contains("10.1.0.0/16"));

    } finally {
      // the audit rows of this test's key, then the key, entity and folder
      if (passwordId != null) {
        new GcDbAccess().sql("delete from grouper_audit_entry where description like ?")
            .addBindVar("% WS JWT key for local entity: %passwordId: " + passwordId + "%").executeSql();
      }
      cleanUp59();
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
