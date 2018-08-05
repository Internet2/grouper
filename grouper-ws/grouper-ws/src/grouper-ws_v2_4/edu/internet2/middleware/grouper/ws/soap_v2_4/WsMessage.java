/*******************************************************************************
 * Copyright 2016 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_4;

import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;

/**
 * Result of one message being sent or received.  The number of
 * messages will equal the number of messages related to the result
 * 
 * @author vsachdeva
 */
public class WsMessage implements Comparable<WsMessage> {

  /**
   * message id
   */
  private String id;
  
  /** sender of the message **/
  private String fromMemberId;
  
  /** payload/body of the message **/
  private String messageBody;
  
  /**
   * @return the message id
   */
  public String getId() {
    return this.id;
  }

  /**
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }
  

  /**
   * @return member id of sender
   */
  public String getFromMemberId() {
    return this.fromMemberId;
  }

  /**
   * @param fromMemberId1 member id of sender
   */
  public void setFromMemberId(String fromMemberId1) {
    this.fromMemberId = fromMemberId1;
  }

  /**
   * @return body of the message
   */
  public String getMessageBody() {
    return this.messageBody;
  }

  /**
   * 
   * @param body1 body of the message
   */
  public void setMessageBody(String messageBody1) {
    this.messageBody = messageBody1;
  }

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * convert a set of messages to results
   * @param messageSet
   * @return the WsMessages (null if none or null)
   */
  public static WsMessage[] convertMessages(Set<GrouperMessage> messageSet) {
    if (messageSet == null || messageSet.size() == 0) {
      return null;
    }
    int messagesSize = messageSet.size();
    WsMessage[] wsMessageResults = new WsMessage[messagesSize];
    int index = 0;
    for (GrouperMessage msg : messageSet) {
      WsMessage wsMessage = new WsMessage(msg);
      wsMessageResults[index] = wsMessage;
      index++;
    }
    return wsMessageResults;

  }


  /**
   * no arg constructor
   */
  public WsMessage() {
    //blank

  }
  
  /**
   * construct based on grouperMessage
   * @param grouperMessage 
   */
  public WsMessage(GrouperMessage grouperMessage) {
    if (grouperMessage != null) {
      this.setId(grouperMessage.getId());
      this.setFromMemberId(grouperMessage.getFromMemberId());
      this.setMessageBody(grouperMessage.getMessageBody());
    }
  }
  

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(WsMessage o2) {
    if (this == o2) {
      return 0;
    }
    if (o2 == null) {
      return 1;
    }
    return GrouperUtil.compare(this.getId(), o2.getId());
  }

}
