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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Server;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory H8 (esbHttpListenerRootMembershipAuthorization), real-HTTP check of the ESB HTTP
 * listener started the way the daemon starts it.  EsbHttpServerTest (JUnit) checks buildHandler
 * and the handler chain on a Jetty server the test builds itself; this test covers what it cannot:
 * GrouperLoader.scheduleEsbListenerJobs reading esb.listeners.http.* from grouper-loader config,
 * Quartz running the EsbHttpServer job, and EsbHttpServer.startServer (the private method that
 * calls buildHandler and binds the port).
 *
 * <p>How: the test points GrouperLoader's scheduler factory (a private static field) at a private
 * in-memory Quartz scheduler for the duration of the test, so the job runs in this JVM and nothing
 * is written to the shared, clustered grouper_QZ_ tables (the local Tomcat daemon must never see
 * this job).  Config is set with GrouperLoaderConfig's override map, which only lives in this
 * JVM.  The listener binds a free loopback port.</p>
 *
 * <p>Steps: (1) no authConfigFile: the job runs and returns (before the fix it started Jetty and
 * blocked in join), and the port refuses connections.  (2) with a temporary realm file: POST of
 * the security report's JSON event with no credentials and with a wrong password gets 401 and the
 * subject is not added; with the realm user's credentials it gets 200 and the subject is added.</p>
 *
 * <p>Stopping the listener: startServer keeps the Jetty Server in a local variable and blocks in
 * join, so the test captures it from a membership hook that runs on the listener's request thread
 * (Jetty's HttpConnection.getCurrentConnection().getServer()).  The hook only acts for this test's
 * group while the test is capturing.  That also proves the accepted event was processed by this
 * listener.</p>
 *
 * <p>Extends TestCase, not GrouperTest (GrouperTest resets the database the running
 * Tomcat shares).  Skipped unless junit.test.tomcat=true, like the other live tests, although it
 * does not call Tomcat.  Creates and deletes folder secPatch35 and a temp realm file.  Same file
 * for every release.  The XMPP part of the fix (mainline only) is not covered here.</p>
 */
public class EsbHttpListenerDaemonHttpTest extends TestCase {

  /** folder this test creates and deletes */
  private static final String STEM_NAME = "secPatch35";

  /** group the ESB event adds the subject to */
  private static final String GROUP_NAME = STEM_NAME + ":esbListenerProof";

  /** realm user with the grouper role */
  private static final String GOOD_USER = "secPatch35esbUser";

  /** password of the realm user (test value, only in a temp file) */
  private static final String GOOD_PASS = "secPatch35pass";

  /** timeout for waits, ms */
  private static final long TIMEOUT_MILLIS = 15000;

  /** config keys this test overrides (removed in finally) */
  private static final String[] CONFIG_KEYS = {
      "esb.listeners.http.enable", "esb.listeners.http.port", "esb.listeners.http.bindaddress",
      "esb.listeners.http.authConfigFile", "esb.listeners.http.ssl.keystore",
      "esb.listeners.xmpp.enable"};

  /** set by the test while the hook should capture the listener's Jetty server */
  private static volatile boolean capturing = false;

  /** Jetty server of the listener, captured on its request thread by the hook */
  private static volatile Server capturedServer = null;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(EsbHttpListenerDaemonHttpTest.class);
    // Grouper starts non-daemon background threads; without this the JVM never exits
    System.exit(0);
  }

  /**
   *
   */
  public EsbHttpListenerDaemonHttpTest() {
    super();
  }

  /**
   * @param name
   */
  public EsbHttpListenerDaemonHttpTest(String name) {
    super(name);
  }

  /**
   * Membership hook that records the Jetty server whose request thread added a member to this
   * test's group.  Does nothing otherwise (hooks cannot be unregistered, so it stays inert).
   */
  public static class CaptureListenerServerHook extends MembershipHooks {

    /**
     * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPostAddMember(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean)
     */
    @Override
    public void membershipPostAddMember(HooksContext hooksContext,
        HooksMembershipChangeBean postAddMemberBean) {
      if (!capturing || postAddMemberBean.getGroup() == null
          || !GROUP_NAME.equals(postAddMemberBean.getGroup().getName())) {
        return;
      }
      // only set on a Jetty request thread, i.e. when the add came through the listener
      HttpConnection httpConnection = HttpConnection.getCurrentConnection();
      if (httpConnection != null) {
        capturedServer = httpConnection.getConnector().getServer();
      }
    }
  }

  /**
   * Quartz job listener that counts down when the ESB HTTP listener job returns
   */
  private static class JobDoneListener implements JobListener {

    /** counted down when the job has run and returned */
    private final CountDownLatch done = new CountDownLatch(1);

    /** counted down when the job starts */
    private final CountDownLatch started = new CountDownLatch(1);

    public String getName() {
      return "secPatch35JobDoneListener";
    }

    public void jobToBeExecuted(JobExecutionContext context) {
      this.started.countDown();
    }

    public void jobExecutionVetoed(JobExecutionContext context) {
    }

    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
      this.done.countDown();
    }
  }

  /**
   * listener started by the daemon code: refuses to start without an auth file; with one, needs
   * Basic auth before any membership change
   * @throws Exception
   */
  public void testDaemonStartedListenerRequiresAuthentication() throws Exception {
    if (!GrouperTest.tomcatRunTests()) {
      return;
    }

    GrouperHooksUtils.addHookManual("hooks.membership.class", CaptureListenerServerHook.class);

    Field schedulerFactoryField = GrouperLoader.class.getDeclaredField("schedulerFactory");
    schedulerFactoryField.setAccessible(true);
    Object originalSchedulerFactory = schedulerFactoryField.get(null);

    Scheduler scheduler = null;
    File realmFile = null;
    int port = freeLoopbackPort();
    String event = "{\"esbListenerEvent\":[{\"subjectId\":\"" + SubjectTestHelper.SUBJ1_ID
        + "\",\"addMembershipGroups\":[[\"" + GROUP_NAME + "\",\"name\"]],\"removeMembershipGroups\":[]}]}";

    try {
      deleteFixtures();
      createGroup();

      // private in-memory scheduler, so the job is not stored in the shared clustered Quartz tables
      Properties quartzProperties = new Properties();
      quartzProperties.setProperty("org.quartz.scheduler.instanceName", "secPatch35Scheduler");
      quartzProperties.setProperty("org.quartz.scheduler.instanceId", "secPatch35");
      quartzProperties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
      quartzProperties.setProperty("org.quartz.threadPool.threadCount", "2");
      quartzProperties.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
      SchedulerFactory testSchedulerFactory = new StdSchedulerFactory(quartzProperties);
      scheduler = testSchedulerFactory.getScheduler();
      schedulerFactoryField.set(null, testSchedulerFactory);
      scheduler.start();

      setConfig("esb.listeners.http.enable", "true");
      setConfig("esb.listeners.http.port", Integer.toString(port));
      setConfig("esb.listeners.http.bindaddress", "127.0.0.1");
      setConfig("esb.listeners.http.ssl.keystore", "");
      setConfig("esb.listeners.xmpp.enable", "false");

      // ---------- step 1: no authConfigFile, the listener must not start
      setConfig("esb.listeners.http.authConfigFile", "");
      JobDoneListener firstRun = new JobDoneListener();
      scheduler.getListenerManager().addJobListener(firstRun);
      GrouperLoader.scheduleEsbListenerJobs();
      // the trigger fires 5 s after scheduling; before the fix the job never returned (join)
      assertTrue("ESB HTTP listener job did not run and return without an authConfigFile "
          + "(before the fix it started the listener and blocked)",
          firstRun.done.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));
      scheduler.getListenerManager().removeJobListener(firstRun.getName());
      assertFalse("listener is accepting connections on 127.0.0.1:" + port + " without an authConfigFile",
          portAcceptsConnections(port));
      assertFalse(isMember());

      // ---------- step 2: with a realm file, Basic auth is required before any change
      realmFile = File.createTempFile("secPatch35EsbRealm", ".properties");
      Writer writer = new FileWriter(realmFile);
      try {
        writer.write(GOOD_USER + ": " + GOOD_PASS + ",grouper\n");
      } finally {
        writer.close();
      }
      setConfig("esb.listeners.http.authConfigFile", realmFile.getAbsolutePath());
      JobDoneListener secondRun = new JobDoneListener();
      scheduler.getListenerManager().addJobListener(secondRun);
      GrouperLoader.scheduleEsbListenerJobs();
      assertTrue("ESB HTTP listener job did not start", secondRun.started.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));
      waitForPort(port);
      assertFalse("job returned, so the listener did not keep running", secondRun.done.getCount() == 0);

      capturing = true;

      assertEquals(401, post(port, null, event));
      assertFalse("unauthenticated POST changed membership", isMember());

      assertEquals(401, post(port, GOOD_USER + ":wrong", event));
      assertFalse("POST with a wrong password changed membership", isMember());

      // control: the configured realm user's event is processed
      assertEquals(200, post(port, GOOD_USER + ":" + GOOD_PASS, event));
      assertTrue("authenticated event was not processed", isMember());
      assertNotNull("membership add did not run on the listener's request thread", capturedServer);

    } finally {
      try {
        stopListener(port, event);
      } finally {
        capturing = false;
        if (scheduler != null) {
          try {
            scheduler.shutdown(false);
          } catch (Exception e) {
            // keep cleaning up
          }
        }
        schedulerFactoryField.set(null, originalSchedulerFactory);
        for (String key : CONFIG_KEYS) {
          GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove(key);
        }
        if (realmFile != null && !realmFile.delete()) {
          realmFile.deleteOnExit();
        }
        deleteFixtures();
      }
    }
  }

  /**
   * stop the listener started by the job.  If the hook has not captured its Jetty server yet
   * (e.g. a failure before the authenticated POST, or the negative control where the listener
   * started without authentication), send the event once so the hook captures it.
   * @param port
   * @param event
   * @throws Exception
   */
  private static void stopListener(int port, String event) throws Exception {
    if (capturedServer == null && portAcceptsConnections(port)) {
      capturing = true;
      try {
        post(port, GOOD_USER + ":" + GOOD_PASS, event);
      } catch (IOException ioe) {
        // best effort
      }
    }
    Server server = capturedServer;
    capturedServer = null;
    if (server != null) {
      server.stop();
      long start = System.currentTimeMillis();
      while (portAcceptsConnections(port) && System.currentTimeMillis() - start < TIMEOUT_MILLIS) {
        Thread.sleep(200);
      }
      assertFalse("listener still running after stop", portAcceptsConnections(port));
    } else if (portAcceptsConnections(port)) {
      fail("could not stop the ESB HTTP listener on 127.0.0.1:" + port + " (it stops when this JVM exits)");
    }
  }

  /**
   * set a grouper-loader config value in this JVM only
   * @param key
   * @param value
   */
  private static void setConfig(String key, String value) {
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(key, value);
  }

  /**
   * create the test group (and the secPatch35 folder)
   * @return the group
   */
  private static Group createGroup() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      return new GroupSave(rootSession).assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * @return true if SUBJ1 is a member of the test group (read fresh from the database)
   */
  private static boolean isMember() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      return GroupFinder.findByName(rootSession, GROUP_NAME, true).hasMember(SubjectTestHelper.SUBJ1);
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

  /**
   * @return a loopback port nothing listens on right now
   * @throws IOException
   */
  private static int freeLoopbackPort() throws IOException {
    ServerSocket serverSocket = new ServerSocket();
    try {
      serverSocket.bind(new InetSocketAddress("127.0.0.1", 0));
      return serverSocket.getLocalPort();
    } finally {
      serverSocket.close();
    }
  }

  /**
   * @param port
   * @return true if something accepts a TCP connection on 127.0.0.1:port
   */
  private static boolean portAcceptsConnections(int port) {
    Socket socket = new Socket();
    try {
      socket.connect(new InetSocketAddress("127.0.0.1", port), 2000);
      return true;
    } catch (ConnectException ce) {
      return false;
    } catch (IOException ioe) {
      return false;
    } finally {
      try {
        socket.close();
      } catch (IOException ioe) {
        // ignore
      }
    }
  }

  /**
   * wait until the listener accepts connections
   * @param port
   * @throws InterruptedException
   */
  private static void waitForPort(int port) throws InterruptedException {
    long start = System.currentTimeMillis();
    while (!portAcceptsConnections(port)) {
      if (System.currentTimeMillis() - start > TIMEOUT_MILLIS) {
        fail("ESB HTTP listener did not start on 127.0.0.1:" + port);
      }
      Thread.sleep(200);
    }
  }

  /**
   * POST the event with an optional Basic auth user:pass
   * @param port
   * @param userColonPass null for no Authorization header
   * @param body
   * @return http status
   * @throws IOException
   */
  private static int post(int port, String userColonPass, String body) throws IOException {
    HttpURLConnection connection = (HttpURLConnection)new URL("http://127.0.0.1:" + port + "/").openConnection();
    try {
      connection.setConnectTimeout((int)TIMEOUT_MILLIS);
      connection.setReadTimeout((int)TIMEOUT_MILLIS);
      connection.setRequestMethod("POST");
      if (userColonPass != null) {
        connection.setRequestProperty("Authorization", "Basic "
            + Base64.getEncoder().encodeToString(userColonPass.getBytes("UTF-8")));
      }
      connection.setDoOutput(true);
      OutputStream outputStream = connection.getOutputStream();
      try {
        outputStream.write(body.getBytes("UTF-8"));
      } finally {
        outputStream.close();
      }
      int status = connection.getResponseCode();
      // drain so the connection closes cleanly
      InputStream inputStream = status < 400 ? connection.getInputStream() : connection.getErrorStream();
      if (inputStream != null) {
        try {
          while (inputStream.read() >= 0) {
            // drain
          }
        } finally {
          inputStream.close();
        }
      }
      return status;
    } finally {
      connection.disconnect();
    }
  }
}
