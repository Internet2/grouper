package edu.internet2.middleware.grouper.app.usdu;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder.AttributeAssignValueFinderResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;

/**
 * 
 */
public class UsduService {
  
  
  /**
   * retrieve subject resolution attribute value for a given member if it exists or null
   * @param member
   * @param attributeAssignValueFinderResult 
   * @return SubjectResolutionAttributeValue or null
   */
  public static SubjectResolutionAttributeValue getSubjectResolutionAttributeValue(Member member, AttributeAssignValueFinderResult attributeAssignValueFinderResult) {
    
    AttributeAssign attributeAssign = null;
    Map<String, String> attributeDefNamesAndValues = null;
    if (attributeAssignValueFinderResult == null) {
      attributeAssign = getAttributeAssign(member);
      if (attributeAssign == null) {
        return null;
      }
    } else {
      attributeDefNamesAndValues = attributeAssignValueFinderResult.retrieveAttributeDefNamesAndValueStrings(member.getId());
      if (GrouperUtil.length(attributeDefNamesAndValues) == 0) {
        return null;
      }
    }
    return buildSubjectResolutionAttributeValue(attributeAssign, member, attributeDefNamesAndValues);
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
    
    AttributeDefName attributeDefName = null;
    
    if (StringUtils.isNotBlank(subjectResolutionAttributeValue.getSubjectResolutionDateLastResolvedString())) {
      attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DATE_LAST_RESOLVED, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), subjectResolutionAttributeValue.getSubjectResolutionDateLastResolvedString());
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DAYS_UNRESOLVED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), subjectResolutionAttributeValue.getSubjectResolutionDaysUnresolvedString());
    
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_LAST_CHECKED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), subjectResolutionAttributeValue.getSubjectResolutionDateLastCheckedString());
    
    // clear the rest        
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DELETE_DATE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), null);
    
    attributeAssign.saveOrUpdate();
    
    member.setSubjectResolutionResolvable(false);
    member.setSubjectResolutionDeleted(false);
    member.store();
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
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DELETE_DATE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(new Date().getTime()));
    
    // clear the rest
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DAYS_UNRESOLVED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), null);
        
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_LAST_CHECKED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), null);
    
    attributeAssign.saveOrUpdate();
    
    member.setSubjectResolutionResolvable(false);
    member.setSubjectResolutionDeleted(true);
    member.store();
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
   * 
   * @return the list of subject sources with unresolved and resolved count
   */
  public static List<SubjectResolutionStat> getSubjectResolutionStats() {
    
    List<Source> sources = new ArrayList<Source>(SubjectFinder.getSources());
    Collections.sort(sources, new Comparator<Source>() {

      public int compare(Source o1, Source o2) {
        if (o1== o2) {
          return 0;
        }
        if (o1 == null) {
          return -1;
        }
        if (o1 == null) {
          return 1;
        }
        return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
      }
    });
    
    GrouperSession session = GrouperSession.startRootSession();
    
    List<SubjectResolutionStat> subjectResolutionStats = new ArrayList<SubjectResolutionStat>();
    
    // select subject_source, count(*) from grouper_members group by subject_source
    List<Object[]> sourceIdCountTotals = HibernateSession.bySqlStatic().listSelect(Object[].class,
        "select subject_source, count(*) from grouper_members group by subject_source ", null, null);
    
    // select distinct source_id, subject_id from grouper_aval_asn_asn_member_v where attribute_def_name_name1 = 'penn:etc:usdu:subjectResolutionMarker'
    // and attribute_def_name_name2 = 'penn:etc:usdu:subjectResolutionResolvable' and value_string = 'false' and enabled2 = 'T';
    
    // select source_id, count(*) from grouper_aval_asn_asn_member_v where attribute_def_name_name1 = 'penn:etc:usdu:subjectResolutionMarker'
    // and attribute_def_name_name2 = 'penn:etc:usdu:subjectResolutionResolvable' and value_string = 'false' and enabled2 = 'T' group by source_id;
    
    final String sqlUnresolvable = "select subject_source as source_id, count(*) from grouper_members where subject_resolution_resolvable='F' group by subject_source";
    List<Object[]> sourceIdCountUnresolvables = HibernateSession.bySqlStatic().listSelect(Object[].class, sqlUnresolvable, null, null);
    

    // select source_id, count(*) from grouper_aval_asn_asn_member_v where attribute_def_name_name1 = 'penn:etc:usdu:subjectResolutionMarker'
    // and attribute_def_name_name2 = 'penn:etc:usdu:subjectResolutionDeleted' and value_string = 'true' and enabled2 = 'T' group by source_id;
    
    final String sqlDeleted = "select subject_source as source_id, count(*) from grouper_members where subject_resolution_deleted='T' group by subject_source";
    List<Object[]> sourceIdCountDeleteds = HibernateSession.bySqlStatic().listSelect(Object[].class, sqlDeleted, null, null);
    

    for (Source source: sources) {
      
      long unresolvedCount = 0L;
      long resolvedCount = 0L;
      long deletedCount = 0L;

      // go through the amount received from totals
      TOTALS:
      for (Object[] sourceIdCountTotal : GrouperUtil.nonNull(sourceIdCountTotals)) {
        String sourceIdTotal = (String)sourceIdCountTotal[0];
        if (!StringUtils.equals(source.getId(), sourceIdTotal)) {
          continue TOTALS;
        }
        resolvedCount = GrouperUtil.intValue(sourceIdCountTotal[1]);

        UNRESOLVABLES:
        for (Object[] sourceIdCountUnresolvable : GrouperUtil.nonNull(sourceIdCountUnresolvables)) {
          String sourceIdUnresolvable = (String)sourceIdCountUnresolvable[0];
          if (!StringUtils.equals(source.getId(), sourceIdUnresolvable)) {
            continue UNRESOLVABLES;
          }
          
          // if there are unresolvables
          unresolvedCount = GrouperUtil.intValue(sourceIdCountUnresolvable[1]);
          resolvedCount -= unresolvedCount;
          
          DELETEDS:
          for (Object[] sourceIdCountDeleted : GrouperUtil.nonNull(sourceIdCountDeleteds)) {
            String sourceIdDeleted = (String)sourceIdCountDeleted[0];
            if (!StringUtils.equals(source.getId(), sourceIdDeleted)) {
              continue DELETEDS;
            }
            deletedCount = GrouperUtil.intValue(sourceIdCountDeleted[1]);
            unresolvedCount -= deletedCount;
            break DELETEDS;
          }
          
          break UNRESOLVABLES;
        }
        
        break TOTALS;
      }

      subjectResolutionStats.add(new SubjectResolutionStat(source.getName(), unresolvedCount, resolvedCount, deletedCount));
      
    }
    
    return subjectResolutionStats;
    
  }
  
  /**
   * 
   * @param queryOptions
   * @param deleted true for delete, false for not deleted, null for all
   * @return unresolved subjects
   */
  public static Set<SubjectResolutionAttributeValue> getUnresolvedSubjects(QueryOptions queryOptions, Boolean deleted) {
    
    Set<SubjectResolutionAttributeValue> unresolvedSubjects = new LinkedHashSet<SubjectResolutionAttributeValue>();
    
    Set<Member> unresolvedMembers = null;
    
    if (deleted == null) {
      unresolvedMembers = GrouperDAOFactory.getFactory().getMember().getUnresolvableMembers(null);
    } else if (deleted) {
      unresolvedMembers = GrouperDAOFactory.getFactory().getMember().getUnresolvableMembers(true);
    } else {
      unresolvedMembers = GrouperDAOFactory.getFactory().getMember().getUnresolvableMembers(false);
    }

// dont do two queries since it messes up paging
//    Set<Member> deletedMembers = new MemberFinder()
//        .assignAttributeCheckReadOnAttributeDef(false)
//        .assignNameOfAttributeDefName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DELETED)
//        .addAttributeValuesOnAssignment("true")
//        .assignQueryOptions(queryOptions)
//        .findMembers();
    
    if (GrouperUtil.length(unresolvedMembers) > 0) {
    
      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
      Date currentDate = new Date();
      Calendar c = Calendar.getInstance();
      
      AttributeAssignValueFinder attributeAssignValueFinder = new AttributeAssignValueFinder();
      
      for (Member member: unresolvedMembers) {
        
        attributeAssignValueFinder.addOwnerMemberIdOfAssignAssign(member.getId());
      }
      
      attributeAssignValueFinder.addAttributeDefNameId(UsduAttributeNames.retrieveAttributeDefNameBase().getId());
      AttributeAssignValueFinderResult attributeAssignValueFinderResult = attributeAssignValueFinder.findAttributeAssignValuesResult();
        
      for (Member member: unresolvedMembers) {
        SubjectResolutionAttributeValue subjectResolutionAttributeValue = getSubjectResolutionAttributeValue(member, attributeAssignValueFinderResult);
      
        if (subjectResolutionAttributeValue.isDeleted()) {
  
          String deletedDate = subjectResolutionAttributeValue.getSubjectResolutionDateDelete();
          subjectResolutionAttributeValue.setDateSubjectWillBeDeletedString(deletedDate);
          
        } else {
        
          String sourceId = member.getSubjectSourceId();
          
          Integer deleteAfterDays = GrouperConfig.retrieveConfig().propertyValueInt("usdu.source."+sourceId+".delete.ifAfterDays");
          if (deleteAfterDays == null) {
            deleteAfterDays = GrouperConfig.retrieveConfig().propertyValueInt("usdu.delete.ifAfterDays", 30);
          }
          
            
          Long daysSubjectHasBeenUnresolved = subjectResolutionAttributeValue.getSubjectResolutionDaysUnresolved();
          Long daysAfterWhichSubjectWillBeDeleted = deleteAfterDays - daysSubjectHasBeenUnresolved;
          
          c.setTime(currentDate);
          c.add(Calendar.DATE, daysAfterWhichSubjectWillBeDeleted.intValue());
          String dateSubjectWillBeDeleted = dateFormat.format(c.getTime());
          
          subjectResolutionAttributeValue.setDateSubjectWillBeDeletedString(dateSubjectWillBeDeleted);
        }
  
        if (!StringUtils.isBlank(subjectResolutionAttributeValue.getSubjectResolutionDateLastResolvedString())) {
          Date lastResolvedDate = subjectResolutionAttributeValue.getSubjectResolutionDateLastResolved();
          int daysBetween = (int)(System.currentTimeMillis() - lastResolvedDate.getTime()) / (1000 * 60 * 60 * 24);
          subjectResolutionAttributeValue.setSubjectResolutionDaysUnresolvedString(Integer.toString(daysBetween));
        }
        
        unresolvedSubjects.add(subjectResolutionAttributeValue);
      }
    }
    
    return unresolvedSubjects;
    
  }
  
  
  /**
   * build subject resolution attribute object from member attributes
   * @param attributeAssign
   * @param member
   * @param nameOfAttributeDefNameToValue 
   * @return SubjectResolutionAttributeValue
   */
  private static SubjectResolutionAttributeValue buildSubjectResolutionAttributeValue(AttributeAssign attributeAssign, Member member, Map<String, String> nameOfAttributeDefNameToValue) {
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign == null ? null : attributeAssign.getAttributeValueDelegate();
    
    SubjectResolutionAttributeValue result = new SubjectResolutionAttributeValue();
    result.setMember(member);

    result.setSubjectResolutionResolvableString(BooleanUtils.toStringTrueFalse(member.isSubjectResolutionResolvable()));
    result.setSubjectResolutionDateLastResolvedString(buildSubjectResolutionValue(attributeValueDelegate, UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DATE_LAST_RESOLVED, nameOfAttributeDefNameToValue));
    result.setSubjectResolutionDaysUnresolvedString(buildSubjectResolutionValue(attributeValueDelegate, UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DAYS_UNRESOLVED, nameOfAttributeDefNameToValue));
    result.setSubjectResolutionDateLastCheckedString(buildSubjectResolutionValue(attributeValueDelegate, UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_LAST_CHECKED, nameOfAttributeDefNameToValue));
    result.setSubjectResolutionDeletedString(BooleanUtils.toStringTrueFalse(member.isSubjectResolutionDeleted()));
    result.setSubjectResolutionDateDeleteString(buildSubjectResolutionValue(attributeValueDelegate, UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DELETE_DATE, nameOfAttributeDefNameToValue));

    return result;
  }
  
  /**
   * get the value of the attribute
   * @param attributeValueDelegate
   * @param nameOfAttributeDefName
   * @param nameOfAttributeDefNameToValue
   * @return the value
   */
  private static String buildSubjectResolutionValue(AttributeValueDelegate attributeValueDelegate, String nameOfAttributeDefName, Map<String, String> nameOfAttributeDefNameToValue){
    if (attributeValueDelegate != null) {
      AttributeAssignValue assignValue = attributeValueDelegate.retrieveAttributeAssignValue(nameOfAttributeDefName);
      if (assignValue != null) {      
        return assignValue.getValueString();
      }
    } else {
      return nameOfAttributeDefNameToValue.get(nameOfAttributeDefName); 
    } 
    return null;
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
