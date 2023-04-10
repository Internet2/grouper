package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

/**
 * main list of wrapper beans
 * @author mchyzer
 *
 */
public class GrouperProvisioningData {

  /**
   * map of retrieved entity to target native entity, optional, only if the target native entity is needed later on
   */
  private Map<ProvisioningEntity, Object> targetEntityToTargetNativeEntity = Collections.synchronizedMap(new HashMap<ProvisioningEntity, Object>());

  
  /**
   * map of retrieved entity to target native entity, optional, only if the target native entity is needed later on
   * @return
   */
  public Map<ProvisioningEntity, Object> getTargetEntityToTargetNativeEntity() {
    return targetEntityToTargetNativeEntity;
  }

  /**
   * cache json to provisioning group so the json doesnt have to be parsed repeatedly
   */
  private Map<String, ProvisioningGroup> cacheJsonToProvisioningGroup = new HashMap<String, ProvisioningGroup>();

  /**
   * cache json to provisioning group so the json doesnt have to be parsed repeatedly
   * @return
   */
  public Map<String, ProvisioningGroup> getCacheJsonToProvisioningGroup() {
    return this.cacheJsonToProvisioningGroup;
  }

  /**
   * convert from json to provisioningEntity
   * @param json
   * @return the provisioningEntity
   */
  public ProvisioningEntity parseJsonCacheEntity(String json) {
    if (StringUtils.isBlank(json)) {
      return null;
    }
    ProvisioningEntity provisioningEntity = this.cacheJsonToProvisioningEntity.get(json);
    
    if (provisioningEntity != null) {
      return provisioningEntity;
    }
    
    try {
      provisioningEntity = new ProvisioningEntity();
      provisioningEntity.fromJsonForCache(json);
      
    } catch (Exception e) {
      LOG.error("Problem parsing json '" + json + "'", e);
      provisioningEntity = null;
    }
    
    this.cacheJsonToProvisioningEntity.put(json, provisioningEntity);
    return provisioningEntity;
    
  }
  

  /**
   * convert from json to provisioningGroup
   * @param json
   * @return the provisioningGroup
   */
  public ProvisioningGroup parseJsonCacheGroup(String json) {
    if (StringUtils.isBlank(json)) {
      return null;
    }
    ProvisioningGroup provisioningGroup = this.cacheJsonToProvisioningGroup.get(json);
    
    if (provisioningGroup != null) {
      return provisioningGroup;
    }
    
    try {
      provisioningGroup = new ProvisioningGroup();
      provisioningGroup.fromJsonForCache(json);
      
    } catch (Exception e) {
      LOG.error("Problem parsing json '" + json + "'", e);
      provisioningGroup = null;
    }
    
    this.cacheJsonToProvisioningGroup.put(json, provisioningGroup);
    return provisioningGroup;
    
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningData.class);

  /**
   * cache json to provisioning entity so the json doesnt have to be parsed repeatedly
   */
  private Map<String, ProvisioningEntity> cacheJsonToProvisioningEntity = new HashMap<String, ProvisioningEntity>();

  
  /**
   * cache json to provisioning entity so the json doesnt have to be parsed repeatedly
   * @return
   */
  public Map<String, ProvisioningEntity> getCacheJsonToProvisioningEntity() {
    return this.cacheJsonToProvisioningEntity;
  }

  /**
   * 
   * @param provisioningGroupWrapper
   */
  public void addAndIndexGroupWrapper(ProvisioningGroupWrapper provisioningGroupWrapper) {
    this.provisioningGroupWrappers.add(provisioningGroupWrapper);
    GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
    
    if (StringUtils.isNotBlank(provisioningGroupWrapper.getGroupId())) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().put(provisioningGroupWrapper.getGroupId(), provisioningGroupWrapper);
    }
    
