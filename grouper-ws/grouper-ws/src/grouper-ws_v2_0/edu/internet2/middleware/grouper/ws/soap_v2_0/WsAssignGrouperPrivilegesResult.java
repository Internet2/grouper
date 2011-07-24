/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignGrouperPrivilegesLiteResult.WsAssignGrouperPrivilegesLiteResultCode;

/**
 * Result of assigning or removing a privilege
 * 
 * @author mchyzer
 */
public class WsAssignGrouperPrivilegesResult  {

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
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
}
