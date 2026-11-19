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
package edu.internet2.middleware.grouper.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory H7 (wsHeaderActAsAdmissionAndChaining), HTTP check against the running WS: when
 * the X-Grouper-actAs headers (ws.grouper.act.as.group) change who a request runs as, the
 * authenticated caller A must itself be in ws.client.user.group.name, and a request body
 * actAsSubjectLookup cannot then move from the header subject B to a third subject C (chaining
 * would give A the body act-as rights of B).  Body act-as without headers is unchanged, and no
 * state is left over for the next request.  GrouperWsHeaderActAsGuardTest simulates the filter;
 * this test checks the GrouperServiceJ2ee wiring through real WS calls.
 *
 * <p>Subjects: A1 = test.subject.5 (header caller, not in ws.client.user.group.name),
 * A2 = test.subject.8 (header caller, in it), B = test.subject.6 (header target, in it, and in
 * ws.act.as.group so it may body-act-as anyone), C = test.subject.7 (the only one who can view
 * test:secPatch33:cOnlyGroup).  ws.client.user.group.name and ws.act.as.group are the local ones
 * (etc:wsGroup, etc:wsActAsGroup); ws.grouper.act.as.group is stored in the database config as
 * test:secPatch33:headerCallers::::test:secPatch33:headerTargets and deleted at the end.</p>
 *
 * <p>The WS reads its config at most every ws.config.secondsBetweenUpdateChecks (600 s by
 * default) plus the database config refresh, so the test first waits (up to 12 minutes) until
 * header act-as A2 -> B works.  After the test the WS keeps the header rule until its next config
 * check; it only names this test's groups, which are deleted.</p>
 *
 * <p>Runs against the running local WS (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running WS uses the same database.  Memberships this test adds to the local
 * groups are removed at the end (only the ones it added), and etc:wsActAsGroup is deleted again
 * if the test had to create it.</p>
 *
 * <p>Uses HttpURLConnection and Java 8 APIs only.</p>
 */
public class GrouperWsHeaderActAsHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch33";

  /** header callers (A1, A2) */
  private static final String HEADER_CALLERS = STEM_NAME + ":headerCallers";

  /** header targets (B) */
  private static final String HEADER_TARGETS = STEM_NAME + ":headerTargets";

  /** only C can view it */
  private static final String C_ONLY_GROUP = STEM_NAME + ":cOnlyGroup";

  /** only B can view it, used to see when the WS has the header rule */
  private static final String B_ONLY_GROUP = STEM_NAME + ":bOnlyGroup";

  /** used by the admission probe */
  private static final String GROUP_NAME = C_ONLY_GROUP;

  /** header caller not in ws.client.user.group.name */
  private static final String A1_ID = "test.subject.5";

  /** header caller in ws.client.user.group.name */
  private static final String A2_ID = "test.subject.8";

  /** header target, WS client, may body-act-as anyone */
  private static final String B_ID = "test.subject.6";

  /** body act-as target */
  private static final String C_ID = "test.subject.7";

  /** local dev password of the test subjects */
  private static final String PASSWORD = "pass";

  /** config key of the header act-as rule */
  private static final String CONFIG_GROUPER_ACT_AS_GROUP = "ws.grouper.act.as.group";

  /** the WS reads its config at most every 600 s plus the database config refresh */
  private static final long CONFIG_WAIT_MILLIS = 12 * 60 * 1000L;

  /** subjects this test added to ws.client.user.group.name, removed in finally */
  private final List<Subject> addedToWsClients = new ArrayList<Subject>();

  /** subjects this test added to ws.act.as.group, removed in finally */
  private final List<Subject> addedToActAs = new ArrayList<Subject>();

  /** true if this test created the ws.act.as.group group */
  private boolean createdActAsGroup = false;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperWsHeaderActAsHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public GrouperWsHeaderActAsHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperWsHeaderActAsHttpTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- tests

  /**
   * the manual test steps 1-4
   * @throws Exception
   */
  public void testHeaderActAsAdmissionAndChaining() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    boolean storedConfig = false;
    try {
      setupGroups();
      assertFalse("precondition: " + A1_ID + " must not be in ws.client.user.group.name", isWsClient(A1_ID));
      admitToWs(A2_ID, B_ID);
      addToActAsGroup(B_ID);
      waitForWsAdmission(A2_ID);
      waitForWsAdmission(B_ID);

      storeWsConfig(CONFIG_GROUPER_ACT_AS_GROUP, HEADER_CALLERS + "::::" + HEADER_TARGETS);
      storedConfig = true;
      waitForHeaderActAsRule();

      // step 1, curl 1: A1 directly is not a WS client
      WsResponse a1Direct = findGroup(A1_ID, null, C_ONLY_GROUP, null);
      assertRefusedNotAuthorized("A1 without headers", a1Direct);

      // step 1, curl 3: A1 -> C is not allowed by the header rule
      WsResponse a1HeaderC = findGroup(A1_ID, C_ID, C_ONLY_GROUP, null);
      assertEquals("A1 header -> C must fail: " + a1HeaderC, "F", success(a1HeaderC.body));
      assertFalse("A1 header -> C must not see the group: " + a1HeaderC, a1HeaderC.body.contains("\"name\":\"" + C_ONLY_GROUP + "\""));

      // step 1, curl 2: A1 -> B is refused since A1 is not admitted (the fix; before, B's admission was enough)
      WsResponse a1HeaderB = findGroup(A1_ID, B_ID, B_ONLY_GROUP, null);
      assertRefusedNotAuthorized("A1 header -> B (A1 is not in ws.client.user.group.name)", a1HeaderB);

      // step 1, curl 4: A1 -> B -> C is refused
      WsResponse a1Chain = findGroup(A1_ID, B_ID, C_ONLY_GROUP, C_ID);
      assertEquals("A1 header -> B, body -> C must fail: " + a1Chain, "F", success(a1Chain.body));
      assertFalse("A1 chain must not see the group: " + a1Chain, a1Chain.body.contains("\"name\":\"" + C_ONLY_GROUP + "\""));

      // step 2, curl 2: A2 (admitted) -> B works, B cannot view the C group
      WsResponse a2HeaderB = findGroup(A2_ID, B_ID, C_ONLY_GROUP, null);
      assertEquals("A2 header -> B must succeed: " + a2HeaderB, "T", success(a2HeaderB.body));
      assertFalse("B cannot view the C group: " + a2HeaderB, a2HeaderB.body.contains("\"name\":\"" + C_ONLY_GROUP + "\""));

      // step 2, curl 4: A2 -> B -> C is refused with the chained act-as message (the fix)
      WsResponse a2Chain = findGroup(A2_ID, B_ID, C_ONLY_GROUP, C_ID);
      assertFalse("A2 header -> B, body -> C must not see the group: " + a2Chain,
          a2Chain.body.contains("\"name\":\"" + C_ONLY_GROUP + "\""));
      assertEquals("A2 chain must fail: " + a2Chain, "F", success(a2Chain.body));
      assertTrue("chained act-as message: " + a2Chain, a2Chain.body.contains("cannot also send an actAsSubjectLookup"));

      // step 3: B without headers, body act-as C, is unchanged
      WsResponse bBodyC = findGroup(B_ID, null, C_ONLY_GROUP, C_ID);
      assertEquals("B body -> C must succeed: " + bBodyC, "T", success(bBodyC.body));
      assertTrue("B body -> C sees the C group: " + bBodyC, bBodyC.body.contains("\"name\":\"" + C_ONLY_GROUP + "\""));

      // step 4: nothing is left over from the refused requests: same results again
      WsResponse a2ChainAgain = findGroup(A2_ID, B_ID, C_ONLY_GROUP, C_ID);
      assertTrue("chained act-as refused again: " + a2ChainAgain,
          a2ChainAgain.body.contains("cannot also send an actAsSubjectLookup"));
      for (int i = 0; i < 3; i++) {
        // a few times, so the calls land on more than one Tomcat thread
        WsResponse bBodyCAgain = findGroup(B_ID, null, C_ONLY_GROUP, C_ID);
        assertTrue("B body -> C after a refused request: " + bBodyCAgain,
            bBodyCAgain.body.contains("\"name\":\"" + C_ONLY_GROUP + "\""));
        WsResponse a2Direct = findGroup(A2_ID, null, C_ONLY_GROUP, null);
        assertEquals("A2 without headers runs as A2: " + a2Direct, "T", success(a2Direct.body));
        assertFalse("A2 cannot view the C group: " + a2Direct, a2Direct.body.contains("\"name\":\"" + C_ONLY_GROUP + "\""));
        assertRefusedNotAuthorized("A1 without headers again", findGroup(A1_ID, null, C_ONLY_GROUP, null));
      }

    } finally {
      try {
        if (storedConfig) {
          deleteWsConfig(CONFIG_GROUPER_ACT_AS_GROUP);
        }
      } finally {
        try {
          removeFromActAsGroup();
        } finally {
          try {
            deleteStem();
          } finally {
            removeFromWs();
          }
        }
      }
    }
  }

  /**
   * @param label
   * @param response
   */
  private static void assertRefusedNotAuthorized(String label, WsResponse response) {
    assertEquals(label + " must fail: " + response, "F", success(response.body));
    assertTrue(label + " must be refused as not authorized: " + response, response.body.contains("User is not authorized"));
  }

  /**
   * send find groups by exact name
   * @param user authenticated caller
   * @param headerActAs subject for the X-Grouper-actAs headers, or null
   * @param groupName to find
   * @param bodyActAs subject for the body actAsSubjectLookup, or null
   * @return the response
   * @throws IOException
   */
  private static WsResponse findGroup(String user, String headerActAs, String groupName, String bodyActAs)
      throws IOException {
    String body = "{\"WsRestFindGroupsRequest\":{"
        + (bodyActAs == null ? "" : "\"actAsSubjectLookup\":{\"subjectId\":\"" + bodyActAs + "\",\"subjectSourceId\":\"jdbc\"},")
        + "\"wsQueryFilter\":{\"queryFilterType\":\"FIND_BY_GROUP_NAME_EXACT\",\"groupName\":\"" + groupName + "\"}}}";
    return call("POST", wsBaseUrl() + "/servicesRest/v2_4_000/groups", user, "application/json", body, headerActAs);
  }

  /**
   * wait until the WS has the header act-as rule: A2 with headers for B sees the group only B can view
   * @throws IOException
   */
  private static void waitForHeaderActAsRule() throws IOException {
    long deadline = System.currentTimeMillis() + CONFIG_WAIT_MILLIS;
    while (true) {
      WsResponse response = findGroup(A2_ID, B_ID, B_ONLY_GROUP, null);
      if (response.body.contains("\"name\":\"" + B_ONLY_GROUP + "\"")) {
        return;
      }
      if (System.currentTimeMillis() > deadline) {
        fail("WS did not pick up " + CONFIG_GROUPER_ACT_AS_GROUP + " within " + (CONFIG_WAIT_MILLIS / 1000)
            + " s: " + response);
      }
      GrouperUtil.sleep(15000);
    }
  }

  // ---------------------------------------------------------------- setup and cleanup

  /**
   * create the header rule groups and the two view-restricted groups
   */
  private static void setupGroups() {
    deleteStem();
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Subject a1 = SubjectFinder.findByIdAndSource(A1_ID, "jdbc", true);
      Subject a2 = SubjectFinder.findByIdAndSource(A2_ID, "jdbc", true);
      Subject b = SubjectFinder.findByIdAndSource(B_ID, "jdbc", true);
      Subject c = SubjectFinder.findByIdAndSource(C_ID, "jdbc", true);

      Group headerCallers = new GroupSave(rootSession).assignName(HEADER_CALLERS).assignCreateParentStemsIfNotExist(true).save();
      headerCallers.addMember(a1, false);
      headerCallers.addMember(a2, false);
      Group headerTargets = new GroupSave(rootSession).assignName(HEADER_TARGETS).assignCreateParentStemsIfNotExist(true).save();
      headerTargets.addMember(b, false);

      Group cOnlyGroup = new GroupSave(rootSession).assignName(C_ONLY_GROUP).assignCreateParentStemsIfNotExist(true).save();
      onlyThisSubjectCanView(cOnlyGroup, c);
      Group bOnlyGroup = new GroupSave(rootSession).assignName(B_ONLY_GROUP).assignCreateParentStemsIfNotExist(true).save();
      onlyThisSubjectCanView(bOnlyGroup, b);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * @param group
   * @param subject the only one (besides wheel) who may view it
   */
  private static void onlyThisSubjectCanView(Group group, Subject subject) {
    Subject everyEntity = SubjectFinder.findAllSubject();
    for (Privilege privilege : new Privilege[] {AccessPrivilege.VIEW, AccessPrivilege.READ}) {
      group.revokePriv(everyEntity, privilege, false);
    }
    group.grantPriv(subject, AccessPrivilege.VIEW, false);
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
   * @param subjectId
   * @return true if in ws.client.user.group.name (or it is blank)
   */
  private static boolean isWsClient(String subjectId) {
    String groupName = GrouperWsConfigInApi.retrieveConfig().propertyValueString("ws.client.user.group.name");
    assertFalse("this test needs ws.client.user.group.name", GrouperUtil.isBlank(groupName));
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, groupName, false);
      return group != null && group.hasMember(SubjectFinder.findByIdAndSource(subjectId, "jdbc", true));
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * add a subject to the (single group) ws.act.as.group, creating the group if needed
   * @param subjectId
   */
  private void addToActAsGroup(String subjectId) {
    String groupName = GrouperWsConfigInApi.retrieveConfig().propertyValueString("ws.act.as.group");
    assertFalse("this test needs ws.act.as.group", GrouperUtil.isBlank(groupName));
    assertFalse("this test needs ws.act.as.group to be one group name: " + groupName,
        groupName.contains(",") || groupName.contains("::::"));
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, groupName, false);
      if (group == null) {
        group = new GroupSave(rootSession).assignName(groupName).assignCreateParentStemsIfNotExist(true).save();
        this.createdActAsGroup = true;
      }
      Subject subject = SubjectFinder.findByIdAndSource(subjectId, "jdbc", true);
      if (!group.hasMember(subject)) {
        group.addMember(subject, false);
        this.addedToActAs.add(subject);
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * undo addToActAsGroup: remove the members it added, delete the group if it created it
   */
  private void removeFromActAsGroup() {
    String groupName = GrouperWsConfigInApi.retrieveConfig().propertyValueString("ws.act.as.group");
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GrouperUtil.isBlank(groupName) ? null : GroupFinder.findByName(rootSession, groupName, false);
      if (group == null) {
        return;
      }
      if (this.createdActAsGroup) {
        group.delete();
        this.createdActAsGroup = false;
      } else {
        for (Subject subject : this.addedToActAs) {
          group.deleteMember(subject, false);
        }
      }
      this.addedToActAs.clear();
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
    return call(method, url, user, contentType, body, null);
  }

  /**
   * send a request with HTTP basic auth (15 s timeouts)
   * @param method
   * @param url
   * @param user subject id, password is PASSWORD
   * @param contentType of the body, or null
   * @param body or null for none
   * @param headerActAsSubjectId if not null, send the X-Grouper-actAs headers for this jdbc subject
   * @return the response
   * @throws IOException
   */
  private static WsResponse call(String method, String url, String user, String contentType, String body,
      String headerActAsSubjectId) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    try {
      connection.setConnectTimeout(15000);
      connection.setReadTimeout(15000);
      connection.setRequestMethod(method);
      String credentials = user + ":" + PASSWORD;
      connection.setRequestProperty("Authorization", "Basic "
          + java.util.Base64.getEncoder().encodeToString(credentials.getBytes("UTF-8")));
      if (headerActAsSubjectId != null) {
        // the header values are base64 (GrouperServiceJ2ee decodes them)
        connection.setRequestProperty("X-Grouper-actAsSourceId",
            java.util.Base64.getEncoder().encodeToString("jdbc".getBytes("UTF-8")));
        connection.setRequestProperty("X-Grouper-actAsSubjectId",
            java.util.Base64.getEncoder().encodeToString(headerActAsSubjectId.getBytes("UTF-8")));
      }
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
