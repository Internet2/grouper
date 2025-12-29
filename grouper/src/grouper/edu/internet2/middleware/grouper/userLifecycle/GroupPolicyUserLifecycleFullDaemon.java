package edu.internet2.middleware.grouper.userLifecycle;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MembershipDAO;
import edu.internet2.middleware.grouper.subj.SafeSubject;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

public class GroupPolicyUserLifecycleFullDaemon extends OtherJobBase {
  
  /** logger */
  protected static final Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(GroupPolicyUserLifecycleFullDaemon.class);
  
  
  /**
   * retrieve all groups that have lifecycle policy attached
   * @return
   */
  private Map<String, String> retrieveGroupsWithPolicies() {
    
    List<Object[]> groupsWithPolicies = new GcDbAccess()
        .sql("select group_name, group_id, value_string from grouper_aval_asn_asn_group_v gaaagv where attribute_def_name_name1 = ? and attribute_def_name_name2 = ?")
        .addBindVar(UserLifecycleAttributeNames.userLifecycleStemName() + ":" + UserLifecycleAttributeNames.USER_LIFECYCLE_POLICY_GROUP_MARKER)
        .addBindVar(UserLifecycleAttributeNames.userLifecycleStemName() + ":" + UserLifecycleAttributeNames.USER_LIFECYCLE_POLICY_GROUP_VALUE_CONFIG_ID)
        .selectList(Object[].class);
      
    // build a cache to be used later so that we don't have to make multiple sql queries
    Map<String, String> groupIdToPolicyConfigId = new HashMap<String, String>();
    for (Object[] groupWithPolicy: groupsWithPolicies) {
      
      String groupId =  GrouperUtil.stringValue(groupWithPolicy[1]);
      String policyConfigId =  GrouperUtil.stringValue(groupWithPolicy[2]);
      
      groupIdToPolicyConfigId.put(groupId, policyConfigId);
    }
    
    return groupIdToPolicyConfigId;
  }
  
  
  /**
   * retrieve lifecycle events for members of the groups that have a lifecycle policy attached
   * @param groupIds - group ids that have a lifecycle policy attached
   * @return - set of group id + member id + lifecycle event id
   */
  private List<GroupMemberEventRef> retrieveLifecycleEvents(Collection<String> groupIds) {
    Timestamp lastFullSyncSuccessStartTimestamp = new GcDbAccess().sql("select max(started_time) from grouper_loader_log where job_name = 'OTHER_JOB_groupPolicyUserLifecycleFullDaemon' and status = 'SUCCESS' ").select(Timestamp.class);
    if (lastFullSyncSuccessStartTimestamp == null) {
      Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);
      lastFullSyncSuccessStartTimestamp = Timestamp.from(oneDayAgo);
//      lastFullSyncSuccessStartTimestamp = new Timestamp(System.currentTimeMillis() - (5010*24*60*60*1000)); // work on all events that happened after yesterday.
    }
    
    //
    String sql = """
        select gle.internal_id, gle.grpr_lcycl_evnt_cnfg_intrnl_id, gmship.owner_group_id, gm.id from grouper_memberships gmship, grouper_members gm,
        grouper_fields gf, grouper_lifecycle_event gle where gmship.member_id = gm.id and gf.id = gmship.field_id and gle.member_internal_id = gm.internal_id and 
        gmship.mship_type = 'immediate' and gmship.enabled = 'T' and gf.name = 'members' and gle.event_micros > %d
        """.formatted(lastFullSyncSuccessStartTimestamp.getTime() * 1000);
    
    GcDbAccess dbAccess = new GcDbAccess().sql(sql).batchSize(50);
    dbAccess.addBindVars(groupIds);
    dbAccess.selectMultipleColumnName("gmship.owner_group_id");
    
    List<Object[]> lifecycleEvents = dbAccess.selectList(Object[].class);
    
    Set<GroupMemberEventRef> groupIdsMemberIdsLifecycleEventIds = new HashSet<>();
    for (Object[] lifecycleEvent: lifecycleEvents) {
      Long lifecycleEventInternalId = GrouperUtil.longValue(lifecycleEvent[0]);
      String groupId = GrouperUtil.stringValue(lifecycleEvent[2]);
      String memberId = GrouperUtil.stringValue(lifecycleEvent[3]);
      GroupMemberEventRef groupIdMemberIdLifecycleEventId = new GroupMemberEventRef(groupId, memberId, lifecycleEventInternalId);
      groupIdsMemberIdsLifecycleEventIds.add(groupIdMemberIdLifecycleEventId);
    }
    
