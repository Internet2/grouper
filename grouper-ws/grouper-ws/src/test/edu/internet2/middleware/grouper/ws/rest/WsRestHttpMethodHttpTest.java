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
package edu.internet2.middleware.grouper.ws.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
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
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfigInApi;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory C1 (wsRestHttpMethodFromRequestObject), HTTP check against the running WS:
 * GrouperRestServlet must refuse a write operation (here WsRestAddMemberLiteRequest) sent with
 * HTTP GET, while the same request with POST or PUT, and reads with GET, still work.
 * WsRestHttpMethodValidatorTest only tests the validator class; this test checks the servlet
 * calls it, through real HTTP requests.
 *
 * <p>Runs against the running local WS (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running WS uses the same database.  Setup and cleanup are this test's own:
 * folder test:secPatch56 (deleted at the end), and test.subject.0 is added to
 * ws.client.user.group.name (only if it was not a member, and removed again at the end).</p>
 *
 * <p>testAllowWritesWithGetOverride stores ws.rest.allowWritesWithGet=true in the database config
 * and waits for the WS to read it, which can take ws.config.secondsBetweenUpdateChecks (600 by
 * default) plus the database config refresh, so it can run about 11 minutes.  It deletes the key
 * at the end (the WS keeps the old value until its next config check).</p>
 *
 * <p>Uses HttpURLConnection and Java 8 APIs only, so the same file works on every release.</p>
 */
public class WsRestHttpMethodHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch56";

  /** group the add member requests go to */
  private static final String GROUP_NAME = STEM_NAME + ":target";

  /** WS caller, has UPDATE and READ on the group */
  private static final String CALLER_ID = "test.subject.0";

  /** subject the write requests try to add, not a WS user */
  private static final String MEMBER_ID = "test.subject.9";

  /** local dev password of the test subjects */
  private static final String PASSWORD = "pass";

  /** the config key the fix adds */
  private static final String CONFIG_ALLOW_WRITES_WITH_GET = "ws.rest.allowWritesWithGet";

  /** the WS reads its config at most every 600 s plus the database config refresh */
  private static final long CONFIG_WAIT_MILLIS = 12 * 60 * 1000L;

  /** subjects this test added to ws.client.user.group.name, removed in finally */
  private final List<Subject> addedToWsClients = new ArrayList<Subject>();

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(WsRestHttpMethodHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public WsRestHttpMethodHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public WsRestHttpMethodHttpTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- tests

  /**
   * the manual test steps 1-3: a write with GET is refused and changes nothing, the same write
   * with POST and PUT works, a read with GET works
   * @throws Exception
   */
  public void testWriteWithGetRefused() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    try {
      setupGroup();
      admitToWs(CALLER_ID);
      waitForWsAdmission(CALLER_ID);

      // step 1: add member lite sent with GET is refused, message names the operation and POST
      WsResponse getWrite = call("GET", addMemberUrl(), CALLER_ID, null, null);
      assertEquals("write with GET must be HTTP 400: " + getWrite, 400, getWrite.status);
      assertEquals(getWrite.toString(), "INVALID_QUERY", resultCode(getWrite.body));
      assertTrue("message names the operation: " + getWrite, getWrite.body.contains("WsRestAddMemberLiteRequest"));
      assertTrue("message says to use POST: " + getWrite, getWrite.body.contains("POST"));
      assertFalse("GET must not add the member", isMember(MEMBER_ID));

      // HEAD is refused the same way (no body comes back, so only the status and no change)
      WsResponse headWrite = call("HEAD", addMemberUrl(), CALLER_ID, null, null);
      assertEquals("write with HEAD must be HTTP 400: " + headWrite, 400, headWrite.status);
      assertFalse("HEAD must not add the member", isMember(MEMBER_ID));

      // step 2: the same url with POST adds the member
      WsResponse postWrite = call("POST", addMemberUrl(), CALLER_ID, null, null);
      assertTrue("POST add member must succeed: " + postWrite, postWrite.status >= 200 && postWrite.status < 300);
      assertEquals(postWrite.toString(), "T", success(postWrite.body));
      assertTrue("POST must add the member", isMember(MEMBER_ID));

      // and with PUT
      removeMember(MEMBER_ID);
      WsResponse putWrite = call("PUT", addMemberUrl(), CALLER_ID, null, null);
      assertTrue("PUT add member must succeed: " + putWrite, putWrite.status >= 200 && putWrite.status < 300);
      assertEquals(putWrite.toString(), "T", success(putWrite.body));
      assertTrue("PUT must add the member", isMember(MEMBER_ID));

      // step 3: reads with GET still work and list the member
      WsResponse getRead = call("GET", groupMembersUrl() + "?wsLiteObjectType=WsRestGetMembersLiteRequest",
          CALLER_ID, null, null);
      assertEquals("read with GET must be HTTP 200: " + getRead, 200, getRead.status);
      assertEquals(getRead.toString(), "T", success(getRead.body));
      assertTrue("members list holds the member: " + getRead, getRead.body.contains(MEMBER_ID));

    } finally {
      cleanup();
    }
  }

  /**
   * the manual test step 4: with ws.rest.allowWritesWithGet=true a write with GET is allowed
   * again.  Waits up to 12 minutes for the WS to read the database config.  The WARN in the WS
   * log goes to the Tomcat console and is not checked here.
   * @throws Exception
   */
  public void testAllowWritesWithGetOverride() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    boolean storedConfig = false;
    try {
      setupGroup();
      admitToWs(CALLER_ID);
      waitForWsAdmission(CALLER_ID);

      storeWsConfig(CONFIG_ALLOW_WRITES_WITH_GET, "true");
      storedConfig = true;

      // keep sending the GET write until the WS has the new config (then it is allowed)
      long deadline = System.currentTimeMillis() + CONFIG_WAIT_MILLIS;
      WsResponse getWrite = null;
      while (true) {
        getWrite = call("GET", addMemberUrl(), CALLER_ID, null, null);
        if (getWrite.status != 400) {
          break;
        }
        assertFalse("a refused GET must not add the member", isMember(MEMBER_ID));
        if (System.currentTimeMillis() > deadline) {
          fail("WS did not pick up " + CONFIG_ALLOW_WRITES_WITH_GET + "=true within "
              + (CONFIG_WAIT_MILLIS / 1000) + " s: " + getWrite);
        }
        GrouperUtil.sleep(15000);
      }
      assertTrue("with the override GET add member must succeed: " + getWrite,
          getWrite.status >= 200 && getWrite.status < 300);
      assertEquals(getWrite.toString(), "T", success(getWrite.body));
      assertTrue("with the override GET adds the member", isMember(MEMBER_ID));

    } finally {
      if (storedConfig) {
        deleteWsConfig(CONFIG_ALLOW_WRITES_WITH_GET);
      }
      cleanup();
    }
  }

  // ---------------------------------------------------------------- setup and cleanup

  /**
   * create the folder and group, grant the caller UPDATE and READ, make sure the member is not in it
   */
  private static void setupGroup() {
    deleteStem();
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = new GroupSave(rootSession).assignName(GROUP_NAME)
          .assignCreateParentStemsIfNotExist(true).save();
      Subject caller = SubjectFinder.findByIdAndSource(CALLER_ID, "jdbc", true);
      group.grantPriv(caller, AccessPrivilege.UPDATE, false);
      group.grantPriv(caller, AccessPrivilege.READ, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete the folder and what is in it, if there
   */
  private static void deleteStem() {
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

  /**
   * delete the folder and take out of ws.client.user.group.name exactly the subjects this test added
   */
  private void cleanup() {
    try {
      deleteStem();
    } finally {
      removeFromWs();
    }
  }

  /**
   * @param subjectId
   * @return true if the subject is a member of the group
   */
  private static boolean isMember(String subjectId) {
    // read the database, not the API: the WS changed the membership in another JVM, and this test
    // JVM's membership caches would still answer with the old state
    int count = HibernateSession.bySqlStatic().select(int.class,
        "select count(*) from grouper_memberships_lw_v where group_name = ? and subject_id = ? "
        + "and subject_source = 'jdbc' and list_name = 'members'",
        GrouperUtil.toListObject(GROUP_NAME, subjectId));
    return count > 0;
  }

  /**
   * @param subjectId
   */
  private static void removeMember(String subjectId) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, GROUP_NAME, true);
      group.deleteMember(SubjectFinder.findByIdAndSource(subjectId, "jdbc", true), false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * store one grouper-ws.properties key in the database config
   * @param key
   * @param value
   */
  private static void storeWsConfig(String key, String value) {
    // GrouperDbConfig checks the caller may edit config, so it needs a (root) session
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      new GrouperDbConfig().configFileName("grouper-ws.properties").propertyName(key).value(value).store();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * delete one grouper-ws.properties key from the database config
   * @param key
   */
  private static void deleteWsConfig(String key) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      new GrouperDbConfig().configFileName("grouper-ws.properties").propertyName(key).delete();
    } catch (RuntimeException re) {
      // already gone
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  // ---------------------------------------------------------------- WS admission

  /**
   * add subjects to ws.client.user.group.name (the WS only admits its members).  Only subjects
   * that are not already members are added and remembered, so cleanup removes only those.
   * @param subjectIds
   */
  private void admitToWs(String... subjectIds) {
    String groupName = GrouperWsConfigInApi.retrieveConfig().propertyValueString("ws.client.user.group.name");
    if (GrouperUtil.isBlank(groupName)) {
      return;
    }
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, groupName, false);
      if (group == null) {
        // the WS creates it empty on first use too
        group = new GroupSave(rootSession).assignName(groupName).assignCreateParentStemsIfNotExist(true).save();
      }
      for (String subjectId : subjectIds) {
        Subject subject = SubjectFinder.findByIdAndSource(subjectId, "jdbc", true);
        if (!group.hasMember(subject)) {
          group.addMember(subject, false);
          this.addedToWsClients.add(subject);
        }
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * remove from ws.client.user.group.name the subjects admitToWs added
   */
  private void removeFromWs() {
    if (this.addedToWsClients.size() == 0) {
      return;
    }
    String groupName = GrouperWsConfigInApi.retrieveConfig().propertyValueString("ws.client.user.group.name");
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, groupName, true);
      for (Subject subject : this.addedToWsClients) {
        group.deleteMember(subject, false);
      }
      this.addedToWsClients.clear();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * the WS caches "not admitted" per subject (ws.client.user.group.cache.minutes), so after
   * adding a subject keep calling until it is let in
   * @param subjectId
   * @throws IOException
   */
  private static void waitForWsAdmission(String subjectId) throws IOException {
    long deadline = System.currentTimeMillis() + 330 * 1000L;
    while (true) {
      WsResponse response = call("POST", wsBaseUrl() + "/servicesRest/v2_4_000/groups", subjectId,
          "application/json", "{\"WsRestFindGroupsRequest\":{\"wsQueryFilter\":{\"queryFilterType\":"
          + "\"FIND_BY_GROUP_NAME_EXACT\",\"groupName\":\"" + GROUP_NAME + "\"}}}");
      if (!response.body.contains("User is not authorized")) {
        return;
      }
      if (System.currentTimeMillis() > deadline) {
        fail("WS still refuses " + subjectId + " (ws.client.user.group.name): " + response);
      }
      GrouperUtil.sleep(10000);
    }
  }

  // ---------------------------------------------------------------- http helpers

  /**
   * one http response
   */
  private static final class WsResponse {

    /** http status */
    private int status;

    /** response body, empty if none */
    private String body;

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "HTTP " + this.status + ": " + (this.body.length() > 3000 ? this.body.substring(0, 3000) + "..." : this.body);
    }
  }

  /**
   * base url of the running WS from the same junit.test.tomcat.* keys the mock tests use
   * @return e.g. http://localhost:8080/grouper-ws
   */
  private static String wsBaseUrl() {
    GrouperConfig config = GrouperConfig.retrieveConfig();
    boolean ssl = config.propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = config.propertyValueString("junit.test.tomcat.domainName", "localhost");
    int port = config.propertyValueInt("junit.test.tomcat.port", 8080);
    return (ssl ? "https" : "http") + "://" + domainName + ":" + port + "/grouper-ws";
  }

  /**
   * @return rest url of the group members
   * @throws IOException
   */
  private static String groupMembersUrl() throws IOException {
    return wsBaseUrl() + "/servicesRest/v2_4_000/groups/" + URLEncoder.encode(GROUP_NAME, "UTF-8") + "/members";
  }

  /**
   * @return add member lite url: the operation is in the query string, so the HTTP method alone
   * decides if it is GET or POST
   * @throws IOException
   */
  private static String addMemberUrl() throws IOException {
    return groupMembersUrl() + "?wsLiteObjectType=WsRestAddMemberLiteRequest&subjectId=" + MEMBER_ID;
  }

  /**
   * send a request with HTTP basic auth (15 s timeouts)
   * @param method
   * @param url
   * @param user subject id, password is PASSWORD
   * @param contentType of the body, or null
   * @param body or null for none
   * @return the response
   * @throws IOException
   */
  private static WsResponse call(String method, String url, String user, String contentType, String body)
      throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    try {
      connection.setConnectTimeout(15000);
      connection.setReadTimeout(15000);
      connection.setRequestMethod(method);
      String credentials = user + ":" + PASSWORD;
      connection.setRequestProperty("Authorization", "Basic "
          + java.util.Base64.getEncoder().encodeToString(credentials.getBytes("UTF-8")));
      if (body != null) {
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", contentType);
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(body.getBytes("UTF-8"));
        outputStream.close();
      }
      WsResponse wsResponse = new WsResponse();
      wsResponse.status = connection.getResponseCode();
      InputStream inputStream = wsResponse.status >= 400 ? connection.getErrorStream() : connection.getInputStream();
      wsResponse.body = readFully(inputStream);
      return wsResponse;
    } finally {
      connection.disconnect();
    }
  }

  /**
   * @param inputStream may be null
   * @return the content as UTF-8, empty if none
   * @throws IOException
   */
  private static String readFully(InputStream inputStream) throws IOException {
    if (inputStream == null) {
      return "";
    }
    try {
      ByteArrayOutputStream result = new ByteArrayOutputStream();
      byte[] buffer = new byte[8192];
      int read;
      while ((read = inputStream.read(buffer)) != -1) {
        result.write(buffer, 0, read);
      }
      return new String(result.toByteArray(), "UTF-8");
    } finally {
      inputStream.close();
    }
  }

  /**
   * @param body json response
   * @return the first resultCode in it, or null
   */
  private static String resultCode(String body) {
    return firstJsonString(body, "resultCode");
  }

  /**
   * @param body json response
   * @return the first success flag in it, or null
   */
  private static String success(String body) {
    return firstJsonString(body, "success");
  }

  /**
   * @param body json
   * @param field name
   * @return first string value of the field, or null
   */
  private static String firstJsonString(String body, String field) {
    Matcher matcher = Pattern.compile("\"" + field + "\":\"([^\"]*)\"").matcher(body);
    return matcher.find() ? matcher.group(1) : null;
  }

}
