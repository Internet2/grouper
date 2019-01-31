/**
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
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsMessage;
import edu.internet2.middleware.grouperClient.ws.beans.WsMessageResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestSendMessageRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

/**
 * @author vsachdeva
 */
public class GcMessageSend {
	
  /** queue or topic **/
  private String queueType;
  
  /** queue or topic name **/
  private String queueOrTopicName;
  
  /** messaging system name **/
  private String messageSystemName;
  
  /** messages to be sent */
  private List<WsMessage> messages = new ArrayList<WsMessage>();
  
  /**
   * routing key for rabbitmq 
   **/
  private String routingKey;
  
  /** create queue/topic if doesn't exist already.
   */
  private Boolean autocreateObjects;

  /**
   * routing key for rabbitmq 
   * @param theRoutingKey 
   * @return  this for chaining
   */
  public GcMessageSend assignRoutingKey(String theRoutingKey) {
    this.routingKey = theRoutingKey;
    return this;
  }

  /**
   * create queue/topic if doesn't exist already.
   * @param theAutocreateObjects
   * @return this for chaining
   */
  public GcMessageSend assignAutocreateObjets(Boolean theAutocreateObjects) {
    this.autocreateObjects = theAutocreateObjects;
    return this;
  }
  
  /**
   * @param theQueueOrTopic
   * @return this for chaining
   */
  public GcMessageSend assignQueueType(String theQueueOrTopic) {
	this.queueType = theQueueOrTopic;
	return this;
  }
  
  /**
   * @param theQueueOrTopicName
   * @return
   */
  public GcMessageSend assignQueueOrTopicName(String theQueueOrTopicName) {
	this.queueOrTopicName = theQueueOrTopicName;
	return this;
  }
  
  /**
   * @param theMessageSystemName
   * @return
   */
  public GcMessageSend assignMessageSystemName(String theMessageSystemName) {
    this.messageSystemName = theMessageSystemName;
    return this;
  }
  
  /**
   * add a message to the list
   * @param wsMessage
   * @return this for chaining
   */
  public GcMessageSend addMessage(WsMessage wsMessage) {
	this.messages.add(wsMessage);
	return this;
  }
	
  /** params */
  private List<WsParam> params = new ArrayList<WsParam>();

  /**
   * add a param to the list
   * @param paramName
   * @param paramValue
   * @return this for chaining
   */
  public GcMessageSend addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
	  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcMessageSend addParam(WsParam wsParam) {
    this.params.add(wsParam);
    return this;
  }
	  
  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcMessageSend assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
	  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.messages) == 0) {
      throw new RuntimeException("Need at least one message to send: " + this);
    }
    if (GrouperClientUtils.isBlank(queueOrTopicName)) {
      throw new RuntimeException("Need queue or topic name where the message(s) needs to be sent "+this);
    }
    if (GrouperClientUtils.isBlank(queueType) || (!GrouperClientUtils.equalsIgnoreCase("queue", this.queueType) && !GrouperClientUtils.equalsIgnoreCase("topic", this.queueType))) {
      throw new RuntimeException("Need type of destination. Valid values are queue and topic) "+this);
    }

  }	

  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcMessageSend assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsMessageResults execute() {
    this.validate();
    WsMessageResults wsMessageResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestSendMessageRequest messageSendRequest = new WsRestSendMessageRequest();

      messageSendRequest.setActAsSubjectLookup(this.actAsSubject);
      messageSendRequest.setQueueType(this.queueType);
      messageSendRequest.setQueueOrTopicName(this.queueOrTopicName);
      messageSendRequest.setMessageSystemName(this.messageSystemName);
      messageSendRequest.setMessages(GrouperClientUtils.toArray(this.messages, WsMessage.class));
      messageSendRequest.setRoutingKey(this.routingKey);
      messageSendRequest.setAutocreateObjects(this.autocreateObjects);
      
      //add params if there are any
      if (this.params.size() > 0) {
        messageSendRequest.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsMessageResults = (WsMessageResults)
        grouperClientWs.executeService("messaging", messageSendRequest, "send messages", this.clientVersion, false);
      
      String resultMessage = wsMessageResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsMessageResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsMessageResults;
    
  }

}
