/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.ws.soap.WsSubjectLookup.SubjectFindResult;
import edu.internet2.middleware.subject.Subject;

/**
 * Result of seeing if one subject is a member of a group.  The number of
 * subjects will equal the number of subjects sent in to the method
 * 
 * @author mchyzer
 */
public class WsHasMemberResult {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsHasMemberResult.class);

  /** the subject that was looked up */
  private WsSubjectLookup subjectLookup;

  /** sujbect info for hasMember */
  private WsSubject wsSubject;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * @return the wsSubject
   */
  public WsSubject getWsSubjectResult() {
    return this.wsSubject;
  }

  /**
   * @param wsSubjectResult1 the wsSubject to set
   */
  public void setWsSubjectResult(WsSubject wsSubjectResult1) {
    this.wsSubject = wsSubjectResult1;
  }

  /** empty constructor */
  public WsHasMemberResult() {
    //nothing
  }

  /**
   * result based on a subject lookup.  Might set stat codes meaning abort mission
   * @param wsSubjectLookup
   * @param subjectAttributeNamesToRetrieve
   */
  public WsHasMemberResult(WsSubjectLookup wsSubjectLookup,
      String[] subjectAttributeNamesToRetrieve) {

    this.subjectLookup = wsSubjectLookup;

    Subject subject = wsSubjectLookup.retrieveSubject();

    // make sure the subject is there
    if (subject == null) {
      // see why not
      SubjectFindResult subjectFindResult = wsSubjectLookup.retrieveSubjectFindResult();
      String error = "Subject: " + wsSubjectLookup + " had problems: "
          + subjectFindResult;
      this.getResultMetadata().setResultMessage(error);
      if (subjectFindResult != null) {
        this.assignResultCode(subjectFindResult.convertToHasMemberResultCode());
      }
    } else {
      this.wsSubject = new WsSubject(subject, subjectAttributeNamesToRetrieve);
    }
  }

  /**
   * result code of a request
   */
  public enum WsHasMemberResultCode {

    /** found the subject */
    SUCCESS,

    /** found multiple results */
    SUBJECT_DUPLICATE,

    /** cant find the subject */
    SUBJECT_NOT_FOUND,

    /** when the source if not available */
    SOURCE_UNAVAILABLE,

    /** the subject is a member */
    IS_MEMBER,

    /** cant find group */
    GROUP_NOT_FOUND,

    /** the subject was found and is a member */
    IS_NOT_MEMBER,

    /** problem with query */
    EXCEPTION,

    /** invalid query (e.g. if everything blank) */
    INVALID_QUERY,

    /** cant find the member.  note this is not an error, its a false */
    MEMBER_NOT_FOUND;

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == IS_MEMBER || this == IS_NOT_MEMBER || this == MEMBER_NOT_FOUND;
    }
  }

  /**
   * assign the code from the enum
   * @param hasMemberResultCode
   */
  public void assignResultCode(WsHasMemberResultCode hasMemberResultCode) {
    this.getResultMetadata().assignResultCode(
        hasMemberResultCode == null ? null : hasMemberResultCode.name());
    this.getResultMetadata().assignSuccess(hasMemberResultCode.isSuccess() ? "T" : "F");
  }

  /**
   * subject that was looked up
   * @return the subjectLookup
   */
  public WsSubjectLookup getSubjectLookup() {
    return this.subjectLookup;
  }

  /**
   * subject that was looked up
   * @param subjectLookup1 the subjectLookup to set
   */
  public void setSubjectLookup(WsSubjectLookup subjectLookup1) {
    this.subjectLookup = subjectLookup1;
  }

  /**
   * assign a resultcode of exception, and process/log the exception
   * @param e
   * @param wsSubjectLookup
   */
  public void assignResultCodeException(Exception e, WsSubjectLookup wsSubjectLookup) {
    this.assignResultCode(WsHasMemberResultCode.EXCEPTION);
    this.getResultMetadata().setResultMessage(ExceptionUtils.getFullStackTrace(e));
    LOG.error(wsSubjectLookup + ", " + e, e);
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

}
