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
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.coresoap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesResults.WsAssignAttributesResultsCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;


/**
 * holds an attribute assign result.  Also holds value results (if value operations were performed).
 * note if attribute assignments have values and the attribute is removed, the values will not be in 
 * this result
 */
public class WsAssignAttributeBatchResult implements Comparable<WsAssignAttributeBatchResult> {

  /**
   * assign the code from the enum
   * @param wsAssignAttributeBatchResultCode
   */
  public void assignResultCode(WsAssignAttributeBatchResultCode wsAssignAttributeBatchResultCode) {
    this.getResultMetadata().assignResultCode(
        wsAssignAttributeBatchResultCode == null ? null : wsAssignAttributeBatchResultCode.name());
    this.getResultMetadata().assignSuccess(wsAssignAttributeBatchResultCode.isSuccess() ? "T" : "F");
  }


  /**
   * constructor
   */
  public WsAssignAttributeBatchResult() {
    super();
  }
  
  /**
   * 
   * @param wsAssignAttributesResults
   * @param wsAssignAttributeResult
   */
  public WsAssignAttributeBatchResult(WsAssignAttributesResults wsAssignAttributesResults, 
      WsAssignAttributeResult wsAssignAttributeResult) {
    this.setChanged(wsAssignAttributeResult.getChanged());
    this.setDeleted(wsAssignAttributeResult.getDeleted());
    this.setValuesChanged(wsAssignAttributeResult.getValuesChanged());
    this.wsAttributeAssigns = wsAssignAttributeResult.getWsAttributeAssigns();
    this.resultMetadata = wsAssignAttributesResults.getResultMetadata();
    WsAssignAttributesResultsCode wsAssignAttributesResultsCode = 
      WsAssignAttributesResults.WsAssignAttributesResultsCode.valueOfIgnoreCase(this.resultMetadata.getResultCode(), false);
    this.resultMetadata.setResultCode(
        WsAssignAttributeBatchResultCode.convertFromWsAssignAttributesResultCode(wsAssignAttributesResultsCode).name());
    
    this.setWsAttributeAssignValueResults(wsAssignAttributeResult.getWsAttributeAssignValueResults());
    
  }
  
  /**
   * construct with error/exception.  default to the result if possible, else assign
   * @param wsAssignAttributesResults
   * @param theError
   * @param e
   */
  public WsAssignAttributeBatchResult(WsAssignAttributesResults wsAssignAttributesResults,
      String theError, Exception e) {
    
    if (wsAssignAttributesResults != null) {
      this.resultMetadata = wsAssignAttributesResults.getResultMetadata();
    }
    if (this.resultMetadata == null) {
      this.resultMetadata = new WsResultMeta();
    }
    String stack = e == null ? "" : ExceptionUtils.getFullStackTrace(e);

    if (StringUtils.isBlank(this.resultMetadata.getResultMessage())) {
      this.resultMetadata.setResultMessage(theError + ", " + stack);
    } else {
      if (!StringUtils.isBlank(theError)) {
        if (!StringUtils.defaultString(this.resultMetadata.getResultMessage()).contains(theError)) {
          this.resultMetadata.setResultMessage(StringUtils.defaultString(this.resultMetadata.getResultMessage()) + ", " + theError);
        }
      }
      
      if (e != null) {
        
        if (!StringUtils.defaultString(this.resultMetadata.getResultMessage()).contains(stack)) {
          this.resultMetadata.setResultMessage(StringUtils.defaultString(this.resultMetadata.getResultMessage()) + ", " + stack);
        }
        
      }
    }    
    if (!StringUtils.isBlank(this.resultMetadata.getResultCode())) {
      this.resultMetadata.setResultCode(
          WsAssignAttributeBatchResultCode.convertFromWsAssignAttributesResultCode(
              WsAssignAttributesResultsCode.valueOfIgnoreCase(this.resultMetadata.getResultCode(), true)).name());
    } else {
      if (e instanceof WsInvalidQueryException) {
        this.resultMetadata.setResultCode(WsAssignAttributeBatchResultCode.INVALID_QUERY.name());
      } else {
        this.resultMetadata.setResultCode(WsAssignAttributeBatchResultCode.EXCEPTION.name());
      }
    }
  }
  
  /**
   * result code of a request.  The possible result codes 
   * of WsGetMembersResultCode (with http status codes) are:
   * SUCCESS(200), EXCEPTION(500), INVALID_QUERY(400)
   */
  public static enum WsAssignAttributeBatchResultCode implements WsResultCode {

    /** found the attributeAssignments (lite status code 200) (success: T) */
    SUCCESS(200),

