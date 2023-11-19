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
package edu.internet2.middleware.grouper.ws.rest.messaging;

import edu.internet2.middleware.grouper.ws.coresoap.WsMessage;
import edu.internet2.middleware.grouper.ws.coresoap.WsParam;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

import java.util.Map;


/**
 * request bean in body of rest request
 */
public class WsRestSendMessageRequest implements WsRequestBean {
  
  
  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  @Override
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.PUT;
  }
  
  /** queue or topic **/
  private String queueType;
  
  /** queue or topic name **/
  private String queueOrTopicName;
  
  /** messaging system name **/
  private String messageSystemName;
  
  /** routing key for rabbitmq **/
  private String routingKey;

  /** if the messaging system can use exchange type (e.g. rabbitmq), set it here **/
  private String exchangeType;

  /** extra queue arguments if needed **/
  private Map<String, Object> queueArguments;

  /** create queue/topic if doesn't exist already. **/
  private String autocreateObjects;

  /** messages to be sent **/
  private WsMessage[] messages;
  
  /**
   * @return the messages to be sent
   */
  public WsMessage[] getMessages() {
    return this.messages;
  }

  /**
   * @param messages1 to be sent
   */
  public void setMessages(WsMessage[] messages1) {
    this.messages = messages1;
  }
  
  /**
   * routing key for rabbitmq
   * @return routingKey
   */
  public String getRoutingKey() {
    return this.routingKey;
  }

  /**
   * routing key for rabbitmq
   * @param routingKey1
   */
  public void setRoutingKey(String routingKey1) {
    this.routingKey = routingKey1;
  }

  /**
   * exchange type (e.g. rabbitmq)
   * @return
   */
  public String getExchangeType() {
    return exchangeType;
  }

  /**
   * exchange type (e.g. rabbitmq)
   * @param exchangeType1
   */
  public void setExchangeType(String exchangeType1) {
    this.exchangeType = exchangeType1;
  }

  /**
   * optional queue argument map for rabbitmq
   * @return queueArguments
   */
  public Map<String, Object> getQueueArguments() {
    return queueArguments;
  }

  /**
   * optional queue argument map for rabbitmq
   * @param queueArguments map of key:value of queue arguments
   */
  public void setQueueArguments(Map<String, Object> queueArguments) {
    this.queueArguments = queueArguments;
  }

  /**
   * queue type
   * @return queueType
   */
  public String getQueueType() {
    return this.queueType;
  }

  /**
   * queue type
   * @param queueType1
   */
  public void setQueueType(String queueType1) {
    this.queueType = queueType1;
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
  
  /**
   * create queue/topic if doesn't exist already.
   * @return autocreateObjects
   */
  public String isAutocreateObjects() {
    return this.autocreateObjects;
  }

  /**
   * create queue/topic if doesn't exist already.
   * @param autocreateObjects1
   */
  public void setAutocreateObjects(String autocreateObjects1) {
    this.autocreateObjects = autocreateObjects1;
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
