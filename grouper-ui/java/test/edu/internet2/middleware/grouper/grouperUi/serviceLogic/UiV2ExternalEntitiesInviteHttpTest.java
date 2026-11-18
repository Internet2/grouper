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
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttrFramework;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * Security advisory M7 (externalInviteEmptyGroupAuthorization), HTTP check against the running local
 * UI: UiV2ExternalEntities.inviteSubmit with no group must be refused before the root invite runs,
 * so no invitation is stored.  ExternalInviteSubmitAuthorizationTest tests the check class; this
 * test covers what it cannot: the call in the real UI handler.
 *
 * <p>Attack: test.subject.0 (UPDATE on secPatch74:invite74, not wheel) posts inviteSubmit by email
 * with an empty inviteAddGroupComboName.  It must get the "group not found" error, never "Problem
 * sending invites", and no invitation for that email may exist (SQL).  Positive control: the same
 * user invites another email to secPatch74:invite74, and that invitation is stored (SQL; the mail
 * itself may succeed or fail with the normal error, mail is not set up locally).</p>
 *
 * <p>Setup (this test's own, removed at the end): folder secPatch74, group secPatch74:invite74,
 * the invitations it creates.  Turns on, in DB config, grouper-ui.properties
 * inviteExternalMembers.enableInvitation and inviteExternalPeople.link-from-new-ui and
 * grouper.properties externalSubjects.enabled, and puts back the previous values in finally.</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which resets
 * the database the running UI uses).  Plain HTTP requests, dev form login, OWASP CSRF token
 * from OwaspJavaScriptServlet.</p>
 */
public class UiV2ExternalEntitiesInviteHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "secPatch74";

  /** group the user may invite into */
  private static final String GROUP_NAME = STEM_NAME + ":invite74";

  /** user with UPDATE on the group, not wheel */
  private static final String USER = "test.subject.0";

  /** email of the attack (no group) */
  private static final String ATTACK_EMAIL = "secpatch74attack@example.org";

  /** email of the positive control */
  private static final String OK_EMAIL = "secpatch74ok@example.org";

  /** {config file, key, value} this test turns on */
  private static final String[][] CONFIG = {
      {"grouper-ui.properties", "inviteExternalMembers.enableInvitation", "true"},
      {"grouper-ui.properties", "inviteExternalPeople.link-from-new-ui", "true"},
      {"grouper.properties", "externalSubjects.enabled", "true"},
  };

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2ExternalEntitiesInviteHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public UiV2ExternalEntitiesInviteHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2ExternalEntitiesInviteHttpTest(String name) {
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

  // ---------------------------------------------------------------- db / config helpers

  /**
   * @param email
   * @return ids of the invitation (owner) assignments for this email, read with SQL
   */
  private static List<String> invitationAssignIds(String email) {
    return new GcDbAccess().sql("select distinct gaa.owner_attribute_assign_id from grouper_attribute_assign gaa, "
        + "grouper_attribute_assign_value gaav, grouper_attribute_def_name gadn where gaav.attribute_assign_id = gaa.id "
        + "and gaa.attribute_def_name_id = gadn.id and gadn.name = ? and gaav.value_string = ?")
        .addBindVar(ExternalSubjectAttrFramework.externalSubjectInviteEmailAddressName()).addBindVar(email)
        .selectList(String.class);
  }

  /**
   * @param configFile
   * @param key
   * @return the value stored in the database config, or null if there is none
   */
  private static String dbConfigValue(String configFile, String key) {
    List<String> values = new GcDbAccess().sql("select config_value from grouper_config where config_file_name = ? "
        + "and config_key = ? and config_file_hierarchy = 'INSTITUTION'").addBindVar(configFile).addBindVar(key)
        .selectList(String.class);
    return values.size() == 0 ? null : values.get(0);
  }

  /**
   * store or delete a DB config value
   * @param configFile
   * @param key
   * @param value null to delete
   */
  private static void setDbConfig(String configFile, String key, String value) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      if (value == null) {
        new GrouperDbConfig().configFileName(configFile).propertyName(key).delete();
      } else {
        new GrouperDbConfig().configFileName(configFile).propertyName(key).value(value).store();
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete this test's invitations, the group and the folder
   */
  private static void cleanUp74() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String email : new String[] {ATTACK_EMAIL, OK_EMAIL}) {
        for (String assignId : invitationAssignIds(email)) {
          AttributeAssign attributeAssign = AttributeAssignFinder.findById(assignId, false);
          if (attributeAssign != null) {
            attributeAssign.delete();
          }
        }
      }
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

  /**
   * @param groupComboValue group id, or empty for the attack
   * @param email
   * @return the inviteSubmit form body
   */
  private static String inviteBody(String groupComboValue, String email) {
    return "appState=" + enc("{}") + "&inviteBy=email&emailAddressesToInvite=" + enc(email)
        + "&emailSubject=" + enc("secPatch74 invitation") + "&messageToUsers=" + enc("secPatch74 invitation")
        + "&inviteAddGroupComboName=" + enc(groupComboValue) + "&inviteAddGroupComboNameDisplay=" + enc(groupComboValue);
  }

  // ---------------------------------------------------------------- test

  /**
   * an invite with no group is refused and stores nothing; an invite to the user's group is stored
   * @throws Exception
   */
  public void testInviteWithoutGroupRefused() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    // previous DB config values, to put back
    String[] previous = new String[CONFIG.length];
    for (int i = 0; i < CONFIG.length; i++) {
      previous[i] = dbConfigValue(CONFIG[i][0], CONFIG[i][1]);
    }
    cleanUp74();
    try {
      String groupId;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        Group group = new GroupSave(rootSession).assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
        group.grantPriv(SubjectFinder.findById(USER, true), AccessPrivilege.UPDATE, false);
        groupId = group.getId();
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }
      for (String[] config : CONFIG) {
        setDbConfig(config[0], config[1], config[2]);
      }

      UiSession user = new UiSession(uiBaseUrl());
      user.login(USER, "pass");
      String path = "/grouperUi/app/UiV2ExternalEntities.inviteSubmit";

      // positive control first: retry until the running UI sees the config (it refuses with
      // "not enabled" until then), so the attack below runs with invitations on
      SimpleHttpResponse ok = null;
      for (int i = 0; i < 12; i++) {
        ok = user.postAjax(path, inviteBody(groupId, OK_EMAIL));
        if (invitationAssignIds(OK_EMAIL).size() > 0) {
          break;
        }
        assertTrue("positive control refused only while config loads: " + describe(ok),
            ok.body().contains("not enabled") || ok.body().contains("not found")
            || ok.body().contains("cannot have external users"));
        Thread.sleep(5000);
      }
      assertEquals("invitation stored for the authorized invite: " + describe(ok), 1, invitationAssignIds(OK_EMAIL).size());
      assertTrue("authorized invite: sent, or the normal mail error: " + describe(ok),
          ok.body().contains("Invites have been sent") || ok.body().contains("Problem sending invites"));

      // attack: no group at all
      SimpleHttpResponse attack = user.postAjax(path, inviteBody("", ATTACK_EMAIL));
      assertTrue("attack gets the group not found error: " + describe(attack),
          attack.body().contains("group not found or you do not have the UPDATE privilege"));
      assertFalse("attack never reaches the mail step: " + describe(attack), attack.body().contains("Problem sending invites"));
      assertFalse("attack is not reported as sent: " + describe(attack), attack.body().contains("Invites have been sent"));
      assertEquals("no invitation stored for the attack email", 0, invitationAssignIds(ATTACK_EMAIL).size());
    } finally {
      try {
        cleanUp74();
      } finally {
        for (int i = 0; i < CONFIG.length; i++) {
          setDbConfig(CONFIG[i][0], CONFIG[i][1], previous[i]);
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
