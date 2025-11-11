package edu.internet2.middleware.grouper.userLifecycle;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class GroupPolicyUserLifecycleFullDaemon extends OtherJobBase {
  
  /** logger */
  protected static final Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(GroupPolicyUserLifecycleFullDaemon.class);
  
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    
    AttributeDef attributeDef = UserLifecycleAttributeNames.retrieveAttributeDefBaseDef();
    if (attributeDef == null) {
      LOG.error(UserLifecycleAttributeNames.userLifecycleStemName() + ":" + UserLifecycleAttributeNames.USER_LIFECYCLE_POLICY_GROUP_MARKER_DEF + " attribute def doesn't exist. Job will not proceed.");
      return null;
    }
    
    // retrieve the last time the full sync ran; we need it to only capture events that happened after this. 
    Timestamp lastFullSyncSuccessStartTimestamp = new GcDbAccess().sql("select max(started_time) from grouper_loader_log where job_name = 'OTHER_JOB_groupPolicyUserLifecycleFullDaemon' and status = 'SUCCESS' ").select(Timestamp.class);
    
//    long lastFullSyncSuccessStartMillis = lastFullSyncSuccessStartTimestamp == null ? -1 : lastFullSyncSuccessStartTimestamp.getTime();
    if (lastFullSyncSuccessStartTimestamp == null) {
      lastFullSyncSuccessStartTimestamp = new Timestamp(System.currentTimeMillis() - 24*60*60*1000); // work on all events that happened after yesterday
    }
    
    // retrieve list of groups that have lifecycle policies attached to them
    List<Object[]> groupsWithPolicies = new GcDbAccess()
      .sql("select group_name, group_id, value_string from grouper_aval_asn_asn_group_v gaaagv where attribute_defn_name1 = ? and attribute_def_name2 = ?")
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
    
    
    List<String> groupIds = new ArrayList<>(groupIdToPolicyConfigId.keySet());
    
    //retrieve lifecycle events for the groups that have policies attached to them and only retrieve events that took place after the most recent full sync
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
      
      membershipAttributes.addAll(gcDbAccess.selectList(Object[].class));
      
    }
    
    /**
     * 
     * if gaaamv.attribute_def_name_name2 equals etc:<>:userLifecycleMshipInFlightLifecycleEventId then only perform the triplet comparison
     * 
     * if there are any group id, member id, gle.internal_id in groupIdsMemberIds list but not in membershipAttributes, we need to assign in flight attribute to that internal id of the user lifecycle event
     * 
     *  membership
     * 
     */
    
    Set<MultiKey> groupIdsMemberIdsLifecycleEventIdsListFromMembershipAttributes = new HashSet<MultiKey>(groupIdsMemberIdsLifecycleEventIds);
    
    for (Object[] membershipAttribute: membershipAttributes) {
      String attributeDefName = GrouperUtil.stringValue(membershipAttribute[0]);
      Long value = GrouperUtil.longObjectValue(membershipAttribute[1], true);
      String groupId = GrouperUtil.stringValue(membershipAttribute[2]);
      String memberId = GrouperUtil.stringValue(membershipAttribute[3]);
      
      if (!StringUtils.equals(attributeDefName, UserLifecycleAttributeNames.userLifecycleStemName() +":"+ UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_LIFECYCLE_EVENT_ID)) {
        continue;
      }
      
      MultiKey groupIdMemberIdLifecycleEventId = new MultiKey(groupId, memberId, value);
      groupIdsMemberIdsLifecycleEventIdsListFromMembershipAttributes.add(groupIdMemberIdLifecycleEventId);
    }
    
    
    //create a list of group id member id objects so that we can make just one call to the membership finder
//    List<MultiKey> groupIdMemberIdForMembershipFinder = new ArrayList<MultiKey>();
    
//    MembershipFinder membershipFinder = new MembershipFinder();
    for (MultiKey groupIdMemberIdLifecycleEventId : groupIdsMemberIdsLifecycleEventIdsList) {
      
      if (!groupIdsMemberIdsLifecycleEventIdsListFromMembershipAttributes.contains(groupIdMemberIdLifecycleEventId)) {
        // assign in flight attribute to that internal id of the user lifecycle event
        
        String groupId = GrouperUtil.stringValue(groupIdMemberIdLifecycleEventId.getKey(0));
        String memberId = GrouperUtil.stringValue(groupIdMemberIdLifecycleEventId.getKey(1));
        Long lifecycleEventId = GrouperUtil.longValue(groupIdMemberIdLifecycleEventId.getKey(2));
        
//        groupIdMemberIdForMembershipFinder.add(groupIdMemberIdLifecycleEventId);
        Membership membership = new MembershipFinder().addGroupId(groupId).addMemberId(memberId).assignMembershipType(MembershipType.IMMEDIATE).findMembership(false);
        
        if (membership != null) {          
          AttributeAssign attributeAssign = membership.getAttributeDelegate().retrieveAssignment("assign", UserLifecycleAttributeNames.retrieveInFlightAttributeDefNameMarker(), false, false);
          if (attributeAssign == null) {
            attributeAssign = membership.getAttributeDelegate().assignAttribute(UserLifecycleAttributeNames.retrieveInFlightAttributeDefNameMarker()).getAttributeAssign();
          }
          
          AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(UserLifecycleAttributeNames.userLifecycleStemName()+":"+UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_LIFECYCLE_EVENT_ID, true);
          attributeAssign.getAttributeValueDelegate().assignValueInteger(attributeDefName.getName(), lifecycleEventId);
          
          attributeDefName = AttributeDefNameFinder.findByName(UserLifecycleAttributeNames.userLifecycleStemName()+":"+UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_ADDED_MICROS, true);
          attributeAssign.getAttributeValueDelegate().assignValueInteger(attributeDefName.getName(), System.currentTimeMillis() * 1000);
          
          attributeAssign.saveOrUpdate();
        }
        
      }
      
    }
    
    // create bean/map for the membership when it started to be in flight for both the ones that already had it and the ones we're adding above on line 175
    // 
    
    
    
    return null;
  }

}
