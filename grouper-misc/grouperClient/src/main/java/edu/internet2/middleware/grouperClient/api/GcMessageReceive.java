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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsMessageResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestReceiveMessageRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.morphString.Crypto;

/**
 * @author vsachdeva
 */
public class GcMessageReceive {
	  

  /**
   * endpoint to grouper WS, e.g. https://server.school.edu/grouper-ws/servicesRest
   */
  private String wsEndpoint;

  /**
   * endpoint to grouper WS, e.g. https://server.school.edu/grouper-ws/servicesRest
   * @param theWsEndpoint
   * @return this for chaining
   */
  public GcMessageReceive assignWsEndpoint(String theWsEndpoint) {
    this.wsEndpoint = theWsEndpoint;
    return this;
  }
  
  /**
   * ws user
   */
  private String wsUser;

  /**
   * ws user
   * @param theWsUser
   * @return this for chaining
   */
  public GcMessageReceive assignWsUser(String theWsUser) {
    this.wsUser = theWsUser;
    return this;
  }
  
  /**
   * ws pass
   */
  private String wsPass;

  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcMessageReceive assignWsPass(String theWsPass) {
    this.wsPass = theWsPass;
    return this;
  }
  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcMessageReceive assignWsPassEncrypted(String theWsPassEncrypted) {
    String encryptKey = GrouperClientUtils.encryptKey();
    return this.assignWsPass(new Crypto(encryptKey).decrypt(theWsPassEncrypted));
  }
  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcMessageReceive assignWsPassFile(File theFile) {
    return this.assignWsPass(GrouperClientUtils.readFileIntoString(theFile));
  }

  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcMessageReceive assignWsPassFileEncrypted(File theFile) {
    return this.assignWsPassEncrypted(GrouperClientUtils.readFileIntoString(theFile));
  }

  /** queue or topic name **/
  private String queueOrTopicName;
  
  /** messaging system name **/
  private String messageSystemName;
  
  /** the millis to block waiting for messages, max of 20000 (optional) **/
  private Integer blockMillis;
  
  /** max number of messages to receive at once, though can't be more than the server maximum (optional) **/
  private Integer maxMessagesToReceiveAtOnce; 
  
  /** routing key - valid for rabbitmq only; **/
  private String routingKey;
  
  /** create queue/topic if doesn't exist already. **/
  private Boolean autocreateObjects;

  /**
   * create queue/topic if doesn't exist already.
   * @param theAutocreateObjects
   * @return this for chaining
   */
  public GcMessageReceive assignAutocreateObjets(Boolean theAutocreateObjects) {
    this.autocreateObjects = theAutocreateObjects;
    return this;
  }

  /**
   * @param theBlockMillis
   * @return
   */
  public GcMessageReceive assignBlockMillis(Integer theBlockMillis) {
    this.blockMillis = theBlockMillis;
    return this;
  }
  
  /**
   * @param theMaxMessagesToReceiveAtOnce
   * @return
   */
  public GcMessageReceive assignMaxMessagesToReceiveAtOnce(Integer theMaxMessagesToReceiveAtOnce) {
    this.maxMessagesToReceiveAtOnce = theMaxMessagesToReceiveAtOnce;
    return this;
  }

  
  /**
   * @param theQueueOrTopicName
   * @return
   */
  public GcMessageReceive assignQueueOrTopicName(String theQueueOrTopicName) {
  	this.queueOrTopicName = theQueueOrTopicName;
  	return this;
  }

  /**
   * @param theRoutingKey
   * @return
   */
  public GcMessageReceive assignRoutingKey(String theRoutingKey) {
    this.routingKey = theRoutingKey;
    return this;
  }

  /**
   * @param theMessageSystemName
   * @return
   */
  public GcMessageReceive assignMessageSystemName(String theMessageSystemName) {
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
  public GcMessageReceive addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
	  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcMessageReceive addParam(WsParam wsParam) {
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
  public GcMessageReceive assignActAsSubject(WsSubjectLookup theActAsSubject) {
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
  }	
  
  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcMessageReceive assignClientVersion(String theClientVersion) {
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
      WsRestReceiveMessageRequest messageReceiveRequest = new WsRestReceiveMessageRequest();

      messageReceiveRequest.setActAsSubjectLookup(this.actAsSubject);
      messageReceiveRequest.setQueueOrTopicName(this.queueOrTopicName);
      messageReceiveRequest.setMessageSystemName(this.messageSystemName);
      messageReceiveRequest.setRoutingKey(this.routingKey);
      messageReceiveRequest.setBlockMillis(this.blockMillis == null ? null : this.blockMillis.toString());
      messageReceiveRequest.setMaxMessagesToReceiveAtOnce(this.maxMessagesToReceiveAtOnce == null ? null : this.maxMessagesToReceiveAtOnce.toString());
      messageReceiveRequest.setAutocreateObjects(this.autocreateObjects == null ? null : (this.autocreateObjects ? "T" : "F"));
      
      //add params if there are any
      if (this.params.size() > 0) {
        messageReceiveRequest.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      grouperClientWs.assignWsUser(this.wsUser);
      grouperClientWs.assignWsPass(this.wsPass);
      grouperClientWs.assignWsEndpoint(this.wsEndpoint);
      
      //kick off the web service
      wsMessageResults = (WsMessageResults)
        grouperClientWs.executeService("messaging", messageReceiveRequest, "receive messages", this.clientVersion, false);
      
      String resultMessage = wsMessageResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsMessageResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsMessageResults;
    
  }

}
