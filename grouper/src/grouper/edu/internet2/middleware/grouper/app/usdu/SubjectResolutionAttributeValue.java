package edu.internet2.middleware.grouper.app.usdu;

import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;

public class SubjectResolutionAttributeValue {
  
  
  private String subjectResolutionResolvableString;
  
  
  private String subjectResolutionDateLastResolvedString;
  
  
  private String subjectResolutionDaysUnresolvedString;
  
  
  private String subjectResolutionLastCheckedString;

  
  public String getSubjectResolutionDateLastResolvedString() {
    return subjectResolutionDateLastResolvedString;
  }

  
  public void setSubjectResolutionDateLastResolvedString(String subjectResolutionDateLastResolvedString) {
    this.subjectResolutionDateLastResolvedString = subjectResolutionDateLastResolvedString;
  }


  public String getSubjectResolutionLastCheckedString() {
    return subjectResolutionLastCheckedString;
  }

  
  public void setSubjectResolutionLastCheckedString(String subjectResolutionLastCheckedString) {
    this.subjectResolutionLastCheckedString = subjectResolutionLastCheckedString;
  }

  
  public void setSubjectResolutionResolvableString(String subjectResolutionResolvableString) {
    this.subjectResolutionResolvableString = subjectResolutionResolvableString;
  }


  public void setSubjectResolutionDaysUnresolvedString(String subjectResolutionDaysUnresolvedString) {
    this.subjectResolutionDaysUnresolvedString = subjectResolutionDaysUnresolvedString;
  }


  public boolean isSubjectResolutionResolvable() {
    return BooleanUtils.toBoolean(subjectResolutionResolvableString);
  }
  
  public Long getSubjectResolutionDaysUnresolved() {
    try {
      return Long.valueOf(subjectResolutionDaysUnresolvedString);
    } catch (Exception e) {
      return 0L;
    }
  }
  
}
