package edu.internet2.middleware.grouper.app.messagingProvisioning;

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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateMembershipRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateMembershipResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperMessagingTargetDao extends GrouperProvisionerTargetDaoBase {

  @Override
  public void registerGrouperProvisionerDaoCapabilities(GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMembership(true);
    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertMembership(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    
  }
  
  @Override
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();
    
    Long indexToBeAdded = GrouperUtil.defaultIfNull(this.getGrouperProvisioner().getGcGrouperSyncJob().getLastSyncIndex(), -1L) + 1;
    this.getGrouperProvisioner().getGcGrouperSyncJob().setLastSyncIndex(indexToBeAdded);
    
    try {
      GrouperMessagingConfiguration messagingConfiguration = (GrouperMessagingConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      GrouperMessagingEntity grouperMessagingEntity = GrouperMessagingEntity.fromProvisioningEntity(targetEntity);
      
      GrouperMessagingApiCommands.sendInsertEntityMesssage(messagingConfiguration, grouperMessagingEntity, indexToBeAdded);
      
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
  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();
    
    Long indexToBeAdded = GrouperUtil.defaultIfNull(this.getGrouperProvisioner().getGcGrouperSyncJob().getLastSyncIndex(), -1L) + 1;
    this.getGrouperProvisioner().getGcGrouperSyncJob().setLastSyncIndex(indexToBeAdded);

    try {
      GrouperMessagingConfiguration messagingConfiguration = (GrouperMessagingConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      GrouperMessagingEntity grouperMessagingEntity = GrouperMessagingEntity.fromProvisioningEntity(targetEntity);
      
      GrouperMessagingApiCommands.sendUpdateEntityMesssage(messagingConfiguration, grouperMessagingEntity, indexToBeAdded);

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
  public TargetDaoDeleteEntityResponse deleteEntity(TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();
    
    Long indexToBeAdded = GrouperUtil.defaultIfNull(this.getGrouperProvisioner().getGcGrouperSyncJob().getLastSyncIndex(), -1L) + 1;
    this.getGrouperProvisioner().getGcGrouperSyncJob().setLastSyncIndex(indexToBeAdded);
    
    try {
      GrouperMessagingConfiguration messagingConfiguration = (GrouperMessagingConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      GrouperMessagingEntity grouperMessagingEntity = GrouperMessagingEntity.fromProvisioningEntity(targetEntity);
      
      GrouperMessagingApiCommands.sendDeleteEntityMesssage(messagingConfiguration, grouperMessagingEntity, indexToBeAdded);

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
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();
    
    Long indexToBeAdded = GrouperUtil.defaultIfNull(this.getGrouperProvisioner().getGcGrouperSyncJob().getLastSyncIndex(), -1L) + 1;
    this.getGrouperProvisioner().getGcGrouperSyncJob().setLastSyncIndex(indexToBeAdded);

    try {
      GrouperMessagingConfiguration messagingConfiguration = (GrouperMessagingConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      GrouperMessagingGroup grouperMessagingGroup = GrouperMessagingGroup.fromProvisioningGroup(targetGroup);
      
      GrouperMessagingApiCommands.sendInsertGroupMesssage(messagingConfiguration, grouperMessagingGroup, indexToBeAdded);
      
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
    
    Long indexToBeAdded = GrouperUtil.defaultIfNull(this.getGrouperProvisioner().getGcGrouperSyncJob().getLastSyncIndex(), -1L) + 1;
    this.getGrouperProvisioner().getGcGrouperSyncJob().setLastSyncIndex(indexToBeAdded);

    try {
      GrouperMessagingConfiguration messagingConfiguration = (GrouperMessagingConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      GrouperMessagingGroup grouperMessagingGroup = GrouperMessagingGroup.fromProvisioningGroup(targetGroup);
      
      GrouperMessagingApiCommands.sendDeleteGroupMesssage(messagingConfiguration, grouperMessagingGroup, indexToBeAdded);

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
  public TargetDaoUpdateGroupResponse updateGroup(TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();
    
    Long indexToBeAdded = GrouperUtil.defaultIfNull(this.getGrouperProvisioner().getGcGrouperSyncJob().getLastSyncIndex(), -1L) + 1;
    this.getGrouperProvisioner().getGcGrouperSyncJob().setLastSyncIndex(indexToBeAdded);

    try {
      
      GrouperMessagingConfiguration messagingConfiguration = (GrouperMessagingConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      GrouperMessagingGroup grouperMessagingGroup = GrouperMessagingGroup.fromProvisioningGroup(targetGroup);
      
      GrouperMessagingApiCommands.sendUpdateGroupMesssage(messagingConfiguration, 
          grouperMessagingGroup, indexToBeAdded);

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
  public TargetDaoInsertMembershipResponse insertMembership(TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoInsertMembershipRequest.getTargetMembership();
    
    Long indexToBeAdded = GrouperUtil.defaultIfNull(this.getGrouperProvisioner().getGcGrouperSyncJob().getLastSyncIndex(), -1L) + 1;
    this.getGrouperProvisioner().getGcGrouperSyncJob().setLastSyncIndex(indexToBeAdded);

    try {
      GrouperMessagingConfiguration messagingConfiguration = (GrouperMessagingConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      GrouperMessagingMembership grouperMessagingMembership = GrouperMessagingMembership.fromProvisioningMembership(targetMembership);
      
      GrouperMessagingApiCommands.sendInsertMembershipMesssage(messagingConfiguration, grouperMessagingMembership, indexToBeAdded);
      
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
    
    Long indexToBeAdded = GrouperUtil.defaultIfNull(this.getGrouperProvisioner().getGcGrouperSyncJob().getLastSyncIndex(), -1L) + 1;
    this.getGrouperProvisioner().getGcGrouperSyncJob().setLastSyncIndex(indexToBeAdded);

    try {
      GrouperMessagingConfiguration messagingConfiguration = (GrouperMessagingConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      GrouperMessagingMembership grouperMessagingMembership = GrouperMessagingMembership.fromProvisioningMembership(targetMembership);
      
      GrouperMessagingApiCommands.sendDeleteMembershipMesssage(messagingConfiguration, grouperMessagingMembership, indexToBeAdded);

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
  public TargetDaoUpdateMembershipResponse updateMembership(TargetDaoUpdateMembershipRequest targetDaoUpdateMembershipRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoUpdateMembershipRequest.getTargetMembership();
    
    Long indexToBeAdded = GrouperUtil.defaultIfNull(this.getGrouperProvisioner().getGcGrouperSyncJob().getLastSyncIndex(), -1L) + 1;
    this.getGrouperProvisioner().getGcGrouperSyncJob().setLastSyncIndex(indexToBeAdded);

    try {
      
      GrouperMessagingConfiguration messagingConfiguration = (GrouperMessagingConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      GrouperMessagingMembership grouperMessagingGroup = GrouperMessagingMembership.fromProvisioningMembership(targetMembership);
      
      // each property change is one unique message
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        String fieldName = provisioningObjectChange.getAttributeName();

        GrouperMessagingApiCommands.sendUpdateMembershipMesssage(messagingConfiguration, 
            grouperMessagingGroup, indexToBeAdded, fieldName, provisioningObjectChange.getOldValue(), provisioningObjectChange.getNewValue());
        
      }

      targetMembership.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoUpdateMembershipResponse();
    } catch (Exception e) {
      targetMembership.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetMembership.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateMembership", startNanos));
    }
    
  }
  

}
