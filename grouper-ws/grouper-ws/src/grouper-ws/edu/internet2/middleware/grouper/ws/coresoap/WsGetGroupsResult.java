package edu.internet2.middleware.grouper.ws.coresoap;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetGroupsLiteResult.WsGetGroupsLiteResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * <pre>
 * results for the get groups call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * SUBJECT_NOT_FOUND: cant find the subject
 * SUBJECT_DUPLICATE: found multiple groups
 * EXCEPTION
 * </pre>
 * @author mchyzer
 */
public class WsGetGroupsResult implements ResultMetadataHolder {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsGetGroupsResult.class);

  /**
   * result code of a request
   */
  public static enum WsGetGroupsResultCode implements WsResultCode {

    /** found the subject (lite http status code 200) (success: T) */
    SUCCESS(200) {
      /** 
       * if there is one result, convert to the results code
       * @return result code
       */
      @Override
      public WsGetGroupsLiteResultCode convertToLiteCode() {
        return WsGetGroupsLiteResultCode.SUCCESS;
      }
    },

    /** problem (lite http status code 500) (success: F) */
    EXCEPTION(500) {
      /** 
       * if there is one result, convert to the results code
       * @return result code
       */
      @Override
      public WsGetGroupsLiteResultCode convertToLiteCode() {
        return WsGetGroupsLiteResultCode.EXCEPTION;
      }
    },

    /** problem (lite http status code 400) (success: F) */
    INVALID_QUERY(400) {
      /** 
       * if there is one result, convert to the results code
       * @return result code
       */
      @Override
      public WsGetGroupsLiteResultCode convertToLiteCode() {
        return WsGetGroupsLiteResultCode.INVALID_QUERY;
      }
    },

    /** couldnt find the member to query (lite http status code 404) (success: F) */
    MEMBER_NOT_FOUND(404) {
      /** 
       * if there is one result, convert to the results code
       * @return result code
       */
      @Override
      public WsGetGroupsLiteResultCode convertToLiteCode() {
        return WsGetGroupsLiteResultCode.MEMBER_NOT_FOUND;
      }
    },

    /** couldnt find the subject to query (lite http status code 404) (success: F) */
    SUBJECT_NOT_FOUND(404) {
      /** 
       * if there is one result, convert to the results code
       * @return result code
       */
      @Override
      public WsGetGroupsLiteResultCode convertToLiteCode() {
        return WsGetGroupsLiteResultCode.SUBJECT_NOT_FOUND;
      }
    },

    /** problem querying the subject, was duplicate (lite http status code 409) (success: F) */
    SUBJECT_DUPLICATE(409) {
      /** 
       * if there is one result, convert to the results code
       * @return result code
       */
      @Override
      public WsGetGroupsLiteResultCode convertToLiteCode() {
        return WsGetGroupsLiteResultCode.SUBJECT_DUPLICATE;
      }
    };

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsGetGroupsResultCode(int statusCode) {
      this.httpStatusCode = statusCode;
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.WsResultCode#getHttpStatusCode()
     */
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }

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
    public abstract WsGetGroupsLiteResultCode convertToLiteCode();

  }

  /**
   * assign the code from the enum
   * @param wsGetGroupsResultsCode
   */
  public void assignResultCode(WsGetGroupsResultCode wsGetGroupsResultsCode) {
    this.getResultMetadata().assignResultCode(wsGetGroupsResultsCode);
  }

  /**
   * convert the result code back to enum
   * @return the enum code
   */
  public WsGetGroupsResultCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsGetGroupsResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * results for each get groups sent in
   */
  private WsGroup[] wsGroups;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * subject that was added 
   */
  private WsSubject wsSubject;

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsGroup[] getWsGroups() {
    return this.wsGroups;
  }

  /**
   * results for each assignment sent in
   * @param results1 the results to set
   */
  public void setWsGroups(WsGroup[] results1) {
    this.wsGroups = results1;
  }

  /**
   * prcess an exception, log, etc
   * @param wsGetGroupsResultsCodeOverride 
   * @param wsAddMemberResultsCodeOverride
   * @param theError
   * @param wsSubjectLookup is current subject
   * @param e
   */
  public void assignResultCodeException(
      WsGetGroupsResultCode wsGetGroupsResultsCodeOverride, String theError, 
      WsSubjectLookup wsSubjectLookup, Exception e) {

    if (e instanceof WsInvalidQueryException) {

      wsGetGroupsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetGroupsResultsCodeOverride, WsGetGroupsResultCode.INVALID_QUERY);

      //see if really something else
      if (e.getCause() instanceof SubjectNotFoundException) {
        wsGetGroupsResultsCodeOverride = WsGetGroupsResultCode.SUBJECT_NOT_FOUND;
      } else if (e.getCause() instanceof SubjectNotUniqueException) {
        wsGetGroupsResultsCodeOverride = WsGetGroupsResultCode.SUBJECT_DUPLICATE;
      } else if (e.getCause() instanceof MemberNotFoundException) {
        wsGetGroupsResultsCodeOverride = WsGetGroupsResultCode.MEMBER_NOT_FOUND;
      }

      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsGetGroupsResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(wsSubjectLookup.toString(), e);

    } else {
      wsGetGroupsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetGroupsResultsCodeOverride, WsGetGroupsResultCode.EXCEPTION);
      LOG.error(theError + ", " + wsSubjectLookup, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsGetGroupsResultsCodeOverride);

    }
  }

  /**
   * put a group in the results
   * @param includeDetail true if the detail for each group should be included
   * @param groupSet
   */
  public void assignGroupResult(Set<Group> groupSet, boolean includeDetail) {
    this.setWsGroups(WsGroup.convertGroups(groupSet, includeDetail));
    this.assignResultCode(WsGetGroupsResultCode.SUCCESS);
  }
  
  /**
   * put pit groups in the results
   * @param pitGroupSet
   */
  public void assignGroupResult(Set<PITGroup> pitGroupSet) {
    this.setWsGroups(WsGroup.convertGroups(pitGroupSet));
    this.assignResultCode(WsGetGroupsResultCode.SUCCESS);
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

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
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  /**
   * convert string to result code
   * @return the result code
   */
  public WsGetGroupsResultCode resultCode() {
    return WsGetGroupsResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

}
