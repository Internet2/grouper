package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperProvisioningMatchingIdIndex {

  private GrouperProvisioner grouperProvisioner = null;

  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }


  public void indexMatchingIdGroups() {
  
    int provisioningGroupWrappersWithNullIds = 0;
    
    Set<Object> matchingIds = new HashSet<Object>();
    
    Map<Object, ProvisioningGroupWrapper> groupMatchingIdToProvisioningGroupWrapper = 
        this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupMatchingIdToProvisioningGroupWrapper();
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper : new ArrayList<ProvisioningGroupWrapper>(
        GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()))) {

      Object matchingId = provisioningGroupWrapper.getMatchingId();
      if (matchingId == null) {
        // this could be an insert?
        provisioningGroupWrappersWithNullIds++;
        continue;
      }
      
      if (matchingIds.contains(matchingId)) {
        
        //lets try to merge
        ProvisioningGroupWrapper provisioningGroupWrapperExisting = groupMatchingIdToProvisioningGroupWrapper.get(matchingId);

        ProvisioningGroupWrapper grouperWrapper = null;
        if (provisioningGroupWrapperExisting.getGrouperProvisioningGroup() != null && provisioningGroupWrapper.getGrouperProvisioningGroup() == null) {
          grouperWrapper = provisioningGroupWrapperExisting;
        }
        if (provisioningGroupWrapper.getGrouperProvisioningGroup() != null && provisioningGroupWrapperExisting.getGrouperProvisioningGroup() == null) {
          grouperWrapper = provisioningGroupWrapper;
        }
        
        ProvisioningGroupWrapper targetWrapper = null;
        if (provisioningGroupWrapperExisting.getTargetProvisioningGroup() != null && provisioningGroupWrapper.getTargetProvisioningGroup() == null) {
          targetWrapper = provisioningGroupWrapperExisting;
        }
        if (provisioningGroupWrapper.getTargetProvisioningGroup() != null && provisioningGroupWrapperExisting.getTargetProvisioningGroup() == null) {
          targetWrapper = provisioningGroupWrapper;
        }
        
        if (grouperWrapper == null || targetWrapper == null || grouperWrapper == targetWrapper) {

          throw new NullPointerException("Why do multiple groups have the same matching id???\n" 
              + provisioningGroupWrapper.getGrouperTargetGroup() + "\n" 
              + provisioningGroupWrapper.getTargetProvisioningGroup() + "\n"
              + provisioningGroupWrapperExisting.getGrouperTargetGroup() + "\n"
              + provisioningGroupWrapperExisting.getTargetProvisioningGroup());

        }

        // switch to grouper wrapper
        groupMatchingIdToProvisioningGroupWrapper.put(matchingId, grouperWrapper);
        
        grouperWrapper.setTargetProvisioningGroup(targetWrapper.getTargetProvisioningGroup());
        grouperWrapper.setTargetNativeGroup(targetWrapper.getTargetNativeGroup());
        
        continue;
      }
      matchingIds.add(matchingId);
  
      groupMatchingIdToProvisioningGroupWrapper.put(matchingId, provisioningGroupWrapper);
    }
    
    if (provisioningGroupWrappersWithNullIds > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("provisioningGroupWrappersWithNullIds"), 0);
      this.getGrouperProvisioner().getDebugMap().put("provisioningGroupWrappersWithNullIds", oldCount + provisioningGroupWrappersWithNullIds);
    }
  
  }

  public void indexMatchingIdMemberships() {
    
    int provisioningMembershipWrappersWithNullIds = 0;
    
    Set<Object> matchingIds = new HashSet<Object>();
    
    Map<Object, ProvisioningMembershipWrapper> membershipMatchingIdToProvisioningMembershipWrapper = 
        this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMembershipMatchingIdToProvisioningMembershipWrapper();
    
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : 
      new ArrayList<ProvisioningMembershipWrapper>(GrouperUtil.nonNull(
          this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()))) {

      Object matchingId = provisioningMembershipWrapper.getMatchingId();
      if (matchingId == null) {
        // this could be an insert?
        provisioningMembershipWrappersWithNullIds++;
        continue;
      }
      
      if (matchingIds.contains(matchingId)) {
        
        //lets try to merge
        ProvisioningMembershipWrapper provisioningMembershipWrapperExisting = membershipMatchingIdToProvisioningMembershipWrapper.get(matchingId);

        ProvisioningMembershipWrapper grouperWrapper = null;
        if (provisioningMembershipWrapperExisting.getGrouperProvisioningMembership() != null && provisioningMembershipWrapper.getGrouperProvisioningMembership() == null) {
          grouperWrapper = provisioningMembershipWrapperExisting;
        }
        if (provisioningMembershipWrapper.getGrouperProvisioningMembership() != null && provisioningMembershipWrapperExisting.getGrouperProvisioningMembership() == null) {
          grouperWrapper = provisioningMembershipWrapper;
        }
        
        ProvisioningMembershipWrapper targetWrapper = null;
        if (provisioningMembershipWrapperExisting.getTargetProvisioningMembership() != null && provisioningMembershipWrapper.getTargetProvisioningMembership() == null) {
          targetWrapper = provisioningMembershipWrapperExisting;
        }
        if (provisioningMembershipWrapper.getTargetProvisioningMembership() != null && provisioningMembershipWrapperExisting.getTargetProvisioningMembership() == null) {
          targetWrapper = provisioningMembershipWrapper;
        }
        
        if (grouperWrapper == null || targetWrapper == null || grouperWrapper == targetWrapper) {

          throw new NullPointerException("Why do multiple memberships have the same matching id???\n" 
              + provisioningMembershipWrapper.getGrouperTargetMembership() + "\n" 
              + provisioningMembershipWrapper.getTargetProvisioningMembership() + "\n"
              + membershipMatchingIdToProvisioningMembershipWrapper.get(matchingId).getGrouperTargetMembership() + "\n"
              + membershipMatchingIdToProvisioningMembershipWrapper.get(matchingId).getTargetProvisioningMembership());

        }

        // switch to grouper wrapper
        membershipMatchingIdToProvisioningMembershipWrapper.put(matchingId, grouperWrapper);
        
        grouperWrapper.setTargetProvisioningMembership(targetWrapper.getTargetProvisioningMembership());
        grouperWrapper.setTargetNativeMembership(targetWrapper.getTargetNativeMembership());
        
        continue;

      }
      matchingIds.add(matchingId);
  
      membershipMatchingIdToProvisioningMembershipWrapper.put(matchingId, provisioningMembershipWrapper);
    }
    
    if (provisioningMembershipWrappersWithNullIds > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("provisioningMembershipWrappersWithNullIds"), 0);
      this.getGrouperProvisioner().getDebugMap().put("provisioningMembershipWrappersWithNullIds", oldCount + provisioningMembershipWrappersWithNullIds);
    }
  
  }

  public void indexMatchingIdEntities() {
    
    int provisioningEntityWrappersWithNullIds = 0;
    
    Set<Object> matchingIds = new HashSet<Object>();
    
    Map<Object, ProvisioningEntityWrapper> entityMatchingIdToProvisioningEntityWrapper = 
        this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getEntityMatchingIdToProvisioningEntityWrapper();
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper : new ArrayList<ProvisioningEntityWrapper>(
        GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()))) {

      Object matchingId = provisioningEntityWrapper.getMatchingId();
      if (matchingId == null) {
        // this could be an insert?
        provisioningEntityWrappersWithNullIds++;
        continue;
      }
      
      if (matchingIds.contains(matchingId)) {

        //lets try to merge
        ProvisioningEntityWrapper provisioningEntityWrapperExisting = entityMatchingIdToProvisioningEntityWrapper.get(matchingId);

        ProvisioningEntityWrapper grouperWrapper = null;
        if (provisioningEntityWrapperExisting.getGrouperProvisioningEntity() != null && provisioningEntityWrapper.getGrouperProvisioningEntity() == null) {
          grouperWrapper = provisioningEntityWrapperExisting;
        }
        if (provisioningEntityWrapper.getGrouperProvisioningEntity() != null && provisioningEntityWrapperExisting.getGrouperProvisioningEntity() == null) {
          grouperWrapper = provisioningEntityWrapper;
        }
        
        ProvisioningEntityWrapper targetWrapper = null;
        if (provisioningEntityWrapperExisting.getTargetProvisioningEntity() != null && provisioningEntityWrapper.getTargetProvisioningEntity() == null) {
          targetWrapper = provisioningEntityWrapperExisting;
        }
        if (provisioningEntityWrapper.getTargetProvisioningEntity() != null && provisioningEntityWrapperExisting.getTargetProvisioningEntity() == null) {
          targetWrapper = provisioningEntityWrapper;
        }
        
        if (grouperWrapper == null || targetWrapper == null || grouperWrapper == targetWrapper) {
          
          throw new NullPointerException("Why do multiple entities have the same matching id???\n" 
              + provisioningEntityWrapper.getGrouperTargetEntity() + "\n" 
              + provisioningEntityWrapper.getTargetProvisioningEntity() + "\n"
              + entityMatchingIdToProvisioningEntityWrapper.get(matchingId).getGrouperTargetEntity() + "\n"
              + entityMatchingIdToProvisioningEntityWrapper.get(matchingId).getTargetProvisioningEntity());

        }

        // switch to grouper wrapper
        entityMatchingIdToProvisioningEntityWrapper.put(matchingId, grouperWrapper);
        
        grouperWrapper.setTargetProvisioningEntity(targetWrapper.getTargetProvisioningEntity());
        grouperWrapper.setTargetNativeEntity(targetWrapper.getTargetNativeEntity());
        
        continue;

      }
      matchingIds.add(matchingId);
  
      entityMatchingIdToProvisioningEntityWrapper.put(matchingId, provisioningEntityWrapper);
    }
    
    if (provisioningEntityWrappersWithNullIds > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("provisioningEntityWrappersWithNullIds"), 0);
      this.getGrouperProvisioner().getDebugMap().put("provisioningEntityWrappersWithNullIds", oldCount + provisioningEntityWrappersWithNullIds);
    }
  
  }


  
}
