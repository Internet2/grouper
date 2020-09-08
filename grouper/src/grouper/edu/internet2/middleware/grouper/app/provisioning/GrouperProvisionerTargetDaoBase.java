package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * generally when you deal with insert/update/delete/retrieve
 * you should use the "targetId" of the parameter to the method.
 * Note: when you retrieve the data, you should not set the targetId,
 * you should let the provisioning framework set that.
 * 
 * The results of retrieving a few objects should be in the same format
 * as when you retrieve all at once.
 * 
 */
public abstract class GrouperProvisionerTargetDaoBase {
  
  public List<ProvisioningGroup> retrieveAllGroups() {
    throw new UnsupportedOperationException();
  }
  
  public List<ProvisioningEntity> retrieveAllEntities() {
    throw new UnsupportedOperationException();
  }
  
 
  public List<ProvisioningMembership> retrieveAllMemberships() {
    throw new UnsupportedOperationException();
  }
  
  /**
   * @paProvisioningGrouproup
   */
  public void deleteGroup(ProvisioningGroup targetGroup) {
    throw new UnsupportedOperationException();
  }

  /**
   * @paProvisioningGrouproup
   */
  public void insertGroup(ProvisioningGroup targetGroup) {
    throw new UnsupportedOperationException();
  }

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
   * reference back up to the provisioner
   * @param grouperProvisioner1
   */
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner1) {
    this.grouperProvisioner = grouperProvisioner1;
  }


  protected void sendChangesToTarget() {

    sendGroupChangesToTarget();
    sendEntityChangesToTarget();
    sendMembershipChangesToTarget();
    
  }

  public void sendGroupChangesToTarget() {
    List<ProvisioningGroup> targetGroupDeletes = this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectDeletes().getProvisioningGroups();
    
    this.deleteGroups(targetGroupDeletes);

    List<ProvisioningGroup> targetGroupInserts = this.getGrouperProvisioner()
        .getGrouperProvisioningData().getTargetObjectInserts().getProvisioningGroups();
    
    this.insertGroups(targetGroupInserts);
    
    List<ProvisioningGroup> targetGroupUpdates = this.getGrouperProvisioner()
        .getGrouperProvisioningData().getTargetObjectUpdates().getProvisioningGroups();
    
    this.updateGroups(targetGroupUpdates);
  }

  /**
   * insert all these groups and either throw exception for all or mark each one with an exception
   * @param targetGroupInserts
   */
  public void updateGroups(List<ProvisioningGroup> targetGroupInserts) {
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(targetGroupInserts)) {
      try {
        updateGroup(provisioningGroup);
      } catch (Exception e) {
        provisioningGroup.setException(e);
      }
    }
  }

  /**
   * delete all these Memberships and either throw exception for all or mark each one with an exception
   * @param targetMembershipDeletes
   */
  public void deleteMemberships(List<ProvisioningMembership> targetMembershipDeletes) {
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(targetMembershipDeletes)) {
      try {
        deleteMembership(provisioningMembership);
      } catch (Exception e) {
        provisioningMembership.setException(e);
      }
    }
  }

  /**
   * retrieve all data from the target
   */
  public void retrieveAllData() {
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    try {
      long start = System.currentTimeMillis();
      List<ProvisioningGroup> targetProvisioningGroups = this.retrieveAllGroups();
      this.getGrouperProvisioner().getGrouperProvisioningData().getTargetProvisioningObjects().setProvisioningGroups(targetProvisioningGroups);
      debugMap.put("retrieveTargetGroupsMillis", System.currentTimeMillis() - start);
      debugMap.put("targetGroupCount", GrouperUtil.length(targetProvisioningGroups));
    } catch (UnsupportedOperationException uoe) {
      //not implemented
    }
    try {
      long start = System.currentTimeMillis();
      List<ProvisioningEntity> targetProvisioningEntities = this.retrieveAllEntities();
      this.getGrouperProvisioner().getGrouperProvisioningData().getTargetProvisioningObjects().setProvisioningEntities(targetProvisioningEntities);
      debugMap.put("retrieveTargetEntitiesMillis", System.currentTimeMillis() - start);
      debugMap.put("targetEntityCount", GrouperUtil.length(targetProvisioningEntities));
    } catch (UnsupportedOperationException uoe) {
      //not implemented
    }
    try {
      long start = System.currentTimeMillis();
      List<ProvisioningMembership> targetProvisioningMemberships = this.retrieveAllMemberships();
      this.getGrouperProvisioner().getGrouperProvisioningData().getTargetProvisioningObjects().setProvisioningMemberships(targetProvisioningMemberships);
      debugMap.put("retrieveTargetMshipsMillis", System.currentTimeMillis() - start);
      debugMap.put("targetMshipCount", GrouperUtil.length(targetProvisioningMemberships));
    } catch (UnsupportedOperationException uoe) {
      //not implemented
    }
  }

  /**
   * retrieve all incremental data from the target from the target ids of the grouper translated and indexed target groups
   */
  public void retrieveIncrementalData() {
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    {
      List<ProvisioningGroup> grouperTargetGroups = this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperTargetObjects().getProvisioningGroups();
      if (GrouperUtil.length(grouperTargetGroups) > 0) {
        long start = System.currentTimeMillis();
        // if there are groups then this must be implemented
        List<ProvisioningGroup> targetProvisioningGroups = this.retrieveGroups(
            grouperTargetGroups);
        this.getGrouperProvisioner().getGrouperProvisioningData().getTargetProvisioningObjects().setProvisioningGroups(targetProvisioningGroups);
        debugMap.put("retrieveTargetGroupsMillis", System.currentTimeMillis() - start);
        debugMap.put("targetGroupCount", GrouperUtil.length(targetProvisioningGroups));
      }
    }
    {
      List<ProvisioningEntity> grouperTargetEntities = this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperTargetObjects().getProvisioningEntities();
      if (GrouperUtil.length(grouperTargetEntities) > 0) {
        long start = System.currentTimeMillis();
        List<ProvisioningEntity> targetProvisioningEntities = this.retrieveEntities(
            grouperTargetEntities);
        this.getGrouperProvisioner().getGrouperProvisioningData().getTargetProvisioningObjects().setProvisioningEntities(targetProvisioningEntities);
        debugMap.put("retrieveTargetEntitiesMillis", System.currentTimeMillis() - start);
        debugMap.put("targetEntityCount", GrouperUtil.length(targetProvisioningEntities));
      }
    }
    {
      List<ProvisioningMembership> grouperTargetMemberships = this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperTargetObjects().getProvisioningMemberships();
      if (GrouperUtil.length(grouperTargetMemberships) > 0) {
        long start = System.currentTimeMillis();
        List<ProvisioningMembership> targetProvisioningMemberships = this.retrieveMemberships(grouperTargetMemberships);
        this.getGrouperProvisioner().getGrouperProvisioningData().getTargetProvisioningObjects().setProvisioningMemberships(targetProvisioningMemberships);
        debugMap.put("retrieveTargetMshipsMillis", System.currentTimeMillis() - start);
        debugMap.put("targetMshipCount", GrouperUtil.length(targetProvisioningMemberships));
      }
      //not implemented
    }
  }

  /**
   * bulk retrieve target provisioning groups, generally use the target Ids in the grouperTargetGroups
   * @param grouperTargetGroups
   * @return the target provisioning groups
   */
  public List<ProvisioningGroup> retrieveGroups(List<ProvisioningGroup> grouperTargetGroups) {
    List<ProvisioningGroup> targetProvisioningGroups = new ArrayList<ProvisioningGroup>();
    for (ProvisioningGroup grouperTargetGroup : GrouperUtil.nonNull(grouperTargetGroups)) {
      ProvisioningGroup targetProvisioningGroup = retrieveGroup(grouperTargetGroup);
      if (targetProvisioningGroup != null) {
        targetProvisioningGroups.add(targetProvisioningGroup);
      }
    }
    return targetProvisioningGroups;
  }

  /**
   * bulk retrieve target provisioning Memberships, generally use the target Ids in the grouperTargetMemberships
   * @param grouperTargetMemberships
   * @return the target provisioning Memberships
   */
  public List<ProvisioningMembership> retrieveMemberships(List<ProvisioningMembership> grouperTargetMemberships) {
    List<ProvisioningMembership> targetProvisioningMemberships = new ArrayList<ProvisioningMembership>();
    for (ProvisioningMembership grouperTargetMembership : GrouperUtil.nonNull(grouperTargetMemberships)) {
      ProvisioningMembership targetProvisioningMembership = retrieveMembership(grouperTargetMembership);
      if (targetProvisioningMembership != null) {
        targetProvisioningMemberships.add(targetProvisioningMembership);
      }
    }
    return targetProvisioningMemberships;
  }

  /**
   * bulk retrieve target provisioning Entities, generally use the target Ids in the grouperTargetEntities
   * @param grouperTargetEntities
   * @return the target provisioning Entities
   */
  public List<ProvisioningEntity> retrieveEntities(List<ProvisioningEntity> grouperTargetEntities) {
    List<ProvisioningEntity> targetProvisioningEntities = new ArrayList<ProvisioningEntity>();
    for (ProvisioningEntity grouperTargetEntity : GrouperUtil.nonNull(grouperTargetEntities)) {
      ProvisioningEntity targetProvisioningEntity = retrieveEntity(grouperTargetEntity);
      if (targetProvisioningEntity != null) {
        targetProvisioningEntities.add(targetProvisioningEntity);
      }
    }
    return targetProvisioningEntities;
  }

  /**
   * return a group by target id of grouper target group, or null if not found
   * @param grouperTargetGroup
   * @return the target provisioning group or null if not found
   */
  public ProvisioningGroup retrieveGroup(ProvisioningGroup grouperTargetGroup) {
    throw new UnsupportedOperationException();
  }

  /**
   * return a Entity by target id of grouper target Entity, or null if not found
   * @param grouperTargetEntity
   * @return the target provisioning Entity or null if not found
   */
  public ProvisioningEntity retrieveEntity(ProvisioningEntity grouperTargetEntity) {
    throw new UnsupportedOperationException();
  }

  /**
   * return a Membership by target id of grouper target Membership, or null if not found
   * @param grouperTargetMembership
   * @return the target provisioning Membership or null if not found
   */
  public ProvisioningMembership retrieveMembership(ProvisioningMembership grouperTargetMembership) {
    throw new UnsupportedOperationException();
  }

  /**
   * @paProvisioningGrouproup
   */
  public void updateGroup(ProvisioningGroup targetGroup) {
    throw new UnsupportedOperationException();
  }

  /**
   * insert all these groups and either throw exception for all or mark each one with an exception
   * @param targetGroupInserts
   */
  public void insertGroups(List<ProvisioningGroup> targetGroupInserts) {
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(targetGroupInserts)) {
      try {
        insertGroup(provisioningGroup);
      } catch (Exception e) {
        provisioningGroup.setException(e);
      }
    }
  }

  /**
   * @paProvisioningEntity
   */
  public void deleteEntity(ProvisioningEntity targetEntity) {
    throw new UnsupportedOperationException();
  }

  /**
   * delete all these entities and either throw exception for all or mark each one with an exception
   * @param targetEntityDeletes
   */
  public void deleteEntities(List<ProvisioningEntity> targetEntityDeletes) {
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(targetEntityDeletes)) {
      try {
        deleteEntity(provisioningEntity);
      } catch (Exception e) {
        provisioningEntity.setException(e);
      }
    }
  }

  /**
   * @paProvisioningGrouproup
   */
  public void insertEntity(ProvisioningEntity targetEntity) {
    throw new UnsupportedOperationException();
  }

  /**
   * insert all these groups and either throw exception for all or mark each one with an exception
   * @param targetEntityInserts
   */
  public void insertEntities(List<ProvisioningEntity> targetEntityInserts) {
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(targetEntityInserts)) {
      try {
        insertEntity(provisioningEntity);
      } catch (Exception e) {
        provisioningEntity.setException(e);
      }
    }
  }

  /**
   * @paProvisioningEntity
   */
  public void updateEntity(ProvisioningEntity targetEntity) {
    throw new UnsupportedOperationException();
  }

  /**
   * insert all these Entities and either throw exception for all or mark each one with an exception
   * @param targetEntityInserts
   */
  public void updateEntities(List<ProvisioningEntity> targetEntityInserts) {
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(targetEntityInserts)) {
      try {
        updateEntity(provisioningEntity);
      } catch (Exception e) {
        provisioningEntity.setException(e);
      }
    }
  }

  /**
   * @paProvisioningMembership
   */
  public void deleteMembership(ProvisioningMembership targetMembership) {
    throw new UnsupportedOperationException();
  }

  /**
   * delete all these groups and either throw exception for all or mark each one with an exception
   * @param targetGroupDeletes
   */
  public void deleteGroups(List<ProvisioningGroup> targetGroupDeletes) {
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(targetGroupDeletes)) {
      try {
        deleteGroup(provisioningGroup);
      } catch (Exception e) {
        provisioningGroup.setException(e);
      }
    }
  }

  /**
   * @paProvisioningMembership
   */
  public void insertMembership(ProvisioningMembership targetMembership) {
    throw new UnsupportedOperationException();
  }

  /**
   * insert all these Memberships and either throw exception for all or mark each one with an exception
   * @param targetMembershipInserts
   */
  public void insertMemberships(List<ProvisioningMembership> targetMembershipInserts) {
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(targetMembershipInserts)) {
      try {
        insertMembership(provisioningMembership);
      } catch (Exception e) {
        provisioningMembership.setException(e);
      }
    }
  }

  /**
   * @paProvisioningMembership
   */
  public void updateMembership(ProvisioningMembership targetMembership) {
    throw new UnsupportedOperationException();
  }

  /**
   * insert all these Memberships and either throw exception for all or mark each one with an exception
   * @param targetGroupInserts
   */
  public void updateMemberships(List<ProvisioningMembership> targetMembershipInserts) {
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(targetMembershipInserts)) {
      try {
        updateMembership(provisioningMembership);
      } catch (Exception e) {
        provisioningMembership.setException(e);
      }
    }
  }

  public void sendEntityChangesToTarget() {
    List<ProvisioningEntity> targetEntityDeletes = this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectDeletes().getProvisioningEntities();
    
    this.deleteEntities(targetEntityDeletes);
  
    List<ProvisioningEntity> targetEntityInserts = this.getGrouperProvisioner()
        .getGrouperProvisioningData().getTargetObjectInserts().getProvisioningEntities();
    
    this.insertEntities(targetEntityInserts);
    
    List<ProvisioningEntity> targetEntityUpdates = this.getGrouperProvisioner()
        .getGrouperProvisioningData().getTargetObjectUpdates().getProvisioningEntities();
    
    this.updateEntities(targetEntityUpdates);
  }

  public void sendMembershipChangesToTarget() {
    List<ProvisioningMembership> targetMembershipDeletes = this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectDeletes().getProvisioningMemberships();
    
    this.deleteMemberships(targetMembershipDeletes);
  
    List<ProvisioningMembership> targetMembershipInserts = this.getGrouperProvisioner()
        .getGrouperProvisioningData().getTargetObjectInserts().getProvisioningMemberships();
    
    this.insertMemberships(targetMembershipInserts);
    
    List<ProvisioningMembership> targetMembershipUpdates = this.getGrouperProvisioner()
        .getGrouperProvisioningData().getTargetObjectUpdates().getProvisioningMemberships();
    
    this.updateMemberships(targetMembershipUpdates);
  }
  
}
