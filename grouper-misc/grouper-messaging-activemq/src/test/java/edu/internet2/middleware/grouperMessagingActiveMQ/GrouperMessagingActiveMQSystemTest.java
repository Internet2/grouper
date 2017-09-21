package edu.internet2.middleware.grouperMessagingActiveMQ;

import static edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType.queue;

import java.util.Collection;

import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.RandomStringUtils;
import junit.framework.TestCase;

public class GrouperMessagingActiveMQSystemTest extends TestCase {
  
  /**
   * @param name
   */
  public GrouperMessagingActiveMQSystemTest(String name) {
    super(name);
  }
  
  private static final String TEST_QUEUE = "test_queue";
  
  public void testSendReceiveDeleteQueue() throws Exception {
    sendReceiveDelete(TEST_QUEUE, queue);
  }
  
  private void sendReceiveDelete(String queueName, GrouperMessageQueueType queueType) throws InterruptedException {
    
    final String messageSystemName = "activemq";
    final String messageId = RandomStringUtils.randomAlphanumeric(50);
    final String testMessageBody = "test message - "+messageId;
    
    GrouperMessagingActiveMQSystem system = new GrouperMessagingActiveMQSystem();
    
    GrouperMessageSendParam sendParam = new GrouperMessageSendParam();
    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
    queueParam.assignQueueType(queueType);
    queueParam.assignQueueOrTopicName(queueName);
    sendParam.assignGrouperMessageQueueParam(queueParam);
    sendParam.assignGrouperMessageSystemName(messageSystemName);
    sendParam.assignAutocreateObjects(true);
    
    sendParam.addMessageBody(testMessageBody);
         
    system.send(sendParam, null);
    
    GrouperMessageReceiveParam receiveParam = new GrouperMessageReceiveParam();
    receiveParam.assignGrouperMessageSystemName(messageSystemName);
    receiveParam.assignGrouperMessageQueueParam(queueParam);
    receiveParam.assignMaxMessagesToReceiveAtOnce(10);
    receiveParam.assignLongPollMillis(1000);
    receiveParam.assignAutocreateObjects(true);
    
    Collection<GrouperMessage> grouperMessages = system.receive(receiveParam, null).getGrouperMessages();
    
    GrouperMessage grouperMessage = grouperMessages.iterator().next();
    
    assertTrue(grouperMessage.getMessageBody().equals(testMessageBody));
    
  }

}