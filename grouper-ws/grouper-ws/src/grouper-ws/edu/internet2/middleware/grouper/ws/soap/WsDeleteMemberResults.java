package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberResult.WsDeleteMemberResultCode;

/**
 * <pre>
 * results for the delete member call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * GROUP_NOT_FOUND: cant find the group
 * GROUP_DUPLICATE: found multiple groups
 * </pre>
 * @author mchyzer
 */
public class WsDeleteMemberResults {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsDeleteMemberResults.class);

  /**
   * prcess an exception, log, etc
   * @param wsDeleteMemberResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsDeleteMemberResultsCode wsDeleteMemberResultsCodeOverride, String theError,
      Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsDeleteMemberResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsDeleteMemberResultsCodeOverride, WsDeleteMemberResultsCode.INVALID_QUERY);
      if (e.getCause() instanceof GroupNotFoundException) {
        wsDeleteMemberResultsCodeOverride = WsDeleteMemberResultsCode.GROUP_NOT_FOUND;
      }
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsDeleteMemberResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsDeleteMemberResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsDeleteMemberResultsCodeOverride, WsDeleteMemberResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsDeleteMemberResultsCodeOverride);

    }
  }

  /**
   * result code of a request
   */
  public enum WsDeleteMemberResultsCode implements WsResultCode {

    /** cant find group (lite http status code 404) (success: F) */
    GROUP_NOT_FOUND(404),

    /** found the subject (lite http status code 200) (success: T) */
    SUCCESS(200),

    /** found the subject (lite http status code 500) (success: F) */
    EXCEPTION(500),

    /** problem deleting existing members (lite http status code 500) (success: F) */
    PROBLEM_DELETING_MEMBERS(500),

    /** if there is one delete, and it is subject duplicate (lite http status code 409) (success: F) */
    SUBJECT_DUPLICATE(409),

    /** if there is one delete, and it is insufficient privs (lite http status code 403) (success: F) */
    INSUFFICIENT_PRIVILEGES(403),

    /** if there is one delete, and it is SUCCESS_BUT_HAS_EFFECTIVE (lite http status code 200) (success: T) */
    SUCCESS_BUT_HAS_EFFECTIVE(200),

    /** if there is one delete, and it is SUCCESS_WASNT_IMMEDIATE (lite http status code 200) (success: T) */
    SUCCESS_WASNT_IMMEDIATE(200),

    /** if there is one delete, and it is SUCCESS_WASNT_IMMEDIATE_BUT_HAS_EFFECTIVE (lite http status code 200) (success: T) */
    SUCCESS_WASNT_IMMEDIATE_BUT_HAS_EFFECTIVE(200),

    /** if there is one delete, and it is SUBJECT_NOT_FOUND (lite http status code 404) (success: F) */
    SUBJECT_NOT_FOUND(404),

    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400);

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this.name().startsWith("SUCCESS");
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsDeleteMemberResultsCode(int statusCode) {
      this.httpStatusCode = statusCode;
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.WsResultCode#getHttpStatusCode()
     */
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }
  }

  /**
   * convert the result code back to enum
   * 
   * @return the enum code
   */
  public WsDeleteMemberResultsCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsDeleteMemberResultsCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * make sure if there is an error, to record that as an error
   * @param grouperTransactionType for request
   * @param theSummary of entire request
   * @return true if success, false if not
   */
  public boolean tallyResults(GrouperTransactionType grouperTransactionType,
      String theSummary) {
    //maybe already a failure
    boolean successOverall = GrouperUtil.booleanValue(this.getResultMetadata()
        .getSuccess(), true);
    if (this.getResults() != null) {
      // check all entries
      int successes = 0;
      int failures = 0;
      for (WsDeleteMemberResult wsDeleteMemberResult : this.getResults()) {
        boolean theSuccess = "T".equalsIgnoreCase(wsDeleteMemberResult
            .getResultMetadata().getSuccess());
        if (theSuccess) {
          successes++;
        } else {
          failures++;
        }
      }

      //if transaction rolled back all line items, 
      if ((!successOverall || failures > 0) && grouperTransactionType.isTransactional()
          && !grouperTransactionType.isReadonly()) {
        successes = 0;
        for (WsDeleteMemberResult wsDeleteMemberResult : this.getResults()) {
          if (GrouperUtil.booleanValue(wsDeleteMemberResult.getResultMetadata()
              .getSuccess(), true)) {
            wsDeleteMemberResult
                .assignResultCode(WsDeleteMemberResultCode.TRANSACTION_ROLLED_BACK);
            failures++;
          }
        }
      }

      if (failures > 0) {
        this.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures of users deleted from the group.   ");
        this.assignResultCode(WsDeleteMemberResultsCode.PROBLEM_DELETING_MEMBERS);
        //this might not be a problem
        LOG.warn(this.getResultMetadata().getResultMessage());

      } else {
        this.assignResultCode(WsDeleteMemberResultsCode.SUCCESS);
      }
      //if there is one result, just set that as the result code of parent
      if (GrouperUtil.length(this.getResults()) == 0) {
        this.assignResultCode(this.getResults()[0].resultCode().convertToResultsCode());
      }

    } else {
      //none is not ok, must pass one in
      this.assignResultCode(WsDeleteMemberResultsCode.INVALID_QUERY);
      this.getResultMetadata().appendResultMessage(
          "You must pass in at least one subject");
    }
    //make response descriptive
    if (GrouperUtil.booleanValue(this.getResultMetadata().getSuccess(), false)) {
      this.getResultMetadata().appendResultMessage("Success for: " + theSummary);
      return true;
    }
    //false if need rollback
    return !grouperTransactionType.isTransactional();
  }

  /**
   * assign the code from the enum
   * @param deleteMemberResultsCode
   */
  public void assignResultCode(WsDeleteMemberResultsCode deleteMemberResultsCode) {
    this.getResultMetadata().assignResultCode(deleteMemberResultsCode);
  }

  /**
   * results for each assignment sent in
   */
  private WsDeleteMemberResult[] results;

  /**
   * group assigned to
   */
  private WsGroup wsGroupAssigned;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsDeleteMemberResult[] getResults() {
    return this.results;
  }

  /**
   * results for each assignment sent in
   * @param results1 the results to set
   */
  public void setResults(WsDeleteMemberResult[] results1) {
    this.results = results1;
  }

  /**
   * group assigned to
   * @return the wsGroupLookup
   */
  public WsGroup getWsGroupAssigned() {
    return this.wsGroupAssigned;
  }

  /**
   * group assigned to
   * @param theWsGroupLookupAssigned the wsGroupLookup to set
   */
  public void setWsGroupAssigned(WsGroup theWsGroupLookupAssigned) {
    this.wsGroupAssigned = theWsGroupLookupAssigned;
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

}
