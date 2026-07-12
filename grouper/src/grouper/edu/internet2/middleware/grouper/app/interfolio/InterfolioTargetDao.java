package edu.internet2.middleware.grouper.app.interfolio;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
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

/**
 * Target DAO for the Interfolio provisioner.  Entity-only (no groups, no memberships).  It retrieves,
 * creates, and updates Interfolio users.
 *
 * On create it also grants product access: it subscribes the new user to RPT (always) and to FS
 * (faculty search) if the provisioner's enableFs config is true.  On delete it removes that access
 * (unsubscribes from RPT, and FS if enableFs) - Interfolio has no hard delete of the user account, so
 * "delete" means remove product access, not remove the person.
 */
public class InterfolioTargetDao extends GrouperProvisionerTargetDaoBase {

  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
  }

  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    // entity-only capabilities; "delete" is a deprovision (unsubscribe), not a hard delete
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);
    // no groups, no memberships
  }

  /**
   * @return the runtime provisioning configuration
   */
  private InterfolioProvisioningConfiguration configuration() {
    return (InterfolioProvisioningConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
  }

  /**
   * @return the configured Interfolio external system config id
   */
  private String configId() {
    return configuration().getInterfolioExternalSystemConfigId();
  }

  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {

    long startNanos = System.nanoTime();

    try {
      String configId = configId();

      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

      // Retrieve the whole roster in one call via csv_report, which returns the UID
      // (institution_user_id) - the stable key we match on.  byc users/search omits the UID, so it
      // cannot be used for the bulk match.  csv_report does not return the pid; it is resolved lazily
      // at write time (see updateEntity/deleteEntity).
      List<InterfolioUser> users = GrouperInterfolioApiCommands.retrieveAllUsersViaCsvReport(configId);
      for (InterfolioUser user : GrouperUtil.nonNull(users)) {
        results.add(user.toProvisioningEntity());
      }

      return new TargetDaoRetrieveAllEntitiesResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }
  }

  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {

    long startNanos = System.nanoTime();

    try {
      String configId = configId();

      String searchValue = GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue());

      // byc users/search returns pid + name + email (it does not return institution_user_id/saml).
      // We match the requested attribute value against the result's email or pid.
      String searchAttribute = targetDaoRetrieveEntityRequest.getSearchAttribute();

      InterfolioUser match = null;
      if (StringUtils.isNotBlank(searchValue)) {
        List<InterfolioUser> users = GrouperInterfolioApiCommands.searchUsers(configId, searchValue, 25, 1);

        if (StringUtils.equals("institution_user_id", searchAttribute)) {
          // matching on the UID: byc search does not return institution_user_id, but we searched BY
          // it, so a single hit is the user - stamp the UID we searched on so the entity matches.
          if (GrouperUtil.length(users) == 1) {
            match = users.get(0);
            match.setInstitutionUserId(searchValue);
          }
        } else {
          for (InterfolioUser user : GrouperUtil.nonNull(users)) {
            if (StringUtils.equals("id", searchAttribute) && StringUtils.equals(searchValue, user.getPid())) {
              match = user;
              break;
            }
            if (StringUtils.equalsIgnoreCase(searchValue, user.getEmail())) {
              match = user;
              break;
            }
          }
        }
      }

      ProvisioningEntity targetEntity = match == null ? null : match.toProvisioningEntity();

      TargetDaoRetrieveEntityResponse response = new TargetDaoRetrieveEntityResponse(targetEntity);
      if (targetDaoRetrieveEntityRequest.isIncludeNativeEntity()) {
        response.setTargetNativeEntity(match);
      }
      return response;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }

  @Override
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    try {
      InterfolioProvisioningConfiguration configuration = configuration();
      String configId = configuration.getInterfolioExternalSystemConfigId();

      InterfolioUser user = InterfolioUser.fromProvisioningEntity(targetEntity);

      InterfolioUser createdUser = GrouperInterfolioApiCommands.createUser(configId,
          user.getInstitutionUserId(), user.getSamlId(), user.getUserType(),
          user.getFirstName(), user.getLastName(), user.getEmail());

      String pid = createdUser.getPid();
      targetEntity.setId(pid);

      // grant product access: RPT always, FS if enabled
      GrouperInterfolioApiCommands.subscribeUserToRpt(configId, pid);
      if (configuration.isEnableFs()) {
        GrouperInterfolioApiCommands.subscribeUserToFs(configId, pid);
      }

      targetEntity.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoInsertEntityResponse();
    } catch (RuntimeException e) {
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

    try {
      String configId = configId();

      // the IAM update is a full replace; institution_user_id must be unchanged (it is immutable)
      InterfolioUser user = InterfolioUser.fromProvisioningEntity(targetEntity);

      // csv_report (the bulk retrieve) does not return the pid, so a user matched on
      // institution_user_id may not have one yet - resolve it via byc search before the IAM PUT.
      String pid = user.getPid();
      if (StringUtils.isBlank(pid)) {
        pid = GrouperInterfolioApiCommands.resolvePid(configId, user.getInstitutionUserId(), user.getEmail());
      }
      if (StringUtils.isBlank(pid)) {
        throw new RuntimeException("Cannot resolve Interfolio pid for institution_user_id '"
            + user.getInstitutionUserId() + "'");
      }
      targetEntity.setId(pid);

      GrouperInterfolioApiCommands.updateUser(configId, pid,
          user.getInstitutionUserId(), user.getSamlId(), user.getUserType(),
          user.getFirstName(), user.getLastName(), user.getEmail());

      targetEntity.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoUpdateEntityResponse();
    } catch (RuntimeException e) {
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

    try {
      InterfolioProvisioningConfiguration configuration = configuration();
      String configId = configuration.getInterfolioExternalSystemConfigId();

      // Interfolio has no hard delete - deprovisioning means removing product access.  Remove RPT
      // always, and FS if enabled.  The user account itself remains in Interfolio.
      String pid = targetEntity.getId();
      if (StringUtils.isBlank(pid)) {
        // csv_report does not return the pid; resolve it from the UID for the deprovision
        InterfolioUser user = InterfolioUser.fromProvisioningEntity(targetEntity);
        pid = GrouperInterfolioApiCommands.resolvePid(configId, user.getInstitutionUserId(), user.getEmail());
      }
      if (StringUtils.isNotBlank(pid)) {
        GrouperInterfolioApiCommands.unsubscribeUserFromRpt(configId, pid);
        if (configuration.isEnableFs()) {
          GrouperInterfolioApiCommands.unsubscribeUserFromFs(configId, pid);
        }
      }

      targetEntity.setProvisioned(true);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoDeleteEntityResponse();
    } catch (RuntimeException e) {
      targetEntity.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteEntity", startNanos));
    }
  }

}
