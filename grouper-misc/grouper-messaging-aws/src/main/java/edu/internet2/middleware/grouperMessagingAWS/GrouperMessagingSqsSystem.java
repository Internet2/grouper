/**
 * @author vsachdeva
 * $Id$
 */
package edu.internet2.middleware.grouperMessagingAWS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

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


public class GrouperMessagingSqsSystem implements GrouperMessagingSystem {
   
  
  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperMessagingSqsSystem.class);
  
  //private MessageReceiveEventListener listener;
  
  private static final Integer MAXIMUM_SQS_QUEUE_NAME_LENGTH = 80;
  
  public GrouperMessagingSqsSystem() {}

  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#send(edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam)
   */
  public GrouperMessageSendResult send(GrouperMessageSendParam grouperMessageSendParam) {
        
    if (grouperMessageSendParam.getGrouperMessageQueueParam() == null) {
      throw new IllegalArgumentException("grouperMessageQueueParam is required.");
    }
    
    if (grouperMessageSendParam.getGrouperMessageQueueParam().getQueueType() != GrouperMessageQueueType.queue) {
      throw new IllegalArgumentException("Only queue type is allowed for amazon sqs messaging system.");
    }
    
    String queueName = grouperMessageSendParam.getGrouperMessageQueueParam().getQueueOrTopicName();
    
    if (StringUtils.isBlank(queueName)) {
      throw new IllegalArgumentException("queueOrTopicName is required.");
    }
    
    GrouperMessageSystemParam grouperMessageSystemParam = grouperMessageSendParam.getGrouperMessageSystemParam();
    if (grouperMessageSystemParam == null || StringUtils.isBlank(grouperMessageSystemParam.getMessageSystemName())) {
      throw new IllegalArgumentException("grouperMessageSystemParam.messageSystemName is a required field.");
    }
    
    String error = createSqsQueue(grouperMessageSystemParam, queueName);
    
    if (error != null) {
      throw new IllegalArgumentException(error);
    }
  
    AmazonSQS sqs = AmazonSqsClientConnectionFactory.INSTANCE.getAmazonSqsClient(grouperMessageSystemParam.getMessageSystemName());
    String queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
    
    for (GrouperMessage grouperMessage: GrouperClientUtils.nonNull(grouperMessageSendParam.getGrouperMessages())) {
      sqs.sendMessage(new SendMessageRequest(queueUrl, grouperMessage.getMessageBody()));
      LOG.info("Sent "+grouperMessage.getMessageBody()+" to SQS.");
    }
 
    return new GrouperMessageSendResult();
  }
  
  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#acknowledge(edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam)
   */
  public GrouperMessageAcknowledgeResult acknowledge(GrouperMessageAcknowledgeParam grouperMessageAcknowledgeParam) {
    return new GrouperMessageAcknowledgeResult();
  }
  
