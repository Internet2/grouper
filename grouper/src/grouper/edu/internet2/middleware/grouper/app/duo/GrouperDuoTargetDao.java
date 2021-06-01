package edu.internet2.middleware.grouper.app.duo;

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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupResponse;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GrouperDuoTargetDao extends GrouperProvisionerTargetDaoBase {

  @Override
  public void loggingStart() {
    GrouperHttpClient.logStart(new GrouperHttpClientLog().assignDoNotLogHeaders(DuoMockServiceHandler.doNotLogHeaders).assignDoNotLogParameters(DuoMockServiceHandler.doNotLogParameters));
  }

  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }

  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(
      TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {

    long startNanos = System.nanoTime();

    try {

      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

      List<GrouperDuoGroup> grouperDuoGroups = GrouperDuoApiCommands
          .retrieveDuoGroups(duoConfiguration.getDuoExternalSystemConfigId());

      for (GrouperDuoGroup grouperDuoGroup : grouperDuoGroups) {
        ProvisioningGroup targetGroup = grouperDuoGroup.toProvisioningGroup();
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

      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

      List<GrouperDuoUser> grouperDuoUsers = GrouperDuoApiCommands
          .retrieveDuoUsers(duoConfiguration.getDuoExternalSystemConfigId());

      for (GrouperDuoUser grouperDuoUser : grouperDuoUsers) {
        ProvisioningEntity targetEntity = grouperDuoUser.toProvisioningEntity();
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
      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      // we can retrieve by id or userPrincipalName, prefer id

      ProvisioningEntity grouperTargetEntity = targetDaoRetrieveEntityRequest
          .getTargetEntity();

      GrouperDuoUser grouperDuoUser = null;

      if (!StringUtils.isBlank(grouperTargetEntity.getId())) {
        grouperDuoUser = GrouperDuoApiCommands.retrieveDuoUser(
            duoConfiguration.getDuoExternalSystemConfigId(), grouperTargetEntity.getId());
      }

      //TODO come back to it because in GrouperDuoApiCommands we don't have the capability to fetch user by principal name
      String userPrincipalName = grouperTargetEntity
          .retrieveAttributeValueString("userPrincipalName");
      if (grouperDuoUser == null && !StringUtils.isBlank(userPrincipalName)) {
        grouperDuoUser = GrouperDuoApiCommands.retrieveDuoUser(
            duoConfiguration.getDuoExternalSystemConfigId(), userPrincipalName);
      }

      ProvisioningEntity targetEntity = grouperDuoUser == null ? null
          : grouperDuoUser.toProvisioningEntity();

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
      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      // we can retrieve by id or displayName

      ProvisioningGroup grouperTargetGroup = targetDaoRetrieveGroupRequest
          .getTargetGroup();

      GrouperDuoGroup grouperDuoGroup = null;

      if (!StringUtils.isBlank(grouperTargetGroup.getId())) {
        grouperDuoGroup = GrouperDuoApiCommands.retrieveDuoGroup(
            duoConfiguration.getDuoExternalSystemConfigId(),
            grouperTargetGroup.getId());
      }

      //TODO come back to it
      String name = grouperTargetGroup.getName();
      if (grouperDuoGroup == null && !StringUtils.isBlank(name)) {
        grouperDuoGroup = GrouperDuoApiCommands.retrieveDuoGroup(
            duoConfiguration.getDuoExternalSystemConfigId(), name);
      }

      ProvisioningGroup targetGroup = grouperDuoGroup == null ? null
          : grouperDuoGroup.toProvisioningGroup();

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
      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      // lets make sure we are doing the right thing
      Set<String> fieldNamesToInsert = new HashSet<String>();

      // Initialize with fields that are required but have defaults and may not be configured explicitly
      fieldNamesToInsert.add("mailEnabled");
      fieldNamesToInsert.add("securityEnabled");

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        String fieldName = GrouperUtil.defaultIfBlank(provisioningObjectChange.getFieldName(), provisioningObjectChange.getAttributeName());
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
          fieldNamesToInsert.add(fieldName);
        }
      }
      
      GrouperDuoGroup grouperDuoGroup = GrouperDuoGroup.fromProvisioningGroup(targetGroup, null);
      
      //TODO come back to it - field names to insert
      GrouperDuoGroup createdGAG = GrouperDuoApiCommands.createDuoGroup(duoConfiguration.getDuoExternalSystemConfigId(), grouperDuoGroup);

      targetGroup.setId(createdGAG.getGroup_id());
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

// Im thinking since we can do batch we shouldnt do individual (reduce technical debt)  
//  @Override
//  public TargetDaoInsertMembershipResponse insertMembership(TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
//    long startNanos = System.nanoTime();
//    ProvisioningMembership targetMembership = targetDaoInsertMembershipRequest.getTargetMembership();
//
//    try {
//      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      GrouperDuoApiCommands.createDuoMembership(duoConfiguration.getDuoExternalSystemConfigId(), targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntityId());
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

  @Override
  public TargetDaoInsertMembershipsResponse insertMemberships(TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {
    long startNanos = System.nanoTime();
    List<ProvisioningMembership> targetMemberships = targetDaoInsertMembershipsRequest.getTargetMemberships();

    try {
      
      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

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
          for (String userId: GrouperUtil.nonNull(userIds)) {
            GrouperDuoApiCommands.associateUserToGroup(duoConfiguration.getDuoExternalSystemConfigId(), userId, groupId);
          }
        } catch (RuntimeException e) {
          runtimeException = e;
        }
        boolean success = runtimeException == null;
        for (String userId : GrouperUtil.nonNull(userIds)) {
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

  public TargetDaoDeleteMembershipResponse deleteMembership(TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {
    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoDeleteMembershipRequest.getTargetMembership();

    try {
      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      GrouperDuoApiCommands.disassociateUserFromGroup(duoConfiguration.getDuoExternalSystemConfigId(), targetMembership.getProvisioningEntityId(), targetMembership.getProvisioningGroupId());

      targetMembership.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoDeleteMembershipResponse();
    } catch (Exception e) {
      targetMembership.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw new RuntimeException("Failed to delete Duo group member (groupId '" + targetMembership.getProvisioningGroupId() + "', member '" + targetMembership.getProvisioningEntityId() + "'", e);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMembership", startNanos));
    }
  }

  @Override
  public TargetDaoUpdateGroupResponse updateGroup(
      TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();

    try {
      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      // lets make sure we are doing the right thing
      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        String fieldName = GrouperUtil.defaultIfBlank(provisioningObjectChange.getFieldName(), provisioningObjectChange.getAttributeName());
        fieldNamesToUpdate.add(fieldName);
      }
      
      GrouperDuoGroup grouperDuoGroup = GrouperDuoGroup.fromProvisioningGroup(targetGroup, null);
      //TODO come back and see if field names is needed
      GrouperDuoApiCommands.updateDuoGroup(duoConfiguration.getDuoExternalSystemConfigId(), grouperDuoGroup);

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
      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      GrouperDuoGroup grouperDuoGroup = GrouperDuoGroup.fromProvisioningGroup(targetGroup, null);
      
      GrouperDuoApiCommands.deleteDuoGroup(duoConfiguration.getDuoExternalSystemConfigId(), grouperDuoGroup.getGroup_id());

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
  public TargetDaoRetrieveMembershipsByEntityResponse retrieveMembershipsByEntity(
      TargetDaoRetrieveMembershipsByEntityRequest targetDaoRetrieveMembershipsByEntityRequest) {
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoRetrieveMembershipsByEntityRequest.getTargetEntity();

    try {
      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<GrouperDuoGroup> duoGroups = GrouperDuoApiCommands.retrieveDuoGroupsByUser(duoConfiguration.getDuoExternalSystemConfigId(), targetEntity.getId());
      
      List<Object> provisioningMemberships = new ArrayList<Object>();
      
      for (GrouperDuoGroup duoGroup : duoGroups) {

        ProvisioningMembership targetMembership = new ProvisioningMembership();
        targetMembership.setProvisioningGroupId(duoGroup.getGroup_id());
        targetMembership.setProvisioningEntityId(targetEntity.getId());
        provisioningMemberships.add(targetMembership);
      }
  
      return new TargetDaoRetrieveMembershipsByEntityResponse(provisioningMemberships);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByEntity", startNanos));
    }
  }

//  @Override
//  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
//    long startNanos = System.nanoTime();
//    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();
//
//    try {
//      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      Set<String> userIds = GrouperDuoApiCommands.retrieveDuoGroupMembers(duoConfiguration.getDuoExternalSystemConfigId(), targetGroup.getId());
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

  @Override
  public TargetDaoRetrieveAllMembershipsResponse retrieveAllMemberships(TargetDaoRetrieveAllMembershipsRequest targetDaoRetrieveAllMembershipsRequest) {
    long startNanos = System.nanoTime();

    try {
      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<GrouperDuoUser> duoUsers = GrouperDuoApiCommands.retrieveDuoUsers(duoConfiguration.getDuoExternalSystemConfigId());

      List<ProvisioningMembership> results = new ArrayList<>();
      
      for (GrouperDuoUser duoUser: duoUsers) {
        Set<GrouperDuoGroup> groupsPerUser = duoUser.getGroups();
        
        for (GrouperDuoGroup duoGroup: groupsPerUser) {
          ProvisioningMembership targetMembership = new ProvisioningMembership();
          targetMembership.setProvisioningEntityId(duoUser.getId());
          targetMembership.setProvisioningGroupId(duoGroup.getGroup_id());
          results.add(targetMembership);
        }
        
      }

      return new TargetDaoRetrieveAllMembershipsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllMemberships", startNanos));
    }
  }

  

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
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
  }

}
