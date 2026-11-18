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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security advisory M3 (groupImportMembershipDatesUnaudited), HTTP check against the running
 * local UI: when a group import has an end date and an imported subject is already a member,
 * UiV2GroupImport now changes the existing membership's dates through Membership.update(), which
 * checks UPDATE as the importing user and writes one updateGroupMembership audit entry naming
 * them.  MembershipDateChangeAuditTest is core API only and leaves group import out; this test
 * drives the real groupImportSubmit handler and checks the rows the running UI wrote.</p>
 *
 * <p>The attack: a user with only READ on a group submits an import into it (the group id put in
 * the group field by hand, and as an extra group id) with an end date for an existing member.
 * Nothing changes: the membership keeps no end date and no audit row is written.  The positive
 * control: the same user with UPDATE on another group imports the same subject with an end date;
 * the membership gets the end date and one audit row with the old and new disabled time, by that
 * user.</p>
 *
 * <p>Setup (this test's own, removed at the end): jdbc subject secPatch58u; folder secPatch58
 * with g1 (test.subject.0 UPDATE+READ) and g2 (test.subject.0 READ only), secPatch58u a member of
 * both.  test.subject.0 is not wheel locally.</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which
 * resets the database the running UI uses).  Plain HTTP requests, dev form login, OWASP
 * CSRF token from OwaspJavaScriptServlet.</p>
 */
