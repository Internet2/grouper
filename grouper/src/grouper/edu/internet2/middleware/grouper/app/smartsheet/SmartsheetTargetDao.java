package edu.internet2.middleware.grouper.app.smartsheet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLists;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChange;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningUpdatable;
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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupResponse;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpClientLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Smartsheet TargetDao -- manages organization users, groups, and group memberships via the
 * Smartsheet API 2.0.
 *
 * <p>Capabilities (see {@link #registerGrouperProvisionerDaoCapabilities}):</p>
 * <ul>
 *   <li>Groups: full CRUD (name, description). Like other provisioners, which groups Grouper may
 *       delete is governed by the usual deleteGroupsIfGrouperCreated / ...IfNotExistInGrouper config.
 *       Groups on the ignore list are filtered from reads and never changed.</li>
 *   <li>Users (entities): add to org, update (names, account flags), and delete. When
 *       {@code disableEntitiesInsteadOfDelete} is set (the default) a delete deactivates the user,
 *       reads filter deactivated users out, and an insert reactivates a deactivated user instead of
 *       adding a duplicate. Otherwise a delete removes the user from the org. Users on the ignore
 *       list are filtered from reads and never changed.</li>
 *   <li>Memberships: bulk add per group (by user id, email fallback), single remove by user id.</li>
 *   <li>Seat types (when configured): users are read with the planId so their seatType is known, and a
 *       seatType change is applied with the upgrade / downgrade endpoints. Users outside the org, which
 *       a planId read can include, are never deactivated or removed.</li>
 * </ul>
 */
public class SmartsheetTargetDao extends GrouperProvisionerTargetDaoBase {

  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
  }

  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }

  /**
   * native ids of users Smartsheet reports as outside the organization (isInternal=false, only known
   * from planId reads). Grouper never deactivates or removes them: they are not org users.
   */
  private Set<String> externalUserIds = new HashSet<String>();

  /**
   * remember whether a user read from Smartsheet is outside the org
   * @param user the user (may be null)
   */
  private void trackExternal(SmartsheetUser user) {
    if (user != null && user.isExternal() && !StringUtils.isBlank(user.getId())) {
      this.externalUserIds.add(user.getId());
    }
  }

  private SmartsheetProvisionerConfiguration getSmartsheetConfiguration() {
    return (SmartsheetProvisionerConfiguration) this.getGrouperProvisioner()
        .retrieveGrouperProvisioningConfiguration();
  }

  private Set<String> ignoreUserEmails(SmartsheetProvisionerConfiguration config) {
    return SmartsheetApiCommands.parseIgnoreSet(config.getSmartsheetIgnoreUserEmails());
  }

  private Set<String> ignoreGroupNames(SmartsheetProvisionerConfiguration config) {
    return SmartsheetApiCommands.parseIgnoreSet(config.getSmartsheetIgnoreGroupNames());
  }

  /**
   * True if a user read from Smartsheet should be treated as absent: ignored, or deactivated when
   * disable-instead-of-delete is on (so the framework reconciles it as gone and insertEntity
   * reactivates it).
   */
  private boolean filterOutUser(SmartsheetProvisionerConfiguration config, Set<String> ignoreEmails, SmartsheetUser user) {
    if (user == null) {
      return true;
    }
    if (SmartsheetApiCommands.isIgnored(user.getEmail(), ignoreEmails)) {
      return true;
    }
    return config.isDisableEntitiesInsteadOfDelete() && user.isDeactivated();
  }

  // ============================
  // Retrieve all data: users (entities) + groups + group memberships.
  // GET /groups has no members, so each group needs a GET /groups/{id}.
  // ============================

  @Override
  public TargetDaoRetrieveAllDataResponse retrieveAllData(
      TargetDaoRetrieveAllDataRequest targetDaoRetrieveAllDataRequest) {

    long startNanos = System.nanoTime();

    TargetDaoRetrieveAllDataResponse response = new TargetDaoRetrieveAllDataResponse();
    GrouperProvisioningLists targetData = new GrouperProvisioningLists();
    response.setTargetData(targetData);

    try {
      SmartsheetProvisionerConfiguration config = getSmartsheetConfiguration();
      String configId = config.getSmartsheetExternalSystemConfigId();
      Set<String> ignoreEmails = ignoreUserEmails(config);

      // all users; remember which ids are visible so memberships only reference known users
      List<ProvisioningEntity> provisioningEntities = new ArrayList<ProvisioningEntity>();
      Set<String> visibleUserIds = new HashSet<String>();
      List<String> declinedEmails = new ArrayList<String>();
      for (SmartsheetUser user : SmartsheetApiCommands.retrieveUsers(configId, ignoreEmails, config.planIdForReads())) {
        if (filterOutUser(config, ignoreEmails, user)) {
          continue;
        }
        trackExternal(user);
        // DECLINED users are left alone (linked, never re-invited); note them so admins can see who
        if (SmartsheetUser.STATUS_DECLINED.equalsIgnoreCase(user.getStatus())) {
          declinedEmails.add(user.getEmail());
        }
        provisioningEntities.add(user.toProvisioningEntity());
        if (!StringUtils.isBlank(user.getId())) {
          visibleUserIds.add(user.getId());
        }
      }
      targetData.setProvisioningEntities(provisioningEntities);
      if (declinedEmails.size() > 0) {
        this.getGrouperProvisioner().getDebugMap().put("smartsheetDeclinedUserCount", declinedEmails.size());
        SmartsheetLog.smartsheetLog("Smartsheet users who declined their invitation (left alone, not re-invited): "
            + GrouperUtil.toStringForLog(declinedEmails));
      }

      // all groups, then one detail call per group for its members
      List<ProvisioningGroup> provisioningGroups = new ArrayList<ProvisioningGroup>();
      List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();

      for (SmartsheetGroup group : SmartsheetApiCommands.retrieveGroups(configId, ignoreGroupNames(config))) {
        provisioningGroups.add(group.toProvisioningGroup());

        SmartsheetGroup groupWithMembers = SmartsheetApiCommands.retrieveGroup(configId, group.getId());
        if (groupWithMembers == null) {
          continue;
        }
        for (SmartsheetUser member : GrouperUtil.nonNull(groupWithMembers.getMembers())) {
          // skip ignored / deactivated / unknown (e.g. outside the org) members
          if (StringUtils.isBlank(member.getId()) || !visibleUserIds.contains(member.getId())) {
            continue;
          }
          ProvisioningMembership membership = new ProvisioningMembership(false);
          membership.setProvisioningGroupId(group.getId());
          membership.setProvisioningEntityId(member.getId());
          provisioningMemberships.add(membership);
        }
      }
      targetData.setProvisioningGroups(provisioningGroups);
      targetData.setProvisioningMemberships(provisioningMemberships);

      return response;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllData", startNanos));
    }
  }

  // ============================
  // Retrieve single entity: by id (the stable link) or by email (first link)
  // ============================

  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(
      TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {

    long startNanos = System.nanoTime();

    try {
      SmartsheetProvisionerConfiguration config = getSmartsheetConfiguration();
      String configId = config.getSmartsheetExternalSystemConfigId();

      String searchAttribute = targetDaoRetrieveEntityRequest.getSearchAttribute();
      String searchValue = GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue());

      String planId = config.planIdForReads();
      SmartsheetUser user;
      if (StringUtils.equals("id", searchAttribute)) {
        user = SmartsheetApiCommands.retrieveUserById(configId, searchValue);
        // GET /users/{id} has no seatType even for a plan; read it from the plan-scoped list by email
        if (user != null && planId != null && !StringUtils.isBlank(user.getEmail())) {
          SmartsheetUser withSeat = SmartsheetApiCommands.retrieveUserByEmail(configId, user.getEmail(), planId);
          if (withSeat != null) {
            user.setSeatType(withSeat.getSeatType());
            user.setInternal(withSeat.getInternal());
          }
        }
      } else if (StringUtils.equals("email", searchAttribute)) {
        user = SmartsheetApiCommands.retrieveUserByEmail(configId, searchValue, planId);
      } else {
        throw new RuntimeException("Not expecting entity search attribute '" + searchAttribute + "', expecting id or email");
      }

      if (filterOutUser(config, ignoreUserEmails(config), user)) {
        user = null;
      }
      trackExternal(user);

      return new TargetDaoRetrieveEntityResponse(user == null ? null : user.toProvisioningEntity());
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }

  // ============================
  // Retrieve single group: by id (the stable link) or by name (first link)
  // ============================

  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(
      TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {

    long startNanos = System.nanoTime();

    try {
      SmartsheetProvisionerConfiguration config = getSmartsheetConfiguration();
      String configId = config.getSmartsheetExternalSystemConfigId();
      Set<String> ignoreGroups = ignoreGroupNames(config);

      String searchAttribute = targetDaoRetrieveGroupRequest.getSearchAttribute();
      String searchValue = GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue());

      SmartsheetGroup group;
      if (StringUtils.equals("id", searchAttribute)) {
        group = SmartsheetApiCommands.retrieveGroup(configId, searchValue);
      } else if (StringUtils.equals("name", searchAttribute)) {
        group = SmartsheetApiCommands.retrieveGroupByName(configId, searchValue, ignoreGroups);
      } else {
        throw new RuntimeException("Not expecting group search attribute '" + searchAttribute + "', expecting id or name");
      }

      if (group != null && SmartsheetApiCommands.isIgnored(group.getName(), ignoreGroups)) {
        group = null;
      }

      return new TargetDaoRetrieveGroupResponse(group == null ? null : group.toProvisioningGroup());
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
    }
  }

  // ============================
  // Insert entity: add the user to the org (or reactivate a deactivated one)
  // ============================

  @Override
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    try {
      SmartsheetProvisionerConfiguration config = getSmartsheetConfiguration();
      String configId = config.getSmartsheetExternalSystemConfigId();

      SmartsheetUser user = SmartsheetUser.fromProvisioningEntity(targetEntity);
      if (StringUtils.isBlank(user.getEmail())) {
        throw new RuntimeException("email is required to add a Smartsheet user");
      }
      // reaching insertEntity for an ignored user means a subject on the ignore list is in a
      // provisioned group: a real conflict the admin needs to resolve, so error instead of skipping
      if (SmartsheetApiCommands.isIgnored(user.getEmail(), ignoreUserEmails(config))) {
        throw new RuntimeException("user '" + user.getEmail() + "' is on the Smartsheet ignore list and must not be "
            + "provisioned; remove it from the provisioned group or from the ignore list");
      }

      // disable-instead-of-delete: reads filter deactivated users, so the user may still exist in
      // the org. Look it up without the filter and reactivate it instead of adding a duplicate.
      SmartsheetUser existing = null;
      if (config.isDisableEntitiesInsteadOfDelete()) {
        if (!StringUtils.isBlank(user.getId())) {
          existing = SmartsheetApiCommands.retrieveUserById(configId, user.getId());
        }
        if (existing == null) {
          existing = SmartsheetApiCommands.retrieveUserByEmail(configId, user.getEmail());
        }
      }

      if (existing != null) {
        // a deactivated user must be reactivated before the seat can change
        if (existing.isDeactivated()) {
          SmartsheetApiCommands.reactivateUser(configId, existing.getId());
        }
        // refresh the managed fields in one PUT
        updateUserWithHint(configId, existing.getId(), user, managedUserFields(targetEntity));
        targetEntity.setId(existing.getId());
      } else {
        SmartsheetUser created = SmartsheetApiCommands.createUser(configId, user, config.isSmartsheetSendEmailOnAddUser());
        targetEntity.setId(created.getId());
      }

      // the org decides a new / reactivated user's seat, so read it and change it if Grouper manages it
      if (config.isManageSeatType() && !StringUtils.isBlank(user.getSeatType())) {
        applySeatType(config, configId, targetEntity.getId(), user.getEmail(), null, user.getSeatType());
      }
      markProvisioned(targetEntity, true);

      return new TargetDaoInsertEntityResponse();
    } catch (RuntimeException e) {
      markProvisioned(targetEntity, false);
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertEntity", startNanos));
    }
  }

  /**
   * The updatable user fields that are configured as entity attributes (so a reactivate only
   * refreshes what Grouper manages).
   * @param targetEntity the entity
   * @return the field names
   */
  private Set<String> managedUserFields(ProvisioningEntity targetEntity) {
    Set<String> result = new LinkedHashSet<String>();
    Set<String> configured = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration()
        .getTargetEntityAttributeNameToConfig().keySet();
    for (String fieldName : updatableUserFields()) {
      if (configured.contains(fieldName)) {
        result.add(fieldName);
      }
    }
    return result;
  }

  /**
   * @return the entity attributes PUT /users/{id} can change (email and status cannot be)
   */
  private static Set<String> updatableUserFields() {
    Set<String> result = new LinkedHashSet<String>(GrouperUtil.toSet("firstName", "lastName"));
    result.addAll(SmartsheetUser.BOOLEAN_FLAG_ATTRIBUTES);
    return result;
  }

  // ============================
  // Update entity: names and account flags (matched by id)
  // ============================

  @Override
  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();

    try {
      SmartsheetProvisionerConfiguration config = getSmartsheetConfiguration();
      String configId = config.getSmartsheetExternalSystemConfigId();

      Set<String> fieldNamesToUpdate = new LinkedHashSet<String>();
      boolean seatTypeChanged = false;
      String oldSeatType = null;
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        if (!StringUtils.isBlank(change.getAttributeName())) {
          fieldNamesToUpdate.add(change.getAttributeName());
        }
        if (StringUtils.equals(SmartsheetUser.ATTR_SEAT_TYPE, change.getAttributeName())) {
          seatTypeChanged = true;
          oldSeatType = GrouperUtil.stringValue(change.getOldValue());
        }
      }
      // seatType is not a PUT /users field, it is changed with upgrade / downgrade below
      fieldNamesToUpdate.retainAll(updatableUserFields());

      SmartsheetUser user = SmartsheetUser.fromProvisioningEntity(targetEntity);
      // never modify an ignore-listed user
      boolean ignored = SmartsheetApiCommands.isIgnored(user.getEmail(), ignoreUserEmails(config));
      if (!ignored && (!fieldNamesToUpdate.isEmpty() || seatTypeChanged)) {
        if (StringUtils.isBlank(targetEntity.getId())) {
          throw new RuntimeException("user id is required for updateEntity");
        }
        if (!fieldNamesToUpdate.isEmpty()) {
          updateUserWithHint(configId, targetEntity.getId(), user, fieldNamesToUpdate);
        }
        if (seatTypeChanged && config.isManageSeatType()) {
          applySeatType(config, configId, targetEntity.getId(), user.getEmail(), oldSeatType, user.getSeatType());
        }
      }
      markProvisioned(targetEntity, true);

      return new TargetDaoUpdateEntityResponse();
    } catch (RuntimeException e) {
      markProvisioned(targetEntity, false);
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateEntity", startNanos));
    }
  }

  // ============================
  // Delete entity: deactivate (default) or remove from the org
  // ============================

  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();

    try {
      SmartsheetProvisionerConfiguration config = getSmartsheetConfiguration();
      String configId = config.getSmartsheetExternalSystemConfigId();

      String email = targetEntity.retrieveAttributeValueString("email");
      // never deactivate/remove an ignore-listed user
      if (SmartsheetApiCommands.isIgnored(email, ignoreUserEmails(config))) {
        markProvisioned(targetEntity, true);
        return new TargetDaoDeleteEntityResponse();
      }

      String userId = targetEntity.getId();
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("user id is required for deleteEntity");
      }
      // a user outside the org (seen on a planId read) is not an org user: nothing to deactivate or remove
      if (this.externalUserIds.contains(userId)) {
        SmartsheetLog.smartsheetLog("Not deactivating / removing Smartsheet user outside the org: " + userId + ", " + email);
        markProvisioned(targetEntity, true);
        return new TargetDaoDeleteEntityResponse();
      }
      if (config.isDisableEntitiesInsteadOfDelete()) {
        SmartsheetApiCommands.deactivateUser(configId, userId);
      } else {
        SmartsheetApiCommands.removeUser(configId, userId, config.getSmartsheetRemoveUserTransferToUserId(),
            config.isSmartsheetRemoveUserFromSharing());
      }
      markProvisioned(targetEntity, true);

      return new TargetDaoDeleteEntityResponse();
    } catch (RuntimeException e) {
      markProvisioned(targetEntity, false);
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteEntity", startNanos));
    }
  }

  /**
   * PUT /users/{id}, adding a hint to the error when an account flag rule is the likely cause.
   */
  private static void updateUserWithHint(String configId, String userId, SmartsheetUser user, Set<String> fieldNamesToUpdate) {
    try {
      SmartsheetApiCommands.updateUser(configId, userId, user, fieldNamesToUpdate);
    } catch (RuntimeException re) {
      if (fieldNamesToUpdate != null && fieldNamesToUpdate.contains("licensedSheetCreator")
          && Boolean.FALSE.equals(user.getLicensedSheetCreator())) {
        throw new RuntimeException("Smartsheet does not allow licensedSheetCreator=false for a Group Admin or Resource Viewer; "
            + "if the licensed sheet creator group is configured, it should include everyone in the group admin "
            + "and resource viewer groups (user " + userId + ")", re);
      }
      throw re;
    }
  }

  /**
   * Move a user to the desired seat with the upgrade / downgrade endpoint, if it differs from the
   * current seat. A provisional member is not downgraded unless smartsheetDowngradeProvisionalMembers.
   * @param config the provisioner config
   * @param configId the external system config id
   * @param userId the native user id
   * @param email the user email (to read the current seat if it is not known)
   * @param currentSeat the current seat, or null to read it
   * @param desiredSeat the computed seat (nothing happens if blank)
   */
  private static void applySeatType(SmartsheetProvisionerConfiguration config, String configId, String userId,
      String email, String currentSeat, String desiredSeat) {

    desiredSeat = SmartsheetUser.normalizeSeatType(desiredSeat);
    if (desiredSeat == null) {
      return;
    }
    currentSeat = SmartsheetUser.normalizeSeatType(currentSeat);
    if (currentSeat == null && !StringUtils.isBlank(email)) {
      SmartsheetUser withSeat = SmartsheetApiCommands.retrieveUserByEmail(configId, email, config.getSmartsheetPlanId());
      currentSeat = withSeat == null ? null : SmartsheetUser.normalizeSeatType(withSeat.getSeatType());
    }
    if (StringUtils.equals(currentSeat, desiredSeat)) {
      return;
    }
    if (SmartsheetProvisioningCompare.isProvisionalLeftAlone(config, currentSeat, desiredSeat)) {
      return;
    }
    SmartsheetApiCommands.changeUserSeatType(configId, userId, config.getSmartsheetPlanId(), desiredSeat,
        SmartsheetApiCommands.isSeatUpgrade(currentSeat, desiredSeat));
  }

  // ============================
  // Groups
  // ============================

  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();

    try {
      SmartsheetProvisionerConfiguration config = getSmartsheetConfiguration();
      String configId = config.getSmartsheetExternalSystemConfigId();

      SmartsheetGroup group = SmartsheetGroup.fromProvisioningGroup(targetGroup);
      // an ignored group is filtered from reads, so creating it would collide on the unique name
      if (SmartsheetApiCommands.isIgnored(group.getName(), ignoreGroupNames(config))) {
        throw new RuntimeException("group '" + group.getName() + "' is on the Smartsheet ignore list and must not be "
            + "provisioned; stop provisioning the Grouper group or remove it from the ignore list");
      }

      SmartsheetGroup created = SmartsheetApiCommands.createGroup(configId, group);
      targetGroup.setId(created.getId());
      markProvisioned(targetGroup, true);

      return new TargetDaoInsertGroupResponse();
    } catch (RuntimeException e) {
      markProvisioned(targetGroup, false);
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertGroup", startNanos));
    }
  }

  @Override
  public TargetDaoUpdateGroupResponse updateGroup(TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();

    try {
      SmartsheetProvisionerConfiguration config = getSmartsheetConfiguration();
      String configId = config.getSmartsheetExternalSystemConfigId();

      Set<String> fieldNamesToUpdate = new LinkedHashSet<String>();
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        if (!StringUtils.isBlank(change.getAttributeName())) {
          fieldNamesToUpdate.add(change.getAttributeName());
        }
      }
      // only name and description are updatable (id is immutable, owner is not managed)
      fieldNamesToUpdate.retainAll(GrouperUtil.toSet("name", "description"));

      if (!fieldNamesToUpdate.isEmpty()) {
        if (StringUtils.isBlank(targetGroup.getId())) {
          throw new RuntimeException("group id is required for updateGroup");
        }
        SmartsheetGroup group = SmartsheetGroup.fromProvisioningGroup(targetGroup);
        SmartsheetApiCommands.updateGroup(configId, targetGroup.getId(), group, fieldNamesToUpdate);
      }
      markProvisioned(targetGroup, true);

      return new TargetDaoUpdateGroupResponse();
    } catch (RuntimeException e) {
      markProvisioned(targetGroup, false);
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateGroup", startNanos));
    }
  }

  @Override
  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest.getTargetGroup();

    try {
      SmartsheetProvisionerConfiguration config = getSmartsheetConfiguration();
      String configId = config.getSmartsheetExternalSystemConfigId();

      // never delete an ignore-listed group
      if (!SmartsheetApiCommands.isIgnored(targetGroup.retrieveAttributeValueString("name"), ignoreGroupNames(config))) {
        if (StringUtils.isBlank(targetGroup.getId())) {
          throw new RuntimeException("group id is required for deleteGroup");
        }
        SmartsheetApiCommands.deleteGroup(configId, targetGroup.getId());
      }
      markProvisioned(targetGroup, true);

      return new TargetDaoDeleteGroupResponse();
    } catch (RuntimeException e) {
      markProvisioned(targetGroup, false);
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteGroup", startNanos));
    }
  }

  // ============================
  // Memberships: bulk add per group, single remove
  // ============================

  @Override
  public TargetDaoInsertMembershipsResponse insertMemberships(
      TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {

    long startNanos = System.nanoTime();
    try {
      SmartsheetProvisionerConfiguration config = getSmartsheetConfiguration();
      String configId = config.getSmartsheetExternalSystemConfigId();
      Set<String> ignoreEmails = ignoreUserEmails(config);

      // group the batch by group id; the add endpoint takes an array of members per group
      Map<String, List<ProvisioningMembership>> groupIdToMemberships = new LinkedHashMap<String, List<ProvisioningMembership>>();
      for (ProvisioningMembership membership : GrouperUtil.nonNull(targetDaoInsertMembershipsRequest.getTargetMemberships())) {
        String groupId = membership.getProvisioningGroupId();
        if (StringUtils.isBlank(groupId)) {
          markProvisioned(membership, false);
          continue;
        }
        groupIdToMemberships.computeIfAbsent(groupId, k -> new ArrayList<ProvisioningMembership>()).add(membership);
      }

      RuntimeException firstException = null;
      for (String groupId : groupIdToMemberships.keySet()) {
        List<ProvisioningMembership> memberships = groupIdToMemberships.get(groupId);
        // send the native user id (what Grouper matches on), with the email as the fallback
        List<SmartsheetUser> members = new ArrayList<SmartsheetUser>();
        for (ProvisioningMembership membership : memberships) {
          String email = membershipEmail(membership);
          if (SmartsheetApiCommands.isIgnored(email, ignoreEmails)) {
            continue;
          }
          SmartsheetUser member = new SmartsheetUser();
          member.setId(membership.getProvisioningEntityId());
          member.setEmail(email);
          if (!StringUtils.isBlank(member.getId()) || !StringUtils.isBlank(member.getEmail())) {
            members.add(member);
          }
        }
        try {
          SmartsheetApiCommands.addGroupMembers(configId, groupId, members);
          for (ProvisioningMembership membership : memberships) {
            markProvisioned(membership, true);
          }
        } catch (RuntimeException e) {
          // keep going with the other groups, mark this group's batch as failed
          for (ProvisioningMembership membership : memberships) {
            markProvisioned(membership, false);
          }
          if (firstException == null) {
            firstException = e;
          }
        }
      }
      if (firstException != null) {
        throw firstException;
      }

      return new TargetDaoInsertMembershipsResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertMemberships", startNanos));
    }
  }

  @Override
  public TargetDaoDeleteMembershipResponse deleteMembership(TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {

    long startNanos = System.nanoTime();
    ProvisioningMembership targetMembership = targetDaoDeleteMembershipRequest.getTargetMembership();

    try {
      SmartsheetProvisionerConfiguration config = getSmartsheetConfiguration();
      String configId = config.getSmartsheetExternalSystemConfigId();

      // never remove an ignore-listed user from a group
      if (!SmartsheetApiCommands.isIgnored(membershipEmail(targetMembership), ignoreUserEmails(config))) {
        SmartsheetApiCommands.removeGroupMember(configId, targetMembership.getProvisioningGroupId(),
            targetMembership.getProvisioningEntityId());
      }
      markProvisioned(targetMembership, true);

      return new TargetDaoDeleteMembershipResponse();
    } catch (RuntimeException e) {
      markProvisioned(targetMembership, false);
      throw new RuntimeException("Failed to delete Smartsheet membership (groupId '" + targetMembership.getProvisioningGroupId()
          + "', userId '" + targetMembership.getProvisioningEntityId() + "')", e);
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMembership", startNanos));
    }
  }

  /**
   * The member email for a membership, from the entity's "email" attribute.
   * @param membership the provisioning membership
   * @return the email, or null
   */
  private static String membershipEmail(ProvisioningMembership membership) {
    ProvisioningEntity entity = membership == null ? null : membership.getProvisioningEntity();
    return entity == null ? null : entity.retrieveAttributeValueString("email");
  }

  /**
   * Mark a provisioning object and all its object-changes as provisioned or not.
   * @param provisioningObject the group, entity, or membership
   * @param provisioned true if the write succeeded
   */
  private static void markProvisioned(ProvisioningUpdatable provisioningObject, boolean provisioned) {
    if (provisioningObject == null) {
      return;
    }
    provisioningObject.setProvisioned(provisioned);
    for (ProvisioningObjectChange change : GrouperUtil.nonNull(provisioningObject.getInternal_objectChanges())) {
      change.setProvisioned(provisioned);
    }
  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanRetrieveAllData(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);

    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);

    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);

    // add is a bulk call per group, remove is one call per member
    grouperProvisionerDaoCapabilities.setCanInsertMemberships(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMembership(true);
  }

}
