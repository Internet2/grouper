package edu.internet2.middleware.grouper.ws.soap_v2_0;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAddMemberResult.WsAddMemberResultCode;

/**
 * <pre>
 * results for the add member call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * GROUP_NOT_FOUND: cant find the group
 * GROUP_DUPLICATE: found multiple groups
 * </pre>
 * 
 * @author mchyzer
 */
public class WsAddMemberResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * result code of a request
   */
  public static enum WsAddMemberResultsCode implements WsResultCode {

    /** cant find group (rest http status code 404) (success: F) */
    GROUP_NOT_FOUND(404),

    /** found the subject (rest http status code 201) (success: T) */
    SUCCESS(201),

    /** found the subject (rest http status code 500) (success: F) */
    EXCEPTION(500),

    /** problem deleting existing members (rest http status code 500) (success: F) */
    PROBLEM_DELETING_MEMBERS(500),

    /** invalid query (e.g. if everything blank) (rest http status code 400) (success: F) */
    INVALID_QUERY(400),

    /** something in one assignment wasnt successful (rest http status code 500) (success: F) */
    PROBLEM_WITH_ASSIGNMENT(500);

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
    private WsAddMemberResultsCode(int statusCode) {
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
   * @param addMemberResultsCode
   */
  public void assignResultCode(WsAddMemberResultsCode addMemberResultsCode) {
    this.getResultMetadata().assignResultCode(addMemberResultsCode);
  }

  /**
   * convert the result code back to enum
   * 
   * @return the enum code
   */
  public WsAddMemberResultsCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsAddMemberResultsCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * results for each assignment sent in
   */
  private WsAddMemberResult[] results;

  /**
   * group assigned to
   */
  private WsGroup wsGroupAssigned;

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsAddMemberResults.class);

  /**
   * prcess an exception, log, etc
   * @param wsAddMemberResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsAddMemberResultsCode wsAddMemberResultsCodeOverride, String theError, Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsAddMemberResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsAddMemberResultsCodeOverride, WsAddMemberResultsCode.INVALID_QUERY);
      if (e.getCause() instanceof GroupNotFoundException) {
        wsAddMemberResultsCodeOverride = WsAddMemberResultsCode.GROUP_NOT_FOUND;
      }
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsAddMemberResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsAddMemberResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsAddMemberResultsCodeOverride, WsAddMemberResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsAddMemberResultsCodeOverride);

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
      for (WsAddMemberResult wsAddMemberResult : this.getResults()) {
        boolean theSuccess = "T".equalsIgnoreCase(wsAddMemberResult.getResultMetadata()
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
        for (WsAddMemberResult wsAddMemberResult : this.getResults()) {
          if (GrouperUtil.booleanValue(
              wsAddMemberResult.getResultMetadata().getSuccess(), true)) {
            wsAddMemberResult
                .assignResultCode(WsAddMemberResultCode.TRANSACTION_ROLLED_BACK);
            failures++;
          }
        }
      }
      if (failures > 0) {
        this.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures of users added to the group.   ");
        this.assignResultCode(WsAddMemberResultsCode.PROBLEM_WITH_ASSIGNMENT);
        //this might not be a problem
        LOG.warn(this.getResultMetadata().getResultMessage());

      } else {
        this.assignResultCode(WsAddMemberResultsCode.SUCCESS);
      }
    } else {
      //none is not ok, must pass one in
      this.assignResultCode(WsAddMemberResultsCode.SUCCESS);
      this.getResultMetadata().appendResultMessage(
          "No subjects were passed in, ");
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
  public WsAddMemberResult[] getResults() {
    return this.results;
  }

  /**
   * results for each assignment sent in
   * 
   * @param results1
   *            the results to set
   */
  public void setResults(WsAddMemberResult[] results1) {
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
