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
 * param to mark a message as processed or return to queue or whatever
 */
public class GrouperMessageAcknowledgeParam {

  /**
   * describes the queue or topic
   */
  private GrouperMessageQueueParam grouperMessageQueueParam;

  /**
   * describes the queue or topic to send this one to
   */
  private GrouperMessageQueueParam grouperMessageAnotherQueueParam;

  /**
   * another queue or topic to send this message to
   * @param anotherQueueParam
   * @return this for chaining
   */
  public GrouperMessageAcknowledgeParam assignAnotherQueueParam(GrouperMessageQueueParam anotherQueueParam) {
    this.grouperMessageAnotherQueueParam = anotherQueueParam;
    return this;
  }

  /**
   * another queue or topic to send this message to
   * @return the grouperMessageAnotherQueueParam
   */
  public GrouperMessageQueueParam getGrouperMessageAnotherQueueParam() {
    return this.grouperMessageAnotherQueueParam;
  }



  /**
   * message body for the message
   */
  private List<GrouperMessage> grouperMessages = new ArrayList<GrouperMessage>();
  /**
   * describes the grouper message system
   */
  private GrouperMessageSystemParam grouperMessageSystemParam;

  /**
   * acknowledgeType
   */
  private GrouperMessageAcknowledgeType acknowledgeType;

  /**
   * type of acknowledgement
   * @param grouperMessageAcknowledgeType
   * @return this for chaining
   */
  public GrouperMessageAcknowledgeParam assignAcknowledgeType(GrouperMessageAcknowledgeType grouperMessageAcknowledgeType) {
    this.acknowledgeType = grouperMessageAcknowledgeType;
    return this;
  }
  
  /**
   * acknowledgeType
   * @return the acknowledgeType
   */
  public GrouperMessageAcknowledgeType getAcknowledgeType() {
    return this.acknowledgeType;
  }



  /**
   * 
   */
  public GrouperMessageAcknowledgeParam() {
  }

  /**
   * add a grouper message to send
   * @param theGrouperMessage
   * @return this for chaining
   */
  public GrouperMessageAcknowledgeParam addGrouperMessage(GrouperMessage theGrouperMessage) {
    this.grouperMessages.add(theGrouperMessage);
    return this;
  }

  /**
   * assign the grouper messaging system
   * @param theGrouperMessageSystemName
   * @return this for chaining
   */
  public GrouperMessageAcknowledgeParam assignGrouperMessageSystemName(String theGrouperMessageSystemName) {
    if (this.grouperMessageSystemParam == null) {
      this.grouperMessageSystemParam = new GrouperMessageSystemParam();
    }
    this.grouperMessageSystemParam.assignMesssageSystemName(theGrouperMessageSystemName);
    return this;
  }

  /**
   * 
   * @param theGrouperMessageQueueParam
   * @return this for chaining
   */
  public GrouperMessageAcknowledgeParam assignGrouperMessageQueueParam(GrouperMessageQueueParam theGrouperMessageQueueParam) {
    this.grouperMessageQueueParam = theGrouperMessageQueueParam;
    return this;
  }

  /**
   * assign grouper messages to send
   * @param theGrouperMessages
   * @return this for chaining
   */
  public GrouperMessageAcknowledgeParam assignGrouperMessages(Collection<GrouperMessage> theGrouperMessages) {
    
    this.grouperMessages.clear();
    for (GrouperMessage theMessage : GrouperClientUtils.nonNull(theGrouperMessages)) {
      this.addGrouperMessage(theMessage);
    }
    return this;
  }

  /**
   * assign th grouper message system param
   * @param theGrouperMessageSystemParam
   * @return this for chaining
   */
  public GrouperMessageAcknowledgeParam assignGrouperMessageSystemParam(GrouperMessageSystemParam theGrouperMessageSystemParam) {
    this.grouperMessageSystemParam = theGrouperMessageSystemParam;
    return this;
  }

  /**
   * assign queue or topic to send the message to
   * @param theQueue
   * @return this for chaining
   */
  public GrouperMessageAcknowledgeParam assignQueueName(String theQueue) {
    if (this.grouperMessageQueueParam == null) {
      this.grouperMessageQueueParam = new GrouperMessageQueueParam();
    }
    this.grouperMessageQueueParam.assignQueueOrTopicName(theQueue);
    return this;
  }

  /**
   * @return the grouperMessageQueueParam
   */
  public GrouperMessageQueueParam getGrouperMessageQueueParam() {
    return this.grouperMessageQueueParam;
  }

  /**
   * get the grouper messages
   * @return messages
   */
  public Collection<GrouperMessage> getGrouperMessages() {
    return this.grouperMessages;
  }

  /**
   * @return the grouperMessageSystemParam
   */
  public GrouperMessageSystemParam getGrouperMessageSystemParam() {
    return this.grouperMessageSystemParam;
  }

}
