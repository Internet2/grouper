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
 * results for the AttributeDefs save call.
 * 
 * result code:
 * code of the result for this AttributeDef overall
 * SUCCESS: means everything ok
 * ATTRIBUTE_DEF_NOT_FOUND: cant find the AttributeDef
 * ATTRIBUTE_DEF_DUPLICATE: found multiple AttributeDefs
 * </pre>
 * @author mchyzer
 */
public class WsAttributeDefSaveLiteResult implements WsResponseBean, ResultMetadataHolder {

  /**
   * result code of a request
   */
  public static enum WsAttributeDefSaveLiteResultCode implements WsResultCode {

    /** didnt find the attribute defs, inserted them (lite http status code 201) (success: T) */
    SUCCESS_INSERTED(201),

    /** found the attribute defs, saved them (lite http status code 201) (success: T) */
    SUCCESS_UPDATED(201),

    /** found the attribute defs, no changes needed (lite http status code 201) (success: T) */
    SUCCESS_NO_CHANGES_NEEDED(201),

    /** either overall exception, or one or more attribute defs had exceptions (lite http status code 500) (success: F) */
    EXCEPTION(500),

    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400),

    /** the attribute def was not found  (lite http status code 404) (success: F) */
    ATTRIBUTE_DEF_NOT_FOUND(404),

    /** user not allowed (lite http status code 403) (success: F) */
    INSUFFICIENT_PRIVILEGES(403),

    /** the stem was not found  (lite http status code 404) (success: F) */
    STEM_NOT_FOUND(404);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name
     */
    @Override
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /**
     * if this is a successful result
     * @return true if success
     */
    @Override
    public boolean isSuccess() {
      return this.name().startsWith("SUCCESS");
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsAttributeDefSaveLiteResultCode(int statusCode) {
      this.httpStatusCode = statusCode;
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.WsResultCode#getHttpStatusCode()
     */
    @Override
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }

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
   * AttributeDef saved 
   */
  private WsAttributeDef wsAttributeDef;

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsAttributeDefSaveLiteResult.class);

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
   * @param wsAttributeDef1 the wsAttributeDef to set
   */
  public void setWsAttributeDef(WsAttributeDef wsAttributeDef1) {
    this.wsAttributeDef = wsAttributeDef1;
  }

  /**
   * assign the code from the enum
   * @param attributeDefSaveLiteResultCode
   * @param clientVersion 
   */
  public void assignResultCode(
      WsAttributeDefSaveLiteResultCode attributeDefSaveLiteResultCode,
      GrouperVersion clientVersion) {
    this.getResultMetadata().assignResultCode(attributeDefSaveLiteResultCode,
        clientVersion);
  }

  /**
   * prcess an exception, log, etc
   * @param wsAttributeDefSaveResultsCodeOverride
   * @param theError
   * @param e
   * @param clientVersion
   */
  public void assignResultCodeException(
      WsAttributeDefSaveLiteResultCode wsAttributeDefSaveResultsCodeOverride,
      String theError, Exception e, GrouperVersion clientVersion) {

    if (e instanceof WsInvalidQueryException) {
      wsAttributeDefSaveResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsAttributeDefSaveResultsCodeOverride,
          WsAttributeDefSaveLiteResultCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsAttributeDefSaveResultsCodeOverride, clientVersion);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsAttributeDefSaveResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsAttributeDefSaveResultsCodeOverride,
          WsAttributeDefSaveLiteResultCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsAttributeDefSaveResultsCodeOverride, clientVersion);

    }
  }

  /**
   * convert the result code back to enum
   * @return the enum code
   */
  public WsAttributeDefSaveLiteResultCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsAttributeDefSaveLiteResultCode.valueOf(this.getResultMetadata()
        .getResultCode());
  }

  /**
   * empty
   */
  public WsAttributeDefSaveLiteResult() {
    //empty
  }

  /**
   * construct from results of other
   * @param wsAttributeDefSaveResults
   */
  public WsAttributeDefSaveLiteResult(WsAttributeDefSaveResults wsAttributeDefSaveResults) {

    this.getResultMetadata().copyFields(wsAttributeDefSaveResults.getResultMetadata());

    WsAttributeDefSaveResult wsAttributeDefSaveResult = GrouperServiceUtils
        .firstInArrayOfOne(wsAttributeDefSaveResults.getResults());
    if (wsAttributeDefSaveResult != null) {
      this.getResultMetadata().copyFields(wsAttributeDefSaveResult.getResultMetadata());

      this.getResultMetadata().assignResultCode(
          wsAttributeDefSaveResult.resultCode().convertToLiteCode());
      this.setWsAttributeDef(wsAttributeDefSaveResult.getWsAttributeDef());
    }
  }

}
