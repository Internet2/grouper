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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeBatchResult.WsAssignAttributeBatchResultCode;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * <pre>
 * results for assigning attributes call.
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
public class WsAssignAttributesBatchResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * make sure if there is an error, to record that as an error
   * @param grouperTransactionType for request
   * @param theSummary
   * @return true if success, false if not
   */
  public boolean tallyResults(GrouperTransactionType grouperTransactionType,
      String theSummary) {
    
    //maybe already a failure
    boolean successOverall = GrouperUtil.booleanValue(this.getResultMetadata()
        .getSuccess(), true);
    if (this.getWsAssignAttributeBatchResultArray() != null) {
      // check all entries
      int successes = 0;
      int failures = 0;

      int arrayLength = GrouperUtil.length(this.getWsAssignAttributeBatchResultArray());
      for (int i=0;i<arrayLength;i++) {
        
        WsAssignAttributeBatchResult wsAssignAttributeBatchResult = this.getWsAssignAttributeBatchResultArray()[i];
        if(wsAssignAttributeBatchResult == null) {
          wsAssignAttributeBatchResult = new WsAssignAttributeBatchResult();
          this.getWsAssignAttributeBatchResultArray()[i] = wsAssignAttributeBatchResult;
        }
        if (wsAssignAttributeBatchResult.getResultMetadata() == null) {
          wsAssignAttributeBatchResult.setResultMetadata(new WsResultMeta());
          wsAssignAttributeBatchResult.getResultMetadata().setSuccess("F");
        }
        boolean theSuccess = "T".equalsIgnoreCase(wsAssignAttributeBatchResult.getResultMetadata()
            .getSuccess());
        if (theSuccess) {
          successes++;
        } else {
          failures++;
        }
      }

      //if transaction rolled back all line items, 
      if ((!successOverall || failures > 0) && grouperTransactionType.isTransactional()
          && !grouperTransactionType.isReadonly()) {
        
        for (int i=0;i<arrayLength;i++) {
          
          WsAssignAttributeBatchResult wsAssignAttributeBatchResult = this.getWsAssignAttributeBatchResultArray()[i];
          if (GrouperUtil.booleanValue(
              wsAssignAttributeBatchResult.getResultMetadata().getSuccess(), true)) {
            wsAssignAttributeBatchResult
                .assignResultCode(WsAssignAttributeBatchResultCode.TRANSACTION_ROLLED_BACK);
            failures++;
          }
          
        }
        
      }
      if (failures > 0) {
        this.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures of assigning attributes.   ");
        this.assignResultCode(WsAssignAttributesBatchResultsCode.PROBLEM_WITH_ASSIGNMENT);
        //this might not be a problem
        LOG.warn(this.getResultMetadata().getResultMessage());

      } else {
        this.assignResultCode(WsAssignAttributesBatchResultsCode.SUCCESS);
      }
    } else {
      //none is not ok, must pass one in
      this.assignResultCode(WsAssignAttributesBatchResultsCode.SUCCESS);
      this.getResultMetadata().appendResultMessage(
          "No attribute assignments were passed in, ");
    }
    //make response descriptive
    if (GrouperUtil.booleanValue(this.getResultMetadata().getSuccess(), false)) {
      this.getResultMetadata().appendResultMessage("Success for: " + theSummary);
      return true;
    }
    //false if need rollback
    return !grouperTransactionType.isTransactional();
  }


  /**
   * process an exception, log, etc
   * @param wsAssignAttributesBatchResultsCode
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsAssignAttributesBatchResultsCode wsAssignAttributesBatchResultsCode, String theError, Exception e) {

    wsAssignAttributesBatchResultsCode = GrouperUtil.defaultIfNull(
        wsAssignAttributesBatchResultsCode, WsAssignAttributesBatchResultsCode.PROBLEM_WITH_ASSIGNMENT);
    //a helpful exception will probably be in the getMessage()
    this.assignResultCode(wsAssignAttributesBatchResultsCode);
    theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
    this.getResultMetadata().appendResultMessage(
        theError + ExceptionUtils.getFullStackTrace(e));
    LOG.error(theError, e);

  }


  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsAssignAttributesBatchResults.class);

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
  private WsAssignAttributeBatchResult[] wsAssignAttributeBatchResultArray;
  
  /**
   * the assignment results being queried
   * @return the assignments being queried
   */
  public WsAssignAttributeBatchResult[] getWsAssignAttributeBatchResultArray() {
    return this.wsAssignAttributeBatchResultArray;
  }

  /**
   * the assignment results being queried
   * @param wsAttributeAssignResults1
   */
  public void setWsAssignAttributeBatchResultArray(WsAssignAttributeBatchResult[] wsAttributeAssignResults1) {
    this.wsAssignAttributeBatchResultArray = wsAttributeAssignResults1;
  }

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * result code of a request.  The possible result codes 
   * of WsGetMembersResultCode (with http status codes) are:
   * SUCCESS(200), EXCEPTION(500), INVALID_QUERY(400)
   */
  public static enum WsAssignAttributesBatchResultsCode implements WsResultCode {

    /** found the attributeAssignments (lite status code 200) (success: T) */
    SUCCESS(200),

    /** if one or more entries had problems, but some others succeeded. (success: F) */
    PROBLEM_WITH_ASSIGNMENT(500);
    
    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name
     */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /**
     * construct with http code
     * @param theHttpStatusCode the code
     */
    private WsAssignAttributesBatchResultsCode(int theHttpStatusCode) {
      this.httpStatusCode = theHttpStatusCode;
    }

    /** http status code for result code */
    private int httpStatusCode;

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }

    /** get the http result code for this status code
     * @return the status code
     */
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }
  }

  /**
   * assign the code from the enum
   * @param getAttributeAssignmentsResultCode
   */
  public void assignResultCode(WsAssignAttributesBatchResultsCode getAttributeAssignmentsResultCode) {
    this.getResultMetadata().assignResultCode(getAttributeAssignmentsResultCode);
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
   * groups that are in the results
   */
  private WsGroup[] wsGroups;

  /**
   * stems that are in the results
   */
  private WsStem[] wsStems;

  /**
   * stems that are in the results
   * @return stems
   */
  public WsStem[] getWsStems() {
    return this.wsStems;
  }

  /**
   * stems that are in the results
   * @param wsStems1
   */
  public void setWsStems(WsStem[] wsStems1) {
    this.wsStems = wsStems1;
  }

  /**
   * results for each assignment sent in
   */
  private WsMembership[] wsMemberships;

  /**
   * subjects that are in the results
   */
  private WsSubject[] wsSubjects;

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
   * results for each assignment sent in
   * @return the results
   */
  public WsMembership[] getWsMemberships() {
    return this.wsMemberships;
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
   * results for each assignment sent in
   * @param results1 the results to set
   */
  public void setWsMemberships(WsMembership[] results1) {
    this.wsMemberships = results1;
  }

  /**
   * subjects that are in the results
   * @param wsSubjects1
   */
  public void setWsSubjects(WsSubject[] wsSubjects1) {
    this.wsSubjects = wsSubjects1;
  }

  /**
   * assign results
   * @param attributeAssignBatchResults 
   * @param theSubjectAttributeNames 
   */
  public void assignResult(List<WsAssignAttributeBatchResult> attributeAssignBatchResults, String[] theSubjectAttributeNames) {
    
    this.subjectAttributeNames = theSubjectAttributeNames;

    this.setWsAssignAttributeBatchResultArray(GrouperUtil.toArray(attributeAssignBatchResults, WsAssignAttributeBatchResult.class));
    
  }

  /**
   * sort the results by assignment
   */
  public void sortResults() {
    //dont do this, they should be in order
    //if (this.wsAssignAttributeBatchResultArray != null) {
    //  Arrays.sort(this.wsAssignAttributeBatchResultArray);
    //}
    if (this.wsAttributeDefNames != null) {
      Arrays.sort(this.wsAttributeDefNames);
    }
    if (this.wsAttributeDefs != null) {
      Arrays.sort(this.wsAttributeDefs);
    }
    if (this.wsGroups != null) {
      Arrays.sort(this.wsGroups);
    }
    if (this.wsMemberships != null) {
      Arrays.sort(this.wsMemberships);
    }
    if (this.wsStems != null) {
      Arrays.sort(this.wsStems);
    }
    if (this.wsSubjects != null) {
      Arrays.sort(this.wsSubjects);
    }
  }
  
  /**
   * add a result to the list of results, and keep track of all the related objects
   * @param wsAssignAttributesResults
   * @param theError 
   * @param e 
   * @param index 
   */
  public void addResult(WsAssignAttributesResults wsAssignAttributesResults, String theError, Exception e, int index) {
    //lets collate the results, note, we can make this more efficient later as far as resolving objects
    
    WsAssignAttributeBatchResult wsAssignAttributeBatchResult = null;
    
    //there should only be one result...
    if (wsAssignAttributesResults != null && GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()) > 0) {
      if (GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()) > 1) {
        throw new RuntimeException("Why are there more one result???? " 
            + GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults()));
      }
      wsAssignAttributeBatchResult = 
        new WsAssignAttributeBatchResult(wsAssignAttributesResults, 
            wsAssignAttributesResults.getWsAttributeAssignResults()[0]);
      
      //do the related objects
      this.wsAttributeDefNames = GrouperServiceUtils.mergeArrays(this.wsAttributeDefNames, wsAssignAttributesResults.getWsAttributeDefNames(), "name", WsAttributeDefName.class);
      this.wsAttributeDefs = GrouperServiceUtils.mergeArrays(this.wsAttributeDefs, wsAssignAttributesResults.getWsAttributeDefs(), "name", WsAttributeDef.class);
      this.wsGroups = GrouperServiceUtils.mergeArrays(this.wsGroups, wsAssignAttributesResults.getWsGroups(), "name", WsGroup.class);
      this.wsMemberships = GrouperServiceUtils.mergeArrays(this.wsMemberships, wsAssignAttributesResults.getWsMemberships(), "membershipId", WsMembership.class);
      this.wsStems = GrouperServiceUtils.mergeArrays(this.wsStems, wsAssignAttributesResults.getWsStems(), "name", WsStem.class);
      this.wsSubjects = GrouperServiceUtils.mergeArrays(this.wsSubjects, wsAssignAttributesResults.getWsSubjects(), new String[]{"sourceId", "id"}, WsSubject.class);
      
    } else {
      wsAssignAttributeBatchResult = new WsAssignAttributeBatchResult(wsAssignAttributesResults, theError, e);
      //add a blank one?
    }
    this.wsAssignAttributeBatchResultArray[index] = wsAssignAttributeBatchResult;
    //add it to the array of results
    
    //there should be one result from the assignment
    //tempResults.get

    
  }
  
  
}
