/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.messaging;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * type of queues
 */
public enum GrouperMessageQueueType {

  /**
   * queue is when you send to one destination, or receive from a destination
   */
  queue,
  
  /**
   * sending to a topic can cause sending to multiple queues
   */
  topic;
 
  /**
   * convert a string to a queue type
   * @param input
   * @param exceptionIfNotFound
   * @return the state or null
   */
  public static GrouperMessageQueueType valueOfIgnoreCase(String input, boolean exceptionIfNotFound) {
    return GrouperClientUtils.enumValueOfIgnoreCase(GrouperMessageQueueType.class, input, exceptionIfNotFound);
  }

}