//  public void addReceiveEventListener(MessageReceiveEventListener listener) {
//    this.listener = listener;
//  }

  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#receive(edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam)
   */
  public GrouperMessageReceiveResult receive(GrouperMessageReceiveParam grouperMessageReceiveParam) {
    
    GrouperMessageSystemParam grouperMessageSystemParam = grouperMessageReceiveParam.getGrouperMessageSystemParam();
    
    if (grouperMessageSystemParam == null || StringUtils.isBlank(grouperMessageSystemParam.getMessageSystemName())) {
      throw new IllegalArgumentException("grouperMessageSystemParam.messageSystemName is required.");
    }
    
    if (grouperMessageReceiveParam.getGrouperMessageQueueParam().getQueueType() != GrouperMessageQueueType.queue) {
      throw new IllegalArgumentException("Only queue type is allowed for amazon sqs messaging system.");
    }
        
    int defaultPageSize = GrouperClientConfig.retrieveConfig().propertyValueInt(String.format("grouper.%s.messaging.defaultPageSize", grouperMessageSystemParam.getMessageSystemName()), 5);
    int maxPageSize = GrouperClientConfig.retrieveConfig().propertyValueInt(String.format("grouper.%s.messaging.maxPageSize", grouperMessageSystemParam.getMessageSystemName()), 10);
    
    Integer maxMessagesToReceiveAtOnce = grouperMessageReceiveParam.getMaxMessagesToReceiveAtOnce();
    
    if (maxMessagesToReceiveAtOnce == null) {
      maxMessagesToReceiveAtOnce = defaultPageSize;
    }
    
    if (maxMessagesToReceiveAtOnce > maxPageSize) {
      maxMessagesToReceiveAtOnce = maxPageSize;
    }
    
    final Integer pageSize = maxMessagesToReceiveAtOnce;
    
    String queueOrTopicName = grouperMessageReceiveParam.getGrouperMessageQueueParam().getQueueOrTopicName();
    
    if (StringUtils.isBlank(queueOrTopicName)) {
      throw new IllegalArgumentException("queueOrTopicName is required.");
    }
    
    Integer longPollMillis = grouperMessageReceiveParam.getLongPollMilis();
    
    if (longPollMillis == null || longPollMillis < 0) {
      longPollMillis = 1000;
    }
    
    GrouperMessageReceiveResult result = new GrouperMessageReceiveResult();
    Collection<GrouperMessage> messages = new ArrayList<GrouperMessage>();
    result.setGrouperMessages(messages);
    
    String error = createSqsQueue(grouperMessageSystemParam, queueOrTopicName);
    
    if (error != null) {
      throw new IllegalArgumentException(error);
    }
    
    AmazonSQS sqs = AmazonSqsClientConnectionFactory.INSTANCE.getAmazonSqsClient(grouperMessageSystemParam.getMessageSystemName());
    
    String queueUrl = sqs.getQueueUrl(queueOrTopicName).getQueueUrl();
    Integer waitTimeSeconds = longPollMillis/1000;
    ReceiveMessageRequest messageRequest = new ReceiveMessageRequest(queueUrl).withMaxNumberOfMessages(pageSize).withWaitTimeSeconds(waitTimeSeconds);
    List<Message> sqsMessages = sqs.receiveMessage(messageRequest).getMessages();
    
    Collection<DeleteMessageBatchRequestEntry> deleteEntries = new ArrayList<DeleteMessageBatchRequestEntry>();
    
    for (Message message: sqsMessages) {
      GrouperMessageSqs sqsMessage = new GrouperMessageSqs(message.getBody(), message.getMessageId());
      messages.add(sqsMessage);
      
      DeleteMessageBatchRequestEntry deleteMessageBatchRequestEntry = 
          new DeleteMessageBatchRequestEntry(message.getMessageId(), message.getReceiptHandle());
      deleteEntries.add(deleteMessageBatchRequestEntry);
    }
    
    DeleteMessageBatchRequest deleteMessageBatchRequest = new DeleteMessageBatchRequest(queueUrl).withEntries(deleteEntries);
    sqs.deleteMessageBatch(deleteMessageBatchRequest);
    
    LOG.info("Received "+sqsMessages.size()+" messages.");
    
    return result;
  }
  
  
  /**
   * @param grouperMessageSystemParam
   * @param queueName
   * @return error if any
   */
  private String createSqsQueue(GrouperMessageSystemParam grouperMessageSystemParam,
      String queueName) {
    
    if (queueName.length() > MAXIMUM_SQS_QUEUE_NAME_LENGTH) {
      return "queue name cannot have more than "+MAXIMUM_SQS_QUEUE_NAME_LENGTH+" characters.";
    }
    
    AmazonSQS sqs = AmazonSqsClientConnectionFactory.INSTANCE.getAmazonSqsClient(grouperMessageSystemParam.getMessageSystemName());
    try {
      GetQueueUrlResult getQueueUrlResult = sqs.getQueueUrl(queueName);
      if (getQueueUrlResult != null) {
        return null;
      }
    } catch (QueueDoesNotExistException e) {
      //do nothing.
    }
    
    if (grouperMessageSystemParam.isAutocreateObjects()) {
      CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
      sqs.createQueue(createQueueRequest);
      return null;
    } else {
      return "queue "+queueName+" doesn't exist. Either create the queue or set the autoCreateObjects to true.";
    }
  }
  
  private enum AmazonSqsClientConnectionFactory {
    
    INSTANCE;
    
    private Map<String, AmazonSQS> messagingSystemNameConnection = new HashMap<String, AmazonSQS>();
           
    private AmazonSQS getAmazonSqsClient(String messagingSystemName) {
      
      if (StringUtils.isBlank(messagingSystemName)) {
        throw new IllegalArgumentException("messagingSystemName is required.");
      }
      
      AmazonSQS sqs =  messagingSystemNameConnection.get(messagingSystemName);
      
      synchronized(AmazonSqsClientConnectionFactory.class) {
        
        if (sqs == null) {
          
          String accessKey = GrouperClientConfig.retrieveConfig().propertyValueString(String.format("grouper.%s.messaging.accessKey", "sqs"));
          String secretKey = GrouperClientConfig.retrieveConfig().propertyValueString(String.format("grouper.%s.messaging.secretKey", "sqs"));
          
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