package edu.internet2.middleware.grouper.ws.coresoap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * 
 * @author vsachdeva
 *
 */
public class WsGetAuditEntriesResults implements WsResponseBean, ResultMetadataHolder {
  
  /**
   * result code of a request
   */
  public static enum WsGetAuditEntriesResultsCode implements WsResultCode {

    /** found the audit entries (http status code 200) (success: T) */
    SUCCESS(200),

    /** problem (http status code 500) (success: F) */
    EXCEPTION(500),

    /** problem (http status code 400) (success: F) */
    INVALID_QUERY(400),

    /** couldnt find the member to query (http status code 404) (success: F) */
    MEMBER_NOT_FOUND(404),

    /** couldnt find the subject to query (http status code 404) (success: F) */
    SUBJECT_NOT_FOUND(404);

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name for version */
    @Override
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsGetAuditEntriesResultsCode(int statusCode) {
      this.httpStatusCode = statusCode;
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.WsResultCode#getHttpStatusCode()
     */
    @Override
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }

    /**
     * if this is a successful result
     * @return true if success
     */
    @Override
    public boolean isSuccess() {
      return this == SUCCESS;
    }

  }
  
  /**
   * assign the code from the enum
   * 
   * @param getAuditEntriesResultsCode
   */
  public void assignResultCode(WsGetAuditEntriesResultsCode getAuditEntriesResultsCode) {
    this.getResultMetadata().assignResultCode(getAuditEntriesResultsCode);
  }

  /**
   * convert the result code back to enum
   * 
   * @return the enum code
   */
  public WsGetAuditEntriesResultsCode retrieveResultCode() {
    if (StringUtils.isBlank(this.getResultMetadata().getResultCode())) {
      return null;
    }
    return WsGetAuditEntriesResultsCode.valueOf(this.getResultMetadata().getResultCode());
  }
  
  /**
   * 
   */
  private WsAuditEntry[] wsAuditEntries;

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsGetAuditEntriesResults.class);

  
  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();
  
  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();
  
  
  /**
   * @return metadata about the result
   */
  @Override
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * metadata about the result
   * @param resultMetadata1
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  /**
   * @return metadata about the result
   */
  @Override
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  /**
   * 
   * @param responseMetadata1
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }
  

  /**
   * @return array of audit entries for response
   */
  public WsAuditEntry[] getWsAuditEntries() {
    return this.wsAuditEntries;
  }

  /**
   * set array of audit entries for response
   * @param wsAuditEntries1
   */
  public void setWsAuditEntries(WsAuditEntry[] wsAuditEntries1) {
    this.wsAuditEntries = wsAuditEntries1;
  }

  /**
   * prcess an exception, log, etc
   * @param WsGetAuditEntriesResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsGetAuditEntriesResultsCode WsGetAuditEntriesResultsCodeOverride, String theError, Exception e) {

    if (e instanceof WsInvalidQueryException) {
      WsGetAuditEntriesResultsCodeOverride = GrouperUtil.defaultIfNull(
          WsGetAuditEntriesResultsCodeOverride, WsGetAuditEntriesResultsCode.INVALID_QUERY);
      if (e.getCause() instanceof SubjectNotFoundException) {
        WsGetAuditEntriesResultsCodeOverride = WsGetAuditEntriesResultsCode.SUBJECT_NOT_FOUND;
      } else if (e.getCause() instanceof MemberNotFoundException) {
        WsGetAuditEntriesResultsCodeOverride = WsGetAuditEntriesResultsCode.MEMBER_NOT_FOUND;
      }
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(WsGetAuditEntriesResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      WsGetAuditEntriesResultsCodeOverride = GrouperUtil.defaultIfNull(
          WsGetAuditEntriesResultsCodeOverride, WsGetAuditEntriesResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(WsGetAuditEntriesResultsCodeOverride);

    }
  }
  

}
