/**
 * Copyright 2018 Internet2
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

package edu.internet2.middleware.grouper.app.messaging;

// https://spaces.at.internet2.edu/display/Grouper/Grouper+messaging+to+web+service+API
public class InputMessageGrouperHeader {
  
  /**
   * message version (mandatory)
   */
  private String messageVersion;
  
  /**
   * timestamp message was sent (mandatory)
   */
  private String timestampInput;
  
  /**
   * type of message (mandatory). eg: grouperMessagingToWebService
   */
  private String type;
  
  /**
   * which endpoint to call in web service (mandatory)
   */
  private String endpoint;
  
  /**
   * for logging (mandatory)
   */
  private String messageInputUuid;
  
  /**
   * if replying back to some queue/topic (optional)
   */
  private String replyToQueueOrTopicName;
  
  /**
   * if replying, "queue" or "topic" (optional)
   */
  private String replyToQueueOrTopic;
  
  /**
   * if replying to rabbitmq (optional)
   */
  private String replyToRoutingKey;
  
  /**
   * if replying to rabbitmq (optional)
   */
  private String replyToExchangeType;
  
  /**
   * http method that would be in WS (mandatory)
   */
  private String httpMethod;
  
  /**
   * http path that would be in the WS (mandatory)
   */
  private String httpPath;
  
  /**
   * message version (mandatory)
   * @return
   */
  public String getMessageVersion() {
    return messageVersion;
  }
  
  /**
   * message version (mandatory)
   * @param messageVersion
   */
  public void setMessageVersion(String messageVersion) {
    this.messageVersion = messageVersion;
  }
  
  /**
   * timestamp message was sent (mandatory)
   * @return
   */
  public String getTimestampInput() {
    return timestampInput;
  }
  
  /**
   * timestamp message was sent (mandatory)
   * @param timestampInput
   */
  public void setTimestampInput(String timestampInput) {
    this.timestampInput = timestampInput;
  }
  
  /**
   * type of message (mandatory). eg: grouperMessagingToWebService
   * @return
   */
  public String getType() {
    return type;
  }
  
  /**
   * type of message (mandatory). eg: grouperMessagingToWebService
   * @param type
   */
  public void setType(String type) {
    this.type = type;
  }
  
  /**
   * which endpoint to call in web service (mandatory)
   * @return
   */
  public String getEndpoint() {
    return endpoint;
  }
  
  /**
   * which endpoint to call in web service (mandatory)
   * @param endpoint
   */
  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }
  
  /**
   * for logging (mandatory)
   * @return
   */
  public String getMessageInputUuid() {
    return messageInputUuid;
  }
  
  /**
   * for logging (mandatory)
   * @param messageInputUuid
   */
  public void setMessageInputUuid(String messageInputUuid) {
    this.messageInputUuid = messageInputUuid;
  }
  
  /**
   * if replying back to some queue/topic (optional)
   * @return
   */
  public String getReplyToQueueOrTopicName() {
    return replyToQueueOrTopicName;
  }
  
  /**
   * if replying back to some queue/topic (optional)
   * @param replyToQueueOrTopicName
   */
  public void setReplyToQueueOrTopicName(String replyToQueueOrTopicName) {
    this.replyToQueueOrTopicName = replyToQueueOrTopicName;
  }
  
  /**
   * if replying, "queue" or "topic" (optional)
   * @return
   */
  public String getReplyToQueueOrTopic() {
    return replyToQueueOrTopic;
  }
  
  /**
   * if replying, "queue" or "topic" (optional)
   * @param replyToQueueOrTopic
   */
  public void setReplyToQueueOrTopic(String replyToQueueOrTopic) {
    this.replyToQueueOrTopic = replyToQueueOrTopic;
  }
  
  /**
   * if replying to rabbitmq (optional)
   * @return
   */
  public String getReplyToRoutingKey() {
    return replyToRoutingKey;
  }

  /**
   * if replying to rabbitmq (optional)
   * @param replyToRoutingKey
   */
  public void setReplyToRoutingKey(String replyToRoutingKey) {
    this.replyToRoutingKey = replyToRoutingKey;
  }
  
  /**
   * if replying to rabbitmq (optional)
   * @return
   */
  public String getReplyToExchangeType() {
    return replyToExchangeType;
  }
  
  /**
   * if replying to rabbitmq (optional)
   * @param replyToExchangeType
   */
  public void setReplyToExchangeType(String replyToExchangeType) {
    this.replyToExchangeType = replyToExchangeType;
  }

  /**
   * http method that would be in WS (mandatory)
   * @return
   */
  public String getHttpMethod() {
    return httpMethod;
  }
  
  /**
   * http method that would be in WS (mandatory)
   * @param httpMethod
   */
  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }
  
  /**
   * http path that would be in the WS (mandatory)
   * @return
   */
  public String getHttpPath() {
    return httpPath;
  }
  
  /**
   * http path that would be in the WS (mandatory)
   * @param httpPath
   */
  public void setHttpPath(String httpPath) {
    this.httpPath = httpPath;
  }

}
