package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.membership.MembershipResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

/**
 * @author shilen
 */
public class GrouperProvisioningTranslator {
  
  
  private static Map<String, ExpirableCache<String, Boolean>> provisionerConfigIdToGroupName = new HashMap<>(); 
  
  private static Map<String, ExpirableCache<String, Boolean>> provisionerConfigIdToGroupId = new HashMap<>(); 

  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;

  /**
   * reference back up to the provisioner
   * @return the provisioner
   */
  public GrouperProvisioner getGrouperProvisioner() {
    return this.grouperProvisioner;
  }
  
  /**
   * Any group id that's been used, for e.g. in the entity translation that needs to trigger an entity recalc if
   * a user is added or removed from the group. This is cached for 2 days.
   * @param provisionerConfigId
   * @return
   */
  public static Set<String> retrieveMembershipGroupIdsForProvisionerConfigId(String provisionerConfigId) {
     ExpirableCache<String,Boolean> cache = provisionerConfigIdToGroupId.get(provisionerConfigId);
     return cache == null ? new HashSet<String>() : cache.keySet();
  }
  
  /**
   * Any group name that's been used, for e.g. in the entity translation that needs to trigger an entity recalc if
   * a user is added or removed from the group. This is cached for 2 days.
   * @param provisionerConfigId
   * @return
   */
  public static Set<String> retrieveMembershipGroupNamesForProvisionerConfigId(String provisionerConfigId) {
    ExpirableCache<String,Boolean> cache = provisionerConfigIdToGroupName.get(provisionerConfigId);
    return cache == null ? new HashSet<String>() : cache.keySet();
 }

  /**
   * make two tables and indexes:
   * grouper_sync_dep_group_user: if a group is here then any changes to group needs to recalculate the user (not memberships)
   * id_index (pk)
   * grouper_sync_id (index 1) (unq) (foreign key cascade delete) (non null)
   * group_uuid (index 2) (unq) (foreign key cascade delete) (non null)
   * field_uuid (index 2) (unq) (foreign key cascade delete) (non null)
   * 
   * grouper_sync_def_group_group: if a group is here then any changes to the group needs to recalculate the group (not memberships
   * id_index (pk)
   * grouper_sync_id (index 1) (unq) (foreign key cascade delete) (non null)
   * group_id  (index 3) (unq) (foreign key cascade delete) (non null)
   * field_id  (index 3) (unq) (foreign key cascade delete) (non null)
   * provisionable_group_id (index 2) (unq) (foreign key cascade delete) (non null)
   * 
   * Get memberships for user cached groups
   * Loop through fields
   * Batch up groups
   * Get memberships for certain users
   * 
   * Get memberships for group cached groups
   * Loop through fields
   * Batch up groups
   * Get memberships for all users
   * 
   * Init if not inited
   * Dont have static caches, do this in provisioner
   * Based user stuff on fields
   * Have a arbitrary group method for groups, and privs (4 methods)
   * Update the dependencies as new ones found
   * Read the dependencies if needed (all in full, certain ones for users)
   * Cache in provisioner if retrieved, retrieve if not and update tables
   */
  
  /**
   * In a provisioning run, all the members of the group that are used for entity translations (for e.g.
   *  a user is active in the target). This is initialized for all members of the group for full sync
   *  or for applicable for incremental sync
   */
  private Map<String, Set<String>> groupNameToMemberIds = new HashMap<>(); 
  
  public boolean isInGroup(String groupName, String memberId) {
    Set<String> groupMemberships = initGroupMemberships(groupName);
    return groupMemberships.contains(memberId);
  }
  
  public synchronized Set<String> initGroupMemberships(String groupName) {
    
    String provisionerConfigId = grouperProvisioner.getConfigId();
    ExpirableCache<String,Boolean> cacheByName = provisionerConfigIdToGroupName.get(provisionerConfigId);
    ExpirableCache<String,Boolean> cacheById = provisionerConfigIdToGroupId.get(provisionerConfigId);
    if (cacheByName == null) {
      cacheByName = new ExpirableCache<>(60 * 24 * 2); // 2 days
      provisionerConfigIdToGroupName.put(provisionerConfigId, cacheByName);
      
      cacheById = new ExpirableCache<>(60 * 24 * 2); // 2 days
      provisionerConfigIdToGroupId.put(provisionerConfigId, cacheById);
    }
    
    Set<String> memberIds = groupNameToMemberIds.get(groupName);
    if (memberIds != null) {
      return memberIds;
    }
    
    memberIds = new HashSet<>();
    Group group = GroupFinder.findByName(groupName, true);
    cacheByName.put(groupName, true);
    cacheById.put(group.getId(), true);
    
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isFullSync()) {
      
      Set<Member> members = group.getMembers();
      
      for (Member member: GrouperUtil.nonNull(members)) {
        memberIds.add(member.getId());
      }
     
    } else {
      
      MembershipFinder membershipFinder = new MembershipFinder();
      membershipFinder.addGroup(groupName);
      
      Set<ProvisioningEntityWrapper> entityWrappers = this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers();
      
      for (ProvisioningEntityWrapper provisioningEntityWrapper: entityWrappers) {
        membershipFinder.addMemberId(provisioningEntityWrapper.getMemberId());
      }
      
      membershipFinder.assignField(Group.getDefaultList());
      
      MembershipResult membershipResult = membershipFinder.findMembershipResult();
      Set<Member> members = membershipResult.members();
      for (Member member: GrouperUtil.nonNull(members)) {
        memberIds.add(member.getId());
      }
    }
    
