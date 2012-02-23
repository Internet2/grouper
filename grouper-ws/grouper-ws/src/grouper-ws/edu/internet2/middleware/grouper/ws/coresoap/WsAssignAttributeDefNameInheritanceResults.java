package edu.internet2.middleware.grouper.ws.coresoap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;



/**
 * returned from the attribute def name inheritance
 * 
 * @author mchyzer
 * 
 */
public class WsAssignAttributeDefNameInheritanceResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * result code of a request.  The possible result codes 
   * of WsAssignAttributeDefNameInheritanceResultsCode (with http status codes) are:
   * SUCCESS(201), EXCEPTION(500), INVALID_QUERY(400), INSUFFICIENT_PRIVILEGES(403)
   */
  public static enum WsAssignAttributeDefNameInheritanceResultsCode implements WsResultCode {
  
    /** found the attributeAssignments (lite status code 200) (success: T) */
    SUCCESS(201),
  
    /** something bad happened (lite status code 500) (success: F) */
    EXCEPTION(500),
  
    /** invalid query (e.g. if everything blank) (lite status code 400) (success: F) */
    INVALID_QUERY(400),
    
    /** 
     * not allowed to assign or remove attribute def name inheritance based on privileges on the attribute definition
     */
    INSUFFICIENT_PRIVILEGES(403);
    
    
    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name
     */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }
  
    /**
     * construct with http code
     * @param theHttpStatusCode the code
     */
    private WsAssignAttributeDefNameInheritanceResultsCode(int theHttpStatusCode) {
      this.httpStatusCode = theHttpStatusCode;
    }
  
    /** http status code for result code */
    private int httpStatusCode;
  
    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }
  
    /** get the http result code for this status code
     * @return the status code
     */
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }

  }

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsAssignAttributeDefNameInheritanceResults.class);

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

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
   * prcess an exception, log, etc
   * @param wsAssignAttributeDefNameInheritanceResultsOverrideCode
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsAssignAttributeDefNameInheritanceResultsCode wsAssignAttributeDefNameInheritanceResultsOverrideCode, 
      String theError,
      Exception e) {
  
    if (e instanceof WsInvalidQueryException) {
      wsAssignAttributeDefNameInheritanceResultsOverrideCode = GrouperUtil.defaultIfNull(
          wsAssignAttributeDefNameInheritanceResultsOverrideCode, WsAssignAttributeDefNameInheritanceResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsAssignAttributeDefNameInheritanceResultsOverrideCode);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else if (e instanceof InsufficientPrivilegeException ) {
      wsAssignAttributeDefNameInheritanceResultsOverrideCode = GrouperUtil.defaultIfNull(
          wsAssignAttributeDefNameInheritanceResultsOverrideCode, WsAssignAttributeDefNameInheritanceResultsCode.INSUFFICIENT_PRIVILEGES);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsAssignAttributeDefNameInheritanceResultsOverrideCode);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);
  

    } else {
      wsAssignAttributeDefNameInheritanceResultsOverrideCode = GrouperUtil.defaultIfNull(
          wsAssignAttributeDefNameInheritanceResultsOverrideCode, WsAssignAttributeDefNameInheritanceResultsCode.EXCEPTION);
      LOG.error(theError, e);
  
      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsAssignAttributeDefNameInheritanceResultsOverrideCode);
  
    }
  }

  /**
   * assign the code from the enum
   * @param wsAssignAttributeDefNameInheritanceResultsCode
   */
  public void assignResultCode(WsAssignAttributeDefNameInheritanceResultsCode wsAssignAttributeDefNameInheritanceResultsCode) {
    this.getResultMetadata().assignResultCode(wsAssignAttributeDefNameInheritanceResultsCode);
  }

}
