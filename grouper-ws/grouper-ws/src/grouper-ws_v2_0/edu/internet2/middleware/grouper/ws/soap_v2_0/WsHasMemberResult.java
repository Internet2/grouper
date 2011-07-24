/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.coresoap.WsHasMemberLiteResult.WsHasMemberLiteResultCode;

/**
 * Result of seeing if one subject is a member of a group.  The number of
 * results will equal the number of subjects sent in to the method
 * 
 * @author mchyzer
 */
public class WsHasMemberResult {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsHasMemberResult.class);

  /** sujbect info for hasMember */
  private WsSubject wsSubject;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * @return the wsSubject
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  /**
   * @param wsSubjectResult1 the wsSubject to set
   */
  public void setWsSubject(WsSubject wsSubjectResult1) {
    this.wsSubject = wsSubjectResult1;
  }

  /** empty constructor */
  public WsHasMemberResult() {
    //nothing
  }

  /**
   * result code of a request
   */
  public static enum WsHasMemberResultCode {

    /** found multiple results */
    SUBJECT_DUPLICATE {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsHasMemberResultLiteCode
       */
      @Override
      public WsHasMemberLiteResultCode convertToLiteCode() {
        return WsHasMemberLiteResultCode.SUBJECT_DUPLICATE;
      }

    },

    /** cant find the subject */
    SUBJECT_NOT_FOUND {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsHasMemberResultLiteCode
       */
      @Override
      public WsHasMemberLiteResultCode convertToLiteCode() {
        return WsHasMemberLiteResultCode.SUBJECT_NOT_FOUND;
      }

    },

    /** the subject is a member  (success = T) */
    IS_MEMBER {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsHasMemberResultLiteCode
       */
      @Override
      public WsHasMemberLiteResultCode convertToLiteCode() {
        return WsHasMemberLiteResultCode.IS_MEMBER;
      }

    },

    /** the subject was found and is not a member (success = T) */
    IS_NOT_MEMBER {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsHasMemberResultLiteCode
       */
      @Override
      public WsHasMemberLiteResultCode convertToLiteCode() {
        return WsHasMemberLiteResultCode.IS_NOT_MEMBER;
      }

    },

    /** problem with query */
    EXCEPTION {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsHasMemberResultLiteCode
       */
      @Override
      public WsHasMemberLiteResultCode convertToLiteCode() {
        return WsHasMemberLiteResultCode.EXCEPTION;
      }

    },

    /** invalid query (e.g. if everything blank) */
    INVALID_QUERY {
      
      /** 
       * if there is one result, convert to the results code
       * @return WsHasMemberResultLiteCode
       */
      @Override
      public WsHasMemberLiteResultCode convertToLiteCode() {
        return WsHasMemberLiteResultCode.INVALID_QUERY;
      }

    };

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this.equals(IS_MEMBER) || this.equals(IS_NOT_MEMBER) 
      || (!GrouperWsConfig.getPropertyBoolean("ws.hasMember.subjectNotFound.returnsError", false) 
          && this.equals(SUBJECT_NOT_FOUND));
    }
    
    /** 
     * if there is one result, convert to the results code
     * @return result code
     */
    public abstract WsHasMemberLiteResultCode convertToLiteCode();

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
