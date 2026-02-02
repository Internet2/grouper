/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.messaging;


import java.util.HashMap;
import java.util.Map;

/**
 * method chaining receive message config
 */
public class GrouperMessageQueueParam {

  /**
   * 
   */
  public GrouperMessageQueueParam() {
  }

  /**
   * queue type: queue or topic
   */
  private GrouperMessageQueueType queueType;

  /**
   * queue arguments (e.g. "x-queue-type" -> "quorum")
   */
  private Map<String, Object> queueArguments;

  /**
   * assign the queue type
   * @param theGrouperMessageQueueType
   * @return this for chaining
   */
  public GrouperMessageQueueParam assignQueueType(GrouperMessageQueueType theGrouperMessageQueueType) {
    this.queueType = theGrouperMessageQueueType;
    return this;
  }
  
  /**
   * assign the queue type
   * @return the queueType
   */
  public GrouperMessageQueueType getQueueType() {
    return this.queueType;
  }

  /**
   * queue or topic name
   */
  private String queueOrTopicName;
  
  /**
   * queue or topic name
   * @param theQueueOrTopicName1
   * @return this for chaining
   */
  public GrouperMessageQueueParam assignQueueOrTopicName(String theQueueOrTopicName1) {
    this.queueOrTopicName = theQueueOrTopicName1;
    return this;
  }

  /**
   * queue or topic name
   * @return the queue or topic
   */
  public String getQueueOrTopicName() {
    return this.queueOrTopicName;
  }

  /**
   * optional queue argument map
   * @param theQueueArguments
   * @return this for chaining
   */
  public GrouperMessageQueueParam assignQueueArguments(Map<String, Object> theQueueArguments) {
    this.queueArguments = theQueueArguments;
    return this;
  }

  /**
   * optional queue argument map
   * @return the argument map
   */
  public Map<String, Object> getQueueArguments() {
    return queueArguments;
  }
}
