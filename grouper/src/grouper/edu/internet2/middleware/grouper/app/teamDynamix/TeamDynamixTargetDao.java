package edu.internet2.middleware.grouper.app.teamDynamix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeMembership;
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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByEntityResponse;
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
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class TeamDynamixTargetDao extends GrouperProvisionerTargetDaoBase {
  

  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
  }

  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    
    grouperProvisionerDaoCapabilities.setInsertMembershipsBatchSize(400);
    grouperProvisionerDaoCapabilities.setDeleteMembershipsBatchSize(400);

    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);

    grouperProvisionerDaoCapabilities.setCanDeleteMemberships(true);

    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);

    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);

    //POST https://rollins.teamdynamix.com/TDWebApi/api/groups/{id}/members?isPrimary={isPrimary}&isNotified={isNotified}&isManager={isManager}    
    grouperProvisionerDaoCapabilities.setCanInsertMemberships(true);

    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);

    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);

    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);

    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByGroup(true);

    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);

    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);

    // read path captures TeamDynamixUser/Group beans through TeamDynamixProvisioningTargetNativeSync.record*
    grouperProvisionerDaoCapabilities.setCanSyncBack(true);

  }

  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(
      TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {

    long startNanos = System.nanoTime();

    try {

      TeamDynamixProvisioningConfiguration teamDynamixConfiguration = (TeamDynamixProvisioningConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

      List<TeamDynamixGroup> teamDynamixGroups = TeamDynamixApiCommands
          .retrieveTeamDynamixGroups(teamDynamixConfiguration.getTeamDynamixExternalSystemConfigId());

      for (TeamDynamixGroup teamDynamixGroup : teamDynamixGroups) {
        ProvisioningGroup targetGroup = teamDynamixGroup.toProvisioningGroup();
        results.add(targetGroup);
        // generic provisioner sync back: the native group is now captured from the raw JSON at the
        // TeamDynamixApiCommands.retrieveTeamDynamixGroups read seam (full fidelity), not here.
      }

      return new TargetDaoRetrieveAllGroupsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(
          new TargetDaoTimingInfo("retrieveAllGroups", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(
      TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {

    long startNanos = System.nanoTime();

    try {

      //TODO
      //boolean includeAllMembershipsIfApplicable = targetDaoRetrieveAllGroupsRequest == null ? false : targetDaoRetrieveAllGroupsRequest.isIncludeAllMembershipsIfApplicable();

      TeamDynamixProvisioningConfiguration teamDynamixConfiguration = (TeamDynamixProvisioningConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

      List<TeamDynamixUser> teamDynamixUsers = TeamDynamixApiCommands
          .retrieveTeamDynamixUsers(teamDynamixConfiguration.getTeamDynamixExternalSystemConfigId());

      for (TeamDynamixUser teamDynamixUser : teamDynamixUsers) {
        ProvisioningEntity targetEntity = teamDynamixUser.toProvisioningEntity();
        results.add(targetEntity);
        // generic provisioner sync back: the native user is now captured from the raw JSON at the
        // TeamDynamixApiCommands.retrieveTeamDynamixUsers read seam (full fidelity), not here.
      }

      return new TargetDaoRetrieveAllEntitiesResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(
          new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }
  }

  
  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();

    try {
      TeamDynamixProvisioningConfiguration teamDynamixConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      TeamDynamixGroup teamDynamixGroup = TeamDynamixGroup.fromProvisioningGroup(targetGroup, null);
      
      TeamDynamixGroup createdTeamDynamixGroup = TeamDynamixApiCommands.createTeamDynamixGroup(teamDynamixConfiguration.getTeamDynamixExternalSystemConfigId(), teamDynamixGroup);

      targetGroup.setId(createdTeamDynamixGroup.getId());
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
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    try {
      TeamDynamixProvisioningConfiguration teamDynamixConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      TeamDynamixUser teamDynamixUser = TeamDynamixUser.fromProvisioningEntity(targetEntity, null);
      teamDynamixUser.setActive(true);
      String userId = TeamDynamixApiCommands.createTeamDynamixUser(teamDynamixConfiguration.getTeamDynamixExternalSystemConfigId(), teamDynamixUser);

      targetEntity.setId(userId);
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
  public TargetDaoInsertMembershipsResponse insertMemberships(TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {
    long startNanos = System.nanoTime();
    List<ProvisioningMembership> targetMemberships = targetDaoInsertMembershipsRequest.getTargetMemberships();

    try {
      
      TeamDynamixProvisioningConfiguration teamDynamixConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      // lets collate by group
      Map<String, List<String>> groupIdToUserIds = new LinkedHashMap<String, List<String>>();
      Map<String, Boolean> groupIdToIsNotified = new LinkedHashMap<String, Boolean>();

      // keep track to mark as complete
      Map<MultiKey, ProvisioningMembership> groupIdUserIdToProvisioningMembership = new HashMap<MultiKey, ProvisioningMembership>();
      
      for (ProvisioningMembership targetMembership : targetMemberships) {

        groupIdUserIdToProvisioningMembership.put(new MultiKey(targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntityId()), targetMembership);
        
        boolean isNotified = GrouperUtil.booleanValue(targetMembership.getProvisioningGroup().getProvisioningGroupWrapper().getGrouperTargetGroup().retrieveAttributeValueBoolean("isNotified"), false);
        groupIdToIsNotified.put(targetMembership.getProvisioningGroupId(), isNotified);
        List<String> userIds = groupIdToUserIds.get(targetMembership.getProvisioningGroupId());
        if (userIds == null) {
          userIds = new ArrayList<String>();
          groupIdToUserIds.put(targetMembership.getProvisioningGroupId(), userIds);
        }
        userIds.add(targetMembership.getProvisioningEntityId());
        
      }
      
      // send batches by group
      for (String groupId : groupIdToUserIds.keySet()) {

        List<String> userIds = groupIdToUserIds.get(groupId);
        
        RuntimeException runtimeException = null;
        try {
          TeamDynamixApiCommands.createTeamDynamixMemberships(teamDynamixConfiguration.getTeamDynamixExternalSystemConfigId(), groupId, userIds, groupIdToIsNotified.get(groupId));
          
        } catch (RuntimeException e) {
          runtimeException = e;
        }
        boolean success = runtimeException == null;
        for (String userId : userIds) {
          MultiKey groupIdUserId = new MultiKey(groupId, userId);
          
          ProvisioningMembership targetMembership = groupIdUserIdToProvisioningMembership.get(groupIdUserId);
          if (targetMembership.getProvisioned() == null) {
            targetMembership.setProvisioned(success);
            targetMembership.setException(runtimeException);
            for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
              provisioningObjectChange.setProvisioned(success);

            }
            // sync-back: on the success path only, write-track the added membership into the native
            // mirror (memberships are tracked from writes, never re-read). groupId/userId are the
            // TeamDynamix target ids we just passed to createTeamDynamixMemberships.
            if (success) {
              TeamDynamixProvisioningTargetNativeSync.captureMembershipInsertFromCurrentProvisioner(groupId, userId);
            }
          }
        }
      }
      
      return new TargetDaoInsertMembershipsResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertMemberships", startNanos));
    }
  }
  
  @Override
  public TargetDaoDeleteMembershipsResponse deleteMemberships(TargetDaoDeleteMembershipsRequest targetDaoDeleteMembershipsRequest) {
    long startNanos = System.nanoTime();
    
    List<ProvisioningMembership> targetMemberships = targetDaoDeleteMembershipsRequest.getTargetMemberships();

    try {
      TeamDynamixProvisioningConfiguration  teamDynamixConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      TeamDynamixApiCommands.deleteTeamDynamixMemberships(teamDynamixConfiguration.getTeamDynamixExternalSystemConfigId(), targetMemberships);

      for (ProvisioningMembership targetMembership: targetMemberships) {

        targetMembership.setProvisioned(true);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(true);
        }
        // sync-back: deleteTeamDynamixMemberships is all-or-nothing (throws on failure), so this
        // loop runs only when every delete succeeded. Drop each removed membership out of the
        // native mirror so the end-of-run flush deletes its grouper_prov_mship row. group/user ids
        // are the TeamDynamix target ids that were sent to the delete call.
        TeamDynamixProvisioningTargetNativeSync.captureMembershipDeleteFromCurrentProvisioner(
            targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntityId());

      }

      return new TargetDaoDeleteMembershipsResponse();
    } catch (Exception e) {
      
      for (ProvisioningMembership targetMembership: targetMemberships) {
        targetMembership.setProvisioned(false);
        targetMembership.setException(e);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(false);
        }
      }
      
      throw new RuntimeException("Failed to delete TeamDynamix memberships", e);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMemberships", startNanos));
    }
  }
  
  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();

    try {
      TeamDynamixProvisioningConfiguration teamDynamixConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      TeamDynamixUser teamDynamixUser = TeamDynamixUser.fromProvisioningEntity(targetEntity, null);
      
      TeamDynamixApiCommands.updateTeamDynamixUserStatus(teamDynamixConfiguration.getTeamDynamixExternalSystemConfigId(),
          teamDynamixUser.getId(), false);

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
  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest.getTargetGroup();

    try {
      TeamDynamixProvisioningConfiguration teamDynamixConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      TeamDynamixGroup teamDynamixGroup = TeamDynamixGroup.fromProvisioningGroup(targetGroup, null);
      
      TeamDynamixApiCommands.updateTeamDynamixGroup(teamDynamixConfiguration.getTeamDynamixExternalSystemConfigId(), teamDynamixGroup, null);
      
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
  public TargetDaoRetrieveEntityResponse retrieveEntity(
      TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {

    long startNanos = System.nanoTime();

    try {
      TeamDynamixProvisioningConfiguration teamDynamixConfiguration = (TeamDynamixProvisioningConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      // we can retrieve by uid or external id, prefer uid

      TeamDynamixUser teamDynamixUser = null;

      if (StringUtils.equals("id", targetDaoRetrieveEntityRequest.getSearchAttribute())) {
        teamDynamixUser = TeamDynamixApiCommands.retrieveTeamDynamixUser(teamDynamixConfiguration.getTeamDynamixExternalSystemConfigId(), 
            GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue()));
      } else if (StringUtils.equals("ExternalID", targetDaoRetrieveEntityRequest.getSearchAttribute())) {
        teamDynamixUser = TeamDynamixApiCommands.retrieveTeamDynamixUserBySearchTerm(
            teamDynamixConfiguration.getTeamDynamixExternalSystemConfigId(), 
            "externalId", 
            GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue()), true);
      } else {
        throw new RuntimeException("Not expecting search attribute '" + targetDaoRetrieveEntityRequest.getSearchAttribute() + "'");
      }
      
      ProvisioningEntity targetEntity = teamDynamixUser == null ? null
          : teamDynamixUser.toProvisioningEntity();

      // generic provisioner sync back: the scoped retrieve path (!selectAllEntities and
      // incremental) now captures the native user from the raw JSON at the TeamDynamixApiCommands
      // read seam (retrieveTeamDynamixUser by id / retrieveTeamDynamixUserBySearchTerm by
      // ExternalID), not here.

      TargetDaoRetrieveEntityResponse targetDaoRetrieveEntityResponse = new TargetDaoRetrieveEntityResponse(targetEntity);
      if (targetDaoRetrieveEntityRequest.isIncludeNativeEntity()) {
        targetDaoRetrieveEntityResponse.setTargetNativeEntity(teamDynamixUser);
      }
      return targetDaoRetrieveEntityResponse;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }

  
  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {

    long startNanos = System.nanoTime();

    try {
      TeamDynamixProvisioningConfiguration teamDynamixConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      ProvisioningGroup grouperTargetGroup = targetDaoRetrieveGroupRequest.getTargetGroup();

      TeamDynamixGroup teamDynamixGroup = null;

      if (StringUtils.equals("id", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        teamDynamixGroup = TeamDynamixApiCommands.retrieveTeamDynamixGroup(teamDynamixConfiguration.getTeamDynamixExternalSystemConfigId(), GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()));
      } else if (StringUtils.equals("Name", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        String name = GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue());
        if (StringUtils.isNotBlank(name)) {
          teamDynamixGroup = TeamDynamixApiCommands.retrieveTeamDynamixGroupByName(
              teamDynamixConfiguration.getTeamDynamixExternalSystemConfigId(), 
              GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()), true);
        }
        
      } else {
        throw new RuntimeException("Not expecting search attribute '" + targetDaoRetrieveGroupRequest.getSearchAttribute() + "'");
      }

      ProvisioningGroup targetGroup = teamDynamixGroup == null ? null : teamDynamixGroup.toProvisioningGroup();

      // generic provisioner sync back: the scoped retrieve path (!selectAllGroups and incremental)
      // now captures the native group from the raw JSON at the TeamDynamixApiCommands read seam
      // (retrieveTeamDynamixGroup by id / retrieveTeamDynamixGroupByName by Name), not here.

      return new TargetDaoRetrieveGroupResponse(targetGroup);

    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
    }
  }
  
  public String resolveTargetEntityId(ProvisioningEntity targetEntity) {
    
    if (targetEntity == null) {
      return null;
    }
    
    if (StringUtils.isNotBlank(targetEntity.getId())) {
      return targetEntity.getId();
    }
    
    TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest = new TargetDaoRetrieveEntitiesRequest();
    targetDaoRetrieveEntitiesRequest.setTargetEntities(GrouperUtil.toList(targetEntity));
    targetDaoRetrieveEntitiesRequest.setIncludeAllMembershipsIfApplicable(false);
    TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().retrieveEntities(
        targetDaoRetrieveEntitiesRequest);

    if (targetDaoRetrieveEntitiesResponse == null || GrouperUtil.length(targetDaoRetrieveEntitiesResponse.getTargetEntities()) == 0) {
      return null;
    }
    
    return targetDaoRetrieveEntitiesResponse.getTargetEntities().get(0).getId();
    
  }
  
  @Override
  public TargetDaoRetrieveMembershipsByEntityResponse retrieveMembershipsByEntity(TargetDaoRetrieveMembershipsByEntityRequest targetDaoRetrieveMembershipsByEntityRequest) {
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoRetrieveMembershipsByEntityRequest.getTargetEntity();

    try {
      TeamDynamixProvisioningConfiguration teamDynamixConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      String targetEntityId = resolveTargetEntityId(targetEntity);
      
      if (StringUtils.isBlank(targetEntityId)) {
        return new TargetDaoRetrieveMembershipsByEntityResponse(new ArrayList<ProvisioningMembership>());
      }

      List<TeamDynamixGroup> teamDynamixGroups = TeamDynamixApiCommands.retrieveTeamDynamixGroupsByUser(teamDynamixConfiguration.getTeamDynamixExternalSystemConfigId(), targetEntityId);
      
      List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();
      List<GrouperProvisioningTargetNativeMembership> nativeMemberships = new ArrayList<GrouperProvisioningTargetNativeMembership>();

      for (TeamDynamixGroup teamDynamixGroup : teamDynamixGroups) {

        ProvisioningMembership targetMembership = new ProvisioningMembership(false);
        targetMembership.setProvisioningGroupId(teamDynamixGroup.getId());
        targetMembership.setProvisioningEntityId(targetEntity.getId());
        provisioningMemberships.add(targetMembership);

        // generic provisioner sync back: scoped membership-by-entity path
        GrouperProvisioningTargetNativeMembership nativeMembership = new GrouperProvisioningTargetNativeMembership();
        nativeMembership.setTargetGroupId(teamDynamixGroup.getId());
        nativeMembership.setTargetUserId(targetEntityId);
        nativeMemberships.add(nativeMembership);
      }
      if (!nativeMemberships.isEmpty()) {
        GrouperProvisioner provisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
        if (provisioner != null) {
          provisioner.retrieveGrouperProvisioningTargetNativeSync()
              .recordTargetNativeMemberships(nativeMemberships);
        }
      }

      return new TargetDaoRetrieveMembershipsByEntityResponse(provisioningMemberships);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByEntity", startNanos));
    }
  }
  
  public String resolveTargetGroupId(ProvisioningGroup targetGroup) {
    
    if (targetGroup == null) {
      return null;
    }
    
    if (StringUtils.isNotBlank(targetGroup.getId())) {
      return targetGroup.getId();
    }
    
    if (StringUtils.isNotBlank(targetGroup.retrieveAttributeValueString("ID"))) {
      return targetGroup.retrieveAttributeValueString("ID");
    }
    
    TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest = new TargetDaoRetrieveGroupsRequest();
    targetDaoRetrieveGroupsRequest.setTargetGroups(GrouperUtil.toList(targetGroup));
    targetDaoRetrieveGroupsRequest.setIncludeAllMembershipsIfApplicable(false);
    TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().retrieveGroups(
        targetDaoRetrieveGroupsRequest);

    if (targetDaoRetrieveGroupsResponse == null || GrouperUtil.length(targetDaoRetrieveGroupsResponse.getTargetGroups()) == 0) {
      return null;
    }
    
    return targetDaoRetrieveGroupsResponse.getTargetGroups().get(0).getId();
    
  }
  
  @Override
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();

    try {
      TeamDynamixProvisioningConfiguration teamDynamixConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      String targetGroupId = resolveTargetGroupId(targetGroup);
      
      if (StringUtils.isBlank(targetGroupId)) {
        return new TargetDaoRetrieveMembershipsByGroupResponse(new ArrayList<ProvisioningMembership>());
      }
      
      List<TeamDynamixUser> teamDynamixUsers = TeamDynamixApiCommands.retrieveTeamDynamixUsersByGroup(teamDynamixConfiguration.getTeamDynamixExternalSystemConfigId(), targetGroupId);

      List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();

      for (TeamDynamixUser teamDynamixUser : teamDynamixUsers) {

        ProvisioningMembership targetMembership = new ProvisioningMembership(false);
        targetMembership.setProvisioningGroupId(targetGroup.getId());
        targetMembership.setProvisioningEntityId(teamDynamixUser.getId());
        provisioningMemberships.add(targetMembership);

      }
      // generic provisioner sync back: capture native memberships for this group
      TeamDynamixProvisioningTargetNativeSync.captureMembershipsForGroupForCurrentProvisioner(
          targetGroupId, teamDynamixUsers);

      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByGroup", startNanos));
    }
    
  }
  
  @Override
  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();

    try {
      TeamDynamixProvisioningConfiguration teamDynamixConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      // lets make sure we are doing the right thing
      Map<String, ProvisioningObjectChangeAction> fieldNamesToProvisioningObjectChangeAction = new HashMap<String, ProvisioningObjectChangeAction>();
      
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToProvisioningObjectChangeAction.put(fieldName, provisioningObjectChange.getProvisioningObjectChangeAction());
      }
      
      TeamDynamixUser teamDynamixUser = TeamDynamixUser.fromProvisioningEntity(targetEntity, null);
      TeamDynamixApiCommands.patchTeamDynamixUser(teamDynamixConfiguration.getTeamDynamixExternalSystemConfigId(), teamDynamixUser, fieldNamesToProvisioningObjectChangeAction);

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
  public TargetDaoUpdateGroupResponse updateGroup(
      TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();

    try {
      TeamDynamixProvisioningConfiguration teamDynamixConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      // lets make sure we are doing the right thing
      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToUpdate.add(fieldName);
      }
      
      TeamDynamixGroup teamDynamixGroup = TeamDynamixGroup.fromProvisioningGroup(targetGroup, null);
      TeamDynamixApiCommands.updateTeamDynamixGroup(teamDynamixConfiguration.getTeamDynamixExternalSystemConfigId(), teamDynamixGroup, null);

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

}
