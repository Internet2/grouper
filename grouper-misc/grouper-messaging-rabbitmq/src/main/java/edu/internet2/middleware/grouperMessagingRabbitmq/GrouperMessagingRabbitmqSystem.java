/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperMessagingRabbitmq;

import java.io.IOException;

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

    String queueOrTopicName = grouperMessageSendParam.getGrouperMessageQueueParam().getQueueOrTopicName();
    
    //TODO check if allowing queue is allowed.
    GrouperMessageSystemParam grouperMessageSystemParam = grouperMessageSendParam.getGrouperMessageSystemParam();
    
    try {
      
      ConnectionFactory factory = new ConnectionFactory();
      
      String host = GrouperConfig.retrieveConfig().propertyValueString("grouper.rabbitmq.messsaging.host", "localhost");
      factory.setHost(host);
      Connection connection = factory.newConnection();
      Channel channel = connection.createChannel();
      channel.queueDeclare(queueOrTopicName, false, false, false, null);
      for (GrouperMessage grouperMessage : GrouperUtil.nonNull(grouperMessageSendParam.getGrouperMessages())) {

        String message = grouperMessage.getMessageBody();
        
        channel.basicPublish("", queueOrTopicName, null, message.getBytes("UTF-8"));
        LOG.info("Send message: "+message);
        channel.close();
        connection.close();
      }
      
    } catch(Exception e) {
      e.printStackTrace();
    }
    return new GrouperMessageSendResult();
  }
  
  
//  public static void main(String[] args) {
//    
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
//  }

  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#acknowledge(edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam)
   */
  public GrouperMessageAcknowledgeResult acknowledge(
      GrouperMessageAcknowledgeParam grouperMessageAcknowledgeParam) {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#receive(edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam)
   */
  public GrouperMessageReceiveResult receive(GrouperMessageReceiveParam grouperMessageReceiveParam) {
    
    try {

      ConnectionFactory factory = new ConnectionFactory();
      String host = GrouperConfig.retrieveConfig().propertyValueString("grouper.rabbitmq.messsaging.host", "localhost");
      factory.setHost(host);
      Connection connection = factory.newConnection();
      Channel channel = connection.createChannel();

      String queueOrTopicName = grouperMessageReceiveParam.getGrouperMessageQueueParam().getQueueOrTopicName();
      channel.queueDeclare(queueOrTopicName, false, false, false, null);

      Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
          String message = new String(body, "UTF-8");
          LOG.info("Received: "+message);
        }
      };
      channel.basicConsume(queueOrTopicName, true, consumer);
    } catch(Exception e) {
      e.printStackTrace();
    }    
    return null;
  }

}
