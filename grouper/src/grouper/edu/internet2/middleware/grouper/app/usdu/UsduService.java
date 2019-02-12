package edu.internet2.middleware.grouper.app.usdu;

import static edu.internet2.middleware.grouper.app.usdu.UsduAttributeNames.SUBJECT_RESOLUTION_DATE_LAST_RESOLVED;
import static edu.internet2.middleware.grouper.app.usdu.UsduSettings.usduStemName;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
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
  
  
  public static void saveOrUpldateSubjectResolutionAttributeValue(SubjectResolutionAttributeValue subjectResolutionAttributeValue, Member member) {
    
    AttributeAssign attributeAssign = getAttributeAssign(member);
    
    if (attributeAssign == null) {
      attributeAssign = member.getAttributeDelegate().addAttribute(UsduAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
    }
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_RESOLVABLE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(subjectResolutionAttributeValue.isSubjectResolutionResolvable()));
    
    if (StringUtils.isNotBlank(subjectResolutionAttributeValue.getSubjectResolutionDateLastResolvedString())) {
      attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DATE_LAST_RESOLVED, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), subjectResolutionAttributeValue.getSubjectResolutionDateLastResolvedString());
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DAYS_UNRESOLVED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(subjectResolutionAttributeValue.getSubjectResolutionDaysUnresolved()));
    
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_LAST_CHECKED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), subjectResolutionAttributeValue.getSubjectResolutionLastCheckedString());
    
    attributeAssign.saveOrUpdate();
    
  }
  
  
  
  /**
   * build subject resolution attribute object from underlying info
   * @param attributeAssign
   * @return SubjectResolutionAttributeValue
   */
  private static SubjectResolutionAttributeValue buildSubjectResolutionAttributeValue(AttributeAssign attributeAssign) {
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    SubjectResolutionAttributeValue result = new SubjectResolutionAttributeValue();
    
    String resolvableString = attributeValueDelegate.retrieveAttributeAssignValue(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_RESOLVABLE).getValueString();
    
    result.setSubjectResolutionResolvableString(resolvableString);
    result.setSubjectResolutionDateLastResolvedString(attributeValueDelegate.retrieveAttributeAssignValue(usduStemName()+":"+SUBJECT_RESOLUTION_DATE_LAST_RESOLVED).getValueString());
    result.setSubjectResolutionDaysUnresolvedString(attributeValueDelegate.retrieveAttributeAssignValue(usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DAYS_UNRESOLVED).getValueString());
    result.setSubjectResolutionLastCheckedString(attributeValueDelegate.retrieveAttributeAssignValue(usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_LAST_CHECKED).getValueString());
    
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