    /** something bad happened (lite status code 500) (success: F) */
    EXCEPTION(500),

    /** in tx, but another item had problem (lite status code 500) (success: F) */
    TRANSACTION_ROLLED_BACK(500),

    /** invalid query (e.g. if everything blank) (lite status code 400) (success: F) */
    INVALID_QUERY(400),
    
    /** not allowed to the privileges on the inputs.  Note if broad search, then the results wont
     * contain items not allowed.  If a specific search e.g. on a group, then if you cant read the
     * group then you cant read the privs
     */
    INSUFFICIENT_PRIVILEGES(403);
    
    /**
     * 
     * @param wsAssignAttributesResultsCode
     * @return the code
     */
    public static WsAssignAttributeBatchResultCode convertFromWsAssignAttributesResultCode(
        WsAssignAttributesResultsCode wsAssignAttributesResultsCode) {
      if (wsAssignAttributesResultsCode == null) {
        return null;
      }
      return wsAssignAttributesResultsCode.convertToBatchResult();
    }
    
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
    private WsAssignAttributeBatchResultCode(int theHttpStatusCode) {
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


  /** set of results of this attribute assign value */
  private WsAttributeAssignValueResult[] wsAttributeAssignValueResults;
  
  /**
   * set of results of this attribute assign value
   * @return the array of result object
   */
  public WsAttributeAssignValueResult[] getWsAttributeAssignValueResults() {
    return this.wsAttributeAssignValueResults;
  }

  /**
   * set of results of this attribute assign value
   * @param wsAttributeAssignValueResults1
   */
  public void setWsAttributeAssignValueResults(
      WsAttributeAssignValueResult[] wsAttributeAssignValueResults1) {
    this.wsAttributeAssignValueResults = wsAttributeAssignValueResults1;
  }
  
  /** if this assignment was changed, T|F */
  private String changed;

  /** if the values were changed, T|F */
  private String valuesChanged;

  /** if this assignment was deleted, T|F */
  private String deleted;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /** assignment(s) involved */
  private WsAttributeAssign[] wsAttributeAssigns;

  /**
   * if the values were changed, T|F
   * @return if the values were changed, T|F
   */
  public String getValuesChanged() {
    return this.valuesChanged;
  }

  /**
   * if the values were changed, T|F
   * @param valuesChanged1
   */
  public void setValuesChanged(String valuesChanged1) {
    this.valuesChanged = valuesChanged1;
  }

  /**
   * if this assignment was changed, T|F
   * @return if changed
   */
  public String getChanged() {
    return this.changed;
  }

  /**
   * if this assignment was changed, T|F
   * @param changed1
   */
  public void setChanged(String changed1) {
    this.changed = changed1;
  }

  /**
   * sort by the underlying attribute assign
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(WsAssignAttributeBatchResult o2) {
    if (this == o2) {
      return 0;
    }
    //lets by null safe here
    if (o2 == null) {
      return 1;
    }
    int thisAttributeAssignsLength = GrouperUtil.length(this.wsAttributeAssigns);
    int otherAttributeAssignsLength = GrouperUtil.length(o2.wsAttributeAssigns);
    if (thisAttributeAssignsLength != otherAttributeAssignsLength) {
      return new Integer(thisAttributeAssignsLength).compareTo(new Integer(otherAttributeAssignsLength));
    }
    for (int i=0; i<thisAttributeAssignsLength; i++) {
      WsAttributeAssign wsAttributeAssign = this.wsAttributeAssigns[i];
      WsAttributeAssign otherAttributeAssign = o2.wsAttributeAssigns[i];
      
      int result = wsAttributeAssign.compareTo(otherAttributeAssign);
      if (result != 0) {
        return result;
      }
    }
    return 0;
  }

  /**
   * if this assignment was deleted, T|F
   * @return if this assignment was deleted, T|F
   */
  public String getDeleted() {
    return this.deleted;
  }

  /**
   * if this assignment was deleted, T|F
   * @param deleted1
   */
  public void setDeleted(String deleted1) {
    this.deleted = deleted1;
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * convert string to result code
   * @return the result code
   */
  public WsAssignAttributeBatchResultCode resultCode() {
    return WsAssignAttributeBatchResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }


  /**
   * assignment involved
   * @return assignment involved
   */
  public WsAttributeAssign[] getWsAttributeAssigns() {
    return this.wsAttributeAssigns;
  }


  /**
   * assignment involved
   * @param wsAttributeAssigns1
   */
  public void setWsAttributeAssigns(WsAttributeAssign[] wsAttributeAssigns1) {
    this.wsAttributeAssigns = wsAttributeAssigns1;
  }
  
  
  
}
