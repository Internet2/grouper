/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.ws.coresoap.WsDeleteMemberLiteResult.WsDeleteMemberLiteResultCode;

/**
 * Result of one subject being deleted from a group.  The number of
 * subjects will equal the number of subjects sent in to the method
 * 
 * @author mchyzer
 */
public class WsDeleteMemberResult   {

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
