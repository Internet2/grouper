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
public class WsAssignPermissionsLiteResults implements WsResponseBean, ResultMetadataHolder {

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
   * attribute def name referenced in the assignments or inputs (and able to read)
   */
  private WsAttributeDefName wsAttributeDefName;
  
  /**
   * attribute def name referenced in the assignments or inputs (and able to read)
   * @return attribute def name
   */
  public WsAttributeDefName getWsAttributeDefName() {
    return this.wsAttributeDefName;
  }

  /**
   * attribute def names referenced in the assignments or inputs (and able to read)
   * @param wsAttributeDefName1
   */
  public void setWsAttributeDefName(WsAttributeDefName wsAttributeDefName1) {
    this.wsAttributeDefName = wsAttributeDefName1;
  }

  /**
   * the assignment results being queried
   */
  private WsAssignPermissionResult wsPermissionAssignResult;
  
  /**
   * the assignment results being queried
   * @return the assignments being queried
   */
  public WsAssignPermissionResult getWsPermissionAssignResult() {
    return this.wsPermissionAssignResult;
  }

  /**
   * the assignment results being queried
   * @param wsPermissionAssignResult1
   */
  public void setWsPermissionAssignResult(WsAssignPermissionResult wsPermissionAssignResult1) {
    this.wsPermissionAssignResult = wsPermissionAssignResult1;
  }

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * 
   */
  public WsAssignPermissionsLiteResults() {
    //default
  }
  
  /**
   * 
   * @param wsAssignPermissionsResults
   */
  public WsAssignPermissionsLiteResults(WsAssignPermissionsResults wsAssignPermissionsResults) {
    this.responseMetadata = wsAssignPermissionsResults.getResponseMetadata();
    this.resultMetadata = wsAssignPermissionsResults.getResultMetadata();
    this.subjectAttributeNames = wsAssignPermissionsResults.getSubjectAttributeNames();
    this.wsPermissionAssignResult = GrouperUtil.arrayPopOne(wsAssignPermissionsResults.getWsAssignPermissionResults());
    this.wsAttributeDefName = GrouperUtil.arrayPopOne(wsAssignPermissionsResults.getWsAttributeDefNames());
    this.wsAttributeDefs = wsAssignPermissionsResults.getWsAttributeDefs();
    this.wsGroup = GrouperUtil.arrayPopOne(wsAssignPermissionsResults.getWsGroups());
    this.wsSubject = GrouperUtil.arrayPopOne(wsAssignPermissionsResults.getWsSubjects());
    
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
   * group that is in the result
   */
  private WsGroup wsGroup;

  /**
   * subjects that are in the result
   */
  private WsSubject wsSubject;

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsAssignPermissionsLiteResults.class);

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
   * @return the wsGroup
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * subject that is in the results
   * @return the subject
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  /**
   * @param wsGroup1 the wsGroup to set
   */
  public void setWsGroup(WsGroup wsGroup1) {
    this.wsGroup = wsGroup1;
  }

  /**
   * subject that is in the results
   * @param wsSubject1
   */
  public void setWsSubject(WsSubject wsSubject1) {
    this.wsSubject = wsSubject1;
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
