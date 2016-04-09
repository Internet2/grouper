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
package edu.internet2.middleware.grouper.ws.soap_v2_3;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.ws.WsResultCode;

/**
 * returned from the send/receive message web service
 * 
 * @author vsachdeva
 * 
 */
public class WsMessageResults {
  
  /**
   * result code of a request
   */
  public static enum WsMessageResultsCode implements WsResultCode {
  
    /** messages sent/received successfully (lite http status code 200) (success: T) */
    SUCCESS(200),
  
    /** problems with operation (lite http status code 500) (success: F) */
    EXCEPTION(500),
  
    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400);
  
    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name
     */
    @Override
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }
  
    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;
  
    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsMessageResultsCode(int statusCode) {
      this.httpStatusCode = statusCode;
    }
  
    /**
     * @see edu.internet2.middleware.grouper.ws.WsResultCode#getHttpStatusCode()
     */
    @Override
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }
  
    /**
     * if this is a successful result
     * 
     * @return true if success
     */
    @Override
    public boolean isSuccess() {
      return this == SUCCESS;
    }
  }

  /** 
   * queue or topic to send to 
   */
  private String queueOrTopicName;
  
  /** 
   * if there are multiple messaging systems, specify which one 
   */
  private String messageSystemName;

  /**
   * has 0 to many messages which were sent or received
   */
  private WsMessage[] messages;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the response
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();


  /**
   * @return the messages which were sent or received
   */
  public WsMessage[] getMessages() {
    return this.messages;
  }

  /**
   * @param messages1 the messages which were sent or received
   */
  public void setMessages(WsMessage[] messages1) {
    this.messages = messages1;
  }
  
  /**
   * 
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
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
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
