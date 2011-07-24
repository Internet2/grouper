/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.ws.coresoap.WsMemberChangeSubjectLiteResult.WsMemberChangeSubjectLiteResultCode;

/**
 * Result of one member changing its subject
 * 
 * @author mchyzer
 */
public class WsMemberChangeSubjectResult  {

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
