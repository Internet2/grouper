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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.ws.soap_v2_3.WsAddMemberLiteResult.WsAddMemberLiteResultCode;
import edu.internet2.middleware.grouper.ws.soap_v2_3.WsSubjectLookup.SubjectFindResult;

/**
 * Result of one subject being added to a group.  The number of
 * subjects will equal the number of subjects sent in to the method
 * 
 * @author mchyzer
 */
public class WsAddMemberResult {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsAddMemberResult.class);

  /** subject that was added */
  private WsSubject wsSubject;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * subject that was added
   * @return the subjectId
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  /**
   * subject that was added
   * @param wsSubject1 the wsSubject1 to set
   */
  public void setWsSubject(WsSubject wsSubject1) {
    this.wsSubject = wsSubject1;
  }

  /**
   * result code of a request
   */
  public static enum WsAddMemberResultCode {

    /** invalid request (success: F) */
    INVALID_QUERY {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAddMemberLiteResultCode convertToLiteCode() {
        return WsAddMemberLiteResultCode.INVALID_QUERY;
      }

    },

    /** cant find group (success: F) */
    GROUP_NOT_FOUND {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAddMemberLiteResultCode convertToLiteCode() {
        return WsAddMemberLiteResultCode.GROUP_NOT_FOUND;
      }

    },

    /** successful addition (success: T) */
    SUCCESS {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAddMemberLiteResultCode convertToLiteCode() {
        return WsAddMemberLiteResultCode.SUCCESS;
      }

    },

    /** successful addition (success: T) */
    SUCCESS_CREATED {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAddMemberLiteResultCode convertToLiteCode() {
        return WsAddMemberLiteResultCode.SUCCESS_CREATED;
      }

    },

    /** successful addition if already a member (success: T) */
    SUCCESS_ALREADY_EXISTED {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAddMemberLiteResultCode convertToLiteCode() {
        return WsAddMemberLiteResultCode.SUCCESS_ALREADY_EXISTED;
      }

    },

    /** the subject was not found (success: F) */
    SUBJECT_NOT_FOUND {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAddMemberLiteResultCode convertToLiteCode() {
        return WsAddMemberLiteResultCode.SUBJECT_NOT_FOUND;
      }

    },

    /** problem with addition (success: F) */
    EXCEPTION {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAddMemberLiteResultCode convertToLiteCode() {
        return WsAddMemberLiteResultCode.EXCEPTION;
      }

    },

    /** user not allowed (success: F) */
    INSUFFICIENT_PRIVILEGES {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAddMemberLiteResultCode convertToLiteCode() {
        return WsAddMemberLiteResultCode.INSUFFICIENT_PRIVILEGES;
      }

    },

    /** if one item failed in the transaction, then roll back (success: F) */
    TRANSACTION_ROLLED_BACK {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAddMemberLiteResultCode convertToLiteCode() {
        //shouldnt happen (one result with rollback)
        return WsAddMemberLiteResultCode.EXCEPTION;
      }

    },

    /** subject duplicate found (success: F) */
    SUBJECT_DUPLICATE {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAddMemberLiteResultCode convertToLiteCode() {
        return WsAddMemberLiteResultCode.SUBJECT_DUPLICATE;
      }

    };

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this.name().startsWith("SUCCESS");
    }

    /** 
     * if there is one result, convert to the results code
     * @return result code
     */
    public abstract WsAddMemberLiteResultCode convertToLiteCode();
  }

  /**
   * convert string to result code
   * @return the result code
   */
  public WsAddMemberResultCode resultCode() {
    return WsAddMemberResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * assign the code from the enum
   * @param addMemberResultCode
   */
  public void assignResultCode(WsAddMemberResultCode addMemberResultCode) {
    this.getResultMetadata().assignResultCode(
        addMemberResultCode == null ? null : addMemberResultCode.name());
    this.getResultMetadata().assignSuccess(addMemberResultCode.isSuccess() ? "T" : "F");
  }

  /**
   * assign a resultcode of exception, and process/log the exception
   * @param e
   * @param wsSubjectLookup1
   */
  public void assignResultCodeException(Exception e, WsSubjectLookup wsSubjectLookup1) {
    this.assignResultCode(WsAddMemberResultCode.EXCEPTION);
    this.getResultMetadata().setResultMessage(ExceptionUtils.getFullStackTrace(e));
    LOG.error(wsSubjectLookup1 + ", " + e, e);
  }

  /**
   * assign the code from the enum
   * @param wsSubjectLookup1
   * @param subjectAttributeNames
   */
  public void processSubject(WsSubjectLookup wsSubjectLookup1,
      String[] subjectAttributeNames) {

    this.setWsSubject(new WsSubject(wsSubjectLookup1));
    this.setWsSubject(new WsSubject(wsSubjectLookup1.retrieveSubject("Subject"),
        subjectAttributeNames, wsSubjectLookup1));
    
    SubjectFindResult subjectFindResult = wsSubjectLookup1.retrieveSubjectFindResult();

    switch (subjectFindResult) {
      case INVALID_QUERY:
        this.assignResultCode(WsAddMemberResultCode.INVALID_QUERY);
        break;
      case SOURCE_UNAVAILABLE:
        this.assignResultCode(WsAddMemberResultCode.EXCEPTION);
        break;
      case SUBJECT_DUPLICATE:
        this.assignResultCode(WsAddMemberResultCode.SUBJECT_DUPLICATE);
        break;
      case SUBJECT_NOT_FOUND:
        this.assignResultCode(WsAddMemberResultCode.SUBJECT_NOT_FOUND);
        break;
      case SUCCESS_CREATED:
      case SUCCESS:
        return;
    }

    this.getResultMetadata().setResultMessage(
        "Subject: " + wsSubjectLookup1 + " had problems: " + subjectFindResult);

  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }
  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

}
