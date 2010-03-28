package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;

/**
 * <pre>
 * results for the get attributeAssignments call.
 * 
 * result code:
 * code of the result for this attribute assignment overall
 * SUCCESS: means everything ok
 * ATTRIBUTE_DEF_NAME_NOT_FOUND: cant find the group
 * ATTRIBUTE_DEF_NOT_FOUND: cant find the group
 * INVALID_QUERY: bad inputs
 * EXCEPTION: something bad happened
 * </pre>
 * @author mchyzer
 */
public class WsGetAttributeAssignmentsResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsGetAttributeAssignmentsResults.class);

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * result code of a request.  The possible result codes 
   * of WsGetMembersResultCode (with http status codes) are:
   * SUCCESS(200), EXCEPTION(500), INVALID_QUERY(400)
   */
  public static enum WsGetAttributeAssignmentsResultsCode implements WsResultCode {

    /** found the attributeAssignments (lite status code 200) (success: T) */
    SUCCESS(200),

    /** something bad happened (lite status code 500) (success: F) */
    EXCEPTION(500),

    /** invalid query (e.g. if everything blank) (lite status code 400) (success: F) */
    INVALID_QUERY(400),
    
    /** not allowed to the privileges on the inputs.  Note if broad search, then the results wont
     * contain items not allowed.  If a specific search e.g. on a group, then if you cant read the
     * group then you cant read the privs
     */
    INSUFFICIENT_PRIVILEGES(403);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name
     */
    public String nameForVersion(GrouperWsVersion clientVersion) {
      return this.name();
    }

    /**
     * construct with http code
     * @param theHttpStatusCode the code
     */
    private WsGetAttributeAssignmentsResultsCode(int theHttpStatusCode) {
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
   * @param getAttributeAssignmentsResultCode
   */
  public void assignResultCode(WsGetAttributeAssignmentsResultsCode getAttributeAssignmentsResultCode) {
    this.getResultMetadata().assignResultCode(getAttributeAssignmentsResultCode);
  }

  /**
   * prcess an exception, log, etc
   * @param wsGetAttributeAssignmentsResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsGetAttributeAssignmentsResultsCode wsGetAttributeAssignmentsResultsCodeOverride, String theError,
      Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsGetAttributeAssignmentsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetAttributeAssignmentsResultsCodeOverride, WsGetAttributeAssignmentsResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsGetAttributeAssignmentsResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsGetAttributeAssignmentsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetAttributeAssignmentsResultsCodeOverride, WsGetAttributeAssignmentsResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsGetAttributeAssignmentsResultsCodeOverride);

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
   * groups that are in the results
   */
  private WsGroup[] wsGroups;

  /**
   * stems that are in the results
   */
  private WsStem[] wsStems;

  /**
   * stems that are in the results
   * @return stems
   */
  public WsStem[] getWsStems() {
    return this.wsStems;
  }

  /**
   * stems that are in the results
   * @param wsStems1
   */
  public void setWsStems(WsStem[] wsStems1) {
    this.wsStems = wsStems1;
  }

  /**
   * results for each assignment sent in
   */
  private WsMembership[] wsMemberships;

  /**
   * subjects that are in the results
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
   * @return the wsGroups
   */
  public WsGroup[] getWsGroups() {
    return this.wsGroups;
  }

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsMembership[] getWsMemberships() {
    return this.wsMemberships;
  }

  /**
   * subjects that are in the results
   * @return the subjects
   */
  public WsSubject[] getWsSubjects() {
    return this.wsSubjects;
  }

  /**
   * @param wsGroup1 the wsGroups to set
   */
  public void setWsGroups(WsGroup[] wsGroup1) {
    this.wsGroups = wsGroup1;
  }

  /**
   * results for each assignment sent in
   * @param results1 the results to set
   */
  public void setWsMemberships(WsMembership[] results1) {
    this.wsMemberships = results1;
  }

  /**
   * subjects that are in the results
   * @param wsSubjects1
   */
  public void setWsSubjects(WsSubject[] wsSubjects1) {
    this.wsSubjects = wsSubjects1;
  }

}
