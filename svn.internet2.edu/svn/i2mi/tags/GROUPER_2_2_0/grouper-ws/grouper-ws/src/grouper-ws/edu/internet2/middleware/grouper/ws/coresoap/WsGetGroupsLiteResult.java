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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

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
public class WsGetGroupsLiteResult implements WsResponseBean, ResultMetadataHolder {

  /**
   * result code of a request
   */
  public static enum WsGetGroupsLiteResultCode implements WsResultCode {

    /** found the subject (rest http status code 200) (success: T) */
    SUCCESS(200),

    /** found the subject (rest http status code 500) (success: F) */
    EXCEPTION(500),

    /** invalid query (e.g. if everything blank) (rest http status code 400) (success: F) */
    INVALID_QUERY(400),
    
    /** couldnt find the member to query (lite http status code 404) (success: F) */
    MEMBER_NOT_FOUND(404),

    /** couldnt find the subject to query (lite http status code 404) (success: F) */
    SUBJECT_NOT_FOUND(404),

    /** problem querying the subject, was duplicate (lite http status code 409) (success: F) */
    SUBJECT_DUPLICATE(409);

    /**
     * if this is a successful result
     * 
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }

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
    private WsGetGroupsLiteResultCode(int statusCode) {
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
  public void assignResultCode(WsGetGroupsLiteResultCode getGroupsResultsCode) {
    this.getResultMetadata().assignResultCode(getGroupsResultsCode);
  }

  /**
   * convert the result code back to enum
   * 
   * @return the enum code
   */
  public WsGetGroupsLiteResultCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsGetGroupsLiteResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsGetGroupsLiteResult.class);

  /**
   * prcess an exception, log, etc
   * @param WsGetGroupsLiteResultCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsGetGroupsLiteResultCode WsGetGroupsLiteResultCodeOverride, String theError, Exception e) {

    if (e instanceof WsInvalidQueryException) {
      WsGetGroupsLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          WsGetGroupsLiteResultCodeOverride, WsGetGroupsLiteResultCode.INVALID_QUERY);
      if (e.getCause() instanceof SubjectNotFoundException) {
        WsGetGroupsLiteResultCodeOverride = WsGetGroupsLiteResultCode.SUBJECT_NOT_FOUND;
      } else if (e.getCause() instanceof SubjectNotUniqueException) {
        WsGetGroupsLiteResultCodeOverride = WsGetGroupsLiteResultCode.SUBJECT_DUPLICATE;
      } else if (e.getCause() instanceof MemberNotFoundException) {
        WsGetGroupsLiteResultCodeOverride = WsGetGroupsLiteResultCode.MEMBER_NOT_FOUND;
      }
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(WsGetGroupsLiteResultCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      WsGetGroupsLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          WsGetGroupsLiteResultCodeOverride, WsGetGroupsLiteResultCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(WsGetGroupsLiteResultCodeOverride);

    }
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
   * results for each get groups sent in
   */
  private WsGroup[] wsGroups;

  /**
   * subject that was added 
   */
  private WsSubject wsSubject;

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

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsGroup[] getWsGroups() {
    return this.wsGroups;
  }

  /**
   * subject that was added
   * @return the subjectId
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  /**
   * results for each assignment sent in
   * @param results1 the results to set
   */
  public void setWsGroups(WsGroup[] results1) {
    this.wsGroups = results1;
  }

  /**
   * subject that was added
   * @param wsSubject1 the wsSubject1 to set
   */
  public void setWsSubject(WsSubject wsSubject1) {
    this.wsSubject = wsSubject1;
  }

  /**
   * empty
   */
  public WsGetGroupsLiteResult() {
    //empty
  }

  /**
   * construct from results of other
   * @param wsGetGroupsResults
   */
  public WsGetGroupsLiteResult(WsGetGroupsResults wsGetGroupsResults) {
  
    this.getResultMetadata().copyFields(wsGetGroupsResults.getResultMetadata());
    this.setSubjectAttributeNames(wsGetGroupsResults.getSubjectAttributeNames());
  
    WsGetGroupsResult wsGetGroupsResult = GrouperServiceUtils
        .firstInArrayOfOne(wsGetGroupsResults.getResults());
    if (wsGetGroupsResult != null) {
      this.getResultMetadata().copyFields(wsGetGroupsResult.getResultMetadata());
      this.getResultMetadata().assignResultCode(
          wsGetGroupsResult.resultCode().convertToLiteCode());
      this.setWsSubject(wsGetGroupsResult.getWsSubject());
      this.setWsGroups(wsGetGroupsResult.getWsGroups());
    }
  }

}
