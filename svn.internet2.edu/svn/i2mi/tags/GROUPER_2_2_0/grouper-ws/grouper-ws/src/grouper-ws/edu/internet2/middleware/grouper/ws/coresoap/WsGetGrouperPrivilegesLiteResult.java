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
package edu.internet2.middleware.grouper.ws.coresoap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup.SubjectFindResult;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * Result of retrieving privileges for a user/group combo (and perhaps 
 * filtered by type), will
 * return a list of permissions
 * 
 * @author mchyzer
 */
public class WsGetGrouperPrivilegesLiteResult implements WsResponseBean, ResultMetadataHolder {

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * assign the code from the enum
   * @param wsSubjectLookup1
   * @param theSubjectAttributeNames
   */
  public void processSubject(WsSubjectLookup wsSubjectLookup1,
      String[] theSubjectAttributeNames) {

    SubjectFindResult subjectFindResult = wsSubjectLookup1.retrieveSubjectFindResult();

    switch (subjectFindResult) {
      case INVALID_QUERY:
        this.assignResultCode(WsGetGrouperPrivilegesLiteResultCode.INVALID_QUERY);
        break;
      case SOURCE_UNAVAILABLE:
        this.assignResultCode(WsGetGrouperPrivilegesLiteResultCode.EXCEPTION);
        break;
      case SUBJECT_DUPLICATE:
        this.assignResultCode(WsGetGrouperPrivilegesLiteResultCode.SUBJECT_DUPLICATE);
        break;
      case SUBJECT_NOT_FOUND:
        this.assignResultCode(WsGetGrouperPrivilegesLiteResultCode.SUBJECT_NOT_FOUND);
        break;
      case SUCCESS:
        return;
    }

    this.getResultMetadata().setResultMessage(
        "Subject: " + wsSubjectLookup1 + " had problems: " + subjectFindResult);

  }

  /**
   * empty
   */
  public WsGetGrouperPrivilegesLiteResult() {
    //empty
  }

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(WsGetGrouperPrivilegesLiteResult.class);


  /**
   * prcess an exception, log, etc
   * @param wsMemberChangeSubjectLiteResultCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsGetGrouperPrivilegesLiteResultCode wsMemberChangeSubjectLiteResultCodeOverride, 
      String theError, Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsMemberChangeSubjectLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsMemberChangeSubjectLiteResultCodeOverride, WsGetGrouperPrivilegesLiteResultCode.INVALID_QUERY);
      if (e.getCause() instanceof StemNotFoundException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsGetGrouperPrivilegesLiteResultCode.STEM_NOT_FOUND;
      }
      if (e.getCause() instanceof GroupNotFoundException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsGetGrouperPrivilegesLiteResultCode.GROUP_NOT_FOUND;
      }
      if (e.getCause() instanceof SubjectNotFoundException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsGetGrouperPrivilegesLiteResultCode.SUBJECT_NOT_FOUND;
      }
      if (e.getCause() instanceof SubjectNotUniqueException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsGetGrouperPrivilegesLiteResultCode.SUBJECT_DUPLICATE;
      }
      if (e.getCause() instanceof InsufficientPrivilegeException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsGetGrouperPrivilegesLiteResultCode.INSUFFICIENT_PRIVILEGES;
      }
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsMemberChangeSubjectLiteResultCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsMemberChangeSubjectLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsMemberChangeSubjectLiteResultCodeOverride, WsGetGrouperPrivilegesLiteResultCode.EXCEPTION);
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
  public void assignResultCode(WsGetGrouperPrivilegesLiteResultCode memberChangeSubjectLiteResultCode1) {
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
   * Privileges for this query
   */
  private WsGrouperPrivilegeResult[] privilegeResults;
  
  /**
   * result code of a request
   */
  public static enum WsGetGrouperPrivilegesLiteResultCode implements WsResultCode {

    /** didnt have problems (rest http status code 200) (success: T) */
    SUCCESS(200),

    /** didnt have problems, queried for one privilege, and it is allowed (rest http status code 200) (success: T) */
    SUCCESS_ALLOWED(200),

    /** didnt have problems, queried for one privilege, and it wasnt allowed (rest http status code 200) (success: T) */
    SUCCESS_NOT_ALLOWED(200),

    /** some exception occurred (rest http status code 500) (success: F) */
    EXCEPTION(500),

    /** if one request, and that is a duplicate (rest http status code 409) (success: F) */
    SUBJECT_DUPLICATE(409),

    /** if one request, and that is a subject not found (rest http status code 404) (success: F) */
    SUBJECT_NOT_FOUND(404),

    /** cant find group (rest http status code 404) (success: F) */
    GROUP_NOT_FOUND(404),

    /** cant find stem (rest http status code 404) (success: F) */
    STEM_NOT_FOUND(404),

    /** cant find type (rest http status code 404) (success: F) */
    TYPE_NOT_FOUND(404),

    /** cant find name (rest http status code 404) (success: F) */
    NAME_NOT_FOUND(404),

    /** if one request, and that is a insufficient privileges (rest http status code 403) (success: F) */
    INSUFFICIENT_PRIVILEGES(403),

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
      return this.name().startsWith("SUCCESS");
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsGetGrouperPrivilegesLiteResultCode(int statusCode) {
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

  /**
   * field 
   */
  private WsParam[] params;


  /**
   * @return the params
   */
  public WsParam[] getParams() {
    return this.params;
  }

  /**
   * @param params1 the params to set
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

  /**
   * Privileges for this query
   * @return the privileges
   */
  public WsGrouperPrivilegeResult[] getPrivilegeResults() {
    return this.privilegeResults;
  }

  /**
   * Privileges for this query
   * @param privilegeResults1
   */
  public void setPrivilegeResults(WsGrouperPrivilegeResult[] privilegeResults1) {
    this.privilegeResults = privilegeResults1;
  }
}
