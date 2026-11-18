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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.subject.Subject;

/**
 * Security advisory H7 (SCIM part), real HTTP against the running local WS: GET
 * /grouper-ws/scim/v2/Groups/&lt;id&gt; as a subject outside ws.client.user.group.name is refused
 * with 403, and as a subject in that group is not.  This is the automated form of the manual test steps
 * for the releases whose admission check is GrouperScimAdmissionFilter (2.6.19, 4.17.7, 5.0.0 to
 * 5.17.2): it proves Jersey's provider package scan registered the filter in the real SCIM
 * servlet, which ScimAdmissionTest (calls the filter directly) cannot.  On the releases with
 * GrouperScimServlet it checks the same thing through the servlet.
 *
 * <p>Needs: junit.test.tomcat=true, the WS with the patched classes in WEB-INF/classes,
 * grouper.is.scim=true, and ws.client.user.group.name set (the test fails at the start with a
 * clear message otherwise; it does not change that global setting, since a blank value admits
 * everyone and a new value would lock out other WS callers for many minutes).  Extends TestCase,
 * not GrouperTest (GrouperTest resets the database the running WS shares).</p>
 *
 * <p>Data: creates and deletes folder secPatch34 with one group that both subjects may VIEW and
 * READ (so before the fix the outsider would get 200 with the group, and the 403 is clearly the
 * admission check).  Adds test.subject.0 to the admission group only if it is not a member and
 * removes exactly that in finally.  The outsider is the first of test.subject.9, .4, .2 that is
 * not in the admission group.  The WS caches admission per subject for minutes, so both checks
 * poll for up to 330 s.  Uses HttpURLConnection (Java 8, so it compiles on 2.6.19).</p>
 *
 * <p>Goes in grouper-ws/grouper-ws/src/test/edu/internet2/middleware/grouper/ws/scim/.</p>
 */
