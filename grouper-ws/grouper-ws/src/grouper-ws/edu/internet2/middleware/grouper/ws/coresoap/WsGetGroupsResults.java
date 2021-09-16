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

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.GrouperWsException;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.util.GrouperWsLog;

/**
 * <pre>
 * results for the get groups call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * EXCEPTION: something bad happened
 * etc.
 * </pre>
 * 
 * @author mchyzer
 */
public class WsGetGroupsResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * result code of a request
   */
  public static enum WsGetGroupsResultsCode implements WsResultCode {

    /** found the subject (rest http status code 201) (success: T)
     * CH 20120210: Why is this a 201????  shouldnt it be 200????
     */
    SUCCESS(201),

    /** found the subject (rest http status code 500) (success: F) */
    EXCEPTION(500),

    /** problem deleting existing members (rest http status code 500) (success: F) */
    PROBLEM_RETRIEVING_GROUPS(500),

    /** invalid query (e.g. if everything blank) (rest http status code 400) (success: F) */
    INVALID_QUERY(400);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /**
     * if this is a successful result
     * 
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
    private WsGetGroupsResultsCode(int statusCode) {
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
   * assign the code from the enum
   * 
   * @param getGroupsResultsCode
   */
  public void assignResultCode(WsGetGroupsResultsCode getGroupsResultsCode) {
    this.getResultMetadata().assignResultCode(getGroupsResultsCode);
  }

  /**
   * convert the result code back to enum
   * 
   * @return the enum code
   */
  public WsGetGroupsResultsCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsGetGroupsResultsCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * results for each assignment sent in
   */
  private WsGetGroupsResult[] results;

  /**
   * prcess an exception, log, etc
   * @param wsGetGroupsResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsGetGroupsResultsCode wsGetGroupsResultsCodeOverride, String theError, Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsGetGroupsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetGroupsResultsCodeOverride, WsGetGroupsResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsGetGroupsResultsCodeOverride);
      this.getResultMetadata().appendResultMessageError(e.getMessage());
      this.getResultMetadata().appendResultMessageError(theError);
      GrouperWsException.logWarn(theError, e);

    } else {
      wsGetGroupsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetGroupsResultsCodeOverride, WsGetGroupsResultsCode.EXCEPTION);
      GrouperWsException.logError(theError, e);

      this.getResultMetadata().appendResultMessageError(theError);
      this.getResultMetadata().appendResultMessageError(e);
      this.assignResultCode(wsGetGroupsResultsCodeOverride);

    }
  }

  /**
   * make sure if there is an error, to record that as an error
   * @param grouperTransactionType for request
   * @param theSummary
   */
  public void tallyResults(String theSummary) {
    //maybe already a failure
    boolean successOverall = GrouperUtil.booleanValue(this.getResultMetadata()
        .getSuccess(), true);
    if (this.getResults() != null) {
      // check all entries
      int successes = 0;
      int failures = 0;
      for (WsGetGroupsResult wsGetGroupsResult : this.getResults()) {
        boolean theSuccess = "T".equalsIgnoreCase(wsGetGroupsResult.getResultMetadata()
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

      if (failures > 0) {
        this.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures of groups retrieved for subjects.   ");
        this.assignResultCode(WsGetGroupsResultsCode.PROBLEM_RETRIEVING_GROUPS);
        //this might not be a problem
        GrouperWsException.logWarn(this.getResultMetadata().getResultMessage());

      } else {
        //if we havent already seen an error...
        if (successOverall) {
          this.assignResultCode(WsGetGroupsResultsCode.SUCCESS);
        }
      }
    } else {
      //none is not ok, must pass one in
      this.assignResultCode(WsGetGroupsResultsCode.INVALID_QUERY);
      this.getResultMetadata().appendResultMessage(
          "You must pass in at least one subject");
    }
    //make response descriptive
    if (GrouperUtil.booleanValue(this.getResultMetadata().getSuccess(), false)) {
      this.getResultMetadata().appendResultMessage("Success for: " + theSummary);
      return;
    }
  }

  /**
   * results for each assignment sent in
   * 
   * @return the results
   */
  public WsGetGroupsResult[] getResults() {
    return this.results;
  }

  /**
   * results for each assignment sent in
   * 
   * @param results1
   *            the results to set
   */
  public void setResults(WsGetGroupsResult[] results1) {
    this.results = results1;
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @return the attributeNames
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @param attributeNamesa the attributeNames to set
   */
  public void setSubjectAttributeNames(String[] attributeNamesa) {
    this.subjectAttributeNames = attributeNamesa;
  }

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  
  /**
   * @param responseMetadata1 the responseMetadata to set
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }

}
