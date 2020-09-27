package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperProvisioningTargetIdIndex {

  private GrouperProvisioner grouperProvisioner = null;

  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }


  public void indexTargetIdOfGrouperEntities(List<ProvisioningEntity> grouperTargetEntities) {
    Map<Object, ProvisioningEntityWrapper> targetEntityIdToProvisioningEntityWrapper = 
        this.grouperProvisioner.retrieveGrouperProvisioningData().getEntityTargetIdToProvisioningEntityWrapper();
  
    int grouperTargetEntitiesWithNullTargetIds = 0;
    
    Set<Object> targetIds = new HashSet<Object>();

    for (ProvisioningEntity grouperTargetEntity : GrouperUtil.nonNull(grouperTargetEntities)) {
      
      Object targetId = grouperTargetEntity.getTargetId();
      if (targetId == null) {
        // this could be an insert?
        grouperTargetEntitiesWithNullTargetIds++;
        // TODO make sure to handle this in the compare
        continue;
      }
      
      if (targetIds.contains(targetId)) {
        throw new NullPointerException("Why do multiple entities from grouper have the same target id???\n" 
            + grouperTargetEntity + "\n" + targetEntityIdToProvisioningEntityWrapper.get(targetId));
      }
      targetIds.add(targetId);

      ProvisioningEntityWrapper provisioningEntityWrapper = grouperTargetEntity.getProvisioningEntityWrapper();
      if (provisioningEntityWrapper == null) {
        throw new NullPointerException("Cant find entity wrapper: " + grouperTargetEntity);
      }
      targetEntityIdToProvisioningEntityWrapper.put(targetId, provisioningEntityWrapper);
    }
    
    if (grouperTargetEntitiesWithNullTargetIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("grouperTargetEntitiesWithNullTargetIds", grouperTargetEntitiesWithNullTargetIds);
    }
  
  
  }


  public void indexTargetIdOfGrouperGroups(List<ProvisioningGroup> grouperTargetGroups) {
    Map<Object, ProvisioningGroupWrapper> targetGroupIdToProvisioningGroupWrapper = 
        this.grouperProvisioner.retrieveGrouperProvisioningData().getGroupTargetIdToProvisioningGroupWrapper();
  
    int grouperTargetGroupsWithNullTargetIds = 0;
    
    Set<Object> targetIds = new HashSet<Object>();
    
    for (ProvisioningGroup grouperTargetGroup : GrouperUtil.nonNull(grouperTargetGroups)) {
      
      Object targetId = grouperTargetGroup.getTargetId();
      if (targetId == null) {
        // this could be an insert?
        grouperTargetGroupsWithNullTargetIds++;
        // TODO make sure to handle this in the compare
        continue;
      }
      
      if (targetIds.contains(targetId)) {
        throw new NullPointerException("Why do multiple groups from grouper have the same target id???\n" 
            + grouperTargetGroup + "\n" + targetGroupIdToProvisioningGroupWrapper.get(targetId));
      }
      targetIds.add(targetId);
  
      ProvisioningGroupWrapper provisioningGroupWrapper = grouperTargetGroup.getProvisioningGroupWrapper();
      if (provisioningGroupWrapper == null) {
        throw new NullPointerException("Cant find group wrapper: " + grouperTargetGroup);
      }
      targetGroupIdToProvisioningGroupWrapper.put(targetId, provisioningGroupWrapper);
    }
    
    if (grouperTargetGroupsWithNullTargetIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("grouperTargetGroupsWithNullTargetIds", grouperTargetGroupsWithNullTargetIds);
    }
  
  }


  public void indexTargetIdOfGrouperMemberships(List<ProvisioningMembership> grouperTargetMemberships) {
    Map<Object, ProvisioningMembershipWrapper> targetMembershipIdToProvisioningMembershipWrapper = 
        this.grouperProvisioner.retrieveGrouperProvisioningData().getMembershipTargetIdToProvisioningMembershipWrapper();
  
    int grouperTargetMembershipsWithNullTargetIds = 0;
    
    Set<Object> targetIds = new HashSet<Object>();

    for (ProvisioningMembership grouperTargetMembership : GrouperUtil.nonNull(grouperTargetMemberships)) {
      
      Object targetId = grouperTargetMembership.getTargetId();
      if (targetId == null) {
        // this could be an insert?
        grouperTargetMembershipsWithNullTargetIds++;
        // TODO make sure to handle this in the compare
        continue;
      }
      
      if (targetIds.contains(targetId)) {
        throw new NullPointerException("Why do multiple memberships from grouper have the same target id???\n" 
            + grouperTargetMembership + "\n" + targetMembershipIdToProvisioningMembershipWrapper.get(targetId));
      }
      targetIds.add(targetId);

  
      ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperTargetMembership.getProvisioningMembershipWrapper();
      if (provisioningMembershipWrapper == null) {
        throw new NullPointerException("Cant find membership wrapper: " + grouperTargetMembership);
      }
      targetMembershipIdToProvisioningMembershipWrapper.put(targetId, provisioningMembershipWrapper);
    }
    
    if (grouperTargetMembershipsWithNullTargetIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("grouperTargetMembershipsWithNullTargetIds", grouperTargetMembershipsWithNullTargetIds);
    }
  
  
  }


  public void indexTargetIdOfGrouperObjects() {
    this.indexTargetIdOfGrouperGroups(
        this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningGroups());
    
    this.indexTargetIdOfGrouperEntities(
        this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningEntities());
  
    this.indexTargetIdOfGrouperMemberships(
        this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningMemberships());

    // these might be empty for full provisioning and thats ok
    this.indexTargetIdOfGrouperGroups(
        this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjectsIncludeDeletes().getProvisioningGroups());
    
    this.indexTargetIdOfGrouperEntities(
        this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjectsIncludeDeletes().getProvisioningEntities());
  
    this.indexTargetIdOfGrouperMemberships(
        this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjectsIncludeDeletes().getProvisioningMemberships());
  }


  public void indexTargetIdOfTargetEntities() {
  
    Map<Object, ProvisioningEntityWrapper> targetEntityIdToProvisioningEntityWrapper = 
        this.grouperProvisioner.retrieveGrouperProvisioningData().getEntityTargetIdToProvisioningEntityWrapper();
  
    // make sure we arent double dipping target provisioning target ids
    Set<Object> targetProvisioningTargetIds = new HashSet<Object>();
    for (ProvisioningEntity targetProvisioningEntity : 
      GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData()
          .getTargetProvisioningObjects().getProvisioningEntities())) {
      
      Object targetId = targetProvisioningEntity.getTargetId();
      if (targetId == null) {
        throw new NullPointerException("Cant find id for targetProvisioningEntity! " + targetProvisioningEntity);
      }
      
      if (targetProvisioningTargetIds.contains(targetId)) {
        throw new NullPointerException("Why do multiple entities from target have the same target id???\n" 
            + targetProvisioningEntity + "\n" + targetEntityIdToProvisioningEntityWrapper.get(targetId));
      }
      targetProvisioningTargetIds.add(targetId);
  
      ProvisioningEntityWrapper provisioningEntityWrapperReal = targetEntityIdToProvisioningEntityWrapper.get(targetId);
      if (provisioningEntityWrapperReal == null) {
        provisioningEntityWrapperReal = new ProvisioningEntityWrapper();
        targetEntityIdToProvisioningEntityWrapper.put(targetId, provisioningEntityWrapperReal);
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


  public void indexTargetIdOfTargetGroups() {
  
    Map<Object, ProvisioningGroupWrapper> targetGroupIdToProvisioningGroupWrapper = 
        this.grouperProvisioner.retrieveGrouperProvisioningData().getGroupTargetIdToProvisioningGroupWrapper();
  
    // make sure we arent double dipping target provisioning target ids
    Set<Object> targetProvisioningTargetIds = new HashSet<Object>();
    for (ProvisioningGroup targetProvisioningGroup : 
      GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData()
          .getTargetProvisioningObjects().getProvisioningGroups())) {
      
      Object targetId = targetProvisioningGroup.getTargetId();
      if (targetId == null) {
        throw new NullPointerException("Cant find id for targetProvisioningGroup! " + targetProvisioningGroup);
      }
      
      if (targetProvisioningTargetIds.contains(targetId)) {
        throw new NullPointerException("Why do multiple groups from target have the same target id???\n" 
            + targetProvisioningGroup + "\n" + targetGroupIdToProvisioningGroupWrapper.get(targetId));
      }
      targetProvisioningTargetIds.add(targetId);
  
      ProvisioningGroupWrapper provisioningGroupWrapperReal = targetGroupIdToProvisioningGroupWrapper.get(targetId);
      if (provisioningGroupWrapperReal == null) {
        provisioningGroupWrapperReal = new ProvisioningGroupWrapper();
        targetGroupIdToProvisioningGroupWrapper.put(targetId, provisioningGroupWrapperReal);
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


  public void indexTargetIdOfTargetMemberships() {
  
    Map<Object, ProvisioningMembershipWrapper> targetMembershipIdToProvisioningMembershipWrapper = 
        this.grouperProvisioner.retrieveGrouperProvisioningData().getMembershipTargetIdToProvisioningMembershipWrapper();
  
    // make sure we arent double dipping target provisioning target ids
    Set<Object> targetProvisioningTargetIds = new HashSet<Object>();
    for (ProvisioningMembership targetProvisioningMembership : 
      GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData()
          .getTargetProvisioningObjects().getProvisioningMemberships())) {
      
      Object targetId = targetProvisioningMembership.getTargetId();
      if (targetId == null) {
        throw new NullPointerException("Cant find id for targetProvisioningMembership! " + targetProvisioningMembership);
      }
      
      if (targetProvisioningTargetIds.contains(targetId)) {
        throw new NullPointerException("Why do multiple memberships from target have the same target id???\n" 
            + targetProvisioningMembership + "\n" + targetMembershipIdToProvisioningMembershipWrapper.get(targetId));
      }
      targetProvisioningTargetIds.add(targetId);
  
      ProvisioningMembershipWrapper provisioningMembershipWrapperReal = targetMembershipIdToProvisioningMembershipWrapper.get(targetId);
      if (provisioningMembershipWrapperReal == null) {
        provisioningMembershipWrapperReal = new ProvisioningMembershipWrapper();
        targetMembershipIdToProvisioningMembershipWrapper.put(targetId, provisioningMembershipWrapperReal);
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


  public void indexTargetIdOfTargetObjects() {
    this.indexTargetIdOfTargetGroups();
    
    this.indexTargetIdOfTargetEntities();
  
    this.indexTargetIdOfTargetMemberships();
  }


  
}
