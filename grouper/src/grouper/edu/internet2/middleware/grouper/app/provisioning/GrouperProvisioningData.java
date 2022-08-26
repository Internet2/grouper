package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

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

  public GrouperProvisioningData() {
  }
  
  /**
   * 
   * @param provisioningGroupWrapper
   */
  public void addAndIndexGroupWrapper(ProvisioningGroupWrapper provisioningGroupWrapper) {
    this.provisioningGroupWrappers.add(provisioningGroupWrapper);
    ProvisioningGroup grouperProvisioningGroup = provisioningGroupWrapper.getGrouperProvisioningGroup();
    GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
    
    if (grouperProvisioningGroup != null) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().put(grouperProvisioningGroup.getId(), provisioningGroupWrapper);
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
    ProvisioningEntity grouperProvisioningEntity = provisioningEntityWrapper.getGrouperProvisioningEntity();
    GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
    
    if (grouperProvisioningEntity != null) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().put(grouperProvisioningEntity.getId(), provisioningEntityWrapper);
    }
    
    if (gcGrouperSyncMember != null) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncMemberIdToProvisioningEntityWrapper().put(gcGrouperSyncMember.getId(), provisioningEntityWrapper);
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
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex()
        .getGroupUuidMemberUuidToProvisioningMembershipWrapper().put(
            groupIdEntityId, provisioningMembershipWrapper);
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
              if (entityWrapper.isCreate() == forCreate) {
                grouperTargetMemberships.add(grouperTargetMembership);
              }
            } 
          }
          
          if (membershipType == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
            if (provisioningMembershipWrapper.getGrouperProvisioningMembership().getProvisioningGroup() != null) {
              ProvisioningGroupWrapper groupWrapper = provisioningMembershipWrapper.getGrouperProvisioningMembership().getProvisioningGroup().getProvisioningGroupWrapper();
              if (groupWrapper.isCreate() == forCreate) {
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
              if (entityWrapper.isCreate() == forCreate) {
                grouperProvisioningMemberships.add(grouperProvisioningMembership);
              }
            } 
          }
          
          if (membershipType == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
            if (provisioningMembershipWrapper.getGrouperProvisioningMembership().getProvisioningGroup() != null) {
              ProvisioningGroupWrapper groupWrapper = provisioningMembershipWrapper.getGrouperProvisioningMembership().getProvisioningGroup().getProvisioningGroupWrapper();
              if (groupWrapper.isCreate() == forCreate) {
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

  

}
