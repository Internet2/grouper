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
   * if the messaging system can use a routing key (e.g. rabbitmq), set it here
   */
  private String routingKey;
  
  /**
   * if the messaging system can use a routing key (e.g. rabbitmq), set it here
   * @param theRoutingKey
   * @return this for chaining
   */
  public GrouperMessageReceiveParam assignRoutingKey(String theRoutingKey) {
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
  public GrouperMessageReceiveParam assignExchangeType(String exchangeType) {
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
   * if objects should be auto created if not there, e.g. 
   * queues, topics, privileges
   * @param theAutocreate
   * @return this for chaining
   */
  public GrouperMessageReceiveParam assignAutocreateObjects(boolean theAutocreate) {
    if (this.grouperMessageSystemParam == null) {
      this.grouperMessageSystemParam = new GrouperMessageSystemParam();
    }
    this.grouperMessageSystemParam.assignAutocreateObjects(theAutocreate);
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
   * @param theQueueName
   * @return this for chaining
   */
  public GrouperMessageReceiveParam assignQueueName(String theQueueName) {
    if (this.grouperMessageQueueParam == null) {
      this.grouperMessageQueueParam = new GrouperMessageQueueParam();
    }
    this.grouperMessageQueueParam.assignQueueOrTopicName(theQueueName);
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
