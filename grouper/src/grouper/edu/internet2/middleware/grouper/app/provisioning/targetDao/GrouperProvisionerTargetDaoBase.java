package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLists;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;


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
  
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * dont return null
   * @param targetDaoRetrieveAllEntitiesRequest
   * @return
   */
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {
    throw new UnsupportedOperationException();
  }
  
 
  public TargetDaoRetrieveAllMembershipsResponse retrieveAllMemberships(TargetDaoRetrieveAllMembershipsRequest targetDaoRetrieveAllMembershipsRequest) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @paProvisioningGrouproup
   */
  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @paProvisioningGrouproup
   */
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest ta) {
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


  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param targetDaoSendChangesToTargetRequest
   * @return
   */
  public TargetDaoSendChangesToTargetResponse sendChangesToTarget(TargetDaoSendChangesToTargetRequest targetDaoSendChangesToTargetRequest) {

    {
      TargetDaoSendGroupChangesToTargetRequest targetDaoSendGroupChangesToTargetRequest = new TargetDaoSendGroupChangesToTargetRequest(
          targetDaoSendChangesToTargetRequest.getTargetObjectInserts().getProvisioningGroups(), 
          targetDaoSendChangesToTargetRequest.getTargetObjectUpdates().getProvisioningGroups(),
          targetDaoSendChangesToTargetRequest.getTargetObjectDeletes().getProvisioningGroups());
      sendGroupChangesToTarget(targetDaoSendGroupChangesToTargetRequest);
    }
    {
      TargetDaoSendEntityChangesToTargetRequest targetDaoSendEntityChangesToTargetRequest = new TargetDaoSendEntityChangesToTargetRequest(
          targetDaoSendChangesToTargetRequest.getTargetObjectInserts().getProvisioningEntities(), 
          targetDaoSendChangesToTargetRequest.getTargetObjectUpdates().getProvisioningEntities(),
          targetDaoSendChangesToTargetRequest.getTargetObjectDeletes().getProvisioningEntities());
      sendEntityChangesToTarget(targetDaoSendEntityChangesToTargetRequest);
    }
    {
      TargetDaoSendMembershipChangesToTargetRequest targetDaoSendMembershipChangesToTargetRequest = new TargetDaoSendMembershipChangesToTargetRequest(
          targetDaoSendChangesToTargetRequest.getTargetObjectInserts().getProvisioningMemberships(), 
          targetDaoSendChangesToTargetRequest.getTargetObjectUpdates().getProvisioningMemberships(),
          targetDaoSendChangesToTargetRequest.getTargetObjectDeletes().getProvisioningMemberships());
      sendMembershipChangesToTarget(targetDaoSendMembershipChangesToTargetRequest);
    }
    return null;
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param targetDaoSendGroupChangesToTargetRequest
   * @return
   */
  public TargetDaoSendGroupChangesToTargetResponse sendGroupChangesToTarget(TargetDaoSendGroupChangesToTargetRequest targetDaoSendGroupChangesToTargetRequest) {

    
    {
      List<ProvisioningGroup> targetGroupDeletes = targetDaoSendGroupChangesToTargetRequest.getTargetGroupDeletes();
      TargetDaoDeleteGroupsRequest targetDaoDeleteGroupsRequest =  new TargetDaoDeleteGroupsRequest();
      targetDaoDeleteGroupsRequest.setTargetGroupDeletes(targetGroupDeletes);
      this.deleteGroups(targetDaoDeleteGroupsRequest);
    }
    List<ProvisioningGroup> targetGroupInserts = targetDaoSendGroupChangesToTargetRequest.getTargetGroupInserts();
    
    this.insertGroups(new TargetDaoInsertGroupsRequest(targetGroupInserts));
    
    List<ProvisioningGroup> targetGroupUpdates = targetDaoSendGroupChangesToTargetRequest.getTargetGroupUpdates();
    
    this.updateGroups(new TargetDaoUpdateGroupsRequest(targetGroupUpdates));
    return null;
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * insert all these groups and either throw exception for all or mark each one with an exception
   * @param targetGroupInserts
   */
  public TargetDaoUpdateGroupsResponse updateGroups(TargetDaoUpdateGroupsRequest targetDaoUpdateGroupsRequest) {
    List<ProvisioningGroup> targetGroups = targetDaoUpdateGroupsRequest == null ? null : targetDaoUpdateGroupsRequest.getTargetGroups();
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(targetGroups)) {
      try {
        updateGroup(new TargetDaoUpdateGroupRequest(provisioningGroup));
      } catch (Exception e) {
        provisioningGroup.setException(e);
        provisioningGroup.setProvisioned(false);
      }
    }
    return null;
  }

  /**
   * delete all these Memberships and either throw exception for all or mark each one with an exception
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param targetMembershipDeletes
   */
  public TargetDaoDeleteMembershipsResponse deleteMemberships(TargetDaoDeleteMembershipsRequest targetDaoDeleteMembershipsRequest) {
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(targetDaoDeleteMembershipsRequest.getTargetMembershipDeletes())) {
      try {
        deleteMembership(new TargetDaoDeleteMembershipRequest(provisioningMembership));
      } catch (Exception e) {
        provisioningMembership.setException(e);
        provisioningMembership.setProvisioned(false);
      }
    }
    return null;
  }

  /**
   * retrieve all data from the target
   */
  public TargetDaoRetrieveAllDataResponse retrieveAllData(TargetDaoRetrieveAllDataRequest targetDaoRetrieveAllDataRequest) {
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    GrouperProvisioningLists targetObjects = new GrouperProvisioningLists();
    TargetDaoRetrieveAllDataResponse result = new TargetDaoRetrieveAllDataResponse(targetObjects);
    try {
      long start = System.currentTimeMillis();
      TargetDaoRetrieveAllGroupsResponse targetDaoRetrieveAllGroupsResponse = this.retrieveAllGroups(new TargetDaoRetrieveAllGroupsRequest(true));
      List<ProvisioningGroup> targetGroups = targetDaoRetrieveAllGroupsResponse == null ? null : targetDaoRetrieveAllGroupsResponse.getTargetGroups();
      targetObjects.setProvisioningGroups(targetGroups);
      debugMap.put("retrieveTargetGroupsMillis", System.currentTimeMillis() - start);
      debugMap.put("targetGroupCount", GrouperUtil.length(targetGroups));
    } catch (UnsupportedOperationException uoe) {
      //not implemented
    }
    try {
      long start = System.currentTimeMillis();
      TargetDaoRetrieveAllEntitiesResponse targetDaoRetrieveAllEntitiesResponse = this.retrieveAllEntities(new TargetDaoRetrieveAllEntitiesRequest(true));
      List<ProvisioningEntity> targetEntities = 
          targetDaoRetrieveAllEntitiesResponse == null ? null : targetDaoRetrieveAllEntitiesResponse.getTargetEntities();
      targetObjects.setProvisioningEntities(targetEntities);
      debugMap.put("retrieveTargetEntitiesMillis", System.currentTimeMillis() - start);
      debugMap.put("targetEntityCount", GrouperUtil.length(targetEntities));
    } catch (UnsupportedOperationException uoe) {
      //not implemented
    }
    try {
      long start = System.currentTimeMillis();
      TargetDaoRetrieveAllMembershipsResponse targetDaoRetrieveAllMembershipsResponse = 
          this.retrieveAllMemberships(new TargetDaoRetrieveAllMembershipsRequest());
      List<ProvisioningMembership> targetMemberships = 
          targetDaoRetrieveAllMembershipsResponse == null ? null : 
            targetDaoRetrieveAllMembershipsResponse.getTargetMemberships();
      targetObjects.setProvisioningMemberships(targetMemberships);
      debugMap.put("retrieveTargetMshipsMillis", System.currentTimeMillis() - start);
      debugMap.put("targetMshipCount", GrouperUtil.length(targetMemberships));
    } catch (UnsupportedOperationException uoe) {
      //not implemented
    }
    return result;
  }

  /**
   * retrieve all incremental data from the target from the target ids of the grouper translated and indexed target groups
   */
  public TargetDaoRetrieveIncrementalDataResponse retrieveIncrementalData(
      TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncementalDataRequest) {
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    GrouperProvisioningLists targetObjects = new GrouperProvisioningLists();
    TargetDaoRetrieveIncrementalDataResponse result = new TargetDaoRetrieveIncrementalDataResponse(targetObjects);
    {
      List<ProvisioningGroup> targetGroups = null;
      targetGroups = targetDaoRetrieveIncementalDataRequest.getTargetGroupsForGroupOnly();
      if (GrouperUtil.length(targetGroups) > 0) {
        long start = System.currentTimeMillis();
        // if there are groups then this must be implemented
        TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = this.retrieveGroups(
            new TargetDaoRetrieveGroupsRequest(targetGroups, false));
        List<ProvisioningGroup> targetGroupsResult = targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups();
        targetObjects.setProvisioningGroups(targetGroupsResult);
        debugMap.put("retrieveTargetGroupsMillis", System.currentTimeMillis() - start);
        debugMap.put("targetGroupCount", GrouperUtil.length(targetGroupsResult));
      }
    }
    {
      List<ProvisioningEntity> targetEntities = null;
      targetEntities = targetDaoRetrieveIncementalDataRequest.getTargetEntitiesForEntityOnly();
      if (GrouperUtil.length(targetEntities) > 0) {
        long start = System.currentTimeMillis();
        TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = this.retrieveEntities(
            new TargetDaoRetrieveEntitiesRequest(targetEntities, false));
        List<ProvisioningEntity> targetEntitiesResult = targetDaoRetrieveEntitiesResponse == null ?
            null : targetDaoRetrieveEntitiesResponse.getTargetEntities();
        targetObjects.setProvisioningEntities(targetEntitiesResult);
        debugMap.put("retrieveTargetEntitiesMillis", System.currentTimeMillis() - start);
        debugMap.put("targetEntityCount", GrouperUtil.length(targetEntitiesResult));
      }
    }
    {
      List<ProvisioningGroup> targetGroups = targetDaoRetrieveIncementalDataRequest.getTargetGroupsForGroupMembershipSync();
      List<ProvisioningEntity> targetEntities = targetDaoRetrieveIncementalDataRequest.getTargetEntitiesForEntityMembershipSync();
      List<MultiKey> targetGroupsEntitiesMemberships = targetDaoRetrieveIncementalDataRequest.getTargetGroupsEntitiesMembershipsForMembershipSync();
      if (GrouperUtil.length(targetGroupsEntitiesMemberships) > 0 || GrouperUtil.length(targetGroups) > 0 || GrouperUtil.length(targetEntities) > 0) {
        long start = System.currentTimeMillis();
        TargetDaoRetrieveMembershipsBulkResponse retrieveMembershipsBulk = this.retrieveMembershipsBulk(new TargetDaoRetrieveMembershipsBulkRequest(targetGroups, targetEntities, targetGroupsEntitiesMemberships));
        List<ProvisioningMembership> targetMemberships = retrieveMembershipsBulk == null ? null : retrieveMembershipsBulk.getTargetMemberships();
        targetObjects.setProvisioningMemberships(targetMemberships);
        debugMap.put("retrieveTargetMshipsMillis", System.currentTimeMillis() - start);
        debugMap.put("targetMshipCount", GrouperUtil.length(targetMemberships));
      }
      //not implemented
    }
    return result;
  }

  /**
   * bulk retrieve target provisioning groups, generally use the target Ids in the targetGroups
   * @return the target provisioning groups
   */
  public TargetDaoRetrieveGroupsResponse retrieveGroups(TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest) {
    List<ProvisioningGroup> targetGroups = new ArrayList<ProvisioningGroup>();
    List<ProvisioningGroup> targetGroupsToRetrieve = targetDaoRetrieveGroupsRequest.getTargetGroups();
    for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(targetGroupsToRetrieve)) {
      TargetDaoRetrieveGroupResponse targetDaoRetrieveGroupResponse = retrieveGroup(new TargetDaoRetrieveGroupRequest(targetGroup, targetDaoRetrieveGroupsRequest.isIncludeAllMembershipsIfApplicable()));
      ProvisioningGroup targetGroupResult = targetDaoRetrieveGroupResponse == null ? null : targetDaoRetrieveGroupResponse.getTargetGroup();
      if (targetGroupResult != null) {
        targetGroups.add(targetGroupResult);
      }
    }
    return new TargetDaoRetrieveGroupsResponse(targetGroups);
  }

  /**
   * bulk retrieve target provisioning Memberships, generally use the target Ids in the targetMemberships
   * @return the target provisioning Memberships
   */
  public TargetDaoRetrieveMembershipsBulkResponse retrieveMembershipsBulk(
      TargetDaoRetrieveMembershipsBulkRequest targetDaoRetrieveMembershipsBulkRequest) {
    List<ProvisioningMembership> targetMemberships = new ArrayList<ProvisioningMembership>();
    
    List<ProvisioningGroup> targetGroups = targetDaoRetrieveMembershipsBulkRequest == null ? null : targetDaoRetrieveMembershipsBulkRequest.getTargetGroups();
     
    TargetDaoRetrieveMembershipsByGroupsResponse retrieveMembershipsByGroups = this.retrieveMembershipsByGroups(new TargetDaoRetrieveMembershipsByGroupsRequest(targetGroups));
    targetMemberships.addAll(retrieveMembershipsByGroups == null ? null : retrieveMembershipsByGroups.getTargetMemberships());
    
    List<ProvisioningEntity> targetEntities = targetDaoRetrieveMembershipsBulkRequest == null ? null : targetDaoRetrieveMembershipsBulkRequest.getTargetEntities();
    TargetDaoRetrieveMembershipsByEntitiesResponse targetDaoRetrieveMembershipsByEntitiesResponse =
        this.retrieveMembershipsByEntities(new TargetDaoRetrieveMembershipsByEntitiesRequest(targetEntities));
    List<ProvisioningMembership> targetMembershipsResult = targetDaoRetrieveMembershipsByEntitiesResponse == null ? null : targetDaoRetrieveMembershipsByEntitiesResponse.getTargetMemberships();
    targetMemberships.addAll(targetMembershipsResult);
    
    List<MultiKey> targetGroupsMembersMemberships = targetDaoRetrieveMembershipsBulkRequest == null ? 
        null : targetDaoRetrieveMembershipsBulkRequest.getTargetGroupsEntitiesMemberships();
    
    TargetDaoRetrieveMembershipsByTargetGroupEntityMembershipResponse retrieveMembershipsByTargetGroupEntityMembership = this.retrieveMembershipsByTargetGroupEntityMembership(new TargetDaoRetrieveMembershipsByTargetGroupEntityMembershipRequest(
        targetGroupsMembersMemberships));
    targetMemberships.addAll(retrieveMembershipsByTargetGroupEntityMembership == null ? null :
      retrieveMembershipsByTargetGroupEntityMembership.getTargetMemberships());
    
    return new TargetDaoRetrieveMembershipsBulkResponse(targetMemberships);
  }
  
  /**
   * bulk retrieve all target provisioning Memberships related to these groups, generally use the target Ids in the targetGroups
   * @return the target provisioning memberships
   */
  public TargetDaoRetrieveMembershipsByGroupsResponse retrieveMembershipsByGroups(TargetDaoRetrieveMembershipsByGroupsRequest targetDaoRetrieveMembershipsByGroupsRequest) {
    List<ProvisioningMembership> targetMemberships = new ArrayList<ProvisioningMembership>();
    List<ProvisioningGroup> targetGroups = targetDaoRetrieveMembershipsByGroupsRequest == null ? null :
      targetDaoRetrieveMembershipsByGroupsRequest.getTargetGroups();
    for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(targetGroups)) {
      TargetDaoRetrieveMembershipsByGroupResponse targetDaoRetrieveMembershipsByGroupResponse = retrieveMembershipsByGroup(
          new TargetDaoRetrieveMembershipsByGroupRequest(targetGroup));
      List<ProvisioningMembership> retrieveMembershipsByGroup = targetDaoRetrieveMembershipsByGroupResponse == null ? null 
          : targetDaoRetrieveMembershipsByGroupResponse.getTargetMemberships();
      List<ProvisioningMembership> targetMembershipsByGroup = retrieveMembershipsByGroup;
      targetMemberships.addAll((GrouperUtil.nonNull(targetMembershipsByGroup)));
    }
    return new TargetDaoRetrieveMembershipsByGroupsResponse(targetMemberships);
  }

  /**
   * bulk retrieve all target provisioning Memberships related to these group
   * @param targetGroup
   * @return the memberships
   */
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(
      TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * bulk retrieve target provisioning Memberships, generally use the target Ids in the targetEntities
   * @return the target provisioning memberships
   */
  public TargetDaoRetrieveMembershipsByEntitiesResponse retrieveMembershipsByEntities(
      TargetDaoRetrieveMembershipsByEntitiesRequest targetDaoRetrieveMembershipsByEntitiesRequest) {
    List<ProvisioningMembership> targetMemberships = new ArrayList<ProvisioningMembership>();
    List<ProvisioningEntity> targetEntitiesToRetrieve = targetDaoRetrieveMembershipsByEntitiesRequest == null ? null : targetDaoRetrieveMembershipsByEntitiesRequest.getTargetEntities();
    for (ProvisioningEntity targetEntity : GrouperUtil.nonNull(targetEntitiesToRetrieve)) {
      TargetDaoRetrieveMembershipsByEntityResponse targetDaoRetrieveMembershipsByEntityResponse = retrieveMembershipsByEntity(new TargetDaoRetrieveMembershipsByEntityRequest(targetEntity));
      List<ProvisioningMembership> targetMembershipsByEntity = targetDaoRetrieveMembershipsByEntityResponse == null ? null : 
          targetDaoRetrieveMembershipsByEntityResponse.getTargetMemberships();
      targetMemberships.addAll((GrouperUtil.nonNull(targetMembershipsByEntity)));
    }
    return new TargetDaoRetrieveMembershipsByEntitiesResponse(targetMemberships);
  }

  /**
   * bulk retrieve all target provisioning Memberships related to these entity
   * @param targetEntity
   * @return the memberships
   */
  public TargetDaoRetrieveMembershipsByEntityResponse retrieveMembershipsByEntity(TargetDaoRetrieveMembershipsByEntityRequest ta) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * 
   * @return the memberships
   */
  public TargetDaoRetrieveMembershipsByTargetGroupEntityMembershipResponse retrieveMembershipsByTargetGroupEntityMembership(
      TargetDaoRetrieveMembershipsByTargetGroupEntityMembershipRequest targetDaoRetrieveMembershipsByTargetGroupEntityMembershipRequest) {
    List<ProvisioningMembership> targetMembershipsToRetrieve = new ArrayList<ProvisioningMembership>();
    List<MultiKey> groupsEntitiesMemberships = targetDaoRetrieveMembershipsByTargetGroupEntityMembershipRequest == null ? null : 
      targetDaoRetrieveMembershipsByTargetGroupEntityMembershipRequest.getTargetGroupsMembersMemberships();
    for (MultiKey targetGroupEntityMembership : GrouperUtil.nonNull(groupsEntitiesMemberships)) {
      ProvisioningMembership targetMembership = (ProvisioningMembership)targetGroupEntityMembership.getKey(2);
      if (targetMembership != null) {
        targetMembershipsToRetrieve.add(targetMembership);
      }
    }
    TargetDaoRetrieveMembershipsResponse targetDaoRetrieveMembershipsResponse = this.retrieveMemberships(new TargetDaoRetrieveMembershipsRequest(targetMembershipsToRetrieve));
    List<ProvisioningMembership> retrieveMemberships = targetDaoRetrieveMembershipsResponse == null ? null : targetDaoRetrieveMembershipsResponse.getTargetMemberships();
    return new TargetDaoRetrieveMembershipsByTargetGroupEntityMembershipResponse(retrieveMemberships);
  }

  /**
   * bulk retrieve target provisioning Memberships, generally use the target Ids in the targetMemberships
   * @ptouperTargetMemberships
   * @return the target provisioning Memberships
   */
  public TargetDaoRetrieveMembershipsResponse retrieveMemberships(TargetDaoRetrieveMembershipsRequest targetDaoRetrieveMembershipsRequest) {
    List<ProvisioningMembership> targetMemberships = new ArrayList<ProvisioningMembership>();
    List<ProvisioningMembership> targetMembershipsToRetrieve = targetDaoRetrieveMembershipsRequest == null ? null : targetDaoRetrieveMembershipsRequest.getTargetMemberships();
    for (ProvisioningMembership targetMembership : GrouperUtil.nonNull(targetMembershipsToRetrieve)) {
      TargetDaoRetrieveMembershipResponse targetDaoRetrieveMembershipResponse = retrieveMembership(new TargetDaoRetrieveMembershipRequest(targetMembership));
      ProvisioningMembership targetMembershipResult = targetDaoRetrieveMembershipResponse == null ? null : targetDaoRetrieveMembershipResponse.getTargetMembership();
      if (targetMembershipResult != null) {
        targetMemberships.add(targetMembershipResult);
      }
    }
    return new TargetDaoRetrieveMembershipsResponse(targetMemberships);
  }

  /**
   * bulk retrieve target provisioning Entities, generally use the target Ids in the targetEntities
   * @param targetEntities
   * @return the target provisioning Entities
   */
  public TargetDaoRetrieveEntitiesResponse retrieveEntities(TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest) {
    List<ProvisioningEntity> targetEntities = new ArrayList<ProvisioningEntity>();
    for (ProvisioningEntity targetEntity : GrouperUtil.nonNull(targetDaoRetrieveEntitiesRequest.getTargetEntities())) {
      TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest = new TargetDaoRetrieveEntityRequest(targetEntity, targetDaoRetrieveEntitiesRequest.isIncludeAllMembershipsIfApplicable());
      TargetDaoRetrieveEntityResponse targetDaoRetrieveEntityResponse = retrieveEntity(targetDaoRetrieveEntityRequest);
      ProvisioningEntity targetEntityResult = targetDaoRetrieveEntityResponse == null ? null : targetDaoRetrieveEntityResponse.getTargetEntity();
      if (targetEntityResult != null) {
        targetEntities.add(targetEntityResult);
      }
    }
    return new TargetDaoRetrieveEntitiesResponse(targetEntities);
  }

  /**
   * return a group by target id of grouper target group, or null if not found
   * @param targetGroup
   * @return the target provisioning group or null if not found
   */
  public TargetDaoRetrieveGroupResponse retrieveGroup(TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * return a Entity by target id of grouper target Entity, or null if not found
   * @param targetEntity
   * @return the target provisioning Entity or null if not found
   */
  public TargetDaoRetrieveEntityResponse retrieveEntity(TargetDaoRetrieveEntityRequest ta) {
    throw new UnsupportedOperationException();
  }

  /**
   * return a Membership by target id of grouper target Membership, or null if not found
   * @param targetMembership
   * @return the target provisioning Membership or null if not found
   */
  public TargetDaoRetrieveMembershipResponse retrieveMembership(TargetDaoRetrieveMembershipRequest targetDaoRetrieveMembershipRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @paProvisioningGrouproup
   */
  public TargetDaoUpdateGroupResponse updateGroup(TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * insert all these groups and either throw exception for all or mark each one with an exception
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param targetGroupInserts
   */
  public TargetDaoInsertGroupsResponse insertGroups(TargetDaoInsertGroupsRequest targetDaoInsertGroupsRequest) {
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(targetDaoInsertGroupsRequest.getTargetGroups())) {
      try {
        insertGroup(new TargetDaoInsertGroupRequest(provisioningGroup));
      } catch (Exception e) {
        provisioningGroup.setException(e);
      }
    }
    return null;
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @paProvisioningEntity
   */
  public TargetDaoDeleteEntityResponse deleteEntity(TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * delete all these entities and either throw exception for all or mark each one with an exception
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param targetEntityDeletes
   */
  public TargetDaoDeleteEntitiesResponse deleteEntities(TargetDaoDeleteEntitiesRequest targetDaoDeleteEntitiesRequest) {
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(targetDaoDeleteEntitiesRequest.getTargetEntityDeletes())) {
      try {
        TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest = new TargetDaoDeleteEntityRequest();
        targetDaoDeleteEntityRequest.setTargetEntity(provisioningEntity);
        deleteEntity(targetDaoDeleteEntityRequest);
      } catch (Exception e) {
        if (provisioningEntity.getProvisioned() == null) {
          provisioningEntity.setProvisioned(false);
        }
        if (provisioningEntity.getException() == null) {
          provisioningEntity.setException(e);
        }
      }
    }
    return null;
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @paProvisioningGrouproup
   */
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * insert all these groups and either throw exception for all or mark each one with an exception
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param targetEntityInserts
   */
  public TargetDaoInsertEntitiesRequest insertEntities(TargetDaoInsertEntitiesRequest targetDaoInsertEntitiesRequest) {
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(targetDaoInsertEntitiesRequest.getTargetEntityInserts())) {
      try {
        insertEntity(new TargetDaoInsertEntityRequest(provisioningEntity));
      } catch (Exception e) {
        provisioningEntity.setException(e);
      }
    }
    return null;
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @paProvisioningEntity
   */
  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * insert all these Entities and either throw exception for all or mark each one with an exception
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param targetEntityInserts
   */
  public TargetDaoUpdateEntitiesResponse updateEntities(TargetDaoUpdateEntitiesRequest targetDaoUpdateEntitiesRequest) {
    List<ProvisioningEntity> targetEntityUpdates = targetDaoUpdateEntitiesRequest == null ? null : targetDaoUpdateEntitiesRequest.getTargetEntities();
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(targetEntityUpdates)) {
      try {
        updateEntity(new TargetDaoUpdateEntityRequest(provisioningEntity));
      } catch (Exception e) {
        provisioningEntity.setException(e);
      }
    }
    return null;
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @paProvisioningMembership
   */
  public TargetDaoDeleteMembershipResponse deleteMembership(TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * delete all these groups and either throw exception for all or mark each one with an exception
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param targetGroupDeletes
   */
  public TargetDaoDeleteGroupsResponse deleteGroups(TargetDaoDeleteGroupsRequest targetDaoDeleteGroupsRequest ) {
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(targetDaoDeleteGroupsRequest.getTargetGroupDeletes())) {
      try {
        TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest = new TargetDaoDeleteGroupRequest();
        targetDaoDeleteGroupRequest.setTargetGroup(provisioningGroup);
        deleteGroup(targetDaoDeleteGroupRequest);
      } catch (Exception e) {
        if (provisioningGroup.getProvisioned() == null) {
          provisioningGroup.setProvisioned(false);
        }
        if (provisioningGroup.getException() == null) {
          provisioningGroup.setException(e);
        }
      }
    }
    return null;
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @paProvisioningMembership
   */
  public TargetDaoInsertMembershipResponse insertMembership(TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * insert all these Memberships and either throw exception for all or mark each one with an exception
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param targetMembershipInserts
   */
  public TargetDaoInsertMembershipsResponse insertMemberships(TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(targetDaoInsertMembershipsRequest.getTargetMembershipInserts())) {
      try {
        insertMembership(new TargetDaoInsertMembershipRequest(provisioningMembership));
      } catch (Exception e) {
        provisioningMembership.setException(e);
      }
    }
    return null;
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @paProvisioningMembership
   */
  public TargetDaoUpdateMembershipResponse updateMembership(TargetDaoUpdateMembershipRequest targetDaoUpdateMembershipRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * update all these Memberships and either throw exception for all or mark each one with an exception
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param targetGroupInserts
   */
  public TargetDaoUpdateMembershipsResponse updateMemberships(TargetDaoUpdateMembershipsRequest targetDaoUpdateMembershipsRequest) {
    List<ProvisioningMembership> targetMemberships = targetDaoUpdateMembershipsRequest == null ? null : targetDaoUpdateMembershipsRequest.getTargetMemberships();
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(targetMemberships)) {
      try {
        updateMembership(new TargetDaoUpdateMembershipRequest(provisioningMembership));
      } catch (Exception e) {
        provisioningMembership.setException(e);
      }
    }
    return null;
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param ta
   * @return 
   */
  public TargetDaoSendEntityChangesToTargetResponse sendEntityChangesToTarget(TargetDaoSendEntityChangesToTargetRequest targetDaoSendEntityChangesToTargetRequest) {
    List<ProvisioningEntity> targetEntityDeletes = targetDaoSendEntityChangesToTargetRequest.getTargetEntityDeletes();
    
    {
      TargetDaoDeleteEntitiesRequest targetDaoDeleteEntitiesRequest = new TargetDaoDeleteEntitiesRequest();
      targetDaoDeleteEntitiesRequest.setTargetEntityDeletes(targetEntityDeletes);
      this.deleteEntities(targetDaoDeleteEntitiesRequest);
    }
    
    List<ProvisioningEntity> targetEntityInserts = targetDaoSendEntityChangesToTargetRequest.getTargetEntityInserts();
    
    this.insertEntities(new TargetDaoInsertEntitiesRequest(targetEntityInserts));
    
    List<ProvisioningEntity> targetEntityUpdates = targetDaoSendEntityChangesToTargetRequest.getTargetEntityUpdates();
    
    this.updateEntities(new TargetDaoUpdateEntitiesRequest(targetEntityUpdates));
    
    return null;
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param targetDaoSendMembershipChangesToTargetRequest
   * @return
   */
  public TargetDaoSendMembershipChangesToTargetResponse sendMembershipChangesToTarget(
      TargetDaoSendMembershipChangesToTargetRequest targetDaoSendMembershipChangesToTargetRequest) {
    
    {
      List<ProvisioningMembership> targetMembershipDeletes = targetDaoSendMembershipChangesToTargetRequest.getTargetMembershipDeletes();
      TargetDaoDeleteMembershipsRequest targetDaoDeleteMembershipsRequest = new TargetDaoDeleteMembershipsRequest();
      targetDaoDeleteMembershipsRequest.setTargetMembershipDeletes(targetMembershipDeletes);
      this.deleteMemberships(targetDaoDeleteMembershipsRequest);
    }

    {
      List<ProvisioningMembership> targetMembershipInserts = targetDaoSendMembershipChangesToTargetRequest.getTargetMembershipInserts();
      
      this.insertMemberships(new TargetDaoInsertMembershipsRequest(targetMembershipInserts));
    }
    
    List<ProvisioningMembership> targetMembershipUpdates = targetDaoSendMembershipChangesToTargetRequest.getTargetMembershipUpdates();
    
    this.updateMemberships(new TargetDaoUpdateMembershipsRequest(targetMembershipUpdates));
    return null;
  }
  
}
