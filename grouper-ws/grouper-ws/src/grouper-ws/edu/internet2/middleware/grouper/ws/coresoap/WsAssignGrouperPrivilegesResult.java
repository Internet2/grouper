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
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignGrouperPrivilegesLiteResult.WsAssignGrouperPrivilegesLiteResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup.SubjectFindResult;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * Result of assigning or removing a privilege
 * 
 * @author mchyzer
 */
public class WsAssignGrouperPrivilegesResult implements ResultMetadataHolder {

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * assign the code from the enum
   * @param wsSubjectLookup1
   * @param theSubjectAttributeNames
   */
  public void processSubject(WsSubjectLookup wsSubjectLookup1,
      String[] theSubjectAttributeNames) {

    SubjectFindResult subjectFindResult = wsSubjectLookup1.retrieveSubjectFindResult();

    switch (subjectFindResult) {
      case INVALID_QUERY:
        this.assignResultCode(WsAssignGrouperPrivilegesResultCode.INVALID_QUERY);
        break;
      case SOURCE_UNAVAILABLE:
        this.assignResultCode(WsAssignGrouperPrivilegesResultCode.EXCEPTION);
        break;
      case SUBJECT_DUPLICATE:
        this.assignResultCode(WsAssignGrouperPrivilegesResultCode.SUBJECT_DUPLICATE);
        break;
      case SUBJECT_NOT_FOUND:
        this.assignResultCode(WsAssignGrouperPrivilegesResultCode.SUBJECT_NOT_FOUND);
        break;
      case SUCCESS:
        return;
    }

    this.getResultMetadata().setResultMessage(
        "Subject: " + wsSubjectLookup1 + " had problems: " + subjectFindResult);

  }

  /**
   * empty
   */
  public WsAssignGrouperPrivilegesResult() {
    //empty
  }

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(WsAssignGrouperPrivilegesResult.class);


  /**
   * prcess an exception, log, etc
   * @param wsMemberChangeSubjectLiteResultCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsAssignGrouperPrivilegesResultCode wsMemberChangeSubjectLiteResultCodeOverride, 
      String theError, Exception e) {

    if (e instanceof SubjectNotUniqueException 
        || e instanceof SubjectNotFoundException || e instanceof WsInvalidQueryException) {
      wsMemberChangeSubjectLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsMemberChangeSubjectLiteResultCodeOverride, WsAssignGrouperPrivilegesResultCode.INVALID_QUERY);
      if (e instanceof SubjectNotFoundException || e.getCause() instanceof SubjectNotFoundException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsAssignGrouperPrivilegesResultCode.SUBJECT_NOT_FOUND;
      }
      if (e instanceof SubjectNotUniqueException || e.getCause() instanceof SubjectNotUniqueException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsAssignGrouperPrivilegesResultCode.SUBJECT_DUPLICATE;
      }
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsMemberChangeSubjectLiteResultCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsMemberChangeSubjectLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsMemberChangeSubjectLiteResultCodeOverride, WsAssignGrouperPrivilegesResultCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsMemberChangeSubjectLiteResultCodeOverride);

    }
  }

  /**
   * assign the code from the enum
   * @param wsAssignGrouperPrivilegesResultCode
   */
  public void assignResultCode(WsAssignGrouperPrivilegesResultCode wsAssignGrouperPrivilegesResultCode) {
    this.getResultMetadata().assignResultCode(wsAssignGrouperPrivilegesResultCode == null 
        ? null : wsAssignGrouperPrivilegesResultCode.name());
    String success = null;
    if (wsAssignGrouperPrivilegesResultCode != null) {
      success = wsAssignGrouperPrivilegesResultCode.isSuccess() ? "T" : "F";
    }
    this.getResultMetadata().setSuccess(success);
  }

  /**
    * metadata about the result
    */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * result code of a request
   */
  public static enum WsAssignGrouperPrivilegesResultCode {

    /** made the update to allow (rest http status code 200) (success: T) */
    SUCCESS_ALLOWED {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAssignGrouperPrivilegesLiteResultCode convertToLiteCode() {
        //shouldnt happen (one result with rollback)
        return WsAssignGrouperPrivilegesLiteResultCode.SUCCESS_ALLOWED;
      }

    },

    /** privilege allow already existed (rest http status code 200) (success: T) */
    SUCCESS_ALLOWED_ALREADY_EXISTED {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAssignGrouperPrivilegesLiteResultCode convertToLiteCode() {
        //shouldnt happen (one result with rollback)
        return WsAssignGrouperPrivilegesLiteResultCode.SUCCESS_ALLOWED_ALREADY_EXISTED;
      }

    },

    /** made the update to deny (rest http status code 200) (success: T) */
    SUCCESS_NOT_ALLOWED {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAssignGrouperPrivilegesLiteResultCode convertToLiteCode() {
        //shouldnt happen (one result with rollback)
        return WsAssignGrouperPrivilegesLiteResultCode.SUCCESS_NOT_ALLOWED;
      }

    },

    /** made the update to deny the immediate privilege, though the user still has an effective privilege, so is still allowed (rest http status code 200) (success: T) */
    SUCCESS_NOT_ALLOWED_EXISTS_EFFECTIVE {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAssignGrouperPrivilegesLiteResultCode convertToLiteCode() {
        //shouldnt happen (one result with rollback)
        return WsAssignGrouperPrivilegesLiteResultCode.SUCCESS_NOT_ALLOWED_EXISTS_EFFECTIVE;
      }

    },

