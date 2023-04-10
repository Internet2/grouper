package edu.internet2.middleware.grouperMessagingRabbitmq;

import static org.junit.Assert.assertArrayEquals;

import java.util.List;

import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * 
 */
public class GrouperMessagingRabbitmqSystemTest extends TestCase {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMessagingRabbitmqSystemTest("testSendToQueue"));
  }
  
  /**
   * @param name
   */
  public GrouperMessagingRabbitmqSystemTest(String name) {
    super(name);
  }
  
  public void testSendToQueue() throws Exception {
    
    final String messageSystemName = "rabbitmq";
    final String testMessageBody = "this is test message body for queue";
    final GrouperMessagingRabbitmqSystem system = new GrouperMessagingRabbitmqSystem(RabbitMQConnectionFactoryFake.INSTANACE);
    
    GrouperMessageSendParam sendParam = new GrouperMessageSendParam();
    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
    queueParam.assignQueueType(GrouperMessageQueueType.queue);
    queueParam.assignQueueOrTopicName("test_queue");
    sendParam.assignGrouperMessageQueueParam(queueParam);
    sendParam.assignGrouperMessageSystemName(messageSystemName);
    
    GrouperMessageRabbitmq rabbitMqMessage = new GrouperMessageRabbitmq(testMessageBody, "test_id");
    
    sendParam.addGrouperMessage(rabbitMqMessage);
    sendParam.assignAutocreateObjects(true);
         
    system.send(sendParam);
    
    List<? extends Object> arguments = FakeConnection.recordedValues.get("queueDeclare");
    assertEquals("test_queue", arguments.get(0));
    
    arguments = FakeConnection.recordedValues.get("basicPublish");
    
    assertEquals("test_queue", arguments.get(1));
    assertArrayEquals(testMessageBody.getBytes("UTF-8"), (byte[])arguments.get(3));
    
  }
  
  
  public void testSendToTopic() throws Exception {
    
    final String messageSystemName = "rabbitmq";
    final String testMessageBody = "this is test message body for topic";
    final GrouperMessagingRabbitmqSystem system = new GrouperMessagingRabbitmqSystem(RabbitMQConnectionFactoryFake.INSTANACE);
    
    GrouperMessageSendParam sendParam = new GrouperMessageSendParam();
    GrouperMessageQueueParam queueParam = new GrouperMessageQueueParam();
    queueParam.assignQueueType(GrouperMessageQueueType.topic);
    queueParam.assignQueueOrTopicName("test_exchange");
    sendParam.assignGrouperMessageQueueParam(queueParam);
    sendParam.assignGrouperMessageSystemName(messageSystemName);
    GrouperMessageRabbitmq rabbitMqMessage = new GrouperMessageRabbitmq(testMessageBody, "test_id");
    
    sendParam.addGrouperMessage(rabbitMqMessage);
    sendParam.assignAutocreateObjects(true);
    sendParam.assignExchangeType("DIRECT");
         
    system.send(sendParam.assignRoutingKey("test_routing_key"));
    
    List<? extends Object> arguments = FakeConnection.recordedValues.get("exchangeDeclare");
    assertEquals("test_exchange", arguments.get(0));
    
    arguments = FakeConnection.recordedValues.get("basicPublish");
    
    assertEquals("test_routing_key", arguments.get(1));
    assertArrayEquals(testMessageBody.getBytes("UTF-8"), (byte[])arguments.get(3));
  }

}