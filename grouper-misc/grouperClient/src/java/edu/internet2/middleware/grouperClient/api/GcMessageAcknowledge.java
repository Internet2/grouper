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
import edu.internet2.middleware.grouperClient.ws.beans.WsMessageAcknowledgeResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAcknowledgeMessageRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

/**
 * @author vsachdeva
 */
public class GcMessageAcknowledge {
	  
  /** queue or topic name **/
  private String queueOrTopicName;
  
  /** messaging system name **/
  private String messageSystemName;
  
  /** what to do with the message. valid options are: mark_as_processed, return_to_queue, return_to_end_of_queue,  send_to_another_queue **/
  private String acknowledgeType;
  
  /** messages to be acknowledged **/
  private List<String> messageIds = new ArrayList<String>();
  
  /** destination of the message if acknowledgeType is send_to_another_queue **/
  private String anotherQueueOrTopicName;
  
  /** destination type if acknowledge type is send_to_another_queue. Valid values are queue and topic **/
  private String anotherQueueType;
  
  /**
   * 
   * @param theAcknowledgeType mark_as_processed, return_to_queue, return_to_end_of_queue,  send_to_another_queue
   * @return the acknolwedge response
   */
  public GcMessageAcknowledge assignAcknowledgeType(String theAcknowledgeType) {
    this.acknowledgeType = theAcknowledgeType;
    return this;
  }
  
  /**
   * @param theAnotherQueueOrTopicName
   * @return this for chaining
   */
  public GcMessageAcknowledge assignAnotherQueueOrTopicName(String theAnotherQueueOrTopicName) {
    this.anotherQueueOrTopicName = theAnotherQueueOrTopicName;
    return this;
  }
  
  /**
   * @param theAnotherQueueOrTopic
   * @return this for chaining
   */
  public GcMessageAcknowledge assignAnotherQueueType(String theAnotherQueueOrTopic) {
    this.anotherQueueType = theAnotherQueueOrTopic;
    return this;
  }
  
  /**
   * add a message to the list
   * @param messageId
   * @return
   */
  public GcMessageAcknowledge addMessageId(String messageId) {
	this.messageIds.add(messageId);
	return this;
  }
  
  /**
   * @param theQueueOrTopicName
   * @return
   */
  public GcMessageAcknowledge assignQueueOrTopicName(String theQueueOrTopicName) {
	this.queueOrTopicName = theQueueOrTopicName;
	return this;
  }
  
  /**
   * @param theMessageSystemName
   * @return
   */
  public GcMessageAcknowledge assignMessageSystemName(String theMessageSystemName) {
	this.messageSystemName = theMessageSystemName;
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
  public GcMessageAcknowledge addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
	  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcMessageAcknowledge addParam(WsParam wsParam) {
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
  public GcMessageAcknowledge assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
	  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.isBlank(queueOrTopicName)) {
      throw new RuntimeException("Need queue or topic name where the message(s) needs to be received from "+this);
    }    
    if (GrouperClientUtils.length(this.messageIds) == 0) {
        throw new RuntimeException("Need at least one message: " + this);
    }
  }	
  
  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcMessageAcknowledge assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsMessageAcknowledgeResults execute() {
    this.validate();
    WsMessageAcknowledgeResults wsAcknowlesgeMessageResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAcknowledgeMessageRequest messageAcknowledgeRequest = new WsRestAcknowledgeMessageRequest();

      messageAcknowledgeRequest.setActAsSubjectLookup(this.actAsSubject);
      messageAcknowledgeRequest.setQueueOrTopicName(this.queueOrTopicName);
      messageAcknowledgeRequest.setMessageSystemName(this.messageSystemName);
      messageAcknowledgeRequest.setMessageIds(GrouperClientUtils.toArray(this.messageIds, String.class));
      messageAcknowledgeRequest.setAcknowledgeType(this.acknowledgeType);
      messageAcknowledgeRequest.setAnotherQueueType(this.anotherQueueType);
      messageAcknowledgeRequest.setAnotherQueueOrTopicName(this.anotherQueueOrTopicName);
      
      //add params if there are any
      if (this.params.size() > 0) {
    	  messageAcknowledgeRequest.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsAcknowlesgeMessageResults = (WsMessageAcknowledgeResults)
        grouperClientWs.executeService("messaging", messageAcknowledgeRequest, "acknowledge messages", this.clientVersion, false);
      
      String resultMessage = wsAcknowlesgeMessageResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsAcknowlesgeMessageResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsAcknowlesgeMessageResults;
    
  }

}
