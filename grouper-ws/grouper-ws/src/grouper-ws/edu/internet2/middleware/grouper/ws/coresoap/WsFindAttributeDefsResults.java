/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouper.ws.coresoap;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;

/**
 * returned from the attribute def find query
 * 
 * @author vsachdeva
 * 
 */
public class WsFindAttributeDefsResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * result code of a request
   */
  public static enum WsFindAttributeDefsResultsCode implements WsResultCode {

    /** found the attribute defs (lite http status code 200) (success: T) */
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
    private WsFindAttributeDefsResultsCode(int statusCode) {
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
   * has 0 to many attribute defs that match the query
   */
  private WsAttributeDef[] attributeDefResuls;

  /**
   * has 0 to many attribute defs that match the query
   * @return attribute defs
   */
  public WsAttributeDef[] getAttributeDefResults() {
    return this.attributeDefResuls;
  }

  /**
   * has 0 to many attribute defs that match the query
   * @param attributeDefs1
   */
  public void setAttributeDefResults(WsAttributeDef[] attributeDefs1) {
    this.attributeDefResuls = attributeDefs1;
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsFindAttributeDefsResults.class);

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

  /**
   * put an attribute def in the results
   * @param attributeDef
   */
  public void assignAttributeDefResult(AttributeDef attributeDef) {
    this.assignAttributeDefResult(GrouperUtil.toSet(attributeDef));
  }

  /**
   * put an attribute def in the results
   * @param attributeDefSet
   */
  public void assignAttributeDefResult(Set<AttributeDef> attributeDefSet) {
    this.setAttributeDefResults(WsAttributeDef.convertAttributeDefs(attributeDefSet));
  }

  /**
   * assign the code from the enum
   * 
   * @param wsFindAttributeDefsResultsCode
   */
  public void assignResultCode(
      WsFindAttributeDefsResultsCode wsFindAttributeDefsResultsCode) {
    this.getResultMetadata().assignResultCode(wsFindAttributeDefsResultsCode);
  }

  /**
   * prcess an exception, log, etc
   * @param wsFindAttributeDefsResultsCodeOverride 
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsFindAttributeDefsResultsCode wsFindAttributeDefsResultsCodeOverride,
      String theError,
      Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsFindAttributeDefsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsFindAttributeDefsResultsCodeOverride,
          WsFindAttributeDefsResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsFindAttributeDefsResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsFindAttributeDefsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsFindAttributeDefsResultsCodeOverride,
          WsFindAttributeDefsResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsFindAttributeDefsResultsCodeOverride);

    }
  }
}
