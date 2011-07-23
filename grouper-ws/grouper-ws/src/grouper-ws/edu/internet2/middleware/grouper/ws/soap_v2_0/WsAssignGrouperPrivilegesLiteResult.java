/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsSubjectLookup.SubjectFindResult;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * Result of assigning or removing a privilege
 * 
 * @author mchyzer
 */
public class WsAssignGrouperPrivilegesLiteResult implements WsResponseBean, ResultMetadataHolder {

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * construct from results of other
   * @param wsAssignGrouperPrivilegesResults
   * @param clientVersion 
   */
  public WsAssignGrouperPrivilegesLiteResult(WsAssignGrouperPrivilegesResults wsAssignGrouperPrivilegesResults) {

    this.getResultMetadata().copyFields(wsAssignGrouperPrivilegesResults.getResultMetadata());
    this.setSubjectAttributeNames(wsAssignGrouperPrivilegesResults.getSubjectAttributeNames());
    this.setWsGroup(wsAssignGrouperPrivilegesResults.getWsGroup());
    this.setWsStem(wsAssignGrouperPrivilegesResults.getWsStem());

    WsAssignGrouperPrivilegesResult wsAssignGrouperPrivilegesResult = GrouperServiceUtils
        .firstInArrayOfOne(wsAssignGrouperPrivilegesResults.getResults());
    if (wsAssignGrouperPrivilegesResult != null) {
      this.getResultMetadata().copyFields(wsAssignGrouperPrivilegesResult.getResultMetadata());
      this.getResultMetadata().assignResultCode(
          wsAssignGrouperPrivilegesResult.resultCode().convertToLiteCode());
      this.setWsSubject(wsAssignGrouperPrivilegesResult.getWsSubject());
      this.setAllowed(wsAssignGrouperPrivilegesResult.getAllowed());
      this.setPrivilegeName(wsAssignGrouperPrivilegesResult.getPrivilegeName());
      this.setPrivilegeType(wsAssignGrouperPrivilegesResult.getPrivilegeType());
    }
  }

  /**
   * assign the code from the enum
   * @param wsSubjectLookup1
   * @param theSubjectAttributeNames
   */
  public void processSubject(WsSubjectLookup wsSubjectLookup1,
      String[] theSubjectAttributeNames) {

    SubjectFindResult subjectFindResult = wsSubjectLookup1.retrieveSubjectFindResult();

    switch (subjectFindResult) {
      case INVALID_QUERY:
        this.assignResultCode(WsAssignGrouperPrivilegesLiteResultCode.INVALID_QUERY);
        break;
      case SOURCE_UNAVAILABLE:
        this.assignResultCode(WsAssignGrouperPrivilegesLiteResultCode.EXCEPTION);
        break;
      case SUBJECT_DUPLICATE:
        this.assignResultCode(WsAssignGrouperPrivilegesLiteResultCode.SUBJECT_DUPLICATE);
        break;
      case SUBJECT_NOT_FOUND:
        this.assignResultCode(WsAssignGrouperPrivilegesLiteResultCode.SUBJECT_NOT_FOUND);
        break;
      case SUCCESS:
        return;
    }

    this.getResultMetadata().setResultMessage(
        "Subject: " + wsSubjectLookup1 + " had problems: " + subjectFindResult);

  }

  /**
   * empty
   */
  public WsAssignGrouperPrivilegesLiteResult() {
    //empty
  }

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(WsAssignGrouperPrivilegesLiteResult.class);


  /**
   * prcess an exception, log, etc
   * @param wsMemberChangeSubjectLiteResultCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsAssignGrouperPrivilegesLiteResultCode wsMemberChangeSubjectLiteResultCodeOverride, 
      String theError, Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsMemberChangeSubjectLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsMemberChangeSubjectLiteResultCodeOverride, WsAssignGrouperPrivilegesLiteResultCode.INVALID_QUERY);
      if (e.getCause() instanceof StemNotFoundException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsAssignGrouperPrivilegesLiteResultCode.STEM_NOT_FOUND;
      }
      if (e.getCause() instanceof GroupNotFoundException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsAssignGrouperPrivilegesLiteResultCode.GROUP_NOT_FOUND;
      }
      if (e.getCause() instanceof SubjectNotFoundException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsAssignGrouperPrivilegesLiteResultCode.SUBJECT_NOT_FOUND;
      }
      if (e.getCause() instanceof SubjectNotUniqueException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsAssignGrouperPrivilegesLiteResultCode.SUBJECT_DUPLICATE;
      }
      if (e.getCause() instanceof InsufficientPrivilegeException) {
        wsMemberChangeSubjectLiteResultCodeOverride = WsAssignGrouperPrivilegesLiteResultCode.INSUFFICIENT_PRIVILEGES;
      }
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsMemberChangeSubjectLiteResultCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsMemberChangeSubjectLiteResultCodeOverride = GrouperUtil.defaultIfNull(
          wsMemberChangeSubjectLiteResultCodeOverride, WsAssignGrouperPrivilegesLiteResultCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsMemberChangeSubjectLiteResultCodeOverride);

    }
  }

  /**
   * assign the code from the enum
   * @param memberChangeSubjectLiteResultCode1
   */
  public void assignResultCode(WsAssignGrouperPrivilegesLiteResultCode memberChangeSubjectLiteResultCode1) {
    this.getResultMetadata().assignResultCode(memberChangeSubjectLiteResultCode1);
  }

