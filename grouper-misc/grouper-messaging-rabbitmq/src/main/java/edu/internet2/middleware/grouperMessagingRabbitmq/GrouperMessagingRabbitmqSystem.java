/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperMessagingRabbitmq;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSystemParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem;


/**
 * docs on client:
 * https://www.rabbitmq.com/tutorials/tutorial-one-java.html
 */
public class GrouperMessagingRabbitmqSystem implements GrouperMessagingSystem {
  
  /**
   * log
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperMessagingRabbitmqSystem.class);
  
  public GrouperMessagingRabbitmqSystem() {}

  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#send(edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam)
   */
  public GrouperMessageSendResult send(GrouperMessageSendParam grouperMessageSendParam) {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(true);
    //TODO check if the member has permission to send message
    Member fromMember = grouperSession.getMember();

    if (grouperMessageSendParam.getGrouperMessageQueueParam().getQueueType() != GrouperMessageQueueType.queue) {
      throw new RuntimeException("For rabbitmq only queue type is allowed."); 
    }
    
    String queueOrTopicName = grouperMessageSendParam.getGrouperMessageQueueParam().getQueueOrTopicName();
    
    GrouperMessageSystemParam grouperMessageSystemParam = grouperMessageSendParam.getGrouperMessageSystemParam();
    if (grouperMessageSystemParam == null || StringUtils.isBlank(grouperMessageSystemParam.getMessageSystemName())) {
      throw new RuntimeException("grouperMessageSystemParam.messageSystemName is a required field.");
    }
    try {
      
      Connection connection = RabbitMQConnectionFactory.INSTANCE.getConnection(grouperMessageSystemParam.getMessageSystemName());
      Channel channel = connection.createChannel();
      if (grouperMessageSystemParam.isAutocreateObjects()) {
        channel.queueDeclare(queueOrTopicName, false, false, false, null);
      }
      
      for (GrouperMessage grouperMessage : GrouperUtil.nonNull(grouperMessageSendParam.getGrouperMessages())) {

        String message = grouperMessage.getMessageBody();
        
        channel.basicPublish("", queueOrTopicName, null, message.getBytes("UTF-8"));
        LOG.info("Send message: "+message);
        channel.close();
      }
      
    } catch(Exception e) {
      e.printStackTrace();
    }
    return new GrouperMessageSendResult();
  }
  
