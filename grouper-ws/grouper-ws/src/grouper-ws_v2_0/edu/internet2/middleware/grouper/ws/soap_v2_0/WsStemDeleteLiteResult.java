package edu.internet2.middleware.grouper.ws.soap_v2_0;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

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
public class WsStemDeleteLiteResult {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsStemDeleteLiteResult.class);

  /**
   * result code of a request
   */
  public static enum WsStemDeleteLiteResultCode implements WsResultCode {

    /** found the stems, deleted them (lite http status code 200) (success: T) */
    SUCCESS(200),

    /** either overall exception, or one or more stems had exceptions (lite http status code 500) (success: F) */
    EXCEPTION(500),

    /** problem deleting existing stems (lite http status code 500) (success: F) */
    PROBLEM_DELETING_STEMS(500),

    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400), 
    
    /** user not allowed (lite http status code 403) (success: F) */
    INSUFFICIENT_PRIVILEGES(403), 
    
    /** the stem was not found (lite http status code 200) (success: T) */
    SUCCESS_STEM_NOT_FOUND(200);

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
    private WsStemDeleteLiteResultCode(int statusCode) {
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
      return this == SUCCESS || this == SUCCESS_STEM_NOT_FOUND;
    }

  }

  /**
   * empty
   */
  public WsStemDeleteLiteResult() {
    //empty
  }
  
  /**
   * construct from results of other
   * @param wsStemDeleteResults
   */
  public WsStemDeleteLiteResult(WsStemDeleteResults wsStemDeleteResults) {

    this.getResultMetadata().copyFields(wsStemDeleteResults.getResultMetadata());

    WsStemDeleteResult wsStemDeleteResult = GrouperServiceUtils
        .firstInArrayOfOne(wsStemDeleteResults.getResults());
    if (wsStemDeleteResult != null) {
      this.getResultMetadata().copyFields(wsStemDeleteResult.getResultMetadata());
      this.getResultMetadata().assignResultCode(
          wsStemDeleteResult.resultCode().convertToLiteCode());
      this.setWsStem(wsStemDeleteResult.getWsStem());
    }
  }

  /**
   * assign the code from the enum
   * @param stemsDeleteResultsCode
   */
  public void assignResultCode(WsStemDeleteLiteResultCode stemsDeleteResultsCode) {
    this.getResultMetadata().assignResultCode(stemsDeleteResultsCode);
  }

  /**
   * convert the result code back to enum
   * @return the enum code
   */
  public WsStemDeleteLiteResultCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsStemDeleteLiteResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * stem data 
   */
  private WsStem wsStem;

  /**
   * prcess an exception, log, etc
   * @param wsStemDeleteLiteResultCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsStemDeleteLiteResultCode wsStemDeleteLiteResultCodeOverride, String theError,
      Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsStemDeleteLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsStemDeleteLiteResultCodeOverride, WsStemDeleteLiteResultCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsStemDeleteLiteResultCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsStemDeleteLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsStemDeleteLiteResultCodeOverride, WsStemDeleteLiteResultCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsStemDeleteLiteResultCodeOverride);

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

  /**
   * @return the wsStem
   */
  public WsStem getWsStem() {
    return this.wsStem;
  }

  /**
   * @param wsStem1 the wsStem to set
   */
  public void setWsStem(WsStem wsStem1) {
    this.wsStem = wsStem1;
  }

}
