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
package edu.internet2.middleware.grouperClient.ws.beans;

/**
 * returned from the acknowledge message web service
 * 
 * @author vsachdeva
 * 
 */
public class WsMessageAcknowledgeResults implements WsResponseBean, ResultMetadataHolder {
  
  /** 
   * queue or topic to send to 
   */
  private String queueOrTopicName;
  
  /** 
   * if there are multiple messaging systems, specify which one 
   */
  private String messageSystemName;

  /**
   * has 0 to many messages which were acknowledged
   */
  private String[] messageIds;


  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the response
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();


  /**
   * @return the messageIds which were acknowledged
   */
  public String[] getMessageIds() {
    return this.messageIds;
  }

  /**
   * @param messageIds1 the messages which were acknowledged
   */
  public void setMessageIds(String[] messageIds1) {
    this.messageIds = messageIds1;
  }
  
  /**
  * @return queueOrTopicName - queue or topic to send to
  */
  public String getQueueOrTopicName() {
    return this.queueOrTopicName;
  }

  /**
   * @param queueOrTopicName1 - queue or topic to send to
  */
  public void setQueueOrTopicName(String queueOrTopicName1) {
    this.queueOrTopicName = queueOrTopicName1;
  }

  /**
   * @return messageSystemName - if there are multiple messaging systems, specify which one
  */
  public String getMessageSystemName() {
    return this.messageSystemName;
  }

  /**
   * @param messageSystemName1 - if there are multiple messaging systems, specify which one
   */
  public void setMessageSystemName(String messageSystemName1) {
    this.messageSystemName = messageSystemName1;
  }

  /**
   * @return the resultMetadata
   */
  @Override
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  @Override
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  /**
   * @param responseMetadata1 the responseMetadata to set
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

}
