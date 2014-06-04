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

import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * <pre>
 * results for the has member call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * GROUP_NOT_FOUND: cant find the group
 * GROUP_DUPLICATE: found multiple groups
 * </pre>
 * @author mchyzer
 */
public class WsHasMemberLiteResult implements WsResponseBean, ResultMetadataHolder {

  /**
   * logger 
   */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(WsHasMemberLiteResult.class);

  /**
   * result code of a request
   */
  public static enum WsHasMemberLiteResultCode implements WsResultCode {

    /** discovered if each was a member of not (lite http status code 404) (success: F) */
    GROUP_NOT_FOUND(404),

    /** had an exception while figuring out if the subjects were members (lite http status code 500) (success: F) */
    EXCEPTION(500),

    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400), 
    
    /** the subject is a member (lite http status code 200) (success = T) */
    IS_MEMBER(200), 
    
    /** the subject was found and is not a member (lite http status code 200) (success = T) */
    IS_NOT_MEMBER(200), 
    
    /** found multiple results (lite http status code 409) (success = F) */
    SUBJECT_DUPLICATE(409), 
    
    /** cant find the subject (lite http status code 404) (success = F) */
    SUBJECT_NOT_FOUND(404);

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
    private WsHasMemberLiteResultCode(int statusCode) {
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
      return this.equals(IS_MEMBER) || this.equals(IS_NOT_MEMBER);
    }
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
   * sujbect info for hasMember 
   */
  private WsSubject wsSubject;

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
   * @return the wsSubject
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  /**
   * @param wsSubjectResult1 the wsSubject to set
   */
  public void setWsSubject(WsSubject wsSubjectResult1) {
    this.wsSubject = wsSubjectResult1;
  }

  /**
   * construct from results of other
   */
  public WsHasMemberLiteResult() {
    //empty
  }
  /**
   * construct from results of other
   * @param wsHasMemberResults
   */
  public WsHasMemberLiteResult(WsHasMemberResults wsHasMemberResults) {

    this.getResultMetadata().copyFields(wsHasMemberResults.getResultMetadata());
    this.setSubjectAttributeNames(wsHasMemberResults.getSubjectAttributeNames());
    this.setWsGroup(wsHasMemberResults.getWsGroup());

    WsHasMemberResult wsHasMemberResult = GrouperServiceUtils
        .firstInArrayOfOne(wsHasMemberResults.getResults());
    if (wsHasMemberResult != null) {
      this.getResultMetadata().copyFields(wsHasMemberResult.getResultMetadata());
      this.getResultMetadata().assignResultCode(
          wsHasMemberResult.resultCode().convertToLiteCode());
      this.setWsSubject(wsHasMemberResult.getWsSubject());
    }
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  /**
   * assign the code from the enum
   * @param addMemberLiteResultCode1
   */
  public void assignResultCode(WsHasMemberLiteResultCode addMemberLiteResultCode1) {
    this.getResultMetadata().assignResultCode(addMemberLiteResultCode1);
  }

  /**
   * prcess an exception, log, etc
   * @param wsHasMemberLiteResultCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsHasMemberLiteResultCode wsHasMemberLiteResultCodeOverride, 
      String theError, Exception e) {
  
    if (e instanceof WsInvalidQueryException) {
      wsHasMemberLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsHasMemberLiteResultCodeOverride, WsHasMemberLiteResultCode.INVALID_QUERY);
      if (e.getCause() instanceof GroupNotFoundException) {
        wsHasMemberLiteResultCodeOverride = WsHasMemberLiteResultCode.GROUP_NOT_FOUND;
      }
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsHasMemberLiteResultCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);
  
    } else {
      wsHasMemberLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsHasMemberLiteResultCodeOverride, WsHasMemberLiteResultCode.EXCEPTION);
      LOG.error(theError, e);
  
      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsHasMemberLiteResultCodeOverride);
  
    }
  }

}
