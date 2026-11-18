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
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfigInApi;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory C1 (wsJsonpWrapperCrossSiteRead), HTTP check against the running WS: the
 * grouperJsonResponseWrapper param is printed in front of a json response, so GrouperRestServlet
 * must refuse a value that is not a plain javascript function name before the operation runs
 * (HTTP 400, INVALID_QUERY, value not echoed), and an error response must never start with raw
 * request text.  A valid name still wraps the response.  WsRestJsonResponseWrapperTest only
 * tests the validator class; this test checks the two servlet call sites through real HTTP.
 *
 * <p>Runs against the running local WS (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running WS uses the same database.  Setup and cleanup are this test's own:
 * folder test:secPatch66 (deleted at the end), and test.subject.1 is added to
 * ws.client.user.group.name (only if it was not a member, and removed again at the end).</p>
 *
 * <p>Assumes ws.allowJsonWrapper is on (the patch sets keep the default true; on mainline, where
 * the default is false, set it to true for this test).</p>
 *
 * <p>Uses HttpURLConnection and Java 8 APIs only, so the same file works on every release.</p>
 */
public class WsRestJsonResponseWrapperHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch66";

  /** group whose members are read */
  private static final String GROUP_NAME = STEM_NAME + ":group";

  /** WS caller, has UPDATE and READ on the group */
  private static final String CALLER_ID = "test.subject.1";

  /** member of the group, listed in the members response */
  private static final String MEMBER_ID = "test.subject.9";

  /** subject the refused write tries to add */
  private static final String WRITE_MEMBER_ID = "test.subject.8";

  /** local dev password of the test subjects */
  private static final String PASSWORD = "pass";

  /** subjects this test added to ws.client.user.group.name, removed in finally */
  private final List<Subject> addedToWsClients = new ArrayList<Subject>();

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(WsRestJsonResponseWrapperHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public WsRestJsonResponseWrapperHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public WsRestJsonResponseWrapperHttpTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- tests

  /**
   * the manual test steps 1-3 and the "before the operation runs" part of the fix
   * @throws Exception
   */
  public void testJsonWrapper() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    try {
      setupGroup();
      addMember(MEMBER_ID);
      admitToWs(CALLER_ID);
      waitForWsAdmission(CALLER_ID);

      String membersUrl = wsBaseUrl() + "/servicesRest/json/v2_4_000/groups/"
          + URLEncoder.encode(GROUP_NAME, "UTF-8") + "/members";

      // step 1: a plain function name wraps the normal members json
      WsResponse wrapped = call("GET", membersUrl + "?grouperJsonResponseWrapper=cb", CALLER_ID, null, null);
      assertEquals("valid wrapper must be HTTP 200: " + wrapped, 200, wrapped.status);
      assertTrue("response starts with cb(: " + wrapped, wrapped.body.startsWith("cb({"));
      assertTrue("response ends with ): " + wrapped, wrapped.body.trim().endsWith(")"));
      assertTrue("normal members json inside: " + wrapped, wrapped.body.contains("WsGetMembersLiteResult"));
      assertEquals(wrapped.toString(), "T", success(wrapped.body));
      assertTrue("member listed: " + wrapped, wrapped.body.contains(MEMBER_ID));

      // step 2: markup as the wrapper is refused with a plain json error that does not echo it
      String badWrapper = "<script>alert(1)</script>";
      WsResponse refused = call("GET", membersUrl + "?grouperJsonResponseWrapper="
          + URLEncoder.encode(badWrapper, "UTF-8"), CALLER_ID, null, null);
      assertEquals("invalid wrapper must be HTTP 400: " + refused, 400, refused.status);
      assertEquals(refused.toString(), "INVALID_QUERY", resultCode(refused.body));
      assertTrue("plain json, nothing wrapped: " + refused, refused.body.startsWith("{"));
      assertFalse("wrapper value must not be echoed: " + refused, refused.body.contains("<script"));
      assertFalse("wrapper value must not be echoed: " + refused, refused.body.contains("alert(1)"));
      assertFalse("members must not be returned: " + refused, refused.body.contains(MEMBER_ID));

      // the refusal comes before the operation runs: a write with a bad wrapper changes nothing
      WsResponse refusedWrite = call("POST", membersUrl + "?wsLiteObjectType=WsRestAddMemberLiteRequest&subjectId="
          + WRITE_MEMBER_ID + "&grouperJsonResponseWrapper=" + URLEncoder.encode(badWrapper, "UTF-8"),
          CALLER_ID, null, null);
      assertEquals("write with invalid wrapper must be HTTP 400: " + refusedWrite, 400, refusedWrite.status);
      assertFalse("write with invalid wrapper must not run", isMember(WRITE_MEMBER_ID));

      // step 3: an error before the wrapper check (bad version in the url) is not wrapped either
      String badWrapper2 = "alert(1);cb";
      WsResponse errorResponse = call("GET", wsBaseUrl() + "/servicesRest/json/v9_9_999/groups/x/members"
          + "?grouperJsonResponseWrapper=" + URLEncoder.encode(badWrapper2, "UTF-8"), CALLER_ID, null, null);
      assertTrue("error response must be an error: " + errorResponse, errorResponse.status >= 400);
      assertFalse("error response must not start with request text: " + errorResponse,
          errorResponse.body.startsWith("alert"));
      assertTrue("error response is plain json: " + errorResponse, errorResponse.body.startsWith("{"));
      assertFalse("wrapper value must not be printed as a wrapper: " + errorResponse,
          errorResponse.body.contains(badWrapper2 + "("));

    } finally {
      cleanup();
    }
  }

  /**
   * @param subjectId
   */
  private static void addMember(String subjectId) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, GROUP_NAME, true);
      group.addMember(SubjectFinder.findByIdAndSource(subjectId, "jdbc", true), false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
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
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, GROUP_NAME, true);
      return group.hasMember(SubjectFinder.findByIdAndSource(subjectId, "jdbc", true));
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
