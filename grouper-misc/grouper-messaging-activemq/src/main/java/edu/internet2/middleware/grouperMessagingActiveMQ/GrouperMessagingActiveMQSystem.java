/**
 * @author vsachdeva
 * $Id$
 */
package edu.internet2.middleware.grouperMessagingActiveMQ;

import static edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType.topic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.qpid.jms.JmsConnectionFactory;

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
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingConfig;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;


public class GrouperMessagingActiveMQSystem implements GrouperMessagingSystem {
   
  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperMessagingActiveMQSystem.class);
  
  public GrouperMessagingActiveMQSystem() {}
  
  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#send(edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam)
   */
  public GrouperMessageSendResult send(GrouperMessageSendParam grouperMessageSendParam) {
        
    GrouperMessageSystemParam systemParam = grouperMessageSendParam.getGrouperMessageSystemParam();
    GrouperMessageQueueParam queueParam = grouperMessageSendParam.getGrouperMessageQueueParam();
    
    validate(queueParam, systemParam);
    
    String queueOrTopicName = queueParam.getQueueOrTopicName();
    
    try {
      Connection connection = ActiveMQClientConnectionFactory.INSTANCE.getActiveMQConnection(systemParam.getMessageSystemName());
      
      // Create a non-transactional session with automatic acknowledgement
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      
      Destination destination = null;
      if (queueParam.getQueueType() == GrouperMessageQueueType.queue) {
        destination = session.createQueue(queueOrTopicName);             
      } else {
        destination = session.createTopic(queueOrTopicName);
      }

      MessageProducer producer = session.createProducer(destination);
      
      for (GrouperMessage grouperMessage: GrouperClientUtils.nonNull(grouperMessageSendParam.getGrouperMessages())) {
        
        TextMessage message = session.createTextMessage (grouperMessage.getMessageBody());
        producer.send(message);
        LOG.info("Sent "+grouperMessage.getMessageBody()+" to ActiveMQ.");
        
      }
    } catch (JMSException e) {
      LOG.error("Error occurred while sending message to messaging system name: "+systemParam.getMessageSystemName(), e);
    }
    
    return new GrouperMessageSendResult();
  }
  
  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#acknowledge(edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam)
   */
  public GrouperMessageAcknowledgeResult acknowledge(GrouperMessageAcknowledgeParam grouperMessageAcknowledgeParam) {
    return new GrouperMessageAcknowledgeResult();
  }

  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#receive(edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam)
   */
  public GrouperMessageReceiveResult receive(GrouperMessageReceiveParam grouperMessageReceiveParam) {
    
    final GrouperMessageSystemParam systemParam = grouperMessageReceiveParam.getGrouperMessageSystemParam();
    final GrouperMessageQueueParam queueParam = grouperMessageReceiveParam.getGrouperMessageQueueParam();
    
    validate(queueParam, systemParam);
    
    GrouperMessagingConfig grouperMessagingConfig = GrouperClientConfig.retrieveConfig().retrieveGrouperMessagingConfigNonNull(systemParam.getMessageSystemName());
    int defaultPageSize = grouperMessagingConfig.propertyValueInt(GrouperClientConfig.retrieveConfig(), "defaultPageSize", 5);
    int maxPageSize = grouperMessagingConfig.propertyValueInt(GrouperClientConfig.retrieveConfig(), "maxPageSize", 10);
    
    Integer maxMessagesToReceiveAtOnce = grouperMessageReceiveParam.getMaxMessagesToReceiveAtOnce();
    
    if (maxMessagesToReceiveAtOnce == null) {
      maxMessagesToReceiveAtOnce = defaultPageSize;
    }
    
    if (maxMessagesToReceiveAtOnce > maxPageSize) {
      maxMessagesToReceiveAtOnce = maxPageSize;
    }
    
    String queueOrTopicName = queueParam.getQueueOrTopicName();
    
    Integer longPollMillis = grouperMessageReceiveParam.getLongPollMilis();
    
    if (longPollMillis == null || longPollMillis < 0) {
      longPollMillis = 1000;
    }
    
    long startReceive = System.currentTimeMillis();
    int pollSleepSeconds = GrouperClientConfig.retrieveConfig().propertyValueInt(String.format("grouper.%s.messaging.polling.sleep.seconds", systemParam.getMessageSystemName()), 5);
    if (pollSleepSeconds < 1) {
      pollSleepSeconds = 1;
    }
    GrouperMessageReceiveResult result = new GrouperMessageReceiveResult();
    final Collection<GrouperMessage> messages = new ArrayList<GrouperMessage>();
    result.setGrouperMessages(messages);
    
    try {
      Connection connection = ActiveMQClientConnectionFactory.INSTANCE.getActiveMQConnection(systemParam.getMessageSystemName());
      
      Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      
      Destination destination = null;
      if (queueParam.getQueueType() == topic) {
        destination = session.createTopic(queueOrTopicName);
      } else {
        destination = session.createQueue(queueOrTopicName);
      }
      
      MessageConsumer consumer = session.createConsumer(destination);
      
      consumer.setMessageListener(new MessageListener() {
        @Override
        public void onMessage(Message message) {
          if (message instanceof TextMessage) {
            String body = null;
            try {
              body = ((TextMessage) message).getText();
              messages.add(new GrouperMessageActiveMQ(body, ((TextMessage) message).getJMSMessageID()));
              message.acknowledge();
            } catch (JMSException e) {
              LOG.error("Error occurred while receiving messages for messaging system name "+systemParam.getMessageSystemName(), e);
            }
          } else {
            LOG.error("Only text messages are allowed with ActiveMQ");
          }
        }
      });
      
      while (true) {
        //dont long poll
        if (longPollMillis < 0) {
          break;
        }
        if (longPollMillis < System.currentTimeMillis() - startReceive ) {
          break;
        }
        GrouperClientUtils.sleep(Math.min(pollSleepSeconds*1000, System.currentTimeMillis() + 20 - startReceive));
      }
      
    } catch(JMSException e) {
      LOG.error("Error occurred while receiving messages for system name "+systemParam.getMessageSystemName(), e);
    }
    
    return result;
  }
  
  private void validate(GrouperMessageQueueParam queueParam, GrouperMessageSystemParam systemParam) {
    
    if (queueParam == null) {
      throw new IllegalArgumentException("grouperMessageQueueParam cannot be null.");
    }
    
    if (systemParam == null) {
      throw new IllegalArgumentException("grouperMessageSystemParam cannot be null.");
    }
    
    if (queueParam.getQueueType() == null) {
      throw new IllegalArgumentException("queueType is a required field.");
    }
    
    String queueName = queueParam.getQueueOrTopicName();
    
    if (StringUtils.isBlank(queueName)) {
      throw new IllegalArgumentException("queueOrTopicName is a required field.");
    }
    
    if (StringUtils.isBlank(systemParam.getMessageSystemName())) {
      throw new IllegalArgumentException("messageSystemName is a required field.");
    }
    
    if (!systemParam.isAutocreateObjects()) {
      throw new IllegalArgumentException("For ActiveMQ, autoCreateObjects has to be set to true.");
    }
    
  }
  
  
  void closeConnection(String messagingSystemName) throws JMSException {
    ActiveMQClientConnectionFactory.INSTANCE.closeConnection(messagingSystemName);
  }
  
  private enum ActiveMQClientConnectionFactory {
    
    INSTANCE;
    
    private Map<String, Connection> messagingSystemNameConnection = new HashMap<String, Connection>();
           
    private Connection getActiveMQConnection(String messagingSystemName) throws JMSException {
      
      if (StringUtils.isBlank(messagingSystemName)) {
        throw new IllegalArgumentException("messagingSystemName is required.");
      }
      
      Connection connection =  messagingSystemNameConnection.get(messagingSystemName);
      
      synchronized(ActiveMQClientConnectionFactory.class) {
        
        if (connection == null) {
          
          GrouperMessagingConfig grouperMessagingConfig = GrouperClientConfig.retrieveConfig().retrieveGrouperMessagingConfigNonNull(messagingSystemName);

          String host = grouperMessagingConfig.propertyValueString(GrouperClientConfig.retrieveConfig(), "host");
          String username = grouperMessagingConfig.propertyValueString(GrouperClientConfig.retrieveConfig(), "username");
          String password = grouperMessagingConfig.propertyValueString(GrouperClientConfig.retrieveConfig(), "password");
          
          if (StringUtils.isNotBlank(password)) {
            password = GrouperClientUtils.decryptFromFileIfFileExists(password, null);
          }
          Integer port = grouperMessagingConfig.propertyValueInt(GrouperClientConfig.retrieveConfig(), "port", -1);
          
          String connectionUrl = "amqp://"+host+":"+port;
          JmsConnectionFactory factory = new JmsConnectionFactory(connectionUrl);
          if (StringUtils.isNotBlank(username)) {
            factory.setUsername(username);
          }
          if (StringUtils.isNotBlank(password)) {
            factory.setPassword(password);
          }
          
          connection = factory.createConnection();
          connection.start();
          messagingSystemNameConnection.put(messagingSystemName, connection);
            
        }
      }
      return connection;
    }
    
    private void closeConnection(String messagingSystemName) throws JMSException {
      if (StringUtils.isBlank(messagingSystemName)) {
        throw new IllegalArgumentException("messagingSystemName is required.");
      }
      Connection connection = messagingSystemNameConnection.get(messagingSystemName);
      synchronized(ActiveMQConnectionFactory.class) {
        if (connection != null) {
          connection.stop();
          connection = null;         
        }
      }
    }
    
  }
  
}