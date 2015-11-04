/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.messaging;


/**
 * grouper message sent to/from grouper messaging systems
 */
public interface GrouperMessage {

  /**
   * queue or topic that the message is sent to/from.
   * most useful when sending messages
   * @return the queue or topic
   */
  public String getQueueOrTopic();
  
  /**
   * queue or topic that the message is sent to/from.
   * most useful when sending messages
   * @param theQueueOrTopic1
   */
  public void setQueueOrTopic(String theQueueOrTopic1);
  
  /**
   * member id of a subjcet that sent the message
   * @return the from member id
   */
  public String getFromMemberId();

  /**
   * @param fromMemberId1 the from to set
   */
  public void setFromMemberId(String fromMemberId1);

  /**
   * @return the id
   */
  public String getId();

  /**
   * @param id1 the id to set
   */
  public void setId(String id1);

  /**
   * @return the message
   */
  public String getMessageBody();
  
  /**
   * @param message1 the message to set
   */
  public void setMessageBody(String message1);

}
