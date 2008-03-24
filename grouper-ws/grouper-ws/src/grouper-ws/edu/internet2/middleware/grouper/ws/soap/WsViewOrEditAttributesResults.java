package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;

/**
 * <pre>
 * results for the viewOrEditAttributes
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * GROUP_NOT_FOUND: cant find the group
 * GROUP_DUPLICATE: found multiple groups
 * </pre>
 * @author mchyzer
 */
public class WsViewOrEditAttributesResults {

  /**
   * result code of a request
   */
  public enum WsViewOrEditAttributesResultsCode {

    /** found the subject, assigned or viewed */
    SUCCESS,

    /** found the subject */
    EXCEPTION,

    /** problem deleting existing members */
    PROBLEM_WITH_GROUPS,

    /** invalid query (e.g. if everything blank) */
    INVALID_QUERY;

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
   * @param viewOrEditAttributesResultCode
   */
  public void assignResultCode(
      WsViewOrEditAttributesResultsCode viewOrEditAttributesResultCode) {
    //TODO if this is used, then use WsResultCode
    this.getResultMetadata().assignResultCode(
        viewOrEditAttributesResultCode == null ? null : viewOrEditAttributesResultCode
            .name());
    this.getResultMetadata().assignSuccess(
        viewOrEditAttributesResultCode.isSuccess() ? "T" : "F");
  }

  /**
   * convert the result code back to enum
   * @return the enum code
   */
  public WsViewOrEditAttributesResultsCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsViewOrEditAttributesResultsCode.valueOf(this.getResultMetadata()
        .getResultCode());
  }

  /**
   * results for each assignment sent in
   */
  private WsViewOrEditAttributesResult[] results;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsViewOrEditAttributesResult[] getResults() {
    return this.results;
  }

  /**
   * results for each assignment sent in
   * @param results1 the results to set
   */
  public void setResults(WsViewOrEditAttributesResult[] results1) {
    this.results = results1;
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

}
