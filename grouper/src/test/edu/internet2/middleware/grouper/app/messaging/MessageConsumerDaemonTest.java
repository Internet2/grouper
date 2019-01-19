package edu.internet2.middleware.grouper.app.messaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;

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
import net.sf.json.JSONObject;

/**
 * @author vsachdeva
 */
public class MessageConsumerDaemonTest extends GrouperTest {
  
  private boolean webServiceCalled;
  private boolean replyToSendCalled;
  private String replyToBody;
  
  /**
   * @param name
   */
  public MessageConsumerDaemonTest(String name) {    
    super(name);
  }
  
  protected void setUp () {
    super.setUp();

  }

  protected void tearDown () {
    super.tearDown();
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
      + "  \"httpPath\": \"/test123\""
      + "},"
      + "\"WsRestAddMemberRequest\":{ \"subjectLookups\":[{"
      + "  \"subjectId\":\"test.subject.0\","
      + "  \"subjectSourceId\":\"jdbc\" }] ,"
      + "   \"wsGroupLookup\":{ \"groupName\":\"test:testGroup\" }  }}";
  
  private String inValidMessage = "{\"grouperHeader\": "
      + "{ \"messageVersion\": null,"
      + "  \"timestampInput\": \"2017-07-23T18:25:43.511Z\","
      + "  \"type\": \"grouperMessagingToWebService\","
      + "  \"endpoint\": \"WsRestAddMemberRequest\","
      + "  \"messageInputUuid\": \"abc123\","
      + "  \"replyToQueueOrTopicName\": \"someQueue\","
      + "  \"replyToQueueOrTopic\": \"queue\","
      + "  \"httpMethod\": \"PUT\","
      + "  \"httpPath\": \" \""
      + "},"
      + "\"WsRestAddMemberRequest\":{ \"subjectLookups\":[{"
      + "  \"subjectId\":\"test.subject.0\","
      + "  \"subjectSourceId\":\"jdbc\" }] ,"
      + "   \"wsGroupLookup\":{ \"groupName\":\"test:testGroup\" }  }}";
  
  public void testProcessMessagesHappyPath() throws Exception {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.wsMessagingBridge.ws.url", 
        "http://localhost:8085/grouper-ws");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.wsMessagingBridge.ws.username", 
        "GrouperSystem");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.wsMessagingBridge.ws.password", 
        "admin123");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.wsMessagingBridge.actAsSubjectSourceId", 
        "g:isa");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.wsMessagingBridge.actAsSubjectId", 
        "GrouperSystem");
    
    MessageConsumerDaemon daemon = new MessageConsumerDaemon();
    FakeGrouperMessageSystem grouperMessageSystem = new FakeGrouperMessageSystem();
    
    Collection<GrouperMessage> grouperMessages = new ArrayList<GrouperMessage>();
    GrouperMessage message = new FakeGrouperMessage(validMessage);
    grouperMessages.add(message);
    
    FakeHttpServer httpServer = new FakeHttpServer();
    httpServer.launchHttpServer();
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("grouper.messaging.fake-config-name.ws.url", "http://localhost:8085");
    
    daemon.processMessages("fakeMessagingSystem", grouperMessageSystem, "queue", "test-queue-name", grouperMessages, "fake-config-name");
    
    httpServer.stopHttpServer();
    
    assertTrue(replyToSendCalled);
    assertTrue(webServiceCalled);
    assertEquals(JSONObject.fromObject(replyToBody).getString("result"), "{\"success\":true}");
  }
  
  public void testProcessMessagesInvalidInputMessages() throws Exception {
    
    MessageConsumerDaemon daemon = new MessageConsumerDaemon();
    FakeGrouperMessageSystem grouperMessageSystem = new FakeGrouperMessageSystem();
    
    Collection<GrouperMessage> grouperMessages = new ArrayList<GrouperMessage>();
    GrouperMessage message = new FakeGrouperMessage(inValidMessage);
    grouperMessages.add(message);
    
    FakeHttpServer httpServer = new FakeHttpServer();
    httpServer.launchHttpServer();
    
    daemon.processMessages("fakeMessagingSystem", grouperMessageSystem, "queue", "test-queue-name", grouperMessages, "fake-config-name");
    
    httpServer.stopHttpServer();
    
    assertTrue(replyToSendCalled);
    assertFalse(webServiceCalled);
    assertEquals(JSONObject.fromObject(replyToBody).getJSONArray("errors").size(), 2);
    assertEquals(JSONObject.fromObject(replyToBody).getJSONArray("errors").getString(0), "grouperHeader.messageVersion is required.");
    assertEquals(JSONObject.fromObject(replyToBody).getJSONArray("errors").getString(1), "grouperHeader.httpPath is required.");
    
  }
  
  class FakeHttpServer {
    
    Server httpServer = null;
    
    void launchHttpServer() throws Exception {
      httpServer = new Server(8085);
      
      SelectChannelConnector connector = new SelectChannelConnector();
      connector.setReuseAddress(true);
      connector.setPort(8085);
      connector.setHost("localhost");
      
      httpServer.setConnectors(new Connector[] {connector});
      
      httpServer.setHandler(new Handler() {
        
        @Override
        public void stop() throws Exception {}
        
        @Override
        public void start() throws Exception {}
        
        @Override
        public void removeLifeCycleListener(Listener arg0) {}
        
        @Override
        public boolean isStopping() {
          return false;
        }
        
        @Override
        public boolean isStopped() {
          return false;
        }
        
        @Override
        public boolean isStarting() {
          return false;
        }
        
        @Override
        public boolean isStarted() {
          return false;
        }
        
        @Override
        public boolean isRunning() {
          return false;
        }
        
        @Override
        public boolean isFailed() {
          return false;
        }
        
        @Override
        public void addLifeCycleListener(Listener arg0) {}
        
        @Override
        public void setServer(Server arg0) {}
        
        @Override
        public void handle(String arg0, HttpServletRequest request, HttpServletResponse response,
            int arg3) throws IOException, ServletException {
          webServiceCalled = true;
          String responseBody = "\"result\": {\"success\": true }";
          response.setHeader("X-Grouper-success", "T");
          response.setHeader("X-Grouper-resultCode", "SUCCESS");
          response.setHeader("X-Grouper-resultCode2", "NONE");
          response.setStatus(200);
          response.setContentType("application/json");
          response.getWriter().write(responseBody);
          ((Request) request).setHandled(true);
        }
        
        @Override
        public Server getServer() {
          return null;
        }
        
        @Override
        public void destroy() {}
      });

      // Start Server
      httpServer.start() ;
      httpServer.join();
      
    }
    
    void stopHttpServer() throws Exception {
       httpServer.stop();
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



