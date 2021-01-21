package edu.internet2.middleware.grouper.app.scim2Provisioning;

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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GrouperScim2TargetDao extends GrouperProvisionerTargetDaoBase {

  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(
      TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {

    long startNanos = System.nanoTime();

    try {

      GrouperScim2ProvisionerConfiguration grouperScim2ProvisionerConfiguration = (GrouperScim2ProvisionerConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

      List<GrouperScim2Group> grouperScim2Groups = GrouperScim2ApiCommands
          .retrieveScimGroups(grouperScim2ProvisionerConfiguration.getBearerTokenExternalSystemConfigId());

      for (GrouperScim2Group grouperScim2Group : grouperScim2Groups) {
        ProvisioningGroup targetGroup = grouperScim2Group.toProvisioningGroup();
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

      GrouperScim2ProvisionerConfiguration grouperScim2Configuration = (GrouperScim2ProvisionerConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

      List<GrouperScim2User> grouperScim2Users = GrouperScim2ApiCommands
          .retrieveScimUsers(grouperScim2Configuration.getBearerTokenExternalSystemConfigId());

      for (GrouperScim2User grouperScim2User : grouperScim2Users) {
        ProvisioningEntity targetEntity = grouperScim2User.toProvisioningEntity();
        results.add(targetEntity);
      }

      return new TargetDaoRetrieveAllEntitiesResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(
          new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(
      TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {

    long startNanos = System.nanoTime();

    try {
      GrouperScim2ProvisionerConfiguration azureConfiguration = (GrouperScim2ProvisionerConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      // we can retrieve by id or userPrincipalName, prefer id
      ProvisioningEntity grouperTargetEntity = targetDaoRetrieveEntityRequest
          .getTargetEntity();

      GrouperScim2User grouperScim2User = null;

      if (!StringUtils.isBlank(grouperTargetEntity.getId())) {
        grouperScim2User = GrouperScim2ApiCommands.retrieveScimUser(
            azureConfiguration.getBearerTokenExternalSystemConfigId(), "id", 
            grouperTargetEntity.getId());
      }

      String userName = grouperTargetEntity
          .retrieveAttributeValueString("userName");
      if (grouperScim2User == null && !StringUtils.isBlank(userName)) {
        grouperScim2User = GrouperScim2ApiCommands.retrieveScimUser(
            azureConfiguration.getBearerTokenExternalSystemConfigId(), "userName", userName);
      }

      ProvisioningEntity targetEntity = grouperScim2User == null ? null
          : grouperScim2User.toProvisioningEntity();

      return new TargetDaoRetrieveEntityResponse(targetEntity);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(
      TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {

    long startNanos = System.nanoTime();

    try {
      GrouperScim2ProvisionerConfiguration scim2ProvisionerConfiguration = (GrouperScim2ProvisionerConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      // we can retrieve by id or displayName

      ProvisioningGroup grouperTargetGroup = targetDaoRetrieveGroupRequest
          .getTargetGroup();

      GrouperScim2Group grouperScim2Group = null;

      if (!StringUtils.isBlank(grouperTargetGroup.getId())) {
        grouperScim2Group = GrouperScim2ApiCommands.retrieveScimGroup(
            scim2ProvisionerConfiguration.getBearerTokenExternalSystemConfigId(), "id",
            grouperTargetGroup.getId());
      }

      String displayName = grouperTargetGroup.getDisplayName();
      if (grouperScim2Group == null && !StringUtils.isBlank(displayName)) {
        grouperScim2Group = GrouperScim2ApiCommands.retrieveScimGroup(
            scim2ProvisionerConfiguration.getBearerTokenExternalSystemConfigId(), "displayName", displayName);
      }

      ProvisioningGroup targetGroup = grouperScim2Group == null ? null
          : grouperScim2Group.toProvisioningGroup();

      return new TargetDaoRetrieveGroupResponse(targetGroup);

    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
    }
  }

  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();

    try {
      GrouperScim2ProvisionerConfiguration azureConfiguration = (GrouperScim2ProvisionerConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      // lets make sure we are doing the right thing
      Set<String> fieldNamesToInsert = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        String fieldName = GrouperUtil.defaultIfBlank(provisioningObjectChange.getFieldName(), provisioningObjectChange.getAttributeName());
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
          fieldNamesToInsert.add(fieldName);
        }
      }
      
      GrouperScim2Group grouperScim2Group = GrouperScim2Group.fromProvisioningGroup(targetGroup, null);
      
      GrouperScim2ApiCommands.createScimGroup(azureConfiguration.getBearerTokenExternalSystemConfigId(), grouperScim2Group, fieldNamesToInsert);

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
  public TargetDaoInsertMembershipsResponse insertMemberships(TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {
    long startNanos = System.nanoTime();
    List<ProvisioningMembership> targetMemberships = targetDaoInsertMembershipsRequest.getTargetMemberships();

    try {
      
      GrouperScim2ProvisionerConfiguration azureConfiguration = (GrouperScim2ProvisionerConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

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
          GrouperScim2ApiCommands.createScimMemberships(azureConfiguration.getBearerTokenExternalSystemConfigId(), groupId, new HashSet<String>(userIds));
        } catch (RuntimeException e) {
          runtimeException = e;
        }
        boolean success = runtimeException == null;
        for (String userId : userIds) {
          ProvisioningMembership targetMembership = groupIdUserIdToProvisioningMembership.get(new MultiKey(groupId, userId));
          
          targetMembership.setProvisioned(success);
          targetMembership.setException(runtimeException);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(success);
            
          }
        }
      }
      
      return new TargetDaoInsertMembershipsResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertMemberships", startNanos));
    }
  }

//  public TargetDaoDeleteMembershipResponse deleteMembership(TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {
//    long startNanos = System.nanoTime();
//    ProvisioningMembership targetMembership = targetDaoDeleteMembershipRequest.getTargetMembership();
//
//    try {
//      GrouperScim2ProvisionerConfiguration azureConfiguration = (GrouperScim2ProvisionerConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      GrouperAzureApiCommands.deleteAzureMembership(azureConfiguration.getBearerTokenExternalSystemConfigId(), targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntityId());
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
//      throw new RuntimeException("Failed to delete Azure group member (groupId '" + targetMembership.getProvisioningGroupId() + "', member '" + targetMembership.getProvisioningEntityId() + "'", e);
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMembership", startNanos));
//    }
//  }
//
//  @Override
//  public TargetDaoUpdateGroupResponse updateGroup(
//      TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {
//    long startNanos = System.nanoTime();
//    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();
//
//    try {
//      GrouperScim2ProvisionerConfiguration scim2Configuration = (GrouperScim2ProvisionerConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//      
//      // lets make sure we are doing the right thing
//      Set<String> fieldNamesToUpdate = new HashSet<String>();
//      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
//        String fieldName = GrouperUtil.defaultIfBlank(provisioningObjectChange.getFieldName(), provisioningObjectChange.getAttributeName());
//        fieldNamesToUpdate.add(fieldName);
//      }
//      
//      GrouperScim2Group grouperScim2Group = GrouperScim2Group.fromProvisioningGroup(targetGroup, null);
//      
//      GrouperScim2ApiCommands.(scim2Configuration.getBearerTokenExternalSystemConfigId(), grouperScim2Group, fieldNamesToUpdate);
//
//      targetGroup.setProvisioned(true);
//
//      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
//        provisioningObjectChange.setProvisioned(true);
//      }
//
//      return new TargetDaoUpdateGroupResponse();
//    } catch (Exception e) {
//      targetGroup.setProvisioned(false);
//      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
//        provisioningObjectChange.setProvisioned(false);
//      }
//      
//      throw e;
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateGroup", startNanos));
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
//      GrouperScim2ProvisionerConfiguration azureConfiguration = (GrouperScim2ProvisionerConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      GrouperAzureGroup grouperAzureGroup = GrouperAzureGroup.fromProvisioningGroup(targetGroup, null);
//      
//      GrouperAzureApiCommands.deleteAzureGroup(azureConfiguration.getBearerTokenExternalSystemConfigId(), grouperAzureGroup.getId());
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
//  @Override
//  public TargetDaoRetrieveMembershipsByEntityResponse retrieveMembershipsByEntity(
//      TargetDaoRetrieveMembershipsByEntityRequest targetDaoRetrieveMembershipsByEntityRequest) {
//    long startNanos = System.nanoTime();
//    ProvisioningEntity targetEntity = targetDaoRetrieveMembershipsByEntityRequest.getTargetEntity();
//
//    try {
//      GrouperScim2ProvisionerConfiguration azureConfiguration = (GrouperScim2ProvisionerConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      Set<String> groupIds = GrouperAzureApiCommands.retrieveAzureUserGroups(azureConfiguration.getBearerTokenExternalSystemConfigId(), targetEntity.getId());
//      
//      List<Object> provisioningMemberships = new ArrayList<Object>();
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
//  @Override
//  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
//    long startNanos = System.nanoTime();
//    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();
//
//    try {
//      GrouperScim2ProvisionerConfiguration azureConfiguration = (GrouperScim2ProvisionerConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      Set<String> userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers(azureConfiguration.getBearerTokenExternalSystemConfigId(), targetGroup.getId());
//      
//      List<Object> provisioningMemberships = new ArrayList<Object>();
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
//  public TargetDaoRetrieveAllMembershipsResponse retrieveAllMemberships(TargetDaoRetrieveAllMembershipsRequest targetDaoRetrieveAllMembershipsRequest) {
//    long startNanos = System.nanoTime();
//
//    try {
//      GrouperScim2ProvisionerConfiguration azureConfiguration = (GrouperScim2ProvisionerConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands
//          .retrieveAzureGroups(azureConfiguration.getBearerTokenExternalSystemConfigId());
//
//      List<ProvisioningMembership> results = new ArrayList<>();
//
//      for (GrouperAzureGroup grouperAzureGroup : GrouperUtil.nonNull(grouperAzureGroups)) {
//        Set<String> userIds = GrouperAzureApiCommands
//            .retrieveAzureGroupMembers(azureConfiguration.getBearerTokenExternalSystemConfigId(), grouperAzureGroup.getId());
//        for (String userId : GrouperUtil.nonNull(userIds)) {
//          
//          ProvisioningMembership targetMembership = new ProvisioningMembership();
//          targetMembership.setProvisioningEntityId(userId);
//          targetMembership.setProvisioningGroupId(grouperAzureGroup.getId());
//          results.add(targetMembership);
//        }
//
//      }
//
//      return new TargetDaoRetrieveAllMembershipsResponse(results);
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllMemberships", startNanos));
//    }
//  }

  

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMembership(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertMemberships(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllMemberships(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsByEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsByGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
  }

}
