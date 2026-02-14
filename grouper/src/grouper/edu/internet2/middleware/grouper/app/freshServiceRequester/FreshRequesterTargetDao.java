package edu.internet2.middleware.grouper.app.freshServiceRequester;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class FreshRequesterTargetDao extends GrouperProvisionerTargetDaoBase {
  
  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
  }

  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }
  
  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();
    long startNanos = System.nanoTime();
    
    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      List<FreshRequesterGroup> requesterGroups = FreshRequesterApiCommands.retrieveRequesterGroups(freshserviceConfiguration.getFreshserviceExternalSystemConfigId());
      
      for (FreshRequesterGroup requesterGroup : requesterGroups) {
        ProvisioningGroup targetGroup = requesterGroup.toProvisioningGroup();
        results.add(targetGroup);
      }
      return new TargetDaoRetrieveAllGroupsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllGroups", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {
    long startNanos = System.nanoTime();
    
    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      if (StringUtils.equals("id", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        FreshRequesterGroup requesterGroup = FreshRequesterApiCommands.retrieveRequesterGroup(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(),
            GrouperUtil.longValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()));
        ProvisioningGroup targetGroup = requesterGroup == null ? null : requesterGroup.toProvisioningGroup();
        return new TargetDaoRetrieveGroupResponse(targetGroup);
      } else if (StringUtils.equals("name", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        List<FreshRequesterGroup> requesterGroups = FreshRequesterApiCommands.retrieveRequesterGroups(freshserviceConfiguration.getFreshserviceExternalSystemConfigId());
        for (FreshRequesterGroup requesterGroup : requesterGroups) {
          if (StringUtils.equals(requesterGroup.getName(), GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()))) {
            ProvisioningGroup targetGroup = requesterGroup == null ? null : requesterGroup.toProvisioningGroup();
            return new TargetDaoRetrieveGroupResponse(targetGroup);
          } 
        } 
      } else {
        throw new RuntimeException("id or name is required as a group search attribute");
      }
      return new TargetDaoRetrieveGroupResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {
    List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();
    long startNanos = System.nanoTime();
    
    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      List<FreshRequesterUser> requesters = FreshRequesterApiCommands.retrieveRequesterUsers(freshserviceConfiguration.getFreshserviceExternalSystemConfigId());
      for (FreshRequesterUser requester : requesters) {
        ProvisioningEntity targetEntity = requester.toProvisioningEntity();
        results.add(targetEntity);
      }
      return new TargetDaoRetrieveAllEntitiesResponse(results);
    }
    finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {
    long startNanos = System.nanoTime();
    
    FreshRequesterUser requester = null;
    
    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      if (StringUtils.equals("email", targetDaoRetrieveEntityRequest.getSearchAttribute())) {
        requester = FreshRequesterApiCommands.retrieveRequesterUserByEmail(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(),
            targetDaoRetrieveEntityRequest.getSearchAttributeValue().toString());
      } else if (StringUtils.equals("id", targetDaoRetrieveEntityRequest.getSearchAttribute())) {
        requester = FreshRequesterApiCommands.retrieveRequesterUser(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(),
            GrouperUtil.longValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue()));
      } else {
        throw new RuntimeException("Not expecting search attribute '" + targetDaoRetrieveEntityRequest.getSearchAttribute() + "'");
      }
      
      ProvisioningEntity targetEntity = requester == null ? null : requester.toProvisioningEntity();
      
      TargetDaoRetrieveEntityResponse targetDaoRetrieveEntityResponse = new TargetDaoRetrieveEntityResponse(targetEntity);
      if (targetDaoRetrieveEntityRequest.isIncludeNativeEntity()) {
        targetDaoRetrieveEntityResponse.setTargetNativeEntity(requester);
      }
      return targetDaoRetrieveEntityResponse;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }
  
  @Override
  public TargetDaoInsertMembershipResponse insertMembership(TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoInsertMembershipRequest.getTargetMembership();
    
    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      FreshRequesterApiCommands.addGroupMembership(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), 
          GrouperUtil.longValue(targetMembership.getProvisioningGroupId()),GrouperUtil.longValue(targetMembership.getProvisioningEntityId()));
      
      targetMembership.setProvisioned(true);
      return new TargetDaoInsertMembershipResponse();
    } catch(Exception e) {
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
  public TargetDaoDeleteMembershipResponse deleteMembership(TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {
    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoDeleteMembershipRequest.getTargetMembership();
    
    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      FreshRequesterApiCommands.removeGroupMembership(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(), 
          GrouperUtil.longValue(targetMembership.getProvisioningGroupId()),GrouperUtil.longValue(targetMembership.getProvisioningEntityId()));
      
      targetMembership.setProvisioned(true);
      return new TargetDaoDeleteMembershipResponse();
    } catch(Exception e) {
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
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();
    
    String targetGroupId = resolveTargetGroupId(targetGroup, this.getGrouperProvisioner());
    List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();
    
    if (StringUtils.isBlank(targetGroupId)) {
      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
    }
    
    try {
      FreshRequesterConfiguration freshserviceConfiguration = (FreshRequesterConfiguration) this.getGrouperProvisioner()
          .retrieveGrouperProvisioningConfiguration();
      
      List<FreshRequesterUser> requesters = FreshRequesterApiCommands.retrieveMembershipsByGroup(freshserviceConfiguration.getFreshserviceExternalSystemConfigId(),
          GrouperUtil.longValue(targetGroupId));
      
      for(FreshRequesterUser requester : requesters) {
        ProvisioningMembership targetMembership = new ProvisioningMembership();
        targetMembership.setProvisioningGroupId(targetGroupId);
        targetMembership.setProvisioningEntityId(Long.toString(requester.getId()));
        provisioningMemberships.add(targetMembership);
      }
      
      return new TargetDaoRetrieveMembershipsByGroupResponse(provisioningMemberships);
      
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByGroup", startNanos));
    }
  }
  
  public String resolveTargetGroupId(ProvisioningGroup targetGroup, GrouperProvisioner grouperProvisioner) {
    if (targetGroup == null) {
      return null;
    }
    
    if (StringUtils.isNotBlank(targetGroup.getId())) {
      return targetGroup.getId();
    }
    
    TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest = new TargetDaoRetrieveGroupsRequest();
    targetDaoRetrieveGroupsRequest.setTargetGroups(GrouperUtil.toList(targetGroup));
    targetDaoRetrieveGroupsRequest.setIncludeAllMembershipsIfApplicable(false);
    TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveGroups(
        targetDaoRetrieveGroupsRequest);

    if (targetDaoRetrieveGroupsResponse == null || GrouperUtil.length(targetDaoRetrieveGroupsResponse.getTargetGroups()) == 0) {
      return null;
    }
    
    return targetDaoRetrieveGroupsResponse.getTargetGroups().get(0).getId();
  }
  

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertMembership(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMembership(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByGroup(true);
    
  }

}
