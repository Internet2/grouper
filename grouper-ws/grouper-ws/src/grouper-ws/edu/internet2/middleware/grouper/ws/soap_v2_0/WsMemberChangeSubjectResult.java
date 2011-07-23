/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsMemberChangeSubjectLiteResult.WsMemberChangeSubjectLiteResultCode;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsSubjectLookup.MemberFindResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsSubjectLookup.SubjectFindResult;

/**
 * Result of one member changing its subject
 * 
 * @author mchyzer
 */
public class WsMemberChangeSubjectResult implements ResultMetadataHolder {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsMemberChangeSubjectResult.class);

  /** subject that was switched to */
  private WsSubject wsSubjectNew;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * subject that was switched from
   */
  private WsSubject wsSubjectOld;

  /**
   * subject that was added
   * @return the subjectId
   */
  public WsSubject getWsSubjectNew() {
    return this.wsSubjectNew;
  }

  /**
   * subject that was added
   * @param wsSubject1 the wsSubject1 to set
   */
  public void setWsSubjectNew(WsSubject wsSubject1) {
    this.wsSubjectNew = wsSubject1;
  }

  /**
   * result code of a request
   */
  public static enum WsMemberChangeSubjectResultCode {

    /** invalid request (success: F) */
    INVALID_QUERY {

      /** 
       * if there is one result, convert to the results code
       * @return WsMemberChangeSubjectResultsCode
       */
      @Override
      public WsMemberChangeSubjectLiteResultCode convertToLiteCode() {
        return WsMemberChangeSubjectLiteResultCode.INVALID_QUERY;
      }

    },

    /** successful addition (success: T) */
    SUCCESS {

      /** 
       * if there is one result, convert to the results code
       * @return WsMemberChangeSubjectResultsCode
       */
      @Override
      public WsMemberChangeSubjectLiteResultCode convertToLiteCode() {
        return WsMemberChangeSubjectLiteResultCode.SUCCESS;
      }

    },

    /** the subject was not found (success: F) */SUBJECT_NOT_FOUND{
    
          /** 
           * if there is one result, convert to the results code
           * @return WsMemberChangeSubjectResultsCode
           */
          @Override
          public WsMemberChangeSubjectLiteResultCode convertToLiteCode() {
            return WsMemberChangeSubjectLiteResultCode.SUBJECT_NOT_FOUND;
          }
    
        }, 
        
   /** the member to change from was not found (success: F) */
    MEMBER_NOT_FOUND {

      /** 
       * if there is one result, convert to the results code
       * @return WsMemberChangeSubjectResultsCode
       */
      @Override
      public WsMemberChangeSubjectLiteResultCode convertToLiteCode() {
        return WsMemberChangeSubjectLiteResultCode.MEMBER_NOT_FOUND;
      }

    },

    /** problem with change (success: F) */
    EXCEPTION {

      /** 
       * if there is one result, convert to the results code
       * @return WsMemberChangeSubjectResultsCode
       */
      @Override
      public WsMemberChangeSubjectLiteResultCode convertToLiteCode() {
        return WsMemberChangeSubjectLiteResultCode.EXCEPTION;
      }

    },

    /** user not allowed (success: F) */
    INSUFFICIENT_PRIVILEGES {

      /** 
       * if there is one result, convert to the results code
       * @return WsMemberChangeSubjectResultsCode
       */
      @Override
      public WsMemberChangeSubjectLiteResultCode convertToLiteCode() {
        return WsMemberChangeSubjectLiteResultCode.INSUFFICIENT_PRIVILEGES;
      }

    },

    /** if one item failed in the transaction, then roll back (success: F) */
    TRANSACTION_ROLLED_BACK {

      /** 
       * if there is one result, convert to the results code
       * @return WsMemberChangeSubjectResultsCode
       */
      @Override
      public WsMemberChangeSubjectLiteResultCode convertToLiteCode() {
        //shouldnt happen (one result with rollback)
        return WsMemberChangeSubjectLiteResultCode.EXCEPTION;
      }

    },

    /** subject duplicate found (success: F) */
    SUBJECT_DUPLICATE {

      /** 
       * if there is one result, convert to the results code
       * @return WsMemberChangeSubjectResultsCode
       */
      @Override
      public WsMemberChangeSubjectLiteResultCode convertToLiteCode() {
        return WsMemberChangeSubjectLiteResultCode.SUBJECT_DUPLICATE;
      }

    };

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }

    /** 
     * if there is one result, convert to the results code
     * @return result code
     */
    public abstract WsMemberChangeSubjectLiteResultCode convertToLiteCode();
  }

  /**
   * convert string to result code
   * @return the result code
   */
  public WsMemberChangeSubjectResultCode resultCode() {
    return WsMemberChangeSubjectResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * assign the code from the enum
   * @param memberChangeSubjectResultCode
   */
  public void assignResultCode(WsMemberChangeSubjectResultCode memberChangeSubjectResultCode) {
    this.getResultMetadata().assignResultCode(
        memberChangeSubjectResultCode == null ? null : memberChangeSubjectResultCode.name());
    this.getResultMetadata().assignSuccess(memberChangeSubjectResultCode.isSuccess() ? "T" : "F");
  }

  /**
   * assign a resultcode of exception, and process/log the exception
   * @param e
   * @param wsMemberChangeSubject
   */
  public void assignResultCodeException(Exception e, WsMemberChangeSubject wsMemberChangeSubject) {
    this.assignResultCode(WsMemberChangeSubjectResultCode.EXCEPTION);
    this.getResultMetadata().setResultMessage(ExceptionUtils.getFullStackTrace(e));
    LOG.error(wsMemberChangeSubject + ", " + e, e);
  }

  /**
   * assign the code from the enum
   * @param wsSubjectLookup1
   * @param subjectAttributeNames
   */
  public void processSubjectNew(WsSubjectLookup wsSubjectLookup1,
      String[] subjectAttributeNames) {

    this.setWsSubjectNew(new WsSubject(wsSubjectLookup1));
    this.setWsSubjectNew(new WsSubject(wsSubjectLookup1.retrieveSubject("Subject"),
        subjectAttributeNames, wsSubjectLookup1));
    
    SubjectFindResult subjectFindResult = wsSubjectLookup1.retrieveSubjectFindResult();

    switch (subjectFindResult) {
      case INVALID_QUERY:
        this.assignResultCode(WsMemberChangeSubjectResultCode.INVALID_QUERY);
        break;
      case SOURCE_UNAVAILABLE:
        this.assignResultCode(WsMemberChangeSubjectResultCode.EXCEPTION);
        break;
      case SUBJECT_DUPLICATE:
        this.assignResultCode(WsMemberChangeSubjectResultCode.SUBJECT_DUPLICATE);
        break;
      case SUBJECT_NOT_FOUND:
        this.assignResultCode(WsMemberChangeSubjectResultCode.SUBJECT_NOT_FOUND);
        break;
      case SUCCESS:
        return;
    }

    this.getResultMetadata().setResultMessage(
        "Subject: " + wsSubjectLookup1 + " had problems: " + subjectFindResult);

  }

  /**
   * assign the code from the enum
   * @param wsSubjectLookup1
   * @param subjectAttributeNames
   * @param includeSubjectDetail 
   */
  public void processMemberOld(WsSubjectLookup wsSubjectLookup1, String[] subjectAttributeNames, boolean includeSubjectDetail) {

    Member oldMember = wsSubjectLookup1.retrieveMember();
    
    MemberFindResult memberFindResult = wsSubjectLookup1.retrieveMemberFindResult();

    switch (memberFindResult) {
      case INVALID_QUERY:
        this.assignResultCode(WsMemberChangeSubjectResultCode.INVALID_QUERY);
        break;
      case MEMBER_NOT_FOUND:
        this.assignResultCode(WsMemberChangeSubjectResultCode.MEMBER_NOT_FOUND);
        break;
      case SUCCESS:
        WsSubject oldWsSubject = new WsSubject(oldMember, subjectAttributeNames, wsSubjectLookup1, includeSubjectDetail);
        this.setWsSubjectOld(oldWsSubject);
        
        return;
    }

    this.getResultMetadata().setResultMessage(
        "Subject: " + wsSubjectLookup1 + " had problems: " + memberFindResult);

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
   * subject that was switched from
   * @return the subjectId
   */
  public WsSubject getWsSubjectOld() {
    return this.wsSubjectOld;
  }

  /**
   * subject that was switched from
   * @param wsSubject1 the wsSubject1 to set
   */
  public void setWsSubjectOld(WsSubject wsSubject1) {
    this.wsSubjectOld = wsSubject1;
  }

}