  /**
    * metadata about the result
    */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * result code of a request
   */
  public static enum WsAssignGrouperPrivilegesLiteResultCode implements WsResultCode {

    /** made the update to allow (rest http status code 200) (success: T) */
    SUCCESS_ALLOWED(200),

    /** privilege allow already existed (rest http status code 200) (success: T) */
    SUCCESS_ALLOWED_ALREADY_EXISTED(200),

    /** made the update to deny (rest http status code 200) (success: T) */
    SUCCESS_NOT_ALLOWED(200),

    /** made the update to deny the immediate privilege, though the user still has an effective privilege, so is still allowed (rest http status code 200) (success: T) */
    SUCCESS_NOT_ALLOWED_EXISTS_EFFECTIVE(200),

    /** privilege deny already existed (rest http status code 200) (success: T) */
    SUCCESS_NOT_ALLOWED_DIDNT_EXIST(200),

    /** privilege deny already existed (rest http status code 200) (success: T) */
    SUCCESS_NOT_ALLOWED_DIDNT_EXIST_BUT_EXISTS_EFFECTIVE(200),

    /** some exception occurred (rest http status code 500) (success: F) */
    EXCEPTION(500),

    /** if one request, and that is a duplicate (rest http status code 409) (success: F) */
    SUBJECT_DUPLICATE(409),

    /** if one request, and that is a subject not found (rest http status code 404) (success: F) */
    SUBJECT_NOT_FOUND(404),

    /** cant find group (rest http status code 404) (success: F) */
    GROUP_NOT_FOUND(404),

    /** cant find stem (rest http status code 404) (success: F) */
    STEM_NOT_FOUND(404),

    /** cant find type (rest http status code 404) (success: F) */
    TYPE_NOT_FOUND(404),

    /** cant find name (rest http status code 404) (success: F) */
    NAME_NOT_FOUND(404),

    /** if one request, and that is a insufficient privileges (rest http status code 403) (success: F) */
    INSUFFICIENT_PRIVILEGES(403),

    /** invalid query (e.g. if everything blank) (rest http status code 400) (success: F) */
    INVALID_QUERY(400);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /**
     * if this is a successful result
     * 
     * @return true if success
     */
    public boolean isSuccess() {
      return this.name().startsWith("SUCCESS");
    }

    /** http status code for rest/lite e.g. 200 */
    private int httpStatusCode;

    /**
     * status code for rest/lite e.g. 200
     * @param statusCode
     */
    private WsAssignGrouperPrivilegesLiteResultCode(int statusCode) {
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
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
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
  private WsResponseMeta responseMetadata = new WsResponseMeta();

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
   * field 
   */
  private WsParam[] params;

  /**
   * whether this privilege is allowed T/F 
   */
  private String allowed;

  /**
   * privilege name, e.g. read, update, stem 
   */
  private String privilegeName;

  /**
   * privilege type, e.g. naming, or access 
   */
  private String privilegeType;

  /**
   * group querying 
   */
  private WsGroup wsGroup;

  /**
   * stem querying 
   */
  private WsStem wsStem;

  /**
   * subject to switch to
   */
  private WsSubject wsSubject;


  /**
   * @return the params
   */
  public WsParam[] getParams() {
    return this.params;
  }

  /**
   * @param params1 the params to set
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

  /**
   * whether this privilege is allowed T/F
   * @return if allowed
   */
  public String getAllowed() {
    return this.allowed;
  }

  /**
   * privilege type, e.g. naming, or access
   * @return the name
   */
  public String getPrivilegeName() {
    return this.privilegeName;
  }

  /**
   * privilege type, e.g. naming, or access
   * @return the type
   */
  public String getPrivilegeType() {
    return this.privilegeType;
  }

  /**
   * group querying
   * @return the group
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * stem querying
   * @return the stem
   */
  public WsStem getWsStem() {
    return this.wsStem;
  }

  /**
   * subject that was changed to
   * @return the subjectId
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  /**
   * whether this privilege is allowed T/F
   * @param allowed1
   */
  public void setAllowed(String allowed1) {
    this.allowed = allowed1;
  }

  /**
   * privilege type, e.g. naming, or access
   * @param privilegeName1
   */
  public void setPrivilegeName(String privilegeName1) {
    this.privilegeName = privilegeName1;
  }

  /**
   * privilege type, e.g. naming, or access
   * @param privilegeType1
   */
  public void setPrivilegeType(String privilegeType1) {
    this.privilegeType = privilegeType1;
  }

  /**
   * group querying
   * @param wsGroup1
   */
  public void setWsGroup(WsGroup wsGroup1) {
    this.wsGroup = wsGroup1;
  }

  /**
   * stem querying
   * @param wsStem1
   */
  public void setWsStem(WsStem wsStem1) {
    this.wsStem = wsStem1;
  }

  /**
   * subject that was changed to
   * @param wsSubject1 the wsSubject1 to set
   */
  public void setWsSubject(WsSubject wsSubject1) {
    this.wsSubject = wsSubject1;
  }
}
