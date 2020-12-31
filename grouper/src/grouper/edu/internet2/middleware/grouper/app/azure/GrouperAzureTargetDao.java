package edu.internet2.middleware.grouper.app.azure;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;

public class GrouperAzureTargetDao extends GrouperProvisionerTargetDaoBase {

  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    
    long startNanos = System.nanoTime();

    try {
      
      //TODO
      //boolean includeAllMembershipsIfApplicable = targetDaoRetrieveAllGroupsRequest == null ? false : targetDaoRetrieveAllGroupsRequest.isIncludeAllMembershipsIfApplicable();
      
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups(azureConfiguration.getAzureExternalSystemConfigId());
      
      for (GrouperAzureGroup grouperAzureGroup : grouperAzureGroups) {
        ProvisioningGroup targetGroup = grouperAzureGroup.toProvisioningGroup();
        results.add(targetGroup);
      }
  
      return new TargetDaoRetrieveAllGroupsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllGroups", startNanos));
    }
  }
  
//  //@Override
//  //public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {}
//
//  @Override
//  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {
//    long startNanos = System.nanoTime();
//    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();
//
//    try {
//      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      GraphApiClient apiClient = AzureGrouperExternalSystem.retrieveApiConnectionForProvisioning(azureConfiguration.getAzureExternalSystemConfigId());
//
//      AzureGraphGroup azureGroup = apiClient.addGroup(targetGroup.getDisplayName(), "?mailNickname", "?description");
//
//      targetGroup.setProvisioned(true);
//      /* ??? */ targetGroup.setId(azureGroup.id);
//      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
//        provisioningObjectChange.setProvisioned(true);
//      }
//
//      return new TargetDaoInsertGroupResponse();
//    } catch (Exception e) {
//      targetGroup.setProvisioned(false);
//      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
//        provisioningObjectChange.setProvisioned(false);
//      }
//      
//      throw e;
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertGroup", startNanos));
//    }
//  }
//  
//  @Override
//  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {
//    
//    long startNanos = System.nanoTime();
//    ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest.getTargetGroup();
//
//    try {
//      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      GraphApiClient apiClient = AzureGrouperExternalSystem.retrieveApiConnectionForProvisioning(azureConfiguration.getAzureExternalSystemConfigId());
//
//      apiClient.removeGroup(targetGroup.getId());
//
//      // todo handle missing group
//
//      targetGroup.setProvisioned(true);
//      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
//        provisioningObjectChange.setProvisioned(true);
//      }
//      return new TargetDaoDeleteGroupResponse();
//    } catch (Exception e) {
//      targetGroup.setProvisioned(false);
//      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
//        provisioningObjectChange.setProvisioned(false);
//      }
//      throw e;
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteGroup", startNanos));
//    }
//  }
//  
//  //@Override
//  //public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {
//  //}
//  
//  @Override
//  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
//    long startNanos = System.nanoTime();
//    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();
//
//    try {
//      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      GraphApiClient apiClient = AzureGrouperExternalSystem.retrieveApiConnectionForProvisioning(azureConfiguration.getAzureExternalSystemConfigId());
//
//      List<AzureGraphGroupMember> azureGroupMembers = null;
//      try {
//        azureGroupMembers = apiClient.getGroupMembers(targetGroup.getId());
//      } catch (IOException e) {
//        throw new RuntimeException("Failed to retrieve Azure group members (groupId '" + targetGroup.getId() + "'", e);
//      }
//
//      List<Object> results = new ArrayList<>();
//
//      for (AzureGraphGroupMember azureUser : azureGroupMembers) {
//        ProvisioningEntity targetEntity = new ProvisioningEntity();
//        targetEntity.setLoginId(azureUser.getUserPrincipalName());
//
//        ProvisioningMembership targetMembership = new ProvisioningMembership();
//        targetMembership.setId(azureUser.getId());
//        targetMembership.setProvisioningGroup(targetGroup);
//        targetMembership.setProvisioningEntity(targetEntity);
//    
//        results.add(targetMembership);
//      }
//  
//      return new TargetDaoRetrieveMembershipsByGroupResponse(results);
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByGroup", startNanos));
//    }
//  }
//
//  @Override
//  public TargetDaoRetrieveAllMembershipsResponse retrieveAllMemberships(TargetDaoRetrieveAllMembershipsRequest targetDaoRetrieveAllMembershipsRequest) {
//    long startNanos = System.nanoTime();
//
//    try {
//      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      GraphApiClient apiClient = AzureGrouperExternalSystem.retrieveApiConnectionForProvisioning(azureConfiguration.getAzureExternalSystemConfigId());
//
//      List<AzureGraphUser> azureUsers = null;
//      try {
//        azureUsers = apiClient.getAllUsers();
//      } catch (IOException e) {
//        throw new RuntimeException("Failed to retrieve full list of users from Azure", e);
//      }
//
//      List<ProvisioningMembership> results = new ArrayList<>();
//
//      for (AzureGraphUser azureUser : azureUsers) {
//        ProvisioningEntity targetEntity = new ProvisioningEntity();
//        targetEntity.setLoginId(azureUser.userPrincipalName);
//
//        ProvisioningMembership targetMembership = new ProvisioningMembership();
//        targetMembership.setId(azureUser.id);
//        targetMembership.setProvisioningEntity(targetEntity);
//
//        results.add(targetMembership);
//      }
//
//      return new TargetDaoRetrieveAllMembershipsResponse(results);
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByGroup", startNanos));
//    }
//  }
//
//  @Override
//  public TargetDaoInsertMembershipResponse insertMembership(TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
//    long startNanos = System.nanoTime();
//    ProvisioningMembership targetMembership = targetDaoInsertMembershipRequest.getTargetMembership();
//
//    try {
//      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      GraphApiClient apiClient = AzureGrouperExternalSystem.retrieveApiConnectionForProvisioning(azureConfiguration.getAzureExternalSystemConfigId());
//
//      ProvisioningGroup targetGroup = targetMembership.getProvisioningGroup();
//      ProvisioningEntity targetEntity = targetMembership.getProvisioningEntity();
//
//      apiClient.addMemberToMS(targetGroup.getId(), targetEntity.getLoginId());
//
//      targetMembership.setProvisioned(true);
//      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
//        provisioningObjectChange.setProvisioned(true);
//      }
//
//      return new TargetDaoInsertMembershipResponse();
//    } catch (Exception e) {
//      targetMembership.setProvisioned(false);
//      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
//        provisioningObjectChange.setProvisioned(false);
//      }
//      
//      throw e;
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertMembership", startNanos));
//    }
//  }
//  
//  public TargetDaoDeleteMembershipResponse deleteMembership(TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {
//    long startNanos = System.nanoTime();
//    ProvisioningMembership targetMembership = targetDaoDeleteMembershipRequest.getTargetMembership();
//
//    ProvisioningGroup targetGroup = targetMembership.getProvisioningGroup();
//    ProvisioningEntity targetEntity = targetMembership.getProvisioningEntity();
//
//    try {
//      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      GraphApiClient apiClient = AzureGrouperExternalSystem.retrieveApiConnectionForProvisioning(azureConfiguration.getAzureExternalSystemConfigId());
//
//      apiClient.removeUserFromGroupInMS(targetGroup.getId(), targetEntity.getLoginId());
//
//      targetMembership.setProvisioned(true);
//      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
//        provisioningObjectChange.setProvisioned(true);
//      }
//
//      return new TargetDaoDeleteMembershipResponse();
//    } catch (Exception e) {
//      targetMembership.setProvisioned(false);
//      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
//        provisioningObjectChange.setProvisioned(false);
//      }
//      throw new RuntimeException("Failed to delete Azure group member (groupId '" + targetGroup.getId() + "', member '" + targetEntity.getLoginId() + "'", e);
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMembership", startNanos));
//    }
//  }
//  
//  @Override
//  public TargetDaoRetrieveGroupResponse retrieveGroup(TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {
//    
//    long startNanos = System.nanoTime();
//
//    try {
//      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      GraphApiClient apiClient = AzureGrouperExternalSystem.retrieveApiConnectionForProvisioning(azureConfiguration.getAzureExternalSystemConfigId());
//
//      ProvisioningGroup targetGroup = targetDaoRetrieveGroupRequest.getTargetGroup();
//
//
//      AzureGraphGroup azureGroup = null;
//      try {
//        azureGroup = apiClient.retrieveGroup(targetGroup.getId());
//      } catch (IOException e) {
//        throw new RuntimeException("Failed to retrieve Azure group (groupId '" + targetGroup.getId() + "'", e);
//      }
//
//      ProvisioningGroup responseGroup = new ProvisioningGroup();
//      responseGroup.setName(azureGroup.mailNickname);
//      responseGroup.setId(azureGroup.id);
//      responseGroup.setDisplayName(azureGroup.displayName);
//
//      return new TargetDaoRetrieveGroupResponse(responseGroup);
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
//    }
//  }
//  
//  @Override
//  public TargetDaoRetrieveEntityResponse retrieveEntity(TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {
//    
//    long startNanos = System.nanoTime();
//
//    try {
//      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      GraphApiClient apiClient = AzureGrouperExternalSystem.retrieveApiConnectionForProvisioning(azureConfiguration.getAzureExternalSystemConfigId());
//
//      ProvisioningEntity targetEntity = targetDaoRetrieveEntityRequest.getTargetEntity();
//      AzureGraphUser azureUser = apiClient.lookupMSUser(targetEntity.getLoginId());
//
//      ProvisioningEntity responseEntity = new ProvisioningEntity();
//      targetEntity.setId(azureUser.id);
//      targetEntity.setLoginId(azureUser.mailNickName);
//
//      return new TargetDaoRetrieveEntityResponse(targetEntity);
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
//    }
//  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsByGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllMemberships(true);
    grouperProvisionerDaoCapabilities.setCanInsertMembership(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMembership(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
  }
}
