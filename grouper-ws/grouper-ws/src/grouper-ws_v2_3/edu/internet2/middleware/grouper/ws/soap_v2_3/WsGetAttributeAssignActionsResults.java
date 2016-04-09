package edu.internet2.middleware.grouper.ws.soap_v2_3;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;

/**
 * returned from the attribute assign actions query 
 * @author vsachdeva
 */
public class WsGetAttributeAssignActionsResults {

  /**
   * logger
  */
  private static final Log LOG = LogFactory.getLog(WsGetAttributeAssignActionsResults.class);
	
  /**
   * has 0 to many tuples that match the query
   */
  private WsAttributeAssignActionTuple[] wsAttributeAssignActionTuples;
	 
  /**
   * has 0 to many attribute defs related to the names/ids/idIndices that match the query
   */
  private WsAttributeDef[] wsAttributeDefs;

  /**
   * @return assign action tuples
   */
  public WsAttributeAssignActionTuple[] getWsAttributeAssignActionTuples() {
	return this.wsAttributeAssignActionTuples;
  }

  /**
   * has 0 to many tuples that match the query
   * @param wsAttributeAssignActionTuples1
   */
  public void setWsAttributeAssignActionTuples(WsAttributeAssignActionTuple[] wsAttributeAssignActionTuples1) {
	this.wsAttributeAssignActionTuples = wsAttributeAssignActionTuples1;
  }
	
  /**
   * @return 0 to many attribute definitions
   */
  public WsAttributeDef[] getWsAttributeDefs() {
	return this.wsAttributeDefs;
  }

  /**
   * has 0 to many attribute defs related to the names/ids/idIndices that match the query
   * @param wsAttributeDefs1
   */
  public void setWsAttributeDefs(WsAttributeDef[] wsAttributeDefs1) {
	this.wsAttributeDefs = wsAttributeDefs1;
  }



  /**
   * result code of a request. The possible result codes of
   * WsGetMembersResultCode (with http status codes) are: SUCCESS(200),
   * EXCEPTION(500), INVALID_QUERY(400)
  */
  public static enum WsGetAttributeAssignActionsResultsCode implements WsResultCode {

	/** found the attributeAssignActions (lite status code 200) (success: T) */
    SUCCESS(200),

	/** something bad happened (lite status code 500) (success: F) */
	EXCEPTION(500),

	/**
	 * invalid query (e.g. if everything blank) (lite status code 400)
	 * (success: F)
	 */
	INVALID_QUERY(400),

	/**
	 * not allowed due to the privileges on the inputs.
	 */
	INSUFFICIENT_PRIVILEGES(403);

	/**
	 * get the name label for a certain version of client
	 * 
	 * @param clientVersion
	 * @return name
	 */
    @Override
	public String nameForVersion(GrouperVersion clientVersion) {
		return this.name();
	}

	/**
	 * construct with http code
	 * 
	 * @param theHttpStatusCode
	 *            the code
	 */
	private WsGetAttributeAssignActionsResultsCode(int theHttpStatusCode) {
		this.httpStatusCode = theHttpStatusCode;
	}

	/** http status code for result code */
	private int httpStatusCode;

	/**
	 * if this is a successful result
	 * 
	 * @return true if success
	 */
	@Override
	public boolean isSuccess() {
		return this == SUCCESS;
	}

	/**
	 * get the http result code for this status code
	 * 
	 * @return the status code
	 */
	@Override
	public int getHttpStatusCode() {
		return this.httpStatusCode;
	}
  }

  /**
   * assign the code from the enum
   * 
   * @param getAttributeAssignmentsResultCode
   */
  public void assignResultCode(WsGetAttributeAssignActionsResultsCode getAttributeAssignmentsResultCode) {
	this.getResultMetadata().assignResultCode(getAttributeAssignmentsResultCode);
  }

  /**
   * process an exception, log, etc
   * 
   * @param wsGetAttributeAssignActionsResultsCodeOverride
   * @param theError
   * @param e
  */
  public void assignResultCodeException( WsGetAttributeAssignActionsResultsCode wsGetAttributeAssignActionsResultsCodeOverride,
	String theError, Exception e) {

	if (e instanceof WsInvalidQueryException) {
		wsGetAttributeAssignActionsResultsCodeOverride = GrouperUtil
				.defaultIfNull(wsGetAttributeAssignActionsResultsCodeOverride,
						WsGetAttributeAssignActionsResultsCode.INVALID_QUERY);
		// a helpful exception will probably be in the getMessage()
		this.assignResultCode(wsGetAttributeAssignActionsResultsCodeOverride);
		this.getResultMetadata().appendResultMessage(e.getMessage());
		this.getResultMetadata().appendResultMessage(theError);
		LOG.warn(e);

	} else {
		wsGetAttributeAssignActionsResultsCodeOverride = GrouperUtil
				.defaultIfNull(wsGetAttributeAssignActionsResultsCodeOverride,
						WsGetAttributeAssignActionsResultsCode.EXCEPTION);
		LOG.error(theError, e);

		theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
		this.getResultMetadata().appendResultMessage(
				theError + ExceptionUtils.getFullStackTrace(e));
		this.assignResultCode(wsGetAttributeAssignActionsResultsCodeOverride);
	}
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
	return this.resultMetadata;
  }

  /**
   * metadata about the result
  */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
  * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
  * @return the response metadata
  */
  public WsResponseMeta getResponseMetadata() {
	return this.responseMetadata;
  }

  /**
   * @param resultMetadata1
   *            the resultMetadata to set
  */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
	this.resultMetadata = resultMetadata1;
  }

  /**
   * @param responseMetadata1
   *            the responseMetadata to set
  */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
	this.responseMetadata = responseMetadata1;
  }

}
