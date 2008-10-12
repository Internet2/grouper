/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberResult.WsHasMemberResultCode;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * <pre>
 * template to lookup a subject
 * 
 * developers make sure each setter calls this.clearSubject();
 * 
 * </pre>
 * @author mchyzer
 */
public class WsSubjectLookup {

  /** find the subject */
  private Subject subject = null;

  /** if there is an exception in find, list it here */
  private Exception cause = null;

  /**
   * result of a subject find
   *
   */
  public static enum SubjectFindResult {

    /** found the subject */
    SUCCESS {

      /** convert to has member result 
       * @return the has member code
       */
      @Override
      public WsHasMemberResultCode convertToHasMemberResultCode() {
        //shouldnt be converting success
        throw new RuntimeException("Shouldnt be converting success...");
      }
    },

    /** found multiple results */
    SUBJECT_DUPLICATE {

      /** convert to has member result 
       * @return the has member code
       */
      @Override
      public WsHasMemberResultCode convertToHasMemberResultCode() {
        return WsHasMemberResultCode.SUBJECT_DUPLICATE;
      }
    },

    /** cant find the subject */
    SUBJECT_NOT_FOUND {

      /** convert to has member result 
       * @return the has member code
       */
      @Override
      public WsHasMemberResultCode convertToHasMemberResultCode() {
        return WsHasMemberResultCode.SUBJECT_NOT_FOUND;
      }
    },

    /** invalid query (e.g. if everything blank) */
    INVALID_QUERY {

      /** convert to has member result 
       * @return the has member code
       */
      @Override
      public WsHasMemberResultCode convertToHasMemberResultCode() {
        return WsHasMemberResultCode.INVALID_QUERY;
      }
    },

    /** when the source if not available */
    SOURCE_UNAVAILABLE {

      /** convert to has member result 
       * @return the has member code
       */
      @Override
      public WsHasMemberResultCode convertToHasMemberResultCode() {
        return WsHasMemberResultCode.EXCEPTION;
      }
    };

    /** convert to has member result 
     * @return the has member code
     */
    public abstract WsHasMemberResultCode convertToHasMemberResultCode();

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }

  }

  /** result of subject find */
  private SubjectFindResult subjectFindResult = null;

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsSubjectLookup.class);

  /**
   * 
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * see if there is a blank query (if there is not id or identifier
   * @return true or false
   */
  public boolean blank() {
    return StringUtils.isBlank(this.subjectId)
        && StringUtils.isBlank(this.subjectIdentifier);
  }

  /**
   * 
   */
  private void retrieveSubjectIfNeeded() {
    //see if we already retrieved
    if (this.subjectFindResult != null) {
      return;
    }
    try {
      //assume success (set otherwise if ther is a proble
      this.subjectFindResult = SubjectFindResult.SUCCESS;

      boolean hasSubjectId = !StringUtils.isBlank(this.subjectId);
      boolean hasSubjectIdentifier = !StringUtils.isBlank(this.subjectIdentifier);
      boolean hasSubjectSource = !StringUtils.isBlank(this.subjectSourceId);

      //must have an id
      if (!hasSubjectId && !hasSubjectIdentifier) {
        this.subjectFindResult = SubjectFindResult.INVALID_QUERY;
        LOG.warn("Invalid query: " + this);
        return;
      }

      //note this doesnt test if both id and identifier are passed in.  if one, assumes it is valid
      if (hasSubjectId) {

        //cant have source without type
        if (hasSubjectSource) {
          this.subject = SubjectFinder.getSource(this.subjectSourceId).getSubject(
              this.subjectId);
          return;
        } 
        this.subject = SubjectFinder.findById(this.subjectId);
        return;
      } else if (hasSubjectIdentifier) {

        //cant have source without type
        if (hasSubjectSource) {
          this.subject = SubjectFinder.getSource(this.subjectSourceId).getSubjectByIdentifier(
              this.subjectIdentifier);
          return;
        }
        this.subject = SubjectFinder.findByIdentifier(this.subjectIdentifier);
        return;

      }

    } catch (SourceUnavailableException sue) {
      LOG.warn(this, sue);
      this.subjectFindResult = SubjectFindResult.SOURCE_UNAVAILABLE;
      this.cause = sue;
    } catch (SubjectNotUniqueException snue) {
      LOG.warn(this, snue);
      this.subjectFindResult = SubjectFindResult.SUBJECT_DUPLICATE;
      this.cause = snue;
    } catch (SubjectNotFoundException snfe) {
      LOG.warn(this, snfe);
      this.subjectFindResult = SubjectFindResult.SUBJECT_NOT_FOUND;
      this.cause = snfe;
    }

  }

  /**
   * clear the subject if a setter is called
   */
  private void clearSubject() {
    this.subject = null;
    this.subjectFindResult = null;
  }

  /** the one id of the subject */
  private String subjectId;

  /** any identifier of the subject */
  private String subjectIdentifier;

  /** optional: source of subject in the subject api source list */
  private String subjectSourceId;

  /**
   * optional: source of subject in the subject api source list
   * @return the subjectSource
   */
  public String getSubjectSourceId() {
    return this.subjectSourceId;
  }

  /**
   * optional: source of subject in the subject api source list
   * @param subjectSource1 the subjectSource to set
   */
  public void setSubjectSourceId(String subjectSource1) {
    this.subjectSourceId = subjectSource1;
    this.clearSubject();
  }

  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the subject
   */
  public Subject retrieveSubject() {
    this.retrieveSubjectIfNeeded();
    return this.subject;
  }

  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @param invalidInputReason label to be put in WsInvalidQueryException
   * @return the subject
   * @throws WsInvalidQueryException
   */
  public Subject retrieveSubject(String invalidInputReason) {
    Subject subject1 = this.retrieveSubject();
    if (subject1 == null) {
      //pass on the cause so it can be acted on
      throw new WsInvalidQueryException("Problem with " + invalidInputReason + ", "
          + this.subjectFindResult + ", " + this, this.cause);
    }
    return subject1;
  }

  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the subjectFindResult, this is never null
   */
  public SubjectFindResult retrieveSubjectFindResult() {
    this.retrieveSubjectIfNeeded();
    return this.subjectFindResult;
  }

  /**
   * id of the subject
   * @return the subjectId
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * id of the subject
   * @param subjectId1 the subjectId to set
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
    this.clearSubject();
  }

  /**
   * any identifier of the subject
   * @return the subjectIdentifier
   */
  public String getSubjectIdentifier() {
    return this.subjectIdentifier;
  }

  /**
   * any identifier of the subject
   * @param subjectIdentifier1 the subjectIdentifier to set
   */
  public void setSubjectIdentifier(String subjectIdentifier1) {
    this.subjectIdentifier = subjectIdentifier1;
    this.clearSubject();
  }

  /**
   * @param subjectId1
   * @param subjectSource1
   * @param subjectIdentifier1
   */
  public WsSubjectLookup(String subjectId1, String subjectSource1,
      String subjectIdentifier1) {
    this.subjectId = subjectId1;
    this.subjectSourceId = subjectSource1;
    this.subjectIdentifier = subjectIdentifier1;
  }

  /**
   * 
   */
  public WsSubjectLookup() {
    //blank
  }
}
