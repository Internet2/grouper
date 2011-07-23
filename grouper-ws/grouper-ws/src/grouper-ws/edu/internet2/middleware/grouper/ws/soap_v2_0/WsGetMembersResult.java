package edu.internet2.middleware.grouper.ws.soap_v2_0;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGetMembersLiteResult.WsGetMembersLiteResultCode;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * <pre>
 * results for the get members call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * GROUP_NOT_FOUND: cant find the group
 * INVALID_QUERY: bad inputs
 * EXCEPTION: something bad happened
 * </pre>
 * @author mchyzer
 */
public class WsGetMembersResult  implements ResultMetadataHolder {

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsGetMembersResult.class);

  /** group that we are checking */
  private WsGroup wsGroup;

  /**
   * result code of a request.  The possible result codes 
   * of WsGetMembersResultCode (with http status codes) are:
   * SUCCESS(200), EXCEPTION(500), INVALID_QUERY(400)
   */
  public static enum WsGetMembersResultCode {

    /** cant find group  */
    GROUP_NOT_FOUND {
      /** 
       * if there is one result, convert to the results code
       * @return result code
       */
      @Override
      public WsGetMembersLiteResultCode convertToLiteCode() {
        return WsGetMembersLiteResultCode.GROUP_NOT_FOUND;
      }
    },

    /** found the members */
    SUCCESS {
      /** 
       * if there is one result, convert to the results code
       * @return result code
       */
      @Override
      public WsGetMembersLiteResultCode convertToLiteCode() {
        return WsGetMembersLiteResultCode.SUCCESS;
      }
    },

    /**
     * if the lookup has both name and uuid, and they dont match
     */
    GROUP_UUID_DOESNT_MATCH_NAME  {
      /** 
       * if there is one result, convert to the results code
       * @return result code
       */
      @Override
      public WsGetMembersLiteResultCode convertToLiteCode() {
        return WsGetMembersLiteResultCode.GROUP_UUID_DOESNT_MATCH_NAME;
      }
    },
    
    /** something bad happened */
    EXCEPTION {
      /** 
       * if there is one result, convert to the results code
       * @return result code
       */
      @Override
      public WsGetMembersLiteResultCode convertToLiteCode() {
        return WsGetMembersLiteResultCode.EXCEPTION;
      }
    },

    /** something bad happened with some of the member retrieval */
    PROBLEM_GETTING_MEMBERS {
      /** 
       * if there is one result, convert to the results code
       * @return result code
       */
      @Override
      public WsGetMembersLiteResultCode convertToLiteCode() {
        return WsGetMembersLiteResultCode.PROBLEM_GETTING_MEMBERS;
      }
    },

    /** invalid query (e.g. if everything blank) */
    INVALID_QUERY {
      /** 
       * if there is one result, convert to the results code
       * @return result code
       */
      @Override
      public WsGetMembersLiteResultCode convertToLiteCode() {
        return WsGetMembersLiteResultCode.INVALID_QUERY;
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
    public abstract WsGetMembersLiteResultCode convertToLiteCode();
  }

  /**
   * assign the code from the enum
   * @param getMembersResultCode
   */
  public void assignResultCode(WsGetMembersResultCode getMembersResultCode) {
    this.getResultMetadata().setResultCode(getMembersResultCode.name());
    this.getResultMetadata().setSuccess(GrouperServiceUtils
        .booleanToStringOneChar(getMembersResultCode.isSuccess()));
  }

  /**
   * prcess an exception, log, etc
   * @param wsGetMembersResultsCodeOverride
   * @param theError
   * @param wsGroupLookup 
   * @param e
   */
  public void assignResultCodeException(
      WsGetMembersResultCode wsGetMembersResultsCodeOverride, String theError,
      WsGroupLookup wsGroupLookup, Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsGetMembersResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetMembersResultsCodeOverride, WsGetMembersResultCode.INVALID_QUERY);
      if (e.getCause() instanceof GroupNotFoundException) {
        wsGetMembersResultsCodeOverride = WsGetMembersResultCode.GROUP_NOT_FOUND;
      }
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsGetMembersResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage()+ ", " + wsGroupLookup + ", " + theError);
      LOG.warn(e);

    } else {
      wsGetMembersResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetMembersResultsCodeOverride, WsGetMembersResultCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ", group: " + wsGroupLookup + ", "  + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsGetMembersResultsCodeOverride);

    }
  }

  /**
   * results for each assignment sent in
   */
  private WsSubject[] wsSubjects;

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsSubject[] getWsSubjects() {
    return this.wsSubjects;
  }

  /**
   * results for each assignment sent in
   * @param results1 the results to set
   */
  public void setWsSubjects(WsSubject[] results1) {
    this.wsSubjects = results1;
  }

  /**
   * @return the wsGroup
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * @param wsGroup1 the wsGroup to set
   */
  public void setWsGroup(WsGroup wsGroup1) {
    this.wsGroup = wsGroup1;
  }

  /**
   * convert members to subject results
   * @param attributeNames1 to get from subjects
   * @param memberSet
   * @param includeSubjectDetails 
   */
  public void assignSubjectResult(Set<Member> memberSet, String[] attributeNames1, boolean includeSubjectDetails) {
    this.setWsSubjects(WsSubject.convertMembers(memberSet, attributeNames1, includeSubjectDetails));
    
    if (this.getWsSubjects() != null) {
      // check all entries
      int successes = 0;
      int failures = 0;
      for (WsSubject wsSubject : this.getWsSubjects()) {
        boolean theSuccess = GrouperUtil.booleanValue(wsSubject.getSuccess(), false);
        if (theSuccess) {
          successes++;
        } else {
          failures++;
        }
      }

      if (failures > 0) {
        this.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures getting members/subjects from the group.   ");
        this.assignResultCode(WsGetMembersResultCode.PROBLEM_GETTING_MEMBERS);

      } else {
        //ok if not failure
        this.assignResultCode(WsGetMembersResultCode.SUCCESS);
      }
    } else {
      //ok if none
      this.assignResultCode(WsGetMembersResultCode.SUCCESS);
    }
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
   * convert string to result code
   * @return the result code
   */
  public WsGetMembersResultCode resultCode() {
    return WsGetMembersResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

}
