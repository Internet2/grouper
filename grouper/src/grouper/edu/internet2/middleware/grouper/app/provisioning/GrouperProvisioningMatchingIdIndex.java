package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GrouperProvisioningMatchingIdIndex {

  private GrouperProvisioner grouperProvisioner = null;

  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }


  /**
   * look through group wrappers and add matching IDs to the index and make sure everything is linked up
   */
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

          throw new RuntimeException("Why do multiple groups have the same matching id???\n" 
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

  /**
   * look through group wrappers focus on grouper and target data which is not yet matched
   */
  public void indexMatchingIdGroupsUnmatched() {
  
    Set<ProvisioningGroup> grouperTargetGroupsUnmatched = new HashSet<ProvisioningGroup>();
    Set<ProvisioningGroup> targetProvisioningGroupsUnmatched = new HashSet<ProvisioningGroup>();
    
    Map<Object, ProvisioningGroupWrapper> groupMatchingIdToProvisioningGroupWrapper = 
        this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupMatchingIdToProvisioningGroupWrapper();

    for (ProvisioningGroupWrapper provisioningGroupWrapper : new ArrayList<ProvisioningGroupWrapper>(
        GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()))) {

      // this is a match
      if (provisioningGroupWrapper.getGrouperTargetGroup() != null && provisioningGroupWrapper.getTargetProvisioningGroup() != null) {
        continue;
      }

      // this is weird, but skip it
      if (provisioningGroupWrapper.getGrouperTargetGroup() == null && provisioningGroupWrapper.getTargetProvisioningGroup() == null) {
        continue;
      }
      
      // this grouperTargetGroup with no match
      if (provisioningGroupWrapper.getGrouperTargetGroup() != null && provisioningGroupWrapper.getTargetProvisioningGroup() == null) {
        grouperTargetGroupsUnmatched.add(provisioningGroupWrapper.getGrouperTargetGroup());
        continue;
      }
      
      // this targetProvisioningGroup with no match
      if (provisioningGroupWrapper.getGrouperTargetGroup() == null && provisioningGroupWrapper.getTargetProvisioningGroup() != null) {
        targetProvisioningGroupsUnmatched.add(provisioningGroupWrapper.getTargetProvisioningGroup());
        continue;
      }
    }

    if (grouperTargetGroupsUnmatched.size() > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("grouperTargetGroupsUnmatched"), 0);
      this.getGrouperProvisioner().getDebugMap().put("grouperTargetGroupsUnmatched", oldCount + grouperTargetGroupsUnmatched.size());
    }
    if (targetProvisioningGroupsUnmatched.size() > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("targetProvisioningGroupsUnmatched"), 0);
      this.getGrouperProvisioner().getDebugMap().put("targetProvisioningGroupsUnmatched", oldCount + grouperTargetGroupsUnmatched.size());
    }
    if (grouperTargetGroupsUnmatched.size() == 0 || targetProvisioningGroupsUnmatched.size() == 0) {
      // if none on either side then we cannot find a deeper match
      return;
    }
    // loop through matching ids
    int provisioningGroupWrappersMatchedFromCache = 0;
    int provisioningGroupWrappersMatchedFromAlternateMatchAttr = 0;
    for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMatchingAttributes()) {
      String matchingAttributeName = matchingAttribute.getName();
      Map<Object, ProvisioningGroup> matchingValueToTargetProvisioningGroup = new HashMap<Object, ProvisioningGroup>();
      for (ProvisioningGroup targetProvisioningGroup : targetProvisioningGroupsUnmatched) {
        // dont worry if dupes... oh well
        Object targetProvisioningGroupCurrentValue = targetProvisioningGroup.retrieveAttributeValue(matchingAttributeName);
        if(!GrouperUtil.isBlank(targetProvisioningGroupCurrentValue)) {
          matchingValueToTargetProvisioningGroup.put(targetProvisioningGroupCurrentValue, targetProvisioningGroup);
        }
      }
      for (ProvisioningGroup grouperTargetGroup : new HashSet<ProvisioningGroup>(grouperTargetGroupsUnmatched)) {
        ProvisioningGroup targetProvisioningGroup = null;
        
        Object grouperTargetGroupCurrentValue = grouperTargetGroup.retrieveAttributeValue(matchingAttributeName);
        if (targetProvisioningGroup == null 
            && !GrouperUtil.isEmpty(grouperTargetGroupCurrentValue)) {
          targetProvisioningGroup = matchingValueToTargetProvisioningGroup.get(grouperTargetGroupCurrentValue);
          if (targetProvisioningGroup != null) {
            provisioningGroupWrappersMatchedFromAlternateMatchAttr++;
          }
        }

        if (targetProvisioningGroup == null) {
          Set<Object> cachedValues = GrouperProvisioningConfigurationAttributeDbCache.cachedValuesForGroup(grouperTargetGroup, matchingAttributeName);
          for (Object cachedValue : GrouperUtil.nonNull(cachedValues)) {
            if (targetProvisioningGroup == null 
                && !GrouperUtil.isEmpty(cachedValue)) {
              targetProvisioningGroup = matchingValueToTargetProvisioningGroup.get(cachedValue);
              if (targetProvisioningGroup != null) {
                provisioningGroupWrappersMatchedFromCache++;
              }
            }
          }
        }
        
        if (targetProvisioningGroup != null) {
          // we have a match!!!!

          //  i guess use the grouper matching id... hmmm... since they dont match
          groupMatchingIdToProvisioningGroupWrapper.put(grouperTargetGroup.getMatchingId(), grouperTargetGroup.getProvisioningGroupWrapper());
          
          // link to group wrapper
          grouperTargetGroup.getProvisioningGroupWrapper().setTargetProvisioningGroup(targetProvisioningGroup);
          grouperTargetGroup.getProvisioningGroupWrapper().setTargetNativeGroup(targetProvisioningGroup.getProvisioningGroupWrapper().getTargetNativeGroup());
          
          // unlink from its wrapper
          if (targetProvisioningGroup.getProvisioningGroupWrapper() != grouperTargetGroup.getProvisioningGroupWrapper()) {
            this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers().remove(targetProvisioningGroup.getProvisioningGroupWrapper());
            targetProvisioningGroup.getProvisioningGroupWrapper().setTargetProvisioningGroup(null);
            targetProvisioningGroup.getProvisioningGroupWrapper().setTargetNativeGroup(null);
            targetProvisioningGroup.getProvisioningGroupWrapper().setGcGrouperSyncGroup(null);
          }
          
          targetProvisioningGroup.setProvisioningGroupWrapper(grouperTargetGroup.getProvisioningGroupWrapper());
          
          grouperTargetGroupsUnmatched.remove(grouperTargetGroup);
          targetProvisioningGroupsUnmatched.remove(targetProvisioningGroup);
        }
      }
    }
    
    if (provisioningGroupWrappersMatchedFromAlternateMatchAttr > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("provisioningGroupWrappersMatchedFromAlternateMatchAttr"), 0);
      this.getGrouperProvisioner().getDebugMap().put("provisioningGroupWrappersMatchedFromAlternateMatchAttr", oldCount + provisioningGroupWrappersMatchedFromAlternateMatchAttr);
    }
    if (provisioningGroupWrappersMatchedFromCache > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("provisioningGroupWrappersMatchedFromCache"), 0);
      this.getGrouperProvisioner().getDebugMap().put("provisioningGroupWrappersMatchedFromCache", oldCount + provisioningGroupWrappersMatchedFromCache);
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
      if (matchingId == null || (((MultiKey)matchingId).getKey(0) == null && ((MultiKey)matchingId).getKey(1) == null)) {
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

          throw new RuntimeException("Why do multiple memberships have the same matching id???\n" 
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
      
      if (provisioningMembershipWrapper.getGrouperIncrementalDataAction() == null) {
        // if there's a group recalc and there are membership inserts; the action either needs to be provided or it needs to be recalc
        provisioningMembershipWrapper.setRecalc(true);
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
          
          throw new RuntimeException("Why do multiple entities have the same matching id???\n" 
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
