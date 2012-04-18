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

import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameDeleteResult.WsAttributeDefNameDeleteResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;



/**
 * <pre>
 * results for the attribute def names delete call.
 * 
 * result code:
 * code of the result for this attribute def name overall
 * SUCCESS: means everything ok
 * ATTRIBUTE_DEF_NAME_NOT_FOUND: cant find the attribute def name
 * </pre>
 * @author mchyzer
 */
public class WsAttributeDefNameDeleteResults implements ResultMetadataHolder, WsResponseBean {

  /**
   * result code of a request.  The possible result codes 
   * of WsAttributeDefNameDeleteResultsCode (with http status codes) are:
   * SUCCESS(200), EXCEPTION(500), INVALID_QUERY(400), INSUFFICIENT_PRIVILEGES(403)
   */
  public static enum WsAttributeDefNameDeleteResultsCode implements WsResultCode {
  
    /** deleted attribute def names (lite status code 200) (success: T) */
    SUCCESS(200),
  
    /** something bad happened (lite status code 500) (success: F) */
    EXCEPTION(500),
  
    /** invalid query (e.g. if everything blank) (lite status code 400) (success: F) */
    INVALID_QUERY(400),
    
    /** if one or more attribute def names could not be deleted (e.g. if everything blank) (lite status code 500) (success: F) */
    PROBLEM_DELETING_ATTRIBUTE_DEF_NAMES(500),
    
    /** 
     * not allowed to assign or remove attribute def name inheritance based on privileges on the attribute definition
     */
    INSUFFICIENT_PRIVILEGES(403);
    
    
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
    private WsAttributeDefNameDeleteResultsCode(int theHttpStatusCode) {
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
   * results for each deletion sent in
   */
  private WsAttributeDefNameDeleteResult[] results;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsAttributeDefNameDeleteResults.class);

  /**
   * results for each deletion sent in
   * @return the results
   */
  public WsAttributeDefNameDeleteResult[] getResults() {
    return this.results;
  }

  /**
   * results for each deletion sent in
   * @param results1 the results to set
   */
  public void setResults(WsAttributeDefNameDeleteResult[] results1) {
    this.results = results1;
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

  /**
   * assign the code from the enum
   * @param attributeDefNamesDeleteResultsCode should not be null
   */
  public void assignResultCode(WsAttributeDefNameDeleteResultsCode attributeDefNamesDeleteResultsCode) {
    this.getResultMetadata().assignResultCode(attributeDefNamesDeleteResultsCode);
  }

  /**
   * prcess an exception, log, etc
   * @param wsAttributeDefNameDeleteResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsAttributeDefNameDeleteResultsCode wsAttributeDefNameDeleteResultsCodeOverride, String theError,
      Exception e) {
  
    if (e instanceof WsInvalidQueryException) {
      wsAttributeDefNameDeleteResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsAttributeDefNameDeleteResultsCodeOverride, WsAttributeDefNameDeleteResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsAttributeDefNameDeleteResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);
  
    } else {
      wsAttributeDefNameDeleteResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsAttributeDefNameDeleteResultsCodeOverride, WsAttributeDefNameDeleteResultsCode.EXCEPTION);
      LOG.error(theError, e);
  
      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsAttributeDefNameDeleteResultsCodeOverride);
  
    }
  }

  /**
   * convert the result code back to enum
   * @return the enum code
   */
  public WsAttributeDefNameDeleteResultsCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsAttributeDefNameDeleteResultsCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * make sure if there is an error, to record that as an error
   * @param grouperTransactionType for request
   * @param theSummary of entire request
   * @return true if not need to rollback, and false if so
   */
  public boolean tallyResults(GrouperTransactionType grouperTransactionType,
      String theSummary) {
    //maybe already a failure
    boolean successOverall = GrouperUtil.booleanValue(this.getResultMetadata()
        .getSuccess(), true);
    if (this.getResults() != null) {
      // check all entries
      int successes = 0;
      int failures = 0;
      for (WsAttributeDefNameDeleteResult wsAttributeDefNameDeleteResult : this.getResults()) {
        boolean theSuccess = "T".equalsIgnoreCase(wsAttributeDefNameDeleteResult.getResultMetadata()
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
        successes = 0;
        for (WsAttributeDefNameDeleteResult wsAttributeDefNameDeleteResult : this.getResults()) {
          if (GrouperUtil.booleanValue(wsAttributeDefNameDeleteResult.getResultMetadata()
              .getSuccess(), true)) {
            wsAttributeDefNameDeleteResult
                .assignResultCode(WsAttributeDefNameDeleteResultCode.TRANSACTION_ROLLED_BACK);
            failures++;
          }
        }
      }
  
      if (failures > 0) {
        this.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures of deleting attribute def names.   ");
        this.assignResultCode(WsAttributeDefNameDeleteResultsCode.PROBLEM_DELETING_ATTRIBUTE_DEF_NAMES);
        //this might not be a problem
        LOG.warn(this.getResultMetadata().getResultMessage());
  
      } else {
        this.assignResultCode(WsAttributeDefNameDeleteResultsCode.SUCCESS);
      }
    } else {
      //none is not ok
      this.assignResultCode(WsAttributeDefNameDeleteResultsCode.INVALID_QUERY);
      this.getResultMetadata().setResultMessage("Must pass in at least one attribute def name to delete");
    }
    //make response descriptive
    if (GrouperUtil.booleanValue(this.getResultMetadata().getSuccess(), false)) {
      this.getResultMetadata().appendResultMessage("Success for: " + theSummary);
      return true;
    }
    //false if need rollback
    return !grouperTransactionType.isTransactional();
  
  }
}
