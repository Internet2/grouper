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

import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.soap_v2_3.WsDeleteMemberLiteResult.WsDeleteMemberLiteResultCode;
import edu.internet2.middleware.grouper.ws.soap_v2_3.WsSubjectLookup.SubjectFindResult;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * Result of one subject being deleted from a group.  The number of
 * subjects will equal the number of subjects sent in to the method
 * 
 * @author mchyzer
 */
public class WsDeleteMemberResult  {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsDeleteMemberResult.class);

  /** subject that was added */
  private WsSubject wsSubject;

  /**
   * result code of a request
   */
  public static enum WsDeleteMemberResultCode {

    /** cant find group */
    GROUP_NOT_FOUND {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsDeleteMemberLiteResultCode
       */
      @Override
      public WsDeleteMemberLiteResultCode convertToLiteCode() {
        return WsDeleteMemberLiteResultCode.GROUP_NOT_FOUND;
      }
    },

    /** invalid request */
    INVALID_QUERY {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsDeleteMemberLiteResultCode
       */
      @Override
      public WsDeleteMemberLiteResultCode convertToLiteCode() {
        return WsDeleteMemberLiteResultCode.INVALID_QUERY;
      }
    },

    /** successful addition */
    SUCCESS {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return true;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsDeleteMemberLiteResultCode
       */
      @Override
      public WsDeleteMemberLiteResultCode convertToLiteCode() {
        return WsDeleteMemberLiteResultCode.SUCCESS;
      }
    },

    /** successful addition */
    SUCCESS_BUT_HAS_EFFECTIVE {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return true;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsDeleteMemberLiteResultCode
       */
      @Override
      public WsDeleteMemberLiteResultCode convertToLiteCode() {
        return WsDeleteMemberLiteResultCode.SUCCESS_BUT_HAS_EFFECTIVE;
      }
    },

    /** successful addition */
    SUCCESS_WASNT_IMMEDIATE {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return true;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsDeleteMemberLiteResultCode
       */
      @Override
      public WsDeleteMemberLiteResultCode convertToLiteCode() {
        return WsDeleteMemberLiteResultCode.SUCCESS_WASNT_IMMEDIATE;
      }
    },

    /** successful addition */
    SUCCESS_WASNT_IMMEDIATE_BUT_HAS_EFFECTIVE {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return true;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsDeleteMemberLiteResultCode
       */
      @Override
      public WsDeleteMemberLiteResultCode convertToLiteCode() {
        return WsDeleteMemberLiteResultCode.SUCCESS_WASNT_IMMEDIATE_BUT_HAS_EFFECTIVE;
      }
    },

    /** the subject was not found */
    SUBJECT_NOT_FOUND {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsDeleteMemberLiteResultCode
       */
      @Override
      public WsDeleteMemberLiteResultCode convertToLiteCode() {
        return WsDeleteMemberLiteResultCode.SUBJECT_NOT_FOUND;
      }
    },

    /** problem with deletion */
    EXCEPTION {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsDeleteMemberLiteResultCode
       */
      @Override
      public WsDeleteMemberLiteResultCode convertToLiteCode() {
        return WsDeleteMemberLiteResultCode.EXCEPTION;
      }

    },

    /** user not allowed */
    INSUFFICIENT_PRIVILEGES {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsDeleteMemberLiteResultCode
       */
      @Override
      public WsDeleteMemberLiteResultCode convertToLiteCode() {
        return WsDeleteMemberLiteResultCode.INSUFFICIENT_PRIVILEGES;
      }

    },

    /** if one item failed in the transaction, then roll back */
    TRANSACTION_ROLLED_BACK {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsDeleteMemberLiteResultCode
       */
      @Override
      public WsDeleteMemberLiteResultCode convertToLiteCode() {
        //this should never happen, rolled back on one record
        return WsDeleteMemberLiteResultCode.EXCEPTION;
      }
    },

    /** subject duplicate found */
    SUBJECT_DUPLICATE {

      /**
       * if this is a successful result
       * @return true if success
       */
      @Override
      public boolean isSuccess() {
        return false;
      }

      /** 
       * if there is one result, convert to the results code
       * @return WsDeleteMemberLiteResultCode
       */
      @Override
      public WsDeleteMemberLiteResultCode convertToLiteCode() {
        return WsDeleteMemberLiteResultCode.SUBJECT_DUPLICATE;
      }

    };

    /**
     * if this is a successful result
     * @return true if success
     */
    public abstract boolean isSuccess();

    /** 
     * if there is one result, convert to the results code
     * @return result code
     */
    public abstract WsDeleteMemberLiteResultCode convertToLiteCode();

  }

  /**
   * convert string to result code
   * @return the result code
   */
  public WsDeleteMemberResultCode resultCode() {
    return WsDeleteMemberResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * assign the code from the enum
   * @param deleteMemberResultCode
   */
  public void assignResultCode(WsDeleteMemberResultCode deleteMemberResultCode) {
    this.getResultMetadata().assignResultCode(
        deleteMemberResultCode == null ? null : deleteMemberResultCode.name());
    this.getResultMetadata().assignSuccess(
        GrouperServiceUtils.booleanToStringOneChar(deleteMemberResultCode.isSuccess()));
    if (LOG.isDebugEnabled()) {
      LOG.debug("Set result code to : " + this.getResultMetadata().getResultCode() + ", " + this.getResultMetadata().getSuccess());
    }
  }

  /**
   * assign a resultcode of exception, and process/log the exception
   * @param e
   * @param wsSubjectLookup
   */
  public void assignResultCodeException(Exception e, WsSubjectLookup wsSubjectLookup) {
    this.assignResultCode(WsDeleteMemberResultCode.EXCEPTION);
    this.getResultMetadata().setResultMessage(ExceptionUtils.getFullStackTrace(e));
    LOG.error(wsSubjectLookup + ", " + e, e);
  }

  /**
   * assign a success based on four situations based on hasImmediate and hasEffective
   * @param hasImmediate
   * @param hasEffective
   * @param canRead if the caller cant read then dont tell them the membership status
   */
  public void assignResultCodeSuccess(boolean hasImmediate, boolean hasEffective, boolean canRead) {
    
    if (!canRead) {
      this.assignResultCode(WsDeleteMemberResultCode.SUCCESS);
      return;
    }
    
    //set success based on scenario
    if (hasEffective) {
      this
          .assignResultCode(hasImmediate ? WsDeleteMemberResultCode.SUCCESS_BUT_HAS_EFFECTIVE
              : WsDeleteMemberResultCode.SUCCESS_WASNT_IMMEDIATE_BUT_HAS_EFFECTIVE);
    } else {
      this.assignResultCode(hasImmediate ? WsDeleteMemberResultCode.SUCCESS
          : WsDeleteMemberResultCode.SUCCESS_WASNT_IMMEDIATE);
    }
  }

  /**
   * assign the code from the enum
   * @param wsSubjectLookup1
   * @param subjectAttributeNames
   */
  public void processSubject(WsSubjectLookup wsSubjectLookup1,
      String[] subjectAttributeNames) {
    processSubject(wsSubjectLookup1, subjectAttributeNames, true);
  }

  /**
   * assign the code from the enum
   * @param wsSubjectLookup1
   * @param subjectAttributeNames
   * @param exceptionIfNotFound 
   */
  public void processSubject(WsSubjectLookup wsSubjectLookup1,
      String[] subjectAttributeNames, boolean exceptionIfNotFound) {

    this.setWsSubject(new WsSubject(wsSubjectLookup1));
    try {
      this.setWsSubject(new WsSubject(wsSubjectLookup1.retrieveSubject("Subject"),
          subjectAttributeNames, wsSubjectLookup1));
    } catch (WsInvalidQueryException wsiqe) {
      if (exceptionIfNotFound) {
        throw wsiqe;
      }
    }
    SubjectFindResult subjectFindResult = wsSubjectLookup1.retrieveSubjectFindResult();

    switch (subjectFindResult) {
      case INVALID_QUERY:
        this.assignResultCode(WsDeleteMemberResultCode.INVALID_QUERY);
        break;
      case SOURCE_UNAVAILABLE:
        this.assignResultCode(WsDeleteMemberResultCode.EXCEPTION);
        break;
      case SUBJECT_DUPLICATE:
        this.assignResultCode(WsDeleteMemberResultCode.SUBJECT_DUPLICATE);
        break;
      case SUBJECT_NOT_FOUND:
        this.assignResultCode(WsDeleteMemberResultCode.SUBJECT_NOT_FOUND);
        break;
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
   * @return the wsSubject
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  
  /**
   * @param wsSubject1 the wsSubject to set
   */
  public void setWsSubject(WsSubject wsSubject1) {
    this.wsSubject = wsSubject1;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

}
