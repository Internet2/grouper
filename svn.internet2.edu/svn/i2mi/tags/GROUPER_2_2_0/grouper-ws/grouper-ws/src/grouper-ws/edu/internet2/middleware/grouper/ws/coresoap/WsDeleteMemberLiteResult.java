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
 * Result of one subject being deleted from a group.  The number of
 * subjects will equal the number of subjects sent in to the method
 * 
 * @author mchyzer
 */
public class WsDeleteMemberLiteResult implements WsResponseBean, ResultMetadataHolder {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(WsDeleteMemberLiteResult.class);

  /**
   * result code of a request
   */
  public static enum WsDeleteMemberLiteResultCode implements WsResultCode {

    /** cant find group (rest http status code 404) (success: F) */
    GROUP_NOT_FOUND(404) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

    },

    /** invalid request (rest http status code 400) (success: F) */
    INVALID_QUERY(400) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

    },

    /** successful addition (rest http status code 200) (success: T) */
    SUCCESS(200) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return true;
      }

    },

    /** successful addition (rest http status code 200) (success: T) */
    SUCCESS_BUT_HAS_EFFECTIVE(200) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return true;
      }

    },

    /** successful addition (rest http status code 200) (success: T) */
    SUCCESS_WASNT_IMMEDIATE(200) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return true;
      }

    },

    /** successful addition (rest http status code 200) (success: T) */
    SUCCESS_WASNT_IMMEDIATE_BUT_HAS_EFFECTIVE(200) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return true;
      }

    },

    /** the subject was not found (rest http status code 200) (success: T) */
    SUBJECT_NOT_FOUND(200) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

    },

    /** problem with deletion (rest http status code 500) (success: F) */
    EXCEPTION(500) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

    },

    /** user not allowed (rest http status code 403) (success: F) */
    INSUFFICIENT_PRIVILEGES(403) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

    },

    /** subject duplicate found (rest http status code 409) (success: F) */
    SUBJECT_DUPLICATE(409) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

    }, 

    /** problem deleting existing members (lite http status code 500) (success: F) */
    PROBLEM_DELETING_MEMBERS(500) {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

    };

    /**
     * if this is a successful result
     * @return true if success
     */
    public abstract boolean isSuccess();
    
    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsDeleteMemberLiteResultCode(int statusCode) {
      this.httpStatusCode = statusCode;
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;
    
    /**
     * @see edu.internet2.middleware.grouper.ws.WsResultCode#getHttpStatusCode()
     */
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }

    
    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

  }

  /**
   * constructor
   */
  public WsDeleteMemberLiteResult() {
    //empty
  }
  
  /**
   * construct from results of other
   * @param wsDeleteMemberResults
   */
  public WsDeleteMemberLiteResult(WsDeleteMemberResults wsDeleteMemberResults) {

    this.getResultMetadata().copyFields(wsDeleteMemberResults.getResultMetadata());
    this.setSubjectAttributeNames(wsDeleteMemberResults.getSubjectAttributeNames());
    this.setWsGroup(wsDeleteMemberResults.getWsGroup());

    WsDeleteMemberResult wsDeleteMemberResult = GrouperServiceUtils
        .firstInArrayOfOne(wsDeleteMemberResults.getResults());
    if (wsDeleteMemberResult != null) {
      this.getResultMetadata().copyFields(wsDeleteMemberResult.getResultMetadata());
      this.getResultMetadata().assignResultCode(
          wsDeleteMemberResult.resultCode().convertToLiteCode());
      this.setWsSubject(wsDeleteMemberResult.getWsSubject());
    }
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * subject that was added 
   */
  private WsSubject wsSubject;

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * group assigned to
   */
  private WsGroup wsGroup;

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * @return the wsSubject
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  /**
   * @param wsSubject1 the wsSubject to set
   */
  public void setWsSubject(WsSubject wsSubject1) {
    this.wsSubject = wsSubject1;
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
   * group assigned to
   * @return the wsGroupLookup
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * group assigned to
   * @param theWsGroupLookupAssigned the wsGroupLookup to set
   */
  public void setWsGroup(WsGroup theWsGroupLookupAssigned) {
    this.wsGroup = theWsGroupLookupAssigned;
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
   * assign the code from the enum
   * @param addMemberLiteResultCode1
   */
  public void assignResultCode(WsDeleteMemberLiteResultCode addMemberLiteResultCode1) {
    this.getResultMetadata().assignResultCode(addMemberLiteResultCode1);
  }

  /**
   * prcess an exception, log, etc
   * @param wsAddMemberLiteResultCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsDeleteMemberLiteResultCode wsAddMemberLiteResultCodeOverride, 
      String theError, Exception e) {
  
    if (e instanceof WsInvalidQueryException) {
      wsAddMemberLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsAddMemberLiteResultCodeOverride, WsDeleteMemberLiteResultCode.INVALID_QUERY);
      if (e.getCause() instanceof GroupNotFoundException) {
        wsAddMemberLiteResultCodeOverride = WsDeleteMemberLiteResultCode.GROUP_NOT_FOUND;
      }
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsAddMemberLiteResultCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);
  
    } else {
      wsAddMemberLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsAddMemberLiteResultCodeOverride, WsDeleteMemberLiteResultCode.EXCEPTION);
      LOG.error(theError, e);
  
      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsAddMemberLiteResultCodeOverride);
  
    }
  }

}
