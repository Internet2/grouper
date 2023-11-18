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
 * @author vsachdeva
 * $Id$
 */
package edu.internet2.middleware.grouperClient.ws.beans;

/**
 * request bean in body of rest request
 */
public class WsRestAcknowledgeMessageRequest implements WsRequestBean {
  
  /** queue or topic name **/
  private String queueOrTopicName;
  
  /** messaging system name **/
  private String messageSystemName;
  
  /** what to do with the message. valid options are: mark_as_processed, return_to_queue, return_to_end_of_queue,  send_to_another_queue **/
  private String acknowledgeType;
  
  /** messages to be acknowledged **/
  private String[] messageIds;
  
  /** destination of the message if acknowledgeType is send_to_another_queue **/
  private String anotherQueueOrTopicName;
	  
  /** destination type if acknowledge type is send_to_another_queue. Valid values are queue and topic **/
  private String anotherQueueType;
    
  /**
   * @return the messages to be marked as processed
   */
  public String[] getMessageIds() {
    return this.messageIds;
  }

  /**
   * @param messageIds1 to be marked as processed
   */
  public void setMessageIds(String[] messageIds1) {
    this.messageIds = messageIds1;
  }
  
  /** 
   * @return queueOrTopicName
   */
  public String getQueueOrTopicName() {
    return this.queueOrTopicName;
  }

  /**
   * @param queueOrTopicName1
   */
  public void setQueueOrTopicName(String queueOrTopicName1) {
    this.queueOrTopicName = queueOrTopicName1;
  }

  /**
   * @return messageSystemName
   */
  public String getMessageSystemName() {
    return this.messageSystemName;
  }

  /**
   * @param messageSystemName1
   */
  public void setMessageSystemName(String messageSystemName1) {
    this.messageSystemName = messageSystemName1;
  }
  
  /** 
   *  what to do with the message. valid options are: mark_as_processed, return_to_queue, return_to_end_of_queue,  send_to_another_queue
   * @return acknowledgeType
   */
  public String getAcknowledgeType() {
    return this.acknowledgeType;
  }

  /**
   *  what to do with the message. valid options are: mark_as_processed, return_to_queue, return_to_end_of_queue,  send_to_another_queue
   * @param acknowledgeType1
   */
  public void setAcknowledgeType(String acknowledgeType1) {
    this.acknowledgeType = acknowledgeType1;
  }

  /**
   * destination of the message if acknowledgeType is send_to_another_queue
   * @return anotherQueueOrTopicName
   */
  public String getAnotherQueueOrTopicName() {
    return this.anotherQueueOrTopicName;
  }

  /**
   * destination of the message if acknowledgeType is send_to_another_queue
   * @param anotherQueueOrTopicName1
   */
  public void setAnotherQueueOrTopicName(String anotherQueueOrTopicName1) {
    this.anotherQueueOrTopicName = anotherQueueOrTopicName1;
  }

  /**
   * destination type if acknowledge type is send_to_another_queue. Valid values are queue and topic
   * @return anotherQueueOrTopic
   */
  public String getAnotherQueueType() {
    return this.anotherQueueType;
  }

  /**
   * destination type if acknowledge type is send_to_another_queue. Valid values are queue and topic
   * @param anotherQueueOrTopic1
   */
  public void setAnotherQueueType(String anotherQueueOrTopic1) {
    this.anotherQueueType = anotherQueueOrTopic1;
  }

  /** is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000 */
  private String clientVersion;
  
  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @return version
   */
  public String getClientVersion() {
    return this.clientVersion;
  }

  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param clientVersion1
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  /** if acting as someone else */
  private WsSubjectLookup actAsSubjectLookup;
  
  /**
   * if acting as someone else
   * @return act as subject
   */
  public WsSubjectLookup getActAsSubjectLookup() {
    return this.actAsSubjectLookup;
  }

  /**
   * if acting as someone else
   * @param actAsSubjectLookup1
   */
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup1) {
    this.actAsSubjectLookup = actAsSubjectLookup1;
  }

  /** optional: reserved for future use */
  private  WsParam[] params;

  /**
   * optional: reserved for future use
   * @return params
   */
  public WsParam[] getParams() {
    return this.params;
  }

  /**
   * optional: reserved for future use
   * @param params1
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

}