public class UiV2GroupImportDateAuditHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "secPatch58";

  /** group the importing user can update */
  private static final String G1_NAME = STEM_NAME + ":g1";

  /** group the importing user can only read */
  private static final String G2_NAME = STEM_NAME + ":g2";

  /** importing user (not wheel locally) */
  private static final String IMPORT_USER = "test.subject.0";

  /** existing member whose dates the import changes (jdbc row this test inserts) */
  private static final String MEMBER = "secPatch58u";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2GroupImportDateAuditHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public UiV2GroupImportDateAuditHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2GroupImportDateAuditHttpTest(String name) {
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
   * the ajax response is json with html inside: undo the json escaping so html can be matched
   * @param body
   * @return body with \" \/ \n and \\uXXXX decoded
   */
  static String unjson(String body) {
    if (body == null) {
      return "";
    }
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < body.length(); i++) {
      char c = body.charAt(i);
      if (c == '\\' && i + 1 < body.length()) {
        char next = body.charAt(i + 1);
        if (next == 'u' && i + 5 < body.length()) {
          result.append((char)Integer.parseInt(body.substring(i + 2, i + 6), 16));
          i += 5;
          continue;
        }
        if (next == 'n') {
          result.append('\n');
        } else if (next == 't') {
          result.append('\t');
        } else if (next == 'r') {
          result.append('\r');
        } else {
          result.append(next);
        }
        i++;
        continue;
      }
      result.append(c);
    }
    return result.toString();
  }

  /**
   * @param response
   * @return status and the start of the (decoded) body, for failure messages
   */
  static String describe(SimpleHttpResponse response) {
    String body = unjson(response.body());
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


  // ---------------------------------------------------------------- db helpers

  /**
   * @param values
   * @return list of the values
   */
  static List<Object> listOf(Object... values) {
    return new ArrayList<Object>(Arrays.asList(values));
  }

  /**
   * @param groupName
   * @return {disabled_timestamp} rows of the member's immediate members-list membership (SQL), as strings (null if none)
   */
  private static List<String> disabledTimestamps(String groupName) {
    return new GcDbAccess().sql("select cast(ms.disabled_timestamp as varchar(30)) from grouper_memberships ms, grouper_fields f, "
        + "grouper_members m, grouper_groups g where ms.field_id = f.id and ms.member_id = m.id and ms.owner_group_id = g.id "
        + "and g.name = ? and f.name = 'members' and m.subject_id = ? and ms.mship_type = 'immediate'")
        .addBindVar(groupName).addBindVar(MEMBER).selectList(String.class);
  }

  /**
   * @param groupName
   * @return {description, logged_in_member_id, act_as_member_id} of updateGroupMembership audit rows for the member
   */
  private static List<Object[]> updateAudits(String groupName) {
    return new GcDbAccess().sql("select e.description, e.logged_in_member_id, e.act_as_member_id from grouper_audit_entry e, "
        + "grouper_audit_type t where e.audit_type_id = t.id and t.action_name = 'updateGroupMembership' and e.description like ?")
        .addBindVar("Updated membership: group: " + groupName + ", subject: jdbc." + MEMBER + ",%").selectList(Object[].class);
  }

  /**
   * insert a jdbc subject like test.subject.N
   * @param subjectId
   */
  private static void insertSubject(String subjectId) {
    HibernateSession.bySqlStatic().executeSql("insert into subject (subjectid, subjecttypeid, name) values (?, 'person', ?)",
        listOf(subjectId, "my name is " + subjectId));
    String[][] attributes = {{"name", "name." + subjectId}, {"loginid", "id." + subjectId},
        {"description", "description." + subjectId}, {"email", subjectId + "@example.edu"}};
    for (String[] attribute : attributes) {
      HibernateSession.bySqlStatic().executeSql("insert into subjectattribute (subjectid, name, value, searchvalue) values (?, ?, ?, ?)",
          listOf(subjectId, attribute[0], attribute[1], attribute[1].toLowerCase()));
    }
  }

  /**
   * delete the groups, folder, this test's audit rows, and the subject
   */
  private static void cleanUp58() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String groupName : new String[] {G1_NAME, G2_NAME}) {
        Group group = GroupFinder.findByName(rootSession, groupName, false);
        if (group != null) {
          group.delete();
        }
      }
      Stem stem = StemFinder.findByName(rootSession, STEM_NAME, false);
      if (stem != null) {
        stem.delete();
      }
      new GcDbAccess().sql("delete from grouper_audit_entry where description like ?")
          .addBindVar("Updated membership: group: " + STEM_NAME + ":%, subject: jdbc." + MEMBER + ",%").executeSql();
      HibernateSession.bySqlStatic().executeSql("delete from subjectattribute where subjectid = ?", listOf(MEMBER));
      HibernateSession.bySqlStatic().executeSql("delete from subject where subjectid = ?", listOf(MEMBER));
      List<String> memberIds = new GcDbAccess().sql("select id from grouper_members where subject_id = ? and subject_source = 'jdbc'")
          .addBindVar(MEMBER).selectList(String.class);
      for (String memberId : memberIds) {
        try {
          HibernateSession.bySqlStatic().executeSql("delete from grouper_members where id = ?", listOf(memberId));
        } catch (RuntimeException re) {
          System.out.println("secPatch58: member row " + memberId + " still referenced, left in place: " + re.getMessage());
        }
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * the import form fields for one subject (by id) with an end date, nothing else changed
   * @param groupFields groupImportGroupComboName=... and/or extraGroupId_N=...
   * @param endDate datetime-local value
   * @return urlencoded body
   */
  private static String importBody(String groupFields, String endDate) {
    return groupFields + "&bulkAddGroupOptions=input&bulkAddOptions=list&entityList=" + enc(MEMBER)
        + "&searchEntitySourceName=all&replaceOrRemoveExistingMembers=none&startDate=&endDate=" + enc(endDate);
  }

  /**
   * date format groupImportSubmit accepts in this release.  4.x and later parse the datetime-local
   * value (GrouperUtil.stringToTimestampTimeRequiredLocalDateTime, "yyyy-MM-dd'T'HH:mm"); 2.6.19
   * has no such method and parses with stringToTimestampTimeRequiredWithoutSeconds, where a 'T'
   * means an ISO time WITH a zone and the plain form is "yyyy/MM/dd HH:mm".  The test JVM runs the
   * same release as the UI, so the method's presence tells which one the UI uses.
   * @return pattern for DateTimeFormatter
   */
  private static String importDatePattern() {
    try {
      edu.internet2.middleware.grouper.util.GrouperUtil.class.getMethod("stringToTimestampTimeRequiredLocalDateTime", String.class);
      return "yyyy-MM-dd'T'HH:mm";
    } catch (NoSuchMethodException nsme) {
      return "yyyy/MM/dd HH:mm";
    }
  }

  // ---------------------------------------------------------------- test

  /**
   * READ-only group refused with no change; UPDATE group gets the end date and one audit row by the importer
   * @throws Exception
   */
  public void testImportDateChangeCheckedAndAudited() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    String baseUrl = uiBaseUrl();
    cleanUp58();
    try {
      String g1Id;
      String g2Id;
      String importerMemberId;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        insertSubject(MEMBER);
        Subject member = SubjectFinder.findByIdAndSource(MEMBER, "jdbc", true);
        Subject importer = SubjectFinder.findById(IMPORT_USER, true);
        importerMemberId = MemberFinder.findBySubject(rootSession, importer, true).getId();
        Group g1 = new GroupSave(rootSession).assignName(G1_NAME).assignCreateParentStemsIfNotExist(true).save();
        g1.grantPriv(importer, AccessPrivilege.UPDATE, false);
        g1.grantPriv(importer, AccessPrivilege.READ, false);
        g1.addMember(member, false);
        Group g2 = new GroupSave(rootSession).assignName(G2_NAME).assignCreateParentStemsIfNotExist(true).save();
        g2.grantPriv(importer, AccessPrivilege.READ, false);
        g2.addMember(member, false);
        g1Id = g1.getId();
        g2Id = g2.getId();
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }
      assertEquals("g1 membership has no end date", Arrays.asList((String)null), disabledTimestamps(G1_NAME));
      assertEquals("g2 membership has no end date", Arrays.asList((String)null), disabledTimestamps(G2_NAME));

      // an end date 30 days out, past the enabled/disabled daemon's interval so no "too soon" warning
      // Whole minutes: before 7.9 the UI parses "yyyy-MM-dd'T'HH:mm" first and SimpleDateFormat ignores trailing
      // text, so submitted seconds were silently dropped (6.5.0, 4.26.0); 2.6.19 has no seconds format at all.
      LocalDateTime end = LocalDateTime.now().plusDays(30).withSecond(0).withNano(0);
      String endDate = end.format(DateTimeFormatter.ofPattern(importDatePattern()));

      UiSession importerSession = new UiSession(baseUrl);
      importerSession.login(IMPORT_USER, "pass");

      // ---- attack 1: the READ-only group's id typed into the group field
      SimpleHttpResponse attack1 = importerSession.postAjax("/grouperUi/app/UiV2GroupImport.groupImportSubmit",
          importBody("groupImportGroupComboName=" + enc(g2Id), endDate));
      String attack1Html = unjson(attack1.body());
      assertTrue("READ-only group refused: " + describe(attack1), attack1Html.contains("\"messageType\":\"error\"")
          && attack1Html.contains("Select a group from the search results"));

      // ---- attack 2: the READ-only group's id as an extra group id (the hidden fields of the multi-group import)
      SimpleHttpResponse attack2 = importerSession.postAjax("/grouperUi/app/UiV2GroupImport.groupImportSubmit",
          importBody("extraGroupId_0=" + enc(g2Id), endDate));
      // a group the user cannot update is not found by the UPDATE-filtered lookup; today that ends in the generic
      // error (the null group is not handled), which fails closed: no import report, nothing changed
      String attack2Html = unjson(attack2.body());
      assertTrue("extra READ-only group refused with an error: " + describe(attack2),
          attack2Html.contains("\"messageType\":\"error\"") && !attack2Html.contains("groupImportReport"));

      // give a background import time to run, then check nothing changed
      Thread.sleep(5000);
      assertEquals("g2 membership still has no end date", Arrays.asList((String)null), disabledTimestamps(G2_NAME));
      assertEquals("no date change audit on g2", 0, updateAudits(G2_NAME).size());

      // ---- positive control: the same import into the group the user can update
      SimpleHttpResponse submit = importerSession.postAjax("/grouperUi/app/UiV2GroupImport.groupImportSubmit",
          importBody("groupImportGroupComboName=" + enc(g1Id), endDate));
      String submitHtml = unjson(submit.body());
      assertTrue("import report shown: " + describe(submit), submitHtml.contains("grouperMainContentDivId")
          && !submitHtml.contains("\"messageType\":\"error\""));

      // the import may finish in a background thread: poll the database
      String disabled = null;
      for (int i = 0; i < 30; i++) {
        disabled = disabledTimestamps(G1_NAME).get(0);
        if (disabled != null) {
          break;
        }
        Thread.sleep(1000);
      }
      assertNotNull("g1 membership got the end date", disabled);
      long expectedMillis = java.sql.Timestamp.valueOf(end).getTime();
      assertEquals("end date saved", expectedMillis, Long.parseLong(disabled));

      List<Object[]> audits = updateAudits(G1_NAME);
      assertEquals("one date change audit on g1", 1, audits.size());
      String description = (String)audits.get(0)[0];
      String loggedIn = (String)audits.get(0)[1];
      String actAs = (String)audits.get(0)[2];
      assertTrue("audit has the old and new disabled time: " + description,
          description.matches(".*disabledTime: null -> " + end.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + " .*"));
      assertTrue("audit names the importer: " + description, description.contains("by: ") && description.contains(IMPORT_USER));
      assertTrue("audit actor is the importer: logged in " + loggedIn + ", act as " + actAs,
          importerMemberId.equals(loggedIn) || importerMemberId.equals(actAs));
      assertEquals("g2 still unchanged", Arrays.asList((String)null), disabledTimestamps(G2_NAME));

    } finally {
      cleanUp58();
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
