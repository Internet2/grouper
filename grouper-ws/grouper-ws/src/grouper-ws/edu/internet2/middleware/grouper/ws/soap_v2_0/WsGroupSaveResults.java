package edu.internet2.middleware.grouper.ws.soap_v2_0;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGroupSaveResult.WsGroupSaveResultCode;

/**
 * <pre>
 * results for the groups save call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * GROUP_NOT_FOUND: cant find the group
 * GROUP_DUPLICATE: found multiple groups
 * </pre>
 * @author mchyzer
 */
public class WsGroupSaveResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * result code of a request
   */
  public static enum WsGroupSaveResultsCode implements WsResultCode {

    /** found the groups, saved them (lite http status code 201) (success: T) */
    SUCCESS(201),

    /** either overall exception, or one or more groups had exceptions (lite http status code 500) (success: F) */
    EXCEPTION(500),

    /** problem saving existing groups (lite http status code 500) (success: F) */
    PROBLEM_SAVING_GROUPS(500),

    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsGroupSaveResultsCode(int statusCode) {
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
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsGroupSaveResults.class);

  /**
   * prcess an exception, log, etc
   * @param wsGroupSaveResultsCodeOverride
   * @param theError
   * @param e
   * @param clientVersion 
   */
  public void assignResultCodeException(
      WsGroupSaveResultsCode wsGroupSaveResultsCodeOverride, String theError, Exception e, GrouperVersion clientVersion) {

    if (e instanceof WsInvalidQueryException) {
      wsGroupSaveResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGroupSaveResultsCodeOverride, WsGroupSaveResultsCode.INVALID_QUERY);
      //      if (e.getCause() instanceof GroupNotFoundException) {
      //        wsGroupSaveResultsCodeOverride = WsGroupSaveResultsCode.GROUP_NOT_FOUND;
      //      }
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsGroupSaveResultsCodeOverride, clientVersion);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsGroupSaveResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGroupSaveResultsCodeOverride, WsGroupSaveResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsGroupSaveResultsCodeOverride, clientVersion);

    }
  }

  /**
   * assign the code from the enum
   * @param groupSaveResultsCode
   * @param clientVersion 
   */
  public void assignResultCode(WsGroupSaveResultsCode groupSaveResultsCode, GrouperVersion clientVersion) {
    this.getResultMetadata().assignResultCode(groupSaveResultsCode, clientVersion);
  }

  /**
   * make sure if there is an error, to record that as an error
   * @param grouperTransactionType for request
   * @param theSummary
   * @param clientVersion 
   * @return true if success, false if not
   */
  public boolean tallyResults(GrouperTransactionType grouperTransactionType,
      String theSummary, GrouperVersion clientVersion) {
    //maybe already a failure
    boolean successOverall = GrouperUtil.booleanValue(this.getResultMetadata()
        .getSuccess(), true);
    if (this.getResults() != null) {
      // check all entries
      int successes = 0;
      int failures = 0;
      for (WsGroupSaveResult wsGroupSaveResult : this.getResults()) {
        boolean theSuccess = "T".equalsIgnoreCase(wsGroupSaveResult.getResultMetadata()
            .getSuccess());
        if (theSuccess) {
          successes++;
        } else {
          failures++;
        }
      }

      //if transaction rolled back all line items, 
      if ((!successOverall || failures > 0) && grouperTransactionType.isTransactional()
          && !grouperTransactionType.isReadonly()) {
        for (WsGroupSaveResult wsGroupSaveResult : this.getResults()) {
          if (GrouperUtil.booleanValue(wsGroupSaveResult.getResultMetadata().getSuccess(),
              true)) {
            wsGroupSaveResult
                .assignResultCode(WsGroupSaveResultCode.TRANSACTION_ROLLED_BACK, clientVersion);
            failures++;
          }
        }
      }

      if (failures > 0) {
        this.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures of saving groups.   ");
        this.assignResultCode(WsGroupSaveResultsCode.PROBLEM_SAVING_GROUPS, clientVersion);
        //this might not be a problem
        LOG.warn(this.getResultMetadata().getResultMessage());

      } else {
        this.assignResultCode(WsGroupSaveResultsCode.SUCCESS, clientVersion);
      }
    } else {
      //none is not ok
      this.assignResultCode(WsGroupSaveResultsCode.INVALID_QUERY, clientVersion);
      this.getResultMetadata().setResultMessage("Must pass in at least one group to save");
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
   * convert the result code back to enum
   * @return the enum code
   */
  public WsGroupSaveResultsCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsGroupSaveResultsCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * results for each deletion sent in
   */
  private WsGroupSaveResult[] results;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * results for each deletion sent in
   * @return the results
   */
  public WsGroupSaveResult[] getResults() {
    return this.results;
  }

  /**
   * results for each deletion sent in
   * @param results1 the results to set
   */
  public void setResults(WsGroupSaveResult[] results1) {
    this.results = results1;
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

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

}
