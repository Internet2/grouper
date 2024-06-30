package edu.internet2.middleware.grouper.app.google;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeDbCache;
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
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class GrouperGoogleTargetDao extends GrouperProvisionerTargetDaoBase {
  
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
      GrouperGoogleConfiguration googleConfiguration = (GrouperGoogleConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
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
      
      GrouperGoogleGroup grouperGoogleGroup = GrouperGoogleGroup.fromProvisioningGroup(targetGroup, null);
      
      GrouperGoogleGroup createdGAG = GrouperGoogleApiCommands.createGoogleGroup(googleConfiguration.getGoogleExternalSystemConfigId(), grouperGoogleGroup, fieldNamesToInsert);

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

      GrouperGoogleConfiguration googleConfiguration = (GrouperGoogleConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

      boolean lookupManagers = googleConfiguration.getTargetGroupAttributeNameToConfig().containsKey("managers");
      boolean lookupOwners = googleConfiguration.getTargetGroupAttributeNameToConfig().containsKey("owners");
      
      List<GrouperGoogleGroup> grouperGoogleGroups = GrouperGoogleApiCommands.retrieveGoogleGroups(googleConfiguration.getGoogleExternalSystemConfigId(),
          null, null, lookupManagers, lookupOwners);

      for (GrouperGoogleGroup grouperGoogleGroup : grouperGoogleGroups) {
        ProvisioningGroup targetGroup = grouperGoogleGroup.toProvisioningGroup();
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
      GrouperGoogleConfiguration googleConfiguration = (GrouperGoogleConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      boolean lookupManagers = googleConfiguration.getTargetGroupAttributeNameToConfig().containsKey("managers");
      boolean lookupOwners = googleConfiguration.getTargetGroupAttributeNameToConfig().containsKey("owners");
      
      List<ProvisioningGroup> targetGroups = new ArrayList<ProvisioningGroup>();
      List<GrouperGoogleGroup> grouperGoogleGroups = GrouperGoogleApiCommands.retrieveGoogleGroups(googleConfiguration.getGoogleExternalSystemConfigId(),
          null, null, lookupManagers, lookupOwners);
      for (GrouperGoogleGroup grouperGoogleGroup : grouperGoogleGroups) {
        ProvisioningGroup targetGroup = grouperGoogleGroup.toProvisioningGroup();
        targetGroups.add(targetGroup);
      }
      targetData.setProvisioningGroups(targetGroups);
      
      List<ProvisioningEntity> targetEntities = new ArrayList<ProvisioningEntity>();
      List<GrouperGoogleUser> googleUsers = GrouperGoogleApiCommands.retrieveGoogleUsers(googleConfiguration.getGoogleExternalSystemConfigId());
      for (GrouperGoogleUser googleUser: googleUsers) {
        targetEntities.add(googleUser.toProvisioningEntity());
      }
      targetData.setProvisioningEntities(targetEntities);
      
      List<ProvisioningMembership> targetMemberships = new ArrayList<>();
      
      for (GrouperGoogleGroup grouperGoogleGroup : grouperGoogleGroups) {
        Set<String> groupMemberIds = GrouperGoogleApiCommands.retrieveGoogleGroupMembers(googleConfiguration.getGoogleExternalSystemConfigId(), grouperGoogleGroup.getId());
        
        for (String groupMemberId: groupMemberIds) {
          ProvisioningMembership provisioningMembership = new ProvisioningMembership(false);
          provisioningMembership.setProvisioningGroupId(grouperGoogleGroup.getId());
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

      GrouperGoogleConfiguration googleConfiguration = (GrouperGoogleConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

      List<GrouperGoogleUser> grouperGoogleUsers = GrouperGoogleApiCommands.retrieveGoogleUsers(googleConfiguration.getGoogleExternalSystemConfigId());

      for (GrouperGoogleUser grouperGoogleUser : grouperGoogleUsers) {
        ProvisioningEntity targetEntity = grouperGoogleUser.toProvisioningEntity();
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
      GrouperGoogleConfiguration googleConfiguration = (GrouperGoogleConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      GrouperGoogleGroup grouperGoogleGroup = null;

      String attributeValue = GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue());
      
      boolean lookupManagers = googleConfiguration.getTargetGroupAttributeNameToConfig().containsKey("managers");
      boolean lookupOwners = googleConfiguration.getTargetGroupAttributeNameToConfig().containsKey("owners");
      
      if (StringUtils.equals("id", targetDaoRetrieveGroupRequest.getSearchAttribute()) || StringUtils.equals("email", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        grouperGoogleGroup = GrouperGoogleApiCommands.retrieveGoogleGroup(googleConfiguration.getGoogleExternalSystemConfigId(), 
            attributeValue, lookupManagers, lookupOwners);
      } else if (StringUtils.equals("name", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
      
        if (StringUtils.isNotBlank(attributeValue)) {
          List<GrouperGoogleGroup> googleGroups = GrouperGoogleApiCommands.retrieveGoogleGroups(googleConfiguration.getGoogleExternalSystemConfigId(),
              "name", attributeValue, lookupManagers, lookupOwners);
          if (googleGroups.size() == 1) {
            grouperGoogleGroup = googleGroups.get(0);
          } else if (googleGroups.size() > 1) {
            throw new RuntimeException("There are "+googleGroups.size()+ " groups found for name: "+attributeValue);
          }
        
        }
      }
      
      ProvisioningGroup targetGroup = grouperGoogleGroup == null ? null : grouperGoogleGroup.toProvisioningGroup();

      return new TargetDaoRetrieveGroupResponse(targetGroup);

    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {

    long startNanos = System.nanoTime();

    try {
      GrouperGoogleConfiguration googleConfiguration = (GrouperGoogleConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      ProvisioningEntity grouperTargetEntity = targetDaoRetrieveEntityRequest.getTargetEntity();

      GrouperGoogleUser grouperGoogleUser = null;

      String attributeValue = GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue());
      if (StringUtils.equals("id", targetDaoRetrieveEntityRequest.getSearchAttribute()) || StringUtils.equals("email", targetDaoRetrieveEntityRequest.getSearchAttribute())) {
        grouperGoogleUser = GrouperGoogleApiCommands.retrieveGoogleUser(googleConfiguration.getGoogleExternalSystemConfigId(), 
            attributeValue);
      }

      if (StringUtils.isNotBlank(grouperTargetEntity.getId())) {
        grouperGoogleUser = GrouperGoogleApiCommands.retrieveGoogleUser(
            googleConfiguration.getGoogleExternalSystemConfigId(), grouperTargetEntity.getId());
      }


      ProvisioningEntity targetEntity = grouperGoogleUser == null ? null: grouperGoogleUser.toProvisioningEntity();

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
      GrouperGoogleConfiguration googleConfiguration = (GrouperGoogleConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      GrouperGoogleUser grouperGoogleUser = GrouperGoogleUser.fromProvisioningEntity(targetEntity, null);
      
      GrouperGoogleUser createdGoogleUser = GrouperGoogleApiCommands.createGoogleUser(googleConfiguration.getGoogleExternalSystemConfigId(), grouperGoogleUser);

      targetEntity.setId(createdGoogleUser.getId());
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
      GrouperGoogleConfiguration googleConfiguration = (GrouperGoogleConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      GrouperGoogleApiCommands.createGoogleMembership(googleConfiguration.getGoogleExternalSystemConfigId(),
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
      GrouperGoogleConfiguration googleConfiguration = (GrouperGoogleConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      GrouperGoogleUser grouperGoogleUser = GrouperGoogleUser.fromProvisioningEntity(targetEntity, null);
      
      GrouperGoogleApiCommands.deleteGoogleUser(googleConfiguration.getGoogleExternalSystemConfigId(), grouperGoogleUser.getId());

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
      GrouperGoogleConfiguration googleConfiguration = (GrouperGoogleConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      GrouperGoogleGroup grouperGoogleGroup = GrouperGoogleGroup.fromProvisioningGroup(targetGroup, null);
      
      GrouperGoogleApiCommands.deleteGoogleGroup(googleConfiguration.getGoogleExternalSystemConfigId(), grouperGoogleGroup.getId());

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
      GrouperGoogleConfiguration googleConfiguration = (GrouperGoogleConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      GrouperGoogleApiCommands.deleteGoogleMembership(googleConfiguration.getGoogleExternalSystemConfigId(), 
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
      throw new RuntimeException("Failed to delete Google group member (groupId '" + targetMembership.getProvisioningGroupId() + "', member '" + targetMembership.getProvisioningEntityId() + "'", e);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMembership", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();

    try {
      GrouperGoogleConfiguration googleConfiguration = (GrouperGoogleConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      String targetGroupId = resolveTargetGroupId(targetGroup);
      
      if (StringUtils.isBlank(targetGroupId)) {
        return new TargetDaoRetrieveMembershipsByGroupResponse(new ArrayList<ProvisioningMembership>());
      }
      
      Set<String> groupMembers = GrouperGoogleApiCommands.retrieveGoogleGroupMembers(googleConfiguration.getGoogleExternalSystemConfigId(), targetGroupId);
      
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
      GrouperGoogleConfiguration googleConfiguration = (GrouperGoogleConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      // lets make sure we are doing the right thing
      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToUpdate.add(fieldName);
      }
      
      GrouperGoogleUser grouperGoogleUser = GrouperGoogleUser.fromProvisioningEntity(targetEntity, null);
      GrouperGoogleApiCommands.updateGoogleUser(googleConfiguration.getGoogleExternalSystemConfigId(), grouperGoogleUser, fieldNamesToUpdate);

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
      GrouperGoogleConfiguration googleConfiguration = (GrouperGoogleConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      // lets make sure we are doing the right thing
      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToUpdate.add(fieldName);
      }
      
      GrouperGoogleGroup grouperGoogleGroup = GrouperGoogleGroup.fromProvisioningGroup(targetGroup, null);
      GrouperGoogleApiCommands.updateGoogleGroup(googleConfiguration.getGoogleExternalSystemConfigId(), grouperGoogleGroup, fieldNamesToUpdate);

      targetGroup.setProvisioned(true);
      
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        if (StringUtils.equals(fieldName, "managers")) {
          if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
            String email = (String) provisioningObjectChange.getNewValue();
            if (StringUtils.isBlank(email)) {
              continue;
            }
            GrouperGoogleApiCommands.createGoogleRoleMembership(googleConfiguration.getGoogleExternalSystemConfigId(),
                grouperGoogleGroup.getId(), email, "MANAGER");
          } else if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.delete) {
            String email = (String) provisioningObjectChange.getOldValue();
            if (StringUtils.isBlank(email)) {
              continue;
            }
            boolean addBackManager = false;
            boolean addBackMember = false;
            
            GrouperProvisioningConfigurationAttributeDbCache entityEmailCacheBucket = googleConfiguration.getEmailCacheBucket();
            GrouperUtil.assertion(entityEmailCacheBucket != null, "You must cache the entity email attribute.");
            String emailCacheDatabaseColumn = entityEmailCacheBucket.getDatabaseColumn();
            
            GrouperProvisioningConfigurationAttributeDbCache entityIdCacheBucket = googleConfiguration.getEntityIdCacheBucket();
            GrouperUtil.assertion(entityIdCacheBucket != null, "You must cache the entity id attribute.");
            
            String sql = "select distinct "+entityIdCacheBucket.getDatabaseColumn()+" from grouper_sync_member gsm, grouper_sync_group gsg , grouper_memberships_lw_v gmlv  "
                + "where gsg.id = ? and gsm."+emailCacheDatabaseColumn+" = ? and gmlv.list_name = 'members' and gmlv.member_id = gsm.member_id  and gmlv.group_id = gsg.group_id ";
            
            String entityIdValue = new GcDbAccess().sql(sql).addBindVar(targetGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup().getId())
            .addBindVar(email).select(String.class);
            
            if (StringUtils.isNotBlank(entityIdValue)) {
              addBackMember = true;
            }
            
            GrouperGoogleApiCommands.deleteGoogleRoleMembership(googleConfiguration.getGoogleExternalSystemConfigId(),
                grouperGoogleGroup.getId(), entityIdValue, email, "MANAGER", addBackManager, addBackMember);
          }
        } else if (StringUtils.equals(fieldName, "owners")) {
          if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
            String email = (String) provisioningObjectChange.getNewValue();
            if (StringUtils.isBlank(email)) {
              continue;
            }
            GrouperGoogleApiCommands.createGoogleRoleMembership(googleConfiguration.getGoogleExternalSystemConfigId(),
                grouperGoogleGroup.getId(), email, "OWNER");
          } else if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.delete) {
            String email = (String) provisioningObjectChange.getOldValue();
            if (StringUtils.isBlank(email)) {
              continue;
            }
            boolean addBackManager = GrouperUtil.nonNull(grouperGoogleGroup.getManagers()).contains(email);
            boolean addBackMember = false;
            String entityIdValue = null;
            if (!addBackManager) {
              GrouperProvisioningConfigurationAttributeDbCache entityEmailCacheBucket = googleConfiguration.getEmailCacheBucket();
              GrouperUtil.assertion(entityEmailCacheBucket != null, "You must cache the entity email attribute.");
              String emailCacheDatabaseColumn = entityEmailCacheBucket.getDatabaseColumn();
              
              GrouperProvisioningConfigurationAttributeDbCache entityIdCacheBucket = googleConfiguration.getEntityIdCacheBucket();
              GrouperUtil.assertion(entityIdCacheBucket != null, "You must cache the entity id attribute.");
              
              String sql = "select distinct "+entityIdCacheBucket.getDatabaseColumn()+" from grouper_sync_member gsm, grouper_sync_group gsg , grouper_memberships_lw_v gmlv  "
                  + "where gsg.id = ? and gsm."+emailCacheDatabaseColumn+" = ? and gmlv.list_name = 'members' and gmlv.member_id = gsm.member_id  and gmlv.group_id = gsg.group_id ";
              
              entityIdValue = new GcDbAccess().sql(sql).addBindVar(targetGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup().getId())
              .addBindVar(email).select(String.class);
              
              if (StringUtils.isNotBlank(entityIdValue)) {
                addBackMember = true;
              }
            }
            
            GrouperGoogleApiCommands.deleteGoogleRoleMembership(googleConfiguration.getGoogleExternalSystemConfigId(),
                grouperGoogleGroup.getId(), entityIdValue, email, "OWNER", addBackManager, addBackMember);
          }
        }
        fieldNamesToUpdate.add(fieldName);
      }
      

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
//    if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isSelectAllGroups() &&
//        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isSelectAllEntities()) {
      grouperProvisionerDaoCapabilities.setCanRetrieveAllData(true);
//    }
//    if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isSelectAllEntities()) {
      grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
//    }
//    if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isSelectAllGroups()) {
      grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
//    }
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
