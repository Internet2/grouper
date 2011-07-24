package edu.internet2.middleware.grouper.ws.soap_v2_0;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * subject bean for web services
 * 
 * @author mchyzer
 * 
 */
public class WsSubject implements Comparable<WsSubject> {

  /** if lookedup by identifier, this is that identifier */
  private String identifierLookup;

  /**
   * identifier used to lookup subject
   * @return the identifier
   */
  public String getIdentifierLookup() {
    return this.identifierLookup;
  }

  /**
   * return the identifier looked up
   * @param identifierLookup1
   */
  public void setIdentifierLookup(String identifierLookup1) {
    this.identifierLookup = identifierLookup1;
  }

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * result code of a request
   */
  public static enum WsSubjectResultCode {

    /** found the stem (or not) */
    SUCCESS,

    /** cant find subject */
    SUBJECT_NOT_FOUND,

    /** duplicate subject records found */
    SUBJECT_DUPLICATE,

    /** source was unavailable */
    SOURCE_UNAVAILABLE,

    /** subject is in member table, but cant be found from subject source */
    UNRESOLVABLE;

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
   * convert members to subject results
   * @param attributeNames to get from subjects
   * @param memberSet
   * @param includeSubjectDetail 
   * @return the subject results
   */
  public static WsSubject[] convertMembers(Set<Member> memberSet, String[] attributeNames, boolean includeSubjectDetail) {
    int memberSetLength = GrouperUtil.length(memberSet);
    if (memberSetLength == 0) {
      return null;
    }

    WsSubject[] wsSubjectResults = new WsSubject[memberSetLength];
    int index = 0;
    for (Member member : memberSet) {
      wsSubjectResults[index++] = new WsSubject(member, attributeNames, null, includeSubjectDetail);
    }
    return wsSubjectResults;
  }

  /** can be SUCCESS (T) or UNRESOLVABLE (F) */
  private String resultCode;

  /** T or F */
  private String success;

  /**
   * 
   */
  private static final String TYPE = "type";

  /**
   * 
   */
  private static final String NAME = "name";

  /**
   * 
   */
  private static final String DESCRIPTION = "description";

  /** prefix of attribute that refers to subject: subject. */
  public static final String SUBJECT_ATTRIBUTE_PREFIX = "subject.";

  /**
   * constructor
   */
  public WsSubject() {
    // blank
  }

  /**
   * this is a temporary constructor to just put the lookup info here
   * in case there is an error retrieving the subject
   * constructor
   * @param wsSubjectLookup to be based on (assuming subject not found,
   * just copy the fields
   */
  public WsSubject(WsSubjectLookup wsSubjectLookup) {
    this.id = StringUtils.defaultIfEmpty(wsSubjectLookup.getSubjectId(),
        wsSubjectLookup.getSubjectIdentifier());
    this.sourceId = wsSubjectLookup.getSubjectSourceId();
  }

  /**
   * constructor to convert jdbc subject to a ws subject
   * 
   * @param subject (can be null)
   * @param subjectAttributeNames (should be calculated based on if detail or not)
   * @param wsSubjectLookup can be null, else the lookup which got the subject
   */
  public WsSubject(Subject subject, String[] subjectAttributeNames, WsSubjectLookup wsSubjectLookup) {
    this.assignSubjectData(subject, subjectAttributeNames);
    if (wsSubjectLookup != null) {
      //if the lookup has info, and it isnt in the wssubject, put it there
      if (!StringUtils.isBlank(wsSubjectLookup.getSubjectIdentifier())) {
        this.identifierLookup = wsSubjectLookup.getSubjectIdentifier();      
      }
      if (StringUtils.isBlank(this.getId()) && !StringUtils.isBlank(wsSubjectLookup.getSubjectId())) {
        this.setId(wsSubjectLookup.getSubjectId());
      }
      if (StringUtils.isBlank(this.getSourceId()) && !StringUtils.isBlank(wsSubjectLookup.getSubjectSourceId())) {
        this.setSourceId(wsSubjectLookup.getSubjectSourceId());
      }
      
    }
  }

  /**
  * construct with member to set internal fields
  * 
  * @param member
  * @param subjectAttributeNames are the attributes the user is getting (either requested or in config)
  * (should be calculated for is detail or not)
   * @param subjectLookup 
   * @param includeSubjectDetails 
  * @param retrieveExtendedSubjectDataBoolean
  *            true to retrieve subject info (more than just the id)
  */
  public WsSubject(Member member, String[] subjectAttributeNames, WsSubjectLookup subjectLookup, boolean includeSubjectDetails) {
    this.setId(member.getSubjectId());
    this.setSourceId(member.getSubjectSource().getId());

    int attributesLength = GrouperUtil.length(subjectAttributeNames);

    if (subjectLookup != null && StringUtils.isNotBlank(subjectLookup.getSubjectIdentifier())) {
      this.identifierLookup = subjectLookup.getSubjectIdentifier();
    }

    // if getting the subject data (extra queries)
    if (attributesLength > 0 || includeSubjectDetails) {
      Subject subject = null;
      try {
        subject = member.getSubject();
      } catch (SubjectNotFoundException snfe) {
        // I guess just ignore if not found, fields will be null
        if (snfe.getCause() instanceof SubjectNotUniqueException) {
          this.assignResultCode(WsSubjectResultCode.SUBJECT_DUPLICATE);
        } else if (snfe.getCause() instanceof SourceUnavailableException) {
          this.assignResultCode(WsSubjectResultCode.SOURCE_UNAVAILABLE);
        } else {
          this.assignResultCode(WsSubjectResultCode.UNRESOLVABLE);
        }
        return;
      }
      this.assignSubjectData(subject, subjectAttributeNames);
    } else {
      //if no other data, then success
      this.assignResultCode(WsSubjectResultCode.SUCCESS);
    }
  }

