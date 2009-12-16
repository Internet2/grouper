package edu.internet2.middleware.grouper.ws.soap;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;

/**
 * <pre>
 * results for the get memberships call, or the get memberships lite call
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * GROUP_NOT_FOUND: cant find the group
 * INVALID_QUERY: bad inputs
 * EXCEPTION: something bad happened
 * </pre>
 * @author mchyzer
 */
public class WsGetMembershipsResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsGetMembershipsResults.class);

  /**
   * result code of a request
   */
  public static enum WsGetMembershipsResultsCode implements WsResultCode {

    /** found the subject (lite http status code 200) (success: T) */
    SUCCESS(200),

    /** bad input (lite http status code 500) (success: F) */
    INVALID_QUERY(500),

    /** something bad happened (lite http status code 500) (success: F) */
    EXCEPTION(500);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name
     */
    public String nameForVersion(GrouperWsVersion clientVersion) {
      return this.name();
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsGetMembershipsResultsCode(int statusCode) {
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
      return this == SUCCESS;
    }
  }

  /**
   * assign the code from the enum
   * @param getMembersResultCode
   */
  public void assignResultCode(WsGetMembershipsResultsCode getMembersResultCode) {
    this.getResultMetadata().assignResultCode(getMembersResultCode);
  }

  /**
   * results for each assignment sent in
   */
  private WsMembership[] wsMemberships;

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * groups that are in the results
   */
  private WsGroup[] wsGroups;

  /**
   * subjects that are in the results
   */
  private WsSubject[] wsSubjects;

  /**
   * subjects that are in the results
   * @return the subjects
   */
  public WsSubject[] getWsSubjects() {
    return this.wsSubjects;
  }

  /**
   * subjects that are in the results
   * @param wsSubjects1
   */
  public void setWsSubjects(WsSubject[] wsSubjects1) {
    this.wsSubjects = wsSubjects1;
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
   * results for each assignment sent in
   * @return the results
   */
  public WsMembership[] getWsMemberships() {
    return this.wsMemberships;
  }

  /**
   * results for each assignment sent in
   * @param results1 the results to set
   */
  public void setWsMemberships(WsMembership[] results1) {
    this.wsMemberships = results1;
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
   * @return the wsGroups
   */
  public WsGroup[] getWsGroups() {
    return this.wsGroups;
  }

  /**
   * @param wsGroup1 the wsGroups to set
   */
  public void setWsGroups(WsGroup[] wsGroup1) {
    this.wsGroups = wsGroup1;
  }

  /**
   * convert members to subject results
   * @param membershipSet
   * @param returnedGroups
   * @param returnedMembers
   */
  public void assignSubjectResult(Set<Object[]> membershipSet, Set<Group> returnedGroups, Set<Member> returnedMembers) {
    this.setWsMemberships(WsMembership.convertMembers(membershipSet, returnedGroups, returnedMembers));
  }


  /**
   * process an exception, log, etc
   * @param wsGetMembershipsResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsGetMembershipsResultsCode wsGetMembershipsResultsCodeOverride, String theError,
      Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsGetMembershipsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetMembershipsResultsCodeOverride, WsGetMembershipsResultsCode.INVALID_QUERY);

      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsGetMembershipsResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsGetMembershipsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetMembershipsResultsCodeOverride, WsGetMembershipsResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsGetMembershipsResultsCodeOverride);

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

}
