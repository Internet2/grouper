/*******************************************************************************
 * Copyright 2016 Internet2
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

import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefSaveResult.WsAttributeDefSaveResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;

/**
 * <pre>
 * results for the attribute defs save call.
 * 
 * result code:
 * code of the result for this attribute def overall
 * SUCCESS: means everything ok
 * EXCEPTION: cant find the attribute def
 * INVALID_QUERY: e.g. if everything blank
 * </pre>
 * @author vsachdeva
 */
public class WsAttributeDefSaveResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * result code of a request
   */
  public static enum WsAttributeDefSaveResultsCode implements WsResultCode {

    /** found the attribute defs, saved them (lite http status code 201) (success: T) */
    SUCCESS(201),

    /** either overall exception, or one or more attribute defs had exceptions (lite http status code 500) (success: F) */
    EXCEPTION(500),

    /** problem saving existing attribute defs (lite http status code 500) (success: F) */
    PROBLEM_SAVING_ATTRIBUTE_DEFS(500),

    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name for version
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
      return this == SUCCESS;
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsAttributeDefSaveResultsCode(int statusCode) {
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
   * results for each attribute defs sent in
   */
  private WsAttributeDefSaveResult[] results;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsAttributeDefSaveResults.class);

  /**
   * results for each attribute def sent in
   * @return the results
   */
  public WsAttributeDefSaveResult[] getResults() {
    return this.results;
  }

  /**
   * results for each attribute def sent in
   * @param results1 the results to set
   */
  public void setResults(WsAttributeDefSaveResult[] results1) {
    this.results = results1;
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
   * assign the code from the enum
   * @param attributeDefSaveResultCode
   * @param clientVersion 
   */
  public void assignResultCode(WsAttributeDefSaveResultsCode attributeDefSaveResultCode,
      GrouperVersion clientVersion) {
    this.getResultMetadata().assignResultCode(attributeDefSaveResultCode, clientVersion);
  }

  /**
   * prcess an exception, log, etc
   * @param wsAttributeDefSaveResultsCodeOverride
   * @param theError
   * @param e
   * @param clientVersion 
   */
  public void assignResultCodeException(
      WsAttributeDefSaveResultsCode wsAttributeDefSaveResultsCodeOverride,
      String theError, Exception e, GrouperVersion clientVersion) {

    if (e instanceof WsInvalidQueryException) {
      wsAttributeDefSaveResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsAttributeDefSaveResultsCodeOverride,
          WsAttributeDefSaveResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsAttributeDefSaveResultsCodeOverride, clientVersion);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsAttributeDefSaveResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsAttributeDefSaveResultsCodeOverride, WsAttributeDefSaveResultsCode.EXCEPTION);
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
  public WsAttributeDefSaveResultsCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsAttributeDefSaveResultsCode
        .valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * make sure if there is an error, to record that as an error
   * @param grouperTransactionType for request
   * @param theSummary
   * @param clientVersion 
   * @return true if success, false if not
   */
  public boolean tallyResults(GrouperTransactionType grouperTransactionType,
      String theSummary, GrouperVersion clientVersion) {
    //maybe already a failure
    boolean successOverall = GrouperUtil.booleanValue(this.getResultMetadata()
        .getSuccess(), true);
    if (this.getResults() != null) {
      // check all entries
      int successes = 0;
      int failures = 0;
      for (WsAttributeDefSaveResult wsAttributeDefSaveResult : this.getResults()) {
        boolean theSuccess = "T".equalsIgnoreCase(wsAttributeDefSaveResult
            .getResultMetadata()
            .getSuccess());
        if (theSuccess) {
          successes++;
        } else {
          failures++;
        }
      }

      //if transaction rolled back all line items, 
      if ((!successOverall || failures > 0) && grouperTransactionType.isTransactional()
          && !grouperTransactionType.isReadonly()) {
        for (WsAttributeDefSaveResult wsAttributeDefSaveResult : this.getResults()) {
          if (GrouperUtil.booleanValue(wsAttributeDefSaveResult.getResultMetadata()
              .getSuccess(),
              true)) {
            wsAttributeDefSaveResult
                .assignResultCode(WsAttributeDefSaveResultCode.TRANSACTION_ROLLED_BACK,
                    clientVersion);
            failures++;
          }
        }
      }

      if (failures > 0) {
        this.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures of saving attribute defs.   ");
        this.assignResultCode(
            WsAttributeDefSaveResultsCode.PROBLEM_SAVING_ATTRIBUTE_DEFS, clientVersion);
        //this might not be a problem
        LOG.warn(this.getResultMetadata().getResultMessage());

      } else {
        this.assignResultCode(WsAttributeDefSaveResultsCode.SUCCESS, clientVersion);
      }
    } else {
      //none is not ok
      this.assignResultCode(WsAttributeDefSaveResultsCode.INVALID_QUERY, clientVersion);
      this.getResultMetadata().setResultMessage(
          "Must pass in at least one attribute def to save");
    }
    //make response descriptive
    if (GrouperUtil.booleanValue(this.getResultMetadata().getSuccess(), false)) {
      this.getResultMetadata().appendResultMessage("Success for: " + theSummary);
      return true;
    }
    //false if need rollback
    return !grouperTransactionType.isTransactional();
  }

}
