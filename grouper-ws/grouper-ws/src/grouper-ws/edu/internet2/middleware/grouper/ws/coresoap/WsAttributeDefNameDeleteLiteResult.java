package edu.internet2.middleware.grouper.ws.coresoap;

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
 * results for the attribute def name delete call.
 * 
 * result code:
 * code of the result for this attribute def name overall
 * SUCCESS: means everything ok
 * ATTRIBUTE_DEF_NAME_NOT_FOUND: cant find the attribute def name
 * </pre>
 * @author mchyzer
 */
public class WsAttributeDefNameDeleteLiteResult {

  /**
   * result code of a request
   */
  public static enum WsAttributeDefNameDeleteLiteResultCode implements WsResultCode {
  
    /** found the attribute def names, deleted them (lite status code 200) (success: T) */
    SUCCESS(200),
  
    /** either overall exception, or one or more attribute def names had exceptions (lite status code 500) (success: F) */
    EXCEPTION(500),
  
    /** problem deleting existing attribute def names (lite status code 500) (success: F) */
    PROBLEM_DELETING_ATTRIBUTE_DEF_NAMES(500),
  
    /** invalid query (e.g. if everything blank) (lite status code 400) (success: F) */
    INVALID_QUERY(400), 
    
    /** in attribute def name lookup, the uuid doesnt match name (lite status code 400) (success: F) */
    ATTRIBUTE_DEF_NAME_UUID_DOESNT_MATCH_NAME(400), 
    
    /** user not allowed  (lite status code 403) (success: F)*/
    INSUFFICIENT_PRIVILEGES(403), 
    
    /** if parent stem cant be found (lite status code 404) (success: F) */
    PARENT_STEM_NOT_FOUND(404), 
    
    /** the parent stem exists but the attribute def name was not found (lite status code 200) (success: T) */
    SUCCESS_ATTRIBUTE_DEF_NAME_NOT_FOUND(200);
  
    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name
     */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }
  
    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;
  
    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsAttributeDefNameDeleteLiteResultCode(int statusCode) {
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
      return this == SUCCESS || this == SUCCESS_ATTRIBUTE_DEF_NAME_NOT_FOUND;
    }
  
  }

  /**
   * empty
   */
  public WsAttributeDefNameDeleteLiteResult() {
    //empty
  }
  
  /**
   * construct from results of other
   * @param wsAttributeDefNameDeleteResults
   */
  public WsAttributeDefNameDeleteLiteResult(WsAttributeDefNameDeleteResults wsAttributeDefNameDeleteResults) {
  
    this.getResultMetadata().copyFields(wsAttributeDefNameDeleteResults.getResultMetadata());
  
    WsAttributeDefNameDeleteResult wsAttributeDefNameDeleteResult = GrouperServiceUtils
        .firstInArrayOfOne(wsAttributeDefNameDeleteResults.getResults());
    if (wsAttributeDefNameDeleteResult != null) {
      this.getResultMetadata().copyFields(wsAttributeDefNameDeleteResult.getResultMetadata());
      this.getResultMetadata().assignResultCode(
          wsAttributeDefNameDeleteResult.resultCode().convertToLiteCode());
      this.setWsAttributeDefName(wsAttributeDefNameDeleteResult.getWsAttributeDefName());
    }
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
   * attribute def name to be deleted
   */
  private WsAttributeDefName wsAttributeDefName;

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsAttributeDefNameDeleteLiteResult.class);

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
   * @return the wsAttributeDefName
   */
  public WsAttributeDefName getWsAttributeDefName() {
    return this.wsAttributeDefName;
  }

  /**
   * @param wsAttributeDefNameResult1 the wsAttributeDefName to set
   */
  public void setWsAttributeDefName(WsAttributeDefName wsAttributeDefNameResult1) {
    this.wsAttributeDefName = wsAttributeDefNameResult1;
  }

  /**
   * assign the code from the enum
   * @param attributeDefNamesDeleteResultsCode should not be null
   */
  public void assignResultCode(WsAttributeDefNameDeleteLiteResultCode attributeDefNamesDeleteResultsCode) {
    this.getResultMetadata().assignResultCode(attributeDefNamesDeleteResultsCode);
  }

  /**
   * prcess an exception, log, etc
   * @param wsAttributeDefNameDeleteLiteResultCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsAttributeDefNameDeleteLiteResultCode wsAttributeDefNameDeleteLiteResultCodeOverride, String theError,
      Exception e) {
  
    if (e instanceof WsInvalidQueryException) {
      wsAttributeDefNameDeleteLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsAttributeDefNameDeleteLiteResultCodeOverride, WsAttributeDefNameDeleteLiteResultCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsAttributeDefNameDeleteLiteResultCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);
  
    } else {
      wsAttributeDefNameDeleteLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsAttributeDefNameDeleteLiteResultCodeOverride, WsAttributeDefNameDeleteLiteResultCode.EXCEPTION);
      LOG.error(theError, e);
  
      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsAttributeDefNameDeleteLiteResultCodeOverride);
  
    }
  }

  /**
   * convert the result code back to enum
   * @return the enum code
   */
  public WsAttributeDefNameDeleteLiteResultCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsAttributeDefNameDeleteLiteResultCode.valueOf(this.getResultMetadata().getResultCode());
  }
}
