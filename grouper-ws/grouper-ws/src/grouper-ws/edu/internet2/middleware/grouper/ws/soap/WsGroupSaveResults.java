package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ws.WsResultCode;

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
public class WsGroupSaveResults {

  /**
   * result code of a request
   */
  public enum WsGroupSaveResultsCode implements WsResultCode {

    /** found the groups, saved them (lite http status code 201) (success: T) */
    SUCCESS(201),

    /** either overall exception, or one or more groups had exceptions (lite http status code 500) (success: F) */
    EXCEPTION(500),

    /** problem deleting existing groups (lite http status code 500) (success: F) */
    PROBLEM_DELETING_GROUPS(500),

    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400);

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
   * assign the code from the enum
   * @param groupSaveResultsCode
   */
  public void assignResultCode(WsGroupSaveResultsCode groupSaveResultsCode) {
    this.getResultMetadata().assignResultCode(groupSaveResultsCode);
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

}
