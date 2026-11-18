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
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory M1 (provisioningSyncRowObjectBindingAuthorization), HTTP check against the
 * running local UI: the three provisioning detail screens must show only the sync row of the
 * group, subject and target the handler authorized, not any row whose id is put in the request.
 * UiV2ProvisioningSyncRowBindingTest calls the handlers in memory without rendering a JSP; this
 * test covers what it cannot: the real requests and the rendered detail JSPs (an unbound row renders
 * as an empty detail, not an error).  The WARN log line goes to the Eclipse console only and is not
 * checked here.
 *
 * <p>Attack: test.subject.0 (not wheel; a viewer of provisioner secPatch89 through
 * groupAllowedToView, READ on secPatch89:cover) opens the membership details of
 * cover/test.subject.1 and the subject details of test.subject.1 with only the sync row id swapped
 * for another subject's row, another (protected) group's row, another target's row or an unknown
 * id.  Each page must render with none of those rows' sentinel values.  Positive control: the same
 * user with the matching ids sees the row's sentinel.  The screens only read; the sync rows are
 * checked unchanged with SQL at the end.</p>
 *
 * <p>Setup (this test's own, removed at the end): provisioner config secPatch89 (an SQL provisioner
 * that is never run, plus groupAllowedToView), folder secPatch89 with groups viewers, cover and
 * protected, and grouper_sync rows for targets secPatch89 and secPatch89b (rows only), written with
 * the GcGrouperSync API.</p>
 *
 * <p>Runs only with junit.test.tomcat=true.  Extends TestCase (not GrouperTest, which resets
 * the database the running UI uses).  Plain HTTP requests, dev form login, OWASP CSRF token
 * from OwaspJavaScriptServlet.</p>
 */
public class UiV2ProvisioningSyncRowBindingHttpTest extends TestCase {

  /** provisioner config id (and sync provisioner name) the user may view */
  private static final String TARGET_A = "secPatch89";

  /** second target, sync rows only */
  private static final String TARGET_B = "secPatch89b";

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "secPatch89";

  /** groupAllowedToView of target A */
  private static final String VIEWERS = STEM_NAME + ":viewers";

  /** group the user can read */
  private static final String COVER = STEM_NAME + ":cover";

  /** group the user has no privilege on */
  private static final String PROTECTED = STEM_NAME + ":protected";

  /** the viewer, not wheel */
  private static final String USER = "test.subject.0";

  /** member whose rows the user may see */
  private static final String SUBJ1 = "test.subject.1";

  /** other member */
  private static final String SUBJ2 = "test.subject.2";

