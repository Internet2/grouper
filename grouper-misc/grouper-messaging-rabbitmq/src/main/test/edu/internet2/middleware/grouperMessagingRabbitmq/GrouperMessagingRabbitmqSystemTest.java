package edu.internet2.middleware.grouperMessagingRabbitmq;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import junit.textui.TestRunner;

public class GrouperMessagingRabbitmqSystemTest extends GrouperTest {
  
  /**
   * log
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperMessagingRabbitmqSystemTest.class);
  
  public static void main(String[] args) throws Exception {
    TestRunner.run(new GrouperMessagingRabbitmqSystemTest("testSend"));
  }
  

  /**
   * @param name
   */
  public GrouperMessagingRabbitmqSystemTest(String name) {
    super(name);
    
  }
  
  public void testSend() throws Exception {
    
    final String messageSystemName = "rabbitmq";
    final String testMessageBody = "this is test message body";
    final GrouperMessagingRabbitmqSystem system = new GrouperMessagingRabbitmqSystem();
    
    GrouperMessageSendParam sendParam = new GrouperMessageSendParam();
    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
    queueParam.assignQueueType(GrouperMessageQueueType.queue);
    queueParam.assignQueueOrTopicName("test_queue");
    sendParam.assignGrouperMessageQueueParam(queueParam);
    sendParam.assignGrouperMessageSystemName(messageSystemName);
    sendParam.addMessageBody(testMessageBody);
    sendParam.assignAutocreateObjects(true); 
         
    system.send(sendParam);
    
    LOG.info("message sent.. now going to receive the same message.");
    
    GrouperMessageReceiveParam receiveParam = new GrouperMessageReceiveParam();
    receiveParam.assignGrouperMessageSystemName(messageSystemName);
    receiveParam.assignGrouperMessageQueueParam(queueParam);
    system.addReceiveEventListener(new MessageReceiveEventListener() {
      
      @Override
      public void messageReceived(String messageBody) {
        assertTrue(messageBody.equals(testMessageBody));
        system.closeConnection(messageSystemName);
      }
    });
    system.receive(receiveParam);
    
  }

}
