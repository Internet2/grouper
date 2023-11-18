package edu.internet2.middleware.grouperMessagingSqs;

import static edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType.queue;

import java.util.Collection;

import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSystemParam;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.RandomStringUtils;
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
  
  /**
   * To run the tests in this file, make sure that the following SQS queues
   * exist and they all are empty.
   */
  
  private static final String TEST_STANDARD_QUEUE = "test_queue";
  private static final String TEST_FIFO_QUEUE = "test_queue.fifo";
  private static final String TEST_ANOTHER_STANDARD_QUEUE = "test_another_queue";
  private static final String TEST_ANOTHER_FIFO_QUEUE = "test_another_queue.fifo";
  
  public void testSendReceiveDeleteStandardQueue() throws Exception {
    sendReceiveDelete(TEST_STANDARD_QUEUE);
  }
  
  public void testSendReceiveDeleteFifoQueue() throws Exception {
    sendReceiveDelete(TEST_FIFO_QUEUE);
  }
  
  public void testAcknowledgeSendToAnotherQueueStandard() throws Exception {
    acknowledgeSendToAnotherQueue(TEST_STANDARD_QUEUE, TEST_ANOTHER_STANDARD_QUEUE);
  }
  
  public void testAcknowledgeSendToAnotherQueueFifo() throws Exception {
    acknowledgeSendToAnotherQueue(TEST_FIFO_QUEUE, TEST_ANOTHER_FIFO_QUEUE);
  }
  
  public void testAcknowledgeRetrunToQueueStandard() throws Exception {
    acknowledgeReturnToQueue(TEST_STANDARD_QUEUE);
  }
  
  public void testAcknowledgeRetrunToQueueFifo() throws Exception {
    acknowledgeReturnToQueue(TEST_FIFO_QUEUE);
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
      assertTrue(e.getMessage().equals("queue "+queueName+" doesn't exist."));
    }
  }
  
  private void sendReceiveDelete(String queueName) {
    
    final String messageSystemName = "sqs";
    final String messageId = RandomStringUtils.randomAlphanumeric(50);
    final String testMessageBody = "test message - "+messageId;
    
    GrouperMessagingSqsSystem system = new GrouperMessagingSqsSystem();
    
    GrouperMessageSendParam sendParam = new GrouperMessageSendParam();
    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
    queueParam.assignQueueType(queue);
    queueParam.assignQueueOrTopicName(queueName);
    sendParam.assignGrouperMessageQueueParam(queueParam);
    sendParam.assignGrouperMessageSystemName(messageSystemName);
    
    sendParam.addMessageBody(testMessageBody);
         
    system.send(sendParam);
    
    GrouperMessageReceiveParam receiveParam = new GrouperMessageReceiveParam();
    receiveParam.assignGrouperMessageSystemName(messageSystemName);
    receiveParam.assignGrouperMessageQueueParam(queueParam);
    receiveParam.assignMaxMessagesToReceiveAtOnce(10);
    
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
  
  private void acknowledgeSendToAnotherQueue(String queueName, String anotherQueueName) {
    
    final String messageSystemName = "sqs";
    final String messageId = RandomStringUtils.randomAlphanumeric(50);
    final String testMessageBody = "test message - "+messageId;
    
    GrouperMessagingSqsSystem system = new GrouperMessagingSqsSystem();
    
    GrouperMessageSendParam sendParam = new GrouperMessageSendParam();
    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
    queueParam.assignQueueType(queue);
    queueParam.assignQueueOrTopicName(queueName);
    sendParam.assignGrouperMessageQueueParam(queueParam);
    sendParam.assignGrouperMessageSystemName(messageSystemName);
    sendParam.addMessageBody(testMessageBody);
         
    system.send(sendParam);
    
    GrouperMessageReceiveParam receiveParam = new GrouperMessageReceiveParam();
    receiveParam.assignGrouperMessageSystemName(messageSystemName);
    receiveParam.assignGrouperMessageQueueParam(queueParam);
    receiveParam.assignMaxMessagesToReceiveAtOnce(10);
    
    GrouperMessage grouperMessage = system.receive(receiveParam).getGrouperMessages().iterator().next();
    
    assertTrue(grouperMessage.getMessageBody().equals(testMessageBody));
    
    GrouperMessageAcknowledgeParam acknowledgeParam = new GrouperMessageAcknowledgeParam();
    acknowledgeParam.assignGrouperMessageQueueParam(queueParam);
    GrouperMessageQueueParam anotherQueueParam = new GrouperMessageQueueParam();
    anotherQueueParam.assignQueueType(queue);
    anotherQueueParam.assignQueueOrTopicName(anotherQueueName);
    acknowledgeParam.assignAnotherQueueParam(anotherQueueParam);
    
    GrouperMessageSystemParam anotherSystemParam = new GrouperMessageSystemParam();
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
  
  private void acknowledgeReturnToQueue(String queueName) {
    
    final String messageSystemName = "sqs";
    final String messageId = RandomStringUtils.randomAlphanumeric(50);
    final String testMessageBody = "test message - "+messageId;
    
    GrouperMessagingSqsSystem system = new GrouperMessagingSqsSystem();
    
    GrouperMessageSendParam sendParam = new GrouperMessageSendParam();
    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
    queueParam.assignQueueType(queue);
    queueParam.assignQueueOrTopicName(queueName);
    sendParam.assignGrouperMessageQueueParam(queueParam);
    sendParam.assignGrouperMessageSystemName(messageSystemName);
    sendParam.addMessageBody(testMessageBody);
         
    system.send(sendParam);
    
    GrouperMessageReceiveParam receiveParam = new GrouperMessageReceiveParam();
    receiveParam.assignGrouperMessageSystemName(messageSystemName);
    receiveParam.assignGrouperMessageQueueParam(queueParam);
    receiveParam.assignMaxMessagesToReceiveAtOnce(10);
    
    GrouperMessage grouperMessage = system.receive(receiveParam).getGrouperMessages().iterator().next();
    
    assertTrue(grouperMessage.getMessageBody().equals(testMessageBody));
    
    GrouperMessageAcknowledgeParam acknowledgeParam = new GrouperMessageAcknowledgeParam();
    acknowledgeParam.assignGrouperMessageQueueParam(queueParam);
    acknowledgeParam.assignGrouperMessageSystemName(messageSystemName);
    acknowledgeParam.addGrouperMessage(new GrouperMessageSqs(testMessageBody, grouperMessage.getId()));
    acknowledgeParam.assignAcknowledgeType(GrouperMessageAcknowledgeType.return_to_queue);
    system.acknowledge(acknowledgeParam);
    
    GrouperMessage message = system.receive(receiveParam).getGrouperMessages().iterator().next();
    
    assertTrue(message.getMessageBody().equals(testMessageBody));
  }

}