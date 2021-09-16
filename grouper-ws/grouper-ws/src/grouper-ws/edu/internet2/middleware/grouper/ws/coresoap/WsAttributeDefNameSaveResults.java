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

import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameSaveResult.WsAttributeDefNameSaveResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.GrouperWsException;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.util.GrouperWsLog;



/**
 * <pre>
 * results for the attribute def names save call.
 * 
 * result code:
 * code of the result for this attribute def name overall
 * SUCCESS: means everything ok
 * EXCEPTION: cant find the attribute def name
 * EXCEPTION: cant find the attribute def name
 * PROBLEM_SAVING_ATTRIBUTE_DEF_NAMES: cant find the attribute def name
 * INVALID_QUERY: e.g. if everything blank
 * </pre>
 * @author mchyzer
 */
public class WsAttributeDefNameSaveResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * result code of a request
   */
  public static enum WsAttributeDefNameSaveResultsCode implements WsResultCode {
  
    /** found the attribute def names, saved them (lite http status code 201) (success: T) */
    SUCCESS(201),
  
    /** either overall exception, or one or more attribute def names had exceptions (lite http status code 500) (success: F) */
    EXCEPTION(500),
  
    /** problem saving existing attribute def names (lite http status code 500) (success: F) */
    PROBLEM_SAVING_ATTRIBUTE_DEF_NAMES(500),
  
    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400);
  
    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name for version
     */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }
  
    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }
  
    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;
  
    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsAttributeDefNameSaveResultsCode(int statusCode) {
      this.httpStatusCode = statusCode;
    }
  
    /**
     * @see edu.internet2.middleware.grouper.ws.WsResultCode#getHttpStatusCode()
     */
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }
  
  }


  /**
   * results for each attribute def name sent in
   */
  private WsAttributeDefNameSaveResult[] results;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * results for each attribute def name sent in
   * @return the results
   */
  public WsAttributeDefNameSaveResult[] getResults() {
    return this.results;
  }

  /**
   * results for each attribute def name sent in
   * @param results1 the results to set
   */
  public void setResults(WsAttributeDefNameSaveResult[] results1) {
    this.results = results1;
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
   * @param groupSaveResultsCode
   * @param clientVersion 
   */
  public void assignResultCode(WsAttributeDefNameSaveResultsCode groupSaveResultsCode, GrouperVersion clientVersion) {
    this.getResultMetadata().assignResultCode(groupSaveResultsCode, clientVersion);
  }

  /**
   * prcess an exception, log, etc
   * @param wsGroupSaveResultsCodeOverride
   * @param theError
   * @param e
   * @param clientVersion 
   */
  public void assignResultCodeException(
      WsAttributeDefNameSaveResultsCode wsGroupSaveResultsCodeOverride, String theError, Exception e, GrouperVersion clientVersion) {
  
    if (e instanceof WsInvalidQueryException) {
      wsGroupSaveResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGroupSaveResultsCodeOverride, WsAttributeDefNameSaveResultsCode.INVALID_QUERY);
      //      if (e.getCause() instanceof GroupNotFoundException) {
      //        wsGroupSaveResultsCodeOverride = WsAttributeDefNameSaveResultsCode.GROUP_NOT_FOUND;
      //      }
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsGroupSaveResultsCodeOverride, clientVersion);
      this.getResultMetadata().appendResultMessageError(e.getMessage());
      this.getResultMetadata().appendResultMessageError(theError);
      GrouperWsException.logWarn(theError, e);
  
    } else {
      wsGroupSaveResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGroupSaveResultsCodeOverride, WsAttributeDefNameSaveResultsCode.EXCEPTION);
      GrouperWsException.logError(theError, e);
  
      this.getResultMetadata().appendResultMessageError(theError);
      this.getResultMetadata().appendResultMessageError(e);
      this.assignResultCode(wsGroupSaveResultsCodeOverride, clientVersion);
  
    }
  }

  /**
   * convert the result code back to enum
   * @return the enum code
   */
  public WsAttributeDefNameSaveResultsCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsAttributeDefNameSaveResultsCode.valueOf(this.getResultMetadata().getResultCode());
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
      for (WsAttributeDefNameSaveResult wsAttributeDefNameSaveResult : this.getResults()) {
        boolean theSuccess = "T".equalsIgnoreCase(wsAttributeDefNameSaveResult.getResultMetadata()
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
        for (WsAttributeDefNameSaveResult wsAttributeDefNameSaveResult : this.getResults()) {
          if (GrouperUtil.booleanValue(wsAttributeDefNameSaveResult.getResultMetadata().getSuccess(),
              true)) {
            wsAttributeDefNameSaveResult
                .assignResultCode(WsAttributeDefNameSaveResultCode.TRANSACTION_ROLLED_BACK, clientVersion);
            failures++;
          }
        }
      }
  
      if (failures > 0) {
        this.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures of saving groups.   ");
        this.assignResultCode(WsAttributeDefNameSaveResultsCode.PROBLEM_SAVING_ATTRIBUTE_DEF_NAMES, clientVersion);
        //this might not be a problem
        GrouperWsException.logWarn(this.getResultMetadata().getResultMessage());
  
      } else {
        this.assignResultCode(WsAttributeDefNameSaveResultsCode.SUCCESS, clientVersion);
      }
    } else {
      //none is not ok
      this.assignResultCode(WsAttributeDefNameSaveResultsCode.INVALID_QUERY, clientVersion);
      this.getResultMetadata().setResultMessage("Must pass in at least one attribute def name to save");
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
