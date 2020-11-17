package edu.internet2.middleware.grouperBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxGroup;
import com.box.sdk.BoxGroupMembership;
import com.box.sdk.BoxUser;
import com.box.sdk.BoxGroupMembership.Role;
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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class GrouperBoxTargetDao extends GrouperProvisionerTargetDaoBase {

  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    
    long startNanos = System.nanoTime();

    try {
      //boolean includeAllMembershipsIfApplicable = targetDaoRetrieveAllGroupsRequest == null ? false : targetDaoRetrieveAllGroupsRequest.isIncludeAllMembershipsIfApplicable();
      
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

      BoxAPIConnection boxAPIConnection = BoxGrouperExternalSystem.retrieveBoxApiConnection(boxConfiguration.getBoxExternalSystemConfigId());
      Iterable<BoxGroup.Info> groups = BoxGroup.getAllGroups(boxAPIConnection);
      
      for (BoxGroup.Info boxGroupInfo : groups) {
        ProvisioningGroup targetGroup = new ProvisioningGroup();
        targetGroup.setName(boxGroupInfo.getName());
        targetGroup.setId(boxGroupInfo.getID());
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
      
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

      BoxAPIConnection boxAPIConnection = BoxGrouperExternalSystem.retrieveBoxApiConnection(boxConfiguration.getBoxExternalSystemConfigId());
      Iterable<BoxUser.Info> users = BoxUser.getAllEnterpriseUsers(boxAPIConnection);

      for (BoxUser.Info boxUserInfo : users) {
        ProvisioningEntity targetEntity = new ProvisioningEntity();
        targetEntity.setId(boxUserInfo.getID());
        targetEntity.setLoginId(boxUserInfo.getLogin());
                
        Status boxStatus = boxUserInfo.getStatus();
        if (boxStatus != null) {
          String statusString = boxStatus.name();
          targetEntity.addAttributeValue("boxStatus", statusString);
        }
        
        targetEntity.addAttributeValue("isBoxSyncEnabled", boxUserInfo.getIsSyncEnabled());
                
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
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      BoxAPIConnection boxAPIConnection = BoxGrouperExternalSystem.retrieveBoxApiConnection(boxConfiguration.getBoxExternalSystemConfigId());
      BoxGroup.createGroup(boxAPIConnection, targetGroup.getName(), null, null, null, boxConfiguration.getInvitabilityLevel(), boxConfiguration.getMemberViewabilityLevel());
      
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
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      BoxAPIConnection boxAPIConnection = BoxGrouperExternalSystem.retrieveBoxApiConnection(boxConfiguration.getBoxExternalSystemConfigId());
      
      BoxGroup boxGroup = getBoxGroup(boxAPIConnection, targetGroup);
      
      if (boxGroup != null) {
        try {
          boxGroup.delete();        
        } catch (BoxAPIException boxAPIException) {
          if (boxAPIException.getResponseCode() == 404) {
            // ok, didn't exist
          } else {
            throw boxAPIException;
          }
        }
      }
      
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
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      BoxAPIConnection boxAPIConnection = BoxGrouperExternalSystem.retrieveBoxApiConnection(boxConfiguration.getBoxExternalSystemConfigId());

      String newStatusString = targetEntity.retrieveAttributeValueString("boxStatus");
      Status newStatus = GrouperClientUtils.isBlank(newStatusString) ? null : Status.valueOf(newStatusString.toUpperCase());
      boolean newIsBoxSyncEnabled = targetEntity.retrieveAttributeValueBoolean("isBoxSyncEnabled");
      
      BoxUser boxUser = getBoxUser(boxAPIConnection, targetEntity);
      BoxUser.Info boxUserInfo = boxUser.getInfo();
      
      boxUserInfo.setStatus(newStatus);
      boxUserInfo.setIsSyncEnabled(newIsBoxSyncEnabled);

      boxUser.updateInfo(boxUserInfo);
      
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
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      BoxAPIConnection boxAPIConnection = BoxGrouperExternalSystem.retrieveBoxApiConnection(boxConfiguration.getBoxExternalSystemConfigId());

      List<ProvisioningMembership> results = new ArrayList<ProvisioningMembership>();
      
      BoxGroup boxGroup = getBoxGroup(boxAPIConnection, targetGroup);
      if (boxGroup == null) {
        return new TargetDaoRetrieveMembershipsByGroupResponse(results);
      }
      
      Collection<BoxGroupMembership.Info> boxGroupMemberships = boxGroup.getMemberships();

      for (BoxGroupMembership.Info boxGroupMembership : boxGroupMemberships) {
        String loginId = boxGroupMembership.getUser().getLogin();
        ProvisioningEntity targetEntity = new ProvisioningEntity();
        targetEntity.setLoginId(loginId);
        
        ProvisioningMembership targetMembership = new ProvisioningMembership();
        targetMembership.setId(boxGroupMembership.getID());
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
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      BoxAPIConnection boxAPIConnection = BoxGrouperExternalSystem.retrieveBoxApiConnection(boxConfiguration.getBoxExternalSystemConfigId());

      ProvisioningGroup targetGroup = targetMembership.getProvisioningGroup();
      ProvisioningEntity targetEntity = targetMembership.getProvisioningEntity();
      
      BoxGroup boxGroup = getBoxGroup(boxAPIConnection, targetGroup);
      BoxUser boxUser = getBoxUser(boxAPIConnection, targetEntity);
      
      if (boxGroup == null) {
        throw new RuntimeException("Cannot find box group: " + targetGroup.getName());
      }
      
      if (boxUser == null) {
        throw new RuntimeException("Cannot find box user: " + targetEntity.getName());
      }
      
      try {
        boxGroup.addMembership(boxUser);
      } catch (BoxAPIException e) {
        //already exists
        if (e.getResponseCode() == 409) {
          // ok
        } else {
          throw e;
        }
      }
      
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
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      BoxAPIConnection boxAPIConnection = BoxGrouperExternalSystem.retrieveBoxApiConnection(boxConfiguration.getBoxExternalSystemConfigId());

      if (!StringUtils.isEmpty(targetMembership.getId())) {
        BoxGroupMembership membership = new BoxGroupMembership(boxAPIConnection, targetMembership.getId());
        try {
          membership.delete();        
        } catch (BoxAPIException e) {
          if (e.getResponseCode() == 404) {
            // ok, didn't exist
          } else {
            throw e;
          }
        }
      } else {
      
        ProvisioningGroup targetGroup = targetMembership.getProvisioningGroup();
        ProvisioningEntity targetEntity = targetMembership.getProvisioningEntity();
        
        BoxGroup boxGroup = getBoxGroup(boxAPIConnection, targetGroup);
        BoxUser boxUser = getBoxUser(boxAPIConnection, targetEntity);

        if (boxGroup == null) {
          throw new RuntimeException("Cannot find box group: " + targetGroup.getName());
        }
        
        if (boxUser == null) {
          throw new RuntimeException("Cannot find box user: " + targetEntity.getName());
        }
        
        BoxUser.Info boxUserInfo = boxUser.getInfo();
        
        try {
          for (BoxGroupMembership.Info boxGroupMembershipInfo : boxGroup.getMemberships()) {
            if (boxGroupMembershipInfo.getRole() == Role.MEMBER && GrouperClientUtils.equals(boxUserInfo.getLogin(), boxGroupMembershipInfo.getUser().getLogin())) {
              boxGroupMembershipInfo.getResource().delete();
            }                  
          }        
        } catch (BoxAPIException e) {
          //didnt exist
          if (e.getResponseCode() == 404) {
            // ok
          } else {
            throw e;
          }
        }
      }
      
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
  public TargetDaoRetrieveGroupResponse retrieveGroup(TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {
    
    long startNanos = System.nanoTime();

    try {      
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      BoxAPIConnection boxAPIConnection = BoxGrouperExternalSystem.retrieveBoxApiConnection(boxConfiguration.getBoxExternalSystemConfigId());
      BoxGroup boxGroup = getBoxGroup(boxAPIConnection, targetDaoRetrieveGroupRequest.getTargetGroup());
      BoxGroup.Info boxGroupInfo = boxGroup.getInfo();
      
      ProvisioningGroup targetGroup = new ProvisioningGroup();
      targetGroup.setName(boxGroupInfo.getName());
      targetGroup.setId(boxGroupInfo.getID());
  
      return new TargetDaoRetrieveGroupResponse(targetGroup);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
    }
  }
  
  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {
    
    long startNanos = System.nanoTime();

    try {      
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      BoxAPIConnection boxAPIConnection = BoxGrouperExternalSystem.retrieveBoxApiConnection(boxConfiguration.getBoxExternalSystemConfigId());
      BoxUser boxUser = getBoxUser(boxAPIConnection, targetDaoRetrieveEntityRequest.getTargetEntity());
      BoxUser.Info boxUserInfo = boxUser.getInfo();
      
      ProvisioningEntity targetEntity = new ProvisioningEntity();
      targetEntity.setId(boxUserInfo.getID());
      targetEntity.setLoginId(boxUserInfo.getLogin());
              
      Status boxStatus = boxUserInfo.getStatus();
      if (boxStatus != null) {
        String statusString = boxStatus.name();
        targetEntity.addAttributeValue("boxStatus", statusString);
      }
      
      targetEntity.addAttributeValue("isBoxSyncEnabled", boxUserInfo.getIsSyncEnabled());
  
      return new TargetDaoRetrieveEntityResponse(targetEntity);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }
  
  private BoxGroup getBoxGroup(BoxAPIConnection boxAPIConnection, ProvisioningGroup targetGroup) {
    BoxGroup boxGroup = null;
    
    if (!StringUtils.isEmpty(targetGroup.getId())) {
      boxGroup = new BoxGroup(boxAPIConnection, targetGroup.getId());
    } else {
      // find using the group name
      Iterable<BoxGroup.Info> groupsIterator = BoxGroup.getAllGroupsByName(boxAPIConnection, targetGroup.getName());
      for (BoxGroup.Info boxGroupInfo : groupsIterator) {
        if (targetGroup.getName().equals(boxGroupInfo.getName())) {
          boxGroup = boxGroupInfo.getResource();
          break;
        }
      }
    }
    
    return boxGroup;
  }
  
  private BoxUser getBoxUser(BoxAPIConnection boxAPIConnection, ProvisioningEntity targetEntity) {
    BoxUser boxUser = null;
    
    if (!StringUtils.isEmpty(targetEntity.getId())) {
      boxUser = new BoxUser(boxAPIConnection, targetEntity.getId());
    } else {
      // find using the login id
      Iterable<BoxUser.Info> usersIterator = BoxUser.getAllEnterpriseUsers(boxAPIConnection, targetEntity.getLoginId());
      for (BoxUser.Info boxUserInfo : usersIterator) {
        if (targetEntity.getLoginId().equals(boxUserInfo.getLogin())) {
          boxUser = boxUserInfo.getResource();
          break;
        }
      }
    }
    
    return boxUser;
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
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
  }
}
