package edu.internet2.middleware.grouper.app.usdu;

import static edu.internet2.middleware.grouper.app.usdu.UsduAttributeNames.SUBJECT_RESOLUTION_DATE_LAST_RESOLVED;
import static edu.internet2.middleware.grouper.app.usdu.UsduSettings.usduStemName;

import java.util.Date;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;

public class UsduService {
  
  
  /**
   * retrieve subject resolution attribute value for a given member if it exists or null
   * @param member
   * @return SubjectResolutionAttributeValue or null
   */
  public static SubjectResolutionAttributeValue getSubjectResolutionAttributeValue(Member member) {
    
    AttributeAssign attributeAssign = getAttributeAssign(member);
    if (attributeAssign == null) {
      return null;
    }
    
    return buildSubjectResolutionAttributeValue(attributeAssign);
  }
  
  /**
   * save or update subject resolution metadata attributes on a given member
   * @param subjectResolutionAttributeValue
   * @param member
   */
  public static void markMemberAsUnresolved(SubjectResolutionAttributeValue subjectResolutionAttributeValue, Member member) {
    
    AttributeAssign attributeAssign = getAttributeAssign(member);
    
    if (attributeAssign == null) {
      attributeAssign = member.getAttributeDelegate().assignAttribute(UsduAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
    }
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_RESOLVABLE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), subjectResolutionAttributeValue.getSubjectResolutionResolvableString());
    
    if (StringUtils.isNotBlank(subjectResolutionAttributeValue.getSubjectResolutionDateLastResolvedString())) {
      attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DATE_LAST_RESOLVED, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), subjectResolutionAttributeValue.getSubjectResolutionDateLastResolvedString());
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DAYS_UNRESOLVED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), subjectResolutionAttributeValue.getSubjectResolutionDaysUnresolvedString());
    
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_LAST_CHECKED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), subjectResolutionAttributeValue.getSubjectResolutionDateLastCheckedString());
    
    // clear the rest
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DELETED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), null);
        
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DELETE_DATE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), null);
    
    attributeAssign.saveOrUpdate();
    
  }
  
  /**
   * set subject resolution attributes on member
   * @param member
   */
  public static void markMemberAsDeleted(Member member) {
    
    AttributeAssign attributeAssign = getAttributeAssign(member);
    
    if (attributeAssign == null) {
      attributeAssign = member.getAttributeDelegate().assignAttribute(UsduAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
    }
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DELETED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(true));

    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DELETE_DATE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(new Date().getTime()));
    
    // clear the rest
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DAYS_UNRESOLVED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), null);
        
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_LAST_CHECKED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), null);
    
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_RESOLVABLE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), null);
    
    attributeAssign.saveOrUpdate();
    
  }
  
  
  /**
   * delete resolution attributes from the given member assignment
   * @param member
   */
  public static void deleteAttributeAssign(Member member) {
    AttributeAssign currentAttributeAssign = getAttributeAssign(member);
    if (currentAttributeAssign != null) {
      currentAttributeAssign.delete();
    }
  }
  
  
  
  /**
   * build subject resolution attribute object from member attributes
   * @param attributeAssign
   * @return SubjectResolutionAttributeValue
   */
  private static SubjectResolutionAttributeValue buildSubjectResolutionAttributeValue(AttributeAssign attributeAssign) {
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    SubjectResolutionAttributeValue result = new SubjectResolutionAttributeValue();
    
    AttributeAssignValue assignValue = attributeValueDelegate.retrieveAttributeAssignValue(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_RESOLVABLE);
    if (assignValue != null) {      
      result.setSubjectResolutionResolvableString(assignValue.getValueString());
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(usduStemName()+":"+SUBJECT_RESOLUTION_DATE_LAST_RESOLVED);
    if (assignValue != null) {      
      result.setSubjectResolutionDateLastResolvedString(assignValue.getValueString());
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DAYS_UNRESOLVED);
    if (assignValue != null) {      
      result.setSubjectResolutionDaysUnresolvedString(assignValue.getValueString());
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_LAST_CHECKED);
    if (assignValue != null) {      
      result.setSubjectResolutionDateLastCheckedString(assignValue.getValueString());
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DELETED);
    if (assignValue != null) {      
      result.setSubjectResolutionDeletedString(assignValue.getValueString());
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DELETE_DATE);
    if (assignValue != null) {
      result.setSubjectResolutionDateDeleteString(assignValue.getValueString());
    }

    return result;
  }
  
  /**
   * get subject resolution attributes for a given member if it exists or null
   * @param member
   * @return
   */
  private static AttributeAssign getAttributeAssign(Member member) {
    
    Set<AttributeAssign> attributeAssigns = member.getAttributeDelegate().retrieveAssignments(UsduAttributeNames.retrieveAttributeDefNameBase());
    
    if (attributeAssigns.isEmpty()) {
      return null;
    }
    
    return attributeAssigns.iterator().next();
    
  }


}
