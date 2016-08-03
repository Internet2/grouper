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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;

/**
 * returned from the external subject find query
 * 
 * @author mchyzer
 * 
 */
public class WsFindExternalSubjectsResults implements WsResponseBean, ResultMetadataHolder {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsFindExternalSubjectsResults.class);

  /**
   * result code of a request
   */
  public static enum WsFindExternalSubjectsResultsCode implements WsResultCode {

    /** found the subject (lite http status code 200) (success: T) */
    SUCCESS(200),

    /** found the subject (lite http status code 500) (success: F) */
    EXCEPTION(500),

    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name for version 
     */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsFindExternalSubjectsResultsCode(int statusCode) {
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
   * @param wsFindExternalSubjectsResultsCode
   */
  public void assignResultCode(WsFindExternalSubjectsResultsCode wsFindExternalSubjectsResultsCode) {
    this.getResultMetadata().assignResultCode(wsFindExternalSubjectsResultsCode);
  }

  /**
   * put a external subject in the results
   * @param externalSubject
   */
  public void assignExternalSubjectResult(ExternalSubject externalSubject) {
    this.assignExternalSubjectResult(GrouperUtil.toSet(externalSubject));
  }

  /**
   * put a external subjects in the results
   * @param externalSubjectSet
   */
  public void assignExternalSubjectResult(Set<ExternalSubject> externalSubjectSet) {
    this.setExternalSubjectResults(WsExternalSubject.convertExternalSubjects(externalSubjectSet));
  }

  /**
   * has 0 to many externalSubjects that match the query
   */
  private WsExternalSubject[] externalSubjectResults;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * has 0 to many externalSubjects that match the query by example
   * 
   * @return the externalSubjectResults
   */
  public WsExternalSubject[] getExternalSubjectResults() {
    return this.externalSubjectResults;
  }

  /**
   * basic results to the query
   * @param externalSubjectResults1 the groupResults to set
   */
  public void setExternalSubjectResults(WsExternalSubject[] externalSubjectResults1) {
    this.externalSubjectResults = externalSubjectResults1;
  }

  /**
   * prcess an exception, log, etc
   * @param wsFindExternalSubjectsResultsCodeOverride 
   * @param wsAddMemberResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsFindExternalSubjectsResultsCode wsFindExternalSubjectsResultsCodeOverride, String theError,
      Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsFindExternalSubjectsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsFindExternalSubjectsResultsCodeOverride, WsFindExternalSubjectsResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsFindExternalSubjectsResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsFindExternalSubjectsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsFindExternalSubjectsResultsCodeOverride, WsFindExternalSubjectsResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsFindExternalSubjectsResultsCodeOverride);

    }
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
