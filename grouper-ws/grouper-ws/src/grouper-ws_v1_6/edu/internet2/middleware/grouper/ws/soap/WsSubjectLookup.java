/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.MemberNotUniqueException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsHasMemberResult.WsHasMemberResultCode;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * <pre>
 * template to lookup a subject.
 * 
 * to lookup a group as a subject, use the group uuid (e.g. fa2dd790-d3f9-4cf4-ac41-bb82e63bff66) in the 
 * subject id of the subject lookup.  Optionally you can use g:gsa as
 * the source id.
 * 
 * developers make sure each setter calls this.clearSubject();
 * 
 * </pre>
 * @author mchyzer
 */
public class WsSubjectLookup {

  /** find the subject */
  @XStreamOmitField
  private Subject subject = null;

  /** find the member */
  @XStreamOmitField
  private Member member = null;

  /** if there is an exception in find, list it here */
  @XStreamOmitField
  private Exception cause = null;

  /** if there is an exception in find member, list it here */
  @XStreamOmitField
  private Exception causeMember = null;

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

  /**
   * result of a subject find
   *
   */
  public static enum MemberFindResult {
  
    /** found the member */
    SUCCESS,
    
    /** invalid query (e.g. everything blank or subject 
     * identifier specified with no subject id) */
    INVALID_QUERY,
    
