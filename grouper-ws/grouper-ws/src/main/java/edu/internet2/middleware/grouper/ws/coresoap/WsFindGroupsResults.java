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

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.GrouperWsException;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * returned from the group find query
 * 
 * @author mchyzer
 * 
 */
@ApiModel(description = "Groups returned from the group find query")
public class WsFindGroupsResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * result code of a request
   */
  public static enum WsFindGroupsResultsCode implements WsResultCode {

    /** found the subject (lite http status code 200) (success: T) */
    SUCCESS(200),

    /** found the subject (lite http status code 500) (success: F) */
    EXCEPTION(500),

    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400),

    /** cant find the stem in a stem search (lite http status code 404) (success: F) */
    STEM_NOT_FOUND(404);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsFindGroupsResultsCode(int statusCode) {
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
     * 
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }
  }

  /**
   * assign the code from the enum
   * 
   * @param wsFindGroupsResultsCode
   */
  public void assignResultCode(WsFindGroupsResultsCode wsFindGroupsResultsCode) {
    this.getResultMetadata().assignResultCode(wsFindGroupsResultsCode);
  }

  /**
   * put a group in the results
   * @param includeDetail true if the detail for each group should be included
   * @param group
   */
  public void assignGroupResult(Group group, boolean includeDetail) {
    this.assignGroupResult(GrouperUtil.toSet(group), includeDetail);
  }

  /**
   * put a group in the results
   * @param includeDetail true if the detail for each group should be included
   * @param groupSet
   */
  public void assignGroupResult(Set<Group> groupSet, boolean includeDetail) {
    this.setGroupResults(WsGroup.convertGroups(groupSet, includeDetail));
  }

  /**
   * has 0 to many groups that match the query
   */
  private WsGroup[] groupResults;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * has 0 to many groups that match the query by example
   * 
   * @return the groupResults
   */
  @ApiModelProperty(value = "has 0 to many groups that match the query")
  public WsGroup[] getGroupResults() {
    return this.groupResults;
  }

  /**
   * basic results to the query
   * @param groupResults1 the groupResults to set
   */
  public void setGroupResults(WsGroup[] groupResults1) {
    this.groupResults = groupResults1;
  }

  /**
   * prcess an exception, log, etc
   * @param wsFindGroupsResultsCodeOverride 
   * @param wsAddMemberResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsFindGroupsResultsCode wsFindGroupsResultsCodeOverride, String theError,
      Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsFindGroupsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsFindGroupsResultsCodeOverride, WsFindGroupsResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsFindGroupsResultsCodeOverride);
      this.getResultMetadata().appendResultMessageError(e.getMessage());
      this.getResultMetadata().appendResultMessageError(theError);
      GrouperWsException.logWarn(theError, e);

    } else {
      wsFindGroupsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsFindGroupsResultsCodeOverride, WsFindGroupsResultsCode.EXCEPTION);
      GrouperWsException.logError(theError, e);

      this.getResultMetadata().appendResultMessageError(theError);
      this.getResultMetadata().appendResultMessageError(e);
      this.assignResultCode(wsFindGroupsResultsCodeOverride);

    }
  }

  /**
   * @return the resultMetadata
   */
  @ApiModelProperty(value = "Result code, if success, status code, result message")
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  @ApiModelProperty(value = "Server version, millis elapsed on server, and warnings")
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
