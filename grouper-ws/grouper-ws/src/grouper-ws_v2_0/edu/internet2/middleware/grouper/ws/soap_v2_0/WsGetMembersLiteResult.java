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
 * results for the get members lite call.
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * EXCEPTION: something bad happened
 * etc
 * </pre>
 * @author mchyzer
 */
public class WsGetMembersLiteResult {

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsGetMembersLiteResult.class);

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * result code of a request.  The possible result codes 
   * of WsGetMembersResultCode (with http status codes) are:
   * SUCCESS(200), EXCEPTION(500), INVALID_QUERY(400)
   */
  public static enum WsGetMembersLiteResultCode implements WsResultCode {

    /** found the members (lite status code 200) (success: T) */
    SUCCESS(200),

    /** something bad happened (lite status code 500) (success: F) */
    EXCEPTION(500),

    /** something bad happened with some of the member retrieval (lite status code 500) (success: F) */
    PROBLEM_GETTING_MEMBERS(500),

    /** invalid query (e.g. if everything blank) (lite status code 400) (success: F) */
    INVALID_QUERY(400),

    /** cant find group (lite status code 404) (success: F)  */
    GROUP_NOT_FOUND(404),

    /**
     * if there is one result, convert to the results code
     *  (lite status code 400) (success: F) 
     */
    GROUP_UUID_DOESNT_MATCH_NAME(400);
    
    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /**
     * construct with http code
     * @param theHttpStatusCode the code
     */
    private WsGetMembersLiteResultCode(int theHttpStatusCode) {
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
   * assign the code from the enum
   * @param getMembersResultCode
   */
  public void assignResultCode(WsGetMembersLiteResultCode getMembersResultCode) {
    this.getResultMetadata().assignResultCode(getMembersResultCode);
  }

  /**
   * prcess an exception, log, etc
   * @param wsGetMembersResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsGetMembersLiteResultCode wsGetMembersResultsCodeOverride, String theError,
      Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsGetMembersResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetMembersResultsCodeOverride, WsGetMembersLiteResultCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsGetMembersResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsGetMembersResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetMembersResultsCodeOverride, WsGetMembersLiteResultCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsGetMembersResultsCodeOverride);

    }
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @return the attributeNames
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @param attributeNamesa the attributeNames to set
   */
  public void setSubjectAttributeNames(String[] attributeNamesa) {
    this.subjectAttributeNames = attributeNamesa;
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
   * group that we are checking 
   */
  private WsGroup wsGroup;

  /**
   * results for each assignment sent in
   */
  private WsSubject[] wsSubjects;

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
   * @return the wsGroup
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsSubject[] getWsSubjects() {
    return this.wsSubjects;
  }

  /**
   * @param wsGroup1 the wsGroup to set
   */
  public void setWsGroup(WsGroup wsGroup1) {
    this.wsGroup = wsGroup1;
  }

  /**
   * results for each assignment sent in
   * @param results1 the results to set
   */
  public void setWsSubjects(WsSubject[] results1) {
    this.wsSubjects = results1;
  }

  /**
   * empty
   */
  public WsGetMembersLiteResult() {
    //empty
  }

  /**
   * construct from results of other
   * @param wsGetMembersResults
   */
  public WsGetMembersLiteResult(WsGetMembersResults wsGetMembersResults) {
  
    this.getResultMetadata().copyFields(wsGetMembersResults.getResultMetadata());
    this.setSubjectAttributeNames(wsGetMembersResults.getSubjectAttributeNames());

    WsGetMembersResult wsGetMembersResult = GrouperServiceUtils
        .firstInArrayOfOne(wsGetMembersResults.getResults());
    if (wsGetMembersResult != null) {
      this.getResultMetadata().copyFields(wsGetMembersResult.getResultMetadata());
      this.getResultMetadata().assignResultCode(
          wsGetMembersResult.resultCode().convertToLiteCode());
      this.setWsSubjects(wsGetMembersResult.getWsSubjects());
      this.setWsGroup(wsGetMembersResult.getWsGroup());
    }
  }

}
