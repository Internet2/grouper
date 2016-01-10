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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * <pre>
 * results for the attribute defs delete call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * ATTRIBUTE_DEF_NOT_FOUND: cant find the attribute def
 * </pre>
 * @author vsachdeva
 */
public class WsAttributeDefDeleteLiteResult implements WsResponseBean,
    ResultMetadataHolder {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsAttributeDefDeleteLiteResult.class);

  /**
   * result code of a request
   */
  public static enum WsAttributeDefDeleteLiteResultCode implements WsResultCode {

    /** found the attribute defs, deleted them (lite status code 200) (success: T) */
    SUCCESS(200),

    /** either overall exception, or one or more attribute defs had exceptions (lite status code 500) (success: F) */
    EXCEPTION(500),

    /** problem deleting existing attribute defs (lite status code 500) (success: F) */
    PROBLEM_DELETING_ATTRIBUTE_DEFS(500),

    /** invalid query (e.g. if everything blank) (lite status code 400) (success: F) */
    INVALID_QUERY(400),

    /** user not allowed  (lite status code 403) (success: F)*/
    INSUFFICIENT_PRIVILEGES(403),

    /** the attribute def was not found (lite status code 200) (success: T) */
    SUCCESS_ATTRIBUTE_DEF_NOT_FOUND(200);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name
     **/
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
    private WsAttributeDefDeleteLiteResultCode(int statusCode) {
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
     * @return true if success
     */
    @Override
    public boolean isSuccess() {
      return this == SUCCESS || this == SUCCESS_ATTRIBUTE_DEF_NOT_FOUND;
    }

  }

  /**
   * empty
   */
  public WsAttributeDefDeleteLiteResult() {
    //empty
  }

  /**
   * construct from results of other
   * @param wsAttributeDefDeleteResults
   */
  public WsAttributeDefDeleteLiteResult(
      WsAttributeDefDeleteResults wsAttributeDefDeleteResults) {

    this.getResultMetadata().copyFields(wsAttributeDefDeleteResults.getResultMetadata());

    WsAttributeDefDeleteResult wsAttributeDefDeleteResult = GrouperServiceUtils
        .firstInArrayOfOne(wsAttributeDefDeleteResults.getResults());
    if (wsAttributeDefDeleteResult != null) {
      this.getResultMetadata().copyFields(wsAttributeDefDeleteResult.getResultMetadata());
      this.getResultMetadata().assignResultCode(
          wsAttributeDefDeleteResult.resultCode().convertToLiteCode());
      this.setWsAttributeDef(wsAttributeDefDeleteResult.getWsAttributeDef());
    }
  }

  /**
   * assign the code from the enum
   * @param attributeDefsDeleteResultsCode should not be null
   */
  public void assignResultCode(
      WsAttributeDefDeleteLiteResultCode attributeDefsDeleteResultsCode) {
    this.getResultMetadata().assignResultCode(attributeDefsDeleteResultsCode);
  }

  /**
   * convert the result code back to enum
   * @return the enum code
   */
  public WsAttributeDefDeleteLiteResultCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsAttributeDefDeleteLiteResultCode.valueOf(this.getResultMetadata()
        .getResultCode());
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * attributeDef to be deleted
   */
  private WsAttributeDef wsAttributeDef;

  /**
   * prcess an exception, log, etc
   * @param wsAttributeDefDeleteLiteResultCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsAttributeDefDeleteLiteResultCode wsAttributeDefDeleteLiteResultCodeOverride,
      String theError,
      Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsAttributeDefDeleteLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsAttributeDefDeleteLiteResultCodeOverride,
          WsAttributeDefDeleteLiteResultCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsAttributeDefDeleteLiteResultCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsAttributeDefDeleteLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsAttributeDefDeleteLiteResultCodeOverride,
          WsAttributeDefDeleteLiteResultCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsAttributeDefDeleteLiteResultCodeOverride);

    }
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

  /**
   * @return the wsAttributeDef
   */
  public WsAttributeDef getWsAttributeDef() {
    return this.wsAttributeDef;
  }

  /**
   * @param wsAttributeDefResult1 the wsGroup to set
   */
  public void setWsAttributeDef(WsAttributeDef wsAttributeDefResult1) {
    this.wsAttributeDef = wsAttributeDefResult1;
  }
}
