package edu.internet2.middleware.grouper.app.duo.role;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

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

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDuoRoleTargetDao.class);

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
      try {
        GrouperDuoRoleApiCommands.updateDuoAdministrator(duoConfiguration.getDuoExternalSystemConfigId(), grouperDuoUser, fieldNamesToUpdate);
      } catch (RuntimeException re) {
        failClosedToReadOnly(duoConfiguration.getDuoExternalSystemConfigId(), grouperDuoUser, fieldNamesToUpdate, re);
        // still an error on this admin so it is visible and retried
        throw re;
      }

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
  
  /**
   * Fail closed: if a role change was rejected (e.g. a custom role name could not be resolved because
   * GET /admin/v1/admin_roles failed, so Duo got an invalid role_id), the administrator would otherwise
   * keep their previous, possibly higher, role.  Set them to read_only instead.  If that fails too, the
   * original error is still thrown by the caller.
   * @param duoExternalSystemConfigId
   * @param grouperDuoUser
   * @param fieldNamesToUpdate
   * @param originalException
   */
  private void failClosedToReadOnly(String duoExternalSystemConfigId, GrouperDuoRoleUser grouperDuoUser, 
      Set<String> fieldNamesToUpdate, RuntimeException originalException) {
    
    if (fieldNamesToUpdate != null && !fieldNamesToUpdate.contains("role")) {
      return;
    }
    if (StringUtils.isBlank(grouperDuoUser.getId()) || StringUtils.equals("read_only", grouperDuoUser.getRole())) {
      return;
    }
    String rejectedRole = grouperDuoUser.getRole();
    try {
      GrouperDuoRoleUser readOnlyDuoUser = new GrouperDuoRoleUser();
      readOnlyDuoUser.setId(grouperDuoUser.getId());
      readOnlyDuoUser.setRole("read_only");
      GrouperDuoRoleApiCommands.updateDuoAdministrator(duoExternalSystemConfigId, readOnlyDuoUser, GrouperUtil.toSet("role"));
      LOG.error("Duo admin '" + grouperDuoUser.getEmail() + "' (" + grouperDuoUser.getId() + ") role update to '" 
          + rejectedRole + "' failed, set to read_only (fail closed)", originalException);
    } catch (RuntimeException re2) {
      LOG.error("Duo admin '" + grouperDuoUser.getEmail() + "' (" + grouperDuoUser.getId() + ") role update to '" 
          + rejectedRole + "' failed, and setting read_only also failed", re2);
    }
  }

  @Override
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {
    long startNanos = System.nanoTime();
    ProvisioningEntity grouperTargetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    try {
      
      GrouperDuoRoleConfiguration duoConfiguration = (GrouperDuoRoleConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
      
      GrouperDuoRoleUser grouperDuoUser = GrouperDuoRoleUser.fromProvisioningEntity(grouperTargetEntity, null);
      
      String memberId = grouperTargetEntity.getProvisioningEntityWrapper().getGrouperProvisioningEntity().getId();

      // role ids or names, resolved to the highest priority role id
      Set<String> possibleRoles = (Set<String>)(Object)grouperTargetEntity.retrieveAttributeValueSet("role");
      if (GrouperUtil.length(possibleRoles) == 0) {
        possibleRoles = GrouperUtil.toSet("read_only");
      }
      String roleIdToBeSet = ((GrouperDuoRoleProvisioner)this.getGrouperProvisioner()).retrieveDuoRoleResolver().pickHighestPriorityRoleId(possibleRoles);
      GrouperDuoRoleUser createdDuoUser = GrouperDuoRoleApiCommands.createDuoAdministrator(
          duoConfiguration.getDuoExternalSystemConfigId(), grouperDuoUser, roleIdToBeSet);

      grouperTargetEntity.setId(createdDuoUser.getId());
      grouperTargetEntity.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(grouperTargetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }
      
      return new TargetDaoInsertEntityResponse();
    } catch (Exception e) {
      grouperTargetEntity.setProvisioned(false);
      grouperTargetEntity.setException(e);

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
      targetEntity.setException(e);

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
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsWithEntity(true);
  }


}
