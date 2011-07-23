package edu.internet2.middleware.grouper.ws.soap_v2_0;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
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
public class WsGetMembersResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsGetMembersResults.class);

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * result code of a request.  The possible result codes 
   * of WsGetMembersResultCode (with http status codes) are:
   * SUCCESS(200), EXCEPTION(500), INVALID_QUERY(400)
   */
  public static enum WsGetMembersResultsCode implements WsResultCode {

    /** found the members (lite status code 200) (success: T) */
    SUCCESS(200),

    /** something bad happened (lite status code 500) (success: F) */
    EXCEPTION(500),

    /** something bad happened with some of the member retrieval (lite status code 500) (success: F) */
    PROBLEM_GETTING_MEMBERS(500),

    /** invalid query (e.g. if everything blank) (lite status code 400) (success: F) */
    INVALID_QUERY(400);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

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
      for (WsGetMembersResult wsGetMembersResult : this.getResults()) {
        boolean theSuccess = "T".equalsIgnoreCase(wsGetMembersResult.getResultMetadata()
            .getSuccess());
        if (theSuccess) {
          successes++;
        } else {
          failures++;
        }
      }

      if (failures > 0) {
        this.getResultMetadata().appendResultMessage(
            "There were " + successes + " successes and " + failures
                + " failures of getting members for groups.   ");
        this.assignResultCode(WsGetMembersResultsCode.PROBLEM_GETTING_MEMBERS);
        //this might not be a problem
        LOG.warn(this.getResultMetadata().getResultMessage());

      } else {
        //if we havent already seen an error...
        if (successOverall) {
          this.assignResultCode(WsGetMembersResultsCode.SUCCESS);
        }
      }
    } else {
      //none is not ok, must pass one in
      this.assignResultCode(WsGetMembersResultsCode.INVALID_QUERY);
      this.getResultMetadata().appendResultMessage(
          "You must pass in at least one group");
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
   * results for each assignment sent in
   */
  private WsGetMembersResult[] results;

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

  /**
   * results for each assignment sent in
   * 
   * @return the results
   */
  public WsGetMembersResult[] getResults() {
    return this.results;
  }

  /**
   * results for each assignment sent in
   * 
   * @param results1
   *            the results to set
   */
  public void setResults(WsGetMembersResult[] results1) {
    this.results = results1;
  }

}