  /** provisioner keys this test stores (never run; the table does not exist) */
  private static final String[][] KEYS = {
      {"class", "edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner"},
      {"dbExternalSystemConfigId", "grouper"},
      {"operateOnGrouperGroups", "true"},
      {"groupTableName", "secpatch89_groups"},
      {"groupTableIdColumn", "id"},
      {"hasTargetGroupLink", "false"},
      {"numberOfGroupAttributes", "1"},
      {"targetGroupAttribute.0.name", "id"},
      {"targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField"},
      {"targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "id"},
      {"groupMatchingAttributeCount", "1"},
      {"groupMatchingAttribute0name", "id"},
      {"addDisabledFullSyncDaemon", "false"},
      {"addDisabledIncrementalSyncDaemon", "false"},
      {"groupAllowedToView", VIEWERS},
  };

  /** sentinels: membership_id of each sync membership row, entity cache 0 of each sync member row */
  private static final String ALLOWED = "SECPATCH89ALLOWED";
  /** */
  private static final String OTHER_SUBJECT = "SECPATCH89OTHERSUBJECT";
  /** */
  private static final String OTHER_GROUP = "SECPATCH89OTHERGROUP";
  /** */
  private static final String OTHER_TARGET = "SECPATCH89OTHERTARGET";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2ProvisioningSyncRowBindingHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public UiV2ProvisioningSyncRowBindingHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2ProvisioningSyncRowBindingHttpTest(String name) {
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

  // ---------------------------------------------------------------- setup helpers

  /**
   * create the sync group, sync member (entity cache 0 = sentinel, if new) and sync membership
   * (membership id = sentinel)
   * @param rootSession
   * @param gcGrouperSync
   * @param group
   * @param subject
   * @param sentinel
   * @return {sync member id, sync membership id}
   */
  private static String[] syncRow(GrouperSession rootSession, GcGrouperSync gcGrouperSync, Group group,
      Subject subject, String sentinel) {
    Member member = MemberFinder.findBySubject(rootSession, subject, true);
    GcGrouperSyncGroup syncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId(group.getId());
    syncGroup.setGroupName(group.getName());
    GcGrouperSyncMember syncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberId(member.getId());
    syncMember.setSubjectId(subject.getId());
    syncMember.setSourceId(subject.getSourceId());
    if (syncMember.getEntityAttributeValueCache0() == null) {
      syncMember.setEntityAttributeValueCache0(sentinel);
    }
    gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();
    GcGrouperSyncMembership syncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao()
        .membershipRetrieveOrCreateBySyncGroupIdAndSyncMemberId(syncGroup.getId(), syncMember.getId());
    syncMembership.setMembershipId(sentinel);
    gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();
    return new String[] {syncMember.getId(), syncMembership.getId()};
  }

  /**
   * delete the sync rows, groups, folder and config keys this test created
   */
  private static void cleanUp89() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String target : new String[] {TARGET_A, TARGET_B}) {
        try {
          GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, target);
          if (gcGrouperSync != null) {
            gcGrouperSync.getGcGrouperSyncMembershipDao().membershipDeleteAll(true);
            gcGrouperSync.getGcGrouperSyncGroupDao().groupDeleteAll(true, true);
            gcGrouperSync.getGcGrouperSyncMemberDao().memberDeleteAll(true, true);
            gcGrouperSync.getGcGrouperSyncJobDao().jobDeleteAll(true);
            gcGrouperSync.getGcGrouperSyncLogDao().logDeleteAll();
            gcGrouperSync.getGcGrouperSyncDao().delete();
          }
        } catch (RuntimeException re) {
          // keep cleaning up the rest
          re.printStackTrace();
        }
      }
      for (String groupName : new String[] {COVER, PROTECTED, VIEWERS}) {
        Group group = GroupFinder.findByName(rootSession, groupName, false);
        if (group != null) {
          group.delete();
        }
      }
      Stem stem = StemFinder.findByName(rootSession, STEM_NAME, false);
      if (stem != null) {
        stem.delete();
      }
      for (String[] key : KEYS) {
        try {
          new GrouperDbConfig().configFileName("grouper-loader.properties")
              .propertyName("provisioner." + TARGET_A + "." + key[0]).delete();
        } catch (RuntimeException re) {
          // already gone; keep cleaning up the rest
        }
      }
      // the UI caches the provisioner list for 5 minutes and only its own save clears it; this
      // clears it here and notifies the running UI (cache database), which drops it in seconds
      edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.clearTargetsCache();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * @return membership_id and entity cache 0 (column member_from_id2) of every sync row of the two targets, for the unchanged check
   */
  private static List<String> syncRowValues() {
    return new GcDbAccess().sql("select gsm.provisioner_name || '|' || gsms.membership_id || '|' || gsme.member_from_id2 "
        + "from grouper_sync gsm, grouper_sync_membership gsms, grouper_sync_member gsme "
        + "where gsms.grouper_sync_id = gsm.id and gsms.grouper_sync_member_id = gsme.id and gsm.provisioner_name in (?, ?) "
        + "order by 1").addBindVar(TARGET_A).addBindVar(TARGET_B).selectList(String.class);
  }

  /**
   * the page rendered and shows none of the sentinels
   * @param response
   * @param what
   */
  private static void assertRenderedWithoutRows(SimpleHttpResponse response, String what) {
    String body = response.body() == null ? "" : response.body();
    assertTrue(what + ": page renders: " + describe(response), body.contains("grouperMainContentDivId"));
    assertFalse(what + ": no error message: " + describe(response), body.contains("\"messageType\":\"error\""));
    for (String sentinel : new String[] {ALLOWED, OTHER_SUBJECT, OTHER_GROUP, OTHER_TARGET}) {
      assertFalse(what + ": must not show " + sentinel + ": " + describe(response), body.contains(sentinel));
    }
  }

  // ---------------------------------------------------------------- test

  /**
   * the matching row is shown; a swapped row id renders an empty detail
   * @throws Exception
   */
  public void testSwappedSyncRowIdNotShown() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    cleanUp89();
    try {
      String coverId;
      String subj1MemberId;
      String[] allowedRow;
      String[] otherSubjectRow;
      String[] otherGroupRow;
      String[] otherTargetRow;
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        for (String[] key : KEYS) {
          new GrouperDbConfig().configFileName("grouper-loader.properties")
              .propertyName("provisioner." + TARGET_A + "." + key[0]).value(key[1]).store();
        }
        // the UI caches the provisioner list for 5 minutes and only its own save clears it; this
        // clears it here and notifies the running UI (cache database), which drops it in seconds
        edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.clearTargetsCache();
        Subject user = SubjectFinder.findById(USER, true);
        Subject subj1 = SubjectFinder.findById(SUBJ1, true);
        Subject subj2 = SubjectFinder.findById(SUBJ2, true);

        Group viewers = new GroupSave(rootSession).assignName(VIEWERS).assignCreateParentStemsIfNotExist(true).save();
        viewers.addMember(user, false);

        Group cover = new GroupSave(rootSession).assignName(COVER).save();
        cover.grantPriv(user, AccessPrivilege.READ, false);
        cover.addMember(subj1, false);
        cover.addMember(subj2, false);
        coverId = cover.getId();
        // the detail screens expect the group to be marked provisionable to the target
        GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
        attributeValue.setDirectAssignment(true);
        attributeValue.setDoProvision(TARGET_A);
        attributeValue.setTargetName(TARGET_A);
        GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, cover);

