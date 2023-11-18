/**
 * @author vsachdeva
 * $Id$
 */
package edu.internet2.middleware.grouperMessagingAWS;

import static edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType.queue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueParam;
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


public class GrouperMessagingSqsSystem implements GrouperMessagingSystem {
   
  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperMessagingSqsSystem.class);
  
  private static final String FIFO_QUEUE_SUFFIX = ".fifo";
  
  private static final String DEFAULT_MESSAGE_GROUP_ID = "grouperMessageGroup";
  
  public GrouperMessagingSqsSystem() {}


  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#send(edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam)
   */
  public GrouperMessageSendResult send(GrouperMessageSendParam grouperMessageSendParam) {
        
    GrouperMessageSystemParam systemParam = grouperMessageSendParam.getGrouperMessageSystemParam();
    GrouperMessageQueueParam queueParam = grouperMessageSendParam.getGrouperMessageQueueParam();
    
    validate(queueParam, systemParam);
    
    String queueName = queueParam.getQueueOrTopicName();
    
    AmazonSQS sqs = AmazonSqsClientConnectionFactory.INSTANCE.getAmazonSqsClient(systemParam.getMessageSystemName());
    
    String queueUrl = getQueueUrl(systemParam.getMessageSystemName(), queueName);
    
    for (GrouperMessage grouperMessage: GrouperClientUtils.nonNull(grouperMessageSendParam.getGrouperMessages())) {
      
      SendMessageRequest sendMessageRequest = new SendMessageRequest(queueUrl, grouperMessage.getMessageBody());
      if (isQueueFIFO(queueName)) {
        sendMessageRequest.setMessageGroupId(DEFAULT_MESSAGE_GROUP_ID);
        sendMessageRequest.setMessageDeduplicationId(UUID.randomUUID().toString());
      }
      
      sqs.sendMessage(sendMessageRequest);
      LOG.info("Sent "+grouperMessage.getMessageBody()+" to SQS.");
    }
 
    return new GrouperMessageSendResult();
  }
  
  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#acknowledge(edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam)
   */
  public GrouperMessageAcknowledgeResult acknowledge(GrouperMessageAcknowledgeParam grouperMessageAcknowledgeParam) {
    
    GrouperMessageSystemParam systemParam = grouperMessageAcknowledgeParam.getGrouperMessageSystemParam();
    GrouperMessageQueueParam queueParam = grouperMessageAcknowledgeParam.getGrouperMessageQueueParam();
    
    validate(queueParam, systemParam);
    
    String queueOrTopicName = queueParam.getQueueOrTopicName();
    
    if (grouperMessageAcknowledgeParam.getAcknowledgeType() == null) {
      throw new IllegalArgumentException("acknowlegeType property cannot be null.");
    }
    
    AmazonSQS sqs = AmazonSqsClientConnectionFactory.INSTANCE.getAmazonSqsClient(systemParam.getMessageSystemName());
    String queueUrl = getQueueUrl(systemParam.getMessageSystemName(), queueOrTopicName);
    
    for (GrouperMessage grouperMessage: GrouperClientUtils.nonNull(grouperMessageAcknowledgeParam.getGrouperMessages())) {
      String id = grouperMessage.getId();
      if (StringUtils.isBlank(id)) {
        throw new IllegalArgumentException("id cannot be null in a message");
      }
        
      switch(grouperMessageAcknowledgeParam.getAcknowledgeType()) {
        
        case mark_as_processed:
          sqs.deleteMessage(queueUrl, id);
          break;
        case return_to_end_of_queue:
          if (isQueueFIFO(queueOrTopicName)) {
            sqs.deleteMessage(queueUrl, id);
            send(new GrouperMessageSendParam().assignGrouperMessageQueueParam(
                grouperMessageAcknowledgeParam.getGrouperMessageQueueParam())
                .assignGrouperMessageSystemParam(grouperMessageAcknowledgeParam.getGrouperMessageSystemParam())
                .addMessageBody(grouperMessage.getMessageBody()));
          } else {
            LOG.warn("return_to_end_of_queue can only work with FIFO queues.");
          }
          break;
        case return_to_queue:
          sqs.changeMessageVisibility(queueUrl, id, 0);
          break;
        case send_to_another_queue:
          
          send(new GrouperMessageSendParam().assignGrouperMessageQueueParam(
              grouperMessageAcknowledgeParam.getGrouperMessageAnotherQueueParam())
              .assignGrouperMessageSystemParam(grouperMessageAcknowledgeParam.getGrouperMessageSystemParam())
              .addMessageBody(grouperMessage.getMessageBody()));
          
          sqs.deleteMessage(queueUrl, id);
          break;
      }
    }
    return new GrouperMessageAcknowledgeResult();
  }
  
  private boolean isQueueFIFO(String queueName) {
    return queueName.endsWith(FIFO_QUEUE_SUFFIX);
  }

  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#receive(edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam)
   */
  public GrouperMessageReceiveResult receive(GrouperMessageReceiveParam grouperMessageReceiveParam) {
    
    GrouperMessageSystemParam systemParam = grouperMessageReceiveParam.getGrouperMessageSystemParam();
    GrouperMessageQueueParam queueParam = grouperMessageReceiveParam.getGrouperMessageQueueParam();
    
    validate(queueParam, systemParam);
   
    GrouperMessagingConfig grouperMessagingConfig = GrouperClientConfig.retrieveConfig().retrieveGrouperMessagingConfigNonNull(systemParam.getMessageSystemName());
    int defaultPageSize = grouperMessagingConfig.propertyValueInt(GrouperClientConfig.retrieveConfig(), "defaultPageSize", 5);
    int maxPageSize = grouperMessagingConfig.propertyValueInt(GrouperClientConfig.retrieveConfig(), "maxPageSize", 5);
    
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
    
    GrouperMessageReceiveResult result = new GrouperMessageReceiveResult();
    Collection<GrouperMessage> messages = new ArrayList<GrouperMessage>();
    result.setGrouperMessages(messages);
    
    AmazonSQS sqs = AmazonSqsClientConnectionFactory.INSTANCE.getAmazonSqsClient(systemParam.getMessageSystemName());
    
    String queueUrl = getQueueUrl(systemParam.getMessageSystemName(), queueOrTopicName);
    
    Integer waitTimeSeconds = longPollMillis/1000;
    ReceiveMessageRequest messageRequest = new ReceiveMessageRequest(queueUrl)
        .withMaxNumberOfMessages(maxMessagesToReceiveAtOnce)
        .withWaitTimeSeconds(waitTimeSeconds);
    List<Message> sqsMessages = sqs.receiveMessage(messageRequest).getMessages();
    
    for (Message message: sqsMessages) {
      GrouperMessageSqs sqsMessage = new GrouperMessageSqs(message.getBody(), message.getReceiptHandle());
      messages.add(sqsMessage);
    }
    
    LOG.info("Received "+sqsMessages.size()+" messages.");
    
    return result;
  }
  
  private void validate(GrouperMessageQueueParam queueParam, GrouperMessageSystemParam systemParam) {
    
    if (queueParam == null) {
      throw new IllegalArgumentException("grouperMessageQueueParam cannot be null.");
    }
    
    if (systemParam == null) {
      throw new IllegalArgumentException("grouperMessageSystemParam cannot be null.");
    }
    
    String queueName = queueParam.getQueueOrTopicName();
    
    if (StringUtils.isBlank(queueName)) {
      throw new IllegalArgumentException("queueOrTopicName is a required field.");
    }
    
    if (StringUtils.isBlank(systemParam.getMessageSystemName())) {
      throw new IllegalArgumentException("messageSystemName is a required field.");
    }
    
    if (queueParam.getQueueType() != queue) {
    	LOG.warn("For AWS SQS, only queue type is allowed.");
    }
    
    if (systemParam.isAutocreateObjects()) {
      LOG.warn("For AWS, setting autoCreateObjects to true does nothing. Queue needs to exist already.");
    }
    
  }
  
  
  /**
   * @param messageSystemName
   * @param queueName
   * @return queueUrl
   * @throws IllegalArgumentException if queue doesn't exist already
   */
  private String getQueueUrl(String messageSystemName, String queueName) {
    
    AmazonSQS sqs = AmazonSqsClientConnectionFactory.INSTANCE.getAmazonSqsClient(messageSystemName);
    try {
      GetQueueUrlResult getQueueUrlResult = sqs.getQueueUrl(queueName);
      return getQueueUrlResult.getQueueUrl();
    } catch (QueueDoesNotExistException e) {
      throw new IllegalArgumentException("queue "+queueName+" doesn't exist.");
    }
    
  }
  
  private enum AmazonSqsClientConnectionFactory {
    
    INSTANCE;
    
    private Map<String, AmazonSQS> messagingSystemNameConnection = new HashMap<String, AmazonSQS>();
           
    private AmazonSQS getAmazonSqsClient(String messagingSystemName) {
      
      if (edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils.isBlank(messagingSystemName)) {
        throw new IllegalArgumentException("messagingSystemName is required.");
      }
      
      AmazonSQS sqs =  messagingSystemNameConnection.get(messagingSystemName);
      
      synchronized(AmazonSqsClientConnectionFactory.class) {
        
        if (sqs == null) {
          
          GrouperMessagingConfig grouperMessagingConfig = GrouperClientConfig.retrieveConfig().retrieveGrouperMessagingConfigNonNull(messagingSystemName);
          String accessKey = grouperMessagingConfig.propertyValueString(GrouperClientConfig.retrieveConfig(), "accessKey");
          String secretKey = grouperMessagingConfig.propertyValueString(GrouperClientConfig.retrieveConfig(), "secretKey");
          
          accessKey = GrouperClientUtils.decryptFromFileIfFileExists(accessKey, null);
          secretKey = GrouperClientUtils.decryptFromFileIfFileExists(secretKey, null);
          
          AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
          AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
          sqs = AmazonSQSClientBuilder.standard().withCredentials(credentialsProvider).build();
          
          messagingSystemNameConnection.put(messagingSystemName, sqs);
            
        }
      }
      return sqs;
    }
    
  }
  
}