    /** privilege deny already existed (rest http status code 200) (success: T) */
    SUCCESS_NOT_ALLOWED_DIDNT_EXIST {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAssignGrouperPrivilegesLiteResultCode convertToLiteCode() {
        //shouldnt happen (one result with rollback)
        return WsAssignGrouperPrivilegesLiteResultCode.SUCCESS_NOT_ALLOWED_DIDNT_EXIST;
      }

    },

    /** privilege deny already existed (rest http status code 200) (success: T) */
    SUCCESS_NOT_ALLOWED_DIDNT_EXIST_BUT_EXISTS_EFFECTIVE {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAssignGrouperPrivilegesLiteResultCode convertToLiteCode() {
        //shouldnt happen (one result with rollback)
        return WsAssignGrouperPrivilegesLiteResultCode.SUCCESS_NOT_ALLOWED_DIDNT_EXIST_BUT_EXISTS_EFFECTIVE;
      }

    },

    /** some exception occurred (rest http status code 500) (success: F) */
    EXCEPTION  {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAssignGrouperPrivilegesLiteResultCode convertToLiteCode() {
        //shouldnt happen (one result with rollback)
        return WsAssignGrouperPrivilegesLiteResultCode.EXCEPTION;
      }

    },

    /** if one request, and that is a duplicate (rest http status code 409) (success: F) */
    SUBJECT_DUPLICATE {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAssignGrouperPrivilegesLiteResultCode convertToLiteCode() {
        //shouldnt happen (one result with rollback)
        return WsAssignGrouperPrivilegesLiteResultCode.SUBJECT_DUPLICATE;
      }

    },

    /** if one request, and that is a subject not found (rest http status code 404) (success: F) */
    SUBJECT_NOT_FOUND {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAssignGrouperPrivilegesLiteResultCode convertToLiteCode() {
        //shouldnt happen (one result with rollback)
        return WsAssignGrouperPrivilegesLiteResultCode.SUBJECT_NOT_FOUND;
      }

    },

    /** if one item failed in the transaction, then roll back (success: F) */
    TRANSACTION_ROLLED_BACK {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAssignGrouperPrivilegesLiteResultCode convertToLiteCode() {
        //shouldnt happen (one result with rollback)
        return WsAssignGrouperPrivilegesLiteResultCode.EXCEPTION;
      }

    },

    
    /** invalid query (e.g. if everything blank) (rest http status code 400) (success: F) */
    INVALID_QUERY  {

      /** 
       * if there is one result, convert to the results code
       * @return WsAddMemberResultsCode
       */
      @Override
      public WsAssignGrouperPrivilegesLiteResultCode convertToLiteCode() {
        //shouldnt happen (one result with rollback)
        return WsAssignGrouperPrivilegesLiteResultCode.EXCEPTION;
      }

    };

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /** 
     * if there is one result, convert to the results code
     * @return result code
     */
    public abstract WsAssignGrouperPrivilegesLiteResultCode convertToLiteCode();

    /**
     * if this is a successful result
     * 
     * @return true if success
     */
    public boolean isSuccess() {
      return this.name().startsWith("SUCCESS");
    }

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

  
  /**
   * field 
   */
  private WsParam[] params;

  /**
   * whether this privilege is allowed T/F 
   */
  private String allowed;

  /**
   * privilege name, e.g. read, update, stem 
   */
  private String privilegeName;

  /**
   * privilege type, e.g. naming, or access 
   */
  private String privilegeType;

  /**
   * subject to switch to
   */
  private WsSubject wsSubject;


  /**
   * @return the params
   */
  public WsParam[] getParams() {
    return this.params;
  }

  /**
   * @param params1 the params to set
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

  /**
   * whether this privilege is allowed T/F
   * @return if allowed
   */
  public String getAllowed() {
    return this.allowed;
  }

  /**
   * privilege type, e.g. naming, or access
   * @return the name
   */
  public String getPrivilegeName() {
    return this.privilegeName;
  }

  /**
   * privilege type, e.g. naming, or access
   * @return the type
   */
  public String getPrivilegeType() {
    return this.privilegeType;
  }

  /**
   * subject that was changed to
   * @return the subjectId
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  /**
   * whether this privilege is allowed T/F
   * @param allowed1
   */
  public void setAllowed(String allowed1) {
    this.allowed = allowed1;
  }

  /**
   * privilege type, e.g. naming, or access
   * @param privilegeName1
   */
  public void setPrivilegeName(String privilegeName1) {
    this.privilegeName = privilegeName1;
  }

  /**
   * privilege type, e.g. naming, or access
   * @param privilegeType1
   */
  public void setPrivilegeType(String privilegeType1) {
    this.privilegeType = privilegeType1;
  }

  /**
   * subject that was changed to
   * @param wsSubject1 the wsSubject1 to set
   */
  public void setWsSubject(WsSubject wsSubject1) {
    this.wsSubject = wsSubject1;
  }

  /**
   * convert string to result code
   * @return the result code
   */
  public WsAssignGrouperPrivilegesResultCode resultCode() {
    return WsAssignGrouperPrivilegesResultCode.valueOf(this.getResultMetadata().getResultCode());
  }
}
