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
package edu.internet2.middleware.grouper.ws.soap_v2_3;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.soap_v2_3.WsAttributeDefDeleteResult.WsAttributeDefDeleteResultCode;

/**
 * <pre>
 * results for the attribute defs delete call.
 * 
 * result code: code of the result for this attribute def overall
 * SUCCESS: means everything ok
 * ATTRIBUTE_DEF_NOT_FOUND: cant find the attribute def
 * </pre>
 * @author vsachdeva
 */
public class WsAttributeDefDeleteResults {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsAttributeDefDeleteResults.class);

  /**
   * result code of a request
   */
  public static enum WsAttributeDefDeleteResultsCode implements WsResultCode {

    /** found the attribute defs, deleted them (lite status code 200) (success: T) */
    SUCCESS(200),

    /** either overall exception, or one or more attribute had exceptions (lite status code 500) (success: F) */
    EXCEPTION(500),

    /** problem deleting existing attribute defs (lite status code 500) (success: F) */
    PROBLEM_DELETING_ATTRIBUTE_DEFS(500),

    /** invalid query (e.g. if everything blank) (lite status code 400) (success: F) */
    INVALID_QUERY(400);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return  name
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
    private WsAttributeDefDeleteResultsCode(int statusCode) {
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
      return this == SUCCESS;
    }

  }

  /**
   * assign the code from the enum
   * @param attributeDefsDeleteResultsCode should not be null
   */
  public void assignResultCode(
      WsAttributeDefDeleteResultsCode attributeDefsDeleteResultsCode) {
    this.getResultMetadata().assignResultCode(attributeDefsDeleteResultsCode);
  }

  /**
   * convert the result code back to enum
   * @return the enum code
   */
  public WsAttributeDefDeleteResultsCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsAttributeDefDeleteResultsCode.valueOf(this.getResultMetadata()
        .getResultCode());
  }

  /**
   * results for each deletion sent in
   */
  private WsAttributeDefDeleteResult[] results;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * results for each deletion sent in
   * @return the results
   */
  public WsAttributeDefDeleteResult[] getResults() {
    return this.results;
  }

  /**
   * results for each deletion sent in
   * @param results1 the results to set
   */
  public void setResults(WsAttributeDefDeleteResult[] results1) {
    this.results = results1;
  }

  /**
   * prcess an exception, log, etc
   * @param wsAttributeDefDeleteResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsAttributeDefDeleteResultsCode wsAttributeDefDeleteResultsCodeOverride,
      String theError,
      Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsAttributeDefDeleteResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsAttributeDefDeleteResultsCodeOverride,
          WsAttributeDefDeleteResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsAttributeDefDeleteResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsAttributeDefDeleteResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsAttributeDefDeleteResultsCodeOverride,
          WsAttributeDefDeleteResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsAttributeDefDeleteResultsCodeOverride);

    }
  }

  /**
   * make sure if there is an error, to record that as an error
   * @param grouperTransactionType for request
   * @param theSummary of entire request
   * @return true if not need to rollback, and false if so
   */
  public boolean tallyResults(GrouperTransactionType grouperTransactionType,
      String theSummary) {
    //maybe already a failure
    boolean successOverall = GrouperUtil.booleanValue(this.getResultMetadata()
        .getSuccess(), true);
    if (this.getResults() != null) {
      // check all entries
      int successes = 0;
      int failures = 0;
      for (WsAttributeDefDeleteResult wsAttributeDefDeleteResult : this.getResults()) {
        boolean theSuccess = "T".equalsIgnoreCase(wsAttributeDefDeleteResult
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
        successes = 0;
        for (WsAttributeDefDeleteResult wsAttributeDefDeleteResult : this.getResults()) {
          if (GrouperUtil.booleanValue(wsAttributeDefDeleteResult.getResultMetadata()
              .getSuccess(), true)) {
            wsAttributeDefDeleteResult
                .assignResultCode(WsAttributeDefDeleteResultCode.TRANSACTION_ROLLED_BACK);
            failures++;
          }
        }
      }

      if (failures > 0) {
        this.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures of deleting attribute defs.   ");
        this.assignResultCode(
            WsAttributeDefDeleteResultsCode.PROBLEM_DELETING_ATTRIBUTE_DEFS);
        //this might not be a problem
        LOG.warn(this.getResultMetadata().getResultMessage());

      } else {
        this.assignResultCode(WsAttributeDefDeleteResultsCode.SUCCESS);
      }
    } else {
      //none is not ok
      this.assignResultCode(WsAttributeDefDeleteResultsCode.INVALID_QUERY);
      this.getResultMetadata().setResultMessage(
          "Must pass in at least one attribute def to delete");
    }
    //make response descriptive
    if (GrouperUtil.booleanValue(this.getResultMetadata().getSuccess(), false)) {
      this.getResultMetadata().appendResultMessage("Success for: " + theSummary);
      return true;
    }
    //false if need rollback
    return !grouperTransactionType.isTransactional();

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
