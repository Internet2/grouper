package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.ws.WsResultCode;

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
public class WsStemDeleteResults {

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
