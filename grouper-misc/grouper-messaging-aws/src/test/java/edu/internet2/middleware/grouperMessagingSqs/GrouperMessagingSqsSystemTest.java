package edu.internet2.middleware.grouperMessagingSqs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.RandomUtils;

import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
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
  
  public void testSendToQueue() throws Exception {
    
    final String messageSystemName = "sqs";
    String messageId = String.valueOf(RandomUtils.nextLong());
    final String testMessageBody = "test message - "+messageId;
    final GrouperMessagingSqsSystem system = new GrouperMessagingSqsSystem();
    
    GrouperMessageSendParam sendParam = new GrouperMessageSendParam();
    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
    queueParam.assignQueueType(GrouperMessageQueueType.queue);
    queueParam.assignQueueOrTopicName("test_queue");
    sendParam.assignGrouperMessageQueueParam(queueParam);
    sendParam.assignGrouperMessageSystemName(messageSystemName);
    sendParam.addGrouperMessage(new GrouperMessageSqs(testMessageBody, messageId));
    sendParam.assignAutocreateObjects(true);
         
    system.send(sendParam);
    
    GrouperMessageReceiveParam receiveParam = new GrouperMessageReceiveParam();
    receiveParam.assignGrouperMessageSystemName(messageSystemName);
    receiveParam.assignGrouperMessageQueueParam(queueParam);
    receiveParam.assignAutocreateObjects(true);
    
    Collection<GrouperMessage> grouperMessages = system.receive(receiveParam).getGrouperMessages();
    Set<String> messageBodies = new HashSet<String>();
    for (GrouperMessage grouperMessage: grouperMessages) {
      messageBodies.add(grouperMessage.getMessageBody());
    }
    
    assertTrue(messageBodies.contains(testMessageBody));
    
  }
  
//  public void testErrorSendingWhenQueueNotThereAlready() {
//    
//    final String messageSystemName = "rabbitmq";
//    final String testMessageBody = "this is test message body for queue";
//    final GrouperMessagingRabbitmqSystem system = new GrouperMessagingRabbitmqSystem();
//    String queueName = String.valueOf(RandomUtils.nextLong());
//    GrouperMessageSendParam sendParam = new GrouperMessageSendParam();
//    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
//    queueParam.assignQueueType(GrouperMessageQueueType.queue);
//    queueParam.assignQueueOrTopicName(queueName);
//    sendParam.assignGrouperMessageQueueParam(queueParam);
//    sendParam.assignGrouperMessageSystemName(messageSystemName);
//    sendParam.addMessageBody(testMessageBody);
//    try {
//      system.send(sendParam);
//    } catch (Exception e) {
//      assertTrue(e.getMessage().equals("queue "+queueName+" doesn't exist. Either create the queue or set the autoCreateObjects to true."));
//    }
//  }
//  
//  public void testErrorSendingWhenExchangeNotThereAlready() {
//    
//    final String messageSystemName = "rabbitmq";
//    final String testMessageBody = "this is test message body for queue";
//    final GrouperMessagingRabbitmqSystem system = new GrouperMessagingRabbitmqSystem();
//    String exchangeName = String.valueOf(RandomUtils.nextLong());
//    GrouperMessageSendParam sendParam = new GrouperMessageSendParam();
//    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
//    queueParam.assignQueueType(GrouperMessageQueueType.topic);
//    queueParam.assignQueueOrTopicName(exchangeName);
//    sendParam.assignGrouperMessageQueueParam(queueParam);
//    sendParam.assignGrouperMessageSystemName(messageSystemName);
//    sendParam.addMessageBody(testMessageBody);
//    try {
//      system.send(sendParam);
//    } catch (Exception e) {
//      assertTrue(e.getMessage().equals("exchange "+exchangeName+" doesn't exist. Either create the exchange or set the autoCreateObjects to true."));
//    }
//  }
//  
//  public void testErrorReceivingWhenQueueNotThereAlready() {
//    
//    final String messageSystemName = "rabbitmq";
//    final GrouperMessagingRabbitmqSystem system = new GrouperMessagingRabbitmqSystem();
//    String queueName = String.valueOf(RandomUtils.nextLong());
//    
//    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
//    queueParam.assignQueueType(GrouperMessageQueueType.queue);
//    queueParam.assignQueueOrTopicName(queueName);
//    
//    GrouperMessageReceiveParam receiveParam = new GrouperMessageReceiveParam();
//    receiveParam.assignGrouperMessageSystemName(messageSystemName);
//    receiveParam.assignGrouperMessageQueueParam(queueParam);
//    system.addReceiveEventListener(new MessageReceiveEventListener() {
//      @Override
//      public void messageReceived(String messageBody) {
//        fail();
//      }
//    });
//    
//    try {
//      system.receive(receiveParam);
//    } catch (Exception e) {
//      assertTrue(e.getMessage().equals("queue "+queueName+" doesn't exist. Either create the queue or set the autoCreateObjects to true."));
//    }
//  }
//  
//  public void testErrorReceivingWhenExchangeNotThereAlready() {
//    
//    final String messageSystemName = "rabbitmq";
//    final GrouperMessagingRabbitmqSystem system = new GrouperMessagingRabbitmqSystem();
//    String exchangeName = String.valueOf(RandomUtils.nextLong());
//    
//    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
//    queueParam.assignQueueType(GrouperMessageQueueType.topic);
//    queueParam.assignQueueOrTopicName(exchangeName);
//    
//    GrouperMessageReceiveParam receiveParam = new GrouperMessageReceiveParam();
//    receiveParam.assignGrouperMessageSystemName(messageSystemName);
//    receiveParam.assignGrouperMessageQueueParam(queueParam);
//    system.addReceiveEventListener(new MessageReceiveEventListener() {
//      @Override
//      public void messageReceived(String messageBody) {
//        fail();
//      }
//    });
//    
//    try {
//      system.receive(receiveParam);
//    } catch (Exception e) {
//      assertTrue(e.getMessage().equals("exchange "+exchangeName+" doesn't exist. Either create the exchange or set the autoCreateObjects to true."));
//    }
//  }
//  
//  public void testSendToTopic() throws Exception {
//    
//    final String messageSystemName = "rabbitmq";
//    final String testMessageBody = "this is test message body for topic";
//    final GrouperMessagingRabbitmqSystem system = new GrouperMessagingRabbitmqSystem();
//    
//    GrouperMessageSendParam sendParam = new GrouperMessageSendParam();
//    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
//    queueParam.assignQueueType(GrouperMessageQueueType.topic);
//    queueParam.assignQueueOrTopicName("test_topic4");
//    sendParam.assignGrouperMessageQueueParam(queueParam);
//    sendParam.assignGrouperMessageSystemName(messageSystemName);
//    sendParam.addMessageBody(testMessageBody);
//    sendParam.assignAutocreateObjects(true);
//         
//    GrouperMessageReceiveParam receiveParam = new GrouperMessageReceiveParam();
//    receiveParam.assignGrouperMessageSystemName(messageSystemName);
//    receiveParam.assignGrouperMessageQueueParam(queueParam);
//    receiveParam.assignAutocreateObjects(true);
//    system.addReceiveEventListener(new MessageReceiveEventListener() {
//      
//      @Override
//      public void messageReceived(String messageBody) {
//        assertTrue(messageBody.equals(testMessageBody));
//        system.closeConnection(messageSystemName);
//      }
//    });
//    system.receive(receiveParam);
//
//    system.send(sendParam);
//  }

}