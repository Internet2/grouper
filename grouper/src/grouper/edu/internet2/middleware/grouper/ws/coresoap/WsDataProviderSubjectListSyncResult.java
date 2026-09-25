package edu.internet2.middleware.grouper.ws.coresoap;

import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsAddMemberResults.WsAddMemberResultsCode;
import edu.internet2.middleware.grouper.ws.exceptions.GrouperWsException;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;

public class WsDataProviderSubjectListSyncResult implements WsResponseBean, ResultMetadataHolder {
  
  /**
   * result code of a request
   */
  public static enum WsDataProviderSubjectListSyncResultCode implements WsResultCode {

    /** executed the data provider sync successfully (http status code 200) (success: T) */
    SUCCESS(200),

    /** exception was thrown while running data provider sync (http status code 500) (success: F) */
    EXCEPTION(500),

    /** invalid input (e.g. if everything blank) (http status code 400) (success: F) */
    INVALID_QUERY(400);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

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
    private WsDataProviderSubjectListSyncResultCode(int statusCode) {
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
  
  

  @Override
  public WsResultMeta getResultMetadata() {
    return resultMetadata;
  }

  @Override
  public WsResponseMeta getResponseMetadata() {
    return responseMetadata;
  }
  
  /**
   * assign a resultcode of exception, and process/log the exception
   * @param theSummary
   * @param e
   */
  public void assignResultCodeException(String theSummary, Exception e) {
    if (e instanceof WsInvalidQueryException) {
      this.assignResultCode(WsDataProviderSubjectListSyncResultCode.INVALID_QUERY);
      this.getResultMetadata().appendResultMessageError(e.getMessage());
      this.getResultMetadata().appendResultMessageError(theSummary);
      GrouperWsException.logWarn(theSummary, e);

    } else {
      GrouperWsException.logError(theSummary, e);
      
      this.getResultMetadata().appendResultMessageError(theSummary);
      this.getResultMetadata().appendResultMessageError(e);
      this.assignResultCode(WsDataProviderSubjectListSyncResultCode.EXCEPTION);
    }
  }
  
  /**
   * assign the code from the enum
   * 
   * @param wsDataProviderSubjectListSyncResultCode
   */
  public void assignResultCode(WsDataProviderSubjectListSyncResultCode wsDataProviderSubjectListSyncResultCode) {
    this.getResultMetadata().assignResultCode(wsDataProviderSubjectListSyncResultCode);
  }
}
