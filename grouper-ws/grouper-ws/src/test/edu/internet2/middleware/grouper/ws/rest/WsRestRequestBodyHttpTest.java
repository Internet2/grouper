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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.framework.TestCase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfigInApi;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory M7 (wsRestMalformedJsonResourceExhaustion), HTTP check against the running WS:
 * GrouperRestServlet reads the request body with a size limit (ws.restMaxRequestBodyBytes) and
 * answers HTTP 413 over it, and a body that does not parse is HTTP 400 INVALID_QUERY with a short
 * message that does not hold the body.  A valid small request still works.
 * WsRestRequestBodyReaderTest uses a proxy request; this test checks the servlet wiring.
 *
 * <p>The size limit is checked with the default limit (no global config change, other tests use
 * the same WS): a raw socket request declares a Content-Length one byte over the limit and sends
 * only a small body.  The fixed servlet refuses it from the declared length without reading;
 * the unfixed servlet waits for a body that never comes (the test fails on the 15 s timeout).</p>
 *
 * <p>Runs against the running local WS (Tomcat), so it is skipped unless grouper.properties has
 * junit.test.tomcat=true.  It extends TestCase, not GrouperTest: GrouperTest resets the
 * database, and the running WS uses the same database.  Setup and cleanup are this test's own:
 * folder test:secPatch114 (deleted at the end), and test.subject.2 is added to
 * ws.client.user.group.name (only if it was not a member, and removed again at the end).
 * The WS log line (one WARN, no body) goes to the Tomcat console and is not checked here.</p>
 *
 * <p>Uses HttpURLConnection, Socket and Java 8 APIs only, so the same file works on every release.</p>
 */
