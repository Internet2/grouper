package edu.internet2.middleware.grouper.app.remedyV2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipResponse;
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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class GrouperRemedyTargetDao extends GrouperProvisionerTargetDaoBase {
  
  @Override
  public TargetDaoDeleteMembershipResponse deleteMembership(TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {
    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoDeleteMembershipRequest.getTargetMembership();

    try {
      GrouperRemedyConfiguration remedyConfiguration = (GrouperRemedyConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
     
      String remedyExternalSystemConfigId = remedyConfiguration.getRemedyExternalSystemConfigId();
      
      Long permissionGroupId = targetMembership.retrieveAttributeValueLong("permissionGroupId");
      String remedyLoginId = targetMembership.retrieveAttributeValueString("remedyLoginId");
      String personId = targetMembership.retrieveAttributeValueString("personId");
      String permissionGroup = targetMembership.retrieveAttributeValueString("permissionGroup");
      
      //TODO see if we can fetch the group by id
      Map<Long, GrouperRemedyGroup> remedyGroups = GrouperRemedyApiCommands.retrieveRemedyGroups(remedyExternalSystemConfigId);
      GrouperRemedyGroup grouperRemedyGroup = null;
      if (remedyGroups.containsKey(permissionGroupId)) {
        grouperRemedyGroup = remedyGroups.get(permissionGroupId);
      }
      
      if (grouperRemedyGroup == null) {
        return new TargetDaoDeleteMembershipResponse();
      }
      
      GrouperRemedyUser grouperRemedyUser = GrouperRemedyApiCommands.retrieveRemedyUser(remedyExternalSystemConfigId, remedyLoginId);
      if (grouperRemedyUser == null) {
        return new TargetDaoDeleteMembershipResponse();
      }
      
      Boolean removed = GrouperRemedyApiCommands.removeUserFromRemedyGroup(remedyExternalSystemConfigId, grouperRemedyUser, grouperRemedyGroup);
      if (removed != null) {
        targetMembership.setProvisioned(true);
        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(true);
        }
      }
      
      return new TargetDaoDeleteMembershipResponse();
    } catch (Exception e) {
      targetMembership.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMembership", startNanos));
    }
  }
  
  @Override
  public TargetDaoInsertMembershipResponse insertMembership(TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoInsertMembershipRequest.getTargetMembership();
    Long permissionGroupId = targetMembership.retrieveAttributeValueLong("permissionGroupId");
    String remedyLoginId = targetMembership.retrieveAttributeValueString("remedyLoginId");
    String personId = targetMembership.retrieveAttributeValueString("personId");
    String permissionGroup = targetMembership.retrieveAttributeValueString("permissionGroup");

    try {
      GrouperRemedyConfiguration remedyConfiguration = (GrouperRemedyConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      String remedyExternalSystemConfigId = remedyConfiguration.getRemedyExternalSystemConfigId();
      GrouperRemedyGroup grouperRemedyGroup = GrouperRemedyApiCommands.retrieveRemedyGroup(remedyExternalSystemConfigId, permissionGroupId);
      
      if (grouperRemedyGroup == null) {
        targetMembership.getProvisioningMembershipWrapper().setErrorCode(GcGrouperSyncErrorCode.DNE);
        throw new RuntimeException("group doesn't exist: "+permissionGroupId);
      }
      
      GrouperRemedyUser grouperRemedyUser = GrouperRemedyApiCommands.retrieveRemedyUser(remedyExternalSystemConfigId, remedyLoginId);
      if (grouperRemedyUser == null) {
        targetMembership.getProvisioningMembershipWrapper().setErrorCode(GcGrouperSyncErrorCode.DNE);
        throw new RuntimeException("user doesn't exist: "+remedyLoginId);
      }
      
      GrouperRemedyApiCommands.assignUserToRemedyGroup(remedyExternalSystemConfigId, remedyLoginId, personId,
          permissionGroup, permissionGroupId);
      
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
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(
      TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {
    
    long startNanos = System.nanoTime();

    try {
      
      GrouperRemedyConfiguration remedyConfiguration = (GrouperRemedyConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String remedyExternalSystemConfigId = remedyConfiguration.getRemedyExternalSystemConfigId();
      
      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();
      
      Map<String, GrouperRemedyUser> remedyUsers = GrouperRemedyApiCommands.retrieveRemedyUsers(remedyExternalSystemConfigId);

      for (GrouperRemedyUser grouperRemedyUser : remedyUsers.values()) {
        ProvisioningEntity targetEntity = grouperRemedyUser.toProvisioningEntity();
        results.add(targetEntity);
      }
  
      return new TargetDaoRetrieveAllEntitiesResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }
    
  }
  
  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    
    long startNanos = System.nanoTime();

    try {
      
      GrouperRemedyConfiguration remedyConfiguration = (GrouperRemedyConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      String remedyExternalSystemConfigId = remedyConfiguration.getRemedyExternalSystemConfigId();
      
      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();
      
      Map<Long, GrouperRemedyGroup> remedyGroups = GrouperRemedyApiCommands.retrieveRemedyGroups(remedyExternalSystemConfigId);

      for (GrouperRemedyGroup grouperRemedyGroup : remedyGroups.values()) {
        ProvisioningGroup targetGroup = grouperRemedyGroup.toProvisioningGroup();
        results.add(targetGroup);
      }
  
      return new TargetDaoRetrieveAllGroupsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllGroups", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveAllMembershipsResponse retrieveAllMemberships(TargetDaoRetrieveAllMembershipsRequest targetDaoRetrieveAllMembershipsRequest) {
    long startNanos = System.nanoTime();

    try {
      GrouperRemedyConfiguration remedyConfiguration = (GrouperRemedyConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String remedyExternalSystemConfigId = remedyConfiguration.getRemedyExternalSystemConfigId();
      
      Map<MultiKey, GrouperRemedyMembership> remedyMemberships = GrouperRemedyApiCommands.retrieveRemedyMemberships(remedyExternalSystemConfigId);

      List<ProvisioningMembership> results = new ArrayList<>();

      for (GrouperRemedyMembership grouperRemedyMembership : remedyMemberships.values()) {
        ProvisioningMembership targetMembership = new ProvisioningMembership();
        
        targetMembership.assignAttributeValue("permissionGroup", grouperRemedyMembership.getPermissionGroup());
        targetMembership.assignAttributeValue("permissionGroupId", grouperRemedyMembership.getPermissionGroupId());
        targetMembership.assignAttributeValue("personId", grouperRemedyMembership.getPersonId());
        targetMembership.assignAttributeValue("remedyLoginId", grouperRemedyMembership.getRemedyLoginId());
        
        results.add(targetMembership);
      }

      return new TargetDaoRetrieveAllMembershipsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllMemberships", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {
    
    long startNanos = System.nanoTime();

    try {      
      GrouperRemedyConfiguration remedyConfiguration = (GrouperRemedyConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String remedyExternalSystemConfigId = remedyConfiguration.getRemedyExternalSystemConfigId();
      
      // we can only retrieve by login id
      GrouperRemedyUser grouperRemedyUser = null;

      if (StringUtils.equals("remedyLoginId", targetDaoRetrieveEntityRequest.getSearchAttribute())) {
        grouperRemedyUser = GrouperRemedyApiCommands.retrieveRemedyUser(
            remedyExternalSystemConfigId,
            GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue()));
      }  else {
        throw new RuntimeException("Not expecting search attribute '" + targetDaoRetrieveEntityRequest.getSearchAttribute() + "'");
      }
      
      ProvisioningEntity targetEntity = grouperRemedyUser == null ? null
          : grouperRemedyUser.toProvisioningEntity();

      TargetDaoRetrieveEntityResponse targetDaoRetrieveEntityResponse = new TargetDaoRetrieveEntityResponse(targetEntity);
      if (targetDaoRetrieveEntityRequest.isIncludeNativeEntity()) {
        targetDaoRetrieveEntityResponse.setTargetNativeEntity(grouperRemedyUser);
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
      GrouperRemedyConfiguration remedyConfiguration = (GrouperRemedyConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String remedyExternalSystemConfigId = remedyConfiguration.getRemedyExternalSystemConfigId();
      
      // we can only retrieve by permission group id
      GrouperRemedyGroup grouperRemedyGroup = null;

      if (StringUtils.equals("permissionGroupId", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        grouperRemedyGroup = GrouperRemedyApiCommands.retrieveRemedyGroup(
            remedyExternalSystemConfigId,
            GrouperUtil.longValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()));
      }  else {
        throw new RuntimeException("Not expecting search attribute '" + targetDaoRetrieveGroupRequest.getSearchAttribute() + "'");
      }
      
      ProvisioningGroup targetGroup = grouperRemedyGroup == null ? null
          : grouperRemedyGroup.toProvisioningGroup();

      TargetDaoRetrieveGroupResponse targetDaoRetrieveGroupResponse = new TargetDaoRetrieveGroupResponse(targetGroup);
      targetDaoRetrieveGroupResponse.setTargetNativeGroup(grouperRemedyGroup);
      return targetDaoRetrieveGroupResponse;
      
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }
  
  
  /**
   * try to find group id from the target 
   * @param targetGroup
   * @return
   */
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
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();
    
    String targetGroupId = resolveTargetGroupId(targetGroup);
    List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();
    
    if (StringUtils.isBlank(targetGroupId)) {
      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
    }
    
    try {
      GrouperRemedyConfiguration remedyConfiguration = (GrouperRemedyConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String remedyExternalSystemConfigId = remedyConfiguration.getRemedyExternalSystemConfigId();
      
      Map<Long, GrouperRemedyGroup> remedyGroups = GrouperRemedyApiCommands.retrieveRemedyGroups(remedyExternalSystemConfigId);
      GrouperRemedyGroup grouperRemedyGroup = null;
      if (remedyGroups.containsKey(Long.valueOf(targetGroupId))) {
        grouperRemedyGroup = remedyGroups.get(Long.valueOf(targetGroupId));
      }
      
      if (grouperRemedyGroup == null) {
        return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
      }
      
      List<GrouperRemedyMembership> remedyMembershipsForGroup = GrouperRemedyApiCommands.retrieveRemedyMembershipsForGroup(remedyExternalSystemConfigId, grouperRemedyGroup);
      
      for (GrouperRemedyMembership remedyMembership : remedyMembershipsForGroup) {
        ProvisioningMembership targetMembership = new ProvisioningMembership();
        
        targetMembership.assignAttributeValue("permissionGroup", remedyMembership.getPermissionGroup());
        targetMembership.assignAttributeValue("permissionGroupId", remedyMembership.getPermissionGroupId());
        targetMembership.assignAttributeValue("personId", remedyMembership.getPersonId());
        targetMembership.assignAttributeValue("remedyLoginId", remedyMembership.getRemedyLoginId());
        
        provisioningMemberships.add(targetMembership);
      }
  
      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
      
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByGroup", startNanos));
    }
  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {

    grouperProvisionerDaoCapabilities.setCanDeleteMembership(true);
    grouperProvisionerDaoCapabilities.setCanInsertMembership(true);

    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllMemberships(true);

    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    

//    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsByEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsByGroup(true);

  }

}