        Group protectedGroup = new GroupSave(rootSession).assignName(PROTECTED).save();
        protectedGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
        protectedGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
        protectedGroup.addMember(subj2, false);

        subj1MemberId = MemberFinder.findBySubject(rootSession, subj1, true).getId();

        GcGrouperSync syncA = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, TARGET_A);
        GcGrouperSync syncB = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, TARGET_B);
        allowedRow = syncRow(rootSession, syncA, cover, subj1, ALLOWED);
        otherSubjectRow = syncRow(rootSession, syncA, cover, subj2, OTHER_SUBJECT);
        // same sync member row as otherSubjectRow (one per subject per target)
        otherGroupRow = syncRow(rootSession, syncA, protectedGroup, subj2, OTHER_GROUP);
        otherTargetRow = syncRow(rootSession, syncB, cover, subj1, OTHER_TARGET);
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }
      List<String> rowsBefore = syncRowValues();
      assertEquals("four sync membership rows: " + rowsBefore, 4, rowsBefore.size());

      UiSession user = new UiSession(uiBaseUrl());
      user.login(USER, "pass");

      String base = "provisioningTargetName=" + TARGET_A + "&groupId=" + coverId + "&memberId=" + subj1MemberId;
      String[] membershipEndpoints = {"/grouperUi/app/UiV2Provisioning.viewProvisioningTargetDetailsOnGroupMembership",
          "/grouperUi/app/UiV2Provisioning.viewProvisioningTargetDetailsOnSubjectMembership"};
      String subjectEndpoint = "/grouperUi/app/UiV2Provisioning.viewProvisioningTargetDetailsOnSubject";

      // positive control first, retried until the running UI sees the secPatch89 config (seconds to ~30 s)
      SimpleHttpResponse allowed = null;
      for (int i = 0; i < 12; i++) {
        allowed = user.postAjax(membershipEndpoints[0], base + "&groupSyncMembershipId=" + allowedRow[1]);
        if (allowed.body().contains(ALLOWED)) {
          break;
        }
        Thread.sleep(5000);
      }

      for (String endpoint : membershipEndpoints) {
        allowed = user.postAjax(endpoint, base + "&groupSyncMembershipId=" + allowedRow[1]);
        assertTrue(endpoint + ": matching row shown: " + describe(allowed), allowed.body().contains(ALLOWED));
        String[][] swaps = {{"other subject", otherSubjectRow[1]}, {"other group", otherGroupRow[1]},
            {"other target", otherTargetRow[1]}, {"unknown id", "abc123secPatch89notThere"}};
        for (String[] swap : swaps) {
          SimpleHttpResponse attack = user.postAjax(endpoint, base + "&groupSyncMembershipId=" + swap[1]);
          assertRenderedWithoutRows(attack, endpoint + " " + swap[0]);
        }
      }

      // subject details: sync member row (entity cache 0 sentinel)
      String subjectBase = "provisioningTargetName=" + TARGET_A + "&memberId=" + subj1MemberId;
      SimpleHttpResponse allowedMember = user.postAjax(subjectEndpoint, subjectBase + "&groupSyncMemberId=" + allowedRow[0]);
      assertTrue("subject details: matching row shown: " + describe(allowedMember), allowedMember.body().contains(ALLOWED));
      String[][] memberSwaps = {{"other subject", otherSubjectRow[0]}, {"other target", otherTargetRow[0]},
          {"unknown id", "abc123secPatch89notThere"}};
      for (String[] swap : memberSwaps) {
        SimpleHttpResponse attack = user.postAjax(subjectEndpoint, subjectBase + "&groupSyncMemberId=" + swap[1]);
        assertRenderedWithoutRows(attack, "subject details " + swap[0]);
      }

      // the screens only read
      assertEquals("sync rows unchanged", rowsBefore, syncRowValues());
    } finally {
      cleanUp89();
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
