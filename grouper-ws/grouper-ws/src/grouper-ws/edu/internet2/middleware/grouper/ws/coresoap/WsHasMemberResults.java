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

import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
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
 * results for the has member call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * GROUP_DUPLICATE: found multiple groups
 * etc
 * </pre>
 * @author mchyzer
 */
public class WsHasMemberResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * result code of a request
   */
  public static enum WsHasMemberResultsCode implements WsResultCode {

    /** problem discovering if each was a member of not (lite http status code 500) (success: F) */
    PROBLEM_CHECKING_MEMBERS(500),

    /** discovered if each was a member of not (lite http status code 200) (success: T) */
    SUCCESS(200),

    /** could not find group (lite http status code 404) (success: F) */
    GROUP_NOT_FOUND(404),

    /** had an exception while figuring out if the subjects were members (lite http status code 500) (success: F) */
    EXCEPTION(500),

    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400);

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsHasMemberResultsCode(int statusCode) {
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
   * @param hasMemberResultsCode
   */
  public void assignResultCode(WsHasMemberResultsCode hasMemberResultsCode) {
    this.getResultMetadata().assignResultCode(hasMemberResultsCode);
  }

  /**
   * convert the result code back to enum
   * @return the enum code
   */
  public WsHasMemberResultsCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsHasMemberResultsCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * results for each assignment sent in
   */
  private WsHasMemberResult[] results;

  /**
   * group that we are checking 
   */
  private WsGroup wsGroup;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsHasMemberResult[] getResults() {
    return this.results;
  }

  /**
   * results for each assignment sent in
   * @param results1 the results to set
   */
  public void setResults(WsHasMemberResult[] results1) {
    this.results = results1;
  }

  /**
   * @return the wsGroup
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * @param wsGroup1 the wsGroup to set
   */
  public void setWsGroup(WsGroup wsGroup1) {
    this.wsGroup = wsGroup1;
  }

  /**
   * prcess an exception, log, etc
   * @param wsHasMembersResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsHasMemberResultsCode wsHasMembersResultsCodeOverride, String theError, Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsHasMembersResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsHasMembersResultsCodeOverride, WsHasMemberResultsCode.INVALID_QUERY);
      if (e.getCause() instanceof GroupNotFoundException) {
        wsHasMembersResultsCodeOverride = WsHasMemberResultsCode.GROUP_NOT_FOUND;
      }
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsHasMembersResultsCodeOverride);
      this.getResultMetadata().appendResultMessageError(e.getMessage());
      this.getResultMetadata().appendResultMessageError(theError);
      GrouperWsException.logWarn(theError, e);
      
    } else {
      wsHasMembersResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsHasMembersResultsCodeOverride, WsHasMemberResultsCode.EXCEPTION);
      GrouperWsException.logError(theError, e);

      this.getResultMetadata().appendResultMessageError(theError);
      this.getResultMetadata().appendResultMessageError(e);
      this.assignResultCode(wsHasMembersResultsCodeOverride);

    }
  }

  /**
   * make sure if there is an error, to record that as an error
   * @param theSummary of entire request
   */
  public void tallyResults(String theSummary) {
    //maybe already a failure
    boolean successOverall = GrouperUtil.booleanValue(this.getResultMetadata()
        .getSuccess(), true);
    if (this.getResults() != null) {
      // check all entries
      int successes = 0;
      int failures = 0;
      for (WsHasMemberResult wsHasMemberResult : this.getResults()) {
        boolean theSuccess = GrouperUtil.booleanValue(wsHasMemberResult
            .getResultMetadata().getSuccess(), false);
        if (theSuccess) {
          successes++;
        } else {
          failures++;
        }
      }

      final Map<String, Object> debugMap = GrouperServiceJ2ee.retrieveDebugMap();

      GrouperWsLog.addToLogIfNotBlank(debugMap, "successes", successes);
      GrouperWsLog.addToLogIfNotBlank(debugMap, "failures", failures);

      if (failures > 0 || !successOverall) {
        this.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures of checking hasMember.   ");
        this.assignResultCode(WsHasMemberResultsCode.PROBLEM_CHECKING_MEMBERS);
        //this might not be a problem
        GrouperWsException.logWarn(this.getResultMetadata().getResultMessage());

      } else {
        this.assignResultCode(WsHasMemberResultsCode.SUCCESS);
      }
    } else {
      //none is not ok
      this.assignResultCode(WsHasMemberResultsCode.INVALID_QUERY);
      this.getResultMetadata().setResultMessage("Must pass in at least one subject to check for membership");
    }
    //make response descriptive
    if (GrouperUtil.booleanValue(this.getResultMetadata().getSuccess(), false)) {
      this.getResultMetadata().appendResultMessage("Success for: " + theSummary);
    }
  }

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
