package edu.internet2.middleware.grouper.app.teamDynamix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

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
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

public class TeamDynamixTargetDao extends GrouperProvisionerTargetDaoBase {
  
  /**
   * cache of owner identifier to owner url
   */
  private static ExpirableCache<Object, String> ownerIdentifierToOwnerUrl = new ExpirableCache<Object, String>(60);


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
    
    //TODO think about batch sizes
    grouperProvisionerDaoCapabilities.setDefaultBatchSize(1);
    grouperProvisionerDaoCapabilities.setRetrieveEntitiesBatchSize(1);
    grouperProvisionerDaoCapabilities.setRetrieveGroupsBatchSize(1);
    grouperProvisionerDaoCapabilities.setRetrieveMembershipsBatchSize(1);
//    grouperProvisionerDaoCapabilities.setInsertMembershipsBatchSize(400);
    
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
    
  }

  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(
      TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {

    long startNanos = System.nanoTime();

    try {

      TeamDynamixProvisioningConfiguration teamDynamixConfiguration = (TeamDynamixProvisioningConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

      List<TeamDynamixGroup> grouperAzureGroups = TeamDynamixApiCommands
          .retrieveTeamDynamixGroups(teamDynamixConfiguration.getTeamDynamixExternalSystemConfigId());

      for (TeamDynamixGroup grouperAzureGroup : grouperAzureGroups) {
        ProvisioningGroup targetGroup = grouperAzureGroup.toProvisioningGroup();
        results.add(targetGroup);
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

      TeamDynamixProvisioningConfiguration azureConfiguration = (TeamDynamixProvisioningConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

      List<TeamDynamixUser> grouperAzureUsers = TeamDynamixApiCommands
          .retrieveTeamDynamixUsers(azureConfiguration.getTeamDynamixExternalSystemConfigId());

      for (TeamDynamixUser grouperAzureUser : grouperAzureUsers) {
        ProvisioningEntity targetEntity = grouperAzureUser.toProvisioningEntity();
        results.add(targetEntity);
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
      TeamDynamixProvisioningConfiguration duoConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      TeamDynamixGroup grouperDuoGroup = TeamDynamixGroup.fromProvisioningGroup(targetGroup, null);
      grouperDuoGroup.setActive(true);
      
      TeamDynamixGroup createdDuoGroup = TeamDynamixApiCommands.createTeamDynamixGroup(duoConfiguration.getTeamDynamixExternalSystemConfigId(), grouperDuoGroup);

      targetGroup.setId(createdDuoGroup.getId());
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
      TeamDynamixProvisioningConfiguration duoConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      TeamDynamixUser grouperDuoUser = TeamDynamixUser.fromProvisioningEntity(targetEntity, null);
      
      String userId = TeamDynamixApiCommands.createTeamDynamixUser(duoConfiguration.getTeamDynamixExternalSystemConfigId(), grouperDuoUser);

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
      
      TeamDynamixProvisioningConfiguration azureConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      // lets collate by group
      Map<String, List<String>> groupIdToUserIds = new LinkedHashMap<String, List<String>>();

      // keep track to mark as complete
      Map<MultiKey, ProvisioningMembership> groupIdUserIdToProvisioningMembership = new HashMap<MultiKey, ProvisioningMembership>();
      
      for (ProvisioningMembership targetMembership : targetMemberships) {

        groupIdUserIdToProvisioningMembership.put(new MultiKey(targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntityId()), targetMembership);
        
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
          TeamDynamixApiCommands.createTeamDynamixMemberships(azureConfiguration.getTeamDynamixExternalSystemConfigId(), groupId, userIds);
          
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
      TeamDynamixProvisioningConfiguration  azureConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      TeamDynamixApiCommands.deleteTeamDynamixMemberships(azureConfiguration.getTeamDynamixExternalSystemConfigId(), targetMemberships);

      for (ProvisioningMembership targetMembership: targetMemberships) {
        
        targetMembership.setProvisioned(true);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(true);
        }
        
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
      
      throw new RuntimeException("Failed to delete Azure memberships", e);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMemberships", startNanos));
    }
  }
//
//  @Override
//  public TargetDaoUpdateGroupsResponse updateGroups(TargetDaoUpdateGroupsRequest targetDaoUpdateGroupsRequest) {
//    
//    long startNanos = System.nanoTime();
//    List<ProvisioningGroup> targetGroups = targetDaoUpdateGroupsRequest.getTargetGroups();
//    
//    Map<GrouperAzureGroup, ProvisioningGroup> azureGroupToTargetGroup = new HashMap<>();
//    Map<GrouperAzureGroup, Set<String>> azureGroupToFieldNamesToUpdate = new HashMap<>();
//    
//    for (ProvisioningGroup targetGroup: targetGroups) {
//      GrouperAzureGroup grouperAzureGroup = GrouperAzureGroup.fromProvisioningGroup(targetGroup, null);
//      azureGroupToTargetGroup.put(grouperAzureGroup, targetGroup);
//      
//      Set<String> fieldNamesToUpdate = new HashSet<String>();
//      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
//        String fieldName = provisioningObjectChange.getAttributeName();
//        fieldNamesToUpdate.add(fieldName);
//      }
//      
//      azureGroupToFieldNamesToUpdate.put(grouperAzureGroup, fieldNamesToUpdate);
//      
//    }
//    
//    try {
//      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//      
//      Map<GrouperAzureGroup, Exception> groupToMaybeException = GrouperAzureApiCommands.updateAzureGroups(azureConfiguration.getAzureExternalSystemConfigId(), azureGroupToFieldNamesToUpdate);
//      
//      for (GrouperAzureGroup grouperAzureGroup: groupToMaybeException.keySet()) {
//        
//        Exception exception = groupToMaybeException.get(grouperAzureGroup);
//        ProvisioningGroup targetGroup = azureGroupToTargetGroup.get(grouperAzureGroup);
//        
//        if (exception == null) {
//          
//          targetGroup.setProvisioned(true);
//
//          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
//            provisioningObjectChange.setProvisioned(true);
//          }
//        } else {
//          targetGroup.setProvisioned(false);
//          targetGroup.setException(exception);
//          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
//            provisioningObjectChange.setProvisioned(false);
//          }
//        }
//        
//      }
//      
//      return new TargetDaoUpdateGroupsResponse();
//    } catch (Exception e) {
//      
//      for (ProvisioningGroup targetGroup: targetGroups) {
//        targetGroup.setProvisioned(false);
//        targetGroup.setException(e);
//        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
//          provisioningObjectChange.setProvisioned(false);
//        }
//      }
//      
//      throw e;
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateEntities", startNanos));
//    }
//    
//    
//  }
//  
//  @Override
//  public TargetDaoUpdateEntitiesResponse updateEntities(TargetDaoUpdateEntitiesRequest targetDaoUpdateEntitiesRequest) {
//    
//    long startNanos = System.nanoTime();
//    List<ProvisioningEntity> targetEntities = targetDaoUpdateEntitiesRequest.getTargetEntities();
//    
//    Map<GrouperAzureUser, ProvisioningEntity> azureUserToTargetEntity = new HashMap<>();
//    Map<GrouperAzureUser, Set<String>> azureUserToFieldNamesToUpdate = new HashMap<>();
//    
//    for (ProvisioningEntity targetEntity: targetEntities) {
//      GrouperAzureUser grouperAzureUser = GrouperAzureUser.fromProvisioningEntity(targetEntity, null);
//      azureUserToTargetEntity.put(grouperAzureUser, targetEntity);
//      
//      Set<String> fieldNamesToUpdate = new HashSet<String>();
//      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
//        String fieldName = provisioningObjectChange.getAttributeName();
//        fieldNamesToUpdate.add(fieldName);
//      }
//      
//      azureUserToFieldNamesToUpdate.put(grouperAzureUser, fieldNamesToUpdate);
//      
//    }
//    
//    try {
//      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//      
//      Map<GrouperAzureUser, Exception> userToMaybeException = GrouperAzureApiCommands.updateAzureUsers(azureConfiguration.getAzureExternalSystemConfigId(), azureUserToFieldNamesToUpdate);
//      
//      for (GrouperAzureUser grouperAzureUser: userToMaybeException.keySet()) {
//        
//        Exception exception = userToMaybeException.get(grouperAzureUser);
//        ProvisioningEntity targetEntity = azureUserToTargetEntity.get(grouperAzureUser);
//        
//        if (exception == null) {
//          
//          targetEntity.setProvisioned(true);
//
//          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
//            provisioningObjectChange.setProvisioned(true);
//          }
//        } else {
//          targetEntity.setProvisioned(false);
//          targetEntity.setException(exception);
//          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
//            provisioningObjectChange.setProvisioned(false);
//          }
//        }
//        
//      }
//      
//      return new TargetDaoUpdateEntitiesResponse();
//    } catch (Exception e) {
//      
//      for (ProvisioningEntity targetEntity: targetEntities) {
//        targetEntity.setProvisioned(false);
//        targetEntity.setException(e);
//        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
//          provisioningObjectChange.setProvisioned(false);
//        }
//      }
//      
//      throw e;
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateEntities", startNanos));
//    }
//    
//  }
//
//  
//  @Override
//  public TargetDaoDeleteGroupsResponse deleteGroups(TargetDaoDeleteGroupsRequest targetDaoDeleteGroupsRequest) {
//    
//    long startNanos = System.nanoTime();
//    List<ProvisioningGroup> targetGroups = targetDaoDeleteGroupsRequest.getTargetGroups();
//
//    try {
//      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      
//      List<GrouperAzureGroup> grouperAzureGroups = new ArrayList<>();
//      
//      Map<GrouperAzureGroup, ProvisioningGroup> azureGroupToTargetGroup = new HashMap<>();
//      
//      for (ProvisioningGroup targetGroup: targetGroups) {
//        GrouperAzureGroup grouperAzureGroup = GrouperAzureGroup.fromProvisioningGroup(targetGroup, null);
//        grouperAzureGroups.add(grouperAzureGroup);
//        azureGroupToTargetGroup.put(grouperAzureGroup, targetGroup);
//      }
//      
//      Map<GrouperAzureGroup, Exception> azureGroupsToMaybeException = GrouperAzureApiCommands.deleteAzureGroups(azureConfiguration.getAzureExternalSystemConfigId(), grouperAzureGroups);
//      
//      for (GrouperAzureGroup azureGroup: azureGroupsToMaybeException.keySet()) {
//        
//        Exception exception = azureGroupsToMaybeException.get(azureGroup);
//
//        ProvisioningGroup targetGroup = azureGroupToTargetGroup.get(azureGroup);
//        if (exception == null) {
//          targetGroup.setProvisioned(true);
//          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
//            provisioningObjectChange.setProvisioned(true);
//          }
//        } else {
//          targetGroup.setProvisioned(false);
//          targetGroup.setException(exception);
//          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
//            provisioningObjectChange.setProvisioned(false);
//          }
//        }
//        
//      }
//    
//      return new TargetDaoDeleteGroupsResponse();
//    } catch (Exception e) {
//     
//      for (ProvisioningGroup targetGroup: targetGroups) {
//        targetGroup.setProvisioned(false);
//        targetGroup.setException(e);
//        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
//          provisioningObjectChange.setProvisioned(false);
//        }
//      }
//      
//      throw e;
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteGroup", startNanos));
//    }
//  }
//    
//  @Override
//  public TargetDaoRetrieveMembershipsByEntityResponse retrieveMembershipsByEntity(
//      TargetDaoRetrieveMembershipsByEntityRequest targetDaoRetrieveMembershipsByEntityRequest) {
//    long startNanos = System.nanoTime();
//    
//    ProvisioningEntity targetEntity = targetDaoRetrieveMembershipsByEntityRequest.getTargetEntity();
//
//    String targetEntityId = resolveTargetEntityId(targetEntity);
//    List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();
//    
//    if (StringUtils.isBlank(targetEntityId)) {
//      return new TargetDaoRetrieveMembershipsByEntityResponse(provisioningMemberships);
//    }
//
//    try {
//      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      Set<String> groupIds = GrouperAzureApiCommands.retrieveAzureUserGroups(azureConfiguration.getAzureExternalSystemConfigId(), targetEntityId);
//      
//      for (String groupId : groupIds) {
//
//        ProvisioningMembership targetMembership = new ProvisioningMembership();
//        targetMembership.setProvisioningGroupId(groupId);
//        targetMembership.setProvisioningEntityId(targetEntity.getId());
//        provisioningMemberships.add(targetMembership);
//      }
//  
//      return new TargetDaoRetrieveMembershipsByEntityResponse(provisioningMemberships);
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByEntity", startNanos));
//    }
//  }
//
//  
//  public String resolveTargetGroupId(ProvisioningGroup targetGroup) {
//    
//    if (targetGroup == null) {
//      return null;
//    }
//    
//    if (StringUtils.isNotBlank(targetGroup.getId())) {
//      return targetGroup.getId();
//    }
//    
//    TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest = new TargetDaoRetrieveGroupsRequest();
//    targetDaoRetrieveGroupsRequest.setTargetGroups(GrouperUtil.toList(targetGroup));
//    targetDaoRetrieveGroupsRequest.setIncludeAllMembershipsIfApplicable(false);
//    TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().retrieveGroups(
//        targetDaoRetrieveGroupsRequest);
//
//    if (targetDaoRetrieveGroupsResponse == null || GrouperUtil.length(targetDaoRetrieveGroupsResponse.getTargetGroups()) == 0) {
//      return null;
//    }
//    
//    return targetDaoRetrieveGroupsResponse.getTargetGroups().get(0).getId();
//    
//  }
//  
//  
//  @Override
//  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
//    long startNanos = System.nanoTime();
//    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();
//    
//    String targetGroupId = resolveTargetGroupId(targetGroup);
//    List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();
//    
//    if (StringUtils.isBlank(targetGroupId)) {
//      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
//    }
//    
//    try {
//      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      Set<String> userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers(azureConfiguration.getAzureExternalSystemConfigId(), targetGroupId);
//      
//      for (String userId : userIds) {
//
//        ProvisioningMembership targetMembership = new ProvisioningMembership();
//        targetMembership.setProvisioningGroupId(targetGroup.getId());
//        targetMembership.setProvisioningEntityId(userId);
//        provisioningMemberships.add(targetMembership);
//      }
//  
//      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByGroup", startNanos));
//    }
//  }
//  
//  @Override
//  public TargetDaoInsertEntitiesResponse insertEntities(TargetDaoInsertEntitiesRequest targetDaoInsertEntitiesRequest) {
//   
//    
//    long startNanos = System.nanoTime();
//    List<ProvisioningEntity> targetEntities = targetDaoInsertEntitiesRequest.getTargetEntityInserts();
//
//    try {
//      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      List<GrouperAzureUser> grouperAzureUsers = new ArrayList<>();
//      
//      Map<GrouperAzureUser, ProvisioningEntity> azureUserToTargetEntity = new HashMap<>();
//      for (ProvisioningEntity targetEntity: targetEntities) {
//        GrouperAzureUser grouperAzureUser = GrouperAzureUser.fromProvisioningEntity(targetEntity, null);
//        grouperAzureUsers.add(grouperAzureUser);
//        azureUserToTargetEntity.put(grouperAzureUser, targetEntity);
//      }
//      
//      Map<GrouperAzureUser, Exception> userToMaybeException = GrouperAzureApiCommands.createAzureUsers(azureConfiguration.getAzureExternalSystemConfigId(), grouperAzureUsers, null);
//      
//      for (GrouperAzureUser grouperAzureUser: userToMaybeException.keySet()) {
//        
//        Exception exception = userToMaybeException.get(grouperAzureUser);
//        ProvisioningEntity targetEntity = azureUserToTargetEntity.get(grouperAzureUser);
//        
//        if (exception == null) {
//          
//          targetEntity.setId(grouperAzureUser.getId());
//          targetEntity.setProvisioned(true);
//
//          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
//            provisioningObjectChange.setProvisioned(true);
//          }
//        } else {
//          targetEntity.setProvisioned(false);
//          targetEntity.setException(exception);
//          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
//            provisioningObjectChange.setProvisioned(false);
//          }
//        }
//        
//      }
//
//      return new TargetDaoInsertEntitiesResponse();
//    } catch (Exception e) {
//      for (ProvisioningEntity targetEntity: targetEntities) {
//        targetEntity.setProvisioned(false);
//        targetEntity.setException(e);
//        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
//          provisioningObjectChange.setProvisioned(false);
//        }
//      }
//      
//      throw e;
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertEntities", startNanos));
//    }
//    
//    
//  }
//
//  @Override
//  public void registerGrouperProvisionerDaoCapabilities(
//      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
//    
//    grouperProvisionerDaoCapabilities.setDefaultBatchSize(20);
//    
//    grouperProvisionerDaoCapabilities.setCanDeleteGroups(true);
//    
//    grouperProvisionerDaoCapabilities.setCanDeleteEntities(true);
//
//    grouperProvisionerDaoCapabilities.setCanDeleteMemberships(true);
//
//    grouperProvisionerDaoCapabilities.setCanInsertEntities(true);
//
//    grouperProvisionerDaoCapabilities.setCanInsertGroups(true);
//
//    grouperProvisionerDaoCapabilities.setCanInsertMemberships(true);
//    grouperProvisionerDaoCapabilities.setInsertMembershipsBatchSize(400);
//
//    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
//    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
////    grouperProvisionerDaoCapabilities.setCanRetrieveAllMemberships(true);
//
//    grouperProvisionerDaoCapabilities.setCanRetrieveEntities(true);
//
//    grouperProvisionerDaoCapabilities.setCanRetrieveGroups(true);
//
//    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByEntity(true);
//    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByGroup(true);
//
//    grouperProvisionerDaoCapabilities.setCanUpdateEntities(true);
//
//    grouperProvisionerDaoCapabilities.setCanUpdateGroups(true);
//  }
//
//  public String resolveTargetEntityId(ProvisioningEntity targetEntity) {
//    
//    if (targetEntity == null) {
//      return null;
//    }
//    
//    if (StringUtils.isNotBlank(targetEntity.getId())) {
//      return targetEntity.getId();
//    }
//    
//    TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest = new TargetDaoRetrieveEntitiesRequest();
//    targetDaoRetrieveEntitiesRequest.setTargetEntities(GrouperUtil.toList(targetEntity));
//    targetDaoRetrieveEntitiesRequest.setIncludeAllMembershipsIfApplicable(false);
//    TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().retrieveEntities(
//        targetDaoRetrieveEntitiesRequest);
//
//    if (targetDaoRetrieveEntitiesResponse == null || GrouperUtil.length(targetDaoRetrieveEntitiesResponse.getTargetEntities()) == 0) {
//      return null;
//    }
//    
//    return targetDaoRetrieveEntitiesResponse.getTargetEntities().get(0).getId();
//    
//  }
//
//  @Override
//  public TargetDaoDeleteEntitiesResponse deleteEntities(TargetDaoDeleteEntitiesRequest targetDaoDeleteEntitiesRequest) {
//    
//    long startNanos = System.nanoTime();
//    List<ProvisioningEntity> targetEntities = targetDaoDeleteEntitiesRequest.getTargetEntities();
//  
//    try {
//      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//  
//      List<GrouperAzureUser> grouperAzureUsers = new ArrayList<>();
//      
//      Map<GrouperAzureUser, ProvisioningEntity> azureUserToTargetEntity = new HashMap<>();
//      
//      for (ProvisioningEntity targetEntity: targetEntities) {
//        GrouperAzureUser grouperAzureUser = GrouperAzureUser.fromProvisioningEntity(targetEntity, null);
//        grouperAzureUsers.add(grouperAzureUser);
//        azureUserToTargetEntity.put(grouperAzureUser, targetEntity);
//      }
//      
//      Map<GrouperAzureUser, Exception> azureUsersToMaybeException = GrouperAzureApiCommands.deleteAzureUsers(azureConfiguration.getAzureExternalSystemConfigId(), grouperAzureUsers);
//      
//      for (GrouperAzureUser azureUser: azureUsersToMaybeException.keySet()) {
//        
//        Exception exception = azureUsersToMaybeException.get(azureUser);
//
//        ProvisioningEntity targetEntity = azureUserToTargetEntity.get(azureUser);
//        if (exception == null) {
//          targetEntity.setProvisioned(true);
//          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
//            provisioningObjectChange.setProvisioned(true);
//          }
//        } else {
//          targetEntity.setProvisioned(false);
//          targetEntity.setException(exception);
//          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
//            provisioningObjectChange.setProvisioned(false);
//          }
//        }
//        
//      }
//      
//      return new TargetDaoDeleteEntitiesResponse();
//    } catch (Exception e) {
//      for (ProvisioningEntity targetEntity: targetEntities) {
//        targetEntity.setProvisioned(false);
//        targetEntity.setException(e);
//        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
//          provisioningObjectChange.setProvisioned(false);
//        }
//      }
//      throw e;
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteEntities", startNanos));
//    }
//  }
  
  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();

    try {
      TeamDynamixProvisioningConfiguration duoConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      TeamDynamixUser grouperDuoUser = TeamDynamixUser.fromProvisioningEntity(targetEntity, null);
      
      TeamDynamixApiCommands.updateTeamDynamixUserStatus(duoConfiguration.getTeamDynamixExternalSystemConfigId(),
          grouperDuoUser.getId(), false);

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
      TeamDynamixProvisioningConfiguration duoConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      TeamDynamixGroup grouperDuoGroup = TeamDynamixGroup.fromProvisioningGroup(targetGroup, null);
      grouperDuoGroup.setActive(false);
      
      TeamDynamixApiCommands.updateTeamDynamixGroup(duoConfiguration.getTeamDynamixExternalSystemConfigId(), grouperDuoGroup, null);
      
      targetGroup.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }
      
//      Map<String, GrouperDuoGroup> groupNameToGroup = cacheGroupNameToGroup.get(Boolean.TRUE);
//      if (groupNameToGroup != null) {
//        groupNameToGroup.remove(grouperDuoGroup.getName());
//      } 
      
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
      TeamDynamixProvisioningConfiguration duoConfiguration = (TeamDynamixProvisioningConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      // we can retrieve by uid or external id, prefer uid

      TeamDynamixUser grouperDuoUser = null;

      if (StringUtils.equals("id", targetDaoRetrieveEntityRequest.getSearchAttribute())) {
        grouperDuoUser = TeamDynamixApiCommands.retrieveTeamDynamixUser(duoConfiguration.getTeamDynamixExternalSystemConfigId(), 
            GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue()));
      } else if (StringUtils.equals("ExternalID", targetDaoRetrieveEntityRequest.getSearchAttribute())) {
        grouperDuoUser = TeamDynamixApiCommands.retrieveTeamDynamixUserBySearchTerm(
            duoConfiguration.getTeamDynamixExternalSystemConfigId(), 
            "externalId", 
            GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue()), true);
      } else {
        throw new RuntimeException("Not expecting search attribute '" + targetDaoRetrieveEntityRequest.getSearchAttribute() + "'");
      }
      
      ProvisioningEntity targetEntity = grouperDuoUser == null ? null
          : grouperDuoUser.toProvisioningEntity();

      TargetDaoRetrieveEntityResponse targetDaoRetrieveEntityResponse = new TargetDaoRetrieveEntityResponse(targetEntity);
      if (targetDaoRetrieveEntityRequest.isIncludeNativeEntity()) {
        targetDaoRetrieveEntityResponse.setTargetNativeEntity(grouperDuoUser);
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
      TeamDynamixProvisioningConfiguration duoConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      ProvisioningGroup grouperTargetGroup = targetDaoRetrieveGroupRequest.getTargetGroup();

      TeamDynamixGroup grouperDuoGroup = null;

      if (StringUtils.equals("Id", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        grouperDuoGroup = TeamDynamixApiCommands.retrieveTeamDynamixGroup(duoConfiguration.getTeamDynamixExternalSystemConfigId(), GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()));
      } else if (StringUtils.equals("Name", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        String name = GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue());
        if (StringUtils.isNotBlank(name)) {
          grouperDuoGroup = TeamDynamixApiCommands.retrieveTeamDynamixGroupByName(
              duoConfiguration.getTeamDynamixExternalSystemConfigId(), GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()));
        }
        
      } else {
        throw new RuntimeException("Not expecting search attribute '" + targetDaoRetrieveGroupRequest.getSearchAttribute() + "'");
      }

      ProvisioningGroup targetGroup = grouperDuoGroup == null ? null : grouperDuoGroup.toProvisioningGroup();

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
      TeamDynamixProvisioningConfiguration duoConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      String targetEntityId = resolveTargetEntityId(targetEntity);
      
      if (StringUtils.isBlank(targetEntityId)) {
        return new TargetDaoRetrieveMembershipsByEntityResponse(new ArrayList<ProvisioningMembership>());
      }

      List<TeamDynamixGroup> duoGroups = TeamDynamixApiCommands.retrieveTeamDynamixGroupsByUser(duoConfiguration.getTeamDynamixExternalSystemConfigId(), targetEntityId);
      
      List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();
      
      for (TeamDynamixGroup duoGroup : duoGroups) {

        ProvisioningMembership targetMembership = new ProvisioningMembership();
        targetMembership.setProvisioningGroupId(duoGroup.getId());
        targetMembership.setProvisioningEntityId(targetEntity.getId());
        provisioningMemberships.add(targetMembership);
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
      TeamDynamixProvisioningConfiguration duoConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      String targetGroupId = resolveTargetGroupId(targetGroup);
      
      if (StringUtils.isBlank(targetGroupId)) {
        return new TargetDaoRetrieveMembershipsByGroupResponse(new ArrayList<ProvisioningMembership>());
      }
      
      List<TeamDynamixUser> duoUsers = TeamDynamixApiCommands.retrieveTeamDynamixUsersByGroup(duoConfiguration.getTeamDynamixExternalSystemConfigId(), targetGroupId);
      
      List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>(); 
      
      for (TeamDynamixUser duoUser : duoUsers) {

        ProvisioningMembership targetMembership = new ProvisioningMembership();
        targetMembership.setProvisioningGroupId(targetGroup.getId());
        targetMembership.setProvisioningEntityId(duoUser.getId());
        provisioningMemberships.add(targetMembership);
        
      }
  
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
      TeamDynamixProvisioningConfiguration duoConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      // lets make sure we are doing the right thing
      Map<String, ProvisioningObjectChangeAction> fieldNamesToProvisioningObjectChangeAction = new HashMap<String, ProvisioningObjectChangeAction>();
      
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToProvisioningObjectChangeAction.put(fieldName, provisioningObjectChange.getProvisioningObjectChangeAction());
      }
      
      TeamDynamixUser grouperDuoUser = TeamDynamixUser.fromProvisioningEntity(targetEntity, null);
      TeamDynamixApiCommands.patchTeamDynamixUser(duoConfiguration.getTeamDynamixExternalSystemConfigId(), grouperDuoUser, fieldNamesToProvisioningObjectChangeAction);

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
      TeamDynamixProvisioningConfiguration duoConfiguration = (TeamDynamixProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      // lets make sure we are doing the right thing
      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToUpdate.add(fieldName);
      }
      
      TeamDynamixGroup grouperDuoGroup = TeamDynamixGroup.fromProvisioningGroup(targetGroup, null);
      grouperDuoGroup.setActive(true);
      TeamDynamixApiCommands.updateTeamDynamixGroup(duoConfiguration.getTeamDynamixExternalSystemConfigId(), grouperDuoGroup, null);

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
