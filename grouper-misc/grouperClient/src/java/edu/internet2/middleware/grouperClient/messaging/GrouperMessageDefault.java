/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.messaging;


/**
 * grouper message sent to/from grouper messaging systems
 */
public class GrouperMessageDefault implements GrouperMessage {

  /**
   * member id of the subject sending from
   */
  private String fromMemberId;

  /** id of this type */
  private String id;

  /**
   * body of message (e.g. the json or encrypted message)
   */
  private String messageBody;
  
  /**
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }

  
  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessage#getFromMemberId()
   */
  public String getFromMemberId() {
    return this.fromMemberId;
  }


  /**
   * @return id
   */
  public String getId() {
    return this.id;
  }


  /**
   * @see GrouperMessage#getMessageBody()
   */
  public String getMessageBody() {
    return this.messageBody;
  }


  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessage#setFromMemberId(java.lang.String)
   */
  public void setFromMemberId(String fromMemberId1) {
  }

  /**
   * @see GrouperMessage#setMessageBody(String)
   */
  public void setMessageBody(String messageBody1) {
    this.messageBody = messageBody1;
  }

}
