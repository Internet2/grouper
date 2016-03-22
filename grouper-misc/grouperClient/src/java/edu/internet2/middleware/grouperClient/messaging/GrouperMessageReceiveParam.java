/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.messaging;


/**
 * method chaining receive message config
 */
public class GrouperMessageReceiveParam {

  /**
   * 
   */
  public GrouperMessageReceiveParam() {
  }
  
  /**
   * maximum number of messages to receive at once
   */
  private Integer maxMessagesToReceiveAtOnce;

  /**
   * how many milliseconds to long poll for a response
   */
  private Integer longPollMilis;

  /**
   * describes the queue or topic
   */
  private GrouperMessageQueueParam grouperMessageQueueParam;

  /**
   * describes the grouper message system
   */
  private GrouperMessageSystemParam grouperMessageSystemParam;

  
  /**
   * maximum number of messages to receive at once
   * @return the maxMessagesToReceiveAtOnce
   */
  public Integer getMaxMessagesToReceiveAtOnce() {
    return this.maxMessagesToReceiveAtOnce;
  }

  /**
   * maximum number of messages to receive at once
   * @param theMaxMessagesToReceiveAtOnce
   * @return this for chaining
   */
  public GrouperMessageReceiveParam assignMaxMessagesToReceiveAtOnce(int theMaxMessagesToReceiveAtOnce) {
    this.maxMessagesToReceiveAtOnce = theMaxMessagesToReceiveAtOnce;
    return this;
  }

  /**
   * how many milliseconds to long poll for a response
   * @return the longPollMilis
   */
  public Integer getLongPollMilis() {
    return this.longPollMilis;
  }

  /**
   * how many milliseconds to long poll for a response
   * @param theLongPollMillis
   * @return this for chaining
   */
  public GrouperMessageReceiveParam assignLongPollMillis(int theLongPollMillis) {
    this.longPollMilis = theLongPollMillis;
    return this;
  }

  /**
   * assign the grouper messaging system
   * @param theGrouperMessageSystemName
   * @return this for chaining
   */
  public GrouperMessageReceiveParam assignGrouperMessageSystemName(String theGrouperMessageSystemName) {
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
  public GrouperMessageReceiveParam assignGrouperMessageQueueParam(GrouperMessageQueueParam theGrouperMessageQueueParam) {
    this.grouperMessageQueueParam = theGrouperMessageQueueParam;
    return this;
  }

  /**
   * assign th grouper message system param
   * @param theGrouperMessageSystemParam
   * @return this for chaining
   */
  public GrouperMessageReceiveParam assignGrouperMessageSystemParam(GrouperMessageSystemParam theGrouperMessageSystemParam) {
    this.grouperMessageSystemParam = theGrouperMessageSystemParam;
    return this;
  }

  /**
   * assign queue or topic to send the message to
   * @param theQueue
   * @return this for chaining
   */
  public GrouperMessageReceiveParam assignQueue(String theQueue) {
    if (this.grouperMessageQueueParam == null) {
      this.grouperMessageQueueParam = new GrouperMessageQueueParam();
    }
    this.grouperMessageQueueParam.assignQueueOrTopic(theQueue);
    return this;
  }

  /**
   * @return the grouperMessageQueueParam
   */
  public GrouperMessageQueueParam getGrouperMessageQueueParam() {
    return this.grouperMessageQueueParam;
  }

  /**
   * @return the grouperMessageSystemParam
   */
  public GrouperMessageSystemParam getGrouperMessageSystemParam() {
    return this.grouperMessageSystemParam;
  }
  
}
