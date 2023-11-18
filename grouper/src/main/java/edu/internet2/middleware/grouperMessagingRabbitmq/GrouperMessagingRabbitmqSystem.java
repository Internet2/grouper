/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperMessagingRabbitmq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
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
          exchangeType, grouperMessageSendParam.getGrouperMessageQueueParam().getQueueType(),
              grouperMessageSendParam.getGrouperMessageQueueParam().getQueueArguments());
      
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
    
    final Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "receive");
    long startNanos = System.nanoTime();
    
    GrouperMessageReceiveResult result = new GrouperMessageReceiveResult();
    GrouperMessageSystemParam grouperMessageSystemParam = grouperMessageReceiveParam.getGrouperMessageSystemParam();

    try {
    
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

      debugMap.put("queueOrTopicName", queueOrTopicName);
      debugMap.put("exchangeType", exchangeType);
      debugMap.put("pageSize", pageSize);


      Integer longPollMillis = grouperMessageReceiveParam.getLongPollMilis();
      
      if (longPollMillis == null || longPollMillis < 0) {
        longPollMillis = 1000;
      }

      debugMap.put("longPollMillis", longPollMillis);

      final Collection<GrouperMessage> messages = new ArrayList<GrouperMessage>();
      result.setGrouperMessages(messages);
      
      Connection connection = connectionFactory.getConnection(grouperMessageSystemParam.getMessageSystemName());
      final Channel channel = connection.createChannel();
      
      final Thread outerThread = Thread.currentThread();
      final boolean[] longPollDone = new boolean[] {false};
      Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
          try {
            
            if (longPollDone[0]) {
              return;
            }
            
            synchronized (outerThread) {
              String message = new String(body, "UTF-8");
              GrouperMessageRabbitmq rabbitmqMessage = new GrouperMessageRabbitmq(message, properties.getMessageId());
              messages.add(rabbitmqMessage);
              channel.basicAck(envelope.getDeliveryTag(), false);
              if (messages.size() >= pageSize) {
                if (channel.isOpen()) {
                  channel.close();
                }
                // not sleep anymore
                outerThread.interrupt();
              }
              if (LOG.isDebugEnabled()) {
                LOG.debug("message: " + message);
              }
            }
          } catch (TimeoutException e) {
            debugMap.put("receiveException", GrouperClientUtils.getFullStackTrace(e));
            LOG.error("Error occurred while closing channel", e);
          }
        }
      };
      
      String error = createQueueOrExchange(grouperMessageSystemParam, channel, queueOrTopicName, 
          exchangeType, grouperMessageReceiveParam.getGrouperMessageQueueParam().getQueueType(),
          grouperMessageReceiveParam.getGrouperMessageQueueParam().getQueueArguments());
      
      debugMap.put("createQueueOrExchangeError", error);

      if (error != null) {
        throw new IllegalArgumentException(error);
      }
      
      if (grouperMessageReceiveParam.getGrouperMessageQueueParam().getQueueType() == GrouperMessageQueueType.topic) {
        debugMap.put("topic", true);
        DeclareOk declareOk = channel.queueDeclare();
        channel.queueBind(declareOk.getQueue(), queueOrTopicName, StringUtils.defaultString(grouperMessageReceiveParam.getRoutingKey(), ""));
        channel.basicConsume(declareOk.getQueue(), false, consumer);
      } else if (grouperMessageReceiveParam.getGrouperMessageQueueParam().getQueueType() == GrouperMessageQueueType.queue) {
        debugMap.put("queue", true);
        channel.basicConsume(queueOrTopicName, false, consumer);
      }
      try {
        Thread.sleep(longPollMillis);
        debugMap.put("finishedLongPoll", true);
        
      } catch (InterruptedException ie) {
        debugMap.put("finishedLongPoll", false);
        //messages were received
      }
      longPollDone[0] = true;
      
      //if messages werent received
      synchronized (outerThread) {
        if (channel.isOpen()) {
          channel.close();
        }
      }

      debugMap.put("messageCount", messages.size());

    } catch(Exception e) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(e));
      throw new RuntimeException("Error occurred while trying to receive messages for "+grouperMessageSystemParam.getMessageSystemName(), e);
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("tookMillis", ((System.nanoTime() - startNanos) / 1000000L));
        LOG.debug(GrouperClientUtils.mapToString(debugMap));
      }
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
      Channel channel, String queueOrTopicName, String exchangeType, GrouperMessageQueueType queueType,
      Map<String, Object> queueArguments) throws IOException {
    
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
          channel.queueDeclare(queueOrTopicName, true, false, false, queueArguments);
        } else {
          try {
            channel.queueDeclarePassive(queueOrTopicName);
          } catch (IOException e) {
            error = "queue "+queueOrTopicName+" doesn't exist. Either create the queue or set the autoCreateObjects to true.";
          }
        }
    } else {
      error = "queue type not defined. Must be either queue or topic.";
    }
    return error;
  }
  
  public void closeConnection(String messagingSystemName) {
    connectionFactory.closeConnection(messagingSystemName);
  }
  
}