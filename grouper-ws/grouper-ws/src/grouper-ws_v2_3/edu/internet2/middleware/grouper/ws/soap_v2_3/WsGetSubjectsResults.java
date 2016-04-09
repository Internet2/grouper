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
package edu.internet2.middleware.grouper.ws.soap_v2_3;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.subject.TooManyResultsWhenFilteringByGroupException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectTooManyResults;

/**
 * <pre>
 * results for the get memberships call, or the get memberships lite call
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * GROUP_NOT_FOUND: cant find the group
 * INVALID_QUERY: bad inputs
 * EXCEPTION: something bad happened
 * </pre>
 * @author mchyzer
 */
public class WsGetSubjectsResults {

  /**
   * result metadata
   * @param resultMetadata1
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }


  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsGetSubjectsResults.class);

  /**
   * result code of a request
   */
  public static enum WsGetSubjectsResultsCode implements WsResultCode {

    /** cant find group (lite http status code 500) (success: F) */
    GROUP_NOT_FOUND(404),

    /** found the subject results [at least one, look at each one for individual results, if no successes, this will not be success] (lite http status code 200) (success: T) */
    SUCCESS(200),

    /** bad input (lite http status code 500) (success: F) */
    INVALID_QUERY(500),

    /** if a search string generates too many results (lite http status code 500) (success: F) */
    TOO_MANY_RESULTS(500),

    /** if a search string generates too many results (less than other too many, e.g. 1000 results) (lite http status code 500) (success: F) */
    TOO_MANY_GROUP_FILTER_RESULTS(500),

    /** something bad happened (lite http status code 500) (success: F) */
    EXCEPTION(500),
    
    /** if one request, and that is a insufficient privileges (rest http status code 403) (success: F) */
    INSUFFICIENT_PRIVILEGES(403);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name
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
    private WsGetSubjectsResultsCode(int statusCode) {
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
   * @param getMembersResultCode
   */
  public void assignResultCode(WsGetSubjectsResultsCode getMembersResultCode) {
    this.getResultMetadata().assignResultCode(getMembersResultCode);
  }

  
  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * subjects that are in the results
   */
  private WsSubject[] wsSubjects;

  /**
   * subjects that are in the results
   * @return the subjects
   */
  public WsSubject[] getWsSubjects() {
    return this.wsSubjects;
  }

  /**
   * subjects that are in the results
   * @param wsSubjects1
   */
  public void setWsSubjects(WsSubject[] wsSubjects1) {
    this.wsSubjects = wsSubjects1;
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

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
   * convert members to subject results
   * @param subjectSet 
   * @param includeGroupDetail 
   * @param includeSubjectDetail 
   * @param theSubjectAttributeNames 
   * @param wsSubjectLookups 
   */
  public void assignResult(Set<Subject> subjectSet, boolean includeGroupDetail, 
      boolean includeSubjectDetail, String[] theSubjectAttributeNames, Set<WsSubjectLookup> wsSubjectLookups) {
    
    this.subjectAttributeNames = theSubjectAttributeNames;

    //index the subject lookups
    Map<MultiKey, WsSubjectLookup> subjectLookupMap = new HashMap<MultiKey, WsSubjectLookup>();
    for (WsSubjectLookup wsSubjectLookup : GrouperUtil.nonNull(wsSubjectLookups)) {
      MultiKey key = new MultiKey(wsSubjectLookup.getSubjectId(), wsSubjectLookup.getSubjectSourceId());
      subjectLookupMap.put(key, wsSubjectLookup);
    }

    if (subjectSet.size() > 0) {
      this.wsSubjects = new WsSubject[subjectSet.size()];
      int index = 0;
      for (Subject subject : subjectSet) {
        
        //lets see if there is a lookup
        MultiKey key = new MultiKey(subject.getSourceId(), subject.getSourceId());
        WsSubjectLookup wsSubjectLookup = subjectLookupMap.get(key);
        
        this.wsSubjects[index] = new WsSubject(subject, theSubjectAttributeNames, wsSubjectLookup);
        
        index++;
      }
    }    
    
    this.sortResults();
    
  }

  /**
   * sort the memberships by group, then subject, then list, then membership type.  
   * sort the groups by name, and the subjects by sourceId,subjectId
   */
  private void sortResults() {
    //maybe we shouldnt do this for huge resultsets, but this makes things more organized and easier to test
    if (this.wsSubjects != null) {
      Arrays.sort(this.wsSubjects);
    }
  }
  
  /**
   * process an exception, log, etc
   * @param wsGetMembershipsResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsGetSubjectsResultsCode wsGetMembershipsResultsCodeOverride, String theError,
      Exception e) {

    if (e instanceof SubjectTooManyResults) {

      this.assignResultCode(WsGetSubjectsResultsCode.INSUFFICIENT_PRIVILEGES);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);
    } else if (e instanceof InsufficientPrivilegeException) {

      this.assignResultCode(WsGetSubjectsResultsCode.INSUFFICIENT_PRIVILEGES);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);
    
    } else if (e instanceof GroupNotFoundException) {

      this.assignResultCode(WsGetSubjectsResultsCode.GROUP_NOT_FOUND);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);
      
    } else if (e instanceof TooManyResultsWhenFilteringByGroupException) {
      this.assignResultCode(WsGetSubjectsResultsCode.TOO_MANY_GROUP_FILTER_RESULTS);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);
      
    } else if (e instanceof WsInvalidQueryException) {
      wsGetMembershipsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetMembershipsResultsCodeOverride, WsGetSubjectsResultsCode.INVALID_QUERY);
      if (e.getCause() instanceof GroupNotFoundException) {
        wsGetMembershipsResultsCodeOverride = WsGetSubjectsResultsCode.GROUP_NOT_FOUND;
      }
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsGetMembershipsResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsGetMembershipsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetMembershipsResultsCodeOverride, WsGetSubjectsResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsGetMembershipsResultsCodeOverride);

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
   * group filtering for
   */
  private WsGroup wsGroup;

  /**
   * @return the wsGroups
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * @param wsGroup1 the wsGroups to set
   */
  public void setWsGroup(WsGroup wsGroup1) {
    this.wsGroup = wsGroup1;
  }


}
