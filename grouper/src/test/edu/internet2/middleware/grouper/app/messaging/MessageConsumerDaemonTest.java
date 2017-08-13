package edu.internet2.middleware.grouper.app.messaging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.util.HttpURLConnection;
import junit.framework.TestCase;
import net.sf.json.JSONObject;

/**
 * @author vsachdeva
 */
public class MessageConsumerDaemonTest extends TestCase {
  
  private boolean webServiceCalled;
  private boolean replyToSendCalled;
  private String replyToBody;
  
  /**
   * @param name
   */
  public MessageConsumerDaemonTest(String name) {    
    super(name);
  }
  
  private String validMessage = "{\"grouperHeader\": "
      + "{ \"messageVersion\": \"1\","
      + "  \"timestampInput\": \"2017-07-23T18:25:43.511Z\","
      + "  \"type\": \"grouperMessagingToWebService\","
      + "  \"endpoint\": \"WsRestAddMemberRequest\","
      + "  \"messageInputUuid\": \"abc123\","
      + "  \"replyToQueueOrTopicName\": \"someQueue\","
      + "  \"replyToQueueOrTopic\": \"queue\","
      + "  \"httpMethod\": \"PUT\","
      + "  \"httpPath\": \"http://localhost:8085/test123\""
      + "},"
      + "\"WsRestAddMemberRequest\":{ \"subjectLookups\":[{"
      + "  \"subjectId\":\"test.subject.0\","
      + "  \"subjectSourceId\":\"jdbc\" }] ,"
      + "   \"wsGroupLookup\":{ \"groupName\":\"test:testGroup\" }  }}";
  
  public void testProcessMessages() throws IOException {
    MessageConsumerDaemon daemon = new MessageConsumerDaemon();
    FakeGrouperMessageSystem grouperMessageSystem = new FakeGrouperMessageSystem();
    
    Collection<GrouperMessage> grouperMessages = new ArrayList<GrouperMessage>();
    GrouperMessage message = new FakeGrouperMessage(validMessage);
    grouperMessages.add(message);
    
    FakeHttpServer httpServer = new FakeHttpServer();
    httpServer.launchHttpServer();
    
    daemon.processMessages("fakeMessagingSystem", grouperMessageSystem, grouperMessages);
    
    httpServer.stopHttpServer();
    
    assertTrue(replyToSendCalled);
    assertTrue(webServiceCalled);
    System.out.println(JSONObject.fromObject(replyToBody).toString());
    assertEquals(JSONObject.fromObject(replyToBody).getString("result"), "{\"success\":true}");
  }
  
  class FakeHttpServer {
    
    HttpServer httpServer = null;
    
    void launchHttpServer() throws IOException {
      httpServer = HttpServer.create(new InetSocketAddress(8085), 0);
      httpServer.createContext("/test123", new HttpHandler() {
        public void handle(HttpExchange exchange) throws IOException {
          webServiceCalled = true;
          byte[] response = "\"result\": {\"success\": true }".getBytes();
          exchange.getResponseHeaders().set("X-Grouper-success", "T");
          exchange.getResponseHeaders().set("X-Grouper-resultCode", "SUCCESS");
          exchange.getResponseHeaders().set("X-Grouper-resultCode2", "NONE");
          exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
          exchange.getResponseBody().write(response);
          exchange.close();
        }
      });
      httpServer.start();
    }
    
    void stopHttpServer() {
       httpServer.stop(0);
       httpServer = null;
    }
    
  }
  
  class FakeGrouperMessage implements GrouperMessage {
    
    private String body;
    
    public FakeGrouperMessage(String body) {
      this.body = body;
    }
   
    @Override
    public String getFromMemberId() {
      return null;
    }

    @Override
    public void setFromMemberId(String fromMemberId1) {}

    @Override
    public String getId() {
      return null;
    }

    @Override
    public void setId(String id1) {}

    @Override
    public String getMessageBody() {
      return body;
    }

    @Override
    public void setMessageBody(String message1) {
      this.body = message1;
    }
    
  }
  
  class FakeGrouperMessageSystem implements GrouperMessagingSystem {

    @Override
    public GrouperMessageSendResult send(GrouperMessageSendParam grouperMessageSendParam) {
       replyToSendCalled = true;
       replyToBody = grouperMessageSendParam.getGrouperMessages().iterator().next().getMessageBody();
       return null;
    }

    @Override
    public GrouperMessageAcknowledgeResult acknowledge(
        GrouperMessageAcknowledgeParam grouperMessageAcknowledgeParam) {
      return null;
    }

    @Override
    public GrouperMessageReceiveResult receive(
        GrouperMessageReceiveParam grouperMessageReceiveParam) {
      return null;
    }
    
  }
  
}



