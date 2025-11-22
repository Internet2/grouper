package edu.internet2.middleware.grouper.userLifecycle;

import static edu.internet2.middleware.grouper.userLifecycle.UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_LIFECYCLE_EVENT_ID;
import static edu.internet2.middleware.grouper.userLifecycle.UserLifecycleAttributeNames.userLifecycleStemName;

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
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.membership.MembershipType;
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
  private Set<MultiKey> retrieveLifecycleEvents(Collection<String> groupIds) {
    Timestamp lastFullSyncSuccessStartTimestamp = new GcDbAccess().sql("select max(started_time) from grouper_loader_log where job_name = 'OTHER_JOB_groupPolicyUserLifecycleFullDaemon' and status = 'SUCCESS' ").select(Timestamp.class);
    lastFullSyncSuccessStartTimestamp = null;
    if (lastFullSyncSuccessStartTimestamp == null) { 
      Instant fiftyDaysAgo = Instant.now().minus(50, ChronoUnit.DAYS); // TODO remove 50 after testing. 
      lastFullSyncSuccessStartTimestamp = Timestamp.from(fiftyDaysAgo);
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
    
    Set<MultiKey> groupIdsMemberIdsLifecycleEventIds = new HashSet<MultiKey>();
    for (Object[] lifecycleEvent: lifecycleEvents) {
      Long lifecycleEventInternalId = GrouperUtil.longValue(lifecycleEvent[0]);
      String groupId = GrouperUtil.stringValue(lifecycleEvent[2]);
      String memberId = GrouperUtil.stringValue(lifecycleEvent[3]);
      MultiKey groupIdMemberIdLifecycleEventId = new MultiKey(groupId, memberId, lifecycleEventInternalId);
      groupIdsMemberIdsLifecycleEventIds.add(groupIdMemberIdLifecycleEventId);
    }
    
    return groupIdsMemberIdsLifecycleEventIds;
  }
  
  
  private void retrieveInFlightAttributesForMemberships() {
    
  }
  
  
  
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    
    AttributeDef attributeDef = UserLifecycleAttributeNames.retrieveAttributeDefBaseDef();
    if (attributeDef == null) {
      LOG.error(UserLifecycleAttributeNames.userLifecycleStemName() + ":" + UserLifecycleAttributeNames.USER_LIFECYCLE_POLICY_GROUP_MARKER_DEF + " attribute def doesn't exist. Job will not proceed.");
      return null;
    }
    
    //Step 1 - retrieve list of groups that have lifecycle policies attached to them.
    Map<String, String> groupIdToPolicyConfigId = retrieveGroupsWithPolicies();
    
    
    //Step 2 - retrieve lifecycle events for groups that have policies attached to them and only retrieve events that took place after the most recent full sync
    Set<MultiKey> groupIdsMemberIdsLifecycleEventIds = retrieveLifecycleEvents(groupIdToPolicyConfigId.keySet());
    
    //groupIdsMemberIdsLifecycleEventIds is all the group id member ids that need to have In Flight attributes. Some of them might already have them so let's try to retrieve them
    // whatever does not have the In 
    
    
    //Step 3 - We have group id, member id, and lifecycle event id from above. Now let's retrieve the in flight attributes assigned to these memberships (group id plus member id basically from above)
    // the idea is that we want to perform lifecycle actions (e.g send email, add expiration date, etc) for these memberships
    // the ones that already don't have the attributes; we need to assign to them in the next step
    
    int batchSize = 50;
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(groupIdsMemberIdsLifecycleEventIds, batchSize, false);
    
    List<MultiKey> groupIdsMemberIdsLifecycleEventIdsList = new ArrayList<MultiKey>(groupIdsMemberIdsLifecycleEventIds);
    
    //retrieve membership attributes for groups and their members that have lifecycle events
    List<Object[]> membershipAttributes = new ArrayList<Object[]>();
    for (int i=0; i<numberOfBatches; i++) {
      
      List<MultiKey> oneBatchOfGroupIdsMemberIds = GrouperUtil.batchList(groupIdsMemberIdsLifecycleEventIdsList, batchSize, i);
      GcDbAccess gcDbAccess = new GcDbAccess();
      StringBuilder sqlBuilder = new StringBuilder("select gaaamv.attribute_def_name_name2, gaaamv.value_integer, gaaamv.group_id, gaaamv.member_id from grouper_aval_asn_asn_mship_v gaaamv where gaaamv.attribute_def_name_name1 = ? and ( ");
      gcDbAccess.addBindVar(UserLifecycleAttributeNames.userLifecycleStemName() +":"+ UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_MARKER);
      boolean first = true;
      for (MultiKey groupIdMemberIdLifecycleEventId: oneBatchOfGroupIdsMemberIds) {
        if (!first) {
          sqlBuilder.append(" or ");
        }
        sqlBuilder.append(" (gaaamv.group_id = ? and gaaamv.member_id = ?) ");
        first = false;
        
        gcDbAccess.addBindVar(groupIdMemberIdLifecycleEventId.getKey(0)).addBindVar(groupIdMemberIdLifecycleEventId.getKey(1));
      }
      
      sqlBuilder.append(" ) ");
      
      membershipAttributes.addAll(gcDbAccess.sql(sqlBuilder.toString()).selectList(Object[].class));
      
    }
    
   
    //Step 4 - now we need to assign in flight attributes to those memberships that were fetched in step 2 and they didn't have the attributes assigned.
    
    Set<Long> lifecycleEventIds = new HashSet<Long>(); // these are the lifecycle event ids for which we need to retrieve lifecycle event config ids 
    
    Set<MultiKey> groupIdsMemberIdsLifecycleEventIdsListFromMembershipAttributes = new HashSet<MultiKey>();
    
    //the set below will be used in step 5
    Set<MultiKey> groupIdsMemberIdsLifecycleEventIdsOnWhichToPeformActions = new HashSet<MultiKey>();
    
    for (Object[] membershipAttribute: membershipAttributes) {
      String attributeDefName = GrouperUtil.stringValue(membershipAttribute[0]);
      Long value = GrouperUtil.longObjectValue(membershipAttribute[1], true);
      String groupId = GrouperUtil.stringValue(membershipAttribute[2]);
      String memberId = GrouperUtil.stringValue(membershipAttribute[3]);
      
      // we are skipping the ones that already have marker (from sql) but not the in flight lifecycle event id
      if (!StringUtils.equals(attributeDefName, UserLifecycleAttributeNames.userLifecycleStemName() +":"+ UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_LIFECYCLE_EVENT_ID)) { 
        continue;
      }
      
      MultiKey groupIdMemberIdLifecycleEventId = new MultiKey(groupId, memberId, value);
      groupIdsMemberIdsLifecycleEventIdsListFromMembershipAttributes.add(groupIdMemberIdLifecycleEventId);
      groupIdsMemberIdsLifecycleEventIdsOnWhichToPeformActions.add(groupIdMemberIdLifecycleEventId);
      lifecycleEventIds.add(value);
    }
    
    //it's like cache to reduce the number of membership look ups
    Map<MultiKey, Membership> groupIdMemberIdToMembership = new HashMap<MultiKey, Membership>(); 
    
    //we need to populate in flight lifecycle attributes for the members who already don't 
    for (MultiKey groupIdMemberIdLifecycleEventId : groupIdsMemberIdsLifecycleEventIdsList) {
      
      if (!groupIdsMemberIdsLifecycleEventIdsListFromMembershipAttributes.contains(groupIdMemberIdLifecycleEventId)) {
        // assign in flight attribute to that internal id of the user lifecycle event
        
        String groupId = GrouperUtil.stringValue(groupIdMemberIdLifecycleEventId.getKey(0));
        String memberId = GrouperUtil.stringValue(groupIdMemberIdLifecycleEventId.getKey(1));
        Long lifecycleEventId = GrouperUtil.longValue(groupIdMemberIdLifecycleEventId.getKey(2));
        
        MultiKey groupIdMemberId = new MultiKey(groupId, memberId);
        
        Membership membership = null;
        if (groupIdMemberIdToMembership.containsKey(groupIdMemberId)) {
          membership = groupIdMemberIdToMembership.get(groupIdMemberId);
        } else {
          membership = new MembershipFinder().addGroupId(groupId).addMemberId(memberId).assignMembershipType(MembershipType.IMMEDIATE).findMembership(false);
          groupIdMemberIdToMembership.put(groupIdMemberId, membership);
        }
        
//        groupIdMemberIdForMembershipFinder.add(groupIdMemberIdLifecycleEventId);
        
        if (membership != null) {
//          AttributeAssign attributeAssign = membership.getAttributeDelegate().retrieveAssignment("assign", UserLifecycleAttributeNames.retrieveInFlightAttributeDefNameMarker(), false, false);
//          if (attributeAssign == null) {
//          }
//          AttributeAssign attributeAssign = membership.getAttributeDelegate().assignAttribute(UserLifecycleAttributeNames.retrieveInFlightAttributeDefNameMarker()).getAttributeAssign();
          AttributeAssign attributeAssign = membership.getAttributeDelegate().addAttribute(UserLifecycleAttributeNames.retrieveInFlightAttributeDefNameMarker()).getAttributeAssign();
          AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(UserLifecycleAttributeNames.userLifecycleStemName()+":"+UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_LIFECYCLE_EVENT_ID, true);
          attributeAssign.getAttributeValueDelegate().assignValueInteger(attributeDefName.getName(), lifecycleEventId);
          
          attributeDefName = AttributeDefNameFinder.findByName(UserLifecycleAttributeNames.userLifecycleStemName()+":"+UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_ADDED_MICROS, true);
          attributeAssign.getAttributeValueDelegate().assignValueInteger(attributeDefName.getName(), System.currentTimeMillis() * 1000);
          
          attributeAssign.saveOrUpdate();
          
          lifecycleEventIds.add(lifecycleEventId);
          groupIdsMemberIdsLifecycleEventIdsOnWhichToPeformActions.add(groupIdMemberIdLifecycleEventId);
        }
        
      }
      
    }
    
    //Step 5 - now we have the memberships that we need to perform lifecycle actions for. To figure out which action to perform for which membership, we need 
    // 1 - get the lifecycle configs attached to the lifecycle events
    // 2 - figure out which policy part has the lifecycle event attached to it. these are the ones we need to consider only.
    // 3 - get the lifecycle actions from the policy parts retrieved in step 5.2 above
    
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
    
    Map<String, Set<PolicyBean>> policyConfigIdToPolicyBeans = retrievePolicyConfigIdToPolicyBeans(new HashSet<String>(groupIdToPolicyConfigId.values())); //we're only interested in policies that are attached to one group
    
    
    //Step 6 - This step is only if the action type is addEndDateOnMembership. It is to calculate value for USER_LIFECYCLE_MSHIP_IN_FLIGHT_MICROS_EXPIRE attribute to the membership
    
    Map<MultiKey, Long> groupIdMemberIdLifecycleEventIdToMicrosExpire = new HashMap<MultiKey, Long>(); 
    for (MultiKey groupIdMemberIdLifecycleEventId : groupIdsMemberIdsLifecycleEventIdsOnWhichToPeformActions) {
      
      String groupId = GrouperUtil.stringValue(groupIdMemberIdLifecycleEventId.getKey(0));
      Long lifecycleEventId = GrouperUtil.longValue(groupIdMemberIdLifecycleEventId.getKey(2));

      //get the lifecycle event config id from lifecycle event id
      String lifecycleEventConfig = lifecycleEventIdToLifecycleEventConfigId.get(lifecycleEventId);
      String policyConfigId = groupIdToPolicyConfigId.get(groupId);
      
      if (policyConfigIdToPolicyBeans.containsKey(policyConfigId)) {
        
        Set<PolicyBean> policyBeans = policyConfigIdToPolicyBeans.get(policyConfigId); //policy bean is basically the policy part. since one policy config id can be attached to multiple policy part ids, that's why it's a set
        
        for (PolicyBean policyBean: policyBeans) {
          if (policyBean.lifecycleEventConfigIds.contains(lifecycleEventConfig)) {
            //this is the policy part where the lifecycle config is used and now we need to perform all the actions
            
            for (String lifecycleActionConfigId : policyBean.lifecycleActionConfigIds) {
              //now get the action details and perform the action
              
              ActionBean actionBean = actionConfigIdToActionDetails.get(lifecycleActionConfigId);
              if (actionBean != null) {
                if (Strings.CS.equals(actionBean.actionType, "addEndDateOnMembership")) {
                  
                  Instant instant = Instant.now().plus(actionBean.numberOfDaysInTheFuture, ChronoUnit.DAYS);
                  long microsInFuture = instant.getEpochSecond() * 1000L * 1000L;
                  groupIdMemberIdLifecycleEventIdToMicrosExpire.put(groupIdMemberIdLifecycleEventId, microsInFuture);
                  
                }
              }
              
            }
          }
        }
        
      }
      
    }
    
    
    // if there are multiple micros values because of either multiple actions or multiple policy parts, pick the minimum one. basically, we want to remove the membership earliest in the future
    // if there's an action of remove member, then we dont need it. I
    // if USER_LIFECYCLE_MSHIP_IN_FLIGHT_MICROS_EXPIRE is already assigned and the value is the same as the minimum, then don't change it
   
//    for (MultiKey groupIdMemberIdLifecycleEventId : groupIdsMemberIdsLifecycleEventIdsOnWhichToPeformActions) {
//      
//      String groupId = GrouperUtil.stringValue(groupIdMemberIdLifecycleEventId.getKey(0));
//      String memberId = GrouperUtil.stringValue(groupIdMemberIdLifecycleEventId.getKey(1));
//      Long lifecycleEventId = GrouperUtil.longValue(groupIdMemberIdLifecycleEventId.getKey(2));
//
//      //get the lifecycle event config id from lifecycle event id
//      String lifecycleEventConfig = lifecycleEventIdToLifecycleEventConfigId.get(lifecycleEventId);
//      String policyConfigId = groupIdToPolicyConfigId.get(groupId);
//      
//      Set<MultiKey> groupIdMemberIdThatDoNotNeedMicrosExpireAttribute = new HashSet<MultiKey>();
//      
//      if (policyConfigIdToPolicyBeans.containsKey(policyConfigId)) {
//        
//        Set<PolicyBean> policyBeans = policyConfigIdToPolicyBeans.get(policyConfigId); //policy bean is basically the policy part. since one policy config id can be attached to multiple policy part ids, that's why it's a set
//        
//        for (PolicyBean policyBean: policyBeans) {
//          if (policyBean.lifecycleEventConfigIds.contains(lifecycleEventConfig)) {
//            //this is the policy part where the lifecycle config is used and now we need to perform all the actions
//            
//            for (String lifecycleActionConfigId : policyBean.lifecycleActionConfigIds) {
//              //now get the action details and perform the action
//              
//              ActionBean actionBean = actionConfigIdToActionDetails.get(lifecycleActionConfigId);
//              if (actionBean != null) {
//                MultiKey groupIdMemberId = new MultiKey(groupId, memberId);
//                if (Strings.CS.equals(actionBean.actionType, "addEndDateOnMembership") && !groupIdMemberIdThatDoNotNeedMicrosExpireAttribute.contains(groupIdMemberId)) {
//                  
//                  Instant instant = Instant.now().plus(actionBean.numberOfDaysInTheFuture, ChronoUnit.DAYS);
//                  long microsInFuture = instant.getEpochSecond() * 1000L * 1000L;
//                  if (groupIdMemberIdLifecycleEventIdToMicrosExpire.containsKey(groupIdMemberId)) {
//                    // we want to assign the minimum value 
//                    long existingValue = groupIdMemberIdLifecycleEventIdToMicrosExpire.get(groupIdMemberId);
//                    groupIdMemberIdLifecycleEventIdToMicrosExpire.put(groupIdMemberId, Math.min(microsInFuture, existingValue));
//                  } else {                    
//                    groupIdMemberIdLifecycleEventIdToMicrosExpire.put(groupIdMemberId, microsInFuture);
//                  }
//                  
//                } else if (Strings.CS.equals(actionBean.actionType, "removeUserFromGroup")) {
//                  groupIdMemberIdThatDoNotNeedMicrosExpireAttribute.add(groupIdMemberId);                  
//                  groupIdMemberIdLifecycleEventIdToMicrosExpire.remove(groupIdMemberId);
//                }
//              }
//              
//            }
//          }
//        }
//        
//      }
//      
//    }
    
    //Step 7 - now actually assign USER_LIFECYCLE_MSHIP_IN_FLIGHT_MICROS_EXPIRE value 
    for (MultiKey groupIdMemberIdLifecycleEventId: groupIdMemberIdLifecycleEventIdToMicrosExpire.keySet()) {
      
      String groupId = GrouperUtil.stringValue(groupIdMemberIdLifecycleEventId.getKey(0));
      String memberId = GrouperUtil.stringValue(groupIdMemberIdLifecycleEventId.getKey(1));
      Long lifecycleEventId = GrouperUtil.longValue(groupIdMemberIdLifecycleEventId.getKey(2));
      
      Membership membership = null;
      if (groupIdMemberIdToMembership.containsKey(new MultiKey(groupId, memberId))) {
        membership = groupIdMemberIdToMembership.get(new MultiKey(groupId, memberId));
      } else {
        membership = new MembershipFinder().addGroupId(groupId).addMemberId(memberId).assignMembershipType(MembershipType.IMMEDIATE).findMembership(false);
        groupIdMemberIdToMembership.put(new MultiKey(groupId, memberId), membership);
      }
      
      if (membership != null) {
        
        Set<AttributeAssign> existingInFlightAttributeAssignments = membership.getAttributeDelegate().retrieveAssignments(UserLifecycleAttributeNames.retrieveInFlightAttributeDefNameMarker());
        
        AttributeAssign attributeAssignThatWillStoreInFlightMicrosExpire = null; 
        //now we need to match on existing lifecycle event id so that we can attach USER_LIFECYCLE_MSHIP_IN_FLIGHT_MICROS_EXPIRE to the correct attribute assignment
        for (AttributeAssign attributeAssign: existingInFlightAttributeAssignments) {
          AttributeAssignValue existingInFlightLifecycleEventId = attributeAssign.getAttributeValueDelegate()
              .retrieveAttributeAssignValue(userLifecycleStemName()+":"+USER_LIFECYCLE_MSHIP_IN_FLIGHT_LIFECYCLE_EVENT_ID);
          if (existingInFlightLifecycleEventId == null || existingInFlightLifecycleEventId.getValueInteger() == null || !GrouperUtil.equals(existingInFlightLifecycleEventId.getValueInteger(), lifecycleEventId)) {
            continue;
          }
          
          attributeAssignThatWillStoreInFlightMicrosExpire = attributeAssign;
          
        }
        
        if (attributeAssignThatWillStoreInFlightMicrosExpire != null) {
          AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(UserLifecycleAttributeNames.userLifecycleStemName()+":"+UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_MICROS_EXPIRE, true);
          attributeAssignThatWillStoreInFlightMicrosExpire.getAttributeValueDelegate().assignValueInteger(attributeDefName.getName(), groupIdMemberIdLifecycleEventIdToMicrosExpire.get(groupIdMemberIdLifecycleEventId));
          
          attributeAssignThatWillStoreInFlightMicrosExpire.saveOrUpdate();
        }
        
        
      }
      
    }
    
    //prepare map so that we can find subjects in one go and email actions can be batched
    Map<String, Set<EmailObject>> memberIdToEmailObjects = new HashMap<String, Set<EmailObject>>();
    Map<String, Set<EmailObject>> groupIdToEmailObjects = new HashMap<String, Set<EmailObject>>();
    
    //now we're finally ready to perform the actions
    for (MultiKey groupIdMemberIdLifecycleEventId : groupIdsMemberIdsLifecycleEventIdsOnWhichToPeformActions) {
      
      String groupId = GrouperUtil.stringValue(groupIdMemberIdLifecycleEventId.getKey(0));
      String memberId = GrouperUtil.stringValue(groupIdMemberIdLifecycleEventId.getKey(1));
      Long lifecycleEventId = GrouperUtil.longValue(groupIdMemberIdLifecycleEventId.getKey(2));

      // if the action is removeUserFromGroup, remove member from the group.
      // if the action is addEndDateOnMembership, now look at the time difference between when the in flight micros expire vs now and if micros expire is in the past, remove the membership
      // remove member from the group otherwise 
      
      
      // batch email to  
      // based on the time difference between when the in flight micros added vs now and if the difference is greater than numberOfDaysInTheFuture,
      // then delete the membership immediately
      
      //get the lifecycle event config id from lifecycle event id
      String lifecycleEventConfig = lifecycleEventIdToLifecycleEventConfigId.get(lifecycleEventId);
      String policyConfigId = groupIdToPolicyConfigId.get(groupId);
      
      if (policyConfigIdToPolicyBeans.containsKey(policyConfigId)) {
        
        Set<PolicyBean> policyBeans = policyConfigIdToPolicyBeans.get(policyConfigId); //policy bean is basically the policy part. since one policy config id can be attached to multiple policy part ids, that's why it's a set
        
        for (PolicyBean policyBean: policyBeans) {
          if (policyBean.lifecycleEventConfigIds.contains(lifecycleEventConfig)) {
            //this is the policy part where the lifecycle config is used and now we need to perform all the actions
            
            for (String lifecycleActionConfigId : policyBean.lifecycleActionConfigIds) {
              //now get the action details and perform the action
              
              ActionBean actionBean = actionConfigIdToActionDetails.get(lifecycleActionConfigId);
              if (actionBean != null) {
                
                if (Strings.CS.equals(actionBean.actionType, "addEndDateOnMembership")) {
                  
                  Membership membership = null;
                  if (groupIdMemberIdToMembership.containsKey(new MultiKey(groupId, memberId))) {
                    membership = groupIdMemberIdToMembership.get(new MultiKey(groupId, memberId));
                  } else {
                    membership = new MembershipFinder().addGroupId(groupId).addMemberId(memberId).assignMembershipType(MembershipType.IMMEDIATE).findMembership(false);
                    groupIdMemberIdToMembership.put(new MultiKey(groupId, memberId), membership);
                  }
                  
                  if (membership != null) {
                    
                    Set<AttributeAssign> existingInFlightAttributeAssignments = membership.getAttributeDelegate().retrieveAssignments(UserLifecycleAttributeNames.retrieveInFlightAttributeDefNameMarker());
                    
                    for (AttributeAssign attributeAssign: existingInFlightAttributeAssignments) {
                      AttributeAssignValue existingInFlightLifecycleEventId = attributeAssign.getAttributeValueDelegate()
                          .retrieveAttributeAssignValue(userLifecycleStemName()+":"+UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_MICROS_EXPIRE);
                      if (existingInFlightLifecycleEventId == null || existingInFlightLifecycleEventId.getValueInteger() == null) {
                        continue;
                      }
                      
                      Long microsExpireAssignedToTheMembership = existingInFlightLifecycleEventId.getValueInteger();
                      
                      if ((System.currentTimeMillis() * 1000L) > microsExpireAssignedToTheMembership) {
                        membership.delete();
                        break;
                      }
                      
                    }
                  }
                  
                } else if (Strings.CS.equals(actionBean.actionType, "removeUserFromGroup")) {
                  
                  Membership membership = null;
                  if (groupIdMemberIdToMembership.containsKey(new MultiKey(groupId, memberId))) {
                    membership = groupIdMemberIdToMembership.get(new MultiKey(groupId, memberId));
                  } else {
                    membership = new MembershipFinder().addGroupId(groupId).addMemberId(memberId).assignMembershipType(MembershipType.IMMEDIATE).findMembership(false);
                  }
                  
                  if (membership != null) {
                    membership.delete();
                  }
                } else if (Strings.CS.equals(actionBean.actionType, "emailUser")) {
                  
                  if (memberIdToEmailObjects.containsKey(memberId)) {
                    Set<EmailObject> set = memberIdToEmailObjects.get(memberId);
                    set.add(new EmailObject(actionBean.emailSubjectLine, actionBean.emailBody));
                  } else {
                    Set<EmailObject> set = new HashSet<GroupPolicyUserLifecycleFullDaemon.EmailObject>();
                    memberIdToEmailObjects.put(memberId, set);
                    set.add(new EmailObject(actionBean.emailSubjectLine, actionBean.emailBody));
                  }
                  
                } else if (Strings.CS.equals(actionBean.actionType, "emailGroupAdmin")) {
                  
                  if (groupIdToEmailObjects.containsKey(groupId)) {
                    Set<EmailObject> set = groupIdToEmailObjects.get(groupId);
                    set.add(new EmailObject(actionBean.emailSubjectLine, actionBean.emailBody));
                  } else {
                    Set<EmailObject> set = new HashSet<GroupPolicyUserLifecycleFullDaemon.EmailObject>();
                    groupIdToEmailObjects.put(groupId, set);
                    set.add(new EmailObject(actionBean.emailSubjectLine, actionBean.emailBody));
                  }
                  
                }
              }
              
            }
          }
        }
        
      }
      
      
    }
    
    //Step 8 - let's send emails members and group admins
    Map<String, Set<EmailObject>> emailAddressToEmailObjects = new HashMap<String, Set<EmailObject>>();
    
    Set<String> memberIds = memberIdToEmailObjects.keySet();
    
    for (String memberId: memberIds) {
      Subject subject = new SubjectFinder().assignMemberId(memberId).findSubject();
      if (subject != null) {
        if (StringUtils.equals(subject.getType().getName(), SubjectTypeEnum.PERSON.getName())) {
          String emailAttributeName = GrouperEmailUtils.emailAttributeNameForSource(subject.getSourceId());
          if (!StringUtils.isBlank(emailAttributeName)) {
            String emailAddress = subject.getAttributeValue(emailAttributeName);
            if (!StringUtils.isBlank(emailAddress)) {
              
              if (emailAddressToEmailObjects.containsKey(emailAddress)) {
                Set<EmailObject> set = emailAddressToEmailObjects.get(emailAddress);
                set.addAll(memberIdToEmailObjects.get(memberId));
              } else {
                Set<EmailObject> set = new HashSet<GroupPolicyUserLifecycleFullDaemon.EmailObject>();
                set.addAll(memberIdToEmailObjects.get(memberId));
                emailAddressToEmailObjects.put(emailAddress, set);
              }
              
            }
          }
        }
      }
    }
    
    boolean hasAtLeastOneGroupInFinder = false;
    GroupFinder groupFinder = new GroupFinder();
    for (String groupId: groupIdToEmailObjects.keySet()) {
      groupFinder.addGroupId(groupId);
      hasAtLeastOneGroupInFinder = true;
    }
    
    Set<Group> groups = hasAtLeastOneGroupInFinder ?  groupFinder.findGroups() : new HashSet<Group>();
    
    for (Group group: GrouperUtil.nonNull(groups)) {
      
      Set<Subject> subjects = GrouperUtil.nonNull(group.getAdmins());
      for (Subject subject: subjects) {
        if (StringUtils.equals(subject.getType().getName(), SubjectTypeEnum.PERSON.getName())) {
          String emailAttributeName = GrouperEmailUtils.emailAttributeNameForSource(subject.getSourceId());
          if (!StringUtils.isBlank(emailAttributeName)) {
            String emailAddress = subject.getAttributeValue(emailAttributeName);
            if (!StringUtils.isBlank(emailAddress)) {
              
              if (emailAddressToEmailObjects.containsKey(emailAddress)) {
                Set<EmailObject> set = emailAddressToEmailObjects.get(emailAddress);
                set.addAll(groupIdToEmailObjects.get(group.getId()));
              } else {
                Set<EmailObject> set = new HashSet<GroupPolicyUserLifecycleFullDaemon.EmailObject>();
                set.addAll(groupIdToEmailObjects.get(group.getId()));
                emailAddressToEmailObjects.put(emailAddress, set);
              }
              
            }
          }
        }
      }
    }
    
    //now we have email address to email objects. let's send
    //optimize to send to multiple recipients at once
    for (String recipient: emailAddressToEmailObjects.keySet()) {
      Set<EmailObject> emailObjects = emailAddressToEmailObjects.get(recipient);
      for (EmailObject emailObject: emailObjects) {
        try {
          new GrouperEmail().setBody(emailObject.emailBody).setSubject(emailObject.subjectLine).setTo(recipient).send();
        } catch (Exception e) {
          LOG.error("Error sending email", e);
        }
      }
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
  
  private Map<String, Set<PolicyBean>> retrievePolicyConfigIdToPolicyBeans(Set<String> policyConfigIds) {
    
    List<UserLifecyclePolicyPartConfiguration> policyPartConfigurations = 
        UserLifecyclePolicyPartConfiguration.retrieveAllUserLifecyclePolicyPartConfigurations();
    
    Map<String, Set<PolicyBean>> policyConfigIdToPolicyBeans = new HashMap<String, Set<PolicyBean>>();
    
    for (UserLifecyclePolicyPartConfiguration policyPartConfiguration: policyPartConfigurations) {
      
      Map<String, GrouperConfigurationModuleAttribute> attributes = policyPartConfiguration.retrieveAttributes();
      GrouperConfigurationModuleAttribute policyConfigIdAttribute = attributes.get("policy");
      
      String policyConfigId = policyConfigIdAttribute.getValueOrExpressionEvaluationValue();
      if (!policyConfigIds.contains(policyConfigId)) {
        continue;
      }
      
      PolicyBean policyBean = new PolicyBean();
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
        Set<PolicyBean> policyBeans = new HashSet<GroupPolicyUserLifecycleFullDaemon.PolicyBean>();
        policyBeans.add(policyBean);
        policyConfigIdToPolicyBeans.put(policyConfigId, policyBeans);
      }
      
    }
    return policyConfigIdToPolicyBeans;
  }
  
  class PolicyBean {
    
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
