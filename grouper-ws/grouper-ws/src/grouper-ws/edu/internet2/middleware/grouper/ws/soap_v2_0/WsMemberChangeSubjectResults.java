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
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsMemberChangeSubjectResult.WsMemberChangeSubjectResultCode;

/**
 * <pre>
 * results for the member change subject call.
 * 
 * result code e.g.:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * </pre>
 * 
 * @author mchyzer
 */
public class WsMemberChangeSubjectResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * result code of a request
   */
  public static enum WsMemberChangeSubjectResultsCode implements WsResultCode {

    /** found the subject (rest http status code 200) (success: T) */
    SUCCESS(200),

    /** found the subject (rest http status code 500) (success: F) */
    EXCEPTION(500),

    /** invalid query (e.g. if everything blank) (rest http status code 400) (success: F) */
    INVALID_QUERY(400),

    /** something in one assignment wasnt successful (rest http status code 500) (success: F) */
    PROBLEM_WITH_CHANGE(500);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /**
     * if this is a successful result
     * 
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
    private WsMemberChangeSubjectResultsCode(int statusCode) {
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
   * assign the code from the enum
   * 
   * @param memberChangeSubjectResultsCode
   */
  public void assignResultCode(WsMemberChangeSubjectResultsCode memberChangeSubjectResultsCode) {
    this.getResultMetadata().assignResultCode(memberChangeSubjectResultsCode);
  }

  /**
   * convert the result code back to enum
   * 
   * @return the enum code
   */
  public WsMemberChangeSubjectResultsCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsMemberChangeSubjectResultsCode.valueOf(
        this.getResultMetadata().getResultCode());
  }

  /**
   * results for each assignment sent in
   */
  private WsMemberChangeSubjectResult[] results;

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsMemberChangeSubjectResults.class);

  /**
   * prcess an exception, log, etc
   * @param wsMemberChangeSubjectResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsMemberChangeSubjectResultsCode wsMemberChangeSubjectResultsCodeOverride, String theError, Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsMemberChangeSubjectResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsMemberChangeSubjectResultsCodeOverride, WsMemberChangeSubjectResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsMemberChangeSubjectResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsMemberChangeSubjectResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsMemberChangeSubjectResultsCodeOverride, WsMemberChangeSubjectResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsMemberChangeSubjectResultsCodeOverride);

    }
  }

  /**
   * make sure if there is an error, to record that as an error
   * @param grouperTransactionType for request
   * @param theSummary
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
      for (WsMemberChangeSubjectResult wsMemberChangeSubjectResult : this.getResults()) {
        boolean theSuccess = "T".equalsIgnoreCase(wsMemberChangeSubjectResult.getResultMetadata()
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
        for (WsMemberChangeSubjectResult wsMemberChangeSubjectResult : this.getResults()) {
          if (GrouperUtil.booleanValue(
              wsMemberChangeSubjectResult.getResultMetadata().getSuccess(), true)) {
            wsMemberChangeSubjectResult
                .assignResultCode(WsMemberChangeSubjectResultCode.TRANSACTION_ROLLED_BACK);
            failures++;
          }
        }
      }
      if (failures > 0) {
        this.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures of changing members subjects.   ");
        this.assignResultCode(WsMemberChangeSubjectResultsCode.PROBLEM_WITH_CHANGE);
        //this might not be a problem
        LOG.warn(this.getResultMetadata().getResultMessage());

      } else {
        this.assignResultCode(WsMemberChangeSubjectResultsCode.SUCCESS);
      }
    } else {
      //none is not ok, must pass one in
      this.assignResultCode(WsMemberChangeSubjectResultsCode.INVALID_QUERY);
      this.getResultMetadata().appendResultMessage(
          "You must pass in at least one change subject bean");
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
   * results for each assignment sent in
   * 
   * @return the results
   */
  public WsMemberChangeSubjectResult[] getResults() {
    return this.results;
  }

  /**
   * results for each assignment sent in
   * 
   * @param results1
   *            the results to set
   */
  public void setResults(WsMemberChangeSubjectResult[] results1) {
    this.results = results1;
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
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

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  
  /**
   * @param responseMetadata1 the responseMetadata to set
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }

}
