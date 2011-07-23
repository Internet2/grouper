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
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGroupDeleteResult.WsGroupDeleteResultCode;

/**
 * <pre>
 * results for the groups delete call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * GROUP_NOT_FOUND: cant find the group
 * GROUP_DUPLICATE: found multiple groups
 * </pre>
 * @author mchyzer
 */
public class WsGroupDeleteResults implements WsResponseBean, ResultMetadataHolder {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsGroupDeleteResults.class);

  /**
   * result code of a request
   */
  public static enum WsGroupDeleteResultsCode implements WsResultCode {

    /** found the groups, deleted them (lite status code 200) (success: T) */
    SUCCESS(200),

    /** either overall exception, or one or more groups had exceptions (lite status code 500) (success: F) */
    EXCEPTION(500),

    /** problem deleting existing groups (lite status code 500) (success: F) */
    PROBLEM_DELETING_GROUPS(500),

    /** invalid query (e.g. if everything blank) (lite status code 400) (success: F) */
    INVALID_QUERY(400);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsGroupDeleteResultsCode(int statusCode) {
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
   * @param groupsDeleteResultsCode should not be null
   */
  public void assignResultCode(WsGroupDeleteResultsCode groupsDeleteResultsCode) {
    this.getResultMetadata().assignResultCode(groupsDeleteResultsCode);
  }

  /**
   * convert the result code back to enum
   * @return the enum code
   */
  public WsGroupDeleteResultsCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsGroupDeleteResultsCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * results for each deletion sent in
   */
  private WsGroupDeleteResult[] results;

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
  public WsGroupDeleteResult[] getResults() {
    return this.results;
  }

  /**
   * results for each deletion sent in
   * @param results1 the results to set
   */
  public void setResults(WsGroupDeleteResult[] results1) {
    this.results = results1;
  }

  /**
   * prcess an exception, log, etc
   * @param wsGroupDeleteResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsGroupDeleteResultsCode wsGroupDeleteResultsCodeOverride, String theError,
      Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsGroupDeleteResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGroupDeleteResultsCodeOverride, WsGroupDeleteResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsGroupDeleteResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsGroupDeleteResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGroupDeleteResultsCodeOverride, WsGroupDeleteResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsGroupDeleteResultsCodeOverride);

    }
  }

  /**
   * make sure if there is an error, to record that as an error
   * @param grouperTransactionType for request
   * @param theSummary of entire request
   * @return true if not need to rollback, and false if so
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
      for (WsGroupDeleteResult wsGroupDeleteResult : this.getResults()) {
        boolean theSuccess = "T".equalsIgnoreCase(wsGroupDeleteResult.getResultMetadata()
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
        successes = 0;
        for (WsGroupDeleteResult wsGroupDeleteResult : this.getResults()) {
          if (GrouperUtil.booleanValue(wsGroupDeleteResult.getResultMetadata()
              .getSuccess(), true)) {
            wsGroupDeleteResult
                .assignResultCode(WsGroupDeleteResultCode.TRANSACTION_ROLLED_BACK);
            failures++;
          }
        }
      }

      if (failures > 0) {
        this.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures of deleting groups.   ");
        this.assignResultCode(WsGroupDeleteResultsCode.PROBLEM_DELETING_GROUPS);
        //this might not be a problem
        LOG.warn(this.getResultMetadata().getResultMessage());

      } else {
        this.assignResultCode(WsGroupDeleteResultsCode.SUCCESS);
      }
    } else {
      //none is not ok
      this.assignResultCode(WsGroupDeleteResultsCode.INVALID_QUERY);
      this.getResultMetadata().setResultMessage("Must pass in at least one group to delete");
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
