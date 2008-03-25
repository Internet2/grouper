package edu.internet2.middleware.grouper.ws.soap;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;

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
public class WsGetMembersResults implements WsResponseBean {

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsGetMembersResults.class);

  /** group that we are checking */
  private WsGroup wsGroup;

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * result code of a request.  The possible result codes 
   * of WsGetMembersResultCode (with http status codes) are:
   * SUCCESS(200), EXCEPTION(500), INVALID_QUERY(400)
   */
  public enum WsGetMembersResultsCode implements WsResultCode {

    /** cant find group (lite status code 400) (success: F)  */
    GROUP_NOT_FOUND(400),

    /** found the members (lite status code 200) (success: T) */
    SUCCESS(200),

    /** something bad happened (lite status code 500) (success: F) */
    EXCEPTION(500),

    /** something bad happened with some of the member retrieval (lite status code 500) (success: F) */
    PROBLEM_GETTING_MEMBERS(500),

    /** invalid query (e.g. if everything blank) (lite status code 400) (success: F) */
    INVALID_QUERY(400);

    /**
     * construct with http code
     * @param theHttpStatusCode the code
     */
    private WsGetMembersResultsCode(int theHttpStatusCode) {
      this.httpStatusCode = theHttpStatusCode;
    }

    /** http status code for result code */
    private int httpStatusCode;

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }

    /** get the http result code for this status code
     * @return the status code
     */
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }
  }

  /**
   * assign the code from the enum
   * @param getMembersResultCode
   */
  public void assignResultCode(WsGetMembersResultsCode getMembersResultCode) {
    this.getResultMetadata().assignResultCode(getMembersResultCode);
  }

  /**
   * prcess an exception, log, etc
   * @param wsGetMembersResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsGetMembersResultsCode wsGetMembersResultsCodeOverride, String theError,
      Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsGetMembersResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetMembersResultsCodeOverride, WsGetMembersResultsCode.INVALID_QUERY);
      if (e.getCause() instanceof GroupNotFoundException) {
        wsGetMembersResultsCodeOverride = WsGetMembersResultsCode.GROUP_NOT_FOUND;
      }
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsGetMembersResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsGetMembersResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetMembersResultsCodeOverride, WsGetMembersResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsGetMembersResultsCodeOverride);

    }
  }

  /**
   * results for each assignment sent in
   */
  private WsSubject[] results;

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsSubject[] getResults() {
    return this.results;
  }

  /**
   * results for each assignment sent in
   * @param results1 the results to set
   */
  public void setResults(WsSubject[] results1) {
    this.results = results1;
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @return the attributeNames
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @param attributeNamesa the attributeNames to set
   */
  public void setSubjectAttributeNames(String[] attributeNamesa) {
    this.subjectAttributeNames = attributeNamesa;
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
   */
  public void assignSubjectResult(Set<Member> memberSet, String[] attributeNames1) {
    this.setSubjectAttributeNames(attributeNames1);
    this.setResults(WsSubject.convertMembers(memberSet, attributeNames1));
  }

  /**
   * make sure if there is an error, to record that as an error
   * @param theSummary
   */
  public void tallyResults(String theSummary) {
    //maybe already a failure
    boolean successOverall = GrouperUtil.booleanValue(this.getResultMetadata()
        .getSuccess(), true);
    if (this.getResults() != null) {
      // check all entries
      int successes = 0;
      int failures = 0;
      for (WsSubject wsSubject : this.getResults()) {
        boolean theSuccess = GrouperUtil.booleanValue(wsSubject.getSuccess(), false);
        if (theSuccess) {
          successes++;
        } else {
          failures++;
        }
      }

      if (failures > 0 || !successOverall) {
        this.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures getting members from the group.   ");
        this.assignResultCode(WsGetMembersResultsCode.PROBLEM_GETTING_MEMBERS);

      } else {
        //ok if not failure
        this.assignResultCode(WsGetMembersResultsCode.SUCCESS);
      }
    } else {
      //ok if none
      this.assignResultCode(WsGetMembersResultsCode.SUCCESS);
    }
    //make response descriptive
    if (GrouperUtil.booleanValue(this.getResultMetadata().getSuccess(), false)) {
      this.getResultMetadata().appendResultMessage("Success for: " + theSummary);
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
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

}
