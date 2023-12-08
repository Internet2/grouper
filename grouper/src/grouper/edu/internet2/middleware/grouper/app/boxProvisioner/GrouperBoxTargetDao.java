package edu.internet2.middleware.grouper.app.boxProvisioner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.lang3.StringUtils;

public class GrouperBoxTargetDao extends GrouperProvisionerTargetDaoBase {
  
  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    
    long startNanos = System.nanoTime();

    try {
      
      //boolean includeAllMembershipsIfApplicable = targetDaoRetrieveAllGroupsRequest == null ? false : targetDaoRetrieveAllGroupsRequest.isIncludeAllMembershipsIfApplicable();
      
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      Set<String> attributesToRetrieve = boxConfiguration.getGroupAttributesToRetrieve();
      
      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();
      
      List<GrouperBoxGroup> grouperBoxGroups = GrouperBoxApiCommands.retrieveBoxGroups(boxConfiguration.getBoxExternalSystemConfigId(), 
          null, attributesToRetrieve);

      for (GrouperBoxGroup grouperBoxGroup : grouperBoxGroups) {
        ProvisioningGroup targetGroup = grouperBoxGroup.toProvisioningGroup();
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
      
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      Set<String> attributesToRetrieve = boxConfiguration.getEntityAttributesToRetrieve();
      
      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();
      
      List<GrouperBoxUser> grouperBoxUsers = GrouperBoxApiCommands.retrieveBoxUsers(boxConfiguration.getBoxExternalSystemConfigId(), null, attributesToRetrieve);

      for (GrouperBoxUser grouperBoxUser : grouperBoxUsers) {
        ProvisioningEntity targetEntity = grouperBoxUser.toProvisioningEntity();
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

      GrouperBoxGroup grouperBoxGroup = GrouperBoxGroup.fromProvisioningGroup(targetGroup, null);
      
      GrouperBoxGroup createdBoxGroup = GrouperBoxApiCommands.createBoxGroup(boxConfiguration.getBoxExternalSystemConfigId(), grouperBoxGroup);

      targetGroup.setId(createdBoxGroup.getId());
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
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    try {
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      GrouperBoxUser grouperBoxUser = GrouperBoxUser.fromProvisioningEntity(targetEntity, null);
      
      GrouperBoxUser createdBoxUser = GrouperBoxApiCommands.createBoxUser(boxConfiguration.getBoxExternalSystemConfigId(), grouperBoxUser);

      targetEntity.setId(createdBoxUser.getId());
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
  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest.getTargetGroup();

    try {
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      GrouperBoxGroup grouperBoxGroup = GrouperBoxGroup.fromProvisioningGroup(targetGroup, null);
      
      GrouperBoxApiCommands.deleteBoxGroup(boxConfiguration.getBoxExternalSystemConfigId(), grouperBoxGroup.getId());

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
  public TargetDaoDeleteEntityResponse deleteEntity(TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();

    try {
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      GrouperBoxUser grouperBoxUser = GrouperBoxUser.fromProvisioningEntity(targetEntity, null);
      
      GrouperBoxApiCommands.deleteBoxUser(boxConfiguration.getBoxExternalSystemConfigId(), grouperBoxUser.getId());

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
  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();

    try {
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      // lets make sure we are doing the right thing
      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToUpdate.add(fieldName);
      }
      
      GrouperBoxUser grouperBoxUser = GrouperBoxUser.fromProvisioningEntity(targetEntity, null);
      GrouperBoxApiCommands.updateBoxUser(boxConfiguration.getBoxExternalSystemConfigId(), grouperBoxUser, fieldNamesToUpdate);

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
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      // lets make sure we are doing the right thing
      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();
        fieldNamesToUpdate.add(fieldName);
      }
      
      GrouperBoxGroup grouperBoxGroup = GrouperBoxGroup.fromProvisioningGroup(targetGroup, null);
      GrouperBoxApiCommands.updateBoxGroup(boxConfiguration.getBoxExternalSystemConfigId(), grouperBoxGroup, fieldNamesToUpdate);

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
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      Map<String, String> memberIdToMembershipId = GrouperBoxApiCommands.retrieveBoxGroupMembers(boxConfiguration.getBoxExternalSystemConfigId(), targetGroup.getId());
      
      for (String userId : memberIdToMembershipId.keySet()) {
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
  public TargetDaoInsertMembershipResponse insertMembership(TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoInsertMembershipRequest.getTargetMembership();

    try {
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      GrouperBoxApiCommands.createBoxMembership(boxConfiguration.getBoxExternalSystemConfigId(),
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
  public TargetDaoDeleteMembershipResponse deleteMembership(TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {
    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoDeleteMembershipRequest.getTargetMembership();

    try {
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      String membershipId = targetMembership.getId();
      if (StringUtils.isNotBlank(membershipId)) {
        GrouperBoxApiCommands.deleteBoxMembership(boxConfiguration.getBoxExternalSystemConfigId(), 
            targetMembership.getId()); 
      } else {
        String groupId = targetMembership.getProvisioningGroupId();
        String entityId = targetMembership.getProvisioningEntityId();
        
        Map<String, String> memberIdToMembershipId = GrouperBoxApiCommands.retrieveBoxGroupMembers(boxConfiguration.getBoxExternalSystemConfigId(), groupId);
        
        if (memberIdToMembershipId.containsKey(entityId)) {
          String membershipIdToDelete = memberIdToMembershipId.get(entityId);
          GrouperBoxApiCommands.deleteBoxMembership(boxConfiguration.getBoxExternalSystemConfigId(), membershipIdToDelete); 
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
      Set<String> attributesToRetrieve = boxConfiguration.getGroupAttributesToRetrieve();
      
      if (StringUtils.equals("id", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        GrouperBoxGroup boxGroup = GrouperBoxApiCommands.retrieveBoxGroup(boxConfiguration.getBoxExternalSystemConfigId(),
            GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()), attributesToRetrieve);
        ProvisioningGroup targetGroup = boxGroup == null ? null : boxGroup.toProvisioningGroup();
        return new TargetDaoRetrieveGroupResponse(targetGroup);
      } else if (StringUtils.equals("name", targetDaoRetrieveGroupRequest.getSearchAttribute())) {
        List<GrouperBoxGroup> boxGroups = GrouperBoxApiCommands.retrieveBoxGroups(boxConfiguration.getBoxExternalSystemConfigId(), 
            GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()), attributesToRetrieve);
        for (GrouperBoxGroup boxGroup: boxGroups) {
          if (StringUtils.equals(boxGroup.getName(), GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue()))) {
            ProvisioningGroup targetGroup = boxGroup == null ? null : boxGroup.toProvisioningGroup();
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
  public TargetDaoRetrieveEntityResponse retrieveEntity(TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {
    
    long startNanos = System.nanoTime();

    try {      
      GrouperBoxConfiguration boxConfiguration = (GrouperBoxConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      Set<String> attributesToRetrieve = boxConfiguration.getEntityAttributesToRetrieve();
      // we can retrieve by id or login

      GrouperBoxUser grouperBoxUser = null;

      if (StringUtils.equals("id", targetDaoRetrieveEntityRequest.getSearchAttribute())) {
        grouperBoxUser = GrouperBoxApiCommands.retrieveBoxUser(boxConfiguration.getBoxExternalSystemConfigId(), 
            GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue()), attributesToRetrieve);
      } else if (StringUtils.equals("login", targetDaoRetrieveEntityRequest.getSearchAttribute())) {
        List<GrouperBoxUser> boxUsers = GrouperBoxApiCommands.retrieveBoxUsers(boxConfiguration.getBoxExternalSystemConfigId(), 
            GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue()), attributesToRetrieve);
        for (GrouperBoxUser boxUser: boxUsers) {
          if (StringUtils.equals(boxUser.getLogin(), GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue()))) {
            grouperBoxUser = boxUser;
            break;
          }
        }
      } else if (StringUtils.equals("name", targetDaoRetrieveEntityRequest.getSearchAttribute())) {
        List<GrouperBoxUser> boxUsers = GrouperBoxApiCommands.retrieveBoxUsers(boxConfiguration.getBoxExternalSystemConfigId(), 
              GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue()), attributesToRetrieve);
        for (GrouperBoxUser boxUser: boxUsers) {
          if (StringUtils.equals(boxUser.getName(), GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue()))) {
            grouperBoxUser = boxUser;
            break;
          }
        }
      }  else {
        throw new RuntimeException("Not expecting search attribute '" + targetDaoRetrieveEntityRequest.getSearchAttribute() + "'");
      }
      
      ProvisioningEntity targetEntity = grouperBoxUser == null ? null
          : grouperBoxUser.toProvisioningEntity();

      TargetDaoRetrieveEntityResponse targetDaoRetrieveEntityResponse = new TargetDaoRetrieveEntityResponse(targetEntity);
      if (targetDaoRetrieveEntityRequest.isIncludeNativeEntity()) {
        targetDaoRetrieveEntityResponse.setTargetNativeEntity(grouperBoxUser);
      }
      return targetDaoRetrieveEntityResponse;
      
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }
  
  @Override
  public void registerGrouperProvisionerDaoCapabilities(GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {

    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsAllByGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMembership(true);
    
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);
    grouperProvisionerDaoCapabilities.setCanInsertMembership(true);
    
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    
  }

}
