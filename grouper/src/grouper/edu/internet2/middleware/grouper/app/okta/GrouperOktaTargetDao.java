package edu.internet2.middleware.grouper.app.okta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLists;
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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataResponse;
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

public class GrouperOktaTargetDao extends GrouperProvisionerTargetDaoBase {
  
  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
  }

  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }
  
  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();

    try {
      GrouperOktaConfiguration oktaConfiguration = (GrouperOktaConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      // lets make sure we are doing the right thing
      Set<String> fieldNamesToInsert = new HashSet<String>();

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
          fieldNamesToInsert.add(fieldName);
        }
      }
      
      fieldNamesToInsert.add("defaultMessageDenyNotificationText");
      fieldNamesToInsert.add("handleDeletedGroup");
      fieldNamesToInsert.add("messageModerationLevel");
      fieldNamesToInsert.add("replyTo");
      fieldNamesToInsert.add("sendMessageDenyNotification");
      fieldNamesToInsert.add("spamModerationLevel");
      
      GrouperOktaGroup grouperOktaGroup = GrouperOktaGroup.fromProvisioningGroup(targetGroup, null);
      
      GrouperOktaGroup createdGAG = GrouperOktaApiCommands.createOktaGroup(oktaConfiguration.getOktaExternalSystemConfigId(), grouperOktaGroup, fieldNamesToInsert);

      targetGroup.setId(createdGAG.getId());
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
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {

    long startNanos = System.nanoTime();

    try {

      GrouperOktaConfiguration oktaConfiguration = (GrouperOktaConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

      List<GrouperOktaGroup> grouperOktaGroups = GrouperOktaApiCommands.retrieveOktaGroups(oktaConfiguration.getOktaExternalSystemConfigId(),
          null, null);

      for (GrouperOktaGroup grouperOktaGroup : grouperOktaGroups) {
        ProvisioningGroup targetGroup = grouperOktaGroup.toProvisioningGroup();
        results.add(targetGroup);
      }

      return new TargetDaoRetrieveAllGroupsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(
          new TargetDaoTimingInfo("retrieveAllGroups", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveAllDataResponse retrieveAllData(TargetDaoRetrieveAllDataRequest targetDaoRetrieveAllDataRequest) {
    
    TargetDaoRetrieveAllDataResponse targetDaoRetrieveAllDataResponse = new TargetDaoRetrieveAllDataResponse();
    
    GrouperProvisioningLists targetData = new GrouperProvisioningLists();
    
    targetDaoRetrieveAllDataResponse.setTargetData(targetData);
    
    long startNanos = System.nanoTime();

    try {
      GrouperOktaConfiguration oktaConfiguration = (GrouperOktaConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      List<ProvisioningGroup> targetGroups = new ArrayList<ProvisioningGroup>();
      List<GrouperOktaGroup> grouperOktaGroups = GrouperOktaApiCommands.retrieveOktaGroups(oktaConfiguration.getOktaExternalSystemConfigId(),
          null, null);
      for (GrouperOktaGroup grouperOktaGroup : grouperOktaGroups) {
        ProvisioningGroup targetGroup = grouperOktaGroup.toProvisioningGroup();
        targetGroups.add(targetGroup);
      }
      targetData.setProvisioningGroups(targetGroups);
      
      List<ProvisioningEntity> targetEntities = new ArrayList<ProvisioningEntity>();
      List<GrouperOktaUser> oktaUsers = GrouperOktaApiCommands.retrieveOktaUsers(oktaConfiguration.getOktaExternalSystemConfigId());
      for (GrouperOktaUser oktaUser: oktaUsers) {
        targetEntities.add(oktaUser.toProvisioningEntity());
      }
      targetData.setProvisioningEntities(targetEntities);
      
      List<ProvisioningMembership> targetMemberships = new ArrayList<>();
      
      for (GrouperOktaGroup grouperOktaGroup : grouperOktaGroups) {
        Set<String> groupMemberIds = GrouperOktaApiCommands.retrieveOktaGroupMembers(oktaConfiguration.getOktaExternalSystemConfigId(), grouperOktaGroup.getId());
        
        for (String groupMemberId: groupMemberIds) {
          ProvisioningMembership provisioningMembership = new ProvisioningMembership(false);
          provisioningMembership.setProvisioningGroupId(grouperOktaGroup.getId());
          provisioningMembership.setProvisioningEntityId(groupMemberId);
          targetMemberships.add(provisioningMembership);
        }
        
      }
      
      targetData.setProvisioningMemberships(targetMemberships);

      return targetDaoRetrieveAllDataResponse;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllData", startNanos));
    }
    
  }
  
  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(
      TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {

    long startNanos = System.nanoTime();

    try {

      GrouperOktaConfiguration oktaConfiguration = (GrouperOktaConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

      List<GrouperOktaUser> grouperOktaUsers = GrouperOktaApiCommands.retrieveOktaUsers(oktaConfiguration.getOktaExternalSystemConfigId());

      for (GrouperOktaUser grouperOktaUser : grouperOktaUsers) {
        ProvisioningEntity targetEntity = grouperOktaUser.toProvisioningEntity();
        results.add(targetEntity);
      }

      return new TargetDaoRetrieveAllEntitiesResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(
          new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {

    long startNanos = System.nanoTime();

    try {
      GrouperOktaConfiguration oktaConfiguration = (GrouperOktaConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      GrouperOktaGroup grouperOktaGroup = null;

      String attributeValue = GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue());
      
      if (StringUtils.equals("id", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        grouperOktaGroup = GrouperOktaApiCommands.retrieveOktaGroup(oktaConfiguration.getOktaExternalSystemConfigId(), 
            attributeValue);
      } else if (StringUtils.equals("name", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
      
        if (StringUtils.isNotBlank(attributeValue)) {
          List<GrouperOktaGroup> oktaGroups = GrouperOktaApiCommands.retrieveOktaGroups(oktaConfiguration.getOktaExternalSystemConfigId(),
              "profile.name", attributeValue);
          if (oktaGroups.size() == 1) {
            grouperOktaGroup = oktaGroups.get(0);
          } else if (oktaGroups.size() > 1) {
            throw new RuntimeException("There are "+oktaGroups.size()+ " groups found for name: "+attributeValue);
          }
        
        }
      }
      
      ProvisioningGroup targetGroup = grouperOktaGroup == null ? null : grouperOktaGroup.toProvisioningGroup();

      return new TargetDaoRetrieveGroupResponse(targetGroup);

    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {

    long startNanos = System.nanoTime();

    try {
      GrouperOktaConfiguration oktaConfiguration = (GrouperOktaConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      ProvisioningEntity grouperTargetEntity = targetDaoRetrieveEntityRequest.getTargetEntity();

      GrouperOktaUser grouperOktaUser = null;

      String attributeValue = GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue());
      
      if (StringUtils.equals("id", targetDaoRetrieveEntityRequest.getSearchAttribute())) {
        grouperOktaUser = GrouperOktaApiCommands.retrieveOktaUser(oktaConfiguration.getOktaExternalSystemConfigId(), 
            "id", attributeValue);
      } else if (StringUtils.equals("login", targetDaoRetrieveEntityRequest.getSearchAttribute())) {
        grouperOktaUser = GrouperOktaApiCommands.retrieveOktaUser(oktaConfiguration.getOktaExternalSystemConfigId(), 
            "profile.login", attributeValue);
      } else if (StringUtils.startsWith(targetDaoRetrieveEntityRequest.getSearchAttribute(), "profile.")) {
        grouperOktaUser = GrouperOktaApiCommands.retrieveOktaUser(oktaConfiguration.getOktaExternalSystemConfigId(), 
            targetDaoRetrieveEntityRequest.getSearchAttribute(), attributeValue);
      } else if (StringUtils.isNotBlank(grouperTargetEntity.getId())) {
        grouperOktaUser = GrouperOktaApiCommands.retrieveOktaUser(
            oktaConfiguration.getOktaExternalSystemConfigId(), "id", grouperTargetEntity.getId());
      }

      ProvisioningEntity targetEntity = grouperOktaUser == null ? null: grouperOktaUser.toProvisioningEntity();

      return new TargetDaoRetrieveEntityResponse(targetEntity);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }
  
  @Override
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    try {
      GrouperOktaConfiguration oktaConfiguration = (GrouperOktaConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      GrouperOktaUser grouperOktaUser = GrouperOktaUser.fromProvisioningEntity(targetEntity, null);
      
      GrouperOktaUser createdOktaUser = GrouperOktaApiCommands.createOktaUser(oktaConfiguration.getOktaExternalSystemConfigId(), grouperOktaUser);

      targetEntity.setId(createdOktaUser.getId());
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
  public TargetDaoInsertMembershipResponse insertMembership(TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoInsertMembershipRequest.getTargetMembership();

    try {
      GrouperOktaConfiguration oktaConfiguration = (GrouperOktaConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      GrouperOktaApiCommands.createOktaMembership(oktaConfiguration.getOktaExternalSystemConfigId(),
          targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntityId());

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
  
  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();

    try {
      GrouperOktaConfiguration oktaConfiguration = (GrouperOktaConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      GrouperOktaUser grouperOktaUser = GrouperOktaUser.fromProvisioningEntity(targetEntity, null);
      
      GrouperOktaApiCommands.deleteOktaUser(oktaConfiguration.getOktaExternalSystemConfigId(), grouperOktaUser.getId());

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
      GrouperOktaConfiguration oktaConfiguration = (GrouperOktaConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      GrouperOktaGroup grouperOktaGroup = GrouperOktaGroup.fromProvisioningGroup(targetGroup, null);
      
      GrouperOktaApiCommands.deleteOktaGroup(oktaConfiguration.getOktaExternalSystemConfigId(), grouperOktaGroup.getId());

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
  
  public TargetDaoDeleteMembershipResponse deleteMembership(TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {
    long startNanos = System.nanoTime();
    
    ProvisioningMembership targetMembership = targetDaoDeleteMembershipRequest.getTargetMembership();

    try {
      GrouperOktaConfiguration oktaConfiguration = (GrouperOktaConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      GrouperOktaApiCommands.deleteOktaMembership(oktaConfiguration.getOktaExternalSystemConfigId(), 
          targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntityId());

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
      throw new RuntimeException("Failed to delete Okta group member (groupId '" + targetMembership.getProvisioningGroupId() + "', member '" + targetMembership.getProvisioningEntityId() + "'", e);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMembership", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();

    try {
      GrouperOktaConfiguration oktaConfiguration = (GrouperOktaConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      String targetGroupId = resolveTargetGroupId(targetGroup);
      
      if (StringUtils.isBlank(targetGroupId)) {
        return new TargetDaoRetrieveMembershipsByGroupResponse(new ArrayList<ProvisioningMembership>());
      }
      
      Set<String> groupMembers = GrouperOktaApiCommands.retrieveOktaGroupMembers(oktaConfiguration.getOktaExternalSystemConfigId(), targetGroupId);
      
      List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>(); 
      
      for (String userId : groupMembers) {

        ProvisioningMembership targetMembership = new ProvisioningMembership(false);
        targetMembership.setProvisioningGroupId(targetGroup.getId());
        targetMembership.setProvisioningEntityId(userId);
        provisioningMemberships.add(targetMembership);
        
      }
  
      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByGroup", startNanos));
    }
    
  }
  
  private String resolveTargetGroupId(ProvisioningGroup targetGroup) {
    
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
  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();

    try {
      GrouperOktaConfiguration oktaConfiguration = (GrouperOktaConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      // lets make sure we are doing the right thing
      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToUpdate.add(fieldName);
      }
      
      GrouperOktaUser grouperOktaUser = GrouperOktaUser.fromProvisioningEntity(targetEntity, null);
      GrouperOktaApiCommands.updateOktaUser(oktaConfiguration.getOktaExternalSystemConfigId(), grouperOktaUser, fieldNamesToUpdate);

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
  public TargetDaoUpdateGroupResponse updateGroup(TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();

    try {
      GrouperOktaConfiguration oktaConfiguration = (GrouperOktaConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      // lets make sure we are doing the right thing
      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToUpdate.add(fieldName);
      }
      
      GrouperOktaGroup grouperOktaGroup = GrouperOktaGroup.fromProvisioningGroup(targetGroup, null);
      GrouperOktaApiCommands.updateOktaGroup(oktaConfiguration.getOktaExternalSystemConfigId(), grouperOktaGroup, fieldNamesToUpdate);

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
  public void registerGrouperProvisionerDaoCapabilities(GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
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
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByGroup(true);
    
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
  }

  private String resolveTargetEntityId(ProvisioningEntity targetEntity) {
    
    if (targetEntity == null) {
      return null;
    }
    
    if (StringUtils.isNotBlank(targetEntity.getId())) {
      return targetEntity.getId();
    }
    
    if (StringUtils.isBlank(targetEntity.getEmail())) {
      return null;
    }
    
    TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest = new TargetDaoRetrieveEntityRequest(targetEntity, false);
    targetDaoRetrieveEntityRequest.setSearchAttribute("email");
    targetDaoRetrieveEntityRequest.setSearchAttributeValue(targetEntity.getEmail());
    TargetDaoRetrieveEntityResponse targetDaoRetrieveEntityResponse = this.retrieveEntity(targetDaoRetrieveEntityRequest);
    
    if (targetDaoRetrieveEntityResponse == null || targetDaoRetrieveEntityResponse.getTargetEntity() == null) {
      return null;
    }
    
    return targetDaoRetrieveEntityResponse.getTargetEntity().getId();
    
  }

}
