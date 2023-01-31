package edu.internet2.middleware.grouper.app.azure;

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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupsResponse;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GrouperAzureTargetDao extends GrouperProvisionerTargetDaoBase {

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

      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands
          .retrieveAzureGroups(azureConfiguration.getAzureExternalSystemConfigId());

      for (GrouperAzureGroup grouperAzureGroup : grouperAzureGroups) {
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

      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

      List<GrouperAzureUser> grouperAzureUsers = GrouperAzureApiCommands
          .retrieveAzureUsers(azureConfiguration.getAzureExternalSystemConfigId());

      for (GrouperAzureUser grouperAzureUser : grouperAzureUsers) {
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
  public TargetDaoRetrieveEntitiesResponse retrieveEntities(TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest) {

    long startNanos = System.nanoTime();
    
    List<ProvisioningEntity> targetEntities = targetDaoRetrieveEntitiesRequest.getTargetEntities();

    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      List<String> fieldValues = new ArrayList<>();
      for(ProvisioningEntity targetEntity: targetEntities) {
        String attributeValue = targetEntity.retrieveAttributeValueString(targetDaoRetrieveEntitiesRequest.getSearchAttribute());
        if (StringUtils.isNotBlank(attributeValue)) {
          fieldValues.add(attributeValue);
        }
      }
      
      List<GrouperAzureUser> azureUsers = GrouperAzureApiCommands.retrieveAzureUsers(
          azureConfiguration.getAzureExternalSystemConfigId(), fieldValues, targetDaoRetrieveEntitiesRequest.getSearchAttribute());
      
      List<ProvisioningEntity> targetEntitiesFromAzure = new ArrayList<>();
      
      for (GrouperAzureUser userFromAuzre: azureUsers) {
        targetEntitiesFromAzure.add(userFromAuzre.toProvisioningEntity());
      }
      
      TargetDaoRetrieveEntitiesResponse response = new TargetDaoRetrieveEntitiesResponse();
      response.setTargetEntities(targetEntitiesFromAzure);

      return response;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntities", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveGroupsResponse retrieveGroups(
      TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest) {

    long startNanos = System.nanoTime();

    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningGroup> grouperTargetGroups = targetDaoRetrieveGroupsRequest.getTargetGroups();
      
      List<String> fieldValues = new ArrayList<>();
      for(ProvisioningGroup targetGroup: grouperTargetGroups) {
        String attributeValue = targetGroup.retrieveAttributeValueString(targetDaoRetrieveGroupsRequest.getSearchAttribute());
        if (StringUtils.isNotBlank(attributeValue)) {
          fieldValues.add(attributeValue);
        }
      }
      
      // we can retrieve by id or displayName
      List<GrouperAzureGroup> azureGroups = GrouperAzureApiCommands.retrieveAzureGroups(
          azureConfiguration.getAzureExternalSystemConfigId(), fieldValues, targetDaoRetrieveGroupsRequest.getSearchAttribute());
      
      
      List<ProvisioningGroup> targetGroupsFromAzure = new ArrayList<>();
      
      for (GrouperAzureGroup groupFromAuzre: azureGroups) {
        targetGroupsFromAzure.add(groupFromAuzre.toProvisioningGroup());
      }
      
      TargetDaoRetrieveGroupsResponse response = new TargetDaoRetrieveGroupsResponse();
      response.setTargetGroups(targetGroupsFromAzure);
      
      return response;

    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroups", startNanos));
    }
  }

  @Override
  public TargetDaoInsertGroupsResponse insertGroups(TargetDaoInsertGroupsRequest targetDaoInsertGroupsRequest) {
    
    long startNanos = System.nanoTime();
    List<ProvisioningGroup> targetGroups = targetDaoInsertGroupsRequest.getTargetGroups();

    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      List<GrouperAzureGroup> grouperAzureGroups = new ArrayList<>();
      
      Map<GrouperAzureGroup, ProvisioningGroup> azureGroupToTargetGroup = new HashMap<>();
      
      Map<GrouperAzureGroup, Set<String>> groupToFieldNamesToInsert = new HashMap<>();
      
      for (ProvisioningGroup targetGroup: targetGroups) {
        GrouperAzureGroup grouperAzureGroup = GrouperAzureGroup.fromProvisioningGroup(targetGroup, null);
        grouperAzureGroups.add(grouperAzureGroup);
        azureGroupToTargetGroup.put(grouperAzureGroup, targetGroup);
        
        
        // lets make sure we are doing the right thing
        Set<String> fieldNamesToInsert = new HashSet<String>();

        // Initialize with fields that are required but have defaults and may not be configured explicitly
        fieldNamesToInsert.add("mailEnabled");
        fieldNamesToInsert.add("securityEnabled");
        
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
          String fieldName = provisioningObjectChange.getAttributeName();
          if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
            fieldNamesToInsert.add(fieldName);
          }
        }
        
        if (grouperAzureGroup.isGroupTypeUnified()) {
          fieldNamesToInsert.add("groupTypeUnified");
        }
        
        groupToFieldNamesToInsert.put(grouperAzureGroup, fieldNamesToInsert);
        
      }
      
      
      Map<GrouperAzureGroup, Exception> groupToMaybeException = GrouperAzureApiCommands.createAzureGroups(azureConfiguration.getAzureExternalSystemConfigId(), groupToFieldNamesToInsert);
      
      for (GrouperAzureGroup grouperAzureGroup: groupToMaybeException.keySet()) {
        
        Exception exception = groupToMaybeException.get(grouperAzureGroup);
        ProvisioningGroup targetGroup = azureGroupToTargetGroup.get(grouperAzureGroup);
        
        if (exception == null) {
          
          targetGroup.setId(grouperAzureGroup.getId());
          targetGroup.setProvisioned(true);

          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(true);
          }
        } else {
          targetGroup.setProvisioned(false);
          targetGroup.setException(exception);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(false);
          }
        }
        
      }

      return new TargetDaoInsertGroupsResponse();
    } catch (Exception e) {
      for (ProvisioningGroup targetGroup: targetGroups) {
        targetGroup.setProvisioned(false);
        targetGroup.setException(e);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(false);
        }
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertGroups", startNanos));
    }
  }

  @Override
  public TargetDaoInsertMembershipsResponse insertMemberships(TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {
    long startNanos = System.nanoTime();
    List<ProvisioningMembership> targetMemberships = targetDaoInsertMembershipsRequest.getTargetMemberships();

    try {
      
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

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
          Map<MultiKey, Exception> groupIdUserIdToException = GrouperAzureApiCommands.createAzureMemberships(azureConfiguration.getAzureExternalSystemConfigId(), groupId, userIds);
          
          for (MultiKey groupIduserId : groupIdUserIdToException.keySet()) {
            ProvisioningMembership targetMembership = groupIdUserIdToProvisioningMembership.get(groupIduserId);
            
            targetMembership.setProvisioned(false);
            targetMembership.setException(groupIdUserIdToException.get(groupIduserId));
            for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
              provisioningObjectChange.setProvisioned(false);
            }
          }
          
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
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      Map<ProvisioningMembership, Exception> targetMembershipsWithMaybeException = GrouperAzureApiCommands.deleteAzureMemberships(azureConfiguration.getAzureExternalSystemConfigId(), targetMemberships);

      for (ProvisioningMembership targetMembership: targetMembershipsWithMaybeException.keySet()) {
        
        Exception exception = targetMembershipsWithMaybeException.get(targetMembership);

        if (exception == null) {
          targetMembership.setProvisioned(true);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(true);
          }
        } else {
          targetMembership.setProvisioned(false);
          targetMembership.setException(exception);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(false);
          }
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

  @Override
  public TargetDaoUpdateGroupsResponse updateGroups(TargetDaoUpdateGroupsRequest targetDaoUpdateGroupsRequest) {
    
    long startNanos = System.nanoTime();
    List<ProvisioningGroup> targetGroups = targetDaoUpdateGroupsRequest.getTargetGroups();
    
    Map<GrouperAzureGroup, ProvisioningGroup> azureGroupToTargetGroup = new HashMap<>();
    Map<GrouperAzureGroup, Set<String>> azureGroupToFieldNamesToUpdate = new HashMap<>();
    
    for (ProvisioningGroup targetGroup: targetGroups) {
      GrouperAzureGroup grouperAzureGroup = GrouperAzureGroup.fromProvisioningGroup(targetGroup, null);
      azureGroupToTargetGroup.put(grouperAzureGroup, targetGroup);
      
      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToUpdate.add(fieldName);
      }
      
      azureGroupToFieldNamesToUpdate.put(grouperAzureGroup, fieldNamesToUpdate);
      
    }
    
    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      Map<GrouperAzureGroup, Exception> groupToMaybeException = GrouperAzureApiCommands.updateAzureGroups(azureConfiguration.getAzureExternalSystemConfigId(), azureGroupToFieldNamesToUpdate);
      
      for (GrouperAzureGroup grouperAzureGroup: groupToMaybeException.keySet()) {
        
        Exception exception = groupToMaybeException.get(grouperAzureGroup);
        ProvisioningGroup targetGroup = azureGroupToTargetGroup.get(grouperAzureGroup);
        
        if (exception == null) {
          
          targetGroup.setProvisioned(true);

          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(true);
          }
        } else {
          targetGroup.setProvisioned(false);
          targetGroup.setException(exception);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(false);
          }
        }
        
      }
      
      return new TargetDaoUpdateGroupsResponse();
    } catch (Exception e) {
      
      for (ProvisioningGroup targetGroup: targetGroups) {
        targetGroup.setProvisioned(false);
        targetGroup.setException(e);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(false);
        }
      }
      
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateEntities", startNanos));
    }
    
    
  }
  
  @Override
  public TargetDaoUpdateEntitiesResponse updateEntities(TargetDaoUpdateEntitiesRequest targetDaoUpdateEntitiesRequest) {
    
    long startNanos = System.nanoTime();
    List<ProvisioningEntity> targetEntities = targetDaoUpdateEntitiesRequest.getTargetEntities();
    
    Map<GrouperAzureUser, ProvisioningEntity> azureUserToTargetEntity = new HashMap<>();
    Map<GrouperAzureUser, Set<String>> azureUserToFieldNamesToUpdate = new HashMap<>();
    
    for (ProvisioningEntity targetEntity: targetEntities) {
      GrouperAzureUser grouperAzureUser = GrouperAzureUser.fromProvisioningEntity(targetEntity, null);
      azureUserToTargetEntity.put(grouperAzureUser, targetEntity);
      
      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToUpdate.add(fieldName);
      }
      
      azureUserToFieldNamesToUpdate.put(grouperAzureUser, fieldNamesToUpdate);
      
    }
    
    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      Map<GrouperAzureUser, Exception> userToMaybeException = GrouperAzureApiCommands.updateAzureUsers(azureConfiguration.getAzureExternalSystemConfigId(), azureUserToFieldNamesToUpdate);
      
      for (GrouperAzureUser grouperAzureUser: userToMaybeException.keySet()) {
        
        Exception exception = userToMaybeException.get(grouperAzureUser);
        ProvisioningEntity targetEntity = azureUserToTargetEntity.get(grouperAzureUser);
        
        if (exception == null) {
          
          targetEntity.setProvisioned(true);

          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(true);
          }
        } else {
          targetEntity.setProvisioned(false);
          targetEntity.setException(exception);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(false);
          }
        }
        
      }
      
      return new TargetDaoUpdateEntitiesResponse();
    } catch (Exception e) {
      
      for (ProvisioningEntity targetEntity: targetEntities) {
        targetEntity.setProvisioned(false);
        targetEntity.setException(e);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(false);
        }
      }
      
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateEntities", startNanos));
    }
    
  }

  
  @Override
  public TargetDaoDeleteGroupsResponse deleteGroups(TargetDaoDeleteGroupsRequest targetDaoDeleteGroupsRequest) {
    
    long startNanos = System.nanoTime();
    List<ProvisioningGroup> targetGroups = targetDaoDeleteGroupsRequest.getTargetGroups();

    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      
      List<GrouperAzureGroup> grouperAzureGroups = new ArrayList<>();
      
      Map<GrouperAzureGroup, ProvisioningGroup> azureGroupToTargetGroup = new HashMap<>();
      
      for (ProvisioningGroup targetGroup: targetGroups) {
        GrouperAzureGroup grouperAzureGroup = GrouperAzureGroup.fromProvisioningGroup(targetGroup, null);
        grouperAzureGroups.add(grouperAzureGroup);
        azureGroupToTargetGroup.put(grouperAzureGroup, targetGroup);
      }
      
      Map<GrouperAzureGroup, Exception> azureGroupsToMaybeException = GrouperAzureApiCommands.deleteAzureGroups(azureConfiguration.getAzureExternalSystemConfigId(), grouperAzureGroups);
      
      for (GrouperAzureGroup azureGroup: azureGroupsToMaybeException.keySet()) {
        
        Exception exception = azureGroupsToMaybeException.get(azureGroup);

        ProvisioningGroup targetGroup = azureGroupToTargetGroup.get(azureGroup);
        if (exception == null) {
          targetGroup.setProvisioned(true);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(true);
          }
        } else {
          targetGroup.setProvisioned(false);
          targetGroup.setException(exception);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(false);
          }
        }
        
      }
    
      return new TargetDaoDeleteGroupsResponse();
    } catch (Exception e) {
     
      for (ProvisioningGroup targetGroup: targetGroups) {
        targetGroup.setProvisioned(false);
        targetGroup.setException(e);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(false);
        }
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

    String targetEntityId = resolveTargetEntityId(targetEntity);
    List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();
    
    if (StringUtils.isBlank(targetEntityId)) {
      return new TargetDaoRetrieveMembershipsByEntityResponse(provisioningMemberships);
    }

    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      Set<String> groupIds = GrouperAzureApiCommands.retrieveAzureUserGroups(azureConfiguration.getAzureExternalSystemConfigId(), targetEntityId);
      
      for (String groupId : groupIds) {

        ProvisioningMembership targetMembership = new ProvisioningMembership();
        targetMembership.setProvisioningGroupId(groupId);
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
    
    String targetGroupId = resolveTargetGroupId(targetGroup);
    List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();
    
    if (StringUtils.isBlank(targetGroupId)) {
      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
    }
    
    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      Set<String> userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers(azureConfiguration.getAzureExternalSystemConfigId(), targetGroupId);
      
      for (String userId : userIds) {

        ProvisioningMembership targetMembership = new ProvisioningMembership();
        targetMembership.setProvisioningGroupId(targetGroup.getId());
        targetMembership.setProvisioningEntityId(userId);
        provisioningMemberships.add(targetMembership);
      }
  
      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByGroup", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveAllMembershipsResponse retrieveAllMemberships(TargetDaoRetrieveAllMembershipsRequest targetDaoRetrieveAllMembershipsRequest) {
    long startNanos = System.nanoTime();

    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands
          .retrieveAzureGroups(azureConfiguration.getAzureExternalSystemConfigId());

      List<ProvisioningMembership> results = new ArrayList<>();

      for (GrouperAzureGroup grouperAzureGroup : GrouperUtil.nonNull(grouperAzureGroups)) {
        Set<String> userIds = GrouperAzureApiCommands
            .retrieveAzureGroupMembers(azureConfiguration.getAzureExternalSystemConfigId(), grouperAzureGroup.getId());
        for (String userId : GrouperUtil.nonNull(userIds)) {
          
          ProvisioningMembership targetMembership = new ProvisioningMembership();
          targetMembership.setProvisioningEntityId(userId);
          targetMembership.setProvisioningGroupId(grouperAzureGroup.getId());
          results.add(targetMembership);
        }

      }

      return new TargetDaoRetrieveAllMembershipsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllMemberships", startNanos));
    }
  }

  
  
  
  @Override
  public TargetDaoInsertEntitiesResponse insertEntities(TargetDaoInsertEntitiesRequest targetDaoInsertEntitiesRequest) {
   
    
    long startNanos = System.nanoTime();
    List<ProvisioningEntity> targetEntities = targetDaoInsertEntitiesRequest.getTargetEntityInserts();

    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<GrouperAzureUser> grouperAzureUsers = new ArrayList<>();
      
      Map<GrouperAzureUser, ProvisioningEntity> azureUserToTargetEntity = new HashMap<>();
      for (ProvisioningEntity targetEntity: targetEntities) {
        GrouperAzureUser grouperAzureUser = GrouperAzureUser.fromProvisioningEntity(targetEntity, null);
        grouperAzureUsers.add(grouperAzureUser);
        azureUserToTargetEntity.put(grouperAzureUser, targetEntity);
      }
      
      Map<GrouperAzureUser, Exception> userToMaybeException = GrouperAzureApiCommands.createAzureUsers(azureConfiguration.getAzureExternalSystemConfigId(), grouperAzureUsers, null);
      
      for (GrouperAzureUser grouperAzureUser: userToMaybeException.keySet()) {
        
        Exception exception = userToMaybeException.get(grouperAzureUser);
        ProvisioningEntity targetEntity = azureUserToTargetEntity.get(grouperAzureUser);
        
        if (exception == null) {
          
          targetEntity.setId(grouperAzureUser.getId());
          targetEntity.setProvisioned(true);

          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(true);
          }
        } else {
          targetEntity.setProvisioned(false);
          targetEntity.setException(exception);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(false);
          }
        }
        
      }

      return new TargetDaoInsertEntitiesResponse();
    } catch (Exception e) {
      for (ProvisioningEntity targetEntity: targetEntities) {
        targetEntity.setProvisioned(false);
        targetEntity.setException(e);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(false);
        }
      }
      
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertEntities", startNanos));
    }
    
    
  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    
    grouperProvisionerDaoCapabilities.setDefaultBatchSize(20);
    
    grouperProvisionerDaoCapabilities.setCanDeleteGroups(true);
    
    grouperProvisionerDaoCapabilities.setCanDeleteEntities(true);

    grouperProvisionerDaoCapabilities.setCanDeleteMemberships(true);

    grouperProvisionerDaoCapabilities.setCanInsertEntities(true);

    grouperProvisionerDaoCapabilities.setCanInsertGroups(true);

    grouperProvisionerDaoCapabilities.setCanInsertMemberships(true);
    grouperProvisionerDaoCapabilities.setInsertMembershipsBatchSize(400);

    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllMemberships(true);

    grouperProvisionerDaoCapabilities.setCanRetrieveEntities(true);

    grouperProvisionerDaoCapabilities.setCanRetrieveGroups(true);

    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsByEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsByGroup(true);

    grouperProvisionerDaoCapabilities.setCanUpdateEntities(true);

    grouperProvisionerDaoCapabilities.setCanUpdateGroups(true);
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
  public TargetDaoDeleteEntitiesResponse deleteEntities(TargetDaoDeleteEntitiesRequest targetDaoDeleteEntitiesRequest) {
    
    long startNanos = System.nanoTime();
    List<ProvisioningEntity> targetEntities = targetDaoDeleteEntitiesRequest.getTargetEntities();
  
    try {
      GrouperAzureConfiguration azureConfiguration = (GrouperAzureConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
  
      List<GrouperAzureUser> grouperAzureUsers = new ArrayList<>();
      
      Map<GrouperAzureUser, ProvisioningEntity> azureUserToTargetEntity = new HashMap<>();
      
      for (ProvisioningEntity targetEntity: targetEntities) {
        GrouperAzureUser grouperAzureUser = GrouperAzureUser.fromProvisioningEntity(targetEntity, null);
        grouperAzureUsers.add(grouperAzureUser);
        azureUserToTargetEntity.put(grouperAzureUser, targetEntity);
      }
      
      Map<GrouperAzureUser, Exception> azureUsersToMaybeException = GrouperAzureApiCommands.deleteAzureUsers(azureConfiguration.getAzureExternalSystemConfigId(), grouperAzureUsers);
      
      for (GrouperAzureUser azureUser: azureUsersToMaybeException.keySet()) {
        
        Exception exception = azureUsersToMaybeException.get(azureUser);

        ProvisioningEntity targetEntity = azureUserToTargetEntity.get(azureUser);
        if (exception == null) {
          targetEntity.setProvisioned(true);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(true);
          }
        } else {
          targetEntity.setProvisioned(false);
          targetEntity.setException(exception);
          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
            provisioningObjectChange.setProvisioned(false);
          }
        }
        
      }
      
      return new TargetDaoDeleteEntitiesResponse();
    } catch (Exception e) {
      for (ProvisioningEntity targetEntity: targetEntities) {
        targetEntity.setProvisioned(false);
        targetEntity.setException(e);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(false);
        }
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteEntities", startNanos));
    }
  }

}
