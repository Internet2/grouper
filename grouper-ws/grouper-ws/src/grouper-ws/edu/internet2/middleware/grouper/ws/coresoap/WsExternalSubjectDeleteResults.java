/*******************************************************************************
 * Copyright 2012 Internet2
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
package edu.internet2.middleware.grouper.ws.coresoap;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsExternalSubjectDeleteResult.WsExternalSubjectDeleteResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.util.GrouperWsLog;

/**
 * <pre>
 * results for the external subjects delete call.
 * 
 * result code:
 * code of the result for this externalSubject overall
 * SUCCESS: means everything ok
 * EXTERNAL_SUBJECT_NOT_FOUND: cant find the externalSubject
 * EXTERNAL_SUBJECT_DUPLICATE: found multiple externalSubjects
 * </pre>
 * @author mchyzer
 */
public class WsExternalSubjectDeleteResults implements WsResponseBean, ResultMetadataHolder {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsExternalSubjectDeleteResults.class);

  /**
   * result code of a request
   */
  public static enum WsExternalSubjectDeleteResultsCode implements WsResultCode {

    /** found the externalSubjects, deleted them (lite status code 200) (success: T) */
    SUCCESS(200),

    /** either overall exception, or one or more externalSubjects had exceptions (lite status code 500) (success: F) */
    EXCEPTION(500),

    /** problem deleting existing externalSubjects (lite status code 500) (success: F) */
    PROBLEM_DELETING_EXTERNAL_SUBJECTS(500),

    /** invalid query (e.g. if everything blank) (lite status code 400) (success: F) */
    INVALID_QUERY(400);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name for version */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsExternalSubjectDeleteResultsCode(int statusCode) {
      this.httpStatusCode = statusCode;
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.WsResultCode#getHttpStatusCode()
     */
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }

  }

  /**
   * assign the code from the enum
   * @param externalSubjectsDeleteResultsCode should not be null
   */
  public void assignResultCode(WsExternalSubjectDeleteResultsCode externalSubjectsDeleteResultsCode) {
    this.getResultMetadata().assignResultCode(externalSubjectsDeleteResultsCode);
  }

  /**
   * convert the result code back to enum
   * @return the enum code
   */
  public WsExternalSubjectDeleteResultsCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsExternalSubjectDeleteResultsCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * results for each deletion sent in
   */
  private WsExternalSubjectDeleteResult[] results;

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
  public WsExternalSubjectDeleteResult[] getResults() {
    return this.results;
  }

  /**
   * results for each deletion sent in
   * @param results1 the results to set
   */
  public void setResults(WsExternalSubjectDeleteResult[] results1) {
    this.results = results1;
  }

  /**
   * prcess an exception, log, etc
   * @param wsExternalSubjectDeleteResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsExternalSubjectDeleteResultsCode wsExternalSubjectDeleteResultsCodeOverride, String theError,
      Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsExternalSubjectDeleteResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsExternalSubjectDeleteResultsCodeOverride, WsExternalSubjectDeleteResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsExternalSubjectDeleteResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsExternalSubjectDeleteResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsExternalSubjectDeleteResultsCodeOverride, WsExternalSubjectDeleteResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsExternalSubjectDeleteResultsCodeOverride);

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
      for (WsExternalSubjectDeleteResult wsExternalSubjectDeleteResult : this.getResults()) {
        boolean theSuccess = "T".equalsIgnoreCase(wsExternalSubjectDeleteResult.getResultMetadata()
            .getSuccess());
        if (theSuccess) {
          successes++;
        } else {
          failures++;
        }
      }

      final Map<String, Object> debugMap = GrouperServiceJ2ee.retrieveDebugMap();

      GrouperWsLog.addToLogIfNotBlank(debugMap, "successes", successes);
      GrouperWsLog.addToLogIfNotBlank(debugMap, "failures", failures);

      //if transaction rolled back all line items, 
      if ((!successOverall || failures > 0) && grouperTransactionType.isTransactional()
          && !grouperTransactionType.isReadonly()) {
        successes = 0;
        for (WsExternalSubjectDeleteResult wsExternalSubjectDeleteResult : this.getResults()) {
          if (GrouperUtil.booleanValue(wsExternalSubjectDeleteResult.getResultMetadata()
              .getSuccess(), true)) {
            wsExternalSubjectDeleteResult
                .assignResultCode(WsExternalSubjectDeleteResultCode.TRANSACTION_ROLLED_BACK);
            failures++;
          }
        }
      }

      if (failures > 0) {
        this.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures of deleting externalSubjects.   ");
        this.assignResultCode(WsExternalSubjectDeleteResultsCode.PROBLEM_DELETING_EXTERNAL_SUBJECTS);
        //this might not be a problem
        LOG.warn(this.getResultMetadata().getResultMessage());

      } else {
        this.assignResultCode(WsExternalSubjectDeleteResultsCode.SUCCESS);
      }
    } else {
      //none is not ok
      this.assignResultCode(WsExternalSubjectDeleteResultsCode.INVALID_QUERY);
      this.getResultMetadata().setResultMessage("Must pass in at least one externalSubject to delete");
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
