package edu.internet2.middleware.grouper.app.duo.role;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityResponse;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDuoRoleTargetDao extends GrouperProvisionerTargetDaoBase {

  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
  }

  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }

  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(
      TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {

    long startNanos = System.nanoTime();

    try {

      GrouperDuoRoleConfiguration duoConfiguration = (GrouperDuoRoleConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

      List<GrouperDuoRoleUser> grouperDuoUsers = GrouperDuoRoleApiCommands.retrieveDuoAdministrators(duoConfiguration.getDuoExternalSystemConfigId());

      for (GrouperDuoRoleUser grouperDuoUser : grouperDuoUsers) {
        ProvisioningEntity targetEntity = grouperDuoUser.toProvisioningEntity();
        results.add(targetEntity);
      }

      return new TargetDaoRetrieveAllEntitiesResponse(results);
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
      GrouperDuoRoleConfiguration duoConfiguration = (GrouperDuoRoleConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      ProvisioningEntity grouperTargetEntity = targetDaoRetrieveEntityRequest.getTargetEntity();

      GrouperDuoRoleUser grouperDuoUser = null;

      if (!StringUtils.isBlank(grouperTargetEntity.getId())) {
        grouperDuoUser = GrouperDuoRoleApiCommands.retrieveDuoAdministrator(
            duoConfiguration.getDuoExternalSystemConfigId(), grouperTargetEntity.getId());
      }

      ProvisioningEntity targetEntity = grouperDuoUser == null ? null
          : grouperDuoUser.toProvisioningEntity();

      return new TargetDaoRetrieveEntityResponse(targetEntity);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }
  
  @Override
  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {
    
    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();

    try {
      GrouperDuoRoleConfiguration duoConfiguration = (GrouperDuoRoleConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      // lets make sure we are doing the right thing
      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        String attributeName = provisioningObjectChange.getAttributeName(); 
        fieldNamesToUpdate.add(attributeName);
      }
      
      GrouperDuoRoleUser grouperDuoUser = GrouperDuoRoleUser.fromProvisioningEntity(targetEntity, null);
      GrouperDuoRoleApiCommands.updateDuoAdministrator(duoConfiguration.getDuoExternalSystemConfigId(), grouperDuoUser, fieldNamesToUpdate);

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
  
  private String pickHighestPriorityRoleName(List<String> roleNames) {
    
    if (roleNames.contains("Owner")) {
      return "Owner";
    } else if (roleNames.contains("Administrator")) {
      return "Administrator";
    } else if (roleNames.contains("Application Manager")) {
      return "Application Manager";
    } else if (roleNames.contains("User Manager")) {
      return "User Manager";
    } else if (roleNames.contains("Help Desk")) {
      return "Help Desk";
    } else if (roleNames.contains("Billing")) {
      return "Billing";
    } else if (roleNames.contains("Phishing Manager")) {
      return "Phishing Manager";
    } else if (roleNames.contains("Read-only")) {
      return "Read-only";
    }
    
    
    throw new RuntimeException("invalid role names");
  }

  @Override
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {
    long startNanos = System.nanoTime();
    ProvisioningEntity grouperTargetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    try {
      
      GrouperDuoRoleConfiguration duoConfiguration = (GrouperDuoRoleConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      GrouperDuoRoleUser grouperDuoUser = GrouperDuoRoleUser.fromProvisioningEntity(grouperTargetEntity, null);
      
      String memberId = grouperTargetEntity.getProvisioningEntityWrapper().getGrouperProvisioningEntity().getId();
      
      //TODO only find groups where the memberId is member of and group has provisioner enabled 
      Set<Group> groups = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, false).getGroups();
      List<String> groupIds = new ArrayList<String>();
      for (Group group: groups) {
        groupIds.add(group.getId());
      }
      List<ProvisioningGroup> provisioningGroups = this.getGrouperProvisioner().retrieveGrouperDao().retrieveGroups(false, groupIds);
      
      List<String> possibleRoleNames = new ArrayList<String>();
      
      for (ProvisioningGroup provisioningGroup: provisioningGroups) {
        if (provisioningGroup.getAttributes() != null && provisioningGroup.getAttributes().containsKey("md_grouper_duoRoles")) {
          Object val = provisioningGroup.getAttributes().get("md_grouper_duoRoles").getValue();
          if (val != null) {
            possibleRoleNames.add((String)val);
          }
        }
      }
      
      if (possibleRoleNames.size() > 0) {
        String roleNameToBeSet = pickHighestPriorityRoleName(possibleRoleNames);
        GrouperDuoRoleUser createdDuoUser = GrouperDuoRoleApiCommands.createDuoAdministrator(
            duoConfiguration.getDuoExternalSystemConfigId(), grouperDuoUser, roleNameToBeSet);

        grouperTargetEntity.setId(createdDuoUser.getId());
        grouperTargetEntity.setProvisioned(true);

        for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(grouperTargetEntity.getInternal_objectChanges())) {
          provisioningObjectChange.setProvisioned(true);
        }
      }
      
      return new TargetDaoInsertEntityResponse();
    } catch (Exception e) {
      grouperTargetEntity.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(grouperTargetEntity.getInternal_objectChanges())) {
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
      GrouperDuoRoleConfiguration duoConfiguration = (GrouperDuoRoleConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      GrouperDuoRoleUser grouperDuoUser = GrouperDuoRoleUser.fromProvisioningEntity(targetEntity, null);
      
      GrouperDuoRoleApiCommands.deleteDuoAdministrator(duoConfiguration.getDuoExternalSystemConfigId(), grouperDuoUser.getId());

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
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);
    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
  }


}
