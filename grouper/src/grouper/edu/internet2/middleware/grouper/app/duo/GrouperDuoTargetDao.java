package edu.internet2.middleware.grouper.app.duo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLists;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataResponse;
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
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

public class GrouperDuoTargetDao extends GrouperProvisionerTargetDaoBase {

  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
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

      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();
      
      boolean loadEntitiesToGrouperTable = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isLoadEntitiesToGrouperTable();

      List<GrouperDuoUser> grouperDuoUsers = GrouperDuoApiCommands.retrieveDuoUsers(duoConfiguration.getDuoExternalSystemConfigId(), loadEntitiesToGrouperTable);

      TargetDaoRetrieveAllEntitiesResponse targetDaoRetrieveAllEntitiesResponse = new TargetDaoRetrieveAllEntitiesResponse(results);

      Map<ProvisioningEntity, Object> targetEntityToTargetNativeEntity = targetDaoRetrieveAllEntitiesResponse
           .getTargetEntityToTargetNativeEntity();
      for (GrouperDuoUser grouperDuoUser : grouperDuoUsers) {
        ProvisioningEntity targetEntity = grouperDuoUser.toProvisioningEntity();
        results.add(targetEntity);
        if (targetDaoRetrieveAllEntitiesRequest.isIncludeNativeEntity()) {
          targetEntityToTargetNativeEntity.put(targetEntity, grouperDuoUser);
        }
      }

      return targetDaoRetrieveAllEntitiesResponse;
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

      // we can retrieve by id or username, prefer id

      GrouperDuoUser grouperDuoUser = null;

