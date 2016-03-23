/**
 * 
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.messaging;


/**
 * Represents the methods that a messaging system
 * needs to support
 */
public interface GrouperMessagingSystem {

  /**
   * send a message to a queue name.  Note, the recipient could be a 
   * queue or a topic (generally always one or the other) based on the 
   * implementation of the messaging system.  Messages must be delievered
   * in the order that collection iterator designates.  If there is a problem
   * delivering the messages, the implementation should log, wait (back off)
   * and retry until it is successful.
   * @param grouperMessageSendParam has the queue or topic, and the message(s) and perhaps args
   * @return result
   */
  public GrouperMessageSendResult send(GrouperMessageSendParam grouperMessageSendParam);

  /**
   * tell the messaging system that these messages are processed
   * generally the message system will use the message id.  Note, the objects
   * sent to this method must be the same that were received in the
   * receiveMessages method.  If there is a problem
   * delivering the messages, the implementation should wait (back off)
   * and retry until it is successful.
   * @param grouperMessageProcessedParam
   * @return result
   */
  public GrouperMessageProcessedResult markAsProcessed(GrouperMessageProcessedParam grouperMessageProcessedParam);

  /**
   * tell the messaging system that these messages werent processed and return to queue
   * to be processed again.
   * generally the message system will use the message id.  Note, the objects
   * sent to this method must be the same that were received in the
   * receiveMessages method
   * @param grouperMessageReturnToQueueParam
   * @return result
   */
  public GrouperMessageReturnToQueueResult returnToQueue(GrouperMessageReturnToQueueParam grouperMessageReturnToQueueParam);

  /**
   * this will generally block until there are messages to process.  These messages
   * are ordered in the order that they were sent.
   * @param grouperMessageReceiveParam grouper messaging receive param
   * @return a message or multiple messages.  It will block until there are messages
   * available for this recipient to process
   */
  public GrouperMessageReceiveResult receive(GrouperMessageReceiveParam grouperMessageReceiveParam);

}
