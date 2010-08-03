package edu.internet2.middleware.grouper.rules;

/**
 * definition for the subject act as for a rule
 * @author mchyzer
 *
 */
public class RuleSubjectActAs {

  /** subject id to act as */
  private String subjectId;
  
  /** source id to act as */
  private String sourceId;
  
  /** subject identifier to act as */
  private String subjectIdentifier;

  /**
   * subject id to act as
   * @return subject id to act as
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * subject id to act as
   * @param subjectId1
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }

  /**
   * source id to act as
   * @return source id to act as
   */
  public String getSourceId() {
    return this.sourceId;
  }

  /**
   * source id to act as
   * @param sourceId1
   */
  public void setSourceId(String sourceId1) {
    this.sourceId = sourceId1;
  }

  /**
   * subject identifier to act as
   * @return subject id to act as
   */
  public String getSubjectIdentifier() {
    return this.subjectIdentifier;
  }

  /**
   * subject identifier to act as
   * @param subjectIdentifier1
   */
  public void setSubjectIdentifier(String subjectIdentifier1) {
    this.subjectIdentifier = subjectIdentifier1;
  }
  
}
