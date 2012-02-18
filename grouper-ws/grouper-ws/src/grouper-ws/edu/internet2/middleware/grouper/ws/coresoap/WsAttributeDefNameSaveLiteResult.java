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
 * results for the AttributeDefNames save call.
 * 
 * result code:
 * code of the result for this AttributeDefName overall
 * SUCCESS: means everything ok
 * ATTRIBUTE_DEF_NAME_NOT_FOUND: cant find the AttributeDefName
 * ATTRIBUTE_DEF_NAME_DUPLICATE: found multiple AttributeDefNames
 * </pre>
 * @author mchyzer
 */
public class WsAttributeDefNameSaveLiteResult {

  /**
   * result code of a request
   */
  public static enum WsAttributeDefNameSaveLiteResultCode implements WsResultCode {
  
    /** didnt find the attribute def names, inserted them (lite http status code 201) (success: T) */
    SUCCESS_INSERTED(201),
  
    /** found the attribute def names, saved them (lite http status code 201) (success: T) */
    SUCCESS_UPDATED(201),
  
    /** found the attribute def names, no changes needed (lite http status code 201) (success: T) */
    SUCCESS_NO_CHANGES_NEEDED(201),
  
    /** either overall exception, or one or more attribute def names had exceptions (lite http status code 500) (success: F) */
    EXCEPTION(500),
  
    /** problem, attribute def name already exists (lite http status code 500) (success: F) */
    ATTRIBUTE_DEF_NAME_ALREADY_EXISTS(500),
    
    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400), 
    
    /** the group was not found  (lite http status code 404) (success: F) */
    ATTRIBUTE_DEF_NAME_NOT_FOUND(404), 
    
    /** user not allowed (lite http status code 403) (success: F) */
    INSUFFICIENT_PRIVILEGES(403), 
    
    /** the stem was not found  (lite http status code 404) (success: F) */
    STEM_NOT_FOUND(404);
  
    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name
     */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }
  
    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this.name().startsWith("SUCCESS");
    }
  
    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;
  
    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsAttributeDefNameSaveLiteResultCode(int statusCode) {
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
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * AttributeDefName saved 
   */
  private WsAttributeDefName wsAttributeDefName;

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsAttributeDefNameSaveLiteResult.class);

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
   * @param wsAttributeDefName1 the wsAttributeDefName to set
   */
  public void setWsAttributeDefName(WsAttributeDefName wsAttributeDefName1) {
    this.wsAttributeDefName = wsAttributeDefName1;
  }

  /**
   * assign the code from the enum
   * @param groupSaveResultsCode
   * @param clientVersion 
   */
  public void assignResultCode(WsAttributeDefNameSaveLiteResultCode groupSaveResultsCode, GrouperVersion clientVersion) {
    this.getResultMetadata().assignResultCode(groupSaveResultsCode, clientVersion);
  }

  /**
   * prcess an exception, log, etc
   * @param wsAttributeDefNameSaveResultsCodeOverride
   * @param theError
   * @param e
   * @param clientVersion
   */
  public void assignResultCodeException(
      WsAttributeDefNameSaveLiteResultCode wsAttributeDefNameSaveResultsCodeOverride, String theError, Exception e, GrouperVersion clientVersion) {
  
    if (e instanceof WsInvalidQueryException) {
      wsAttributeDefNameSaveResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsAttributeDefNameSaveResultsCodeOverride, WsAttributeDefNameSaveLiteResultCode.INVALID_QUERY);
      //      if (e.getCause() instanceof AttributeDefNameNotFoundException) {
      //        wsAttributeDefNameSaveResultsCodeOverride = WsAttributeDefNameSaveResultsCode.GROUP_NOT_FOUND;
      //      }
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsAttributeDefNameSaveResultsCodeOverride, clientVersion);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);
  
    } else {
      wsAttributeDefNameSaveResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsAttributeDefNameSaveResultsCodeOverride, WsAttributeDefNameSaveLiteResultCode.EXCEPTION);
      LOG.error(theError, e);
  
      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsAttributeDefNameSaveResultsCodeOverride, clientVersion);
  
    }
  }

  /**
   * convert the result code back to enum
   * @return the enum code
   */
  public WsAttributeDefNameSaveLiteResultCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsAttributeDefNameSaveLiteResultCode.valueOf(this.getResultMetadata().getResultCode());
  }

  /**
   * empty
   */
  public WsAttributeDefNameSaveLiteResult() {
    //empty
  }

  /**
   * construct from results of other
   * @param wsAttributeDefNameSaveResults
   */
  public WsAttributeDefNameSaveLiteResult(WsAttributeDefNameSaveResults wsAttributeDefNameSaveResults) {
  
    this.getResultMetadata().copyFields(wsAttributeDefNameSaveResults.getResultMetadata());
  
    WsAttributeDefNameSaveResult wsAttributeDefNameSaveResult = GrouperServiceUtils
        .firstInArrayOfOne(wsAttributeDefNameSaveResults.getResults());
    if (wsAttributeDefNameSaveResult != null) {
      this.getResultMetadata().copyFields(wsAttributeDefNameSaveResult.getResultMetadata());
  
      this.getResultMetadata().assignResultCode(
          wsAttributeDefNameSaveResult.resultCode().convertToLiteCode());
      this.setWsAttributeDefName(wsAttributeDefNameSaveResult.getWsAttributeDefName());
    }
  }

}
