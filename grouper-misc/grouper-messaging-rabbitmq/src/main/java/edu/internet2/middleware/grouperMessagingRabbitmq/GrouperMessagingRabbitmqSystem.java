/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperMessagingRabbitmq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;

import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSystemParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingConfig;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;


/**
 * docs on client:
 * https://www.rabbitmq.com/tutorials/tutorial-one-java.html
 */
public class GrouperMessagingRabbitmqSystem implements GrouperMessagingSystem {
   
  
  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperMessagingRabbitmqSystem.class);
  
  private RabbitMQConnectionFactory connectionFactory;
  
  public GrouperMessagingRabbitmqSystem() {
    this(RabbitMQConnectionFactoryImpl.INSTANCE);
  }
  
  protected GrouperMessagingRabbitmqSystem(RabbitMQConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }
  
  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#send(edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam)
   */
  @Override
  public GrouperMessageSendResult send(GrouperMessageSendParam grouperMessageSendParam) {
        
    if (grouperMessageSendParam.getGrouperMessageQueueParam() == null) {
      throw new IllegalArgumentException("grouperMessageQueueParam is required.");
    }
    
    String queueOrTopicName = grouperMessageSendParam.getGrouperMessageQueueParam().getQueueOrTopicName();
    String exchangeType = grouperMessageSendParam.getExchangeType();
    
    if (StringUtils.isBlank(queueOrTopicName)) {
      throw new IllegalArgumentException("queueOrTopicName is required.");
    }
    
    GrouperMessageSystemParam grouperMessageSystemParam = grouperMessageSendParam.getGrouperMessageSystemParam();
    if (grouperMessageSystemParam == null || StringUtils.isBlank(grouperMessageSystemParam.getMessageSystemName())) {
      throw new IllegalArgumentException("grouperMessageSystemParam.messageSystemName is a required field.");
    }
    
    try {
      
      Connection connection = connectionFactory.getConnection(grouperMessageSystemParam.getMessageSystemName());
      Channel channel = connection.createChannel();
      
      String error = createQueueOrExchange(grouperMessageSystemParam, channel, queueOrTopicName, 
          exchangeType, grouperMessageSendParam.getGrouperMessageQueueParam().getQueueType());
      
      if (error != null) {
        throw new IllegalArgumentException(error);
      }
      
      for (GrouperMessage grouperMessage: GrouperClientUtils.nonNull(grouperMessageSendParam.getGrouperMessages())) {
        String message = grouperMessage.getMessageBody();
        if (grouperMessageSendParam.getGrouperMessageQueueParam().getQueueType() == GrouperMessageQueueType.topic) {
          channel.basicPublish(queueOrTopicName, StringUtils.defaultString(grouperMessageSendParam.getRoutingKey(), ""), MessageProperties.PERSISTENT_BASIC, message.getBytes("UTF-8"));
        } else {
          channel.basicPublish("", queueOrTopicName, MessageProperties.PERSISTENT_BASIC, message.getBytes("UTF-8"));
        }
        LOG.info("Sent message: "+message);
      }
      channel.close();
      
    } catch(IOException e) {
      throw new RuntimeException("Error occurred while sending message to messaging system: "+grouperMessageSystemParam.getMessageSystemName(), e);
    } catch(TimeoutException e) {
      throw new RuntimeException("Error occurred while closing channel for messaging system: "+grouperMessageSystemParam.getMessageSystemName(), e);
    }
    return new GrouperMessageSendResult();
  }
  
  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#acknowledge(edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam)
   */
  @Override
  public GrouperMessageAcknowledgeResult acknowledge(GrouperMessageAcknowledgeParam grouperMessageAcknowledgeParam) {
    return new GrouperMessageAcknowledgeResult();
  }
  
  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#receive(edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam)
   */
  @Override
  public GrouperMessageReceiveResult receive(GrouperMessageReceiveParam grouperMessageReceiveParam) {
    
    GrouperMessageSystemParam grouperMessageSystemParam = grouperMessageReceiveParam.getGrouperMessageSystemParam();
    if (grouperMessageSystemParam == null || StringUtils.isBlank(grouperMessageSystemParam.getMessageSystemName())) {
      throw new IllegalArgumentException("grouperMessageSystemParam.messageSystemName is required.");
    }
    GrouperMessagingConfig grouperMessagingConfig = GrouperClientConfig.retrieveConfig().retrieveGrouperMessagingConfigNonNull(grouperMessageSystemParam.getMessageSystemName());
    int defaultPageSize = grouperMessagingConfig.propertyValueInt(GrouperClientConfig.retrieveConfig(), "defaultPageSize", 5);
    int maxPageSize = grouperMessagingConfig.propertyValueInt(GrouperClientConfig.retrieveConfig(), "maxPageSize", 5);
        
    Integer maxMessagesToReceiveAtOnce = grouperMessageReceiveParam.getMaxMessagesToReceiveAtOnce();
    
    if (maxMessagesToReceiveAtOnce == null) {
      maxMessagesToReceiveAtOnce = defaultPageSize;
    }
    
    if (maxMessagesToReceiveAtOnce > maxPageSize) {
      maxMessagesToReceiveAtOnce = maxPageSize;
    }
    
    final Integer pageSize = maxMessagesToReceiveAtOnce;
    
    String queueOrTopicName = grouperMessageReceiveParam.getGrouperMessageQueueParam().getQueueOrTopicName();
    String exchangeType = grouperMessageReceiveParam.getExchangeType();
    
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

      Connection connection = connectionFactory.getConnection(grouperMessageSystemParam.getMessageSystemName());
      final Channel channel = connection.createChannel();
      Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
          String message = new String(body, "UTF-8");
          GrouperMessageRabbitmq rabbitmqMessage = new GrouperMessageRabbitmq(message, properties.getMessageId());
          messages.add(rabbitmqMessage);
          channel.basicAck(envelope.getDeliveryTag(), false);
          if (messages.size() >= pageSize) {
            try {
              channel.close();
            } catch (TimeoutException e) {
              LOG.error("Error occurred while closing channel", e);
            }
          }
          LOG.info("Received: "+message);
        }
      };
      
      String error = createQueueOrExchange(grouperMessageSystemParam, channel, queueOrTopicName, 
          exchangeType, grouperMessageReceiveParam.getGrouperMessageQueueParam().getQueueType());
      
      if (error != null) {
        throw new IllegalArgumentException(error);
      }
      
      if (grouperMessageReceiveParam.getGrouperMessageQueueParam().getQueueType() == GrouperMessageQueueType.topic) {
        DeclareOk declareOk = channel.queueDeclare();
        channel.queueBind(declareOk.getQueue(), queueOrTopicName, StringUtils.defaultString(grouperMessageReceiveParam.getRoutingKey(), ""));
        channel.basicConsume(declareOk.getQueue(), false, consumer);
      } else if (grouperMessageReceiveParam.getGrouperMessageQueueParam().getQueueType() == GrouperMessageQueueType.queue) {
        channel.basicConsume(queueOrTopicName, false, consumer);
      }
      
      new Timer().schedule(
        new java.util.TimerTask() {
          @Override
          public void run() {
            try {
              if (channel.isOpen()) {
                channel.close();
              }
            } catch (Exception e) {
              LOG.error("Error occurred while closing channel", e); 
            }
          }
        }, longPollMillis);
    } catch(IOException e) {
      throw new RuntimeException("Error occurred while trying to receive messages for "+grouperMessageSystemParam.getMessageSystemName(), e);
    }
    
    return result;
  }
  
  
  /**
   * @param grouperMessageSystemParam
   * @param channel
   * @param queueOrTopicName
   * @param queueType
   * @return
   * @throws IOException
   */
  private String createQueueOrExchange(GrouperMessageSystemParam grouperMessageSystemParam,
      Channel channel, String queueOrTopicName, String exchangeType, GrouperMessageQueueType queueType) throws IOException {
    
    String error = null;
    
    if (queueType == GrouperMessageQueueType.topic) {
      if (grouperMessageSystemParam.isAutocreateObjects()) {
        
        BuiltinExchangeType exchange;
        try {
          exchange = BuiltinExchangeType.valueOf(exchangeType.toUpperCase());
        } catch(Exception e) {
          String validExchangeTypes = StringUtils.join(BuiltinExchangeType.values(), ",");
          error = "exchange type "+exchangeType+" is not valid. Valid options are: "+validExchangeTypes;
          return error;
        }
        channel.exchangeDeclare(queueOrTopicName, exchange, true);
        
      } else {
        try {
          channel.exchangeDeclarePassive(queueOrTopicName);
        } catch (IOException e) {
          error = "exchange "+queueOrTopicName+" doesn't exist. Either create the exchange or set the autoCreateObjects to true.";
        }
      }
    } else if (queueType == GrouperMessageQueueType.queue) {
        if (grouperMessageSystemParam.isAutocreateObjects()) {
          channel.queueDeclare(queueOrTopicName, true, false, false, null);
        } else {
          try {
            channel.queueDeclarePassive(queueOrTopicName);
          } catch (IOException e) {
            error = "queue "+queueOrTopicName+" doesn't exist. Either create the queue or set the autoCreateObjects to true.";
          }
        }
    }
    return error;
  }
  
  public void closeConnection(String messagingSystemName) {
    connectionFactory.closeConnection(messagingSystemName);
  }
  
}