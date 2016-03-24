/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.messaging;


/**
 * this is what you can do when acknowledging a message
 */
public enum GrouperMessageAcknowledgeType {

  /**
   * return to the queue, will be in next retrieve of messages
   */
  return_to_queue,
  
  /**
   * mark as processed, do not get this message again
   */
  mark_as_processed,
  
  /**
   * return to end of queue, once other messages are retrieved, this one will be delievered again
   */
  return_to_end_of_queue,

  /**
   * send to a dead letter queue or another error queue or topic or whatever
   */
  send_to_another_queue;
  
}
