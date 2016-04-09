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
/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_3;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * Result of one member changing its subject
 * 
 * @author mchyzer
 */
public class WsMemberChangeSubjectLiteResult {

  /**
   * empty
   */
  public WsMemberChangeSubjectLiteResult() {
    //empty
  }

  /**
   * construct from results of other
   * @param wsMemberChangeSubjectResults
   */
  public WsMemberChangeSubjectLiteResult(WsMemberChangeSubjectResults wsMemberChangeSubjectResults) {

    this.getResultMetadata().copyFields(wsMemberChangeSubjectResults.getResultMetadata());

    WsMemberChangeSubjectResult wsMemberChangeSubjectResult = GrouperServiceUtils
        .firstInArrayOfOne(wsMemberChangeSubjectResults.getResults());
    if (wsMemberChangeSubjectResult != null) {
      this.getResultMetadata().copyFields(wsMemberChangeSubjectResult.getResultMetadata());
      this.getResultMetadata().assignResultCode(
          wsMemberChangeSubjectResult.resultCode().convertToLiteCode());
      this.setWsSubjectNew(wsMemberChangeSubjectResult.getWsSubjectNew());
      this.setWsSubjectOld(wsMemberChangeSubjectResult.getWsSubjectOld());

    }
  }

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(WsMemberChangeSubjectLiteResult.class);


  /**
   * prcess an exception, log, etc
   * @param wsMemberChangeSubjectLiteResultCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsMemberChangeSubjectLiteResultCode wsMemberChangeSubjectLiteResultCodeOverride, 
      String theError, Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsMemberChangeSubjectLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsMemberChangeSubjectLiteResultCodeOverride, WsMemberChangeSubjectLiteResultCode.INVALID_QUERY);
      if (e.getCause() instanceof MemberNotFoundException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsMemberChangeSubjectLiteResultCode.MEMBER_NOT_FOUND;
      }
      if (e.getCause() instanceof SubjectNotFoundException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsMemberChangeSubjectLiteResultCode.SUBJECT_NOT_FOUND;
      }
      if (e.getCause() instanceof SubjectNotUniqueException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsMemberChangeSubjectLiteResultCode.SUBJECT_DUPLICATE;
      }
      if (e.getCause() instanceof InsufficientPrivilegeException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsMemberChangeSubjectLiteResultCode.INSUFFICIENT_PRIVILEGES;
      }
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsMemberChangeSubjectLiteResultCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsMemberChangeSubjectLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsMemberChangeSubjectLiteResultCodeOverride, WsMemberChangeSubjectLiteResultCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsMemberChangeSubjectLiteResultCodeOverride);

    }
  }

  /**
   * assign the code from the enum
   * @param memberChangeSubjectLiteResultCode1
   */
  public void assignResultCode(WsMemberChangeSubjectLiteResultCode memberChangeSubjectLiteResultCode1) {
    this.getResultMetadata().assignResultCode(memberChangeSubjectLiteResultCode1);
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
   * subject to switch to
   */
  private WsSubject wsSubjectNew;

  /**
   * result code of a request
   */
  public static enum WsMemberChangeSubjectLiteResultCode implements WsResultCode {

    /** cant find group (rest http status code 404) (success: F) */
    MEMBER_NOT_FOUND(404),

    /** made the update (rest http status code 200) (success: T) */
    SUCCESS(200),

    /** some exception occurred (rest http status code 500) (success: F) */
    EXCEPTION(500),

    /** if one request, and that is a duplicate (rest http status code 409) (success: F) */
    SUBJECT_DUPLICATE(409),

    /** if one request, and that is a subject not found (rest http status code 404) (success: F) */
    SUBJECT_NOT_FOUND(404),

    /** if one request, and that is a insufficient privileges (rest http status code 403) (success: F) */
    INSUFFICIENT_PRIVILEGES(403),

    /** invalid query (e.g. if everything blank) (rest http status code 400) (success: F) */
    INVALID_QUERY(400);

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
    private WsMemberChangeSubjectLiteResultCode(int statusCode) {
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
   * subject that was changed to
   * @return the subjectId
   */
  public WsSubject getWsSubjectNew() {
    return this.wsSubjectNew;
  }

  /**
   * subject that was changed to
   * @param wsSubject1 the wsSubject1 to set
   */
  public void setWsSubjectNew(WsSubject wsSubject1) {
    this.wsSubjectNew = wsSubject1;
  }

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * subject to switch from
   */
  private WsSubject wsSubjectOld;

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
   * subject that was swapped out
   * @return the subjectId
   */
  public WsSubject getWsSubjectOld() {
    return this.wsSubjectOld;
  }

  /**
   * subject that was swapped out
   * @param wsSubject1 the wsSubject1 to set
   */
  public void setWsSubjectOld(WsSubject wsSubject1) {
    this.wsSubjectOld = wsSubject1;
  }

}
