package edu.internet2.middleware.grouper.app.messaging;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem;
import junit.textui.TestRunner;

/**
 * Tests that the messaging-to-WS bridge (MessageConsumerDaemon) only posts to the configured
 * Grouper WS REST endpoint, whatever grouperHeader.httpPath a queue message carries.  Before
 * the fix the path was concatenated onto ws.url, so "@otherHost:port/x" sent the bridge's
 * Basic credentials to another host.
 * <p>
 * The unit tests call MessageConsumerDaemonWsUrl directly (they do not compile before the fix).
 * The integration test runs the real daemon and GrouperHttpClient against two loopback HTTP
 * servers with dummy credentials.
 * </p>
 */
public class MessageConsumerDaemonWsUrlTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new MessageConsumerDaemonWsUrlTest("testIntegrationAuthoritySwitchRejected"));
  }

  /**
   * @param name
   */
  public MessageConsumerDaemonWsUrlTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- unit tests

  /**
   * normal base URL forms and REST paths keep working
   */
  public void testAllowedPaths() {
    assertEquals("http://h:8080/grouper-ws/servicesRest/v4_0_000/groups",
        MessageConsumerDaemonWsUrl.buildWsUrl("http://h:8080", "/grouper-ws/servicesRest/v4_0_000/groups"));
    assertEquals("http://h:8080/grouper-ws/servicesRest/v4_0_000/groups",
        MessageConsumerDaemonWsUrl.buildWsUrl("http://h:8080/grouper-ws", "/servicesRest/v4_0_000/groups"));
    // trailing slash on the base does not produce a double slash
    assertEquals("https://h/grouper-ws/servicesRest/json/v2_4_000/groups/a%3Ab/members",
        MessageConsumerDaemonWsUrl.buildWsUrl("https://h/grouper-ws/", "/servicesRest/json/v2_4_000/groups/a%3Ab/members"));
    // docker style host name with an underscore (URI.getHost() is null for it)
    assertEquals("http://grouper_ws:8080/grouper-ws/servicesRest/v4_0_000/groups",
        MessageConsumerDaemonWsUrl.buildWsUrl("http://grouper_ws:8080/grouper-ws", "/servicesRest/v4_0_000/groups"));
  }

  /**
   * paths that could change the host, leave the base path, or reach a non REST servlet
   */
  public void testRejectedPaths() {
    String base = "http://h:8080";
    String[] badPaths = new String[] {
        null,
        "",
        "@evil:8081/capture",
        "@evil:8081/servicesRest/x",
        "/@evil/servicesRest/x",
        "//evil/servicesRest/x",
        "http://evil/servicesRest/x",
        "servicesRest/x",
        "/servicesRest/../../x",
        "/servicesRest/./x",
        "/servicesRest/%2e%2e/x",
        "/servicesRest/%2E%2E/x",
        "/a%2F..%2Fservicesrest",
        "/servicesRest/a%2Fb",
        "/servicesRest/%40evil",
        "/servicesRest/%",
        "/servicesRest/%zz",
        "/servicesRest/..;/x",
        "/servicesRest/x#frag",
        "/servicesRest/x?y=1",
        "\\evil\\servicesRest",
        "/servicesRest/x\r\nHost: evil",
        "/servicesRest/x y",
        "/servicesRest/x%0d%0a",
        "/manager/html",
        "/grouper-ws/servicesrest/x",
    };
    for (String badPath : badPaths) {
      try {
        String url = MessageConsumerDaemonWsUrl.buildWsUrl(base, badPath);
        fail("path should be rejected: " + badPath + " -> " + url);
      } catch (IllegalArgumentException iae) {
        // expected; the message must not echo the untrusted path
        if (badPath != null && badPath.length() > 3) {
          assertFalse(iae.getMessage(), iae.getMessage().contains(badPath));
        }
      }
    }
  }

  /**
   * bad configured base URLs fail closed
   */
  public void testRejectedBaseUrls() {
    String path = "/grouper-ws/servicesRest/v4_0_000/groups";
    String[] badBases = new String[] {
        null, "", "   ", "h:8080", "ftp://h/x", "http://u:p@h:8080", "http://h:8080/x?y=1", "http:///x", "not a url"
    };
    for (String badBase : badBases) {
      try {
        String url = MessageConsumerDaemonWsUrl.buildWsUrl(badBase, path);
        fail("base should be rejected: " + badBase + " -> " + url);
      } catch (IllegalArgumentException iae) {
        // expected; never echo the base (it might hold user info)
        if (badBase != null && badBase.trim().length() > 0) {
          assertFalse(iae.getMessage(), iae.getMessage().contains(badBase));
        }
      }
    }
  }

  // ---------------------------------------------------------------- integration test

  /**
   * the reproduction from the security report: the attacker server must get nothing, and the
   * intended server must get the normal request with the credential
   * @throws Exception
   */
  public void testIntegrationAuthoritySwitchRejected() throws Exception {

    final AtomicInteger intendedCalls = new AtomicInteger();
    final AtomicReference<String> intendedAuth = new AtomicReference<String>();
    final AtomicInteger attackerCalls = new AtomicInteger();

    HttpServer intended = startServer(intendedCalls, intendedAuth);
    HttpServer attacker = startServer(attackerCalls, new AtomicReference<String>());

    String prefix = "grouper.messaging.ssrfTest82.";
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "ws.url",
        "http://127.0.0.1:" + intended.getAddress().getPort());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "ws.username", "dummyBridge");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "ws.password", "dummyPassword");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "actAsSubjectSourceId", "g:isa");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "actAsSubjectId", "GrouperSystem");

    try {
      // attack: authority switch to the attacker server
      ReplyCapture replyCapture = new ReplyCapture();
      new MessageConsumerDaemon().processMessages("fakeMessagingSystem", replyCapture, "queue", "test-queue-name",
          messages("@127.0.0.1:" + attacker.getAddress().getPort() + "/capture"), "ssrfTest82");

      assertEquals("attacker server must not be contacted", 0, attackerCalls.get());
      assertEquals("intended server must not be contacted for a rejected path", 0, intendedCalls.get());
      assertNotNull("an error reply is sent", replyCapture.body);
      assertTrue(replyCapture.body, replyCapture.body.contains("EXCEPTION"));
      assertFalse(replyCapture.body, replyCapture.body.contains("dummyPassword"));

      // normal REST path still reaches only the intended server, with the credential
      replyCapture = new ReplyCapture();
      new MessageConsumerDaemon().processMessages("fakeMessagingSystem", replyCapture, "queue", "test-queue-name",
          messages("/grouper-ws/servicesRest/json/v2_4_000/groups"), "ssrfTest82");

      assertEquals(0, attackerCalls.get());
      assertEquals(1, intendedCalls.get());
      assertNotNull(intendedAuth.get());
      assertTrue(intendedAuth.get(), intendedAuth.get().startsWith("Basic "));
    } finally {
      intended.stop(0);
      attacker.stop(0);
      for (String suffix : new String[] {"ws.url", "ws.username", "ws.password", "actAsSubjectSourceId", "actAsSubjectId"}) {
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove(prefix + suffix);
      }
    }
  }

  /**
   * @param httpPath
   * @return one bridge message with that path
   */
  private static Collection<GrouperMessage> messages(String httpPath) {
    String body = "{\"grouperHeader\": "
        + "{ \"messageVersion\": \"1\","
        + "  \"timestampInput\": \"2026-11-02T19:00:00.000Z\","
        + "  \"type\": \"grouperMessagingToWebService\","
        + "  \"endpoint\": \"WsRestFindGroupsRequest\","
        + "  \"messageInputUuid\": \"ssrf-test\","
        + "  \"replyToQueueOrTopicName\": \"someQueue\","
        + "  \"replyToQueueOrTopic\": \"queue\","
        + "  \"httpMethod\": \"POST\","
        + "  \"httpPath\": \"" + httpPath + "\""
        + "},"
        + "\"WsRestFindGroupsRequest\":{} }";
    Collection<GrouperMessage> result = new ArrayList<GrouperMessage>();
    result.add(new SimpleMessage(body));
    return result;
  }

  /**
   * loopback server that counts calls, records the Authorization header and answers like WS
   * @param calls
   * @param auth
   * @return the started server
   * @throws IOException
   */
  private static HttpServer startServer(final AtomicInteger calls, final AtomicReference<String> auth) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", new HttpHandler() {

      @Override
      public void handle(HttpExchange exchange) throws IOException {
        calls.incrementAndGet();
        auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
        byte[] responseBytes = "\"result\": {\"success\": true }".getBytes("UTF-8");
        exchange.getResponseHeaders().add("X-Grouper-success", "T");
        exchange.getResponseHeaders().add("X-Grouper-resultCode", "SUCCESS");
        exchange.getResponseHeaders().add("X-Grouper-resultCode2", "NONE");
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.close();
      }
    });
    server.start();
    return server;
  }

  /**
   * a queue message
   */
  private static class SimpleMessage implements GrouperMessage {

    /** body */
    private String body;

    /**
     * @param body1
     */
    SimpleMessage(String body1) {
      this.body = body1;
    }

    @Override
    public String getFromMemberId() {
      return null;
    }

    @Override
    public void setFromMemberId(String fromMemberId1) {
    }

    @Override
    public String getId() {
      return null;
    }

    @Override
    public void setId(String id1) {
    }

    @Override
    public String getMessageBody() {
      return this.body;
    }

    @Override
    public void setMessageBody(String message1) {
      this.body = message1;
    }
  }

  /**
   * messaging system that records the reply message
   */
  private static class ReplyCapture implements GrouperMessagingSystem {

    /** last reply body */
    private String body;

    @Override
    public GrouperMessageSendResult send(GrouperMessageSendParam grouperMessageSendParam) {
      this.body = grouperMessageSendParam.getGrouperMessages().iterator().next().getMessageBody();
      return null;
    }

    @Override
    public GrouperMessageAcknowledgeResult acknowledge(GrouperMessageAcknowledgeParam grouperMessageAcknowledgeParam) {
      return null;
    }

    @Override
    public GrouperMessageReceiveResult receive(GrouperMessageReceiveParam grouperMessageReceiveParam) {
      return null;
    }
  }
}
