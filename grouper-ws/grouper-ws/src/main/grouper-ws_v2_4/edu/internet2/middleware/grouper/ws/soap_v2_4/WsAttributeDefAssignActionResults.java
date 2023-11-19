package edu.internet2.middleware.grouper.ws.soap_v2_4;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.WsResultCode;

/**
 * holds an attribute assign actions result.
 * @author vsachdeva
 *
 */
public class WsAttributeDefAssignActionResults {

  /** attribute def to which action(s) are assigned **/
  private WsAttributeDef wsAttributeDef;

  /** actions with operation performed **/
  private WsAttributeDefActionOperationPerformed[] actions;

  /**
   * attribute def to which action(s) are assigned
   * @return wsAttributeDef
   */
  public WsAttributeDef getWsAttributeDef() {
    return this.wsAttributeDef;
  }

  /**
   * attribute def to which action(s) are assigned
   * @param wsAttributeDefs1
   */
  public void setWsAttributeDef(WsAttributeDef wsAttributeDefs1) {
    this.wsAttributeDef = wsAttributeDefs1;
  }

  /**
   * @return actions with operations
   */
  public WsAttributeDefActionOperationPerformed[] getActions() {
    return this.actions;
  }

  /**
   * actions with operations
   * @param actions1
   */
  public void setActions(WsAttributeDefActionOperationPerformed[] actions1) {
    this.actions = actions1;
  }

  /**
    * result code of a request.  The possible result codes 
    * of WsGetMembersResultCode (with http status codes) are:
    * SUCCESS(200), EXCEPTION(500), INVALID_QUERY(400)
   */
  public static enum WsAttributeDefAssignActionsResultsCode implements WsResultCode {

    /** assigned action(s) (status code 200) (success: T) */
    SUCCESS(200),

    /** something bad happened (status code 500) (success: F) */
    EXCEPTION(500),

    /** invalid query (e.g. if everything blank) (status code 400) (success: F) */
    INVALID_QUERY(400),

    /** 
     * insufficient privileges
     */
    INSUFFICIENT_PRIVILEGES(403);

    /** get the name label for a certain version of client 
      * @param clientVersion 
      * @return name
     */
    @Override
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /**
     * construct with http code
     * @param theHttpStatusCode the code
     */
    private WsAttributeDefAssignActionsResultsCode(int theHttpStatusCode) {
      this.httpStatusCode = theHttpStatusCode;
    }

    /** http status code for result code */
    private int httpStatusCode;

    /**
     * if this is a successful result
     * @return true if success
     */
    @Override
    public boolean isSuccess() {
      return this == SUCCESS;
    }

    /** get the http result code for this status code
     * @return the status code
     */
    @Override
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }

    /**
     * do a case-insensitive matching
     * 
     * @param string
     * @param exceptionOnNull will not allow null or blank entries
     * @return the enum or null or exception if not found
     */
    public static WsAttributeDefAssignActionsResultsCode valueOfIgnoreCase(String string,
        boolean exceptionOnNull) {
      return GrouperUtil.enumValueOfIgnoreCase(
          WsAttributeDefAssignActionsResultsCode.class,
          string, exceptionOnNull);

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
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  /**
   * @param responseMetadata1 the responseMetadata to set
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }


  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

}