public class ScimAdmissionHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "secPatch34";

  /** group both subjects may read; its id is in the SCIM URL */
  private static final String GROUP_NAME = STEM_NAME + ":scimAdmissionTarget";

  /** admitted caller (added to ws.client.user.group.name if needed) */
  private static final String ALLOWED_ID = "test.subject.0";

  /** outsider candidates, first one not in ws.client.user.group.name is used */
  private static final String[] OUTSIDER_CANDIDATE_IDS = new String[] {"test.subject.9", "test.subject.4", "test.subject.2"};

  /** local dev password of the test subjects */
  private static final String PASSWORD = "pass";

  /** the WS caches the admission result per subject (ws.client.user.group.cache.minutes, 5 min) */
  private static final long ADMISSION_WAIT_MILLIS = 330 * 1000L;

  /** http timeouts */
  private static final int TIMEOUT_MILLIS = 15000;

  /** subjects this test added to ws.client.user.group.name, removed in finally */
  private final List<Subject> addedToWsClients = new ArrayList<Subject>();

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(ScimAdmissionHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public ScimAdmissionHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public ScimAdmissionHttpTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- test

  /**
   * outsider: 403 from /scim/v2/Groups/&lt;id&gt;; admitted subject: not 403
   * @throws Exception
   */
  public void testScimGroupGetAdmission() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    String scimUrl = wsBaseUrl() + "scim/v2/";

    // probe without credentials, so no subject gets a cached admission result before setup.
    // SCIM off: Tomcat answers 404; on: 401
    HttpResult probe = send(scimUrl + "ServiceProviderConfig", null);
    assertTrue("SCIM is not enabled in the running WS (GET ServiceProviderConfig returned "
        + probe.status + "): set grouper.is.scim=true for the WS webapp and restart Tomcat", probe.status != 404);

    String wsGroupName = wsClientUserGroupName();
    assertTrue("this test needs ws.client.user.group.name set in the running WS and in this JVM's "
        + "grouper-ws.properties (e.g. etc:wsGroup); blank admits everyone, so there is nothing to test",
        wsGroupName != null && wsGroupName.trim().length() > 0);

    try {
      deleteFixtures();
      String outsiderId;
      String groupId;

      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        Group wsGroup = GroupFinder.findByName(rootSession, wsGroupName, false);
        if (wsGroup == null) {
          // the WS creates it empty on first use too
          wsGroup = new GroupSave(rootSession).assignName(wsGroupName).assignCreateParentStemsIfNotExist(true).save();
        }

        // admitted subject: add only if not already there, remember to remove exactly that
        Subject allowed = SubjectFinder.findByIdAndSource(ALLOWED_ID, "jdbc", true);
        if (!wsGroup.hasMember(allowed)) {
          wsGroup.addMember(allowed, false);
          this.addedToWsClients.add(allowed);
        }

        // outsider: never added, so nothing of the site's admission group is changed for it
        outsiderId = null;
        for (String candidateId : OUTSIDER_CANDIDATE_IDS) {
          if (!wsGroup.hasMember(SubjectFinder.findByIdAndSource(candidateId, "jdbc", true))) {
            outsiderId = candidateId;
            break;
          }
        }
        assertNotNull("precondition: one of these must not be in " + wsGroupName + ": "
            + java.util.Arrays.asList(OUTSIDER_CANDIDATE_IDS), outsiderId);
        Subject outsider = SubjectFinder.findByIdAndSource(outsiderId, "jdbc", true);

        // both may read the group, so only the admission check can refuse the outsider
        Group group = new GroupSave(rootSession).assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
        for (Subject subject : new Subject[] {allowed, outsider}) {
          group.grantPriv(subject, AccessPrivilege.VIEW, false);
          group.grantPriv(subject, AccessPrivilege.READ, false);
        }
        groupId = group.getId();
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }

      String groupUrl = scimUrl + "Groups/" + groupId;

      // ---------- outsider: 403 (before the fix: 200 with the group).  Poll, since an earlier
      // "allowed" may still be cached for this subject in the WS
      HttpResult outsiderResult = pollUntil(groupUrl, outsiderId, true);
      assertEquals(outsiderId + " is not in " + wsGroupName + ", GET " + groupUrl + ": " + outsiderResult,
          403, outsiderResult.status);
      assertFalse("403 body must not carry the group: " + outsiderResult, outsiderResult.body.contains(GROUP_NAME));

      // ---------- admitted subject: not 403 (poll: a "not allowed" from before setup may be cached)
      HttpResult allowedResult = pollUntil(groupUrl, ALLOWED_ID, false);
      assertTrue(ALLOWED_ID + " is in " + wsGroupName + ", GET " + groupUrl + ": " + allowedResult,
          allowedResult.status != 403);
      assertTrue(ALLOWED_ID + " must authenticate (not 401): " + allowedResult, allowedResult.status != 401);

      System.out.println("^secPatch34 SCIM admission over HTTP: " + outsiderId + " " + outsiderResult.status
          + ", " + ALLOWED_ID + " " + allowedResult.status);

    } finally {
      try {
        removeFromWs(wsGroupName);
      } finally {
        deleteFixtures();
      }
    }
  }

  /**
   * GET the url as the subject until the status is (want403) or is not (!want403) 403, or the
   * admission cache wait is over
   * @param url
   * @param subjectId
   * @param want403
   * @return the last result
   * @throws IOException
   */
  private static HttpResult pollUntil(String url, String subjectId, boolean want403) throws IOException {
    long deadline = System.currentTimeMillis() + ADMISSION_WAIT_MILLIS;
    while (true) {
      HttpResult result = send(url, subjectId);
      if ((result.status == 403) == want403 || System.currentTimeMillis() > deadline) {
        return result;
      }
      try {
        Thread.sleep(10000);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        return result;
      }
    }
  }

  // ---------------------------------------------------------------- setup and cleanup

  /**
   * @return the WS admission group (ws.client.user.group.name in grouper-ws.properties), or null
   */
  private static String wsClientUserGroupName() {
    return GrouperWsConfig.retrieveConfig().propertyValueString(GrouperWsConfig.WS_CLIENT_USER_GROUP_NAME);
  }

  /**
   * remove from ws.client.user.group.name the subjects this test added
   * @param wsGroupName
   */
  private void removeFromWs(String wsGroupName) {
    if (this.addedToWsClients.size() == 0) {
      return;
    }
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group wsGroup = GroupFinder.findByName(rootSession, wsGroupName, true);
      for (Subject subject : this.addedToWsClients) {
        wsGroup.deleteMember(subject, false);
      }
      this.addedToWsClients.clear();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the test group and folder if they exist
   */
  private static void deleteFixtures() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
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

  // ---------------------------------------------------------------- http

  /**
   * status and body of a response
   */
  private static class HttpResult {

    /** status */
    private int status;

    /** body, empty if none */
    private String body = "";

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "HTTP " + this.status + ": " + (this.body.length() > 2000 ? this.body.substring(0, 2000) + "..." : this.body);
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
   * GET with WS Basic auth
   * @param url
   * @param subjectId user name, null for no credentials
   * @return status and body
   * @throws IOException
   */
  private static HttpResult send(String url, String subjectId) throws IOException {
    HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
    try {
      connection.setConnectTimeout(TIMEOUT_MILLIS);
      connection.setReadTimeout(TIMEOUT_MILLIS);
      connection.setRequestMethod("GET");
      if (subjectId != null) {
        connection.setRequestProperty("Authorization", "Basic "
            + java.util.Base64.getEncoder().encodeToString((subjectId + ":" + PASSWORD).getBytes("UTF-8")));
      }
      connection.setRequestProperty("Accept", "application/scim+json");
      HttpResult result = new HttpResult();
      result.status = connection.getResponseCode();
      InputStream inputStream = result.status < 400 ? connection.getInputStream() : connection.getErrorStream();
      if (inputStream != null) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
          byte[] buffer = new byte[4096];
          int read;
          while ((read = inputStream.read(buffer)) >= 0) {
            bytes.write(buffer, 0, read);
          }
        } finally {
          inputStream.close();
        }
        result.body = new String(bytes.toByteArray(), "UTF-8");
      }
      return result;
    } finally {
      connection.disconnect();
    }
  }
}
