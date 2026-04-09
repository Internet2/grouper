package edu.internet2.middleware.grouper.app.truefoundry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLists;
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
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoReplaceGroupMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoReplaceGroupMembershipsResponse;
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
 * TrueFoundry TargetDao — manages users, teams, roles, and team memberships
 * via the TrueFoundry native REST API.
 *
 * Entity ID note: The provisioning entity ID is the native TrueFoundry user ID
 * (e.g. "pt3vuwlxupmefpk8i9cj11du").  The email address is stored as a separate
 * attribute and is used for API calls (register, deactivate, role assignment, team
 * membership).  The native ID is used for SCIM display name updates
 * (PATCH /scim/v2/{tenant}/{sso}/Users/{nativeId}).
 *
 * Team membership note: TrueFoundry has no individual add/remove member endpoints.
 * Team membership is managed by replacing the full member list via PUT /teams.
 * insertMembership and deleteMembership for teams both retrieve the current team
 * state, modify the member list, and PUT the full manifest back.
 *
 * Role membership note: Role assignments are read from the rolesWithResource array
 * on each user returned by the subjects endpoint.  insertMembership assigns a role
 * to a user; deleteMembership assigns the configured default role.
 */
public class TrueFoundryTargetDao extends GrouperProvisionerTargetDaoBase {

  private static final Log LOG = LogFactory.getLog(TrueFoundryTargetDao.class);

  /**
   * roleId → set of user emails that had this role in TrueFoundry at the start of this sync.
   * Populated in retrieveAllData; read by replaceGroupMemberships to detect users losing a role.
   */
  private Map<String, Set<String>> startOfSyncRoleIdToUserEmails = new LinkedHashMap<String, Set<String>>();

  /**
   * Emails of users that have been (re)assigned a role during this sync.
   * Used by the role delete/replace paths to avoid clobbering a just-assigned role with the default.
   */
  private Set<String> usersAssignedRoleThisSync = new HashSet<String>();

  @Override
  public boolean loggingStart() {
    return GrouperHttpClient.logStart(new GrouperHttpClientLog());
  }

  @Override
  public String loggingStop() {
    return GrouperHttpClient.logEnd();
  }

  /**
   * Assign a TrueFoundry role to a user and track that we touched their role in this sync.
   */
  private void assignUserRoleTracked(String configId, TrueFoundrySettings settings,
      String userEmail, String roleName) {
    TrueFoundryApiCommands.assignUserRole(configId, settings, userEmail, roleName);
    if (!StringUtils.isBlank(userEmail)) {
      this.usersAssignedRoleThisSync.add(userEmail);
    }
  }

  private TrueFoundryProvisionerConfiguration getTrueFoundryConfiguration() {
    return (TrueFoundryProvisionerConfiguration) this.getGrouperProvisioner()
        .retrieveGrouperProvisioningConfiguration();
  }

  private TrueFoundrySettings getTrueFoundrySettings() {
    TrueFoundrySettings settings = new TrueFoundrySettings();
    settings.loadFromConfiguration(getTrueFoundryConfiguration());
    return settings;
  }

  // ============================
  // Retrieve all data: users + teams + roles + team memberships in one call.
  // The subjects endpoint (GET /subjects) returns users AND all teams with their
  // members and managers embedded, plus rolesWithResource on each user for role memberships.
  // Roles are fetched separately via retrieveRoles().
  // ============================

