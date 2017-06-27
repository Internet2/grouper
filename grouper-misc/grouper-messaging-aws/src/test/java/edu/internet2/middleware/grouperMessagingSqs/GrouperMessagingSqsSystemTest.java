package edu.internet2.middleware.grouperMessagingSqs;

import static edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType.queue;

import java.util.Collection;

import org.apache.commons.lang3.RandomStringUtils;

import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSystemParam;
import edu.internet2.middleware.grouperMessagingAWS.GrouperMessageSqs;
import edu.internet2.middleware.grouperMessagingAWS.GrouperMessagingSqsSystem;
import junit.framework.TestCase;

public class GrouperMessagingSqsSystemTest extends TestCase {
  
  /**
   * @param name
   */
  public GrouperMessagingSqsSystemTest(String name) {
    super(name);
  }
  
  public void tesaSendReceiveDelete() throws Exception {
    
    final String messageSystemName = "sqs";
    final String messageId = RandomStringUtils.randomAlphanumeric(50);
    final String testMessageBody = "test message - "+messageId;
    final String queueName = RandomStringUtils.randomAlphanumeric(50);
    
    GrouperMessagingSqsSystem system = new GrouperMessagingSqsSystem();
    
    GrouperMessageSendParam sendParam = new GrouperMessageSendParam();
    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
    queueParam.assignQueueType(queue);
    queueParam.assignQueueOrTopicName(queueName);
    sendParam.assignGrouperMessageQueueParam(queueParam);
    sendParam.assignGrouperMessageSystemName(messageSystemName);
    
    sendParam.addMessageBody(testMessageBody);
    sendParam.assignAutocreateObjects(true);
         
    system.send(sendParam);
    
    GrouperMessageReceiveParam receiveParam = new GrouperMessageReceiveParam();
    receiveParam.assignGrouperMessageSystemName(messageSystemName);
    receiveParam.assignGrouperMessageQueueParam(queueParam);
    receiveParam.assignAutocreateObjects(true);
    receiveParam.assignMaxMessagesToReceiveAtOnce(10);
    receiveParam.assignLongPollMillis(3000);
    
    GrouperMessage grouperMessage = system.receive(receiveParam).getGrouperMessages().iterator().next();
    
    assertTrue(grouperMessage.getMessageBody().equals(testMessageBody));
    
    GrouperMessageAcknowledgeParam acknowledgeParam = new GrouperMessageAcknowledgeParam();
    acknowledgeParam.assignGrouperMessageSystemName(messageSystemName);
    acknowledgeParam.assignGrouperMessageQueueParam(queueParam);
    acknowledgeParam.addGrouperMessage(new GrouperMessageSqs(testMessageBody, grouperMessage.getId()));
    acknowledgeParam.assignAcknowledgeType(GrouperMessageAcknowledgeType.mark_as_processed);
    system.acknowledge(acknowledgeParam);
    
    Collection<GrouperMessage> grouperMessagesAfterDeleting = system.receive(receiveParam).getGrouperMessages();
    assertTrue(grouperMessagesAfterDeleting.isEmpty());
    
  }
  
  public void testErrorSendingWhenQueueNotThereAlready() {
    
    final String messageSystemName = "sqs";
    final String testMessageBody = "this is test message body for queue";
    
    GrouperMessagingSqsSystem system = new GrouperMessagingSqsSystem();
    String queueName = RandomStringUtils.randomAlphanumeric(50);
    GrouperMessageSendParam sendParam = new GrouperMessageSendParam();
    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
    queueParam.assignQueueType(queue);
    queueParam.assignQueueOrTopicName(queueName);
    sendParam.assignGrouperMessageQueueParam(queueParam);
    sendParam.assignGrouperMessageSystemName(messageSystemName);
    sendParam.addMessageBody(testMessageBody);
    try {
      system.send(sendParam);
    } catch (Exception e) {
      assertTrue(e.getMessage().equals("queue "+queueName+" doesn't exist. Either create the queue or set the autoCreateObjects to true."));
    }
  }
  
  public void testAcknowledgeSendToAnotherQueue() throws Exception {
    
    final String messageSystemName = "sqs";
    final String messageId = RandomStringUtils.randomAlphanumeric(50);
    final String testMessageBody = "test message - "+messageId;
    final String queueName = RandomStringUtils.randomAlphanumeric(50);
    
    GrouperMessagingSqsSystem system = new GrouperMessagingSqsSystem();
    
    GrouperMessageSendParam sendParam = new GrouperMessageSendParam();
    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
    queueParam.assignQueueType(queue);
    queueParam.assignQueueOrTopicName(queueName);
    sendParam.assignGrouperMessageQueueParam(queueParam);
    sendParam.assignGrouperMessageSystemName(messageSystemName);
    
    sendParam.addMessageBody(testMessageBody);
    sendParam.assignAutocreateObjects(true);
         
    system.send(sendParam);
    
    GrouperMessageReceiveParam receiveParam = new GrouperMessageReceiveParam();
    receiveParam.assignGrouperMessageSystemName(messageSystemName);
    receiveParam.assignGrouperMessageQueueParam(queueParam);
    receiveParam.assignAutocreateObjects(true);
    receiveParam.assignMaxMessagesToReceiveAtOnce(10);
    receiveParam.assignLongPollMillis(3000);
    
    GrouperMessage grouperMessage = system.receive(receiveParam).getGrouperMessages().iterator().next();
    
    assertTrue(grouperMessage.getMessageBody().equals(testMessageBody));
    
    String anotherQueueName = RandomStringUtils.randomAlphanumeric(50);
    
    GrouperMessageAcknowledgeParam acknowledgeParam = new GrouperMessageAcknowledgeParam();
    acknowledgeParam.assignGrouperMessageQueueParam(queueParam);
    GrouperMessageQueueParam anotherQueueParam = new GrouperMessageQueueParam();
    anotherQueueParam.assignQueueType(queue);
    anotherQueueParam.assignQueueOrTopicName(anotherQueueName);
    acknowledgeParam.assignAnotherQueueParam(anotherQueueParam);
    
    GrouperMessageSystemParam anotherSystemParam = new GrouperMessageSystemParam();
    anotherSystemParam.assignAutocreateObjects(true);
    anotherSystemParam.assignMesssageSystemName(messageSystemName);
    acknowledgeParam.assignGrouperMessageSystemParam(anotherSystemParam);
    
    acknowledgeParam.addGrouperMessage(new GrouperMessageSqs(testMessageBody, grouperMessage.getId()));
    acknowledgeParam.assignAcknowledgeType(GrouperMessageAcknowledgeType.send_to_another_queue);
    system.acknowledge(acknowledgeParam);
    
    // sending to another queue means delete from current queue and add to antoher queue.
    Collection<GrouperMessage> grouperMessagesAfterDeleting = system.receive(receiveParam).getGrouperMessages();
    assertTrue(grouperMessagesAfterDeleting.isEmpty());
    
    // check if another queue has the message
    receiveParam = new GrouperMessageReceiveParam();
    receiveParam.assignGrouperMessageSystemName(messageSystemName);
    receiveParam.assignGrouperMessageQueueParam(anotherQueueParam);
    
    grouperMessage = system.receive(receiveParam).getGrouperMessages().iterator().next();
    assertTrue(grouperMessage.getMessageBody().equals(testMessageBody));
    
  }

}