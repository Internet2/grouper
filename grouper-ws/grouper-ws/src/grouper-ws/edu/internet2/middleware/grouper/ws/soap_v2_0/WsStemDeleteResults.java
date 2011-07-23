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
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsStemDeleteResult.WsStemDeleteResultCode;

/**
 * <pre>
 * results for the stems delete call.
 * 
 * result code:
 * code of the result for this stem overall
 * SUCCESS: means everything ok
 * </pre>
 * @author mchyzer
 */
public class WsStemDeleteResults implements WsResponseBean, ResultMetadataHolder {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsStemDeleteResults.class);

  /**
   * result code of a request
   */
  public static enum WsStemDeleteResultsCode implements WsResultCode {

    /** found the stems, deleted them (lite http status code 200) (success: T) */
    SUCCESS(200),

    /** either overall exception, or one or more stems had exceptions (lite http status code 500) (success: F) */
    EXCEPTION(500),

    /** problem deleting existing stems (lite http status code 500) (success: F) */
    PROBLEM_DELETING_STEMS(500),

    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400);

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
    private WsStemDeleteResultsCode(int statusCode) {
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
   * @param stemsDeleteResultsCode
   */
  public void assignResultCode(WsStemDeleteResultsCode stemsDeleteResultsCode) {
    this.getResultMetadata().assignResultCode(stemsDeleteResultsCode);
  }

  /**
   * convert the result code back to enum
   * @return the enum code
   */
  public WsStemDeleteResultsCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsStemDeleteResultsCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * results for each deletion sent in
   */
  private WsStemDeleteResult[] results;

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
  public WsStemDeleteResult[] getResults() {
    return this.results;
  }

  /**
   * results for each deletion sent in
   * @param results1 the results to set
   */
  public void setResults(WsStemDeleteResult[] results1) {
    this.results = results1;
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
      for (WsStemDeleteResult wsStemDeleteResult : this.getResults()) {
        boolean theSuccess = "T".equalsIgnoreCase(wsStemDeleteResult.getResultMetadata()
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
        for (WsStemDeleteResult wsStemDeleteResult : this.getResults()) {
          if (GrouperUtil.booleanValue(wsStemDeleteResult.getResultMetadata()
              .getSuccess(), true)) {
            wsStemDeleteResult
                .assignResultCode(WsStemDeleteResultCode.TRANSACTION_ROLLED_BACK);
            failures++;
          }
        }
      }

      if (failures > 0) {
        this.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures deleting stems.   ");
        this.assignResultCode(WsStemDeleteResultsCode.PROBLEM_DELETING_STEMS);
        //this might not be a problem
        LOG.warn(this.getResultMetadata().getResultMessage());

      } else {
        this.assignResultCode(WsStemDeleteResultsCode.SUCCESS);
      }
    } else {
      //none is not ok
      this.assignResultCode(WsStemDeleteResultsCode.INVALID_QUERY);
      this.getResultMetadata().setResultMessage("Must pass in at least one stem to delete");
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
   * prcess an exception, log, etc
   * @param wsStemDeleteResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsStemDeleteResultsCode wsStemDeleteResultsCodeOverride, String theError,
      Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsStemDeleteResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsStemDeleteResultsCodeOverride, WsStemDeleteResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsStemDeleteResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsStemDeleteResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsStemDeleteResultsCodeOverride, WsStemDeleteResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsStemDeleteResultsCodeOverride);

    }
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