      if (StringUtils.equals("id", targetDaoRetrieveEntityRequest.getSearchAttribute())) {
        grouperDuoUser = GrouperDuoApiCommands.retrieveDuoUser(duoConfiguration.getDuoExternalSystemConfigId(), 
            GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue()));
      } else if (StringUtils.equals("loginId", targetDaoRetrieveEntityRequest.getSearchAttribute())) {
        grouperDuoUser = GrouperDuoApiCommands.retrieveDuoUserByName(
            duoConfiguration.getDuoExternalSystemConfigId(), GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue()));
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

  
  private static ExpirableCache<Boolean, Map<String, GrouperDuoGroup>> cacheGroupNameToGroup = new ExpirableCache<Boolean, Map<String, GrouperDuoGroup>>(5);
  
  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {

    long startNanos = System.nanoTime();

    try {
      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      ProvisioningGroup grouperTargetGroup = targetDaoRetrieveGroupRequest.getTargetGroup();

      GrouperDuoGroup grouperDuoGroup = null;

      if (StringUtils.equals("id", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        grouperDuoGroup = GrouperDuoApiCommands.retrieveDuoGroup(duoConfiguration.getDuoExternalSystemConfigId(), GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()));
      } else if (StringUtils.equals("name", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        String name = GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue());
        if (StringUtils.isNotBlank(name)) {
          
          Map<String, GrouperDuoGroup> groupNameToGroup = cacheGroupNameToGroup.get(Boolean.TRUE);
          
          grouperDuoGroup = groupNameToGroup == null ? null : groupNameToGroup.get(name);
          
          if (grouperDuoGroup == null) {
            List<GrouperDuoGroup> allDuoGroups = GrouperDuoApiCommands.retrieveDuoGroups(duoConfiguration.getDuoExternalSystemConfigId());
            
            groupNameToGroup = new HashMap<String, GrouperDuoGroup>();
            for (GrouperDuoGroup currentDuoGroup: GrouperUtil.nonNull(allDuoGroups)) {
              groupNameToGroup.put(currentDuoGroup.getName(), currentDuoGroup);
            }
            cacheGroupNameToGroup.put(Boolean.TRUE, groupNameToGroup);
            
            grouperDuoGroup = groupNameToGroup.get(name);
            
          }
          
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

  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();

    try {
      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      GrouperDuoGroup grouperDuoGroup = GrouperDuoGroup.fromProvisioningGroup(targetGroup, null);
      
      GrouperDuoGroup createdDuoGroup = GrouperDuoApiCommands.createDuoGroup(duoConfiguration.getDuoExternalSystemConfigId(), grouperDuoGroup);

      targetGroup.setId(createdDuoGroup.getGroup_id());
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
  public TargetDaoInsertMembershipResponse insertMembership(TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoInsertMembershipRequest.getTargetMembership();

    try {
      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      GrouperDuoApiCommands.associateUserToGroup(duoConfiguration.getDuoExternalSystemConfigId(), targetMembership.getProvisioningEntityId(), targetMembership.getProvisioningGroupId());

      targetMembership.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoInsertMembershipResponse();
    } catch (Exception e) {
      targetMembership.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertMembership", startNanos));
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
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToUpdate.add(fieldName);
      }
      
      GrouperDuoGroup grouperDuoGroup = GrouperDuoGroup.fromProvisioningGroup(targetGroup, null);
      GrouperDuoApiCommands.updateDuoGroup(duoConfiguration.getDuoExternalSystemConfigId(), grouperDuoGroup, fieldNamesToUpdate);

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
      
      Map<String, GrouperDuoGroup> groupNameToGroup = cacheGroupNameToGroup.get(Boolean.TRUE);
      if (groupNameToGroup != null) {
        groupNameToGroup.remove(grouperDuoGroup.getName());
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
  public TargetDaoRetrieveMembershipsByEntityResponse retrieveMembershipsByEntity(TargetDaoRetrieveMembershipsByEntityRequest targetDaoRetrieveMembershipsByEntityRequest) {
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoRetrieveMembershipsByEntityRequest.getTargetEntity();

    try {
      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      String targetEntityId = resolveTargetEntityId(targetEntity);
      
      if (StringUtils.isBlank(targetEntityId)) {
        return new TargetDaoRetrieveMembershipsByEntityResponse(new ArrayList<ProvisioningMembership>());
      }

      List<GrouperDuoGroup> duoGroups = GrouperDuoApiCommands.retrieveDuoGroupsByUser(duoConfiguration.getDuoExternalSystemConfigId(), targetEntityId);
      
      List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();
      
      for (GrouperDuoGroup duoGroup : duoGroups) {

        ProvisioningMembership targetMembership = new ProvisioningMembership(false);
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

  
  public String resolveTargetGroupId(ProvisioningGroup targetGroup) {
    
    if (targetGroup == null) {
      return null;
    }
    
    if (StringUtils.isNotBlank(targetGroup.getId())) {
      return targetGroup.getId();
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
      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      String targetGroupId = resolveTargetGroupId(targetGroup);
      
      if (StringUtils.isBlank(targetGroupId)) {
        return new TargetDaoRetrieveMembershipsByGroupResponse(new ArrayList<ProvisioningMembership>());
      }
      
      List<GrouperDuoUser> duoUsers = GrouperDuoApiCommands.retrieveDuoUserIdsUserNamesByGroup(duoConfiguration.getDuoExternalSystemConfigId(), targetGroupId);
      
      List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>(); 
      
      for (GrouperDuoUser duoUser : duoUsers) {

        ProvisioningMembership targetMembership = new ProvisioningMembership(false);
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
      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      // lets make sure we are doing the right thing
      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToUpdate.add(fieldName);
      }
      
      GrouperDuoUser grouperDuoUser = GrouperDuoUser.fromProvisioningEntity(targetEntity, null);
      GrouperDuoApiCommands.updateDuoUser(duoConfiguration.getDuoExternalSystemConfigId(), grouperDuoUser, fieldNamesToUpdate);

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
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    try {
      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      GrouperDuoUser grouperDuoUser = GrouperDuoUser.fromProvisioningEntity(targetEntity, null);
      
      GrouperDuoUser createdDuoUser = GrouperDuoApiCommands.createDuoUser(duoConfiguration.getDuoExternalSystemConfigId(), grouperDuoUser);

      targetEntity.setId(createdDuoUser.getId());
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
  public TargetDaoDeleteEntityResponse deleteEntity(TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();

    try {
      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      GrouperDuoUser grouperDuoUser = GrouperDuoUser.fromProvisioningEntity(targetEntity, null);
      
      GrouperDuoApiCommands.deleteDuoUser(duoConfiguration.getDuoExternalSystemConfigId(), grouperDuoUser.getId());

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
  public TargetDaoRetrieveAllDataResponse retrieveAllData(TargetDaoRetrieveAllDataRequest targetDaoRetrieveAllDataRequest) {
    
    TargetDaoRetrieveAllDataResponse targetDaoRetrieveAllDataResponse = new TargetDaoRetrieveAllDataResponse();
    
    GrouperProvisioningLists targetData = new GrouperProvisioningLists();
    
    targetDaoRetrieveAllDataResponse.setTargetData(targetData);
    
    long startNanos = System.nanoTime();

    try {
      GrouperDuoConfiguration duoConfiguration = (GrouperDuoConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      TargetDaoRetrieveAllGroupsResponse targetDaoRetrieveAllGroupsResponse = this.retrieveAllGroups(new TargetDaoRetrieveAllGroupsRequest(false));
      
      List<ProvisioningGroup> targetGroups = targetDaoRetrieveAllGroupsResponse.getTargetGroups();
      
      targetData.setProvisioningGroups(targetGroups);
      
      boolean loadEntitiesToGrouperTable = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isLoadEntitiesToGrouperTable();
      
      List<GrouperDuoUser> duoUsers = GrouperDuoApiCommands.retrieveDuoUsers(duoConfiguration.getDuoExternalSystemConfigId(), loadEntitiesToGrouperTable);

      List<ProvisioningMembership> targetMemberships = new ArrayList<>();
      targetData.setProvisioningMemberships(targetMemberships);
      
      List<ProvisioningEntity> targetEntities = new ArrayList<ProvisioningEntity>();
      targetData.setProvisioningEntities(targetEntities);
      
      Map<ProvisioningEntity, Object> targetEntityToTargetNativeEntity = targetDaoRetrieveAllDataResponse
           .getTargetEntityToTargetNativeEntity();

      for (GrouperDuoUser duoUser: duoUsers) {
        
        ProvisioningEntity targetEntity = duoUser.toProvisioningEntity();
        targetEntities.add(targetEntity);

        if (targetDaoRetrieveAllDataRequest.isIncludeNativeEntity()) {
          targetEntityToTargetNativeEntity.put(targetEntity, duoUser);
        }

        Set<GrouperDuoGroup> groupsPerUser = duoUser.getGroups();
        
        for (GrouperDuoGroup duoGroup: groupsPerUser) {
          ProvisioningMembership targetMembership = new ProvisioningMembership(false);
          targetMembership.setProvisioningEntityId(duoUser.getId());
          targetMembership.setProvisioningGroupId(duoGroup.getGroup_id());
          targetMemberships.add(targetMembership);
        }
        
      }

      return targetDaoRetrieveAllDataResponse;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllMemberships", startNanos));
    }
    
  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMembership(true);
    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertMembership(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllData(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
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

}