    if (gcGrouperSyncGroup != null) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdToProvisioningGroupWrapper().put(gcGrouperSyncGroup.getId(), provisioningGroupWrapper);
    }    
  }
  
  /**
   * 
   * @param provisioningEntityWrapper
   */
  public void addAndIndexEntityWrapper(ProvisioningEntityWrapper provisioningEntityWrapper) {
    this.provisioningEntityWrappers.add(provisioningEntityWrapper);
    GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
    
    if (StringUtils.isNotBlank(provisioningEntityWrapper.getMemberId())) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().put(provisioningEntityWrapper.getMemberId(), provisioningEntityWrapper);
    }
    
    if (gcGrouperSyncMember != null) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncMemberIdToProvisioningEntityWrapper().put(gcGrouperSyncMember.getId(), provisioningEntityWrapper);
    }    
  }
  
  /**
   * 
   * @param provisioningEntityWrapper
   */
  public void removeAndUnindexEntityWrapper(ProvisioningEntityWrapper provisioningEntityWrapper) {
    this.provisioningEntityWrappers.remove(provisioningEntityWrapper);
    GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
    
    if (StringUtils.isNotBlank(provisioningEntityWrapper.getMemberId())) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().remove(provisioningEntityWrapper.getMemberId());
    }
    
    if (gcGrouperSyncMember != null) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncMemberIdToProvisioningEntityWrapper().remove(gcGrouperSyncMember.getId());
    }    
  }
  
  /**
   * 
   * @param provisioningGroupWrapper
   */
  public void removeAndUnindexGroupWrapper(ProvisioningGroupWrapper provisioningGroupWrapper) {
    this.provisioningGroupWrappers.remove(provisioningGroupWrapper);
    GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
    
    if (StringUtils.isNotBlank(provisioningGroupWrapper.getGroupId())) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().remove(provisioningGroupWrapper.getGroupId());
    }
    
    if (gcGrouperSyncGroup != null) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdToProvisioningGroupWrapper().remove(gcGrouperSyncGroup.getId());
    }    
  }
  
  /**
   * 
   * @param provisioningMembershipWrapper
   */
  public void addAndIndexMembershipWrapper(ProvisioningMembershipWrapper provisioningMembershipWrapper) {
    this.provisioningMembershipWrappers.add(provisioningMembershipWrapper);
    ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
    GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
    
    if (grouperProvisioningMembership != null && !StringUtils.isBlank(grouperProvisioningMembership.getProvisioningGroupId())
        && !StringUtils.isBlank(grouperProvisioningMembership.getProvisioningEntityId())) {
      MultiKey groupIdEntityId = new MultiKey(grouperProvisioningMembership.getProvisioningGroupId(), grouperProvisioningMembership.getProvisioningEntityId());
      provisioningMembershipWrapper.setGroupIdMemberId(groupIdEntityId);
    }
    
    if (provisioningMembershipWrapper.getGroupIdMemberId() != null) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex()
      .getGroupUuidMemberUuidToProvisioningMembershipWrapper().put(
          provisioningMembershipWrapper.getGroupIdMemberId(), provisioningMembershipWrapper);
    }
    
    if (gcGrouperSyncMembership != null) {
      MultiKey syncGroupIdSyncMemberId = new MultiKey(gcGrouperSyncMembership.getGrouperSyncGroupId(), gcGrouperSyncMembership.getGrouperSyncMemberId());
      provisioningMembershipWrapper.setSyncGroupIdSyncMemberId(syncGroupIdSyncMemberId);
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper().put(
          syncGroupIdSyncMemberId, provisioningMembershipWrapper);
    }
  }
  
  /**
   * all group wrappers
   */
  private Set<ProvisioningGroupWrapper> provisioningGroupWrappers = new HashSet<ProvisioningGroupWrapper>();

  /**
   * all entity wrappers
   */
  private Set<ProvisioningEntityWrapper> provisioningEntityWrappers = new HashSet<ProvisioningEntityWrapper>();

  /**
   * all membership wrappers
   */
  private Set<ProvisioningMembershipWrapper> provisioningMembershipWrappers = new HashSet<ProvisioningMembershipWrapper>();

  private GrouperProvisioner grouperProvisioner;

  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }


  
  public Set<ProvisioningGroupWrapper> getProvisioningGroupWrappers() {
    return provisioningGroupWrappers;
  }


  public Set<ProvisioningEntityWrapper> getProvisioningEntityWrappers() {
    return provisioningEntityWrappers;
  }

  
  public Set<ProvisioningMembershipWrapper> getProvisioningMembershipWrappers() {
    return provisioningMembershipWrappers;
  }


  /**
   * extract list of non null target provisioning groups
   * @return groups
   */
  public List<ProvisioningGroup> retrieveTargetProvisioningGroups() {
    List<ProvisioningGroup> targetProvisioningGroups = new ArrayList<ProvisioningGroup>();
    for (ProvisioningGroupWrapper provisioningGroupWrapper : this.provisioningGroupWrappers) {
      ProvisioningGroup targetProvisioningGroup = provisioningGroupWrapper.getTargetProvisioningGroup();
      if (targetProvisioningGroup != null) {
        targetProvisioningGroups.add(targetProvisioningGroup);
      }
    }
    return targetProvisioningGroups;
  }

  /**
   * extract list of non null target provisioning entities
   * @return entities
   */
  public List<ProvisioningEntity> retrieveTargetProvisioningEntities() {
    List<ProvisioningEntity> targetProvisioningEntities = new ArrayList<ProvisioningEntity>();
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.provisioningEntityWrappers) {
      ProvisioningEntity targetProvisioningEntity = provisioningEntityWrapper.getTargetProvisioningEntity();
      if (targetProvisioningEntity != null) {
        targetProvisioningEntities.add(targetProvisioningEntity);
      }
    }
    return targetProvisioningEntities;
  }

  /**
   * extract list of non null target provisioning memberships
   * @return memberships
   */
  public List<ProvisioningMembership> retrieveTargetProvisioningMemberships() {
    List<ProvisioningMembership> targetProvisioningMemberships = new ArrayList<ProvisioningMembership>();
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.provisioningMembershipWrappers) {
      ProvisioningMembership targetProvisioningMembership = provisioningMembershipWrapper.getTargetProvisioningMembership();
      if (targetProvisioningMembership != null) {
        targetProvisioningMemberships.add(targetProvisioningMembership);
      }
    }
    return targetProvisioningMemberships;
  }

  /**
   * extract list of non null grouper target groups
   * @return groups
   */
  public List<ProvisioningGroup> retrieveGrouperTargetGroups() {
    List<ProvisioningGroup> grouperTargetGroups = new ArrayList<ProvisioningGroup>();
    for (ProvisioningGroupWrapper provisioningGroupWrapper : this.provisioningGroupWrappers) {
      ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
      if (grouperTargetGroup != null) {
        grouperTargetGroups.add(grouperTargetGroup);
      }
    }
    return grouperTargetGroups;
  }

  /**
   * extract list of non null grouper target entities
   * @return groups
   */
  public List<ProvisioningEntity> retrieveGrouperTargetEntities() {
    List<ProvisioningEntity> grouperTargetEntities = new ArrayList<ProvisioningEntity>();
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.provisioningEntityWrappers) {
      ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();
      if (grouperTargetEntity != null) {
        grouperTargetEntities.add(grouperTargetEntity);
      }
    }
    return grouperTargetEntities;
  }

  /**
   * extract list of non null grouper target memberships
   * @param forCreate - null means all, true means only if we're in the create and we're creating memberships while we create groups and entities, 
   * false means we're just doing updates and deletes
   * @return groups
   */
  public List<ProvisioningMembership> retrieveGrouperTargetMemberships(Boolean forCreate) {
    List<ProvisioningMembership> grouperTargetMemberships = new ArrayList<ProvisioningMembership>();
    
    GrouperProvisioningBehaviorMembershipType membershipType = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType();
    
    boolean createGroupsAndEntitiesBeforeTranslatingMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isCreateGroupsAndEntitiesBeforeTranslatingMemberships();
    
    if (forCreate != null && forCreate && createGroupsAndEntitiesBeforeTranslatingMemberships) {
      return grouperTargetMemberships;
    }
    
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.provisioningMembershipWrappers) {
      ProvisioningMembership grouperTargetMembership = provisioningMembershipWrapper.getGrouperTargetMembership();
      if (grouperTargetMembership != null) {
        
        if (forCreate == null || membershipType == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
          grouperTargetMemberships.add(grouperTargetMembership);
        } else if (!forCreate && createGroupsAndEntitiesBeforeTranslatingMemberships) {
          grouperTargetMemberships.add(grouperTargetMembership);
        } else {
          
          if (membershipType == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
            if (provisioningMembershipWrapper.getGrouperProvisioningMembership().getProvisioningEntity() != null) {
              ProvisioningEntityWrapper entityWrapper = provisioningMembershipWrapper.getGrouperProvisioningMembership().getProvisioningEntity().getProvisioningEntityWrapper();
              if (entityWrapper.getProvisioningStateEntity().isCreate() == forCreate) {
                grouperTargetMemberships.add(grouperTargetMembership);
              }
            } 
          }
          
          if (membershipType == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
            if (provisioningMembershipWrapper.getGrouperProvisioningMembership().getProvisioningGroup() != null) {
              ProvisioningGroupWrapper groupWrapper = provisioningMembershipWrapper.getGrouperProvisioningMembership().getProvisioningGroup().getProvisioningGroupWrapper();
              if (groupWrapper.getProvisioningStateGroup().isCreate() == forCreate) {
                grouperTargetMemberships.add(grouperTargetMembership);
              }
            } 
            
          }
        }
      }
      
    }
    
    return grouperTargetMemberships;
  }

  /**
   * 
   * @return the lists
   */
  public GrouperProvisioningLists retrieveGrouperTargetProvisioningLists() {
    GrouperProvisioningLists result = new GrouperProvisioningLists();
    result.setProvisioningEntities(this.retrieveGrouperTargetEntities());
    result.setProvisioningGroups(this.retrieveGrouperTargetGroups());
    result.setProvisioningMemberships(this.retrieveGrouperTargetMemberships(null));
    return result;
  }
  
  /**
   * extract list of non null grouper provisioning groups
   * @return groups
   */
  public List<ProvisioningGroup> retrieveGrouperProvisioningGroups() {
    List<ProvisioningGroup> grouperProvisioningGroups = new ArrayList<ProvisioningGroup>();
    for (ProvisioningGroupWrapper provisioningGroupWrapper : this.provisioningGroupWrappers) {
      ProvisioningGroup grouperProvisioningGroup = provisioningGroupWrapper.getGrouperProvisioningGroup();
      if (grouperProvisioningGroup != null) {
        grouperProvisioningGroups.add(grouperProvisioningGroup);
      }
    }
    return grouperProvisioningGroups;
  }

  /**
   * extract list of non null grouper provisioning membership
   * @param forCreate - null means all, true means only if we're in the create and we're creating memberships while we create groups and entities, 
   * false means we're just doing updates and deletes
   * @return memberships
   */
  public List<ProvisioningMembership> retrieveGrouperProvisioningMemberships(Boolean forCreate) {
    List<ProvisioningMembership> grouperProvisioningMemberships = new ArrayList<ProvisioningMembership>();
    
    GrouperProvisioningBehaviorMembershipType membershipType = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType();
    
    boolean createGroupsAndEntitiesBeforeTranslatingMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isCreateGroupsAndEntitiesBeforeTranslatingMemberships();
    
    if (forCreate != null && forCreate && createGroupsAndEntitiesBeforeTranslatingMemberships) {
      return grouperProvisioningMemberships;
    }
    
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.provisioningMembershipWrappers) {
      ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
      if (grouperProvisioningMembership != null) {
        
        if (forCreate == null || membershipType == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
          grouperProvisioningMemberships.add(grouperProvisioningMembership);
        } else if (!forCreate && createGroupsAndEntitiesBeforeTranslatingMemberships) {
          grouperProvisioningMemberships.add(grouperProvisioningMembership);
        } else {
          
          if (membershipType == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
            if (provisioningMembershipWrapper.getGrouperProvisioningMembership().getProvisioningEntity() != null) {
              ProvisioningEntityWrapper entityWrapper = provisioningMembershipWrapper.getGrouperProvisioningMembership().getProvisioningEntity().getProvisioningEntityWrapper();
              if (entityWrapper.getProvisioningStateEntity().isCreate() == forCreate) {
                grouperProvisioningMemberships.add(grouperProvisioningMembership);
              }
            } 
          }
          
          if (membershipType == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
            if (provisioningMembershipWrapper.getGrouperProvisioningMembership().getProvisioningGroup() != null) {
              ProvisioningGroupWrapper groupWrapper = provisioningMembershipWrapper.getGrouperProvisioningMembership().getProvisioningGroup().getProvisioningGroupWrapper();
              if (groupWrapper.getProvisioningStateGroup().isCreate() == forCreate) {
                grouperProvisioningMemberships.add(grouperProvisioningMembership);
              }
            } 
            
          }
        }
      }
      
    }
    return grouperProvisioningMemberships;
  }

  /**
   * extract list of non null sync members
   * @return groups
   */
  public List<GcGrouperSyncMember> retrieveGcGrouperSyncMembers() {
    List<GcGrouperSyncMember> result = new ArrayList<GcGrouperSyncMember>();
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.provisioningEntityWrappers) {
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
      if (gcGrouperSyncMember != null) {
        result.add(gcGrouperSyncMember);
      }
    }
    return result;
  }

  /**
   * extract list of non null sync groups
   * @return groups
   */
  public List<GcGrouperSyncGroup> retrieveGcGrouperSyncGroups() {
    List<GcGrouperSyncGroup> result = new ArrayList<GcGrouperSyncGroup>();
    for (ProvisioningGroupWrapper provisioningGroupWrapper : this.provisioningGroupWrappers) {
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
      if (gcGrouperSyncGroup != null) {
        result.add(gcGrouperSyncGroup);
      }
    }
    return result;
  }

  /**
   * extract list of non null sync Memberships
   * @return groups
   */
  public List<GcGrouperSyncMembership> retrieveGcGrouperSyncMemberships() {
    List<GcGrouperSyncMembership> result = new ArrayList<GcGrouperSyncMembership>();
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.provisioningMembershipWrappers) {
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
      if (gcGrouperSyncMembership != null) {
        result.add(gcGrouperSyncMembership);
      }
    }
    return result;
  }


  /**
   * extract list of non null grouper provisioning entities
   * @return groups
   */
  public List<ProvisioningEntity> retrieveGrouperProvisioningEntities() {
    List<ProvisioningEntity> grouperProvisioningEntities = new ArrayList<ProvisioningEntity>();
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.provisioningEntityWrappers) {
      ProvisioningEntity grouperProvisioningEntity = provisioningEntityWrapper.getGrouperProvisioningEntity();
      if (grouperProvisioningEntity != null) {
        grouperProvisioningEntities.add(grouperProvisioningEntity);
      }
    }
    return grouperProvisioningEntities;
  }

  /**
   * extract list of non null target provisioning entities
   * @return ProvisioningGroups or ProvisioningStateEntities
   */
  public List<Object> retrieveIncrementalEntities() {
    List<Object> result = new ArrayList<Object>();
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.provisioningEntityWrappers) {
      
      if (provisioningEntityWrapper.getGrouperTargetEntity() != null) {
        result.add(provisioningEntityWrapper.getGrouperTargetEntity());
      } else if (provisioningEntityWrapper.getGrouperProvisioningEntity() != null) {
        result.add(provisioningEntityWrapper.getGrouperProvisioningEntity());
      } else if (provisioningEntityWrapper.getGcGrouperSyncMember() != null) {
        result.add(provisioningEntityWrapper.getGcGrouperSyncMember());
      } else if (provisioningEntityWrapper.getProvisioningStateEntity() != null) {
        result.add(provisioningEntityWrapper.getProvisioningStateEntity());
      }
    }
    return result;
  }

  
  /**
   * extract list of non null target provisioning groups
   * @return ProvisioningGroups or ProvisioningStateGroups
   */
  public List<Object> retrieveIncrementalGroups() {
    List<Object> result = new ArrayList<Object>();
    for (ProvisioningGroupWrapper provisioningGroupWrapper : this.provisioningGroupWrappers) {
      
      if (provisioningGroupWrapper.getGrouperTargetGroup() != null) {
        result.add(provisioningGroupWrapper.getGrouperTargetGroup());
      } else if (provisioningGroupWrapper.getGrouperProvisioningGroup() != null) {
        result.add(provisioningGroupWrapper.getGrouperProvisioningGroup());
      } else if (provisioningGroupWrapper.getGcGrouperSyncGroup() != null) {
        result.add(provisioningGroupWrapper.getGcGrouperSyncGroup());
      } else if (provisioningGroupWrapper.getProvisioningStateGroup() != null) {
        result.add(provisioningGroupWrapper.getProvisioningStateGroup());
      }
    }
    return result;
  }

  /**
   * extract list of non null target provisioning memberships
   * @return memberships
   */
  public List<Object> retrieveIncrementalMemberships() {
    List<Object> result = new ArrayList<Object>();
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.provisioningMembershipWrappers) {
      
      if (provisioningMembershipWrapper.getGrouperTargetMembership() != null) {
        result.add(provisioningMembershipWrapper.getGrouperTargetMembership());
      } else if (provisioningMembershipWrapper.getGrouperProvisioningMembership() != null) {
        result.add(provisioningMembershipWrapper.getGrouperProvisioningMembership());
      } else if (provisioningMembershipWrapper.getGcGrouperSyncMembership() != null) {
        result.add(provisioningMembershipWrapper.getGcGrouperSyncMembership());
      } else if (provisioningMembershipWrapper.getProvisioningStateMembership() != null) {
        result.add(provisioningMembershipWrapper.getProvisioningStateMembership());
      }
    }
    return result;
  }

  public void addIncrementalGroup(String groupId, boolean recalcGroup, boolean recalcMemberships,
      Long millisSince1970, GrouperIncrementalDataAction grouperIncrementalDataAction) {

    ProvisioningGroupWrapper provisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(groupId);
    if (provisioningGroupWrapper == null) {
      provisioningGroupWrapper = new ProvisioningGroupWrapper();
      provisioningGroupWrapper.setGrouperProvisioner(this.getGrouperProvisioner());
      provisioningGroupWrapper.setGroupId(groupId);
      this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers().add(provisioningGroupWrapper);
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().put(groupId, provisioningGroupWrapper);
    }
    
    if (millisSince1970 != null 
        && (provisioningGroupWrapper.getProvisioningStateGroup().getMillisSince1970() == null 
          || millisSince1970 > provisioningGroupWrapper.getProvisioningStateGroup().getMillisSince1970())) {
      provisioningGroupWrapper.getProvisioningStateGroup().setMillisSince1970(millisSince1970);
    }

    if (recalcGroup) {
      provisioningGroupWrapper.getProvisioningStateGroup().setRecalcObject(recalcGroup);
    }

    if (recalcMemberships) {
      provisioningGroupWrapper.getProvisioningStateGroup().setRecalcGroupMemberships(recalcMemberships);
    }

    if (grouperIncrementalDataAction != null) {
      provisioningGroupWrapper.getProvisioningStateGroup().setGrouperIncrementalDataAction(grouperIncrementalDataAction);
      switch (grouperIncrementalDataAction) {
        case insert:
          
          provisioningGroupWrapper.getProvisioningStateGroup().setCreate(true);
          break;
          
        case delete:
          
          provisioningGroupWrapper.getProvisioningStateGroup().setDelete(true);
          break;

        case update:
          
          provisioningGroupWrapper.getProvisioningStateGroup().setUpdate(true);
          break;
        default:
          break;
      }
    }
  }

  public void addIncrementalEntity(String memberId, boolean recalcEntity, boolean recalcMemberships,
      Long millisSince1970, GrouperIncrementalDataAction grouperIncrementalDataAction) {

    ProvisioningEntityWrapper provisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberId);
    if (provisioningEntityWrapper == null) {
      provisioningEntityWrapper = new ProvisioningEntityWrapper();
      provisioningEntityWrapper.setGrouperProvisioner(grouperProvisioner);
      provisioningEntityWrapper.setMemberId(memberId);
      this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers().add(provisioningEntityWrapper);
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().put(memberId, provisioningEntityWrapper);
    }
    
    if (millisSince1970 != null 
        && (provisioningEntityWrapper.getProvisioningStateEntity().getMillisSince1970() == null 
          || millisSince1970 > provisioningEntityWrapper.getProvisioningStateEntity().getMillisSince1970())) {
      provisioningEntityWrapper.getProvisioningStateEntity().setMillisSince1970(millisSince1970);
    }

    if (recalcEntity) {
      provisioningEntityWrapper.getProvisioningStateEntity().setRecalcObject(recalcEntity);
    }

    if (recalcMemberships) {
      provisioningEntityWrapper.getProvisioningStateEntity().setRecalcEntityMemberships(recalcMemberships);
    }
    
    if (grouperIncrementalDataAction != null) {
      provisioningEntityWrapper.getProvisioningStateEntity().setGrouperIncrementalDataAction(grouperIncrementalDataAction);
    }
  }

  
  public void addIncrementalMembership(String groupId, String memberId, boolean recalcMembership, Long millisSince1970, GrouperIncrementalDataAction grouperIncrementalDataAction) {

    MultiKey multiKey = new MultiKey(groupId, memberId);
    
    ProvisioningMembershipWrapper provisioningMembershipWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(multiKey);
    if (provisioningMembershipWrapper == null) {
      provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
      provisioningMembershipWrapper.setGrouperProvisioner(grouperProvisioner);
      provisioningMembershipWrapper.setGroupIdMemberId(multiKey);
      this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers().add(provisioningMembershipWrapper);
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().put(multiKey, provisioningMembershipWrapper);
    }
    
    if (millisSince1970 != null 
        && (provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970() == null 
          || millisSince1970 > provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970())) {
      provisioningMembershipWrapper.getProvisioningStateMembership().setMillisSince1970(millisSince1970);
    }

    if (recalcMembership) {
      provisioningMembershipWrapper.getProvisioningStateMembership().setRecalcObject(recalcMembership);
    }
    
    if (grouperIncrementalDataAction != null) {
      provisioningMembershipWrapper.getProvisioningStateMembership().setGrouperIncrementalDataAction(grouperIncrementalDataAction);
    }

  }

  /**
   * 
   * @param provisioningMembershipWrapper
   */
  public void removeAndUnindexMembershipWrapper(ProvisioningMembershipWrapper provisioningMembershipWrapper) {
    this.provisioningMembershipWrappers.remove(provisioningMembershipWrapper);
    GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
    
    if (provisioningMembershipWrapper.getGroupIdMemberId() != null) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().remove(provisioningMembershipWrapper.getGroupIdMemberId());
    }
    
    if (gcGrouperSyncMembership != null) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper().remove(provisioningMembershipWrapper.getSyncGroupIdSyncMemberId());
    }    
  }


}
