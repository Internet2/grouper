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

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesResults.WsAssignAttributesResultsCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;

/**
 * <pre>
 * results for assigning permissions call.
 * 
 * result code:
 * code of the result for this attribute assignment overall
 * SUCCESS: means everything ok
 * INSUFFICIENT_PRIVILEGES: problem with some input where privileges are not sufficient
 * INVALID_QUERY: bad inputs
 * EXCEPTION: something bad happened
 * </pre>
 * @author mchyzer
 */
public class WsAssignPermissionsResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * construct
   */
  public WsAssignPermissionsResults() {
    //empty
  }

  /**
   * convert
   * @param wsAssignAttributesResults
   */
  public WsAssignPermissionsResults(WsAssignAttributesResults wsAssignAttributesResults) {
    this.responseMetadata = wsAssignAttributesResults.getResponseMetadata();
    this.resultMetadata = wsAssignAttributesResults.getResultMetadata();
    this.subjectAttributeNames = wsAssignAttributesResults.getSubjectAttributeNames();
    int assignAttributesLength = GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults());
    if (assignAttributesLength > 0) {
      this.wsAssignPermissionResults = new WsAssignPermissionResult[assignAttributesLength];
      for (int i=0;i<assignAttributesLength;i++) {
        this.wsAssignPermissionResults[i] = new WsAssignPermissionResult(wsAssignAttributesResults.getWsAttributeAssignResults()[i]);
      }
    }
    this.wsAttributeDefNames = wsAssignAttributesResults.getWsAttributeDefNames();
    this.wsAttributeDefs = wsAssignAttributesResults.getWsAttributeDefs();
    this.wsGroups = wsAssignAttributesResults.getWsGroups();
    this.wsSubjects = wsAssignAttributesResults.getWsSubjects();
  }
  
  /**
   * attribute def references in the assignments or inputs (and able to be read)
   */
  private WsAttributeDef[] wsAttributeDefs;
  
  /**
   * attribute def references in the assignments or inputs (and able to be read)
   * @return attribute defs
   */
  public WsAttributeDef[] getWsAttributeDefs() {
    return this.wsAttributeDefs;
  }

  /**
   * attribute def references in the assignments or inputs (and able to be read)
   * @param wsAttributeDefs1
   */
  public void setWsAttributeDefs(WsAttributeDef[] wsAttributeDefs1) {
    this.wsAttributeDefs = wsAttributeDefs1;
  }

  /**
   * attribute def names referenced in the assignments or inputs (and able to read)
   */
  private WsAttributeDefName[] wsAttributeDefNames;
  
  /**
   * attribute def names referenced in the assignments or inputs (and able to read)
   * @return attribute def names
   */
  public WsAttributeDefName[] getWsAttributeDefNames() {
    return this.wsAttributeDefNames;
  }

  /**
   * attribute def names referenced in the assignments or inputs (and able to read)
   * @param wsAttributeDefNames1
   */
  public void setWsAttributeDefNames(WsAttributeDefName[] wsAttributeDefNames1) {
    this.wsAttributeDefNames = wsAttributeDefNames1;
  }

  /**
   * the assignment results being queried
   */
  private WsAssignPermissionResult[] wsAssignPermissionResults;
  
  /**
   * the assignment results being queried
   * @return the assignments being queried
   */
  public WsAssignPermissionResult[] getWsAssignPermissionResults() {
    return this.wsAssignPermissionResults;
  }

  /**
   * the assignment results being queried
   * @param wsPermissionAssignResults1
   */
  public void setWsAssignPermissionResults(WsAssignPermissionResult[] wsPermissionAssignResults1) {
    this.wsAssignPermissionResults = wsPermissionAssignResults1;
  }

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

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
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * groups that are in the results
   */
  private WsGroup[] wsGroups;

  /**
   * subjects that are in the results
   */
  private WsSubject[] wsSubjects;

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsAssignPermissionsResults.class);

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
   * @return the wsGroups
   */
  public WsGroup[] getWsGroups() {
    return this.wsGroups;
  }

  /**
   * subjects that are in the results
   * @return the subjects
   */
  public WsSubject[] getWsSubjects() {
    return this.wsSubjects;
  }

  /**
   * @param wsGroup1 the wsGroups to set
   */
  public void setWsGroups(WsGroup[] wsGroup1) {
    this.wsGroups = wsGroup1;
  }

  /**
   * subjects that are in the results
   * @param wsSubjects1
   */
  public void setWsSubjects(WsSubject[] wsSubjects1) {
    this.wsSubjects = wsSubjects1;
  }

  /**
   * prcess an exception, log, etc
   * @param wsGetAttributeAssignmentsResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsAssignAttributesResultsCode wsGetAttributeAssignmentsResultsCodeOverride, String theError,
      Exception e) {
  
    if (e instanceof WsInvalidQueryException) {
      wsGetAttributeAssignmentsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetAttributeAssignmentsResultsCodeOverride, WsAssignAttributesResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsGetAttributeAssignmentsResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);
  
    } else {
      wsGetAttributeAssignmentsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetAttributeAssignmentsResultsCodeOverride, WsAssignAttributesResultsCode.EXCEPTION);
      LOG.error(theError, e);
  
      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsGetAttributeAssignmentsResultsCodeOverride);
  
    }
  }

  /**
   * assign the code from the enum
   * @param getAttributeAssignmentsResultCode
   */
  public void assignResultCode(WsAssignAttributesResultsCode getAttributeAssignmentsResultCode) {
    this.getResultMetadata().assignResultCode(getAttributeAssignmentsResultCode);
  }
}