    /** cant find member */
    MEMBER_NOT_FOUND;
  
    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }
  
  }

  /** result of subject find */
  @XStreamOmitField
  private SubjectFindResult subjectFindResult = null;

  /** result of subject find */
  @XStreamOmitField
  private MemberFindResult memberFindResult = null;

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsSubjectLookup.class);

  /**
   * create if any not null, otherwise null
   * @param subjectId
   * @param sourceId
   * @param subjectIdentifier
   * @return the subject lookup
   */
  public static WsSubjectLookup createIfNeeded(String subjectId, String sourceId, String subjectIdentifier) {
    WsSubjectLookup wsSubjectLookup = null;
    if (StringUtils.isNotBlank(subjectId) || StringUtils.isNotBlank(subjectIdentifier) || StringUtils.isNotBlank(sourceId)) {
      wsSubjectLookup = new WsSubjectLookup(subjectId, sourceId, subjectIdentifier);
    }
    return wsSubjectLookup;
  }
  
  /**
   * 
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    GrouperStartup.startup();
    WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
    wsSubjectLookup.setSubjectId("whatever");
    System.out.println(wsSubjectLookup);
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
   * see if this group lookup has data
   * @return true if it has data
   */
  public boolean hasData() {
    return !StringUtils.isBlank(this.subjectId) || !StringUtils.isBlank(this.subjectIdentifier);
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
              this.subjectId, true);
          return;
        } 
        this.subject = SubjectFinder.findById(this.subjectId, true);
        return;
      } else if (hasSubjectIdentifier) {
  
        //cant have source without type
        if (hasSubjectSource) {
          this.subject = SubjectFinder.getSource(this.subjectSourceId).getSubjectByIdentifier(
              this.subjectIdentifier, true);
          return;
        }
        this.subject = SubjectFinder.findByIdentifier(this.subjectIdentifier, true);
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
   * lazy load the member
   */
  private void retrieveMemberIfNeeded() {
    //see if we already retrieved
    if (this.memberFindResult != null) {
      return;
    }
    try {
      //assume success (set otherwise if ther is a proble
      this.memberFindResult = MemberFindResult.SUCCESS;

      boolean hasSubjectId = !StringUtils.isBlank(this.subjectId);
      boolean hasSubjectSource = !StringUtils.isBlank(this.subjectSourceId);
      
      Subject theSubject = this.retrieveSubject();
      
      if (theSubject != null) {
        this.member = GrouperDAOFactory.getFactory().getMember().findBySubject(theSubject, true);
      } else {
        //we need to find with the params passed in
        if (hasSubjectId && hasSubjectSource) {
          this.member = GrouperDAOFactory.getFactory().getMember().findBySubject(this.subjectId, this.subjectSourceId, true);
        } else if (hasSubjectId) {
          this.member = GrouperDAOFactory.getFactory().getMember().findBySubject(this.subjectId, true);
        } else {
          this.memberFindResult = MemberFindResult.INVALID_QUERY;
          LOG.warn("Cannot find subject: " + this);
        }
      }
    } catch (MemberNotUniqueException mnue) {
      LOG.warn("Member not unique: " + this, mnue);
      this.memberFindResult = MemberFindResult.INVALID_QUERY;
      this.cause = mnue;
    } catch (MemberNotFoundException mnfe) {
      LOG.warn(this, mnfe);
      this.memberFindResult = MemberFindResult.MEMBER_NOT_FOUND;
      this.cause = mnfe;
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
   * 
   * <pre>
   * Retrieve the member object for this subject, do not create if not there
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the member
   */
  public Member retrieveMember() {
    this.retrieveMemberIfNeeded();
    return this.member;
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
  public Member retrieveMember(String invalidInputReason) {
    Member member1 = this.retrieveMember();
    if (member1 == null) {
      //pass on the cause so it can be acted on
      throw new WsInvalidQueryException("Problem with " + invalidInputReason + ", "
          + this.memberFindResult + ", " + this, this.causeMember);
    }
    return member1;
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
   * <pre>
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the memberFindResult, this is never null
   */
  public MemberFindResult retrieveMemberFindResult() {
    this.retrieveMemberIfNeeded();
    return this.memberFindResult;
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
   * convert subject lookups to member ids (create if not exist)
   * @param grouperSession
   * @param wsSubjectLookups
   * @param errorMessage
   * @return the group ids
   */
  public static Set<String> convertToMemberIds(GrouperSession grouperSession, WsSubjectLookup[] wsSubjectLookups, StringBuilder errorMessage) {
    return convertToMemberIds(grouperSession, wsSubjectLookups, errorMessage, new int[1]);
  }

  /**
   * convert subject lookups to member ids (create if not exist)
   * @param grouperSession
   * @param wsSubjectLookups
   * @param errorMessage
   * @param lookupCount is an array of size one int where 1 will be added if there are records, and no change if not
   * @return the group ids
   */
  public static Set<String> convertToMemberIds(GrouperSession grouperSession, WsSubjectLookup[] wsSubjectLookups, StringBuilder errorMessage, int[] lookupCount) {
    //get all the subjects
    //we could probably batch these to get better performance.
    Set<String> memberIds = null;
    if (!GrouperServiceUtils.nullArray(wsSubjectLookups)) {
      
      memberIds = new LinkedHashSet<String>();
      int i=0;
      
      boolean foundRecords = false;
      
      for (WsSubjectLookup wsSubjectLookup : wsSubjectLookups) {
        
        if (wsSubjectLookup == null || !wsSubjectLookup.hasData()) {
          continue;
        }
        
        if (!foundRecords) {
          lookupCount[0]++;
          foundRecords = true;
        }
        
        Subject subject = wsSubjectLookup.retrieveSubject();
        if (subject != null) {
          Member member = MemberFinder.findBySubject(grouperSession, subject, true);
          memberIds.add(member.getUuid());
        } else {
          
          if (errorMessage.length() > 0) {
            errorMessage.append(", ");
          }
          
          errorMessage.append("Error on subject index: " + i + ", " + wsSubjectLookup.retrieveSubjectFindResult() + ", " + wsSubjectLookup.toStringCompact());
        }
        
        i++;
      }
      
    }
    return memberIds;
  }

  /**
   * make sure this is an explicit toString
   * @return return a compact to string
   */
  public String toStringCompact() {
    
    String sourceId = StringUtils.isBlank(this.subjectSourceId) ? "" : ("source: " + this.subjectSourceId + ", ");
    
    if (!StringUtils.isBlank(this.subjectId)) {
      return sourceId + "subjectId: " + this.subjectId;
    }
    if (!StringUtils.isBlank(this.subjectIdentifier)) {
      return sourceId + "subjectIdentifier: " + this.subjectIdentifier;
    }
    return "blank";
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
