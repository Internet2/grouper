/**
 * 
 */
package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLists;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * wraps the dao so it can convert methods and see if things are available
 * @author mchyzer-local
 *
 */
public class GrouperProvisionerTargetDaoAdapter extends GrouperProvisionerTargetDaoBase {

  private GrouperProvisionerTargetDaoBase wrappedDao;

  
  
  
  @Override
  public GrouperProvisionerDaoCapabilities getGrouperProvisionerDaoCapabilities() {
    return this.wrappedDao.getGrouperProvisionerDaoCapabilities();
  }


  @Override
  public void setGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    this.wrappedDao.setGrouperProvisionerDaoCapabilities(grouperProvisionerDaoCapabilities);
  }


  @Override
  public void addTargetDaoTimingInfo(TargetDaoTimingInfo targetDaoTimingInfo) {
    this.wrappedDao.addTargetDaoTimingInfo(targetDaoTimingInfo);
  }


  @Override
  public List<TargetDaoTimingInfo> getTargetDaoTimingInfos() {
    return this.wrappedDao.getTargetDaoTimingInfos();
  }


  @Override
  public void setTargetDaoTimingInfos(List<TargetDaoTimingInfo> targetDaoTimingInfos) {
    this.wrappedDao.setTargetDaoTimingInfos(targetDaoTimingInfos);
  }


  public GrouperProvisionerTargetDaoBase getWrappedDao() {
    return wrappedDao;
  }

  
  public void setWrappedDao(GrouperProvisionerTargetDaoBase wrappedDao) {
    this.wrappedDao = wrappedDao;
  }

  public GrouperProvisionerTargetDaoAdapter(GrouperProvisioner grouperProvisioner, GrouperProvisionerTargetDaoBase wrappedDao) {
    super();
    super.setGrouperProvisioner(grouperProvisioner);
    this.wrappedDao = wrappedDao;
  }

  /**
   * 
   */
  public GrouperProvisionerTargetDaoAdapter() {
  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    // this probably would never be called here, but just delegate just in case
    this.wrappedDao.registerGrouperProvisionerDaoCapabilities(grouperProvisionerDaoCapabilities);
  }


  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(
      TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveAllGroups(), false)) {
      return this.wrappedDao.retrieveAllGroups(targetDaoRetrieveAllGroupsRequest);
    }
    throw new RuntimeException("Dao cannot retrieve all groups");
  }


  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(
      TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveAllEntities(), false)) {
      return this.wrappedDao.retrieveAllEntities(targetDaoRetrieveAllEntitiesRequest);
    }
    
    throw new RuntimeException("Dao cannot retrieve all entities");
    
  }


  @Override
  public TargetDaoRetrieveAllMembershipsResponse retrieveAllMemberships(
      TargetDaoRetrieveAllMembershipsRequest targetDaoRetrieveAllMembershipsRequest) {
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveAllMemberships(), false)) {
      return this.wrappedDao.retrieveAllMemberships(targetDaoRetrieveAllMembershipsRequest);
    }

    throw new RuntimeException("Dao cannot retrieve all memberships");
  }


  @Override
  public TargetDaoDeleteGroupResponse deleteGroup(
      TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {
    
    if (targetDaoDeleteGroupRequest.getTargetGroup() == null) {
      return new TargetDaoDeleteGroupResponse();
    }

    ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest.getTargetGroup();
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteGroup(), false)) {
      try {
        TargetDaoDeleteGroupResponse targetDaoDeleteGroupResponse = this.wrappedDao.deleteGroup(targetDaoDeleteGroupRequest);
        if (targetGroup.getProvisioned() == null) {
          throw new RuntimeException("Dao did not set deleted group as provisioned: " + this.wrappedDao);
        }
        return targetDaoDeleteGroupResponse;
      } catch (RuntimeException e) {
        
        if (targetGroup.getProvisioned() == null) {
          targetGroup.setProvisioned(false);
        }
        if (targetGroup.getException() == null) {
          GrouperUtil.injectInException(e, targetGroup.toString());
          targetGroup.setException(e);
        }
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteGroups(), false)) {
      this.deleteGroups(new TargetDaoDeleteGroupsRequest(GrouperUtil.toList(targetGroup)));
      return null;
    }

    throw new RuntimeException("Dao cannot delete group or groups");
  }


  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();

    if (targetGroup == null) {
      return new TargetDaoInsertGroupResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertGroup(), false)) {
      try {
        TargetDaoInsertGroupResponse targetDaoInsertGroupResponse = this.wrappedDao.insertGroup(targetDaoInsertGroupRequest);
        if (targetGroup.getProvisioned() == null) {
          throw new RuntimeException("Dao did not set inserted group as provisioned: " + this.wrappedDao);
        }
        return targetDaoInsertGroupResponse;
      } catch (RuntimeException e) {
        
        if (targetGroup.getProvisioned() == null) {
          targetGroup.setProvisioned(false);
        }
        if (targetGroup.getException() == null) {
          GrouperUtil.injectInException(e, targetGroup.toString());
          targetGroup.setException(e);
        }
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertGroups(), false)) {
      this.insertGroups(new TargetDaoInsertGroupsRequest(GrouperUtil.toList(targetGroup)));
      return null;
    }

    throw new RuntimeException("Dao cannot insert group or groups");

  }

  @Override
  public TargetDaoSendChangesToTargetResponse sendChangesToTarget(
      TargetDaoSendChangesToTargetRequest targetDaoSendChangesToTargetRequest) {
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanSendChangesToTarget(), false)) {
      return this.wrappedDao.sendChangesToTarget(targetDaoSendChangesToTargetRequest);
    }
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


  @Override
  public TargetDaoSendGroupChangesToTargetResponse sendGroupChangesToTarget(
      TargetDaoSendGroupChangesToTargetRequest targetDaoSendGroupChangesToTargetRequest) {

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanSendGroupChangesToTarget(), false)) {
      return this.wrappedDao.sendGroupChangesToTarget(targetDaoSendGroupChangesToTargetRequest);
    }
    if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGroupsDeleteIfDeletedFromGrouper(), false)) {
      List<ProvisioningGroup> targetGroupDeletes = targetDaoSendGroupChangesToTargetRequest.getTargetGroupDeletes();
      TargetDaoDeleteGroupsRequest targetDaoDeleteGroupsRequest = new TargetDaoDeleteGroupsRequest();
      targetDaoDeleteGroupsRequest.setTargetGroups(targetGroupDeletes);
      this.deleteGroups(targetDaoDeleteGroupsRequest);
    }

    if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGroupsInsert(), false)) {
      List<ProvisioningGroup> targetGroupInserts = targetDaoSendGroupChangesToTargetRequest.getTargetGroupInserts();
      
      this.insertGroups(new TargetDaoInsertGroupsRequest(targetGroupInserts));
    }
    if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGroupsUpdate(), false)) {
      List<ProvisioningGroup> targetGroupUpdates = targetDaoSendGroupChangesToTargetRequest.getTargetGroupUpdates();
      
      this.updateGroups(new TargetDaoUpdateGroupsRequest(targetGroupUpdates));
    }
    return null;

  }


  @Override
  public TargetDaoUpdateGroupsResponse updateGroups(
      TargetDaoUpdateGroupsRequest targetDaoUpdateGroupsRequest) {
    
    if (GrouperUtil.length(targetDaoUpdateGroupsRequest.getTargetGroups()) == 0) {
      return new TargetDaoUpdateGroupsResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateGroups(), false)) {
      try {
        TargetDaoUpdateGroupsResponse targetDaoUpdateGroupsResponse = this.wrappedDao.updateGroups(targetDaoUpdateGroupsRequest);
        for (ProvisioningGroup provisioningGroup : targetDaoUpdateGroupsRequest.getTargetGroups()) { 
          if (provisioningGroup.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set updated group as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoUpdateGroupsResponse;
      } catch (RuntimeException e) {
        for (ProvisioningGroup targetGroup : targetDaoUpdateGroupsRequest.getTargetGroups()) { 
          
          if (targetGroup.getProvisioned() == null) {
            targetGroup.setProvisioned(false);
          }
          if (targetGroup.getException() == null) {
            GrouperUtil.injectInException(e, targetGroup.toString());
            targetGroup.setException(e);
          }
        }
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateGroup(), false)) {
      for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(targetDaoUpdateGroupsRequest.getTargetGroups())) {
        updateGroup(new TargetDaoUpdateGroupRequest(provisioningGroup));
      }
      return null;
    }

    throw new RuntimeException("Dao cannot update group or groups");
  }


  @Override
  public TargetDaoDeleteMembershipsResponse deleteMemberships(
      TargetDaoDeleteMembershipsRequest targetDaoDeleteMembershipsRequest) {

    if (GrouperUtil.length(targetDaoDeleteMembershipsRequest.getTargetMemberships()) == 0) {
      return new TargetDaoDeleteMembershipsResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteMemberships(), false)) {
      try {
        TargetDaoDeleteMembershipsResponse targetDaoDeleteMembershipsResponse = this.wrappedDao.deleteMemberships(targetDaoDeleteMembershipsRequest);
        for (ProvisioningMembership provisioningMembership : targetDaoDeleteMembershipsRequest.getTargetMemberships()) { 
          if (provisioningMembership.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set deleted membership as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoDeleteMembershipsResponse;
      } catch (RuntimeException e) {
        for (ProvisioningMembership targetMembership : targetDaoDeleteMembershipsRequest.getTargetMemberships()) { 
          
          if (targetMembership.getProvisioned() == null) {
            targetMembership.setProvisioned(false);
          }
          if (targetMembership.getException() == null) {
            GrouperUtil.injectInException(e, targetMembership.toString());
            targetMembership.setException(e);
          }
        }
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteMembership(), false)) {
      for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(targetDaoDeleteMembershipsRequest.getTargetMemberships())) {
        deleteMembership(new TargetDaoDeleteMembershipRequest(provisioningMembership));
      }
      return null;
    }

    throw new RuntimeException("Dao cannot delete membership or memberships");
  }


  @Override
  public TargetDaoRetrieveAllDataResponse retrieveAllData(
      TargetDaoRetrieveAllDataRequest targetDaoRetrieveAllDataRequest) {

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveAllData(), false)) {
      return this.wrappedDao.retrieveAllData(targetDaoRetrieveAllDataRequest);
    }

    GrouperProvisioningLists targetObjects = new GrouperProvisioningLists();
    TargetDaoRetrieveAllDataResponse result = new TargetDaoRetrieveAllDataResponse(targetObjects);
    
    if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGroupsRetrieveAll(), false)) {
      
      TargetDaoRetrieveAllGroupsResponse targetDaoRetrieveAllGroupsResponse = this.retrieveAllGroups(new TargetDaoRetrieveAllGroupsRequest(true));
      List<ProvisioningGroup> targetGroups = targetDaoRetrieveAllGroupsResponse == null ? null : targetDaoRetrieveAllGroupsResponse.getTargetGroups();
      targetObjects.setProvisioningGroups(targetGroups);

    }
    
    if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getEntitiesRetrieveAll(), false)) {
      
      TargetDaoRetrieveAllEntitiesResponse targetDaoRetrieveAllEntitiesResponse = this.retrieveAllEntities(new TargetDaoRetrieveAllEntitiesRequest(true));
      List<ProvisioningEntity> targetEntities = targetDaoRetrieveAllEntitiesResponse == null ? null : targetDaoRetrieveAllEntitiesResponse.getTargetEntities();
      targetObjects.setProvisioningEntities(targetEntities);

    }
    
    if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getMembershipsRetrieveAll(), false)) {
      
      TargetDaoRetrieveAllMembershipsResponse targetDaoRetrieveAllMembershipsResponse = this.retrieveAllMemberships(new TargetDaoRetrieveAllMembershipsRequest());
      List<ProvisioningMembership> targetMemberships = targetDaoRetrieveAllMembershipsResponse == null ? null : targetDaoRetrieveAllMembershipsResponse.getTargetMemberships();
      targetObjects.setProvisioningMemberships(targetMemberships);

    }
    return result;
  }


  @Override
  public TargetDaoRetrieveIncrementalDataResponse retrieveIncrementalData(
      TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncementalDataRequest) {
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveIncrementalData(), false)) {
      return this.wrappedDao.retrieveIncrementalData(targetDaoRetrieveIncementalDataRequest);
    }
    TargetDaoRetrieveIncrementalDataResponse result = new TargetDaoRetrieveIncrementalDataResponse();
    if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGroupsRetrieve(), false)) {
      List<ProvisioningGroup> targetGroups = null;
      targetGroups = targetDaoRetrieveIncementalDataRequest.getTargetGroupsForGroupOnly();
      if (GrouperUtil.length(targetGroups) > 0) {
        // if there are groups then this must be implemented
        TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = this.retrieveGroups(
            new TargetDaoRetrieveGroupsRequest(targetGroups, false));
        List<ProvisioningGroup> targetGroupsResult = targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups();
        result.setProvisioningGroups(targetGroupsResult);
      }
    }
    if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getEntitiesRetrieve(), false)) {
      List<ProvisioningEntity> targetEntities = null;
      targetEntities = targetDaoRetrieveIncementalDataRequest.getTargetEntitiesForEntityOnly();
      if (GrouperUtil.length(targetEntities) > 0) {
        TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = this.retrieveEntities(
            new TargetDaoRetrieveEntitiesRequest(targetEntities, false));
        List<ProvisioningEntity> targetEntitiesResult = targetDaoRetrieveEntitiesResponse == null ?
            null : targetDaoRetrieveEntitiesResponse.getTargetEntities();
        result.setProvisioningEntities(targetEntitiesResult);
      }
    }
    if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getMembershipsRetrieve(), false)) {
      List<ProvisioningGroup> targetGroups = targetDaoRetrieveIncementalDataRequest.getTargetGroupsForGroupMembershipSync();
      List<ProvisioningEntity> targetEntities = targetDaoRetrieveIncementalDataRequest.getTargetEntitiesForEntityMembershipSync();
      List<Object> targetGroupsEntitiesMemberships = targetDaoRetrieveIncementalDataRequest.getTargetMembershipObjectsForMembershipSync();
      if (GrouperUtil.length(targetGroupsEntitiesMemberships) > 0 || GrouperUtil.length(targetGroups) > 0 || GrouperUtil.length(targetEntities) > 0) {
        TargetDaoRetrieveMembershipsBulkResponse retrieveMembershipsBulk = this.retrieveMembershipsBulk(new TargetDaoRetrieveMembershipsBulkRequest(targetGroups, targetEntities, targetGroupsEntitiesMemberships));
        List<Object> targetMemberships = retrieveMembershipsBulk == null ? null : retrieveMembershipsBulk.getTargetMemberships();
        result.setProvisioningMemberships(targetMemberships);
      }
    }
    return result;

  }


  @Override
  public TargetDaoRetrieveGroupsResponse retrieveGroups(
      TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest) {
    
    if (GrouperUtil.length(targetDaoRetrieveGroupsRequest.getTargetGroups()) == 0) {
      return new TargetDaoRetrieveGroupsResponse();
    }

    for (ProvisioningGroup provisioningGroup : targetDaoRetrieveGroupsRequest.getTargetGroups()) {
      provisioningGroup.assignSearchFilter();
    }
    
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveGroups(), false)) {
      return this.wrappedDao.retrieveGroups(targetDaoRetrieveGroupsRequest);
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveGroup(), false)) {
      
      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();
      
      for (ProvisioningGroup provisioningGroup : targetDaoRetrieveGroupsRequest.getTargetGroups()) {

        TargetDaoRetrieveGroupResponse targetDaoRetrieveGroupResponse = this.wrappedDao.retrieveGroup(new TargetDaoRetrieveGroupRequest(provisioningGroup, targetDaoRetrieveGroupsRequest.isIncludeAllMembershipsIfApplicable()));
        if (targetDaoRetrieveGroupResponse != null && targetDaoRetrieveGroupResponse.getTargetGroup() != null) {
          results.add(targetDaoRetrieveGroupResponse.getTargetGroup());
        }
      }
      return new TargetDaoRetrieveGroupsResponse(results);
    }

    throw new RuntimeException("Dao cannot retrieve groups or group");

  }


  @Override
  public TargetDaoRetrieveMembershipsBulkResponse retrieveMembershipsBulk(
      TargetDaoRetrieveMembershipsBulkRequest targetDaoRetrieveMembershipsBulkRequest) {
    
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsBulk(), false)) {
      return this.wrappedDao.retrieveMembershipsBulk(targetDaoRetrieveMembershipsBulkRequest);
    }

    List<Object> targetMembershipsResults = new ArrayList<Object>();
    
    List<ProvisioningGroup> targetGroups = targetDaoRetrieveMembershipsBulkRequest == null ? null : targetDaoRetrieveMembershipsBulkRequest.getTargetGroupsForAllMemberships();
    
    if (GrouperUtil.length(targetGroups) > 0) {
      TargetDaoRetrieveMembershipsByGroupsResponse retrieveMembershipsByGroups = this.retrieveMembershipsByGroups(new TargetDaoRetrieveMembershipsByGroupsRequest(targetGroups));
      targetMembershipsResults.addAll(retrieveMembershipsByGroups == null ? null : retrieveMembershipsByGroups.getTargetMemberships());
    }
    
    List<ProvisioningEntity> targetEntities = targetDaoRetrieveMembershipsBulkRequest == null ? null : targetDaoRetrieveMembershipsBulkRequest.getTargetEntitiesForAllMemberships();
    if (GrouperUtil.length(targetEntities) > 0) {
      TargetDaoRetrieveMembershipsByEntitiesResponse targetDaoRetrieveMembershipsByEntitiesResponse =
          this.retrieveMembershipsByEntities(new TargetDaoRetrieveMembershipsByEntitiesRequest(targetEntities));
      List<Object> targetMembershipsResult = targetDaoRetrieveMembershipsByEntitiesResponse == null ? null : targetDaoRetrieveMembershipsByEntitiesResponse.getTargetMemberships();
      targetMembershipsResults.addAll(targetMembershipsResult);
    }
    
    List<Object> targetMembershipsInput = targetDaoRetrieveMembershipsBulkRequest == null ? 
        null : targetDaoRetrieveMembershipsBulkRequest.getTargetMemberships();
    if (GrouperUtil.length(targetMembershipsInput) > 0) {
      
      TargetDaoRetrieveMembershipsResponse retrieveMembershipsResponse 
        = this.retrieveMemberships(new TargetDaoRetrieveMembershipsRequest(
          targetMembershipsInput));
      targetMembershipsResults.addAll(retrieveMembershipsResponse == null ? null :
        retrieveMembershipsResponse.getTargetMemberships());
    }
    
    return new TargetDaoRetrieveMembershipsBulkResponse(targetMembershipsResults);
  }


  @Override
  public TargetDaoRetrieveMembershipsByGroupsResponse retrieveMembershipsByGroups(
      TargetDaoRetrieveMembershipsByGroupsRequest targetDaoRetrieveMembershipsByGroupsRequest) {
    
    if (GrouperUtil.length(targetDaoRetrieveMembershipsByGroupsRequest.getTargetGroups()) == 0) {
      return new TargetDaoRetrieveMembershipsByGroupsResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByGroups(), false)) {
      return this.wrappedDao.retrieveMembershipsByGroups(targetDaoRetrieveMembershipsByGroupsRequest);
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByGroup(), false)) {
      
      List<Object> results = new ArrayList<Object>();
      
      for (ProvisioningGroup provisioningGroup : targetDaoRetrieveMembershipsByGroupsRequest.getTargetGroups()) {

        TargetDaoRetrieveMembershipsByGroupResponse targetDaoRetrieveMembershipsByGroupResponse = this.wrappedDao.retrieveMembershipsByGroup(
            new TargetDaoRetrieveMembershipsByGroupRequest(provisioningGroup));
        results.addAll(GrouperUtil.nonNull(targetDaoRetrieveMembershipsByGroupResponse.getTargetMemberships()));
      }
      return new TargetDaoRetrieveMembershipsByGroupsResponse(results);

    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = this.retrieveGroups(
          new TargetDaoRetrieveGroupsRequest(targetDaoRetrieveMembershipsByGroupsRequest.getTargetGroups(), true));
      return new TargetDaoRetrieveMembershipsByGroupsResponse((List<Object>)(Object)targetDaoRetrieveGroupsResponse.getTargetGroups());
    }
    
    throw new RuntimeException("Dao cannot retrieve memberships by group or groups");
  }


  @Override
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(
      TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
    
    if (targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup() == null) {
      return new TargetDaoRetrieveMembershipsByGroupResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByGroup(), false)) {
      return this.wrappedDao.retrieveMembershipsByGroup(targetDaoRetrieveMembershipsByGroupRequest);
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByGroups(), false)) {

      TargetDaoRetrieveMembershipsByGroupsRequest targetDaoRetrieveMembershipsByGroupsRequest = 
          new TargetDaoRetrieveMembershipsByGroupsRequest(GrouperUtil.toList(targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup()));
      
      TargetDaoRetrieveMembershipsByGroupsResponse targetDaoRetrieveMembershipsByGroupsResponse 
        = this.wrappedDao.retrieveMembershipsByGroups(targetDaoRetrieveMembershipsByGroupsRequest);

      return new TargetDaoRetrieveMembershipsByGroupResponse(targetDaoRetrieveMembershipsByGroupsResponse.getTargetMemberships());
    }

    throw new RuntimeException("Dao cannot retrieve memberships by group or groups");
  }


  @Override
  public TargetDaoRetrieveMembershipsByEntitiesResponse retrieveMembershipsByEntities(
      TargetDaoRetrieveMembershipsByEntitiesRequest targetDaoRetrieveMembershipsByEntitiesRequest) {
    
    if (GrouperUtil.length(targetDaoRetrieveMembershipsByEntitiesRequest.getTargetEntities()) == 0) {
      return new TargetDaoRetrieveMembershipsByEntitiesResponse();
    }
   
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByEntities(), false)) {
      return this.wrappedDao.retrieveMembershipsByEntities(targetDaoRetrieveMembershipsByEntitiesRequest);
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByEntity(), false)) {
      
      List<Object> results = new ArrayList<Object>();
      
      for (ProvisioningEntity provisioningEntity : targetDaoRetrieveMembershipsByEntitiesRequest.getTargetEntities()) {

        TargetDaoRetrieveMembershipsByEntityResponse targetDaoRetrieveMembershipsByEntityResponse = this.wrappedDao.retrieveMembershipsByEntity(
            new TargetDaoRetrieveMembershipsByEntityRequest(provisioningEntity));
        results.addAll(GrouperUtil.nonNull(targetDaoRetrieveMembershipsByEntityResponse.getTargetMemberships()));
      }
      return new TargetDaoRetrieveMembershipsByEntitiesResponse(results);

    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = this.retrieveEntities(
          new TargetDaoRetrieveEntitiesRequest(targetDaoRetrieveMembershipsByEntitiesRequest.getTargetEntities(), true));
      return new TargetDaoRetrieveMembershipsByEntitiesResponse((List<Object>)(Object)targetDaoRetrieveEntitiesResponse.getTargetEntities());
    }

    throw new RuntimeException("Dao cannot retrieve memberships by entity or entities");
  }


  @Override
  public TargetDaoRetrieveMembershipsByEntityResponse retrieveMembershipsByEntity(
      TargetDaoRetrieveMembershipsByEntityRequest targetDaoRetrieveMembershipsByEntityRequest) {
    
    if (targetDaoRetrieveMembershipsByEntityRequest.getTargetEntity() == null) {
      return new TargetDaoRetrieveMembershipsByEntityResponse();
    }
    
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByEntity(), false)) {
      return this.wrappedDao.retrieveMembershipsByEntity(targetDaoRetrieveMembershipsByEntityRequest);
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByEntities(), false)) {

      TargetDaoRetrieveMembershipsByEntitiesRequest targetDaoRetrieveMembershipsByEntitiesRequest = 
          new TargetDaoRetrieveMembershipsByEntitiesRequest(GrouperUtil.toList(targetDaoRetrieveMembershipsByEntityRequest.getTargetEntity()));
      
      TargetDaoRetrieveMembershipsByEntitiesResponse targetDaoRetrieveMembershipsByEntitiesResponse 
        = this.wrappedDao.retrieveMembershipsByEntities(targetDaoRetrieveMembershipsByEntitiesRequest);

      return new TargetDaoRetrieveMembershipsByEntityResponse(targetDaoRetrieveMembershipsByEntitiesResponse.getTargetMemberships());
    }

    throw new RuntimeException("Dao cannot retrieve memberships by entity or entities");
  }


  @Override
  public TargetDaoRetrieveMembershipsResponse retrieveMemberships(
      TargetDaoRetrieveMembershipsRequest targetDaoRetrieveMembershipsRequest) {
    
    if (GrouperUtil.length(targetDaoRetrieveMembershipsRequest.getTargetMemberships()) == 0) {
      return new TargetDaoRetrieveMembershipsResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMemberships(), false)) {
      return this.wrappedDao.retrieveMemberships(targetDaoRetrieveMembershipsRequest);
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembership(), false)) {
      
      List<Object> results = new ArrayList<Object>();
      
      for (Object provisioningMembership : targetDaoRetrieveMembershipsRequest.getTargetMemberships()) {

        TargetDaoRetrieveMembershipResponse targetDaoRetrieveMembershipResponse = this.wrappedDao.retrieveMembership(new TargetDaoRetrieveMembershipRequest(provisioningMembership));
        if (targetDaoRetrieveMembershipResponse != null && targetDaoRetrieveMembershipResponse.getTargetMembership() != null) {
          results.add(targetDaoRetrieveMembershipResponse.getTargetMembership());
        }
      }
      return new TargetDaoRetrieveMembershipsResponse(results);
    }

    throw new RuntimeException("Dao cannot retrieve memberships or membership");
  }


  @Override
  public TargetDaoRetrieveEntitiesResponse retrieveEntities(
      TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest) {
    
    if (GrouperUtil.length(targetDaoRetrieveEntitiesRequest.getTargetEntities()) == 0) {
      return new TargetDaoRetrieveEntitiesResponse();
    }

    for (ProvisioningEntity provisioningEntity : targetDaoRetrieveEntitiesRequest.getTargetEntities()) {
      provisioningEntity.assignSearchFilter();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveEntities(), false)) {
      return this.wrappedDao.retrieveEntities(targetDaoRetrieveEntitiesRequest);
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveEntity(), false)) {
      
      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();
      
      for (ProvisioningEntity provisioningEntity : targetDaoRetrieveEntitiesRequest.getTargetEntities()) {

        TargetDaoRetrieveEntityResponse targetDaoRetrieveEntityResponse = this.wrappedDao.retrieveEntity(new TargetDaoRetrieveEntityRequest(provisioningEntity, targetDaoRetrieveEntitiesRequest.isIncludeAllMembershipsIfApplicable()));
        if (targetDaoRetrieveEntityResponse != null && targetDaoRetrieveEntityResponse.getTargetEntity() != null) {
          results.add(targetDaoRetrieveEntityResponse.getTargetEntity());
        }
      }
      return new TargetDaoRetrieveEntitiesResponse(results);
    }

    throw new RuntimeException("Dao cannot retrieve entities or entity");
    
  }


  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(
      TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {
    
    if (targetDaoRetrieveGroupRequest.getTargetGroup() == null) {
      return new TargetDaoRetrieveGroupResponse();
    }

    targetDaoRetrieveGroupRequest.getTargetGroup().assignSearchFilter();
    
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveGroup(), false)) {
      return this.wrappedDao.retrieveGroup(targetDaoRetrieveGroupRequest);
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveGroups(), false)) {
      
      TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = 
          this.wrappedDao.retrieveGroups(new TargetDaoRetrieveGroupsRequest(GrouperUtil.toList(targetDaoRetrieveGroupRequest.getTargetGroup()), 
          targetDaoRetrieveGroupRequest.isIncludeAllMembershipsIfApplicable()));

      return new TargetDaoRetrieveGroupResponse(targetDaoRetrieveGroupsResponse == null ? null :
        GrouperUtil.length(targetDaoRetrieveGroupsResponse.getTargetGroups()) == 0 ? null : 
            targetDaoRetrieveGroupsResponse.getTargetGroups().get(0));
    }

    throw new RuntimeException("Dao cannot retrieve groups or group");
  }


  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(
      TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {
    
    if (targetDaoRetrieveEntityRequest.getTargetEntity() == null) {
      return new TargetDaoRetrieveEntityResponse();
    }

    targetDaoRetrieveEntityRequest.getTargetEntity().assignSearchFilter();

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveEntity(), false)) {
      return this.wrappedDao.retrieveEntity(targetDaoRetrieveEntityRequest);
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveEntities(), false)) {
      
      TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = 
          this.wrappedDao.retrieveEntities(new TargetDaoRetrieveEntitiesRequest(GrouperUtil.toList(targetDaoRetrieveEntityRequest.getTargetEntity()), 
          targetDaoRetrieveEntityRequest.isIncludeAllMembershipsIfApplicable()));

      return new TargetDaoRetrieveEntityResponse(targetDaoRetrieveEntitiesResponse == null ? null :
        GrouperUtil.length(targetDaoRetrieveEntitiesResponse.getTargetEntities()) == 0 ? null : 
            targetDaoRetrieveEntitiesResponse.getTargetEntities().get(0));
    }

    throw new RuntimeException("Dao cannot retrieve entities or entity");
  }


  @Override
  public TargetDaoRetrieveMembershipResponse retrieveMembership(
      TargetDaoRetrieveMembershipRequest targetDaoRetrieveMembershipRequest) {
    
    if (targetDaoRetrieveMembershipRequest.getTargetMembership() == null) {
      return new TargetDaoRetrieveMembershipResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMembership(), false)) {
      return this.wrappedDao.retrieveMembership(targetDaoRetrieveMembershipRequest);
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanRetrieveMemberships(), false)) {
      
      TargetDaoRetrieveMembershipsResponse targetDaoRetrieveMembershipsResponse = 
          this.wrappedDao.retrieveMemberships(new TargetDaoRetrieveMembershipsRequest(GrouperUtil.toList(targetDaoRetrieveMembershipRequest.getTargetMembership())));

      return new TargetDaoRetrieveMembershipResponse(targetDaoRetrieveMembershipsResponse == null ? null :
        GrouperUtil.length(targetDaoRetrieveMembershipsResponse.getTargetMemberships()) == 0 ? null : 
            targetDaoRetrieveMembershipsResponse.getTargetMemberships().get(0));
    }

    throw new RuntimeException("Dao cannot retrieve memberships or membership");
  }


  @Override
  public TargetDaoUpdateGroupResponse updateGroup(
      TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {
    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();
    
    if (targetGroup == null) {
      return new TargetDaoUpdateGroupResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateGroup(), false)) {
      try {
        TargetDaoUpdateGroupResponse targetDaoUpdateGroupResponse = this.wrappedDao.updateGroup(targetDaoUpdateGroupRequest);
        if (targetGroup.getProvisioned() == null) {
          throw new RuntimeException("Dao did not set updated group as provisioned: " + this.wrappedDao);
        }
        return targetDaoUpdateGroupResponse;
      } catch (RuntimeException e) {
        
        if (targetGroup.getProvisioned() == null) {
          targetGroup.setProvisioned(false);
        }
        if (targetGroup.getException() == null) {
          GrouperUtil.injectInException(e, targetGroup.toString());
          targetGroup.setException(e);
        }
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateGroups(), false)) {
      this.updateGroups(new TargetDaoUpdateGroupsRequest(GrouperUtil.toList(targetGroup)));
      return null;
    }

    throw new RuntimeException("Dao cannot update group or groups");
  }


  @Override
  public TargetDaoInsertGroupsResponse insertGroups(
      TargetDaoInsertGroupsRequest targetDaoInsertGroupsRequest) {
    
    if (GrouperUtil.length(targetDaoInsertGroupsRequest.getTargetGroups()) == 0) {
      return new TargetDaoInsertGroupsResponse();
    }
    
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertGroups(), false)) {
      try {
        TargetDaoInsertGroupsResponse targetDaoInsertGroupsResponse = this.wrappedDao.insertGroups(targetDaoInsertGroupsRequest);
        for (ProvisioningGroup provisioningGroup : targetDaoInsertGroupsRequest.getTargetGroups()) { 
          if (provisioningGroup.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set inserted group as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoInsertGroupsResponse;
      } catch (RuntimeException e) {
        for (ProvisioningGroup targetGroup : targetDaoInsertGroupsRequest.getTargetGroups()) { 
          
          if (targetGroup.getProvisioned() == null) {
            targetGroup.setProvisioned(false);
          }
          if (targetGroup.getException() == null) {
            GrouperUtil.injectInException(e, targetGroup.toString());
            targetGroup.setException(e);
          }
        }
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertGroup(), false)) {
      for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(targetDaoInsertGroupsRequest.getTargetGroups())) {
        insertGroup(new TargetDaoInsertGroupRequest(provisioningGroup));
      }
      return null;
    }

    throw new RuntimeException("Dao cannot insert group or groups");
  }


  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(
      TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {
    
    if (targetDaoDeleteEntityRequest.getTargetEntity() == null) {
      return new TargetDaoDeleteEntityResponse();
    }

    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteEntity(), false)) {
      try {
        TargetDaoDeleteEntityResponse targetDaoDeleteEntityResponse = this.wrappedDao.deleteEntity(targetDaoDeleteEntityRequest);
        if (targetEntity.getProvisioned() == null) {
          throw new RuntimeException("Dao did not set deleted entity as provisioned: " + this.wrappedDao);
        }
        return targetDaoDeleteEntityResponse;
      } catch (RuntimeException e) {
        
        if (targetEntity.getProvisioned() == null) {
          targetEntity.setProvisioned(false);
        }
        if (targetEntity.getException() == null) {
          GrouperUtil.injectInException(e, targetEntity.toString());
          targetEntity.setException(e);
        }
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteEntities(), false)) {
      this.deleteEntities(new TargetDaoDeleteEntitiesRequest(GrouperUtil.toList(targetEntity)));
      return null;
    }

    throw new RuntimeException("Dao cannot insert entity or entities");

  }


  @Override
  public TargetDaoDeleteEntitiesResponse deleteEntities(
      TargetDaoDeleteEntitiesRequest targetDaoDeleteEntitiesRequest) {

    if (GrouperUtil.length(targetDaoDeleteEntitiesRequest.getTargetEntities()) == 0) {
      return new TargetDaoDeleteEntitiesResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteEntities(), false)) {
      try {
        TargetDaoDeleteEntitiesResponse targetDaoDeleteEntitiesResponse = this.wrappedDao.deleteEntities(targetDaoDeleteEntitiesRequest);
        for (ProvisioningEntity provisioningEntity : targetDaoDeleteEntitiesRequest.getTargetEntities()) { 
          if (provisioningEntity.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set deleted entity as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoDeleteEntitiesResponse;
      } catch (RuntimeException e) {
        for (ProvisioningEntity targetEntity : targetDaoDeleteEntitiesRequest.getTargetEntities()) { 
          
          if (targetEntity.getProvisioned() == null) {
            targetEntity.setProvisioned(false);
          }
          if (targetEntity.getException() == null) {
            GrouperUtil.injectInException(e, targetEntity.toString());
            targetEntity.setException(e);
          }
        }
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteEntity(), false)) {
      for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(targetDaoDeleteEntitiesRequest.getTargetEntities())) {
        deleteEntity(new TargetDaoDeleteEntityRequest(provisioningEntity));
      }
      return null;
    }

    throw new RuntimeException("Dao cannot delete entity or entities");
  }


  @Override
  public TargetDaoInsertEntityResponse insertEntity(
      TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {

    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    if (targetEntity == null) {
      return new TargetDaoInsertEntityResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertEntity(), false)) {
      try {
        TargetDaoInsertEntityResponse targetDaoInsertEntityResponse = this.wrappedDao.insertEntity(targetDaoInsertEntityRequest);
        if (targetEntity.getProvisioned() == null) {
          throw new RuntimeException("Dao did not set inserted entity as provisioned: " + this.wrappedDao);
        }
        return targetDaoInsertEntityResponse;
      } catch (RuntimeException e) {
        
        if (targetEntity.getProvisioned() == null) {
          targetEntity.setProvisioned(false);
        }
        if (targetEntity.getException() == null) {
          GrouperUtil.injectInException(e, targetEntity.toString());
          targetEntity.setException(e);
        }
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertEntities(), false)) {
      this.insertEntities(new TargetDaoInsertEntitiesRequest(GrouperUtil.toList(targetEntity)));
      return null;
    }

    throw new RuntimeException("Dao cannot insert entity or entities");
  }


  @Override
  public TargetDaoInsertEntitiesResponse insertEntities(
      TargetDaoInsertEntitiesRequest targetDaoInsertEntitiesRequest) {

    if (GrouperUtil.length(targetDaoInsertEntitiesRequest.getTargetEntityInserts()) == 0) {
      return new TargetDaoInsertEntitiesResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertEntities(), false)) {
      try {
        TargetDaoInsertEntitiesResponse targetDaoInsertEntitiesResponse = this.wrappedDao.insertEntities(targetDaoInsertEntitiesRequest);
        for (ProvisioningEntity provisioningEntity : targetDaoInsertEntitiesRequest.getTargetEntityInserts()) { 
          if (provisioningEntity.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set inserted entity as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoInsertEntitiesResponse;
      } catch (RuntimeException e) {
        for (ProvisioningEntity targetEntity : targetDaoInsertEntitiesRequest.getTargetEntityInserts()) { 
          
          if (targetEntity.getProvisioned() == null) {
            targetEntity.setProvisioned(false);
          }
          if (targetEntity.getException() == null) {
            GrouperUtil.injectInException(e, targetEntity.toString());
            targetEntity.setException(e);
          }
        }
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertEntity(), false)) {
      for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(targetDaoInsertEntitiesRequest.getTargetEntityInserts())) {
        insertEntity(new TargetDaoInsertEntityRequest(provisioningEntity));
      }
      return null;
    }

    throw new RuntimeException("Dao cannot insert entity or entities");
  }


  @Override
  public TargetDaoUpdateEntityResponse updateEntity(
      TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {
    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();
    
    if (targetEntity == null) {
      return new TargetDaoUpdateEntityResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateEntity(), false)) {
      try {
        TargetDaoUpdateEntityResponse targetDaoUpdateEntityResponse = this.wrappedDao.updateEntity(targetDaoUpdateEntityRequest);
        if (targetEntity.getProvisioned() == null) {
          throw new RuntimeException("Dao did not set updateed entity as provisioned: " + this.wrappedDao);
        }
        return targetDaoUpdateEntityResponse;
      } catch (RuntimeException e) {
        
        if (targetEntity.getProvisioned() == null) {
          targetEntity.setProvisioned(false);
        }
        if (targetEntity.getException() == null) {
          GrouperUtil.injectInException(e, targetEntity.toString());
          targetEntity.setException(e);
        }
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateEntities(), false)) {
      this.updateEntities(new TargetDaoUpdateEntitiesRequest(GrouperUtil.toList(targetEntity)));
      return null;
    }

    throw new RuntimeException("Dao cannot update entity or entities");
  }


  @Override
  public TargetDaoUpdateEntitiesResponse updateEntities(
      TargetDaoUpdateEntitiesRequest targetDaoUpdateEntitiesRequest) {
    
    if (GrouperUtil.length(targetDaoUpdateEntitiesRequest.getTargetEntities()) == 0) {
      return new TargetDaoUpdateEntitiesResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateEntities(), false)) {
      try {
        TargetDaoUpdateEntitiesResponse targetDaoUpdateEntitiesResponse = this.wrappedDao.updateEntities(targetDaoUpdateEntitiesRequest);
        for (ProvisioningEntity provisioningEntity : targetDaoUpdateEntitiesRequest.getTargetEntities()) { 
          if (provisioningEntity.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set updated entity as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoUpdateEntitiesResponse;
      } catch (RuntimeException e) {
        for (ProvisioningEntity targetEntity : targetDaoUpdateEntitiesRequest.getTargetEntities()) { 
          
          if (targetEntity.getProvisioned() == null) {
            targetEntity.setProvisioned(false);
          }
          if (targetEntity.getException() == null) {
            GrouperUtil.injectInException(e, targetEntity.toString());
            targetEntity.setException(e);
          }
        }
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateEntity(), false)) {
      for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(targetDaoUpdateEntitiesRequest.getTargetEntities())) {
        updateEntity(new TargetDaoUpdateEntityRequest(provisioningEntity));
      }
      return null;
    }

    throw new RuntimeException("Dao cannot update entity or entities");
  }


  @Override
  public TargetDaoDeleteMembershipResponse deleteMembership(
      TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {
    ProvisioningMembership targetMembership = targetDaoDeleteMembershipRequest.getTargetMembership();
    
    if (targetMembership == null) {
      return new TargetDaoDeleteMembershipResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteMembership(), false)) {
      try {
        TargetDaoDeleteMembershipResponse targetDaoDeleteMembershipResponse = this.wrappedDao.deleteMembership(targetDaoDeleteMembershipRequest);
        if (targetMembership.getProvisioned() == null) {
          throw new RuntimeException("Dao did not set deleted membership as provisioned: " + this.wrappedDao);
        }
        return targetDaoDeleteMembershipResponse;
      } catch (RuntimeException e) {
        
        if (targetMembership.getProvisioned() == null) {
          targetMembership.setProvisioned(false);
        }
        if (targetMembership.getException() == null) {
          GrouperUtil.injectInException(e, targetMembership.toString());
          targetMembership.setException(e);
        }
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteMemberships(), false)) {
      this.deleteMemberships(new TargetDaoDeleteMembershipsRequest(GrouperUtil.toList(targetMembership)));
      return null;
    }

    throw new RuntimeException("Dao cannot delete membership or memberships");

  }


  @Override
  public TargetDaoDeleteGroupsResponse deleteGroups(
      TargetDaoDeleteGroupsRequest targetDaoDeleteGroupsRequest) {

    if (GrouperUtil.length(targetDaoDeleteGroupsRequest.getTargetGroups()) == 0) {
      return new TargetDaoDeleteGroupsResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteGroups(), false)) {
      try {
        TargetDaoDeleteGroupsResponse targetDaoDeleteGroupsResponse = this.wrappedDao.deleteGroups(targetDaoDeleteGroupsRequest);
        for (ProvisioningGroup provisioningGroup : targetDaoDeleteGroupsRequest.getTargetGroups()) { 
          if (provisioningGroup.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set deleted group as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoDeleteGroupsResponse;
      } catch (RuntimeException e) {
        for (ProvisioningGroup targetGroup : targetDaoDeleteGroupsRequest.getTargetGroups()) { 
          
          if (targetGroup.getProvisioned() == null) {
            targetGroup.setProvisioned(false);
          }
          if (targetGroup.getException() == null) {
            GrouperUtil.injectInException(e, targetGroup.toString());
            targetGroup.setException(e);
          }
        }
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanDeleteGroup(), false)) {
      for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(targetDaoDeleteGroupsRequest.getTargetGroups())) {
        deleteGroup(new TargetDaoDeleteGroupRequest(provisioningGroup));
      }
      return null;
    }

    throw new RuntimeException("Dao cannot delete group or groups");
    
  }


  @Override
  public TargetDaoInsertMembershipResponse insertMembership(
      TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
    ProvisioningMembership targetMembership = targetDaoInsertMembershipRequest.getTargetMembership();
    
    if (targetMembership == null) {
      return new TargetDaoInsertMembershipResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertMembership(), false)) {
      try {
        TargetDaoInsertMembershipResponse targetDaoInsertMembershipResponse = this.wrappedDao.insertMembership(targetDaoInsertMembershipRequest);
        if (targetMembership.getProvisioned() == null) {
          throw new RuntimeException("Dao did not set inserted membership as provisioned: " + this.wrappedDao);
        }
        return targetDaoInsertMembershipResponse;
      } catch (RuntimeException e) {
        
        if (targetMembership.getProvisioned() == null) {
          targetMembership.setProvisioned(false);
        }
        if (targetMembership.getException() == null) {
          GrouperUtil.injectInException(e, targetMembership.toString());
          targetMembership.setException(e);
        }
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertMemberships(), false)) {
      this.insertMemberships(new TargetDaoInsertMembershipsRequest(GrouperUtil.toList(targetMembership)));
      return null;
    }

    throw new RuntimeException("Dao cannot insert membership or memberships");
  }


  @Override
  public TargetDaoInsertMembershipsResponse insertMemberships(
      TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {
    
    if (GrouperUtil.length(targetDaoInsertMembershipsRequest.getTargetMemberships()) == 0) {
      return new TargetDaoInsertMembershipsResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertMemberships(), false)) {
      try {
        TargetDaoInsertMembershipsResponse targetDaoInsertMembershipsResponse = this.wrappedDao.insertMemberships(targetDaoInsertMembershipsRequest);
        for (ProvisioningMembership provisioningMembership : targetDaoInsertMembershipsRequest.getTargetMemberships()) { 
          if (provisioningMembership.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set inserted membership as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoInsertMembershipsResponse;
      } catch (RuntimeException e) {
        for (ProvisioningMembership targetMembership : targetDaoInsertMembershipsRequest.getTargetMemberships()) { 
          
          if (targetMembership.getProvisioned() == null) {
            targetMembership.setProvisioned(false);
          }
          if (targetMembership.getException() == null) {
            GrouperUtil.injectInException(e, targetMembership.toString());
            targetMembership.setException(e);
          }
        }
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanInsertMembership(), false)) {
      for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(targetDaoInsertMembershipsRequest.getTargetMemberships())) {
        insertMembership(new TargetDaoInsertMembershipRequest(provisioningMembership));
      }
      return null;
    }

    throw new RuntimeException("Dao cannot insert membership or memberships");
  }


  @Override
  public TargetDaoUpdateMembershipResponse updateMembership(
      TargetDaoUpdateMembershipRequest targetDaoUpdateMembershipRequest) {
    ProvisioningMembership targetMembership = targetDaoUpdateMembershipRequest.getTargetMembership();
    
    if (targetMembership == null) {
      return new TargetDaoUpdateMembershipResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateMembership(), false)) {
      try {
        TargetDaoUpdateMembershipResponse targetDaoUpdateMembershipResponse = this.wrappedDao.updateMembership(targetDaoUpdateMembershipRequest);
        if (targetMembership.getProvisioned() == null) {
          throw new RuntimeException("Dao did not set updated membership as provisioned: " + this.wrappedDao);
        }
        return targetDaoUpdateMembershipResponse;
      } catch (RuntimeException e) {
        
        if (targetMembership.getProvisioned() == null) {
          targetMembership.setProvisioned(false);
        }
        if (targetMembership.getException() == null) {
          GrouperUtil.injectInException(e, targetMembership.toString());
          targetMembership.setException(e);
        }
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateMemberships(), false)) {
      this.updateMemberships(new TargetDaoUpdateMembershipsRequest(GrouperUtil.toList(targetMembership)));
      return null;
    }

    throw new RuntimeException("Dao cannot update membership or memberships");
  }

  @Override
  public TargetDaoUpdateMembershipsResponse updateMemberships(
      TargetDaoUpdateMembershipsRequest targetDaoUpdateMembershipsRequest) {
    
    if (GrouperUtil.length(targetDaoUpdateMembershipsRequest.getTargetMemberships()) == 0) {
      return new TargetDaoUpdateMembershipsResponse();
    }

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateMemberships(), false)) {
      try {
        TargetDaoUpdateMembershipsResponse targetDaoUpdateMembershipsResponse = this.wrappedDao.updateMemberships(targetDaoUpdateMembershipsRequest);
        for (ProvisioningMembership provisioningMembership : targetDaoUpdateMembershipsRequest.getTargetMemberships()) { 
          if (provisioningMembership.getProvisioned() == null) {
            throw new RuntimeException("Dao did not set updated membership as provisioned: " + this.wrappedDao);
          }
        }
        return targetDaoUpdateMembershipsResponse;
      } catch (RuntimeException e) {
        for (ProvisioningMembership targetMembership : targetDaoUpdateMembershipsRequest.getTargetMemberships()) { 
          
          if (targetMembership.getProvisioned() == null) {
            targetMembership.setProvisioned(false);
          }
          if (targetMembership.getException() == null) {
            GrouperUtil.injectInException(e, targetMembership.toString());
            targetMembership.setException(e);
          }
        }
      }
      return null;
    }
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanUpdateMembership(), false)) {
      for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(targetDaoUpdateMembershipsRequest.getTargetMemberships())) {
        updateMembership(new TargetDaoUpdateMembershipRequest(provisioningMembership));
      }
      return null;
    }

    throw new RuntimeException("Dao cannot update membership or memberships");
  }


  @Override
  public TargetDaoSendEntityChangesToTargetResponse sendEntityChangesToTarget(
      TargetDaoSendEntityChangesToTargetRequest targetDaoSendEntityChangesToTargetRequest) {
    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanSendEntityChangesToTarget(), false)) {
      return this.wrappedDao.sendEntityChangesToTarget(targetDaoSendEntityChangesToTargetRequest);
    }
    if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getEntitiesDeleteIfDeletedFromGrouper(), false)) {
      List<ProvisioningEntity> targetEntityDeletes = targetDaoSendEntityChangesToTargetRequest.getTargetEntityDeletes();
      TargetDaoDeleteEntitiesRequest targetDaoDeleteEntitiesRequest = new TargetDaoDeleteEntitiesRequest();
      targetDaoDeleteEntitiesRequest.setTargetEntities(targetEntityDeletes);
      this.deleteEntities(targetDaoDeleteEntitiesRequest);
    }

    if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getEntitiesInsert(), false)) {
      List<ProvisioningEntity> targetEntityInserts = targetDaoSendEntityChangesToTargetRequest.getTargetEntityInserts();
      
      this.insertEntities(new TargetDaoInsertEntitiesRequest(targetEntityInserts));
    }
    if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getEntitiesUpdate(), false)) {
      List<ProvisioningEntity> targetEntityUpdates = targetDaoSendEntityChangesToTargetRequest.getTargetEntityUpdates();
      
      this.updateEntities(new TargetDaoUpdateEntitiesRequest(targetEntityUpdates));
    }
    return null;
  }


  @Override
  public TargetDaoSendMembershipChangesToTargetResponse sendMembershipChangesToTarget(
      TargetDaoSendMembershipChangesToTargetRequest targetDaoSendMembershipChangesToTargetRequest) {

    if (GrouperUtil.booleanValue(this.wrappedDao.getGrouperProvisionerDaoCapabilities().getCanSendMembershipChangesToTarget(), false)) {
      return this.wrappedDao.sendMembershipChangesToTarget(targetDaoSendMembershipChangesToTargetRequest);
    }
    if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getMembershipsDeleteIfDeletedFromGrouper(), false)) {
      List<ProvisioningMembership> targetMembershipDeletes = targetDaoSendMembershipChangesToTargetRequest.getTargetMembershipDeletes();
      TargetDaoDeleteMembershipsRequest targetDaoDeleteMembershipsRequest = new TargetDaoDeleteMembershipsRequest();
      targetDaoDeleteMembershipsRequest.setTargetMemberships(targetMembershipDeletes);
      this.deleteMemberships(targetDaoDeleteMembershipsRequest);
    }

    if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getMembershipsInsert(), false)) {
      List<ProvisioningMembership> targetMembershipInserts = targetDaoSendMembershipChangesToTargetRequest.getTargetMembershipInserts();
      
      this.insertMemberships(new TargetDaoInsertMembershipsRequest(targetMembershipInserts));
    }
    if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getMembershipsUpdate(), false)) {
      List<ProvisioningMembership> targetMembershipUpdates = targetDaoSendMembershipChangesToTargetRequest.getTargetMembershipUpdates();
      
      this.updateMemberships(new TargetDaoUpdateMembershipsRequest(targetMembershipUpdates));
    }
    return null;

  }



}
