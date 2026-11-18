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
package edu.internet2.middleware.grouper.ws.scim;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory M4 (scimMembershipDeleteAuthorization), real-HTTP check of Grouper's own SCIM
 * server (GrouperScimServlet, /grouper-ws/scim/v2/Memberships, registered by
 * CommonServletContainerInitializer when grouper.is.scim=true, authenticated by the WS filter
 * with WS Basic auth).  MembershipWriteAuthorizationTest (JUnit) covers the core API; this test
 * covers what it cannot: the status codes TierMembershipProvider returns in the running WS.
 *
 * <ol>
 * <li>test.subject.0 (READ only) finds test.subject.1's membership id with a filter GET;</li>
 * <li>test.subject.0 DELETE of that id: 403, membership still there (before the fix: 204 and
 * deleted);</li>
 * <li>test.subject.0 GET by id of a membership in a group it cannot see: 404 with no group or
 * subject data in the body (before the fix: 200 with the row);</li>
 * <li>test.subject.0 PUT of new dates on the step 1 id: 403, dates unchanged (before: 500);</li>
 * <li>test.subject.3 (READ and UPDATE) DELETE of the step 1 id: 204, membership gone, and a
 * deleteGroupMembership audit entry logged in as test.subject.3 (before: no audit).</li>
 * </ol>
 *
 * <p>Runs against the running local WS, so it is skipped unless junit.test.tomcat=true.  Extends
 * TestCase, not GrouperTest (GrouperTest resets the database the running WS shares).
 * Creates and deletes folder secPatch11; adds test.subject.0 and test.subject.3 to the WS
 * admission group (ws.client.user.group.name) only if they are not members, and removes exactly
 * those in finally.  Fails at the start with a clear message if the SCIM servlet is not
 * enabled.</p>
 */
public class TierMembershipProviderAuthorizationHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "secPatch11";

  /** group where test.subject.0 has READ only */
  private static final String GROUP_NAME = STEM_NAME + ":scimDeleteAuthTest";

  /** group test.subject.0 cannot see */
  private static final String HIDDEN_GROUP_NAME = STEM_NAME + ":scimHidden";

  /** WS password of the local test subjects */
  private static final String PASS = "pass";

  /** timeout, ms */
  private static final int TIMEOUT_MILLIS = 15000;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(TierMembershipProviderAuthorizationHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public TierMembershipProviderAuthorizationHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public TierMembershipProviderAuthorizationHttpTest(String name) {
    super(name);
  }

  /**
   * status and body of a response
   */
  private static class HttpResult {
    /** status */
    private int status;
    /** body */
    private String body;
  }

  /**
   * the SCIM membership endpoints refuse writes and reads the caller is not allowed
   * @throws Exception
   */
  public void testScimMembershipAuthorization() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    String scimUrl = wsBaseUrl() + "scim/v2/";

    // probe without credentials: an authenticated probe would make the WS cache "not in the
    // admission group" for the subject before setup adds it.  Off, Tomcat answers 404; on, 401
    HttpResult probe = send("GET", scimUrl + "ServiceProviderConfig", null, null);
    assertTrue("SCIM servlet is not enabled in the running WS (GET ServiceProviderConfig returned "
        + probe.status + "): set grouper.is.scim=true in grouper.hibernate.properties of the WS "
        + "webapp and restart Tomcat", probe.status != 404);

    List<Subject> addedToWsGroup = new ArrayList<Subject>();
    long startMillis = System.currentTimeMillis();

    try {
      deleteFixtures();

      String visibleMembershipId;
      String hiddenMembershipId;
      String subj3MemberId;
      Timestamp originalEnabled;
      Timestamp originalDisabled;

      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        // WS admission: the WS only lets members of ws.client.user.group.name call it, and caches
        // the result per subject, so add them before the first call
        String wsGroupName = wsClientUserGroupName();
        if (wsGroupName != null && wsGroupName.trim().length() > 0) {
          Group wsGroup = GroupFinder.findByName(rootSession, wsGroupName, true);
          for (Subject subject : new Subject[] {SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ3}) {
            if (wsGroup.addMember(subject, false)) {
              addedToWsGroup.add(subject);
            }
          }
        }

        Group group = new GroupSave(rootSession).assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
        Group hiddenGroup = new GroupSave(rootSession).assignName(HIDDEN_GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
        // no default privileges for everyone, so only the grants below apply
        for (Group g : new Group[] {group, hiddenGroup}) {
          g.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
          g.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
        }
        group.addMember(SubjectTestHelper.SUBJ1, false);
        group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
        group.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.READ, false);
        group.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.UPDATE, false);
        hiddenGroup.addMember(SubjectTestHelper.SUBJ2, false);

        Membership visible = MembershipFinder.findImmediateMembership(rootSession, group, SubjectTestHelper.SUBJ1, true);
        visibleMembershipId = visible.getUuid();
        originalEnabled = visible.getEnabledTime();
        originalDisabled = visible.getDisabledTime();
        hiddenMembershipId = MembershipFinder.findImmediateMembership(rootSession, hiddenGroup, SubjectTestHelper.SUBJ2, true).getUuid();
        Member subj3Member = MemberFinder.findBySubject(rootSession, SubjectTestHelper.SUBJ3, true);
        subj3MemberId = subj3Member.getId();
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }

      // ---------- step 1: READ-only caller finds the membership id with a filter
      String filter = "groupName eq \"" + GROUP_NAME + "\" and subjectId eq \"" + SubjectTestHelper.SUBJ1_ID + "\"";
      HttpResult step1 = send("GET", scimUrl + "Memberships?filter=" + URLEncoder.encode(filter, "UTF-8"),
          SubjectTestHelper.SUBJ0_ID, null);
      assertEquals(step1.body, 200, step1.status);
      assertTrue("filter result does not have the membership id: " + step1.body,
          step1.body.contains(visibleMembershipId));

      // ---------- step 2: READ-only caller cannot delete it
      HttpResult step2 = send("DELETE", scimUrl + "Memberships/" + visibleMembershipId, SubjectTestHelper.SUBJ0_ID, null);
      assertEquals(step2.body, 403, step2.status);
      assertTrue("READ-only caller deleted the membership", isMember(GROUP_NAME, SubjectTestHelper.SUBJ1));

      // ---------- step 3: a membership the caller cannot see is not found, and leaks nothing
      HttpResult step3 = send("GET", scimUrl + "Memberships/" + hiddenMembershipId, SubjectTestHelper.SUBJ0_ID, null);
      assertEquals(step3.body, 404, step3.status);
      assertFalse("404 body names the hidden group: " + step3.body, step3.body.contains("scimHidden"));
      assertFalse("404 body names the hidden member: " + step3.body, step3.body.contains(SubjectTestHelper.SUBJ2_ID));

      // ---------- step 4: READ-only caller cannot change the dates
      String putBody = "{\"schemas\":[\"urn:tier:params:scim:schemas:Membership\"],"
          + "\"enabledTime\":\"2019-01-01T00:00:00\",\"disabledTime\":\"2020-01-01T00:00:00\"}";
      HttpResult step4 = send("PUT", scimUrl + "Memberships/" + visibleMembershipId, SubjectTestHelper.SUBJ0_ID, putBody);
      assertEquals(step4.body, 403, step4.status);
      rootSession = GrouperSession.startRootSession();
      try {
        Membership after = MembershipFinder.findByUuid(rootSession, visibleMembershipId, true, false);
        assertEquals("enabled time changed by a READ-only caller", originalEnabled, after.getEnabledTime());
        assertEquals("disabled time changed by a READ-only caller", originalDisabled, after.getDisabledTime());
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }

      // ---------- step 5: control: a caller with UPDATE deletes it, and it is audited
      HttpResult step5 = send("DELETE", scimUrl + "Memberships/" + visibleMembershipId, SubjectTestHelper.SUBJ3_ID, null);
      assertEquals(step5.body, 204, step5.status);
      assertFalse("UPDATE caller could not delete the membership", isMember(GROUP_NAME, SubjectTestHelper.SUBJ1));
      int auditCount = HibernateSession.bySqlStatic().select(int.class,
          "select count(*) from grouper_audit_entry ae, grouper_audit_type at where ae.audit_type_id = at.id"
          + " and at.action_name = 'deleteGroupMembership' and ae.logged_in_member_id = ? and ae.created_on >= ?"
          + " and ae.description like ?",
          GrouperUtil.toListObject(subj3MemberId, startMillis, "%" + GROUP_NAME + "%"));
      assertEquals("deleteGroupMembership audit entries logged in as test.subject.3", 1, auditCount);

    } finally {
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        if (addedToWsGroup.size() > 0) {
          Group wsGroup = GroupFinder.findByName(rootSession, wsClientUserGroupName(), true);
          for (Subject subject : addedToWsGroup) {
            wsGroup.deleteMember(subject, false);
          }
        }
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }
      deleteFixtures();
    }
  }

  /**
   * @return the WS admission group (ws.client.user.group.name in grouper-ws.properties), or null
   */
  private static String wsClientUserGroupName() {
    return GrouperWsConfig.retrieveConfig().propertyValueString("ws.client.user.group.name");
  }

  /**
   * @param groupName
   * @param subject
   * @return true if subject is a member (read fresh as root)
   */
  private static boolean isMember(String groupName, Subject subject) {
    // read the database, not the API: SCIM changed the membership in the WS JVM, and this test
    // JVM's membership caches would still answer with the old state
    int count = HibernateSession.bySqlStatic().select(int.class,
        "select count(*) from grouper_memberships_lw_v where group_name = ? and subject_id = ? "
        + "and subject_source = ? and list_name = 'members'",
        GrouperUtil.toListObject(groupName, subject.getId(), subject.getSourceId()));
    return count > 0;
  }

  /**
   * delete the test groups and folder if they exist
   */
  private static void deleteFixtures() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String groupName : new String[] {GROUP_NAME, HIDDEN_GROUP_NAME}) {
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
   * base url of the running WS: grouper.ws.url if set, else the junit.test.tomcat.* keys
   * @return e.g. http://localhost:8080/grouper-ws/
   */
  private static String wsBaseUrl() {
    GrouperConfig config = GrouperConfig.retrieveConfig();
    String url = config.propertyValueString("grouper.ws.url");
    if (url == null || url.trim().length() == 0) {
      boolean ssl = config.propertyValueBoolean("junit.test.tomcat.ssl", false);
      url = (ssl ? "https" : "http") + "://" + config.propertyValueString("junit.test.tomcat.domainName", "localhost")
          + ":" + config.propertyValueInt("junit.test.tomcat.port", 8080) + "/grouper-ws/";
    }
    return url.endsWith("/") ? url : (url + "/");
  }

  /**
   * send a SCIM request with WS Basic auth
   * @param method
   * @param url
   * @param subjectId user name, null for no credentials
   * @param body null for none
   * @return status and body
   * @throws IOException
   */
  private static HttpResult send(String method, String url, String subjectId, String body) throws IOException {
    HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
    try {
      connection.setConnectTimeout(TIMEOUT_MILLIS);
      connection.setReadTimeout(TIMEOUT_MILLIS);
      connection.setRequestMethod(method);
      if (subjectId != null) {
        connection.setRequestProperty("Authorization", "Basic "
            + Base64.getEncoder().encodeToString((subjectId + ":" + PASS).getBytes("UTF-8")));
      }
      connection.setRequestProperty("Accept", "application/scim+json");
      if (body != null) {
        connection.setRequestProperty("Content-Type", "application/scim+json");
        connection.setDoOutput(true);
        OutputStream outputStream = connection.getOutputStream();
        try {
          outputStream.write(body.getBytes("UTF-8"));
        } finally {
          outputStream.close();
        }
      }
      HttpResult result = new HttpResult();
      result.status = connection.getResponseCode();
      InputStream inputStream = result.status < 400 ? connection.getInputStream() : connection.getErrorStream();
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      if (inputStream != null) {
        try {
          byte[] buffer = new byte[4096];
          int read;
          while ((read = inputStream.read(buffer)) >= 0) {
            bytes.write(buffer, 0, read);
          }
        } finally {
          inputStream.close();
        }
      }
      result.body = new String(bytes.toByteArray(), "UTF-8");
      return result;
    } finally {
      connection.disconnect();
    }
  }
}
