package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashSet;
import java.util.List;
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


  public void indexMatchingIdOfGrouperEntities(List<ProvisioningEntity> grouperTargetEntities) {
    Map<Object, ProvisioningEntityWrapper> targetEntityIdToProvisioningEntityWrapper = 
        this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getEntityMatchingIdToProvisioningEntityWrapper();
  
    int grouperTargetEntitiesWithNullMatchingIds = 0;
    
    Set<Object> macthingIds = new HashSet<Object>();

    for (ProvisioningEntity grouperTargetEntity : GrouperUtil.nonNull(grouperTargetEntities)) {
      
      Object macthingId = grouperTargetEntity.getMatchingId();
      if (macthingId == null) {
        // this could be an insert?
        grouperTargetEntitiesWithNullMatchingIds++;
        // TODO make sure to handle this in the compare
        continue;
      }
      
      if (macthingIds.contains(macthingId)) {
        throw new NullPointerException("Why do multiple entities from grouper have the same matching id???\n" 
            + grouperTargetEntity + "\n" + targetEntityIdToProvisioningEntityWrapper.get(macthingId));
      }
      macthingIds.add(macthingId);

      ProvisioningEntityWrapper provisioningEntityWrapper = grouperTargetEntity.getProvisioningEntityWrapper();
      if (provisioningEntityWrapper == null) {
        throw new NullPointerException("Cant find entity wrapper: " + grouperTargetEntity);
      }
      targetEntityIdToProvisioningEntityWrapper.put(macthingId, provisioningEntityWrapper);
    }
    
    if (grouperTargetEntitiesWithNullMatchingIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("grouperTargetEntitiesWithNullMatchingIds", grouperTargetEntitiesWithNullMatchingIds);
    }
  
  
  }


  public void indexMatchingIdOfGrouperGroups(List<ProvisioningGroup> grouperTargetGroups) {
    Map<Object, ProvisioningGroupWrapper> targetGroupIdToProvisioningGroupWrapper = 
        this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupMatchingIdToProvisioningGroupWrapper();
  
    int grouperTargetGroupsWithNullMatchingIds = 0;
    
    Set<Object> macthingIds = new HashSet<Object>();
    
    for (ProvisioningGroup grouperTargetGroup : GrouperUtil.nonNull(grouperTargetGroups)) {
      
      Object macthingId = grouperTargetGroup.getMatchingId();
      if (macthingId == null) {
        // this could be an insert?
        grouperTargetGroupsWithNullMatchingIds++;
        // TODO make sure to handle this in the compare
        continue;
      }
      
      if (macthingIds.contains(macthingId)) {
        throw new NullPointerException("Why do multiple groups from grouper have the same matching id???\n" 
            + grouperTargetGroup + "\n" + targetGroupIdToProvisioningGroupWrapper.get(macthingId));
      }
      macthingIds.add(macthingId);
  
      ProvisioningGroupWrapper provisioningGroupWrapper = grouperTargetGroup.getProvisioningGroupWrapper();
      if (provisioningGroupWrapper == null) {
        throw new NullPointerException("Cant find group wrapper: " + grouperTargetGroup);
      }
      targetGroupIdToProvisioningGroupWrapper.put(macthingId, provisioningGroupWrapper);
    }
    
    if (grouperTargetGroupsWithNullMatchingIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("grouperTargetGroupsWithNullMatchingIds", grouperTargetGroupsWithNullMatchingIds);
    }
  
  }


  public void indexMatchingIdOfGrouperMemberships(List<ProvisioningMembership> grouperTargetMemberships) {
    Map<Object, ProvisioningMembershipWrapper> targetMembershipIdToProvisioningMembershipWrapper = 
        this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMembershipMatchingIdToProvisioningMembershipWrapper();
  
    int grouperTargetMembershipsWithNullMatchingIds = 0;
    
    Set<Object> macthingIds = new HashSet<Object>();

    for (ProvisioningMembership grouperTargetMembership : GrouperUtil.nonNull(grouperTargetMemberships)) {
      
      Object macthingId = grouperTargetMembership.getMatchingId();
      if (macthingId == null) {
        // this could be an insert?
        grouperTargetMembershipsWithNullMatchingIds++;
        // TODO make sure to handle this in the compare
        continue;
      }
      
      if (macthingIds.contains(macthingId)) {
        throw new NullPointerException("Why do multiple memberships from grouper have the same matching id???\n" 
            + grouperTargetMembership + "\n" + targetMembershipIdToProvisioningMembershipWrapper.get(macthingId));
      }
      macthingIds.add(macthingId);

  
      ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperTargetMembership.getProvisioningMembershipWrapper();
      if (provisioningMembershipWrapper == null) {
        throw new NullPointerException("Cant find membership wrapper: " + grouperTargetMembership);
      }
      targetMembershipIdToProvisioningMembershipWrapper.put(macthingId, provisioningMembershipWrapper);
    }
    
    if (grouperTargetMembershipsWithNullMatchingIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("grouperTargetMembershipsWithNullMatchingIds", grouperTargetMembershipsWithNullMatchingIds);
    }
  
  
  }


  public void indexMatchingIdOfGrouperObjects() {
    this.indexMatchingIdOfGrouperGroups(
        this.grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().getProvisioningGroups());
    
    this.indexMatchingIdOfGrouperEntities(
        this.grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().getProvisioningEntities());
  
    this.indexMatchingIdOfGrouperMemberships(
        this.grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().getProvisioningMemberships());

    // these might be empty for full provisioning and thats ok
    this.indexMatchingIdOfGrouperGroups(
        this.grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjectsIncludeDeletes().getProvisioningGroups());
    
    this.indexMatchingIdOfGrouperEntities(
        this.grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjectsIncludeDeletes().getProvisioningEntities());
  
    this.indexMatchingIdOfGrouperMemberships(
        this.grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjectsIncludeDeletes().getProvisioningMemberships());
  }


  public void indexMatchingIdOfTargetEntities() {
  
    Map<Object, ProvisioningEntityWrapper> targetEntityIdToProvisioningEntityWrapper = 
        this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getEntityMatchingIdToProvisioningEntityWrapper();
  
    // make sure we arent double dipping target provisioning matching ids
    Set<Object> targetProvisioningMatchingIds = new HashSet<Object>();
    for (ProvisioningEntity targetProvisioningEntity : 
      GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataTarget()
          .getTargetProvisioningObjects().getProvisioningEntities())) {
      
      Object macthingId = targetProvisioningEntity.getMatchingId();
      if (macthingId == null) {
        throw new NullPointerException("Cant find id for targetProvisioningEntity! " + targetProvisioningEntity);
      }
      
      if (targetProvisioningMatchingIds.contains(macthingId)) {
        throw new NullPointerException("Why do multiple entities from target have the same matching id???\n" 
            + targetProvisioningEntity + "\n" + targetEntityIdToProvisioningEntityWrapper.get(macthingId));
      }
      targetProvisioningMatchingIds.add(macthingId);
  
      ProvisioningEntityWrapper provisioningEntityWrapperReal = targetEntityIdToProvisioningEntityWrapper.get(macthingId);
      if (provisioningEntityWrapperReal == null) {
        provisioningEntityWrapperReal = new ProvisioningEntityWrapper();
        targetEntityIdToProvisioningEntityWrapper.put(macthingId, provisioningEntityWrapperReal);
      }
  
      ProvisioningEntityWrapper provisioningEntityWrapperTarget = targetProvisioningEntity.getProvisioningEntityWrapper();
  
      // lets merge these to get our complete wrapper
      targetProvisioningEntity.setProvisioningEntityWrapper(provisioningEntityWrapperReal);
      provisioningEntityWrapperReal.setTargetProvisioningEntity(targetProvisioningEntity);
      if (provisioningEntityWrapperTarget != null) {
        provisioningEntityWrapperReal.setTargetNativeEntity(provisioningEntityWrapperTarget.getTargetNativeEntity());
      }
    }
  
  }


  public void indexMatchingIdOfTargetGroups() {
  
    Map<Object, ProvisioningGroupWrapper> targetGroupIdToProvisioningGroupWrapper = 
        this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupMatchingIdToProvisioningGroupWrapper();
  
    // make sure we arent double dipping target provisioning matching ids
    Set<Object> targetProvisioningMatchingIds = new HashSet<Object>();
    for (ProvisioningGroup targetProvisioningGroup : 
      GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataTarget()
          .getTargetProvisioningObjects().getProvisioningGroups())) {
      
      Object macthingId = targetProvisioningGroup.getMatchingId();
      if (macthingId == null) {
        throw new NullPointerException("Cant find id for targetProvisioningGroup! " + targetProvisioningGroup);
      }
      
      if (targetProvisioningMatchingIds.contains(macthingId)) {
        throw new NullPointerException("Why do multiple groups from target have the same matching id???\n" 
            + targetProvisioningGroup + "\n" + targetGroupIdToProvisioningGroupWrapper.get(macthingId));
      }
      targetProvisioningMatchingIds.add(macthingId);
  
      ProvisioningGroupWrapper provisioningGroupWrapperReal = targetGroupIdToProvisioningGroupWrapper.get(macthingId);
      if (provisioningGroupWrapperReal == null) {
        provisioningGroupWrapperReal = new ProvisioningGroupWrapper();
        targetGroupIdToProvisioningGroupWrapper.put(macthingId, provisioningGroupWrapperReal);
      }
  
      ProvisioningGroupWrapper provisioningGroupWrapperTarget = targetProvisioningGroup.getProvisioningGroupWrapper();
  
      // lets merge these to get our complete wrapper
      targetProvisioningGroup.setProvisioningGroupWrapper(provisioningGroupWrapperReal);
      provisioningGroupWrapperReal.setTargetProvisioningGroup(targetProvisioningGroup);
      if (provisioningGroupWrapperTarget != null) {
        provisioningGroupWrapperReal.setTargetNativeGroup(provisioningGroupWrapperTarget.getTargetNativeGroup());
      }
    }
  
  }


  public void indexMatchingIdOfTargetMemberships() {
  
    Map<Object, ProvisioningMembershipWrapper> targetMembershipIdToProvisioningMembershipWrapper = 
        this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMembershipMatchingIdToProvisioningMembershipWrapper();
  
    // make sure we arent double dipping target provisioning matching ids
    Set<Object> targetProvisioningMatchingIds = new HashSet<Object>();
    for (ProvisioningMembership targetProvisioningMembership : 
      GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataTarget()
          .getTargetProvisioningObjects().getProvisioningMemberships())) {
      
      Object macthingId = targetProvisioningMembership.getMatchingId();
      if (macthingId == null) {
        throw new NullPointerException("Cant find id for targetProvisioningMembership! " + targetProvisioningMembership);
      }
      
      if (targetProvisioningMatchingIds.contains(macthingId)) {
        throw new NullPointerException("Why do multiple memberships from target have the same matching id???\n" 
            + targetProvisioningMembership + "\n" + targetMembershipIdToProvisioningMembershipWrapper.get(macthingId));
      }
      targetProvisioningMatchingIds.add(macthingId);
  
      ProvisioningMembershipWrapper provisioningMembershipWrapperReal = targetMembershipIdToProvisioningMembershipWrapper.get(macthingId);
      if (provisioningMembershipWrapperReal == null) {
        provisioningMembershipWrapperReal = new ProvisioningMembershipWrapper();
        targetMembershipIdToProvisioningMembershipWrapper.put(macthingId, provisioningMembershipWrapperReal);
      }
  
      ProvisioningMembershipWrapper provisioningMembershipWrapperTarget = targetProvisioningMembership.getProvisioningMembershipWrapper();
  
      // lets merge these to get our complete wrapper
      targetProvisioningMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapperReal);
      provisioningMembershipWrapperReal.setTargetProvisioningMembership(targetProvisioningMembership);
      if (provisioningMembershipWrapperTarget != null) {
        provisioningMembershipWrapperReal.setTargetNativeMembership(provisioningMembershipWrapperTarget.getTargetNativeMembership());
      }
      
      
      
    }
  
  }


  public void indexMatchingIdOfTargetObjects() {
    this.indexMatchingIdOfTargetGroups();
    
    this.indexMatchingIdOfTargetEntities();
  
    this.indexMatchingIdOfTargetMemberships();
  }


  
}
