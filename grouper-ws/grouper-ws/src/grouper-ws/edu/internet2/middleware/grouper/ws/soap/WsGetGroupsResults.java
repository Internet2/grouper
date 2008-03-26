package edu.internet2.middleware.grouper.ws.soap;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.MemberNotFoundException;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * <pre>
 * results for the add member call.
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
public class WsGetGroupsResults implements WsResponseBean {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsGetGroupsResults.class);

  /**
   * result code of a request
   */
  public enum WsGetGroupsResultsCode implements WsResultCode {

    /** found the subject (lite http status code 200) (success: T) */
    SUCCESS(200),

    /** problem (lite http status code 500) (success: F) */
    EXCEPTION(500),

    /** problem (lite http status code 400) (success: F) */
    INVALID_QUERY(400),

    /** couldnt find the member to query (lite http status code 404) (success: F) */
    MEMBER_NOT_FOUND(404),

    /** couldnt find the subject to query (lite http status code 404) (success: F) */
    SUBJECT_NOT_FOUND(404),

    /** problem querying the subject, was duplicate (lite http status code 409) (success: F) */
    SUBJECT_DUPLICATE(409);

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsGetGroupsResultsCode(int statusCode) {
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

  }

  /**
   * assign the code from the enum
   * @param wsGetGroupsResultsCode
   */
  public void assignResultCode(WsGetGroupsResultsCode wsGetGroupsResultsCode) {
    this.getResultMetadata().assignResultCode(wsGetGroupsResultsCode);
  }

  /**
   * convert the result code back to enum
   * @return the enum code
   */
  public WsGetGroupsResultsCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsGetGroupsResultsCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * results for each assignment sent in
   */
  private WsGroup[] results;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsGroup[] getResults() {
    return this.results;
  }

  /**
   * results for each assignment sent in
   * @param results1 the results to set
   */
  public void setResults(WsGroup[] results1) {
    this.results = results1;
  }

  /**
   * prcess an exception, log, etc
   * @param wsGetGroupsResultsCodeOverride 
   * @param wsAddMemberResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsGetGroupsResultsCode wsGetGroupsResultsCodeOverride, String theError, Exception e) {

    if (e instanceof WsInvalidQueryException) {

      wsGetGroupsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetGroupsResultsCodeOverride, WsGetGroupsResultsCode.INVALID_QUERY);

      //see if really something else
      if (e.getCause() instanceof SubjectNotFoundException) {
        wsGetGroupsResultsCodeOverride = WsGetGroupsResultsCode.SUBJECT_NOT_FOUND;
      } else if (e.getCause() instanceof SubjectNotUniqueException) {
        wsGetGroupsResultsCodeOverride = WsGetGroupsResultsCode.SUBJECT_DUPLICATE;
      } else if (e.getCause() instanceof MemberNotFoundException) {
        wsGetGroupsResultsCodeOverride = WsGetGroupsResultsCode.MEMBER_NOT_FOUND;
      }

      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsGetGroupsResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsGetGroupsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetGroupsResultsCodeOverride, WsGetGroupsResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsGetGroupsResultsCodeOverride);

    }
  }

  /**
   * put a group in the results
   * @param includeDetail true if the detail for each group should be included
   * @param group
   */
  public void assignGroupResult(Group group, boolean includeDetail) {
    this.assignGroupResult(GrouperUtil.toSet(group), includeDetail);
  }

  /**
   * put a group in the results
   * @param includeDetail true if the detail for each group should be included
   * @param groupSet
   */
  public void assignGroupResult(Set<Group> groupSet, boolean includeDetail) {
    this.setResults(WsGroup.convertGroups(groupSet, includeDetail));
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  /**
   * @param responseMetadata1 the responseMetadata to set
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }

}