  @Override
  public TargetDaoRetrieveAllDataResponse retrieveAllData(
      TargetDaoRetrieveAllDataRequest targetDaoRetrieveAllDataRequest) {

    TargetDaoRetrieveAllDataResponse response = new TargetDaoRetrieveAllDataResponse();
    GrouperProvisioningLists targetData = new GrouperProvisioningLists();
    response.setTargetData(targetData);

    long startNanos = System.nanoTime();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();
      TrueFoundrySettings settings = getTrueFoundrySettings();

      // one subjects call returns active users + all teams with members/managers
      TrueFoundryApiCommands.SubjectsData subjectsData =
          TrueFoundryApiCommands.retrieveSubjectsData(configId, settings);

      // cache start-of-sync role memberships so the role delete/replace paths can
      // detect users losing their role and fall back to the default role
      this.startOfSyncRoleIdToUserEmails.clear();
      this.usersAssignedRoleThisSync.clear();
      if (subjectsData.roleMembershipsByRoleId != null) {
        for (Map.Entry<String, Set<String>> entry : subjectsData.roleMembershipsByRoleId.entrySet()) {
          this.startOfSyncRoleIdToUserEmails.put(entry.getKey(),
              new LinkedHashSet<String>(GrouperUtil.nonNull(entry.getValue())));
        }
      }

      // build entities from users and build email-to-nativeId map for membership translation
      List<ProvisioningEntity> provisioningEntities = new ArrayList<ProvisioningEntity>();
      Map<String, String> emailToNativeId = new LinkedHashMap<String, String>();
      for (TrueFoundryUser user : GrouperUtil.nonNull(subjectsData.users)) {
        provisioningEntities.add(user.toProvisioningEntity());
        if (StringUtils.isNotBlank(user.getEmail()) && StringUtils.isNotBlank(user.getId())) {
          emailToNativeId.put(user.getEmail(), user.getId());
        }
      }
      targetData.setProvisioningEntities(provisioningEntities);

      // build groups: roles + teams
      List<ProvisioningGroup> provisioningGroups = new ArrayList<ProvisioningGroup>();

      List<TrueFoundryGroup> roles = TrueFoundryApiCommands.retrieveRoles(configId, settings);
      for (TrueFoundryGroup role : GrouperUtil.nonNull(roles)) {
        provisioningGroups.add(role.toProvisioningGroup());
      }

      for (TrueFoundryGroup team : GrouperUtil.nonNull(subjectsData.teams)) {
        provisioningGroups.add(team.toProvisioningGroup());
      }

      targetData.setProvisioningGroups(provisioningGroups);

      // build team memberships from manifest members/managers
      // TrueFoundry uses emails in team manifests; translate to native IDs for entity matching
      List<ProvisioningMembership> provisioningMemberships = new ArrayList<ProvisioningMembership>();

      boolean addManagerMetadata = config.isTrueFoundryAddTeamManagerMetadata();
      String managerMetadataName = config.getTrueFoundryTeamManagerMetadataName();

      for (TrueFoundryGroup team : GrouperUtil.nonNull(subjectsData.teams)) {
        String teamId = team.getId();
        Set<String> managerSet = new HashSet<String>(GrouperUtil.nonNull(team.getManagers()));

        for (String memberEmail : GrouperUtil.nonNull(team.getMembers())) {
          if (StringUtils.isBlank(memberEmail)) {
            continue;
          }
          String nativeId = emailToNativeId.get(memberEmail);
          if (StringUtils.isBlank(nativeId)) {
            continue;
          }
          ProvisioningMembership membership = new ProvisioningMembership(false);
          membership.setProvisioningGroupId(teamId);
          membership.setProvisioningEntityId(nativeId);
          if (addManagerMetadata) {
            membership.assignAttributeValue(managerMetadataName,
                String.valueOf(managerSet.contains(memberEmail)));
          }
          provisioningMemberships.add(membership);
        }
      }

      // build role memberships from rolesWithResource on each user
      // rolesWithResource uses roleId (already native) and user email (translate to native ID)
      Map<String, Set<String>> roleMembershipsByRoleId = subjectsData.roleMembershipsByRoleId;
      for (Map.Entry<String, Set<String>> entry : roleMembershipsByRoleId.entrySet()) {
        String roleId = entry.getKey();
        for (String userEmail : entry.getValue()) {
          String nativeId = emailToNativeId.get(userEmail);
          if (StringUtils.isBlank(nativeId)) {
            continue;
          }
          ProvisioningMembership membership = new ProvisioningMembership(false);
          membership.setProvisioningGroupId(roleId);
          membership.setProvisioningEntityId(nativeId);
          provisioningMemberships.add(membership);
        }
      }

      targetData.setProvisioningMemberships(provisioningMemberships);

      return response;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveAllData", startNanos));
    }
  }

  // ============================
  // Retrieve single entity (user by email)
  // ============================

  @Override
  public TargetDaoRetrieveEntityResponse retrieveEntity(
      TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {

    long startNanos = System.nanoTime();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();
      TrueFoundrySettings settings = getTrueFoundrySettings();

      String searchAttribute = targetDaoRetrieveEntityRequest.getSearchAttribute();
      String searchValue = GrouperUtil.stringValue(targetDaoRetrieveEntityRequest.getSearchAttributeValue());

      TrueFoundryUser foundUser = null;

      // TrueFoundry supports search by email only (search by ID is not supported)
      if (StringUtils.equals("id", searchAttribute) || StringUtils.equals("email", searchAttribute)) {
        foundUser = TrueFoundryApiCommands.retrieveUserByEmail(configId, settings, searchValue, false);
      }

      ProvisioningEntity targetEntity = foundUser == null ? null : foundUser.toProvisioningEntity();
      return new TargetDaoRetrieveEntityResponse(targetEntity);

    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveEntity", startNanos));
    }
  }

  // ============================
  // Retrieve single group
  // ============================

  @Override
  public TargetDaoRetrieveGroupResponse retrieveGroup(
      TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {

    long startNanos = System.nanoTime();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();
      TrueFoundrySettings settings = getTrueFoundrySettings();

      ProvisioningGroup grouperTargetGroup = targetDaoRetrieveGroupRequest.getTargetGroup();
      String groupType = grouperTargetGroup == null ? null
          : grouperTargetGroup.retrieveAttributeValueString("groupType");
      String searchAttribute = targetDaoRetrieveGroupRequest.getSearchAttribute();
      String searchValue = GrouperUtil.stringValue(targetDaoRetrieveGroupRequest.getSearchAttributeValue());

      TrueFoundryGroup foundGroup = null;

      if (TrueFoundryGroup.GROUP_TYPE_TEAM.equals(groupType)) {
        if (StringUtils.equals("id", searchAttribute)) {
          foundGroup = TrueFoundryApiCommands.getTeamById(configId, settings, searchValue);
        } else {
          // search by name — retrieve all teams and find by name
          List<TrueFoundryGroup> teams = TrueFoundryApiCommands.retrieveTeams(configId, settings);
          for (TrueFoundryGroup team : GrouperUtil.nonNull(teams)) {
            if (StringUtils.equals("name", searchAttribute)
                && StringUtils.equals(team.getName(), searchValue)) {
              foundGroup = team;
              break;
            }
          }
        }
      } else if (TrueFoundryGroup.GROUP_TYPE_ROLE.equals(groupType)) {
        // retrieve all roles and find by id or name
        List<TrueFoundryGroup> roles = TrueFoundryApiCommands.retrieveRoles(configId, settings);
        for (TrueFoundryGroup role : GrouperUtil.nonNull(roles)) {
          if (StringUtils.equals("id", searchAttribute)
              && StringUtils.equals(role.getId(), searchValue)) {
            foundGroup = role;
            break;
          } else if (StringUtils.equals("name", searchAttribute)
              && StringUtils.equals(role.getName(), searchValue)) {
            foundGroup = role;
            break;
          }
        }
      } else {
        throw new RuntimeException("Unknown groupType '" + groupType + "' for retrieveGroup");
      }

      ProvisioningGroup targetGroup = foundGroup == null ? null : foundGroup.toProvisioningGroup();
      return new TargetDaoRetrieveGroupResponse(targetGroup);

    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("retrieveGroup", startNanos));
    }
  }

  // ============================
  // Insert group (teams only — roles are managed in TrueFoundry UI)
  // ============================

  @Override
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoInsertGroupRequest.getTargetGroup();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();
      TrueFoundrySettings settings = getTrueFoundrySettings();

      TrueFoundryGroup trueFoundryGroup = TrueFoundryGroup.fromProvisioningGroup(targetGroup, null);
      String groupType = trueFoundryGroup.getGroupType();

      TrueFoundryGroup createdGroup;

      if (TrueFoundryGroup.GROUP_TYPE_TEAM.equals(groupType)) {
        // create team — memberships are added via insertMembership after creation
        createdGroup = TrueFoundryApiCommands.createTeam(configId, settings, trueFoundryGroup);
      } else if (TrueFoundryGroup.GROUP_TYPE_ROLE.equals(groupType)) {
        // roles are managed in the TrueFoundry UI by administrators
        // creating roles via the provisioner is not supported in normal operation
        createdGroup = TrueFoundryApiCommands.createOrUpdateRole(configId, settings, trueFoundryGroup);
      } else {
        throw new RuntimeException("Invalid groupType: '" + groupType + "', expected 'team' or 'role'");
      }

      if (createdGroup != null && StringUtils.isNotBlank(createdGroup.getId())) {
        targetGroup.setId(createdGroup.getId());
      }
      targetGroup.setProvisioned(true);

      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(
          targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(true);
      }

      return new TargetDaoInsertGroupResponse();
    } catch (Exception e) {
      targetGroup.setProvisioned(false);
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(
          targetGroup.getInternal_objectChanges())) {
        provisioningObjectChange.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertGroup", startNanos));
    }
  }

  // ============================
  // Update group (teams: re-PUT manifest; roles: re-PUT manifest)
  // ============================

  @Override
  public TargetDaoUpdateGroupResponse updateGroup(TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoUpdateGroupRequest.getTargetGroup();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();
      TrueFoundrySettings settings = getTrueFoundrySettings();

      Set<String> fieldNamesToUpdate = new HashSet<String>();
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        if (!StringUtils.isBlank(change.getAttributeName())) {
          fieldNamesToUpdate.add(change.getAttributeName());
        }
      }

      TrueFoundryGroup trueFoundryGroup = TrueFoundryGroup.fromProvisioningGroup(targetGroup,
          fieldNamesToUpdate);
      String groupType = trueFoundryGroup.getGroupType();

      if (TrueFoundryGroup.GROUP_TYPE_TEAM.equals(groupType)) {
        // for teams, re-PUT the manifest to update name or other group-level fields
        // membership changes are handled via insertMembership/deleteMembership
        TrueFoundryApiCommands.updateTeam(configId, settings, trueFoundryGroup);
      } else if (TrueFoundryGroup.GROUP_TYPE_ROLE.equals(groupType)) {
        TrueFoundryApiCommands.createOrUpdateRole(configId, settings, trueFoundryGroup);
      } else {
        throw new RuntimeException("Invalid groupType: '" + groupType + "', expected 'team' or 'role'");
      }

      targetGroup.setProvisioned(true);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        change.setProvisioned(true);
      }

      return new TargetDaoUpdateGroupResponse();
    } catch (Exception e) {
      targetGroup.setProvisioned(false);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        change.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateGroup", startNanos));
    }
  }

  // ============================
  // Delete group (teams: DELETE; roles: DELETE — though roles are normally UI-managed)
  // ============================

  @Override
  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoDeleteGroupRequest.getTargetGroup();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();
      TrueFoundrySettings settings = getTrueFoundrySettings();

      TrueFoundryGroup trueFoundryGroup = TrueFoundryGroup.fromProvisioningGroup(targetGroup, null);
      String groupType = trueFoundryGroup.getGroupType();

      if (TrueFoundryGroup.GROUP_TYPE_TEAM.equals(groupType)) {
        TrueFoundryApiCommands.deleteTeam(configId, settings, trueFoundryGroup.getId());
      } else if (TrueFoundryGroup.GROUP_TYPE_ROLE.equals(groupType)) {
        TrueFoundryApiCommands.deleteRole(configId, settings, trueFoundryGroup.getId());
      } else {
        throw new RuntimeException("Invalid groupType: '" + groupType + "', expected 'team' or 'role'");
      }

      targetGroup.setProvisioned(true);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        change.setProvisioned(true);
      }

      return new TargetDaoDeleteGroupResponse();
    } catch (Exception e) {
      targetGroup.setProvisioned(false);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetGroup.getInternal_objectChanges())) {
        change.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteGroup", startNanos));
    }
  }

  // ============================
  // Insert entity (register user, then look up to get ID)
  // ============================

  @Override
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoInsertEntityRequest.getTargetEntity();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();
      TrueFoundrySettings settings = getTrueFoundrySettings();

      TrueFoundryUser trueFoundryUser = TrueFoundryUser.fromProvisioningEntity(targetEntity, null);
      String email = trueFoundryUser.getEmail();

      if (StringUtils.isBlank(email)) {
        throw new RuntimeException("user email is required for insertEntity");
      }

      // create the user (or reactivate if inactive), set display name if SCIM is configured
      TrueFoundryUser createdUser = TrueFoundryApiCommands.createUser(configId, settings, trueFoundryUser);

      // entity ID = native TrueFoundry user ID
      String nativeId = createdUser != null ? createdUser.getId() : null;
      targetEntity.setId(StringUtils.defaultIfBlank(nativeId, email));
      targetEntity.setProvisioned(true);

      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        change.setProvisioned(true);
      }

      return new TargetDaoInsertEntityResponse();
    } catch (Exception e) {
      targetEntity.setProvisioned(false);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        change.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertEntity", startNanos));
    }
  }

  // ============================
  // Update entity (display name via SCIM, active via deactivate/activate)
  // ============================

  @Override
  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoUpdateEntityRequest.getTargetEntity();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();
      TrueFoundrySettings settings = getTrueFoundrySettings();

      Set<String> fieldNamesToUpdate = new LinkedHashSet<String>();
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        if (!StringUtils.isBlank(change.getAttributeName())) {
          fieldNamesToUpdate.add(change.getAttributeName());
        }
      }

      TrueFoundryUser trueFoundryUser = TrueFoundryUser.fromProvisioningEntity(targetEntity,
          fieldNamesToUpdate);

      // update display name via SCIM PATCH (only if SCIM is configured)
      // Uses the native TrueFoundry user ID (from the entity's id attribute) in the SCIM URL path
      if (fieldNamesToUpdate.contains("displayName") && config.isScimDisplayNameConfigured()) {
        String nativeId = targetEntity.getId();
        if (StringUtils.isNotBlank(nativeId)) {
          TrueFoundryApiCommands.updateUserDisplayName(configId, settings,
              nativeId, trueFoundryUser.getDisplayName());
        }
      }

      // handle active state changes
      if (fieldNamesToUpdate.contains("active")) {
        String email = trueFoundryUser.getEmail();
        if (StringUtils.isNotBlank(email)) {
          Boolean active = trueFoundryUser.getActive();
          if (active != null && active) {
            TrueFoundryApiCommands.activateUser(configId, settings, email);
          } else {
            TrueFoundryApiCommands.deactivateUser(configId, settings, email);
          }
        }
      }

      targetEntity.setProvisioned(true);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        change.setProvisioned(true);
      }

      return new TargetDaoUpdateEntityResponse();
    } catch (Exception e) {
      targetEntity.setProvisioned(false);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        change.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("updateEntity", startNanos));
    }
  }

  // ============================
  // Delete entity (deactivate — hard delete is blocked if user has team memberships)
  // ============================

  @Override
  public TargetDaoDeleteEntityResponse deleteEntity(TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {

    long startNanos = System.nanoTime();
    ProvisioningEntity targetEntity = targetDaoDeleteEntityRequest.getTargetEntity();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();

      TrueFoundrySettings settings = getTrueFoundrySettings();

      TrueFoundryUser trueFoundryUser = TrueFoundryUser.fromProvisioningEntity(targetEntity, null);
      String email = trueFoundryUser.getEmail();

      if (StringUtils.isBlank(email)) {
        throw new RuntimeException("user email is required for deleteEntity");
      }

      // deactivate instead of hard delete — hard delete is blocked if user has team memberships
      TrueFoundryApiCommands.deactivateUser(configId, settings, email);

      targetEntity.setProvisioned(true);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        change.setProvisioned(true);
      }

      return new TargetDaoDeleteEntityResponse();
    } catch (Exception e) {
      targetEntity.setProvisioned(false);
      for (ProvisioningObjectChange change : GrouperUtil.nonNull(targetEntity.getInternal_objectChanges())) {
        change.setProvisioned(false);
      }
      throw e;
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteEntity", startNanos));
    }
  }

  // ============================
  // Insert memberships (plural)
  //   teams: group by teamId, one GET + PUT per team for all members being added
  //   roles: assign role to each user via PATCH /users/roles
  // ============================

  @Override
  public TargetDaoInsertMembershipsResponse insertMemberships(
      TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {

    long startNanos = System.nanoTime();
    List<ProvisioningMembership> targetMemberships =
        targetDaoInsertMembershipsRequest.getTargetMemberships();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();
      TrueFoundrySettings settings = getTrueFoundrySettings();

      // group team memberships by teamId: teamId -> (managerEmails, regularMemberEmails, memberships)
      Map<String, List<String>> teamManagerEmails = new LinkedHashMap<String, List<String>>();
      Map<String, List<String>> teamRegularMemberEmails = new LinkedHashMap<String, List<String>>();
      Map<String, List<ProvisioningMembership>> teamMemberships =
          new LinkedHashMap<String, List<ProvisioningMembership>>();

      for (ProvisioningMembership targetMembership : GrouperUtil.nonNull(targetMemberships)) {
        ProvisioningGroup provisioningGroup = targetMembership.getProvisioningGroup();
        String groupType = provisioningGroup == null ? null
            : provisioningGroup.retrieveAttributeValueString("groupType");
        String groupId = targetMembership.getProvisioningGroupId();
        // entity ID is the native TrueFoundry ID; get the email from the entity's email attribute
        ProvisioningEntity provisioningEntity = targetMembership.getProvisioningEntity();
        String userEmail = provisioningEntity == null ? null
            : provisioningEntity.retrieveAttributeValueString("email");

        if (TrueFoundryGroup.GROUP_TYPE_TEAM.equals(groupType)) {
          boolean isManager = false;
          if (config.isTrueFoundryAddTeamManagerMetadata() && provisioningGroup != null) {
            // The translator populates the target group's "managers" attribute
            // with the set of native TrueFoundry user IDs that should be managers
            // (derived from the md_trueFoundryManagerGroupName group metadata).
            // Check whether this membership's native entity ID is in that set.
            Set<?> managerEntityIds = provisioningGroup.retrieveAttributeValueSet("managers");
            String nativeEntityId = targetMembership.getProvisioningEntityId();
            if (managerEntityIds != null && nativeEntityId != null
                && managerEntityIds.contains(nativeEntityId)) {
              isManager = true;
            }
          }

          if (!teamMemberships.containsKey(groupId)) {
            teamManagerEmails.put(groupId, new ArrayList<String>());
            teamRegularMemberEmails.put(groupId, new ArrayList<String>());
            teamMemberships.put(groupId, new ArrayList<ProvisioningMembership>());
          }
          if (isManager) {
            teamManagerEmails.get(groupId).add(userEmail);
          } else {
            teamRegularMemberEmails.get(groupId).add(userEmail);
          }
          teamMemberships.get(groupId).add(targetMembership);

        } else if (TrueFoundryGroup.GROUP_TYPE_ROLE.equals(groupType)) {
          // role assignment — no batch API, process individually
          String roleName = provisioningGroup.retrieveAttributeValueString("name");
          try {
            assignUserRoleTracked(configId, settings, userEmail, roleName);
            targetMembership.setProvisioned(true);
            for (ProvisioningObjectChange change : GrouperUtil.nonNull(
                targetMembership.getInternal_objectChanges())) {
              change.setProvisioned(true);
            }
          } catch (Exception e) {
            targetMembership.setProvisioned(false);
            for (ProvisioningObjectChange change : GrouperUtil.nonNull(
                targetMembership.getInternal_objectChanges())) {
              change.setProvisioned(false);
            }
          }

        } else {
          throw new RuntimeException(
              "Invalid groupType: '" + groupType + "', expected 'team' or 'role'");
        }
      }

      // process each team with a single GET + PUT
      for (String teamId : teamMemberships.keySet()) {
        List<ProvisioningMembership> memberships = teamMemberships.get(teamId);
        try {
          TrueFoundryApiCommands.addTeamMembers(configId, settings, teamId,
              teamManagerEmails.get(teamId), teamRegularMemberEmails.get(teamId));
          for (ProvisioningMembership m : memberships) {
            m.setProvisioned(true);
            for (ProvisioningObjectChange change : GrouperUtil.nonNull(
                m.getInternal_objectChanges())) {
              change.setProvisioned(true);
            }
          }
        } catch (Exception e) {
          for (ProvisioningMembership m : memberships) {
            m.setProvisioned(false);
            for (ProvisioningObjectChange change : GrouperUtil.nonNull(
                m.getInternal_objectChanges())) {
              change.setProvisioned(false);
            }
          }
        }
      }

      return new TargetDaoInsertMembershipsResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("insertMemberships", startNanos));
    }
  }

  // ============================
  // Delete memberships (plural)
  //   teams: group by teamId, one GET + PUT per team for all members being removed
  //   roles: assign default role to each user via PATCH /users/roles
  // ============================

  @Override
  public TargetDaoDeleteMembershipsResponse deleteMemberships(
      TargetDaoDeleteMembershipsRequest targetDaoDeleteMembershipsRequest) {

    long startNanos = System.nanoTime();
    List<ProvisioningMembership> targetMemberships =
        targetDaoDeleteMembershipsRequest.getTargetMemberships();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();
      TrueFoundrySettings settings = getTrueFoundrySettings();

      // group team memberships by teamId: teamId -> (emailsToRemove, memberships)
      Map<String, List<String>> teamEmailsToRemove = new LinkedHashMap<String, List<String>>();
      Map<String, List<ProvisioningMembership>> teamMemberships =
          new LinkedHashMap<String, List<ProvisioningMembership>>();

      for (ProvisioningMembership targetMembership : GrouperUtil.nonNull(targetMemberships)) {
        ProvisioningGroup provisioningGroup = targetMembership.getProvisioningGroup();
        String groupType = provisioningGroup == null ? null
            : provisioningGroup.retrieveAttributeValueString("groupType");
        String groupId = targetMembership.getProvisioningGroupId();
        // entity ID is the native TrueFoundry ID; get the email from the entity's email attribute
        ProvisioningEntity provisioningEntity = targetMembership.getProvisioningEntity();
        String userEmail = provisioningEntity == null ? null
            : provisioningEntity.retrieveAttributeValueString("email");

        if (TrueFoundryGroup.GROUP_TYPE_TEAM.equals(groupType)) {
          if (!teamMemberships.containsKey(groupId)) {
            teamEmailsToRemove.put(groupId, new ArrayList<String>());
            teamMemberships.put(groupId, new ArrayList<ProvisioningMembership>());
          }
          teamEmailsToRemove.get(groupId).add(userEmail);
          teamMemberships.get(groupId).add(targetMembership);

        } else if (TrueFoundryGroup.GROUP_TYPE_ROLE.equals(groupType)) {
          // TrueFoundry users always have exactly one role. A role delete means the user
          // should no longer have this role; fall back to the configured default role unless
          // an insert earlier in this sync already assigned them a different role.
          if (!StringUtils.isBlank(userEmail)
              && !this.usersAssignedRoleThisSync.contains(userEmail)) {
            String defaultRole = config.getTrueFoundryDefaultRole();
            if (!StringUtils.isBlank(defaultRole)) {
              try {
                assignUserRoleTracked(configId, settings, userEmail, defaultRole);
              } catch (Exception e) {
                targetMembership.setProvisioned(false);
                for (ProvisioningObjectChange change : GrouperUtil.nonNull(
                    targetMembership.getInternal_objectChanges())) {
                  change.setProvisioned(false);
                }
                continue;
              }
            }
          }
          targetMembership.setProvisioned(true);
          for (ProvisioningObjectChange change : GrouperUtil.nonNull(
              targetMembership.getInternal_objectChanges())) {
            change.setProvisioned(true);
          }

        } else {
          throw new RuntimeException(
              "Invalid groupType: '" + groupType + "' for deleteMemberships"
              + ", provisioningGroup=" + (provisioningGroup == null ? "null" : provisioningGroup.toString())
              + ", groupId='" + groupId + "', userEmail='" + userEmail + "'"
              + ", expected 'team' or 'role'");
        }
      }

      // process each team with a single GET + PUT
      for (String teamId : teamMemberships.keySet()) {
        List<ProvisioningMembership> memberships = teamMemberships.get(teamId);
        try {
          TrueFoundryApiCommands.removeTeamMembers(configId, settings, teamId,
              teamEmailsToRemove.get(teamId));
          for (ProvisioningMembership m : memberships) {
            m.setProvisioned(true);
            for (ProvisioningObjectChange change : GrouperUtil.nonNull(
                m.getInternal_objectChanges())) {
              change.setProvisioned(true);
            }
          }
        } catch (Exception e) {
          for (ProvisioningMembership m : memberships) {
            m.setProvisioned(false);
            for (ProvisioningObjectChange change : GrouperUtil.nonNull(
                m.getInternal_objectChanges())) {
              change.setProvisioned(false);
            }
          }
        }
      }

      return new TargetDaoDeleteMembershipsResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("deleteMemberships", startNanos));
    }
  }

  // ============================
  // Replace all memberships for a group (called when replaceMemberships=true on full sync)
  //   teams: compute the full member/manager split from the supplied list and PUT the manifest
  //   roles: assign the role to each user via PATCH /users/roles (TF users have at most one role)
  // ============================

  @Override
  public TargetDaoReplaceGroupMembershipsResponse replaceGroupMemberships(
      TargetDaoReplaceGroupMembershipsRequest targetDaoReplaceGroupMembershipsRequest) {

    long startNanos = System.nanoTime();
    ProvisioningGroup targetGroup = targetDaoReplaceGroupMembershipsRequest.getTargetGroup();
    List<ProvisioningMembership> targetMemberships =
        targetDaoReplaceGroupMembershipsRequest.getTargetMemberships();

    try {
      TrueFoundryProvisionerConfiguration config = getTrueFoundryConfiguration();
      String configId = config.getTrueFoundryExternalSystemConfigId();
      TrueFoundrySettings settings = getTrueFoundrySettings();

      String groupType = targetGroup == null ? null
          : targetGroup.retrieveAttributeValueString("groupType");

      if (TrueFoundryGroup.GROUP_TYPE_TEAM.equals(groupType)) {
        String teamId = targetGroup.getId();

        // The translator populates the target group's "managers" attribute with the set of
        // native TrueFoundry user IDs that should be managers (derived from the
        // md_trueFoundryManagerGroupName group metadata).
        Set<?> managerEntityIds = null;
        if (config.isTrueFoundryAddTeamManagerMetadata()) {
          managerEntityIds = targetGroup.retrieveAttributeValueSet("managers");
        }

        List<String> memberEmails = new ArrayList<String>();
        List<String> managerEmails = new ArrayList<String>();

        for (ProvisioningMembership targetMembership : GrouperUtil.nonNull(targetMemberships)) {
          ProvisioningEntity provisioningEntity = targetMembership.getProvisioningEntity();
          String userEmail = provisioningEntity == null ? null
              : provisioningEntity.retrieveAttributeValueString("email");
          if (StringUtils.isBlank(userEmail)) {
            continue;
          }
          memberEmails.add(userEmail);

          if (managerEntityIds != null) {
            String nativeEntityId = targetMembership.getProvisioningEntityId();
            if (nativeEntityId != null && managerEntityIds.contains(nativeEntityId)) {
              managerEmails.add(userEmail);
            }
          }
        }

        try {
          TrueFoundryApiCommands.replaceTeamMembers(configId, settings, teamId,
              memberEmails, managerEmails.isEmpty() ? null : managerEmails);
          for (ProvisioningMembership m : GrouperUtil.nonNull(targetMemberships)) {
            m.setProvisioned(true);
            for (ProvisioningObjectChange change : GrouperUtil.nonNull(
                m.getInternal_objectChanges())) {
              change.setProvisioned(true);
            }
          }
        } catch (Exception e) {
          for (ProvisioningMembership m : GrouperUtil.nonNull(targetMemberships)) {
            m.setProvisioned(false);
            for (ProvisioningObjectChange change : GrouperUtil.nonNull(
                m.getInternal_objectChanges())) {
              change.setProvisioned(false);
            }
          }
          throw e;
        }

      } else if (TrueFoundryGroup.GROUP_TYPE_ROLE.equals(groupType)) {
        // roles: each user has exactly one role; assign this role to each listed user.
        // After assigning the desired users, any user who had this role at the start of the
        // sync but isn't in the desired list — and hasn't been assigned a different role
        // earlier in this sync — is demoted to the configured default role.
        String roleName = targetGroup.retrieveAttributeValueString("name");
        String roleId = targetGroup.getId();

        Set<String> desiredUserEmails = new LinkedHashSet<String>();
        for (ProvisioningMembership targetMembership : GrouperUtil.nonNull(targetMemberships)) {
          ProvisioningEntity provisioningEntity = targetMembership.getProvisioningEntity();
          String userEmail = provisioningEntity == null ? null
              : provisioningEntity.retrieveAttributeValueString("email");
          if (StringUtils.isBlank(userEmail)) {
            continue;
          }
          desiredUserEmails.add(userEmail);
          try {
            assignUserRoleTracked(configId, settings, userEmail, roleName);
            targetMembership.setProvisioned(true);
            for (ProvisioningObjectChange change : GrouperUtil.nonNull(
                targetMembership.getInternal_objectChanges())) {
              change.setProvisioned(true);
            }
          } catch (Exception e) {
            targetMembership.setProvisioned(false);
            for (ProvisioningObjectChange change : GrouperUtil.nonNull(
                targetMembership.getInternal_objectChanges())) {
              change.setProvisioned(false);
            }
          }
        }

        // demote users leaving this role to the default role
        String defaultRole = config.getTrueFoundryDefaultRole();
        if (!StringUtils.isBlank(defaultRole) && roleId != null) {
          Set<String> startOfSyncUsers = this.startOfSyncRoleIdToUserEmails.get(roleId);
          if (startOfSyncUsers != null) {
            for (String startUserEmail : startOfSyncUsers) {
              if (StringUtils.isBlank(startUserEmail)) {
                continue;
              }
              if (desiredUserEmails.contains(startUserEmail)) {
                continue;
              }
              // skip if an earlier replace/insert already assigned this user a role
              if (this.usersAssignedRoleThisSync.contains(startUserEmail)) {
                continue;
              }
              try {
                assignUserRoleTracked(configId, settings, startUserEmail, defaultRole);
              } catch (Exception e) {
                // log and continue — this demotion is best-effort
                LOG.warn("TrueFoundry: failed to demote user '" + startUserEmail
                    + "' from role '" + roleName + "' to default role '" + defaultRole + "'", e);
              }
            }
          }
        }

      } else {
        throw new RuntimeException(
            "Invalid groupType: '" + groupType + "' for replaceGroupMemberships, expected 'team' or 'role'");
      }

      return new TargetDaoReplaceGroupMembershipsResponse();
    } finally {
      this.addTargetDaoTimingInfo(new TargetDaoTimingInfo("replaceGroupMemberships", startNanos));
    }
  }

  // ============================
  // DAO capabilities
  // ============================

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);
    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMemberships(true);
    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);
    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertMemberships(true);
    grouperProvisionerDaoCapabilities.setCanReplaceGroupMemberships(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllData(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
  }

}