    return new ArrayList<GroupPolicyUserLifecycleFullDaemon.GroupMemberEventRef>(groupIdsMemberIdsLifecycleEventIds);
  }
  
  private Map<MultiKey, Membership> retrieveMembershipsFromGroupIdsMemberIds(List<GroupMemberEventRef> groupMemberEventRefs) {
    
    List<String> groupIds = new ArrayList<String>();
    List<String> memberIds = new ArrayList<String>();
    for (GroupMemberEventRef groupMemberEventRef: groupMemberEventRefs) {
      groupIds.add(groupMemberEventRef.groupId);
      memberIds.add(groupMemberEventRef.memberId);
    }
    Set<Membership> allMemberships = Hib3MembershipDAO.findAllMemberships(groupIds, memberIds);
    
    Map<MultiKey, Membership> groupIdMemberIdToMembership = new HashMap<MultiKey, Membership>();
    for (Membership membership: allMemberships) {
      groupIdMemberIdToMembership.put(new MultiKey(membership.getOwnerGroupId(), membership.getMemberUuid()), membership);
    }
    return groupIdMemberIdToMembership;
  }
  
  
  /**
   * From the database, retrieve memberships where they already have in flight attributes assigned. call it alreadyHavingInFlightAttributes
   * Assign in flight attributes to the remaining ones. Assign in flight micros expire at the same time 
   * @param groupMemberEventRefs - memberships with the lifecycle event internal id that took place since the last run
   * @param policyConfigIdToPolicyBeans - policy config id to set of policy parts. one group can have one policy attached and the same policy can be in multiple policy parts. 
   * @param actionConfigIdToActionDetails - action config id to action details map so that we can easily look up action type 
   * @param lifecycleEventIdToLifecycleEventConfigId - lifecycle event internal id to lifecycle event config id. it's passed to reduce individual look ups
   * @param groupIdToPolicyConfigId - group id to policy config id map. each group can have at most one policy attached.
   * @param membershipsFromGroupIdsMemberIds - memberships look up based on group id/member id
   */
  private void workOnActionTypeAddEndDateOnMembership(List<GroupMemberEventRef> groupMemberEventRefs, 
      Map<String, Set<PolicyPartBean>> policyConfigIdToPolicyBeans, Map<String, ActionBean> actionConfigIdToActionDetails,
      Map<Long, String> lifecycleEventIdToLifecycleEventConfigId, Map<String, String> groupIdToPolicyConfigId,
      Map<MultiKey, Membership> membershipsFromGroupIdsMemberIds) {
    
    List<GroupMemberEventRef> refsWithAddEndDateOnMembershipAction = new ArrayList<>();
    
    Map<GroupMemberEventRef, ActionBean> groupMemberEventRefToActionBean = new HashMap<>();//we're going to use it later when populating micros expire attribute value
    
    for (GroupMemberEventRef groupMemberEventRef: groupMemberEventRefs) {
      
      String lifecycleEventConfig = lifecycleEventIdToLifecycleEventConfigId.get(groupMemberEventRef.lifecycleEventInternalId);
      String policyConfigId = groupIdToPolicyConfigId.get(groupMemberEventRef.groupId);
      
      if (policyConfigIdToPolicyBeans.containsKey(policyConfigId)) {
        
        Set<PolicyPartBean> policyBeans = policyConfigIdToPolicyBeans.get(policyConfigId); //policy bean is basically the policy part. since one policy config id can be attached to multiple policy part ids, that's why it's a set
        
        for (PolicyPartBean policyBean: policyBeans) {
          if (policyBean.lifecycleEventConfigIds.contains(lifecycleEventConfig)) {
            //this is the policy part where the lifecycle config is used and now we need to perform all the actions
            
            for (String lifecycleActionConfigId : policyBean.lifecycleActionConfigIds) {
              //now get the action details and perform the action
              
              ActionBean actionBean = actionConfigIdToActionDetails.get(lifecycleActionConfigId);
              if (actionBean != null && Strings.CS.equals(actionBean.actionType, "addEndDateOnMembership")) {
                groupMemberEventRefToActionBean.put(groupMemberEventRef, actionBean);
                refsWithAddEndDateOnMembershipAction.add(groupMemberEventRef);
              }
            }
          }
        }
      }
      
    }
    
    int batchSize = 50;
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(refsWithAddEndDateOnMembershipAction, batchSize, false);
    
    //retrieve in flight membership attributes 
    List<Object[]> membershipAttributes = new ArrayList<Object[]>();
    for (int i=0; i<numberOfBatches; i++) {
      
      List<GroupMemberEventRef> oneBatchOfGroupIdsMemberIds = GrouperUtil.batchList(refsWithAddEndDateOnMembershipAction, batchSize, i);
      GcDbAccess gcDbAccess = new GcDbAccess();
      StringBuilder sqlBuilder = new StringBuilder("select gaaamv.attribute_def_name_name2, gaaamv.value_integer, gaaamv.group_id, gaaamv.member_id from grouper_aval_asn_asn_mship_v gaaamv where gaaamv.attribute_def_name_name1 = ? and ( ");
      gcDbAccess.addBindVar(UserLifecycleAttributeNames.userLifecycleStemName() +":"+ UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_MARKER);
      boolean first = true;
      for (GroupMemberEventRef groupIdMemberIdLifecycleEventId: oneBatchOfGroupIdsMemberIds) {
        if (!first) {
          sqlBuilder.append(" or ");
        }
        sqlBuilder.append(" (gaaamv.group_id = ? and gaaamv.member_id = ?) ");
        first = false;
        
        gcDbAccess.addBindVar(groupIdMemberIdLifecycleEventId.groupId).addBindVar(groupIdMemberIdLifecycleEventId.memberId);
      }
      
      sqlBuilder.append(" ) ");
      
      membershipAttributes.addAll(gcDbAccess.sql(sqlBuilder.toString()).selectList(Object[].class));
      
    }
    
    List<GroupMemberEventRef> alreadyHavingInFlightAttributes = new ArrayList<>();
    
//    Set<MultiKey> groupIdsMemberIdsLifecycleEventIdsOnWhichToPeformActions = new HashSet<MultiKey>();
    
    for (Object[] membershipAttribute: membershipAttributes) {
      String attributeDefName = GrouperUtil.stringValue(membershipAttribute[0]);
      Long value = GrouperUtil.longObjectValue(membershipAttribute[1], true);
      String groupId = GrouperUtil.stringValue(membershipAttribute[2]);
      String memberId = GrouperUtil.stringValue(membershipAttribute[3]);
      
      // we are skipping the ones that already have marker (from sql) but not the in flight lifecycle event id
      if (!StringUtils.equals(attributeDefName, UserLifecycleAttributeNames.userLifecycleStemName() +":"+ UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_LIFECYCLE_EVENT_ID)) { 
        continue;
      }
      
      GroupMemberEventRef groupIdMemberIdLifecycleEventId = new GroupMemberEventRef(groupId, memberId, value);
      alreadyHavingInFlightAttributes.add(groupIdMemberIdLifecycleEventId);
//      groupIdsMemberIdsLifecycleEventIdsOnWhichToPeformActions.add(groupIdMemberIdLifecycleEventId);
    }
    
    // now the remaining ones are where we actually need to assign in flight attributes
    refsWithAddEndDateOnMembershipAction.removeAll(alreadyHavingInFlightAttributes);
    
    for (GroupMemberEventRef groupMemberEventRef: refsWithAddEndDateOnMembershipAction) {
      
      ActionBean actionBean = groupMemberEventRefToActionBean.get(groupMemberEventRef);
      
      MultiKey groupIdMemberId = new MultiKey(groupMemberEventRef.groupId, groupMemberEventRef.memberId);
      if (membershipsFromGroupIdsMemberIds.containsKey(groupIdMemberId)) {
        Membership membership = membershipsFromGroupIdsMemberIds.get(groupIdMemberId);
        if (membership != null) {
          AttributeAssign attributeAssign = membership.getAttributeDelegate().addAttribute(UserLifecycleAttributeNames.retrieveInFlightAttributeDefNameMarker()).getAttributeAssign();
          AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(UserLifecycleAttributeNames.userLifecycleStemName()+":"+UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_LIFECYCLE_EVENT_ID, true);
          attributeAssign.getAttributeValueDelegate().assignValueInteger(attributeDefName.getName(), groupMemberEventRef.lifecycleEventInternalId);
          
          attributeDefName = AttributeDefNameFinder.findByName(UserLifecycleAttributeNames.userLifecycleStemName()+":"+UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_ADDED_MICROS, true);
          attributeAssign.getAttributeValueDelegate().assignValueInteger(attributeDefName.getName(), System.currentTimeMillis() * 1000);
          
          Instant instant = Instant.now().plus(actionBean.numberOfDaysInTheFuture, ChronoUnit.DAYS);
          long microsInFuture = instant.getEpochSecond() * 1000L * 1000L;
          
          attributeDefName = AttributeDefNameFinder.findByName(UserLifecycleAttributeNames.userLifecycleStemName()+":"+UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_MICROS_EXPIRE, true);
          attributeAssign.getAttributeValueDelegate().assignValueInteger(attributeDefName.getName(), microsInFuture);
          
          attributeAssign.saveOrUpdate();
        }
      }
    }
    
    
  }
  
  /**
   * From the database, retrieve memberships where they already have in flight attributes assigned. call it alreadyHavingInFlightAttributes
   * Assign in flight attributes to the remaining ones. Assign in flight micros expire at the same time 
   * @param groupMemberEventRefs - memberships with the lifecycle event internal id that took place since the last run
   * @param policyConfigIdToPolicyBeans - policy config id to set of policy parts. one group can have one policy attached and the same policy can be in multiple policy parts. 
   * @param actionConfigIdToActionDetails - action config id to action details map so that we can easily look up action type 
   * @param lifecycleEventIdToLifecycleEventConfigId - lifecycle event internal id to lifecycle event config id. it's passed to reduce individual look ups
   * @param groupIdToPolicyConfigId - group id to policy config id map. each group can have at most one policy attached.
   * @param membershipsFromGroupIdsMemberIds - memberships look up based on group id/member id
   */
  private void workOnActionTypeRemoveUserFromGroup(List<GroupMemberEventRef> groupMemberEventRefs, 
      Map<String, Set<PolicyPartBean>> policyConfigIdToPolicyBeans, Map<String, ActionBean> actionConfigIdToActionDetails,
      Map<Long, String> lifecycleEventIdToLifecycleEventConfigId, Map<String, String> groupIdToPolicyConfigId,
      Map<MultiKey, Membership> membershipsFromGroupIdsMemberIds) {
    
    List<GroupMemberEventRef> refsWithRemoveUserFromGroupAction = new ArrayList<>();
    
    for (GroupMemberEventRef groupMemberEventRef: groupMemberEventRefs) {
      
      String lifecycleEventConfig = lifecycleEventIdToLifecycleEventConfigId.get(groupMemberEventRef.lifecycleEventInternalId);
      String policyConfigId = groupIdToPolicyConfigId.get(groupMemberEventRef.groupId);
      
      if (policyConfigIdToPolicyBeans.containsKey(policyConfigId)) {
        
        Set<PolicyPartBean> policyBeans = policyConfigIdToPolicyBeans.get(policyConfigId); //policy bean is basically the policy part. since one policy config id can be attached to multiple policy part ids, that's why it's a set
        
        for (PolicyPartBean policyBean: policyBeans) {
          if (policyBean.lifecycleEventConfigIds.contains(lifecycleEventConfig)) {
            //this is the policy part where the lifecycle config is used and now we need to perform all the actions
            
            for (String lifecycleActionConfigId : policyBean.lifecycleActionConfigIds) {
              //now get the action details and perform the action
              
              ActionBean actionBean = actionConfigIdToActionDetails.get(lifecycleActionConfigId);
              if (actionBean != null && Strings.CS.equals(actionBean.actionType, "removeUserFromGroup")) {
                refsWithRemoveUserFromGroupAction.add(groupMemberEventRef);
              }
            }
          }
        }
      }
      
    }
    
    
    for (GroupMemberEventRef groupMemberEventRef: refsWithRemoveUserFromGroupAction) {
      
      MultiKey groupIdMemberId = new MultiKey(groupMemberEventRef.groupId, groupMemberEventRef.memberId);
      if (membershipsFromGroupIdsMemberIds.containsKey(groupIdMemberId)) {
        Membership membership = membershipsFromGroupIdsMemberIds.get(groupIdMemberId);
        if (membership != null) {
         membership.delete();
        }
      }
    }
    
  }
  
  
  private void populateSubjectsAndGroups(Set<String> memberIds, Set<String> groupIds, Map<String, Subject> memberIdToSubject, Map<String, Group> groupIdToGroup) {
    
    for (String memberId: memberIds) {      
      Subject subject = new SubjectFinder().assignMemberId(memberId).findSubject();
      if (subject != null) {
        memberIdToSubject.put(memberId, subject);
      }
    }
    
    boolean hasAtLeastOneGroupId = false;
    GroupFinder finder = new GroupFinder();
    for (String groupId: groupIds) {
      finder.addGroupId(groupId);
      hasAtLeastOneGroupId = true;
    }
    
    if (hasAtLeastOneGroupId) {
      Set<Group> groups = finder.findGroups();
      
      for (Group group: groups) {
        groupIdToGroup.put(group.getId(), group);
      }
    }
    
  }
  
  private String getEmailAddressFromSubject(Subject subject) {
    
    String emailAddress = null;
    if (StringUtils.equals(subject.getType().getName(), SubjectTypeEnum.PERSON.getName())) {
      String emailAttributeName = GrouperEmailUtils.emailAttributeNameForSource(subject.getSourceId());
      if (!StringUtils.isBlank(emailAttributeName)) {
        emailAddress = subject.getAttributeValue(emailAttributeName);
      }
    }
    
    return emailAddress;
    
  }
  
  private List<String> getEmailAddressesFromSubjects(Set<Subject> subjects) {
    
    List<String> emailAddresses = new ArrayList<String>();
    for (Subject subject: subjects) {
      
      if (StringUtils.equals(subject.getType().getName(), SubjectTypeEnum.PERSON.getName())) {
        String emailAttributeName = GrouperEmailUtils.emailAttributeNameForSource(subject.getSourceId());
        if (!StringUtils.isBlank(emailAttributeName)) {
          String emailAddress = subject.getAttributeValue(emailAttributeName);
          emailAddresses.add(emailAddress);
        }
      }
      
      
    }
    
    return emailAddresses;
  }
  
  /**
   * @param groupMemberEventRefs - memberships with the lifecycle event internal id that took place since the last run
   * @param policyConfigIdToPolicyBeans - policy config id to set of policy parts. one group can have one policy attached and the same policy can be in multiple policy parts. 
   * @param actionConfigIdToActionDetails - action config id to action details map so that we can easily look up action type 
   * @param lifecycleEventIdToLifecycleEventConfigId - lifecycle event internal id to lifecycle event config id. it's passed to reduce individual look ups
   * @param groupIdToPolicyConfigId - group id to policy config id map. each group can have at most one policy attached.
   * @param membershipsFromGroupIdsMemberIds - memberships look up based on group id/member id
   */
  private void workOnActionTypeEmailUser(List<GroupMemberEventRef> groupMemberEventRefs, 
      Map<String, Set<PolicyPartBean>> policyConfigIdToPolicyBeans, Map<String, ActionBean> actionConfigIdToActionDetails,
      Map<Long, String> lifecycleEventIdToLifecycleEventConfigId, Map<String, String> groupIdToPolicyConfigId,
      Map<MultiKey, Membership> membershipsFromGroupIdsMemberIds) {
    
    Set<String> memberIds = new HashSet<>();
    Set<String> groupIds = new HashSet<>();
    
    List<GroupMemberEventRef> eligibleGroupMemberEventRefs = new ArrayList<>();
    for (GroupMemberEventRef groupMemberEventRef: groupMemberEventRefs) {
      
      String lifecycleEventConfig = lifecycleEventIdToLifecycleEventConfigId.get(groupMemberEventRef.lifecycleEventInternalId);
      String policyConfigId = groupIdToPolicyConfigId.get(groupMemberEventRef.groupId);
      
      if (policyConfigIdToPolicyBeans.containsKey(policyConfigId)) {
        
        Set<PolicyPartBean> policyBeans = policyConfigIdToPolicyBeans.get(policyConfigId); //policy bean is basically the policy part. since one policy config id can be attached to multiple policy part ids, that's why it's a set
        
        for (PolicyPartBean policyBean: policyBeans) {
          if (policyBean.lifecycleEventConfigIds.contains(lifecycleEventConfig)) {
            //this is the policy part where the lifecycle config is used and now we need to perform all the actions
            
            for (String lifecycleActionConfigId : policyBean.lifecycleActionConfigIds) {
              //now get the action details and perform the action
              
              ActionBean actionBean = actionConfigIdToActionDetails.get(lifecycleActionConfigId);
              if (actionBean != null && Strings.CS.equals(actionBean.actionType, "emailUser")) {
                eligibleGroupMemberEventRefs.add(groupMemberEventRef);
                memberIds.add(groupMemberEventRef.memberId);
                groupIds.add(groupMemberEventRef.groupId);
              }
            }
          }
        }
      }
    }
    
    Map<String, Subject> memberIdToSubject = new HashMap<>(); 
    
    Map<String, Group> groupIdToGroup = new HashMap<>(); 
    
    populateSubjectsAndGroups(memberIds, groupIds, memberIdToSubject, groupIdToGroup);
    
    Map<MultiKey, List<Map<String, Object>>> actionConfigIdEmailAddressToListOfRecordMaps = new HashMap<>();
    
    for (GroupMemberEventRef groupMemberEventRef: eligibleGroupMemberEventRefs) {
      
      Subject subject = memberIdToSubject.get(groupMemberEventRef.memberId);
      Group group = groupIdToGroup.get(groupMemberEventRef.groupId);
      
      if (subject == null || group == null) {
        continue;
      }
      
      String lifecycleEventConfig = lifecycleEventIdToLifecycleEventConfigId.get(groupMemberEventRef.lifecycleEventInternalId);
      String policyConfigId = groupIdToPolicyConfigId.get(groupMemberEventRef.groupId);
      
      if (policyConfigIdToPolicyBeans.containsKey(policyConfigId)) {
        
        Set<PolicyPartBean> policyBeans = policyConfigIdToPolicyBeans.get(policyConfigId); //policy bean is basically the policy part. since one policy config id can be attached to multiple policy part ids, that's why it's a set
        
        for (PolicyPartBean policyBean: policyBeans) {
          if (policyBean.lifecycleEventConfigIds.contains(lifecycleEventConfig)) {
            //this is the policy part where the lifecycle config is used and now we need to perform all the actions
            
            for (String lifecycleActionConfigId : policyBean.lifecycleActionConfigIds) {
              //now get the action details and perform the action
              ActionBean actionBean = actionConfigIdToActionDetails.get(lifecycleActionConfigId);
              if (actionBean != null && Strings.CS.equals(actionBean.actionType, "emailUser")) {
                
                String emailAddressFromSubject = getEmailAddressFromSubject(subject);
                if (StringUtils.isNotBlank(emailAddressFromSubject)) {
                  
                  MultiKey actionConfigIdEmailAddress = new MultiKey(lifecycleActionConfigId, emailAddressFromSubject);
                  
                  if (actionConfigIdEmailAddressToListOfRecordMaps.containsKey(actionConfigIdEmailAddress)) {
                    List<Map<String, Object>> listOfRecordMaps = actionConfigIdEmailAddressToListOfRecordMaps.get(actionConfigIdEmailAddress);
                    
                    Map<String, Object> recordMap = new HashMap<String, Object>();
                    
                    SafeSubject safeSubject = new SafeSubject(subject);
                    recordMap.put("safeSubjectRecipient", safeSubject);
                    recordMap.put("safeSubjectLifecycleUser", safeSubject);
                    recordMap.put("groupDescription", group.getDescription());
                    recordMap.put("groupDisplayExtension", group.getDisplayExtension());
                    recordMap.put("groupDisplayName", group.getDisplayName());
                    recordMap.put("groupExtension", group.getExtension());
                    recordMap.put("groupId", group.getId());
                    recordMap.put("groupName", group.getName());
                    
                    listOfRecordMaps.add(recordMap);
                    
                  } else {
                    List<Map<String, Object>> listOfRecordMaps = new ArrayList<Map<String, Object>>();
                    
                    Map<String, Object> recordMap = new HashMap<String, Object>();
                    
                    SafeSubject safeSubject = new SafeSubject(subject);
                    recordMap.put("safeSubjectRecipient", safeSubject);
                    recordMap.put("safeSubjectLifecycleUser", safeSubject);
                    recordMap.put("groupDescription", group.getDescription());
                    recordMap.put("groupDisplayExtension", group.getDisplayExtension());
                    recordMap.put("groupDisplayName", group.getDisplayName());
                    recordMap.put("groupExtension", group.getExtension());
                    recordMap.put("groupId", group.getId());
                    recordMap.put("groupName", group.getName());
                    
                    listOfRecordMaps.add(recordMap);
                    
                    actionConfigIdEmailAddressToListOfRecordMaps.put(actionConfigIdEmailAddress, listOfRecordMaps);
                  }
                }
              }
            }
          }
        }
      }
    }
    
    //now we've the data structure ready to send emails
    for (MultiKey actionConfigIdEmailAddress: actionConfigIdEmailAddressToListOfRecordMaps.keySet()) {
      
      List<Map<String, Object>> listOfRecordMaps = actionConfigIdEmailAddressToListOfRecordMaps.get(actionConfigIdEmailAddress);
      
      if (listOfRecordMaps.size() == 0) {
        continue;
      }
      
      ActionBean actionBean = actionConfigIdToActionDetails.get(actionConfigIdEmailAddress.getKey(0));
      
      Map<String, Object> variableMap = new HashMap<String, Object>();
      variableMap.put("listOfRecordMaps", listOfRecordMaps);
      
      String subjectText = GrouperUtil.substituteExpressionLanguageTemplate(actionBean.emailSubjectLine, variableMap, true, false, true);
      String emailBodyTemplate = GrouperUtil.replace(actionBean.emailBody, "__NEWLINE__", "\n");
      String bodyText = GrouperUtil.substituteExpressionLanguageTemplate(emailBodyTemplate, variableMap, true, false, true);

      GrouperEmail grouperEmail = new GrouperEmail().setSubject(subjectText).setBody(bodyText);
      
      grouperEmail.setTo(GrouperUtil.stringValue(actionConfigIdEmailAddress.getKey(1)));
      
      grouperEmail.send();
      
    }
    
    
  }
  
  /**
   * @param groupMemberEventRefs - memberships with the lifecycle event internal id that took place since the last run
   * @param policyConfigIdToPolicyBeans - policy config id to set of policy parts. one group can have one policy attached and the same policy can be in multiple policy parts. 
   * @param actionConfigIdToActionDetails - action config id to action details map so that we can easily look up action type 
   * @param lifecycleEventIdToLifecycleEventConfigId - lifecycle event internal id to lifecycle event config id. it's passed to reduce individual look ups
   * @param groupIdToPolicyConfigId - group id to policy config id map. each group can have at most one policy attached.
   * @param membershipsFromGroupIdsMemberIds - memberships look up based on group id/member id
   */
  private void workOnActionTypeEmailGroupAdmin(List<GroupMemberEventRef> groupMemberEventRefs, 
      Map<String, Set<PolicyPartBean>> policyConfigIdToPolicyBeans, Map<String, ActionBean> actionConfigIdToActionDetails,
      Map<Long, String> lifecycleEventIdToLifecycleEventConfigId, Map<String, String> groupIdToPolicyConfigId,
      Map<MultiKey, Membership> membershipsFromGroupIdsMemberIds) {
    
    Set<String> memberIds = new HashSet<>();
    Set<String> groupIds = new HashSet<>();
    
    List<GroupMemberEventRef> eligibleGroupMemberEventRefs = new ArrayList<>();
    for (GroupMemberEventRef groupMemberEventRef: groupMemberEventRefs) {
      
      String lifecycleEventConfig = lifecycleEventIdToLifecycleEventConfigId.get(groupMemberEventRef.lifecycleEventInternalId);
      String policyConfigId = groupIdToPolicyConfigId.get(groupMemberEventRef.groupId);
      
      if (policyConfigIdToPolicyBeans.containsKey(policyConfigId)) {
        
        Set<PolicyPartBean> policyBeans = policyConfigIdToPolicyBeans.get(policyConfigId); //policy bean is basically the policy part. since one policy config id can be attached to multiple policy part ids, that's why it's a set
        
        for (PolicyPartBean policyBean: policyBeans) {
          if (policyBean.lifecycleEventConfigIds.contains(lifecycleEventConfig)) {
            //this is the policy part where the lifecycle config is used and now we need to perform all the actions
            
            for (String lifecycleActionConfigId : policyBean.lifecycleActionConfigIds) {
              //now get the action details and perform the action
              
              ActionBean actionBean = actionConfigIdToActionDetails.get(lifecycleActionConfigId);
              if (actionBean != null && Strings.CS.equals(actionBean.actionType, "emailGroupAdmin")) {
                eligibleGroupMemberEventRefs.add(groupMemberEventRef);
                memberIds.add(groupMemberEventRef.memberId);
                groupIds.add(groupMemberEventRef.groupId);
              }
            }
          }
        }
      }
    }
    
    Map<String, Subject> memberIdToSubject = new HashMap<>(); 
    
    Map<String, Group> groupIdToGroup = new HashMap<>(); 
    
    populateSubjectsAndGroups(memberIds, groupIds, memberIdToSubject, groupIdToGroup);
    
    Map<MultiKey, List<Map<String, Object>>> actionConfigIdEmailAddressToListOfRecordMaps = new HashMap<>();
    
    for (GroupMemberEventRef groupMemberEventRef: eligibleGroupMemberEventRefs) {
      
      Subject lifecycleSubject = memberIdToSubject.get(groupMemberEventRef.memberId);
      Group group = groupIdToGroup.get(groupMemberEventRef.groupId);
      
      if (lifecycleSubject == null || group == null) {
        continue;
      }
      
      Set<Subject> recipientSubjects = group.getAdmins();
      
      String lifecycleEventConfig = lifecycleEventIdToLifecycleEventConfigId.get(groupMemberEventRef.lifecycleEventInternalId);
      String policyConfigId = groupIdToPolicyConfigId.get(groupMemberEventRef.groupId);
      
      if (policyConfigIdToPolicyBeans.containsKey(policyConfigId)) {
        
        Set<PolicyPartBean> policyBeans = policyConfigIdToPolicyBeans.get(policyConfigId); //policy bean is basically the policy part. since one policy config id can be attached to multiple policy part ids, that's why it's a set
        
        for (PolicyPartBean policyBean: policyBeans) {
          if (policyBean.lifecycleEventConfigIds.contains(lifecycleEventConfig)) {
            //this is the policy part where the lifecycle config is used and now we need to perform all the actions
            
            for (String lifecycleActionConfigId : policyBean.lifecycleActionConfigIds) {
              //now get the action details and perform the action
              ActionBean actionBean = actionConfigIdToActionDetails.get(lifecycleActionConfigId);
              if (actionBean != null && Strings.CS.equals(actionBean.actionType, "emailGroupAdmin")) {
                
                List<String> emailAddressesFromSubjects = getEmailAddressesFromSubjects(recipientSubjects);
                if (!emailAddressesFromSubjects.isEmpty()) {
                  
                  for (String emailAddress: emailAddressesFromSubjects) {
                    
                    MultiKey actionConfigIdEmailAddress = new MultiKey(lifecycleActionConfigId, emailAddress);
                    
                    if (actionConfigIdEmailAddressToListOfRecordMaps.containsKey(actionConfigIdEmailAddress)) {
                      List<Map<String, Object>> listOfRecordMaps = actionConfigIdEmailAddressToListOfRecordMaps.get(actionConfigIdEmailAddress);
                      
                      Map<String, Object> recordMap = new HashMap<String, Object>();
                      
                      SafeSubject safeSubject = new SafeSubject(lifecycleSubject);
//                      recordMap.put("safeSubjectRecipient", );
                      recordMap.put("safeSubjectLifecycleUser", safeSubject);
                      recordMap.put("groupDescription", group.getDescription());
                      recordMap.put("groupDisplayExtension", group.getDisplayExtension());
                      recordMap.put("groupDisplayName", group.getDisplayName());
                      recordMap.put("groupExtension", group.getExtension());
                      recordMap.put("groupId", group.getId());
                      recordMap.put("groupName", group.getName());
                      
                      listOfRecordMaps.add(recordMap);
                      
                    } else {
                      List<Map<String, Object>> listOfRecordMaps = new ArrayList<Map<String, Object>>();
                      
                      Map<String, Object> recordMap = new HashMap<String, Object>();
                      
                      SafeSubject safeSubject = new SafeSubject(lifecycleSubject);
//                      recordMap.put("safeSubjectRecipient", safeSubject);
                      recordMap.put("safeSubjectLifecycleUser", safeSubject);
                      recordMap.put("groupDescription", group.getDescription());
                      recordMap.put("groupDisplayExtension", group.getDisplayExtension());
                      recordMap.put("groupDisplayName", group.getDisplayName());
                      recordMap.put("groupExtension", group.getExtension());
                      recordMap.put("groupId", group.getId());
                      recordMap.put("groupName", group.getName());
                      
                      listOfRecordMaps.add(recordMap);
                      
                      actionConfigIdEmailAddressToListOfRecordMaps.put(actionConfigIdEmailAddress, listOfRecordMaps);
                    }
                    
                  }
                  
 
                }
              }
            }
          }
        }
      }
    }
    
    //now we've the data structure ready to send emails
    for (MultiKey actionConfigIdEmailAddress: actionConfigIdEmailAddressToListOfRecordMaps.keySet()) {
      
      ActionBean actionBean = actionConfigIdToActionDetails.get(actionConfigIdEmailAddress.getKey(0));
      
      List<Map<String, Object>> listOfRecordMaps = actionConfigIdEmailAddressToListOfRecordMaps.get(actionConfigIdEmailAddress);
      if (listOfRecordMaps.size() == 0) {
        continue;
      }
      
      Map<String, Object> variableMap = new HashMap<String, Object>();
      variableMap.put("listOfRecordMaps", listOfRecordMaps);
      
      String subjectText = GrouperUtil.substituteExpressionLanguageTemplate(actionBean.emailSubjectLine, variableMap, true, false, true);
      String emailBodyTemplate = GrouperUtil.replace(actionBean.emailBody, "__NEWLINE__", "\n");
      String bodyText = GrouperUtil.substituteExpressionLanguageTemplate(emailBodyTemplate, variableMap, true, false, true);

      GrouperEmail grouperEmail = new GrouperEmail().setSubject(subjectText).setBody(bodyText);
      
      grouperEmail.setTo(GrouperUtil.stringValue(actionConfigIdEmailAddress.getKey(1)));
      
      grouperEmail.send();
      
    }
    
  }
  
  
  //TODO write unit tests
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    AttributeDef attributeDef = UserLifecycleAttributeNames.retrieveAttributeDefBaseDef();
    if (attributeDef == null) {
      LOG.error(UserLifecycleAttributeNames.userLifecycleStemName() + ":" + UserLifecycleAttributeNames.USER_LIFECYCLE_POLICY_GROUP_MARKER_DEF + " attribute def doesn't exist. Job will not proceed.");
      return null;
    }
    
    //Step 1 - retrieve list of groups that have lifecycle policies attached to them.
    Map<String, String> groupIdToPolicyConfigId = retrieveGroupsWithPolicies();
    
    if (groupIdToPolicyConfigId.isEmpty()) {
      return null;
    }
    
    
    //Step 2 - retrieve lifecycle events for groups that have policies attached to them and only retrieve events that took place after the most recent full sync
    List<GroupMemberEventRef> groupIdsMemberIdsLifecycleEventIds = retrieveLifecycleEvents(groupIdToPolicyConfigId.keySet());
    
    if (groupIdsMemberIdsLifecycleEventIds.isEmpty()) {
      return null;
    }
    
    Set<Long> lifecycleEventIds = new HashSet<Long>(); //these are the lifecycle event ids for which we need to retrieve lifecycle event config ids
    
    for (GroupMemberEventRef groupIdMemberIdLifecycleEventId: groupIdsMemberIdsLifecycleEventIds) {
      lifecycleEventIds.add(groupIdMemberIdLifecycleEventId.lifecycleEventInternalId);
    }
    
    // Prepare metadata for step 3 and beyond.
    //retrieve lifecycle event config ids at once so that we don't have to make multiple sql queries
    String sqlToRetrieveLifecycleEventConfig = """
        select gle.internal_id, gle.grpr_lcycl_evnt_cnfg_intrnl_id, glec.config_id from grouper_lifecycle_event gle, grouper_lifecycle_event_config glec where glec.internal_id = gle.grpr_lcycl_evnt_cnfg_intrnl_id 
        """;
    
    GcDbAccess dbAccess = new GcDbAccess().sql(sqlToRetrieveLifecycleEventConfig).batchSize(50);
    dbAccess.addBindVars(lifecycleEventIds);
    dbAccess.selectMultipleColumnName("gle.internal_id");
    
    List<Object[]> lifecycleEventsWithConfigs = dbAccess.selectList(Object[].class);
    
    Map<Long, String> lifecycleEventIdToLifecycleEventConfigId = new HashMap<Long, String>();
    for (Object[] objectArray: lifecycleEventsWithConfigs) {
      
      Long lifecycleEventId = GrouperUtil.longObjectValue(objectArray[0], false);
      String lifecycleEventConfigId = GrouperUtil.stringValue(objectArray[2]);
      
      lifecycleEventIdToLifecycleEventConfigId.put(lifecycleEventId, lifecycleEventConfigId);
      
    }
    
    Map<String, ActionBean> actionConfigIdToActionDetails = retrieveActionConfigIdToActionDetails();
    
    Map<String, Set<PolicyPartBean>> policyConfigIdToPolicyBeans = retrievePolicyConfigIdToPolicyBeans(new HashSet<String>(groupIdToPolicyConfigId.values())); //we're only interested in policies that are attached to the group
    
    Map<MultiKey,Membership> membershipsFromGroupIdsMemberIds = retrieveMembershipsFromGroupIdsMemberIds(groupIdsMemberIdsLifecycleEventIds);
    
    //Step 3 - Work on group id, member id, and lifecycle event id from above where action type is addEndDateOnMembership
    workOnActionTypeAddEndDateOnMembership(groupIdsMemberIdsLifecycleEventIds, policyConfigIdToPolicyBeans, 
        actionConfigIdToActionDetails, lifecycleEventIdToLifecycleEventConfigId, 
        groupIdToPolicyConfigId, membershipsFromGroupIdsMemberIds);
    
    
    //Step 4 - Work on group id, member id, and lifecycle event id from above where action type is removeUserFromGroup
    workOnActionTypeRemoveUserFromGroup(groupIdsMemberIdsLifecycleEventIds, policyConfigIdToPolicyBeans, 
        actionConfigIdToActionDetails, lifecycleEventIdToLifecycleEventConfigId, 
        groupIdToPolicyConfigId, membershipsFromGroupIdsMemberIds);
    
    
    //Step 5 - Work on group id, member id, and lifecycle event id from above where action type is emailUser
    workOnActionTypeEmailUser(groupIdsMemberIdsLifecycleEventIds, policyConfigIdToPolicyBeans, 
        actionConfigIdToActionDetails, lifecycleEventIdToLifecycleEventConfigId, 
        groupIdToPolicyConfigId, membershipsFromGroupIdsMemberIds);
    
    //Step 6 - Work on group id, member id, and lifecycle event id from above where action type is emailGroupAdmin
    workOnActionTypeEmailGroupAdmin(groupIdsMemberIdsLifecycleEventIds, policyConfigIdToPolicyBeans, 
        actionConfigIdToActionDetails, lifecycleEventIdToLifecycleEventConfigId, 
        groupIdToPolicyConfigId, membershipsFromGroupIdsMemberIds);
    
    // Step 7 - Remove memberships where in flight micros expire is in the past. It should work on all memberships where USER_LIFECYCLE_MSHIP_IN_FLIGHT_MICROS_EXPIRE is assigned
    GcDbAccess gcDbAccess = new GcDbAccess();
    Instant instant = Instant.now();
    long microsNow = instant.getEpochSecond() * 1000L * 1000L;
    
    StringBuilder sqlBuilder = new StringBuilder("select gaaamv.group_id, gaaamv.member_id from grouper_aval_asn_asn_mship_v gaaamv where gaaamv.attribute_def_name_name1 = ? and  gaaamv.attribute_def_name_name2 = ? and value_integer < ? ");
    gcDbAccess.addBindVar(UserLifecycleAttributeNames.userLifecycleStemName() +":"+ UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_MARKER);
    gcDbAccess.addBindVar(UserLifecycleAttributeNames.userLifecycleStemName() +":"+ UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_MICROS_EXPIRE);
    gcDbAccess.addBindVar(microsNow);
    
    List<String[]> groupIdsMemberIds = gcDbAccess.sql(sqlBuilder.toString()).selectList(String[].class);
    List<String> groupIds = new ArrayList<>();
    List<String> memberIds = new ArrayList<>();
    for (String[] groupIdMemberId: groupIdsMemberIds) {
      groupIds.add(groupIdMemberId[0]);
      memberIds.add(groupIdMemberId[1]);
    }
    
    Set<Membership> membershipsToDelete = Hib3MembershipDAO.findAllMemberships(groupIds, memberIds);
    
    for (Membership membership: membershipsToDelete) {
      membership.delete();
    }
    
    
    return null;
  }
  
  
  private Map<String, ActionBean> retrieveActionConfigIdToActionDetails() {
    Map<String, ActionBean> actionConfigIdToActionDetails = new HashMap<String, GroupPolicyUserLifecycleFullDaemon.ActionBean>();
    
    List<UserLifecycleActionConfiguration> lifecycleActionConfigurations = UserLifecycleActionConfiguration.retrieveAllUserLifecycleActionConfigurations();
    
    for (UserLifecycleActionConfiguration userLifecycleActionConfiguration: lifecycleActionConfigurations) {
      
      Map<String, GrouperConfigurationModuleAttribute> attributes = userLifecycleActionConfiguration.retrieveAttributes();
      GrouperConfigurationModuleAttribute actionTypeAttribute = attributes.get("actionType");
      
      ActionBean actionBean = new ActionBean();
      actionBean.configId = userLifecycleActionConfiguration.getConfigId();
      actionBean.actionType = actionTypeAttribute.getValueOrExpressionEvaluationValue();
      if (Strings.CS.equals(actionBean.actionType, "addEndDateOnMembership")) {
        GrouperConfigurationModuleAttribute numberOfDaysInTheFutureAttribute = attributes.get("numberOfDaysInTheFuture");
        int numberOfDaysInTheFuture = GrouperUtil.intValue(numberOfDaysInTheFutureAttribute.getValueOrExpressionEvaluationValue(), 0);
        actionBean.numberOfDaysInTheFuture = numberOfDaysInTheFuture;
      } else if (Strings.CS.equals(actionBean.actionType, "emailManager")) {
        GrouperConfigurationModuleAttribute dataFieldConfigIdAttribute = attributes.get("dataFieldConfigId");
        String dataFieldConfigId = dataFieldConfigIdAttribute.getValueOrExpressionEvaluationValue();
        actionBean.dataFieldConfigId = dataFieldConfigId;

        GrouperConfigurationModuleAttribute subjectIdIdentifierAttribute = attributes.get("subjectIdIdentifier");
        String subjectIdIdentifier = subjectIdIdentifierAttribute.getValueOrExpressionEvaluationValue();
        actionBean.subjectIdIdentifier = subjectIdIdentifier;

        GrouperConfigurationModuleAttribute subjectSourceAttribute = attributes.get("subjectSource");
        String subjectSource = subjectSourceAttribute.getValueOrExpressionEvaluationValue();
        actionBean.subjectSource = subjectSource;
        
        GrouperConfigurationModuleAttribute emailSubjectLineAttribute = attributes.get("emailSubjectLine");
        String emailSubjectLine = emailSubjectLineAttribute.getValueOrExpressionEvaluationValue();
        actionBean.emailSubjectLine = emailSubjectLine;

        GrouperConfigurationModuleAttribute emailBodyAttribute = attributes.get("emailBody");
        String emailBody = emailBodyAttribute.getValueOrExpressionEvaluationValue();
        actionBean.emailBody = emailBody;
      } else if (Strings.CS.equals(actionBean.actionType, "emailUser") || Strings.CS.equals(actionBean.actionType, "emailGroupAdmin")) {
        
        GrouperConfigurationModuleAttribute emailSubjectLineAttribute = attributes.get("emailSubjectLine");
        String emailSubjectLine = emailSubjectLineAttribute.getValueOrExpressionEvaluationValue();
        actionBean.emailSubjectLine = emailSubjectLine;

        GrouperConfigurationModuleAttribute emailBodyAttribute = attributes.get("emailBody");
        String emailBody = emailBodyAttribute.getValueOrExpressionEvaluationValue();
        actionBean.emailBody = emailBody;
      }
      
      actionConfigIdToActionDetails.put(actionBean.configId, actionBean);
      
    }
    return actionConfigIdToActionDetails;
  }
  
  private Map<String, Set<PolicyPartBean>> retrievePolicyConfigIdToPolicyBeans(Set<String> policyConfigIds) {
    
    List<UserLifecyclePolicyPartConfiguration> policyPartConfigurations = 
        UserLifecyclePolicyPartConfiguration.retrieveAllUserLifecyclePolicyPartConfigurations();
    
    Map<String, Set<PolicyPartBean>> policyConfigIdToPolicyBeans = new HashMap<String, Set<PolicyPartBean>>();
    
    for (UserLifecyclePolicyPartConfiguration policyPartConfiguration: policyPartConfigurations) {
      
      Map<String, GrouperConfigurationModuleAttribute> attributes = policyPartConfiguration.retrieveAttributes();
      GrouperConfigurationModuleAttribute policyConfigIdAttribute = attributes.get("policy");
      
      String policyConfigId = policyConfigIdAttribute.getValueOrExpressionEvaluationValue();
      if (!policyConfigIds.contains(policyConfigId)) {
        continue;
      }
      
      PolicyPartBean policyBean = new PolicyPartBean();
      policyBean.policyPartConfigId = policyPartConfiguration.getConfigId();
      policyBean.policyConfigId = policyConfigId;
      policyBean.lifecycleEventConfigIds = new HashSet<String>();
      policyBean.lifecycleActionConfigIds = new HashSet<String>();

      GrouperConfigurationModuleAttribute numberOfLifecycleEventsAttribute = attributes.get("numberOfLifecycleEvents");
      
      int numberOfLifecycleEvents = GrouperUtil.intValue(numberOfLifecycleEventsAttribute.getValueOrExpressionEvaluationValue(), 0);
      for (int i=0; i<numberOfLifecycleEvents; i++) {
        GrouperConfigurationModuleAttribute lifecycleEventConfigIdAttribute = attributes.get("lifeCycleEvents."+i+".lifeCycleEventConfig");
        String valueOrExpressionEvaluationValue = lifecycleEventConfigIdAttribute.getValueOrExpressionEvaluationValue();
        
        if (StringUtils.isNotBlank(valueOrExpressionEvaluationValue)) {
          policyBean.lifecycleEventConfigIds.add(valueOrExpressionEvaluationValue);
        }
        
      }
      
      GrouperConfigurationModuleAttribute numberOfLifecycleActionsAttribute = attributes.get("numberOfLifecycleActions");
      
      int numberOfLifecycleActions = GrouperUtil.intValue(numberOfLifecycleActionsAttribute.getValueOrExpressionEvaluationValue(), 0);
      for (int i=0; i<numberOfLifecycleActions; i++) {
        GrouperConfigurationModuleAttribute lifecycleActionConfigIdAttribute = attributes.get("lifeCycleActions."+i+".lifeCycleActionConfig");
        String valueOrExpressionEvaluationValue = lifecycleActionConfigIdAttribute.getValueOrExpressionEvaluationValue();
        
        if (StringUtils.isNotBlank(valueOrExpressionEvaluationValue)) {
          policyBean.lifecycleActionConfigIds.add(valueOrExpressionEvaluationValue);
        }
        
      }
      
      if (policyConfigIdToPolicyBeans.containsKey(policyConfigId)) {
        policyConfigIdToPolicyBeans.get(policyConfigId).add(policyBean);
      } else {
        Set<PolicyPartBean> policyBeans = new HashSet<GroupPolicyUserLifecycleFullDaemon.PolicyPartBean>();
        policyBeans.add(policyBean);
        policyConfigIdToPolicyBeans.put(policyConfigId, policyBeans);
      }
      
    }
    return policyConfigIdToPolicyBeans;
  }
  
  class GroupMemberEventRef {
    String groupId;
    String memberId;
    Long lifecycleEventInternalId;
    
    public GroupMemberEventRef(String groupId, String memberId,
        Long lifecycleEventInternalId) {
      this.groupId = groupId;
      this.memberId = memberId;
      this.lifecycleEventInternalId = lifecycleEventInternalId;
    }
    
    @Override
    public int hashCode() {
      return new HashCodeBuilder()
          .append(this.groupId)
          .append(this.memberId)
          .append(this.lifecycleEventInternalId)
          .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof GroupMemberEventRef)) {
        return false;
      }
      GroupMemberEventRef groupMemberEventRef = (GroupMemberEventRef)obj;
      return new EqualsBuilder()
          .append(this.groupId, groupMemberEventRef.groupId)
          .append(this.memberId, groupMemberEventRef.memberId)
          .append(this.lifecycleEventInternalId, groupMemberEventRef.lifecycleEventInternalId)
          .isEquals();
    }
    
    
  }
  
  class PolicyPartBean { 
     
    String policyPartConfigId;
    String policyConfigId;
    Set<String> lifecycleEventConfigIds;
    Set<String> lifecycleActionConfigIds;
    
  }
  
  class ActionBean {
    
    String configId;
    String actionType;
    
    //attributes for different action types
    int numberOfDaysInTheFuture;
    String dataFieldConfigId;
    String subjectIdIdentifier;
    String subjectSource;
    String emailSubjectLine;
    String emailBody;
    
  }
  
  class EmailObject {
    
    String subjectLine;
    String emailBody;
    
    EmailObject(String subjectLine, String emailBody) {
      this.subjectLine = subjectLine;
      this.emailBody = emailBody;
    }
    
  }
  
}