  public static void main(String[] args) {
    
    GrouperSession.startRootSession();
    GrouperMessagingRabbitmqSystem system = new GrouperMessagingRabbitmqSystem();
    
    GrouperMessageSendParam sendParam = new GrouperMessageSendParam();
    sendParam.assignGrouperMessageQueueParam(new GrouperMessageQueueParam().assignQueueType(GrouperMessageQueueType.queue));
    sendParam.assignGrouperMessageSystemName("rabbitmq");
    sendParam.addMessageBody("this is test message body.");
    
    system.send(sendParam);
    
//    String QUEUE_NAME = "test_queue";
//    try {
//      ConnectionFactory factory = new ConnectionFactory();
//      factory.setHost("localhost");
//      Connection connection = factory.newConnection();
//      Channel channel = connection.createChannel();
//      
//      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//      String message = "Hello world";
//      channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
//      System.out.println(" [x] Sent '" + message + "'");
//      channel.close();
//      connection.close();
//      
//      
//      factory = new ConnectionFactory();
//      factory.setHost("localhost");
//      connection = factory.newConnection();
//      channel = connection.createChannel();
//
//      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//      Consumer consumer = new DefaultConsumer(channel) {
//        @Override
//        public void handleDelivery(String consumerTag, Envelope envelope,
//                                   AMQP.BasicProperties properties, byte[] body)
//            throws IOException {
//          String message = new String(body, "UTF-8");
//          System.out.println(" [x] Received '" + message + "'");
//        }
//      };
//      channel.basicConsume(QUEUE_NAME, true, consumer);
//      
//      
//    } catch(Exception e) {
//      e.printStackTrace();
//    }
  }

  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#acknowledge(edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam)
   */
  public GrouperMessageAcknowledgeResult acknowledge(GrouperMessageAcknowledgeParam grouperMessageAcknowledgeParam) {
    try {

      GrouperMessageSystemParam grouperMessageSystemParam = grouperMessageAcknowledgeParam.getGrouperMessageSystemParam();
      if (grouperMessageSystemParam == null || StringUtils.isBlank(grouperMessageSystemParam.getMessageSystemName())) {
        throw new RuntimeException("grouperMessageSystemParam.messageSystemName is a required field.");
      }
      Connection connection = RabbitMQConnectionFactory.INSTANCE.getConnection(grouperMessageSystemParam.getMessageSystemName());
      final Channel channel = connection.createChannel();

      String queueOrTopicName = grouperMessageAcknowledgeParam.getGrouperMessageQueueParam().getQueueOrTopicName();
      channel.queueDeclare(queueOrTopicName, false, false, false, null);

      Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
          String message = new String(body, "UTF-8");
          LOG.info("Acknowledging: "+message);
          channel.basicAck(envelope.getDeliveryTag(), false);
        }
      };
      boolean autoAck = false;
      channel.basicConsume(queueOrTopicName, autoAck, consumer);
    } catch(Exception e) {
      e.printStackTrace();
    }    
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#receive(edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam)
   */
  public GrouperMessageReceiveResult receive(GrouperMessageReceiveParam grouperMessageReceiveParam) {
    
    GrouperMessageSystemParam grouperMessageSystemParam = grouperMessageReceiveParam.getGrouperMessageSystemParam();
    if (grouperMessageSystemParam == null || StringUtils.isBlank(grouperMessageSystemParam.getMessageSystemName())) {
      throw new RuntimeException("grouperMessageSystemParam.messageSystemName is a required field.");
    }
    
    try {

      Connection connection = RabbitMQConnectionFactory.INSTANCE.getConnection(grouperMessageSystemParam.getMessageSystemName());
      Channel channel = connection.createChannel();
      String queueOrTopicName = grouperMessageReceiveParam.getGrouperMessageQueueParam().getQueueOrTopicName();
      channel.queueDeclare(queueOrTopicName, false, false, false, null);

      Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
          String message = new String(body, "UTF-8");
          LOG.info("Received: "+message);
          System.out.println("Received: "+message);
        }
      };
      boolean autoAck = true;
      channel.basicConsume(queueOrTopicName, autoAck, consumer);
    } catch(Exception e) {
      e.printStackTrace();
    }    
    return null;
  }
  
  
  public enum RabbitMQConnectionFactory {
    
    INSTANCE;
    
    private Connection connection;
    private String messagingSystemName;
    
    public Connection getConnection(String messagingSystemName) {
      
      if (connection == null || !this.messagingSystemName.equals(messagingSystemName)) {
        String host = GrouperConfig.retrieveConfig().propertyValueString(String.format("grouper.%s.messsaging.host", messagingSystemName), "localhost");
        String virtualHost = GrouperConfig.retrieveConfig().propertyValueString(String.format("grouper.%s.messsaging.virtualhost", messagingSystemName));
        String username = GrouperConfig.retrieveConfig().propertyValueString(String.format("grouper.%s.messsaging.username", messagingSystemName));
        String password = GrouperConfig.retrieveConfig().propertyValueString(String.format("grouper.%s.messsaging.password", messagingSystemName));
        Integer port = GrouperConfig.retrieveConfig().propertyValueInt(String.format("grouper.%s.messsaging.port", messagingSystemName));
        
        try {
          ConnectionFactory factory = new ConnectionFactory();
          factory.setHost(host);
          
          if (!StringUtils.isEmpty(virtualHost)) {
            factory.setVirtualHost(virtualHost);
          }
          
          if (!StringUtils.isEmpty(username)) {
            factory.setUsername(username);
          }
          
          if (!StringUtils.isEmpty(password)) {
            factory.setPassword(password);
          }
          
          if (port != null) {
            factory.setPort(port);
          }
          
          this.connection = factory.newConnection();
          this.messagingSystemName = messagingSystemName;
          
        } catch (Exception e) {
          throw new RuntimeException("Error occurred while connecting to rabbitmq host: "+host);
        }
        
      }
      return this.connection;
    }
  }
  
}