package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ws.WsResultCode;

/**
 * <pre>
 * results for the viewOrEditPrivileges call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * GROUP_NOT_FOUND: cant find the group
 * GROUP_DUPLICATE: found multiple groups
 * </pre>
 * @author mchyzer
 */
public class WsViewOrEditPrivilegesResults {

  /**
   * result code of a request
   */
  public enum WsViewOrEditPrivilegesResultsCode implements WsResultCode {

    /** found the subject, assigned or viewed (lite http status code 201) (success: T) */
    SUCCESS(201),

    /** found the subject (lite http status code 500) (success: F) */
    EXCEPTION(500),

    /** problem deleting existing members (lite http status code 500) (success: F) */
    PROBLEM_WITH_MEMBERS(500),

    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400);

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsViewOrEditPrivilegesResultsCode(int statusCode) {
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
   * @param viewOrEditPrivilegesResultCode
   */
  public void assignResultCode(
      WsViewOrEditPrivilegesResultsCode viewOrEditPrivilegesResultCode) {
    this.getResultMetadata().assignResultCode(viewOrEditPrivilegesResultCode);
  }

  /**
   * convert the result code back to enum
   * @return the enum code
   */
  public WsViewOrEditPrivilegesResultsCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsViewOrEditPrivilegesResultsCode.valueOf(this.getResultMetadata()
        .getResultCode());
  }

  /**
   * results for each assignment sent in
   */
  private WsViewOrEditPrivilegesResult[] results;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsViewOrEditPrivilegesResult[] getResults() {
    return this.results;
  }

  /**
   * results for each assignment sent in
   * @param results1 the results to set
   */
  public void setResults(WsViewOrEditPrivilegesResult[] results1) {
    this.results = results1;
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

}
