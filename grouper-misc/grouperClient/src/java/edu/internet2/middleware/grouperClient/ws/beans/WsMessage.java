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
package edu.internet2.middleware.grouperClient.ws.beans;

/**
 * Result of one message being sent or received.  The number of
 * messages will equal the number of messages related to the result
 * 
 * @author vsachdeva
 */
public class WsMessage {

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
   * no arg constructor
   */
  public WsMessage() {
    //blank
  }

}
