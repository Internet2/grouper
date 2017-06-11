/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperMessagingRabbitmq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSystemParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;


/**
 * docs on client:
 * https://www.rabbitmq.com/tutorials/tutorial-one-java.html
 */
public class GrouperMessagingRabbitmqSystem implements GrouperMessagingSystem {
   
  
  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperMessagingRabbitmqSystem.class);
  
  private MessageReceiveEventListener listener;
  
  public GrouperMessagingRabbitmqSystem() {}

  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#send(edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam)
   */
  public GrouperMessageSendResult send(GrouperMessageSendParam grouperMessageSendParam) {
        
    if (grouperMessageSendParam.getGrouperMessageQueueParam() == null) {
      throw new IllegalArgumentException("grouperMessageQueueParam is required.");
    }
    
    if (grouperMessageSendParam.getGrouperMessageQueueParam().getQueueType() != GrouperMessageQueueType.queue) {
      throw new IllegalArgumentException("For rabbitmq only queue type is allowed."); 
    }
    
    String queueOrTopicName = grouperMessageSendParam.getGrouperMessageQueueParam().getQueueOrTopicName();
    
    if (StringUtils.isBlank(queueOrTopicName)) {
      throw new IllegalArgumentException("queueOrTopicName is required.");
    }
    
    GrouperMessageSystemParam grouperMessageSystemParam = grouperMessageSendParam.getGrouperMessageSystemParam();
    if (grouperMessageSystemParam == null || StringUtils.isBlank(grouperMessageSystemParam.getMessageSystemName())) {
      throw new IllegalArgumentException("grouperMessageSystemParam.messageSystemName is a required field.");
    }
    try {
      
      Connection connection = RabbitMQConnectionFactory.INSTANCE.getConnection(grouperMessageSystemParam.getMessageSystemName());
      Channel channel = connection.createChannel();
      if (grouperMessageSystemParam.isAutocreateObjects()) {
        channel.queueDeclare(queueOrTopicName, false, false, false, null);
      }
      
      for (GrouperMessage grouperMessage: GrouperClientUtils.nonNull(grouperMessageSendParam.getGrouperMessages())) {

        String message = grouperMessage.getMessageBody();
        channel.basicPublish("", queueOrTopicName, null, message.getBytes("UTF-8"));
        LOG.info("Sent message: "+message);
        channel.close();
      }
      
    } catch(Exception e) {
      throw new RuntimeException("Error occurred while sending message to messaging system: "+grouperMessageSystemParam.getMessageSystemName(), e);
    }
    return new GrouperMessageSendResult();
  }
  
  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#acknowledge(edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam)
   */
  public GrouperMessageAcknowledgeResult acknowledge(GrouperMessageAcknowledgeParam grouperMessageAcknowledgeParam) {
    
    GrouperMessageSystemParam grouperMessageSystemParam = grouperMessageAcknowledgeParam.getGrouperMessageSystemParam();
    if (grouperMessageSystemParam == null || StringUtils.isBlank(grouperMessageSystemParam.getMessageSystemName())) {
      throw new IllegalArgumentException("grouperMessageSystemParam.messageSystemName is a required field.");
    }
    
    try {
      Connection connection = RabbitMQConnectionFactory.INSTANCE.getConnection(grouperMessageSystemParam.getMessageSystemName());
      final Channel channel = connection.createChannel();

      String queueOrTopicName = grouperMessageAcknowledgeParam.getGrouperMessageQueueParam().getQueueOrTopicName();
      channel.queueDeclare(queueOrTopicName, false, false, false, null);
      for (GrouperMessage message: GrouperClientUtils.nonNull(grouperMessageAcknowledgeParam.getGrouperMessages())) {
        channel.basicAck(Long.valueOf(message.getId()), false);
      }
    } catch(Exception e) {
      throw new RuntimeException("Error occurred while trying to acknowledge messages for "+grouperMessageSystemParam.getMessageSystemName(), e);
    }    
    return new GrouperMessageAcknowledgeResult();
  }
  
  public void addReceiveEventListener(MessageReceiveEventListener listener) {
    this.listener = listener;
  }

  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#receive(edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam)
   */
  public GrouperMessageReceiveResult receive(GrouperMessageReceiveParam grouperMessageReceiveParam) {
    
    GrouperMessageSystemParam grouperMessageSystemParam = grouperMessageReceiveParam.getGrouperMessageSystemParam();
    if (grouperMessageSystemParam == null || StringUtils.isBlank(grouperMessageSystemParam.getMessageSystemName())) {
      throw new IllegalArgumentException("grouperMessageSystemParam.messageSystemName is required.");
    }
        
    int defaultPageSize = GrouperClientConfig.retrieveConfig().propertyValueInt(String.format("grouper.%s.messaging.defaultPageSize", grouperMessageSystemParam.getMessageSystemName()), 5);
    int maxPageSize = GrouperClientConfig.retrieveConfig().propertyValueInt(String.format("grouper.%s.messaging.maxPageSize", grouperMessageSystemParam.getMessageSystemName()), 50);
    
    Integer maxMessagesToReceiveAtOnce = grouperMessageReceiveParam.getMaxMessagesToReceiveAtOnce();
    
    if (maxMessagesToReceiveAtOnce == null) {
      maxMessagesToReceiveAtOnce = defaultPageSize;
    }
    
    if (maxMessagesToReceiveAtOnce > maxPageSize) {
      maxMessagesToReceiveAtOnce = maxPageSize;
    }
    
    final Integer pageSize = maxMessagesToReceiveAtOnce;
    
    if (grouperMessageReceiveParam.getGrouperMessageQueueParam().getQueueType() != GrouperMessageQueueType.queue) {
      throw new IllegalArgumentException("For rabbitmq only queue type is allowed."); 
    }
    
    String queueOrTopicName = grouperMessageReceiveParam.getGrouperMessageQueueParam().getQueueOrTopicName();
    
    if (StringUtils.isBlank(queueOrTopicName)) {
      throw new IllegalArgumentException("queueOrTopicName is required.");
    }
    
    Integer longPollMillis = grouperMessageReceiveParam.getLongPollMilis();
    
    if (longPollMillis == null || longPollMillis < 0) {
      longPollMillis = 1000;
    }
    
    GrouperMessageReceiveResult result = new GrouperMessageReceiveResult();
    final Collection<GrouperMessage> messages = new ArrayList<GrouperMessage>();
    result.setGrouperMessages(messages);
    
    try {

      Connection connection = RabbitMQConnectionFactory.INSTANCE.getConnection(grouperMessageSystemParam.getMessageSystemName());
      final Channel channel = connection.createChannel();
      
      if (grouperMessageSystemParam.isAutocreateObjects()) {
        channel.queueDeclare(queueOrTopicName, false, false, false, null);
      }
      
      Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
          String message = new String(body, "UTF-8");
          GrouperMessageRabbitmq rabbitmqMessage = new GrouperMessageRabbitmq(message, properties.getMessageId());
          messages.add(rabbitmqMessage);
          if (messages.size() >= pageSize) {
            try {
              channel.close();
            } catch (TimeoutException e) {
              LOG.error("Error occurred while closing channel", e);
            }
          }
          if (listener != null) {
            listener.messageReceived(message);
          }
          LOG.info("Received: "+message);
        }
      };
      channel.basicConsume(queueOrTopicName, true, consumer);
 
      new Timer().schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    try {
                      if (channel.isOpen()) {
                          channel.close();
                      }
                    } catch (Exception e) {
                      throw new RuntimeException("Error occurred while closing channel", e); 
                    }
                }
            }, 
            longPollMillis);
      
    } catch(Exception e) {
      throw new RuntimeException("Error occurred while trying to receive messages for "+grouperMessageSystemParam.getMessageSystemName(), e);
    }    
    return result;
  }
  
  public void closeConnection(String messagingSystemName) {
    RabbitMQConnectionFactory.INSTANCE.closeConnection(messagingSystemName);
  }
  
  private enum RabbitMQConnectionFactory {
    
    INSTANCE;
    
    private Map<String, Connection> messagingSystemNameConnection = new HashMap<String, Connection>();
           
    private void closeConnection(String messagingSystemName) {
      if (StringUtils.isBlank(messagingSystemName)) {
        throw new IllegalArgumentException("messagingSystemName is required.");
      }
      Connection connection = messagingSystemNameConnection.get(messagingSystemName);
      synchronized(RabbitMQConnectionFactory.class) { 
        if (connection != null && connection.isOpen()) {
          try {
            connection.close();
            connection = null;
          } catch(IOException e) {
            throw new RuntimeException("Error occurred while closing rabbitmq connection for "+messagingSystemName);
          }
        }
      }
    }
    
    private Connection getConnection(String messagingSystemName) {
      
      if (StringUtils.isBlank(messagingSystemName)) {
        throw new IllegalArgumentException("messagingSystemName is required.");
      }
      
      Connection connection =  messagingSystemNameConnection.get(messagingSystemName);
      
      synchronized(RabbitMQConnectionFactory.class) {
        if (connection != null && !connection.isOpen()) {
          connection = null;
        }
        
        if (connection == null || !connection.isOpen()) {
          
          String host = GrouperClientConfig.retrieveConfig().propertyValueString(String.format("grouper.%s.messsaging.host", messagingSystemName));
          String virtualHost = GrouperClientConfig.retrieveConfig().propertyValueString(String.format("grouper.%s.messsaging.virtualhost", messagingSystemName));
          String username = GrouperClientConfig.retrieveConfig().propertyValueString(String.format("grouper.%s.messsaging.username", messagingSystemName));
          String password = GrouperClientConfig.retrieveConfig().propertyValueString(String.format("grouper.%s.messsaging.password", messagingSystemName));
          Integer port = GrouperClientConfig.retrieveConfig().propertyValueInt(String.format("grouper.%s.messsaging.port", messagingSystemName));
          
          try {
            ConnectionFactory factory = new ConnectionFactory();
           
            if (!StringUtils.isEmpty(host)) {
              factory.setHost(host);
            }
            
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
            connection = factory.newConnection();
            messagingSystemNameConnection.put(messagingSystemName, connection);
            
          } catch (Exception e) {
            throw new RuntimeException("Error occurred while connecting to rabbitmq host: "+host+" for "+messagingSystemName);
          }
      }
      
      }
      return connection;
    }
  }
  
}