public class WsRestRequestBodyHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "test:secPatch114";

  /** group the valid request reads */
  private static final String GROUP_NAME = STEM_NAME + ":group";

  /** WS caller, has UPDATE and READ on the group */
  private static final String CALLER_ID = "test.subject.2";

  /** local dev password of the test subjects */
  private static final String PASSWORD = "pass";

  /** the config key and default of the fix (WsRestRequestBodyReader) */
  private static final String CONFIG_MAX_REQUEST_BODY_BYTES = "ws.restMaxRequestBodyBytes";

  /** default of ws.restMaxRequestBodyBytes */
  private static final int DEFAULT_MAX_REQUEST_BODY_BYTES = 100000000;

  /** marker in the malformed body, must not come back in the response */
  private static final String MARKER = "SECPATCH114MARKER";

  /** subjects this test added to ws.client.user.group.name, removed in finally */
  private final List<Subject> addedToWsClients = new ArrayList<Subject>();

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(WsRestRequestBodyHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public WsRestRequestBodyHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public WsRestRequestBodyHttpTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- tests

  /**
   * the manual test step 1: a body over the limit is HTTP 413 with a short message naming the limit
   * @throws Exception
   */
  public void testBodyOverLimitIs413() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    try {
      setupGroup();
      admitToWs(CALLER_ID);
      waitForWsAdmission(CALLER_ID);

      // the limit the WS uses: the same config the test JVM sees (local file + database), else the default
      int limit = GrouperWsConfigInApi.retrieveConfig().propertyValueInt(CONFIG_MAX_REQUEST_BODY_BYTES,
          DEFAULT_MAX_REQUEST_BODY_BYTES);
      assertTrue("this test needs a positive " + CONFIG_MAX_REQUEST_BODY_BYTES, limit > 0);

      String smallBody = findGroupsBody();
      String rawResponse = rawPostDeclaredLength("/servicesRest/v2_4_000/groups", CALLER_ID,
          (long) limit + 1, smallBody);

      assertTrue("over the limit must be HTTP 413: " + rawResponse,
          rawResponse.startsWith("HTTP/1.1 413") || rawResponse.startsWith("HTTP/1.0 413"));
      assertTrue("message says too large: " + rawResponse, rawResponse.contains("too large"));
      assertTrue("message names the limit: " + rawResponse, rawResponse.contains(Integer.toString(limit)));
      assertTrue("message names the setting: " + rawResponse, rawResponse.contains(CONFIG_MAX_REQUEST_BODY_BYTES));
      assertTrue("result code INVALID_QUERY: " + rawResponse, rawResponse.contains("\"resultCode\":\"INVALID_QUERY\""));
      assertFalse("no stack trace in the response: " + rawResponse, rawResponse.contains("\\tat edu."));
    } finally {
      cleanup();
    }
  }

  /**
   * the manual test step 2: a body that does not parse is HTTP 400 INVALID_QUERY, the body is not
   * in the response
   * @throws Exception
   */
  public void testMalformedBodyIs400WithoutBody() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    try {
      setupGroup();
      admitToWs(CALLER_ID);
      waitForWsAdmission(CALLER_ID);

      // about 500 bytes, not json after the first key
      StringBuilder malformed = new StringBuilder("{\"WsRestFindGroupsRequest\": " + MARKER + " ");
      while (malformed.length() < 500) {
        malformed.append(MARKER).append(" xyz ");
      }

      WsResponse response = call("POST", wsBaseUrl() + "/servicesRest/v2_4_000/groups", CALLER_ID,
          "application/json", malformed.toString());
      assertEquals("malformed body must be HTTP 400: " + response, 400, response.status);
      assertEquals(response.toString(), "INVALID_QUERY", resultCode(response.body));
      assertTrue("short parse message: " + response, response.body.contains("Could not parse the request body"));
      assertFalse("the body must not come back in the response: " + response, response.body.contains(MARKER));
      assertFalse("no stack trace in the response: " + response, response.body.contains("\\tat "));
      assertTrue("response is short: " + response, response.body.length() < 3000);
    } finally {
      cleanup();
    }
  }

  /**
   * the manual test step 3: a valid small request works as before
   * @throws Exception
   */
  public void testValidSmallRequestWorks() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }
    try {
      setupGroup();
      admitToWs(CALLER_ID);
      waitForWsAdmission(CALLER_ID);

      WsResponse response = call("POST", wsBaseUrl() + "/servicesRest/v2_4_000/groups", CALLER_ID,
          "application/json", "{\"WsRestGetMembersRequest\":{\"wsGroupLookups\":[{\"groupName\":\""
          + GROUP_NAME + "\"}]}}");
      assertEquals("valid request must be HTTP 200: " + response, 200, response.status);
      assertEquals(response.toString(), "T", success(response.body));
      assertTrue(response.toString(), response.body.contains("WsGetMembersResults"));
    } finally {
      cleanup();
    }
  }

  // ---------------------------------------------------------------- raw socket

  /**
   * @return a small valid find groups body
   */
  private static String findGroupsBody() {
    return "{\"WsRestFindGroupsRequest\":{\"wsQueryFilter\":{\"queryFilterType\":"
        + "\"FIND_BY_GROUP_NAME_EXACT\",\"groupName\":\"" + GROUP_NAME + "\"}}}";
  }

  /**
   * POST with a declared Content-Length larger than what is sent (HttpURLConnection cannot do
   * that), then read what the server answers within 15 s
   * @param path after /grouper-ws
   * @param user subject id, password is PASSWORD
   * @param declaredLength Content-Length header value
   * @param body what is actually sent
   * @return the raw response (status line, headers, body), or fails on timeout without a response
   * @throws IOException
   */
  private static String rawPostDeclaredLength(String path, String user, long declaredLength, String body)
      throws IOException {
    GrouperConfig config = GrouperConfig.retrieveConfig();
    assertFalse("raw socket test is plain http only", config.propertyValueBoolean("junit.test.tomcat.ssl", false));
    String domainName = config.propertyValueString("junit.test.tomcat.domainName", "localhost");
    int port = config.propertyValueInt("junit.test.tomcat.port", 8080);

    Socket socket = new Socket();
    try {
      socket.connect(new InetSocketAddress(domainName, port), 15000);
      socket.setSoTimeout(15000);
      String credentials = user + ":" + PASSWORD;
      String request = "POST /grouper-ws" + path + " HTTP/1.1\r\n"
          + "Host: " + domainName + ":" + port + "\r\n"
          + "Authorization: Basic " + java.util.Base64.getEncoder().encodeToString(credentials.getBytes("UTF-8")) + "\r\n"
          + "Content-Type: application/json\r\n"
          + "Content-Length: " + declaredLength + "\r\n"
          + "Connection: close\r\n"
          + "\r\n"
          + body;
      OutputStream outputStream = socket.getOutputStream();
      outputStream.write(request.getBytes("UTF-8"));
      outputStream.flush();

      // read until the server closes or stops sending; keep what came before a timeout
      InputStream inputStream = socket.getInputStream();
      ByteArrayOutputStream result = new ByteArrayOutputStream();
      byte[] buffer = new byte[8192];
      try {
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
          result.write(buffer, 0, read);
          if (completeResponse(result)) {
            break;
          }
        }
      } catch (SocketTimeoutException ste) {
        if (result.size() == 0) {
          fail("no response within 15 s: the WS is waiting to read the whole declared body ("
              + declaredLength + " bytes) instead of refusing it from the Content-Length");
        }
      } catch (IOException ioe) {
        // the server may reset the connection after answering (unread body); keep what came
        if (result.size() == 0) {
          throw ioe;
        }
      }
      return new String(result.toByteArray(), "UTF-8");
    } finally {
      socket.close();
    }
  }

  /**
   * @param result bytes read so far
   * @return true if the headers and a final chunk or the full Content-Length body are there
   * @throws IOException
   */
  private static boolean completeResponse(ByteArrayOutputStream result) throws IOException {
    String soFar = new String(result.toByteArray(), "UTF-8");
    int headerEnd = soFar.indexOf("\r\n\r\n");
    if (headerEnd < 0) {
      return false;
    }
    String headers = soFar.substring(0, headerEnd).toLowerCase();
    if (headers.contains("transfer-encoding: chunked")) {
      return soFar.endsWith("\r\n0\r\n\r\n");
    }
    Matcher matcher = Pattern.compile("content-length: *([0-9]+)").matcher(headers);
    if (matcher.find()) {
      return result.size() - (headerEnd + 4) >= Integer.parseInt(matcher.group(1));
    }
    return false;
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
