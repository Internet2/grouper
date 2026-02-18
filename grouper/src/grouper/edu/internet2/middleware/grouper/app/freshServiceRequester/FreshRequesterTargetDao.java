package edu.internet2.middleware.grouper.app.freshServiceRequester;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupResponse;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class FreshRequesterTargetDao extends GrouperProvisionerTargetDaoBase {
  
  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
  }

  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }
  
  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();
    long startNanos = System.nanoTime();
    
    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      List<FreshRequesterGroup> requesterGroups = FreshRequesterApiCommands.retrieveRequesterGroups(freshserviceConfiguration.getFreshserviceExternalSystemConfigId());
      
      for (FreshRequesterGroup requesterGroup : requesterGroups) {
        ProvisioningGroup targetGroup = requesterGroup.toProvisioningGroup();
        results.add(targetGroup);
      }
      return new TargetDaoRetrieveAllGroupsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllGroups", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {
    long startNanos = System.nanoTime();
    
    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      if (StringUtils.equals("id", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        FreshRequesterGroup requesterGroup = FreshRequesterApiCommands.retrieveRequesterGroup(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(),
            GrouperUtil.longValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()));
        ProvisioningGroup targetGroup = requesterGroup == null ? null : requesterGroup.toProvisioningGroup();
        return new TargetDaoRetrieveGroupResponse(targetGroup);
      } else if (StringUtils.equals("name", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        List<FreshRequesterGroup> requesterGroups = FreshRequesterApiCommands.retrieveRequesterGroups(freshserviceConfiguration.getFreshserviceExternalSystemConfigId());
        for (FreshRequesterGroup requesterGroup : requesterGroups) {
          if (StringUtils.equals(requesterGroup.getName(), GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()))) {
            ProvisioningGroup targetGroup = requesterGroup == null ? null : requesterGroup.toProvisioningGroup();
            return new TargetDaoRetrieveGroupResponse(targetGroup);
          } 
        } 
      } else {
        throw new RuntimeException("id or name is required as a group search attribute");
      }
      return new TargetDaoRetrieveGroupResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
    }
  }
  
  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();

    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();

      FreshRequesterGroup grouperRequesterGroup = FreshRequesterGroup.fromProvisioningGroup(targetGroup, null);

      FreshRequesterGroup createdGroup = FreshRequesterApiCommands.createRequesterGroup(
          freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), grouperRequesterGroup);

      targetGroup.setId(String.valueOf(createdGroup.getId()));
      targetGroup.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoInsertGroupResponse();
    } catch (Exception e) {
      targetGroup.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertGroup", startNanos));
    }
  }

  @Override
  public TargetDaoUpdateGroupResponse updateGroup(TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();

    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();

      // collect the field names and actions that need to be updated from the provisioning object changes
      Map<String, ProvisioningObjectChangeAction> fieldsToUpdate = new LinkedHashMap<String, ProvisioningObjectChangeAction>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldsToUpdate.put(fieldName, provisioningObjectChange.getProvisioningObjectChangeAction());
      }

      FreshRequesterGroup grouperRequesterGroup = FreshRequesterGroup.fromProvisioningGroup(targetGroup, null);

      FreshRequesterApiCommands.updateRequesterGroup(
          freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), grouperRequesterGroup, fieldsToUpdate);

      targetGroup.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoUpdateGroupResponse();
    } catch (Exception e) {
      targetGroup.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateGroup", startNanos));
    }
  }

  @Override
  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest.getTargetGroup();

    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();

      FreshRequesterGroup grouperRequesterGroup = FreshRequesterGroup.fromProvisioningGroup(targetGroup, null);

      FreshRequesterApiCommands.deleteRequesterGroup(
          freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), grouperRequesterGroup.getId());

      targetGroup.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoDeleteGroupResponse();
    } catch (Exception e) {
      targetGroup.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteGroup", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {
    List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();
    long startNanos = System.nanoTime();
    
    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      List<FreshRequesterUser> requesters = FreshRequesterApiCommands.retrieveRequesterUsers(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), false);
      for (FreshRequesterUser requester : requesters) {
        ProvisioningEntity targetEntity = requester.toProvisioningEntity();
        results.add(targetEntity);
      }
      return new TargetDaoRetrieveAllEntitiesResponse(results);
    }
    finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {
    long startNanos = System.nanoTime();
    
    FreshRequesterUser requester = null;
    
    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      requester = FreshRequesterApiCommands.retrieveRequesterUserByAttribute(
          freshserviceConfiguration.getFreshserviceExternalSystemConfigId(),
          targetDaoRetrieveEntityRequest.getSearchAttribute(),
          targetDaoRetrieveEntityRequest.getSearchAttributeValue());
      
      ProvisioningEntity targetEntity = requester == null ? null : requester.toProvisioningEntity();
      
      TargetDaoRetrieveEntityResponse targetDaoRetrieveEntityResponse = new TargetDaoRetrieveEntityResponse(targetEntity);
      if (targetDaoRetrieveEntityRequest.isIncludeNativeEntity()) {
        targetDaoRetrieveEntityResponse.setTargetNativeEntity(requester);
      }
      return targetDaoRetrieveEntityResponse;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }
  
  @Override
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();

      FreshRequesterUser grouperRequesterUser = FreshRequesterUser.fromProvisioningEntity(targetEntity, null);

      FreshRequesterUser createdUser = FreshRequesterApiCommands.createRequesterUser(
          freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), grouperRequesterUser);

      targetEntity.setId(String.valueOf(createdUser.getId()));
      targetEntity.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoInsertEntityResponse();
    } catch (Exception e) {
      targetEntity.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertEntity", startNanos));
    }
  }

  @Override
  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();

    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();

      // collect the field names that need to be updated from the provisioning object changes
      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToUpdate.add(fieldName);
      }

      FreshRequesterUser grouperRequesterUser = FreshRequesterUser.fromProvisioningEntity(targetEntity, null);

      FreshRequesterApiCommands.updateRequesterUser(
          freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), grouperRequesterUser, fieldNamesToUpdate);

      targetEntity.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoUpdateEntityResponse();
    } catch (Exception e) {
      targetEntity.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateEntity", startNanos));
    }
  }

  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();

    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();

      FreshRequesterUser grouperRequesterUser = FreshRequesterUser.fromProvisioningEntity(targetEntity, null);

      // deactivate (soft delete) the requester user
      FreshRequesterApiCommands.deactivateRequesterUser(
          freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), grouperRequesterUser.getId());

      targetEntity.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoDeleteEntityResponse();
    } catch (Exception e) {
      targetEntity.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteEntity", startNanos));
    }
  }

  @Override
  public TargetDaoInsertMembershipResponse insertMembership(TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoInsertMembershipRequest.getTargetMembership();
    
    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      FreshRequesterApiCommands.addGroupMembership(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), 
          GrouperUtil.longValue(targetMembership.getProvisioningGroupId()),GrouperUtil.longValue(targetMembership.getProvisioningEntityId()));
      
      targetMembership.setProvisioned(true);
      return new TargetDaoInsertMembershipResponse();
    } catch(Exception e) {
      targetMembership.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertMembership", startNanos));
    }
  }
  
  @Override
  public TargetDaoDeleteMembershipResponse deleteMembership(TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {
    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoDeleteMembershipRequest.getTargetMembership();
    
    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      FreshRequesterApiCommands.removeGroupMembership(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), 
          GrouperUtil.longValue(targetMembership.getProvisioningGroupId()),GrouperUtil.longValue(targetMembership.getProvisioningEntityId()));
      
      targetMembership.setProvisioned(true);
      return new TargetDaoDeleteMembershipResponse();
    } catch(Exception e) {
      targetMembership.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMembership", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();
    
    String targetGroupId = resolveTargetGroupId(targetGroup, this.getGrouperProvisioner());
    List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();
    
    if (StringUtils.isBlank(targetGroupId)) {
      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
    }
    
    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      List<FreshRequesterUser> requesters = FreshRequesterApiCommands.retrieveMembershipsByGroup(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(),
          GrouperUtil.longValue(targetGroupId));
      
      for(FreshRequesterUser requester : requesters) {
        ProvisioningMembership targetMembership = new ProvisioningMembership();
        targetMembership.setProvisioningGroupId(targetGroupId);
        targetMembership.setProvisioningEntityId(requester.getId() == null ? null : Long.toString(requester.getId()));
        provisioningMemberships.add(targetMembership);
      }
      
      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
      
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByGroup", startNanos));
    }
  }
  
  public String resolveTargetGroupId(ProvisioningGroup targetGroup, GrouperProvisioner grouperProvisioner) {
    if (targetGroup == null) {
      return null;
    }
    
    if (StringUtils.isNotBlank(targetGroup.getId())) {
      return targetGroup.getId();
    }
    
    TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest = new TargetDaoRetrieveGroupsRequest();
    targetDaoRetrieveGroupsRequest.setTargetGroups(GrouperUtil.toList(targetGroup));
    targetDaoRetrieveGroupsRequest.setIncludeAllMembershipsIfApplicable(false);
    TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveGroups(
        targetDaoRetrieveGroupsRequest);

    if (targetDaoRetrieveGroupsResponse == null || GrouperUtil.length(targetDaoRetrieveGroupsResponse.getTargetGroups()) == 0) {
      return null;
    }
    
    return targetDaoRetrieveGroupsResponse.getTargetGroups().get(0).getId();
  }
  

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertMembership(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMembership(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByGroup(true);
    
  }

}
