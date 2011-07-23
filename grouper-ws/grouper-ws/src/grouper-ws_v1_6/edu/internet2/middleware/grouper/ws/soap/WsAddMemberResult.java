/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAddMemberLiteResult.WsAddMemberLiteResultCode;

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
