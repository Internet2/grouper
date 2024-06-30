package edu.internet2.middleware.grouper.app.remedyV2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;

public class GrouperRemedyTargetDao extends GrouperProvisionerTargetDaoBase {
  
  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
  }

  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }

  @Override
  public TargetDaoDeleteMembershipResponse deleteMembership(TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {
    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoDeleteMembershipRequest.getTargetMembership();

    try {
      GrouperRemedyConfiguration remedyConfiguration = (GrouperRemedyConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
     
      String remedyExternalSystemConfigId = remedyConfiguration.getRemedyExternalSystemConfigId();
      
      Long permissionGroupId = targetMembership.retrieveAttributeValueLong("permissionGroupId");
      String remedyLoginId = targetMembership.retrieveAttributeValueString("remedyLoginId");
      
      Boolean removed = GrouperRemedyApiCommands.removeUserFromRemedyGroup(remedyExternalSystemConfigId, remedyLoginId, permissionGroupId);
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
//      GrouperRemedyGroup grouperRemedyGroup = GrouperRemedyApiCommands.retrieveRemedyGroup(remedyExternalSystemConfigId, permissionGroupId);
      
      if (permissionGroupId == null) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignMembershipError(targetMembership.getProvisioningMembershipWrapper(), GcGrouperSyncErrorCode.DNE, "Group does not have an id: " + permissionGroup);
        //targetMembership.getProvisioningMembershipWrapper().setErrorCode(GcGrouperSyncErrorCode.DNE);
        return new TargetDaoInsertMembershipResponse();
      }
      
//      GrouperRemedyUser grouperRemedyUser = GrouperRemedyApiCommands.retrieveRemedyUser(remedyExternalSystemConfigId, remedyLoginId);
      if (StringUtils.isBlank(personId)) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignMembershipError(targetMembership.getProvisioningMembershipWrapper(), GcGrouperSyncErrorCode.DNE, "User does not have an id: " + remedyLoginId);
        //targetMembership.getProvisioningMembershipWrapper().setErrorCode(GcGrouperSyncErrorCode.DNE);
        return new TargetDaoInsertMembershipResponse();
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
  
  private Map<Long, GrouperRemedyGroup> permissionGroupIdToGroup;
  
  private Map<String, GrouperRemedyGroup> permissionGroupToGroup;
  
  private synchronized Map<String, GrouperRemedyGroup> permissionGroupToGroup() {
    if (permissionGroupToGroup == null) {
      retrieveAllGroups(new TargetDaoRetrieveAllGroupsRequest(false));
    }
    return permissionGroupToGroup;
  }
  
  private synchronized Map<Long, GrouperRemedyGroup> permissionGroupIdToGroup() {
    if (permissionGroupIdToGroup == null) {
      retrieveAllGroups(new TargetDaoRetrieveAllGroupsRequest(false));
    }
    return permissionGroupIdToGroup;
  }


  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    
    long startNanos = System.nanoTime();

    try {
      
      GrouperRemedyConfiguration remedyConfiguration = (GrouperRemedyConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      String remedyExternalSystemConfigId = remedyConfiguration.getRemedyExternalSystemConfigId();
      
      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();
      
      Map<Long, GrouperRemedyGroup> remedyGroups = GrouperRemedyApiCommands.retrieveRemedyGroups(remedyExternalSystemConfigId);

      permissionGroupIdToGroup = new HashMap<>();
      permissionGroupToGroup = new HashMap<>();
      
      for (GrouperRemedyGroup grouperRemedyGroup : remedyGroups.values()) {
        permissionGroupIdToGroup.put(grouperRemedyGroup.getPermissionGroupId(), grouperRemedyGroup);
        permissionGroupToGroup.put(grouperRemedyGroup.getPermissionGroup(), grouperRemedyGroup);
        ProvisioningGroup targetGroup = grouperRemedyGroup.toProvisioningGroup();
        results.add(targetGroup);
      }
  
      return new TargetDaoRetrieveAllGroupsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllGroups", startNanos));
    }
  }
  
//  @Override
//  public TargetDaoRetrieveAllMembershipsResponse retrieveAllMemberships(TargetDaoRetrieveAllMembershipsRequest targetDaoRetrieveAllMembershipsRequest) {
//    long startNanos = System.nanoTime();
//
//    try {
//      GrouperRemedyConfiguration remedyConfiguration = (GrouperRemedyConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
//      String remedyExternalSystemConfigId = remedyConfiguration.getRemedyExternalSystemConfigId();
//      
//      Map<MultiKey, GrouperRemedyMembership> remedyMemberships = GrouperRemedyApiCommands.retrieveRemedyMemberships(remedyExternalSystemConfigId);
//
//      List<ProvisioningMembership> results = new ArrayList<>();
//
//      for (GrouperRemedyMembership grouperRemedyMembership : remedyMemberships.values()) {
//        ProvisioningMembership targetMembership = new ProvisioningMembership();
//        
//        targetMembership.assignAttributeValue("permissionGroup", grouperRemedyMembership.getPermissionGroup());
//        targetMembership.assignAttributeValue("permissionGroupId", grouperRemedyMembership.getPermissionGroupId());
//        targetMembership.assignAttributeValue("personId", grouperRemedyMembership.getPersonId());
//        targetMembership.assignAttributeValue("remedyLoginId", grouperRemedyMembership.getRemedyLoginId());
//        
//        results.add(targetMembership);
//      }
//
//      return new TargetDaoRetrieveAllMembershipsResponse(results);
//    } finally {
//      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllMemberships", startNanos));
//    }
//  }
  
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
      // we can only retrieve by permission group id
      GrouperRemedyGroup grouperRemedyGroup = null;

      if (StringUtils.equals("permissionGroupId", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        grouperRemedyGroup = permissionGroupIdToGroup().get(GrouperUtil.longValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()));
      } else if (StringUtils.equals("permissionGroup", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        grouperRemedyGroup = permissionGroupToGroup().get(targetDaoRetrieveGroupRequest.getSearchAttributeValue());
      } else {
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
  
  
  @Override
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();
    
    List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();
    
    try {
      GrouperRemedyConfiguration remedyConfiguration = (GrouperRemedyConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      String remedyExternalSystemConfigId = remedyConfiguration.getRemedyExternalSystemConfigId();
      
      Long permissionGroupId = targetGroup.retrieveAttributeValueLong("permissionGroupId");
      GrouperUtil.assertion(permissionGroupId != null, "Permission group id is null for: " + targetGroup.retrieveAttributeValueString("permissionGroup"));
      List<GrouperRemedyMembership> remedyMembershipsForGroup = GrouperRemedyApiCommands.retrieveRemedyMembershipsForGroup(remedyExternalSystemConfigId, permissionGroupId);
      
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
//    grouperProvisionerDaoCapabilities.setCanRetrieveAllMemberships(true);

    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    

//    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsByEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByGroup(true);

  }

}
