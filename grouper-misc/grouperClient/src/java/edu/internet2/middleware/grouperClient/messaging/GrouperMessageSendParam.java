/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.messaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * param for sending a message
 */
public class GrouperMessageSendParam {

  /**
   * if the messaging system can use a routing key (e.g. rabbitmq), set it here
   */
  private String routingKey;
  
  /**
   * if the messaging system can use a routing key (e.g. rabbitmq), set it here
   * @param theRoutingKey
   * @return this for chaining
   */
  public GrouperMessageSendParam assignRoutingKey(String theRoutingKey) {
    this.routingKey = theRoutingKey;
    return this;
  }
  
  /**
   * if the messaging system can use a routing key (e.g. rabbitmq), set it here
   * @return the routing key
   */
  public String getRoutingKey() {
    return this.routingKey;
  }
  
  /**
   * if the messaging system can use exchange type (e.g. rabbitmq)
   */
  private String exchangeType;
  
  /**
   * if the messaging system can use exchange type (e.g. rabbitmq), set it here
   * @param exchangeType
   * @return this for chaining
   */
  public GrouperMessageSendParam assignExchangeType(String exchangeType) {
    this.exchangeType = exchangeType;
    return this;
  }
  
  /**
   * if the messaging system can use exchange type (e.g. rabbitmq)
   * @return exchange type
   */
  public String getExchangeType() {
    return this.exchangeType;
  }
  
  /**
   * describes the grouper message system
   */
  private GrouperMessageSystemParam grouperMessageSystemParam;
  
  /**
   * assign th grouper message system param
   * @param theGrouperMessageSystemParam
   * @return this for chaining
   */
  public GrouperMessageSendParam assignGrouperMessageSystemParam(GrouperMessageSystemParam theGrouperMessageSystemParam) {
    this.grouperMessageSystemParam = theGrouperMessageSystemParam;
    return this;
  }
  
  /**
   * assign the grouper messaging system
   * @param theGrouperMessageSystemName
   * @return this for chaining
   */
  public GrouperMessageSendParam assignGrouperMessageSystemName(String theGrouperMessageSystemName) {
    if (this.grouperMessageSystemParam == null) {
      this.grouperMessageSystemParam = new GrouperMessageSystemParam();
    }
    this.grouperMessageSystemParam.assignMesssageSystemName(theGrouperMessageSystemName);
    return this;
  }
  
  /**
   * if objects should be auto created if not there, e.g. 
   * queues, topics, privileges
   * @param theAutocreate
   * @return this for chaining
   */
  public GrouperMessageSendParam assignAutocreateObjects(boolean theAutocreate) {
    if (this.grouperMessageSystemParam == null) {
      this.grouperMessageSystemParam = new GrouperMessageSystemParam();
    }
    this.grouperMessageSystemParam.assignAutocreateObjects(theAutocreate);
    return this;
  }

  /**
   * describes the queue or topic
   */
  private GrouperMessageQueueParam grouperMessageQueueParam;

  /**
   * 
   * @param theGrouperMessageQueueParam
   * @return this for chaining
   */
  public GrouperMessageSendParam assignGrouperMessageQueueParam(GrouperMessageQueueParam theGrouperMessageQueueParam) {
    this.grouperMessageQueueParam = theGrouperMessageQueueParam;
    return this;
  }

  /**
   * assign queue or topic to send the message to
   * @param theQueueOrTopicName
   * @return this for chaining
   */
  public GrouperMessageSendParam assignQueueOrTopicName(String theQueueOrTopicName) {
    if (this.grouperMessageQueueParam == null) {
      this.grouperMessageQueueParam = new GrouperMessageQueueParam();
    }
    this.grouperMessageQueueParam.assignQueueOrTopicName(theQueueOrTopicName);
    return this;
  }
  
  /**
   * assign if queue or topic
   * @param grouperMessageQueueType
   * @return this for chaining
   */
  public GrouperMessageSendParam assignQueueType(GrouperMessageQueueType grouperMessageQueueType) {
    if (this.grouperMessageQueueParam == null) {
      this.grouperMessageQueueParam = new GrouperMessageQueueParam();
    }
    this.grouperMessageQueueParam.assignQueueType(grouperMessageQueueType);
    return this;
  }


  /**
   * message body for the message
   */
  private List<GrouperMessage> grouperMessages = new ArrayList<GrouperMessage>();

  /**
   * message body for the message
   * @param theMessageBody
   * @return this for chaining
   */
  public GrouperMessageSendParam addMessageBody(String theMessageBody) {
    GrouperMessage grouperMessage = new GrouperMessageDefault();
    grouperMessage.setMessageBody(theMessageBody);
    this.grouperMessages.add(grouperMessage);
    return this;
  }

  
  /**
   * @return the grouperMessageSystemParam
   */
  public GrouperMessageSystemParam getGrouperMessageSystemParam() {
    return this.grouperMessageSystemParam;
  }

  
  /**
   * @return the grouperMessageQueueParam
   */
  public GrouperMessageQueueParam getGrouperMessageQueueParam() {
    return this.grouperMessageQueueParam;
  }

  /**
   * assign multiple message bodies
   * @param theMessageBodies
   * @return this for chaining
   */
  public GrouperMessageSendParam assignMessageBodies(Collection<String> theMessageBodies) {
    
    this.grouperMessages.clear();
    for (String theMessageBody : GrouperClientUtils.nonNull(theMessageBodies)) {
      this.addMessageBody(theMessageBody);
    }
    return this;
  }
  
  /**
   * add a grouper message to send
   * @param theGrouperMessage
   * @return this for chaining
   */
  public GrouperMessageSendParam addGrouperMessage(GrouperMessage theGrouperMessage) {
    this.grouperMessages.add(theGrouperMessage);
    return this;
  }
  
  /**
   * assign grouper messages to send
   * @param theGrouperMessages
   * @return this for chaining
   */
  public GrouperMessageSendParam assignGrouperMessages(Collection<GrouperMessage> theGrouperMessages) {
    
    this.grouperMessages.clear();
    for (GrouperMessage theMessage : GrouperClientUtils.nonNull(theGrouperMessages)) {
      this.addGrouperMessage(theMessage);
    }
    return this;
  }
  
  /**
   * get the grouper messages
   * @return messages
   */
  public Collection<GrouperMessage> getGrouperMessages() {
    return this.grouperMessages;
  }

  /**
   * 
   */
  public GrouperMessageSendParam() {
  }

}
