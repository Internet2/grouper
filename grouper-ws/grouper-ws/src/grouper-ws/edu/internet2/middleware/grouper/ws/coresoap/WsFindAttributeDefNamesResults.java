package edu.internet2.middleware.grouper.ws.coresoap;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;



/**
 * returned from the attribute def name find query
 * 
 * @author mchyzer
 * 
 */
public class WsFindAttributeDefNamesResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * result code of a request
   */
  public static enum WsFindAttributeDefNamesResultsCode implements WsResultCode {
  
    /** found the attribute def names (lite http status code 200) (success: T) */
    SUCCESS(200),
  
    /** problems with operation (lite http status code 500) (success: F) */
    EXCEPTION(500),
  
    /** invalid query (e.g. if everything blank) (lite http status code 400) (success: F) */
    INVALID_QUERY(400);
  
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
    private WsFindAttributeDefNamesResultsCode(int statusCode) {
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
     * 
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }
  }


  /**
   * has 0 to many attribute def names that match the query
   */
  private WsAttributeDefName[] attributeDefNameResults;

  /**
   * has 0 to many attribute defs related to the names that match the query
   */
  private WsAttributeDef[] attributeDefs;

  /**
   * has 0 to many attribute defs related to the names that match the query
   * @return attribute defs
   */
  public WsAttributeDef[] getAttributeDefs() {
    return this.attributeDefs;
  }

  /**
   * has 0 to many attribute defs related to the names that match the query
   * @param attributeDefs1
   */
  public void setAttributeDefs(WsAttributeDef[] attributeDefs1) {
    this.attributeDefs = attributeDefs1;
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsFindAttributeDefNamesResults.class);

  /**
   * has 0 to many attribute def names that match the query by example
   * 
   * @return the attribute def name Results
   */
  public WsAttributeDefName[] getAttributeDefNameResults() {
    return this.attributeDefNameResults;
  }

  /**
   * basic results to the query
   * @param attributeDefNameResults1 the groupResults to set
   */
  public void setAttributeDefNameResults(WsAttributeDefName[] attributeDefNameResults1) {
    this.attributeDefNameResults = attributeDefNameResults1;
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
   * put an attribute def name in the results
   * @param attributeDefName
   */
  public void assignAttributeDefNameResult(AttributeDefName attributeDefName) {
    this.assignAttributeDefNameResult(GrouperUtil.toSet(attributeDefName));
  }

  /**
   * put an attribute def name in the results
   * @param attributeDefNameSet
   */
  public void assignAttributeDefNameResult(Set<AttributeDefName> attributeDefNameSet) {
    this.setAttributeDefNameResults(WsAttributeDefName.convertAttributeDefNames(attributeDefNameSet));
  }

  /**
   * assign the code from the enum
   * 
   * @param wsFindAttributeDefNamesResultsCode
   */
  public void assignResultCode(WsFindAttributeDefNamesResultsCode wsFindAttributeDefNamesResultsCode) {
    this.getResultMetadata().assignResultCode(wsFindAttributeDefNamesResultsCode);
  }

  /**
   * prcess an exception, log, etc
   * @param wsFindAttributeDefNamesResultsCodeOverride 
   * @param wsAddMemberResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsFindAttributeDefNamesResultsCode wsFindAttributeDefNamesResultsCodeOverride, String theError,
      Exception e) {
  
    if (e instanceof WsInvalidQueryException) {
      wsFindAttributeDefNamesResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsFindAttributeDefNamesResultsCodeOverride, WsFindAttributeDefNamesResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsFindAttributeDefNamesResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);
  
    } else {
      wsFindAttributeDefNamesResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsFindAttributeDefNamesResultsCodeOverride, WsFindAttributeDefNamesResultsCode.EXCEPTION);
      LOG.error(theError, e);
  
      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsFindAttributeDefNamesResultsCodeOverride);
  
    }
  }
}
