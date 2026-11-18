package edu.internet2.middleware.grouper.userLifecycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dictionary.GrouperDictionary;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

public class UserLifecycleService {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UserLifecycleService.class);
  
  /**
   * 
   * @return number of lifecycle event configs
   */
  public static int retrieveUserLifecycleEventNumberOfConfigs() {
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(GrouperLifecycleEventConfig.lifecycleEventConfigIds));
    return configIdsInConfig.size();
  }
  
  /**
   * 
   * @return number of lifecycle action configs
   */
  public static int retrieveUserLifecycleActionNumberOfConfigs() {
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(UserLifecycleActionConfiguration.lifecycleActionConfigIds));
    return configIdsInConfig.size();
  }
  
  /**
   * 
   * @return number of lifecycle policy configs
   */
  public static int retrieveUserLifecyclePolicyNumberOfConfigs() {
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(UserLifecyclePolicyConfiguration.lifecyclePolicyConfigIds));
    return configIdsInConfig.size();
  }
  
  /**
   * 
   * @return number of lifecycle policy part configs
   */
  public static int retrieveUserLifecyclePolicyPartNumberOfConfigs() {
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(UserLifecyclePolicyPartConfiguration.lifecyclePolicyPartConfigIds));
    return configIdsInConfig.size();
  }
  
  /**
   * Retrieve user lifecycle policies for the given subject. For sysadmin and public policies, no permission check is done 
   * @param subject
   * @return
   */
  public static List<UserLifecyclePolicyConfiguration> retrieveUserLifecyclePolicies(Subject subject) {
    
    final List<UserLifecyclePolicyConfiguration> result = new ArrayList<>();
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
        
        List<UserLifecyclePolicyConfiguration> allUserLifecyclePolicyConfigurations = UserLifecyclePolicyConfiguration.retrieveAllUserLifecyclePolicyConfigurations();
        
        if (PrivilegeHelper.isWheelOrRoot(subject)) {
          result.addAll(allUserLifecyclePolicyConfigurations);
          return result;
        }
        
        for (UserLifecyclePolicyConfiguration policyConfiguration: allUserLifecyclePolicyConfigurations) {
          String attributeValueFromConfig = policyConfiguration.retrieveAttributeValueFromConfig("isPublic", false);
          if (GrouperUtil.booleanValue(attributeValueFromConfig, false)) {
            result.add(policyConfiguration);
            continue;
          } 
          
          String groupIdOrName = policyConfiguration.retrieveAttributeValueFromConfig("groupIdOrName", false);
          
          Group group = GroupFinder.findByUuid(groupIdOrName, false);
          if (group == null) {
            group = GroupFinder.findByName(groupIdOrName, false);
          }
          
          if (group == null) {
            LOG.error(String.format("On user lifecycle policy config '%s' group '%s' not found", policyConfiguration.getConfigId(), groupIdOrName));
            continue;
          }
          
          if (group.hasMember(subject)) {
            result.add(policyConfiguration);
          }
          
        }
        
        return result;
        
      };
    });
    return result;
  }
  
  /**
   * Decide whether a subject may save this policy config id on this group. The caller (the group
   * edit screen) has already checked that the subject is ADMIN on the group.
   * <ul>
   *   <li>null means the form did not submit the field: no change, allowed</li>
   *   <li>blank means remove the policy: allowed for any group ADMIN, even for a restricted policy
   *   (an ADMIN can delete the group anyway, so blocking removal would protect nothing); the
   *   removal is audited</li>
   *   <li>the policy already on the group: no change, allowed (the edit screen shows an assigned
   *   policy the user cannot use as a read-only choice, and resubmits it)</li>
   *   <li>any other policy (assign or replace): the subject must be allowed to use the NEW policy
   *   (wheel or root, public policy, or member of the policy's group)</li>
   * </ul>
   * @param group the group
   * @param policyConfigId submitted policy config id, null for no change, blank to remove
   * @param subject the real (logged in) subject making the change
   * @return true if allowed
   */
  public static boolean subjectCanSavePolicyConfigOnGroup(Group group, String policyConfigId, Subject subject) {
    
    // field not submitted: nothing will change
    if (policyConfigId == null) {
      return true;
    }
    
    // removal is allowed for any group admin (audited in savePolicyConfigOnGroup)
    if (StringUtils.isBlank(policyConfigId)) {
      return true;
    }
    
    // keeping the policy that is already assigned is not a change
    String existingPolicyConfigId = retrieveExistingPolicyConfigId(group);
    if (StringUtils.equals(existingPolicyConfigId, policyConfigId)) {
      return true;
    }
    
    // assigning or replacing: the subject must be able to use the new policy
    for (UserLifecyclePolicyConfiguration userLifecyclePolicyConfiguration: retrieveUserLifecyclePolicies(subject)) {
      if (StringUtils.equals(userLifecyclePolicyConfiguration.getConfigId(), policyConfigId)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Assign, replace or remove the user lifecycle policy on a group, as root, after checking
   * subjectCanSavePolicyConfigOnGroup. Each change writes an audit entry naming the real subject.
   * @param group the group
   * @param policyConfigId policy config id to assign; blank removes the policy; null means no change
   * (a form that did not submit the field must not remove the policy)
   * @param subject the real (logged in) subject making the change
   * @return true if a change was made
   */
  public static boolean savePolicyConfigOnGroup(Group group, String policyConfigId, Subject subject) {
    
    // a missing form field is not a removal request
    if (policyConfigId == null) {
      return false;
    }
    
    boolean[] madeChange = {false};
    
    if (!subjectCanSavePolicyConfigOnGroup(group, policyConfigId, subject)) {
      throw new RuntimeException("Subject "+subject.getId()+ " not allowed to save user lifecycle policy!!");
    }
    
    final String existingPolicyConfigId = retrieveExistingPolicyConfigId(group);
    final String[] attributeAssignId = {null};
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      @Override
      public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
        
        AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", UserLifecycleAttributeNames.retrieveAttributeDefNameMarker(), false, false);
        
        if (attributeAssign == null && StringUtils.isNotBlank(policyConfigId)) {
          // we only want to add the assignment if the policy we're going to add is not blank. blank actually means remove it if it already exists
          attributeAssign = group.getAttributeDelegate().assignAttribute(UserLifecycleAttributeNames.retrieveAttributeDefNameMarker()).getAttributeAssign();
        }
        
        if (attributeAssign != null) {
          attributeAssignId[0] = attributeAssign.getId();
        }
        
        if (StringUtils.isBlank(policyConfigId) && attributeAssign != null) {
          attributeAssign.delete();
          madeChange[0] = true;
        } else if (attributeAssign != null) {
          AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(UserLifecycleAttributeNames.userLifecycleStemName()+":"+UserLifecycleAttributeNames.USER_LIFECYCLE_POLICY_GROUP_VALUE_CONFIG_ID, true);
          
          //check if the same policy is already assigned
          AttributeAssignValue existingAttributeAssignValue = attributeAssign.getAttributeValueDelegate().retrieveAttributeAssignValue(attributeDefName.getName());
          if (existingAttributeAssignValue != null && StringUtils.equals(existingAttributeAssignValue.getValueString(), policyConfigId)) {
            return null;
          }
          
          attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), policyConfigId);
          attributeAssign.saveOrUpdate();
          madeChange[0] = true;
        }
        
        if (madeChange[0]) {
          auditPolicyChange(theGrouperSession, group, attributeAssignId[0], existingPolicyConfigId, policyConfigId, subject);
        }
        return null;
        
      };
    });
    
    return madeChange[0];
    
  }
  
  /**
   * Write one audit entry for a policy assign, replace or remove. The attribute assignment audits
   * that the change itself writes run in a root session and do not say which policy, and from GSH
   * they do not name the real subject, so this entry does both.
   * @param rootSession root session
   * @param group the group
   * @param attributeAssignId the marker assignment id
   * @param oldPolicyConfigId policy before, blank for none
   * @param newPolicyConfigId policy after, blank for none
   * @param subject the real subject making the change
   */
  private static void auditPolicyChange(GrouperSession rootSession, Group group, String attributeAssignId,
      String oldPolicyConfigId, String newPolicyConfigId, Subject subject) {
    
    // reuse the attribute-assign-on-group audit types, so no new audit type is needed
    AuditTypeBuiltin auditTypeBuiltin = null;
    String description = null;
    if (StringUtils.isBlank(newPolicyConfigId)) {
      auditTypeBuiltin = AuditTypeBuiltin.ATTRIBUTE_ASSIGN_GROUP_DELETE;
      description = "Removed user lifecycle policy '" + StringUtils.defaultString(oldPolicyConfigId) + "'";
    } else if (StringUtils.isBlank(oldPolicyConfigId)) {
      auditTypeBuiltin = AuditTypeBuiltin.ATTRIBUTE_ASSIGN_GROUP_ADD;
      description = "Assigned user lifecycle policy '" + newPolicyConfigId + "'";
    } else {
      auditTypeBuiltin = AuditTypeBuiltin.ATTRIBUTE_ASSIGN_GROUP_UPDATE;
      description = "Changed user lifecycle policy from '" + oldPolicyConfigId + "' to '" + newPolicyConfigId + "'";
    }
    
    AttributeDefName markerAttributeDefName = UserLifecycleAttributeNames.retrieveAttributeDefNameMarker();
    
    AuditEntry auditEntry = new AuditEntry(auditTypeBuiltin, 
        "id", attributeAssignId,
        "ownerGroupName", group.getName(),
        "ownerGroupId", group.getId(),
        "attributeDefNameName", markerAttributeDefName.getName(),
        "attributeDefNameId", markerAttributeDefName.getId(),
        "action", "assign",
        "attributeDefId", markerAttributeDefName.getAttributeDefId());
    auditEntry.setDescription(description);
    
    // context fields (logged in member from the UI, ip address, etc)
    GrouperContext.assignAuditEntryFields(auditEntry);
    
    // the change runs as root, so make sure the real subject is the actor: logged in member if the
    // context has none (e.g. GSH), else act-as if the context names someone else
    Member member = subject == null ? null : MemberFinder.findBySubject(rootSession, subject, true);
    if (member != null) {
      if (StringUtils.isBlank(auditEntry.getLoggedInMemberId())) {
        auditEntry.setLoggedInMemberId(member.getId());
      } else if (!StringUtils.equals(auditEntry.getLoggedInMemberId(), member.getId())) {
        auditEntry.setActAsMemberId(member.getId());
      }
    }
    
    auditEntry.saveOrUpdate(false);
  }
  
  public static String retrieveExistingPolicyConfigId(Group group) {
    
    String result = (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      @Override
      public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
        
        AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", UserLifecycleAttributeNames.retrieveAttributeDefNameMarker(), false, false);
        
        if (attributeAssign == null) {
          return "";
        }
        
        AttributeAssignValue attributeAssignValue = attributeAssign.getAttributeValueDelegate().retrieveAttributeAssignValue(UserLifecycleAttributeNames.userLifecycleStemName()+":"+UserLifecycleAttributeNames.USER_LIFECYCLE_POLICY_GROUP_VALUE_CONFIG_ID);
        if (attributeAssignValue == null || StringUtils.isBlank(attributeAssignValue.getValueString())) {
          return "";
        } 
        
        String policyConfigIdFromDb = attributeAssignValue.getValueString();
        
        return policyConfigIdFromDb == null ? "" : policyConfigIdFromDb;
      };
    });
    
    return result;
    
  }

  /**
   * collecton of memberships with the following data (max 100):
   *  - membershipId
   *  - groupId
   *  - subjectId
   *  - sourceId
   *  - eventInternalId (if multiple for a membership, based on highest priority)
   *  - event time in micros (if multiple for a membership, based on highest priority)
   *  - membership expiration time in micros (if multiple for a membership, based on earliest)
   *  - natural language text that the user is authorized to see (if multiple for a membership, based on highest priority)
   * @return collection
   */
  @SuppressWarnings("unchecked")
  public static Collection<Object[]> retrieveInFlightMembershipEventsSecure() {
    int maxMemberships = 100;
    Subject loggedInSubject = GrouperSession.staticGrouperSession().getSubject();
    boolean isWheelOrRoot = PrivilegeHelper.isWheelOrRoot(loggedInSubject);
    
    return (Collection<Object[]>)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
       
        Map<String, Map<String, Object[]>> membershipIdToAttributeAssignmentIdToDetailsUnsorted = new LinkedHashMap<>();
        Map<String, Map<String, Object[]>> membershipIdToAttributeAssignmentIdToDetailsSorted = new LinkedHashMap<>();
        AttributeDefName inFlightAttributeDefNameMarker = UserLifecycleAttributeNames.retrieveInFlightAttributeDefNameMarker();
        
        GcDbAccess gcDbAccess = new GcDbAccess();
        StringBuilder sqlBuilder = new StringBuilder("select gaaamv.attribute_assign_id1, gaaamv.membership_id, gaaamv.group_id, gaaamv.subject_id, gaaamv.source_id, gaaamv.attribute_def_name_name2, gaaamv.value_integer from grouper_aval_asn_asn_mship_v gaaamv ");
        if (isWheelOrRoot) {
          sqlBuilder.append("where ");
        } else {
          Member member = MemberFinder.findBySubject(rootSession, loggedInSubject, false);
          if (member == null) {
            return new ArrayList<Object[]>();
          }
          
          sqlBuilder.append(", grouper_memberships_all_v gmav where gmav.owner_group_id=gaaamv.group_id and gmav.field_id = ? and gmav.member_id = ? and gmav.immediate_mship_enabled='T' and ");
          gcDbAccess.addBindVar(FieldFinder.find("admins", true).getId());
          gcDbAccess.addBindVar(member.getId());
        }
        
        sqlBuilder.append("gaaamv.attribute_def_name_id1 = ?");
        gcDbAccess.addBindVar(inFlightAttributeDefNameMarker.getId());
        
        List<Object[]> attributeAssignments = gcDbAccess.sql(sqlBuilder.toString()).selectList(Object[].class);
        
        for (Object[] attributeAssignment : attributeAssignments) {
          String attributeAssignId = (String)attributeAssignment[0];
          String membershipId = (String)attributeAssignment[1];
          String groupId = (String)attributeAssignment[2];
          String subjectId = (String)attributeAssignment[3];
          String sourceId = (String)attributeAssignment[4];
          String userLifecycleMshipInFlightAttributeName = (String)attributeAssignment[5];
          Long value = GrouperUtil.longObjectValue(attributeAssignment[6], false);
          
          if (!membershipIdToAttributeAssignmentIdToDetailsUnsorted.containsKey(membershipId)) {
            membershipIdToAttributeAssignmentIdToDetailsUnsorted.put(membershipId, new LinkedHashMap<String, Object[]>());
          }
          
          Map<String, Object[]> attributeAssignmentIdToDetails = membershipIdToAttributeAssignmentIdToDetailsUnsorted.get(membershipId);
          
          if (!attributeAssignmentIdToDetails.containsKey(attributeAssignId)) {
            attributeAssignmentIdToDetails.put(attributeAssignId, new Object[]{membershipId, groupId, subjectId, sourceId, null, null, null, null});
          }
          
          if (userLifecycleMshipInFlightAttributeName.equals(UserLifecycleAttributeNames.userLifecycleStemName() + ":" + UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_LIFECYCLE_EVENT_ID)) {
            attributeAssignmentIdToDetails.get(attributeAssignId)[4] = value;
          } else if (userLifecycleMshipInFlightAttributeName.equals(UserLifecycleAttributeNames.userLifecycleStemName() + ":" + UserLifecycleAttributeNames.USER_LIFECYCLE_MSHIP_IN_FLIGHT_MICROS_EXPIRE)) {
            attributeAssignmentIdToDetails.get(attributeAssignId)[6] = value;
          }
        }
        
        // sort based on earliest removal date based on membership id
        List<String> membershipIds = new ArrayList<String>(membershipIdToAttributeAssignmentIdToDetailsUnsorted.keySet());
        Collections.sort(membershipIds, new Comparator<String>() {
          public int compare(String a, String b) {
            Long minA = earliestMembershipRemovalMicros(membershipIdToAttributeAssignmentIdToDetailsUnsorted.get(a));
            Long minB = earliestMembershipRemovalMicros(membershipIdToAttributeAssignmentIdToDetailsUnsorted.get(b));
            
            if (minA == null) {
              minA = Long.MAX_VALUE;
            }
            
            if (minB == null) {
              minB = Long.MAX_VALUE;
            }

            if (minA < minB) {
              return -1;
            }
            if (minA > minB) {
              return 1;
            }
            return 0;
          }
        });

        // trim per max and add to sorted map
        for (int i = 0; i < Math.min(maxMemberships, membershipIds.size()); i++) {
          String membershipId = membershipIds.get(i);
          membershipIdToAttributeAssignmentIdToDetailsSorted.put(membershipId, membershipIdToAttributeAssignmentIdToDetailsUnsorted.get(membershipId));
        }
        
        // now we need to get the events and then the event configs to find the highest priority, event dates, etc
        Set<Long> eventInternalIds = new LinkedHashSet<>();
        for (Map<String, Object[]> attributeAssignmentIdToDetails : membershipIdToAttributeAssignmentIdToDetailsSorted.values()) {
          for (Object[] details : attributeAssignmentIdToDetails.values()) {
            if (details[4] != null) {
              eventInternalIds.add((Long)details[4]);
            }
          }
        }
        
        // for all the events, query grouper_lifecycle_event for more data
        List<GrouperLifecycleEvent> grouperLifecycleEvents = new GcDbAccess().sql("select * from grouper_lifecycle_event ")
            .selectMultipleColumnName("internal_id")
            .bindVars(new ArrayList<Long>(eventInternalIds))
            .selectList(GrouperLifecycleEvent.class);
        
        Set<Long> eventConfigInternalIds = new LinkedHashSet<>();
        
        Map<Long, GrouperLifecycleEvent> grouperLifecycleEventsByInternalId = new LinkedHashMap<>();
        for (GrouperLifecycleEvent grouperLifecycleEvent : grouperLifecycleEvents) {
          grouperLifecycleEventsByInternalId.put(grouperLifecycleEvent.getInternalId(), grouperLifecycleEvent);
          eventConfigInternalIds.add(grouperLifecycleEvent.getGroupLifecycleEventConfigInternalId());
        }
        
        // get the event configs and see if the user is a member of the privileged group
        List<GrouperLifecycleEventConfig> grouperLifecycleEventConfigs = new GcDbAccess().sql("select * from grouper_lifecycle_event_config ")
            .selectMultipleColumnName("internal_id")
            .bindVars(new ArrayList<Long>(eventConfigInternalIds))
            .selectList(GrouperLifecycleEventConfig.class);
        
        Map<Long, GrouperLifecycleEventConfig> grouperLifecycleEventConfigsByInternalId = new LinkedHashMap<>();
        Map<String, Boolean> groupIdOrNameToHasMember = new LinkedHashMap<>();
        for (GrouperLifecycleEventConfig grouperLifecycleEventConfig : grouperLifecycleEventConfigs) {
          grouperLifecycleEventConfigsByInternalId.put(grouperLifecycleEventConfig.getInternalId(), grouperLifecycleEventConfig);
          
          String naturalLanguageDescriptionJexlPrivilegedGroupIdOrName = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent." + grouperLifecycleEventConfig.getConfigId() + ".naturalLanguageDescriptionJexlPrivilegedGroupIdOrName");
          if (!StringUtils.isBlank(naturalLanguageDescriptionJexlPrivilegedGroupIdOrName) && !groupIdOrNameToHasMember.containsKey(naturalLanguageDescriptionJexlPrivilegedGroupIdOrName)) {
            boolean hasMember = false;
            Group group = GrouperDAOFactory.getFactory().getGroup().findByUuidOrName(naturalLanguageDescriptionJexlPrivilegedGroupIdOrName, naturalLanguageDescriptionJexlPrivilegedGroupIdOrName, false);
            if (group != null) {
              hasMember = group.hasMember(loggedInSubject);
            }
            
            groupIdOrNameToHasMember.put(naturalLanguageDescriptionJexlPrivilegedGroupIdOrName, hasMember);
          }          
        }
        
        // query the dictionary for the natural language text and save the change magnitude
        Set<Long> dictionaryInternalIds = new LinkedHashSet<>();
        Map<Long, Float> eventInternalIdToChangeMagnitudeFloat = new LinkedHashMap<>();
        Set<Long> eventInternalIdsToUsePrivilegedText = new LinkedHashSet<>();
        for (GrouperLifecycleEvent grouperLifecycleEvent : grouperLifecycleEvents) {
          GrouperLifecycleEventConfig grouperLifecycleEventConfig = grouperLifecycleEventConfigsByInternalId.get(grouperLifecycleEvent.getGroupLifecycleEventConfigInternalId());
          String naturalLanguageDescriptionJexlPrivilegedGroupIdOrName = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent." + grouperLifecycleEventConfig.getConfigId() + ".naturalLanguageDescriptionJexlPrivilegedGroupIdOrName");
          if (groupIdOrNameToHasMember.get(naturalLanguageDescriptionJexlPrivilegedGroupIdOrName) == Boolean.TRUE && grouperLifecycleEvent.getNaturalLanguagePrivilegeDictionaryInternalId() != null) {
            dictionaryInternalIds.add(grouperLifecycleEvent.getNaturalLanguagePrivilegeDictionaryInternalId());
            eventInternalIdsToUsePrivilegedText.add(grouperLifecycleEvent.getInternalId());
          } else if (grouperLifecycleEvent.getNaturalLanguageUnPrivilegeDictionaryInternalId() != null) {
            dictionaryInternalIds.add(grouperLifecycleEvent.getNaturalLanguageUnPrivilegeDictionaryInternalId());
          }
          
          float changeMagnitude = GrouperUtil.floatValue(GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent." + grouperLifecycleEventConfig.getConfigId() + ".changeMagnitude"), 0);
          eventInternalIdToChangeMagnitudeFloat.put(grouperLifecycleEvent.getInternalId(), changeMagnitude);
        }
        
        List<GrouperDictionary> grouperDictionaryEntries = new GcDbAccess().sql("select * from grouper_dictionary ")
            .selectMultipleColumnName("internal_id")
            .bindVars(new ArrayList<Long>(dictionaryInternalIds))
            .selectList(GrouperDictionary.class);
        
        Map<Long, GrouperDictionary> grouperDictionaryEntriesByInternalId = new LinkedHashMap<>();
        for (GrouperDictionary grouperDictionary : grouperDictionaryEntries) {
          grouperDictionaryEntriesByInternalId.put(grouperDictionary.getInternalId(), grouperDictionary);
        }
        
        // collect all the data now
        Map<String, Object[]> membershipIdToDetails = new LinkedHashMap<>();
        for (String membershipId : membershipIdToAttributeAssignmentIdToDetailsSorted.keySet()) {
          Map<String, Object[]> attributeAssignmentIdToDetails = membershipIdToAttributeAssignmentIdToDetailsSorted.get(membershipId);
          Long earliestMembershipRemoval = earliestMembershipRemovalMicros(attributeAssignmentIdToDetails);
          float changeMagnitudeRankUsed = -1;
          Long membershipRemovalMicrosOfChangeMagnitudeUsed = null;
          for (Object[] details : attributeAssignmentIdToDetails.values()) {
            if (!membershipIdToDetails.containsKey(membershipId)) {
              membershipIdToDetails.put(membershipId, new Object[]{details[0], details[1], details[2], details[3], null, null, earliestMembershipRemoval, null});
            }
            
            Long eventInternalId = (Long)details[4];
            Long membershipRemovalMicros = (Long)details[6];
            if (eventInternalId == null) {
              continue;
            }
            Float currentChangeMagnitudeRank = eventInternalIdToChangeMagnitudeFloat.get(eventInternalId);
            if (currentChangeMagnitudeRank != null && currentChangeMagnitudeRank >= changeMagnitudeRankUsed) {
              GrouperLifecycleEvent grouperLifecycleEvent = grouperLifecycleEventsByInternalId.get(eventInternalId);
              if (grouperLifecycleEvent == null) {
                continue;
              }
              
              if (currentChangeMagnitudeRank == changeMagnitudeRankUsed) {
                // if we found multiple with the same change magnitude, prefer the one with the earlier membership removal
                if (membershipRemovalMicros >= membershipRemovalMicrosOfChangeMagnitudeUsed) {
                  continue;
                }
              }
              
              membershipIdToDetails.get(membershipId)[4] = eventInternalId;              
              membershipIdToDetails.get(membershipId)[5] = grouperLifecycleEvent.getEventMicros();
              
              GrouperDictionary dictionary = eventInternalIdsToUsePrivilegedText.contains(grouperLifecycleEvent.getInternalId()) ? grouperDictionaryEntriesByInternalId.get(grouperLifecycleEvent.getNaturalLanguagePrivilegeDictionaryInternalId()) : grouperDictionaryEntriesByInternalId.get(grouperLifecycleEvent.getNaturalLanguageUnPrivilegeDictionaryInternalId());
              String text = dictionary == null ? null : dictionary.getTheText();
              membershipIdToDetails.get(membershipId)[7] = text;
                            
              changeMagnitudeRankUsed = currentChangeMagnitudeRank;
              membershipRemovalMicrosOfChangeMagnitudeUsed = membershipRemovalMicros;
            }
          }
        }
                
        return membershipIdToDetails.values();
      }
    });
  }

  private static Long earliestMembershipRemovalMicros(Map<String, Object[]> attributeAssignmentIdToDetails) {
    Long min = null;

    if (attributeAssignmentIdToDetails == null) {
      return min;
    }

    Iterator<Object[]> it = attributeAssignmentIdToDetails.values().iterator();
    while (it.hasNext()) {
      Object[] row = it.next();
      if (row[6] == null) {
        continue;
      }

      Long membershipRemovalMicros = (Long)row[6];
      if (min == null || membershipRemovalMicros < min) {
        min = membershipRemovalMicros;
      }
    }

    return min;
  }
}
