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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;



/**
 * returned from the acknowledge web service
 * 
 * @author vsachdeva
 * 
 */
public class WsMessageAcknowledgeResults {
  
  /** logger */
  private static final Log LOG = LogFactory.getLog(WsMessageAcknowledgeResults.class);

  /**
   * result code of a request
   */
  public static enum WsMessageAcknowledgeResultsCode implements WsResultCode {
  
    /** messages acknowledged successfully (lite http status code 200) (success: T) */
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
    private WsMessageAcknowledgeResultsCode(int statusCode) {
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
   * 
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


  /**
   * assign the code from the enum
   * 
   * @param wsSendMessageResultsCode
   */
  public void assignResultCode(WsMessageAcknowledgeResultsCode wsSendMessageResultsCode) {
    this.getResultMetadata().assignResultCode(wsSendMessageResultsCode);
  }

  /**
   * prcess an exception, log, etc
   * @param wsMessageAcknowledgeResultsCodeOverride 
   * @param wsAddMemberResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsMessageAcknowledgeResultsCode wsMessageAcknowledgeResultsCodeOverride, String theError,
      Exception e) {
  
    if (e instanceof WsInvalidQueryException) {
      wsMessageAcknowledgeResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsMessageAcknowledgeResultsCodeOverride, WsMessageAcknowledgeResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsMessageAcknowledgeResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);
  
    } else {
      wsMessageAcknowledgeResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsMessageAcknowledgeResultsCodeOverride, WsMessageAcknowledgeResultsCode.EXCEPTION);
      LOG.error(theError, e);
  
      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsMessageAcknowledgeResultsCodeOverride);
  
    }
  }
}