    groupNameToMemberIds.put(groupName, memberIds);
    return memberIds;
    
  } 

  /**
   * reference back up to the provisioner
   * @param grouperProvisioner1
   */
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner1) {
    this.grouperProvisioner = grouperProvisioner1;
  }

  public List<ProvisioningMembership> translateGrouperToTargetMemberships(
      List<ProvisioningMembership> grouperProvisioningMemberships, boolean includeDelete) {
    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperMemberships()
        ) {
      return null;
    }

    Collection<Object> changedMemberships = new HashSet<Object>();

    int invalidMembershipsDuringTranslation = 0; //TODO: looks like we're never updating this value
    int membershipsRemovedDueToGroupRemoved = 0;
    int membershipsRemovedDueToEntityRemoved = 0;
    int membershipsRemovedDueToGroupWrapperNull = 0;
    int membershipsRemovedDueToEntityWrapperNull = 0;

    
    // clear out the membership attribute, it might have a default value in there
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      String groupMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupMembershipAttributeName();
      if (!StringUtils.isBlank(groupMembershipAttribute)) {
        for (ProvisioningGroup provisioningGroup : this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups()) {
          ProvisioningGroupWrapper provisioningGroupWrapper = provisioningGroup.getProvisioningGroupWrapper();
          if (provisioningGroupWrapper != null && provisioningGroupWrapper.getProvisioningStateGroup().isTranslatedMemberships()) {
            continue;
          }
          provisioningGroup.clearAttribute(groupMembershipAttribute);
        }
      }
    }
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      String entityMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityMembershipAttributeName();
      if (!StringUtils.isBlank(entityMembershipAttribute)) {
        for (ProvisioningEntity provisioningEntity : this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities()) {
          ProvisioningEntityWrapper provisioningEntityWrapper = provisioningEntity.getProvisioningEntityWrapper();
          if (provisioningEntityWrapper != null && provisioningEntityWrapper.getProvisioningStateEntity().isTranslatedMemberships()) {
            continue;
          }
          provisioningEntity.clearAttribute(entityMembershipAttribute);
        }
      }
    }
    
    // not null if group attributes
    Set<ProvisioningGroupWrapper> groupAttributesTranslated = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes ? new HashSet<ProvisioningGroupWrapper>() : null;
    Set<ProvisioningEntityWrapper> entityAttributesTranslated = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes ? new HashSet<ProvisioningEntityWrapper>() : null;
    
    List<ProvisioningMembership> grouperTargetMemberships = new ArrayList<ProvisioningMembership>();
    List<ProvisioningMembership> grouperTargetMembershipsTranslated = new ArrayList<ProvisioningMembership>();

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("Membership"));

    Iterator<ProvisioningMembership> iterator = GrouperUtil.nonNull(grouperProvisioningMemberships).iterator();
    
    while (iterator.hasNext()) {
      
      ProvisioningMembership grouperProvisioningMembership = iterator.next();
  
      try {

        ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperProvisioningMembership.getProvisioningMembershipWrapper();
        
        GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
  
        ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningMembership.getProvisioningGroup().getProvisioningGroupWrapper();
        
        ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningMembership.getProvisioningEntity().getProvisioningEntityWrapper();

        if (provisioningGroupWrapper == null) {
          membershipsRemovedDueToGroupWrapperNull++;
          continue;
        }
        
        if (provisioningEntityWrapper == null) {
          membershipsRemovedDueToEntityWrapperNull++;
          continue;
        }

        if (provisioningGroupWrapper.getProvisioningStateGroup().isGroupRemovedDueToAttribute()) {
          membershipsRemovedDueToGroupRemoved++;
          continue;
        }
        
        if (provisioningEntityWrapper.getProvisioningStateEntity().isEntityRemovedDueToAttribute()) {
          membershipsRemovedDueToEntityRemoved++;
          continue;
        }
  
        if (groupAttributesTranslated != null) {
          groupAttributesTranslated.add(provisioningGroupWrapper);
        }
        if (entityAttributesTranslated != null) {
          entityAttributesTranslated.add(provisioningEntityWrapper);
        }
  
        
        ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
        
        GcGrouperSyncErrorCode errorCode = provisioningGroupWrapper.getErrorCode();
        String errorMessage = provisioningGroupWrapper.getGcGrouperSyncGroup().getErrorMessage();
        if (errorCode == null && !StringUtils.isBlank(errorMessage)) {
          errorCode = GcGrouperSyncErrorCode.ERR;
        }
            
        ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();
        
        if (errorCode == null) {
          errorCode = provisioningEntityWrapper.getErrorCode();
          errorMessage = provisioningEntityWrapper.getGcGrouperSyncMember().getErrorMessage();
          if (errorCode == null && !StringUtils.isBlank(errorMessage)) {
            errorCode = GcGrouperSyncErrorCode.ERR;
          }
        }
        
        // if this is an add, and the user isnt there, then there is a problem
        boolean isDelete = gcGrouperSyncMembership.isInTarget() || provisioningMembershipWrapper.getProvisioningStateMembership().isDelete();
  
        boolean isEntityInTarget = (provisioningEntityWrapper.getGcGrouperSyncMember().getInTarget() != null && provisioningEntityWrapper.getGcGrouperSyncMember().getInTarget()) || provisioningEntityWrapper.getTargetProvisioningEntity() != null;
        
        if (!isDelete && errorCode == null && this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isOnlyAddMembershipsIfUserExistsInTarget()
            && !isEntityInTarget) {
          errorCode = GcGrouperSyncErrorCode.DNE;
        }
        
        if (errorCode != null) {
          this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignMembershipError(provisioningMembershipWrapper, errorCode, errorMessage);
          continue;
        }
  
        ProvisioningMembership grouperTargetMembership = new ProvisioningMembership();
        if (this.translateGrouperToTargetAutomatically) {
          grouperTargetMembership = grouperProvisioningMembership.clone();
        }
        if (provisioningMembershipWrapper.getGrouperTargetMembership() == null) {
          grouperTargetMembershipsTranslated.add(grouperTargetMembership);
        }
        
        
        grouperTargetMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
   
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        
        elVariableMap.put("grouperProvisioningGroup", grouperProvisioningMembership.getProvisioningGroup());
        elVariableMap.put("provisioningGroupWrapper", provisioningGroupWrapper);
        elVariableMap.put("grouperTargetGroup", grouperTargetGroup);
        elVariableMap.put("gcGrouperSyncGroup", provisioningGroupWrapper.getGcGrouperSyncGroup());
   
          elVariableMap.put("grouperProvisioningEntity", grouperProvisioningMembership.getProvisioningEntity());
        elVariableMap.put("provisioningEntityWrapper", provisioningEntityWrapper);
        elVariableMap.put("grouperTargetEntity", grouperTargetEntity);
        elVariableMap.put("gcGrouperSyncMember", provisioningEntityWrapper.getGcGrouperSyncMember());
        
        elVariableMap.put("grouperProvisioningMembership", grouperProvisioningMembership);
        elVariableMap.put("provisioningMembershipWrapper", provisioningMembershipWrapper);
        elVariableMap.put("grouperTargetMembership", grouperTargetMembership);
        elVariableMap.put("gcGrouperSyncMembership", gcGrouperSyncMembership);
  
        // attribute translations
        for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig().values()) {
          String expressionToUse = getTargetExpressionToUse(!gcGrouperSyncMembership.isInTarget(), grouperProvisioningConfigurationAttribute);
          
          boolean continueTranslation = continueTranslation(elVariableMap, grouperProvisioningConfigurationAttribute);
          if (!continueTranslation) {
            grouperTargetMembership.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), null);
            //throw new RuntimeException("Not continuing translation because the translation continue condition '" + grouperProvisioningConfigurationAttribute.getTranslationContinueCondition()+"'  did not evaluate to be true");
          }
          
          if (continueTranslation && (StringUtils.isNotBlank(expressionToUse) 
              || StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupField())
              || StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperTargetGroupField())
              || StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperTargetEntityField())
              || StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningEntityField()))) {
            Object result = attributeTranslationOrCache( 
                grouperTargetMembership.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName()), elVariableMap, !gcGrouperSyncMembership.isInTarget(), 
                grouperProvisioningConfigurationAttribute, provisioningGroupWrapper, provisioningEntityWrapper);
  
            grouperTargetMembership.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), result);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(changedMemberships, grouperTargetMembership, grouperProvisioningConfigurationAttribute, null);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().convertNullsEmpties(changedMemberships, grouperTargetMembership, grouperProvisioningConfigurationAttribute, null);
  
          }
        }
        
        // if the group is missing, has an invalid attribute don't bother setting the membership
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes 
            && provisioningGroupWrapper.getGrouperTargetGroup() != null) {
          String groupMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupMembershipAttributeName();
          if (!StringUtils.isEmpty(groupMembershipAttribute)) {
            GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(groupMembershipAttribute);
            if (grouperProvisioningConfigurationAttribute != null) {
              
              Object result = null;
              
              if (!StringUtils.isBlank(this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupMembershipAttributeValue())) {
                result = translateFromGrouperProvisioningEntityField(provisioningEntityWrapper, 
                    this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupMembershipAttributeValue());
              }
              if (result != null) {
                
                MultiKey validationError = this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validFieldOrAttributeValue(grouperTargetGroup, grouperProvisioningConfigurationAttribute, result);
                if (validationError != null) {
                  
                  errorCode = (GcGrouperSyncErrorCode)validationError.getKey(0);
                  errorMessage = (String)validationError.getKey(1);
                  this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignMembershipError(provisioningMembershipWrapper, errorCode, errorMessage);
  
                  continue;
                }
                
                GrouperProvisioningConfigurationAttributeValueType valueType = grouperProvisioningConfigurationAttribute.getValueType();
                if (valueType != null) {
                  if (!valueType.correctTypeNonSet(result)) {
                    result = valueType.convert(result);
                  }
                }
                
                grouperTargetGroup.addAttributeValueForMembership(result, provisioningMembershipWrapper, true);
              }
            }
          }
        }
        
        // if the entity is missing, has an invalid attribute don't bother setting the membership
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes
            && provisioningEntityWrapper.getGrouperTargetEntity() != null) {
          String userMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityMembershipAttributeName();
          if (!StringUtils.isEmpty(userMembershipAttribute)) {
            GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().get(userMembershipAttribute);
  
            if (grouperProvisioningConfigurationAttribute != null) {
              
              Object result = null;
              
              if (!StringUtils.isBlank(this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityMembershipAttributeValue())) {
                result = translateFromGrouperProvisioningGroupField(provisioningGroupWrapper, 
                    this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityMembershipAttributeValue());
              }
  
              if (result != null) {
                if (!grouperProvisioningMembership.getProvisioningEntity().getProvisioningEntityWrapper().getProvisioningStateEntity().isDelete()) {
                  MultiKey validationError = this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validFieldOrAttributeValue(grouperTargetEntity, grouperProvisioningConfigurationAttribute, result);
                  if (validationError != null) {
                    
                    errorCode = (GcGrouperSyncErrorCode)validationError.getKey(0);
                    errorMessage = (String)validationError.getKey(1);
                    this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignMembershipError(provisioningMembershipWrapper, errorCode, errorMessage);
  
                    continue;
                  }
                }
                  
                GrouperProvisioningConfigurationAttributeValueType valueType = grouperProvisioningConfigurationAttribute.getValueType();
                if (valueType != null) {
                  if (!valueType.correctTypeNonSet(result)) {
                    result = valueType.convert(result);
                  }
                }
                
                grouperTargetEntity.addAttributeValueForMembership(result, provisioningMembershipWrapper, true);
              }
  
              }
            }
          }
        
        for (String script: scripts) {
  
          runScript(script, elVariableMap);
          
        }
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects &&
            grouperTargetMembership.isEmpty()) {
          
          grouperTargetMembership.setProvisioningEntityId(grouperTargetEntity == null ? null: grouperTargetEntity.getId());
          grouperTargetMembership.setProvisioningEntity(grouperTargetEntity);
          grouperTargetMembership.setProvisioningGroup(grouperTargetGroup);
          grouperTargetMembership.setProvisioningGroupId(grouperTargetGroup == null ? null: grouperTargetGroup.getId());
        } else {
          if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
            if (grouperTargetMembership.getProvisioningEntity() == null) {
              grouperTargetMembership.setProvisioningEntity(grouperTargetEntity);
            }
            if (grouperTargetMembership.getProvisioningGroup() == null) {
              grouperTargetMembership.setProvisioningGroup(grouperTargetGroup);
            }
          }
        }
          
        if (grouperTargetMembership.isRemoveFromList() || grouperTargetMembership.isEmpty()) {
          continue;
        }
        if (grouperTargetGroup != null) {
          if (!StringUtils.equals(grouperTargetGroup.getId(), grouperTargetMembership.getProvisioningGroupId())) {
            grouperTargetMembership.setProvisioningGroupId(grouperTargetGroup.getId());
            grouperTargetMembership.setProvisioningGroup(grouperTargetGroup);
          }
          
        }
        
        if (grouperTargetEntity != null) {
          if (!StringUtils.equals(grouperTargetEntity.getId(), grouperTargetMembership.getProvisioningEntityId())) {
            grouperTargetMembership.setProvisioningEntityId(grouperTargetEntity.getId());
            grouperTargetMembership.setProvisioningEntity(grouperTargetEntity);
          }
        } 
  
        grouperTargetMembership.getProvisioningMembershipWrapper().setGrouperTargetMembership(grouperTargetMembership);
  //      if (includeDelete) {
  //        grouperTargetMembership.getProvisioningMembershipWrapper().setDelete(true);
  //      }
  
        if (grouperTargetMembership.getProvisioningMembershipWrapper().getGcGrouperSyncMembership().isInTarget() && grouperTargetMembership.getProvisioningMembershipWrapper().getGrouperTargetMembership() == null) {
          grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().setDelete(true);
        }
        
        if (!grouperTargetMembership.getProvisioningMembershipWrapper().getGcGrouperSyncMembership().isInTarget() && grouperTargetMembership.getProvisioningMembershipWrapper().getGrouperTargetMembership() != null) {
          grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().setCreate(true);
        }
        
  //      if (!grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().isRecalcObject() 
  //          && grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().getGrouperIncrementalDataAction() == GrouperIncrementalDataAction.delete) {
  //        grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().setDelete(true);
  //      }
  //      if (!grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().isRecalcObject() 
  //          && grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().getGrouperIncrementalDataAction() == GrouperIncrementalDataAction.insert) {
  //        grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().setCreate(true);
  //      }
        
        grouperTargetMemberships.add(grouperTargetMembership); 
      } catch (RuntimeException re) {
        try {
          this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignMembershipError(grouperProvisioningMembership.getProvisioningMembershipWrapper(), GcGrouperSyncErrorCode.ERR, 
              grouperProvisioningMembership.getProvisioningMembershipWrapper() + ", " + GrouperUtil.getFullStackTrace(re));
        } catch (RuntimeException e) {
          throw new RuntimeException("Cannot translate membership or log error for: " + grouperProvisioningMembership, re);
        }
      }
        
    }
    
    // set default for membership attribute, it might be blank and have a default value in there
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      String groupMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupMembershipAttributeName();
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(groupMembershipAttribute);

      if (!StringUtils.isBlank(groupMembershipAttribute) && grouperProvisioningConfigurationAttribute != null && !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getDefaultValue())) {
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForGroups(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups(), grouperProvisioningConfigurationAttribute);
      }
    }
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      String entityMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityMembershipAttributeName();
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().get(entityMembershipAttribute);

      if (!StringUtils.isBlank(entityMembershipAttribute) && grouperProvisioningConfigurationAttribute != null && !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getDefaultValue())) {
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForEntities(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities(), grouperProvisioningConfigurationAttribute);
      }
    }
    
    if (invalidMembershipsDuringTranslation > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "invalidMembershipsDuringTranslation", invalidMembershipsDuringTranslation);
    }
    if (GrouperUtil.length(grouperTargetMembershipsTranslated) > 0) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperMembershipsToTarget, grouperTargetMembershipsTranslated);
    }
    
    if (membershipsRemovedDueToGroupRemoved > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "membershipsRemovedDueToGroupRemoved", membershipsRemovedDueToGroupRemoved);
    }
    if (membershipsRemovedDueToEntityRemoved > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "membershipsRemovedDueToEntityRemoved", membershipsRemovedDueToEntityRemoved);
    }
    if (membershipsRemovedDueToGroupWrapperNull > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "membershipsRemovedDueToGroupWrapperNull", membershipsRemovedDueToGroupWrapperNull);
    }
    if (membershipsRemovedDueToEntityWrapperNull > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "membershipsRemovedDueToEntityWrapperNull", membershipsRemovedDueToEntityWrapperNull);
    }

    for (ProvisioningGroupWrapper provisioningGroupWrapper : GrouperUtil.nonNull(groupAttributesTranslated)) {
      provisioningGroupWrapper.getProvisioningStateGroup().setTranslatedMemberships(true);
    }
    for (ProvisioningEntityWrapper provisioningEntityWrapper : GrouperUtil.nonNull(entityAttributesTranslated)) {
      provisioningEntityWrapper.getProvisioningStateEntity().setTranslatedMemberships(true);
    }

    return grouperTargetMemberships;
  }

  public List<ProvisioningEntity> translateGrouperToTargetEntities(
      List<ProvisioningEntity> grouperProvisioningEntities, boolean includeDelete, boolean forCreate) {
    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperEntities()) {
      return null;
    }

    Collection<Object> changedEntities = new HashSet<Object>();
    List<ProvisioningEntity> grouperTargetEntities = new ArrayList<ProvisioningEntity>();

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("Entity"));
    
    if (forCreate) {
      scripts.addAll(GrouperUtil.nonNull(GrouperUtil.nonNull(
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("EntityCreateOnly")));
    }

    List<ProvisioningEntity> grouperTargetEntitiesTranslated = new ArrayList<ProvisioningEntity>();

    PROVISIONING_ENTITY_BLOCK: for (ProvisioningEntity grouperProvisioningEntity: GrouperUtil.nonNull(grouperProvisioningEntities)) {
      
      try {
        ProvisioningEntity grouperTargetEntity = new ProvisioningEntity();
        if (this.translateGrouperToTargetAutomatically) {
          grouperTargetEntity = grouperProvisioningEntity.clone();
        }
        ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningEntity.getProvisioningEntityWrapper();
        
        if (provisioningEntityWrapper.getGrouperTargetEntity() == null) {
          grouperTargetEntitiesTranslated.add(grouperTargetEntity);
        }
  
        grouperTargetEntity.setProvisioningEntityWrapper(provisioningEntityWrapper);
  
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("grouperProvisioningEntity", grouperProvisioningEntity);
        elVariableMap.put("provisioningEntityWrapper", provisioningEntityWrapper);
        GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
        elVariableMap.put("gcGrouperSyncMember", gcGrouperSyncMember);
        elVariableMap.put("grouperTargetEntity", grouperTargetEntity);
  
        // do the required's first
        for (boolean required : new boolean[] {true, false}) {
          // attribute translations
          for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : entityTargetAttributesInTranslationOrder()) {
            if (grouperProvisioningConfigurationAttribute.isRequired() == required) {
              //TODO call translateFromGrouperProvisioningEntityField once only
              if (!grouperProvisioningConfigurationAttribute.isUpdate() && StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningEntityField())
                  && !GrouperUtil.isBlank(translateFromGrouperProvisioningEntityField(provisioningEntityWrapper, grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningEntityField()))) {
                
                Object result = translateFromGrouperProvisioningEntityField(provisioningEntityWrapper, grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningEntityField());
                String attributeOrFieldName = grouperProvisioningConfigurationAttribute.getName();
                grouperTargetEntity.assignAttributeValue(attributeOrFieldName, result);
                this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(changedEntities, grouperTargetEntity, grouperProvisioningConfigurationAttribute, null);
                this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().convertNullsEmpties(changedEntities, grouperTargetEntity, grouperProvisioningConfigurationAttribute, null);
                continue;
              }
              
              
              String expressionToUse = getTargetExpressionToUse(forCreate, grouperProvisioningConfigurationAttribute);
              String staticValuesToUse = getTranslateFromStaticValuesToUse(forCreate, grouperProvisioningConfigurationAttribute);
              String grouperProvisioningEntityField = getTranslateFromGrouperProvisioningEntityField(forCreate, grouperProvisioningConfigurationAttribute);
  
              boolean continueTranslation = continueTranslation(elVariableMap, grouperProvisioningConfigurationAttribute);
              if (!continueTranslation) {
                grouperTargetEntity.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), null);
                // throw new RuntimeException("Not continuing translation because the translation continue condition '" + grouperProvisioningConfigurationAttribute.getTranslationContinueCondition()+"'  did not evaluate to be true");
              }
              
              if (continueTranslation && (!StringUtils.isBlank(expressionToUse) || !StringUtils.isBlank(staticValuesToUse) || !StringUtils.isBlank(grouperProvisioningEntityField)
                  || this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isEntityAttributeNameHasCache(grouperProvisioningConfigurationAttribute.getName())
                  || this.shouldTranslateEntityAttribute(provisioningEntityWrapper, grouperProvisioningConfigurationAttribute))) { 
  
                Object result = attributeTranslationOrCache( 
                    grouperTargetEntity.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName()), elVariableMap, forCreate, 
                    grouperProvisioningConfigurationAttribute, null, provisioningEntityWrapper);
                
                if (grouperProvisioningConfigurationAttribute.getSyncMemberCacheAttribute() != null
                    && grouperProvisioningConfigurationAttribute.getSyncMemberCacheAttribute().getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.grouper) {
                  gcGrouperSyncMember.assignField(grouperProvisioningConfigurationAttribute.getSyncMemberCacheAttribute().getCacheName(), result);
                }
                grouperTargetEntity.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), result);
                this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(changedEntities, grouperTargetEntity, grouperProvisioningConfigurationAttribute, null);
                this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().convertNullsEmpties(changedEntities, grouperTargetEntity, grouperProvisioningConfigurationAttribute, null);
  
                if (required && GrouperUtil.isBlank(result) && gcGrouperSyncMember.isProvisionable()) {
                  // short circuit this since other fields might need this field and its not there and invalid anyways
                  this.getGrouperProvisioner().retrieveGrouperProvisioningValidation()
                  .assignErrorCodeToEntityWrapper(grouperTargetEntity, grouperProvisioningConfigurationAttribute, 
                      provisioningEntityWrapper);
                  continue PROVISIONING_ENTITY_BLOCK;
                }
              
              }
            }
          }
          
        }
        
        for (String script: scripts) {
                 
          runScript(script, elVariableMap);
          
        }
  
        if (grouperTargetEntity.isRemoveFromList() || grouperTargetEntity.isEmpty()) {
          continue;
        }
        
        grouperTargetEntities.add(grouperTargetEntity);
        
        provisioningEntityWrapper.setGrouperTargetEntity(grouperTargetEntity);
        if (includeDelete) {
          provisioningEntityWrapper.getProvisioningStateEntity().setDelete(true);
        } else if (forCreate) {
          provisioningEntityWrapper.getProvisioningStateEntity().setCreate(true);
        }
      } catch (RuntimeException re) {
        try {
          this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignEntityError(grouperProvisioningEntity.getProvisioningEntityWrapper(), GcGrouperSyncErrorCode.ERR, 
              grouperProvisioningEntity.getProvisioningEntityWrapper() + ", " + GrouperUtil.getFullStackTrace(re));
        } catch (RuntimeException e) {
          throw new RuntimeException("Cannot translate entity or log error for: " + grouperProvisioningEntity, re);
        }
      }

    }
    if (GrouperUtil.length(grouperTargetEntitiesTranslated) > 0) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperEntitiesToTarget, grouperTargetEntitiesTranslated);
    }

    return grouperTargetEntities;
  }

  /**
   * @param elVariableMap
   * @param grouperProvisioningConfigurationAttribute
   * @return true if continue with translation otherwise false
   */
  public static boolean continueTranslation(Map<String, Object> elVariableMap,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    
    
    String translationContinueCondition = grouperProvisioningConfigurationAttribute.getTranslationContinueCondition();
    boolean checkForNullsInScript = grouperProvisioningConfigurationAttribute.isCheckForNullsInScript();
    return continueTranslation(elVariableMap, checkForNullsInScript, translationContinueCondition);
  }
  
  /**
   * @param elVariableMap
   * @param checkForNullsInScript
   * @param translationContinueCondition
   * @return true if continue with translation otherwise false
   */
  public static boolean continueTranslation(Map<String, Object> elVariableMap, boolean checkForNullsInScript, String translationContinueCondition) {
    
    if (checkForNullsInScript) {
      if (StringUtils.isNotBlank(translationContinueCondition)) {
        try {
          Object result = GrouperUtil.substituteExpressionLanguageScript(translationContinueCondition, elVariableMap, true, true, true);
          boolean resultBoolean = GrouperUtil.booleanValue(result, false);
          return resultBoolean;
        } catch (RuntimeException re) {
          GrouperUtil.injectInException(re, ", script: '" + translationContinueCondition + "', ");
          GrouperUtil.injectInException(re, GrouperUtil.toStringForLog(elVariableMap));
          throw re;
        }
      }
    }
    
    return true;
  }

  /**
   * 
   * @param provisioningEntityWrapper
   * @param grouperProvisioningConfigurationAttribute
   * @return
   */
  public boolean shouldTranslateEntityAttribute(
      ProvisioningEntityWrapper provisioningEntityWrapper,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    return false;
  }

  public Collection<GrouperProvisioningConfigurationAttribute> entityTargetAttributesInTranslationOrder() {
    return this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().values();
  }

  public List<ProvisioningGroup> translateGrouperToTargetGroups(List<ProvisioningGroup> grouperProvisioningGroups, boolean includeDelete, boolean forCreate) {

    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperGroups()) {
      return null;
    }
    
    Collection<Object> changedEntities = new HashSet<Object>();

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("Group"));

    if (forCreate) {
      scripts.addAll(GrouperUtil.nonNull(GrouperUtil.nonNull(
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("GroupCreateOnly")));
    }
    List<ProvisioningGroup> grouperTargetGroups = new ArrayList<ProvisioningGroup>();
    List<ProvisioningGroup> grouperTargetGroupsTranslated = new ArrayList<ProvisioningGroup>();

    PROVISIONING_GROUP_BLOCK: for (ProvisioningGroup grouperProvisioningGroup: GrouperUtil.nonNull(grouperProvisioningGroups)) {
      
      try {
        ProvisioningGroup grouperTargetGroup = new ProvisioningGroup();
        
        if (this.translateGrouperToTargetAutomatically) {
          grouperTargetGroup = grouperProvisioningGroup.clone();
        }
        
        ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningGroup.getProvisioningGroupWrapper();
        
        if (provisioningGroupWrapper.getGrouperTargetGroup() == null) {
          grouperTargetGroupsTranslated.add(grouperTargetGroup);
        }
        
        grouperTargetGroup.setProvisioningGroupWrapper(provisioningGroupWrapper);
  
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("grouperProvisioningGroup", grouperProvisioningGroup);
        elVariableMap.put("provisioningGroupWrapper", provisioningGroupWrapper);
        elVariableMap.put("grouperTargetGroup", grouperTargetGroup);
        GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
        elVariableMap.put("gcGrouperSyncGroup", gcGrouperSyncGroup);
  
        // do the required's first
        for (boolean required : new boolean[] {true, false}) {
          // attribute translations
          for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : groupAttributesInTranslationOrder()) {
            if (grouperProvisioningConfigurationAttribute.isRequired() == required) {
              
              if (!grouperProvisioningConfigurationAttribute.isUpdate() && StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupField())
                  && !GrouperUtil.isBlank(translateFromGrouperProvisioningGroupField(provisioningGroupWrapper, grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupField()))) {
                
                Object result = translateFromGrouperProvisioningGroupField(provisioningGroupWrapper, grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupField());
                String attributeOrFieldName = grouperProvisioningConfigurationAttribute.getName();
                grouperTargetGroup.assignAttributeValue(attributeOrFieldName, result);
                this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(changedEntities, grouperTargetGroup, grouperProvisioningConfigurationAttribute, null);
                this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().convertNullsEmpties(changedEntities, grouperTargetGroup, grouperProvisioningConfigurationAttribute, null);
                continue;
              }
              
              String expressionToUse = getTargetExpressionToUse(forCreate, grouperProvisioningConfigurationAttribute);
              String staticValuesToUse = getTranslateFromStaticValuesToUse(forCreate, grouperProvisioningConfigurationAttribute);
              String grouperProvisioningGroupField = getTranslateFromGrouperProvisioningGroupField(forCreate, grouperProvisioningConfigurationAttribute);
  
              boolean continueTranslation = continueTranslation(elVariableMap, grouperProvisioningConfigurationAttribute);
              if (!continueTranslation) {
                grouperTargetGroup.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), null);
                //throw new RuntimeException("Not continuing translation because the translation continue condition '" + grouperProvisioningConfigurationAttribute.getTranslationContinueCondition()+"'  did not evaluate to be true");
              }
              
              if (continueTranslation && (!StringUtils.isBlank(expressionToUse) || !StringUtils.isBlank(staticValuesToUse) || !StringUtils.isBlank(grouperProvisioningGroupField)
                  || this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isGroupAttributeNameHasCache(grouperProvisioningConfigurationAttribute.getName())
                  || this.shouldTranslateGroupAttribute(provisioningGroupWrapper, grouperProvisioningConfigurationAttribute))) { 
                Object result = attributeTranslationOrCache( 
                    grouperTargetGroup.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName()), elVariableMap, forCreate, 
                    grouperProvisioningConfigurationAttribute, provisioningGroupWrapper, null);
  
                if (grouperProvisioningConfigurationAttribute.getSyncGroupCacheAttribute() != null
                    && grouperProvisioningConfigurationAttribute.getSyncGroupCacheAttribute().getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.grouper) {
                  gcGrouperSyncGroup.assignField(grouperProvisioningConfigurationAttribute.getSyncGroupCacheAttribute().getCacheName(), result);
                }
                String attributeOrFieldName = grouperProvisioningConfigurationAttribute.getName();
                grouperTargetGroup.assignAttributeValue(attributeOrFieldName, result);
                this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(changedEntities, grouperTargetGroup, grouperProvisioningConfigurationAttribute, null);
                this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().convertNullsEmpties(changedEntities, grouperTargetGroup, grouperProvisioningConfigurationAttribute, null);
                if (required && GrouperUtil.isBlank(result) && gcGrouperSyncGroup.isProvisionable()) {
                  // short circuit this since other fields might need this field and its not there and invalid anyways
                  this.getGrouperProvisioner().retrieveGrouperProvisioningValidation()
                  .assignErrorCodeToGroupWrapper(grouperTargetGroup, grouperProvisioningConfigurationAttribute, 
                      grouperTargetGroup.getProvisioningGroupWrapper());
                  continue PROVISIONING_GROUP_BLOCK;
                } 
              }
            }
          }        
        }      
        
        for (String script: scripts) {
  
          
          runScript(script, elVariableMap);
          
        }
  
        if (grouperTargetGroup.isRemoveFromList() || grouperTargetGroup.isEmpty()) {
          continue;
        }
  
        provisioningGroupWrapper.setGrouperTargetGroup(grouperTargetGroup);
        if (includeDelete) {
          provisioningGroupWrapper.getProvisioningStateGroup().setDelete(true);
        } else if (forCreate) {
          provisioningGroupWrapper.getProvisioningStateGroup().setCreate(true);
        }
        grouperTargetGroups.add(grouperTargetGroup);
      } catch (RuntimeException re) {
        try {
          this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignGroupError(grouperProvisioningGroup.getProvisioningGroupWrapper(), GcGrouperSyncErrorCode.ERR, 
              grouperProvisioningGroup.getProvisioningGroupWrapper() + ", " + GrouperUtil.getFullStackTrace(re));
        } catch (RuntimeException e) {
          throw new RuntimeException("Cannot translate group or log error for: " + grouperProvisioningGroup, re);
        }
      }
    }
    if (GrouperUtil.length(grouperTargetGroupsTranslated) > 0) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperGroupsToTarget, grouperTargetGroupsTranslated);
    }

    return grouperTargetGroups;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningTranslator.class);

  /**
   * if the provisioner might generate a transation
   * @param provisioningGroupWrapper
   * @param grouperProvisioningConfigurationAttribute
   * @return
   */
  public boolean shouldTranslateGroupAttribute(
      ProvisioningGroupWrapper provisioningGroupWrapper,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    return false;
  }

  public Collection<GrouperProvisioningConfigurationAttribute> groupAttributesInTranslationOrder() {
    return this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().values();
  }

  /**
   * translate from gc grouper sync entity and field name to the value
   * @param provisioningEntityWrapper
   * @param field
   * @return the value
   */
  public Object translateFromGrouperProvisioningEntityField(ProvisioningEntityWrapper provisioningEntityWrapper, String field) {
    
    // "id", "email", "loginid", "memberId", "entityAttributeValueCache0", "entityAttributeValueCache1", "entityAttributeValueCache2", "entityAttributeValueCache3", "name", "subjectId", "subjectSourceId", "description", "subjectIdentifier0", "subjectIdentifier1", "subjectIdentifier2"
    if (provisioningEntityWrapper == null) { 
      return null;
    }
    
    ProvisioningEntity provisioningEntity = provisioningEntityWrapper.getGrouperProvisioningEntity();
    
    if (provisioningEntity != null) {

      if (StringUtils.equals("id", field) && !StringUtils.isBlank(provisioningEntity.getId())) {
        return provisioningEntity.getId();
      }
      if (StringUtils.equals("email", field)) {
        return provisioningEntity.getEmail();
      }
      if (StringUtils.equals("loginid", field)) {
        return provisioningEntity.getLoginId();
      }
      if (StringUtils.equals("name", field)) {
        return provisioningEntity.getName();
      }
      if (StringUtils.equals("subjectId", field) && !StringUtils.isBlank(provisioningEntity.getSubjectId())) {
        return provisioningEntity.getSubjectId();
      }
      if (StringUtils.equals("subjectSourceId", field) && !StringUtils.isBlank(provisioningEntity.getSubjectSourceId())) {
        return provisioningEntity.getSubjectSourceId();
      }
      if (StringUtils.equals("description", field)) {
        return provisioningEntity.getDescription();
      }
      if (StringUtils.equals("subjectIdentifier0", field) && !StringUtils.isBlank(provisioningEntity.getSubjectIdentifier0())) {
        return provisioningEntity.getSubjectIdentifier0();
      }
      if (StringUtils.equals("subjectIdentifier1", field) && !StringUtils.isBlank(provisioningEntity.getSubjectIdentifier1())) {
        return provisioningEntity.getSubjectIdentifier1();
      }
      if (StringUtils.equals("subjectIdentifier2", field) && !StringUtils.isBlank(provisioningEntity.getSubjectIdentifier2())) {
        return provisioningEntity.getSubjectIdentifier2();
      }
      if (StringUtils.equals("subjectIdentifier", field)) {
        return provisioningEntity.getSubjectIdentifier();
      }
      if (StringUtils.equals("idIndex", field)) {
        return provisioningEntity.getIdIndex();
      }
    }
    
    GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
    
    if (gcGrouperSyncMember != null) {
      if (StringUtils.equals("id", field)) {
        return gcGrouperSyncMember.getId();
      }
      if (StringUtils.equals("subjectId", field)) {
        return gcGrouperSyncMember.getSubjectId();
      }
      if (StringUtils.equals("subjectSourceId", field)) {
        return gcGrouperSyncMember.getSourceId();
      }
      if (StringUtils.equals("subjectIdentifier", field)) {
        return gcGrouperSyncMember.getSubjectIdentifier();
      }
      if (StringUtils.equals("entityAttributeValueCache0", field)) {
        String cacheValue = gcGrouperSyncMember.getEntityAttributeValueCache0();
        if (GrouperUtil.isBlank(cacheValue) || !cacheValue.contains("{")) {
          return cacheValue;
        }
        GrouperProvisioningConfigurationAttributeDbCache cache = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[0];
        if (cache == null || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          return cacheValue;
        }
        return this.grouperProvisioner.retrieveGrouperProvisioningData().parseJsonCacheEntity(cacheValue);
      }
      if (StringUtils.equals("entityAttributeValueCache1", field)) {
        String cacheValue = gcGrouperSyncMember.getEntityAttributeValueCache1();
        if (GrouperUtil.isBlank(cacheValue) || !cacheValue.contains("{")) {
          return cacheValue;
        }
        GrouperProvisioningConfigurationAttributeDbCache cache = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[1];
        if (cache == null || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          return cacheValue;
        }
        return this.grouperProvisioner.retrieveGrouperProvisioningData().parseJsonCacheEntity(cacheValue);
      }
      if (StringUtils.equals("entityAttributeValueCache2", field)) {
        String cacheValue = gcGrouperSyncMember.getEntityAttributeValueCache2();
        if (GrouperUtil.isBlank(cacheValue) || !cacheValue.contains("{")) {
          return cacheValue;
        }
        GrouperProvisioningConfigurationAttributeDbCache cache = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[2];
        if (cache == null || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          return cacheValue;
        }
        return this.grouperProvisioner.retrieveGrouperProvisioningData().parseJsonCacheEntity(cacheValue);
      }
      if (StringUtils.equals("entityAttributeValueCache3", field)) {
        String cacheValue = gcGrouperSyncMember.getEntityAttributeValueCache3();
        if (GrouperUtil.isBlank(cacheValue) || !cacheValue.contains("{")) {
          return cacheValue;
        }
        GrouperProvisioningConfigurationAttributeDbCache cache = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[3];
        if (cache == null || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          return cacheValue;
        }
        return this.grouperProvisioner.retrieveGrouperProvisioningData().parseJsonCacheEntity(cacheValue);
      }
    }

    //if we couldnt find the data but the field was ok, its just null
    if (StringUtils.equalsAny(field, "id", "email", "loginid", "memberId", "entityAttributeValueCache0", 
        "entityAttributeValueCache1", "entityAttributeValueCache2", "entityAttributeValueCache3", "name", 
        "subjectId", "subjectSourceId", "description", "subjectIdentifier", "subjectIdentifier0", "subjectIdentifier1", "subjectIdentifier2", "idIndex")) {
      return null;
    }
    
    throw new RuntimeException("Not expecting grouperProvisioningEntityField: '" + field + "'");
  }
  

  public Object attributeTranslationOrCache(Object currentValue, Map<String, Object> elVariableMap, boolean forCreate,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute, 
      ProvisioningGroupWrapper provisioningGroupWrapper, ProvisioningEntityWrapper provisioningEntityWrapper) {
    
    if (grouperProvisioningConfigurationAttribute == null) {
      return currentValue;
    }
    
    boolean[] translate = new boolean[] {false};
    boolean[] shouldRetrieveFromCache = new boolean[] {false};

    Object result = attributeTranslation(elVariableMap, forCreate,
        grouperProvisioningConfigurationAttribute, provisioningGroupWrapper,
        provisioningEntityWrapper, translate, shouldRetrieveFromCache);
    
    if (GrouperUtil.isBlank(result) && translate[0] && shouldRetrieveFromCache[0]) {
      Object cachedResult = attributeTranslationRetrieveFromCache(
          grouperProvisioningConfigurationAttribute, provisioningGroupWrapper,
          provisioningEntityWrapper);
      
      if (cachedResult != null) {
        result = cachedResult;
      }
    }
    
    return result;
    
  }

  /**
   * 
   * @param elVariableMap
   * @param forCreate
   * @param grouperProvisioningConfigurationAttribute
   * @param provisioningGroupWrapper
   * @param provisioningEntityWrapper
   * @param translate
   * @param shouldRetrieveFromCache - Only retrieve from cache if the object is delete or if there's no other translation possibility, e.g. originated from target
   * @return
   */
  public Object attributeTranslation(Map<String, Object> elVariableMap, boolean forCreate,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute,
      ProvisioningGroupWrapper provisioningGroupWrapper,
      ProvisioningEntityWrapper provisioningEntityWrapper, boolean[] translate, boolean[] shouldRetrieveFromCache) {
    
    String expressionToUse = getTargetExpressionToUse(forCreate, grouperProvisioningConfigurationAttribute);
    String staticValuesToUse = getTranslateFromStaticValuesToUse(forCreate, grouperProvisioningConfigurationAttribute);
    String grouperProvisioningGroupField = getTranslateFromGrouperProvisioningGroupField(forCreate, grouperProvisioningConfigurationAttribute);
    String grouperTargetGroupField = getTranslateFromGrouperTargetGroupField(forCreate, grouperProvisioningConfigurationAttribute);
    String grouperProvisioningEntityField = getTranslateFromGrouperProvisioningEntityField(forCreate, grouperProvisioningConfigurationAttribute);
    String grouperTargetEntityField = getTranslateFromGrouperTargetEntityField(forCreate, grouperProvisioningConfigurationAttribute);

    Object result = null;
    if (!StringUtils.isBlank(expressionToUse)) {
      result = runScript(expressionToUse, elVariableMap);
      translate[0] = true;
    } else if (!StringUtils.isBlank(staticValuesToUse)) {
      if (grouperProvisioningConfigurationAttribute.isMultiValued()) {
        result = GrouperUtil.splitTrimToSet(staticValuesToUse, ",");
      } else {
        result = staticValuesToUse;
      }
      
      translate[0] = true;
    } else if (provisioningGroupWrapper != null && provisioningGroupWrapper.getGrouperProvisioningGroup() != null && !StringUtils.isBlank(grouperProvisioningGroupField)) {
      result = translateFromGrouperProvisioningGroupField(provisioningGroupWrapper, 
          grouperProvisioningGroupField);
      translate[0] = true;
    } else if (provisioningEntityWrapper != null && provisioningEntityWrapper.getGrouperProvisioningEntity() != null && !StringUtils.isBlank(grouperProvisioningEntityField)) {
      result = translateFromGrouperProvisioningEntityField(provisioningEntityWrapper, 
          grouperProvisioningEntityField);
      translate[0] = true;
    } else if (provisioningGroupWrapper != null && provisioningGroupWrapper.getGrouperTargetGroup() != null && !StringUtils.isBlank(grouperTargetGroupField)) {
      
      ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
      result = grouperTargetGroup.retrieveAttributeValue(grouperTargetGroupField);
      translate[0] = true;
      
    } else if (provisioningEntityWrapper != null && provisioningEntityWrapper.getGrouperTargetEntity() != null && !StringUtils.isBlank(grouperTargetEntityField)) {
      
      ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();
      result = grouperTargetEntity.retrieveAttributeValue(grouperTargetEntityField);
      translate[0] = true;
      
    } else {
      if (provisioningGroupWrapper != null && provisioningGroupWrapper.getGcGrouperSyncGroup() != null 
          && grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.group) {
        // look for grouper source first, then target
        for (GrouperProvisioningConfigurationAttributeDbCache groupCache : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()) {
          if (groupCache != null && StringUtils.equals(groupCache.getAttributeName(), grouperProvisioningConfigurationAttribute.getName()) 
              && groupCache.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.attribute) {
            shouldRetrieveFromCache[0] = true;
            translate[0] = true;
            break;
          }
        }
      }
      if (provisioningEntityWrapper != null && provisioningEntityWrapper.getGcGrouperSyncMember() != null
          && grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.entity) {
        // look for grouper source first, then target
        for (GrouperProvisioningConfigurationAttributeDbCache entityCache : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()) {
          if (entityCache != null && StringUtils.equals(entityCache.getAttributeName(), grouperProvisioningConfigurationAttribute.getName()) 
              && entityCache.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.attribute) {
            shouldRetrieveFromCache[0] = true;
            translate[0] = true;
            break;
          }
        }
      }
    }
    
    if (provisioningEntityWrapper != null && provisioningEntityWrapper.getProvisioningStateEntity().isDelete()) {
      shouldRetrieveFromCache[0] = true;
    }
    
    if (provisioningGroupWrapper != null && provisioningGroupWrapper.getProvisioningStateGroup().isDelete()) {
      shouldRetrieveFromCache[0] = true;
    }
    return result;
  }

  public Object attributeTranslationRetrieveFromCache(
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute,
      ProvisioningGroupWrapper provisioningGroupWrapper,
      ProvisioningEntityWrapper provisioningEntityWrapper) {
    
    if (provisioningEntityWrapper != null
        && provisioningEntityWrapper.getGcGrouperSyncMember() != null
        && grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.entity) {

      for (GrouperProvisioningConfigurationAttributeDbCache entityCache : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()) {
        if (entityCache != null && StringUtils.equals(entityCache.getAttributeName(), grouperProvisioningConfigurationAttribute.getName()) 
            && entityCache.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.attribute) {
          Object result = translateFromGrouperProvisioningEntityField(provisioningEntityWrapper, 
              "entityAttributeValueCache" + entityCache.getIndex());
          return result;
        }
      }
      
    }
    
    if (provisioningGroupWrapper != null
        && provisioningGroupWrapper.getGcGrouperSyncGroup() != null
        && grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.group) {
      for (GrouperProvisioningConfigurationAttributeDbCache groupCache : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()) {
        if (groupCache != null && StringUtils.equals(groupCache.getAttributeName(), grouperProvisioningConfigurationAttribute.getName()) 
            && groupCache.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.attribute) {
          Object result = translateFromGrouperProvisioningGroupField(provisioningGroupWrapper, 
              "groupAttributeValueCache" + groupCache.getIndex());
          return result;
        }
      }
    }
    
    return null;
    
  }
  
  /**
   * get the matching and search ids for a target group (could be grouperTargetGroup or targetProvisioningGroup)
   * @param targetGroups
   */
  public void idTargetGroups(List<ProvisioningGroup> targetGroups) {

    if (GrouperUtil.isBlank(targetGroups)) {
      return;
    }

    for (ProvisioningGroup targetGroup: GrouperUtil.nonNull(targetGroups)) {
      
      Set<ProvisioningUpdatableAttributeAndValue> provisioningUpdatableAttributeAndValues = new LinkedHashSet<ProvisioningUpdatableAttributeAndValue>();
      targetGroup.setMatchingIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);

      if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isGroupMatchingAttributeSameAsSearchAttribute()) {
        targetGroup.setSearchIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);
      }

      // first do all current values, then do all past values
      for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMatchingAttributes()) {
        String matchingAttributeName = matchingAttribute.getName();
        
        // dont worry if dupes... oh well
//        Object targetCurrentValue = massageToString(targetGroup.retrieveAttributeValue(matchingAttributeName), 2);
        Object targetCurrentValue = targetGroup.retrieveAttributeValue(matchingAttributeName);

        if(!GrouperUtil.isBlank(targetCurrentValue)) {
          
          ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(
              this.getGrouperProvisioner(), matchingAttributeName, targetCurrentValue,
              GrouperProvisioningConfigurationAttributeType.group);
          provisioningUpdatableAttributeAndValue.setCurrentValue(true);
          provisioningUpdatableAttributeAndValues.add(provisioningUpdatableAttributeAndValue);
        }
      }
      
      // dont get old values for target side objects
      if (!targetGroup.isGrouperTargetObject()) {
        continue;
      }
      
      //old values
      for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMatchingAttributes()) {
        String matchingAttributeName = matchingAttribute.getName();

        Set<Object> cachedValues = GrouperProvisioningConfigurationAttributeDbCache.cachedValuesForGroup(targetGroup, matchingAttributeName);

        for (Object cachedValue : GrouperUtil.nonNull(cachedValues)) {
          
          if (!GrouperUtil.isEmpty(cachedValue)) {
            cachedValue = massageToString(cachedValue, 2);
            
            ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(
                this.getGrouperProvisioner(), matchingAttributeName, cachedValue, GrouperProvisioningConfigurationAttributeType.group);
            provisioningUpdatableAttributeAndValue.setCurrentValue(false);
            
            // keep the order so see if its there before adding
            if (!provisioningUpdatableAttributeAndValues.contains(provisioningUpdatableAttributeAndValue)) {
              provisioningUpdatableAttributeAndValues.add(provisioningUpdatableAttributeAndValue);
            }
          }
        }
      }
    }
    // search attributes
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isGroupMatchingAttributeSameAsSearchAttribute()) {
      for (ProvisioningGroup targetGroup: GrouperUtil.nonNull(targetGroups)) {
        
        Set<ProvisioningUpdatableAttributeAndValue> provisioningUpdatableAttributeAndValues = new LinkedHashSet<ProvisioningUpdatableAttributeAndValue>();
        targetGroup.setSearchIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);

        // first do all current values, then do all past values
        for (GrouperProvisioningConfigurationAttribute searchAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupSearchAttributes()) {
          String searchAttributeName = searchAttribute.getName();
          
          // dont worry if dupes... oh well
          Object targetCurrentValue = massageToString(targetGroup.retrieveAttributeValue(searchAttributeName), 2);

          if(!GrouperUtil.isBlank(targetCurrentValue)) {
            
            ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(
                this.getGrouperProvisioner(), searchAttributeName, targetCurrentValue, GrouperProvisioningConfigurationAttributeType.group);
            provisioningUpdatableAttributeAndValue.setCurrentValue(true);
            provisioningUpdatableAttributeAndValues.add(provisioningUpdatableAttributeAndValue);
          }
        }
        
        // dont get old values for target side objects
        if (!targetGroup.isGrouperTargetObject()) {
          continue;
        }
        
        //old values
        for (GrouperProvisioningConfigurationAttribute searchAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupSearchAttributes()) {
          String searchAttributeName = searchAttribute.getName();

          Set<Object> cachedValues = GrouperProvisioningConfigurationAttributeDbCache.cachedValuesForGroup(targetGroup, searchAttributeName);

          for (Object cachedValue : GrouperUtil.nonNull(cachedValues)) {
            
            if (!GrouperUtil.isEmpty(cachedValue)) {
              cachedValue = massageToString(cachedValue, 2);
              ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(
                  this.getGrouperProvisioner(), searchAttributeName, cachedValue, GrouperProvisioningConfigurationAttributeType.group);
              provisioningUpdatableAttributeAndValue.setCurrentValue(false);
              // keep the order so see if its there before adding
              if (!provisioningUpdatableAttributeAndValues.contains(provisioningUpdatableAttributeAndValue)) {
                provisioningUpdatableAttributeAndValues.add(provisioningUpdatableAttributeAndValue);
              }
            }
          }
        }
      }
      
    }
    
  }

  public void idTargetEntities(List<ProvisioningEntity> targetEntities) {

    if (GrouperUtil.isBlank(targetEntities)) {
      return;
    }

    for (ProvisioningEntity targetEntity: GrouperUtil.nonNull(targetEntities)) {
      
      Set<ProvisioningUpdatableAttributeAndValue> provisioningUpdatableAttributeAndValues = new LinkedHashSet<ProvisioningUpdatableAttributeAndValue>();
      targetEntity.setMatchingIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);

      if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isEntityMatchingAttributeSameAsSearchAttribute()) {
        targetEntity.setSearchIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);
      }

      boolean grouperTargetObject = targetEntity.isGrouperTargetObject();

      // first do all current values, then do all past values
      for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMatchingAttributes()) {
        String matchingAttributeName = matchingAttribute.getName();
        
        // dont worry if dupes... oh well
//        Object targetCurrentValue = massageToString(targetEntity.retrieveAttributeValue(matchingAttributeName), 2);
        Object targetCurrentValue = targetEntity.retrieveAttributeValue(matchingAttributeName);

        if(!GrouperUtil.isBlank(targetCurrentValue)) {
          
          ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(
              this.getGrouperProvisioner(), matchingAttributeName, targetCurrentValue, GrouperProvisioningConfigurationAttributeType.entity);
          provisioningUpdatableAttributeAndValue.setCurrentValue(true);
          provisioningUpdatableAttributeAndValues.add(provisioningUpdatableAttributeAndValue);
        }
      }
      
      // dont get old values for target side objects
      if (!grouperTargetObject) {
        continue;
      }
      
      //old values
      for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMatchingAttributes()) {
        String matchingAttributeName = matchingAttribute.getName();

        Set<Object> cachedValues = GrouperProvisioningConfigurationAttributeDbCache.cachedValuesForEntity(targetEntity, matchingAttributeName);

        for (Object cachedValue : GrouperUtil.nonNull(cachedValues)) {
          
          if (!GrouperUtil.isEmpty(cachedValue)) {
            cachedValue = massageToString(cachedValue, 2);
            ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(
                this.getGrouperProvisioner(), matchingAttributeName, cachedValue, GrouperProvisioningConfigurationAttributeType.entity);
            provisioningUpdatableAttributeAndValue.setCurrentValue(false);
            
            // keep the order so see if its there before adding
            if (!provisioningUpdatableAttributeAndValues.contains(provisioningUpdatableAttributeAndValue)) {
              provisioningUpdatableAttributeAndValues.add(provisioningUpdatableAttributeAndValue);
            }
          }
        }
      }
    }
    // search attributes
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isEntityMatchingAttributeSameAsSearchAttribute()) {
      for (ProvisioningEntity targetEntity: GrouperUtil.nonNull(targetEntities)) {
        
        Set<ProvisioningUpdatableAttributeAndValue> provisioningUpdatableAttributeAndValues = new LinkedHashSet<ProvisioningUpdatableAttributeAndValue>();
        targetEntity.setSearchIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);

        // first do all current values, then do all past values
        for (GrouperProvisioningConfigurationAttribute searchAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntitySearchAttributes()) {
          String searchAttributeName = searchAttribute.getName();
          
          // dont worry if dupes... oh well
          Object targetCurrentValue = massageToString(targetEntity.retrieveAttributeValue(searchAttributeName), 2);

          if(!GrouperUtil.isBlank(targetCurrentValue)) {
            
            ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(
                this.getGrouperProvisioner(), searchAttributeName, targetCurrentValue, GrouperProvisioningConfigurationAttributeType.entity);
            provisioningUpdatableAttributeAndValue.setCurrentValue(true);

            provisioningUpdatableAttributeAndValues.add(provisioningUpdatableAttributeAndValue);
          }
        }
        
        // dont get old values for target side objects
        if (!targetEntity.isGrouperTargetObject()) {
          continue;
        }
        
        //old values
        for (GrouperProvisioningConfigurationAttribute searchAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntitySearchAttributes()) {
          String searchAttributeName = searchAttribute.getName();

          Set<Object> cachedValues = GrouperProvisioningConfigurationAttributeDbCache.cachedValuesForEntity(targetEntity, searchAttributeName);

          for (Object cachedValue : GrouperUtil.nonNull(cachedValues)) {
            
            if (!GrouperUtil.isEmpty(cachedValue)) {
              cachedValue = massageToString(cachedValue, 2);
              ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(
                  this.getGrouperProvisioner(), searchAttributeName, cachedValue, GrouperProvisioningConfigurationAttributeType.entity);
              provisioningUpdatableAttributeAndValue.setCurrentValue(false);
              
              // keep the order so see if its there before adding
              if (!provisioningUpdatableAttributeAndValues.contains(provisioningUpdatableAttributeAndValue)) {
                provisioningUpdatableAttributeAndValues.add(provisioningUpdatableAttributeAndValue);
              }
            }
          }
        }
      }
      
    }
  }

  public Object massageToString(Object id, int timeToLive) {
    if (timeToLive-- < 0) {
      throw new RuntimeException("timeToLive expired?????  why????");
    }
    if (id == null) {
      return null;
    }
    if (id instanceof String) {
      return id;
    }
    if (id instanceof Number) {
      return id.toString();
    }
    if (id instanceof MultiKey) {
      MultiKey idMultiKey = (MultiKey)id;
      Object[] newMultiKey = new Object[idMultiKey.size()];
      for (int i=0;i<newMultiKey.length;i++) {
        newMultiKey[i] = massageToString(idMultiKey.getKey(i), timeToLive);
      }
      return new MultiKey(newMultiKey);
    }
    // uh...
    throw new RuntimeException("matching ids should be string, number, or multikey of string and number! " + id.getClass() + ", " + id);
  }

  public void idTargetMemberships(List<ProvisioningMembership> targetMemberships) {

    if (GrouperUtil.isBlank(targetMemberships)) {
      return;
    }
    String membershipIdScript = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getMembershipMatchingIdExpression(); 

    String membershipIdAttribute = null; //this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getMembershipMatchingIdAttribute();

    //this is a legacy typo
    if (StringUtils.equals(membershipIdAttribute, "provisioningGroupId,provisioningMembershipId")) {
      membershipIdAttribute = "provisioningGroupId,provisioningEntityId";
    }
    
    if (StringUtils.isBlank(membershipIdScript) && StringUtils.isBlank(membershipIdAttribute)) {
      
      if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
        membershipIdAttribute = "provisioningGroupId,provisioningEntityId";
      } else {
        return;
      }
    }
    
    OUTER: for (ProvisioningMembership targetMembership: GrouperUtil.nonNull(targetMemberships)) {
      
      Object id = null;
      if (!StringUtils.isBlank(membershipIdAttribute)) {
        if ("provisioningGroupId,provisioningEntityId".equals(membershipIdAttribute)) {
          id = new MultiKey(targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntityId());
        } else {
          Object idValue = targetMembership.retrieveAttributeValue(membershipIdAttribute);
          if (idValue instanceof Collection) {
            throw new RuntimeException("Cant have a multivalued matching id attribute: '" + membershipIdAttribute + "', " + targetMembership);
          }
          id = idValue;
        }
      } else if (!StringUtils.isBlank(membershipIdScript)) {
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetMembership", targetMembership);
        
        id = runScript(membershipIdScript, elVariableMap);

                
      } else {
        throw new RuntimeException("Must have membershipMatchingIdAttribute, or membershipMatchingIdExpression");
      }
//      id = massageToString(id, 2);
      if (id instanceof MultiKey) {
        
        MultiKey matchingIdMultiKey = (MultiKey)id;
        for (int i=0; i<matchingIdMultiKey.size(); i++) {
          if (matchingIdMultiKey.getKey(i) == null) {
            // if the membership is a delete and not in target then just dont worry about it, its old
            if (targetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().isDelete()
                && (targetMembership.getProvisioningMembershipWrapper().getGcGrouperSyncMembership().getInTarget() == null
                || !targetMembership.getProvisioningMembershipWrapper().getGcGrouperSyncMembership().getInTarget())) {
              this.getGrouperProvisioner().retrieveGrouperProvisioningData().removeAndUnindexMembershipWrapper(targetMembership.getProvisioningMembershipWrapper());
              continue OUTER;
            }
            GcGrouperSyncErrorCode errorCode = GcGrouperSyncErrorCode.DNE;
            String errorMessage = "membership multiKey has blank value in index: " + i;
            this.grouperProvisioner.retrieveGrouperProvisioningValidation()
              .assignMembershipError(targetMembership.getProvisioningMembershipWrapper(), errorCode, errorMessage);
            continue OUTER;
          }
        }
        
      }
      if (id == null) {
        // if the membership is a delete and not in target then just dont worry about it, its old
        if (targetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().isDelete()
            && (targetMembership.getProvisioningMembershipWrapper().getGcGrouperSyncMembership().getInTarget() == null
            || !targetMembership.getProvisioningMembershipWrapper().getGcGrouperSyncMembership().getInTarget())) {
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().removeAndUnindexMembershipWrapper(targetMembership.getProvisioningMembershipWrapper());
          continue OUTER;
        }
      }
      // just hard code to "id" since memberships just have one matching id
      ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(
          this.getGrouperProvisioner(), "id", id,
          GrouperProvisioningConfigurationAttributeType.membership);
      provisioningUpdatableAttributeAndValue.setCurrentValue(true);

      targetMembership.setMatchingIdAttributeNameToValues(GrouperUtil.toSet(provisioningUpdatableAttributeAndValue));

    }

  }

  public Object runScript(String script, Map<String, Object> elVariableMap) {
    return runScriptStatic(script, elVariableMap);
  }

  public static Object runScriptStatic(String script, Map<String, Object> elVariableMap) {
    try {
      if (!script.contains("${")) {
        script = "${" + script + "}";
      }
      return GrouperUtil.substituteExpressionLanguageScript(script, elVariableMap, true, false, false);
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, ", script: '" + script + "', ");
      GrouperUtil.injectInException(re, GrouperUtil.toStringForLog(elVariableMap));
      throw re;
    }
  }

  public Object runExpression(String script, Map<String, Object> elVariableMap) {
    try {
      if (!script.contains("${")) {
        script = "${" + script + "}";
      }
      return GrouperUtil.substituteExpressionLanguage(script, elVariableMap, true, false, false);
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, ", script: '" + script + "', ");
      GrouperUtil.injectInException(re, GrouperUtil.toStringForLog(elVariableMap));
      throw re;
    }
  }

  public void matchingIdTargetObjects() {
    idTargetGroups(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveTargetProvisioningGroups());
    idTargetEntities(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities());
    idTargetMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveTargetProvisioningMemberships());
  }
  
  /**
   * translate from gc grouper sync group and field name to the value
   * @param provisioningGroupWrapper
   * @param field
   * @return the value
   */
  public Object translateFromGrouperProvisioningGroupField(ProvisioningGroupWrapper provisioningGroupWrapper, String field) {
    
    // "id", "idIndex", "idIndexString", "displayExtension", "displayName", "extension", "groupAttributeValueCache0", "groupAttributeValueCache1", "groupAttributeValueCache2", "groupAttributeValueCache3", "name", "description"
    if (provisioningGroupWrapper == null) { 
      return null;
    }
    
    ProvisioningGroup provisioningGroup = provisioningGroupWrapper.getGrouperProvisioningGroup();
    
    if (provisioningGroup != null) {
      if (StringUtils.equals("id", field) && !StringUtils.isBlank(provisioningGroup.getId())) {
        return provisioningGroup.getId();
      }
      if (StringUtils.equals("idIndex", field) && provisioningGroup.getIdIndex() != null) {
        return provisioningGroup.getIdIndex();
      }
      if (StringUtils.equals("idIndexString", field) && provisioningGroup.getIdIndex() != null) {
        return GrouperUtil.stringValue(provisioningGroup.getIdIndex());
      }
      if (StringUtils.equals("displayExtension", field)) {
        return GrouperUtil.stringValue(provisioningGroup.getDisplayExtension());
      }
      if (StringUtils.equals("displayName", field)) {
        return GrouperUtil.stringValue(provisioningGroup.getDisplayName());
      }
      if (StringUtils.equals("extension", field) && !StringUtils.isBlank(provisioningGroup.getExtension())) {
        return GrouperUtil.stringValue(provisioningGroup.getExtension());
      }
      if (StringUtils.equals("name", field) && !StringUtils.isBlank(provisioningGroup.getName())) {
        return GrouperUtil.stringValue(provisioningGroup.getName());
      }
      if (StringUtils.equals("description", field)) {
        return GrouperUtil.stringValue(provisioningGroup.retrieveAttributeValueString("description"));
      }
      
    }
    
    GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
    
    if (gcGrouperSyncGroup != null) {
      if (StringUtils.equals("id", field)) {
        return gcGrouperSyncGroup.getGroupId();
      }
      if (StringUtils.equals("idIndex", field)) {
        return gcGrouperSyncGroup.getGroupIdIndex();
      }
      if (StringUtils.equals("idIndexString", field)) {
        return GrouperUtil.stringValue(gcGrouperSyncGroup.getGroupIdIndex());
      }
      if (StringUtils.equals("extension", field)) {
        return GrouperUtil.extensionFromName(gcGrouperSyncGroup.getGroupName());
      }
      if (StringUtils.equals("name", field)) {
        return gcGrouperSyncGroup.getGroupName();
      }
      if (StringUtils.equals("groupAttributeValueCache0", field)) {
        String cacheValue = gcGrouperSyncGroup.getGroupAttributeValueCache0();
        if (GrouperUtil.isBlank(cacheValue) || !cacheValue.contains("{")) {
          return cacheValue;
        }
        GrouperProvisioningConfigurationAttributeDbCache cache = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()[0];
        if (cache == null || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          return cacheValue;
        }
        return this.grouperProvisioner.retrieveGrouperProvisioningData().parseJsonCacheGroup(cacheValue);
      }
      if (StringUtils.equals("groupAttributeValueCache1", field)) {
        String cacheValue = gcGrouperSyncGroup.getGroupAttributeValueCache1();
        if (GrouperUtil.isBlank(cacheValue) || !cacheValue.contains("{")) {
          return cacheValue;
        }
        GrouperProvisioningConfigurationAttributeDbCache cache = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()[1];
        if (cache == null || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          return cacheValue;
        }
        return this.grouperProvisioner.retrieveGrouperProvisioningData().parseJsonCacheGroup(cacheValue);
      }
      if (StringUtils.equals("groupAttributeValueCache2", field)) {
        String cacheValue = gcGrouperSyncGroup.getGroupAttributeValueCache2();
        if (GrouperUtil.isBlank(cacheValue) || !cacheValue.contains("{")) {
          return cacheValue;
        }
        GrouperProvisioningConfigurationAttributeDbCache cache = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()[2];
        if (cache == null || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          return cacheValue;
        }
        return this.grouperProvisioner.retrieveGrouperProvisioningData().parseJsonCacheGroup(cacheValue);
      }
      if (StringUtils.equals("groupAttributeValueCache3", field)) {
        String cacheValue = gcGrouperSyncGroup.getGroupAttributeValueCache3();
        if (GrouperUtil.isBlank(cacheValue) || !cacheValue.contains("{")) {
          return cacheValue;
        }
        GrouperProvisioningConfigurationAttributeDbCache cache = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()[3];
        if (cache == null || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          return cacheValue;
        }
        return this.grouperProvisioner.retrieveGrouperProvisioningData().parseJsonCacheGroup(cacheValue);
      }
      
    }
    
    //if we couldnt find the data but the field was ok, its just null
    if (StringUtils.equalsAny(field, "id", "idIndex", "idIndexString", "displayExtension", "displayName", "extension", 
        "groupAttributeValueCache0", "groupAttributeValueCache1", "groupAttributeValueCache2", "groupAttributeValueCache3", 
        "name", "description")) {
      return null;
    }
    
    throw new RuntimeException("Not expecting grouperProvisioningGroupField: '" + field + "'");

  }

  private boolean translateGrouperToTargetAutomatically;
  
  public void setTranslateGrouperToTargetAutomatically(boolean translateGrouperToTargetAutomatically) {
    this.translateGrouperToTargetAutomatically = translateGrouperToTargetAutomatically;
  }

  
  public boolean isTranslateGrouperToTargetAutomatically() {
    return translateGrouperToTargetAutomatically;
  }

  public String getTranslateFromGrouperProvisioningGroupField(boolean forCreate, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    String expression = grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupField();
    boolean hasExpression = !StringUtils.isBlank(expression);
    String expressionCreateOnly = grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupFieldCreateOnly();
    boolean hasExpressionCreateOnly = !StringUtils.isBlank(expressionCreateOnly);
    String expressionToUse = null;
    if (forCreate && hasExpressionCreateOnly) {
      expressionToUse = expressionCreateOnly;
    } else if (hasExpression) {
      expressionToUse = expression;
    }

    return expressionToUse;
  }
  
  public String getTranslateFromGrouperTargetGroupField(boolean forCreate, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    String expression = grouperProvisioningConfigurationAttribute.getTranslateFromGrouperTargetGroupField();
    return expression;
  }
  
  public String getTranslateFromGrouperTargetEntityField(boolean forCreate, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    String expression = grouperProvisioningConfigurationAttribute.getTranslateFromGrouperTargetEntityField();
    return expression;
  }

  public String getTranslateFromGrouperProvisioningEntityField(boolean forCreate, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    String expression = grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningEntityField();
    boolean hasExpression = !StringUtils.isBlank(expression);
    String expressionCreateOnly = grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningEntityFieldCreateOnly();
    boolean hasExpressionCreateOnly = !StringUtils.isBlank(expressionCreateOnly);
    String expressionToUse = null;
    if (forCreate && hasExpressionCreateOnly) {
      expressionToUse = expressionCreateOnly;
    } else if (hasExpression) {
      expressionToUse = expression;
    }

    return expressionToUse;
  }
  

  public String getTargetExpressionToUse(boolean forCreate, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    String expression = grouperProvisioningConfigurationAttribute.getTranslateExpression();
    boolean hasExpression = !StringUtils.isBlank(expression);
    String expressionCreateOnly = grouperProvisioningConfigurationAttribute.getTranslateExpressionCreateOnly();
    boolean hasExpressionCreateOnly = !StringUtils.isBlank(expressionCreateOnly);
    String expressionToUse = null;
    if (forCreate && hasExpressionCreateOnly) {
      expressionToUse = expressionCreateOnly;
    } else if (hasExpression) {
      expressionToUse = expression;
    }

    return expressionToUse;
  }
  
  public String getTranslateFromStaticValuesToUse(boolean forCreate, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    String staticValues = grouperProvisioningConfigurationAttribute.getTranslateFromStaticValues();
    boolean hasStaticValues = !StringUtils.isBlank(staticValues);
    String staticValuesCreateOnly = grouperProvisioningConfigurationAttribute.getTranslateFromStaticValuesCreateOnly();
    boolean hasStaticValuesCreateOnly = !StringUtils.isBlank(staticValuesCreateOnly);
    String staticValuesToUse = null;
    if (forCreate && hasStaticValuesCreateOnly) {
      staticValuesToUse = staticValuesCreateOnly;
    } else if (hasStaticValues) {
      staticValuesToUse = staticValues;
    }

    return staticValuesToUse;
  }
}