package edu.internet2.middleware.grouper.ws.coresoap;

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
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * <pre>
 * results for the groups delete call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * GROUP_NOT_FOUND: cant find the group
 * GROUP_DUPLICATE: found multiple groups
 * </pre>
 * @author mchyzer
 */
public class WsGroupDeleteLiteResult implements WsResponseBean, ResultMetadataHolder {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsGroupDeleteLiteResult.class);

  /**
   * result code of a request
   */
  public static enum WsGroupDeleteLiteResultCode implements WsResultCode {

    /** found the groups, deleted them (lite status code 200) (success: T) */
    SUCCESS(200),

    /** either overall exception, or one or more groups had exceptions (lite status code 500) (success: F) */
    EXCEPTION(500),

    /** problem deleting existing groups (lite status code 500) (success: F) */
    PROBLEM_DELETING_GROUPS(500),

    /** invalid query (e.g. if everything blank) (lite status code 400) (success: F) */
    INVALID_QUERY(400), 
    
    /** in group lookup, the uuid doesnt match name (lite status code 400) (success: F) */
    GROUP_UUID_DOESNT_MATCH_NAME(400), 
    
    /** user not allowed  (lite status code 403) (success: F)*/
    INSUFFICIENT_PRIVILEGES(403), 
    
    /** if parent stem cant be found (lite status code 404) (success: F) */
    PARENT_STEM_NOT_FOUND(404), 
    
    /** the group was not found (lite status code 200) (success: F) */
    SUCCESS_GROUP_NOT_FOUND(200);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsGroupDeleteLiteResultCode(int statusCode) {
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
      return this == SUCCESS || this == SUCCESS_GROUP_NOT_FOUND;
    }

  }

  /**
   * empty
   */
  public WsGroupDeleteLiteResult() {
    //empty
  }
  
  /**
   * construct from results of other
   * @param wsGroupDeleteResults
   */
  public WsGroupDeleteLiteResult(WsGroupDeleteResults wsGroupDeleteResults) {

    this.getResultMetadata().copyFields(wsGroupDeleteResults.getResultMetadata());

    WsGroupDeleteResult wsGroupDeleteResult = GrouperServiceUtils
        .firstInArrayOfOne(wsGroupDeleteResults.getResults());
    if (wsGroupDeleteResult != null) {
      this.getResultMetadata().copyFields(wsGroupDeleteResult.getResultMetadata());
      this.getResultMetadata().assignResultCode(
          wsGroupDeleteResult.resultCode().convertToLiteCode());
      this.setWsGroup(wsGroupDeleteResult.getWsGroup());
    }
  }

  
  /**
   * assign the code from the enum
   * @param groupsDeleteResultsCode should not be null
   */
  public void assignResultCode(WsGroupDeleteLiteResultCode groupsDeleteResultsCode) {
    this.getResultMetadata().assignResultCode(groupsDeleteResultsCode);
  }

  /**
   * convert the result code back to enum
   * @return the enum code
   */
  public WsGroupDeleteLiteResultCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsGroupDeleteLiteResultCode.valueOf(this.getResultMetadata().getResultCode());
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
   * group to be deleted
   */
  private WsGroup wsGroup;

  /**
   * prcess an exception, log, etc
   * @param wsGroupDeleteLiteResultCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsGroupDeleteLiteResultCode wsGroupDeleteLiteResultCodeOverride, String theError,
      Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsGroupDeleteLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsGroupDeleteLiteResultCodeOverride, WsGroupDeleteLiteResultCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsGroupDeleteLiteResultCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsGroupDeleteLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsGroupDeleteLiteResultCodeOverride, WsGroupDeleteLiteResultCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsGroupDeleteLiteResultCodeOverride);

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
   * @return the wsGroup
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * @param wsGroupResult1 the wsGroup to set
   */
  public void setWsGroup(WsGroup wsGroupResult1) {
    this.wsGroup = wsGroupResult1;
  }
}
