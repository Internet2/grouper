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
package edu.internet2.middleware.grouper.esb.listener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.textui.TestRunner;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.HandlerWrapper;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SecurityHandler;
import org.quartz.Job;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;

/**
 * Security advisory H8 (ESB HTTP listener root membership changes): the experimental incoming
 * ESB HTTP listener adds and removes memberships of any group as GrouperSystem, so it must
 * never act on an unauthenticated request.
 * <p>
 * Before the fix: with no esb.listeners.http.authConfigFile the listener ran with no
 * authentication at all, and with one the Jetty SecurityHandler was a SIBLING of the
 * membership handler, so the membership change happened even when Basic auth failed.
 * After the fix: no auth file (or one that does not load) means the listener does not start,
 * and the membership handler is a child of the SecurityHandler (plus a guard that refuses a
 * request without an authenticated user).
 * </p>
 * The XMPP listener (same root membership path, sender checked by substring) is removed:
 * in patches EsbXmppListener is replaced by a class that refuses to start, on mainline it is
 * deleted.  testXmppListenerRemoved accepts either.
 * <p>
 * Before the fix this class does not compile (EsbHttpServer.buildHandler does not exist).
 * </p>
 */
public class EsbHttpServerTest extends GrouperTest {

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new EsbHttpServerTest("testUnauthenticatedRequestDoesNotChangeMembership"));
  }

  /**
   *
   */
  public EsbHttpServerTest() {
    super();
  }

  /**
   * @param name
   */
  public EsbHttpServerTest(String name) {
    super(name);
  }

  /** group the listener is asked to add the subject to */
  private static final String GROUP_NAME = "test:esbListenerProof";

  /** user and password in the realm file with the grouper role */
  private static final String GOOD_USER = "esbTestUser";

  /** password of the realm users */
  private static final String GOOD_PASS = "esbTestPass123";

  /** user in the realm file with no role, so the security constraint refuses it */
  private static final String NO_ROLE_USER = "esbNoRoleUser";

  /**
   * write a Jetty HashUserRealm file with a user that may call the listener and one that may not
   * @return the file
   * @throws IOException
   */
  private static File writeRealmFile() throws IOException {
    File realmFile = File.createTempFile("esbHttpServerTestRealm", ".properties");
    realmFile.deleteOnExit();
    Writer writer = new FileWriter(realmFile);
    try {
      writer.write(GOOD_USER + ": " + GOOD_PASS + ",grouper\n");
      writer.write(NO_ROLE_USER + ": " + GOOD_PASS + "\n");
    } finally {
      writer.close();
    }
    return realmFile;
  }

  /**
   * no auth file, a blank one, or one that does not load: the listener must not start
   */
  public void testBuildHandlerRequiresAuthFile() {
    assertNull(EsbHttpServer.buildHandler(null));
    assertNull(EsbHttpServer.buildHandler(""));
    assertNull(EsbHttpServer.buildHandler("   "));
    assertNull(EsbHttpServer.buildHandler(new File(System.getProperty("java.io.tmpdir"),
        "esbHttpServerTestDoesNotExist_" + System.currentTimeMillis() + ".properties").getAbsolutePath()));
  }

  /**
   * with a realm file the membership handler is wrapped by the security handler, not a sibling
   * @throws IOException
   */
  public void testHandlerChainIsWrapped() throws IOException {
    File realmFile = writeRealmFile();
    Handler handler = EsbHttpServer.buildHandler(realmFile.getAbsolutePath());
    assertNotNull(handler);
    assertTrue(handler.getClass().getName(), handler instanceof SecurityHandler);
    Handler guard = ((SecurityHandler)handler).getHandler();
    assertTrue(String.valueOf(guard), guard instanceof EsbHttpServer.RequireAuthenticatedUserHandler);
    Handler applicationHandler = ((HandlerWrapper)guard).getHandler();
    assertTrue(String.valueOf(applicationHandler), applicationHandler instanceof EsbHttpHandler);
  }

  /**
   * the guard and the security handler stop the request before the application handler, for
   * every method and path, and let a correctly authenticated user through
   * @throws Exception
   */
  public void testApplicationHandlerOnlyReachedWhenAuthenticated() throws Exception {
    File realmFile = writeRealmFile();
    final int[] calls = new int[] {0};
    Handler stub = new AbstractHandler() {

      public void handle(String target, HttpServletRequest request,
          HttpServletResponse response, int dispatch) throws IOException {
        calls[0]++;
        ((Request)request).setHandled(true);
        response.setStatus(HttpServletResponse.SC_OK);
      }
    };
    Server server = startServer(EsbHttpServer.buildHandler(realmFile.getAbsolutePath(), stub));
    try {
      int port = server.getConnectors()[0].getLocalPort();
      for (String method : new String[] {"POST", "PUT", "GET"}) {
        for (String path : new String[] {"/", "/x/y", "/;x"}) {
          assertTrue(post(port, method, path, null, "{}") >= 400);
          assertTrue(post(port, method, path, GOOD_USER + ":wrong", "{}") >= 400);
          assertTrue(post(port, method, path, NO_ROLE_USER + ":" + GOOD_PASS, "{}") >= 400);
          assertEquals(method + " " + path, 0, calls[0]);
        }
      }
      // control: a good user gets through
      assertEquals(200, post(port, "POST", "/", GOOD_USER + ":" + GOOD_PASS, "{}"));
      assertEquals(1, calls[0]);
    } finally {
      server.stop();
    }
  }

  /**
   * the security report's attack: an unauthenticated (or badly authenticated) POST must not add a
   * membership; check the membership directly, not the HTTP status.  A good user still can.
   * @throws Exception
   */
  public void testUnauthenticatedRequestDoesNotChangeMembership() throws Exception {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
    assertFalse(group.hasMember(SubjectTestHelper.SUBJ1));

    String event = "{\"esbListenerEvent\":[{\"subjectId\":\"" + SubjectTestHelper.SUBJ1_ID
        + "\",\"addMembershipGroups\":[[\"" + GROUP_NAME + "\",\"name\"]],\"removeMembershipGroups\":[]}]}";

    File realmFile = writeRealmFile();
    Server server = startServer(EsbHttpServer.buildHandler(realmFile.getAbsolutePath()));
    try {
      int port = server.getConnectors()[0].getLocalPort();

      assertEquals(401, post(port, "POST", "/", null, event));
      assertFalse(group.hasMember(SubjectTestHelper.SUBJ1));

      assertEquals(401, post(port, "POST", "/", GOOD_USER + ":wrong", event));
      assertFalse(group.hasMember(SubjectTestHelper.SUBJ1));

      assertTrue(post(port, "POST", "/", NO_ROLE_USER + ":" + GOOD_PASS, event) >= 400);
      assertFalse(group.hasMember(SubjectTestHelper.SUBJ1));

      // control: the configured, authenticated caller still works
      assertEquals(200, post(port, "POST", "/", GOOD_USER + ":" + GOOD_PASS, event));
      assertTrue(group.hasMember(SubjectTestHelper.SUBJ1));
    } finally {
      server.stop();
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the XMPP listener is removed: on mainline the class is gone, in patches it is a stub that
   * refuses to start (logs an error, connects to nothing, does not throw)
   * @throws Exception
   */
  public void testXmppListenerRemoved() throws Exception {
    Class<?> xmppListenerClass = null;
    try {
      xmppListenerClass = Class.forName("edu.internet2.middleware.grouper.esb.listener.EsbXmppListener");
    } catch (ClassNotFoundException cnfe) {
      // mainline: removed
      return;
    }
    // patch: the old class read the job data map and connected to the XMPP server, so a null
    // context threw; the stub only logs an error
    Job job = (Job)xmppListenerClass.getDeclaredConstructor().newInstance();
    job.execute(null);
    assertNotNull(xmppListenerClass.getDeclaredField("REMOVED_MESSAGE"));
  }

  /**
   * start a jetty server on a free loopback port
   * @param handler
   * @return the started server
   * @throws Exception
   */
  private static Server startServer(Handler handler) throws Exception {
    assertNotNull(handler);
    Server server = new Server();
    SelectChannelConnector connector = new SelectChannelConnector();
    connector.setHost("127.0.0.1");
    connector.setPort(0);
    server.addConnector(connector);
    server.setHandler(handler);
    server.start();
    return server;
  }

  /**
   * send a request with an optional Basic auth user:pass and return the status
   * @param port
   * @param method
   * @param path
   * @param userColonPass null for no Authorization header
   * @param body
   * @return http status
   * @throws IOException
   */
  private static int post(int port, String method, String path, String userColonPass, String body) throws IOException {
    HttpURLConnection connection = (HttpURLConnection)new URL("http://127.0.0.1:" + port + path).openConnection();
    try {
      connection.setRequestMethod(method);
      if (userColonPass != null) {
        connection.setRequestProperty("Authorization", "Basic "
            + Base64.getEncoder().encodeToString(userColonPass.getBytes("UTF-8")));
      }
      if (!"GET".equals(method)) {
        connection.setDoOutput(true);
        OutputStream outputStream = connection.getOutputStream();
        try {
          outputStream.write(body.getBytes("UTF-8"));
        } finally {
          outputStream.close();
        }
      }
      return connection.getResponseCode();
    } finally {
      connection.disconnect();
    }
  }
}
