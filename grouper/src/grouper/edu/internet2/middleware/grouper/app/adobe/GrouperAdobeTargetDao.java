package edu.internet2.middleware.grouper.app.adobe;

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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsResponse;
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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupResponse;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

public class GrouperAdobeTargetDao extends GrouperProvisionerTargetDaoBase {

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

      GrouperAdobeConfiguration adobeConfiguration = (GrouperAdobeConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      String orgId = adobeConfiguration.getOrgId();

      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

      List<GrouperAdobeGroup> grouperAdobeGroups = GrouperAdobeApiCommands.retrieveAdobeGroups(adobeConfiguration.getAdobeExternalSystemConfigId(), orgId);
      
      TargetDaoRetrieveAllGroupsResponse response = new TargetDaoRetrieveAllGroupsResponse(results);
      
      Map<ProvisioningGroup,Object> targetGroupToTargetNativeGroup = response.getTargetGroupToTargetNativeGroup();
      
      cacheGroupNameToGroup.clear();
      cacheGroupIdToGroup.clear();
      populateGroupCache(grouperAdobeGroups);

      for (GrouperAdobeGroup grouperAdobeGroup : grouperAdobeGroups) {
        ProvisioningGroup targetGroup = grouperAdobeGroup.toProvisioningGroup();
        results.add(targetGroup);
        targetGroupToTargetNativeGroup.put(targetGroup, grouperAdobeGroup);
      }

      return response;
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

      GrouperAdobeConfiguration adobeConfiguration = (GrouperAdobeConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      String orgId = adobeConfiguration.getOrgId();

      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();
      
      boolean loadEntitiesToGrouperTable = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isLoadEntitiesToGrouperTable();

      List<GrouperAdobeUser> grouperAdobeUsers = GrouperAdobeApiCommands.retrieveAdobeUsers(adobeConfiguration.getAdobeExternalSystemConfigId(), loadEntitiesToGrouperTable, orgId);

      TargetDaoRetrieveAllEntitiesResponse targetDaoRetrieveAllEntitiesResponse = new TargetDaoRetrieveAllEntitiesResponse(results);

      Map<ProvisioningEntity, Object> targetEntityToTargetNativeEntity = targetDaoRetrieveAllEntitiesResponse.getTargetEntityToTargetNativeEntity();
      for (GrouperAdobeUser grouperAdobeUser : grouperAdobeUsers) {
        ProvisioningEntity targetEntity = grouperAdobeUser.toProvisioningEntity();
        results.add(targetEntity);
        if (targetDaoRetrieveAllEntitiesRequest.isIncludeNativeEntity()) {
          targetEntityToTargetNativeEntity.put(targetEntity, grouperAdobeUser);
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
      GrouperAdobeConfiguration adobeConfiguration = (GrouperAdobeConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      String orgId = adobeConfiguration.getOrgId();
      boolean loadEntitiesToGrouperTable = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isLoadEntitiesToGrouperTable();

      // we can retrieve by email only
      GrouperAdobeUser grouperAdobeUser = null;

      if (StringUtils.equals("email", targetDaoRetrieveEntityRequest.getSearchAttribute())) {
        grouperAdobeUser = GrouperAdobeApiCommands.retrieveAdobeUser(adobeConfiguration.getAdobeExternalSystemConfigId(), 
            GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue()), loadEntitiesToGrouperTable, orgId);
      } else {
        throw new RuntimeException("Not expecting search attribute '" + targetDaoRetrieveEntityRequest.getSearchAttribute() + "'");
      }
      
      ProvisioningEntity targetEntity = grouperAdobeUser == null ? null
          : grouperAdobeUser.toProvisioningEntity();

      TargetDaoRetrieveEntityResponse targetDaoRetrieveEntityResponse = new TargetDaoRetrieveEntityResponse(targetEntity);
      if (targetDaoRetrieveEntityRequest.isIncludeNativeEntity()) {
        targetDaoRetrieveEntityResponse.setTargetNativeEntity(grouperAdobeUser);
      }
      return targetDaoRetrieveEntityResponse;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }

  
  private static ExpirableCache<Boolean, Map<String, GrouperAdobeGroup>> cacheGroupNameToGroup = new ExpirableCache<Boolean, Map<String, GrouperAdobeGroup>>(5);
  private static ExpirableCache<Boolean, Map<Long, GrouperAdobeGroup>> cacheGroupIdToGroup = new ExpirableCache<Boolean, Map<Long, GrouperAdobeGroup>>(5);
  
  private void populateGroupCache(List<GrouperAdobeGroup> allAdobeGroups) {
    
    Map<String, GrouperAdobeGroup> groupNameToGroup = new HashMap<String, GrouperAdobeGroup>();
    Map<Long, GrouperAdobeGroup> groupIdToGroup = new HashMap<Long, GrouperAdobeGroup>();
    for (GrouperAdobeGroup currentAdobeGroup: GrouperUtil.nonNull(allAdobeGroups)) {
      groupNameToGroup.put(currentAdobeGroup.getName(), currentAdobeGroup);
      groupIdToGroup.put(currentAdobeGroup.getId(), currentAdobeGroup);
    }
    cacheGroupNameToGroup.put(Boolean.TRUE, groupNameToGroup);
    cacheGroupIdToGroup.put(Boolean.TRUE, groupIdToGroup);
  }
  
  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {

    long startNanos = System.nanoTime();

    try {
      GrouperAdobeConfiguration adobeConfiguration = (GrouperAdobeConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String orgId = adobeConfiguration.getOrgId();

      GrouperAdobeGroup grouperAdobeGroup = null;

       if (StringUtils.equals("name", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        String name = GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue());
        if (StringUtils.isNotBlank(name)) {
          
          Map<String, GrouperAdobeGroup> groupNameToGroup = cacheGroupNameToGroup.get(Boolean.TRUE);
          
          grouperAdobeGroup = groupNameToGroup == null ? null : groupNameToGroup.get(name);
          
          if (grouperAdobeGroup == null) {
            List<GrouperAdobeGroup> allAdobeGroups = GrouperAdobeApiCommands.retrieveAdobeGroups(adobeConfiguration.getAdobeExternalSystemConfigId(), orgId);
            
            populateGroupCache(allAdobeGroups);
            
            groupNameToGroup = cacheGroupNameToGroup.get(Boolean.TRUE);
            grouperAdobeGroup = groupNameToGroup.get(name);
            
          }
          
        }
        
      } else if (StringUtils.equals("id", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        Long id = GrouperUtil.longValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue());
        if (id != null) {
          
          grouperAdobeGroup = retrieveGroupById(id);
          
          Map<Long, GrouperAdobeGroup> groupIdToGroup = cacheGroupIdToGroup.get(Boolean.TRUE);
          
          grouperAdobeGroup = groupIdToGroup == null ? null : groupIdToGroup.get(id);
          
          if (grouperAdobeGroup == null) {
            List<GrouperAdobeGroup> allAdobeGroups = GrouperAdobeApiCommands.retrieveAdobeGroups(adobeConfiguration.getAdobeExternalSystemConfigId(), orgId);
            
            populateGroupCache(allAdobeGroups);
            
            groupIdToGroup = cacheGroupIdToGroup.get(Boolean.TRUE);
            grouperAdobeGroup = groupIdToGroup.get(id);
            
          }
          
        }
        
      } else {
        throw new RuntimeException("Not expecting search attribute '" + targetDaoRetrieveGroupRequest.getSearchAttribute() + "'");
      }

      ProvisioningGroup targetGroup = grouperAdobeGroup == null ? null : grouperAdobeGroup.toProvisioningGroup();
      TargetDaoRetrieveGroupResponse response = new TargetDaoRetrieveGroupResponse(targetGroup);
      response.setTargetNativeGroup(grouperAdobeGroup);
      return response;

    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
    }
  }

  @Override
  public TargetDaoInsertGroupsResponse insertGroups(TargetDaoInsertGroupsRequest targetDaoInsertGroupsRequest) {
    
    long startNanos = System.nanoTime();
    
    List<ProvisioningGroup> groupsToInsert = targetDaoInsertGroupsRequest.getTargetGroups();
    

    try {
      GrouperAdobeConfiguration adobeConfiguration = (GrouperAdobeConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      String orgId = adobeConfiguration.getOrgId();
      
      for (ProvisioningGroup groupToInsert: groupsToInsert) {
        GrouperAdobeGroup grouperAdobeGroup = GrouperAdobeGroup.fromProvisioningGroup(groupToInsert, null);
        
        GrouperAdobeApiCommands.createAdobeGroup(adobeConfiguration.getAdobeExternalSystemConfigId(), grouperAdobeGroup, orgId);
        
      }
      
      TargetDaoInsertGroupsResponse response = new TargetDaoInsertGroupsResponse();
      
      List<GrouperAdobeGroup> allAdobeGroups = GrouperAdobeApiCommands.retrieveAdobeGroups(adobeConfiguration.getAdobeExternalSystemConfigId(), orgId);
      
      populateGroupCache(allAdobeGroups);
      
      Map<String, GrouperAdobeGroup> groupNameToGroup = cacheGroupNameToGroup.get(Boolean.TRUE);
      
      for (ProvisioningGroup targetGroup: groupsToInsert) {
        String name = targetGroup.getName();
        GrouperAdobeGroup grouperAdobeGroup = groupNameToGroup.get(name);
        targetGroup.setId(grouperAdobeGroup.getId().toString());
        targetGroup.setProvisioned(true);
        
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(true);
        }
      }

      return response;
    } catch (Exception e) {
      for (ProvisioningGroup targetGroup: groupsToInsert) {
        targetGroup.setProvisioned(false);
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
  public TargetDaoInsertMembershipResponse insertMembership(TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoInsertMembershipRequest.getTargetMembership();
    

    try {
      String email = targetMembership.getProvisioningEntity().getEmail();
      String groupName = targetMembership.getProvisioningGroup().getName();
      
      GrouperAdobeConfiguration adobeConfiguration = (GrouperAdobeConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String orgId = adobeConfiguration.getOrgId();

      GrouperAdobeApiCommands.associateUserToGroup(adobeConfiguration.getAdobeExternalSystemConfigId(), email, groupName, orgId);

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
      
      String email = targetMembership.getProvisioningEntity().getEmail();
      String groupName = targetMembership.getProvisioningGroup().getName();
      
      GrouperAdobeConfiguration adobeConfiguration = (GrouperAdobeConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String orgId = adobeConfiguration.getOrgId();

      GrouperAdobeApiCommands.disassociateUserFromGroup(adobeConfiguration.getAdobeExternalSystemConfigId(), email, groupName, orgId);

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
      throw new RuntimeException("Failed to delete Adobe group member (groupId '" + targetMembership.getProvisioningGroupId() + "', member '" + targetMembership.getProvisioningEntityId() + "'", e);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMembership", startNanos));
    }
  }

  public GrouperAdobeGroup retrieveGroupById(Object id) {
   
    if (id == null) {
      return null;
    }
    id = GrouperUtil.longValue(id);
    
    Map<Long, GrouperAdobeGroup> groupIdToGroup = cacheGroupIdToGroup.get(Boolean.TRUE);
    
    GrouperAdobeGroup grouperAdobeGroup = groupIdToGroup == null ? null : groupIdToGroup.get(id);
    
    if (grouperAdobeGroup == null) {
      
      GrouperAdobeConfiguration adobeConfiguration = (GrouperAdobeConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String orgId = adobeConfiguration.getOrgId();

      List<GrouperAdobeGroup> allAdobeGroups = GrouperAdobeApiCommands.retrieveAdobeGroups(adobeConfiguration.getAdobeExternalSystemConfigId(), orgId);
      
      populateGroupCache(allAdobeGroups);
      
      groupIdToGroup = cacheGroupIdToGroup.get(Boolean.TRUE);
      grouperAdobeGroup = groupIdToGroup.get(id);
      
    }

    return grouperAdobeGroup;
  }
  
  public GrouperAdobeGroup retrieveGroupByName(String name) {
    
    Map<String, GrouperAdobeGroup> groupNameToGroup = cacheGroupNameToGroup.get(Boolean.TRUE);
    
    GrouperAdobeGroup grouperAdobeGroup = groupNameToGroup == null ? null : groupNameToGroup.get(name);
    
    if (grouperAdobeGroup == null) {
      
      GrouperAdobeConfiguration adobeConfiguration = (GrouperAdobeConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String orgId = adobeConfiguration.getOrgId();

      List<GrouperAdobeGroup> allAdobeGroups = GrouperAdobeApiCommands.retrieveAdobeGroups(adobeConfiguration.getAdobeExternalSystemConfigId(), orgId);
      
      populateGroupCache(allAdobeGroups);
      
      groupNameToGroup = cacheGroupNameToGroup.get(Boolean.TRUE);
      grouperAdobeGroup = groupNameToGroup.get(name);
      
    }

    return grouperAdobeGroup;
  }
  
  @Override
  public TargetDaoUpdateGroupResponse updateGroup(
      TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();

    try {
      GrouperAdobeConfiguration adobeConfiguration = (GrouperAdobeConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      String orgId = adobeConfiguration.getOrgId();
      
      // lets make sure we are doing the right thing
      Set<String> fieldNamesToUpdate = new HashSet<String>();
      
      String oldGroupName = null;
      String newGroupName = null;
      
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToUpdate.add(fieldName);
        
        GrouperUtil.assertion(provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.update, "change action must be update!");
        
        if (StringUtils.equals(fieldName, "name")) {
          oldGroupName = GrouperUtil.stringValue(provisioningObjectChange.getOldValue());
          newGroupName = GrouperUtil.stringValue(provisioningObjectChange.getNewValue());
        } else {
          throw new RuntimeException("Only name should be configured for updates!");
        }
       
      }
      
      if (StringUtils.isBlank(oldGroupName) || StringUtils.isBlank(newGroupName)) {
        throw new RuntimeException();
      }
      
      GrouperAdobeApiCommands.updateAdobeGroup(adobeConfiguration.getAdobeExternalSystemConfigId(), oldGroupName, newGroupName, orgId);

      targetGroup.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }
      
      // select the group from group name cache based on the old group name. proceed only if it's not null. 
      // change the name in the object to the new name
      // delete the key based on the old group name from the group name cache
      // Add to the group name cache based on the new name
      
      GrouperAdobeGroup grouperAdobeGroup = cacheGroupNameToGroup.get(Boolean.TRUE).get(oldGroupName);
      if (grouperAdobeGroup != null) {
        grouperAdobeGroup.setName(newGroupName);
        cacheGroupNameToGroup.get(Boolean.TRUE).remove(oldGroupName);
        cacheGroupNameToGroup.get(Boolean.TRUE).put(newGroupName, grouperAdobeGroup);
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
      GrouperAdobeConfiguration adobeConfiguration = (GrouperAdobeConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String orgId = adobeConfiguration.getOrgId();

      GrouperAdobeGroup grouperAdobeGroup = GrouperAdobeGroup.fromProvisioningGroup(targetGroup, null);
      
      GrouperAdobeApiCommands.deleteAdobeGroup(adobeConfiguration.getAdobeExternalSystemConfigId(), grouperAdobeGroup.getName(), orgId);

      targetGroup.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }
      
      Map<String, GrouperAdobeGroup> groupNameToGroup = cacheGroupNameToGroup.get(Boolean.TRUE);
      if (groupNameToGroup != null) {
        groupNameToGroup.remove(grouperAdobeGroup.getName());
      } 
      
      Map<Long, GrouperAdobeGroup> groupIdToGroup = cacheGroupIdToGroup.get(Boolean.TRUE);
      if (groupIdToGroup != null) {
        groupIdToGroup.remove(grouperAdobeGroup.getId());
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
      GrouperAdobeConfiguration adobeConfiguration = (GrouperAdobeConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String orgId = adobeConfiguration.getOrgId();
      
      boolean loadEntitiesToGrouperTable = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isLoadEntitiesToGrouperTable();
      
      GrouperAdobeUser grouperAdobeUser = null;
      if (StringUtils.equals("email", targetDaoRetrieveMembershipsByEntityRequest.getSearchAttribute())) {
        grouperAdobeUser = GrouperAdobeApiCommands.retrieveAdobeUser(adobeConfiguration.getAdobeExternalSystemConfigId(), 
            GrouperUtil.stringValue(targetDaoRetrieveMembershipsByEntityRequest.getSearchAttributeValue()), loadEntitiesToGrouperTable, orgId);
      } else if (StringUtils.isNotBlank(targetEntity.getEmail())) {
        grouperAdobeUser = GrouperAdobeApiCommands.retrieveAdobeUser(adobeConfiguration.getAdobeExternalSystemConfigId(), 
            targetEntity.getEmail(), loadEntitiesToGrouperTable, orgId);
      } else {
        throw new RuntimeException("Email not found in targetEntity.getEmail and also in search attibute '" + targetDaoRetrieveMembershipsByEntityRequest.getSearchAttribute() + "'");
      }
      
      Set<String> groups = grouperAdobeUser.getGroups();
      List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();
      Map<String, GrouperAdobeGroup> theCacheGroupNameToGroup = cacheGroupNameToGroup.get(Boolean.TRUE);

      if (theCacheGroupNameToGroup != null) {
        for (String groupName: GrouperUtil.nonNull(groups)) {
          
          GrouperAdobeGroup grouperAdobeGroup = theCacheGroupNameToGroup.get(groupName);
          if (grouperAdobeGroup != null) {
            ProvisioningMembership targetMembership = new ProvisioningMembership(false);
            targetMembership.setProvisioningGroupId(grouperAdobeGroup.getId().toString());
            targetMembership.setProvisioningEntityId(targetEntity.getId());
            provisioningMemberships.add(targetMembership);
          }
        }
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
//      GrouperAdobeConfiguration duoConfiguration = (GrouperAdobeConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//
//      Set<String> userIds = GrouperAdobeApiCommands.retrieveAdobeGroupMembers(duoConfiguration.getAdobeExternalSystemConfigId(), targetGroup.getId());
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
  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();

    try {
      GrouperAdobeConfiguration duoConfiguration = (GrouperAdobeConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      String orgId = duoConfiguration.getOrgId();
      
      // lets make sure we are doing the right thing
      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToUpdate.add(fieldName);
      }
      
      GrouperAdobeUser grouperAdobeUser = GrouperAdobeUser.fromProvisioningEntity(targetEntity, null);
      GrouperAdobeApiCommands.updateAdobeUser(duoConfiguration.getAdobeExternalSystemConfigId(), grouperAdobeUser, fieldNamesToUpdate, orgId);

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
      GrouperAdobeConfiguration adobeConfiguration = (GrouperAdobeConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      String orgId = adobeConfiguration.getOrgId();
      String userTypeOnCreate = adobeConfiguration.getUserTypeOnCreate();
      
      GrouperAdobeUser grouperAdobeUser = GrouperAdobeUser.fromProvisioningEntity(targetEntity, null);
      
      GrouperAdobeUser createdAdobeUser = GrouperAdobeApiCommands.createAdobeUser(adobeConfiguration.getAdobeExternalSystemConfigId(), grouperAdobeUser, userTypeOnCreate, orgId);

      if (createdAdobeUser != null) {
        targetEntity.setId(createdAdobeUser.getId());
        targetEntity.setProvisioned(true);
  
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(true);
        }
      } else {
        
        throw new RuntimeException("Cannot create entity!");

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
      GrouperAdobeConfiguration adobeConfiguration = (GrouperAdobeConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String orgId = adobeConfiguration.getOrgId();
      boolean deleteAccountWhenDeleteUser = adobeConfiguration.isDeleteAccountWhenDeleteUser();
      
      GrouperAdobeUser grouperAdobeUser = GrouperAdobeUser.fromProvisioningEntity(targetEntity, null);
      
      GrouperAdobeApiCommands.deleteAdobeUser(adobeConfiguration.getAdobeExternalSystemConfigId(), grouperAdobeUser.getEmail(), deleteAccountWhenDeleteUser, orgId);

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
      GrouperAdobeConfiguration adobeConfiguration = (GrouperAdobeConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String orgId = adobeConfiguration.getOrgId();
      
      TargetDaoRetrieveAllGroupsResponse targetDaoRetrieveAllGroupsResponse = this.retrieveAllGroups(new TargetDaoRetrieveAllGroupsRequest(false));
      
      List<ProvisioningGroup> targetGroups = targetDaoRetrieveAllGroupsResponse.getTargetGroups();
      
      Map<ProvisioningGroup,Object> targetGroupToTargetNativeGroups = targetDaoRetrieveAllGroupsResponse.getTargetGroupToTargetNativeGroup();
      
      targetData.setProvisioningGroups(targetGroups);
      
      boolean loadEntitiesToGrouperTable = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isLoadEntitiesToGrouperTable();
      
      List<GrouperAdobeUser> adobeUsers = GrouperAdobeApiCommands.retrieveAdobeUsers(adobeConfiguration.getAdobeExternalSystemConfigId(),
          loadEntitiesToGrouperTable, orgId);
      List<ProvisioningMembership> targetMemberships = new ArrayList<>();
      targetData.setProvisioningMemberships(targetMemberships);
      
      List<ProvisioningEntity> targetEntities = new ArrayList<ProvisioningEntity>();
      targetData.setProvisioningEntities(targetEntities);
      
      Map<ProvisioningEntity, Object> targetEntityToTargetNativeEntity = targetDaoRetrieveAllDataResponse.getTargetEntityToTargetNativeEntity();
      
      Map<ProvisioningGroup,Object> targetGroupToTargetNativeGroup = targetDaoRetrieveAllDataResponse.getTargetGroupToTargetNativeGroup();
      targetGroupToTargetNativeGroup.putAll(targetGroupToTargetNativeGroups);

      for (GrouperAdobeUser adobeUser: adobeUsers) {
        
        ProvisioningEntity targetEntity = adobeUser.toProvisioningEntity();
        targetEntities.add(targetEntity);

        if (targetDaoRetrieveAllDataRequest.isIncludeNativeEntity()) {
          targetEntityToTargetNativeEntity.put(targetEntity, adobeUser);
        }

        Set<String> groupNames = GrouperUtil.nonNull(adobeUser.getGroups());
        
        for (String group: groupNames) {
          if (cacheGroupNameToGroup.get(Boolean.TRUE).containsKey(group)) {
            GrouperAdobeGroup grouperAdobeGroup = cacheGroupNameToGroup.get(Boolean.TRUE).get(group);
            ProvisioningMembership targetMembership = new ProvisioningMembership(false);
            targetMembership.setProvisioningEntityId(adobeUser.getId());
            targetMembership.setProvisioningGroupId(grouperAdobeGroup.getId().toString());
            targetMemberships.add(targetMembership);
          }
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
    grouperProvisionerDaoCapabilities.setCanInsertGroups(true);
    grouperProvisionerDaoCapabilities.setCanInsertMembership(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllData(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    
  }

  //  @Override
  //  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
  //    long startNanos = System.nanoTime();
  //    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();
  //
  //    try {
  //      GrouperAdobeConfiguration duoConfiguration = (GrouperAdobeConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
  //
  //      Set<String> userIds = GrouperAdobeApiCommands.retrieveAdobeGroupMembers(duoConfiguration.getAdobeExternalSystemConfigId(), targetGroup.getId());
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
