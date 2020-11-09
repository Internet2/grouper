package edu.internet2.middleware.grouperBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.box.sdk.BoxGroupMembership;
import com.box.sdk.BoxUser.Status;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperBoxTargetDao extends GrouperProvisionerTargetDaoBase {

  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    
    long startNanos = System.nanoTime();

    try {
      //boolean includeAllMembershipsIfApplicable = targetDaoRetrieveAllGroupsRequest == null ? false : targetDaoRetrieveAllGroupsRequest.isIncludeAllMembershipsIfApplicable();
      
      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();
      Map<String, GrouperBoxGroup> grouperBoxGroups = GrouperBoxCommands.retrieveBoxGroups();
      for (String groupName : grouperBoxGroups.keySet()) {
        ProvisioningGroup targetGroup = new ProvisioningGroup();
        targetGroup.setName(groupName);        
        results.add(targetGroup);
      }
  
      return new TargetDaoRetrieveAllGroupsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllGroups", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {
    
    long startNanos = System.nanoTime();

    try {
      //boolean includeAllMembershipsIfApplicable = targetDaoRetrieveAllEntitiesRequest == null ? false : targetDaoRetrieveAllEntitiesRequest.isIncludeAllMembershipsIfApplicable();
      
      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();
      Map<String, GrouperBoxUser> grouperBoxUsers = GrouperBoxCommands.retrieveBoxUsers();

      for (String loginId : grouperBoxUsers.keySet()) {
        GrouperBoxUser grouperBoxUser = grouperBoxUsers.get(loginId);
        ProvisioningEntity targetEntity = new ProvisioningEntity();
        targetEntity.setLoginId(loginId);
                
        Status boxStatus = grouperBoxUser.getBoxUserInfo().getStatus();
        if (boxStatus != null) {
          String statusString = boxStatus.name();
          targetEntity.addAttributeValue("boxStatus", statusString);
        }
        
        targetEntity.addAttributeValue("isBoxSyncEnabled", grouperBoxUser.getBoxUserInfo().getIsSyncEnabled());
                
        results.add(targetEntity);
      }
  
      return new TargetDaoRetrieveAllEntitiesResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }
  }
  
  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();

    try {
      GrouperBoxCommands.createBoxGroup(targetGroup.getName(), false);
      
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
  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest.getTargetGroup();

    try {
      GrouperBoxCommands.deleteBoxGroup(GrouperBoxCommands.retrieveBoxGroups().get(targetGroup.getName()), false);
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
  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();

    try {
      String newStatusString = targetEntity.retrieveAttributeValueString("boxStatus");
      Status newStatus = GrouperClientUtils.isBlank(newStatusString) ? null : Status.valueOf(newStatusString.toUpperCase());
      boolean newIsBoxSyncEnabled = targetEntity.retrieveAttributeValueBoolean("isBoxSyncEnabled");
            
      GrouperBoxUser grouperBoxUser = GrouperBoxCommands.retrieveBoxUser(targetEntity.getLoginId());
      grouperBoxUser.getBoxUserInfo().setStatus(newStatus);
      grouperBoxUser.getBoxUserInfo().setIsSyncEnabled(newIsBoxSyncEnabled);

      GrouperBoxCommands.updateBoxUser(grouperBoxUser, false);
      
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
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoRetrieveMembershipsByGroupRequest.getTargetGroup();
    
    try {      
      List<ProvisioningMembership> results = new ArrayList<ProvisioningMembership>();
      Collection<BoxGroupMembership.Info> boxGroupMemberships = GrouperBoxCommands.retrieveMembershipsForBoxGroup(GrouperBoxCommands.retrieveBoxGroups().get(targetGroup.getName()));

      for (BoxGroupMembership.Info groupBoxMembership : boxGroupMemberships) {
        String loginId = groupBoxMembership.getUser().getLogin();
        ProvisioningEntity targetEntity = new ProvisioningEntity();
        targetEntity.setLoginId(loginId);
        
        ProvisioningMembership targetMembership = new ProvisioningMembership();
        targetMembership.setProvisioningGroup(targetGroup);
        targetMembership.setProvisioningEntity(targetEntity);
    
        results.add(targetMembership);
      }
  
      return new TargetDaoRetrieveMembershipsByGroupResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveMembershipsByGroup", startNanos));
    }
  }
  
  @Override
  public TargetDaoInsertMembershipResponse insertMembership(TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoInsertMembershipRequest.getTargetMembership();

    try {
      ProvisioningGroup targetGroup = targetMembership.getProvisioningGroup();
      ProvisioningEntity targetEntity = targetMembership.getProvisioningEntity();

      GrouperBoxUser grouperBoxUser = GrouperBoxCommands.retrieveBoxUser(targetEntity.getLoginId());
      GrouperBoxGroup grouperBoxGroup = GrouperBoxCommands.retrieveBoxGroups().get(targetGroup.getName());
      
      GrouperBoxCommands.assignUserToBoxGroup(grouperBoxUser, grouperBoxGroup, false);
      
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
      ProvisioningGroup targetGroup = targetMembership.getProvisioningGroup();
      ProvisioningEntity targetEntity = targetMembership.getProvisioningEntity();

      GrouperBoxUser grouperBoxUser = GrouperBoxCommands.retrieveBoxUser(targetEntity.getLoginId());
      GrouperBoxGroup grouperBoxGroup = GrouperBoxCommands.retrieveBoxGroups().get(targetGroup.getName());
      
      GrouperBoxCommands.removeUserFromBoxGroup(grouperBoxUser, grouperBoxGroup, false);
      
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
      
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMembership", startNanos));
    }
  }
  
  @Override
  public void registerGrouperProvisionerDaoCapabilities(GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsByGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertMembership(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMembership(true);
  }
}
