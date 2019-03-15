package edu.internet2.middleware.grouper.app.usdu;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

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
   * set to true when all the memberships are deleted.
   * when this attribute is set to true, clear subjectResolutionResolvableString, subjectResolutionDaysUnresolved and subjectResolutionDateLastCheckedString
   */
  private String subjectResolutionDeletedString;
  
  /**
   * timestamp when member was marked as deleted
   */
  private String subjectResolutionDateDeleteString;
  
  /**
   * member that has been assigned the attribute
   */
  private Member member;
  
  /**
   * date subject will be deleted string
   */
  private String dateSubjectWillBeDeletedString;
  

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
   * 
   * @return false if the subject is not resolvable
   */
  public String getSubjectResolutionResolvableString() {
    return subjectResolutionResolvableString;
  }

  /**
   * 
   * @return number of days subject has been unresolved for
   */
  public String getSubjectResolutionDaysUnresolvedString() {
    return subjectResolutionDaysUnresolvedString;
  }
  
  
  /**
   * 
   * @return number of days subject has been unresolved for
   */
  public Long getSubjectResolutionDaysUnresolved() {
    try {
      return Long.valueOf(subjectResolutionDaysUnresolvedString);
    } catch (NumberFormatException e) {
      return 0L;
    }
  }

  
  /**
   * number of days subject has been unresolved for
   * @param subjectResolutionDaysUnresolved
   */
  public void setSubjectResolutionDaysUnresolvedString(String subjectResolutionDaysUnresolvedString) {
    this.subjectResolutionDaysUnresolvedString = subjectResolutionDaysUnresolvedString;
  }

  /**
   * set to true when all the memberships are deleted. 
   * when this attribute is set to true, clear subjectResolutionResolvableString, subjectResolutionDaysUnresolvedString and subjectResolutionDateLastCheckedString
   * @param subjectResolutionDeletedString
   */
  public void setSubjectResolutionDeletedString(String subjectResolutionDeletedString) {
    this.subjectResolutionDeletedString = subjectResolutionDeletedString;
  }
  
  /**
   * set to true when all the memberships are deleted. 
   * when this attribute is set to true, clear subjectResolutionResolvableString, subjectResolutionDaysUnresolvedString and subjectResolutionDateLastCheckedString
   * @return
   */
  public String getSubjectResolutionDeletedString() {
    return subjectResolutionDeletedString;
  }

  /**
   * 
   * @return timestamp when member was marked as deleted
   */
  public String getSubjectResolutionDateDelete() {
    
    if (StringUtils.isNotBlank(subjectResolutionDateDeleteString)) {
      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
      Long millis = Long.valueOf(subjectResolutionDateDeleteString);
      return dateFormat.format(new Date(millis));
    }
    return null;
  }

  /**
   * timestamp when member was marked as deleted
   * @param subjectResolutionDateDeleteString
   */
  public void setSubjectResolutionDateDeleteString(String subjectResolutionDateDeleteString) {
    this.subjectResolutionDateDeleteString = subjectResolutionDateDeleteString;
  }

  /**
   * 
   * @return member that has been assigned the attribute
   */
  public Member getMember() {
    return member;
  }

  /**
   * member that has been assigned the attribute
   * @param member
   */
  public void setMember(Member member) {
    this.member = member;
  }

  /**
   * 
   * @return date subject will be deleted string
   */
  public String getDateSubjectWillBeDeletedString() {
    return dateSubjectWillBeDeletedString;
  }

  /**
   * date subject will be deleted string
   * @param dateSubjectWillBeDeletedString
   */
  public void setDateSubjectWillBeDeletedString(String dateSubjectWillBeDeletedString) {
    this.dateSubjectWillBeDeletedString = dateSubjectWillBeDeletedString;
  }
  
  
}