  /**
   * assign subject data
   * @param subject to assign from , can be null
   * @param retrieveExtendedSubjectDataBoolean true to retrieve extended subject data
   * @param subjectAttributeNames to retrieve, can be empty or null
   */
  private void assignSubjectData(Subject subject, String[] subjectAttributeNames) {

    //see if nothing to assign
    if (subject == null) {
      this.assignResultCode(WsSubjectResultCode.SUBJECT_NOT_FOUND);
      return;
    }

    this.setId(subject.getId());
    this.setSourceId(subject.getSource().getId());

    this.setName(subject.getName());
    
    int attributesLength = GrouperUtil.length(subjectAttributeNames);

    // if getting the subject data (extra queries)
    if (attributesLength > 0) {

      //extended means add description or name (or custom)
      this.attributeValues = new String[attributesLength];
      int index = 0;
      for (String attributeName : subjectAttributeNames) {
        //NOTE: dont send nulls, it might mess up XML, only send empty string if not there
        this.attributeValues[index] = StringUtils.defaultString(subject.getAttributeValue(attributeName));
        //type might be in getter
        if (StringUtils.equals(TYPE, attributeName)
            && StringUtils.isBlank(this.attributeValues[index])) {
          //note, this might go away in next subject api
          this.attributeValues[index] = StringUtils.defaultString(subject.getType().getName());
        }
        if (StringUtils.equals(DESCRIPTION, attributeName)
            && StringUtils.isBlank(this.attributeValues[index])) {
          //note, this might go away in next subject api
          this.attributeValues[index] = StringUtils.defaultString(subject.getDescription());
        }
        if (StringUtils.equals(NAME, attributeName)
            && StringUtils.isBlank(this.attributeValues[index])) {
          //note, this might go away in next subject api
          this.attributeValues[index] = StringUtils.defaultString(subject.getName());
        }
        index++;
      }
    }
    this.assignResultCode(WsSubjectResultCode.SUCCESS);

  }

  /** id of subject, note if no subject found, and identifier was passed in,
   * that will be placed here */
  private String id;

  /** name of subject */
  private String name;

  /** source of subject */
  private String sourceId;

  /**
   * attribute data of subjects in group (in same order as attributeNames)
   */
  private String[] attributeValues;

  /**
   * subject id, note if no subject found, and identifier was passed in,
   * that will be placed here
   * @return the id
   */
  public String getId() {
    return this.id;
  }

  /**
   * subject id, note if no subject found, and identifier was passed in,
   * that will be placed here
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * @param name1
   *            the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * if attributes are being sent back per config in the grouper.properties,
   * this is attribute0 value, this is extended subject data
   * 
   * @return the attribute0
   */
  public String[] getAttributeValues() {
    return this.attributeValues;
  }

  /**
   * attribute data of subjects in group (in same order as attributeNames)
   * 
   * @param attributesa
   *            the attributes to set
   */
  public void setAttributeValues(String[] attributesa) {
    this.attributeValues = attributesa;
  }

  /**
   * @return the source
   */
  public String getSourceId() {
    return this.sourceId;
  }

  /**
   * @param source1 the source to set
   */
  public void setSourceId(String source1) {
    this.sourceId = source1;
  }

  /**
   * @return the resultCode
   */
  public String getResultCode() {
    return this.resultCode;
  }

  /**
   * @param resultCode1 the resultCode to set
   */
  public void setResultCode(String resultCode1) {
    this.resultCode = resultCode1;
  }

  /**
   * assign the code from the enum
   * 
   * @param wsSubjectResultCode
   */
  public void assignResultCode(WsSubjectResultCode wsSubjectResultCode) {
    this.setResultCode(wsSubjectResultCode == null ? null : wsSubjectResultCode.name());
    this.setSuccess(GrouperServiceUtils.booleanToStringOneChar(wsSubjectResultCode
        .isSuccess()));
  }

  /**
   * T or F for success
   * @return the success
   */
  public String getSuccess() {
    return this.success;
  }

  /**
   * T or F for success
   * @param success1 the success to set
   */
  public void setSuccess(String success1) {
    this.success = success1;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(WsSubject o2) {
    if (this == o2) {
      return 0;
    }
    //lets by null safe here
    if (this == null) {
      return -1;
    }
    if (o2 == null) {
      return 1;
    }
    int compare = GrouperUtil.compare(this.getSourceId(), o2.getSourceId());
    if (compare != 0) {
      return compare;
    }
    return GrouperUtil.compare(this.getId(), o2.getId());

  }

}
