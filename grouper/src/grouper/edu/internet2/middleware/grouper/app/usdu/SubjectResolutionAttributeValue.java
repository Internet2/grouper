package edu.internet2.middleware.grouper.app.usdu;

import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;

/**
 * bean that represents metadata attributes on member 
 */
public class SubjectResolutionAttributeValue {
  
  /**
   * false if the subject is not resolvable
   */
  private String subjectResolutionResolvableString;
  
  /**
   * date when the subject was last resolvable
   */
  private String subjectResolutionDateLastResolvedString;
  
  /**
   * number of days subject has been unresolved
   */
  private String subjectResolutionDaysUnresolvedString;
  
  /**
   * date when the subject was last checked for resolvable/unresolvable
   */
  private String subjectResolutionDateLastCheckedString;

  /**
   * 
   * @return date when the subject was last resolvable
   */
  public String getSubjectResolutionDateLastResolvedString() {
    return subjectResolutionDateLastResolvedString;
  }

  /**
   * date when the subject was last resolvable
   * @param subjectResolutionDateLastResolvedString
   */
  public void setSubjectResolutionDateLastResolvedString(String subjectResolutionDateLastResolvedString) {
    this.subjectResolutionDateLastResolvedString = subjectResolutionDateLastResolvedString;
  }

  /**
   * 
   * @return date when the subject was last checked for resolvable/unresolvable
   */
  public String getSubjectResolutionDateLastCheckedString() {
    return subjectResolutionDateLastCheckedString;
  }

  /**
   * date when the subject was last checked for resolvable/unresolvable
   * @param subjectResolutionLastCheckedString
   */
  public void setSubjectResolutionDateLastCheckedString(String subjectResolutionDateLastCheckedString) {
    this.subjectResolutionDateLastCheckedString = subjectResolutionDateLastCheckedString;
  }

  /**
   * 
   * @param subjectResolutionResolvableString false if the subject is not resolvable
   */
  public void setSubjectResolutionResolvableString(String subjectResolutionResolvableString) {
    this.subjectResolutionResolvableString = subjectResolutionResolvableString;
  }

  /**
   * false if the subject is not resolvable
   * @param subjectResolutionDaysUnresolvedString
   */
  public void setSubjectResolutionDaysUnresolvedString(String subjectResolutionDaysUnresolvedString) {
    this.subjectResolutionDaysUnresolvedString = subjectResolutionDaysUnresolvedString;
  }

  /**
   * 
   * @return false if the subject is not resolvable
   */
  public boolean isSubjectResolutionResolvable() {
    return BooleanUtils.toBoolean(subjectResolutionResolvableString);
  }
  
  /**
   * 
   * @return number of days subject has been unresolved
   */
  public Long getSubjectResolutionDaysUnresolved() {
    try {
      return Long.valueOf(subjectResolutionDaysUnresolvedString);
    } catch (Exception e) {
      return 0L;
    }
  }
  
}
